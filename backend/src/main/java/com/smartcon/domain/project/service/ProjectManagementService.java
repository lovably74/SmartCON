package com.smartcon.domain.project.service;

import com.smartcon.domain.project.dto.CreateProjectRequest;
import com.smartcon.domain.project.dto.ProjectListRequest;
import com.smartcon.domain.project.dto.ProjectResponse;
import com.smartcon.domain.project.dto.UpdateProjectRequest;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.user.entity.Role;

import java.util.List;

/**
 * 프로젝트 관리 서비스 인터페이스
 * 5단계 역할 기반 프로젝트 생성, 조회, 수정 기능 제공
 */
public interface ProjectManagementService {

    /**
     * 프로젝트 생성 (본사관리자, 현장관리자)
     * 
     * @param request 프로젝트 생성 요청
     * @param tenantId 테넌트 ID
     * @param userId 생성자 사용자 ID
     * @param userRole 생성자 역할
     * @return 생성된 프로젝트 응답
     */
    ProjectResponse createProject(CreateProjectRequest request, Long tenantId, Long userId, Role userRole);

    /**
     * 프로젝트 수정 (본사관리자, 현장관리자)
     * 
     * @param projectId 프로젝트 ID
     * @param request 프로젝트 수정 요청
     * @param tenantId 테넌트 ID
     * @param userId 수정자 사용자 ID
     * @param userRole 수정자 역할
     * @return 수정된 프로젝트 응답
     */
    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long tenantId, Long userId, Role userRole);

    /**
     * 프로젝트 단건 조회 (역할별 권한 적용)
     * 
     * @param projectId 프로젝트 ID
     * @param tenantId 테넌트 ID
     * @param userId 조회자 사용자 ID
     * @param userRole 조회자 역할
     * @return 프로젝트 응답
     */
    ProjectResponse getProject(Long projectId, Long tenantId, Long userId, Role userRole);

    /**
     * 프로젝트 목록 조회 (역할별 권한 적용)
     * - 본사관리자: 테넌트 내 모든 프로젝트
     * - 현장관리자: 자신이 관리하는 프로젝트만
     * - 노무팀장/일반노무자: 자신이 배정된 프로젝트만
     * 
     * @param request 프로젝트 목록 조회 요청 (정렬, 필터링, 검색)
     * @param tenantId 테넌트 ID
     * @param userId 조회자 사용자 ID
     * @param userRole 조회자 역할
     * @return 프로젝트 목록
     */
    List<ProjectResponse> getProjects(ProjectListRequest request, Long tenantId, Long userId, Role userRole);

    /**
     * 테넌트별 프로젝트 수 조회
     * 
     * @param tenantId 테넌트 ID
     * @return 프로젝트 수
     */
    long countProjectsByTenant(Long tenantId);

    /**
     * 테넌트별 활성 프로젝트 수 조회
     * 
     * @param tenantId 테넌트 ID
     * @return 활성 프로젝트 수
     */
    long countActiveProjectsByTenant(Long tenantId);

    /**
     * 특정 사용자가 관리하는 프로젝트 조회
     * 
     * @param tenantId 테넌트 ID
     * @param userId 사용자 ID
     * @return 프로젝트 목록
     */
    List<ProjectResponse> getProjectsByManager(Long tenantId, Long userId);

    /**
     * 프로젝트 삭제 (본사관리자만)
     * 
     * @param projectId 프로젝트 ID
     * @param tenantId 테넌트 ID
     * @param userId 삭제자 사용자 ID
     * @param userRole 삭제자 역할
     */
    void deleteProject(Long projectId, Long tenantId, Long userId, Role userRole);

    /**
     * 현장관리자 초대 (본사관리자만)
     * 
     * @param projectId 프로젝트 ID
     * @param managerId 초대할 관리자 사용자 ID
     * @param tenantId 테넌트 ID
     * @param userId 초대자 사용자 ID
     * @param userRole 초대자 역할
     */
    void inviteSiteManager(Long projectId, Long managerId, Long tenantId, Long userId, Role userRole);

    /**
     * 현장관리자 배정 해제 (본사관리자만)
     * 
     * @param projectId 프로젝트 ID
     * @param managerId 해제할 관리자 사용자 ID
     * @param tenantId 테넌트 ID
     * @param userId 해제자 사용자 ID
     * @param userRole 해제자 역할
     */
    void removeSiteManager(Long projectId, Long managerId, Long tenantId, Long userId, Role userRole);
}
