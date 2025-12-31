package com.smartcon.global.security;

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

/**
 * JWT 토큰 검증 필터
 * 개발 단계에서는 간단한 토큰 검증을 수행합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
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
            // 개발 단계에서는 슈퍼관리자 API가 아닌 경우 통과
            if (!requestURI.startsWith("/api/v1/admin/")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            log.warn("슈퍼관리자 API 접근 시도 - 인증 토큰 없음: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"인증 토큰이 필요합니다.\"}");
            return;
        }
        
        String token = authHeader.substring(7); // "Bearer " 제거
        
        try {
            // 개발 단계에서는 간단한 토큰 검증
            if (validateToken(token)) {
                String role = extractRoleFromToken(token);
                
                // 슈퍼관리자 API 접근 시 권한 확인
                if (requestURI.startsWith("/api/v1/admin/") && !"SUPER_ADMIN".equals(role)) {
                    log.warn("슈퍼관리자 API 접근 거부 - 권한 부족: {} (역할: {})", requestURI, role);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"슈퍼관리자 권한이 필요합니다.\"}");
                    return;
                }
                
                // 인증 정보 설정
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken("user", null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 토큰 검증 성공 - URI: {}, 역할: {}", requestURI, role);
            } else {
                log.warn("JWT 토큰 검증 실패 - URI: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"유효하지 않은 토큰입니다.\"}");
                return;
            }
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생 - URI: {}, 오류: {}", requestURI, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"토큰 처리 중 오류가 발생했습니다.\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
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
     * 토큰 유효성 검증 (개발용 간단한 구현)
     */
    private boolean validateToken(String token) {
        // 개발 단계에서는 간단한 토큰 검증
        // 실제 운영에서는 JWT 라이브러리를 사용하여 서명 검증 등을 수행해야 함
        
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // 개발용 토큰 패턴 검증
        if (token.startsWith("dev-super-admin-")) {
            return true;
        }
        
        if (token.startsWith("dev-admin-")) {
            return true;
        }
        
        if (token.startsWith("dev-user-")) {
            return true;
        }
        
        // 기본적으로 유효하지 않은 토큰으로 처리
        return false;
    }
    
    /**
     * 토큰에서 역할 추출 (개발용 간단한 구현)
     */
    private String extractRoleFromToken(String token) {
        // 개발 단계에서는 토큰 패턴으로 역할 결정
        if (token.startsWith("dev-super-admin-")) {
            return "SUPER_ADMIN";
        }
        
        if (token.startsWith("dev-admin-")) {
            return "ADMIN";
        }
        
        if (token.startsWith("dev-user-")) {
            return "USER";
        }
        
        return "USER"; // 기본 역할
    }
}