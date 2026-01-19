package com.smartcon.domain.project;

import com.smartcon.domain.project.dto.ProjectListRequest;
import com.smartcon.domain.project.dto.ProjectResponse;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.entity.ProjectManager;
import com.smartcon.domain.project.repository.ProjectManagerRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.project.service.ProjectManagementServiceImpl;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 프로젝트 목록 정렬 및 검색 속성 테스트
 * Feature: smartcon-lite-role-based-system, Property 5 & 6
 * 
 * Property 5: Site List Ordering and Filtering
 * For any site list request, the system should order sites by recent login, 
 * recent assignment, and remaining construction period, and support filtering by site status
 * Validates: Requirements 1.12, 1.13
 * 
 * Property 6: Search Functionality Accuracy
 * For any site name search query, the system should return only sites 
 * whose names contain the search term
 * Validates: Requirements 1.14
 */
class ProjectListOrderingAndSearchPropertyTest {

    private TestContext createTestContext() {
        TenantContext.setCurrentTenantId(1L);
        
        ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
        ProjectManagerRepository projectManagerRepository = Mockito.mock(ProjectManagerRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        
        ProjectManagementServiceImpl projectManagementService = new ProjectManagementServiceImpl(
            projectRepository,
            projectManagerRepository,
            userRepository
        );
        
        return new TestContext(projectRepository, projectManagerRepository, userRepository, projectManagementService);
    }
    
    private void cleanupTestContext() {
        TenantContext.clear();
    }
    
    private static class TestContext {
        final ProjectRepository projectRepository;
        final ProjectManagerRepository projectManagerRepository;
        final UserRepository userRepository;
        final ProjectManagementServiceImpl projectManagementService;
        
        TestContext(ProjectRepository projectRepository, ProjectManagerRepository projectManagerRepository,
                   UserRepository userRepository, ProjectManagementServiceImpl projectManagementService) {
            this.projectRepository = projectRepository;
            this.projectManagerRepository = projectManagerRepository;
            this.userRepository = userRepository;
            this.projectManagementService = projectManagementService;
        }
    }

    /**
     * Property 5.1: 최근 로그인 순 정렬
     * 
     * 모든 프로젝트 목록에 대해:
     * recentLogin 정렬 기준으로 조회하면, 프로젝트 관리자의 마지막 접근 시간이 
     * 최신인 프로젝트가 먼저 나와야 함
     */
    @Property(tries = 100)
    @Label("최근 로그인 순 정렬이 올바르게 동작한다")
    void recentLoginSortingWorks(
            @ForAll("projectListWithAccessTimes") List<Project> projects) {

        TestContext ctx = createTestContext();
        try {
            // Given: 프로젝트 목록과 본사관리자 사용자
            Long tenantId = 1L;
            Long userId = 1L;
            Role userRole = Role.ROLE_HQ;

            // Mock 설정
            when(ctx.projectRepository.findAll()).thenReturn(projects);

            // When: 최근 로그인 순으로 정렬 요청
            ProjectListRequest request = ProjectListRequest.builder()
                    .sortBy("recentLogin")
                    .sortOrder("desc")
                    .page(0)
                    .size(100)
                    .build();

            List<ProjectResponse> response = ctx.projectManagementService.getProjects(request, tenantId, userId, userRole);

            // Then: 정렬된 결과가 내림차순으로 올바르게 정렬되어 있는지 검증
            if (response.size() > 1) {
                for (int i = 0; i < response.size() - 1; i++) {
                    LocalDateTime current = getLatestAccessTime(projects, response.get(i).getId());
                    LocalDateTime next = getLatestAccessTime(projects, response.get(i + 1).getId());
                    
                    // 내림차순: 현재 항목의 시간이 다음 항목의 시간보다 크거나 같아야 함
                    // (최신 시간이 앞에 와야 함)
                    assertThat(current)
                        .as("프로젝트 %d의 접근 시간(%s)이 프로젝트 %d의 접근 시간(%s)보다 최신이거나 같아야 함",
                            response.get(i).getId(), current,
                            response.get(i + 1).getId(), next)
                        .isAfterOrEqualTo(next);
                }
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 5.2: 최근 배정 순 정렬
     * 
     * 모든 프로젝트 목록에 대해:
     * recentAssignment 정렬 기준으로 조회하면, 프로젝트 관리자의 배정 시간이 
     * 최신인 프로젝트가 먼저 나와야 함
     */
    @Property(tries = 100)
    @Label("최근 배정 순 정렬이 올바르게 동작한다")
    void recentAssignmentSortingWorks(
            @ForAll("projectListWithAssignmentTimes") List<Project> projects) {

        TestContext ctx = createTestContext();
        try {
            // Given: 프로젝트 목록과 본사관리자 사용자
            Long tenantId = 1L;
            Long userId = 1L;
            Role userRole = Role.ROLE_HQ;

            // Mock 설정
            when(ctx.projectRepository.findAll()).thenReturn(projects);

            // When: 최근 배정 순으로 정렬 요청
            ProjectListRequest request = ProjectListRequest.builder()
                    .sortBy("recentAssignment")
                    .sortOrder("desc")
                    .page(0)
                    .size(100)
                    .build();

            List<ProjectResponse> response = ctx.projectManagementService.getProjects(request, tenantId, userId, userRole);

            // Then: 정렬된 결과가 내림차순으로 올바르게 정렬되어 있는지 검증
            if (response.size() > 1) {
                for (int i = 0; i < response.size() - 1; i++) {
                    LocalDateTime current = getLatestAssignmentTime(projects, response.get(i).getId());
                    LocalDateTime next = getLatestAssignmentTime(projects, response.get(i + 1).getId());
                    
                    // 내림차순: 현재 항목의 시간이 다음 항목의 시간보다 크거나 같아야 함
                    // (최신 배정 시간이 앞에 와야 함)
                    assertThat(current)
                        .as("프로젝트 %d의 배정 시간(%s)이 프로젝트 %d의 배정 시간(%s)보다 최신이거나 같아야 함",
                            response.get(i).getId(), current,
                            response.get(i + 1).getId(), next)
                        .isAfterOrEqualTo(next);
                }
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 5.3: 공사 기간 남은 순 정렬
     * 
     * 모든 프로젝트 목록에 대해:
     * constructionPeriod 정렬 기준으로 조회하면, 공사 종료일이 빠른 프로젝트가 먼저 나와야 함
     */
    @Property(tries = 100)
    @Label("공사 기간 남은 순 정렬이 올바르게 동작한다")
    void constructionPeriodSortingWorks(
            @ForAll("projectListWithConstructionPeriods") List<Project> projects) {

        TestContext ctx = createTestContext();
        try {
            // Given: 프로젝트 목록과 본사관리자 사용자
            Long tenantId = 1L;
            Long userId = 1L;
            Role userRole = Role.ROLE_HQ;

            // Mock 설정
            when(ctx.projectRepository.findAll()).thenReturn(projects);

            // When: 공사 기간 남은 순으로 정렬 요청 (오름차순 - 종료일이 빠른 순)
            ProjectListRequest request = ProjectListRequest.builder()
                    .sortBy("constructionPeriod")
                    .sortOrder("asc")
                    .page(0)
                    .size(100)
                    .build();

            List<ProjectResponse> response = ctx.projectManagementService.getProjects(request, tenantId, userId, userRole);

            // Then: 공사 종료일이 빠른 프로젝트가 먼저 나와야 함
            if (response.size() > 1) {
                for (int i = 0; i < response.size() - 1; i++) {
                    LocalDate current = response.get(i).getConstructionPeriodEnd();
                    LocalDate next = response.get(i + 1).getConstructionPeriodEnd();
                    
                    // 종료일이 더 빠르거나 같아야 함 (오름차순)
                    assertThat(current).isBeforeOrEqualTo(next);
                }
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 5.4: 상태별 필터링
     * 
     * 모든 프로젝트 목록에 대해:
     * 특정 상태로 필터링하면, 해당 상태의 프로젝트만 반환되어야 함
     */
    @Property(tries = 100)
    @Label("상태별 필터링이 올바르게 동작한다")
    void statusFilteringWorks(
            @ForAll("projectListWithMixedStatuses") List<Project> projects,
            @ForAll("projectStatus") Project.ProjectStatus filterStatus) {

        TestContext ctx = createTestContext();
        try {
            // Given: 다양한 상태의 프로젝트 목록과 본사관리자 사용자
            Long tenantId = 1L;
            Long userId = 1L;
            Role userRole = Role.ROLE_HQ;

            // Mock 설정
            List<Project> filteredProjects = projects.stream()
                    .filter(p -> p.getStatus().equals(filterStatus))
                    .collect(Collectors.toList());
            when(ctx.projectRepository.findByTenantIdAndStatus(tenantId, filterStatus))
                    .thenReturn(filteredProjects);

            // When: 특정 상태로 필터링 요청
            ProjectListRequest request = ProjectListRequest.builder()
                    .status(filterStatus)
                    .page(0)
                    .size(100)
                    .build();

            List<ProjectResponse> response = ctx.projectManagementService.getProjects(request, tenantId, userId, userRole);

            // Then: 모든 반환된 프로젝트가 필터링한 상태여야 함
            assertThat(response).allMatch(p -> p.getStatus().equals(filterStatus));
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 6: 검색 기능 정확성
     * 
     * 모든 프로젝트 목록과 검색어에 대해:
     * 검색어를 포함하는 프로젝트만 반환되어야 함
     */
    @Property(tries = 100)
    @Label("검색 기능이 올바르게 동작한다")
    void searchFunctionalityWorks(
            @ForAll("projectListWithVariousNames") List<Project> projects,
            @ForAll("searchTerm") String searchTerm) {

        TestContext ctx = createTestContext();
        try {
            // Given: 다양한 이름의 프로젝트 목록과 본사관리자 사용자
            Long tenantId = 1L;
            Long userId = 1L;
            Role userRole = Role.ROLE_HQ;

            // Mock 설정
            when(ctx.projectRepository.findAll()).thenReturn(projects);

            // When: 검색어로 검색 요청
            ProjectListRequest request = ProjectListRequest.builder()
                    .search(searchTerm)
                    .page(0)
                    .size(100)
                    .build();

            List<ProjectResponse> response = ctx.projectManagementService.getProjects(request, tenantId, userId, userRole);

            // Then: 모든 반환된 프로젝트의 이름이 검색어를 포함해야 함
            String searchLower = searchTerm.toLowerCase();
            assertThat(response).allMatch(p -> p.getName().toLowerCase().contains(searchLower));
            
            // 그리고: 검색어를 포함하는 모든 프로젝트가 반환되어야 함
            long expectedCount = projects.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower))
                    .count();
            assertThat(response).hasSize((int) expectedCount);
        } finally {
            cleanupTestContext();
        }
    }

    // ========== Arbitraries (데이터 생성기) ==========

    @Provide
    Arbitrary<List<Project>> projectListWithAccessTimes() {
        return Arbitraries.integers().between(2, 10).flatMap(size -> {
            // 정렬 검증을 위해 순차적으로 증가하는 날짜 생성
            List<LocalDateTime> sortedDates = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now().minusDays(size * 2L);
            for (int i = 0; i < size; i++) {
                sortedDates.add(baseTime.plusDays(i * 2L));
            }
            
            // 날짜를 무작위로 섞어서 프로젝트 생성
            List<Project> projects = new ArrayList<>();
            List<LocalDateTime> shuffledDates = new ArrayList<>(sortedDates);
            java.util.Collections.shuffle(shuffledDates);
            
            for (int i = 0; i < size; i++) {
                projects.add(createProjectWithAccessTime((long) (i + 1), "Project" + i, shuffledDates.get(i)));
            }
            
            return Arbitraries.just(projects);
        });
    }

    @Provide
    Arbitrary<List<Project>> projectListWithAssignmentTimes() {
        return Arbitraries.integers().between(2, 10).flatMap(size -> {
            // 정렬 검증을 위해 순차적으로 증가하는 날짜 생성
            List<LocalDateTime> sortedDates = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now().minusDays(size * 2L);
            for (int i = 0; i < size; i++) {
                sortedDates.add(baseTime.plusDays(i * 2L));
            }
            
            // 날짜를 무작위로 섞어서 프로젝트 생성
            List<Project> projects = new ArrayList<>();
            List<LocalDateTime> shuffledDates = new ArrayList<>(sortedDates);
            java.util.Collections.shuffle(shuffledDates);
            
            for (int i = 0; i < size; i++) {
                projects.add(createProjectWithAssignmentTime((long) (i + 1), "Project" + i, shuffledDates.get(i)));
            }
            
            return Arbitraries.just(projects);
        });
    }

    @Provide
    Arbitrary<List<Project>> projectListWithConstructionPeriods() {
        return Arbitraries.integers().between(2, 10).flatMap(size -> {
            Arbitrary<Project> projectArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(10),
                Arbitraries.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(30),
                              LocalDate.now().plusDays(60), LocalDate.now().plusDays(90),
                              LocalDate.now().plusDays(180))
            ).as((id, name, endDate) -> createProjectWithConstructionPeriod(id, name, endDate));
            
            return projectArbitrary.list().ofSize(size);
        });
    }

    @Provide
    Arbitrary<List<Project>> projectListWithMixedStatuses() {
        return Arbitraries.integers().between(3, 10).flatMap(size -> {
            Arbitrary<Project> projectArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofLength(10),
                Arbitraries.of(Project.ProjectStatus.values())
            ).as((id, name, status) -> createProjectWithStatus(id, name, status));
            
            return projectArbitrary.list().ofSize(size);
        });
    }

    @Provide
    Arbitrary<Project.ProjectStatus> projectStatus() {
        return Arbitraries.of(Project.ProjectStatus.values());
    }

    @Provide
    Arbitrary<List<Project>> projectListWithVariousNames() {
        return Arbitraries.integers().between(3, 10).flatMap(size -> {
            Arbitrary<Project> projectArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.of("서울현장", "부산현장", "대구현장", "인천현장", "광주현장",
                              "대전현장", "울산현장", "세종현장", "경기현장", "강원현장")
            ).as((id, name) -> createProjectWithName(id, name));
            
            return projectArbitrary.list().ofSize(size);
        });
    }

    @Provide
    Arbitrary<String> searchTerm() {
        return Arbitraries.of("서울", "부산", "대구", "인천", "현장");
    }

    // ========== Helper Methods ==========

    private Project createProjectWithAccessTime(Long id, String name, LocalDateTime accessTime) {
        Project project = Project.builder()
                .name(name)
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .status(Project.ProjectStatus.ACTIVE)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(id);
        project.setTenantId(1L);
        project.setCreatedAt(LocalDateTime.now().minusDays(30));

        // 프로젝트 관리자 추가 (마지막 접근 시간 설정)
        ProjectManager manager = ProjectManager.builder()
                .project(project)
                .user(createMockUser(id))
                .role(ProjectManager.ManagerRole.SITE_MANAGER)
                .assignedAt(LocalDateTime.now().minusDays(20))
                .isActive(true)
                .lastAccessAt(accessTime)
                .build();
        manager.setId(id);
        project.getProjectManagers().add(manager);

        return project;
    }

    private Project createProjectWithAssignmentTime(Long id, String name, LocalDateTime assignmentTime) {
        Project project = Project.builder()
                .name(name)
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .status(Project.ProjectStatus.ACTIVE)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(id);
        project.setTenantId(1L);
        project.setCreatedAt(LocalDateTime.now().minusDays(30));

        // 프로젝트 관리자 추가 (배정 시간 설정)
        ProjectManager manager = ProjectManager.builder()
                .project(project)
                .user(createMockUser(id))
                .role(ProjectManager.ManagerRole.SITE_MANAGER)
                .assignedAt(assignmentTime)
                .isActive(true)
                .build();
        manager.setId(id);
        project.getProjectManagers().add(manager);

        return project;
    }

    private Project createProjectWithConstructionPeriod(Long id, String name, LocalDate endDate) {
        Project project = Project.builder()
                .name(name)
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(endDate)
                .status(Project.ProjectStatus.ACTIVE)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(id);
        project.setTenantId(1L);
        project.setCreatedAt(LocalDateTime.now().minusDays(30));

        return project;
    }

    private Project createProjectWithStatus(Long id, String name, Project.ProjectStatus status) {
        Project project = Project.builder()
                .name(name)
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .status(status)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(id);
        project.setTenantId(1L);
        project.setCreatedAt(LocalDateTime.now().minusDays(30));

        return project;
    }

    private Project createProjectWithName(Long id, String name) {
        Project project = Project.builder()
                .name(name)
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .status(Project.ProjectStatus.ACTIVE)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(id);
        project.setTenantId(1L);
        project.setCreatedAt(LocalDateTime.now().minusDays(30));

        return project;
    }

    private User createMockUser(Long id) {
        User user = User.builder()
                .name("User" + id)
                .email("user" + id + "@test.com")
                .roles(Set.of(Role.ROLE_SITE))
                .build();
        user.setId(id);
        return user;
    }

    private LocalDateTime getLatestAccessTime(List<Project> projects, Long projectId) {
        return projects.stream()
                .filter(p -> p.getId().equals(projectId))
                .findFirst()
                .map(p -> p.getProjectManagers().stream()
                        .filter(ProjectManager::isActive)
                        .map(ProjectManager::getLastAccessAt)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElse(LocalDateTime.MIN))
                .orElse(LocalDateTime.MIN);
    }

    private LocalDateTime getLatestAssignmentTime(List<Project> projects, Long projectId) {
        return projects.stream()
                .filter(p -> p.getId().equals(projectId))
                .findFirst()
                .map(p -> p.getProjectManagers().stream()
                        .filter(ProjectManager::isActive)
                        .map(ProjectManager::getAssignedAt)
                        .max(Comparator.naturalOrder())
                        .orElse(LocalDateTime.MIN))
                .orElse(LocalDateTime.MIN);
    }
}
