package com.smartcon.infra.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OAuth2 토큰 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2TokenResponse {

    private String accessToken; // 액세스 토큰
    private String refreshToken; // 리프레시 토큰
    private String tokenType; // 토큰 타입 (Bearer)
    private Long expiresIn; // 만료 시간 (초)
    private LocalDateTime expiresAt; // 만료 일시
    private String scope; // 권한 범위

    /**
     * 토큰이 유효한지 확인
     */
    public boolean isValid() {
        return expiresAt != null && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * 토큰 만료까지 남은 시간 (초)
     */
    public long getSecondsUntilExpiration() {
        if (expiresAt == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }
}
