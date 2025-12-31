package com.smartcon.global.security;

import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.service.SubscriptionAccessControlService;
import com.smartcon.global.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 구독 상태별 서비스 접근 제어 인터셉터
 * 테넌트의 구독 상태에 따라 API 접근을 제어합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAccessInterceptor implements HandlerInterceptor {
    
    private final SubscriptionAccessControlService accessControlService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("구독 접근 제어 검사 - URI: {}, Method: {}", requestURI, method);
        
        // 접근 제어 제외 경로 확인
        if (isExcludedPath(requestURI)) {
            log.debug("접근 제어 제외 경로: {}", requestURI);
            return true;
        }
        
        // 테넌트 컨텍스트 확인
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            log.debug("테넌트 컨텍스트가 없음 - 접근 허용");
            return true;
        }
        
        // 구독 상태별 접근 제어 검사
        SubscriptionAccessResult accessResult = accessControlService.checkAccess(tenantId, requestURI);
        
        if (!accessResult.isAllowed()) {
            log.warn("구독 상태로 인한 접근 거부 - 테넌트: {}, 상태: {}, URI: {}", 
                    tenantId, accessResult.getSubscriptionStatus(), requestURI);
            
            // 접근 거부 응답 설정
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorResponse = String.format(
                "{\"error\":\"ACCESS_DENIED\",\"message\":\"%s\",\"subscriptionStatus\":\"%s\",\"redirectUrl\":\"%s\"}", 
                accessResult.getMessage(),
                accessResult.getSubscriptionStatus(),
                accessResult.getRedirectUrl()
            );
            
            response.getWriter().write(errorResponse);
            return false;
        }
        
        log.debug("구독 접근 제어 통과 - 테넌트: {}", tenantId);
        return true;
    }
    
    /**
     * 접근 제어 제외 경로 확인
     */
    private boolean isExcludedPath(String requestURI) {
        // 인증 관련 경로
        if (requestURI.startsWith("/api/v1/auth/")) {
            return true;
        }
        
        // 구독 관련 경로 (구독 신청, 상태 조회 등)
        if (requestURI.startsWith("/api/v1/subscriptions/")) {
            return true;
        }
        
        // 슈퍼관리자 경로
        if (requestURI.startsWith("/api/v1/admin/")) {
            return true;
        }
        
        // 헬스체크 및 시스템 경로
        if (requestURI.startsWith("/api/actuator/") || 
            requestURI.startsWith("/api/h2-console/") ||
            requestURI.equals("/api/health")) {
            return true;
        }
        
        // 정적 리소스
        if (requestURI.startsWith("/static/") || 
            requestURI.startsWith("/assets/") ||
            requestURI.endsWith(".js") ||
            requestURI.endsWith(".css") ||
            requestURI.endsWith(".ico")) {
            return true;
        }
        
        return false;
    }
}