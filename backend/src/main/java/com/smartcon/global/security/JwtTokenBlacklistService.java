package com.smartcon.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 블랙리스트 서비스
 * Spring Security 6.x 최적화 및 안전한 로그아웃 구현
 * 메모리 기반 블랙리스트 (운영 환경에서는 Redis 권장)
 */
@Service
@Slf4j
public class JwtTokenBlacklistService {

    private final ConcurrentHashMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final JwtTokenService jwtTokenService;

    public JwtTokenBlacklistService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
        
        // 만료된 토큰 정리 작업 (1시간마다 실행)
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
        
        log.info("JWT 토큰 블랙리스트 서비스 초기화 완료");
    }

    /**
     * 토큰을 블랙리스트에 추가 (로그아웃 시 사용)
     * @param token JWT 토큰
     */
    public void blacklistToken(String token) {
        try {
            Date expiration = jwtTokenService.extractExpiration(token);
            blacklistedTokens.put(token, expiration);
            log.debug("토큰이 블랙리스트에 추가됨 - 만료 시간: {}", expiration);
        } catch (Exception e) {
            log.warn("토큰 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * 만료된 토큰들을 블랙리스트에서 제거
     */
    private void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int removedCount = 0;
        
        var iterator = blacklistedTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().toInstant().isBefore(now)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.info("만료된 블랙리스트 토큰 {} 개 정리 완료", removedCount);
        }
    }

    /**
     * 블랙리스트 통계 정보
     * @return 현재 블랙리스트에 있는 토큰 수
     */
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }

    /**
     * 서비스 종료 시 스케줄러 정리
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("JWT 토큰 블랙리스트 서비스 종료 완료");
    }
}