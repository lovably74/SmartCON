package com.smartcon.global.config;

import com.smartcon.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * 구독 승인 워크플로우를 위한 권한 기반 접근 제어 강화
 * JWT 토큰 검증 및 역할 기반 접근 제어 구현
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
            
            // 권한 설정 - 구독 승인 워크플로우 보안 강화
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
                
                // 슈퍼관리자 전용 API - 강화된 보안 (JWT 필터에서 처리)
                .requestMatchers("/v1/admin/**").hasRole("SUPER")
                
                // 구독 승인 관련 API - 슈퍼관리자 전용
                .requestMatchers("/v1/admin/subscriptions/**").hasRole("SUPER")
                
                // 자동 승인 규칙 관리 - 슈퍼관리자 전용
                .requestMatchers("/v1/admin/auto-approval/**").hasRole("SUPER")
                
                // 알림 관리 - 슈퍼관리자 전용
                .requestMatchers("/v1/admin/notifications/**").hasRole("SUPER")
                
                // 테넌트 관리 - 슈퍼관리자 전용
                .requestMatchers("/v1/admin/tenants/**").hasRole("SUPER")
                
                // 시스템 모니터링 - 슈퍼관리자 전용
                .requestMatchers("/v1/admin/system/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/dashboard/**").hasRole("SUPER")
                .requestMatchers("/v1/admin/billing/**").hasRole("SUPER")
                
                // 일반 API - 인증 필요 (개발 단계에서는 임시로 허용)
                .requestMatchers("/v1/**").permitAll()
                
                // 기타 모든 요청 허용 (개발 단계)
                .anyRequest().permitAll()
            )
            
            // H2 콘솔을 위한 헤더 설정 (deprecated 메서드 대신 새로운 방식 사용)
            .headers(headers -> headers
                .frameOptions().sameOrigin()
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