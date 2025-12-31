package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.SubscriptionPlanRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.security.SubscriptionAccessResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 구독 접근 제어 서비스 통합 테스트
 * 
 * 실제 데이터베이스와 Spring 컨텍스트를 사용하여
 * 구독 상태별 접근 제어 로직을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionAccessControlIntegrationTest {

    @Autowired
    private SubscriptionAccessControlService subscriptionAccessControlService;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    private Tenant testTenant;
    private SubscriptionPlan testPlan;
    
    @BeforeEach
    void setUp() {
        // 테스트용 테넌트 생성
        testTenant = new Tenant();
        testTenant.setCompanyName("테스트 회사");
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setEmail("test@example.com");
        testTenant.setPhoneNumber("010-1234-5678");
        testTenant = tenantRepository.save(testTenant);
        
        // 테스트용 구독 요금제 생성
        testPlan = SubscriptionPlan.builder()
                .planId("basic")
                .name("기본 요금제")
                .description("기본 요금제입니다")
                .monthlyPrice(BigDecimal.valueOf(50000))
                .maxSites(1)
                .maxUsers(10)
                .build();
        testPlan = subscriptionPlanRepository.save(testPlan);
    }
    
    @Test
    @DisplayName("활성 구독이 있는 테넌트는 서비스 접근이 허용되어야 한다")
    void activeSubscriptionShouldAllowAccess() {
        // Given: 활성 구독 생성
        Subscription activeSubscription = createSubscription(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(activeSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 허용
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.getMessage()).isNull();
    }
    
    @Test
    @DisplayName("체험판 구독이 있는 테넌트는 서비스 접근이 허용되어야 한다")
    void trialSubscriptionShouldAllowAccess() {
        // Given: 체험판 구독 생성
        Subscription trialSubscription = createSubscription(SubscriptionStatus.TRIAL);
        subscriptionRepository.save(trialSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 허용
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TRIAL);
        assertThat(result.getMessage()).isNull();
    }
    
    @Test
    @DisplayName("자동 승인된 구독이 있는 테넌트는 서비스 접근이 허용되어야 한다")
    void autoApprovedSubscriptionShouldAllowAccess() {
        // Given: 자동 승인된 구독 생성
        Subscription autoApprovedSubscription = createSubscription(SubscriptionStatus.AUTO_APPROVED);
        subscriptionRepository.save(autoApprovedSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 허용
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.AUTO_APPROVED);
        assertThat(result.getMessage()).isNull();
    }
    
    @Test
    @DisplayName("승인 대기 구독이 있는 테넌트는 서비스 접근이 차단되어야 한다")
    void pendingSubscriptionShouldBlockAccess() {
        // Given: 승인 대기 구독 생성
        Subscription pendingSubscription = createSubscription(SubscriptionStatus.PENDING_APPROVAL);
        subscriptionRepository.save(pendingSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 차단
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        assertThat(result.getMessage()).contains("승인이 진행 중입니다");
    }
    
    @Test
    @DisplayName("거부된 구독이 있는 테넌트는 서비스 접근이 차단되어야 한다")
    void rejectedSubscriptionShouldBlockAccess() {
        // Given: 거부된 구독 생성
        Subscription rejectedSubscription = createSubscription(SubscriptionStatus.REJECTED);
        subscriptionRepository.save(rejectedSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 차단
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        assertThat(result.getMessage()).contains("거부");
    }
    
    @Test
    @DisplayName("중지된 구독이 있는 테넌트는 서비스 접근이 차단되어야 한다")
    void suspendedSubscriptionShouldBlockAccess() {
        // Given: 중지된 구독 생성
        Subscription suspendedSubscription = createSubscription(SubscriptionStatus.SUSPENDED);
        subscriptionRepository.save(suspendedSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 차단
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(result.getMessage()).contains("중지");
    }
    
    @Test
    @DisplayName("종료된 구독이 있는 테넌트는 서비스 접근이 차단되어야 한다")
    void terminatedSubscriptionShouldBlockAccess() {
        // Given: 종료된 구독 생성
        Subscription terminatedSubscription = createSubscription(SubscriptionStatus.TERMINATED);
        subscriptionRepository.save(terminatedSubscription);
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 차단
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        assertThat(result.getMessage()).contains("종료");
    }
    
    @Test
    @DisplayName("구독이 없는 테넌트는 서비스 접근이 차단되어야 한다")
    void noSubscriptionShouldBlockAccess() {
        // Given: 구독이 없는 상태 (setUp에서 테넌트만 생성)
        
        // When: 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/attendance");
        
        // Then: 접근 차단
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getSubscriptionStatus()).isNull();
        assertThat(result.getMessage()).contains("구독이 없습니다");
    }
    
    @Test
    @DisplayName("슈퍼 관리자 API는 구독 상태와 관계없이 접근이 허용되어야 한다")
    void superAdminApiShouldAlwaysAllowAccess() {
        // Given: 승인 대기 구독 생성 (일반적으로 차단되는 상태)
        Subscription pendingSubscription = createSubscription(SubscriptionStatus.PENDING_APPROVAL);
        subscriptionRepository.save(pendingSubscription);
        
        // When: 슈퍼 관리자 API 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/super/dashboard");
        
        // Then: 접근 허용 (슈퍼 관리자 API는 구독 상태 무관)
        assertThat(result.isAllowed()).isTrue();
    }
    
    @Test
    @DisplayName("인증 관련 API는 구독 상태와 관계없이 접근이 허용되어야 한다")
    void authApiShouldAlwaysAllowAccess() {
        // Given: 구독이 없는 상태
        
        // When: 인증 API 접근 제어 확인
        SubscriptionAccessResult result = subscriptionAccessControlService
                .checkAccess(testTenant.getId(), "/api/v1/auth/login");
        
        // Then: 접근 허용 (인증 API는 구독 상태 무관)
        assertThat(result.isAllowed()).isTrue();
    }
    
    /**
     * 테스트용 구독 생성 헬퍼 메서드
     */
    private Subscription createSubscription(SubscriptionStatus status) {
        return Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(status)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .build();
    }
}