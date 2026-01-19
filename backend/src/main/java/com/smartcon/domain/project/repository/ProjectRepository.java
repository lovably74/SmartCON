package com.smartcon.domain.project.repository;

import com.smartcon.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로젝트 리포지토리
 * 5단계 역할 기반 시스템의 현장 관리
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 테넌트별 활성 프로젝트 조회
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
    List<Project> findActiveProjectsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 프로젝트 상태별 조회
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId AND p.status = :status")
    List<Project> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, 
                                         @Param("status") Project.ProjectStatus status);

    /**
     * 테넌트별 프로젝트명으로 검색
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId AND p.name LIKE %:name%")
    List<Project> findByTenantIdAndNameContaining(@Param("tenantId") Long tenantId, 
                                                 @Param("name") String name);

    /**
     * 테넌트별 프로젝트 수 조회
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트별 활성 프로젝트 수 조회
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
    long countActiveProjectsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 특정 사용자가 관리하는 프로젝트 조회
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN p.projectManagers pm " +
           "WHERE p.tenantId = :tenantId AND pm.user.id = :userId AND pm.isActive = true")
    List<Project> findProjectsByManagerUserId(@Param("tenantId") Long tenantId, 
                                             @Param("userId") Long userId);

    /**
     * 테넌트와 ID로 프로젝트 조회
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId AND p.id = :id")
    Optional<Project> findByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("id") Long id);

    // ========== 대시보드용 통계 메서드 ==========

    /**
     * 테넌트별 프로젝트 수 조회 (String tenantId)
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") String tenantId);

    /**
     * 테넌트별 상태별 프로젝트 수 조회 (String tenantId)
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId AND p.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") Project.ProjectStatus status);

    /**
     * 상태별 전체 프로젝트 수 조회 (슈퍼관리자용)
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") Project.ProjectStatus status);

    /**
     * 특정 프로젝트가 특정 상태인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p " +
           "WHERE p.id = :projectId AND p.status = :status")
    boolean existsByIdAndStatus(@Param("projectId") Long projectId, @Param("status") Project.ProjectStatus status);

    /**
     * 테넌트별 노무자 수 상위 프로젝트 조회 (상위 5개)
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId " +
           "ORDER BY (SELECT COUNT(DISTINCT ar.worker.id) FROM AttendanceRecord ar WHERE ar.project.id = p.id) DESC")
    List<Project> findTop5ByTenantIdOrderByWorkerCountDesc(@Param("tenantId") String tenantId);

    /**
     * 테넌트별 활성 프로젝트 조회 (String tenantId)
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId AND p.status = :status")
    List<Project> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") Project.ProjectStatus status);
}