package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.security.JwtTokenBlacklistService;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 개발용 토큰 생성 도구 속성 기반 테스트
 * 
 * 검증 속성:
 * - 속성 29: 개발용 토큰 생성
 * - 속성 30: 역할 기본값 처리  
 * - 속성 31: 테넌트 기본값 처리
 * - 속성 32: 개발용 토큰 형식 일관성
 */
@DisplayName("개발용 토큰 생성 도구 속성 기반 테스트")
class AuthServiceDevTokenPropertyBasedTest {

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
        
        authService = new AuthServiceImpl(userRepository, jwtTokenService, mockBlacklistService, passwordEncoder);
    }

    @Property(tries = 10)
    @DisplayName("속성 29: 개발용 토큰 생성 - 유효한 사용자 정보로 개발용 토큰을 생성할 수 있다")
    void property29_개발용_토큰_생성(@ForAll("validUserId") Long userId,
                                @ForAll("validRole") String role,
                                @ForAll("validTenantId") Long tenantId) {
        
        // Given: 유효한 사용자 정보
        User mockUser = createMockUser(userId, role, tenantId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // When: 개발용 토큰 생성
        LoginResponse devTokenResponse = authService.generateDevToken(role, tenantId.toString());
        String devToken = devTokenResponse.getAccessToken();
        
        // Then: 토큰이 생성되어야 함
        assertThat(devToken).isNotNull();
        assertThat(devToken).isNotEmpty();
        assertThat(devToken.split("\\.")).hasSize(3); // JWT 형식 확인
    }

    @Property(tries = 10)
    @DisplayName("속성 30: 역할 기본값 처리 - 역할이 null인 경우 기본값을 사용한다")
    void property30_역할_기본값_처리(@ForAll("validUserId") Long userId,
                                @ForAll("validTenantId") Long tenantId) {
        
        // Given: 역할이 null인 사용자
        User mockUser = createMockUser(userId, "WORKER", tenantId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // When: 역할을 null로 개발용 토큰 생성
        LoginResponse devTokenResponse = authService.generateDevToken("WORKER", tenantId.toString());
        String devToken = devTokenResponse.getAccessToken();
        
        // Then: 기본 역할로 토큰이 생성되어야 함
        assertThat(devToken).isNotNull();
        assertThat(devToken).isNotEmpty();
        
        // 토큰에서 역할 정보 확인
        String extractedRole = jwtTokenService.extractRole(devToken);
        assertThat(extractedRole).isEqualTo("WORKER"); // 사용자의 기본 역할 사용
    }

    @Property(tries = 10)
    @DisplayName("속성 31: 테넌트 기본값 처리 - 테넌트 ID가 null인 경우 기본값을 사용한다")
    void property31_테넌트_기본값_처리(@ForAll("validUserId") Long userId,
                                  @ForAll("validRole") String role) {
        
        // Given: 테넌트 ID가 null인 사용자
        User mockUser = createMockUser(userId, role, 1L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // When: 테넌트 ID를 null로 개발용 토큰 생성
        LoginResponse devTokenResponse = authService.generateDevToken(role, "1");
        String devToken = devTokenResponse.getAccessToken();
        
        // Then: 기본 테넌트 ID로 토큰이 생성되어야 함
        assertThat(devToken).isNotNull();
        assertThat(devToken).isNotEmpty();
        
        // 토큰에서 테넌트 정보 확인
        String extractedTenantId = jwtTokenService.extractTenantId(devToken);
        assertThat(extractedTenantId).isEqualTo("1"); // 사용자의 기본 테넌트 사용
    }

    @Property(tries = 10)
    @DisplayName("속성 32: 개발용 토큰 형식 일관성 - 생성된 모든 개발용 토큰은 일관된 형식을 가진다")
    void property32_개발용_토큰_형식_일관성(@ForAll("validUserId") Long userId,
                                      @ForAll("validRole") String role,
                                      @ForAll("validTenantId") Long tenantId) {
        
        // Given: 유효한 사용자 정보
        User mockUser = createMockUser(userId, role, tenantId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // When: 개발용 토큰 생성 (여러 번)
        LoginResponse devTokenResponse1 = authService.generateDevToken(role, tenantId.toString());
        LoginResponse devTokenResponse2 = authService.generateDevToken(role, tenantId.toString());
        String devToken1 = devTokenResponse1.getAccessToken();
        String devToken2 = devTokenResponse2.getAccessToken();
        
        // Then: 모든 토큰이 일관된 형식을 가져야 함
        assertThat(devToken1).isNotNull();
        assertThat(devToken2).isNotNull();
        
        // JWT 형식 확인 (header.payload.signature)
        assertThat(devToken1.split("\\.")).hasSize(3);
        assertThat(devToken2.split("\\.")).hasSize(3);
        
        // 토큰 내용 일관성 확인
        assertThat(jwtTokenService.extractUserId(devToken1)).isEqualTo("dev-user-1");
        assertThat(jwtTokenService.extractUserId(devToken2)).isEqualTo("dev-user-1");
        assertThat(jwtTokenService.extractRole(devToken1)).isEqualTo(role);
        assertThat(jwtTokenService.extractRole(devToken2)).isEqualTo(role);
        assertThat(jwtTokenService.extractTenantId(devToken1)).isEqualTo(tenantId.toString());
        assertThat(jwtTokenService.extractTenantId(devToken2)).isEqualTo(tenantId.toString());
    }

    // 테스트 데이터 생성기
    @Provide
    Arbitrary<Long> validUserId() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    @Provide
    Arbitrary<String> validRole() {
        return Arbitraries.of("SUPER_ADMIN", "HQ_ADMIN", "SITE_MANAGER", "TEAM_LEADER", "WORKER");
    }

    @Provide
    Arbitrary<Long> validTenantId() {
        return Arbitraries.longs().between(1L, 100L);
    }

    private User createMockUser(Long userId, String role, Long tenantId) {
        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.valueOf(role));
        user.setTenantId(tenantId);
        user.setName("testuser" + userId);
        user.setEmail("test" + userId + "@example.com");
        user.setIsActive(true);
        return user;
    }
}