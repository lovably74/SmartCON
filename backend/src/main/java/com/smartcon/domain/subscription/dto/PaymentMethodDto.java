package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.PaymentMethod;
import com.smartcon.domain.subscription.entity.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 수단 DTO
 */
@Getter
@Builder
public class PaymentMethodDto {
    
    private Long id;
    private PaymentType type;
    private String cardNumberMasked;
    private String cardHolderName;
    private String expiryDate;
    private String bankName;
    private String accountNumberMasked;
    private String accountHolderName;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime lastUsedAt;
    
    /**
     * Entity를 DTO로 변환
     */
    public static PaymentMethodDto from(PaymentMethod paymentMethod) {
        return PaymentMethodDto.builder()
                .id(paymentMethod.getId())
                .type(paymentMethod.getType())
                .cardNumberMasked(paymentMethod.getCardNumberMasked())
                .cardHolderName(paymentMethod.getCardHolderName())
                .expiryDate(paymentMethod.getExpiryDate())
                .bankName(paymentMethod.getBankName())
                .accountNumberMasked(paymentMethod.getAccountNumberMasked())
                .accountHolderName(paymentMethod.getAccountHolderName())
                .isDefault(paymentMethod.getIsDefault())
                .isActive(paymentMethod.getIsActive())
                .lastUsedAt(paymentMethod.getLastUsedAt())
                .build();
    }
}