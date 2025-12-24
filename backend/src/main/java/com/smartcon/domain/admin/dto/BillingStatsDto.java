package com.smartcon.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 결제 통계 정보 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillingStatsDto {

    // 전체 통계
    private BigDecimal totalRevenue;        // 전체 매출
    private BigDecimal monthlyRevenue;      // 이번 달 매출
    private BigDecimal dailyRevenue;        // 오늘 매출

    // 결제 건수 통계
    private long totalPayments;             // 전체 결제 건수
    private long completedPayments;         // 완료된 결제 건수
    private long failedPayments;            // 실패한 결제 건수
    private long pendingPayments;           // 대기중인 결제 건수

    // 월별 매출 추이
    private List<MonthlyRevenueDto> monthlyTrends;

    // 실패한 결제 목록 (최근 10건)
    private List<FailedPaymentDto> recentFailedPayments;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyRevenueDto {
        private int year;
        private int month;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailedPaymentDto {
        private Long id;
        private String tenantId;
        private String companyName;
        private BigDecimal amount;
        private String failureReason;
        private String createdAt;
    }
}