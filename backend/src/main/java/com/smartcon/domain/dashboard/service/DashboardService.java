package com.smartcon.domain.dashboard.service;

import com.smartcon.domain.dashboard.dto.DashboardDataResponse;
import com.smartcon.domain.user.entity.Role;

/**
 * 대시보드 서비스 인터페이스
 * 역할별 대시보드 데이터 생성 및 KPI 지표 계산
 */
public interface DashboardService {

    /**
     * 역할별 대시보드 데이터 조회
     * 
     * @param userId 사용자 ID
     * @param role 사용자 역할
     * @param siteId 현장 ID (현장관리자, 팀장, 노무자의 경우 필수)
     * @return 역할별 대시보드 데이터
     */
    DashboardDataResponse getDashboardData(Long userId, Role role, Long siteId);

    /**
     * 슈퍼관리자 대시보드 데이터 조회
     * 
     * @return 슈퍼관리자 대시보드 데이터
     */
    DashboardDataResponse.SuperAdminData getSuperAdminDashboard();

    /**
     * 본사관리자 대시보드 데이터 조회
     * 
     * @param tenantId 테넌트 ID
     * @return 본사관리자 대시보드 데이터
     */
    DashboardDataResponse.HqAdminData getHqAdminDashboard(String tenantId);

    /**
     * 현장관리자 대시보드 데이터 조회
     * 
     * @param siteId 현장 ID
     * @return 현장관리자 대시보드 데이터
     */
    DashboardDataResponse.SiteManagerData getSiteManagerDashboard(Long siteId);

    /**
     * 노무팀장 대시보드 데이터 조회
     * 
     * @param teamId 팀 ID
     * @param siteId 현장 ID
     * @return 노무팀장 대시보드 데이터
     */
    DashboardDataResponse.TeamLeaderData getTeamLeaderDashboard(Long teamId, Long siteId);

    /**
     * 일반노무자 대시보드 데이터 조회
     * 
     * @param workerId 노무자 ID
     * @param siteId 현장 ID
     * @return 일반노무자 대시보드 데이터
     */
    DashboardDataResponse.WorkerData getWorkerDashboard(Long workerId, Long siteId);

    /**
     * 공통 KPI 데이터 조회
     * 
     * @param role 사용자 역할
     * @param siteId 현장 ID (역할에 따라 선택적)
     * @return 공통 KPI 데이터
     */
    DashboardDataResponse.CommonKpiData getCommonKpiData(Role role, Long siteId);
}
