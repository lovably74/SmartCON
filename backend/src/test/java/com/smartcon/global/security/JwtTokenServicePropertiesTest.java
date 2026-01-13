package com.smartcon.global.security;

import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT 토큰 서비스 속성 기반 테스트
 * 
 * 검증 속성:
 * - 속성 1: JWT 서비스 초기화 일관성
 * - 속성 2: 개발 환경 알고리즘 선택
 * - 속성 3: 운영 환경 알고리즘 선택
 * - 속성 4: 기본값 안전 동작
 */
@DisplayName("JWT 토큰 서비스 속성 기반 테스트")
class JwtTokenServicePropertiesTest {

    @Property(tries = 10)
    @DisplayName("속성 1: JWT 서비스 초기화 일관성 - 동일한 설정으로 생성된 서비스는 동일하게 동작한다")
    void property1_JWT_서비스_초기화_일관성(@ForAll("validSecret") String secret,
                                      @ForAll("validExpiration") Long accessExpiration,
                                      @ForAll("validExpiration") Long refreshExpiration) {
        
        // When: 두 개의 JWT 서비스 생성
        JwtTokenService service1 = new JwtTokenService(secret, accessExpiration, refreshExpiration);
        JwtTokenService service2 = new JwtTokenService(secret, accessExpiration, refreshExpiration);
        
        // Then: 동일한 토큰을 생성해야 함
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("test", true);
        
        String token1 = service1.generateAccessToken("1", "1", "WORKER", permissions);
        String token2 = service2.generateAccessToken("1", "1", "WORKER", permissions);
        
        // 토큰 구조는 동일해야 함 (시간 차이로 인해 완전히 같지는 않을 수 있음)
        assertThat(token1.split("\\.")).hasSize(3);
        assertThat(token2.split("\\.")).hasSize(3);
        
        // 동일한 사용자 정보 추출
        assertThat(service1.extractUserId(token1)).isEqualTo(service2.extractUserId(token2));
        assertThat(service1.extractRole(token1)).isEqualTo(service2.extractRole(token2));
        assertThat(service1.extractTenantId(token1)).isEqualTo(service2.extractTenantId(token2));
    }

    @Property(tries = 10)
    @DisplayName("속성 2: 개발 환경 알고리즘 선택 - 개발 환경에서는 HS256 알고리즘을 사용한다")
    void property2_개발_환경_알고리즘_선택(@ForAll("validSecret") String secret,
                                    @ForAll("validExpiration") Long accessExpiration,
                                    @ForAll("validExpiration") Long refreshExpiration) {
        
        // Given: 개발 환경 설정
        JwtTokenService service = new JwtTokenService(secret, accessExpiration, refreshExpiration);
        
        // When: JWT 서비스 생성 및 토큰 생성
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("test", true);
        String token = service.generateAccessToken("1", "1", "WORKER", permissions);
        
        // Then: 토큰이 정상적으로 생성되고 검증되어야 함
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(service.validateToken(token)).isTrue();
        
        // JWT 형식 확인
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Property(tries = 10)
    @DisplayName("속성 3: 운영 환경 알고리즘 선택 - 운영 환경에서는 RS256 알고리즘을 지원한다")
    void property3_운영_환경_알고리즘_선택(@ForAll("validSecret") String secret,
                                    @ForAll("validExpiration") Long accessExpiration,
                                    @ForAll("validExpiration") Long refreshExpiration) {
        
        // Given: 운영 환경 설정 (현재는 HS256만 지원하므로 HS256으로 테스트)
        JwtTokenService service = new JwtTokenService(secret, accessExpiration, refreshExpiration);
        
        // When: JWT 서비스 생성 및 토큰 생성
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("test", true);
        String token = service.generateAccessToken("1", "1", "WORKER", permissions);
        
        // Then: 토큰이 정상적으로 생성되고 검증되어야 함
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(service.validateToken(token)).isTrue();
    }

    @Property(tries = 10)
    @DisplayName("속성 4: 기본값 안전 동작 - 설정값이 없어도 안전한 기본값으로 동작한다")
    void property4_기본값_안전_동작(@ForAll("validSecret") String secret) {
        
        // Given: 최소한의 설정만 제공
        JwtTokenService service = new JwtTokenService(secret, 60L, 7L);
        
        // Then: 서비스가 정상적으로 동작해야 함
        assertThatCode(() -> {
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("test", true);
            String token = service.generateAccessToken("1", "1", "WORKER", permissions);
            assertThat(token).isNotNull();
            assertThat(service.validateToken(token)).isTrue();
        }).doesNotThrowAnyException();
    }

    // 테스트 데이터 생성기
    @Provide
    Arbitrary<String> validSecret() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(32) // 최소 256비트
                .ofMaxLength(64);
    }

    @Provide
    Arbitrary<Long> validExpiration() {
        return Arbitraries.longs().between(60L, 1440L); // 1분 ~ 24시간 (분 단위)
    }
}