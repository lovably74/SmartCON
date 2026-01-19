package com.smartcon.global.security;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * OAuth2 토큰 검증 서비스
 * Kakao, Naver 소셜 로그인 토큰의 유효성 검증
 */
@Component
public class OAuth2TokenValidator {

    private final RestTemplate restTemplate;

    // Kakao API 엔드포인트
    private static final String KAKAO_TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    // Naver API 엔드포인트
    private static final String NAVER_TOKEN_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    public OAuth2TokenValidator() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Kakao 액세스 토큰 검증
     * @param accessToken Kakao 액세스 토큰
     * @return 토큰이 유효하면 true
     */
    public boolean validateKakaoToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_TOKEN_INFO_URL,
                HttpMethod.GET,
                entity,
                Map.class
            );

            // 응답 상태 코드가 200이고 응답 본문에 id가 있으면 유효한 토큰
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody() != null && 
                   response.getBody().containsKey("id");
        } catch (Exception e) {
            // 토큰 검증 실패 (만료, 잘못된 토큰 등)
            return false;
        }
    }

    /**
     * Naver 액세스 토큰 검증
     * @param accessToken Naver 액세스 토큰
     * @return 토큰이 유효하면 true
     */
    public boolean validateNaverToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                NAVER_TOKEN_INFO_URL,
                HttpMethod.GET,
                entity,
                Map.class
            );

            // 응답 상태 코드가 200이고 resultcode가 "00"이면 유효한 토큰
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody() != null && 
                   "00".equals(response.getBody().get("resultcode"));
        } catch (Exception e) {
            // 토큰 검증 실패 (만료, 잘못된 토큰 등)
            return false;
        }
    }

    /**
     * Kakao 사용자 정보 조회
     * @param accessToken Kakao 액세스 토큰
     * @return 사용자 정보 Map (id, email 등)
     */
    public Map<String, Object> getKakaoUserInfo(String accessToken) {
        if (!validateKakaoToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Kakao 토큰입니다");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Kakao 사용자 정보 조회 실패", e);
        }
    }

    /**
     * Naver 사용자 정보 조회
     * @param accessToken Naver 액세스 토큰
     * @return 사용자 정보 Map (id, email 등)
     */
    public Map<String, Object> getNaverUserInfo(String accessToken) {
        if (!validateNaverToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Naver 토큰입니다");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                NAVER_TOKEN_INFO_URL,
                HttpMethod.GET,
                entity,
                Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Naver 사용자 정보 조회 실패", e);
        }
    }

    /**
     * 소셜 제공자별 토큰 검증
     * @param provider 소셜 제공자 (KAKAO, NAVER)
     * @param accessToken 액세스 토큰
     * @return 토큰이 유효하면 true
     */
    public boolean validateToken(String provider, String accessToken) {
        if (provider == null || accessToken == null) {
            return false;
        }

        return switch (provider.toUpperCase()) {
            case "KAKAO" -> validateKakaoToken(accessToken);
            case "NAVER" -> validateNaverToken(accessToken);
            default -> false;
        };
    }

    /**
     * 소셜 제공자별 사용자 정보 조회
     * @param provider 소셜 제공자 (KAKAO, NAVER)
     * @param accessToken 액세스 토큰
     * @return 사용자 정보 Map
     */
    public Map<String, Object> getUserInfo(String provider, String accessToken) {
        if (provider == null || accessToken == null) {
            throw new IllegalArgumentException("제공자와 토큰은 필수입니다");
        }

        return switch (provider.toUpperCase()) {
            case "KAKAO" -> getKakaoUserInfo(accessToken);
            case "NAVER" -> getNaverUserInfo(accessToken);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 제공자입니다: " + provider);
        };
    }

    /**
     * 토큰 형식 검증 (기본적인 형식 체크)
     * @param token 검증할 토큰
     * @return 형식이 올바르면 true
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // 최소 길이 확인 (일반적으로 OAuth2 토큰은 최소 20자 이상)
        if (token.length() < 20) {
            return false;
        }

        // 허용된 문자만 포함하는지 확인 (영문, 숫자, 하이픈, 언더스코어, 점)
        return token.matches("^[A-Za-z0-9_.-]+$");
    }
}
