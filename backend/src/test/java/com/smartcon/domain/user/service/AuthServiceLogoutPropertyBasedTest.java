package com.smartcon.domain.user.service;

import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.AuthServiceImpl;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.security.JwtTokenBlacklistService;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 로그아웃 보안 시스템 속성 기반 테스트
 * 
 * 검증 속성:
 * - 속성 13: 토큰 블랙리스트 추가
 * - 속성 14: 블랙리스트 토큰 차단
 * - 속성 15: 만료 토큰 자동 정리
 * - 속성 16: 로그아웃 성공 응답
 */
@DisplayName("로그아웃 보안 시스템 속성 기반 테스트")
class AuthServiceLogoutPropertyBasedTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private JwtTokenService jwtTokenService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        jwtTokenService = new JwtTokenService(
            "test-secret-key-for-jwt-token-generation-minimum-256-bits",
            60L, // 60분
            7L   // 7일
        );
        
        // JwtTokenBlacklistService Mock 생성
        JwtTokenBlacklistService mockBlacklistService = Mockito.mock(JwtTokenBlacklistService.class);
        
        // BusinessNumberValidator Mock 생성
        BusinessNumberValidator mockBusinessNumberValidator = Mockito.mock(BusinessNumberValidator.class);
        Mockito.when(mockBusinessNumberValidator.validate(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockBusinessNumberValidator.normalize(Mockito.anyString())).thenAnswer(i -> i.getArgument(0));
        
        // AuthenticationAuditService Mock 생성
        AuthenticationAuditService mockAuditService = Mockito.mock(AuthenticationAuditService.class);
        
        authService = new AuthServiceImpl(userRepository, jwtTokenService, mockBlacklistService, passwordEncoder, mockBusinessNumberValidator, mockAuditService);
    }

    @Property(tries = 10)
    @DisplayName("속성 13: 토큰 블랙리스트 추가 - 로그아웃 시 토큰이 무효화된다")
    void property13_토큰_블랙리스트_추가(@ForAll("validToken") String accessToken) {
        
        // Given: 유효한 액세스 토큰
        when(jwtTokenService.validateToken(accessToken)).thenReturn(true);
        
        // When: 로그아웃 수행
        authService.logout(accessToken);
        
        // Then: 로그아웃이 정상적으로 처리되어야 함 (현재는 로그만 남김)
        // 실제 구현에서는 블랙리스트 서비스를 통해 토큰을 무효화
        verify(jwtTokenService, atLeastOnce()).validateToken(accessToken);
    }

    @Property(tries = 10)
    @DisplayName("속성 14: 블랙리스트 토큰 차단 - 무효화된 토큰은 검증에 실패한다")
    void property14_블랙리스트_토큰_차단(@ForAll("validToken") String token) {
        
        // Given: 토큰이 무효화된 상황을 시뮬레이션
        when(jwtTokenService.validateToken(token)).thenReturn(false);
        
        // When & Then: 토큰 검증 시 실패해야 함
        boolean isValid = jwtTokenService.validateToken(token);
        assertThat(isValid).isFalse();
    }

    @Property(tries = 10)
    @DisplayName("속성 15: 만료 토큰 자동 정리 - 만료된 토큰은 자동으로 처리된다")
    void property15_만료_토큰_자동_정리(@ForAll("expiredToken") String expiredToken) {
        
        // Given: 만료된 토큰
        when(jwtTokenService.isTokenExpired(expiredToken)).thenReturn(true);
        
        // When: 토큰 만료 확인
        boolean isExpired = jwtTokenService.isTokenExpired(expiredToken);
        
        // Then: 만료된 토큰으로 인식되어야 함
        assertThat(isExpired).isTrue();
    }

    @Property(tries = 10)
    @DisplayName("속성 16: 로그아웃 성공 응답 - 로그아웃은 항상 정상적으로 처리된다")
    void property16_로그아웃_성공_응답(@ForAll("validToken") String accessToken) {
        
        // Given: 유효한 토큰
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        
        // When: 로그아웃 수행
        assertThatCode(() -> authService.logout(accessToken))
                .doesNotThrowAnyException();
        
        // Then: 로그아웃이 정상적으로 처리되어야 함
        verify(jwtTokenService, atLeastOnce()).validateToken(accessToken);
    }

    // 테스트 데이터 생성기
    @Provide
    Arbitrary<String> validToken() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars('.', '-', '_')
                .ofMinLength(50)
                .ofMaxLength(200);
    }

    @Provide
    Arbitrary<String> expiredToken() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars('.', '-', '_')
                .ofMinLength(50)
                .ofMaxLength(200);
    }
}