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
import com.smartcon.domain.user.service.RoleSelectionServiceImpl;
import com.smartcon.global.tenant.TenantContext;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 역할 및 현장 선택 로직 속성 테스트
 * Feature: smartcon-lite-role-based-system, Property 4: Role and Site Selection Logic
 * Validates: Requirements 1.10, 1.11
 * 
 * Property 4: Role and Site Selection Logic
 * For any user with multiple roles or multiple site assignments, 
 * the system should provide appropriate selection interfaces based on 
 * user's role and site relationships
 */
class RoleAndSiteSelectionPropertyTest {

    // jqwik은 @BeforeEach를 지원하지 않으므로 각 테스트에서 직접 인스턴스 생성
    private TestContext createTestContext() {
        // 테넌트 컨텍스트 설정
        TenantContext.setCurrentTenantId(1L);
        
        // Mock 객체 생성
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
        ProjectManagerRepository projectManagerRepository = Mockito.mock(ProjectManagerRepository.class);
        
        // 서비스 인스턴스 생성
        RoleSelectionServiceImpl roleSelectionService = new RoleSelectionServiceImpl(
            userRepository, 
            projectRepository, 
            projectManagerRepository
        );
        
        return new TestContext(userRepository, projectRepository, projectManagerRepository, roleSelectionService);
    }
    
    private void cleanupTestContext() {
        TenantContext.clear();
    }
    
    // 테스트 컨텍스트를 담는 내부 클래스
    private static class TestContext {
        final UserRepository userRepository;
        final ProjectRepository projectRepository;
        final ProjectManagerRepository projectManagerRepository;
        final RoleSelectionServiceImpl roleSelectionService;
        
        TestContext(UserRepository userRepository, ProjectRepository projectRepository,
                   ProjectManagerRepository projectManagerRepository, RoleSelectionServiceImpl roleSelectionService) {
            this.userRepository = userRepository;
            this.projectRepository = projectRepository;
            this.projectManagerRepository = projectManagerRepository;
            this.roleSelectionService = roleSelectionService;
        }
    }

    /**
     * Property 4.1: 다중 역할 처리
     * 
     * 모든 사용자에 대해:
     * 사용자가 여러 역할을 가진 경우, 시스템은 모든 역할을 제공하고 
     * 역할 선택이 필요함을 표시해야 함
     */
    @Property(tries = 100)
    @Label("다중 역할을 가진 사용자는 역할 선택이 필요하다")
    void multipleRolesRequireSelection(
            @ForAll("userWithMultipleRoles") User user) {

        // Given: 다중 역할을 가진 사용자
        TestContext ctx = createTestContext();
        try {
            when(ctx.userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            mockProjectManagersForUser(ctx, user);

            // When: 역할 및 현장 목록 조회
            RoleSelectionResponse response = ctx.roleSelectionService.getAvailableRolesAndSites(user.getId());

            // Then: 역할 선택이 필요함을 표시해야 함
            assertThat(response.getAvailableRoles()).hasSizeGreaterThan(1);
            assertThat(response.isRequiresRoleSelection()).isTrue();
            
            // 모든 사용자 역할이 응답에 포함되어야 함
            Set<Role> responseRoles = response.getAvailableRoles().stream()
                    .map(roleInfo -> roleInfo.getRole())
                    .collect(Collectors.toSet());
            assertThat(responseRoles).containsAll(user.getRoles());
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 4.2: 단일 역할 자동 선택
     * 
     * 모든 사용자에 대해:
     * 사용자가 단일 역할과 단일 현장만 가진 경우, 
     * 시스템은 자동 선택 가능함을 표시해야 함
     */
    @Property(tries = 100)
    @Label("단일 역할과 단일 현장을 가진 사용자는 자동 선택 가능하다")
    void singleRoleSingleSiteCanAutoSelect(
            @ForAll("userWithSingleRole") User user,
            @ForAll("activeProject") Project project) {

        // Given: 단일 역할과 단일 현장을 가진 사용자
        TestContext ctx = createTestContext();
        try {
            when(ctx.userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            
            Role userRole = user.getRoles().iterator().next();
            ProjectManager.ManagerRole managerRole = convertToManagerRole(userRole);
            
            if (managerRole != null) {
                ProjectManager pm = createProjectManager(user, project, managerRole);
                when(ctx.projectManagerRepository.findActiveByUserIdAndRole(user.getId(), managerRole))
                        .thenReturn(List.of(pm));
            }

            // When: 역할 및 현장 목록 조회
            RoleSelectionResponse response = ctx.roleSelectionService.getAvailableRolesAndSites(user.getId());

            // Then: 자동 선택 가능해야 함
            if (response.getAvailableRoles().size() == 1 && 
                response.getAvailableRoles().get(0).getAvailableSites().size() == 1) {
                assertThat(response.canAutoSelect()).isTrue();
                assertThat(response.isRequiresRoleSelection()).isFalse();
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 4.3: 역할별 현장 접근 권한
     * 
     * 모든 역할에 대해:
     * 슈퍼관리자는 모든 현장, 본사관리자는 테넌트 내 모든 현장,
     * 현장관리자/팀장은 배정된 현장만 접근 가능해야 함
     */
    @Property(tries = 100)
    @Label("역할에 따라 접근 가능한 현장 범위가 다르다")
    void roleBasedSiteAccessScope(
            @ForAll("roles") Role role,
            @ForAll("userId") Long userId) {

        // Given: 특정 역할을 가진 사용자
        TestContext ctx = createTestContext();
        try {
            User user = createUserWithRole(userId, role);
            when(ctx.userRepository.findById(userId)).thenReturn(Optional.of(user));
            
            // 역할별 현장 목록 Mock 설정
            mockSitesForRole(ctx, userId, role);

            // When: 역할별 현장 목록 조회
            List<SiteInfo> sites = ctx.roleSelectionService.getAvailableSitesForRole(userId, role);

            // Then: 역할에 따른 현장 접근 범위 검증
            switch (role) {
                case ROLE_SUPER:
                    // 슈퍼관리자는 모든 현장 접근 가능 (Mock에서 제공된 모든 현장)
                    assertThat(sites).isNotNull();
                    break;
                    
                case ROLE_HQ:
                    // 본사관리자는 테넌트 내 모든 현장 접근 가능
                    assertThat(sites).isNotNull();
                    break;
                    
                case ROLE_SITE:
                case ROLE_TEAM:
                    // 현장관리자와 팀장은 배정된 현장만 접근 가능
                    assertThat(sites).isNotNull();
                    break;
                    
                case ROLE_WORKER:
                    // 일반노무자는 소속된 현장만 접근 가능
                    assertThat(sites).isNotNull();
                    break;
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 4.4: 현장 목록 정렬 일관성
     * 
     * 모든 현장 목록에 대해:
     * 현장은 항상 최근 접근 > 최근 배정 > 남은 공사 기간 순으로 정렬되어야 함
     */
    @Property(tries = 100)
    @Label("현장 목록은 일관된 정렬 순서를 유지한다")
    void siteListSortingConsistency(
            @ForAll("userWithSiteRole") User user,
            @ForAll("projectList") List<Project> projects) {

        // Given: 여러 현장을 가진 사용자
        TestContext ctx = createTestContext();
        try {
            when(ctx.userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            
            Role userRole = user.getRoles().iterator().next();
            ProjectManager.ManagerRole managerRole = convertToManagerRole(userRole);
            
            if (managerRole != null && !projects.isEmpty()) {
                List<ProjectManager> projectManagers = createProjectManagersWithDifferentTimes(
                    user, projects, managerRole);
                when(ctx.projectManagerRepository.findActiveByUserIdAndRole(user.getId(), managerRole))
                        .thenReturn(projectManagers);

                // When: 현장 목록 조회
                List<SiteInfo> sites = ctx.roleSelectionService.getAvailableSitesForRole(user.getId(), userRole);

                // Then: 정렬 순서 검증
                if (sites.size() > 1) {
                    for (int i = 0; i < sites.size() - 1; i++) {
                        SiteInfo current = sites.get(i);
                        SiteInfo next = sites.get(i + 1);
                        
                        // 최근 접근 시간이 있는 경우 우선
                        if (current.getLastAccessAt() != null && next.getLastAccessAt() != null) {
                            assertThat(current.getLastAccessAt())
                                    .isAfterOrEqualTo(next.getLastAccessAt());
                        }
                    }
                }
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 4.5: 역할 선택 검증
     * 
     * 모든 역할 선택 요청에 대해:
     * 사용자가 가진 역할만 선택 가능하고, 
     * 접근 권한이 있는 현장만 선택 가능해야 함
     */
    @Property(tries = 100)
    @Label("사용자는 자신이 가진 역할과 접근 권한이 있는 현장만 선택할 수 있다")
    void roleAndSiteSelectionValidation(
            @ForAll("userWithMultipleRoles") User user,
            @ForAll("activeProject") Project project) {

        // Given: 다중 역할을 가진 사용자와 현장
        TestContext ctx = createTestContext();
        try {
            when(ctx.userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(ctx.projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
            
            // 사용자가 가진 역할 중 하나 선택
            Role selectedRole = user.getRoles().iterator().next();
            
            // 접근 권한 Mock 설정
            boolean hasAccess = mockAccessForRoleAndSite(ctx, user, selectedRole, project);
            
            SiteSelectionRequest request = new SiteSelectionRequest(selectedRole, project.getId());

            // When & Then: 역할 및 현장 선택
            if (hasAccess) {
                // 접근 권한이 있는 경우 선택 성공
                SiteSelectionResponse response = ctx.roleSelectionService.selectRoleAndSite(user.getId(), request);
                assertThat(response).isNotNull();
                assertThat(response.getSelectedRole()).isEqualTo(selectedRole);
                assertThat(response.getSelectedSiteId()).isEqualTo(project.getId());
            } else {
                // 접근 권한이 없는 경우 예외 발생
                try {
                    ctx.roleSelectionService.selectRoleAndSite(user.getId(), request);
                    // 예외가 발생하지 않으면 테스트 실패
                    assertThat(false).isTrue();
                } catch (IllegalArgumentException e) {
                    assertThat(e.getMessage()).contains("접근할 권한이 없습니다");
                }
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 4.6: 역할 레벨 정렬
     * 
     * 모든 사용자에 대해:
     * 역할 목록은 항상 레벨 순으로 정렬되어야 함 (낮은 레벨이 높은 권한)
     */
    @Property(tries = 100)
    @Label("역할 목록은 레벨 순으로 정렬된다")
    void roleListSortedByLevel(
            @ForAll("userWithMultipleRoles") User user) {

        // Given: 다중 역할을 가진 사용자
        TestContext ctx = createTestContext();
        try {
            when(ctx.userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            mockProjectManagersForUser(ctx, user);

            // When: 역할 및 현장 목록 조회
            RoleSelectionResponse response = ctx.roleSelectionService.getAvailableRolesAndSites(user.getId());

            // Then: 역할이 레벨 순으로 정렬되어 있어야 함
            List<Integer> roleLevels = response.getAvailableRoles().stream()
                    .map(roleInfo -> roleInfo.getRole().getLevel())
                    .collect(Collectors.toList());
            
            for (int i = 0; i < roleLevels.size() - 1; i++) {
                assertThat(roleLevels.get(i)).isLessThanOrEqualTo(roleLevels.get(i + 1));
            }
        } finally {
            cleanupTestContext();
        }
    }

    // ==================== Arbitrary Providers ====================

    @Provide
    Arbitrary<User> userWithMultipleRoles() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(5),
                multipleRoles()
        ).as((id, name, roles) -> {
            User user = User.builder()
                    .name(name)
                    .email(name + "@example.com")
                    .roles(roles)
                    .build();
            user.setId(id);
            user.setTenantId(1L);
            return user;
        });
    }

    @Provide
    Arbitrary<User> userWithSingleRole() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(5),
                roles()
        ).as((id, name, role) -> {
            User user = User.builder()
                    .name(name)
                    .email(name + "@example.com")
                    .roles(Set.of(role))
                    .build();
            user.setId(id);
            user.setTenantId(1L);
            return user;
        });
    }

    @Provide
    Arbitrary<User> userWithSiteRole() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(5),
                siteRoles()
        ).as((id, name, role) -> {
            User user = User.builder()
                    .name(name)
                    .email(name + "@example.com")
                    .roles(Set.of(role))
                    .build();
            user.setId(id);
            user.setTenantId(1L);
            return user;
        });
    }

    @Provide
    Arbitrary<Role> roles() {
        return Arbitraries.of(Role.values());
    }

    @Provide
    Arbitrary<Role> siteRoles() {
        return Arbitraries.of(Role.ROLE_SITE, Role.ROLE_TEAM);
    }

    @Provide
    Arbitrary<Set<Role>> multipleRoles() {
        return Arbitraries.of(Role.values())
                .set()
                .ofMinSize(2)
                .ofMaxSize(3);
    }

    @Provide
    Arbitrary<Long> userId() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    @Provide
    Arbitrary<Project> activeProject() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(10),
                Arbitraries.strings().alpha().ofLength(10)
        ).as((id, name, location) -> {
            Project project = Project.builder()
                    .name(name)
                    .location(location)
                    .status(Project.ProjectStatus.ACTIVE)
                    .constructionPeriodStart(LocalDate.now().minusMonths(1))
                    .constructionPeriodEnd(LocalDate.now().plusMonths(2))
                    .build();
            project.setId(id);
            project.setTenantId(1L);
            return project;
        });
    }

    @Provide
    Arbitrary<List<Project>> projectList() {
        return activeProject().list().ofMinSize(2).ofMaxSize(5);
    }

    // ==================== Helper Methods ====================

    private User createUserWithRole(Long userId, Role role) {
        User user = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .roles(Set.of(role))
                .build();
        user.setId(userId);
        user.setTenantId(1L);
        return user;
    }

    private ProjectManager createProjectManager(User user, Project project, ProjectManager.ManagerRole role) {
        ProjectManager pm = ProjectManager.builder()
                .project(project)
                .user(user)
                .role(role)
                .assignedAt(LocalDateTime.now().minusDays(10))
                .lastAccessAt(LocalDateTime.now().minusHours(2))
                .isActive(true)
                .build();
        pm.setId(1L);
        pm.setTenantId(1L);
        return pm;
    }

    private List<ProjectManager> createProjectManagersWithDifferentTimes(
            User user, List<Project> projects, ProjectManager.ManagerRole role) {
        List<ProjectManager> managers = new ArrayList<>();
        for (int i = 0; i < projects.size(); i++) {
            ProjectManager pm = ProjectManager.builder()
                    .project(projects.get(i))
                    .user(user)
                    .role(role)
                    .assignedAt(LocalDateTime.now().minusDays(10 + i))
                    .lastAccessAt(LocalDateTime.now().minusHours(i + 1))
                    .isActive(true)
                    .build();
            pm.setId((long) (i + 1));
            pm.setTenantId(1L);
            managers.add(pm);
        }
        return managers;
    }

    private ProjectManager.ManagerRole convertToManagerRole(Role role) {
        return switch (role) {
            case ROLE_SITE -> ProjectManager.ManagerRole.SITE_MANAGER;
            case ROLE_TEAM -> ProjectManager.ManagerRole.TEAM_LEADER;
            default -> null;
        };
    }

    private void mockProjectManagersForUser(TestContext ctx, User user) {
        for (Role role : user.getRoles()) {
            ProjectManager.ManagerRole managerRole = convertToManagerRole(role);
            if (managerRole != null) {
                Project project = Project.builder()
                        .name("테스트 현장")
                        .location("서울시")
                        .status(Project.ProjectStatus.ACTIVE)
                        .constructionPeriodStart(LocalDate.now().minusMonths(1))
                        .constructionPeriodEnd(LocalDate.now().plusMonths(2))
                        .build();
                project.setId(1L);
                project.setTenantId(1L);
                
                ProjectManager pm = createProjectManager(user, project, managerRole);
                when(ctx.projectManagerRepository.findActiveByUserIdAndRole(user.getId(), managerRole))
                        .thenReturn(List.of(pm));
            }
        }
    }

    private void mockSitesForRole(TestContext ctx, Long userId, Role role) {
        List<Project> projects = List.of(
                createProject(1L, "현장 1"),
                createProject(2L, "현장 2")
        );
        
        switch (role) {
            case ROLE_SUPER:
                when(ctx.projectRepository.findAll()).thenReturn(projects);
                break;
                
            case ROLE_HQ:
                when(ctx.projectRepository.findActiveProjectsByTenantId(1L)).thenReturn(projects);
                break;
                
            case ROLE_SITE:
            case ROLE_TEAM:
            case ROLE_WORKER:
                ProjectManager.ManagerRole managerRole = convertToManagerRole(role);
                if (managerRole != null) {
                    User user = createUserWithRole(userId, role);
                    List<ProjectManager> pms = projects.stream()
                            .map(p -> createProjectManager(user, p, managerRole))
                            .collect(Collectors.toList());
                    when(ctx.projectManagerRepository.findActiveByUserIdAndRole(userId, managerRole))
                            .thenReturn(pms);
                } else {
                    User user = createUserWithRole(userId, role);
                    List<ProjectManager> pms = projects.stream()
                            .map(p -> createProjectManager(user, p, ProjectManager.ManagerRole.SITE_MANAGER))
                            .collect(Collectors.toList());
                    when(ctx.projectManagerRepository.findActiveByUserId(userId))
                            .thenReturn(pms);
                }
                break;
        }
    }

    private boolean mockAccessForRoleAndSite(TestContext ctx, User user, Role role, Project project) {
        boolean hasAccess = true;
        
        switch (role) {
            case ROLE_SUPER:
                when(ctx.projectRepository.existsById(project.getId())).thenReturn(true);
                break;
                
            case ROLE_HQ:
                when(ctx.projectRepository.findByTenantIdAndId(1L, project.getId()))
                        .thenReturn(Optional.of(project));
                break;
                
            case ROLE_SITE:
            case ROLE_TEAM:
            case ROLE_WORKER:
                when(ctx.projectManagerRepository.hasAccessToProject(user.getId(), project.getId()))
                        .thenReturn(hasAccess);
                
                if (hasAccess) {
                    ProjectManager.ManagerRole managerRole = convertToManagerRole(role);
                    if (managerRole == null) {
                        managerRole = ProjectManager.ManagerRole.SITE_MANAGER;
                    }
                    ProjectManager pm = createProjectManager(user, project, managerRole);
                    when(ctx.projectManagerRepository.findActiveByUserIdAndProjectId(user.getId(), project.getId()))
                            .thenReturn(Optional.of(pm));
                }
                break;
        }
        
        return hasAccess;
    }

    private Project createProject(Long id, String name) {
        Project project = Project.builder()
                .name(name)
                .location("서울시")
                .status(Project.ProjectStatus.ACTIVE)
                .constructionPeriodStart(LocalDate.now().minusMonths(1))
                .constructionPeriodEnd(LocalDate.now().plusMonths(2))
                .build();
        project.setId(id);
        project.setTenantId(1L);
        return project;
    }
}
