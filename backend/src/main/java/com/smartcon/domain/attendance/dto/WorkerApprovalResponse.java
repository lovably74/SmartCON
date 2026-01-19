package com.smartcon.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신규 출역자 승인 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerApprovalResponse {

    private Long workerId;
    private String workerName;
    private Long projectId;
    private String projectName;
    private Boolean approved;
    private String approvalNote;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private Integer syncedDeviceCount; // 동기화된 디바이스 수
    private Integer totalDeviceCount; // 전체 디바이스 수
    private List<String> syncFailedDevices; // 동기화 실패한 디바이스 목록
    private String message;
}
