package com.smartcon.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 로그인 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private Long expiresIn; // Access Token 만료 시간 (초)
    
    private UserInfo user;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String id;
        private String name;
        private String email;
        private String role;
        private String tenantId;
        private Map<String, Object> permissions;
        private String profileImageUrl;
    }
}