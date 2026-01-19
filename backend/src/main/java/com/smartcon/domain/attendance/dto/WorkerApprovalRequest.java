package com.smartcon.domain.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 신규 출역자 승인 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerApprovalRequest {

    @NotNull(message = "노무자 ID는 필수입니다")
    private Long workerId;

    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    private String faceEmbedding; // FaceNet 임베딩 데이터

    private String approvalNote; // 승인 메모

    @NotNull(message = "승인 여부는 필수입니다")
    private Boolean approved;

    private String rejectionReason; // 거부 사유 (승인 거부시)
}
