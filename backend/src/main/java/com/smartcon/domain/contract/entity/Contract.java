package com.smartcon.domain.contract.entity;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.user.entity.User;
import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 근로계약 엔티티
 * 노무자와 현장 간의 근로계약 정보를 관리
 */
@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends BaseTenantEntity {

    /**
     * 노무자 (계약 대상)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    /**
     * 프로젝트 (현장)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 근무일자
     */
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    /**
     * 공종 (직종)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private AttendanceRecord.JobType jobType;

    /**
     * 단가 (일당)
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * 계약 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.PENDING;

    /**
     * 계약서 파일 URL (S3)
     */
    @Column(name = "contract_file_url", length = 500)
    private String contractFileUrl;

    /**
     * 전자서명 데이터 (Base64 인코딩)
     */
    @Lob
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData;

    /**
     * 서명 완료 일시
     */
    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    /**
     * 계약 수정 요청 사항 (노무자 메모)
     */
    @Column(name = "modification_request", columnDefinition = "TEXT")
    private String modificationRequest;

    /**
     * 계약 만료일
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * 계약 생성자 (현장관리자 또는 본사관리자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    /**
     * 계약 상태 열거형
     */
    public enum ContractStatus {
        PENDING("서명대기", "계약서가 생성되어 노무자의 서명을 기다리는 상태"),
        SIGNED("서명완료", "노무자가 전자서명을 완료한 상태"),
        EXPIRED("만료", "계약 유효기간이 지난 상태"),
        CANCELLED("취소", "계약이 취소된 상태");

        private final String displayName;
        private final String description;

        ContractStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 서명 가능한 상태인지 확인
         */
        public boolean isSignable() {
            return this == PENDING;
        }

        /**
         * 수정 요청 가능한 상태인지 확인
         */
        public boolean isModifiable() {
            return this == PENDING;
        }
    }

    /**
     * 계약서 서명 처리
     */
    public void sign(String signatureData) {
        if (!this.status.isSignable()) {
            throw new IllegalStateException(
                String.format("서명 불가능한 상태입니다: %s", this.status.getDisplayName())
            );
        }

        this.signatureData = signatureData;
        this.signedAt = LocalDateTime.now();
        this.status = ContractStatus.SIGNED;
    }

    /**
     * 계약 수정 요청
     */
    public void requestModification(String modificationRequest) {
        if (!this.status.isModifiable()) {
            throw new IllegalStateException(
                String.format("수정 요청 불가능한 상태입니다: %s", this.status.getDisplayName())
            );
        }

        this.modificationRequest = modificationRequest;
    }

    /**
     * 계약 취소
     */
    public void cancel() {
        if (this.status == ContractStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 계약입니다");
        }

        this.status = ContractStatus.CANCELLED;
    }

    /**
     * 계약 만료 처리
     */
    public void expire() {
        if (this.status == ContractStatus.EXPIRED) {
            throw new IllegalStateException("이미 만료된 계약입니다");
        }

        this.status = ContractStatus.EXPIRED;
    }

    /**
     * 계약이 유효한지 확인
     */
    public boolean isValid() {
        return this.status == ContractStatus.SIGNED && 
               (this.expiryDate == null || !LocalDate.now().isAfter(this.expiryDate));
    }

    /**
     * 계약이 만료되었는지 확인
     */
    public boolean isExpired() {
        return this.expiryDate != null && LocalDate.now().isAfter(this.expiryDate);
    }

    /**
     * 서명 완료 여부 확인
     */
    public boolean isSigned() {
        return this.status == ContractStatus.SIGNED;
    }

    /**
     * 서명 대기 중인지 확인
     */
    public boolean isPending() {
        return this.status == ContractStatus.PENDING;
    }

    /**
     * 계약서 파일이 존재하는지 확인
     */
    public boolean hasContractFile() {
        return this.contractFileUrl != null && !this.contractFileUrl.trim().isEmpty();
    }

    /**
     * 전자서명이 존재하는지 확인
     */
    public boolean hasSignature() {
        return this.signatureData != null && !this.signatureData.trim().isEmpty();
    }

    /**
     * 수정 요청이 있는지 확인
     */
    public boolean hasModificationRequest() {
        return this.modificationRequest != null && !this.modificationRequest.trim().isEmpty();
    }
}
