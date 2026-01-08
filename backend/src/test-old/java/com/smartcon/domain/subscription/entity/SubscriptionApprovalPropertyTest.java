package com.smartcon.domain.subscription.entity;

import com.smartcon.domain.tenant.entity.Tenant;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 구독 승인 워크플로우 Property-Based Test
 * 
 * Feature: subscription-approval-workflow
 * Property 1: Subscription Request Creation
 */
class SubscriptionApprovalPropertyTest {
    
    /**
     * Property 1: Subscription Request Creation
     * For any valid subscription request, creating the subscription should result in a subscription with PENDING_APPROVAL status
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 1: Subscription Request Creation")
    void subscriptionRequestCreation(
            @ForAll("validPlanIds") String planId,
            @ForAll("validBillingCycles") BillingCycle billingCycle,
            @ForAll("validPrices") BigDecimal monthlyPrice) {
        
        // Given - 테스트용 테넌트와 요금제 생성
        var testTenant = createTestTenant();
        var testPlan = createTestPlan(planId, monthlyPrice);
        
        // When - 새로운 구독 생성 (기본 상태는 PENDING_APPROVAL이어야 함)
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .startDate(LocalDate.now())
                .billingCycle(billingCycle)
                .monthlyPrice(monthlyPrice)
                .build();
        
        // Then - 모든 유효한 구독 요청은 PENDING_APPROVAL 상태로 생성되어야 함
        assertThat(subscription.getStatus())
                .as("모든 유효한 구독 요청은 PENDING_APPROVAL 상태로 생성되어야 함")
                .isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        
        assertThat(subscription.getApprovalRequestedAt())
                .as("승인 요청 시간이 설정되어야 함")
                .isNotNull();
        
        assertThat(subscription.isPendingApproval())
                .as("승인 대기 상태 확인 메서드가 true를 반환해야 함")
                .isTrue();
    }
    
    /**
     * 유효한 요금제 ID 생성기
     */
    @Provide
    Arbitrary<String> validPlanIds() {
        return Arbitraries.of("basic", "standard", "premium", "enterprise");
    }
    
    /**
     * 유효한 결제 주기 생성기
     */
    @Provide
    Arbitrary<BillingCycle> validBillingCycles() {
        return Arbitraries.of(BillingCycle.MONTHLY, BillingCycle.YEARLY);
    }
    
    /**
     * 유효한 가격 생성기
     */
    @Provide
    Arbitrary<BigDecimal> validPrices() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000000))
                .ofScale(2);
    }
    
    /**
     * 테스트용 테넌트 생성
     */
    private Tenant createTestTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setCompanyName("테스트 회사");
        tenant.setBusinessNo("123-45-67890");
        tenant.setRepresentativeName("홍길동");
        return tenant;
    }
    
    /**
     * 테스트용 요금제 생성
     */
    private SubscriptionPlan createTestPlan(String planId, BigDecimal monthlyPrice) {
        return SubscriptionPlan.builder()
                .planId(planId)
                .name(planId.toUpperCase())
                .monthlyPrice(monthlyPrice)
                .yearlyPrice(monthlyPrice.multiply(BigDecimal.valueOf(10.8)))
                .maxSites(10)
                .maxUsers(100)
                .maxStorageGb(50)
                .isActive(true)
                .build();
    }
}