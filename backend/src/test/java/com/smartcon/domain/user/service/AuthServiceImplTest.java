package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.LoginRequest;
import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.RefreshTokenRequest;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 구현체 단위 테스트
 * 개선된 로그인 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenBlacklistService blacklistService;

    private JwtTokenService jwtTokenService;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private AuthenticationAuditService auditService;

    @BeforeEach
    void setUp() {
        // JWT 토큰 서비스 초기화
        jwtTokenService = new JwtTokenService(
            "test-secret-key-for-jwt-token-service-testing-purpose-only",
            60L, // 60분
            7L   // 7일
        );
        passwordEncoder = new BCryptPasswordEncoder();
        
        // BusinessNumberValidator 모의 객체 생성
        BusinessNumberValidator businessNumberValidator = Mockito.mock(BusinessNumberValidator.class);
        Mockito.when(businessNumberValidator.validate(Mockito.anyString())).thenReturn(true);
        Mockito.when(businessNumberValidator.normalize(Mockito.anyString())).thenAnswer(i -> i.getArgument(0));
        
        // AuthenticationAuditService 모의 객체 생성
        auditService = Mockito.mock(AuthenticationAuditService.class);
        
        authService = new AuthServiceImpl(
            userRepository,
            jwtTokenService,
            blacklistService,
            passwordEncoder,
            businessNumberValidator,
            auditService
        );
    }

    @Test
    @DisplayName("로그인 성공 - 유효한 사용자와 비밀번호")
    void loginSuccess() {
        // Given
        String email = "test@smartcon.com";
        String password = "password123";
        String tenantId = "1";
        
        User user = createValidUser(email, password, tenantId);
        LoginRequest request = LoginRequest.builder()
            .email(email)
            .password(password)
            .tenantId(tenantId)
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // 로그인 성공 시에는 실패 횟수가 0이므로 save가 호출되지 않음

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo(email);
        assertThat(response.getUser().getRole()).isEqualTo("ROLE_WORKER");
        assertThat(response.getUser().getPermissions()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void loginFailure_UserNotFound() {
        // Given
        LoginRequest request = LoginRequest.builder()
            .email("nonexistent@smartcon.com")
            .password("password123")
            .tenantId("1")
            .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginFailure_WrongPassword() {
        // Given
        String email = "test@smartcon.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String tenantId = "1";
        
        User user = createValidUser(email, correctPassword, tenantId);
        LoginRequest request = LoginRequest.builder()
            .email(email)
            .password(wrongPassword)
            .tenantId(tenantId)
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        
        // 실패 횟수가 증가했는지 확인
        verify(userRepository).save(argThat(savedUser -> 
            savedUser.getLoginFailureCount() > 0));
    }

    @Test
    @DisplayName("로그인 실패 - 계정 잠금")
    void loginFailure_AccountLocked() {
        // Given
        String email = "locked@smartcon.com";
        String password = "password123";
        String tenantId = "1";
        
        User user = createValidUser(email, password, tenantId);
        user.setLoginFailureCount(5); // 계정 잠금 상태
        
        LoginRequest request = LoginRequest.builder()
            .email(email)
            .password(password)
            .tenantId(tenantId)
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("계정이 잠겨있습니다");
    }

    @Test
    @DisplayName("로그인 실패 - 비활성 계정")
    void loginFailure_InactiveAccount() {
        // Given
        String email = "inactive@smartcon.com";
        String password = "password123";
        String tenantId = "1";
        
        User user = createValidUser(email, password, tenantId);
        user.setIsActive(false); // 비활성 계정
        
        LoginRequest request = LoginRequest.builder()
            .email(email)
            .password(password)
            .tenantId(tenantId)
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("비활성화된 계정입니다");
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 미인증")
    void loginFailure_EmailNotVerified() {
        // Given
        String email = "unverified@smartcon.com";
        String password = "password123";
        String tenantId = "1";
        
        User user = createValidUser(email, password, tenantId);
        user.setIsEmailVerified(false); // 이메일 미인증
        user.setBusinessNumber("123-45-67890"); // 사업자 계정
        
        LoginRequest request = LoginRequest.builder()
            .email(email)
            .password(password)
            .tenantId(tenantId)
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이메일 인증이 필요합니다");
    }

    @Test
    @DisplayName("개발용 토큰 생성 - 기본값 사용")
    void generateDevToken_WithDefaults() {
        // Given
        String role = null; // 기본값 사용
        String tenantId = null; // 기본값 사용

        // When
        LoginResponse response = authService.generateDevToken(role, tenantId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getRole()).isEqualTo("ROLE_SUPER"); // 기본값
        assertThat(response.getUser().getTenantId()).isEqualTo("dev-tenant"); // 기본값
        assertThat(response.getUser().getPermissions()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("개발용 토큰 생성 - 지정된 역할과 테넌트")
    void generateDevToken_WithSpecifiedValues() {
        // Given
        String role = "ROLE_HQ";
        String tenantId = "test-tenant";

        // When
        LoginResponse response = authService.generateDevToken(role, tenantId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUser().getRole()).isEqualTo("ROLE_HQ");
        assertThat(response.getUser().getTenantId()).isEqualTo("test-tenant");
        
        // HQ 역할의 권한이 포함되어 있는지 확인
        assertThat(response.getUser().getPermissions())
            .containsKeys("tenant.read", "user.read", "attendance.read");
    }

    @Test
    @DisplayName("토큰 검증 - 유효한 토큰")
    void validateToken_ValidToken() {
        // Given
        String role = "ROLE_SUPER";
        String tenantId = "1";
        LoginResponse devTokenResponse = authService.generateDevToken(role, tenantId);
        String token = devTokenResponse.getAccessToken();

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 유효한 Refresh Token")
    void refreshToken_Success() {
        // Given
        String email = "test@smartcon.com";
        String password = "password123";
        String tenantId = "1";
        
        User user = createValidUser(email, password, tenantId);
        
        // 개발용 토큰으로 Refresh Token 생성
        LoginResponse devTokenResponse = authService.generateDevToken("ROLE_WORKER", tenantId);
        String refreshToken = devTokenResponse.getRefreshToken();
        
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken(refreshToken)
            .build();

        // 개발용 토큰은 실제 사용자 조회 없이 처리되므로 mock 설정 불필요

        // When
        LoginResponse response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken); // 기존 Refresh Token 재사용
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("dev@smartcon.com"); // 개발용 토큰의 이메일
        assertThat(response.getUser().getRole()).isEqualTo("ROLE_WORKER");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refreshToken_InvalidToken() {
        // Given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken("invalid.token.here")
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("유효하지 않은 Refresh Token입니다");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Access Token으로 갱신 시도")
    void refreshToken_WrongTokenType() {
        // Given
        LoginResponse devTokenResponse = authService.generateDevToken("ROLE_WORKER", "1");
        String accessToken = devTokenResponse.getAccessToken(); // Access Token을 사용
        
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken(accessToken)
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Refresh Token이 아닙니다");
    }

    // === 테스트 헬퍼 메서드 ===

    private User createValidUser(String email, String password, String tenantId) {
        User user = User.builder()
            .name("테스트 사용자")
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .isActive(true)
            .isEmailVerified(true)
            .loginFailureCount(0)
            .build();
        user.addRole(Role.ROLE_WORKER);
        
        user.setTenantId(Long.parseLong(tenantId));
        user.setId(1L);
        return user;
    }
}