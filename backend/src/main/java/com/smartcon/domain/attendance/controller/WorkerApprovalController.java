package com.smartcon.domain.attendance.controller;

import com.smartcon.domain.attendance.dto.WorkerApprovalRequest;
import com.smartcon.domain.attendance.dto.WorkerApprovalResponse;
import com.smartcon.domain.attendance.service.WorkerApprovalService;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 신규 출역자 승인 컨트롤러
 * 승인시 안면인식기 자동 연동 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/worker-approval")
@RequiredArgsConstructor
public class WorkerApprovalController {

    private final WorkerApprovalService workerApprovalService;

    /**
     * 신규 출역자 승인
     * 승인시 안면인식기에 자동으로 정보 전달
     */
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<WorkerApprovalResponse>> approveWorker(
            @Valid @RequestBody WorkerApprovalRequest request) {
        log.info("[API] 신규 출역자 승인 요청: 노무자 ID={}, 프로젝트 ID={}", 
                request.getWorkerId(), request.getProjectId());

        WorkerApprovalResponse response = workerApprovalService.approveWorker(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * 신규 출역자 승인 거부
     */
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<WorkerApprovalResponse>> rejectWorker(
            @Valid @RequestBody WorkerApprovalRequest request) {
        log.info("[API] 신규 출역자 승인 거부 요청: 노무자 ID={}, 프로젝트 ID={}", 
                request.getWorkerId(), request.getProjectId());

        WorkerApprovalResponse response = workerApprovalService.rejectWorker(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * 승인 실패시 재시도
     */
    @PostMapping("/retry")
    public ResponseEntity<ApiResponse<WorkerApprovalResponse>> retryApproval(
            @RequestParam Long workerId,
            @RequestParam Long projectId) {
        log.info("[API] 승인 재시도 요청: 노무자 ID={}, 프로젝트 ID={}", workerId, projectId);

        WorkerApprovalResponse response = workerApprovalService.retryApproval(workerId, projectId);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }
}
