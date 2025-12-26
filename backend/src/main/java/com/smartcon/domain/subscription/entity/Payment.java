package com.smartcon.domain.subscription.entity;

import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 결제 내역 엔티티
 * 
 * 구독 결제 내역을 관리합니다.
 * 결제 성공/실패, 환불 등의 상태를 추적합니다.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_key", unique = true)
    private String paymentKey; // PG사 결제 키
    
    @Column(name = "order_id", unique = true)
    private String orderId;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount")
    private BigDecimal taxAmount;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;
    
    @Column(name = "billing_period_end")
    private LocalDate billingPeriodEnd;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "failed_reason")
    private String failedReason;
    
    @Column(name = "pg_response", columnDefinition = "TEXT")
    private String pgResponse; // PG사 응답 JSON
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @Column(name = "tax_invoice_issued")
    private Boolean taxInvoiceIssued = false;
    
    @Builder
    public Payment(Tenant tenant, Subscription subscription, PaymentMethod paymentMethod,
                  String paymentKey, String orderId, BigDecimal amount, BigDecimal discountAmount,
                  BigDecimal taxAmount, PaymentStatus status, LocalDate billingPeriodStart,
                  LocalDate billingPeriodEnd, String invoiceNumber) {
        this.tenant = tenant;
        this.subscription = subscription;
        this.paymentMethod = paymentMethod;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.taxAmount = taxAmount;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.invoiceNumber = invoiceNumber;
        this.taxInvoiceIssued = false;
    }
    
    /**
     * 결제 성공 처리
     */
    public void markAsSuccess(LocalDateTime paidAt, String pgResponse) {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = paidAt;
        this.pgResponse = pgResponse;
    }
    
    /**
     * 결제 실패 처리
     */
    public void markAsFailed(String failedReason, String pgResponse) {
        this.status = PaymentStatus.FAILED;
        this.failedReason = failedReason;
        this.pgResponse = pgResponse;
    }
    
    /**
     * 결제 취소 처리
     */
    public void markAsCancelled(String reason) {
        this.status = PaymentStatus.CANCELLED;
        this.failedReason = reason;
    }
    
    /**
     * 환불 처리
     */
    public void markAsRefunded(String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.failedReason = reason;
    }
    
    /**
     * 세금계산서 발행 완료 처리
     */
    public void markTaxInvoiceIssued() {
        this.taxInvoiceIssued = true;
    }
    
    /**
     * PG 응답 업데이트
     */
    public void updatePgResponse(String pgResponse) {
        this.pgResponse = pgResponse;
    }
    
    /**
     * 실제 결제 금액 계산 (할인 적용 후)
     */
    public BigDecimal getActualAmount() {
        return amount.subtract(discountAmount);
    }
    
    /**
     * 결제 성공 여부 확인
     */
    public boolean isSuccessful() {
        return PaymentStatus.SUCCESS.equals(this.status);
    }
    
    /**
     * 결제 실패 여부 확인
     */
    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status);
    }
}