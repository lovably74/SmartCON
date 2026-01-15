package com.smartcon.domain.user.dto;

import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통합 로그인 요청 DTO
 * 개인사용자(소셜 로그인)와 관리자(사업자 로그인)를 구분하여 처리
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnifiedLoginRequest {

    @NotNull(message = "로그인 유형은 필수입니다")
    private LoginType loginType; // BUSINESS 또는 SOCIAL

    // 사업자 로그인용 필드 (관리자: 슈퍼관리자, 본사관리자, 현장관리자)
    private String businessNumber; // 사업자번호
    private String password; // 비밀번호

    // 소셜 로그인용 필드 (개인사용자: 노무팀장, 일반노무자)
    private SocialProvider provider; // KAKAO, NAVER
    private String authCode; // OAuth2 인증 코드
    private String phoneNumber; // 최초 소셜 로그인시 휴대폰 번호

    /**
     * 사업자 로그인 요청인지 확인
     */
    public boolean isBusinessLogin() {
        return loginType == LoginType.BUSINESS;
    }

    /**
     * 소셜 로그인 요청인지 확인
     */
    public boolean isSocialLogin() {
        return loginType == LoginType.SOCIAL;
    }

    /**
     * 사업자 로그인 요청 검증
     */
    public void validateBusinessLogin() {
        if (isBusinessLogin()) {
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new IllegalArgumentException("사업자번호는 필수입니다");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다");
            }
        }
    }

    /**
     * 소셜 로그인 요청 검증
     */
    public void validateSocialLogin() {
        if (isSocialLogin()) {
            if (provider == null) {
                throw new IllegalArgumentException("소셜 제공자는 필수입니다");
            }
            if (authCode == null || authCode.isBlank()) {
                throw new IllegalArgumentException("인증 코드는 필수입니다");
            }
        }
    }
}
