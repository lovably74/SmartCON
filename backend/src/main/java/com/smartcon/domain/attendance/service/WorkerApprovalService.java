package com.smartcon.domain.attendance.service;

import com.smartcon.domain.attendance.dto.WorkerApprovalRequest;
import com.smartcon.domain.attendance.dto.WorkerApprovalResponse;

/**
 * 신규 출역자 승인 서비스 인터페이스
 */
public interface WorkerApprovalService {

    /**
     * 신규 출역자 승인 처리
     * 승인시 안면인식기에 자동으로 정보 전달
     */
    WorkerApprovalResponse approveWorker(WorkerApprovalRequest request);

    /**
     * 신규 출역자 승인 거부
     */
    WorkerApprovalResponse rejectWorker(WorkerApprovalRequest request);

    /**
     * 승인 실패시 재시도
     */
    WorkerApprovalResponse retryApproval(Long workerId, Long projectId);
}
