package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.Payment;
import com.smartcon.domain.subscription.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 결제 내역 DTO
 */
@Getter
@Builder
public class PaymentDto {
    
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long subscriptionId;
    private String planName;
    private PaymentMethodDto paymentMethod;
    private String paymentKey;
    private String orderId;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private PaymentStatus status;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private LocalDateTime paidAt;
    private String failedReason;
    private String invoiceNumber;
    private Boolean taxInvoiceIssued;
    private BigDecimal actualAmount;
    
    /**
     * Entity를 DTO로 변환
     */
    public static PaymentDto from(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .tenantId(payment.getTenant().getId())
                .tenantName(payment.getTenant().getCompanyName())
                .subscriptionId(payment.getSubscription().getId())
                .planName(payment.getSubscription().getPlan().getName())
                .paymentMethod(payment.getPaymentMethod() != null ? 
                    PaymentMethodDto.from(payment.getPaymentMethod()) : null)
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .discountAmount(payment.getDiscountAmount())
                .taxAmount(payment.getTaxAmount())
                .status(payment.getStatus())
                .billingPeriodStart(payment.getBillingPeriodStart())
                .billingPeriodEnd(payment.getBillingPeriodEnd())
                .paidAt(payment.getPaidAt())
                .failedReason(payment.getFailedReason())
                .invoiceNumber(payment.getInvoiceNumber())
                .taxInvoiceIssued(payment.getTaxInvoiceIssued())
                .actualAmount(payment.getActualAmount())
                .build();
    }
}