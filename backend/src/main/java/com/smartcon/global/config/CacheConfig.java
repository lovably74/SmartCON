package com.smartcon.global.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * 캐시 설정
 * 
 * 자동 승인 규칙, 대시보드 통계 등의 데이터를 캐싱하여 성능을 향상시킵니다.
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {
    
    /**
     * 캐시 매니저 설정
     * 
     * 운영 환경에서는 Redis 등의 분산 캐시를 사용할 수 있습니다.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // 캐시 이름 정의
        cacheManager.setCacheNames(Arrays.asList(
            "autoApprovalRules",        // 자동 승인 규칙
            "subscriptionStats",        // 구독 통계
            "approvalStats",           // 승인 통계
            "notificationStats",       // 알림 통계
            "tenantInfo",              // 테넌트 정보
            "adminInfo"                // 관리자 정보
        ));
        
        // 캐시 설정
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}