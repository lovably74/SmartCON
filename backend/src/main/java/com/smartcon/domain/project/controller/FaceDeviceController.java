package com.smartcon.domain.project.controller;

import com.smartcon.domain.attendance.service.FaceRecognitionIntegrationService;
import com.smartcon.domain.project.dto.DeviceSyncStatusResponse;
import com.smartcon.domain.project.dto.FaceDeviceRequest;
import com.smartcon.domain.project.dto.FaceDeviceResponse;
import com.smartcon.domain.project.service.FaceDeviceService;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 안면인식기 디바이스 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/face-devices")
@RequiredArgsConstructor
public class FaceDeviceController {

    private final FaceDeviceService faceDeviceService;
    private final FaceRecognitionIntegrationService integrationService;

    /**
     * 안면인식기 디바이스 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FaceDeviceResponse>> registerDevice(
            @Valid @RequestBody FaceDeviceRequest request) {
        log.info("[API] 안면인식기 등록 요청: 시리얼={}, 프로젝트 ID={}", 
                request.getSerialNumber(), request.getProjectId());

        FaceDeviceResponse response = faceDeviceService.registerDevice(request);
        return ResponseEntity.ok(ApiResponse.success(response, "안면인식기가 등록되었습니다"));
    }

    /**
     * 프로젝트별 디바이스 목록 조회
     */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<List<FaceDeviceResponse>>> getProjectDevices(
            @PathVariable Long projectId) {
        log.info("[API] 프로젝트 디바이스 목록 조회: 프로젝트 ID={}", projectId);

        List<FaceDeviceResponse> response = faceDeviceService.getProjectDevices(projectId);
        return ResponseEntity.ok(ApiResponse.success(response, "디바이스 목록 조회 성공"));
    }

    /**
     * 디바이스 상세 조회
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<FaceDeviceResponse>> getDeviceById(
            @PathVariable Long deviceId) {
        log.info("[API] 디바이스 상세 조회: ID={}", deviceId);

        FaceDeviceResponse response = faceDeviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(ApiResponse.success(response, "디바이스 조회 성공"));
    }

    /**
     * 디바이스 정보 수정
     */
    @PutMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<FaceDeviceResponse>> updateDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody FaceDeviceRequest request) {
        log.info("[API] 디바이스 정보 수정: ID={}", deviceId);

        FaceDeviceResponse response = faceDeviceService.updateDevice(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "디바이스 정보가 수정되었습니다"));
    }

    /**
     * 디바이스 삭제
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long deviceId) {
        log.info("[API] 디바이스 삭제: ID={}", deviceId);

        faceDeviceService.deleteDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success(null, "디바이스가 삭제되었습니다"));
    }

    /**
     * 디바이스 동기화 상태 조회
     */
    @GetMapping("/sync-status/{serialNumber}")
    public ResponseEntity<ApiResponse<DeviceSyncStatusResponse>> getDeviceSyncStatus(
            @PathVariable String serialNumber) {
        log.info("[API] 디바이스 동기화 상태 조회: 시리얼={}", serialNumber);

        DeviceSyncStatusResponse response = integrationService.getDeviceSyncStatus(serialNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "동기화 상태 조회 성공"));
    }

    /**
     * 프로젝트의 모든 디바이스 동기화 상태 조회
     */
    @GetMapping("/projects/{projectId}/sync-status")
    public ResponseEntity<ApiResponse<List<DeviceSyncStatusResponse>>> getProjectDeviceStatuses(
            @PathVariable Long projectId) {
        log.info("[API] 프로젝트 디바이스 동기화 상태 조회: 프로젝트 ID={}", projectId);

        List<DeviceSyncStatusResponse> response = integrationService.getProjectDeviceStatuses(projectId);
        return ResponseEntity.ok(ApiResponse.success(response, "동기화 상태 조회 성공"));
    }

    /**
     * 노무자를 디바이스에 등록
     */
    @PostMapping("/{serialNumber}/workers/{workerId}")
    public ResponseEntity<ApiResponse<Void>> registerWorkerToDevice(
            @PathVariable String serialNumber,
            @PathVariable Long workerId) {
        log.info("[API] 노무자 디바이스 등록: 시리얼={}, 노무자 ID={}", serialNumber, workerId);

        integrationService.registerWorkerToDevice(workerId, serialNumber);
        return ResponseEntity.ok(ApiResponse.success(null, "노무자가 디바이스에 등록되었습니다"));
    }

    /**
     * 노무자를 디바이스에서 제거
     */
    @DeleteMapping("/{serialNumber}/workers/{workerId}")
    public ResponseEntity<ApiResponse<Void>> removeWorkerFromDevice(
            @PathVariable String serialNumber,
            @PathVariable Long workerId) {
        log.info("[API] 노무자 디바이스 제거: 시리얼={}, 노무자 ID={}", serialNumber, workerId);

        integrationService.removeWorkerFromDevice(workerId, serialNumber);
        return ResponseEntity.ok(ApiResponse.success(null, "노무자가 디바이스에서 제거되었습니다"));
    }

    /**
     * 프로젝트의 모든 디바이스에 노무자 동기화
     */
    @PostMapping("/projects/{projectId}/workers/{workerId}/sync")
    public ResponseEntity<ApiResponse<Void>> syncWorkerToAllDevices(
            @PathVariable Long projectId,
            @PathVariable Long workerId) {
        log.info("[API] 프로젝트 전체 디바이스 노무자 동기화: 프로젝트 ID={}, 노무자 ID={}", 
                projectId, workerId);

        integrationService.syncWorkerToAllDevices(workerId, projectId);
        return ResponseEntity.ok(ApiResponse.success(null, "노무자가 모든 디바이스에 동기화되었습니다"));
    }
}
