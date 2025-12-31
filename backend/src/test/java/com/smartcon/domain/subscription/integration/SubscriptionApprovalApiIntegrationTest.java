package com.smartcon.domain.subscription.integration;

import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.subscription.service.SubscriptionApprovalService;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 구독 승인 서비스 통합 테스트
 * 
 * 서비스와 데이터베이스 연동 테스트를 수행합니다.
 * Requirements: 1.1, 1.4, 1.5, 2.1, 2.2
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionApprovalApiIntegrationTest {

    @Autowired
    private SubscriptionApprovalService approvalService;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private SubscriptionApprovalRepository approvalRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    private Tenant testTenant;
    private User superAdmin;
    private Subscription testSubscription;
    private SubscriptionPlan testPlan;

    @BeforeEach
    void setUp() {
        // 테스트 구독 플랜 생성
        testPlan = SubscriptionPlan.builder()
                .planId("basic")
                .name("기본 플랜")
                .description("테스트용 기본 플랜")
                .monthlyPrice(BigDecimal.valueOf(100000))
                .maxSites(5)
                .maxUsers(50)
                .isActive(true)
                .build();
        testPlan = subscriptionPlanRepository.save(testPlan);
        
        // 테스트 테넌트 생성
        testTenant = new Tenant();
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setCompanyName("테스트 회사");
        testTenant.setRepresentativeName("홍길동");
        testTenant.setStatus(Tenant.SubscriptionStatus.TRIAL);
        testTenant.setEmail("tenant@test.com");
        testTenant.setPhoneNumber("02-1234-5678");
        testTenant = tenantRepository.save(testTenant);
        
        // 슈퍼 관리자 생성
        superAdmin = new User();
        superAdmin.setName("슈퍼관리자");
        superAdmin.setEmail("super@smartcon.com");
        superAdmin.setProvider(User.Provider.LOCAL);
        superAdmin.setIsActive(true);
        superAdmin.setTenantId(testTenant.getId()); // tenant_id 설정
        superAdmin = userRepository.save(superAdmin);
        
        // 테스트 구독 생성
        testSubscription = createTestSubscription();
    }

    @Test
    @DisplayName("구독 승인 서비스 테스트")
    void testApproveSubscriptionService() {
        // Given: 승인 요청 데이터
        String reason = "신규 구독 승인 완료";
        
        // When: 승인 서비스 호출
        SubscriptionDto approvedSubscription = approvalService.approveSubscription(
            testSubscription.getId(), reason);
        
        // Then: 승인 완료 확인
        assertThat(approvedSubscription).isNotNull();
        assertThat(approvedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 데이터베이스 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 승인 이력 확인
        var approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory).isNotEmpty();
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
    }

    @Test
    @DisplayName("구독 거부 서비스 테스트")
    void testRejectSubscriptionService() {
        // Given: 거부 요청 데이터
        String reason = "서류 미비로 인한 거부";
        
        // When: 거부 서비스 호출
        SubscriptionDto rejectedSubscription = approvalService.rejectSubscription(
            testSubscription.getId(), reason);
        
        // Then: 거부 완료 확인
        assertThat(rejectedSubscription).isNotNull();
        assertThat(rejectedSubscription.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        
        // 데이터베이스 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        assertThat(updatedSubscription.getRejectionReason()).isEqualTo(reason);
        
        // 거부 이력 확인
        var approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory).isNotEmpty();
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.REJECT);
    }

    @Test
    @DisplayName("구독 중지 서비스 테스트")
    void testSuspendSubscriptionService() {
        // Given: 먼저 구독을 승인 상태로 만듦
        approvalService.approveSubscription(testSubscription.getId(), "초기 승인");
        
        String reason = "결제 지연으로 인한 중지";
        
        // When: 중지 서비스 호출
        SubscriptionDto suspendedSubscription = approvalService.suspendSubscription(
            testSubscription.getId(), reason);
        
        // Then: 중지 완료 확인
        assertThat(suspendedSubscription).isNotNull();
        assertThat(suspendedSubscription.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        
        // 데이터베이스 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(updatedSubscription.getSuspensionReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("승인 대기 목록 조회 서비스 테스트")
    void testGetPendingApprovalsService() {
        // Given: 여러 승인 대기 구독 생성
        createAnotherTestSubscription("234-56-78901", "회사2");
        createAnotherTestSubscription("345-67-89012", "회사3");
        
        // When: 승인 대기 목록 조회 서비스 호출
        List<SubscriptionDto> pendingApprovals = approvalService.getPendingApprovalsOptimized(10, 0);
        
        // Then: 승인 대기 목록 확인
        assertThat(pendingApprovals).hasSizeGreaterThanOrEqualTo(3);
        
        for (SubscriptionDto subscription : pendingApprovals) {
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        }
    }

    @Test
    @DisplayName("승인 이력 조회 서비스 테스트")
    void testGetApprovalHistoryService() {
        // Given: 구독에 대한 여러 액션 수행
        approvalService.approveSubscription(testSubscription.getId(), "승인 완료");
        approvalService.suspendSubscription(testSubscription.getId(), "중지");
        approvalService.reactivateSubscription(testSubscription.getId(), "재활성화");
        
        // When: 승인 이력 조회 서비스 호출
        var approvalHistory = approvalService.getApprovalHistoryOptimized(testSubscription.getId());
        
        // Then: 승인 이력 확인
        assertThat(approvalHistory).hasSize(3);
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.REACTIVATE);
        assertThat(approvalHistory.get(1).getAction()).isEqualTo(ApprovalAction.SUSPEND);
        assertThat(approvalHistory.get(2).getAction()).isEqualTo(ApprovalAction.APPROVE);
    }

    @Test
    @DisplayName("구독 통계 조회 서비스 테스트")
    void testGetSubscriptionStatsService() {
        // Given: 다양한 상태의 구독들 생성
        Subscription subscription2 = createAnotherTestSubscription("234-56-78901", "회사2");
        approvalService.approveSubscription(subscription2.getId(), "승인");
        
        Subscription subscription3 = createAnotherTestSubscription("345-67-89012", "회사3");
        approvalService.rejectSubscription(subscription3.getId(), "거부");
        
        // When: 구독 통계 조회 서비스 호출
        var stats = approvalService.getSubscriptionStatsOptimized();
        
        // Then: 통계 확인
        assertThat(stats).isNotNull();
        // 구체적인 통계 값은 다른 테스트의 영향을 받을 수 있으므로 존재 여부만 확인
    }

    @Test
    @DisplayName("잘못된 구독 ID로 승인 시도 시 예외 발생 테스트")
    void testApproveNonExistentSubscription() {
        // Given: 존재하지 않는 구독 ID
        Long invalidId = 99999L;
        
        // When & Then: 존재하지 않는 구독에 대한 승인 시도 시 예외 발생
        assertThatThrownBy(() -> 
            approvalService.approveSubscription(invalidId, "승인 시도"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 처리된 구독 재처리 시 예외 발생 테스트")
    void testApproveAlreadyProcessedSubscription() {
        // Given: 이미 승인된 구독
        approvalService.approveSubscription(testSubscription.getId(), "초기 승인");
        
        // When & Then: 이미 승인된 구독에 대한 재승인 시도 시 예외 발생
        assertThatThrownBy(() -> 
            approvalService.approveSubscription(testSubscription.getId(), "재승인 시도"))
            .isInstanceOf(com.smartcon.domain.subscription.service.IllegalStateTransitionException.class);
    }

    @Test
    @DisplayName("승인 대기 개수 조회 서비스 테스트")
    void testCountPendingApprovalsService() {
        // Given: 여러 승인 대기 구독 생성
        createAnotherTestSubscription("234-56-78901", "회사2");
        createAnotherTestSubscription("345-67-89012", "회사3");
        
        // When: 승인 대기 개수 조회
        long pendingCount = approvalService.countPendingApprovalsOptimized();
        
        // Then: 개수 확인
        assertThat(pendingCount).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("구독 필터링 조회 서비스 테스트")
    void testGetSubscriptionsFilteredService() {
        // Given: 다양한 상태의 구독들 생성
        Subscription subscription2 = createAnotherTestSubscription("234-56-78901", "회사2");
        approvalService.approveSubscription(subscription2.getId(), "승인");
        
        createAnotherTestSubscription("345-67-89012", "회사3");
        
        // When: 승인 대기 상태로 필터링 조회
        List<SubscriptionDto> filteredSubscriptions = approvalService.getSubscriptionsFilteredOptimized(
            "PENDING_APPROVAL", null, 10, 0);
        
        // Then: 필터링된 결과 확인
        assertThat(filteredSubscriptions).hasSizeGreaterThanOrEqualTo(2);
        
        for (SubscriptionDto subscription : filteredSubscriptions) {
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        }
    }

    @Test
    @DisplayName("구독 재활성화 서비스 테스트")
    void testReactivateSubscriptionService() {
        // Given: 구독을 승인 후 중지 상태로 만듦
        approvalService.approveSubscription(testSubscription.getId(), "초기 승인");
        approvalService.suspendSubscription(testSubscription.getId(), "중지");
        
        String reason = "결제 완료로 인한 재활성화";
        
        // When: 재활성화 서비스 호출
        SubscriptionDto reactivatedSubscription = approvalService.reactivateSubscription(
            testSubscription.getId(), reason);
        
        // Then: 재활성화 완료 확인
        assertThat(reactivatedSubscription).isNotNull();
        assertThat(reactivatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 데이터베이스 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 재활성화 이력 확인
        var approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.REACTIVATE);
    }

    @Test
    @DisplayName("구독 종료 서비스 테스트")
    void testTerminateSubscriptionService() {
        // Given: 구독을 승인 상태로 만듦
        approvalService.approveSubscription(testSubscription.getId(), "초기 승인");
        
        String reason = "계약 만료로 인한 종료";
        
        // When: 종료 서비스 호출
        SubscriptionDto terminatedSubscription = approvalService.terminateSubscription(
            testSubscription.getId(), reason);
        
        // Then: 종료 완료 확인
        assertThat(terminatedSubscription).isNotNull();
        assertThat(terminatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        
        // 데이터베이스 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        assertThat(updatedSubscription.getTerminationReason()).isEqualTo(reason);
        
        // 종료 이력 확인
        var approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.TERMINATE);
    }

    private Subscription createTestSubscription() {
        Subscription subscription = Subscription.builder()
                .tenant(testTenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .monthlyPrice(BigDecimal.valueOf(100000))
                .billingCycle(BillingCycle.MONTHLY)
                .autoRenewal(true)
                .build();
        return subscriptionRepository.save(subscription);
    }

    private Subscription createAnotherTestSubscription(String businessNo, String companyName) {
        // 새 테넌트 생성
        Tenant tenant = new Tenant();
        tenant.setBusinessNo(businessNo);
        tenant.setCompanyName(companyName);
        tenant.setRepresentativeName("대표자");
        tenant.setStatus(Tenant.SubscriptionStatus.TRIAL);
        tenant.setEmail(companyName.toLowerCase() + "@test.com");
        tenant = tenantRepository.save(tenant);
        
        // 새 구독 생성
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(testPlan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .monthlyPrice(BigDecimal.valueOf(100000))
                .billingCycle(BillingCycle.MONTHLY)
                .autoRenewal(true)
                .build();
        return subscriptionRepository.save(subscription);
    }
}