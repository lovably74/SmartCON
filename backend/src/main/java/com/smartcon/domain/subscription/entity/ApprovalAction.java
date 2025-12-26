package com.smartcon.domain.subscription.entity;

/**
 * 승인 액션 열거형
 */
public enum ApprovalAction {
    /**
     * 승인
     */
    APPROVE,
    
    /**
     * 거부
     */
    REJECT,
    
    /**
     * 중지
     */
    SUSPEND,
    
    /**
     * 종료
     */
    TERMINATE,
    
    /**
     * 재활성화
     */
    REACTIVATE,
    
    /**
     * 자동 승인
     */
    AUTO_APPROVE
}