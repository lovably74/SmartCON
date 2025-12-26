package com.smartcon.domain.subscription.entity;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 자동 승인 규칙 엔티티
 * 
 * 구독 신청 시 자동 승인을 위한 규칙을 관리합니다.
 * 요금제, 결제 방법, 테넌트 검증 상태 등을 기준으로 자동 승인 여부를 결정합니다.
 */
@Entity
@Table(name = "auto_approval_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoApprovalRule extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "plan_ids", columnDefinition = "JSON")
    private String planIds; // JSON 배열 형태로 저장
    
    @Column(name = "verified_tenants_only", nullable = false)
    private Boolean verifiedTenantsOnly = false;
    
    @Column(name = "payment_methods", columnDefinition = "JSON")
    private String paymentMethods; // JSON 배열 형태로 저장
    
    @Column(name = "max_amount", precision = 10, scale = 2)
    private BigDecimal maxAmount;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
    
    @Builder
    public AutoApprovalRule(String ruleName, Boolean isActive, String planIds,
                           Boolean verifiedTenantsOnly, String paymentMethods,
                           BigDecimal maxAmount, Integer priority) {
        this.ruleName = ruleName;
        this.isActive = isActive != null ? isActive : true;
        this.planIds = planIds;
        this.verifiedTenantsOnly = verifiedTenantsOnly != null ? verifiedTenantsOnly : false;
        this.paymentMethods = paymentMethods;
        this.maxAmount = maxAmount;
        this.priority = priority != null ? priority : 0;
    }
    
    /**
     * 규칙 활성화/비활성화
     */
    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * 규칙 우선순위 변경
     */
    public void updatePriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * 규칙 내용 업데이트
     */
    public void updateRule(String ruleName, String planIds, Boolean verifiedTenantsOnly,
                          String paymentMethods, BigDecimal maxAmount) {
        this.ruleName = ruleName;
        this.planIds = planIds;
        this.verifiedTenantsOnly = verifiedTenantsOnly;
        this.paymentMethods = paymentMethods;
        this.maxAmount = maxAmount;
    }
}