package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CI값 기본 기능 테스트
 * Property-based 테스트의 핵심 로직을 JUnit 5로 검증
 */
class CiValueTest {

    @Test
    @DisplayName("CI값 객체 생성 - CI값 객체가 올바르게 생성되어야 함")
    void ciValueObjectCreation() {
        // Given: 전화번호
        String phoneNumber = "010-1234-5678";

        // When: CI값 객체 생성
        CiValue ciValue = new CiValue(phoneNumber);

        // Then: CI값 객체가 올바르게 생성되어야 함
        assertThat(ciValue).isNotNull();
        assertThat(ciValue.getValue()).isNotNull();
        assertThat(ciValue.getValue()).startsWith("CI_");
        assertThat(ciValue.isValid()).isTrue();
        assertThat(ciValue.getGeneratedAt()).isNotNull();
        assertThat(ciValue.getPhoneNumber()).isNotNull();
    }

    @Test
    @DisplayName("CI값 고유성 - 서로 다른 전화번호는 서로 다른 CI값을 생성해야 함")
    void ciValueUniqueness() {
        // Given: 서로 다른 전화번호들
        String phoneNumber1 = "010-1234-5678";
        String phoneNumber2 = "010-9876-5432";

        // When: 각각 CI값 생성
        CiValue ciValue1 = new CiValue(phoneNumber1);
        CiValue ciValue2 = new CiValue(phoneNumber2);

        // Then: CI값들은 고유해야 함
        assertThat(ciValue1.getValue()).isNotNull();
        assertThat(ciValue2.getValue()).isNotNull();
        assertThat(ciValue1.getValue()).isNotEqualTo(ciValue2.getValue());
        assertThat(ciValue1.getValue()).startsWith("CI_");
        assertThat(ciValue2.getValue()).startsWith("CI_");
    }

    @Test
    @DisplayName("사용자 CI값 설정 - 사용자에게 CI값이 올바르게 설정되어야 함")
    void userCiValueSetting() {
        // Given: 사용자와 전화번호
        String phoneNumber = "010-1234-5678";
        User user = User.builder()
                .name("테스트사용자")
                .email("test@example.com")
                .phoneNumber(phoneNumber)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();

        // When: CI값 설정
        user.setCiValue(phoneNumber);

        // Then: CI값이 올바르게 설정되어야 함
        assertThat(user.getCiValue()).isNotNull();
        assertThat(user.getCiValue().isValid()).isTrue();
        assertThat(user.getCiValueString()).isNotNull();
        assertThat(user.getCiValueString()).startsWith("CI_");
        assertThat(user.isCiBasedUser()).isTrue();
        assertThat(user.isPhoneVerified()).isTrue();
    }

    @Test
    @DisplayName("계정 정보 일관성 - CI값 기반 사용자는 항상 소셜 로그인 유형이어야 함")
    void accountInfoConsistency() {
        // Given: CI값으로 생성된 사용자
        String phoneNumber = "010-1234-5678";
        User user = User.builder()
                .name("테스트사용자")
                .email("test@example.com")
                .phoneNumber(phoneNumber)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();
        user.setCiValue(phoneNumber);
        user.addRole(User.Role.ROLE_WORKER);

        // Then: 사용자 정보 일관성 검증
        assertThat(user.isCiBasedUser()).isTrue();
        assertThat(user.isBusinessUser()).isFalse();
        assertThat(user.getLoginType()).isEqualTo(LoginType.SOCIAL);
        assertThat(user.canUseLoginType(LoginType.SOCIAL)).isTrue();
        assertThat(user.canUseLoginType(LoginType.BUSINESS)).isFalse();
        assertThat(user.isPersonalUser()).isTrue();
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("CI값 마스킹 - CI값이 보안을 위해 마스킹되어야 함")
    void ciValueMasking() {
        // Given: CI값 객체
        String phoneNumber = "010-1234-5678";
        CiValue ciValue = new CiValue(phoneNumber);

        // When: 마스킹된 CI값 조회
        String maskedValue = ciValue.getMaskedValue();

        // Then: 마스킹이 올바르게 적용되어야 함
        assertThat(maskedValue).isNotNull();
        assertThat(maskedValue).contains("****");
        assertThat(maskedValue).startsWith("CI_");
        assertThat(maskedValue.length()).isLessThan(ciValue.getValue().length());
    }
}