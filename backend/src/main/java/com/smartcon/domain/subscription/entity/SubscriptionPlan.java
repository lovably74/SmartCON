package com.smartcon.domain.subscription.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class SubscriptionPlan {
    
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
    
    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 저장 전 처리
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 엔티티 업데이트 전 처리
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 요금제 활성화/비활성화
     */
    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 기능 목록 업데이트
     */
    public void updateFeatures(List<String> features) {
        this.features.clear();
        if (features != null) {
            this.features.addAll(features);
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 동등성 비교 (planId 기반)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SubscriptionPlan that = (SubscriptionPlan) obj;
        return planId != null && planId.equals(that.planId);
    }

    /**
     * 해시코드 생성 (planId 기반)
     */
    @Override
    public int hashCode() {
        return planId != null ? planId.hashCode() : 0;
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("SubscriptionPlan{planId='%s', name='%s', monthlyPrice=%s}", 
                planId, name, monthlyPrice);
    }
}