package com.smartcon.global.security;

import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 5단계 역할 기반 접근 제어 (RBAC) 컴포넌트
 * 계층적 권한 구조를 기반으로 리소스 접근 권한을 검증합니다.
 * 
 * 역할 계층:
 * 1. ROLE_SUPER (슈퍼관리자) - 모든 리소스 접근 가능
 * 2. ROLE_HQ (본사관리자) - 회사 내 모든 리소스 접근 가능
 * 3. ROLE_SITE (현장관리자) - 담당 현장 리소스 접근 가능
 * 4. ROLE_TEAM (노무팀장) - 팀 리소스 접근 가능
 * 5. ROLE_WORKER (일반노무자) - 개인 리소스만 접근 가능
 */
@Slf4j
@Component
public class RoleBasedAccessControl {

    /**
     * 사용자가 특정 역할에 접근할 수 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @param targetRole 접근하려는 대상 역할
     * @return 접근 가능 여부
     */
    public boolean canAccessRole(Set<Role> userRoles, Role targetRole) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        // 사용자의 역할 중 하나라도 대상 역할에 접근 가능하면 true
        return userRoles.stream()
                .anyMatch(role -> role.canAccess(targetRole));
    }

    /**
     * 사용자가 특정 리소스를 읽을 수 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @param resourceOwnerRole 리소스 소유자의 역할
     * @return 읽기 권한 여부
     */
    public boolean canReadResource(Set<Role> userRoles, Role resourceOwnerRole) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        return userRoles.stream()
                .anyMatch(role -> role.canRead(resourceOwnerRole));
    }

    /**
     * 사용자가 특정 리소스를 수정할 수 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @param resourceOwnerRole 리소스 소유자의 역할
     * @return 쓰기 권한 여부
     */
    public boolean canWriteResource(Set<Role> userRoles, Role resourceOwnerRole) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        return userRoles.stream()
                .anyMatch(role -> role.canWrite(resourceOwnerRole));
    }

    /**
     * 사용자가 특정 리소스를 삭제할 수 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @param resourceOwnerRole 리소스 소유자의 역할
     * @return 삭제 권한 여부
     */
    public boolean canDeleteResource(Set<Role> userRoles, Role resourceOwnerRole) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        return userRoles.stream()
                .anyMatch(role -> role.canDelete(resourceOwnerRole));
    }

    /**
     * 사용자가 특정 로그인 유형을 사용할 수 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @param loginType 로그인 유형
     * @return 로그인 유형 사용 가능 여부
     */
    public boolean canUseLoginType(Set<Role> userRoles, LoginType loginType) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        return userRoles.stream()
                .anyMatch(role -> role.isValidLoginType(loginType));
    }

    /**
     * 사용자가 슈퍼관리자 권한을 가지고 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @return 슈퍼관리자 권한 여부
     */
    public boolean isSuperAdmin(Set<Role> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        return userRoles.contains(Role.ROLE_SUPER);
    }

    /**
     * 사용자가 본사관리자 이상 권한을 가지고 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @return 본사관리자 이상 권한 여부
     */
    public boolean isHqOrAbove(Set<Role> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        return userRoles.stream()
                .anyMatch(Role::isHqOrAbove);
    }

    /**
     * 사용자가 현장관리자 이상 권한을 가지고 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @return 현장관리자 이상 권한 여부
     */
    public boolean isSiteManagerOrAbove(Set<Role> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        return userRoles.stream()
                .anyMatch(Role::isSiteManagerOrAbove);
    }

    /**
     * 사용자가 관리자 역할을 가지고 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @return 관리자 역할 여부
     */
    public boolean isAdmin(Set<Role> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        return userRoles.stream()
                .anyMatch(Role::isAdminRole);
    }

    /**
     * 사용자가 개인 사용자 역할을 가지고 있는지 확인
     * 
     * @param userRoles 사용자의 역할 집합
     * @return 개인 사용자 역할 여부
     */
    public boolean isPersonalUser(Set<Role> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        return userRoles.stream()
                .anyMatch(Role::isPersonalRole);
    }

    /**
     * 사용자의 최고 권한 역할 반환
     * 
     * @param userRoles 사용자의 역할 집합
     * @return 최고 권한 역할 (레벨이 가장 낮은 역할)
     */
    public Role getHighestRole(Set<Role> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return null;
        }

        return userRoles.stream()
                .min((r1, r2) -> Integer.compare(r1.getLevel(), r2.getLevel()))
                .orElse(null);
    }

    /**
     * 로그인 유형별 역할 검증
     * 관리자는 사업자 로그인만, 개인사용자는 소셜 로그인만 사용 가능
     * 
     * @param user 사용자
     * @param loginType 로그인 유형
     * @return 검증 결과
     */
    public boolean validateLoginTypeForUser(User user, LoginType loginType) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            log.warn("사용자 또는 역할 정보가 없습니다.");
            return false;
        }

        return canUseLoginType(user.getRoles(), loginType);
    }

    /**
     * 역할별 허용된 로그인 유형 반환
     * 
     * @param role 역할
     * @return 허용된 로그인 유형 집합
     */
    public Set<LoginType> getAllowedLoginTypes(Role role) {
        if (role == null) {
            return Set.of();
        }

        return role.getAllowedLoginTypes();
    }

    /**
     * 사용자가 특정 현장에 접근할 수 있는지 확인
     * (추후 현장별 권한 매핑 구현시 사용)
     * 
     * @param userRoles 사용자의 역할 집합
     * @param siteId 현장 ID
     * @return 현장 접근 권한 여부
     */
    public boolean canAccessSite(Set<Role> userRoles, Long siteId) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        // 슈퍼관리자와 본사관리자는 모든 현장 접근 가능
        if (isSuperAdmin(userRoles) || isHqOrAbove(userRoles)) {
            return true;
        }

        // TODO: 현장별 권한 매핑 테이블 조회하여 검증
        // 현재는 현장관리자 이상이면 접근 가능하도록 임시 구현
        return isSiteManagerOrAbove(userRoles);
    }

    /**
     * 사용자가 특정 팀에 접근할 수 있는지 확인
     * (추후 팀별 권한 매핑 구현시 사용)
     * 
     * @param userRoles 사용자의 역할 집합
     * @param teamId 팀 ID
     * @return 팀 접근 권한 여부
     */
    public boolean canAccessTeam(Set<Role> userRoles, Long teamId) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("사용자 역할이 없습니다.");
            return false;
        }

        // 현장관리자 이상은 모든 팀 접근 가능
        if (isSiteManagerOrAbove(userRoles)) {
            return true;
        }

        // TODO: 팀별 권한 매핑 테이블 조회하여 검증
        // 현재는 팀장 이상이면 접근 가능하도록 임시 구현
        return userRoles.stream()
                .anyMatch(role -> role == Role.ROLE_TEAM);
    }
}
