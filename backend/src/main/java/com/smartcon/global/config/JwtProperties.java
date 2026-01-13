package com.smartcon.global.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 속성 클래스
 * application.yml의 jwt 설정을 바인딩하여 타입 안전성 제공
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT 서명용 비밀키 (HMAC 알고리즘용)
     */
    private String secret = "smartcon-jwt-secret-key-for-development-only-change-in-production";
    
    /**
     * Access Token 만료 시간 (분 단위)
     */
    private long accessTokenExpirationMinutes = 60;
    
    /**
     * Refresh Token 만료 시간 (일 단위)
     */
    private long refreshTokenExpirationDays = 7;
    
    /**
     * RSA 알고리즘 사용 여부 (true: RSA256, false: HMAC-SHA256)
     */
    private boolean useRsa = false;
    
    /**
     * JWT 발급자 (iss 클레임)
     */
    private String issuer = "smartcon-lite";
    
    /**
     * JWT 대상 (aud 클레임)
     */
    private String audience = "smartcon-api";
}