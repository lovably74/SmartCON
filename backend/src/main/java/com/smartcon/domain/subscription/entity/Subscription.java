package com.smartcon.domain.subscription.entity;

import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.user.entity.User;
import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 구독 엔티티
 * 
 * 테넌트의 구독 정보를 관리합니다.
 * 구독 상태, 결제 주기, 요금 등을 포함합니다.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "plan_id", nullable = false)
    private SubscriptionPlan plan;
    
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;
    
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;
    
    @Column(name = "monthly_price")
    private BigDecimal monthlyPrice;
    
    @Column(name = "discount_rate")
    private BigDecimal discountRate = BigDecimal.ZERO;
    
    @Column(name = "auto_renewal")
    private Boolean autoRenewal = true;
    
    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;
    
    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    // 승인 관련 필드 추가
    @Column(name = "approval_requested_at")
    private LocalDateTime approvalRequestedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "suspension_reason", columnDefinition = "TEXT")
    private String suspensionReason;
    
    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;
    
    // 낙관적 잠금을 위한 버전 필드
    @Version
    private Long version;
    
    @Builder
    public Subscription(Tenant tenant, SubscriptionPlan plan, SubscriptionStatus status,
                       LocalDate startDate, LocalDate endDate, LocalDate nextBillingDate,
                       BillingCycle billingCycle, BigDecimal monthlyPrice, BigDecimal discountRate,
                       Boolean autoRenewal, LocalDate trialEndDate) {
        this.tenant = tenant;
        this.plan = plan;
        this.status = status != null ? status : SubscriptionStatus.PENDING_APPROVAL;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextBillingDate = nextBillingDate;
        this.billingCycle = billingCycle != null ? billingCycle : BillingCycle.MONTHLY;
        this.monthlyPrice = monthlyPrice;
        this.discountRate = discountRate != null ? discountRate : BigDecimal.ZERO;
        this.autoRenewal = autoRenewal != null ? autoRenewal : true;
        this.trialEndDate = trialEndDate;
        this.approvalRequestedAt = LocalDateTime.now();
    }
    
    /**
     * 구독 상태 변경
     */
    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
    }
    
    /**
     * 요금제 변경
     */
    public void changePlan(SubscriptionPlan newPlan, BigDecimal newPrice) {
        this.plan = newPlan;
        this.monthlyPrice = newPrice;
    }
    
    /**
     * 다음 결제일 업데이트
     */
    public void updateNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
    
    /**
     * 구독 해지
     */
    public void cancel(LocalDate cancellationDate, String reason) {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancellationDate = cancellationDate;
        this.cancellationReason = reason;
        this.autoRenewal = false;
    }
    
    /**
     * 구독 일시 중지
     */
    public void suspend(String reason) {
        this.status = SubscriptionStatus.SUSPENDED;
        this.suspensionReason = reason;
    }
    
    /**
     * 구독 재개
     */
    public void resume() {
        this.status = SubscriptionStatus.ACTIVE;
        this.suspensionReason = null;
    }
    
    /**
     * 구독 승인
     */
    public void approve(User approver) {
        this.status = SubscriptionStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approver;
        this.rejectionReason = null;
    }
    
    /**
     * 구독 거부
     */
    public void reject(String reason) {
        this.status = SubscriptionStatus.REJECTED;
        this.rejectionReason = reason;
    }
    
    /**
     * 구독 종료
     */
    public void terminate(String reason) {
        this.status = SubscriptionStatus.TERMINATED;
        this.terminationReason = reason;
        this.autoRenewal = false;
    }
    
    /**
     * 자동 승인
     */
    public void autoApprove() {
        this.status = SubscriptionStatus.AUTO_APPROVED;
        this.approvedAt = LocalDateTime.now();
    }
    
    /**
     * 승인 요청 시간 업데이트
     */
    public void updateApprovalRequestedAt(LocalDateTime approvalRequestedAt) {
        this.approvalRequestedAt = approvalRequestedAt;
    }
    
    /**
     * 체험판 종료일 설정
     */
    public void setTrialEndDate(LocalDate trialEndDate) {
        this.trialEndDate = trialEndDate;
    }
    
    /**
     * 자동 갱신 설정 변경
     */
    public void updateAutoRenewal(boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }
    
    /**
     * 할인율 적용
     */
    public void applyDiscount(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }
    
    /**
     * 체험판 여부 확인
     */
    public boolean isTrial() {
        return trialEndDate != null && LocalDate.now().isBefore(trialEndDate);
    }
    
    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return endDate != null && LocalDate.now().isAfter(endDate);
    }
    
    /**
     * 승인 대기 여부 확인
     */
    public boolean isPendingApproval() {
        return status == SubscriptionStatus.PENDING_APPROVAL;
    }
    
    /**
     * 활성 상태 여부 확인 (ACTIVE 또는 AUTO_APPROVED)
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.AUTO_APPROVED;
    }
}