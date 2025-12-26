package com.smartcon.domain.subscription.entity;

import com.smartcon.domain.user.entity.User;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 승인 이력 기록 Property-Based Test
 * 
 * Feature: subscription-approval-workflow
 * Property 19: Audit Log Creation
 */
class ApprovalHistoryPropertyTest {
    
    /**
     * Property 19: Audit Log Creation
     * For any subscription status change, an audit log entry should be created with timestamp, admin ID, old status, new status, and reason
     * Validates: Requirements 5.1
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 19: Audit Log Creation")
    void auditLogCreation(
            @ForAll("validSubscriptionIds") Long subscriptionId,
            @ForAll("validStatusTransitions") StatusTransition transition,
            @ForAll("validReasons") String reason,
            @ForAll("validApprovalActions") ApprovalAction action) {
        
        // Given - 테스트용 관리자 생성
        User admin = createTestAdmin();
        
        // When - 승인 이력 생성
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(transition.fromStatus())
                .toStatus(transition.toStatus())
                .reason(reason)
                .action(action)
                .processedAt(LocalDateTime.now())
                .build();
        
        // Then - 모든 필수 정보가 기록되어야 함
        assertThat(approval.getSubscriptionId())
                .as("구독 ID가 기록되어야 함")
                .isEqualTo(subscriptionId);
        
        assertThat(approval.getAdmin())
                .as("관리자 정보가 기록되어야 함")
                .isNotNull()
                .isEqualTo(admin);
        
        assertThat(approval.getFromStatus())
                .as("이전 상태가 기록되어야 함")
                .isEqualTo(transition.fromStatus());
        
        assertThat(approval.getToStatus())
                .as("변경된 상태가 기록되어야 함")
                .isEqualTo(transition.toStatus());
        
        assertThat(approval.getReason())
                .as("변경 사유가 기록되어야 함")
                .isEqualTo(reason);
        
        assertThat(approval.getAction())
                .as("수행된 액션이 기록되어야 함")
                .isEqualTo(action);
        
        assertThat(approval.getProcessedAt())
                .as("처리 시간이 기록되어야 함")
                .isNotNull()
                .isBefore(LocalDateTime.now().plusSeconds(1)); // 약간의 여유를 둠
    }
    
    /**
     * 유효한 구독 ID 생성기
     */
    @Provide
    Arbitrary<Long> validSubscriptionIds() {
        return Arbitraries.longs().between(1L, 1000000L);
    }
    
    /**
     * 유효한 상태 전환 생성기
     */
    @Provide
    Arbitrary<StatusTransition> validStatusTransitions() {
        return Combinators.combine(
                Arbitraries.of(SubscriptionStatus.values()),
                Arbitraries.of(SubscriptionStatus.values())
        ).as(StatusTransition::new)
        .filter(transition -> !transition.fromStatus().equals(transition.toStatus())); // 같은 상태로의 전환은 제외
    }
    
    /**
     * 유효한 사유 생성기
     */
    @Provide
    Arbitrary<String> validReasons() {
        return Arbitraries.oneOf(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(10).ofMaxLength(100),
                Arbitraries.of(
                        "서비스 정책 위반",
                        "결제 문제로 인한 중지",
                        "고객 요청에 의한 승인",
                        "자동 승인 규칙 적용",
                        "관리자 판단에 의한 거부",
                        "시스템 점검으로 인한 일시 중지"
                )
        );
    }
    
    /**
     * 유효한 승인 액션 생성기
     */
    @Provide
    Arbitrary<ApprovalAction> validApprovalActions() {
        return Arbitraries.of(ApprovalAction.values());
    }
    
    /**
     * 테스트용 관리자 생성
     */
    private User createTestAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@smartcon.com");
        admin.setName("관리자");
        return admin;
    }
    
    /**
     * 상태 전환을 나타내는 레코드
     */
    private record StatusTransition(SubscriptionStatus fromStatus, SubscriptionStatus toStatus) {}
}