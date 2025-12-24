package com.smartcon.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * 개발 단계에서는 인증을 비활성화하고 CORS를 허용
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (개발용)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 정책 설정 (JWT 사용 시 STATELESS)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 권한 설정 - 개발 단계에서는 모든 요청 허용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/h2-console/**").permitAll()  // H2 콘솔 접근 허용
                .requestMatchers("/api/api/v1/admin/**").permitAll() // 슈퍼관리자 API 허용 (개발용)
                .requestMatchers("/api/actuator/**").permitAll()     // Actuator 허용
                .anyRequest().permitAll()  // 개발 단계에서는 모든 요청 허용
            )
            
            // H2 콘솔을 위한 헤더 설정
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );

        return http.build();
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