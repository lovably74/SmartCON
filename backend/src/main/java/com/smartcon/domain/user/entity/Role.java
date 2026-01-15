package com.smartcon.domain.user.entity;

import java.util.Set;

/**
 * 5단계 사용자 역할 열거형
 * 계층적 권한 구조를 가진 역할 시스템
 * 
 * 역할 계층:
 * 1. ROLE_SUPER (슈퍼관리자) - 시스템 전체 관리, 구독 승인
 * 2. ROLE_HQ (본사관리자) - 회사 전체 관리, 사업자번호 로그인
 * 3. ROLE_SITE (현장관리자) - 현장별 관리, 사업자번호 로그인
 * 4. ROLE_TEAM (노무팀장) - 팀 단위 관리, 소셜 로그인
 * 5. ROLE_WORKER (일반노무자) - 개인 정보 관리, 소셜 로그인
 */
public enum Role {
    ROLE_SUPER(1, "슈퍼관리자", true, false),      // 시스템 전체 관리, 구독 승인
    ROLE_HQ(2, "본사관리자", true, false),         // 회사 전체 관리, 사업자번호 로그인
    ROLE_SITE(3, "현장관리자", true, false),       // 현장별 관리, 사업자번호 로그인
    ROLE_TEAM(4, "노무팀장", false, true),         // 팀 단위 관리, 소셜 로그인
    ROLE_WORKER(5, "일반노무자", false, true);     // 개인 정보 관리, 소셜 로그인
    
    private final int level;
    private final String displayName;
    private final boolean requiresBusinessLogin;
    private final boolean allowsSocialLogin;
    
    Role(int level, String displayName, boolean requiresBusinessLogin, boolean allowsSocialLogin) {
        this.level = level;
        this.displayName = displayName;
        this.requiresBusinessLogin = requiresBusinessLogin;
        this.allowsSocialLogin = allowsSocialLogin;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean requiresBusinessLogin() {
        return requiresBusinessLogin;
    }
    
    public boolean allowsSocialLogin() {
        return allowsSocialLogin;
    }
    
    /**
     * 계층적 권한 체크: 현재 역할이 대상 역할에 접근 가능한지 확인
     * 레벨이 낮을수록 높은 권한을 가짐 (1 > 2 > 3 > 4 > 5)
     * 
     * @param targetRole 접근하려는 대상 역할
     * @return 접근 가능 여부
     */
    public boolean canAccess(Role targetRole) {
        return this.level <= targetRole.level;
    }
    
    /**
     * 특정 리소스에 대한 읽기 권한 확인
     * 
     * @param resourceOwnerRole 리소스 소유자의 역할
     * @return 읽기 권한 여부
     */
    public boolean canRead(Role resourceOwnerRole) {
        // 상위 역할은 하위 역할의 리소스를 읽을 수 있음
        return this.level <= resourceOwnerRole.level;
    }
    
    /**
     * 특정 리소스에 대한 쓰기 권한 확인
     * 
     * @param resourceOwnerRole 리소스 소유자의 역할
     * @return 쓰기 권한 여부
     */
    public boolean canWrite(Role resourceOwnerRole) {
        // 상위 역할은 하위 역할의 리소스를 수정할 수 있음
        return this.level <= resourceOwnerRole.level;
    }
    
    /**
     * 특정 리소스에 대한 삭제 권한 확인
     * 
     * @param resourceOwnerRole 리소스 소유자의 역할
     * @return 삭제 권한 여부
     */
    public boolean canDelete(Role resourceOwnerRole) {
        // 상위 역할은 하위 역할의 리소스를 삭제할 수 있음
        return this.level <= resourceOwnerRole.level;
    }
    
    /**
     * 로그인 유형 검증
     * 
     * @param loginType 로그인 유형
     * @return 해당 로그인 유형 사용 가능 여부
     */
    public boolean isValidLoginType(LoginType loginType) {
        return switch (loginType) {
            case BUSINESS -> requiresBusinessLogin;
            case SOCIAL -> allowsSocialLogin;
        };
    }
    
    /**
     * 허용된 로그인 유형 반환
     * 
     * @return 허용된 로그인 유형 집합
     */
    public Set<LoginType> getAllowedLoginTypes() {
        if (requiresBusinessLogin) {
            return Set.of(LoginType.BUSINESS);
        } else if (allowsSocialLogin) {
            return Set.of(LoginType.SOCIAL);
        }
        return Set.of();
    }
    
    /**
     * 관리자 역할 여부 확인
     * 
     * @return 관리자 역할 여부 (SUPER, HQ, SITE)
     */
    public boolean isAdminRole() {
        return this == ROLE_SUPER || this == ROLE_HQ || this == ROLE_SITE;
    }
    
    /**
     * 개인 사용자 역할 여부 확인
     * 
     * @return 개인 사용자 역할 여부 (TEAM, WORKER)
     */
    public boolean isPersonalRole() {
        return this == ROLE_TEAM || this == ROLE_WORKER;
    }
    
    /**
     * 슈퍼관리자 여부 확인
     * 
     * @return 슈퍼관리자 여부
     */
    public boolean isSuperAdmin() {
        return this == ROLE_SUPER;
    }
    
    /**
     * 본사관리자 이상 권한 확인
     * 
     * @return 본사관리자 이상 권한 여부
     */
    public boolean isHqOrAbove() {
        return this.level <= ROLE_HQ.level;
    }
    
    /**
     * 현장관리자 이상 권한 확인
     * 
     * @return 현장관리자 이상 권한 여부
     */
    public boolean isSiteManagerOrAbove() {
        return this.level <= ROLE_SITE.level;
    }
    
    /**
     * Spring Security 권한 문자열 반환
     * 
     * @return Spring Security 권한 문자열 (예: "ROLE_SUPER")
     */
    public String getAuthority() {
        return this.name();
    }
}
