package com.smartcon.domain.admin.controller;

import com.smartcon.domain.admin.dto.DashboardStatsDto;
import com.smartcon.domain.admin.service.SuperAdminService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
}