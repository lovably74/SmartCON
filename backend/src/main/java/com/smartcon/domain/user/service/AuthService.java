package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.LoginRequest;
import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.RefreshTokenRequest;

/**
 * 인증 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 사용자 로그인
     */
    LoginResponse login(LoginRequest request);

    /**
     * 토큰 갱신
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 로그아웃
     */
    void logout(String accessToken);

    /**
     * 토큰 검증
     */
    boolean validateToken(String token);

    /**
     * 개발용 토큰 생성
     */
    LoginResponse generateDevToken(String role, String tenantId);
}