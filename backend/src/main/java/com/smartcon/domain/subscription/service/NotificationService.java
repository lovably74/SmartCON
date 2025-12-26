package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 알림 서비스 인터페이스
 */
public interface NotificationService {
    
    /**
     * 구독 신청 알림 발송
     */
    void sendSubscriptionRequestNotification(Long subscriptionId);
    
    /**
     * 승인 결과 알림 발송
     */
    void sendApprovalResultNotification(Long subscriptionId, boolean approved, String reason);
    
    /**
     * 리마인더 알림 발송
     */
    void sendReminderNotifications();
    
    /**
     * 구독 중지 알림 발송
     */
    void sendSubscriptionSuspendedNotification(Long subscriptionId, String reason);
    
    /**
     * 구독 종료 알림 발송
     */
    void sendSubscriptionTerminatedNotification(Long subscriptionId, String reason);
    
    /**
     * 구독 재활성화 알림 발송
     */
    void sendSubscriptionReactivatedNotification(Long subscriptionId);
    
    /**
     * 사용자별 알림 조회
     */
    Page<NotificationDto> getNotifications(Long userId, Pageable pageable);
    
    /**
     * 읽지 않은 알림 조회
     */
    List<NotificationDto> getUnreadNotifications(Long userId);
    
    /**
     * 읽지 않은 알림 개수 조회
     */
    long getUnreadNotificationCount(Long userId);
    
    /**
     * 알림을 읽음으로 표시
     */
    void markAsRead(Long notificationId);
    
    /**
     * 모든 알림을 읽음으로 표시
     */
    void markAllAsRead(Long userId);
    
    /**
     * 오래된 읽은 알림 정리
     */
    void cleanupOldNotifications();
}