package com.smartcon.domain.admin.service;

import com.smartcon.domain.admin.dto.BillingStatsDto;
import com.smartcon.domain.admin.dto.DashboardStatsDto;
import com.smartcon.domain.admin.dto.TenantSummaryDto;
import com.smartcon.domain.billing.entity.BillingRecord;
import com.smartcon.domain.billing.repository.BillingRecordRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 슈퍼관리자 기능 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SuperAdminService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final BillingRecordRepository billingRecordRepository;

    /**
     * 대시보드 통계 정보 조회
     */
    public DashboardStatsDto getDashboardStats() {
        log.debug("대시보드 통계 정보 조회 시작");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfToday = now.withHour(23).withMinute(59).withSecond(59);

        DashboardStatsDto stats = new DashboardStatsDto();

        // 테넌트 통계
        stats.setTotalTenants(tenantRepository.count());
        stats.setActiveTenants(tenantRepository.countByStatus(Tenant.SubscriptionStatus.ACTIVE));
        stats.setSuspendedTenants(tenantRepository.countByStatus(Tenant.SubscriptionStatus.SUSPENDED));
        stats.setNewTenantsThisMonth(tenantRepository.countByCreatedAtBetween(startOfMonth, endOfMonth));

        // 사용자 통계
        stats.setTotalUsers(userRepository.count());
        stats.setNewUsersThisMonth(userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth));

        // 결제 통계
        stats.setTotalRevenue(billingRecordRepository.getTotalRevenueByPeriod(LocalDateTime.of(2020, 1, 1, 0, 0), now));
        stats.setMonthlyRevenue(billingRecordRepository.getTotalRevenueByPeriod(startOfMonth, endOfMonth));
        stats.setCompletedPayments(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.SUCCESS));
        stats.setFailedPayments(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.FAILED));
        stats.setPendingPayments(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.PENDING));

        // 시스템 상태 (간단한 구현)
        stats.setSystemStatus("HEALTHY");
        stats.setActiveConnections(0L); // 실제 구현 시 커넥션 풀에서 조회

        log.debug("대시보드 통계 정보 조회 완료: 총 테넌트 {}, 활성 테넌트 {}", stats.getTotalTenants(), stats.getActiveTenants());
        return stats;
    }

    /**
     * 테넌트 목록 조회 (페이징)
     */
    public Page<TenantSummaryDto> getTenants(String search, Tenant.SubscriptionStatus status, Pageable pageable) {
        log.debug("테넌트 목록 조회 시작 - 검색어: {}, 상태: {}, 페이지: {}", search, status, pageable.getPageNumber());

        Page<Tenant> tenants;

        if (search != null && !search.trim().isEmpty() && status != null) {
            tenants = tenantRepository.findByCompanyNameContainingIgnoreCaseAndStatus(search.trim(), status, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            tenants = tenantRepository.findByCompanyNameContainingIgnoreCase(search.trim(), pageable);
        } else if (status != null) {
            tenants = tenantRepository.findByStatus(status, pageable);
        } else {
            tenants = tenantRepository.findAll(pageable);
        }

        return tenants.map(tenant -> {
            long userCount = userRepository.countByTenantId(tenant.getId());
            return TenantSummaryDto.from(tenant, userCount);
        });
    }

    /**
     * 테넌트 상태 변경
     */
    @Transactional
    public void updateTenantStatus(Long tenantId, Tenant.SubscriptionStatus status) {
        log.debug("테넌트 상태 변경 시작 - ID: {}, 새 상태: {}", tenantId, status);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

        Tenant.SubscriptionStatus oldStatus = tenant.getStatus();
        tenant.setStatus(status);
        tenantRepository.save(tenant);

        log.info("테넌트 상태 변경 완료 - ID: {}, {} -> {}", tenantId, oldStatus, status);
    }

    /**
     * 결제 통계 정보 조회
     */
    public BillingStatsDto getBillingStats() {
        log.debug("결제 통계 정보 조회 시작");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfToday = now.withHour(23).withMinute(59).withSecond(59);
        LocalDateTime sixMonthsAgo = now.minusMonths(6);

        BillingStatsDto stats = new BillingStatsDto();

        // 매출 통계
        stats.setTotalRevenue(billingRecordRepository.getTotalRevenueByPeriod(LocalDateTime.of(2020, 1, 1, 0, 0), now));
        stats.setMonthlyRevenue(billingRecordRepository.getTotalRevenueByPeriod(startOfMonth, endOfMonth));
        stats.setDailyRevenue(billingRecordRepository.getTotalRevenueByPeriod(startOfToday, endOfToday));

        // 결제 건수 통계
        stats.setTotalPayments(billingRecordRepository.count());
        stats.setCompletedPayments(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.SUCCESS));
        stats.setFailedPayments(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.FAILED));
        stats.setPendingPayments(billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.PENDING));

        // 월별 매출 추이 (최근 6개월)
        List<Object[]> monthlyData = billingRecordRepository.getMonthlyRevenueStats(sixMonthsAgo);
        List<BillingStatsDto.MonthlyRevenueDto> monthlyTrends = monthlyData.stream()
                .map(data -> new BillingStatsDto.MonthlyRevenueDto(
                        (Integer) data[0], // year
                        (Integer) data[1], // month
                        (BigDecimal) data[2] // revenue
                ))
                .collect(Collectors.toList());
        stats.setMonthlyTrends(monthlyTrends);

        // 최근 실패한 결제 목록 (최근 10건)
        List<BillingRecord> failedPayments = billingRecordRepository.findByPaymentStatusOrderByCreatedAtDesc(BillingRecord.PaymentStatus.FAILED)
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        List<BillingStatsDto.FailedPaymentDto> recentFailedPayments = failedPayments.stream()
                .map(payment -> {
                    // 테넌트 정보 조회
                    String companyName = tenantRepository.findById(payment.getTenantId())
                            .map(Tenant::getCompanyName)
                            .orElse("알 수 없음");

                    return new BillingStatsDto.FailedPaymentDto(
                            payment.getId(),
                            payment.getTenantId().toString(),
                            companyName,
                            payment.getTotalAmount(),
                            payment.getFailureReason(),
                            payment.getCreatedAt().toString()
                    );
                })
                .collect(Collectors.toList());
        stats.setRecentFailedPayments(recentFailedPayments);

        log.debug("결제 통계 정보 조회 완료 - 총 매출: {}, 이번 달 매출: {}", stats.getTotalRevenue(), stats.getMonthlyRevenue());
        return stats;
    }

    /**
     * 최근 생성된 테넌트 목록 조회
     */
    public List<TenantSummaryDto> getRecentTenants() {
        log.debug("최근 생성된 테넌트 목록 조회 시작");

        List<Tenant> recentTenants = tenantRepository.findTop10ByOrderByCreatedAtDesc();
        
        return recentTenants.stream()
                .map(tenant -> {
                    long userCount = userRepository.countByTenantId(tenant.getId());
                    return TenantSummaryDto.from(tenant, userCount);
                })
                .collect(Collectors.toList());
    }
}