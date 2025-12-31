package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일별 승인 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyApprovalStatsDto {
    
    /**
     * 승인 날짜
     */
    private LocalDate approvalDate;
    
    /**
     * 승인 액션
     */
    private ApprovalAction action;
    
    /**
     * 총 처리 건수
     */
    private long totalProcessed;
    
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
}