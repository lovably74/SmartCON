package com.smartcon.global.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT 토큰 서비스 간단한 테스트
 * jqwik 테스트가 작동하지 않는 경우를 위한 대안 테스트
 */
class JwtTokenServiceSimpleTest {

    @Test
    @DisplayName("JWT 서비스 기본 초기화 테스트")
    void jwtServiceBasicInitialization() {
        // Given: 기본 JWT 설정
        // When: JWT 서비스 초기화
        JwtTokenService service = new JwtTokenService(
            "test-secret-key-for-jwt-token-generation-minimum-256-bits",
            60L, // 60분
            7L   // 7일
        );
        
        // Then: 토큰 생성 및 검증이 정상적으로 작동해야 함
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("admin.read", true);
        
        String accessToken = service.generateAccessToken("test-user", "1", "ROLE_SUPER", permissions);
        String refreshToken = service.generateRefreshToken("test-user", "1");
        
        assertThat(accessToken).isNotNull().isNotEmpty();
        assertThat(refreshToken).isNotNull().isNotEmpty();
        assertThat(service.validateToken(accessToken)).isTrue();
        assertThat(service.validateToken(refreshToken)).isTrue();
        
        // 토큰에서 정보 추출 확인
        assertThat(service.extractUserId(accessToken)).isEqualTo("test-user");
        assertThat(service.extractTenantId(accessToken)).isEqualTo("1");
        assertThat(service.extractRole(accessToken)).isEqualTo("ROLE_SUPER");
        assertThat(service.isAccessToken(accessToken)).isTrue();
        assertThat(service.isRefreshToken(refreshToken)).isTrue();
    }
}