package com.smartcon.domain.user;

import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.UnifiedLoginRequest;
import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.AuthService;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 소셜 로그인 플로우 일관성 속성 테스트
 * Feature: smartcon-lite-role-based-system, Property 1: Social Login Flow Consistency
 * Validates: Requirements 1.2, 1.3
 * 
 * Property 1: Social Login Flow Consistency
 * For any user selecting "개인사용자" login type, the system should provide Kakao and Naver social login options 
 * and handle first-time users with phone verification
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("소셜 로그인 플로우 일관성 속성 테스트")
class SocialLoginFlowPropertyTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Property 1: 소셜 로그인 플로우 일관성
     * 
     * 개인사용자 로그인 유형을 선택한 모든 사용자에 대해:
     * 1. 카카오 또는 네이버 소셜 로그인 옵션이 제공되어야 함
     * 2. 최초 사용자는 휴대폰 인증을 통해 처리되어야 함
     * 3. 로그인 성공 시 적절한 토큰이 발급되어야 함
     */
    @Property(tries = 100)
    @DisplayName("개인사용자 소셜 로그인 플로우는 일관되게 동작해야 한다")
    @Transactional
    void socialLoginFlowShouldBeConsistent(
            @ForAll("socialProviders") SocialProvider provider,
            @ForAll("phoneNumbers") String phoneNumber,
            @ForAll("authCodes") String authCode) {

        // Given: 개인사용자 소셜 로그인 요청
        UnifiedLoginRequest request = UnifiedLoginRequest.builder()
                .loginType(LoginType.SOCIAL)
                .provider(provider)
                .authCode(authCode)
                .phoneNumber(phoneNumber)
                .build();

        // When: 소셜 로그인 수행
        LoginResponse response = authService.authenticateUnified(request);

        // Then: 로그인 응답이 유효해야 함
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isGreaterThan(0);

        // Then: 사용자 정보가 포함되어야 함
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isNotBlank();
        
        // Then: 사용자가 개인사용자 역할을 가져야 함 (TEAM 또는 WORKER)
        String role = response.getUser().getRole();
        assertThat(role).isIn("ROLE_TEAM", "ROLE_WORKER");

        // Then: 데이터베이스에 사용자가 저장되어야 함
        User savedUser = userRepository.findById(Long.parseLong(response.getUser().getId())).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getLoginType()).isEqualTo(LoginType.SOCIAL);
        assertThat(savedUser.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    /**
     * 소셜 제공자 생성기
     */
    @Provide
    Arbitrary<SocialProvider> socialProviders() {
        return Arbitraries.of(SocialProvider.KAKAO, SocialProvider.NAVER);
    }

    /**
     * 휴대폰 번호 생성기
     */
    @Provide
    Arbitrary<String> phoneNumbers() {
        return Arbitraries.integers()
                .between(10000000, 99999999)
                .map(num -> "010" + num);
    }

    /**
     * 인증 코드 생성기
     */
    @Provide
    Arbitrary<String> authCodes() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(20)
                .ofMaxLength(40);
    }
}
