package com.smartcon.domain.subscription.integration;

import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.subscription.service.SubscriptionApprovalService;
import com.smartcon.domain.subscription.service.AutoApprovalRuleService;
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
 * 구독 승인 다양한 시나리오 통합 테스트
 * 
 * 다양한 비즈니스 시나리오별 통합 테스트를 수행합니다.
 * Requirements: 1.1, 1.4, 1.5, 2.1, 2.2
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionApprovalScenariosIntegrationTest {

    @Autowired
    private SubscriptionApprovalService approvalService;
    
    @Autowired
    private AutoApprovalRuleService autoApprovalRuleService;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private SubscriptionApprovalRepository approvalRepository;
    
    @Autowired
    private AutoApprovalRuleRepository autoApprovalRuleRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    private Tenant testTenant;
    private Tenant premiumTenant;
    private User superAdmin;
    private Subscription basicSubscription;
    private Subscription premiumSubscription;
    private SubscriptionPlan basicPlan;
    private SubscriptionPlan premiumPlan;

    @BeforeEach
    void setUp() {
        // 기본 플랜 생성
        basicPlan = SubscriptionPlan.builder()
                .planId("basic")
                .name("기본 플랜")
                .description("기본 플랜")
                .monthlyPrice(BigDecimal.valueOf(50000))
                .maxSites(3)
                .maxUsers(30)
                .isActive(true)
                .build();
        basicPlan = subscriptionPlanRepository.save(basicPlan);
        
        // 프리미엄 플랜 생성
        premiumPlan = SubscriptionPlan.builder()
                .planId("premium")
                .name("프리미엄 플랜")
                .description("프리미엄 플랜")
                .monthlyPrice(BigDecimal.valueOf(200000))
                .maxSites(10)
                .maxUsers(100)
                .isActive(true)
                .build();
        premiumPlan = subscriptionPlanRepository.save(premiumPlan);
        
        // 일반 테넌트 생성
        testTenant = new Tenant();
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setCompanyName("일반 회사");
        testTenant.setRepresentativeName("홍길동");
        testTenant.setStatus(Tenant.SubscriptionStatus.TRIAL);
        testTenant.setEmail("normal@test.com");
        testTenant = tenantRepository.save(testTenant);
        
        // 프리미엄 테넌트 생성
        premiumTenant = new Tenant();
        premiumTenant.setBusinessNo("987-65-43210");
        premiumTenant.setCompanyName("프리미엄 회사");
        premiumTenant.setRepresentativeName("김프리미엄");
        premiumTenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        premiumTenant.setEmail("premium@test.com");
        premiumTenant = tenantRepository.save(premiumTenant);
        
        // 슈퍼 관리자 생성
        superAdmin = new User();
        superAdmin.setName("슈퍼관리자");
        superAdmin.setEmail("super@smartcon.com");
        superAdmin.setProvider(User.Provider.LOCAL);
        superAdmin.setIsActive(true);
        superAdmin.setTenantId(testTenant.getId()); // tenant_id 설정
        superAdmin = userRepository.save(superAdmin);
        
        // 테스트 구독들 생성
        basicSubscription = createSubscription(testTenant, basicPlan);
        premiumSubscription = createSubscription(premiumTenant, premiumPlan);
    }

    @Test
    @DisplayName("기본 플랜 수동 승인 시나리오")
    void testBasicPlanManualApprovalScenario() {
        // Given: 기본 플랜 구독이 승인 대기 상태
        assertThat(basicSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        
        // When: 수동 승인 처리
        String approvalReason = "기본 플랜 승인 완료";
        SubscriptionDto approvedSubscription = approvalService.approveSubscription(
            basicSubscription.getId(), approvalReason);
        
        // Then: 승인 완료 및 구독 활성화
        assertThat(approvedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 데이터베이스에서 확인
        Subscription updatedSubscription = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(updatedSubscription.getApprovedAt()).isNotNull();
        
        // 승인 이력 확인
        List<SubscriptionApproval> approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(basicSubscription.getId());
        assertThat(approvalHistory).isNotEmpty();
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
        assertThat(approvalHistory.get(0).getReason()).isEqualTo(approvalReason);
    }

    @Test
    @DisplayName("승인 거부 후 재신청 시나리오")
    void testRejectionAndReapplicationScenario() {
        // Given: 첫 번째 승인 요청 및 거부
        String rejectionReason = "서류 미비";
        SubscriptionDto rejectedSubscription = approvalService.rejectSubscription(
            basicSubscription.getId(), rejectionReason);
        
        assertThat(rejectedSubscription.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        
        // When: 새로운 구독 생성 (재신청)
        Subscription reapplicationSubscription = createSubscription(testTenant, basicPlan);
        
        // Then: 새로운 구독이 승인 대기 상태로 생성됨
        assertThat(reapplicationSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        assertThat(reapplicationSubscription.getId()).isNotEqualTo(basicSubscription.getId());
        
        // When: 재신청 구독 승인
        String reapprovalReason = "서류 보완 후 승인";
        SubscriptionDto reapprovedSubscription = approvalService.approveSubscription(
            reapplicationSubscription.getId(), reapprovalReason);
        
        // Then: 재신청 구독이 승인됨
        assertThat(reapprovedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 기존 거부된 구독은 여전히 거부 상태
        Subscription originalSubscription = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        assertThat(originalSubscription).isNotNull();
        assertThat(originalSubscription.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
    }

    @Test
    @DisplayName("동시 다중 승인 요청 처리 시나리오")
    void testConcurrentMultipleApprovalScenario() {
        // Given: 여러 테넌트의 동시 승인 요청
        Tenant tenant2 = createAnotherTenant("234-56-78901", "회사2");
        Tenant tenant3 = createAnotherTenant("345-67-89012", "회사3");
        
        Subscription subscription2 = createSubscription(tenant2, basicPlan);
        Subscription subscription3 = createSubscription(tenant3, premiumPlan);
        
        // When: 선택적 승인/거부 처리
        approvalService.approveSubscription(basicSubscription.getId(), "회사1 승인");
        approvalService.rejectSubscription(subscription2.getId(), "회사2 거부");
        approvalService.approveSubscription(subscription3.getId(), "회사3 승인");
        
        // Then: 각각 올바른 상태로 변경됨
        Subscription updatedSub1 = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        Subscription updatedSub2 = subscriptionRepository.findById(subscription2.getId()).orElse(null);
        Subscription updatedSub3 = subscriptionRepository.findById(subscription3.getId()).orElse(null);
        
        assertThat(updatedSub1.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(updatedSub2.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        assertThat(updatedSub3.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 각각의 승인 이력 확인
        List<SubscriptionApproval> history1 = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(basicSubscription.getId());
        List<SubscriptionApproval> history2 = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(subscription2.getId());
        List<SubscriptionApproval> history3 = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(subscription3.getId());
        
        assertThat(history1.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
        assertThat(history2.get(0).getAction()).isEqualTo(ApprovalAction.REJECT);
        assertThat(history3.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
    }

    @Test
    @DisplayName("구독 생명주기 전체 시나리오")
    void testCompleteSubscriptionLifecycleScenario() {
        // Given: 승인 대기 중인 구독
        assertThat(basicSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        
        // When: 1. 승인
        approvalService.approveSubscription(basicSubscription.getId(), "초기 승인");
        
        // Then: 활성 상태
        Subscription subscription = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // When: 2. 중지
        approvalService.suspendSubscription(basicSubscription.getId(), "결제 지연");
        
        // Then: 중지 상태
        subscription = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(subscription.getSuspensionReason()).isEqualTo("결제 지연");
        
        // When: 3. 재활성화
        approvalService.reactivateSubscription(basicSubscription.getId(), "결제 완료");
        
        // Then: 다시 활성 상태
        subscription = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // When: 4. 종료
        approvalService.terminateSubscription(basicSubscription.getId(), "계약 만료");
        
        // Then: 종료 상태
        subscription = subscriptionRepository.findById(basicSubscription.getId()).orElse(null);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        assertThat(subscription.getTerminationReason()).isEqualTo("계약 만료");
        
        // 전체 이력 확인 (승인, 중지, 재활성화, 종료)
        List<SubscriptionApproval> fullHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(basicSubscription.getId());
        assertThat(fullHistory).hasSize(4);
        assertThat(fullHistory.get(0).getAction()).isEqualTo(ApprovalAction.TERMINATE);
        assertThat(fullHistory.get(1).getAction()).isEqualTo(ApprovalAction.REACTIVATE);
        assertThat(fullHistory.get(2).getAction()).isEqualTo(ApprovalAction.SUSPEND);
        assertThat(fullHistory.get(3).getAction()).isEqualTo(ApprovalAction.APPROVE);
    }

    @Test
    @DisplayName("승인 대기 목록 필터링 시나리오")
    void testPendingApprovalsFilteringScenario() {
        // Given: 다양한 상태의 구독들 생성
        Subscription subscription2 = createAnotherTestSubscription("234-56-78901", "회사2");
        Subscription subscription3 = createAnotherTestSubscription("345-67-89012", "회사3");
        
        // 일부 구독 승인 처리
        approvalService.approveSubscription(subscription2.getId(), "승인");
        
        // When: 승인 대기 목록 조회
        List<SubscriptionDto> pendingApprovals = approvalService.getPendingApprovalsOptimized(10, 0);
        
        // Then: 승인 대기 상태인 구독들만 조회됨
        assertThat(pendingApprovals).hasSizeGreaterThanOrEqualTo(2); // basicSubscription, subscription3
        
        for (SubscriptionDto subscription : pendingApprovals) {
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        }
        
        // 승인 대기 개수 확인
        long pendingCount = approvalService.countPendingApprovalsOptimized();
        assertThat(pendingCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("대량 승인 요청 처리 성능 시나리오")
    void testBulkApprovalProcessingScenario() {
        // Given: 대량 승인 요청 생성 (5개)
        for (int i = 0; i < 5; i++) {
            Tenant tenant = createAnotherTenant("200-00-0000" + i, "대량테스트회사" + i);
            createSubscription(tenant, basicPlan);
        }
        
        // When: 승인 대기 목록 조회
        List<SubscriptionDto> allPendingApprovals = approvalService.getPendingApprovalsOptimized(20, 0);
        
        // Then: 모든 요청이 정상적으로 생성됨
        assertThat(allPendingApprovals).hasSizeGreaterThanOrEqualTo(6); // 기존 1개 + 새로 생성된 5개
        
        // When: 일괄 승인 처리 (처음 3개만)
        int approvedCount = 0;
        for (SubscriptionDto subscription : allPendingApprovals) {
            if (approvedCount < 3) {
                approvalService.approveSubscription(subscription.getId(), "일괄 승인 처리 " + approvedCount);
                approvedCount++;
            }
        }
        
        // Then: 3개가 승인되고 나머지는 여전히 대기 상태
        long remainingPending = approvalService.countPendingApprovalsOptimized();
        assertThat(remainingPending).isGreaterThanOrEqualTo(3); // 최소 3개는 여전히 대기 상태
    }

    @Test
    @DisplayName("잘못된 구독 상태 변경 시도 시나리오")
    void testInvalidStatusChangeScenario() {
        // Given: 이미 거부된 구독
        approvalService.rejectSubscription(basicSubscription.getId(), "거부됨");
        
        // When & Then: 거부된 구독을 다시 승인하려고 시도하면 예외 발생
        assertThatThrownBy(() -> 
            approvalService.approveSubscription(basicSubscription.getId(), "재승인 시도"))
            .isInstanceOf(com.smartcon.domain.subscription.service.IllegalStateTransitionException.class);
    }

    @Test
    @DisplayName("존재하지 않는 구독 처리 시나리오")
    void testNonExistentSubscriptionScenario() {
        // Given: 존재하지 않는 구독 ID
        Long invalidSubscriptionId = 99999L;
        
        // When & Then: 존재하지 않는 구독에 대한 승인 시도 시 예외 발생
        assertThatThrownBy(() -> 
            approvalService.approveSubscription(invalidSubscriptionId, "존재하지 않는 구독"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private Subscription createSubscription(Tenant tenant, SubscriptionPlan plan) {
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .monthlyPrice(plan.getMonthlyPrice())
                .billingCycle(BillingCycle.MONTHLY)
                .autoRenewal(true)
                .build();
        return subscriptionRepository.save(subscription);
    }

    private Tenant createAnotherTenant(String businessNo, String companyName) {
        Tenant tenant = new Tenant();
        tenant.setBusinessNo(businessNo);
        tenant.setCompanyName(companyName);
        tenant.setRepresentativeName("대표자");
        tenant.setStatus(Tenant.SubscriptionStatus.TRIAL);
        tenant.setEmail(companyName.toLowerCase() + "@test.com");
        return tenantRepository.save(tenant);
    }

    private Subscription createAnotherTestSubscription(String businessNo, String companyName) {
        Tenant tenant = createAnotherTenant(businessNo, companyName);
        return createSubscription(tenant, basicPlan);
    }
}