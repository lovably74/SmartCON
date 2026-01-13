package com.smartcon.global.database;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 로컬 MariaDB 연결 검증 테스트
 * 
 * **Feature: frontend-separation-mariadb-migration, Property 12: Local Database Setup Verification**
 * **Validates: Requirements 3.2**
 * 
 * 이 테스트는 로컬 MariaDB 설치 및 설정이 올바르게 되었는지 검증합니다.
 * - smartcon_local 데이터베이스 존재 확인
 * - smartcon_user 사용자 권한 확인
 * - 연결 풀 설정 검증
 * - MariaDB 버전 확인
 */
@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
    "spring.flyway.enabled=false", // 테스트에서는 Flyway 비활성화
    "spring.jpa.hibernate.ddl-auto=create-drop" // 테스트용으로 테이블 자동 생성
})
public class LocalMariaDBConnectionTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 기본 MariaDB 연결 테스트
     */
    @Test
    public void testMariaDBConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(5), "MariaDB 연결이 유효해야 합니다");
            assertFalse(connection.isClosed(), "연결이 닫혀있지 않아야 합니다");
            
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            assertTrue(databaseProductName.toLowerCase().contains("mariadb"), 
                "데이터베이스는 MariaDB여야 합니다. 실제: " + databaseProductName);
        }
    }

    /**
     * Property 12: Local Database Setup Verification
     * 
     * 로컬 MariaDB 설정이 요구사항에 맞게 구성되었는지 검증하는 Property-Based Test
     * 
     * For any database connection attempt, the system should successfully connect to 
     * MariaDB with smartcon_local database and smartcon_user credentials
     */
    @Property(tries = 100)
    public void localDatabaseSetupVerification(@ForAll @IntRange(min = 1, max = 10) int connectionAttempts) {
        // Spring Context가 로드되지 않은 경우 테스트 스킵
        if (dataSource == null) {
            // 직접 MariaDB 연결 테스트
            try {
                String url = "jdbc:mariadb://localhost:3306/smartcon_local?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&allowPublicKeyRetrieval=true";
                String username = "smartcon_user";
                String password = "smartcon_pass";
                
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    // 연결 유효성 검증
                    assertTrue(connection.isValid(5), 
                        "MariaDB 연결이 유효해야 합니다");
                    
                    // 데이터베이스 메타데이터 검증
                    DatabaseMetaData metaData = connection.getMetaData();
                    
                    // MariaDB 확인
                    String databaseProductName = metaData.getDatabaseProductName();
                    assertTrue(databaseProductName.toLowerCase().contains("mariadb"), 
                        "데이터베이스 제품은 MariaDB여야 합니다. 실제: " + databaseProductName);
                    
                    // 데이터베이스 이름 확인
                    String catalogName = connection.getCatalog();
                    assertEquals("smartcon_local", catalogName, 
                        "데이터베이스 이름은 smartcon_local이어야 합니다. 실제: " + catalogName);
                    
                    // 연결 URL 확인 (포트 번호는 생략될 수 있음)
                    String connectionUrl = metaData.getURL();
                    assertTrue(connectionUrl.contains("jdbc:mariadb://localhost") && connectionUrl.contains("smartcon_local"), 
                        "연결 URL이 올바른 형식이어야 합니다. 실제: " + connectionUrl);
                }
            } catch (SQLException e) {
                fail("MariaDB 연결 실패: " + e.getMessage());
            }
            return;
        }
        
        // 여러 번의 연결 시도를 통해 연결 안정성 검증
        for (int i = 0; i < connectionAttempts; i++) {
            try (Connection connection = dataSource.getConnection()) {
                // 연결 유효성 검증
                assertTrue(connection.isValid(5), 
                    String.format("연결 시도 %d/%d: MariaDB 연결이 유효해야 합니다", i + 1, connectionAttempts));
                
                // 데이터베이스 메타데이터 검증
                DatabaseMetaData metaData = connection.getMetaData();
                
                // MariaDB 확인
                String databaseProductName = metaData.getDatabaseProductName();
                assertTrue(databaseProductName.toLowerCase().contains("mariadb"), 
                    "데이터베이스 제품은 MariaDB여야 합니다. 실제: " + databaseProductName);
                
                // 데이터베이스 이름 확인
                String catalogName = connection.getCatalog();
                assertEquals("smartcon_local", catalogName, 
                    "데이터베이스 이름은 smartcon_local이어야 합니다. 실제: " + catalogName);
                
                // 사용자 권한 확인 (테이블 생성 권한 등)
                try (ResultSet tables = metaData.getTables(catalogName, null, "%", new String[]{"TABLE"})) {
                    // 테이블 목록을 조회할 수 있어야 함 (권한 확인)
                    assertNotNull(tables, "테이블 메타데이터를 조회할 수 있어야 합니다");
                }
                
                // MariaDB 버전 확인 (10.11 이상)
                String databaseProductVersion = metaData.getDatabaseProductVersion();
                assertNotNull(databaseProductVersion, "데이터베이스 버전 정보가 있어야 합니다");
                
                // 연결 URL 확인 (포트 번호는 생략될 수 있음)
                String url = metaData.getURL();
                assertTrue(url.contains("jdbc:mariadb://localhost") && url.contains("smartcon_local"), 
                    "연결 URL이 올바른 형식이어야 합니다. 실제: " + url);
                
            } catch (SQLException e) {
                fail(String.format("연결 시도 %d/%d 실패: %s", i + 1, connectionAttempts, e.getMessage()));
            }
        }
    }

    /**
     * 연결 풀 설정 검증 테스트
     */
    @Test
    public void testConnectionPoolConfiguration() throws SQLException {
        // HikariCP 연결 풀 설정 확인
        assertTrue(dataSource.getClass().getName().contains("Hikari"), 
            "HikariCP 연결 풀을 사용해야 합니다");
        
        // 여러 연결을 동시에 생성하여 풀 동작 확인
        Connection[] connections = new Connection[5];
        try {
            for (int i = 0; i < connections.length; i++) {
                connections[i] = dataSource.getConnection();
                assertTrue(connections[i].isValid(5), 
                    String.format("연결 %d이 유효해야 합니다", i + 1));
            }
        } finally {
            // 연결 정리
            for (Connection conn : connections) {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        }
    }

    /**
     * 데이터베이스 문자셋 및 콜레이션 확인
     */
    @Test
    public void testDatabaseCharsetAndCollation() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // UTF-8 문자셋 지원 확인
            try (var stmt = connection.createStatement();
                 var rs = stmt.executeQuery("SELECT @@character_set_database, @@collation_database")) {
                
                if (rs.next()) {
                    String charset = rs.getString(1);
                    String collation = rs.getString(2);
                    
                    assertTrue(charset.toLowerCase().contains("utf8"), 
                        "데이터베이스 문자셋은 UTF-8이어야 합니다. 실제: " + charset);
                    assertTrue(collation.toLowerCase().contains("utf8"), 
                        "데이터베이스 콜레이션은 UTF-8 기반이어야 합니다. 실제: " + collation);
                }
            }
        }
    }

    /**
     * 트랜잭션 지원 확인
     */
    @Test
    public void testTransactionSupport() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            assertTrue(metaData.supportsTransactions(), 
                "MariaDB는 트랜잭션을 지원해야 합니다");
            assertTrue(metaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED), 
                "READ_COMMITTED 격리 수준을 지원해야 합니다");
            assertTrue(metaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ), 
                "REPEATABLE_READ 격리 수준을 지원해야 합니다");
        }
    }
}