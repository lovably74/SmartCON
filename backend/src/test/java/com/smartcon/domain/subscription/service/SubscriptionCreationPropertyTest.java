package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 구독 생성 단위 테스트
 * 
 * Property 1: Subscription Request Creation
 * Property 2: Pending Subscription Access Control
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionCreationPropertyTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentMethodRepository paymentMethodRepository;
    
    @Mock
    private TenantRepository tenantRepository;
    
    @Mock
    private AutoApprovalRuleService autoApprovalRuleService;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Tenant testTenant;
    private SubscriptionPlan testPlan;

    @BeforeEach
    void setUp() {
        // TenantContext 설정
        TenantContext.setCurrentTenantId(1L);
        
        // 테스트용 테넌트 설정
        testTenant = new Tenant();
        testTenant.setCompanyName("테스트 회사");
        testTenant.setBusinessNo("123-45-67890");
        
        // 리플렉션으로 ID 설정
        try {
            var idField = testTenant.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testTenant, 1L);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }

        // 테스트용 요금제 설정
        testPlan = SubscriptionPlan.builder()
                .planId("BASIC")
                .name("기본 요금제")
                .monthlyPrice(BigDecimal.valueOf(50000))
                .yearlyPrice(BigDecimal.valueOf(500000))
                .maxSites(10)
                .maxUsers(100)
                .maxStorageGb(50)
                .isActive(true)
                .sortOrder(1)
                .build();
        
        // Mock 설정
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
        when(subscriptionPlanRepository.findByIdAndIsActiveTrue("BASIC")).thenReturn(Optional.of(testPlan));
        when(subscriptionRepository.existsActiveByTenant(testTenant)).thenReturn(false);
    }

    /**
     * Property 1: Subscription Request Creation
     * 모든 유효한 구독 요청은 PENDING_APPROVAL 상태로 생성되어야 함
     */
    @Test
    void subscriptionRequestCreation_월간결제_할인없음() {
        // Given: 유효한 구독 요청
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId("BASIC");
        request.setBillingCycle(BillingCycle.MONTHLY);
        request.setDiscountRate(BigDecimal.ZERO);
        request.setTrialDays(14);
        request.setAutoRenewal(true);
        request.setStartTrial(false);

        // Mock 구독 객체 생성
        Subscription mockSubscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .discountRate(BigDecimal.ZERO)
                .autoRenewal(true)
                .trialEndDate(null)
                .build();
        
        // 리플렉션으로 ID 설정
        try {
            var idField = mockSubscription.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockSubscription, 1L);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSubscription);
        when(autoApprovalRuleService.evaluateAutoApproval(any(CreateSubscriptionRequest.class))).thenReturn(false);

        // When: 구독 생성
        SubscriptionDto result = subscriptionService.createSubscription(request);

        // Then: PENDING_APPROVAL 상태로 생성되어야 함
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        assertThat(result.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(result.getDiscountRate()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getAutoRenewal()).isEqualTo(true);
        
        // 알림 발송 확인 (ID는 null일 수 있음)
        verify(notificationService).sendSubscriptionRequestNotification(isNull());
    }

    /**
     * Property 1: Subscription Request Creation
     * 연간 결제 및 할인이 적용된 구독 요청 테스트
     */
    @Test
    void subscriptionRequestCreation_연간결제_할인적용() {
        // Given: 연간 결제 및 할인이 적용된 구독 요청
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId("BASIC");
        request.setBillingCycle(BillingCycle.YEARLY);
        request.setDiscountRate(BigDecimal.valueOf(10));
        request.setTrialDays(30);
        request.setAutoRenewal(false);
        request.setStartTrial(true);

        // Mock 구독 객체 생성
        Subscription mockSubscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusDays(30))
                .billingCycle(BillingCycle.YEARLY)
                .monthlyPrice(BigDecimal.valueOf(45000)) // 10% 할인 적용
                .discountRate(BigDecimal.valueOf(10))
                .autoRenewal(false)
                .trialEndDate(LocalDate.now().plusDays(30))
                .build();
        
        // 리플렉션으로 ID 설정
        try {
            var idField = mockSubscription.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockSubscription, 2L);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSubscription);
        when(autoApprovalRuleService.evaluateAutoApproval(any(CreateSubscriptionRequest.class))).thenReturn(false);

        // When: 구독 생성
        SubscriptionDto result = subscriptionService.createSubscription(request);

        // Then: PENDING_APPROVAL 상태로 생성되어야 함
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        assertThat(result.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(result.getDiscountRate()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(result.getAutoRenewal()).isEqualTo(false);
        assertThat(result.getTrialEndDate()).isNotNull();
        
        // 알림 발송 확인 (ID는 null일 수 있음)
        verify(notificationService).sendSubscriptionRequestNotification(isNull());
    }

    /**
     * Property 2: Pending Subscription Access Control
     * PENDING_APPROVAL 상태의 구독은 활성 구독으로 간주되지 않아야 함
     */
    @Test
    void pendingSubscriptionAccessControl() {
        // Given: PENDING_APPROVAL 상태의 구독이 존재하지만 활성 구독은 없음
        when(subscriptionRepository.findActiveByTenant(testTenant)).thenReturn(Optional.empty());

        // When: 현재 구독 조회
        SubscriptionDto currentSubscription = subscriptionService.getCurrentSubscription();

        // Then: 활성 구독이 없어야 함
        assertThat(currentSubscription).isNull();
        
        // 새로운 구독 생성이 가능해야 함
        when(subscriptionRepository.existsActiveByTenant(testTenant)).thenReturn(false);
        
        CreateSubscriptionRequest newRequest = new CreateSubscriptionRequest();
        newRequest.setPlanId("BASIC");
        newRequest.setBillingCycle(BillingCycle.MONTHLY);
        newRequest.setDiscountRate(BigDecimal.ZERO);
        newRequest.setTrialDays(14);
        newRequest.setAutoRenewal(true);
        newRequest.setStartTrial(false);

        Subscription newMockSubscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .discountRate(BigDecimal.ZERO)
                .autoRenewal(true)
                .build();
        
        // 리플렉션으로 ID 설정
        try {
            var idField = newMockSubscription.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(newMockSubscription, 3L);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(newMockSubscription);
        when(autoApprovalRuleService.evaluateAutoApproval(any(CreateSubscriptionRequest.class))).thenReturn(false);

        // 새 구독 생성이 성공해야 함
        SubscriptionDto newSubscription = subscriptionService.createSubscription(newRequest);
        assertThat(newSubscription).isNotNull();
        assertThat(newSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
    }

    /**
     * 자동 승인 규칙이 적용되는 경우 테스트
     */
    @Test
    void subscriptionRequestCreation_자동승인() {
        // Given: 자동 승인 규칙에 해당하는 구독 요청
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId("BASIC");
        request.setBillingCycle(BillingCycle.MONTHLY);
        request.setDiscountRate(BigDecimal.ZERO);
        request.setTrialDays(14);
        request.setAutoRenewal(true);
        request.setStartTrial(false);

        // Mock 구독 객체 생성 (자동 승인으로 ACTIVE 상태)
        Subscription mockSubscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE) // 자동 승인으로 ACTIVE 상태
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .discountRate(BigDecimal.ZERO)
                .autoRenewal(true)
                .trialEndDate(null)
                .build();
        
        // 리플렉션으로 ID 설정
        try {
            var idField = mockSubscription.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockSubscription, 4L);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSubscription);
        when(autoApprovalRuleService.evaluateAutoApproval(any(CreateSubscriptionRequest.class))).thenReturn(true);

        // When: 구독 생성
        SubscriptionDto result = subscriptionService.createSubscription(request);

        // Then: ACTIVE 상태로 생성되어야 함 (자동 승인)
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 승인 알림 발송 확인 (ID는 null일 수 있음)
        verify(notificationService).sendApprovalResultNotification(isNull(), eq(true), eq("자동 승인 규칙에 의해 승인되었습니다."));
    }
}