package com.smartcon.domain.subscription.entity;

import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 수단 엔티티
 * 
 * 테넌트의 결제 수단 정보를 관리합니다.
 * 신용카드, 계좌이체 등의 결제 방법을 지원합니다.
 */
@Entity
@Table(name = "payment_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @Enumerated(EnumType.STRING)
    private PaymentType type;
    
    @Column(name = "card_number_masked")
    private String cardNumberMasked; // 1234-****-****-5678
    
    @Column(name = "card_holder_name")
    private String cardHolderName;
    
    @Column(name = "expiry_date")
    private String expiryDate; // MM/YY
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_number_masked")
    private String accountNumberMasked;
    
    @Column(name = "account_holder_name")
    private String accountHolderName;
    
    @Column(name = "billing_key")
    private String billingKey; // PG사 자동결제 키
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Builder
    public PaymentMethod(Tenant tenant, PaymentType type, String cardNumberMasked,
                        String cardHolderName, String expiryDate, String bankName,
                        String accountNumberMasked, String accountHolderName,
                        String billingKey, Boolean isDefault, Boolean isActive) {
        this.tenant = tenant;
        this.type = type;
        this.cardNumberMasked = cardNumberMasked;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.bankName = bankName;
        this.accountNumberMasked = accountNumberMasked;
        this.accountHolderName = accountHolderName;
        this.billingKey = billingKey;
        this.isDefault = isDefault != null ? isDefault : false;
        this.isActive = isActive != null ? isActive : true;
    }
    
    /**
     * 기본 결제 수단으로 설정
     */
    public void setAsDefault() {
        this.isDefault = true;
    }
    
    /**
     * 기본 결제 수단 해제
     */
    public void unsetDefault() {
        this.isDefault = false;
    }
    
    /**
     * 결제 수단 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.isDefault = false;
    }
    
    /**
     * 결제 수단 활성화
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * 마지막 사용 시간 업데이트
     */
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }
    
    /**
     * 카드 정보 업데이트
     */
    public void updateCardInfo(String cardNumberMasked, String cardHolderName, String expiryDate) {
        this.cardNumberMasked = cardNumberMasked;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
    }
    
    /**
     * 계좌 정보 업데이트
     */
    public void updateAccountInfo(String bankName, String accountNumberMasked, String accountHolderName) {
        this.bankName = bankName;
        this.accountNumberMasked = accountNumberMasked;
        this.accountHolderName = accountHolderName;
    }
    
    /**
     * 빌링키 업데이트
     */
    public void updateBillingKey(String billingKey) {
        this.billingKey = billingKey;
    }
}