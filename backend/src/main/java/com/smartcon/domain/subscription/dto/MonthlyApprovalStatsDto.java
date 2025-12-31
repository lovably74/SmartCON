package com.smartcon.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 월별 승인 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyApprovalStatsDto {
    
    /**
     * 연도
     */
    private int year;
    
    /**
     * 월
     */
    private int month;
    
    /**
     * 총 신청 수
     */
    private long totalRequests;
    
    /**
     * 승인된 수
     */
    private long approvedCount;
    
    /**
     * 거부된 수
     */
    private long rejectedCount;
    
    /**
     * 승인률 계산
     */
    public double getApprovalRate() {
        long processedCount = approvedCount + rejectedCount;
        return processedCount > 0 ? (double) approvedCount / processedCount * 100.0 : 0.0;
    }
    
    /**
     * 월 표시용 문자열
     */
    public String getMonthLabel() {
        return String.format("%d년 %d월", year, month);
    }
}