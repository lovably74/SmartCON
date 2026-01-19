package com.smartcon.domain.attendance.service;

import com.smartcon.domain.attendance.dto.WorkerApprovalRequest;
import com.smartcon.domain.attendance.dto.WorkerApprovalResponse;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.FaceRecognitionDeviceRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 신규 출역자 승인 서비스 구현체
 * 승인시 안면인식기 자동 연동 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerApprovalServiceImpl implements WorkerApprovalService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FaceRecognitionDeviceRepository deviceRepository;
    private final FaceRecognitionIntegrationService integrationService;

    @Override
    @Transactional
    public WorkerApprovalResponse approveWorker(WorkerApprovalRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[신규 출역자 승인] 테넌트: {}, 노무자 ID: {}, 프로젝트 ID: {}", 
                tenantId, request.getWorkerId(), request.getProjectId());

        // 노무자 조회
        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + request.getWorkerId()));

        // 프로젝트 조회
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + request.getProjectId()));

        // FaceNet 임베딩 데이터 저장
        if (request.getFaceEmbedding() != null && !request.getFaceEmbedding().trim().isEmpty()) {
            worker.setFaceEmbedding(request.getFaceEmbedding());
            userRepository.save(worker);
            log.info("[FaceNet 임베딩 저장] 노무자: {}", worker.getName());
        }

        // 프로젝트의 모든 활성 디바이스 조회
        List<FaceRecognitionDevice> devices = deviceRepository.findActiveDevicesByProjectId(request.getProjectId());
        
        int totalDeviceCount = devices.size();
        int syncedDeviceCount = 0;
        List<String> syncFailedDevices = new ArrayList<>();

        // 각 디바이스에 노무자 등록 (재시도 메커니즘 포함)
        for (FaceRecognitionDevice device : devices) {
            try {
                registerWorkerWithRetry(request.getWorkerId(), device.getSerialNumber(), 3);
                syncedDeviceCount++;
                log.info("[디바이스 등록 성공] 디바이스: {}, 노무자: {}", 
                        device.getSerialNumber(), worker.getName());
            } catch (Exception e) {
                log.error("[디바이스 등록 실패] 디바이스: {}, 노무자: {}, 오류: {}", 
                        device.getSerialNumber(), worker.getName(), e.getMessage());
                syncFailedDevices.add(device.getSerialNumber());
                
                // 디바이스 동기화 상태를 FAILED로 업데이트
                integrationService.handleDeviceConnectionFailure(device.getSerialNumber(), e);
            }
        }

        // 응답 생성
        WorkerApprovalResponse response = WorkerApprovalResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .projectId(project.getId())
                .projectName(project.getName())
                .approved(true)
                .approvalNote(request.getApprovalNote())
                .approvedAt(LocalDateTime.now())
                .syncedDeviceCount(syncedDeviceCount)
                .totalDeviceCount(totalDeviceCount)
                .syncFailedDevices(syncFailedDevices)
                .build();

        // 메시지 설정
        if (syncedDeviceCount == totalDeviceCount) {
            response.setMessage("신규 출역자가 승인되었으며, 모든 디바이스에 동기화되었습니다");
        } else if (syncedDeviceCount > 0) {
            response.setMessage(String.format("신규 출역자가 승인되었으나, %d개 디바이스 동기화에 실패했습니다", 
                    syncFailedDevices.size()));
        } else {
            response.setMessage("신규 출역자가 승인되었으나, 모든 디바이스 동기화에 실패했습니다");
        }

        log.info("[신규 출역자 승인 완료] 노무자: {}, 동기화: {}/{}", 
                worker.getName(), syncedDeviceCount, totalDeviceCount);

        return response;
    }

    @Override
    @Transactional
    public WorkerApprovalResponse rejectWorker(WorkerApprovalRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[신규 출역자 승인 거부] 테넌트: {}, 노무자 ID: {}, 프로젝트 ID: {}", 
                tenantId, request.getWorkerId(), request.getProjectId());

        // 노무자 조회
        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + request.getWorkerId()));

        // 프로젝트 조회
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + request.getProjectId()));

        // 응답 생성
        WorkerApprovalResponse response = WorkerApprovalResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .projectId(project.getId())
                .projectName(project.getName())
                .approved(false)
                .rejectionReason(request.getRejectionReason())
                .approvedAt(LocalDateTime.now())
                .message("신규 출역자 승인이 거부되었습니다")
                .build();

        log.info("[신규 출역자 승인 거부 완료] 노무자: {}, 사유: {}", 
                worker.getName(), request.getRejectionReason());

        return response;
    }

    @Override
    @Transactional
    public WorkerApprovalResponse retryApproval(Long workerId, Long projectId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[승인 재시도] 테넌트: {}, 노무자 ID: {}, 프로젝트 ID: {}", 
                tenantId, workerId, projectId);

        // 노무자 조회
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + workerId));

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        // 동기화 실패한 디바이스 조회
        List<FaceRecognitionDevice> failedDevices = deviceRepository.findByProjectId(projectId)
                .stream()
                .filter(d -> d.getSyncStatus() == FaceRecognitionDevice.DeviceSyncStatus.FAILED)
                .toList();

        int totalDeviceCount = failedDevices.size();
        int syncedDeviceCount = 0;
        List<String> syncFailedDevices = new ArrayList<>();

        // 실패한 디바이스에 대해 재시도
        for (FaceRecognitionDevice device : failedDevices) {
            try {
                registerWorkerWithRetry(workerId, device.getSerialNumber(), 3);
                syncedDeviceCount++;
                log.info("[재시도 성공] 디바이스: {}, 노무자: {}", 
                        device.getSerialNumber(), worker.getName());
            } catch (Exception e) {
                log.error("[재시도 실패] 디바이스: {}, 노무자: {}, 오류: {}", 
                        device.getSerialNumber(), worker.getName(), e.getMessage());
                syncFailedDevices.add(device.getSerialNumber());
            }
        }

        // 응답 생성
        WorkerApprovalResponse response = WorkerApprovalResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .projectId(project.getId())
                .projectName(project.getName())
                .approved(true)
                .approvedAt(LocalDateTime.now())
                .syncedDeviceCount(syncedDeviceCount)
                .totalDeviceCount(totalDeviceCount)
                .syncFailedDevices(syncFailedDevices)
                .build();

        // 메시지 설정
        if (syncedDeviceCount == totalDeviceCount) {
            response.setMessage("모든 디바이스 동기화에 성공했습니다");
        } else if (syncedDeviceCount > 0) {
            response.setMessage(String.format("%d개 디바이스 동기화에 성공했으나, %d개는 여전히 실패 상태입니다", 
                    syncedDeviceCount, syncFailedDevices.size()));
        } else {
            response.setMessage("모든 디바이스 동기화 재시도에 실패했습니다");
        }

        log.info("[승인 재시도 완료] 노무자: {}, 동기화: {}/{}", 
                worker.getName(), syncedDeviceCount, totalDeviceCount);

        return response;
    }

    /**
     * 재시도 메커니즘을 포함한 노무자 등록
     */
    private void registerWorkerWithRetry(Long workerId, String deviceSerialNumber, int maxRetries) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                integrationService.registerWorkerToDevice(workerId, deviceSerialNumber);
                return; // 성공시 즉시 반환
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                
                if (retryCount < maxRetries) {
                    log.warn("[디바이스 등록 재시도] 시도: {}/{}, 디바이스: {}, 오류: {}", 
                            retryCount, maxRetries, deviceSerialNumber, e.getMessage());
                    
                    // 재시도 전 대기 (지수 백오프)
                    try {
                        Thread.sleep(1000L * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                }
            }
        }

        // 모든 재시도 실패
        throw new RuntimeException("디바이스 등록 실패 (최대 재시도 횟수 초과): " + deviceSerialNumber, lastException);
    }
}
