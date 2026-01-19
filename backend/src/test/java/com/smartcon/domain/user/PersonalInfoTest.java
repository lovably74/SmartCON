package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 개인정보 관리 기능 테스트
 * PersonalInfo, BusinessInfo, BankAccount 엔티티의 암호화 및 마스킹 기능 검증
 */
class PersonalInfoTest {

    @Test
    @DisplayName("개인정보 객체 생성 - 개인정보 객체가 올바르게 생성되어야 함")
    void personalInfoCreation() {
        // Given: 개인정보 데이터
        PersonalInfo personalInfo = PersonalInfo.builder()
                .realName("홍길동")
                .birthDate(LocalDate.of(1990, 5, 15))
                .gender("남성")
                .emergencyContact("010-9876-5432")
                .emergencyContactName("홍어머니")
                .homeAddress("서울시 강남구 테헤란로 123")
                .postalCode("06142")
                .build();

        // When: 주민번호 설정
        personalInfo.setSsn("9005151234567");

        // Then: 개인정보가 올바르게 설정되어야 함
        assertThat(personalInfo.getRealName()).isEqualTo("홍길동");
        assertThat(personalInfo.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(personalInfo.getGender()).isEqualTo("남성");
        assertThat(personalInfo.getEncryptedSsn()).isNotNull();
        assertThat(personalInfo.getMaskedSsn()).isEqualTo("******-*******");
        assertThat(personalInfo.isValid()).isTrue();
    }

    @Test
    @DisplayName("개인정보 마스킹 - 민감한 정보가 마스킹되어야 함")
    void personalInfoMasking() {
        // Given: 개인정보
        PersonalInfo personalInfo = PersonalInfo.builder()
                .realName("김철수")
                .emergencyContact("010-1234-5678")
                .homeAddress("부산시 해운대구 센텀중앙로 79")
                .build();

        // When: 마스킹된 정보 조회
        String maskedContact = personalInfo.getMaskedEmergencyContact();
        String maskedAddress = personalInfo.getMaskedHomeAddress();

        // Then: 마스킹이 올바르게 적용되어야 함
        assertThat(maskedContact).isEqualTo("010-****-5678");
        assertThat(maskedAddress).contains("*");
        assertThat(maskedAddress.length()).isEqualTo(personalInfo.getHomeAddress().length());
    }

    @Test
    @DisplayName("개인정보 완성도 계산 - 완성도가 올바르게 계산되어야 함")
    void personalInfoCompletionPercentage() {
        // Given: 부분적으로 입력된 개인정보
        PersonalInfo personalInfo = PersonalInfo.builder()
                .realName("이영희")
                .emergencyContact("010-5555-6666")
                .build();

        // When: 완성도 계산
        int completionPercentage = personalInfo.getCompletionPercentage();

        // Then: 완성도가 올바르게 계산되어야 함
        assertThat(completionPercentage).isEqualTo(25); // 2/8 * 100 = 25%
        assertThat(personalInfo.isEssentialInfoComplete()).isFalse();
    }

    @Test
    @DisplayName("사업자 정보 객체 생성 - 사업자 정보가 올바르게 생성되어야 함")
    void businessInfoCreation() {
        // Given: 사업자 정보
        BusinessInfo businessInfo = BusinessInfo.builder()
                .companyName("(주)스마트콘")
                .ceoName("박대표")
                .businessType("건설업")
                .businessAddress("서울시 서초구 강남대로 465")
                .businessPhone("02-1234-5678")
                .businessEmail("info@smartcon.co.kr")
                .build();

        // When: 사업자번호 설정
        businessInfo.setBusinessNumber("1234567890");

        // Then: 사업자 정보가 올바르게 설정되어야 함
        assertThat(businessInfo.getCompanyName()).isEqualTo("(주)스마트콘");
        assertThat(businessInfo.getCeoName()).isEqualTo("박대표");
        assertThat(businessInfo.getEncryptedBusinessNumber()).isNotNull();
        assertThat(businessInfo.getMaskedBusinessNumber()).isEqualTo("***-**-*****");
        assertThat(businessInfo.isComplete()).isTrue();
        assertThat(businessInfo.isValid()).isTrue();
    }

    @Test
    @DisplayName("사업자번호 유효성 검증 - 사업자번호 체크섬이 올바르게 검증되어야 함")
    void businessNumberValidation() {
        // Given: 유효한 사업자번호와 무효한 사업자번호
        String validBusinessNumber = "1208800767"; // 유효한 사업자번호 (삼성전자)
        String invalidBusinessNumber = "1234567890"; // 무효한 사업자번호

        // When & Then: 유효성 검증
        assertThat(BusinessInfo.isValidBusinessNumber(validBusinessNumber)).isTrue();
        assertThat(BusinessInfo.isValidBusinessNumber(invalidBusinessNumber)).isFalse();
        assertThat(BusinessInfo.isValidBusinessNumber(null)).isFalse();
        assertThat(BusinessInfo.isValidBusinessNumber("123")).isFalse();
    }

    @Test
    @DisplayName("급여 계좌 정보 생성 - 계좌 정보가 올바르게 생성되어야 함")
    void bankAccountCreation() {
        // Given: 계좌 정보
        BankAccount bankAccount = BankAccount.builder()
                .accountHolder("홍길동")
                .isSalaryAccount(true)
                .build();

        // When: 은행 정보 및 계좌번호 설정
        bankAccount.setBankCodeAndName("004");
        bankAccount.setAccountNumber("123456789012");

        // Then: 계좌 정보가 올바르게 설정되어야 함
        assertThat(bankAccount.getBankName()).isEqualTo("KB국민은행");
        assertThat(bankAccount.getBankCode()).isEqualTo("004");
        assertThat(bankAccount.getEncryptedAccountNumber()).isNotNull();
        assertThat(bankAccount.getMaskedAccountNumber()).isEqualTo("***-**-****-***");
        assertThat(bankAccount.isComplete()).isTrue();
        assertThat(bankAccount.isValid()).isTrue();
    }

    @Test
    @DisplayName("계좌 인증 처리 - 계좌 인증이 올바르게 처리되어야 함")
    void bankAccountVerification() {
        // Given: 완성된 계좌 정보
        BankAccount bankAccount = BankAccount.builder()
                .bankName("신한은행")
                .bankCode("088")
                .accountHolder("김철수")
                .build();
        bankAccount.setAccountNumber("110123456789");

        // When: 계좌 인증 처리
        assertThat(bankAccount.canVerify()).isTrue();
        bankAccount.verify();

        // Then: 인증이 완료되어야 함
        assertThat(bankAccount.getIsVerified()).isTrue();
        assertThat(bankAccount.canVerify()).isFalse();
    }

    @Test
    @DisplayName("사용자 개인정보 통합 관리 - 사용자에게 개인정보가 올바르게 설정되어야 함")
    void userPersonalInfoIntegration() {
        // Given: 사용자 생성
        User user = User.builder()
                .name("테스트사용자")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .build();
        user.addRole(Role.ROLE_WORKER);

        // When: 개인정보 초기화 및 설정
        user.initializePersonalInfo();
        user.initializeBankAccount();
        
        user.getPersonalInfo().setRealName("실제이름");
        user.getPersonalInfo().setSsn("9001011234567");
        user.getPersonalInfo().setEmergencyContact("010-9999-8888");
        
        user.getBankAccount().setBankCodeAndName("020");
        user.getBankAccount().setAccountNumber("1002123456789");
        user.getBankAccount().setAccountHolder("실제이름");

        // Then: 사용자 개인정보가 올바르게 설정되어야 함
        assertThat(user.getPersonalInfo()).isNotNull();
        assertThat(user.getBankAccount()).isNotNull();
        assertThat(user.getPersonalInfoCompletionPercentage()).isGreaterThan(0);
        assertThat(user.getBankAccountCompletionPercentage()).isEqualTo(100);
        assertThat(user.getMaskedPersonalInfoSummary()).contains("실제이름");
        assertThat(user.getMaskedBankAccountSummary()).contains("우리은행");
    }

    @Test
    @DisplayName("관리자 사업자 정보 관리 - 관리자에게 사업자 정보가 올바르게 설정되어야 함")
    void adminBusinessInfoManagement() {
        // Given: 관리자 사용자 생성
        User admin = User.builder()
                .name("관리자")
                .email("admin@company.com")
                .phoneNumber("02-1234-5678")
                .authProvider(AuthProvider.LOCAL)
                .loginType(LoginType.BUSINESS)
                .businessNumber("1208800767")
                .build();
        admin.addRole(Role.ROLE_HQ);

        // When: 사업자 정보 초기화 및 설정
        admin.initializeBusinessInfo();
        
        admin.getBusinessInfo().setCompanyName("테스트회사");
        admin.getBusinessInfo().setBusinessNumber("1208800767");
        admin.getBusinessInfo().setCeoName("대표이사");
        admin.getBusinessInfo().setBusinessAddress("서울시 강남구");
        admin.getBusinessInfo().setBusinessEmail("ceo@company.com");

        // Then: 관리자 사업자 정보가 올바르게 설정되어야 함
        assertThat(admin.getBusinessInfo()).isNotNull();
        assertThat(admin.isAdmin()).isTrue();
        assertThat(admin.isBusinessUser()).isTrue();
        assertThat(admin.getBusinessInfoCompletionPercentage()).isGreaterThan(70);
        assertThat(admin.getMaskedBusinessInfoSummary()).contains("테스트회사");
        assertThat(admin.getOverallProfileCompletionPercentage()).isGreaterThan(0);
    }
}