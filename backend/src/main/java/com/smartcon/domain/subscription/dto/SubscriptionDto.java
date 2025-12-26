package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.BillingCycle;
import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 구독 정보 DTO
 */
@Getter
@Builder
public class SubscriptionDto {
    
    private Long id;
    private Long tenantId;
    private String tenantName;
    private SubscriptionPlanDto plan;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextBillingDate;
    private BillingCycle billingCycle;
    private BigDecimal monthlyPrice;
    private BigDecimal discountRate;
    private Boolean autoRenewal;
    private LocalDate trialEndDate;
    private LocalDate cancellationDate;
    private String cancellationReason;
    private Boolean isTrial;
    private Boolean isExpired;
    
    /**
     * Entity를 DTO로 변환
     */
    public static SubscriptionDto from(Subscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .tenantId(subscription.getTenant().getId())
                .tenantName(subscription.getTenant().getCompanyName())
                .plan(SubscriptionPlanDto.from(subscription.getPlan()))
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .billingCycle(subscription.getBillingCycle())
                .monthlyPrice(subscription.getMonthlyPrice())
                .discountRate(subscription.getDiscountRate())
                .autoRenewal(subscription.getAutoRenewal())
                .trialEndDate(subscription.getTrialEndDate())
                .cancellationDate(subscription.getCancellationDate())
                .cancellationReason(subscription.getCancellationReason())
                .isTrial(subscription.isTrial())
                .isExpired(subscription.isExpired())
                .build();
    }
}