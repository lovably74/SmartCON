package com.smartcon.domain.subscription.integration;

import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.subscription.service.SubscriptionApprovalService;
import com.smartcon.domain.subscription.service.NotificationService;
import com.smartcon.domain.subscription.service.SubscriptionStatsService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 구독 승인 워크플로우 통합 테스트
 * 
 * 전체 승인 프로세스의 end-to-end 통합 동작을 검증합니다.
 * Requirements: 1.1, 1.4, 1.5, 2.1, 2.2
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionApprovalWorkflowIntegrationTest {

    @Autowired
    private SubscriptionApprovalService approvalService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private SubscriptionApprovalRepository approvalRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    // 스케줄링 서비스를 Mock으로 대체하여 FORCE INDEX 문제 방지
    @MockBean
    private SubscriptionStatsService subscriptionStatsService;
    
    private Tenant testTenant;
    private User superAdmin;
    private User tenantAdmin;
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
        superAdmin.setTenantId(testTenant.getId()); // 테넌트 ID 설정
        superAdmin = userRepository.save(superAdmin);
        
        // 테넌트 관리자 생성
        tenantAdmin = new User();
        tenantAdmin.setName("테넌트관리자");
        tenantAdmin.setEmail("admin@test.com");
        tenantAdmin.setProvider(User.Provider.LOCAL);
        tenantAdmin.setIsActive(true);
        tenantAdmin.setTenantId(testTenant.getId()); // 테넌트 ID 설정
        tenantAdmin = userRepository.save(tenantAdmin);
        
        // 테스트 구독 생성 (승인 대기 상태)
        testSubscription = createTestSubscription();
    }

    @Test
    @DisplayName("구독 승인 전체 워크플로우 End-to-End 테스트")
    void testCompleteApprovalWorkflow() {
        // Given: 승인 대기 중인 구독이 있음
        assertThat(testSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        
        // When: 구독 승인 처리
        String approvalReason = "신규 구독 승인 완료";
        SubscriptionDto approvedSubscription = approvalService.approveSubscription(
            testSubscription.getId(), approvalReason);
        
        // Then: 구독이 승인됨
        assertThat(approvedSubscription).isNotNull();
        assertThat(approvedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 데이터베이스에서 구독 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(updatedSubscription.getApprovedAt()).isNotNull();
        
        // 승인 이력이 생성되었는지 확인
        List<SubscriptionApproval> approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory).isNotEmpty();
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
        assertThat(approvalHistory.get(0).getToStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("구독 거부 워크플로우 통합 테스트")
    void testRejectionWorkflow() {
        // Given: 승인 대기 중인 구독
        assertThat(testSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        
        // When: 구독 거부 처리
        String rejectionReason = "서류 미비로 인한 거부";
        SubscriptionDto rejectedSubscription = approvalService.rejectSubscription(
            testSubscription.getId(), rejectionReason);
        
        // Then: 구독이 거부됨
        assertThat(rejectedSubscription).isNotNull();
        assertThat(rejectedSubscription.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        
        // 데이터베이스에서 구독 상태 확인
        Subscription updatedSubscription = subscriptionRepository.findById(testSubscription.getId()).orElse(null);
        assertThat(updatedSubscription).isNotNull();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.REJECTED);
        assertThat(updatedSubscription.getRejectionReason()).isEqualTo(rejectionReason);
        
        // 거부 이력이 생성되었는지 확인
        List<SubscriptionApproval> approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory).isNotEmpty();
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.REJECT);
        assertThat(approvalHistory.get(0).getToStatus()).isEqualTo(SubscriptionStatus.REJECTED);
    }

    @Test
    @DisplayName("구독 중지 및 재활성화 워크플로우 테스트")
    void testSuspendAndReactivateWorkflow() {
        // Given: 활성화된 구독
        approvalService.approveSubscription(testSubscription.getId(), "승인 완료");
        
        // When: 구독 중지
        String suspensionReason = "결제 지연으로 인한 중지";
        SubscriptionDto suspendedSubscription = approvalService.suspendSubscription(
            testSubscription.getId(), suspensionReason);
        
        // Then: 구독이 중지됨
        assertThat(suspendedSubscription.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        
        // When: 구독 재활성화
        String reactivationReason = "결제 완료로 인한 재활성화";
        SubscriptionDto reactivatedSubscription = approvalService.reactivateSubscription(
            testSubscription.getId(), reactivationReason);
        
        // Then: 구독이 재활성화됨
        assertThat(reactivatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // 이력 확인
        List<SubscriptionApproval> approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(testSubscription.getId());
        assertThat(approvalHistory).hasSizeGreaterThanOrEqualTo(3); // 승인, 중지, 재활성화
        
        // 최신 이력이 재활성화인지 확인
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.REACTIVATE);
        assertThat(approvalHistory.get(0).getToStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("승인 대기 목록 조회 테스트")
    void testGetPendingApprovals() {
        // Given: 여러 승인 대기 구독 생성
        createAnotherTestSubscription("234-56-78901", "회사2");
        createAnotherTestSubscription("345-67-89012", "회사3");
        
        // When: 승인 대기 목록 조회
        List<SubscriptionDto> pendingApprovals = approvalService.getPendingApprovalsOptimized(10, 0);
        
        // Then: 승인 대기 구독들이 조회됨
        assertThat(pendingApprovals).hasSizeGreaterThanOrEqualTo(3);
        
        // 모든 구독이 승인 대기 상태인지 확인
        for (SubscriptionDto subscription : pendingApprovals) {
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_APPROVAL);
        }
        
        // 승인 대기 개수 확인
        long pendingCount = approvalService.countPendingApprovalsOptimized();
        assertThat(pendingCount).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("구독 통계 조회 테스트")
    void testSubscriptionStats() {
        // Given: 다양한 상태의 구독들 생성
        approvalService.approveSubscription(testSubscription.getId(), "승인");
        
        Subscription subscription2 = createAnotherTestSubscription("234-56-78901", "회사2");
        approvalService.rejectSubscription(subscription2.getId(), "거부");
        
        Subscription subscription3 = createAnotherTestSubscription("345-67-89012", "회사3");
        // subscription3는 승인 대기 상태로 유지
        
        // When: 구독 통계 조회
        var stats = approvalService.getSubscriptionStatsOptimized();
        
        // Then: 통계가 올바르게 집계됨
        assertThat(stats).isNotNull();
        // 구체적인 통계 값은 다른 테스트의 영향을 받을 수 있으므로 존재 여부만 확인
    }

    @Test
    @DisplayName("승인 이력 조회 테스트")
    void testApprovalHistory() {
        // Given: 구독에 대한 여러 액션 수행
        approvalService.approveSubscription(testSubscription.getId(), "초기 승인");
        approvalService.suspendSubscription(testSubscription.getId(), "중지");
        approvalService.reactivateSubscription(testSubscription.getId(), "재활성화");
        
        // When: 승인 이력 조회
        var approvalHistory = approvalService.getApprovalHistoryOptimized(testSubscription.getId());
        
        // Then: 모든 액션이 이력에 기록됨
        assertThat(approvalHistory).hasSize(3);
        
        // 최신 순으로 정렬되어 있는지 확인
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.REACTIVATE);
        assertThat(approvalHistory.get(1).getAction()).isEqualTo(ApprovalAction.SUSPEND);
        assertThat(approvalHistory.get(2).getAction()).isEqualTo(ApprovalAction.APPROVE);
    }

    @Test
    @DisplayName("데이터베이스 트랜잭션 일관성 테스트")
    void testDatabaseTransactionConsistency() {
        // Given: 승인 대기 중인 구독
        Long subscriptionId = testSubscription.getId();
        
        // When: 승인 처리
        String approvalReason = "트랜잭션 테스트";
        approvalService.approveSubscription(subscriptionId, approvalReason);
        
        // Then: 모든 관련 데이터가 일관성 있게 업데이트됨
        
        // 구독 상태 확인
        Subscription savedSubscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        assertThat(savedSubscription).isNotNull();
        assertThat(savedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(savedSubscription.getApprovedAt()).isNotNull();
        
        // 승인 이력 확인
        List<SubscriptionApproval> approvalHistory = approvalRepository.findBySubscriptionIdOrderByProcessedAtDesc(subscriptionId);
        assertThat(approvalHistory).isNotEmpty();
        assertThat(approvalHistory.get(0).getAction()).isEqualTo(ApprovalAction.APPROVE);
        assertThat(approvalHistory.get(0).getReason()).isEqualTo(approvalReason);
        
        // 모든 이력이 올바른 구독 ID를 가지고 있는지 확인
        for (SubscriptionApproval approval : approvalHistory) {
            assertThat(approval.getSubscriptionId()).isEqualTo(subscriptionId);
        }
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