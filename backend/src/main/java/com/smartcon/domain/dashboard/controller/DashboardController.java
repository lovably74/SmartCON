package com.smartcon.domain.dashboard.controller;

import com.smartcon.domain.dashboard.dto.DashboardDataResponse;
import com.smartcon.domain.dashboard.service.DashboardService;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 대시보드 API 컨트롤러
 * 역할별 대시보드 데이터 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 역할별 대시보드 데이터 조회
     * 
     * @param userId 사용자 ID
     * @param role 사용자 역할
     * @param siteId 현장 ID (선택적)
     * @return 역할별 대시보드 데이터
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDataResponse>> getDashboardData(
            @RequestParam Long userId,
            @RequestParam Role role,
            @RequestParam(required = false) Long siteId) {
        
        log.info("[대시보드 API] 사용자 ID: {}, 역할: {}, 현장 ID: {}", userId, role, siteId);

        DashboardDataResponse dashboardData = dashboardService.getDashboardData(userId, role, siteId);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    /**
     * 슈퍼관리자 대시보드 데이터 조회
     * 
     * @return 슈퍼관리자 대시보드 데이터
     */
    @GetMapping("/super-admin")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<ApiResponse<DashboardDataResponse.SuperAdminData>> getSuperAdminDashboard() {
        log.info("[슈퍼관리자 대시보드 API] 데이터 조회");

        DashboardDataResponse.SuperAdminData dashboardData = dashboardService.getSuperAdminDashboard();

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    /**
     * 본사관리자 대시보드 데이터 조회
     * 
     * @param tenantId 테넌트 ID
     * @return 본사관리자 대시보드 데이터
     */
    @GetMapping("/hq-admin")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<DashboardDataResponse.HqAdminData>> getHqAdminDashboard(
            @RequestParam String tenantId) {
        
        log.info("[본사관리자 대시보드 API] 테넌트 ID: {}", tenantId);

        DashboardDataResponse.HqAdminData dashboardData = dashboardService.getHqAdminDashboard(tenantId);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    /**
     * 현장관리자 대시보드 데이터 조회
     * 
     * @param siteId 현장 ID
     * @return 현장관리자 대시보드 데이터
     */
    @GetMapping("/site-manager")
    @PreAuthorize("hasRole('SITE')")
    public ResponseEntity<ApiResponse<DashboardDataResponse.SiteManagerData>> getSiteManagerDashboard(
            @RequestParam Long siteId) {
        
        log.info("[현장관리자 대시보드 API] 현장 ID: {}", siteId);

        DashboardDataResponse.SiteManagerData dashboardData = dashboardService.getSiteManagerDashboard(siteId);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    /**
     * 노무팀장 대시보드 데이터 조회
     * 
     * @param teamId 팀 ID
     * @param siteId 현장 ID
     * @return 노무팀장 대시보드 데이터
     */
    @GetMapping("/team-leader")
    @PreAuthorize("hasRole('TEAM')")
    public ResponseEntity<ApiResponse<DashboardDataResponse.TeamLeaderData>> getTeamLeaderDashboard(
            @RequestParam Long teamId,
            @RequestParam Long siteId) {
        
        log.info("[노무팀장 대시보드 API] 팀 ID: {}, 현장 ID: {}", teamId, siteId);

        DashboardDataResponse.TeamLeaderData dashboardData = dashboardService.getTeamLeaderDashboard(teamId, siteId);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    /**
     * 일반노무자 대시보드 데이터 조회
     * 
     * @param workerId 노무자 ID
     * @param siteId 현장 ID
     * @return 일반노무자 대시보드 데이터
     */
    @GetMapping("/worker")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<DashboardDataResponse.WorkerData>> getWorkerDashboard(
            @RequestParam Long workerId,
            @RequestParam Long siteId) {
        
        log.info("[일반노무자 대시보드 API] 노무자 ID: {}, 현장 ID: {}", workerId, siteId);

        DashboardDataResponse.WorkerData dashboardData = dashboardService.getWorkerDashboard(workerId, siteId);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    /**
     * 공통 KPI 데이터 조회
     * 
     * @param role 사용자 역할
     * @param siteId 현장 ID (선택적)
     * @return 공통 KPI 데이터
     */
    @GetMapping("/common-kpi")
    public ResponseEntity<ApiResponse<DashboardDataResponse.CommonKpiData>> getCommonKpiData(
            @RequestParam Role role,
            @RequestParam(required = false) Long siteId) {
        
        log.info("[공통 KPI API] 역할: {}, 현장 ID: {}", role, siteId);

        DashboardDataResponse.CommonKpiData kpiData = dashboardService.getCommonKpiData(role, siteId);

        return ResponseEntity.ok(ApiResponse.success(kpiData));
    }
}
