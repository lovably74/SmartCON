package com.smartcon.infra.oauth2;

import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * 네이버 OAuth2 클라이언트 구현
 */
@Component
@Slf4j
public class NaverOAuth2Client implements OAuth2Client {

    @Value("${oauth2.naver.client-id:}")
    private String clientId;

    @Value("${oauth2.naver.client-secret:}")
    private String clientSecret;

    @Value("${oauth2.naver.authorization-uri:https://nid.naver.com/oauth2.0/authorize}")
    private String authorizationUri;

    @Value("${oauth2.naver.token-uri:https://nid.naver.com/oauth2.0/token}")
    private String tokenUri;

    @Value("${oauth2.naver.user-info-uri:https://openapi.naver.com/v1/nid/me}")
    private String userInfoUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.NAVER;
    }

    @Override
    public String getAuthorizationUrl(String redirectUri, String state) {
        return String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&state=%s",
                authorizationUri, clientId, redirectUri, state);
    }

    @Override
    public OAuth2TokenResponse getAccessToken(String authCode, String redirectUri) {
        log.info("네이버 액세스 토큰 요청 - 인증 코드: {}", authCode);

        // TODO: 실제 네이버 API 호출 구현
        // 현재는 임시 응답 반환
        return OAuth2TokenResponse.builder()
                .accessToken("naver_access_token_" + authCode)
                .refreshToken("naver_refresh_token_" + authCode)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1시간
                .expiresAt(LocalDateTime.now().plusHours(1))
                .scope("profile mobile")
                .build();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        log.info("네이버 사용자 정보 조회 - 액세스 토큰: {}", accessToken);

        // TODO: 실제 네이버 API 호출 구현
        // 현재는 임시 사용자 정보 반환
        return OAuth2UserInfo.builder()
                .provider(SocialProvider.NAVER)
                .providerId("naver_user_" + System.currentTimeMillis())
                .email("naver_user@example.com")
                .name("네이버 사용자")
                .phoneNumber("01087654321")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
    }

    @Override
    public OAuth2TokenResponse refreshAccessToken(String refreshToken) {
        log.info("네이버 액세스 토큰 갱신 - 리프레시 토큰: {}", refreshToken);

        // TODO: 실제 네이버 API 호출 구현
        // 현재는 임시 응답 반환
        return OAuth2TokenResponse.builder()
                .accessToken("naver_new_access_token_" + refreshToken)
                .refreshToken(refreshToken) // 리프레시 토큰은 재사용
                .tokenType("Bearer")
                .expiresIn(3600L) // 1시간
                .expiresAt(LocalDateTime.now().plusHours(1))
                .scope("profile mobile")
                .build();
    }
}
