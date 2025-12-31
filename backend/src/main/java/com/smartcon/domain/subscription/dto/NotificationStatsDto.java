package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 알림 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsDto {
    
    /**
     * 알림 타입
     */
    private NotificationType type;
    
    /**
     * 총 발송 수
     */
    private long totalSent;
    
    /**
     * 총 읽음 수
     */
    private long totalRead;
    
    /**
     * 읽음률 (%)
     */
    private double readRate;
    
    /**
     * 평균 읽음 시간 (분)
     */
    private double avgReadTimeMinutes;
    
    /**
     * 알림 타입을 한국어로 반환
     */
    public String getTypeDisplayName() {
        switch (type) {
            case SUBSCRIPTION_REQUEST:
                return "구독 신청";
            case SUBSCRIPTION_APPROVED:
                return "구독 승인";
            case SUBSCRIPTION_REJECTED:
                return "구독 거부";
            case SUBSCRIPTION_SUSPENDED:
                return "구독 중지";
            case SUBSCRIPTION_TERMINATED:
                return "구독 종료";
            case SUBSCRIPTION_REACTIVATED:
                return "구독 재활성화";
            case APPROVAL_REMINDER:
                return "승인 리마인더";
            default:
                return type.name();
        }
    }
    
    /**
     * 평균 읽음 시간을 시간:분 형태로 반환
     */
    public String getFormattedAvgReadTime() {
        if (avgReadTimeMinutes < 60) {
            return String.format("%.0f분", avgReadTimeMinutes);
        } else {
            int hours = (int) (avgReadTimeMinutes / 60);
            int minutes = (int) (avgReadTimeMinutes % 60);
            return String.format("%d시간 %d분", hours, minutes);
        }
    }
    
    /**
     * 읽지 않은 수 계산
     */
    public long getTotalUnread() {
        return totalSent - totalRead;
    }
}