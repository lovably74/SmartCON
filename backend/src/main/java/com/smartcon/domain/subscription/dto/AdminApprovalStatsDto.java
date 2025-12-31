package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자별 승인 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApprovalStatsDto {
    
    /**
     * 관리자 이름
     */
    private String adminName;
    
    /**
     * 관리자 이메일
     */
    private String adminEmail;
    
    /**
     * 승인 액션
     */
    private ApprovalAction action;
    
    /**
     * 승인 처리 건수
     */
    private long approvalCount;
    
    /**
     * 평균 처리 시간 (분)
     */
    private double avgProcessingMinutes;
    
    /**
     * 평균 처리 시간을 시간:분 형태로 반환
     */
    public String getFormattedAvgProcessingTime() {
        if (avgProcessingMinutes < 60) {
            return String.format("%.0f분", avgProcessingMinutes);
        } else {
            int hours = (int) (avgProcessingMinutes / 60);
            int minutes = (int) (avgProcessingMinutes % 60);
            return String.format("%d시간 %d분", hours, minutes);
        }
    }
}