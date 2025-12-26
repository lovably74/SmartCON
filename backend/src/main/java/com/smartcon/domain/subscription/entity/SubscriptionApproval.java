package com.smartcon.domain.subscription.entity;

import com.smartcon.domain.user.entity.User;
import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구독 승인 이력 엔티티
 * 
 * 구독 상태 변경에 대한 승인 이력을 관리합니다.
 * 슈퍼관리자의 승인, 거부, 중지, 종료 등의 액션을 기록합니다.
 */
@Entity
@Table(name = "subscription_approvals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionApproval extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false)
    private SubscriptionStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private SubscriptionStatus toStatus;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ApprovalAction action;
    
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
    
    @Builder
    public SubscriptionApproval(Long subscriptionId, User admin, SubscriptionStatus fromStatus,
                               SubscriptionStatus toStatus, String reason, ApprovalAction action,
                               LocalDateTime processedAt) {
        this.subscriptionId = subscriptionId;
        this.admin = admin;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.action = action;
        this.processedAt = processedAt != null ? processedAt : LocalDateTime.now();
    }
}