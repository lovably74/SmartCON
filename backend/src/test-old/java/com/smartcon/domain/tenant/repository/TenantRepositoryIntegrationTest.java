package com.smartcon.domain.tenant.repository;

import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.global.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantRepository MariaDB 통합 테스트
 */
@DisplayName("TenantRepository MariaDB 통합 테스트")
class TenantRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    @DisplayName("테넌트 생성 및 조회 테스트")
    void createAndFindTenant_ShouldWork() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setCompanyName("테스트 회사");
        tenant.setBusinessNo("123-45-67890");
        tenant.setRepresentativeName("김대표");
        tenant.setEmail("test@company.com");
        tenant.setPhoneNumber("02-1234-5678");
        tenant.setRoadAddress("서울시 강남구");
        tenant.setPlanId("STANDARD");
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);

        // When
        Tenant savedTenant = tenantRepository.save(tenant);

        // Then
        assertThat(savedTenant.getId()).isNotNull();
        assertThat(savedTenant.getCompanyName()).isEqualTo("테스트 회사");
        assertThat(savedTenant.getCreatedAt()).isNotNull();
        assertThat(savedTenant.getUpdatedAt()).isNotNull();

        // 조회 테스트
        Optional<Tenant> foundTenant = tenantRepository.findById(savedTenant.getId());
        assertThat(foundTenant).isPresent();
        assertThat(foundTenant.get().getCompanyName()).isEqualTo("테스트 회사");
    }

    @Test
    @DisplayName("상태별 테넌트 개수 조회 테스트")
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        Tenant activeTenant = createTestTenant("활성 회사", Tenant.SubscriptionStatus.ACTIVE);
        Tenant suspendedTenant = createTestTenant("정지 회사", Tenant.SubscriptionStatus.SUSPENDED);
        
        tenantRepository.save(activeTenant);
        tenantRepository.save(suspendedTenant);

        // When
        long activeCount = tenantRepository.countByStatus(Tenant.SubscriptionStatus.ACTIVE);
        long suspendedCount = tenantRepository.countByStatus(Tenant.SubscriptionStatus.SUSPENDED);

        // Then
        assertThat(activeCount).isGreaterThanOrEqualTo(1);
        assertThat(suspendedCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("회사명으로 테넌트 검색 테스트")
    void findByCompanyNameContainingIgnoreCase_ShouldReturnMatchingTenants() {
        // Given
        Tenant tenant1 = createTestTenant("ABC 건설", Tenant.SubscriptionStatus.ACTIVE);
        Tenant tenant2 = createTestTenant("XYZ 건축", Tenant.SubscriptionStatus.ACTIVE);
        
        tenantRepository.save(tenant1);
        tenantRepository.save(tenant2);

        Pageable pageable = PageRequest.of(0, 10);

        // When - H2에서 LIKE ESCAPE 문제를 피하기 위해 간단한 검색어 사용
        Page<Tenant> result = tenantRepository.findByCompanyNameContainingIgnoreCase("ABC", pageable);

        // Then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().stream()
                .anyMatch(t -> t.getCompanyName().contains("ABC"))).isTrue();
    }

    @Test
    @DisplayName("기간별 테넌트 생성 개수 조회 테스트")
    void countByCreatedAtBetween_ShouldReturnCorrectCount() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        Tenant tenant = createTestTenant("신규 회사", Tenant.SubscriptionStatus.ACTIVE);
        tenantRepository.save(tenant);

        // When
        long count = tenantRepository.countByCreatedAtBetween(startDate, endDate);

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("최근 생성된 테넌트 조회 테스트")
    void findTop10ByOrderByCreatedAtDesc_ShouldReturnRecentTenants() {
        // Given
        for (int i = 0; i < 5; i++) {
            Tenant tenant = createTestTenant("회사" + i, Tenant.SubscriptionStatus.ACTIVE);
            tenantRepository.save(tenant);
        }

        // When
        List<Tenant> recentTenants = tenantRepository.findTop10ByOrderByCreatedAtDesc();

        // Then
        assertThat(recentTenants).isNotEmpty();
        assertThat(recentTenants.size()).isLessThanOrEqualTo(10);
        
        // 생성일 순서 확인
        for (int i = 0; i < recentTenants.size() - 1; i++) {
            assertThat(recentTenants.get(i).getCreatedAt())
                    .isAfterOrEqualTo(recentTenants.get(i + 1).getCreatedAt());
        }
    }

    private Tenant createTestTenant(String companyName, Tenant.SubscriptionStatus status) {
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
        tenant.setStatus(status);
        return tenant;
    }
}