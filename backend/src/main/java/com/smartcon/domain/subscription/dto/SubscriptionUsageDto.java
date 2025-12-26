package com.smartcon.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 구독 사용량 모니터링 DTO
 */
@Getter
@Builder
public class SubscriptionUsageDto {
    
    private SubscriptionDto subscription;
    private Integer currentSites;
    private Integer currentUsers;
    private Integer currentStorageGb;
    private Integer usagePercentage;
    
    // 사용량 임계치 상태
    private Boolean isNearLimit; // 80% 이상
    private Boolean isOverLimit; // 100% 이상
    
    public Boolean getIsNearLimit() {
        return usagePercentage >= 80;
    }
    
    public Boolean getIsOverLimit() {
        return usagePercentage >= 100;
    }
}