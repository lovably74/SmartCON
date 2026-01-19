package com.smartcon.domain.project.controller;

import com.smartcon.domain.project.dto.CreateProjectRequest;
import com.smartcon.domain.project.dto.ProjectListRequest;
import com.smartcon.domain.project.dto.ProjectResponse;
import com.smartcon.domain.project.dto.UpdateProjectRequest;
import com.smartcon.domain.project.service.ProjectManagementService;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.global.common.ApiResponse;
import com.smartcon.global.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 프로젝트 관리 컨트롤러
 * 5단계 역할 기반 프로젝트 생성, 조회, 수정 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectManagementController {

    private final ProjectManagementService projectManagementService;

    /**
     * 프로젝트 생성 (본사관리자, 현장관리자)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("프로젝트 생성 요청 - 테넌트: {}, 사용자: {}, 역할: {}", tenantId, userId, userRole);

        ProjectResponse response = projectManagementService.createProject(request, tenantId, userId, userRole);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "프로젝트가 생성되었습니다."));
    }

    /**
     * 프로젝트 수정 (본사관리자, 현장관리자)
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("프로젝트 수정 요청 - ID: {}, 테넌트: {}, 사용자: {}", projectId, tenantId, userId);

        ProjectResponse response = projectManagementService.updateProject(projectId, request, tenantId, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(response, "프로젝트가 수정되었습니다."));
    }

    /**
     * 프로젝트 단건 조회 (역할별 권한 적용)
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("프로젝트 조회 요청 - ID: {}, 테넌트: {}, 사용자: {}", projectId, tenantId, userId);

        ProjectResponse response = projectManagementService.getProject(projectId, tenantId, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 목록 조회 (역할별 권한 적용, 정렬/필터링/검색 지원)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recentLogin") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("프로젝트 목록 조회 요청 - 테넌트: {}, 사용자: {}, 역할: {}", tenantId, userId, userRole);

        // 요청 DTO 생성
        ProjectListRequest request = ProjectListRequest.builder()
                .status(status != null ? com.smartcon.domain.project.entity.Project.ProjectStatus.valueOf(status) : null)
                .search(search)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();

        List<ProjectResponse> response = projectManagementService.getProjects(request, tenantId, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 삭제 (본사관리자만)
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("프로젝트 삭제 요청 - ID: {}, 테넌트: {}, 사용자: {}", projectId, tenantId, userId);

        projectManagementService.deleteProject(projectId, tenantId, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(null, "프로젝트가 삭제되었습니다."));
    }

    /**
     * 현장관리자 초대 (본사관리자만)
     */
    @PostMapping("/{projectId}/managers/{managerId}")
    public ResponseEntity<ApiResponse<Void>> inviteSiteManager(
            @PathVariable Long projectId,
            @PathVariable Long managerId,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("현장관리자 초대 요청 - 프로젝트: {}, 관리자: {}, 테넌트: {}", projectId, managerId, tenantId);

        projectManagementService.inviteSiteManager(projectId, managerId, tenantId, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(null, "현장관리자가 초대되었습니다."));
    }

    /**
     * 현장관리자 배정 해제 (본사관리자만)
     */
    @DeleteMapping("/{projectId}/managers/{managerId}")
    public ResponseEntity<ApiResponse<Void>> removeSiteManager(
            @PathVariable Long projectId,
            @PathVariable Long managerId,
            Authentication authentication) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = getUserIdFromAuthentication(authentication);
        Role userRole = getUserRoleFromAuthentication(authentication);

        log.info("현장관리자 배정 해제 요청 - 프로젝트: {}, 관리자: {}, 테넌트: {}", projectId, managerId, tenantId);

        projectManagementService.removeSiteManager(projectId, managerId, tenantId, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(null, "현장관리자 배정이 해제되었습니다."));
    }

    /**
     * 프로젝트 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getProjectStats(Authentication authentication) {
        Long tenantId = TenantContext.getCurrentTenantId();

        log.info("프로젝트 통계 조회 요청 - 테넌트: {}", tenantId);

        long totalProjects = projectManagementService.countProjectsByTenant(tenantId);
        long activeProjects = projectManagementService.countActiveProjectsByTenant(tenantId);

        Map<String, Long> stats = Map.of(
                "totalProjects", totalProjects,
                "activeProjects", activeProjects
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Authentication에서 사용자 ID 추출
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // 실제 구현에서는 JWT 토큰에서 사용자 ID를 추출
        // 현재는 임시로 1L 반환
        return 1L;
    }

    /**
     * Authentication에서 사용자 역할 추출
     */
    private Role getUserRoleFromAuthentication(Authentication authentication) {
        // 실제 구현에서는 JWT 토큰에서 역할을 추출
        // 현재는 임시로 첫 번째 권한을 역할로 변환
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(Role::valueOf)
                .orElse(Role.ROLE_WORKER);
    }
}
