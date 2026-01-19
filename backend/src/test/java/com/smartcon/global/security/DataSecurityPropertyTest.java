package com.smartcon.global.security;

import com.smartcon.domain.user.entity.*;
import com.smartcon.domain.user.service.UserEncryptionService;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 데이터 보안 및 검증 속성 테스트
 * Feature: smartcon-lite-role-based-system
 * Property 13: Data Encryption and Security
 * Property 15: CI Value and OAuth2 Validation
 * Validates: Requirements 27.4, 27.6, 27.7
 */
class DataSecurityPropertyTest {

    private final EncryptionService encryptionService = new EncryptionService("");
    private final UserEncryptionService userEncryptionService = new UserEncryptionService(encryptionService);
    private final CiValueValidator ciValueValidator = new CiValueValidator();
    private final OAuth2TokenValidator oauth2TokenValidator = new OAuth2TokenValidator();

    /**
     * Property 13.1: 암호화 라운드트립 속성
     * 
     * 모든 평문 데이터에 대해:
     * 암호화 후 복호화하면 원본 데이터와 동일해야 함
     */
    @Property(tries = 100)
    @Label("Property 13.1: 암호화 후 복호화하면 원본 데이터를 얻을 수 있다")
    void encryptionRoundTripPreservesData(@ForAll("sensitiveData") String plainText) {
        // When: 데이터를 암호화한 후 복호화
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then: 원본 데이터와 동일해야 함
        assertThat(decrypted).isEqualTo(plainText);
    }

    /**
     * Property 13.2: 암호화된 데이터는 평문과 달라야 함
     * 
     * 모든 평문 데이터에 대해:
     * 암호화된 데이터는 원본 평문과 달라야 함
     */
    @Property(tries = 100)
    @Label("Property 13.2: 암호화된 데이터는 평문과 다르다")
    void encryptedDataDiffersFromPlainText(@ForAll("sensitiveData") String plainText) {
        // When: 데이터를 암호화
        String encrypted = encryptionService.encrypt(plainText);

        // Then: 암호화된 데이터는 평문과 달라야 함
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encryptionService.isEncrypted(encrypted)).isTrue();
    }

    /**
     * Property 13.3: 동일한 평문을 여러 번 암호화하면 다른 암호문 생성
     * 
     * 모든 평문 데이터에 대해:
     * 동일한 평문을 여러 번 암호화하면 매번 다른 암호문이 생성되어야 함 (IV 사용)
     */
    @Property(tries = 100)
    @Label("Property 13.3: 동일한 평문을 여러 번 암호화하면 다른 암호문이 생성된다")
    void sameDataEncryptedMultipleTimesProducesDifferentCiphertext(@ForAll("sensitiveData") String plainText) {
        // When: 동일한 데이터를 두 번 암호화
        String encrypted1 = encryptionService.encrypt(plainText);
        String encrypted2 = encryptionService.encrypt(plainText);

        // Then: 암호문은 달라야 하지만 복호화하면 같은 평문이어야 함
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(plainText);
        assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(plainText);
    }

    /**
     * Property 13.4: 주민번호 암호화 및 마스킹
     * 
     * 모든 주민번호에 대해:
     * 암호화된 주민번호는 복호화 가능하고, 마스킹된 주민번호는 일부만 표시되어야 함
     */
    @Property(tries = 100)
    @Label("Property 13.4: 주민번호는 암호화되고 마스킹된다")
    void ssnIsEncryptedAndMasked(@ForAll("ssn") String ssn) {
        // Given: PersonalInfo 객체 생성
        PersonalInfo personalInfo = new PersonalInfo();

        // When: 주민번호 암호화 및 설정
        userEncryptionService.encryptAndSetSsn(personalInfo, ssn);

        // Then: 암호화된 주민번호는 복호화 가능해야 함
        String decrypted = userEncryptionService.decryptSsn(personalInfo);
        assertThat(decrypted).isEqualTo(ssn);

        // And: 마스킹된 주민번호는 일부만 표시되어야 함
        String masked = userEncryptionService.getMaskedSsn(personalInfo);
        assertThat(masked).isNotEqualTo(ssn);
        assertThat(masked).contains("*");
    }

    /**
     * Property 13.5: 사업자번호 암호화 및 마스킹
     * 
     * 모든 사업자번호에 대해:
     * 암호화된 사업자번호는 복호화 가능하고, 마스킹된 사업자번호는 일부만 표시되어야 함
     */
    @Property(tries = 100)
    @Label("Property 13.5: 사업자번호는 암호화되고 마스킹된다")
    void businessNumberIsEncryptedAndMasked(@ForAll("businessNumber") String businessNumber) {
        // Given: BusinessInfo 객체 생성
        BusinessInfo businessInfo = new BusinessInfo();

        // When: 사업자번호 암호화 및 설정
        userEncryptionService.encryptAndSetBusinessNumber(businessInfo, businessNumber);

        // Then: 암호화된 사업자번호는 복호화 가능해야 함
        String decrypted = userEncryptionService.decryptBusinessNumber(businessInfo);
        assertThat(decrypted).isEqualTo(businessNumber);

        // And: 마스킹된 사업자번호는 일부만 표시되어야 함
        String masked = userEncryptionService.getMaskedBusinessNumber(businessInfo);
        assertThat(masked).isNotEqualTo(businessNumber);
        assertThat(masked).contains("*");
    }

    /**
     * Property 13.6: 계좌번호 암호화 및 마스킹
     * 
     * 모든 계좌번호에 대해:
     * 암호화된 계좌번호는 복호화 가능하고, 마스킹된 계좌번호는 일부만 표시되어야 함
     */
    @Property(tries = 100)
    @Label("Property 13.6: 계좌번호는 암호화되고 마스킹된다")
    void accountNumberIsEncryptedAndMasked(@ForAll("accountNumber") String accountNumber) {
        // Given: BankAccount 객체 생성
        BankAccount bankAccount = new BankAccount();

        // When: 계좌번호 암호화 및 설정
        userEncryptionService.encryptAndSetAccountNumber(bankAccount, accountNumber);

        // Then: 암호화된 계좌번호는 복호화 가능해야 함
        String decrypted = userEncryptionService.decryptAccountNumber(bankAccount);
        assertThat(decrypted).isEqualTo(accountNumber);

        // And: 마스킹된 계좌번호는 일부만 표시되어야 함
        String masked = userEncryptionService.getMaskedAccountNumber(bankAccount);
        assertThat(masked).isNotEqualTo(accountNumber);
        assertThat(masked).contains("*");
    }

    /**
     * Property 15.1: CI값 형식 검증
     * 
     * 모든 유효한 휴대폰 번호에 대해:
     * 생성된 CI값은 올바른 형식을 가져야 함
     */
    @Property(tries = 100)
    @Label("Property 15.1: 생성된 CI값은 올바른 형식을 가진다")
    void generatedCiValueHasValidFormat(@ForAll("phoneNumber") String phoneNumber) {
        // When: CI값 생성
        String ciValue = ciValueValidator.generateCiValue(phoneNumber);

        // Then: CI값은 올바른 형식이어야 함
        assertThat(ciValueValidator.isValidFormat(ciValue)).isTrue();
        assertThat(ciValue).startsWith("CI_");
        assertThat(ciValue).hasSize(35); // "CI_" + 32자리 해시
    }

    /**
     * Property 15.2: CI값 유효 기간 검증
     * 
     * 모든 생성 시간에 대해:
     * 현재 시간으로부터 5년 이내의 CI값은 유효해야 함
     */
    @Property(tries = 100)
    @Label("Property 15.2: 5년 이내의 CI값은 유효하다")
    void ciValueWithinValidityPeriodIsValid(@ForAll("daysAgo") int daysAgo) {
        // Given: 특정 시간에 생성된 CI값
        LocalDateTime generatedAt = LocalDateTime.now().minusDays(daysAgo);

        // When: 유효 기간 검증
        boolean isValid = ciValueValidator.isWithinValidityPeriod(generatedAt);

        // Then: 5년(1825일) 이내이면 유효해야 함
        if (daysAgo < 1825) {
            assertThat(isValid).isTrue();
        } else {
            assertThat(isValid).isFalse();
        }
    }

    /**
     * Property 15.3: 잘못된 형식의 CI값 거부
     * 
     * 모든 잘못된 형식의 문자열에 대해:
     * CI값 형식 검증은 실패해야 함
     */
    @Property(tries = 100)
    @Label("Property 15.3: 잘못된 형식의 CI값은 거부된다")
    void invalidCiValueFormatIsRejected(@ForAll("invalidCiValue") String invalidCiValue) {
        // When: CI값 형식 검증
        boolean isValid = ciValueValidator.isValidFormat(invalidCiValue);

        // Then: 검증은 실패해야 함
        assertThat(isValid).isFalse();
    }

    /**
     * Property 15.4: 휴대폰 번호 정규화
     * 
     * 모든 휴대폰 번호에 대해:
     * 정규화된 휴대폰 번호는 숫자만 포함해야 함
     */
    @Property(tries = 100)
    @Label("Property 15.4: 휴대폰 번호 정규화는 숫자만 남긴다")
    void phoneNumberNormalizationRemovesNonDigits(@ForAll("phoneNumberWithFormat") String phoneNumber) {
        // When: 휴대폰 번호 정규화
        String normalized = ciValueValidator.normalizePhoneNumber(phoneNumber);

        // Then: 숫자만 포함해야 함
        assertThat(normalized).matches("^[0-9]+$");
    }

    /**
     * Property 15.5: 유효한 휴대폰 번호 검증
     * 
     * 모든 유효한 휴대폰 번호에 대해:
     * 휴대폰 번호 검증은 성공해야 함
     */
    @Property(tries = 100)
    @Label("Property 15.5: 유효한 휴대폰 번호는 검증을 통과한다")
    void validPhoneNumberPassesValidation(@ForAll("phoneNumber") String phoneNumber) {
        // When: 휴대폰 번호 정규화 및 검증
        String normalized = ciValueValidator.normalizePhoneNumber(phoneNumber);
        boolean isValid = ciValueValidator.isValidPhoneNumber(normalized);

        // Then: 검증은 성공해야 함
        assertThat(isValid).isTrue();
    }

    /**
     * Property 15.6: OAuth2 토큰 형식 검증
     * 
     * 모든 유효한 토큰 형식에 대해:
     * 토큰 형식 검증은 성공해야 함
     */
    @Property(tries = 100)
    @Label("Property 15.6: 유효한 OAuth2 토큰 형식은 검증을 통과한다")
    void validOAuth2TokenFormatPassesValidation(@ForAll("oauth2Token") String token) {
        // When: 토큰 형식 검증
        boolean isValid = oauth2TokenValidator.isValidTokenFormat(token);

        // Then: 검증은 성공해야 함
        assertThat(isValid).isTrue();
    }

    /**
     * Property 15.7: 잘못된 OAuth2 토큰 형식 거부
     * 
     * 모든 잘못된 토큰 형식에 대해:
     * 토큰 형식 검증은 실패해야 함
     */
    @Property(tries = 100)
    @Label("Property 15.7: 잘못된 OAuth2 토큰 형식은 거부된다")
    void invalidOAuth2TokenFormatIsRejected(@ForAll("invalidOAuth2Token") String token) {
        // When: 토큰 형식 검증
        boolean isValid = oauth2TokenValidator.isValidTokenFormat(token);

        // Then: 검증은 실패해야 함
        assertThat(isValid).isFalse();
    }

    /**
     * Property 15.8: CI값 마스킹
     * 
     * 모든 유효한 CI값에 대해:
     * 마스킹된 CI값은 일부만 표시되어야 함
     */
    @Property(tries = 100)
    @Label("Property 15.8: CI값은 마스킹되어 일부만 표시된다")
    void ciValueIsMasked(@ForAll("phoneNumber") String phoneNumber) {
        // Given: CI값 생성
        String ciValue = ciValueValidator.generateCiValue(phoneNumber);

        // When: CI값 마스킹
        String masked = ciValueValidator.maskCiValue(ciValue);

        // Then: 마스킹된 CI값은 일부만 표시되어야 함
        assertThat(masked).isNotEqualTo(ciValue);
        assertThat(masked).contains("*");
        assertThat(masked).startsWith("CI_");
    }

    /**
     * Property 15.9: 잘못된 휴대폰 번호로 CI값 생성 실패
     * 
     * 모든 잘못된 휴대폰 번호에 대해:
     * CI값 생성은 예외를 발생시켜야 함
     */
    @Property(tries = 100)
    @Label("Property 15.9: 잘못된 휴대폰 번호로 CI값 생성은 실패한다")
    void invalidPhoneNumberFailsCiValueGeneration(@ForAll("invalidPhoneNumber") String invalidPhone) {
        // When & Then: CI값 생성은 예외를 발생시켜야 함
        assertThatThrownBy(() -> ciValueValidator.generateCiValue(invalidPhone))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property 15.10: null 또는 빈 문자열 암호화 처리
     * 
     * null 또는 빈 문자열에 대해:
     * 암호화는 null을 반환해야 함
     */
    @Property(tries = 10)
    @Label("Property 15.10: null 또는 빈 문자열 암호화는 null을 반환한다")
    void nullOrEmptyStringEncryptionReturnsNull(@ForAll("nullOrEmpty") String input) {
        // When: null 또는 빈 문자열 암호화
        String encrypted = encryptionService.encrypt(input);

        // Then: null을 반환해야 함
        assertThat(encrypted).isNull();
    }

    // ==================== Arbitraries (데이터 생성기) ====================

    @Provide
    Arbitrary<String> sensitiveData() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> ssn() {
        // 주민번호 형식: YYMMDD-NNNNNNN (13자리)
        return Arbitraries.integers().between(0, 99).map(yy -> String.format("%02d", yy))
                .flatMap(yy -> Arbitraries.integers().between(1, 12).map(mm -> String.format("%02d", mm))
                        .flatMap(mm -> Arbitraries.integers().between(1, 28).map(dd -> String.format("%02d", dd))
                                .flatMap(dd -> Arbitraries.integers().between(1000000, 9999999)
                                        .map(n -> yy + mm + dd + "-" + n))));
    }

    @Provide
    Arbitrary<String> businessNumber() {
        // 사업자번호 형식: NNN-NN-NNNNN (10자리)
        return Arbitraries.integers().between(100, 999)
                .flatMap(first -> Arbitraries.integers().between(10, 99)
                        .flatMap(second -> Arbitraries.integers().between(10000, 99999)
                                .map(third -> first + "-" + second + "-" + third)));
    }

    @Provide
    Arbitrary<String> accountNumber() {
        // 계좌번호 형식: 10~14자리 숫자
        return Arbitraries.integers().between(10, 14)
                .flatMap(length -> Arbitraries.strings()
                        .withCharRange('0', '9')
                        .ofLength(length));
    }

    @Provide
    Arbitrary<String> phoneNumber() {
        // 한국 휴대폰 번호: 010-NNNN-NNNN
        return Arbitraries.integers().between(0, 9999).map(n -> String.format("%04d", n))
                .flatMap(first -> Arbitraries.integers().between(0, 9999).map(n -> String.format("%04d", n))
                        .map(second -> "010" + first + second));
    }

    @Provide
    Arbitrary<String> phoneNumberWithFormat() {
        // 다양한 형식의 휴대폰 번호
        return Arbitraries.integers().between(0, 9999).map(n -> String.format("%04d", n))
                .flatMap(first -> Arbitraries.integers().between(0, 9999).map(n -> String.format("%04d", n))
                        .flatMap(second -> Arbitraries.of(
                                "010-" + first + "-" + second,
                                "010" + first + second,
                                "010 " + first + " " + second
                        )));
    }

    @Provide
    Arbitrary<String> invalidCiValue() {
        return Arbitraries.of(
                "",
                "CI_",
                "CI_123",
                "INVALID",
                "CI_GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG",
                "ci_abcdef1234567890abcdef1234567890",
                "CI_abcdef1234567890abcdef12345678901" // 너무 긴 경우
        );
    }

    @Provide
    Arbitrary<Integer> daysAgo() {
        // 0일 전부터 2000일 전까지 (약 5.5년)
        return Arbitraries.integers().between(0, 2000);
    }

    @Provide
    Arbitrary<String> oauth2Token() {
        // 유효한 OAuth2 토큰 형식 (20~100자의 영문, 숫자, 특수문자)
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars('-', '_', '.')
                .ofMinLength(20)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> invalidOAuth2Token() {
        return Arbitraries.of(
                "",
                "short",
                "invalid token with spaces",
                "토큰한글포함",
                "token@with#invalid$chars"
        );
    }

    @Provide
    Arbitrary<String> invalidPhoneNumber() {
        return Arbitraries.of(
                "",
                "123",
                "02012345678", // 지역번호 (02로 시작)
                "012123456789", // 잘못된 접두사 (012)
                "010-1234-567", // 짧은 번호 (10자리)
                "abcdefghijk", // 문자만
                "010123456789" // 너무 긴 번호 (12자리)
        );
    }

    @Provide
    Arbitrary<String> nullOrEmpty() {
        return Arbitraries.of(null, "", "   ");
    }
}
