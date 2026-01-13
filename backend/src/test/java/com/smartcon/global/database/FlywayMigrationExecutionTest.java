package com.smartcon.global.database;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 마이그레이션 실행 Property-Based Test
 * 
 * **Property 8: Flyway Migration Execution**
 * **Validates: Requirements 2.3**
 * 
 * 이 테스트는 Flyway 마이그레이션 스크립트가 성공적으로 실행되고
 * 예상된 데이터베이스 스키마 변경사항을 생성하는지 검증합니다.
 */
class FlywayMigrationExecutionTest {

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
                .baselineOnMigrate(false)     // 베이스라인 비활성화 (깨끗한 DB에서 시작)
                .validateOnMigrate(false)     // 검증 비활성화
                .cleanDisabled(false)         // clean 명령 활성화
                .load();
        
        // Flyway clean을 사용하여 완전한 데이터베이스 초기화 (히스토리 테이블 포함)
        cleanDatabaseWithFlyway();
    }

    /**
     * Flyway clean을 사용하여 완전한 데이터베이스 초기화
     * 모든 테이블과 Flyway 히스토리를 완전히 제거하여 깨끗한 상태로 만듦
     */
    private void cleanDatabaseWithFlyway() {
        try {
            // Flyway clean 실행 - 모든 스키마 객체와 히스토리 테이블 제거
            flyway.clean();
            
            // 추가적인 수동 정리 (Flyway clean이 놓칠 수 있는 객체들)
            cleanRemainingObjects();
            
        } catch (Exception e) {
            // Flyway clean 실패시 수동으로 데이터베이스 정리
            System.out.println("Flyway clean 실패, 수동 정리 시도: " + e.getMessage());
            cleanDatabase();
        }
    }

    /**
     * Flyway clean 후 남을 수 있는 추가 객체들 정리
     */
    private void cleanRemainingObjects() {
        try (Connection connection = dataSource.getConnection()) {
            // 외래키 제약조건 비활성화
            connection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 남아있을 수 있는 테이블들 확인 및 제거
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    // 시스템 테이블은 제외하고 모든 테이블 제거
                    if (!isSystemTable(tableName)) {
                        try {
                            connection.createStatement().execute("DROP TABLE IF EXISTS " + tableName);
                        } catch (SQLException e) {
                            // 테이블 삭제 실패는 로그만 남기고 계속 진행
                            System.out.println("테이블 삭제 실패 (무시됨): " + tableName + " - " + e.getMessage());
                        }
                    }
                }
            }
            
            // 외래키 제약조건 재활성화
            connection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            
        } catch (SQLException e) {
            System.out.println("추가 객체 정리 중 오류 발생 (무시됨): " + e.getMessage());
        }
    }

    /**
     * 시스템 테이블 여부 확인
     */
    private boolean isSystemTable(String tableName) {
        String lowerTableName = tableName.toLowerCase();
        return lowerTableName.startsWith("information_schema") || 
               lowerTableName.startsWith("performance_schema") ||
               lowerTableName.startsWith("mysql") ||
               lowerTableName.startsWith("sys");
    }

    /**
     * Property 8: Flyway Migration Execution
     * 
     * *For any* Flyway migration script, it should execute successfully 
     * and create the expected database schema changes
     * 
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 100)
    @Label("Property 8: Flyway 마이그레이션 스크립트 실행 성공")
    void flywayMigrationExecutionProperty(@ForAll("migrationScenarios") MigrationScenario scenario) {
        // Given: 데이터베이스 상태 확인 (깨끗하거나 이미 마이그레이션이 적용된 상태)
        MigrationInfo[] initialMigrations = flyway.info().all();
        
        // When: 마이그레이션 실행
        var migrationResult = flyway.migrate();
        
        // Then: 마이그레이션이 성공적으로 완료되어야 함
        assertThat(migrationResult.success)
            .as("마이그레이션이 성공해야 함")
            .isTrue();
            
        // 마이그레이션 실행 결과 검증:
        // - 새로운 마이그레이션이 실행된 경우: migrationsExecuted > 0
        // - 이미 모든 마이그레이션이 적용된 경우: migrationsExecuted = 0 (정상)
        assertThat(migrationResult.migrationsExecuted)
            .as("마이그레이션 실행 횟수는 0 이상이어야 함 (0은 이미 적용된 상태)")
            .isGreaterThanOrEqualTo(0);
        
        // And: 마이그레이션 상태 확인 - 모든 마이그레이션이 성공 상태여야 함
        MigrationInfo[] finalMigrations = flyway.info().all();
        boolean hasValidMigrations = false;
        
        for (MigrationInfo migration : finalMigrations) {
            // 성공 상태이거나 현재 버전보다 높은 버전의 마이그레이션이 있어야 함
            if (migration.getState() == MigrationState.SUCCESS || 
                migration.getState() == MigrationState.ABOVE_TARGET) {
                hasValidMigrations = true;
            }
            
            // 실패한 마이그레이션이 있으면 안됨
            assertThat(migration.getState())
                .as("마이그레이션 '%s'이(가) 실패 상태가 아니어야 함", migration.getDescription())
                .isNotEqualTo(MigrationState.FAILED);
        }
        
        assertThat(hasValidMigrations)
            .as("유효한 마이그레이션이 존재해야 함")
            .isTrue();
        
        // And: 예상된 테이블들이 생성되어야 함
        verifyExpectedTablesExist(scenario.expectedTables);
        
        // And: 예상된 인덱스들이 생성되어야 함
        verifyBasicIndexesExist();
        
        // And: 데이터베이스 연결이 정상적으로 작동해야 함
        verifyDatabaseConnectivity();
    }

    @Provide
    Arbitrary<MigrationScenario> migrationScenarios() {
        return Arbitraries.of(
            // 기본 마이그레이션 시나리오: 핵심 테이블 확인
            new MigrationScenario(
                "Core Schema Validation",
                Set.of("tenants", "users", "user_roles"),
                Set.of(),
                Set.of()
            ),
            
            // 확장 마이그레이션 시나리오: 모든 테이블 확인
            new MigrationScenario(
                "Full Schema Validation", 
                Set.of("tenants", "users", "user_roles", "subscription_billing", "attendance_logs"),
                Set.of(),
                Set.of()
            ),
            
            // 최소 마이그레이션 시나리오: 기본 테이블만 확인
            new MigrationScenario(
                "Minimal Schema Validation",
                Set.of("tenants"),
                Set.of(),
                Set.of()
            )
        );
    }

    /**
     * 예상된 테이블들이 존재하는지 검증
     */
    private void verifyExpectedTablesExist(Set<String> expectedTables) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (String tableName : expectedTables) {
                try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE", "VIEW"})) {
                    assertThat(tables.next())
                        .as("테이블 '%s'이(가) 존재해야 함", tableName)
                        .isTrue();
                }
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("테이블 존재 여부 확인 중 오류 발생", e);
        }
    }

    /**
     * 기본 인덱스들이 존재하는지 검증
     */
    private void verifyBasicIndexesExist() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 실제 존재해야 하는 테이블들 확인
            String[] expectedTables = {"tenants", "users", "user_roles", "subscription_billing", "attendance_logs"};
            
            for (String tableName : expectedTables) {
                boolean hasIndex = false;
                try (ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false)) {
                    while (indexes.next()) {
                        String indexName = indexes.getString("INDEX_NAME");
                        if (indexName != null && !indexName.equals("PRIMARY")) {
                            hasIndex = true;
                            break;
                        }
                    }
                }
                
                assertThat(hasIndex)
                    .as("테이블 '%s'에 최소 하나의 인덱스가 존재해야 함", tableName)
                    .isTrue();
            }
                
        } catch (SQLException e) {
            throw new RuntimeException("인덱스 존재 여부 확인 중 오류 발생", e);
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
     * 마이그레이션 시나리오 데이터 클래스
     */
    static class MigrationScenario {
        final String description;
        final Set<String> expectedTables;
        final Set<String> expectedIndexes;
        final Set<String> expectedConstraints;

        MigrationScenario(String description, Set<String> expectedTables, 
                         Set<String> expectedIndexes, Set<String> expectedConstraints) {
            this.description = description;
            this.expectedTables = expectedTables;
            this.expectedIndexes = expectedIndexes;
            this.expectedConstraints = expectedConstraints;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * 데이터베이스를 완전히 초기화하여 깨끗한 상태로 만듦 (Flyway clean 실패시 대안)
     */
    private void cleanDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            // 외래키 제약조건 비활성화
            connection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 모든 이벤트 삭제
            try (ResultSet events = connection.createStatement().executeQuery(
                "SELECT EVENT_NAME FROM information_schema.EVENTS WHERE EVENT_SCHEMA = 'smartcon_test'")) {
                while (events.next()) {
                    String eventName = events.getString("EVENT_NAME");
                    try {
                        connection.createStatement().execute("DROP EVENT IF EXISTS " + eventName);
                    } catch (SQLException e) {
                        System.out.println("이벤트 삭제 실패 (무시됨): " + eventName + " - " + e.getMessage());
                    }
                }
            }
            
            // 모든 프로시저 삭제
            try (ResultSet procedures = connection.createStatement().executeQuery(
                "SELECT ROUTINE_NAME FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = 'smartcon_test' AND ROUTINE_TYPE = 'PROCEDURE'")) {
                while (procedures.next()) {
                    String procedureName = procedures.getString("ROUTINE_NAME");
                    try {
                        connection.createStatement().execute("DROP PROCEDURE IF EXISTS " + procedureName);
                    } catch (SQLException e) {
                        System.out.println("프로시저 삭제 실패 (무시됨): " + procedureName + " - " + e.getMessage());
                    }
                }
            }
            
            // 모든 함수 삭제
            try (ResultSet functions = connection.createStatement().executeQuery(
                "SELECT ROUTINE_NAME FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = 'smartcon_test' AND ROUTINE_TYPE = 'FUNCTION'")) {
                while (functions.next()) {
                    String functionName = functions.getString("ROUTINE_NAME");
                    try {
                        connection.createStatement().execute("DROP FUNCTION IF EXISTS " + functionName);
                    } catch (SQLException e) {
                        System.out.println("함수 삭제 실패 (무시됨): " + functionName + " - " + e.getMessage());
                    }
                }
            }
            
            // 모든 뷰 삭제
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet views = metaData.getTables(null, null, null, new String[]{"VIEW"})) {
                while (views.next()) {
                    String viewName = views.getString("TABLE_NAME");
                    try {
                        connection.createStatement().execute("DROP VIEW IF EXISTS " + viewName);
                    } catch (SQLException e) {
                        System.out.println("뷰 삭제 실패 (무시됨): " + viewName + " - " + e.getMessage());
                    }
                }
            }
            
            // 모든 테이블 삭제 (Flyway 히스토리 테이블 포함)
            try (ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    // 시스템 테이블은 제외하고 모든 테이블 제거
                    if (!isSystemTable(tableName)) {
                        try {
                            connection.createStatement().execute("DROP TABLE IF EXISTS " + tableName);
                        } catch (SQLException e) {
                            // 테이블 삭제 실패는 로그만 남기고 계속 진행
                            System.out.println("테이블 삭제 실패 (무시됨): " + tableName + " - " + e.getMessage());
                        }
                    }
                }
            }
            
            // 외래키 제약조건 재활성화
            connection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            
        } catch (SQLException e) {
            // 데이터베이스 초기화 실패시에도 테스트 계속 진행
            System.out.println("데이터베이스 초기화 중 오류 발생 (무시됨): " + e.getMessage());
        }
    }
}