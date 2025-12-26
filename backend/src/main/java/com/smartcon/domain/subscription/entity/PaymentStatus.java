package com.smartcon.domain.subscription.entity;

/**
 * 결제 상태 열거형
 */
public enum PaymentStatus {
    /**
     * 결제 대기 중
     */
    PENDING,
    
    /**
     * 결제 성공
     */
    SUCCESS,
    
    /**
     * 결제 실패
     */
    FAILED,
    
    /**
     * 결제 취소
     */
    CANCELLED,
    
    /**
     * 환불 완료
     */
    REFUNDED
}