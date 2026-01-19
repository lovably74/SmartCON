package com.smartcon.domain.contract.dto;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.contract.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 계약 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {

    /**
     * 계약 ID
     */
    private Long id;

    /**
     * 노무자 ID
     */
    private Long workerId;

    /**
     * 노무자 이름
     */
    private String workerName;

    /**
     * 프로젝트 ID
     */
    private Long projectId;

    /**
     * 프로젝트 이름
     */
    private String projectName;

    /**
     * 근무일자
     */
    private LocalDate workDate;

    /**
     * 공종 (직종)
     */
    private AttendanceRecord.JobType jobType;

    /**
     * 공종 표시명
     */
    private String jobTypeDisplayName;

    /**
     * 단가 (일당)
     */
    private BigDecimal unitPrice;

    /**
     * 계약 상태
     */
    private Contract.ContractStatus status;

    /**
     * 계약 상태 표시명
     */
    private String statusDisplayName;

    /**
     * 계약서 파일 URL
     */
    private String contractFileUrl;

    /**
     * 서명 완료 여부
     */
    private Boolean isSigned;

    /**
     * 서명 완료 일시
     */
    private LocalDateTime signedAt;

    /**
     * 계약 만료일
     */
    private LocalDate expiryDate;

    /**
     * 수정 요청 사항
     */
    private String modificationRequest;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * 계약 생성자 ID
     */
    private Long createdByUserId;

    /**
     * 계약 생성자 이름
     */
    private String createdByUserName;

    /**
     * Contract 엔티티로부터 DTO 생성
     */
    public static ContractResponse from(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .workerId(contract.getWorker().getId())
                .workerName(contract.getWorker().getName())
                .projectId(contract.getProject().getId())
                .projectName(contract.getProject().getName())
                .workDate(contract.getWorkDate())
                .jobType(contract.getJobType())
                .jobTypeDisplayName(contract.getJobType().getDisplayName())
                .unitPrice(contract.getUnitPrice())
                .status(contract.getStatus())
                .statusDisplayName(contract.getStatus().getDisplayName())
                .contractFileUrl(contract.getContractFileUrl())
                .isSigned(contract.isSigned())
                .signedAt(contract.getSignedAt())
                .expiryDate(contract.getExpiryDate())
                .modificationRequest(contract.getModificationRequest())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .createdByUserId(contract.getCreatedBy() != null ? contract.getCreatedBy().getId() : null)
                .createdByUserName(contract.getCreatedBy() != null ? contract.getCreatedBy().getName() : null)
                .build();
    }
}
