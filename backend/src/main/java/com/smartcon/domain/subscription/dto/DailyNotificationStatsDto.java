package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일별 알림 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyNotificationStatsDto {
    
    /**
     * 알림 날짜
     */
    private LocalDate notificationDate;
    
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
     * 읽음률 계산
     */
    public double getReadRate() {
        return totalSent > 0 ? (double) totalRead / totalSent * 100.0 : 0.0;
    }
    
    /**
     * 읽지 않은 수 계산
     */
    public long getTotalUnread() {
        return totalSent - totalRead;
    }
    
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
}