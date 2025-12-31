package com.smartcon.domain.subscription.service;

import com.smartcon.global.security.SubscriptionAccessResult;

/**
 * 구독 상태별 접근 제어 서비스 인터페이스
 */
public interface SubscriptionAccessControlService {
    
    /**
     * 테넌트의 서비스 접근 권한 확인
     * 
     * @param tenantId 테넌트 ID
     * @param requestPath 요청 경로
     * @return 접근 제어 결과
     */
    SubscriptionAccessResult checkAccess(Long tenantId, String requestPath);
    
    /**
     * 구독 상태별 서비스 접근 가능 여부 확인
     * 
     * @param tenantId 테넌트 ID
     * @return 서비스 접근 가능 여부
     */
    boolean isServiceAccessible(Long tenantId);
    
    /**
     * 구독 상태별 안내 메시지 조회
     * 
     * @param tenantId 테넌트 ID
     * @return 상태별 안내 메시지
     */
    String getStatusMessage(Long tenantId);
}