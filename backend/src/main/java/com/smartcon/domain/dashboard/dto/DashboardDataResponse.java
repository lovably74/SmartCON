package com.smartcon.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 역할별 대시보드 데이터 응답 DTO
 * 각 역할(슈퍼관리자, 본사관리자, 현장관리자, 팀장, 노무자)에 맞는 KPI 및 통계 데이터 제공
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDataResponse {

    // 공통 KPI 지표
    private CommonKpiData commonKpi;

    // 역할별 특화 데이터
    private SuperAdminData superAdminData;
    private HqAdminData hqAdminData;
    private SiteManagerData siteManagerData;
    private TeamLeaderData teamLeaderData;
    private WorkerData workerData;

    /**
     * 공통 KPI 데이터 (모든 역할에 공통)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommonKpiData {
        private Long activeProjectCount; // 활성 프로젝트 수
        private Long registeredWorkerCount; // 등록된 노무자 수
        private Long unsignedContractCount; // 미서명 계약서 수
        private Long todayAttendanceCount; // 금일 출역자 수
        private BigDecimal todayTotalWorkHours; // 금일 총 근무시간
    }

    /**
     * 슈퍼관리자 대시보드 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuperAdminData {
        private Long totalTenantCount; // 전체 테넌트 수
        private Long activeTenantCount; // 활성 테넌트 수
        private Long pendingSubscriptionCount; // 승인 대기 구독 수
        private Long totalUserCount; // 전체 사용자 수
        private Long totalProjectCount; // 전체 프로젝트 수
        private BigDecimal monthlyRevenue; // 월간 매출
        private List<TenantSummary> recentTenants; // 최근 가입 테넌트
    }

    /**
     * 본사관리자 대시보드 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HqAdminData {
        private Long totalSiteCount; // 전체 현장 수
        private Long activeSiteCount; // 활성 현장 수
        private Long totalWorkerCount; // 전체 노무자 수
        private Long activeWorkerCount; // 활성 노무자 수
        private BigDecimal monthlyLaborCost; // 월간 인건비
        private BigDecimal averageAttendanceRate; // 평균 출역률
        private List<SiteSummary> topSitesByWorkerCount; // 노무자 수 상위 현장
        private List<ContractStatusSummary> contractStatusByMonth; // 월별 계약 현황
    }

    /**
     * 현장관리자 대시보드 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SiteManagerData {
        private Long siteId; // 현장 ID
        private String siteName; // 현장명
        private Long totalWorkerCount; // 전체 노무자 수
        private Long todayAttendanceCount; // 금일 출역자 수
        private Long todayAbsentCount; // 금일 결근자 수
        private BigDecimal todayAttendanceRate; // 금일 출역률
        private BigDecimal weeklyAttendanceRate; // 주간 출역률
        private Long pendingContractCount; // 서명 대기 계약서 수
        private Long faceDeviceCount; // 안면인식기 수
        private Long faceDeviceSyncedCount; // 동기화 완료 안면인식기 수
        private List<TeamSummary> teamSummaries; // 팀별 요약
        private List<JobTypeSummary> jobTypeSummaries; // 공종별 요약
    }

    /**
     * 노무팀장 대시보드 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamLeaderData {
        private Long teamId; // 팀 ID
        private String teamName; // 팀명
        private Long teamMemberCount; // 팀원 수
        private Long todayAttendanceCount; // 금일 출역자 수
        private Long todayAbsentCount; // 금일 결근자 수
        private BigDecimal todayAttendanceRate; // 금일 출역률
        private BigDecimal weeklyAverageWorkHours; // 주간 평균 근무시간
        private Long pendingContractCount; // 서명 대기 계약서 수
        private List<WorkerSummary> workerSummaries; // 팀원별 요약
    }

    /**
     * 일반노무자 대시보드 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkerData {
        private Long workerId; // 노무자 ID
        private String workerName; // 노무자명
        private Long currentSiteId; // 현재 현장 ID
        private String currentSiteName; // 현재 현장명
        private Integer monthlyAttendanceDays; // 월간 출역일수
        private BigDecimal monthlyTotalWorkHours; // 월간 총 근무시간
        private BigDecimal monthlyEstimatedWage; // 월간 예상 급여
        private Long pendingContractCount; // 서명 대기 계약서 수
        private Boolean isFaceRegistered; // 안면 등록 여부
        private Integer profileCompletionPercentage; // 프로필 완성도
        private List<RecentAttendance> recentAttendances; // 최근 출역 기록
    }

    /**
     * 테넌트 요약 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TenantSummary {
        private Long tenantId;
        private String tenantName;
        private String subscriptionStatus;
        private Long projectCount;
        private Long workerCount;
        private String createdAt;
    }

    /**
     * 현장 요약 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SiteSummary {
        private Long siteId;
        private String siteName;
        private Long workerCount;
        private Long todayAttendanceCount;
        private BigDecimal attendanceRate;
        private String status;
    }

    /**
     * 계약 현황 요약
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractStatusSummary {
        private String month; // YYYY-MM
        private Long signedCount;
        private Long pendingCount;
        private Long expiredCount;
    }

    /**
     * 팀 요약 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamSummary {
        private Long teamId;
        private String teamName;
        private Long memberCount;
        private Long todayAttendanceCount;
        private BigDecimal attendanceRate;
    }

    /**
     * 공종별 요약 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobTypeSummary {
        private String jobType;
        private String jobTypeName;
        private Long workerCount;
        private Long todayAttendanceCount;
    }

    /**
     * 노무자 요약 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkerSummary {
        private Long workerId;
        private String workerName;
        private String jobType;
        private Boolean isTodayAttended;
        private BigDecimal weeklyWorkHours;
        private String contractStatus;
    }

    /**
     * 최근 출역 기록
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentAttendance {
        private String workDate; // YYYY-MM-DD
        private String checkInTime; // HH:mm
        private String checkOutTime; // HH:mm
        private BigDecimal workHours;
        private String dataSource; // FACE_RECOGNITION, MANUAL_ENTRY, ADMIN_CORRECTION
    }
}
