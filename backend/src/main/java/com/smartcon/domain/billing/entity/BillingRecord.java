package com.smartcon.domain.billing.entity;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 구독 결제 기록 엔티티
 * MariaDB 최적화 적용
 */
@Entity
@Table(name = "subscription_billing")
@Getter
@Setter
@NoArgsConstructor
public class BillingRecord extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId; // 테넌트 ID

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart; // 결제 기간 시작일

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd; // 결제 기간 종료일

    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate; // 결제일

    @Column(name = "subscription_plan", nullable = false, length = 50)
    private String subscriptionPlan; // 구독 플랜

    @Column(name = "plan_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal planAmount; // 플랜 금액

    @Column(name = "sites_count")
    private Integer sitesCount = 0; // 현장 수

    @Column(name = "users_count")
    private Integer usersCount = 0; // 사용자 수

    @Column(name = "storage_usage_gb", precision = 10, scale = 2)
    private BigDecimal storageUsageGb = BigDecimal.ZERO; // 저장공간 사용량(GB)

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount; // 기본 요금

    @Column(name = "usage_amount", precision = 10, scale = 2)
    private BigDecimal usageAmount = BigDecimal.ZERO; // 사용량 요금

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO; // 할인 금액

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO; // 부가세

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 총 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod; // 결제 방법

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING; // 결제 상태

    @Column(name = "pg_provider", length = 50)
    private String pgProvider; // PG사명

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId; // PG 거래 ID

    @Column(name = "pg_approval_number", length = 50)
    private String pgApprovalNumber; // PG 승인번호

    @Column(name = "payment_attempts")
    private Integer paymentAttempts = 0; // 결제 시도 횟수

    @Column(name = "last_payment_attempt_at")
    private LocalDateTime lastPaymentAttemptAt; // 마지막 결제 시도 일시

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt; // 결제 완료 일시

    @Column(name = "failure_reason", length = 500)
    private String failureReason; // 실패 사유

    /**
     * 결제 상태 열거형
     */
    public enum PaymentStatus {
        PENDING,    // 대기중
        SUCCESS,    // 성공
        FAILED,     // 실패
        CANCELLED,  // 취소
        REFUNDED    // 환불
    }

    /**
     * 결제 방법 열거형
     */
    public enum PaymentMethod {
        CARD,           // 신용카드
        BANK_TRANSFER,  // 계좌이체
        VIRTUAL_ACCOUNT // 가상계좌
    }

    /**
     * 결제 성공 여부 확인
     */
    public boolean isPaymentSuccessful() {
        return PaymentStatus.SUCCESS.equals(paymentStatus);
    }

    /**
     * 결제 실패 여부 확인
     */
    public boolean isPaymentFailed() {
        return PaymentStatus.FAILED.equals(paymentStatus);
    }

    /**
     * 결제 시도 횟수 증가
     */
    public void incrementPaymentAttempts() {
        this.paymentAttempts = (this.paymentAttempts == null ? 0 : this.paymentAttempts) + 1;
        this.lastPaymentAttemptAt = LocalDateTime.now();
    }

    /**
     * 결제 완료 처리
     */
    public void markAsCompleted(String transactionId, String approvalNumber) {
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.pgTransactionId = transactionId;
        this.pgApprovalNumber = approvalNumber;
        this.paymentCompletedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    /**
     * 결제 실패 처리
     */
    public void markAsFailed(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = reason;
        incrementPaymentAttempts();
    }
}