package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 승인 처리 성능 분석 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalPerformanceDto {
    
    /**
     * 승인 액션
     */
    private ApprovalAction action;
    
    /**
     * 총 처리 건수
     */
    private long totalCount;
    
    /**
     * 최소 처리 시간 (분)
     */
    private double minProcessingMinutes;
    
    /**
     * 최대 처리 시간 (분)
     */
    private double maxProcessingMinutes;
    
    /**
     * 평균 처리 시간 (분)
     */
    private double avgProcessingMinutes;
    
    /**
     * 표준편차 (분)
     */
    private double stddevProcessingMinutes;
    
    /**
     * 액션 타입을 한국어로 반환
     */
    public String getActionDisplayName() {
        switch (action) {
            case APPROVE:
                return "승인";
            case REJECT:
                return "거부";
            case SUSPEND:
                return "중지";
            case TERMINATE:
                return "종료";
            case REACTIVATE:
                return "재활성화";
            case AUTO_APPROVE:
                return "자동승인";
            default:
                return action.name();
        }
    }
    
    /**
     * 평균 처리 시간을 시간:분 형태로 반환
     */
    public String getFormattedAvgProcessingTime() {
        return formatMinutes(avgProcessingMinutes);
    }
    
    /**
     * 최소 처리 시간을 시간:분 형태로 반환
     */
    public String getFormattedMinProcessingTime() {
        return formatMinutes(minProcessingMinutes);
    }
    
    /**
     * 최대 처리 시간을 시간:분 형태로 반환
     */
    public String getFormattedMaxProcessingTime() {
        return formatMinutes(maxProcessingMinutes);
    }
    
    /**
     * 분을 시간:분 형태로 포맷팅
     */
    private String formatMinutes(double minutes) {
        if (minutes < 60) {
            return String.format("%.0f분", minutes);
        } else {
            int hours = (int) (minutes / 60);
            int mins = (int) (minutes % 60);
            return String.format("%d시간 %d분", hours, mins);
        }
    }
    
    /**
     * 성능 등급 반환 (평균 처리 시간 기준)
     */
    public String getPerformanceGrade() {
        if (avgProcessingMinutes <= 30) {
            return "매우 빠름";
        } else if (avgProcessingMinutes <= 60) {
            return "빠름";
        } else if (avgProcessingMinutes <= 180) {
            return "보통";
        } else if (avgProcessingMinutes <= 360) {
            return "느림";
        } else {
            return "매우 느림";
        }
    }
}