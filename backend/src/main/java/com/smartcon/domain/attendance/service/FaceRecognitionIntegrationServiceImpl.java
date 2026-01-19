package com.smartcon.domain.attendance.service;

import com.smartcon.domain.project.dto.DeviceSyncStatusResponse;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.FaceRecognitionDeviceRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 안면인식기 통합 서비스 구현체
 * FaceNet API 연동 및 디바이스 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaceRecognitionIntegrationServiceImpl implements FaceRecognitionIntegrationService {

    private static final BigDecimal CONFIDENCE_THRESHOLD = new BigDecimal("0.85");

    private final FaceRecognitionDeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public void registerWorkerToDevice(Long workerId, String deviceSerialNumber) {
        log.info("[안면인식기 노무자 등록] 노무자 ID: {}, 디바이스: {}", workerId, deviceSerialNumber);

        // 노무자 조회
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + workerId));

        // 디바이스 조회
        FaceRecognitionDevice device = deviceRepository.findBySerialNumber(deviceSerialNumber)
                .orElseThrow(() -> new IllegalArgumentException("안면인식기를 찾을 수 없습니다: " + deviceSerialNumber));

        try {
            // TODO: 실제 FaceNet API 호출
            // FaceNetApiClient.registerWorker(device.getApiEndpoint(), device.getApiKey(), worker);
            
            // 임시 구현: 동기화 상태 업데이트
            device.updateSyncStatus(FaceRecognitionDevice.DeviceSyncStatus.SYNCED);
            deviceRepository.save(device);
            
            log.info("[안면인식기 노무자 등록 완료] 노무자: {}, 디바이스: {}", worker.getName(), deviceSerialNumber);
        } catch (Exception e) {
            log.error("[안면인식기 노무자 등록 실패] 노무자 ID: {}, 디바이스: {}, 오류: {}", 
                    workerId, deviceSerialNumber, e.getMessage());
            handleDeviceConnectionFailure(deviceSerialNumber, e);
            throw new RuntimeException("안면인식기 노무자 등록 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void removeWorkerFromDevice(Long workerId, String deviceSerialNumber) {
        log.info("[안면인식기 노무자 제거] 노무자 ID: {}, 디바이스: {}", workerId, deviceSerialNumber);

        // 노무자 조회
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + workerId));

        // 디바이스 조회
        FaceRecognitionDevice device = deviceRepository.findBySerialNumber(deviceSerialNumber)
                .orElseThrow(() -> new IllegalArgumentException("안면인식기를 찾을 수 없습니다: " + deviceSerialNumber));

        try {
            // TODO: 실제 FaceNet API 호출
            // FaceNetApiClient.removeWorker(device.getApiEndpoint(), device.getApiKey(), workerId);
            
            log.info("[안면인식기 노무자 제거 완료] 노무자: {}, 디바이스: {}", worker.getName(), deviceSerialNumber);
        } catch (Exception e) {
            log.error("[안면인식기 노무자 제거 실패] 노무자 ID: {}, 디바이스: {}, 오류: {}", 
                    workerId, deviceSerialNumber, e.getMessage());
            handleDeviceConnectionFailure(deviceSerialNumber, e);
            throw new RuntimeException("안면인식기 노무자 제거 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void syncWorkerToAllDevices(Long workerId, Long projectId) {
        log.info("[프로젝트 전체 디바이스 노무자 동기화] 노무자 ID: {}, 프로젝트 ID: {}", workerId, projectId);

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        // 프로젝트의 활성 디바이스 목록 조회
        List<FaceRecognitionDevice> devices = deviceRepository.findActiveDevicesByProjectId(projectId);

        if (devices.isEmpty()) {
            log.warn("[프로젝트에 활성 디바이스 없음] 프로젝트 ID: {}", projectId);
            return;
        }

        // 각 디바이스에 노무자 등록
        int successCount = 0;
        int failureCount = 0;

        for (FaceRecognitionDevice device : devices) {
            try {
                registerWorkerToDevice(workerId, device.getSerialNumber());
                successCount++;
            } catch (Exception e) {
                log.error("[디바이스 동기화 실패] 디바이스: {}, 오류: {}", 
                        device.getSerialNumber(), e.getMessage());
                failureCount++;
            }
        }

        log.info("[프로젝트 전체 디바이스 노무자 동기화 완료] 성공: {}, 실패: {}", successCount, failureCount);
    }

    @Override
    public DeviceSyncStatusResponse getDeviceSyncStatus(String deviceSerialNumber) {
        log.info("[디바이스 동기화 상태 조회] 디바이스: {}", deviceSerialNumber);

        FaceRecognitionDevice device = deviceRepository.findBySerialNumber(deviceSerialNumber)
                .orElseThrow(() -> new IllegalArgumentException("안면인식기를 찾을 수 없습니다: " + deviceSerialNumber));

        return DeviceSyncStatusResponse.from(device);
    }

    @Override
    public List<DeviceSyncStatusResponse> getProjectDeviceStatuses(Long projectId) {
        log.info("[프로젝트 디바이스 상태 조회] 프로젝트 ID: {}", projectId);

        List<FaceRecognitionDevice> devices = deviceRepository.findByProjectId(projectId);

        return devices.stream()
                .map(DeviceSyncStatusResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleDeviceConnectionFailure(String deviceSerialNumber, Exception error) {
        log.error("[디바이스 연결 실패 처리] 디바이스: {}, 오류: {}", deviceSerialNumber, error.getMessage());

        FaceRecognitionDevice device = deviceRepository.findBySerialNumber(deviceSerialNumber)
                .orElseThrow(() -> new IllegalArgumentException("안면인식기를 찾을 수 없습니다: " + deviceSerialNumber));

        // 동기화 상태를 FAILED로 업데이트
        device.updateSyncStatus(FaceRecognitionDevice.DeviceSyncStatus.FAILED);
        deviceRepository.save(device);

        // TODO: 관리자에게 알림 전송
        log.info("[디바이스 동기화 상태 업데이트] 디바이스: {}, 상태: FAILED", deviceSerialNumber);
    }

    @Override
    @Transactional
    public void updateDeviceSyncStatus(String deviceSerialNumber, FaceRecognitionDevice.DeviceSyncStatus status) {
        log.info("[디바이스 동기화 상태 업데이트] 디바이스: {}, 상태: {}", deviceSerialNumber, status);

        FaceRecognitionDevice device = deviceRepository.findBySerialNumber(deviceSerialNumber)
                .orElseThrow(() -> new IllegalArgumentException("안면인식기를 찾을 수 없습니다: " + deviceSerialNumber));

        device.updateSyncStatus(status);
        deviceRepository.save(device);

        log.info("[디바이스 동기화 상태 업데이트 완료] 디바이스: {}, 상태: {}", deviceSerialNumber, status);
    }

    @Override
    public boolean validateFaceMatchConfidence(BigDecimal confidence) {
        if (confidence == null) {
            log.warn("[안면인식 신뢰도 검증 실패] 신뢰도 값이 null입니다");
            return false;
        }

        boolean isValid = confidence.compareTo(CONFIDENCE_THRESHOLD) >= 0;
        
        if (!isValid) {
            log.warn("[안면인식 신뢰도 검증 실패] 신뢰도: {}, 임계값: {}", confidence, CONFIDENCE_THRESHOLD);
        }

        return isValid;
    }
}
