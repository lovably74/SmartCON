package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 구독 Repository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
class SubscriptionRepositoryTest {
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    private Tenant testTenant;
    private SubscriptionPlan testPlan;
    
    @BeforeEach
    void setUp() {
        // 테스트용 테넌트 생성
        testTenant = new Tenant();
        testTenant.setCompanyName("테스트 회사");
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setRepresentativeName("홍길동");
        testTenant = tenantRepository.save(testTenant);
        
        // 테스트용 요금제 생성
        testPlan = SubscriptionPlan.builder()
                .planId("test_plan")
                .name("테스트 요금제")
                .monthlyPrice(BigDecimal.valueOf(100000))
                .maxSites(10)
                .maxUsers(100)
                .build();
        testPlan = subscriptionPlanRepository.save(testPlan);
    }
    
    @Test
    @DisplayName("테넌트의 활성 구독 조회")
    void findActiveByTenant() {
        // Given
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(100000))
                .build();
        subscriptionRepository.save(subscription);
        
        // When
        Optional<Subscription> result = subscriptionRepository.findActiveByTenant(testTenant);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.get().getTenant().getId()).isEqualTo(testTenant.getId());
    }
    
    @Test
    @DisplayName("테넌트의 구독 이력 조회")
    void findByTenantOrderByCreatedAtDesc() {
        // Given
        Subscription oldSubscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.CANCELLED)
                .startDate(LocalDate.now().minusMonths(6))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .build();
        subscriptionRepository.save(oldSubscription);
        
        Subscription newSubscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(100000))
                .build();
        subscriptionRepository.save(newSubscription);
        
        // When
        List<Subscription> result = subscriptionRepository.findByTenantOrderByCreatedAtDesc(testTenant);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.get(1).getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }
    
    @Test
    @DisplayName("다음 결제일로 구독 조회")
    void findByNextBillingDate() {
        // Given
        LocalDate nextBillingDate = LocalDate.now().plusDays(1);
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(nextBillingDate)
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(100000))
                .build();
        subscriptionRepository.save(subscription);
        
        // When
        List<Subscription> result = subscriptionRepository.findByNextBillingDate(nextBillingDate);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNextBillingDate()).isEqualTo(nextBillingDate);
    }
    
    @Test
    @DisplayName("체험판 만료 예정 구독 조회")
    void findTrialEndingOn() {
        // Given
        LocalDate trialEndDate = LocalDate.now().plusDays(1);
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(13))
                .trialEndDate(trialEndDate)
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(100000))
                .build();
        subscriptionRepository.save(subscription);
        
        // When
        List<Subscription> result = subscriptionRepository.findTrialEndingOn(trialEndDate);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrialEndDate()).isEqualTo(trialEndDate);
    }
    
    @Test
    @DisplayName("테넌트의 활성 구독 존재 여부 확인")
    void existsActiveByTenant() {
        // Given
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(100000))
                .build();
        subscriptionRepository.save(subscription);
        
        // When
        boolean exists = subscriptionRepository.existsActiveByTenant(testTenant);
        
        // Then
        assertThat(exists).isTrue();
    }
}