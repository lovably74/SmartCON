package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 구독 요금제 DTO
 */
@Getter
@Builder
public class SubscriptionPlanDto {
    
    private String id;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxSites;
    private Integer maxUsers;
    private Integer maxStorageGb;
    private List<String> features;
    private Boolean isActive;
    private Integer sortOrder;
    
    /**
     * Entity를 DTO로 변환
     */
    public static SubscriptionPlanDto from(SubscriptionPlan plan) {
        return SubscriptionPlanDto.builder()
                .id(plan.getPlanId())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxSites(plan.getMaxSites())
                .maxUsers(plan.getMaxUsers())
                .maxStorageGb(plan.getMaxStorageGb())
                .features(plan.getFeatures())
                .isActive(plan.getIsActive())
                .sortOrder(plan.getSortOrder())
                .build();
    }
}