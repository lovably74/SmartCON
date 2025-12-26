package com.smartcon.domain.subscription.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 자동 승인 규칙 DTO
 */
@Data
@Builder
public class AutoApprovalRuleDto {
    
    private Long id;
    private String ruleName;
    private Boolean isActive;
    private List<String> planIds;
    private Boolean verifiedTenantsOnly;
    private List<String> paymentMethods;
    private BigDecimal maxAmount;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 규칙 설명 생성
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        
        if (planIds != null && !planIds.isEmpty()) {
            desc.append("요금제: ").append(planIds).append(", ");
        }
        
        if (verifiedTenantsOnly != null && verifiedTenantsOnly) {
            desc.append("검증된 테넌트만, ");
        }
        
        if (paymentMethods != null && !paymentMethods.isEmpty()) {
            desc.append("결제 방법: ").append(paymentMethods).append(", ");
        }
        
        if (maxAmount != null) {
            desc.append("최대 금액: ").append(maxAmount).append("원, ");
        }
        
        if (desc.length() > 0) {
            desc.setLength(desc.length() - 2); // 마지막 ", " 제거
        }
        
        return desc.toString();
    }
}