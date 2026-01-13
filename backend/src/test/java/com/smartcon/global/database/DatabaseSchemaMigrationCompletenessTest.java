package com.smartcon.global.database;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터베이스 스키마 마이그레이션 완성도 검증 Property-Based Test
 * 
 * **Property 17: Database Schema Migration Completeness**
 * **Validates: Requirements 5.2**
 * 
 * 이 테스트는 마이그레이션 실행 후 모든 필요한 테이블, 인덱스, 
 * 외래키 제약조건이 올바르게 생성되었는지 검증합니다.
 */
class DatabaseSchemaMigrationCompletenessTest {

    private static Flyway flyway;
    private static DataSource dataSource;

    @BeforeProperty
    void setUp() {
        // 로컬 MariaDB 데이터소스 설정
        dataSource = DataSourceBuilder.create()
                .driverClassName("org.mariadb.jdbc.Driver")
                .url("jdbc:mariadb://localhost:3306/smartcon_test")
                .username("smartcon_user")
                .password("smartcon_pass")
                .build();
        
        // Flyway 인스턴스 생성 및 설정
        flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateOnMigrate(false)
                .cleanDisabled(false)
                .load();
        
        // 데이터베이스 초기화 및 마이그레이션 실행
        initializeDatabaseWithMigration();
    }

    /**
     * 데이터베이스 초기화 및 마이그레이션 실행
     */
    private void initializeDatabaseWithMigration() {
        try {
            // Flyway clean으로 완전 초기화
            flyway.clean();
            
            // 마이그레이션 실행
            var result = flyway.migrate();
            
            if (!result.success) {
                throw new RuntimeException("마이그레이션 실행 실패");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("데이터베이스 초기화 실패", e);
        }
    }

    /**
     * Property 17: Database Schema Migration Completeness
     * 
     * *For any* migration execution, all necessary tables, indexes, 
     * and foreign key constraints should be created correctly in MariaDB
     * 
     * **Validates: Requirements 5.2**
     */
    @Property(tries = 100)
    @Label("Property 17: 데이터베이스 스키마 마이그레이션 완성도")
    void databaseSchemaMigrationCompletenessProperty(@ForAll("schemaValidationScenarios") SchemaValidationScenario scenario) {
        // Given: 마이그레이션이 실행된 데이터베이스
        
        // When & Then: 스키마 완성도 검증
        switch (scenario.validationType) {
            case CORE_TABLES:
                verifyCoreTablesExist(scenario.expectedElements);
                break;
            case TABLE_INDEXES:
                verifyTableIndexesExist(scenario.expectedElements);
                break;
            case FOREIGN_KEY_CONSTRAINTS:
                verifyForeignKeyConstraintsExist(scenario.expectedElements);
                break;
            case TABLE_COLUMNS:
                verifyTableColumnsExist(scenario.expectedElements);
                break;
            case CHECK_CONSTRAINTS:
                verifyCheckConstraintsExist(scenario.expectedElements);
                break;
        }
        
        // And: 데이터베이스 연결이 정상적으로 작동해야 함
        verifyDatabaseConnectivity();
    }

    @Provide
    Arbitrary<SchemaValidationScenario> schemaValidationScenarios() {
        return Arbitraries.of(
            // 핵심 테이블 존재 검증
            new SchemaValidationScenario(
                ValidationType.CORE_TABLES,
                "Core Tables Validation",
                Set.of("tenants", "users", "user_roles", "subscription_billing", "attendance_logs")
            ),
            
            // 테이블 인덱스 존재 검증
            new SchemaValidationScenario(
                ValidationType.TABLE_INDEXES,
                "Table Indexes Validation",
                Set.of("idx_tenants_business_number", "idx_users_tenant_id", "idx_users_email", 
                       "idx_billing_tenant_status", "idx_attendance_tenant_date")
            ),
            
            // 외래키 제약조건 검증
            new SchemaValidationScenario(
                ValidationType.FOREIGN_KEY_CONSTRAINTS,
                "Foreign Key Constraints Validation",
                Set.of("users.tenant_id->tenants.id", "user_roles.user_id->users.id", 
                       "subscription_billing.tenant_id->tenants.id", "attendance_logs.tenant_id->tenants.id")
            ),
            
            // 필수 컬럼 존재 검증
            new SchemaValidationScenario(
                ValidationType.TABLE_COLUMNS,
                "Essential Columns Validation",
                Set.of("tenants.business_number", "tenants.status", "users.email", 
                       "users.tenant_id", "subscription_billing.payment_status")
            ),
            
            // 체크 제약조건 검증
            new SchemaValidationScenario(
                ValidationType.CHECK_CONSTRAINTS,
                "Check Constraints Validation",
                Set.of("chk_tenants_max_sites", "chk_users_email_format", 
                       "chk_billing_amounts_positive", "chk_attendance_man_day")
            )
        );
    }

    /**
     * 핵심 테이블들이 존재하는지 검증
     */
    private void verifyCoreTablesExist(Set<String> expectedTables) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (String tableName : expectedTables) {
                try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    assertThat(tables.next())
                        .as("핵심 테이블 '%s'이(가) 존재해야 함", tableName)
                        .isTrue();
                        
                    // 테이블이 InnoDB 엔진을 사용하는지 확인
                    verifyTableEngine(connection, tableName, "InnoDB");
                    
                    // 테이블이 utf8mb4 문자셋을 사용하는지 확인
                    verifyTableCharset(connection, tableName, "utf8mb4");
                }
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("핵심 테이블 존재 여부 확인 중 오류 발생", e);
        }
    }

    /**
     * 테이블 인덱스들이 존재하는지 검증
     */
    private void verifyTableIndexesExist(Set<String> expectedIndexes) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Set<String> existingIndexes = new HashSet<>();
            
            // 모든 테이블의 인덱스 정보 수집
            String[] tables = {"tenants", "users", "user_roles", "subscription_billing", "attendance_logs"};
            for (String tableName : tables) {
                try (ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false)) {
                    while (indexes.next()) {
                        String indexName = indexes.getString("INDEX_NAME");
                        if (indexName != null && !indexName.equals("PRIMARY")) {
                            existingIndexes.add(indexName);
                        }
                    }
                }
            }
            
            // 예상된 인덱스들이 존재하는지 확인
            for (String expectedIndex : expectedIndexes) {
                assertThat(existingIndexes)
                    .as("인덱스 '%s'이(가) 존재해야 함", expectedIndex)
                    .contains(expectedIndex);
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("테이블 인덱스 존재 여부 확인 중 오류 발생", e);
        }
    }

    /**
     * 외래키 제약조건들이 존재하는지 검증
     */
    private void verifyForeignKeyConstraintsExist(Set<String> expectedConstraints) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Set<String> existingConstraints = new HashSet<>();
            
            // 모든 테이블의 외래키 정보 수집
            String[] tables = {"users", "user_roles", "subscription_billing", "attendance_logs"};
            for (String tableName : tables) {
                try (ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName)) {
                    while (foreignKeys.next()) {
                        String fkTableName = foreignKeys.getString("FKTABLE_NAME");
                        String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                        String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                        String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                        
                        String constraintKey = fkTableName + "." + fkColumnName + "->" + pkTableName + "." + pkColumnName;
                        existingConstraints.add(constraintKey);
                    }
                }
            }
            
            // 예상된 외래키 제약조건들이 존재하는지 확인
            for (String expectedConstraint : expectedConstraints) {
                assertThat(existingConstraints)
                    .as("외래키 제약조건 '%s'이(가) 존재해야 함", expectedConstraint)
                    .contains(expectedConstraint);
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("외래키 제약조건 존재 여부 확인 중 오류 발생", e);
        }
    }

    /**
     * 필수 테이블 컬럼들이 존재하는지 검증
     */
    private void verifyTableColumnsExist(Set<String> expectedColumns) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (String columnSpec : expectedColumns) {
                String[] parts = columnSpec.split("\\.");
                if (parts.length != 2) continue;
                
                String tableName = parts[0];
                String columnName = parts[1];
                
                boolean columnExists = false;
                try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
                    if (columns.next()) {
                        columnExists = true;
                        
                        // 컬럼 타입 및 제약조건 검증
                        verifyColumnProperties(columns, tableName, columnName);
                    }
                }
                
                assertThat(columnExists)
                    .as("테이블 '%s'의 컬럼 '%s'이(가) 존재해야 함", tableName, columnName)
                    .isTrue();
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("테이블 컬럼 존재 여부 확인 중 오류 발생", e);
        }
    }

    /**
     * 체크 제약조건들이 존재하는지 검증
     */
    private void verifyCheckConstraintsExist(Set<String> expectedConstraints) {
        try (Connection connection = dataSource.getConnection()) {
            Set<String> existingConstraints = new HashSet<>();
            
            // MariaDB의 체크 제약조건 조회
            String query = """
                SELECT CONSTRAINT_NAME 
                FROM information_schema.CHECK_CONSTRAINTS 
                WHERE CONSTRAINT_SCHEMA = 'smartcon_test'
                """;
                
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    existingConstraints.add(resultSet.getString("CONSTRAINT_NAME"));
                }
            }
            
            // 예상된 체크 제약조건들이 존재하는지 확인
            for (String expectedConstraint : expectedConstraints) {
                assertThat(existingConstraints)
                    .as("체크 제약조건 '%s'이(가) 존재해야 함", expectedConstraint)
                    .contains(expectedConstraint);
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("체크 제약조건 존재 여부 확인 중 오류 발생", e);
        }
    }

    /**
     * 테이블 엔진 검증
     */
    private void verifyTableEngine(Connection connection, String tableName, String expectedEngine) {
        try {
            String query = """
                SELECT ENGINE 
                FROM information_schema.TABLES 
                WHERE TABLE_SCHEMA = 'smartcon_test' AND TABLE_NAME = ?
                """;
                
            try (var statement = connection.prepareStatement(query)) {
                statement.setString(1, tableName);
                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String actualEngine = resultSet.getString("ENGINE");
                        assertThat(actualEngine)
                            .as("테이블 '%s'의 엔진이 '%s'이어야 함", tableName, expectedEngine)
                            .isEqualToIgnoringCase(expectedEngine);
                    }
                }
            }
        } catch (SQLException e) {
            // 엔진 검증 실패는 경고로만 처리
            System.out.println("테이블 엔진 검증 실패 (무시됨): " + tableName + " - " + e.getMessage());
        }
    }

    /**
     * 테이블 문자셋 검증
     */
    private void verifyTableCharset(Connection connection, String tableName, String expectedCharset) {
        try {
            String query = """
                SELECT TABLE_COLLATION 
                FROM information_schema.TABLES 
                WHERE TABLE_SCHEMA = 'smartcon_test' AND TABLE_NAME = ?
                """;
                
            try (var statement = connection.prepareStatement(query)) {
                statement.setString(1, tableName);
                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String collation = resultSet.getString("TABLE_COLLATION");
                        assertThat(collation)
                            .as("테이블 '%s'의 문자셋이 '%s' 기반이어야 함", tableName, expectedCharset)
                            .startsWith(expectedCharset);
                    }
                }
            }
        } catch (SQLException e) {
            // 문자셋 검증 실패는 경고로만 처리
            System.out.println("테이블 문자셋 검증 실패 (무시됨): " + tableName + " - " + e.getMessage());
        }
    }

    /**
     * 컬럼 속성 검증
     */
    private void verifyColumnProperties(ResultSet columns, String tableName, String columnName) throws SQLException {
        String columnType = columns.getString("TYPE_NAME");
        int nullable = columns.getInt("NULLABLE");
        
        // 중요 컬럼들의 속성 검증
        if (columnName.equals("id")) {
            assertThat(columnType.toUpperCase())
                .as("ID 컬럼은 BIGINT 타입이어야 함")
                .contains("BIGINT");
        }
        
        if (columnName.equals("email")) {
            assertThat(nullable)
                .as("이메일 컬럼은 NOT NULL이어야 함")
                .isEqualTo(DatabaseMetaData.columnNoNulls);
        }
        
        if (columnName.equals("business_number")) {
            assertThat(nullable)
                .as("사업자번호 컬럼은 NOT NULL이어야 함")
                .isEqualTo(DatabaseMetaData.columnNoNulls);
        }
    }

    /**
     * 데이터베이스 연결성 검증
     */
    private void verifyDatabaseConnectivity() {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1))
                .as("데이터베이스 연결이 유효해야 함")
                .isTrue();
                
            // 간단한 쿼리 실행 테스트
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1");
            assertThat(resultSet.next())
                .as("기본 쿼리가 실행되어야 함")
                .isTrue();
        } catch (SQLException e) {
            throw new RuntimeException("데이터베이스 연결성 확인 중 오류 발생", e);
        }
    }

    /**
     * 스키마 검증 시나리오 데이터 클래스
     */
    static class SchemaValidationScenario {
        final ValidationType validationType;
        final String description;
        final Set<String> expectedElements;

        SchemaValidationScenario(ValidationType validationType, String description, Set<String> expectedElements) {
            this.validationType = validationType;
            this.description = description;
            this.expectedElements = expectedElements;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * 검증 타입 열거형
     */
    enum ValidationType {
        CORE_TABLES,
        TABLE_INDEXES,
        FOREIGN_KEY_CONSTRAINTS,
        TABLE_COLUMNS,
        CHECK_CONSTRAINTS
    }
}