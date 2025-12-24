package com.smartcon.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 대시보드 통계 정보 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {

    // 테넌트 관련 통계
    private long totalTenants;          // 전체 테넌트 수
    private long activeTenants;         // 활성 테넌트 수
    private long suspendedTenants;      // 중지된 테넌트 수
    private long newTenantsThisMonth;   // 이번 달 신규 테넌트 수

    // 사용자 관련 통계
    private long totalUsers;            // 전체 사용자 수
    private long newUsersThisMonth;     // 이번 달 신규 사용자 수

    // 결제 관련 통계
    private BigDecimal totalRevenue;        // 전체 매출
    private BigDecimal monthlyRevenue;      // 이번 달 매출
    private long completedPayments;         // 완료된 결제 건수
    private long failedPayments;            // 실패한 결제 건수
    private long pendingPayments;           // 대기중인 결제 건수

    // 시스템 상태
    private String systemStatus;        // 시스템 상태 (HEALTHY, WARNING, ERROR)
    private long activeConnections;     // 활성 연결 수
}