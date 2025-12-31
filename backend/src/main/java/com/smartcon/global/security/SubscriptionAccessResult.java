package com.smartcon.global.security;

import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 구독 접근 제어 결과
 */
@Getter
@Builder
public class SubscriptionAccessResult {
    
    /**
     * 접근 허용 여부
     */
    private final boolean allowed;
    
    /**
     * 구독 상태
     */
    private final SubscriptionStatus subscriptionStatus;
    
    /**
     * 접근 거부 메시지
     */
    private final String message;
    
    /**
     * 리다이렉션 URL
     */
    private final String redirectUrl;
    
    /**
     * 접근 허용 결과 생성
     */
    public static SubscriptionAccessResult allowed() {
        return SubscriptionAccessResult.builder()
                .allowed(true)
                .build();
    }
    
    /**
     * 구독 상태와 함께 접근 허용 결과 생성
     */
    public static SubscriptionAccessResult allowedWithStatus(SubscriptionStatus status) {
        return SubscriptionAccessResult.builder()
                .allowed(true)
                .subscriptionStatus(status)
                .build();
    }
    
    /**
     * 특별한 사유로 접근 허용 결과 생성 (메시지 포함)
     */
    public static SubscriptionAccessResult allowedWithMessage(String message) {
        return SubscriptionAccessResult.builder()
                .allowed(true)
                .message(message)
                .build();
    }
    
    /**
     * 접근 거부 결과 생성
     */
    public static SubscriptionAccessResult denied(SubscriptionStatus status, String message, String redirectUrl) {
        return SubscriptionAccessResult.builder()
                .allowed(false)
                .subscriptionStatus(status)
                .message(message)
                .redirectUrl(redirectUrl)
                .build();
    }
}