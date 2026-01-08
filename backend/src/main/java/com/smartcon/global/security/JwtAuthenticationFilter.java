package com.smartcon.global.security;

import com.smartcon.global.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JWT 토큰 검증 필터
 * 실제 JWT 토큰 서비스를 사용한 토큰 검증 및 인증 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenService jwtTokenService;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 인증이 필요 없는 경로는 건너뛰기
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        // Authorization 헤더가 없는 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 슈퍼관리자 API가 아닌 경우 통과 (개발 단계)
            if (!requestURI.startsWith("/api/v1/admin/")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            log.warn("슈퍼관리자 API 접근 시도 - 인증 토큰 없음: {}", requestURI);
            sendUnauthorizedResponse(response, "인증 토큰이 필요합니다");
            return;
        }
        
        String token = authHeader.substring(7); // "Bearer " 제거
        
        try {
            // JWT 토큰 검증
            if (!jwtTokenService.validateToken(token)) {
                log.warn("JWT 토큰 검증 실패 - URI: {}", requestURI);
                sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다");
                return;
            }

            // Access Token인지 확인
            if (!jwtTokenService.isAccessToken(token)) {
                log.warn("Access Token이 아닌 토큰으로 API 접근 시도 - URI: {}", requestURI);
                sendUnauthorizedResponse(response, "Access Token이 필요합니다");
                return;
            }

            // 토큰에서 사용자 정보 추출
            String userId = jwtTokenService.extractUserId(token);
            String tenantId = jwtTokenService.extractTenantId(token);
            String role = jwtTokenService.extractRole(token);
            Map<String, Object> permissions = jwtTokenService.extractPermissions(token);

            // 테넌트 컨텍스트 설정
            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
            }

            // 슈퍼관리자 API 접근 시 권한 확인
            if (requestURI.startsWith("/v1/admin/") && !"ROLE_SUPER".equals(role)) {
                log.warn("슈퍼관리자 API 접근 거부 - 권한 부족: {} (역할: {})", requestURI, role);
                sendForbiddenResponse(response, "슈퍼관리자 권한이 필요합니다");
                return;
            }
            
            // 인증 정보 설정
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            
            // 추가 정보 설정
            authentication.setDetails(Map.of(
                "tenantId", tenantId,
                "permissions", permissions
            ));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("JWT 토큰 검증 성공 - URI: {}, 사용자: {}, 역할: {}", requestURI, userId, role);
            
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생 - URI: {}, 오류: {}", requestURI, e.getMessage());
            sendUnauthorizedResponse(response, "토큰 처리 중 오류가 발생했습니다");
            return;
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 테넌트 컨텍스트 정리
            TenantContext.clear();
        }
    }
    
    /**
     * 공개 경로인지 확인
     */
    private boolean isPublicPath(String requestURI) {
        return requestURI.startsWith("/api/h2-console/") ||
               requestURI.startsWith("/api/actuator/") ||
               requestURI.startsWith("/api/v1/auth/") ||
               requestURI.equals("/api/v1/subscriptions/plans") ||
               requestURI.equals("/api/v1/subscriptions/create") ||
               requestURI.equals("/api/v1/subscriptions/current") ||
               requestURI.startsWith("/static/") ||
               requestURI.startsWith("/assets/") ||
               requestURI.endsWith(".js") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".ico");
    }
    
    /**
     * 401 Unauthorized 응답 전송
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
            "{\"error\":\"UNAUTHORIZED\",\"message\":\"%s\"}", message));
    }
    
    /**
     * 403 Forbidden 응답 전송
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
            "{\"error\":\"FORBIDDEN\",\"message\":\"%s\"}", message));
    }
}