package com.smartcon.domain.admin.controller;

import com.smartcon.domain.admin.dto.ApprovalStatsDto;
import com.smartcon.domain.admin.dto.DashboardStatsDto;
import com.smartcon.domain.admin.dto.SubscriptionExportDto;
import com.smartcon.domain.admin.service.SuperAdminService;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.global.config.MultiTenantConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 슈퍼관리자 컨트롤러 단위 테스트
 */
@WebMvcTest(value = SuperAdminController.class, 
           excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MultiTenantConfig.class))
@DisplayName("슈퍼관리자 컨트롤러 테스트")
class SuperAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuperAdminService superAdminService;

    @Test
    @DisplayName("대시보드 통계 조회 API 테스트")
    @WithMockUser(roles = "SUPER")
    void getDashboardStats_ShouldReturnStats() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalTenants(15L);
        stats.setActiveTenants(10L);
        stats.setSuspendedTenants(3L);
        stats.setNewTenantsThisMonth(5L);
        stats.setTotalUsers(150L);
        stats.setNewUsersThisMonth(25L);
        stats.setTotalRevenue(BigDecimal.valueOf(1000000));
        stats.setMonthlyRevenue(BigDecimal.valueOf(100000));
        stats.setCompletedPayments(100L);
        stats.setFailedPayments(5L);
        stats.setPendingPayments(10L);
        stats.setSystemStatus("HEALTHY");
        stats.setActiveConnections(0L);

        when(superAdminService.getDashboardStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTenants").value(15))
                .andExpect(jsonPath("$.data.activeTenants").value(10))
                .andExpect(jsonPath("$.data.suspendedTenants").value(3))
                .andExpect(jsonPath("$.data.totalUsers").value(150))
                .andExpect(jsonPath("$.data.systemStatus").value("HEALTHY"));
    }

    @Test
    @DisplayName("테넌트 상태 변경 API 테스트")
    @WithMockUser(roles = "SUPER")
    void updateTenantStatus_ShouldReturnSuccess() throws Exception {
        // Given
        Long tenantId = 1L;
        doNothing().when(superAdminService).updateTenantStatus(eq(tenantId), eq(Tenant.SubscriptionStatus.SUSPENDED));

        // When & Then
        mockMvc.perform(patch("/api/v1/admin/tenants/{id}/status", tenantId)
                .param("status", "SUSPENDED")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("시스템 상태 확인 API 테스트")
    @WithMockUser(roles = "SUPER")
    void getSystemHealth_ShouldReturnHealthy() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("HEALTHY"));
    }

    @Test
    @DisplayName("승인 대시보드 통계 조회 API 테스트")
    @WithMockUser(roles = "SUPER")
    void getApprovalStats_ShouldReturnApprovalStats() throws Exception {
        // Given
        ApprovalStatsDto stats = new ApprovalStatsDto();
        stats.setTotalSubscriptions(100L);
        stats.setPendingApprovals(15L);
        stats.setApprovedSubscriptions(70L);
        stats.setRejectedSubscriptions(10L);
        stats.setSuspendedSubscriptions(3L);
        stats.setTerminatedSubscriptions(2L);
        stats.setAutoApprovedCount(50L);
        stats.setManualApprovalCount(20L);
        stats.setAutoApprovalRate(71.43);
        stats.setAverageProcessingHours(24.5);
        stats.setPendingOverThreeDays(5L);

        when(superAdminService.getApprovalStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/approval/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSubscriptions").value(100))
                .andExpect(jsonPath("$.data.pendingApprovals").value(15))
                .andExpect(jsonPath("$.data.approvedSubscriptions").value(70))
                .andExpect(jsonPath("$.data.autoApprovedCount").value(50))
                .andExpect(jsonPath("$.data.autoApprovalRate").value(71.43))
                .andExpect(jsonPath("$.data.averageProcessingHours").value(24.5))
                .andExpect(jsonPath("$.data.pendingOverThreeDays").value(5));
    }

    @Test
    @DisplayName("구독 데이터 내보내기 API 테스트")
    @WithMockUser(roles = "SUPER")
    void exportSubscriptions_ShouldReturnExportData() throws Exception {
        // Given
        List<SubscriptionExportDto> exportData = Arrays.asList(
                SubscriptionExportDto.builder()
                        .subscriptionId(1L)
                        .tenantName("테스트 회사 A")
                        .planName("기본 요금제")
                        .status(SubscriptionStatus.ACTIVE)
                        .monthlyFee(BigDecimal.valueOf(50000))
                        .createdAt(LocalDateTime.now().minusDays(10))
                        .approvedAt(LocalDateTime.now().minusDays(9))
                        .adminName("관리자")
                        .autoApproved(false)
                        .build(),
                SubscriptionExportDto.builder()
                        .subscriptionId(2L)
                        .tenantName("테스트 회사 B")
                        .planName("프리미엄 요금제")
                        .status(SubscriptionStatus.PENDING_APPROVAL)
                        .monthlyFee(BigDecimal.valueOf(100000))
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .autoApproved(true)
                        .build()
        );

        when(superAdminService.exportSubscriptionData(isNull(), isNull(), isNull())).thenReturn(exportData);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/subscriptions/export")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].subscriptionId").value(1))
                .andExpect(jsonPath("$[0].tenantName").value("테스트 회사 A"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].subscriptionId").value(2))
                .andExpect(jsonPath("$[1].tenantName").value("테스트 회사 B"))
                .andExpect(jsonPath("$[1].status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("구독 데이터 내보내기 API - 상태 필터링 테스트")
    @WithMockUser(roles = "SUPER")
    void exportSubscriptions_WithStatusFilter_ShouldReturnFilteredData() throws Exception {
        // Given
        List<SubscriptionExportDto> exportData = Arrays.asList(
                SubscriptionExportDto.builder()
                        .subscriptionId(1L)
                        .tenantName("테스트 회사 A")
                        .planName("기본 요금제")
                        .status(SubscriptionStatus.ACTIVE)
                        .monthlyFee(BigDecimal.valueOf(50000))
                        .createdAt(LocalDateTime.now().minusDays(10))
                        .approvedAt(LocalDateTime.now().minusDays(9))
                        .adminName("관리자")
                        .autoApproved(false)
                        .build()
        );

        when(superAdminService.exportSubscriptionData(eq(SubscriptionStatus.ACTIVE), isNull(), isNull()))
                .thenReturn(exportData);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/subscriptions/export")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("승인 통계 조회 실패 시 오류 응답 테스트")
    @WithMockUser(roles = "SUPER")
    void getApprovalStats_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Given
        when(superAdminService.getApprovalStats()).thenThrow(new RuntimeException("데이터베이스 연결 오류"));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/approval/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("승인 통계 정보를 조회할 수 없습니다: 데이터베이스 연결 오류"));
    }
}