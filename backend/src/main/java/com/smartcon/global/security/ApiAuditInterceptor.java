package com.smartcon.global.security;

import com.smartcon.global.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Enumeration;

/**
 * API 요청 로깅 및 감사 추적 인터셉터
 * 슈퍼관리자 API 요청에 대한 상세한 로깅을 수행합니다.
 */
@Component
@Slf4j
public class ApiAuditInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 슈퍼관리자 API 요청만 상세 로깅
        if (isAdminApiRequest(requestURI)) {
            logAdminApiRequest(request);
        } else {
            // 일반 API 요청은 간단한 로깅
            log.debug("API 요청 - {} {}", method, requestURI);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();
        
        // 슈퍼관리자 API 응답 로깅
        if (isAdminApiRequest(requestURI)) {
            logAdminApiResponse(request, response, ex);
        }
        
        // 오류 발생 시 추가 로깅
        if (ex != null) {
            log.error("API 요청 처리 중 오류 발생 - {} {} - 상태: {} - 오류: {}", 
                    method, requestURI, status, ex.getMessage(), ex);
        }
    }
    
    /**
     * 슈퍼관리자 API 요청인지 확인
     */
    private boolean isAdminApiRequest(String requestURI) {
        return requestURI.startsWith("/api/v1/admin/");
    }
    
    /**
     * 슈퍼관리자 API 요청 상세 로깅
     */
    private void logAdminApiRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        Long tenantId = TenantContext.getCurrentTenantId();
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("슈퍼관리자 API 요청 시작\n");
        logMessage.append("  시간: ").append(LocalDateTime.now()).append("\n");
        logMessage.append("  메서드: ").append(method).append("\n");
        logMessage.append("  URI: ").append(requestURI).append("\n");
        
        if (queryString != null) {
            logMessage.append("  쿼리: ").append(queryString).append("\n");
        }
        
        logMessage.append("  클라이언트 IP: ").append(remoteAddr).append("\n");
        logMessage.append("  User-Agent: ").append(userAgent).append("\n");
        
        if (tenantId != null) {
            logMessage.append("  테넌트 ID: ").append(tenantId).append("\n");
        }
        
        // 요청 헤더 로깅 (민감한 정보 제외)
        logMessage.append("  헤더:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!isSensitiveHeader(headerName)) {
                String headerValue = request.getHeader(headerName);
                logMessage.append("    ").append(headerName).append(": ").append(headerValue).append("\n");
            }
        }
        
        log.info(logMessage.toString());
    }
    
    /**
     * 슈퍼관리자 API 응답 로깅
     */
    private void logAdminApiResponse(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        int status = response.getStatus();
        String contentType = response.getContentType();
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("슈퍼관리자 API 요청 완료\n");
        logMessage.append("  시간: ").append(LocalDateTime.now()).append("\n");
        logMessage.append("  메서드: ").append(method).append("\n");
        logMessage.append("  URI: ").append(requestURI).append("\n");
        logMessage.append("  응답 상태: ").append(status).append("\n");
        
        if (contentType != null) {
            logMessage.append("  Content-Type: ").append(contentType).append("\n");
        }
        
        if (ex != null) {
            logMessage.append("  오류: ").append(ex.getMessage()).append("\n");
        }
        
        // 응답 상태에 따른 로그 레벨 조정
        if (status >= 500) {
            log.error(logMessage.toString());
        } else if (status >= 400) {
            log.warn(logMessage.toString());
        } else {
            log.info(logMessage.toString());
        }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 민감한 헤더인지 확인
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        return lowerHeaderName.contains("authorization") ||
               lowerHeaderName.contains("cookie") ||
               lowerHeaderName.contains("token") ||
               lowerHeaderName.contains("password") ||
               lowerHeaderName.contains("secret");
    }
}