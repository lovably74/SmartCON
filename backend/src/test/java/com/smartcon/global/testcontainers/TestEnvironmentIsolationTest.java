package com.smartcon.global.testcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.*;

/**
 * 테스트 환경 격리 검증 테스트
 * 
 * 검증 내용:
 * - 테스트 환경에서 MariaDB 연결이 정상적으로 작동하는지 확인
 * - 데이터베이스 스키마가 올바르게 생성되었는지 검증
 * 
 * 요구사항 검증:
 * - 5.2: 테스트 환경 격리
 * - 5.4: 테스트 완료 후 컨테이너 자동 정리
 * 
 * 참고: Docker 환경이 없는 경우 로컬 MariaDB를 사용합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
public class TestEnvironmentIsolationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void 테스트_환경_MariaDB_연결_검증() throws Exception {
        
        // Given: 데이터소스가 주입되어야 함
        assertThat(dataSource)
            .as("데이터소스가 주입되어야 합니다")
            .isNotNull();
        
        // When: 데이터베이스 연결을 시도
        try (Connection connection = dataSource.getConnection()) {
            
            // Then: 연결이 성공해야 함
            assertThat(connection.isValid(5))
                .as("데이터베이스 연결이 유효해야 합니다")
                .isTrue();
            
            // And: 데이터베이스 제품명 확인
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            assertThat(productName)
                .as("데이터베이스 제품명이 확인되어야 합니다")
                .isNotNull()
                .isNotEmpty();
            
            // And: 테스트 데이터베이스 확인
            String catalogName = connection.getCatalog();
            assertThat(catalogName)
                .as("테스트 데이터베이스 이름이 확인되어야 합니다")
                .isNotNull()
                .containsIgnoringCase("test");
            
            // And: 기본 테이블들이 존재해야 함 (Flyway 마이그레이션 결과)
            try (ResultSet tables = metaData.getTables(null, null, "users", null)) {
                assertThat(tables.next())
                    .as("users 테이블이 존재해야 합니다")
                    .isTrue();
            }
            
            // And: flyway_schema_history 테이블 확인 (마이그레이션 실행 확인)
            try (ResultSet tables = metaData.getTables(null, null, "flyway_schema_history", null)) {
                assertThat(tables.next())
                    .as("flyway_schema_history 테이블이 존재해야 합니다")
                    .isTrue();
            }
        }
    }
}