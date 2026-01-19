package com.smartcon.domain.project.repository;

import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 안면인식기 디바이스 리포지토리
 */
@Repository
public interface FaceRecognitionDeviceRepository extends JpaRepository<FaceRecognitionDevice, Long> {

    /**
     * 시리얼 번호로 디바이스 조회
     */
    Optional<FaceRecognitionDevice> findBySerialNumber(String serialNumber);

    /**
     * 프로젝트별 디바이스 목록 조회
     */
    @Query("SELECT d FROM FaceRecognitionDevice d WHERE d.project.id = :projectId")
    List<FaceRecognitionDevice> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 활성 디바이스 목록 조회
     */
    @Query("SELECT d FROM FaceRecognitionDevice d WHERE d.isActive = true")
    List<FaceRecognitionDevice> findActiveDevices();

    /**
     * 프로젝트별 활성 디바이스 목록 조회
     */
    @Query("SELECT d FROM FaceRecognitionDevice d WHERE d.project.id = :projectId AND d.isActive = true")
    List<FaceRecognitionDevice> findActiveDevicesByProjectId(@Param("projectId") Long projectId);

    /**
     * 동기화 상태별 디바이스 조회
     */
    @Query("SELECT d FROM FaceRecognitionDevice d WHERE d.syncStatus = :status")
    List<FaceRecognitionDevice> findBySyncStatus(@Param("status") FaceRecognitionDevice.DeviceSyncStatus status);

    // ========== 대시보드용 통계 메서드 ==========

    /**
     * 프로젝트별 디바이스 수 조회
     */
    @Query("SELECT COUNT(d) FROM FaceRecognitionDevice d WHERE d.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 프로젝트별 동기화 상태별 디바이스 수 조회
     */
    @Query("SELECT COUNT(d) FROM FaceRecognitionDevice d WHERE d.project.id = :projectId AND d.syncStatus = :syncStatus")
    long countByProjectIdAndSyncStatus(@Param("projectId") Long projectId, @Param("syncStatus") FaceRecognitionDevice.DeviceSyncStatus syncStatus);
}
