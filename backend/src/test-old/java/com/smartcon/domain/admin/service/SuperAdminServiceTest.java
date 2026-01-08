package com.smartcon.domain.admin.service;

import com.smartcon.domain.admin.dto.ApprovalStatsDto;
import com.smartcon.domain.admin.dto.DashboardStatsDto;
import com.smartcon.domain.admin.dto.SubscriptionExportDto;
import com.smartcon.domain.billing.entity.BillingRecord;
import com.smartcon.domain.billing.repository.BillingRecordRepository;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.SubscriptionApprovalRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionApprovalRepository subscriptionApprovalRepository;

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

    @Test
    @DisplayName("승인 대시보드 통계 정보 조회 테스트")
    void getApprovalStats_ShouldReturnCorrectStats() {
        // Given
        when(subscriptionRepository.count()).thenReturn(100L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.PENDING_APPROVAL)).thenReturn(15L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(70L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.REJECTED)).thenReturn(10L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.SUSPENDED)).thenReturn(3L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.TERMINATED)).thenReturn(2L);
        
        when(subscriptionApprovalRepository.countByAutoApproved(true)).thenReturn(50L);
        when(subscriptionApprovalRepository.countByAutoApproved(false)).thenReturn(20L);
        
        when(subscriptionApprovalRepository.getAverageProcessingTime())
                .thenReturn(java.util.Collections.singletonList(new Object[]{24.5}));
        
        when(subscriptionRepository.countByStatusAndCreatedAtBefore(
                eq(SubscriptionStatus.PENDING_APPROVAL), any(LocalDateTime.class))).thenReturn(5L);

        // When
        ApprovalStatsDto result = superAdminService.getApprovalStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalSubscriptions()).isEqualTo(100L);
        assertThat(result.getPendingApprovals()).isEqualTo(15L);
        assertThat(result.getApprovedSubscriptions()).isEqualTo(70L);
        assertThat(result.getRejectedSubscriptions()).isEqualTo(10L);
        assertThat(result.getSuspendedSubscriptions()).isEqualTo(3L);
        assertThat(result.getTerminatedSubscriptions()).isEqualTo(2L);
        assertThat(result.getAutoApprovedCount()).isEqualTo(50L);
        assertThat(result.getManualApprovalCount()).isEqualTo(20L);
        assertThat(result.getAutoApprovalRate()).isEqualTo(71.43);
        assertThat(result.getAverageProcessingHours()).isEqualTo(24.5);
        assertThat(result.getPendingOverThreeDays()).isEqualTo(5L);
    }

    @Test
    @DisplayName("승인 통계 - 승인 이력이 없는 경우 테스트")
    void getApprovalStats_WithNoApprovalHistory_ShouldReturnZeroStats() {
        // Given
        when(subscriptionRepository.count()).thenReturn(50L);
        when(subscriptionRepository.countByStatus(any(SubscriptionStatus.class))).thenReturn(0L);
        when(subscriptionApprovalRepository.countByAutoApproved(true)).thenReturn(0L);
        when(subscriptionApprovalRepository.countByAutoApproved(false)).thenReturn(0L);
        when(subscriptionApprovalRepository.getAverageProcessingTime()).thenReturn(java.util.Collections.emptyList());
        when(subscriptionRepository.countByStatusAndCreatedAtBefore(
                eq(SubscriptionStatus.PENDING_APPROVAL), any(LocalDateTime.class))).thenReturn(0L);

        // When
        ApprovalStatsDto result = superAdminService.getApprovalStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalSubscriptions()).isEqualTo(50L);
        assertThat(result.getAutoApprovalRate()).isEqualTo(0.0);
        assertThat(result.getAverageProcessingHours()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("구독 데이터 내보내기 테스트")
    void exportSubscriptionData_ShouldReturnExportData() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> mockData = Arrays.asList(
                new Object[]{1L, "테스트 회사 A", "기본 요금제", SubscriptionStatus.ACTIVE, 
                           BigDecimal.valueOf(50000), now.minusDays(10), now.minusDays(9), 
                           now.minusDays(5), "승인 완료", "관리자", false},
                new Object[]{2L, "테스트 회사 B", "프리미엄 요금제", SubscriptionStatus.PENDING_APPROVAL, 
                           BigDecimal.valueOf(100000), now.minusDays(5), null, 
                           null, null, null, true}
        );

        when(subscriptionRepository.getSubscriptionExportData(
                eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockData);

        // When
        List<SubscriptionExportDto> result = superAdminService.exportSubscriptionData(
                SubscriptionStatus.ACTIVE, now.minusDays(30), now);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        SubscriptionExportDto first = result.get(0);
        assertThat(first.getSubscriptionId()).isEqualTo(1L);
        assertThat(first.getTenantName()).isEqualTo("테스트 회사 A");
        assertThat(first.getPlanName()).isEqualTo("기본 요금제");
        assertThat(first.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(first.getMonthlyFee()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(first.isAutoApproved()).isFalse();
        
        SubscriptionExportDto second = result.get(1);
        assertThat(second.getSubscriptionId()).isEqualTo(2L);
        assertThat(second.getTenantName()).isEqualTo("테스트 회사 B");
        assertThat(second.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        assertThat(second.isAutoApproved()).isTrue();
    }

    @Test
    @DisplayName("구독 데이터 내보내기 - 필터 없이 전체 데이터 조회 테스트")
    void exportSubscriptionData_WithoutFilters_ShouldReturnAllData() {
        // Given
        List<Object[]> mockData = java.util.Collections.singletonList(
                new Object[]{1L, "테스트 회사", "기본 요금제", SubscriptionStatus.ACTIVE, 
                           BigDecimal.valueOf(50000), LocalDateTime.now(), LocalDateTime.now(), 
                           LocalDateTime.now(), "승인", "관리자", false}
        );

        when(subscriptionRepository.getSubscriptionExportData(null, null, null))
                .thenReturn(mockData);

        // When
        List<SubscriptionExportDto> result = superAdminService.exportSubscriptionData(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTenantName()).isEqualTo("테스트 회사");
    }
}