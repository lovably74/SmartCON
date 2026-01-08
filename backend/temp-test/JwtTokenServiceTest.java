package com.smartcon.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 토큰 서비스 테스트
 */
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        // 테스트용 JWT 토큰 서비스 초기화
        jwtTokenService = new JwtTokenService(
                "test-secret-key-for-jwt-token-service-testing-only",
                60, // 60분
                7   // 7일
        );
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        // Given
        String userId = "test-user-123";
        String tenantId = "test-tenant-456";
        String role = "ROLE_SUPER";
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("admin.read", true);
        permissions.put("admin.write", true);

        // When
        String accessToken = jwtTokenService.generateAccessToken(userId, tenantId, role, permissions);

        // Then
        assertNotNull(accessToken);
        assertTrue(jwtTokenService.validateToken(accessToken));
        assertTrue(jwtTokenService.isAccessToken(accessToken));
        assertFalse(jwtTokenService.isRefreshToken(accessToken));
        
        // 토큰에서 정보 추출 검증
        assertEquals(userId, jwtTokenService.extractUserId(accessToken));
        assertEquals(tenantId, jwtTokenService.extractTenantId(accessToken));
        assertEquals(role, jwtTokenService.extractRole(accessToken));
        
        Map<String, Object> extractedPermissions = jwtTokenService.extractPermissions(accessToken);
        assertEquals(true, extractedPermissions.get("admin.read"));
        assertEquals(true, extractedPermissions.get("admin.write"));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        // Given
        String userId = "test-user-123";
        String tenantId = "test-tenant-456";

        // When
        String refreshToken = jwtTokenService.generateRefreshToken(userId, tenantId);

        // Then
        assertNotNull(refreshToken);
        assertTrue(jwtTokenService.validateToken(refreshToken));
        assertFalse(jwtTokenService.isAccessToken(refreshToken));
        assertTrue(jwtTokenService.isRefreshToken(refreshToken));
        
        // 토큰에서 정보 추출 검증
        assertEquals(userId, jwtTokenService.extractUserId(refreshToken));
        assertEquals(tenantId, jwtTokenService.extractTenantId(refreshToken));
    }

    @Test
    void testInvalidTokenValidation() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertFalse(jwtTokenService.validateToken(invalidToken));
        assertFalse(jwtTokenService.validateToken(null));
        assertFalse(jwtTokenService.validateToken(""));
    }

    @Test
    void testTokenExpiration() {
        // Given - 만료 시간이 매우 짧은 토큰 서비스
        JwtTokenService shortExpiryService = new JwtTokenService(
                "test-secret-key-for-jwt-token-service-testing-only",
                0, // 0분 (즉시 만료)
                0  // 0일 (즉시 만료)
        );

        String userId = "test-user-123";
        String tenantId = "test-tenant-456";
        String role = "ROLE_USER";
        Map<String, Object> permissions = new HashMap<>();

        // When
        String accessToken = shortExpiryService.generateAccessToken(userId, tenantId, role, permissions);

        // Then - 토큰이 생성되지만 즉시 만료됨
        assertNotNull(accessToken);
        
        // 짧은 시간 후 토큰이 만료되었는지 확인
        try {
            Thread.sleep(1000); // 1초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(shortExpiryService.isTokenExpired(accessToken));
    }
}