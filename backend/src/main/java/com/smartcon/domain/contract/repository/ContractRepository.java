package com.smartcon.domain.contract.repository;

import com.smartcon.domain.contract.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 근로계약 리포지토리
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * 노무자 ID로 계약 목록 조회
     */
    List<Contract> findByWorkerId(Long workerId);

    /**
     * 노무자 ID와 상태로 계약 목록 조회
     */
    List<Contract> findByWorkerIdAndStatus(Long workerId, Contract.ContractStatus status);

    /**
     * 프로젝트 ID로 계약 목록 조회
     */
    List<Contract> findByProjectId(Long projectId);

    /**
     * 프로젝트 ID와 상태로 계약 목록 조회
     */
    List<Contract> findByProjectIdAndStatus(Long projectId, Contract.ContractStatus status);

    /**
     * 프로젝트 ID와 상태로 계약 목록 조회 (페이징)
     */
    Page<Contract> findByProjectIdAndStatus(Long projectId, Contract.ContractStatus status, Pageable pageable);

    /**
     * 노무자 ID와 프로젝트 ID로 계약 조회
     */
    Optional<Contract> findByWorkerIdAndProjectIdAndWorkDate(Long workerId, Long projectId, LocalDate workDate);

    /**
     * 상태별 계약 목록 조회
     */
    List<Contract> findByStatus(Contract.ContractStatus status);

    /**
     * 상태별 계약 목록 조회 (페이징)
     */
    Page<Contract> findByStatus(Contract.ContractStatus status, Pageable pageable);

    /**
     * 만료일이 지난 계약 목록 조회
     */
    @Query("SELECT c FROM Contract c WHERE c.expiryDate < :currentDate AND c.status != 'EXPIRED'")
    List<Contract> findExpiredContracts(@Param("currentDate") LocalDate currentDate);

    /**
     * 특정 기간 내 계약 목록 조회
     */
    @Query("SELECT c FROM Contract c WHERE c.workDate BETWEEN :startDate AND :endDate")
    List<Contract> findByWorkDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 프로젝트별 상태별 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.project.id = :projectId AND c.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Contract.ContractStatus status);

    /**
     * 노무자별 상태별 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.worker.id = :workerId AND c.status = :status")
    long countByWorkerIdAndStatus(@Param("workerId") Long workerId, @Param("status") Contract.ContractStatus status);

    /**
     * 수정 요청이 있는 계약 목록 조회
     */
    @Query("SELECT c FROM Contract c WHERE c.modificationRequest IS NOT NULL AND c.modificationRequest != ''")
    List<Contract> findContractsWithModificationRequests();

    /**
     * 프로젝트별 수정 요청이 있는 계약 목록 조회
     */
    @Query("SELECT c FROM Contract c WHERE c.project.id = :projectId AND c.modificationRequest IS NOT NULL AND c.modificationRequest != ''")
    List<Contract> findContractsWithModificationRequestsByProjectId(@Param("projectId") Long projectId);

    // ========== 대시보드용 통계 메서드 ==========

    /**
     * 상태별 전체 계약 수 조회 (슈퍼관리자용)
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countByStatus(@Param("status") Contract.ContractStatus status);

    /**
     * 테넌트별 상태별 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.tenantId = :tenantId AND c.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") Contract.ContractStatus status);

    /**
     * 테넌트별 상태별 서명일 기준 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.tenantId = :tenantId AND c.status = :status " +
           "AND c.signedAt BETWEEN :startDate AND :endDate")
    long countByTenantIdAndStatusAndSignedAtBetween(@Param("tenantId") String tenantId,
                                                    @Param("status") Contract.ContractStatus status,
                                                    @Param("startDate") java.time.LocalDateTime startDate,
                                                    @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 테넌트별 상태별 생성일 기준 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.tenantId = :tenantId AND c.status = :status " +
           "AND c.createdAt BETWEEN :startDate AND :endDate")
    long countByTenantIdAndStatusAndCreatedAtBetween(@Param("tenantId") String tenantId,
                                                     @Param("status") Contract.ContractStatus status,
                                                     @Param("startDate") java.time.LocalDateTime startDate,
                                                     @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 테넌트별 상태별 수정일 기준 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.tenantId = :tenantId AND c.status = :status " +
           "AND c.updatedAt BETWEEN :startDate AND :endDate")
    long countByTenantIdAndStatusAndUpdatedAtBetween(@Param("tenantId") String tenantId,
                                                     @Param("status") Contract.ContractStatus status,
                                                     @Param("startDate") java.time.LocalDateTime startDate,
                                                     @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 노무자별 프로젝트별 상태별 계약 수 조회
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.worker.id = :workerId AND c.project.id = :projectId AND c.status = :status")
    long countByWorkerIdAndProjectIdAndStatus(@Param("workerId") Long workerId,
                                              @Param("projectId") Long projectId,
                                              @Param("status") Contract.ContractStatus status);
}
