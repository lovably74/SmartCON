package com.smartcon.global.config;

import com.smartcon.global.security.JwtAuthenticationFilter;
import com.smartcon.global.security.RoleBasedAccessControl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * 5단계 역할 기반 접근 제어 (RBAC) 구현
 * JWT 토큰 검증 및 계층적 권한 구조 적용
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoleBasedAccessControl roleBasedAccessControl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 정책 설정 (JWT 사용 시 STATELESS)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // JWT 인증 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 권한 설정 - 5단계 역할 기반 접근 제어
            .authorizeHttpRequests(auth -> auth
                // H2 콘솔 접근 허용 (개발용)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Actuator 허용
                .requestMatchers("/actuator/**").permitAll()
                
                // 인증 관련 경로 허용
                .requestMatchers("/v1/auth/**").permitAll()
                
                // 구독 신청 관련 경로 허용 (테넌트가 구독 신청할 수 있어야 함)
                .requestMatchers("/v1/subscriptions/plans").permitAll()
                .requestMatchers("/v1/subscriptions/create").permitAll()
                .requestMatchers("/v1/subscriptions/current").permitAll()
                
                // === 슈퍼관리자 전용 API (ROLE_SUPER) ===
                .requestMatchers("/v1/admin/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/subscriptions/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/auto-approval/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/notifications/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/tenants/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/system/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/dashboard/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/billing/**").hasRole("SUPER")
                
                // === 본사관리자 이상 API (ROLE_HQ, ROLE_SUPER) ===
                .requestMatchers("/v1/hq/**").hasAnyRole("HQ", "SUPER")
                .requestMatchers("/v1/projects/create").hasAnyRole("HQ", "SUPER")
                .requestMatchers("/v1/projects/*/delete").hasAnyRole("HQ", "SUPER")
                .requestMatchers("/v1/users/admins/**").hasAnyRole("HQ", "SUPER")
                
                // === 현장관리자 이상 API (ROLE_SITE, ROLE_HQ, ROLE_SUPER) ===
                .requestMatchers("/v1/site/**").hasAnyRole("SITE", "HQ", "SUPER")
                .requestMatchers("/v1/attendance/modify").hasAnyRole("SITE", "HQ", "SUPER")
                .requestMatchers("/v1/workers/approve").hasAnyRole("SITE", "HQ", "SUPER")
                .requestMatchers("/v1/contracts/*/resend").hasAnyRole("SITE", "HQ", "SUPER")
                
                // === 노무팀장 이상 API (ROLE_TEAM, ROLE_SITE, ROLE_HQ, ROLE_SUPER) ===
                .requestMatchers("/v1/team/**").hasAnyRole("TEAM", "SITE", "HQ", "SUPER")
                .requestMatchers("/v1/attendance/team/**").hasAnyRole("TEAM", "SITE", "HQ", "SUPER")
                
                // === 일반노무자 이상 API (모든 인증된 사용자) ===
                .requestMatchers("/v1/worker/**").hasAnyRole("WORKER", "TEAM", "SITE", "HQ", "SUPER")
                .requestMatchers("/v1/profile/**").hasAnyRole("WORKER", "TEAM", "SITE", "HQ", "SUPER")
                .requestMatchers("/v1/attendance/my/**").hasAnyRole("WORKER", "TEAM", "SITE", "HQ", "SUPER")
                .requestMatchers("/v1/contracts/my/**").hasAnyRole("WORKER", "TEAM", "SITE", "HQ", "SUPER")
                
                // 일반 API - 인증 필요 (개발 단계에서는 임시로 허용)
                .requestMatchers("/v1/**").permitAll()
                
                // 기타 모든 요청 허용 (개발 단계)
                .anyRequest().permitAll()
            )
            
            // H2 콘솔을 위한 헤더 설정
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 설정
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더 설정
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 자격 증명 허용
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}