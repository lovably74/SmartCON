package com.smartcon.domain.subscription.entity;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 구독 요금제 엔티티
 * 
 * 시스템에서 제공하는 구독 요금제 정보를 관리합니다.
 * Basic, Standard, Premium, Enterprise 등의 요금제를 정의합니다.
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionPlan extends BaseEntity {
    
    @Id
    @Column(name = "plan_id")
    private String planId; // basic, standard, premium, enterprise
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(name = "yearly_price")
    private BigDecimal yearlyPrice;
    
    @Column(name = "max_sites", nullable = false)
    private Integer maxSites;
    
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;
    
    @Column(name = "max_storage_gb")
    private Integer maxStorageGb;
    
    @ElementCollection
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Builder
    public SubscriptionPlan(String planId, String name, String description, 
                           BigDecimal monthlyPrice, BigDecimal yearlyPrice,
                           Integer maxSites, Integer maxUsers, Integer maxStorageGb,
                           List<String> features, Boolean isActive, Integer sortOrder) {
        this.planId = planId;
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.maxSites = maxSites;
        this.maxUsers = maxUsers;
        this.maxStorageGb = maxStorageGb;
        this.features = features != null ? features : new ArrayList<>();
        this.isActive = isActive != null ? isActive : true;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
    
    /**
     * 요금제 활성화/비활성화
     */
    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * 요금제 정보 업데이트
     */
    public void updatePlan(String name, String description, 
                          BigDecimal monthlyPrice, BigDecimal yearlyPrice,
                          Integer maxSites, Integer maxUsers, Integer maxStorageGb) {
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.maxSites = maxSites;
        this.maxUsers = maxUsers;
        this.maxStorageGb = maxStorageGb;
    }
    
    /**
     * 기능 목록 업데이트
     */
    public void updateFeatures(List<String> features) {
        this.features.clear();
        if (features != null) {
            this.features.addAll(features);
        }
    }
}