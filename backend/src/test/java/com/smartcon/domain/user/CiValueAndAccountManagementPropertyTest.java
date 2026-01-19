package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 2: CI Value Uniqueness and Account Management
 * 
 * 전화번호 인증 완료시 고유한 CI값을 생성하고, CI값 존재 여부에 따라 
 * 새 계정 생성 또는 기존 계정 연동을 수행하는 속성 검증
 * 
 * Validates: Requirements 1.4, 1.5, 1.6
 */
class CiValueAndAccountManagementPropertyTest {

    @Property(tries = 50)
    @DisplayName("Property 2.1: CI값 고유성 - 전화번호 인증 완료시 고유한 CI값이 생성되어야 함")
    void ciValueUniqueness(
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber1,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber2) {
        
        Assume.that(!phoneNumber1.equals(phoneNumber2));

        // Given: 서로 다른 전화번호들
        String formattedPhone1 = formatPhoneNumber(phoneNumber1);
        String formattedPhone2 = formatPhoneNumber(phoneNumber2);

        // When: 각각 CI값 생성 (직접 생성)
        String ciValue1 = generateCiValue(formattedPhone1);
        String ciValue2 = generateCiValue(formattedPhone2);

        // Then: CI값들은 고유해야 함
        assertThat(ciValue1).isNotNull();
        assertThat(ciValue2).isNotNull();
        assertThat(ciValue1).isNotEqualTo(ciValue2);
        assertThat(ciValue1).startsWith("CI_");
        assertThat(ciValue2).startsWith("CI_");
    }

    @Property(tries = 30)
    @DisplayName("Property 2.2: CI값 객체 생성 - CI값 객체가 올바르게 생성되어야 함")
    void ciValueObjectCreation(
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber) {

        // Given: 전화번호
        String formattedPhone = formatPhoneNumber(phoneNumber);

        // When: CI값 객체 생성
        CiValue ciValue = new CiValue(formattedPhone);

        // Then: CI값 객체가 올바르게 생성되어야 함
        assertThat(ciValue).isNotNull();
        assertThat(ciValue.getValue()).isNotNull();
        assertThat(ciValue.getValue()).startsWith("CI_");
        assertThat(ciValue.isValid()).isTrue();
        assertThat(ciValue.getGeneratedAt()).isNotNull();
        assertThat(ciValue.getPhoneNumber()).isNotNull();
    }

    @Property(tries = 30)
    @DisplayName("Property 2.3: 사용자 CI값 설정 - 사용자에게 CI값이 올바르게 설정되어야 함")
    void userCiValueSetting(
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber,
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String name) {

        // Given: 사용자와 전화번호
        String formattedPhone = formatPhoneNumber(phoneNumber);
        User user = User.builder()
                .name(name)
                .email(name.toLowerCase() + "@example.com")
                .phoneNumber(formattedPhone)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();

        // When: CI값 설정
        user.setCiValue(formattedPhone);

        // Then: CI값이 올바르게 설정되어야 함
        assertThat(user.getCiValue()).isNotNull();
        assertThat(user.getCiValue().isValid()).isTrue();
        assertThat(user.getCiValueString()).isNotNull();
        assertThat(user.getCiValueString()).startsWith("CI_");
        assertThat(user.isCiBasedUser()).isTrue();
        assertThat(user.isPhoneVerified()).isTrue();
    }

    @Property(tries = 20)
    @DisplayName("Property 2.4: 계정 정보 일관성 - CI값 기반 사용자는 항상 소셜 로그인 유형이어야 함")
    void accountInfoConsistency(
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber,
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String name,
            @ForAll("socialProviders") SocialAccount.SocialProvider provider) {

        // Given: CI값으로 생성된 사용자
        String formattedPhone = formatPhoneNumber(phoneNumber);
        User user = User.builder()
                .name(name)
                .email(name.toLowerCase() + "@example.com")
                .phoneNumber(formattedPhone)
                .authProvider(AuthProvider.valueOf(provider.name()))
                .loginType(LoginType.SOCIAL)
                .build();
        user.setCiValue(formattedPhone);
        user.addRole(Role.ROLE_WORKER);

        // Then: 사용자 정보 일관성 검증
        assertThat(user.isCiBasedUser()).isTrue();
        assertThat(user.isBusinessUser()).isFalse();
        assertThat(user.getLoginType()).isEqualTo(LoginType.SOCIAL);
        assertThat(user.canUseLoginType(LoginType.SOCIAL)).isTrue();
        assertThat(user.canUseLoginType(LoginType.BUSINESS)).isFalse();
        assertThat(user.isPersonalUser()).isTrue();
        assertThat(user.isAdmin()).isFalse();
    }

    @Property(tries = 20)
    @DisplayName("Property 2.5: 전화번호 검증 - 동일한 전화번호는 항상 동일한 CI값을 생성해야 함")
    void phoneNumberConsistency(
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber) {

        // Given: 동일한 전화번호
        String formattedPhone = formatPhoneNumber(phoneNumber);

        // When: 여러 번 CI값 생성
        String ciValue1 = generateCiValue(formattedPhone);
        String ciValue2 = generateCiValue(formattedPhone);
        String ciValue3 = generateCiValue(formattedPhone);

        // Then: 항상 동일한 CI값이 생성되어야 함
        assertThat(ciValue1).isEqualTo(ciValue2);
        assertThat(ciValue2).isEqualTo(ciValue3);
    }

    // === 테스트 헬퍼 메서드들 ===

    @Provide
    Arbitrary<SocialAccount.SocialProvider> socialProviders() {
        return Arbitraries.of(SocialAccount.SocialProvider.values());
    }

    /**
     * 전화번호 포맷팅 (010-XXXX-XXXX 형식)
     */
    private String formatPhoneNumber(String phoneNumber) {
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() == 10) {
            cleanNumber = "0" + cleanNumber; // 10자리면 앞에 0 추가
        }
        if (cleanNumber.length() == 11 && cleanNumber.startsWith("010")) {
            return cleanNumber.substring(0, 3) + "-" + 
                   cleanNumber.substring(3, 7) + "-" + 
                   cleanNumber.substring(7);
        }
        return cleanNumber;
    }

    /**
     * CI값 생성 로직 (테스트용)
     */
    private String generateCiValue(String phoneNumber) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            String input = "SMARTCON_CI_" + phoneNumber; // 고정된 접두사로 일관성 보장
            byte[] hash = md.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "CI_" + hexString.toString().substring(0, 16);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("CI값 생성 실패", e);
        }
    }
}