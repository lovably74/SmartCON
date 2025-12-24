package com.smartcon.domain.admin.service;

import com.smartcon.domain.admin.dto.DashboardStatsDto;
import com.smartcon.domain.billing.entity.BillingRecord;
import com.smartcon.domain.billing.repository.BillingRecordRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 슈퍼관리자 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("슈퍼관리자 서비스 테스트")
class SuperAdminServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillingRecordRepository billingRecordRepository;

    @InjectMocks
    private SuperAdminService superAdminService;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
    }

    @Test
    @DisplayName("대시보드 통계 정보 조회 테스트")
    void getDashboardStats_ShouldReturnCorrectStats() {
        // Given
        when(tenantRepository.count()).thenReturn(15L);
        when(tenantRepository.countByStatus(Tenant.SubscriptionStatus.ACTIVE)).thenReturn(10L);
        when(tenantRepository.countByStatus(Tenant.SubscriptionStatus.SUSPENDED)).thenReturn(3L);
        when(tenantRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(5L);
        
        when(userRepository.count()).thenReturn(150L);
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(25L);
        
        when(billingRecordRepository.getTotalRevenueByPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(1000000));
        when(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.SUCCESS)).thenReturn(100L);
        when(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.FAILED)).thenReturn(5L);
        when(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.PENDING)).thenReturn(10L);

        // When
        DashboardStatsDto result = superAdminService.getDashboardStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalTenants()).isEqualTo(15L);
        assertThat(result.getActiveTenants()).isEqualTo(10L);
        assertThat(result.getSuspendedTenants()).isEqualTo(3L);
        assertThat(result.getNewTenantsThisMonth()).isEqualTo(5L);
        assertThat(result.getTotalUsers()).isEqualTo(150L);
        assertThat(result.getNewUsersThisMonth()).isEqualTo(25L);
        assertThat(result.getCompletedPayments()).isEqualTo(100L);
        assertThat(result.getFailedPayments()).isEqualTo(5L);
        assertThat(result.getPendingPayments()).isEqualTo(10L);
        assertThat(result.getSystemStatus()).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("테넌트 상태 변경 테스트")
    void updateTenantStatus_ShouldUpdateSuccessfully() {
        // Given
        Long tenantId = 1L;
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        
        when(tenantRepository.findById(tenantId)).thenReturn(java.util.Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        // When
        superAdminService.updateTenantStatus(tenantId, Tenant.SubscriptionStatus.SUSPENDED);

        // Then
        assertThat(tenant.getStatus()).isEqualTo(Tenant.SubscriptionStatus.SUSPENDED);
    }

    @Test
    @DisplayName("존재하지 않는 테넌트 상태 변경 시 예외 발생 테스트")
    void updateTenantStatus_WithNonExistentTenant_ShouldThrowException() {
        // Given
        Long tenantId = 999L;
        when(tenantRepository.findById(tenantId)).thenReturn(java.util.Optional.empty());

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> superAdminService.updateTenantStatus(tenantId, Tenant.SubscriptionStatus.SUSPENDED)
        );
    }
}