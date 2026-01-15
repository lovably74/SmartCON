package com.smartcon.integration;

import com.smartcon.SmartconApplication;
import com.smartcon.domain.attendance.repository.AttendanceRecordRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.tenant.TenantContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 5단계 역할 기반 시스템 전체 프로젝트 구조 통합 테스트
 * Task 1.1: 프로젝트 구조 설정 테스트 - 통합 검증
 * Requirements: 1.1
 */
@SpringBootTest(classes = SmartconApplication.class)
@ActiveProfiles("test")
@DisplayName("프로젝트 구조 통합 검증 테스트")
class ProjectStructureIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    @DisplayName("Spring Boot 애플리케이션 컨텍스트 로딩 검증")
    void testApplicationContextLoads() {
        // Given & When: Spring Boot 애플리케이션 컨텍스트 로딩
        
        // Then: 애플리케이션 컨텍스트가 정상적으로 로딩되었는지 검증
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(SmartconApplication.class)).isNotNull();
        
        // 핵심 서비스 빈들이 등록되었는지 검증
        assertThat(applicationContext.getBean("userRepository")).isNotNull();
        assertThat(applicationContext.getBean("projectRepository")).isNotNull();
        assertThat(applicationContext.getBean("attendanceRecordRepository")).isNotNull();
        assertThat(applicationContext.getBean("subscriptionRepository")).isNotNull();
        assertThat(applicationContext.getBean("jwtTokenService")).isNotNull();
    }

    @Test
    @DisplayName("JPA Repository 인터페이스 구조 검증")
    void testJpaRepositoryStructure() {
        // Given & When: JPA Repository 인터페이스들 검증
        
        // Then: 모든 Repository가 JpaRepository를 상속하는지 검증
        assertThat(userRepository).isInstanceOf(JpaRepository.class);
        assertThat(projectRepository).isInstanceOf(JpaRepository.class);
        assertThat(attendanceRecordRepository).isInstanceOf(JpaRepository.class);
        assertThat(subscriptionRepository).isInstanceOf(JpaRepository.class);
        
        // Repository 메서드들이 정상 작동하는지 검증
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(projectRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(attendanceRecordRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(subscriptionRepository.count()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("데이터베이스 스키마 구조 검증")
    void testDatabaseSchemaStructure() throws Exception {
        // Given: 데이터베이스 연결
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // When: 테이블 목록 조회
            List<String> tableNames = new ArrayList<>();
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (!tableName.startsWith("INFORMATION_SCHEMA")) {
                        tableNames.add(tableName.toLowerCase());
                    }
                }
            }
            
            // Then: 핵심 테이블들이 존재하는지 검증
            assertThat(tableNames).contains("users");
            assertThat(tableNames).contains("projects");
            assertThat(tableNames).contains("attendance_records");
            assertThat(tableNames).contains("subscriptions");
            assertThat(tableNames).contains("tenants");
            
            // 멀티테넌트 구조 검증 - tenant_id 컬럼 존재 확인
            verifyTenantIdColumn(metaData, "users");
            verifyTenantIdColumn(metaData, "projects");
            verifyTenantIdColumn(metaData, "attendance_records");
            verifyTenantIdColumn(metaData, "subscriptions");
        }
    }

    @Test
    @DisplayName("JWT 토큰 서비스 구조 검증")
    void testJwtTokenServiceStructure() {
        // Given & When: JWT 토큰 서비스 검증
        
        // Then: JWT 토큰 서비스가 정상적으로 초기화되었는지 검증
        assertThat(jwtTokenService).isNotNull();
        
        // 토큰 생성 및 검증 기능이 작동하는지 검증
        String testToken = jwtTokenService.generateAccessToken("1", "1", "ROLE_USER", java.util.Map.of());
        assertThat(testToken).isNotNull();
        assertThat(testToken).isNotEmpty();
        
        // 토큰 검증 기능 확인
        assertThat(jwtTokenService.validateToken(testToken)).isTrue();
        
        // 토큰에서 클레임 추출 기능 확인
        assertThat(jwtTokenService.extractClaims(testToken)).isNotNull();
        assertThat(jwtTokenService.extractUserId(testToken)).isEqualTo("1");
        assertThat(jwtTokenService.extractTenantId(testToken)).isEqualTo("1");
        assertThat(jwtTokenService.extractRole(testToken)).isEqualTo("ROLE_USER");
        assertThat(jwtTokenService.isAccessToken(testToken)).isTrue();
    }

    @Test
    @DisplayName("멀티테넌트 컨텍스트 구조 검증")
    void testMultiTenantContextStructure() {
        // Given: 테넌트 컨텍스트 초기 상태
        assertThat(TenantContext.getCurrentTenantId()).isNull();
        
        // When: 테넌트 ID 설정
        Long testTenantId = 123L;
        TenantContext.setCurrentTenantId(testTenantId);
        
        // Then: 테넌트 컨텍스트가 정상 작동하는지 검증
        assertThat(TenantContext.getCurrentTenantId()).isEqualTo(testTenantId);
        
        // 컨텍스트 정리
        TenantContext.clear();
        assertThat(TenantContext.getCurrentTenantId()).isNull();
    }

    @Test
    @DisplayName("5단계 역할 시스템 enum 구조 검증")
    void testRoleSystemEnumStructure() {
        // Given & When: 역할 시스템 enum 검증
        
        // Then: 5단계 역할이 모두 정의되어 있는지 검증
        com.smartcon.domain.user.entity.User.Role[] roles = {
            com.smartcon.domain.user.entity.User.Role.ROLE_SUPER, 
            com.smartcon.domain.user.entity.User.Role.ROLE_HQ, 
            com.smartcon.domain.user.entity.User.Role.ROLE_SITE, 
            com.smartcon.domain.user.entity.User.Role.ROLE_TEAM, 
            com.smartcon.domain.user.entity.User.Role.ROLE_WORKER
        };
        assertThat(roles).hasSize(5);
        assertThat(com.smartcon.domain.user.entity.User.Role.ROLE_SUPER.getDisplayName()).isEqualTo("슈퍼관리자");
        assertThat(com.smartcon.domain.user.entity.User.Role.ROLE_HQ.getDisplayName()).isEqualTo("본사관리자");
        assertThat(com.smartcon.domain.user.entity.User.Role.ROLE_SITE.getDisplayName()).isEqualTo("현장관리자");
        assertThat(com.smartcon.domain.user.entity.User.Role.ROLE_TEAM.getDisplayName()).isEqualTo("노무팀장");
        assertThat(com.smartcon.domain.user.entity.User.Role.ROLE_WORKER.getDisplayName()).isEqualTo("일반노무자");
        
        // 로그인 타입 enum 검증
        assertThat(com.smartcon.domain.user.entity.LoginType.values()).hasSize(2);
        assertThat(com.smartcon.domain.user.entity.LoginType.BUSINESS.getDisplayName()).isEqualTo("사업자 로그인");
        assertThat(com.smartcon.domain.user.entity.LoginType.SOCIAL.getDisplayName()).isEqualTo("소셜 로그인");
        
        // 인증 제공자 enum 검증
        assertThat(com.smartcon.domain.user.entity.AuthProvider.values()).hasSize(3);
        assertThat(com.smartcon.domain.user.entity.AuthProvider.LOCAL.getDisplayName()).isEqualTo("일반 로그인");
        assertThat(com.smartcon.domain.user.entity.AuthProvider.KAKAO.getDisplayName()).isEqualTo("카카오");
        assertThat(com.smartcon.domain.user.entity.AuthProvider.NAVER.getDisplayName()).isEqualTo("네이버");
    }

    @Test
    @DisplayName("프로젝트 패키지 구조 검증")
    void testProjectPackageStructure() {
        // Given & When: 패키지 구조 검증
        
        // Then: 도메인별 패키지가 올바르게 구성되어 있는지 검증
        assertThat(applicationContext.getBeansOfType(Object.class).keySet())
                .anyMatch(beanName -> beanName.contains("user"))
                .anyMatch(beanName -> beanName.contains("project"))
                .anyMatch(beanName -> beanName.contains("attendance"))
                .anyMatch(beanName -> beanName.contains("subscription"));
        
        // 글로벌 패키지 구성 검증
        assertThat(applicationContext.getBean("jwtTokenService")).isNotNull();
        
        // TenantContext는 유틸리티 클래스이므로 빈으로 등록되지 않음
        // 대신 TenantContext 클래스가 존재하는지 확인
        try {
            Class.forName("com.smartcon.global.tenant.TenantContext");
            // TenantContext 클래스가 존재함을 확인
        } catch (ClassNotFoundException e) {
            throw new AssertionError("TenantContext 클래스가 존재하지 않습니다", e);
        }
    }

    /**
     * 테이블에 tenant_id 컬럼이 존재하는지 검증하는 헬퍼 메서드
     */
    private void verifyTenantIdColumn(DatabaseMetaData metaData, String tableName) throws Exception {
        boolean hasTenantIdColumn = false;
        try (ResultSet columns = metaData.getColumns(null, null, tableName.toUpperCase(), null)) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if ("TENANT_ID".equalsIgnoreCase(columnName)) {
                    hasTenantIdColumn = true;
                    break;
                }
            }
        }
        assertThat(hasTenantIdColumn)
                .as("테이블 %s에 tenant_id 컬럼이 존재해야 합니다", tableName)
                .isTrue();
    }
}