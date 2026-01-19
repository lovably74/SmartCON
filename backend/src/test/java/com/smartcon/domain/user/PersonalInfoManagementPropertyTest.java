package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 7: Personal Information Management
 * 
 * Feature: smartcon-lite-role-based-system, Property 7: Personal Information Management
 * 
 * 유효한 개인정보 업데이트시 데이터를 검증하고 저장하며, 
 * 이후 조회시 변경사항이 반영되는 속성 검증
 * 
 * Validates: Requirements 2.1, 2.4
 */
class PersonalInfoManagementPropertyTest {

    @Property(tries = 100)
    @DisplayName("Property 7.1: 개인정보 업데이트 일관성 - 유효한 개인정보 업데이트시 데이터가 올바르게 저장되고 조회되어야 함")
    void personalInfoUpdateConsistency(
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String name,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber,
            @ForAll @StringLength(min = 5, max = 100) String address,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String emergencyContact) {

        // Given: 개인사용자 생성
        String formattedPhone = formatPhoneNumber(phoneNumber);
        String formattedEmergency = formatPhoneNumber(emergencyContact);
        
        User user = User.builder()
                .name(name)
                .email(name.toLowerCase() + "@example.com")
                .phoneNumber(formattedPhone)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();
        user.addRole(Role.ROLE_WORKER);
        user.setCiValue(formattedPhone);
        user.initializePersonalInfo();
        user.initializeBankAccount();

        // When: 개인정보 업데이트
        user.getPersonalInfo().setHomeAddress(address);
        user.getPersonalInfo().setEmergencyContact(formattedEmergency);
        user.getPersonalInfo().setRealName(name);
        
        user.getBankAccount().setBankCodeAndName("004");
        user.getBankAccount().setAccountNumber("123456789012");
        user.getBankAccount().setAccountHolder(name);

        // Then: 업데이트된 정보가 올바르게 저장되고 조회되어야 함
        assertThat(user.getPersonalInfo()).isNotNull();
        assertThat(user.getPersonalInfo().getHomeAddress()).isEqualTo(address);
        assertThat(user.getPersonalInfo().getEmergencyContact()).isEqualTo(formattedEmergency);
        assertThat(user.getPersonalInfo().getRealName()).isEqualTo(name);
        assertThat(user.getPersonalInfo().isValid()).isTrue();
        
        // 은행 계좌 정보도 함께 검증
        assertThat(user.getBankAccount()).isNotNull();
        assertThat(user.getBankAccount().getBankName()).isEqualTo("KB국민은행");
        assertThat(user.getBankAccount().getAccountHolder()).isEqualTo(name);
        assertThat(user.getBankAccount().isValid()).isTrue();
    }

    @Property(tries = 100)
    @DisplayName("Property 7.2: 개인정보 검증 - 업데이트된 개인정보는 항상 유효성 검증을 통과해야 함")
    void personalInfoValidation(
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String realName,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String emergencyContact,
            @ForAll @StringLength(min = 5, max = 100) String address) {

        // Given: 개인정보 객체 생성
        PersonalInfo personalInfo = PersonalInfo.builder()
                .realName(realName)
                .emergencyContact(formatPhoneNumber(emergencyContact))
                .homeAddress(address)
                .build();

        // When: 유효성 검증
        boolean isValid = personalInfo.isValid();

        // Then: 유효한 데이터는 항상 검증을 통과해야 함
        assertThat(isValid).isTrue();
        assertThat(personalInfo.getRealName()).isEqualTo(realName);
        assertThat(personalInfo.getHomeAddress()).isEqualTo(address);
    }

    @Property(tries = 100)
    @DisplayName("Property 7.3: 개인정보 마스킹 - 민감한 정보는 항상 마스킹되어 표시되어야 함")
    void personalInfoMasking(
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String emergencyContact,
            @ForAll @StringLength(min = 10, max = 100) String address) {

        // Given: 민감한 개인정보
        PersonalInfo personalInfo = PersonalInfo.builder()
                .emergencyContact(formatPhoneNumber(emergencyContact))
                .homeAddress(address)
                .build();

        // When: 마스킹된 정보 조회
        String maskedContact = personalInfo.getMaskedEmergencyContact();
        String maskedAddress = personalInfo.getMaskedHomeAddress();

        // Then: 마스킹이 올바르게 적용되어야 함
        assertThat(maskedContact).isNotNull();
        assertThat(maskedContact).contains("*");
        assertThat(maskedContact).isNotEqualTo(personalInfo.getEmergencyContact());
        
        assertThat(maskedAddress).isNotNull();
        assertThat(maskedAddress).contains("*");
        assertThat(maskedAddress.length()).isEqualTo(address.length());
    }

    @Property(tries = 100)
    @DisplayName("Property 7.4: 개인정보 완성도 계산 - 완성도는 입력된 필드 수에 비례해야 함")
    void personalInfoCompletionPercentage(
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String realName,
            @ForAll boolean hasEmergencyContact,
            @ForAll boolean hasAddress,
            @ForAll boolean hasSsn) {

        // Given: 부분적으로 입력된 개인정보
        PersonalInfo.PersonalInfoBuilder builder = PersonalInfo.builder()
                .realName(realName);
        
        if (hasEmergencyContact) {
            builder.emergencyContact("010-1234-5678");
        }
        if (hasAddress) {
            builder.homeAddress("서울시 강남구");
        }
        
        PersonalInfo personalInfo = builder.build();
        
        if (hasSsn) {
            personalInfo.setSsn("9001011234567");
        }

        // When: 완성도 계산
        int completionPercentage = personalInfo.getCompletionPercentage();

        // Then: 완성도는 0-100 사이여야 하며, 입력된 필드가 많을수록 높아야 함
        assertThat(completionPercentage).isBetween(0, 100);
        
        // 최소한 실명은 입력되었으므로 0보다 커야 함
        assertThat(completionPercentage).isGreaterThan(0);
        
        // 모든 필드가 입력되면 완성도가 높아야 함
        if (hasEmergencyContact && hasAddress && hasSsn) {
            assertThat(completionPercentage).isGreaterThanOrEqualTo(50);
        }
    }

    @Property(tries = 100)
    @DisplayName("Property 7.5: 주민번호 자동 파싱 - 주민번호 입력시 생년월일과 성별이 자동으로 추출되어야 함")
    void ssnAutoParsing(
            @ForAll("validSsn") String ssn) {

        // Given: 유효한 주민번호
        PersonalInfo personalInfo = PersonalInfo.builder().build();

        // When: 주민번호 설정
        personalInfo.setSsn(ssn);

        // Then: 생년월일과 성별이 자동으로 추출되어야 함
        assertThat(personalInfo.getEncryptedSsn()).isNotNull();
        assertThat(personalInfo.getBirthDate()).isNotNull();
        assertThat(personalInfo.getGender()).isNotNull();
        assertThat(personalInfo.getGender()).isIn("남성", "여성");
        
        // 생년월일은 과거여야 함
        assertThat(personalInfo.getBirthDate()).isBefore(LocalDate.now());
    }

    @Property(tries = 100)
    @DisplayName("Property 7.6: 사용자 프로필 완성도 - 개인정보 업데이트시 전체 프로필 완성도가 증가해야 함")
    void userProfileCompletionIncrease(
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String name,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber) {

        // Given: 기본 정보만 있는 사용자
        String formattedPhone = formatPhoneNumber(phoneNumber);
        User user = User.builder()
                .name(name)
                .email(name.toLowerCase() + "@example.com")
                .phoneNumber(formattedPhone)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();
        user.addRole(Role.ROLE_WORKER);
        user.setCiValue(formattedPhone);
        user.initializePersonalInfo();
        user.initializeBankAccount();
        
        int initialCompletion = user.getOverallProfileCompletionPercentage();

        // When: 개인정보 업데이트
        user.getPersonalInfo().setRealName("실제이름");
        user.getPersonalInfo().setSsn("9001011234567");
        user.getPersonalInfo().setEmergencyContact("010-9999-8888");
        user.getPersonalInfo().setHomeAddress("서울시 강남구");
        
        user.getBankAccount().setBankCodeAndName("004");
        user.getBankAccount().setAccountNumber("123456789012");
        user.getBankAccount().setAccountHolder("실제이름");

        int updatedCompletion = user.getOverallProfileCompletionPercentage();

        // Then: 전체 프로필 완성도가 증가해야 함
        assertThat(updatedCompletion).isGreaterThan(initialCompletion);
        assertThat(updatedCompletion).isBetween(0, 100);
    }

    @Property(tries = 100)
    @DisplayName("Property 7.7: 개인정보 업데이트 멱등성 - 동일한 데이터로 여러 번 업데이트해도 결과가 동일해야 함")
    void personalInfoUpdateIdempotency(
            @ForAll @StringLength(min = 2, max = 20) @AlphaChars String name,
            @ForAll @StringLength(min = 10, max = 11) @NumericChars String phoneNumber,
            @ForAll @StringLength(min = 5, max = 100) String address) {

        // Given: 개인사용자 생성
        String formattedPhone = formatPhoneNumber(phoneNumber);
        User user = User.builder()
                .name(name)
                .email(name.toLowerCase() + "@example.com")
                .phoneNumber(formattedPhone)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();
        user.addRole(Role.ROLE_WORKER);
        user.setCiValue(formattedPhone);
        user.initializePersonalInfo();
        user.initializeBankAccount();

        // When: 동일한 데이터로 여러 번 업데이트
        user.getPersonalInfo().setHomeAddress(address);
        String firstAddress = user.getPersonalInfo().getHomeAddress();
        
        user.getPersonalInfo().setHomeAddress(address);
        String secondAddress = user.getPersonalInfo().getHomeAddress();
        
        user.getPersonalInfo().setHomeAddress(address);
        String thirdAddress = user.getPersonalInfo().getHomeAddress();

        // Then: 모든 업데이트 결과가 동일해야 함 (멱등성)
        assertThat(firstAddress).isEqualTo(address);
        assertThat(secondAddress).isEqualTo(address);
        assertThat(thirdAddress).isEqualTo(address);
        assertThat(firstAddress).isEqualTo(secondAddress);
        assertThat(secondAddress).isEqualTo(thirdAddress);
    }

    // === 테스트 헬퍼 메서드들 ===

    /**
     * 유효한 주민번호 생성기
     */
    @Provide
    Arbitrary<String> validSsn() {
        return Arbitraries.integers().between(1950, 2005).flatMap(year -> {
            int shortYear = year % 100;
            return Arbitraries.integers().between(1, 12).flatMap(month -> {
                return Arbitraries.integers().between(1, 28).flatMap(day -> {
                    return Arbitraries.integers().between(1, 4).map(genderCode -> {
                        return String.format("%02d%02d%02d%d%06d",
                                shortYear, month, day, genderCode, 
                                (int)(Math.random() * 1000000));
                    });
                });
            });
        });
    }

    /**
     * 전화번호 포맷팅 (010-XXXX-XXXX 형식)
     */
    private String formatPhoneNumber(String phoneNumber) {
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() == 10) {
            cleanNumber = "0" + cleanNumber;
        }
        if (cleanNumber.length() == 11 && cleanNumber.startsWith("010")) {
            return cleanNumber.substring(0, 3) + "-" + 
                   cleanNumber.substring(3, 7) + "-" + 
                   cleanNumber.substring(7);
        }
        return cleanNumber;
    }
}
