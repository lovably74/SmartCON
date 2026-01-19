package com.smartcon.domain.project.service;

import com.smartcon.domain.project.dto.CreateProjectRequest;
import com.smartcon.domain.project.dto.ProjectListRequest;
import com.smartcon.domain.project.dto.ProjectResponse;
import com.smartcon.domain.project.dto.UpdateProjectRequest;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.entity.ProjectManager;
import com.smartcon.domain.project.exception.ProjectAccessDeniedException;
import com.smartcon.domain.project.exception.ProjectNotFoundException;
import com.smartcon.domain.project.repository.ProjectManagerRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로젝트 관리 서비스 구현
 * 5단계 역할 기반 프로젝트 생성, 조회, 수정 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectManagementServiceImpl implements ProjectManagementService {

    private final ProjectRepository projectRepository;
    private final ProjectManagerRepository projectManagerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Long tenantId, Long userId, Role userRole) {
        log.info("프로젝트 생성 시작 - 테넌트: {}, 사용자: {}, 역할: {}", tenantId, userId, userRole);

        // 권한 확인: 본사관리자 또는 현장관리자만 생성 가능
        if (!userRole.isHqOrAbove() && !userRole.equals(Role.ROLE_SITE)) {
            throw new ProjectAccessDeniedException("프로젝트 생성 권한이 없습니다. 역할: " + userRole.getDisplayName());
        }

        // 프로젝트 엔티티 생성
        Project project = Project.builder()
                .name(request.getName())
                .constructionPeriodStart(request.getConstructionPeriodStart())
                .constructionPeriodEnd(request.getConstructionPeriodEnd())
                .siteManagerName(request.getSiteManagerName())
                .location(request.getLocation())
                .description(request.getDescription())
                .status(Project.ProjectStatus.ACTIVE)
                .faceDevices(new ArrayList<>())
                .projectManagers(new ArrayList<>())
                .build();
        
        project.setTenantId(tenantId);

        // 안면인식기 디바이스 추가
        if (request.getFaceRecognitionDeviceSerials() != null) {
            for (String serialNumber : request.getFaceRecognitionDeviceSerials()) {
                FaceRecognitionDevice device = FaceRecognitionDevice.builder()
                        .project(project)
                        .serialNumber(serialNumber)
                        .deviceName("Device-" + serialNumber)
                        .isActive(true)
                        .syncStatus(FaceRecognitionDevice.DeviceSyncStatus.PENDING)
                        .build();
                device.setTenantId(tenantId);
                project.addFaceDevice(device);
            }
        }

        // 현장관리자 배정
        if (request.getSiteManagerId() != null) {
            User siteManager = userRepository.findById(request.getSiteManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("현장관리자를 찾을 수 없습니다. ID: " + request.getSiteManagerId()));
            
            ProjectManager projectManager = ProjectManager.builder()
                    .project(project)
                    .user(siteManager)
                    .role(ProjectManager.ManagerRole.SITE_MANAGER)
                    .assignedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            projectManager.setTenantId(tenantId);
            project.addProjectManager(projectManager);
        }

        // 프로젝트 저장
        Project savedProject = projectRepository.save(project);
        
        log.info("프로젝트 생성 완료 - ID: {}, 이름: {}", savedProject.getId(), savedProject.getName());
        
        return ProjectResponse.from(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long tenantId, Long userId, Role userRole) {
        log.info("프로젝트 수정 시작 - ID: {}, 테넌트: {}, 사용자: {}, 역할: {}", projectId, tenantId, userId, userRole);

        // 프로젝트 조회
        Project project = projectRepository.findByTenantIdAndId(tenantId, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId, tenantId));

        // 권한 확인
        validateProjectAccess(project, userId, userRole, "수정");

        // 프로젝트 정보 업데이트
        project.setName(request.getName());
        project.setConstructionPeriodStart(request.getConstructionPeriodStart());
        project.setConstructionPeriodEnd(request.getConstructionPeriodEnd());
        project.setSiteManagerName(request.getSiteManagerName());
        project.setLocation(request.getLocation());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());

        Project updatedProject = projectRepository.save(project);
        
        log.info("프로젝트 수정 완료 - ID: {}, 이름: {}", updatedProject.getId(), updatedProject.getName());
        
        return ProjectResponse.from(updatedProject);
    }

    @Override
    public ProjectResponse getProject(Long projectId, Long tenantId, Long userId, Role userRole) {
        log.info("프로젝트 조회 - ID: {}, 테넌트: {}, 사용자: {}, 역할: {}", projectId, tenantId, userId, userRole);

        // 프로젝트 조회
        Project project = projectRepository.findByTenantIdAndId(tenantId, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId, tenantId));

        // 권한 확인
        validateProjectAccess(project, userId, userRole, "조회");

        return ProjectResponse.from(project);
    }

    @Override
    public List<ProjectResponse> getProjects(ProjectListRequest request, Long tenantId, Long userId, Role userRole) {
        log.info("프로젝트 목록 조회 - 테넌트: {}, 사용자: {}, 역할: {}", tenantId, userId, userRole);

        // 기본값 설정
        request.setDefaults();

        List<Project> projects;

        // 역할별 프로젝트 조회
        if (userRole.isHqOrAbove()) {
            // 본사관리자 이상: 테넌트 내 모든 프로젝트
            if (request.hasStatusFilter()) {
                projects = projectRepository.findByTenantIdAndStatus(tenantId, request.getStatus());
            } else {
                projects = projectRepository.findAll().stream()
                        .filter(p -> p.getTenantId().equals(tenantId))
                        .collect(Collectors.toList());
            }
        } else {
            // 현장관리자, 노무팀장, 일반노무자: 자신이 관리/배정된 프로젝트만
            projects = projectRepository.findProjectsByManagerUserId(tenantId, userId);
            
            // 상태 필터 적용
            if (request.hasStatusFilter()) {
                projects = projects.stream()
                        .filter(p -> p.getStatus().equals(request.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        // 검색어 필터링
        if (request.hasSearch()) {
            String searchLower = request.getSearch().toLowerCase();
            projects = projects.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // 정렬 적용
        projects = applySorting(projects, request.getSortBy(), request.getSortOrder());

        // 페이징 적용
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), projects.size());
        
        if (start >= projects.size()) {
            return new ArrayList<>();
        }

        List<Project> pagedProjects = projects.subList(start, end);

        log.info("프로젝트 목록 조회 완료 - 총 {}개, 페이지 {}개", projects.size(), pagedProjects.size());

        return ProjectResponse.fromList(pagedProjects);
    }

    @Override
    public long countProjectsByTenant(Long tenantId) {
        return projectRepository.countByTenantId(tenantId);
    }

    @Override
    public long countActiveProjectsByTenant(Long tenantId) {
        return projectRepository.countActiveProjectsByTenantId(tenantId);
    }

    @Override
    public List<ProjectResponse> getProjectsByManager(Long tenantId, Long userId) {
        List<Project> projects = projectRepository.findProjectsByManagerUserId(tenantId, userId);
        return ProjectResponse.fromList(projects);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long tenantId, Long userId, Role userRole) {
        log.info("프로젝트 삭제 시작 - ID: {}, 테넌트: {}, 사용자: {}, 역할: {}", projectId, tenantId, userId, userRole);

        // 권한 확인: 본사관리자만 삭제 가능
        if (!userRole.isHqOrAbove()) {
            throw new ProjectAccessDeniedException("프로젝트 삭제 권한이 없습니다. 역할: " + userRole.getDisplayName());
        }

        // 프로젝트 조회
        Project project = projectRepository.findByTenantIdAndId(tenantId, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId, tenantId));

        // 프로젝트 삭제
        projectRepository.delete(project);
        
        log.info("프로젝트 삭제 완료 - ID: {}", projectId);
    }

    @Override
    @Transactional
    public void inviteSiteManager(Long projectId, Long managerId, Long tenantId, Long userId, Role userRole) {
        log.info("현장관리자 초대 시작 - 프로젝트: {}, 관리자: {}, 테넌트: {}", projectId, managerId, tenantId);

        // 권한 확인: 본사관리자만 초대 가능
        if (!userRole.isHqOrAbove()) {
            throw new ProjectAccessDeniedException("현장관리자 초대 권한이 없습니다. 역할: " + userRole.getDisplayName());
        }

        // 프로젝트 조회
        Project project = projectRepository.findByTenantIdAndId(tenantId, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId, tenantId));

        // 관리자 사용자 조회
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + managerId));

        // 이미 배정되어 있는지 확인
        boolean alreadyAssigned = project.getProjectManagers().stream()
                .anyMatch(pm -> pm.getUser().getId().equals(managerId) && pm.isActive());

        if (alreadyAssigned) {
            throw new IllegalStateException("이미 배정된 관리자입니다. 관리자 ID: " + managerId);
        }

        // 프로젝트 관리자 추가
        ProjectManager projectManager = ProjectManager.builder()
                .project(project)
                .user(manager)
                .role(ProjectManager.ManagerRole.SITE_MANAGER)
                .assignedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        projectManager.setTenantId(tenantId);
        
        project.addProjectManager(projectManager);
        projectRepository.save(project);
        
        log.info("현장관리자 초대 완료 - 프로젝트: {}, 관리자: {}", projectId, managerId);
    }

    @Override
    @Transactional
    public void removeSiteManager(Long projectId, Long managerId, Long tenantId, Long userId, Role userRole) {
        log.info("현장관리자 배정 해제 시작 - 프로젝트: {}, 관리자: {}, 테넌트: {}", projectId, managerId, tenantId);

        // 권한 확인: 본사관리자만 해제 가능
        if (!userRole.isHqOrAbove()) {
            throw new ProjectAccessDeniedException("현장관리자 배정 해제 권한이 없습니다. 역할: " + userRole.getDisplayName());
        }

        // 프로젝트 조회
        Project project = projectRepository.findByTenantIdAndId(tenantId, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId, tenantId));

        // 프로젝트 관리자 찾기
        ProjectManager projectManager = project.getProjectManagers().stream()
                .filter(pm -> pm.getUser().getId().equals(managerId) && pm.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("배정된 관리자를 찾을 수 없습니다. 관리자 ID: " + managerId));

        // 비활성화
        projectManager.setIsActive(false);
        projectRepository.save(project);
        
        log.info("현장관리자 배정 해제 완료 - 프로젝트: {}, 관리자: {}", projectId, managerId);
    }

    /**
     * 프로젝트 접근 권한 검증
     */
    private void validateProjectAccess(Project project, Long userId, Role userRole, String action) {
        // 본사관리자 이상은 모든 프로젝트 접근 가능
        if (userRole.isHqOrAbove()) {
            return;
        }

        // 현장관리자, 노무팀장, 일반노무자는 자신이 배정된 프로젝트만 접근 가능
        boolean hasAccess = project.getProjectManagers().stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userId) && pm.isActive());

        if (!hasAccess) {
            throw new ProjectAccessDeniedException(
                    String.format("프로젝트 %s 권한이 없습니다. Project ID: %d, User ID: %d, Role: %s",
                            action, project.getId(), userId, userRole.getDisplayName())
            );
        }
    }

    /**
     * 프로젝트 목록 정렬 적용
     */
    private List<Project> applySorting(List<Project> projects, String sortBy, String sortOrder) {
        Comparator<Project> comparator;

        switch (sortBy) {
            case "recentLogin":
                // 최근 로그인 순 (프로젝트 관리자의 마지막 접근 시간 기준)
                comparator = Comparator.comparing(p -> 
                    p.getProjectManagers().stream()
                            .filter(ProjectManager::isActive)
                            .map(ProjectManager::getLastAccessAt)
                            .filter(java.util.Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(LocalDateTime.MIN),
                    Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;

            case "recentAssignment":
                // 최근 배정 순 (프로젝트 관리자의 배정 시간 기준)
                comparator = Comparator.comparing(p -> 
                    p.getProjectManagers().stream()
                            .filter(ProjectManager::isActive)
                            .map(ProjectManager::getAssignedAt)
                            .max(Comparator.naturalOrder())
                            .orElse(LocalDateTime.MIN),
                    Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;

            case "constructionPeriod":
                // 공사 기간 남은 순 (종료일 기준)
                comparator = Comparator.comparing(Project::getConstructionPeriodEnd,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            default:
                // 기본: 생성일 순
                comparator = Comparator.comparing(Project::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()));
        }

        // 정렬 순서 적용
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return projects.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
