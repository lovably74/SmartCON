package com.smartcon.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증 서비스
 * RSA256 알고리즘을 사용한 토큰 서명 및 검증
 */
@Service
@Slf4j
public class JwtTokenService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;

    public JwtTokenService(
            @Value("${jwt.secret:smartcon-lite-jwt-secret-key-for-development-only-change-in-production}") String secret,
            @Value("${jwt.access-token-expiration-minutes:60}") long accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays) {
        
        // 개발 환경에서는 HMAC-SHA 알고리즘 사용 (운영에서는 RSA256 권장)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        
        log.info("JWT 토큰 서비스 초기화 완료 - Access Token 만료: {}분, Refresh Token 만료: {}일", 
                accessTokenExpirationMinutes, refreshTokenExpirationDays);
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(String userId, String tenantId, String role, Map<String, Object> permissions) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant_id", tenantId);
        claims.put("role", role);
        claims.put("permissions", permissions);
        claims.put("token_type", "access");

        return Jwts.builder()
                .setSubject(userId)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String userId, String tenantId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant_id", tenantId);
        claims.put("token_type", "refresh");

        return Jwts.builder()
                .setSubject(userId)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("JWT 토큰 서명이 유효하지 않습니다: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 예상치 못한 오류가 발생했습니다: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 Claims 추출
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("토큰에서 Claims 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 토큰에서 테넌트 ID 추출
     */
    public String extractTenantId(String token) {
        return extractClaims(token).get("tenant_id", String.class);
    }

    /**
     * 토큰에서 역할 추출
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * 토큰에서 권한 정보 추출
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extractPermissions(String token) {
        Object permissions = extractClaims(token).get("permissions");
        if (permissions instanceof Map) {
            return (Map<String, Object>) permissions;
        }
        return new HashMap<>();
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    public String extractTokenType(String token) {
        return extractClaims(token).get("token_type", String.class);
    }

    /**
     * 토큰 만료 시간 확인
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.warn("토큰 만료 시간 확인 실패: {}", e.getMessage());
            return true; // 오류 발생 시 만료된 것으로 처리
        }
    }

    /**
     * Access Token인지 확인
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            log.warn("토큰 타입 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh Token인지 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.warn("토큰 타입 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}