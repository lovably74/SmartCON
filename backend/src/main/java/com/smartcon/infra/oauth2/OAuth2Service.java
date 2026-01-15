package com.smartcon.infra.oauth2;

import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OAuth2 서비스
 * 소셜 로그인 제공자별 OAuth2 클라이언트 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final List<OAuth2Client> oauth2Clients;
    private final Map<SocialProvider, OAuth2Client> clientMap;

    /**
     * OAuth2 클라이언트 목록으로 맵 생성
     */
    public OAuth2Service(List<OAuth2Client> oauth2Clients) {
        this.oauth2Clients = oauth2Clients;
        this.clientMap = oauth2Clients.stream()
                .collect(Collectors.toMap(OAuth2Client::getProvider, Function.identity()));
        log.info("OAuth2 클라이언트 초기화 완료 - 제공자: {}", clientMap.keySet());
    }

    /**
     * 소셜 제공자에 해당하는 OAuth2 클라이언트 조회
     */
    public OAuth2Client getClient(SocialProvider provider) {
        OAuth2Client client = clientMap.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 제공자입니다: " + provider);
        }
        return client;
    }

    /**
     * OAuth2 인증 URL 생성
     */
    public String getAuthorizationUrl(SocialProvider provider, String redirectUri, String state) {
        return getClient(provider).getAuthorizationUrl(redirectUri, state);
    }

    /**
     * 인증 코드로 액세스 토큰 요청
     */
    public OAuth2TokenResponse getAccessToken(SocialProvider provider, String authCode, String redirectUri) {
        return getClient(provider).getAccessToken(authCode, redirectUri);
    }

    /**
     * 액세스 토큰으로 사용자 정보 조회
     */
    public OAuth2UserInfo getUserInfo(SocialProvider provider, String accessToken) {
        return getClient(provider).getUserInfo(accessToken);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    public OAuth2TokenResponse refreshAccessToken(SocialProvider provider, String refreshToken) {
        return getClient(provider).refreshAccessToken(refreshToken);
    }

    /**
     * 지원하는 소셜 제공자 목록 조회
     */
    public List<SocialProvider> getSupportedProviders() {
        return List.copyOf(clientMap.keySet());
    }
}
