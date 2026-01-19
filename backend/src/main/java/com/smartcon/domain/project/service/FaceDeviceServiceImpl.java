package com.smartcon.domain.project.service;

import com.smartcon.domain.project.dto.FaceDeviceRequest;
import com.smartcon.domain.project.dto.FaceDeviceResponse;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.FaceRecognitionDeviceRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 안면인식기 디바이스 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaceDeviceServiceImpl implements FaceDeviceService {

    private final FaceRecognitionDeviceRepository deviceRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public FaceDeviceResponse registerDevice(FaceDeviceRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[안면인식기 등록] 테넌트: {}, 시리얼: {}, 프로젝트 ID: {}", 
                tenantId, request.getSerialNumber(), request.getProjectId());

        // 프로젝트 조회
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + request.getProjectId()));

        // 시리얼 번호 중복 확인
        if (deviceRepository.findBySerialNumber(request.getSerialNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 시리얼 번호입니다: " + request.getSerialNumber());
        }

        // 디바이스 생성
        FaceRecognitionDevice device = FaceRecognitionDevice.builder()
                .project(project)
                .serialNumber(request.getSerialNumber())
                .deviceName(request.getDeviceName())
                .apiEndpoint(request.getApiEndpoint())
                .apiKey(request.getApiKey())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .location(request.getLocation())
                .firmwareVersion(request.getFirmwareVersion())
                .syncStatus(FaceRecognitionDevice.DeviceSyncStatus.PENDING)
                .build();

        FaceRecognitionDevice savedDevice = deviceRepository.save(device);
        log.info("[안면인식기 등록 완료] ID: {}, 시리얼: {}", savedDevice.getId(), savedDevice.getSerialNumber());

        return FaceDeviceResponse.from(savedDevice);
    }

    @Override
    public List<FaceDeviceResponse> getProjectDevices(Long projectId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[프로젝트 디바이스 목록 조회] 테넌트: {}, 프로젝트 ID: {}", tenantId, projectId);

        List<FaceRecognitionDevice> devices = deviceRepository.findByProjectId(projectId);

        return devices.stream()
                .map(FaceDeviceResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public FaceDeviceResponse getDeviceById(Long deviceId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[디바이스 상세 조회] 테넌트: {}, 디바이스 ID: {}", tenantId, deviceId);

        FaceRecognitionDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다: " + deviceId));

        return FaceDeviceResponse.from(device);
    }

    @Override
    @Transactional
    public FaceDeviceResponse updateDevice(Long deviceId, FaceDeviceRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[디바이스 정보 수정] 테넌트: {}, 디바이스 ID: {}", tenantId, deviceId);

        FaceRecognitionDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다: " + deviceId));

        // 정보 업데이트
        if (request.getDeviceName() != null) {
            device.setDeviceName(request.getDeviceName());
        }
        if (request.getApiEndpoint() != null) {
            device.setApiEndpoint(request.getApiEndpoint());
        }
        if (request.getApiKey() != null) {
            device.setApiKey(request.getApiKey());
        }
        if (request.getIsActive() != null) {
            device.setIsActive(request.getIsActive());
        }
        if (request.getLocation() != null) {
            device.setLocation(request.getLocation());
        }
        if (request.getFirmwareVersion() != null) {
            device.setFirmwareVersion(request.getFirmwareVersion());
        }

        FaceRecognitionDevice savedDevice = deviceRepository.save(device);
        log.info("[디바이스 정보 수정 완료] ID: {}", savedDevice.getId());

        return FaceDeviceResponse.from(savedDevice);
    }

    @Override
    @Transactional
    public void deleteDevice(Long deviceId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[디바이스 삭제] 테넌트: {}, 디바이스 ID: {}", tenantId, deviceId);

        FaceRecognitionDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다: " + deviceId));

        deviceRepository.delete(device);
        log.info("[디바이스 삭제 완료] ID: {}", deviceId);
    }

    @Override
    public List<FaceDeviceResponse> getActiveDevices() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[활성 디바이스 목록 조회] 테넌트: {}", tenantId);

        List<FaceRecognitionDevice> devices = deviceRepository.findActiveDevices();

        return devices.stream()
                .map(FaceDeviceResponse::from)
                .collect(Collectors.toList());
    }
}
