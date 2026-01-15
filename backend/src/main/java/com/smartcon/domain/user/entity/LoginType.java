package com.smartcon.domain.user.entity;

import java.util.Set;

/**
 * 로그인 유형 열거형
 * 통합 로그인 시스템의 로그인 방식 구분
 */
public enum LoginType {
    BUSINESS("사업자 로그인"),    // 사업자번호 + 비밀번호 (관리자용)
    SOCIAL("소셜 로그인");       // 카카오/네이버 (개인사용자용)

    private final String displayName;

    LoginType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 해당 로그인 유형에서 허용되는 역할들 반환
     */
    public Set<Role> getAllowedRoles() {
        return switch (this) {
            case BUSINESS -> Set.of(Role.ROLE_SUPER, Role.ROLE_HQ, Role.ROLE_SITE);
            case SOCIAL -> Set.of(Role.ROLE_TEAM, Role.ROLE_WORKER);
        };
    }

    /**
     * 특정 역할이 이 로그인 유형을 사용할 수 있는지 확인
     */
    public boolean isAllowedForRole(Role role) {
        return getAllowedRoles().contains(role);
    }

    /**
     * 관리자용 로그인 유형인지 확인
     */
    public boolean isAdminLoginType() {
        return this == BUSINESS;
    }

    /**
     * 개인사용자용 로그인 유형인지 확인
     */
    public boolean isPersonalLoginType() {
        return this == SOCIAL;
    }
}