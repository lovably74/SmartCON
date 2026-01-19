package com.smartcon.domain.attendance.service;

import com.smartcon.domain.project.dto.DeviceSyncStatusResponse;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;

import java.util.List;

/**
 * 안면인식기 통합 서비스 인터페이스
 * FaceNet API 연동 및 디바이스 관리
 */
public interface FaceRecognitionIntegrationService {

    /**
     * 노무자를 안면인식기에 등록
     */
    void registerWorkerToDevice(Long workerId, String deviceSerialNumber);

    /**
     * 노무자를 안면인식기에서 제거
     */
    void removeWorkerFromDevice(Long workerId, String deviceSerialNumber);

    /**
     * 프로젝트의 모든 디바이스에 노무자 동기화
     */
    void syncWorkerToAllDevices(Long workerId, Long projectId);

    /**
     * 디바이스 동기화 상태 조회
     */
    DeviceSyncStatusResponse getDeviceSyncStatus(String deviceSerialNumber);

    /**
     * 프로젝트의 모든 디바이스 상태 조회
     */
    List<DeviceSyncStatusResponse> getProjectDeviceStatuses(Long projectId);

    /**
     * 디바이스 연결 실패 처리
     */
    void handleDeviceConnectionFailure(String deviceSerialNumber, Exception error);

    /**
     * 디바이스 동기화 상태 업데이트
     */
    void updateDeviceSyncStatus(String deviceSerialNumber, FaceRecognitionDevice.DeviceSyncStatus status);

    /**
     * 안면인식 신뢰도 검증
     */
    boolean validateFaceMatchConfidence(java.math.BigDecimal confidence);
}
