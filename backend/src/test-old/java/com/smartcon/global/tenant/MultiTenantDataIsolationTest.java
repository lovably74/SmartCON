package com.smartcon.global.tenant;

import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 멀티테넌트 데이터 격리 테스트
 * MariaDB 환경에서 테넌트별 데이터 분리가 올바르게 작동하는지 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("멀티테넌트 데이터 격리 테스트")
class MultiTenantDataIsolationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User tenant1User;
    private User tenant2User;

    @BeforeEach
    void setUp() {
        // 테넌트 1 사용자 생성
        TenantContext.setCurrentTenantId(1L);
        tenant1User = new User();
        tenant1User.setName("테넌트1 사용자");
        tenant1User.setEmail("user1@tenant1.com");
        tenant1User = userRepository.save(tenant1User);

        // 테넌트 2 사용자 생성
        TenantContext.setCurrentTenantId(2L);
        tenant2User = new User();
        tenant2User.setName("테넌트2 사용자");
        tenant2User.setEmail("user2@tenant2.com");
        tenant2User = userRepository.save(tenant2User);

        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        disableTenantFilter();
        TenantContext.clear();
    }

    @Test
    @DisplayName("테넌트별 데이터 격리 확인")
    void testTenantDataIsolation() {
        // 테넌트 1 컨텍스트에서 조회
        TenantContext.setCurrentTenantId(1L);
        enableTenantFilter(1L);
        
        List<User> tenant1Users = userRepository.findAll();
        
        // 테넌트 1에서는 테넌트 1의 사용자만 조회되어야 함
        assertThat(tenant1Users).isNotEmpty();
        assertThat(tenant1Users).allMatch(user -> user.getTenantId().equals(1L));
        
        boolean hasTestUser = tenant1Users.stream()
                .anyMatch(user -> "테넌트1 사용자".equals(user.getName()));
        assertThat(hasTestUser).isTrue();

        disableTenantFilter();

        // 테넌트 2 컨텍스트에서 조회
        TenantContext.setCurrentTenantId(2L);
        enableTenantFilter(2L);
        
        List<User> tenant2Users = userRepository.findAll();
        
        // 테넌트 2에서는 테넌트 2의 사용자만 조회되어야 함
        assertThat(tenant2Users).isNotEmpty();
        assertThat(tenant2Users).allMatch(user -> user.getTenantId().equals(2L));
        
        boolean hasTestUser2 = tenant2Users.stream()
                .anyMatch(user -> "테넌트2 사용자".equals(user.getName()));
        assertThat(hasTestUser2).isTrue();
    }

    @Test
    @DisplayName("테넌트 컨텍스트 없이 조회 시 모든 데이터 조회")
    void testNoTenantContextShowsAllData() {
        // 테넌트 컨텍스트 없이 조회 (슈퍼관리자 모드)
        TenantContext.clear();
        disableTenantFilter();
        
        List<User> allUsers = userRepository.findAll();
        
        // 모든 테넌트의 사용자가 조회되어야 함
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(2);
        
        // 테넌트 1과 2의 사용자가 모두 포함되어야 함
        boolean hasTenant1User = allUsers.stream()
                .anyMatch(user -> "테넌트1 사용자".equals(user.getName()));
        boolean hasTenant2User = allUsers.stream()
                .anyMatch(user -> "테넌트2 사용자".equals(user.getName()));
        
        assertThat(hasTenant1User).isTrue();
        assertThat(hasTenant2User).isTrue();
    }

    @Test
    @DisplayName("테넌트 ID 자동 설정 확인")
    void testTenantIdAutoAssignment() {
        // 테넌트 3 컨텍스트 설정
        TenantContext.setCurrentTenantId(3L);
        
        // 새 사용자 생성 (테넌트 ID 수동 설정 없음)
        User newUser = new User();
        newUser.setName("자동 테넌트 사용자");
        newUser.setEmail("auto@tenant3.com");
        
        User savedUser = userRepository.save(newUser);
        
        // 테넌트 ID가 자동으로 설정되었는지 확인
        assertThat(savedUser.getTenantId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("테넌트 필터 활성화/비활성화 테스트")
    void testTenantFilterToggle() {
        // 필터 없이 모든 데이터 조회
        disableTenantFilter();
        List<User> allUsers = userRepository.findAll();
        int totalCount = allUsers.size();
        
        // 테넌트 1 필터 활성화
        enableTenantFilter(1L);
        List<User> tenant1Users = userRepository.findAll();
        
        // 테넌트 2 필터 활성화
        disableTenantFilter();
        enableTenantFilter(2L);
        List<User> tenant2Users = userRepository.findAll();
        
        // 각 테넌트의 사용자 수는 전체보다 적어야 함
        assertThat(tenant1Users.size()).isLessThan(totalCount);
        assertThat(tenant2Users.size()).isLessThan(totalCount);
        
        // 테넌트별로 다른 사용자가 조회되어야 함
        assertThat(tenant1Users).allMatch(user -> user.getTenantId().equals(1L));
        assertThat(tenant2Users).allMatch(user -> user.getTenantId().equals(2L));
    }

    /**
     * 테넌트 필터 활성화
     */
    private void enableTenantFilter(Long tenantId) {
        try {
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
    }

    /**
     * 테넌트 필터 비활성화
     */
    private void disableTenantFilter() {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("tenantFilter");
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
    }
}