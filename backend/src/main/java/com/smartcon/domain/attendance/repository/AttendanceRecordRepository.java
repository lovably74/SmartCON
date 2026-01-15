package com.smartcon.domain.attendance.repository;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 출역 기록 리포지토리
 * 안면인식기 연동 및 실시간 출역 데이터 처리
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 테넌트별 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId")
    List<AttendanceRecord> findByTenantId(@Param("tenantId") String tenantId);

    /**
     * 특정 프로젝트의 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId AND ar.project.id = :projectId")
    List<AttendanceRecord> findByTenantIdAndProjectId(@Param("tenantId") String tenantId, 
                                                     @Param("projectId") Long projectId);

    /**
     * 특정 노무자의 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId AND ar.worker.id = :workerId")
    List<AttendanceRecord> findByTenantIdAndWorkerId(@Param("tenantId") String tenantId, 
                                                    @Param("workerId") Long workerId);

    /**
     * 특정 날짜의 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId AND ar.workDate = :workDate")
    List<AttendanceRecord> findByTenantIdAndWorkDate(@Param("tenantId") String tenantId, 
                                                    @Param("workDate") LocalDate workDate);

    /**
     * 기간별 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId " +
           "AND ar.workDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByTenantIdAndWorkDateBetween(@Param("tenantId") String tenantId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * 특정 프로젝트의 오늘 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId " +
           "AND ar.project.id = :projectId AND ar.workDate = :today")
    List<AttendanceRecord> findTodayAttendanceByProject(@Param("tenantId") String tenantId,
                                                       @Param("projectId") Long projectId,
                                                       @Param("today") LocalDate today);

    /**
     * 특정 프로젝트의 오늘 신규 출역자 수 조회
     */
    @Query("SELECT COUNT(DISTINCT ar.worker.id) FROM AttendanceRecord ar " +
           "WHERE ar.tenantId = :tenantId AND ar.project.id = :projectId AND ar.workDate = :today " +
           "AND NOT EXISTS (SELECT 1 FROM AttendanceRecord ar2 " +
           "WHERE ar2.tenantId = :tenantId AND ar2.project.id = :projectId " +
           "AND ar2.worker.id = ar.worker.id AND ar2.workDate < :today)")
    long countNewWorkersToday(@Param("tenantId") String tenantId,
                             @Param("projectId") Long projectId,
                             @Param("today") LocalDate today);

    /**
     * 특정 프로젝트의 오늘 총 출역자 수 조회
     */
    @Query("SELECT COUNT(DISTINCT ar.worker.id) FROM AttendanceRecord ar " +
           "WHERE ar.tenantId = :tenantId AND ar.project.id = :projectId AND ar.workDate = :today")
    long countTotalWorkersToday(@Param("tenantId") String tenantId,
                               @Param("projectId") Long projectId,
                               @Param("today") LocalDate today);

    /**
     * 안면인식 데이터로 생성된 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId " +
           "AND ar.dataSource = 'FACE_RECOGNITION'")
    List<AttendanceRecord> findFaceRecognitionRecords(@Param("tenantId") String tenantId);

    /**
     * 시간이 수정된 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId " +
           "AND (ar.originalCheckInTime IS NOT NULL OR ar.originalCheckOutTime IS NOT NULL)")
    List<AttendanceRecord> findTimeModifiedRecords(@Param("tenantId") String tenantId);

    /**
     * 특정 노무자의 특정 날짜 출역 기록 조회
     */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.tenantId = :tenantId " +
           "AND ar.worker.id = :workerId AND ar.workDate = :workDate")
    Optional<AttendanceRecord> findByTenantIdAndWorkerIdAndWorkDate(@Param("tenantId") String tenantId,
                                                                   @Param("workerId") Long workerId,
                                                                   @Param("workDate") LocalDate workDate);
}