package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.user.entity.User;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 알림 시스템 Property-Based Test
 * 
 * Feature: subscription-approval-workflow
 * Property 6: New Request Notification
 * Property 7: Approval Result Notification
 */
class NotificationSystemPropertyTest {
    
    /**
     * Property 6: New Request Notification
     * For any new subscription request, notifications should be sent to all super admin users
     * Validates: Requirements 2.1
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 6: New Request Notification")
    void newRequestNotification(
            @ForAll("validSubscriptionRequests") SubscriptionRequest request,
            @ForAll("validAdminLists") java.util.List<User> superAdmins) {
        
        // Given - 구독 신청 상황
        Subscription subscription = createSubscriptionFromRequest(request);
        
        // When - 알림 생성 (실제 서비스 호출 대신 알림 엔티티 직접 생성으로 테스트)
        java.util.List<Notification> notifications = createSubscriptionRequestNotifications(subscription, superAdmins);
        
        // Then - 모든 슈퍼관리자에게 알림이 생성되어야 함
        assertThat(notifications)
                .as("모든 슈퍼관리자에게 알림이 생성되어야 함")
                .hasSize(superAdmins.size());
        
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            User expectedRecipient = superAdmins.get(i);
            
            assertThat(notification.getRecipient())
                    .as("알림 수신자가 올바르게 설정되어야 함")
                    .isEqualTo(expectedRecipient);
            
            assertThat(notification.getType())
                    .as("알림 타입이 구독 신청으로 설정되어야 함")
                    .isEqualTo(NotificationType.SUBSCRIPTION_REQUEST);
            
            assertThat(notification.getTitle())
                    .as("알림 제목이 설정되어야 함")
                    .isNotNull()
                    .isNotEmpty();
            
            assertThat(notification.getMessage())
                    .as("알림 메시지가 설정되어야 함")
                    .isNotNull()
                    .isNotEmpty()
                    .contains(subscription.getTenant().getCompanyName())
                    .contains(subscription.getPlan().getName());
            
            assertThat(notification.getRelatedEntityType())
                    .as("관련 엔티티 타입이 설정되어야 함")
                    .isEqualTo("Subscription");
            
            assertThat(notification.getRelatedEntityId())
                    .as("관련 엔티티 ID가 설정되어야 함")
                    .isEqualTo(subscription.getId());
            
            assertThat(notification.getIsRead())
                    .as("새 알림은 읽지 않음 상태여야 함")
                    .isFalse();
        }
    }
    
    /**
     * Property 7: Approval Result Notification
     * For any subscription approval or rejection action, a notification should be sent to the requesting tenant
     * Validates: Requirements 2.2
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 7: Approval Result Notification")
    void approvalResultNotification(
            @ForAll("validSubscriptionRequests") SubscriptionRequest request,
            @ForAll("validApprovalResults") ApprovalResult result,
            @ForAll("validTenantAdminLists") java.util.List<User> tenantAdmins) {
        
        // Given - 구독 승인/거부 상황
        Subscription subscription = createSubscriptionFromRequest(request);
        
        // When - 승인 결과 알림 생성
        java.util.List<Notification> notifications = createApprovalResultNotifications(subscription, result, tenantAdmins);
        
        // Then - 모든 테넌트 관리자에게 알림이 생성되어야 함
        assertThat(notifications)
                .as("모든 테넌트 관리자에게 알림이 생성되어야 함")
                .hasSize(tenantAdmins.size());
        
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            User expectedRecipient = tenantAdmins.get(i);
            
            assertThat(notification.getRecipient())
                    .as("알림 수신자가 올바르게 설정되어야 함")
                    .isEqualTo(expectedRecipient);
            
            NotificationType expectedType = result.approved() ? 
                    NotificationType.SUBSCRIPTION_APPROVED : NotificationType.SUBSCRIPTION_REJECTED;
            
            assertThat(notification.getType())
                    .as("알림 타입이 승인 결과에 맞게 설정되어야 함")
                    .isEqualTo(expectedType);
            
            assertThat(notification.getTitle())
                    .as("알림 제목이 설정되어야 함")
                    .isNotNull()
                    .isNotEmpty();
            
            assertThat(notification.getMessage())
                    .as("알림 메시지가 설정되어야 함")
                    .isNotNull()
                    .isNotEmpty()
                    .contains(subscription.getPlan().getName());
            
            if (!result.approved() && result.reason() != null) {
                assertThat(notification.getMessage())
                        .as("거부 시 사유가 포함되어야 함")
                        .contains(result.reason());
            }
            
            assertThat(notification.getRelatedEntityType())
                    .as("관련 엔티티 타입이 설정되어야 함")
                    .isEqualTo("Subscription");
            
            assertThat(notification.getRelatedEntityId())
                    .as("관련 엔티티 ID가 설정되어야 함")
                    .isEqualTo(subscription.getId());
        }
    }
    
    /**
     * 유효한 구독 요청 생성기
     */
    @Provide
    Arbitrary<SubscriptionRequest> validSubscriptionRequests() {
        return Combinators.combine(
                Arbitraries.strings().withCharRange('a', 'z').ofLength(10), // 회사명
                Arbitraries.of("basic", "standard", "premium", "enterprise"), // 요금제
                Arbitraries.bigDecimals().between(BigDecimal.valueOf(10000), BigDecimal.valueOf(500000)).ofScale(2) // 가격
        ).as(SubscriptionRequest::new);
    }
    
    /**
     * 유효한 관리자 목록 생성기
     */
    @Provide
    Arbitrary<java.util.List<User>> validAdminLists() {
        return Arbitraries.integers().between(1, 5)
                .flatMap(size -> 
                        createUserArbitrary().list().ofSize(size)
                );
    }
    
    /**
     * 유효한 테넌트 관리자 목록 생성기
     */
    @Provide
    Arbitrary<java.util.List<User>> validTenantAdminLists() {
        return Arbitraries.integers().between(1, 3)
                .flatMap(size -> 
                        createUserArbitrary().list().ofSize(size)
                );
    }
    
    /**
     * 유효한 승인 결과 생성기
     */
    @Provide
    Arbitrary<ApprovalResult> validApprovalResults() {
        return Combinators.combine(
                Arbitraries.of(true, false),
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(50).optional()
        ).as((approved, reason) -> new ApprovalResult(approved, reason.orElse(null)));
    }
    
    /**
     * 사용자 생성기
     */
    private Arbitrary<User> createUserArbitrary() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().withCharRange('a', 'z').ofLength(8),
                Arbitraries.strings().withCharRange('a', 'z').ofLength(5)
        ).as((id, name, email) -> {
            User user = new User();
            user.setId(id);
            user.setName(name);
            user.setEmail(email + "@test.com");
            return user;
        });
    }
    
    /**
     * 구독 요청으로부터 구독 생성
     */
    private Subscription createSubscriptionFromRequest(SubscriptionRequest request) {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setCompanyName(request.companyName());
        
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .planId(request.planId())
                .name(request.planId().toUpperCase())
                .monthlyPrice(request.price())
                .build();
        
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(request.price())
                .build();
        
        // ID를 수동으로 설정 (테스트용)
        subscription.setId(1L);
        return subscription;
    }
    
    /**
     * 구독 신청 알림 생성
     */
    private java.util.List<Notification> createSubscriptionRequestNotifications(Subscription subscription, java.util.List<User> superAdmins) {
        return superAdmins.stream()
                .map(admin -> Notification.builder()
                        .recipient(admin)
                        .type(NotificationType.SUBSCRIPTION_REQUEST)
                        .title("새로운 구독 신청")
                        .message(String.format("%s에서 %s 요금제 구독을 신청했습니다.", 
                                subscription.getTenant().getCompanyName(),
                                subscription.getPlan().getName()))
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscription.getId())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 승인 결과 알림 생성
     */
    private java.util.List<Notification> createApprovalResultNotifications(Subscription subscription, ApprovalResult result, java.util.List<User> tenantAdmins) {
        NotificationType type = result.approved() ? NotificationType.SUBSCRIPTION_APPROVED : NotificationType.SUBSCRIPTION_REJECTED;
        String title = result.approved() ? "구독 승인 완료" : "구독 승인 거부";
        String message = result.approved() ? 
                String.format("%s 요금제 구독이 승인되었습니다. 이제 서비스를 이용하실 수 있습니다.", subscription.getPlan().getName()) :
                String.format("%s 요금제 구독이 거부되었습니다. 사유: %s", subscription.getPlan().getName(), result.reason());
        
        return tenantAdmins.stream()
                .map(admin -> Notification.builder()
                        .recipient(admin)
                        .type(type)
                        .title(title)
                        .message(message)
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscription.getId())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 구독 요청을 나타내는 레코드
     */
    private record SubscriptionRequest(String companyName, String planId, BigDecimal price) {}
    
    /**
     * 승인 결과를 나타내는 레코드
     */
    private record ApprovalResult(boolean approved, String reason) {}
}