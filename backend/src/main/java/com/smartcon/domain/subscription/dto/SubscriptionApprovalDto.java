package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.ApprovalAction;
import com.smartcon.domain.subscription.entity.SubscriptionApproval;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 구독 승인 이력 DTO
 */
@Getter
@Builder
public class SubscriptionApprovalDto {
    
    private Long id;
    private Long subscriptionId;
    private Long adminId;
    private String adminName;
    private SubscriptionStatus fromStatus;
    private SubscriptionStatus toStatus;
    private String reason;
    private ApprovalAction action;
    private LocalDateTime processedAt;
    
    /**
     * Entity를 DTO로 변환
     */
    public static SubscriptionApprovalDto from(SubscriptionApproval approval) {
        return SubscriptionApprovalDto.builder()
                .id(approval.getId())
                .subscriptionId(approval.getSubscriptionId())
                .adminId(approval.getAdmin().getId())
                .adminName(approval.getAdmin().getName())
                .fromStatus(approval.getFromStatus())
                .toStatus(approval.getToStatus())
                .reason(approval.getReason())
                .action(approval.getAction())
                .processedAt(approval.getProcessedAt())
                .build();
    }
}