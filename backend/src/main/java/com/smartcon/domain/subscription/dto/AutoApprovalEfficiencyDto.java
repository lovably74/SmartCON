package com.smartcon.domain.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자동 승인 효율성 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoApprovalEfficiencyDto {
    
    /**
     * 자동 승인된 건수
     */
    private long autoApproved;
    
    /**
     * 수동 처리된 건수
     */
    private long manualProcessed;
    
    /**
     * 자동 승인률 (%)
     */
    private double autoApprovalRate;
    
    /**
     * 전체 처리 건수
     */
    public long getTotalProcessed() {
        return autoApproved + manualProcessed;
    }
    
    /**
     * 수동 처리율 계산
     */
    public double getManualProcessingRate() {
        return 100.0 - autoApprovalRate;
    }
    
    /**
     * 효율성 등급 반환
     */
    public String getEfficiencyGrade() {
        if (autoApprovalRate >= 80) {
            return "매우 높음";
        } else if (autoApprovalRate >= 60) {
            return "높음";
        } else if (autoApprovalRate >= 40) {
            return "보통";
        } else if (autoApprovalRate >= 20) {
            return "낮음";
        } else {
            return "매우 낮음";
        }
    }
}