package com.smartcon.domain.tenant.entity;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 고객사(Tenant) 정보를 관리하는 엔티티
 * MariaDB 최적화 적용
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant extends BaseEntity {

    @Column(name = "business_number", unique = true, nullable = false, length = 12)
    private String businessNo; // 사업자번호

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName; // 회사명

    @Column(name = "ceo_name", length = 50)
    private String representativeName; // 대표자명

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL; // 활성, 중지, 해지

    @Column(name = "subscription_plan", length = 50)
    private String planId; // 구독 플랜 ID

    @Column(name = "max_sites")
    private Integer maxSites = 1; // 최대 현장 수

    @Column(name = "max_users")
    private Integer maxUsers = 10; // 최대 사용자 수

    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // 대표 전화

    @Column(name = "email", length = 100)
    private String email; // 대표 이메일

    @Column(name = "road_address", length = 200)
    private String roadAddress; // 도로명 주소

    @Column(name = "detail_address", length = 100)
    private String detailAddress; // 상세 주소

    @Column(name = "zip_code", length = 10)
    private String zipCode; // 우편번호

    @Column(name = "billing_email", length = 100)
    private String billingEmail; // 세금계산서 이메일

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod; // 결제 방법

    /**
     * 구독 상태 열거형
     */
    public enum SubscriptionStatus {
        TRIAL,      // 체험
        ACTIVE,     // 활성
        SUSPENDED,  // 일시정지
        TERMINATED  // 해지
    }

    /**
     * 결제 방법 열거형
     */
    public enum PaymentMethod {
        CARD,           // 카드
        BANK_TRANSFER   // 계좌이체
    }
}
