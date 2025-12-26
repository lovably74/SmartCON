package com.smartcon.domain.subscription.entity;

/**
 * 구독 상태 열거형
 */
public enum SubscriptionStatus {
    /**
     * 승인 대기 - 구독 신청 후 슈퍼관리자 승인 대기 중
     */
    PENDING_APPROVAL,
    
    /**
     * 승인 거부 - 슈퍼관리자가 구독 신청을 거부함
     */
    REJECTED,
    
    /**
     * 자동 승인 - 자동 승인 규칙에 의해 승인됨
     */
    AUTO_APPROVED,
    
    /**
     * 활성 - 정상적으로 서비스 이용 중
     */
    ACTIVE,
    
    /**
     * 일시 중지 - 결제 실패 등으로 일시 중지
     */
    SUSPENDED,
    
    /**
     * 해지됨 - 사용자가 해지 신청
     */
    CANCELLED,
    
    /**
     * 만료됨 - 구독 기간 만료
     */
    EXPIRED,
    
    /**
     * 종료됨 - 슈퍼관리자가 강제 종료함
     */
    TERMINATED,
    
    /**
     * 체험판 - 무료 체험 기간
     */
    TRIAL
}