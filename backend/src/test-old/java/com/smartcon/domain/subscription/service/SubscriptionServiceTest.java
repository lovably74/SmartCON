package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.subscription.dto.SubscriptionPlanDto;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 구독 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    
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
    private com.smartcon.domain.subscription.service.NotificationService notificationService;
    
    @InjectMocks
    private SubscriptionService subscriptionService;
    
    private Tenant testTenant;
    private SubscriptionPlan testPlan;
    
    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setCompanyName("테스트 회사");
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setRepresentativeName("홍길동");
        
        testPlan = SubscriptionPlan.builder()
                .planId("basic")
                .name("Basic")
                .monthlyPrice(BigDecimal.valueOf(50000))
                .yearlyPrice(BigDecimal.valueOf(540000))
                .maxSites(3)
                .maxUsers(50)
                .maxStorageGb(10)
                .build();
    }
    
    @Test
    @DisplayName("활성 요금제 목록 조회")
    void getActivePlans() {
        // Given
        List<SubscriptionPlan> plans = Arrays.asList(testPlan);
        given(subscriptionPlanRepository.findActiveOrderBySortOrder()).willReturn(plans);
        
        // When
        List<SubscriptionPlanDto> result = subscriptionService.getActivePlans();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("basic");
        assertThat(result.get(0).getName()).isEqualTo("Basic");
    }
    
    @Test
    @DisplayName("구독 신청 성공")
    void createSubscription_Success() {
        // Given
        Long tenantId = 1L;
        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);
            
            CreateSubscriptionRequest request = new CreateSubscriptionRequest();
            request.setPlanId("basic");
            request.setBillingCycle(BillingCycle.MONTHLY);
            request.setStartTrial(true);
            request.setTrialDays(14);
            
            given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
            given(subscriptionRepository.existsActiveByTenant(testTenant)).willReturn(false);
            given(subscriptionPlanRepository.findByPlanIdAndIsActiveTrue("basic")).willReturn(Optional.of(testPlan));
            
            Subscription savedSubscription = Subscription.builder()
                    .tenant(testTenant)
                    .plan(testPlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .billingCycle(BillingCycle.MONTHLY)
                    .monthlyPrice(BigDecimal.valueOf(50000))
                    .trialEndDate(LocalDate.now().plusDays(14))
                    .build();
            given(subscriptionRepository.save(any(Subscription.class))).willReturn(savedSubscription);
            
            // When
            SubscriptionDto result = subscriptionService.createSubscription(request);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(result.getIsTrial()).isTrue();
            verify(subscriptionRepository).save(any(Subscription.class));
        }
    }
    
    @Test
    @DisplayName("구독 신청 실패 - 이미 활성 구독 존재")
    void createSubscription_AlreadyExists() {
        // Given
        Long tenantId = 1L;
        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);
            
            CreateSubscriptionRequest request = new CreateSubscriptionRequest();
            request.setPlanId("basic");
            request.setBillingCycle(BillingCycle.MONTHLY);
            
            given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
            given(subscriptionRepository.existsActiveByTenant(testTenant)).willReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.createSubscription(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 활성 구독이 존재합니다");
        }
    }
    
    @Test
    @DisplayName("구독 신청 실패 - 존재하지 않는 요금제")
    void createSubscription_PlanNotFound() {
        // Given
        Long tenantId = 1L;
        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);
            
            CreateSubscriptionRequest request = new CreateSubscriptionRequest();
            request.setPlanId("invalid_plan");
            request.setBillingCycle(BillingCycle.MONTHLY);
            
            given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
            given(subscriptionRepository.existsActiveByTenant(testTenant)).willReturn(false);
            given(subscriptionPlanRepository.findByPlanIdAndIsActiveTrue("invalid_plan")).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> subscriptionService.createSubscription(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 요금제입니다");
        }
    }
    
    @Test
    @DisplayName("현재 구독 조회")
    void getCurrentSubscription() {
        // Given
        Long tenantId = 1L;
        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);
            
            given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
            
            Subscription subscription = Subscription.builder()
                    .tenant(testTenant)
                    .plan(testPlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .billingCycle(BillingCycle.MONTHLY)
                    .monthlyPrice(BigDecimal.valueOf(50000))
                    .build();
            given(subscriptionRepository.findActiveByTenant(testTenant)).willReturn(Optional.of(subscription));
            
            // When
            SubscriptionDto result = subscriptionService.getCurrentSubscription();
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(result.getTenantId()).isEqualTo(tenantId);
        }
    }
    
    @Test
    @DisplayName("구독 해지")
    void cancelSubscription() {
        // Given
        Long subscriptionId = 1L;
        String reason = "서비스 불만족";
        
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .build();
        given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.of(subscription));
        
        // When
        SubscriptionDto result = subscriptionService.cancelSubscription(subscriptionId, reason);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(result.getCancellationReason()).isEqualTo(reason);
    }
}