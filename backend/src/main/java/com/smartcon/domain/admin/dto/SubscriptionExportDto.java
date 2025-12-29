package com.smartcon.domain.admin.dto;

import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 구독 데이터 내보내기용 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionExportDto {
    
    private Long subscriptionId;
    private String tenantName;
    private String planName;
    private SubscriptionStatus status;
    private BigDecimal monthlyFee;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime lastPaymentAt;
    private String approvalReason;
    private String adminName;
    private boolean autoApproved;
    
    /**
     * 자동 승인 여부 확인
     */
    public boolean isAutoApproved() {
        return autoApproved;
    }
}