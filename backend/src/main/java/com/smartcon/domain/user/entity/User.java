package com.smartcon.domain.user.entity;

import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 시스템 사용자 엔티티 (5단계 역할 기반 시스템)
 * CI값 기반 사용자 관리 및 다중 소셜 계정 연동 지원
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTenantEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // CI값 (연계정보 고유 키값) - 전화번호 인증을 통한 개인 식별
    @Embedded
    private CiValue ciValue;

    // 사업자번호 (관리자용)
    @Column(name = "business_number", length = 12)
    private String businessNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL; // 인증 제공자

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    @Builder.Default
    private LoginType loginType = LoginType.BUSINESS; // 로그인 유형

    @Column(name = "password_hash", length = 255)
    private String passwordHash; // 비밀번호 해시

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private Boolean isPhoneVerified = false;

    // 안면인식 임베딩 데이터 (MariaDB TEXT 타입 사용)
    @Lob
    @Column(name = "face_embedding", columnDefinition = "TEXT")
    private String faceEmbedding;

    @Column(name = "login_failure_count")
    @Builder.Default
    private Integer loginFailureCount = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil; // 계정 잠금 해제 시간

    // 5단계 역할 시스템 (다중 역할 지원)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // 소셜 계정 목록 (다중 소셜 계정 연동)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    // 개인정보 (임베디드)
    @Embedded
    private PersonalInfo personalInfo;

    // 사업자 정보 (임베디드) - 관리자 사용자용
    @Embedded
    private BusinessInfo businessInfo;

    // 급여 계좌 정보 (임베디드)
    @Embedded
    private BankAccount bankAccount;

    /**
     * 활성 사용자 여부 확인
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * 이메일 인증 여부 확인
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    /**
     * 로그인 실패 횟수 증가
     */
    public void incrementLoginFailureCount() {
        this.loginFailureCount = (this.loginFailureCount == null ? 0 : this.loginFailureCount) + 1;
        
        // 5회 실패시 30분 잠금
        if (this.loginFailureCount >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * 로그인 실패 횟수 초기화
     */
    public void resetLoginFailureCount() {
        this.loginFailureCount = 0;
        this.accountLockedUntil = null;
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        
        // 잠금 시간이 지났으면 자동 해제
        if (LocalDateTime.now().isAfter(accountLockedUntil)) {
            this.accountLockedUntil = null;
            this.loginFailureCount = 0;
            return false;
        }
        
        return true;
    }

    /**
     * 특정 역할을 가지고 있는지 확인
     */
    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    /**
     * 역할 추가
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * 역할 제거
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * 관리자 역할인지 확인 (SUPER, HQ, SITE)
     */
    public boolean isAdmin() {
        return hasRole(Role.ROLE_SUPER) || hasRole(Role.ROLE_HQ) || hasRole(Role.ROLE_SITE);
    }

    /**
     * 개인사용자 역할인지 확인 (TEAM, WORKER)
     */
    public boolean isPersonalUser() {
        return hasRole(Role.ROLE_TEAM) || hasRole(Role.ROLE_WORKER);
    }

    /**
     * 소셜 계정 추가
     */
    public void addSocialAccount(SocialAccount socialAccount) {
        this.socialAccounts.add(socialAccount);
        socialAccount.setUser(this);
    }

    /**
     * 특정 소셜 제공자 계정이 연결되어 있는지 확인
     */
    public boolean hasSocialProvider(SocialAccount.SocialProvider provider) {
        return socialAccounts.stream()
                .anyMatch(account -> account.getProvider().equals(provider));
    }

    /**
     * 전화번호 인증 여부 확인
     */
    public boolean isPhoneVerified() {
        return Boolean.TRUE.equals(isPhoneVerified);
    }

    /**
     * CI값 기반 사용자인지 확인
     */
    public boolean isCiBasedUser() {
        return ciValue != null && ciValue.isValid();
    }

    /**
     * 사업자 기반 사용자인지 확인
     */
    public boolean isBusinessUser() {
        return businessNumber != null && !businessNumber.trim().isEmpty();
    }

    /**
     * 특정 로그인 유형을 사용할 수 있는지 확인
     */
    public boolean canUseLoginType(LoginType loginType) {
        return switch (loginType) {
            case BUSINESS -> roles.stream().anyMatch(role -> 
                role == Role.ROLE_SUPER || role == Role.ROLE_HQ || role == Role.ROLE_SITE);
            case SOCIAL -> roles.stream().anyMatch(role -> 
                role == Role.ROLE_TEAM || role == Role.ROLE_WORKER);
        };
    }

    /**
     * CI값과 동일한 사용자인지 확인
     */
    public boolean isSamePersonAs(String ciValueString) {
        return this.ciValue != null && this.ciValue.getValue() != null && 
               this.ciValue.getValue().equals(ciValueString);
    }

    /**
     * CI값 설정
     */
    public void setCiValue(String phoneNumber) {
        this.ciValue = new CiValue(phoneNumber);
        this.isPhoneVerified = true;
    }

    /**
     * CI값 직접 설정 (기존 CI값이 있는 경우)
     */
    public void setCiValueDirect(String ciValueString) {
        if (this.ciValue == null) {
            this.ciValue = new CiValue();
        }
        this.ciValue.setValue(ciValueString);
    }

    /**
     * CI값 문자열 반환
     */
    public String getCiValueString() {
        return ciValue != null ? ciValue.getValue() : null;
    }

    /**
     * 프로필 이미지 URL 반환
     */
    public String getProfileImageUrl() {
        return personalInfo != null ? personalInfo.getProfilePhotoUrl() : null;
    }

    /**
     * 개인정보 초기화
     */
    public void initializePersonalInfo() {
        if (this.personalInfo == null) {
            this.personalInfo = new PersonalInfo();
        }
    }

    /**
     * 사업자 정보 초기화
     */
    public void initializeBusinessInfo() {
        if (this.businessInfo == null) {
            this.businessInfo = new BusinessInfo();
        }
    }

    /**
     * 급여 계좌 정보 초기화
     */
    public void initializeBankAccount() {
        if (this.bankAccount == null) {
            this.bankAccount = new BankAccount();
        }
    }

    /**
     * 개인정보 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getPersonalInfoCompletionPercentage() {
        if (personalInfo == null) {
            return 0;
        }
        return personalInfo.getCompletionPercentage();
    }

    /**
     * 사업자 정보 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getBusinessInfoCompletionPercentage() {
        if (businessInfo == null) {
            return 0;
        }
        return businessInfo.getCompletionPercentage();
    }

    /**
     * 급여 계좌 정보 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getBankAccountCompletionPercentage() {
        if (bankAccount == null) {
            return 0;
        }
        return bankAccount.getCompletionPercentage();
    }

    /**
     * 전체 프로필 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getOverallProfileCompletionPercentage() {
        int totalPercentage = 0;
        int categoryCount = 0;

        // 기본 정보 (이름, 이메일, 전화번호)
        int basicInfoPercentage = 0;
        if (name != null && !name.trim().isEmpty()) basicInfoPercentage += 33;
        if (email != null && !email.trim().isEmpty()) basicInfoPercentage += 33;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) basicInfoPercentage += 34;
        totalPercentage += basicInfoPercentage;
        categoryCount++;

        // 개인정보
        if (isPersonalUser()) {
            totalPercentage += getPersonalInfoCompletionPercentage();
            categoryCount++;
            
            totalPercentage += getBankAccountCompletionPercentage();
            categoryCount++;
        }

        // 사업자 정보 (관리자인 경우)
        if (isAdmin()) {
            totalPercentage += getBusinessInfoCompletionPercentage();
            categoryCount++;
        }

        return categoryCount > 0 ? totalPercentage / categoryCount : 0;
    }

    /**
     * 필수 정보 입력 완료 여부
     * @return 필수 정보가 모두 입력되었으면 true
     */
    public boolean isEssentialInfoComplete() {
        // 기본 정보 확인
        boolean basicComplete = name != null && !name.trim().isEmpty() &&
                               email != null && !email.trim().isEmpty() &&
                               phoneNumber != null && !phoneNumber.trim().isEmpty();

        if (!basicComplete) {
            return false;
        }

        // 역할별 필수 정보 확인
        if (isPersonalUser()) {
            return personalInfo != null && personalInfo.isEssentialInfoComplete();
        } else if (isAdmin()) {
            return businessInfo != null && businessInfo.isComplete();
        }

        return true;
    }

    /**
     * 마스킹된 개인정보 요약 반환
     * @return 마스킹된 개인정보 요약
     */
    public String getMaskedPersonalInfoSummary() {
        if (personalInfo == null) {
            return "개인정보 미입력";
        }

        StringBuilder summary = new StringBuilder();
        
        if (personalInfo.getRealName() != null) {
            summary.append("이름: ").append(personalInfo.getRealName()).append(", ");
        }
        
        if (personalInfo.getMaskedSsn() != null) {
            summary.append("주민번호: ").append(personalInfo.getMaskedSsn()).append(", ");
        }
        
        if (personalInfo.getMaskedEmergencyContact() != null) {
            summary.append("비상연락처: ").append(personalInfo.getMaskedEmergencyContact());
        }

        return summary.length() > 0 ? summary.toString() : "개인정보 미입력";
    }

    /**
     * 마스킹된 사업자 정보 요약 반환
     * @return 마스킹된 사업자 정보 요약
     */
    public String getMaskedBusinessInfoSummary() {
        if (businessInfo == null) {
            return "사업자 정보 미입력";
        }
        return businessInfo.getBusinessSummary();
    }

    /**
     * 마스킹된 계좌 정보 요약 반환
     * @return 마스킹된 계좌 정보 요약
     */
    public String getMaskedBankAccountSummary() {
        if (bankAccount == null) {
            return "계좌 정보 미입력";
        }
        return bankAccount.getAccountSummary();
    }
}
