package com.smartcon.global.security;

import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.Role;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 역할 기반 접근 제어 속성 테스트
 * Feature: smartcon-lite-role-based-system, Property 14: Role-Based Access Control
 * Validates: Requirements 27.5
 * 
 * Property 14: Role-Based Access Control
 * For any user request to access a resource, the system should only allow access 
 * if the user's current role has appropriate permissions for that resource
 */
@DisplayName("역할 기반 접근 제어 속성 테스트")
class RoleBasedAccessControlPropertyTest {

    private RoleBasedAccessControl rbac;

    @BeforeEach
    void setUp() {
        rbac = new RoleBasedAccessControl();
    }

    /**
     * Property 14.1: 계층적 권한 체크
     * 
     * 모든 역할 조합에 대해:
     * 상위 역할(레벨이 낮은)은 하위 역할(레벨이 높은)의 리소스에 접근할 수 있어야 함
     */
    @Property(tries = 100)
    @DisplayName("상위 역할은 하위 역할의 리소스에 접근할 수 있어야 한다")
    void higherRoleShouldAccessLowerRoleResources(
            @ForAll("roles") Role userRole,
            @ForAll("roles") Role resourceRole) {

        // Given: 사용자 역할 집합
        Set<Role> userRoles = Set.of(userRole);

        // When: 리소스 접근 권한 확인
        boolean canAccess = rbac.canAccessRole(userRoles, resourceRole);

        // Then: 상위 역할은 하위 역할의 리소스에 접근 가능해야 함
        if (userRole.getLevel() <= resourceRole.getLevel()) {
            assertThat(canAccess).isTrue();
        } else {
            assertThat(canAccess).isFalse();
        }
    }

    /**
     * Property 14.2: 읽기 권한 검증
     * 
     * 모든 역할 조합에 대해:
     * 상위 역할은 하위 역할의 리소스를 읽을 수 있어야 함
     */
    @Property(tries = 100)
    @DisplayName("상위 역할은 하위 역할의 리소스를 읽을 수 있어야 한다")
    void higherRoleShouldReadLowerRoleResources(
            @ForAll("roles") Role userRole,
            @ForAll("roles") Role resourceOwnerRole) {

        // Given: 사용자 역할 집합
        Set<Role> userRoles = Set.of(userRole);

        // When: 읽기 권한 확인
        boolean canRead = rbac.canReadResource(userRoles, resourceOwnerRole);

        // Then: 상위 역할은 하위 역할의 리소스를 읽을 수 있어야 함
        if (userRole.getLevel() <= resourceOwnerRole.getLevel()) {
            assertThat(canRead).isTrue();
        } else {
            assertThat(canRead).isFalse();
        }
    }

    /**
     * Property 14.3: 쓰기 권한 검증
     * 
     * 모든 역할 조합에 대해:
     * 상위 역할은 하위 역할의 리소스를 수정할 수 있어야 함
     */
    @Property(tries = 100)
    @DisplayName("상위 역할은 하위 역할의 리소스를 수정할 수 있어야 한다")
    void higherRoleShouldWriteLowerRoleResources(
            @ForAll("roles") Role userRole,
            @ForAll("roles") Role resourceOwnerRole) {

        // Given: 사용자 역할 집합
        Set<Role> userRoles = Set.of(userRole);

        // When: 쓰기 권한 확인
        boolean canWrite = rbac.canWriteResource(userRoles, resourceOwnerRole);

        // Then: 상위 역할은 하위 역할의 리소스를 수정할 수 있어야 함
        if (userRole.getLevel() <= resourceOwnerRole.getLevel()) {
            assertThat(canWrite).isTrue();
        } else {
            assertThat(canWrite).isFalse();
        }
    }

    /**
     * Property 14.4: 삭제 권한 검증
     * 
     * 모든 역할 조합에 대해:
     * 상위 역할은 하위 역할의 리소스를 삭제할 수 있어야 함
     */
    @Property(tries = 100)
    @DisplayName("상위 역할은 하위 역할의 리소스를 삭제할 수 있어야 한다")
    void higherRoleShouldDeleteLowerRoleResources(
            @ForAll("roles") Role userRole,
            @ForAll("roles") Role resourceOwnerRole) {

        // Given: 사용자 역할 집합
        Set<Role> userRoles = Set.of(userRole);

        // When: 삭제 권한 확인
        boolean canDelete = rbac.canDeleteResource(userRoles, resourceOwnerRole);

        // Then: 상위 역할은 하위 역할의 리소스를 삭제할 수 있어야 함
        if (userRole.getLevel() <= resourceOwnerRole.getLevel()) {
            assertThat(canDelete).isTrue();
        } else {
            assertThat(canDelete).isFalse();
        }
    }

    /**
     * Property 14.5: 로그인 유형 검증
     * 
     * 모든 역할과 로그인 유형 조합에 대해:
     * 관리자 역할(SUPER, HQ, SITE)은 사업자 로그인만 사용 가능
     * 개인사용자 역할(TEAM, WORKER)은 소셜 로그인만 사용 가능
     */
    @Property(tries = 100)
    @DisplayName("역할별로 허용된 로그인 유형만 사용할 수 있어야 한다")
    void roleShouldOnlyUseAllowedLoginType(
            @ForAll("roles") Role role,
            @ForAll("loginTypes") LoginType loginType) {

        // Given: 사용자 역할 집합
        Set<Role> userRoles = Set.of(role);

        // When: 로그인 유형 사용 가능 여부 확인
        boolean canUseLoginType = rbac.canUseLoginType(userRoles, loginType);

        // Then: 역할에 맞는 로그인 유형만 사용 가능해야 함
        boolean expectedResult = role.isValidLoginType(loginType);
        assertThat(canUseLoginType).isEqualTo(expectedResult);

        // Then: 관리자 역할은 사업자 로그인만 가능
        if (role.isAdminRole()) {
            if (loginType == LoginType.BUSINESS) {
                assertThat(canUseLoginType).isTrue();
            } else {
                assertThat(canUseLoginType).isFalse();
            }
        }

        // Then: 개인사용자 역할은 소셜 로그인만 가능
        if (role.isPersonalRole()) {
            if (loginType == LoginType.SOCIAL) {
                assertThat(canUseLoginType).isTrue();
            } else {
                assertThat(canUseLoginType).isFalse();
            }
        }
    }

    /**
     * Property 14.6: 슈퍼관리자 권한 검증
     * 
     * 모든 역할 집합에 대해:
     * ROLE_SUPER가 포함된 경우에만 슈퍼관리자로 인식되어야 함
     */
    @Property(tries = 100)
    @DisplayName("ROLE_SUPER가 포함된 경우에만 슈퍼관리자로 인식되어야 한다")
    void onlySuperRoleShouldBeRecognizedAsSuperAdmin(
            @ForAll("roleSets") Set<Role> userRoles) {

        // When: 슈퍼관리자 권한 확인
        boolean isSuperAdmin = rbac.isSuperAdmin(userRoles);

        // Then: ROLE_SUPER가 포함된 경우에만 true
        assertThat(isSuperAdmin).isEqualTo(userRoles.contains(Role.ROLE_SUPER));
    }

    /**
     * Property 14.7: 본사관리자 이상 권한 검증
     * 
     * 모든 역할 집합에 대해:
     * ROLE_SUPER 또는 ROLE_HQ가 포함된 경우 본사관리자 이상으로 인식되어야 함
     */
    @Property(tries = 100)
    @DisplayName("ROLE_SUPER 또는 ROLE_HQ가 포함된 경우 본사관리자 이상으로 인식되어야 한다")
    void hqOrAboveRolesShouldBeRecognized(
            @ForAll("roleSets") Set<Role> userRoles) {

        // When: 본사관리자 이상 권한 확인
        boolean isHqOrAbove = rbac.isHqOrAbove(userRoles);

        // Then: ROLE_SUPER 또는 ROLE_HQ가 포함된 경우에만 true
        boolean expected = userRoles.contains(Role.ROLE_SUPER) || userRoles.contains(Role.ROLE_HQ);
        assertThat(isHqOrAbove).isEqualTo(expected);
    }

    /**
     * Property 14.8: 현장관리자 이상 권한 검증
     * 
     * 모든 역할 집합에 대해:
     * ROLE_SUPER, ROLE_HQ, ROLE_SITE 중 하나가 포함된 경우 현장관리자 이상으로 인식되어야 함
     */
    @Property(tries = 100)
    @DisplayName("ROLE_SUPER, ROLE_HQ, ROLE_SITE 중 하나가 포함된 경우 현장관리자 이상으로 인식되어야 한다")
    void siteManagerOrAboveRolesShouldBeRecognized(
            @ForAll("roleSets") Set<Role> userRoles) {

        // When: 현장관리자 이상 권한 확인
        boolean isSiteManagerOrAbove = rbac.isSiteManagerOrAbove(userRoles);

        // Then: ROLE_SUPER, ROLE_HQ, ROLE_SITE 중 하나가 포함된 경우에만 true
        boolean expected = userRoles.contains(Role.ROLE_SUPER) 
                || userRoles.contains(Role.ROLE_HQ) 
                || userRoles.contains(Role.ROLE_SITE);
        assertThat(isSiteManagerOrAbove).isEqualTo(expected);
    }

    /**
     * Property 14.9: 관리자 역할 검증
     * 
     * 모든 역할 집합에 대해:
     * ROLE_SUPER, ROLE_HQ, ROLE_SITE 중 하나가 포함된 경우 관리자로 인식되어야 함
     */
    @Property(tries = 100)
    @DisplayName("ROLE_SUPER, ROLE_HQ, ROLE_SITE 중 하나가 포함된 경우 관리자로 인식되어야 한다")
    void adminRolesShouldBeRecognized(
            @ForAll("roleSets") Set<Role> userRoles) {

        // When: 관리자 역할 확인
        boolean isAdmin = rbac.isAdmin(userRoles);

        // Then: ROLE_SUPER, ROLE_HQ, ROLE_SITE 중 하나가 포함된 경우에만 true
        boolean expected = userRoles.stream().anyMatch(Role::isAdminRole);
        assertThat(isAdmin).isEqualTo(expected);
    }

    /**
     * Property 14.10: 개인사용자 역할 검증
     * 
     * 모든 역할 집합에 대해:
     * ROLE_TEAM 또는 ROLE_WORKER가 포함된 경우 개인사용자로 인식되어야 함
     */
    @Property(tries = 100)
    @DisplayName("ROLE_TEAM 또는 ROLE_WORKER가 포함된 경우 개인사용자로 인식되어야 한다")
    void personalUserRolesShouldBeRecognized(
            @ForAll("roleSets") Set<Role> userRoles) {

        // When: 개인사용자 역할 확인
        boolean isPersonalUser = rbac.isPersonalUser(userRoles);

        // Then: ROLE_TEAM 또는 ROLE_WORKER가 포함된 경우에만 true
        boolean expected = userRoles.stream().anyMatch(Role::isPersonalRole);
        assertThat(isPersonalUser).isEqualTo(expected);
    }

    /**
     * Property 14.11: 최고 권한 역할 반환
     * 
     * 모든 역할 집합에 대해:
     * 레벨이 가장 낮은(권한이 가장 높은) 역할이 반환되어야 함
     */
    @Property(tries = 100)
    @DisplayName("레벨이 가장 낮은 역할이 최고 권한 역할로 반환되어야 한다")
    void highestRoleShouldBeLowestLevel(
            @ForAll("nonEmptyRoleSets") Set<Role> userRoles) {

        // When: 최고 권한 역할 조회
        Role highestRole = rbac.getHighestRole(userRoles);

        // Then: 반환된 역할이 null이 아니어야 함
        assertThat(highestRole).isNotNull();

        // Then: 반환된 역할이 사용자 역할 집합에 포함되어야 함
        assertThat(userRoles).contains(highestRole);

        // Then: 반환된 역할의 레벨이 가장 낮아야 함 (권한이 가장 높음)
        for (Role role : userRoles) {
            assertThat(highestRole.getLevel()).isLessThanOrEqualTo(role.getLevel());
        }
    }

    /**
     * Property 14.12: 다중 역할 권한 검증
     * 
     * 다중 역할을 가진 사용자에 대해:
     * 역할 중 하나라도 권한이 있으면 접근 가능해야 함
     */
    @Property(tries = 100)
    @DisplayName("다중 역할 중 하나라도 권한이 있으면 접근 가능해야 한다")
    void multipleRolesShouldGrantAccessIfAnyHasPermission(
            @ForAll("nonEmptyRoleSets") Set<Role> userRoles,
            @ForAll("roles") Role targetRole) {

        // When: 역할 접근 권한 확인
        boolean canAccess = rbac.canAccessRole(userRoles, targetRole);

        // Then: 역할 중 하나라도 접근 가능하면 true
        boolean expected = userRoles.stream().anyMatch(role -> role.canAccess(targetRole));
        assertThat(canAccess).isEqualTo(expected);
    }

    /**
     * Property 14.13: 빈 역할 집합 처리
     * 
     * 빈 역할 집합 또는 null에 대해:
     * 모든 권한 확인이 false를 반환해야 함
     */
    @Property(tries = 50)
    @DisplayName("빈 역할 집합은 모든 권한 확인에서 false를 반환해야 한다")
    void emptyRoleSetShouldDenyAllAccess(
            @ForAll("roles") Role targetRole) {

        // Given: 빈 역할 집합
        Set<Role> emptyRoles = new HashSet<>();

        // When & Then: 모든 권한 확인이 false
        assertThat(rbac.canAccessRole(emptyRoles, targetRole)).isFalse();
        assertThat(rbac.canReadResource(emptyRoles, targetRole)).isFalse();
        assertThat(rbac.canWriteResource(emptyRoles, targetRole)).isFalse();
        assertThat(rbac.canDeleteResource(emptyRoles, targetRole)).isFalse();
        assertThat(rbac.isSuperAdmin(emptyRoles)).isFalse();
        assertThat(rbac.isHqOrAbove(emptyRoles)).isFalse();
        assertThat(rbac.isSiteManagerOrAbove(emptyRoles)).isFalse();
        assertThat(rbac.isAdmin(emptyRoles)).isFalse();
        assertThat(rbac.isPersonalUser(emptyRoles)).isFalse();
        assertThat(rbac.getHighestRole(emptyRoles)).isNull();

        // When & Then: null 역할 집합도 false
        assertThat(rbac.canAccessRole(null, targetRole)).isFalse();
        assertThat(rbac.canReadResource(null, targetRole)).isFalse();
        assertThat(rbac.canWriteResource(null, targetRole)).isFalse();
        assertThat(rbac.canDeleteResource(null, targetRole)).isFalse();
        assertThat(rbac.isSuperAdmin(null)).isFalse();
        assertThat(rbac.isHqOrAbove(null)).isFalse();
        assertThat(rbac.isSiteManagerOrAbove(null)).isFalse();
        assertThat(rbac.isAdmin(null)).isFalse();
        assertThat(rbac.isPersonalUser(null)).isFalse();
        assertThat(rbac.getHighestRole(null)).isNull();
    }

    /**
     * Property 14.14: 현장 접근 권한 검증
     * 
     * 모든 역할 집합에 대해:
     * 슈퍼관리자와 본사관리자는 모든 현장에 접근 가능해야 함
     */
    @Property(tries = 100)
    @DisplayName("슈퍼관리자와 본사관리자는 모든 현장에 접근 가능해야 한다")
    void superAndHqAdminShouldAccessAllSites(
            @ForAll("roleSets") Set<Role> userRoles,
            @ForAll("siteIds") Long siteId) {

        // When: 현장 접근 권한 확인
        boolean canAccessSite = rbac.canAccessSite(userRoles, siteId);

        // Then: 슈퍼관리자 또는 본사관리자가 포함된 경우 접근 가능
        if (userRoles.contains(Role.ROLE_SUPER) || userRoles.contains(Role.ROLE_HQ)) {
            assertThat(canAccessSite).isTrue();
        }
    }

    /**
     * Property 14.15: 팀 접근 권한 검증
     * 
     * 모든 역할 집합에 대해:
     * 현장관리자 이상은 모든 팀에 접근 가능해야 함
     */
    @Property(tries = 100)
    @DisplayName("현장관리자 이상은 모든 팀에 접근 가능해야 한다")
    void siteManagerOrAboveShouldAccessAllTeams(
            @ForAll("roleSets") Set<Role> userRoles,
            @ForAll("teamIds") Long teamId) {

        // When: 팀 접근 권한 확인
        boolean canAccessTeam = rbac.canAccessTeam(userRoles, teamId);

        // Then: 현장관리자 이상이 포함된 경우 접근 가능
        if (rbac.isSiteManagerOrAbove(userRoles)) {
            assertThat(canAccessTeam).isTrue();
        }
    }

    // ==================== Arbitrary Providers ====================

    /**
     * 역할 생성기
     */
    @Provide
    Arbitrary<Role> roles() {
        return Arbitraries.of(Role.values());
    }

    /**
     * 로그인 유형 생성기
     */
    @Provide
    Arbitrary<LoginType> loginTypes() {
        return Arbitraries.of(LoginType.values());
    }

    /**
     * 역할 집합 생성기 (빈 집합 포함)
     */
    @Provide
    Arbitrary<Set<Role>> roleSets() {
        return Arbitraries.of(Role.values())
                .set()
                .ofMinSize(0)
                .ofMaxSize(5);
    }

    /**
     * 비어있지 않은 역할 집합 생성기
     */
    @Provide
    Arbitrary<Set<Role>> nonEmptyRoleSets() {
        return Arbitraries.of(Role.values())
                .set()
                .ofMinSize(1)
                .ofMaxSize(5);
    }

    /**
     * 현장 ID 생성기
     */
    @Provide
    Arbitrary<Long> siteIds() {
        return Arbitraries.longs()
                .between(1L, 1000L);
    }

    /**
     * 팀 ID 생성기
     */
    @Provide
    Arbitrary<Long> teamIds() {
        return Arbitraries.longs()
                .between(1L, 1000L);
    }
}
