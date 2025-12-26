package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 구독 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateSubscriptionRequest {
    
    @NotBlank(message = "요금제 ID는 필수입니다")
    private String planId;
    
    @NotNull(message = "결제 주기는 필수입니다")
    private BillingCycle billingCycle;
    
    private BigDecimal discountRate;
    
    private Boolean autoRenewal = true;
    
    private Boolean startTrial = false;
    
    private Integer trialDays = 14;
}