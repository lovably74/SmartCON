package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.SocialAccount;
import com.smartcon.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 관리 서비스 인터페이스
 * 5단계 역할 기반 시스템의 사용자 관리 기능
 */
public interface UserManagementService {

    /**
     * CI값 기반 사용자 조회/생성 (개인사용자용)
     * CI값이 존재하면 기존 사용자 반환, 없으면 새 사용자 생성
     */
    User findOrCreateUserByCi(String ciValue, SocialAccount.SocialProvider provider);

    /**
     * 사업자번호 기반 사용자 조회 (관리자용)
     */
    Optional<User> findUserByBusinessNumber(String businessNumber);

    /**
     * 사용자의 역할 목록 조회
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 특정 역할을 가진 사용자의 현장 목록 조회
     */
    List<Long> getUserSiteIds(Long userId, Role role);

    /**
     * 개인정보 업데이트
     */
    void updatePersonalInfo(Long userId, PersonalInfoUpdateRequest request);

    /**
     * 사업자 정보 업데이트
     */
    void updateBusinessInfo(Long userId, BusinessInfoUpdateRequest request);

    /**
     * 소셜 계정 추가
     */
    void addSocialAccount(Long userId, SocialAccount.SocialProvider provider, String providerId, String providerEmail);

    /**
     * 사용자 활성화/비활성화
     */
    void toggleUserStatus(Long userId, boolean isActive);

    /**
     * CI값 생성 (전화번호 인증 기반)
     */
    String generateCiValue(String phoneNumber);

    /**
     * 전화번호 인증 완료 처리
     */
    void completePhoneVerification(Long userId, String phoneNumber);

    // === DTO 클래스들 ===

    /**
     * 개인정보 업데이트 요청 DTO
     */
    record PersonalInfoUpdateRequest(
            String residentNumber,
            String address,
            String emergencyContact,
            String profileImageUrl,
            BankAccountInfo bankAccount
    ) {}

    /**
     * 사업자 정보 업데이트 요청 DTO
     */
    record BusinessInfoUpdateRequest(
            String companyName,
            String businessRegistrationNumber,
            String representativeName,
            String businessAddress,
            String businessPhone,
            String businessEmail,
            String businessType,
            String businessItem
    ) {}

    /**
     * 은행 계좌 정보 DTO
     */
    record BankAccountInfo(
            String bankName,
            String accountNumber,
            String accountHolder
    ) {}
}