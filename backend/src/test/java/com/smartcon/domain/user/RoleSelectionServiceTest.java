package com.smartcon.domain.user;

import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.entity.ProjectManager;
import com.smartcon.domain.project.repository.ProjectManagerRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.dto.RoleSelectionResponse;
import com.smartcon.domain.user.dto.SiteInfo;
import com.smartcon.domain.user.dto.SiteSelectionRequest;
import com.smartcon.domain.user.dto.SiteSelectionResponse;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.RoleSelectionService;
import com.smartcon.domain.user.service.RoleSelectionServiceImpl;
import com.smartcon.global.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 역할 및 현장 선택 서비스 테스트
 * Requirements: 1.10, 1.11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("역할 및 현장 선택 서비스 테스트")
class RoleSelectionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectManagerRepository projectManagerRepository;

    @InjectMocks
    private RoleSelectionServiceImpl roleSelectionService;

    private User testUser;
    private Project testProject1;
    private Project testProject2;
    private ProjectManager testProjectManager1;
    private ProjectManager testProjectManager2;

    @BeforeEach
    void setUp() {
        // 테넌트 컨텍스트 설정
        TenantContext.setCurrentTenantId(1L);

        // 테스트 사용자 생성 (다중 역할)
        testUser = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .roles(Set.of(Role.ROLE_SITE, Role.ROLE_TEAM))
                .build();
        testUser.setId(1L);
        testUser.setTenantId(1L);

        // 테스트 프로젝트 생성
        testProject1 = Project.builder()
                .name("테스트 현장 1")
                .location("서울시 강남구")
                .status(Project.ProjectStatus.ACTIVE)
                .constructionPeriodStart(LocalDate.now().minusMonths(1))
                .constructionPeriodEnd(LocalDate.now().plusMonths(2))
                .build();
        testProject1.setId(1L);
        testProject1.setTenantId(1L);

        testProject2 = Project.builder()
                .name("테스트 현장 2")
                .location("서울시 서초구")
                .status(Project.ProjectStatus.ACTIVE)
                .constructionPeriodStart(LocalDate.now().minusMonths(2))
                .constructionPeriodEnd(LocalDate.now().plusMonths(1))
                .build();
        testProject2.setId(2L);
        testProject2.setTenantId(1L);

        // 테스트 프로젝트 관리자 생성
        testProjectManager1 = ProjectManager.builder()
                .project(testProject1)
                .user(testUser)
                .role(ProjectManager.ManagerRole.SITE_MANAGER)
                .assignedAt(LocalDateTime.now().minusDays(10))
                .lastAccessAt(LocalDateTime.now().minusHours(2))
                .isActive(true)
                .build();
        testProjectManager1.setId(1L);
        testProjectManager1.setTenantId(1L);

        testProjectManager2 = ProjectManager.builder()
                .project(testProject2)
                .user(testUser)
                .role(ProjectManager.ManagerRole.TEAM_LEADER)
                .assignedAt(LocalDateTime.now().minusDays(5))
                .lastAccessAt(LocalDateTime.now().minusHours(1))
                .isActive(true)
                .build();
        testProjectManager2.setId(2L);
        testProjectManager2.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("다중 역할을 가진 사용자의 역할 및 현장 목록 조회")
    void testGetAvailableRolesAndSites_MultipleRoles() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(projectManagerRepository.findActiveByUserIdAndRole(1L, ProjectManager.ManagerRole.SITE_MANAGER))
                .thenReturn(List.of(testProjectManager1));
        when(projectManagerRepository.findActiveByUserIdAndRole(1L, ProjectManager.ManagerRole.TEAM_LEADER))
                .thenReturn(List.of(testProjectManager2));

        // When
        RoleSelectionResponse response = roleSelectionService.getAvailableRolesAndSites(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("테스트 사용자");
        assertThat(response.getAvailableRoles()).hasSize(2);
        assertThat(response.isRequiresRoleSelection()).isTrue();
        
        // 역할 레벨 순으로 정렬되어 있는지 확인
        assertThat(response.getAvailableRoles().get(0).getRole()).isEqualTo(Role.ROLE_SITE);
        assertThat(response.getAvailableRoles().get(1).getRole()).isEqualTo(Role.ROLE_TEAM);
    }

    @Test
    @DisplayName("단일 역할, 단일 현장인 경우 자동 선택 가능")
    void testGetAvailableRolesAndSites_SingleRoleSingleSite() {
        // Given
        User singleRoleUser = User.builder()
                .name("단일 역할 사용자")
                .email("single@example.com")
                .roles(Set.of(Role.ROLE_SITE))
                .build();
        singleRoleUser.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(singleRoleUser));
        when(projectManagerRepository.findActiveByUserIdAndRole(2L, ProjectManager.ManagerRole.SITE_MANAGER))
                .thenReturn(List.of(testProjectManager1));

        // When
        RoleSelectionResponse response = roleSelectionService.getAvailableRolesAndSites(2L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.canAutoSelect()).isTrue();
        assertThat(response.isRequiresRoleSelection()).isFalse();
    }

    @Test
    @DisplayName("현장관리자 역할로 접근 가능한 현장 목록 조회")
    void testGetAvailableSitesForRole_SiteManager() {
        // Given
        when(projectManagerRepository.findActiveByUserIdAndRole(1L, ProjectManager.ManagerRole.SITE_MANAGER))
                .thenReturn(List.of(testProjectManager1));

        // When
        List<SiteInfo> sites = roleSelectionService.getAvailableSitesForRole(1L, Role.ROLE_SITE);

        // Then
        assertThat(sites).hasSize(1);
        assertThat(sites.get(0).getSiteId()).isEqualTo(1L);
        assertThat(sites.get(0).getSiteName()).isEqualTo("테스트 현장 1");
    }

    @Test
    @DisplayName("본사관리자는 테넌트 내 모든 현장 접근 가능")
    void testGetAvailableSitesForRole_HQAdmin() {
        // Given
        User hqUser = User.builder()
                .name("본사관리자")
                .email("hq@example.com")
                .roles(Set.of(Role.ROLE_HQ))
                .build();
        hqUser.setId(3L);

        when(projectRepository.findActiveProjectsByTenantId(1L))
                .thenReturn(List.of(testProject1, testProject2));

        // When
        List<SiteInfo> sites = roleSelectionService.getAvailableSitesForRole(3L, Role.ROLE_HQ);

        // Then
        assertThat(sites).hasSize(2);
    }

    @Test
    @DisplayName("역할 및 현장 선택 성공")
    void testSelectRoleAndSite_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject1));
        when(projectManagerRepository.hasAccessToProject(1L, 1L)).thenReturn(true);
        when(projectManagerRepository.findActiveByUserIdAndProjectId(1L, 1L))
                .thenReturn(Optional.of(testProjectManager1));

        SiteSelectionRequest request = new SiteSelectionRequest(Role.ROLE_SITE, 1L);

        // When
        SiteSelectionResponse response = roleSelectionService.selectRoleAndSite(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getSelectedRole()).isEqualTo(Role.ROLE_SITE);
        assertThat(response.getSelectedSiteId()).isEqualTo(1L);
        assertThat(response.getSiteName()).isEqualTo("테스트 현장 1");

        // 현장 접근 시간이 업데이트되었는지 확인
        verify(projectManagerRepository).save(any(ProjectManager.class));
    }

    @Test
    @DisplayName("사용자가 가지고 있지 않은 역할로 선택 시도시 예외 발생")
    void testSelectRoleAndSite_InvalidRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        SiteSelectionRequest request = new SiteSelectionRequest(Role.ROLE_HQ, 1L);

        // When & Then
        assertThatThrownBy(() -> roleSelectionService.selectRoleAndSite(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자가 해당 역할을 가지고 있지 않습니다");
    }

    @Test
    @DisplayName("접근 권한이 없는 현장 선택 시도시 예외 발생")
    void testSelectRoleAndSite_NoAccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(projectManagerRepository.hasAccessToProject(1L, 1L)).thenReturn(false);

        SiteSelectionRequest request = new SiteSelectionRequest(Role.ROLE_SITE, 1L);

        // When & Then
        assertThatThrownBy(() -> roleSelectionService.selectRoleAndSite(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 역할로 현장에 접근할 권한이 없습니다");
    }

    @Test
    @DisplayName("역할 및 현장 접근 권한 검증 - 현장관리자")
    void testValidateRoleAndSiteAccess_SiteManager() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(projectManagerRepository.hasAccessToProject(1L, 1L)).thenReturn(true);

        // When
        boolean hasAccess = roleSelectionService.validateRoleAndSiteAccess(1L, Role.ROLE_SITE, 1L);

        // Then
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("역할 및 현장 접근 권한 검증 - 본사관리자")
    void testValidateRoleAndSiteAccess_HQAdmin() {
        // Given
        User hqUser = User.builder()
                .name("본사관리자")
                .email("hq@example.com")
                .roles(Set.of(Role.ROLE_HQ))
                .build();
        hqUser.setId(3L);
        hqUser.setTenantId(1L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(hqUser));
        when(projectRepository.findByTenantIdAndId(1L, 1L)).thenReturn(Optional.of(testProject1));

        // When
        boolean hasAccess = roleSelectionService.validateRoleAndSiteAccess(3L, Role.ROLE_HQ, 1L);

        // Then
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("역할 및 현장 접근 권한 검증 - 슈퍼관리자")
    void testValidateRoleAndSiteAccess_SuperAdmin() {
        // Given
        User superUser = User.builder()
                .name("슈퍼관리자")
                .email("super@example.com")
                .roles(Set.of(Role.ROLE_SUPER))
                .build();
        superUser.setId(4L);

        when(userRepository.findById(4L)).thenReturn(Optional.of(superUser));
        when(projectRepository.existsById(1L)).thenReturn(true);

        // When
        boolean hasAccess = roleSelectionService.validateRoleAndSiteAccess(4L, Role.ROLE_SUPER, 1L);

        // Then
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("현장 접근 시간 업데이트")
    void testUpdateSiteAccessTime() {
        // Given
        when(projectManagerRepository.findActiveByUserIdAndProjectId(1L, 1L))
                .thenReturn(Optional.of(testProjectManager1));

        // When
        roleSelectionService.updateSiteAccessTime(1L, 1L);

        // Then
        verify(projectManagerRepository).save(any(ProjectManager.class));
        assertThat(testProjectManager1.getLastAccessAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 역할 조회 시 예외 발생")
    void testGetAvailableRolesAndSites_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleSelectionService.getAvailableRolesAndSites(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
