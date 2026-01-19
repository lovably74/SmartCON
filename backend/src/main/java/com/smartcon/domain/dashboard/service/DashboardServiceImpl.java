package com.smartcon.domain.dashboard.service;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.attendance.repository.AttendanceRecordRepository;
import com.smartcon.domain.contract.entity.Contract;
import com.smartcon.domain.contract.repository.ContractRepository;
import com.smartcon.domain.dashboard.dto.DashboardDataResponse;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.FaceRecognitionDeviceRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.subscription.entity.SubscriptionApproval;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.SubscriptionApprovalRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 대시보드 서비스 구현
 * 역할별 대시보드 데이터 생성 및 KPI 지표 계산
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ContractRepository contractRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionApprovalRepository subscriptionApprovalRepository;
    private final FaceRecognitionDeviceRepository faceDeviceRepository;

    @Override
    public DashboardDataResponse getDashboardData(Long userId, Role role, Long siteId) {
        log.info("[대시보드 데이터 조회] 사용자 ID: {}, 역할: {}, 현장 ID: {}", userId, role, siteId);

        DashboardDataResponse.DashboardDataResponseBuilder builder = DashboardDataResponse.builder();

        // 공통 KPI 데이터
        builder.commonKpi(getCommonKpiData(role, siteId));

        // 역할별 특화 데이터
        switch (role) {
            case ROLE_SUPER -> builder.superAdminData(getSuperAdminDashboard());
            case ROLE_HQ -> builder.hqAdminData(getHqAdminDashboard(TenantContext.getCurrentTenant()));
            case ROLE_SITE -> {
                if (siteId != null) {
                    builder.siteManagerData(getSiteManagerDashboard(siteId));
                }
            }
            case ROLE_TEAM -> {
                // TODO: 팀 엔티티 구현 후 추가
                log.warn("[대시보드] 팀장 대시보드는 아직 구현되지 않았습니다.");
            }
            case ROLE_WORKER -> {
                if (siteId != null) {
                    builder.workerData(getWorkerDashboard(userId, siteId));
                }
            }
        }

        return builder.build();
    }

    @Override
    public DashboardDataResponse.SuperAdminData getSuperAdminDashboard() {
        log.info("[슈퍼관리자 대시보드] 데이터 조회 시작");

        // 전체 테넌트 수
        long totalTenantCount = tenantRepository.count();

        // 활성 테넌트 수
        long activeTenantCount = tenantRepository.countByIsActive(true);

        // 승인 대기 구독 수
        long pendingSubscriptionCount = subscriptionApprovalRepository
                .countByToStatus(SubscriptionStatus.PENDING_APPROVAL);

        // 전체 사용자 수
        long totalUserCount = userRepository.count();

        // 전체 프로젝트 수
        long totalProjectCount = projectRepository.count();

        // 월간 매출 (TODO: 실제 결제 데이터 연동 필요)
        BigDecimal monthlyRevenue = BigDecimal.ZERO;

        // 최근 가입 테넌트 (최근 5개)
        List<Tenant> recentTenants = tenantRepository.findTop5ByOrderByCreatedAtDesc();
        List<DashboardDataResponse.TenantSummary> tenantSummaries = recentTenants.stream()
                .map(tenant -> {
                    // Tenant는 BaseEntity를 상속하므로 getId()로 ID를 가져옴
                    long projectCount = projectRepository.countByTenantId(String.valueOf(tenant.getId()));
                    long workerCount = userRepository.countByTenantIdAndRolesContaining(
                            String.valueOf(tenant.getId()), Role.ROLE_WORKER);

                    return DashboardDataResponse.TenantSummary.builder()
                            .tenantId(tenant.getId())
                            .tenantName(tenant.getCompanyName())
                            .subscriptionStatus(tenant.getStatus().name())
                            .projectCount(projectCount)
                            .workerCount(workerCount)
                            .createdAt(tenant.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardDataResponse.SuperAdminData.builder()
                .totalTenantCount(totalTenantCount)
                .activeTenantCount(activeTenantCount)
                .pendingSubscriptionCount(pendingSubscriptionCount)
                .totalUserCount(totalUserCount)
                .totalProjectCount(totalProjectCount)
                .monthlyRevenue(monthlyRevenue)
                .recentTenants(tenantSummaries)
                .build();
    }

    @Override
    public DashboardDataResponse.HqAdminData getHqAdminDashboard(String tenantId) {
        log.info("[본사관리자 대시보드] 테넌트 ID: {}", tenantId);

        // 전체 현장 수
        long totalSiteCount = projectRepository.countByTenantId(tenantId);

        // 활성 현장 수
        long activeSiteCount = projectRepository.countByTenantIdAndStatus(
                tenantId, Project.ProjectStatus.ACTIVE);

        // 전체 노무자 수
        long totalWorkerCount = userRepository.countByTenantIdAndRolesContaining(
                tenantId, Role.ROLE_WORKER);

        // 활성 노무자 수 (최근 30일 이내 출역 기록이 있는 노무자)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long activeWorkerCount = attendanceRecordRepository
                .countDistinctWorkersByTenantIdAndWorkDateAfter(tenantId, thirtyDaysAgo);

        // 월간 인건비 (이번 달)
        LocalDate monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal monthlyLaborCost = attendanceRecordRepository
                .sumTotalWageByTenantIdAndWorkDateBetween(tenantId, monthStart, monthEnd);

        // 평균 출역률 (이번 달)
        BigDecimal averageAttendanceRate = calculateAverageAttendanceRate(tenantId, monthStart, monthEnd);

        // 노무자 수 상위 현장 (상위 5개)
        List<Project> topProjects = projectRepository.findTop5ByTenantIdOrderByWorkerCountDesc(tenantId);
        List<DashboardDataResponse.SiteSummary> siteSummaries = topProjects.stream()
                .map(project -> {
                    long workerCount = attendanceRecordRepository
                            .countDistinctWorkersByProjectId(project.getId());
                    long todayAttendance = attendanceRecordRepository
                            .countByProjectIdAndWorkDate(project.getId(), LocalDate.now());
                    BigDecimal attendanceRate = calculateSiteAttendanceRate(project.getId(), LocalDate.now());

                    return DashboardDataResponse.SiteSummary.builder()
                            .siteId(project.getId())
                            .siteName(project.getName())
                            .workerCount(workerCount)
                            .todayAttendanceCount(todayAttendance)
                            .attendanceRate(attendanceRate)
                            .status(project.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());

        // 월별 계약 현황 (최근 6개월)
        List<DashboardDataResponse.ContractStatusSummary> contractStatusByMonth = 
                calculateContractStatusByMonth(tenantId, 6);

        return DashboardDataResponse.HqAdminData.builder()
                .totalSiteCount(totalSiteCount)
                .activeSiteCount(activeSiteCount)
                .totalWorkerCount(totalWorkerCount)
                .activeWorkerCount(activeWorkerCount)
                .monthlyLaborCost(monthlyLaborCost != null ? monthlyLaborCost : BigDecimal.ZERO)
                .averageAttendanceRate(averageAttendanceRate)
                .topSitesByWorkerCount(siteSummaries)
                .contractStatusByMonth(contractStatusByMonth)
                .build();
    }

    @Override
    public DashboardDataResponse.SiteManagerData getSiteManagerDashboard(Long siteId) {
        log.info("[현장관리자 대시보드] 현장 ID: {}", siteId);

        Project project = projectRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + siteId));

        // 전체 노무자 수
        long totalWorkerCount = attendanceRecordRepository.countDistinctWorkersByProjectId(siteId);

        // 금일 출역자 수
        LocalDate today = LocalDate.now();
        long todayAttendanceCount = attendanceRecordRepository.countByProjectIdAndWorkDate(siteId, today);

        // 금일 결근자 수 (등록된 노무자 - 출역자)
        long todayAbsentCount = totalWorkerCount - todayAttendanceCount;

        // 금일 출역률
        BigDecimal todayAttendanceRate = calculateSiteAttendanceRate(siteId, today);

        // 주간 출역률 (최근 7일)
        LocalDate weekAgo = today.minusDays(7);
        BigDecimal weeklyAttendanceRate = calculateAverageAttendanceRateForSite(siteId, weekAgo, today);

        // 서명 대기 계약서 수
        long pendingContractCount = contractRepository.countByProjectIdAndStatus(
                siteId, Contract.ContractStatus.PENDING);

        // 안면인식기 수
        long faceDeviceCount = faceDeviceRepository.countByProjectId(siteId);

        // 동기화 완료 안면인식기 수
        long faceDeviceSyncedCount = faceDeviceRepository.countByProjectIdAndSyncStatus(
                siteId, FaceRecognitionDevice.DeviceSyncStatus.SYNCED);

        // 팀별 요약 (TODO: 팀 엔티티 구현 후 추가)
        List<DashboardDataResponse.TeamSummary> teamSummaries = new ArrayList<>();

        // 공종별 요약
        List<DashboardDataResponse.JobTypeSummary> jobTypeSummaries = calculateJobTypeSummaries(siteId, today);

        return DashboardDataResponse.SiteManagerData.builder()
                .siteId(siteId)
                .siteName(project.getName())
                .totalWorkerCount(totalWorkerCount)
                .todayAttendanceCount(todayAttendanceCount)
                .todayAbsentCount(todayAbsentCount)
                .todayAttendanceRate(todayAttendanceRate)
                .weeklyAttendanceRate(weeklyAttendanceRate)
                .pendingContractCount(pendingContractCount)
                .faceDeviceCount(faceDeviceCount)
                .faceDeviceSyncedCount(faceDeviceSyncedCount)
                .teamSummaries(teamSummaries)
                .jobTypeSummaries(jobTypeSummaries)
                .build();
    }

    @Override
    public DashboardDataResponse.TeamLeaderData getTeamLeaderDashboard(Long teamId, Long siteId) {
        log.info("[노무팀장 대시보드] 팀 ID: {}, 현장 ID: {}", teamId, siteId);

        // TODO: 팀 엔티티 구현 후 추가
        log.warn("[대시보드] 팀장 대시보드는 아직 구현되지 않았습니다.");

        return DashboardDataResponse.TeamLeaderData.builder()
                .teamId(teamId)
                .teamName("미구현")
                .teamMemberCount(0L)
                .todayAttendanceCount(0L)
                .todayAbsentCount(0L)
                .todayAttendanceRate(BigDecimal.ZERO)
                .weeklyAverageWorkHours(BigDecimal.ZERO)
                .pendingContractCount(0L)
                .workerSummaries(new ArrayList<>())
                .build();
    }

    @Override
    public DashboardDataResponse.WorkerData getWorkerDashboard(Long workerId, Long siteId) {
        log.info("[일반노무자 대시보드] 노무자 ID: {}, 현장 ID: {}", workerId, siteId);

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + workerId));

        Project project = projectRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + siteId));

        // 월간 출역일수 (이번 달)
        LocalDate monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        int monthlyAttendanceDays = attendanceRecordRepository
                .countDistinctWorkDatesByWorkerIdAndProjectIdAndWorkDateBetween(
                        workerId, siteId, monthStart, monthEnd);

        // 월간 총 근무시간
        BigDecimal monthlyTotalWorkHours = attendanceRecordRepository
                .sumWorkHoursByWorkerIdAndProjectIdAndWorkDateBetween(
                        workerId, siteId, monthStart, monthEnd);

        // 월간 예상 급여
        BigDecimal monthlyEstimatedWage = attendanceRecordRepository
                .sumTotalWageByWorkerIdAndProjectIdAndWorkDateBetween(
                        workerId, siteId, monthStart, monthEnd);

        // 서명 대기 계약서 수
        long pendingContractCount = contractRepository.countByWorkerIdAndProjectIdAndStatus(
                workerId, siteId, Contract.ContractStatus.PENDING);

        // 안면 등록 여부
        boolean isFaceRegistered = worker.getFaceEmbedding() != null && 
                                   !worker.getFaceEmbedding().trim().isEmpty();

        // 프로필 완성도
        int profileCompletionPercentage = worker.getOverallProfileCompletionPercentage();

        // 최근 출역 기록 (최근 7일)
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<AttendanceRecord> recentRecords = attendanceRecordRepository
                .findByWorkerIdAndProjectIdAndWorkDateBetweenOrderByWorkDateDesc(
                        workerId, siteId, weekAgo, LocalDate.now());

        List<DashboardDataResponse.RecentAttendance> recentAttendances = recentRecords.stream()
                .map(record -> DashboardDataResponse.RecentAttendance.builder()
                        .workDate(record.getWorkDate().format(DateTimeFormatter.ISO_DATE))
                        .checkInTime(record.getCheckInTime() != null ? 
                                record.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
                        .checkOutTime(record.getCheckOutTime() != null ? 
                                record.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
                        .workHours(record.getWorkHours())
                        .dataSource(record.getDataSource().name())
                        .build())
                .collect(Collectors.toList());

        return DashboardDataResponse.WorkerData.builder()
                .workerId(workerId)
                .workerName(worker.getName())
                .currentSiteId(siteId)
                .currentSiteName(project.getName())
                .monthlyAttendanceDays(monthlyAttendanceDays)
                .monthlyTotalWorkHours(monthlyTotalWorkHours != null ? monthlyTotalWorkHours : BigDecimal.ZERO)
                .monthlyEstimatedWage(monthlyEstimatedWage != null ? monthlyEstimatedWage : BigDecimal.ZERO)
                .pendingContractCount(pendingContractCount)
                .isFaceRegistered(isFaceRegistered)
                .profileCompletionPercentage(profileCompletionPercentage)
                .recentAttendances(recentAttendances)
                .build();
    }

    @Override
    public DashboardDataResponse.CommonKpiData getCommonKpiData(Role role, Long siteId) {
        log.info("[공통 KPI 데이터] 역할: {}, 현장 ID: {}", role, siteId);

        String tenantId = TenantContext.getCurrentTenant();
        LocalDate today = LocalDate.now();

        long activeProjectCount;
        long registeredWorkerCount;
        long unsignedContractCount;
        long todayAttendanceCount;
        BigDecimal todayTotalWorkHours;

        // 역할에 따라 범위 조정
        if (role == Role.ROLE_SUPER) {
            // 슈퍼관리자: 전체 시스템
            activeProjectCount = projectRepository.countByStatus(Project.ProjectStatus.ACTIVE);
            registeredWorkerCount = userRepository.countByRolesContaining(Role.ROLE_WORKER);
            unsignedContractCount = contractRepository.countByStatus(Contract.ContractStatus.PENDING);
            todayAttendanceCount = attendanceRecordRepository.countByWorkDate(today);
            todayTotalWorkHours = attendanceRecordRepository.sumWorkHoursByWorkDate(today);
        } else if (role == Role.ROLE_HQ) {
            // 본사관리자: 테넌트 범위
            activeProjectCount = projectRepository.countByTenantIdAndStatus(
                    tenantId, Project.ProjectStatus.ACTIVE);
            registeredWorkerCount = userRepository.countByTenantIdAndRolesContaining(
                    tenantId, Role.ROLE_WORKER);
            unsignedContractCount = contractRepository.countByTenantIdAndStatus(
                    tenantId, Contract.ContractStatus.PENDING);
            todayAttendanceCount = attendanceRecordRepository.countByTenantIdAndWorkDate(tenantId, today);
            todayTotalWorkHours = attendanceRecordRepository.sumWorkHoursByTenantIdAndWorkDate(tenantId, today);
        } else if (role == Role.ROLE_SITE && siteId != null) {
            // 현장관리자: 현장 범위
            activeProjectCount = projectRepository.existsByIdAndStatus(siteId, Project.ProjectStatus.ACTIVE) ? 1L : 0L;
            registeredWorkerCount = attendanceRecordRepository.countDistinctWorkersByProjectId(siteId);
            unsignedContractCount = contractRepository.countByProjectIdAndStatus(
                    siteId, Contract.ContractStatus.PENDING);
            todayAttendanceCount = attendanceRecordRepository.countByProjectIdAndWorkDate(siteId, today);
            todayTotalWorkHours = attendanceRecordRepository.sumWorkHoursByProjectIdAndWorkDate(siteId, today);
        } else {
            // 기타 역할: 기본값
            activeProjectCount = 0L;
            registeredWorkerCount = 0L;
            unsignedContractCount = 0L;
            todayAttendanceCount = 0L;
            todayTotalWorkHours = BigDecimal.ZERO;
        }

        return DashboardDataResponse.CommonKpiData.builder()
                .activeProjectCount(activeProjectCount)
                .registeredWorkerCount(registeredWorkerCount)
                .unsignedContractCount(unsignedContractCount)
                .todayAttendanceCount(todayAttendanceCount)
                .todayTotalWorkHours(todayTotalWorkHours != null ? todayTotalWorkHours : BigDecimal.ZERO)
                .build();
    }

    // ========== 헬퍼 메서드 ==========

    /**
     * 평균 출역률 계산 (테넌트 범위)
     */
    private BigDecimal calculateAverageAttendanceRate(String tenantId, LocalDate startDate, LocalDate endDate) {
        List<Project> projects = projectRepository.findByTenantIdAndStatus(
                tenantId, Project.ProjectStatus.ACTIVE);

        if (projects.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRate = projects.stream()
                .map(project -> calculateAverageAttendanceRateForSite(project.getId(), startDate, endDate))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalRate.divide(BigDecimal.valueOf(projects.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 현장별 평균 출역률 계산
     */
    private BigDecimal calculateAverageAttendanceRateForSite(Long siteId, LocalDate startDate, LocalDate endDate) {
        long totalWorkerCount = attendanceRecordRepository.countDistinctWorkersByProjectId(siteId);

        if (totalWorkerCount == 0) {
            return BigDecimal.ZERO;
        }

        long workDays = startDate.datesUntil(endDate.plusDays(1)).count();
        long expectedAttendances = totalWorkerCount * workDays;

        if (expectedAttendances == 0) {
            return BigDecimal.ZERO;
        }

        long actualAttendances = attendanceRecordRepository
                .countByProjectIdAndWorkDateBetween(siteId, startDate, endDate);

        return BigDecimal.valueOf(actualAttendances)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(expectedAttendances), 2, RoundingMode.HALF_UP);
    }

    /**
     * 현장 출역률 계산 (특정 날짜)
     */
    private BigDecimal calculateSiteAttendanceRate(Long siteId, LocalDate workDate) {
        long totalWorkerCount = attendanceRecordRepository.countDistinctWorkersByProjectId(siteId);

        if (totalWorkerCount == 0) {
            return BigDecimal.ZERO;
        }

        long attendanceCount = attendanceRecordRepository.countByProjectIdAndWorkDate(siteId, workDate);

        return BigDecimal.valueOf(attendanceCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalWorkerCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 월별 계약 현황 계산
     */
    private List<DashboardDataResponse.ContractStatusSummary> calculateContractStatusByMonth(
            String tenantId, int monthCount) {
        
        List<DashboardDataResponse.ContractStatusSummary> summaries = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());

        for (int i = 0; i < monthCount; i++) {
            LocalDate monthStart = currentMonth.minusMonths(i);
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());

            long signedCount = contractRepository.countByTenantIdAndStatusAndSignedAtBetween(
                    tenantId, Contract.ContractStatus.SIGNED, 
                    monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));

            long pendingCount = contractRepository.countByTenantIdAndStatusAndCreatedAtBetween(
                    tenantId, Contract.ContractStatus.PENDING,
                    monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));

            long expiredCount = contractRepository.countByTenantIdAndStatusAndUpdatedAtBetween(
                    tenantId, Contract.ContractStatus.EXPIRED,
                    monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));

            summaries.add(DashboardDataResponse.ContractStatusSummary.builder()
                    .month(monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .signedCount(signedCount)
                    .pendingCount(pendingCount)
                    .expiredCount(expiredCount)
                    .build());
        }

        return summaries;
    }

    /**
     * 공종별 요약 계산
     */
    private List<DashboardDataResponse.JobTypeSummary> calculateJobTypeSummaries(Long siteId, LocalDate workDate) {
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByProjectIdAndWorkDate(siteId, workDate);

        return records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getJobType, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    AttendanceRecord.JobType jobType = entry.getKey();
                    Long count = entry.getValue();

                    // 해당 공종의 전체 노무자 수
                    long totalWorkerCount = attendanceRecordRepository
                            .countDistinctWorkersByProjectIdAndJobType(siteId, jobType);

                    return DashboardDataResponse.JobTypeSummary.builder()
                            .jobType(jobType.name())
                            .jobTypeName(jobType.getDisplayName())
                            .workerCount(totalWorkerCount)
                            .todayAttendanceCount(count)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
