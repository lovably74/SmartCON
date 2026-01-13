package com.smartcon.domain.user.controller;

import com.smartcon.domain.user.service.AuthService;
import com.smartcon.global.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthController 토큰 검증 API 테스트
 * 요구사항 9.1, 9.2, 9.3, 9.4 검증
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTokenValidationTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Mock 초기화는 @Mock 어노테이션으로 자동 처리됨
    }

    @Test
    @DisplayName("유효한 토큰 검증 - 성공 응답 (요구사항 9.2)")
    void validateToken_ValidToken_Success() {
        // Given
        String authHeader = "Bearer valid-jwt-token";
        when(authService.validateToken("valid-jwt-token")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("유효한 토큰입니다");
        
        verify(authService).validateToken("valid-jwt-token");
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 - 실패 응답 (요구사항 9.2)")
    void validateToken_InvalidToken_Failure() {
        // Given
        String authHeader = "Bearer invalid-jwt-token";
        when(authService.validateToken("invalid-jwt-token")).thenReturn(false);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).isNull();
        assertThat(response.getBody().getMessage()).contains("INVALID_TOKEN");
        
        verify(authService).validateToken("invalid-jwt-token");
    }

    @Test
    @DisplayName("Authorization 헤더 없음 - 오류 처리 (요구사항 9.3)")
    void validateToken_MissingAuthHeader_Error() {
        // Given
        String authHeader = null;

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("MISSING_TOKEN");
        assertThat(response.getBody().getMessage()).isEqualTo("MISSING_TOKEN: Authorization 헤더가 누락되었습니다");
        
        verify(authService, never()).validateToken(any());
    }

    @Test
    @DisplayName("빈 Authorization 헤더 - 오류 처리 (요구사항 9.3)")
    void validateToken_EmptyAuthHeader_Error() {
        // Given
        String authHeader = "";

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("MISSING_TOKEN");
        
        verify(authService, never()).validateToken(any());
    }

    @Test
    @DisplayName("잘못된 토큰 형식 - Bearer 없음 (요구사항 9.3)")
    void validateToken_InvalidFormat_NoBearerPrefix_Error() {
        // Given
        String authHeader = "invalid-token-format";

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("INVALID_TOKEN_FORMAT");
        assertThat(response.getBody().getMessage()).isEqualTo("INVALID_TOKEN_FORMAT: Bearer 토큰 형식이 아닙니다");
        
        verify(authService, never()).validateToken(any());
    }

    @Test
    @DisplayName("빈 토큰 - Bearer 뒤에 토큰 없음 (요구사항 9.3)")
    void validateToken_EmptyToken_Error() {
        // Given
        String authHeader = "Bearer ";

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("EMPTY_TOKEN");
        assertThat(response.getBody().getMessage()).isEqualTo("EMPTY_TOKEN: 토큰이 비어있습니다");
        
        verify(authService, never()).validateToken(any());
    }

    @Test
    @DisplayName("공백만 있는 토큰 - 오류 처리 (요구사항 9.3)")
    void validateToken_WhitespaceOnlyToken_Error() {
        // Given
        String authHeader = "Bearer    ";

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("EMPTY_TOKEN");
        
        verify(authService, never()).validateToken(any());
    }

    @Test
    @DisplayName("AuthService 예외 발생 - 안전한 처리 (요구사항 9.4)")
    void validateToken_ServiceException_SafeHandling() {
        // Given
        String authHeader = "Bearer some-token";
        when(authService.validateToken("some-token")).thenThrow(new RuntimeException("예상치 못한 오류"));

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("INTERNAL_ERROR: 토큰 검증 처리 중 오류가 발생했습니다");
        
        verify(authService).validateToken("some-token");
    }

    @Test
    @DisplayName("토큰 앞뒤 공백 제거 - 정상 처리 (요구사항 9.4)")
    void validateToken_TokenWithWhitespace_TrimmedCorrectly() {
        // Given
        String authHeader = "Bearer   valid-token-with-spaces   ";
        when(authService.validateToken("valid-token-with-spaces")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = authController.validateToken(authHeader);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isTrue();
        
        verify(authService).validateToken("valid-token-with-spaces");
    }
}