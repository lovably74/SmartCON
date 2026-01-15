package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.*;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.LoginType;

/**
 * 인증 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 사용자 로그인 (기존 방식 - 하위 호환성 유지)
     */
    LoginResponse login(LoginRequest request);

    /**
     * 통합 로그인 처리
     * 개인사용자(소셜 로그인)와 관리자(사업자 로그인)를 구분하여 처리
     */
    LoginResponse authenticateUnified(UnifiedLoginRequest request);

    /**
     * 휴대폰 인증 및 CI값 생성
     * 최초 소셜 로그인시 사용
     */
    CiValueResponse generateCiValue(PhoneVerificationRequest request);

    /**
     * 사용자 역할 목록 조회
     * 다중 역할을 가진 사용자의 역할 선택을 위해 사용
     */
    UserRolesResponse getUserRoles(Long userId);

    /**
     * 역할 선택 및 토큰 재발급
     * 다중 역할을 가진 사용자가 특정 역할을 선택할 때 사용
     */
    LoginResponse selectRole(Long userId, RoleSelectionRequest request);

    /**
     * 로그인 유형별 역할 검증
     * 특정 역할이 해당 로그인 유형을 사용할 수 있는지 확인
     */
    boolean validateLoginTypeForRole(Role role, com.smartcon.domain.user.entity.LoginType loginType);

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
