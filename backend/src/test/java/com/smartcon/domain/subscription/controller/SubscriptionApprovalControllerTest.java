package com.smartcon.domain.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcon.domain.subscription.dto.SubscriptionApprovalDto;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.subscription.dto.SubscriptionPlanDto;
import com.smartcon.domain.subscription.entity.ApprovalAction;
import com.smartcon.domain.subscription.entity.BillingCycle;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.service.SubscriptionApprovalService;
import com.smartcon.global.config.MultiTenantConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 구독 승인 컨트롤러 단위 테스트
 */
@WebMvcTest(value = SubscriptionApprovalController.class,
           excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MultiTenantConfig.class))
@DisplayName("구독 승인 컨트롤러 테스트")
class SubscriptionApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionApprovalService subscriptionApprovalService;

    @Test
    @DisplayName("승인 대기 구독 목록 조회 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void getPendingApprovals_ShouldReturnPendingSubscriptions() throws Exception {
        // Given
        SubscriptionDto subscription = createTestSubscriptionDto();
        Page<SubscriptionDto> pendingPage = new PageImpl<>(
                Arrays.asList(subscription),
                PageRequest.of(0, 20),
                1L
        );

        when(subscriptionApprovalService.getPendingApprovals(any(Pageable.class)))
                .thenReturn(pendingPage);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/subscription-approvals/pending")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("구독 승인 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void approveSubscription_WithValidRequest_ShouldReturnApprovedSubscription() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String reason = "정상적인 구독 신청으로 승인합니다.";
        SubscriptionDto approvedSubscription = createTestSubscriptionDto();
        approvedSubscription = SubscriptionDto.builder()
                .id(approvedSubscription.getId())
                .tenantId(approvedSubscription.getTenantId())
                .tenantName(approvedSubscription.getTenantName())
                .plan(approvedSubscription.getPlan())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(approvedSubscription.getStartDate())
                .endDate(approvedSubscription.getEndDate())
                .nextBillingDate(approvedSubscription.getNextBillingDate())
                .billingCycle(approvedSubscription.getBillingCycle())
                .monthlyPrice(approvedSubscription.getMonthlyPrice())
                .discountRate(approvedSubscription.getDiscountRate())
                .autoRenewal(approvedSubscription.getAutoRenewal())
                .trialEndDate(approvedSubscription.getTrialEndDate())
                .cancellationDate(approvedSubscription.getCancellationDate())
                .cancellationReason(approvedSubscription.getCancellationReason())
                .isTrial(approvedSubscription.getIsTrial())
                .isExpired(approvedSubscription.getIsExpired())
                .build();

        when(subscriptionApprovalService.approveSubscription(eq(subscriptionId), eq(reason)))
                .thenReturn(approvedSubscription);

        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason(reason);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/approve", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("구독 승인 - 사유 없음 (유효성 검사 실패)")
    @WithMockUser(roles = "SUPER_ADMIN")
    void approveSubscription_WithoutReason_ShouldReturnBadRequest() throws Exception {
        // Given
        Long subscriptionId = 1L;
        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason(""); // 빈 사유

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/approve", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("구독 승인 - 사유 길이 부족 (유효성 검사 실패)")
    @WithMockUser(roles = "SUPER_ADMIN")
    void approveSubscription_WithShortReason_ShouldReturnBadRequest() throws Exception {
        // Given
        Long subscriptionId = 1L;
        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason("짧음"); // 10자 미만

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/approve", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("구독 거부 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void rejectSubscription_WithValidRequest_ShouldReturnRejectedSubscription() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String reason = "결제 정보가 부정확하여 거부합니다.";
        SubscriptionDto rejectedSubscription = createTestSubscriptionDto();
        rejectedSubscription = SubscriptionDto.builder()
                .id(rejectedSubscription.getId())
                .tenantId(rejectedSubscription.getTenantId())
                .tenantName(rejectedSubscription.getTenantName())
                .plan(rejectedSubscription.getPlan())
                .status(SubscriptionStatus.REJECTED)
                .startDate(rejectedSubscription.getStartDate())
                .endDate(rejectedSubscription.getEndDate())
                .nextBillingDate(rejectedSubscription.getNextBillingDate())
                .billingCycle(rejectedSubscription.getBillingCycle())
                .monthlyPrice(rejectedSubscription.getMonthlyPrice())
                .discountRate(rejectedSubscription.getDiscountRate())
                .autoRenewal(rejectedSubscription.getAutoRenewal())
                .trialEndDate(rejectedSubscription.getTrialEndDate())
                .cancellationDate(rejectedSubscription.getCancellationDate())
                .cancellationReason(rejectedSubscription.getCancellationReason())
                .isTrial(rejectedSubscription.getIsTrial())
                .isExpired(rejectedSubscription.getIsExpired())
                .build();

        when(subscriptionApprovalService.rejectSubscription(eq(subscriptionId), eq(reason)))
                .thenReturn(rejectedSubscription);

        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason(reason);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/reject", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("구독 중지 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void suspendSubscription_WithValidRequest_ShouldReturnSuspendedSubscription() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String reason = "결제 연체로 인한 서비스 중지입니다.";
        SubscriptionDto suspendedSubscription = createTestSubscriptionDto();
        suspendedSubscription = SubscriptionDto.builder()
                .id(suspendedSubscription.getId())
                .tenantId(suspendedSubscription.getTenantId())
                .tenantName(suspendedSubscription.getTenantName())
                .plan(suspendedSubscription.getPlan())
                .status(SubscriptionStatus.SUSPENDED)
                .startDate(suspendedSubscription.getStartDate())
                .endDate(suspendedSubscription.getEndDate())
                .nextBillingDate(suspendedSubscription.getNextBillingDate())
                .billingCycle(suspendedSubscription.getBillingCycle())
                .monthlyPrice(suspendedSubscription.getMonthlyPrice())
                .discountRate(suspendedSubscription.getDiscountRate())
                .autoRenewal(suspendedSubscription.getAutoRenewal())
                .trialEndDate(suspendedSubscription.getTrialEndDate())
                .cancellationDate(suspendedSubscription.getCancellationDate())
                .cancellationReason(suspendedSubscription.getCancellationReason())
                .isTrial(suspendedSubscription.getIsTrial())
                .isExpired(suspendedSubscription.getIsExpired())
                .build();

        when(subscriptionApprovalService.suspendSubscription(eq(subscriptionId), eq(reason)))
                .thenReturn(suspendedSubscription);

        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason(reason);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/suspend", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    @DisplayName("구독 종료 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void terminateSubscription_WithValidRequest_ShouldReturnTerminatedSubscription() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String reason = "서비스 약관 위반으로 인한 계정 종료입니다.";
        SubscriptionDto terminatedSubscription = createTestSubscriptionDto();
        terminatedSubscription = SubscriptionDto.builder()
                .id(terminatedSubscription.getId())
                .tenantId(terminatedSubscription.getTenantId())
                .tenantName(terminatedSubscription.getTenantName())
                .plan(terminatedSubscription.getPlan())
                .status(SubscriptionStatus.TERMINATED)
                .startDate(terminatedSubscription.getStartDate())
                .endDate(terminatedSubscription.getEndDate())
                .nextBillingDate(terminatedSubscription.getNextBillingDate())
                .billingCycle(terminatedSubscription.getBillingCycle())
                .monthlyPrice(terminatedSubscription.getMonthlyPrice())
                .discountRate(terminatedSubscription.getDiscountRate())
                .autoRenewal(terminatedSubscription.getAutoRenewal())
                .trialEndDate(terminatedSubscription.getTrialEndDate())
                .cancellationDate(terminatedSubscription.getCancellationDate())
                .cancellationReason(terminatedSubscription.getCancellationReason())
                .isTrial(terminatedSubscription.getIsTrial())
                .isExpired(terminatedSubscription.getIsExpired())
                .build();

        when(subscriptionApprovalService.terminateSubscription(eq(subscriptionId), eq(reason)))
                .thenReturn(terminatedSubscription);

        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason(reason);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/terminate", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("TERMINATED"));
    }

    @Test
    @DisplayName("구독 재활성화 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void reactivateSubscription_WithValidRequest_ShouldReturnReactivatedSubscription() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String reason = "결제 문제가 해결되어 서비스를 재활성화합니다.";
        SubscriptionDto reactivatedSubscription = createTestSubscriptionDto();
        reactivatedSubscription = SubscriptionDto.builder()
                .id(reactivatedSubscription.getId())
                .tenantId(reactivatedSubscription.getTenantId())
                .tenantName(reactivatedSubscription.getTenantName())
                .plan(reactivatedSubscription.getPlan())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(reactivatedSubscription.getStartDate())
                .endDate(reactivatedSubscription.getEndDate())
                .nextBillingDate(reactivatedSubscription.getNextBillingDate())
                .billingCycle(reactivatedSubscription.getBillingCycle())
                .monthlyPrice(reactivatedSubscription.getMonthlyPrice())
                .discountRate(reactivatedSubscription.getDiscountRate())
                .autoRenewal(reactivatedSubscription.getAutoRenewal())
                .trialEndDate(reactivatedSubscription.getTrialEndDate())
                .cancellationDate(reactivatedSubscription.getCancellationDate())
                .cancellationReason(reactivatedSubscription.getCancellationReason())
                .isTrial(reactivatedSubscription.getIsTrial())
                .isExpired(reactivatedSubscription.getIsExpired())
                .build();

        when(subscriptionApprovalService.reactivateSubscription(eq(subscriptionId), eq(reason)))
                .thenReturn(reactivatedSubscription);

        SubscriptionApprovalController.ApprovalRequest request = new SubscriptionApprovalController.ApprovalRequest();
        request.setReason(reason);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/subscription-approvals/{subscriptionId}/reactivate", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("승인 이력 조회 - 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void getApprovalHistory_ShouldReturnApprovalHistory() throws Exception {
        // Given
        Long subscriptionId = 1L;
        List<SubscriptionApprovalDto> approvalHistory = Arrays.asList(
                createTestApprovalDto(1L, ApprovalAction.APPROVE),
                createTestApprovalDto(2L, ApprovalAction.SUSPEND)
        );

        when(subscriptionApprovalService.getApprovalHistory(eq(subscriptionId)))
                .thenReturn(approvalHistory);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/subscription-approvals/{subscriptionId}/history", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].action").value("APPROVE"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].action").value("SUSPEND"));
    }

    /**
     * 테스트용 구독 DTO 생성
     */
    private SubscriptionDto createTestSubscriptionDto() {
        SubscriptionPlanDto plan = SubscriptionPlanDto.builder()
                .id("basic")
                .name("기본 플랜")
                .description("기본 구독 플랜")
                .monthlyPrice(BigDecimal.valueOf(50000))
                .maxUsers(10)
                .maxSites(5)
                .features(Arrays.asList("기본 기능"))
                .isActive(true)
                .build();

        return SubscriptionDto.builder()
                .id(1L)
                .tenantId(1L)
                .tenantName("테스트 회사")
                .plan(plan)
                .status(SubscriptionStatus.PENDING_APPROVAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .monthlyPrice(BigDecimal.valueOf(50000))
                .discountRate(BigDecimal.ZERO)
                .autoRenewal(true)
                .trialEndDate(null)
                .cancellationDate(null)
                .cancellationReason(null)
                .isTrial(false)
                .isExpired(false)
                .build();
    }

    /**
     * 테스트용 승인 이력 DTO 생성
     */
    private SubscriptionApprovalDto createTestApprovalDto(Long id, ApprovalAction action) {
        return SubscriptionApprovalDto.builder()
                .id(id)
                .subscriptionId(1L)
                .adminId(1L)
                .adminName("슈퍼관리자")
                .fromStatus(SubscriptionStatus.PENDING_APPROVAL)
                .toStatus(action == ApprovalAction.APPROVE ? SubscriptionStatus.ACTIVE : SubscriptionStatus.SUSPENDED)
                .reason("테스트 사유")
                .action(action)
                .processedAt(LocalDateTime.now())
                .build();
    }
}