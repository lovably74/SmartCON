package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.entity.BillingCycle;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.security.SubscriptionAccessResult;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 구독 접근 제어 서비스 속성 기반 테스트
 * Feature: subscription-approval-workflow, Property 2: Pending Subscription Access Control
 * 
 * Property 2: PENDING_APPROVAL 상태의 구독에 대해 서비스 접근이 차단되어야 함
 * Validates: Requirements 1.2
 */
class SubscriptionAccessControlPropertyTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TenantRepository tenantRepository;

    private SubscriptionAccessControlService subscriptionAccessControlService;
    private Tenant testTenant;
    private SubscriptionPlan testPlan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionAccessControlService = new SubscriptionAccessControlServiceImpl(subscriptionRepository, tenantRepository);
        
        // 테스트용 테넌트 생성
        testTenant = new Tenant();
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setCompanyName("테스트 회사");
        testTenant.setRepresentativeName("홍길동");
        testTenant.setEmail("test@example.com");
        testTenant.setPhoneNumber("010-1234-5678");
        testTenant.setRoadAddress("서울시 강남구");
        // ID 설정 (실제로는 JPA가 설정하지만 테스트에서는 수동 설정)
        testTenant.setId(1L);

        // 테스트용 구독 플랜 생성
        testPlan = SubscriptionPlan.builder()
                .planId("BASIC")
                .name("기본 플랜")
                .monthlyPrice(BigDecimal.valueOf(50000))
                .maxSites(5)
                .maxUsers(50)
                .maxStorageGb(10)
                .isActive(true)
                .sortOrder(1)
                .build();
                
        // 기본 Mock 설정
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
    }

    /**
     * Property 2: Pending Subscription Access Control
     * 모든 PENDING_APPROVAL 상태의 구독에 대해 서비스 접근이 차단되어야 함
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 2: Pending Subscription Access Control")
    void pendingSubscriptionsShouldBlockServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @Positive BigDecimal monthlyPrice,
            @ForAll BillingCycle billingCycle
    ) {
        // Given: PENDING_APPROVAL 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(billingCycle)
                .monthlyPrice(monthlyPrice)
                .autoRenewal(true)
                .build();
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 거부되어야 함
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        assertThat(result.getMessage()).contains("승인");
        assertThat(result.getRedirectUrl()).isNotNull();
    }

    /**
     * 활성 구독 상태에서는 서비스 접근이 허용되어야 함 (대조군 테스트)
     */
    @Property(tries = 50)
    void activeSubscriptionsShouldAllowServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @Positive BigDecimal monthlyPrice,
            @ForAll BillingCycle billingCycle
    ) {
        // Given: ACTIVE 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(billingCycle)
                .monthlyPrice(monthlyPrice)
                .autoRenewal(true)
                .build();
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 허용되어야 함
        assertThat(result.isAllowed()).isTrue();
    }

    /**
     * 거부된 구독 상태에서는 서비스 접근이 차단되어야 함
     */
    @Property(tries = 50)
    void rejectedSubscriptionsShouldBlockServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @NotBlank String rejectionReason,
            @ForAll @Positive BigDecimal monthlyPrice
    ) {
        // Given: REJECTED 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.REJECTED)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(monthlyPrice)
                .autoRenewal(true)
                .build();
        
        subscription.reject(rejectionReason);
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 거부되어야 함
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        assertThat(result.getMessage()).contains("거부");
        assertThat(result.getMessage()).contains(rejectionReason);
    }

    /**
     * 중지된 구독 상태에서는 서비스 접근이 차단되어야 함
     */
    @Property(tries = 50)
    void suspendedSubscriptionsShouldBlockServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @NotBlank String suspensionReason,
            @ForAll @Positive BigDecimal monthlyPrice
    ) {
        // Given: SUSPENDED 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(monthlyPrice)
                .autoRenewal(true)
                .build();
        
        subscription.suspend(suspensionReason);
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 거부되어야 함
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(result.getMessage()).contains("중지");
        assertThat(result.getMessage()).contains(suspensionReason);
    }

    /**
     * 종료된 구독 상태에서는 서비스 접근이 차단되어야 함
     */
    @Property(tries = 50)
    void terminatedSubscriptionsShouldBlockServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @NotBlank String terminationReason,
            @ForAll @Positive BigDecimal monthlyPrice
    ) {
        // Given: TERMINATED 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(monthlyPrice)
                .autoRenewal(true)
                .build();
        
        subscription.terminate(terminationReason);
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 거부되어야 함
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        assertThat(result.getMessage()).contains("종료");
        assertThat(result.getMessage()).contains(terminationReason);
    }

    /**
     * 구독이 없는 테넌트는 서비스 접근이 차단되어야 함
     */
    @Property(tries = 30)
    void tenantsWithoutSubscriptionShouldBlockServiceAccess(
            @ForAll @NotBlank String requestPath
    ) {
        // Given: 구독이 없는 테넌트
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.empty());

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 거부되어야 함
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getMessage()).contains("구독");
        assertThat(result.getRedirectUrl()).contains("subscription");
    }

    /**
     * 자동 승인된 구독은 서비스 접근이 허용되어야 함
     */
    @Property(tries = 30)
    void autoApprovedSubscriptionsShouldAllowServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @Positive BigDecimal monthlyPrice
    ) {
        // Given: AUTO_APPROVED 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.AUTO_APPROVED)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(monthlyPrice)
                .autoRenewal(true)
                .build();
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 허용되어야 함
        assertThat(result.isAllowed()).isTrue();
    }

    /**
     * 체험판 구독은 서비스 접근이 허용되어야 함
     */
    @Property(tries = 30)
    void trialSubscriptionsShouldAllowServiceAccess(
            @ForAll @NotBlank String requestPath,
            @ForAll @Positive BigDecimal monthlyPrice
    ) {
        // Given: TRIAL 상태의 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(monthlyPrice)
                .trialEndDate(LocalDate.now().plusDays(14))
                .autoRenewal(true)
                .build();
        
        // Mock 설정
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 서비스 접근 권한 확인
        SubscriptionAccessResult result = subscriptionAccessControlService.checkAccess(
                testTenant.getId(), 
                requestPath
        );

        // Then: 접근이 허용되어야 함
        assertThat(result.isAllowed()).isTrue();
    }

    /**
     * 서비스 접근 가능 여부 확인 메서드 테스트
     */
    @Test
    void isServiceAccessible_shouldReturnCorrectResult() {
        // Given: PENDING_APPROVAL 상태의 구독
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .autoRenewal(true)
                .build();
        
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When & Then: 서비스 접근 불가
        assertThat(subscriptionAccessControlService.isServiceAccessible(testTenant.getId())).isFalse();

        // Given: 구독 상태를 ACTIVE로 변경
        subscription.updateStatus(SubscriptionStatus.ACTIVE);

        // When & Then: 서비스 접근 가능
        assertThat(subscriptionAccessControlService.isServiceAccessible(testTenant.getId())).isTrue();
    }

    /**
     * 상태 메시지 조회 테스트
     */
    @Test
    void getStatusMessage_shouldReturnAppropriateMessage() {
        // Given: PENDING_APPROVAL 상태의 구독
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .autoRenewal(true)
                .build();
        
        when(subscriptionRepository.findCurrentByTenant(testTenant)).thenReturn(Optional.of(subscription));

        // When: 상태 메시지 조회
        String message = subscriptionAccessControlService.getStatusMessage(testTenant.getId());

        // Then: 적절한 메시지 반환
        assertThat(message).contains("승인");
    }

    // 테스트용 생성기들
    @Provide
    Arbitrary<BillingCycle> billingCycle() {
        return Arbitraries.of(BillingCycle.values());
    }
}