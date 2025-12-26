package com.smartcon.domain.subscription.entity;

/**
 * 알림 타입 열거형
 */
public enum NotificationType {
    /**
     * 구독 신청 알림
     */
    SUBSCRIPTION_REQUEST,
    
    /**
     * 구독 승인 알림
     */
    SUBSCRIPTION_APPROVED,
    
    /**
     * 구독 거부 알림
     */
    SUBSCRIPTION_REJECTED,
    
    /**
     * 구독 중지 알림
     */
    SUBSCRIPTION_SUSPENDED,
    
    /**
     * 구독 종료 알림
     */
    SUBSCRIPTION_TERMINATED,
    
    /**
     * 승인 리마인더 알림
     */
    APPROVAL_REMINDER,
    
    /**
     * 구독 재활성화 알림
     */
    SUBSCRIPTION_REACTIVATED
}