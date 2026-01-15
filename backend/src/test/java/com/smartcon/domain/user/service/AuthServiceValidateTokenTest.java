package com.smartcon.domain.user.service;

import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.security.JwtTokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthService validateToken 메서드 테스트
 * 실제 구현에 맞춘 테스트 (블랙리스트 서비스 미통합 상태)
 * 요구사항 9.1, 9.3, 9.4 검증
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceValidateTokenTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtTokenBlacklistService blacklistService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // BusinessNumberValidator Mock 생성
        BusinessNumberValidator mockBusinessNumberValidator = Mockito.mock(BusinessNumberValidator.class);
        Mockito.when(mockBusinessNumberValidator.validate(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockBusinessNumberValidator.normalize(Mockito.anyString())).thenAnswer(i -> i.getArgument(0));
        
        authService = new AuthServiceImpl(userRepository, jwtTokenService, blacklistService, passwordEncoder, mockBusinessNumberValidator);
    }

    @Test
    @DisplayName("유효한 토큰 검증 - 성공 (요구사항 9.1)")
    void validateToken_ValidToken_Success() {
        // Given
        String validToken = "valid-jwt-token";
        when(jwtTokenService.validateToken(validToken)).thenReturn(true);

        // When
        boolean result = authService.validateToken(validToken);

        // Then
        assertThat(result).isTrue();
        verify(jwtTokenService).validateToken(validToken);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 - 실패 (요구사항 9.1)")
    void validateToken_InvalidToken_Failure() {
        // Given
        String invalidToken = "invalid-jwt-token";
        when(jwtTokenService.validateToken(invalidToken)).thenReturn(false);

        // When
        boolean result = authService.validateToken(invalidToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtTokenService).validateToken(invalidToken);
    }

    @Test
    @DisplayName("null 토큰 검증 - 실패 (요구사항 9.3)")
    void validateToken_NullToken_Failure() {
        // Given
        String nullToken = null;

        // When
        boolean result = authService.validateToken(nullToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtTokenService, never()).validateToken(any());
    }

    @Test
    @DisplayName("빈 토큰 검증 - 실패 (요구사항 9.3)")
    void validateToken_EmptyToken_Failure() {
        // Given
        String emptyToken = "";

        // When
        boolean result = authService.validateToken(emptyToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtTokenService, never()).validateToken(any());
    }

    @Test
    @DisplayName("공백만 있는 토큰 검증 - 실패 (요구사항 9.3)")
    void validateToken_WhitespaceOnlyToken_Failure() {
        // Given
        String whitespaceToken = "   ";

        // When
        boolean result = authService.validateToken(whitespaceToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtTokenService, never()).validateToken(any());
    }

    @Test
    @DisplayName("토큰 앞뒤 공백 제거 후 검증 (요구사항 9.4)")
    void validateToken_TokenWithWhitespace_TrimmedAndValidated() {
        // Given
        String tokenWithWhitespace = "  valid-token  ";
        String trimmedToken = "valid-token";
        when(jwtTokenService.validateToken(trimmedToken)).thenReturn(true);

        // When
        boolean result = authService.validateToken(tokenWithWhitespace);

        // Then
        assertThat(result).isTrue();
        verify(jwtTokenService).validateToken(trimmedToken);
    }

    @Test
    @DisplayName("JWT 서비스 예외 발생 - 안전한 처리 (요구사항 9.3, 9.4)")
    void validateToken_JwtServiceException_SafeHandling() {
        // Given
        String token = "some-token";
        when(jwtTokenService.validateToken(token)).thenThrow(new RuntimeException("JWT 처리 오류"));

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertThat(result).isFalse();
        verify(jwtTokenService).validateToken(token);
    }
}