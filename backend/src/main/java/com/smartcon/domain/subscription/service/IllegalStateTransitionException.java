package com.smartcon.domain.subscription.service;

/**
 * 잘못된 구독 상태 전환 예외
 * 
 * 비즈니스 규칙에 위반되는 상태 전환 시도 시 발생합니다.
 */
public class IllegalStateTransitionException extends RuntimeException {
    
    public IllegalStateTransitionException(String message) {
        super(message);
    }
    
    public IllegalStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}