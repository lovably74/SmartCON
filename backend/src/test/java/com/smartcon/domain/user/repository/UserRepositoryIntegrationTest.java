package com.smartcon.domain.user.repository;

import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.global.config.BaseIntegrationTest;
import com.smartcon.global.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository MariaDB 통합 테스트
 */
@DisplayName("UserRepository MariaDB 통합 테스트")
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("사용자 생성 및 조회 테스트")
    void createAndFindUser_ShouldWork() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        TenantContext.setCurrentTenantId(tenant.getId());

        User user = new User();
        user.setTenantId(tenant.getId());
        user.setEmail("test@company.com");
        user.setName("김테스트");
        user.setPasswordHash("$2a$10$dummyhash");
        user.setPhoneNumber("010-1234-5678");
        user.setIsActive(true);
        user.setIsEmailVerified(true);
        user.setProvider(User.Provider.LOCAL);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@company.com");
        assertThat(savedUser.getTenantId()).isEqualTo(tenant.getId());
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("테넌트별 사용자 개수 조회 테스트")
    void countByTenantId_ShouldReturnCorrectCount() {
        // Given
        Tenant tenant1 = createAndSaveTenant("회사1");
        Tenant tenant2 = createAndSaveTenant("회사2");

        // 테넌트1에 사용자 2명 생성
        TenantContext.setCurrentTenantId(tenant1.getId());
        createAndSaveUser(tenant1.getId(), "user1@company1.com", "사용자1");
        createAndSaveUser(tenant1.getId(), "user2@company1.com", "사용자2");

        // 테넌트2에 사용자 1명 생성
        TenantContext.setCurrentTenantId(tenant2.getId());
        createAndSaveUser(tenant2.getId(), "user1@company2.com", "사용자3");

        TenantContext.clear();

        // When
        long tenant1UserCount = userRepository.countByTenantId(tenant1.getId());
        long tenant2UserCount = userRepository.countByTenantId(tenant2.getId());

        // Then
        assertThat(tenant1UserCount).isEqualTo(2);
        assertThat(tenant2UserCount).isEqualTo(1);
    }

    @Test
    @DisplayName("기간별 사용자 생성 개수 조회 테스트")
    void countByCreatedAtBetween_ShouldReturnCorrectCount() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        TenantContext.setCurrentTenantId(tenant.getId());

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        createAndSaveUser(tenant.getId(), "newuser@company.com", "신규사용자");

        TenantContext.clear();

        // When
        long count = userRepository.countByCreatedAtBetween(startDate, endDate);

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("멀티테넌트 데이터 격리 테스트")
    void multiTenantDataIsolation_ShouldWork() {
        // Given
        Tenant tenant1 = createAndSaveTenant("회사1");
        Tenant tenant2 = createAndSaveTenant("회사2");

        // 테넌트1 컨텍스트에서 사용자 생성
        TenantContext.setCurrentTenantId(tenant1.getId());
        createAndSaveUser(tenant1.getId(), "user@company1.com", "회사1사용자");

        // 테넌트2 컨텍스트에서 사용자 생성
        TenantContext.setCurrentTenantId(tenant2.getId());
        createAndSaveUser(tenant2.getId(), "user@company2.com", "회사2사용자");

        // When & Then - 테넌트1 컨텍스트에서 조회
        TenantContext.setCurrentTenantId(tenant1.getId());
        List<User> tenant1Users = userRepository.findAll();
        assertThat(tenant1Users).hasSize(1);
        assertThat(tenant1Users.get(0).getEmail()).isEqualTo("user@company1.com");

        // 테넌트2 컨텍스트에서 조회
        TenantContext.setCurrentTenantId(tenant2.getId());
        List<User> tenant2Users = userRepository.findAll();
        assertThat(tenant2Users).hasSize(1);
        assertThat(tenant2Users.get(0).getEmail()).isEqualTo("user@company2.com");
    }

    private Tenant createAndSaveTenant(String companyName) {
        // 각 테스트마다 고유한 사업자번호 생성 (현재 시간 기반)
        String uniqueBusinessNo = String.format("%03d-%02d-%05d", 
            System.currentTimeMillis() % 1000,
            (int)(Math.random() * 100),
            (int)(Math.random() * 100000));
            
        Tenant tenant = new Tenant();
        tenant.setCompanyName(companyName);
        tenant.setBusinessNo(uniqueBusinessNo);
        tenant.setRepresentativeName("김대표");
        tenant.setEmail("test@company.com");
        tenant.setPhoneNumber("02-1234-5678");
        tenant.setRoadAddress("서울시 강남구");
        tenant.setPlanId("STANDARD");
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    private User createAndSaveUser(Long tenantId, String email, String name) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash("$2a$10$dummyhash");
        user.setPhoneNumber("010-1234-5678");
        user.setIsActive(true);
        user.setIsEmailVerified(true);
        user.setProvider(User.Provider.LOCAL);
        return userRepository.save(user);
    }
}