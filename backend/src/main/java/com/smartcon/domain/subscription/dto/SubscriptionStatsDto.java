package com.smartcon.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구독 통계 DTO
 * 대시보드에서 사용되는 구독 상태별 통계 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatsDto {
    
    /**
     * 승인 대기 중인 구독 수
     */
    private long pendingCount;
    
    /**
     * 활성 구독 수
     */
    private long activeCount;
    
    /**
     * 중지된 구독 수
     */
    private long suspendedCount;
    
    /**
     * 종료된 구독 수
     */
    private long terminatedCount;
    
    /**
     * 거부된 구독 수
     */
    private long rejectedCount;
    
    /**
     * 전체 구독 수
     */
    public long getTotalCount() {
        return pendingCount + activeCount + suspendedCount + terminatedCount + rejectedCount;
    }
    
    /**
     * 승인률 계산 (활성 구독 / 전체 처리된 구독)
     */
    public double getApprovalRate() {
        long processedCount = activeCount + rejectedCount + terminatedCount;
        return processedCount > 0 ? (double) activeCount / processedCount * 100.0 : 0.0;
    }
}