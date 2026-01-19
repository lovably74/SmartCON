package com.smartcon.domain.project.repository;

import com.smartcon.domain.project.entity.ProjectManager;
import com.smartcon.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로젝트 관리자 리포지토리
 * 현장별 관리자 배정 및 권한 관리
 */
@Repository
public interface ProjectManagerRepository extends JpaRepository<ProjectManager, Long> {

    /**
     * 사용자 ID로 활성 프로젝트 관리자 목록 조회
     */
    @Query("SELECT pm FROM ProjectManager pm " +
           "WHERE pm.user.id = :userId AND pm.isActive = true " +
           "ORDER BY pm.lastAccessAt DESC NULLS LAST, pm.assignedAt DESC")
    List<ProjectManager> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 역할로 활성 프로젝트 관리자 목록 조회
     */
    @Query("SELECT pm FROM ProjectManager pm " +
           "WHERE pm.user.id = :userId AND pm.role = :role AND pm.isActive = true " +
           "ORDER BY pm.lastAccessAt DESC NULLS LAST, pm.assignedAt DESC")
    List<ProjectManager> findActiveByUserIdAndRole(@Param("userId") Long userId, 
                                                   @Param("role") ProjectManager.ManagerRole role);

    /**
     * 사용자 ID와 프로젝트 ID로 활성 프로젝트 관리자 조회
     */
    @Query("SELECT pm FROM ProjectManager pm " +
           "WHERE pm.user.id = :userId AND pm.project.id = :projectId AND pm.isActive = true")
    Optional<ProjectManager> findActiveByUserIdAndProjectId(@Param("userId") Long userId, 
                                                            @Param("projectId") Long projectId);

    /**
     * 프로젝트 ID로 활성 관리자 목록 조회
     */
    @Query("SELECT pm FROM ProjectManager pm " +
           "WHERE pm.project.id = :projectId AND pm.isActive = true")
    List<ProjectManager> findActiveByProjectId(@Param("projectId") Long projectId);

    /**
     * 사용자가 특정 프로젝트에 접근 권한이 있는지 확인
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectManager pm " +
           "WHERE pm.user.id = :userId AND pm.project.id = :projectId AND pm.isActive = true")
    boolean hasAccessToProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    /**
     * 사용자의 프로젝트 수 조회
     */
    @Query("SELECT COUNT(DISTINCT pm.project.id) FROM ProjectManager pm " +
           "WHERE pm.user.id = :userId AND pm.isActive = true")
    long countProjectsByUserId(@Param("userId") Long userId);

    /**
     * 사용자와 역할별 프로젝트 수 조회
     */
    @Query("SELECT COUNT(DISTINCT pm.project.id) FROM ProjectManager pm " +
           "WHERE pm.user.id = :userId AND pm.role = :role AND pm.isActive = true")
    long countProjectsByUserIdAndRole(@Param("userId") Long userId, 
                                     @Param("role") ProjectManager.ManagerRole role);
}
