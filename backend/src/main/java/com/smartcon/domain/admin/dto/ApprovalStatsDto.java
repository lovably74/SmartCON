package com.smartcon.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 승인 대시보드 통계 정보 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalStatsDto {

    // 구독 승인 관련 통계
    private long totalSubscriptions;        // 전체 구독 수
    private long pendingApprovals;          // 승인 대기 중인 구독 수
    private long approvedSubscriptions;     // 승인된 구독 수
    private long rejectedSubscriptions;     // 거부된 구독 수
    private long suspendedSubscriptions;    // 중지된 구독 수
    private long terminatedSubscriptions;   // 종료된 구독 수
    
    // 자동 승인 관련 통계
    private long autoApprovedCount;         // 자동 승인된 구독 수
    private long manualApprovalCount;       // 수동 승인된 구독 수
    private double autoApprovalRate;        // 자동 승인 비율 (%)
    
    // 처리 시간 관련 통계
    private double averageProcessingHours;  // 평균 처리 시간 (시간)
    private long pendingOverThreeDays;      // 3일 이상 대기 중인 구독 수
}