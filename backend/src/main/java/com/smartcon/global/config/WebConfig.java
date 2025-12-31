package com.smartcon.global.config;

import com.smartcon.global.security.ApiAuditInterceptor;
import com.smartcon.global.security.SubscriptionAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final SubscriptionAccessInterceptor subscriptionAccessInterceptor;
    private final ApiAuditInterceptor apiAuditInterceptor;
    
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // API 감사 추적 인터셉터 등록 (가장 먼저 실행)
        registry.addInterceptor(apiAuditInterceptor)
                .addPathPatterns("/api/v1/**")
                .order(1);
        
        // 구독 접근 제어 인터셉터 등록
        registry.addInterceptor(subscriptionAccessInterceptor)
                .addPathPatterns("/api/v1/**")  // API 경로에만 적용
                .excludePathPatterns(
                    "/api/v1/auth/**",          // 인증 관련 경로 제외
                    "/api/v1/subscriptions/**", // 구독 관련 경로 제외
                    "/api/v1/admin/**",         // 슈퍼관리자 경로 제외
                    "/api/actuator/**",         // Actuator 제외
                    "/api/h2-console/**",       // H2 콘솔 제외
                    "/api/health"               // 헬스체크 제외
                )
                .order(2);
    }
}