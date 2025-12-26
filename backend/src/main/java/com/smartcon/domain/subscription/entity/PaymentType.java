package com.smartcon.domain.subscription.entity;

/**
 * 결제 수단 유형 열거형
 */
public enum PaymentType {
    /**
     * 신용카드
     */
    CARD,
    
    /**
     * 계좌이체
     */
    BANK_TRANSFER,
    
    /**
     * 가상계좌
     */
    VIRTUAL_ACCOUNT
}