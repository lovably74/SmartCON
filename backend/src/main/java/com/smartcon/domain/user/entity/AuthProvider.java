package com.smartcon.domain.user.entity;

/**
 * 인증 제공자 열거형
 * 사용자 인증 방식 구분
 */
public enum AuthProvider {
    LOCAL("일반 로그인"),      // 사업자번호 + 비밀번호
    KAKAO("카카오"),          // 카카오 소셜 로그인
    NAVER("네이버");          // 네이버 소셜 로그인

    private final String displayName;

    AuthProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 소셜 제공자인지 확인
     */
    public boolean isSocialProvider() {
        return this == KAKAO || this == NAVER;
    }

    /**
     * 로컬 제공자인지 확인
     */
    public boolean isLocalProvider() {
        return this == LOCAL;
    }

    /**
     * 해당 제공자의 로그인 유형 반환
     */
    public LoginType getLoginType() {
        return isSocialProvider() ? LoginType.SOCIAL : LoginType.BUSINESS;
    }

    /**
     * OAuth2 제공자 URL 반환
     */
    public String getOAuth2Url() {
        return switch (this) {
            case KAKAO -> "https://kauth.kakao.com/oauth/authorize";
            case NAVER -> "https://nid.naver.com/oauth2.0/authorize";
            case LOCAL -> null; // 로컬 로그인은 OAuth2 URL이 없음
        };
    }

    /**
     * 제공자별 클라이언트 ID 설정 키 반환
     */
    public String getClientIdConfigKey() {
        return switch (this) {
            case KAKAO -> "oauth2.kakao.client-id";
            case NAVER -> "oauth2.naver.client-id";
            case LOCAL -> null;
        };
    }

    /**
     * 제공자별 클라이언트 시크릿 설정 키 반환
     */
    public String getClientSecretConfigKey() {
        return switch (this) {
            case KAKAO -> "oauth2.kakao.client-secret";
            case NAVER -> "oauth2.naver.client-secret";
            case LOCAL -> null;
        };
    }
}