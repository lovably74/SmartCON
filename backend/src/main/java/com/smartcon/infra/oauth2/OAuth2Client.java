package com.smartcon.infra.oauth2;

import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;

/**
 * OAuth2 클라이언트 인터페이스
 * 소셜 로그인 제공자별 OAuth2 인증 처리
 */
public interface OAuth2Client {

    /**
     * 지원하는 소셜 제공자 반환
     */
    SocialProvider getProvider();

    /**
     * OAuth2 인증 URL 생성
     * @param redirectUri 리다이렉트 URI
     * @param state CSRF 방지용 상태값
     * @return 인증 URL
     */
    String getAuthorizationUrl(String redirectUri, String state);

    /**
     * 인증 코드로 액세스 토큰 요청
     * @param authCode 인증 코드
     * @param redirectUri 리다이렉트 URI
     * @return OAuth2 토큰 정보
     */
    OAuth2TokenResponse getAccessToken(String authCode, String redirectUri);

    /**
     * 액세스 토큰으로 사용자 정보 조회
     * @param accessToken 액세스 토큰
     * @return 사용자 정보
     */
    OAuth2UserInfo getUserInfo(String accessToken);

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 OAuth2 토큰 정보
     */
    OAuth2TokenResponse refreshAccessToken(String refreshToken);
}
