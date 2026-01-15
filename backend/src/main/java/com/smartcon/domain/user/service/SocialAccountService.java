package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.SocialAccount;
import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import com.smartcon.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 소셜 계정 관리 서비스 인터페이스
 */
public interface SocialAccountService {

    /**
     * 소셜 계정 연동
     * @param user 사용자
     * @param provider 소셜 제공자
     * @param providerId 제공자의 사용자 ID
     * @param providerEmail 제공자의 이메일
     * @param providerName 제공자의 사용자명
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresAt 토큰 만료 시간
     * @return 생성된 소셜 계정
     */
    SocialAccount linkSocialAccount(
            User user,
            SocialProvider provider,
            String providerId,
            String providerEmail,
            String providerName,
            String accessToken,
            String refreshToken,
            LocalDateTime expiresAt
    );

    /**
     * 소셜 계정 연동 해제
     * @param user 사용자
     * @param provider 소셜 제공자
     */
    void unlinkSocialAccount(User user, SocialProvider provider);

    /**
     * 사용자의 소셜 계정 목록 조회
     * @param user 사용자
     * @return 소셜 계정 목록
     */
    List<SocialAccount> getSocialAccounts(User user);

    /**
     * 사용자의 특정 소셜 계정 조회
     * @param user 사용자
     * @param provider 소셜 제공자
     * @return 소셜 계정 (없으면 null)
     */
    SocialAccount getSocialAccount(User user, SocialProvider provider);

    /**
     * 소셜 계정 토큰 업데이트
     * @param socialAccount 소셜 계정
     * @param accessToken 새로운 액세스 토큰
     * @param refreshToken 새로운 리프레시 토큰
     * @param expiresAt 새로운 만료 시간
     */
    void updateTokens(SocialAccount socialAccount, String accessToken, String refreshToken, LocalDateTime expiresAt);

    /**
     * 주 소셜 계정 설정
     * @param user 사용자
     * @param provider 소셜 제공자
     */
    void setPrimarySocialAccount(User user, SocialProvider provider);
}
