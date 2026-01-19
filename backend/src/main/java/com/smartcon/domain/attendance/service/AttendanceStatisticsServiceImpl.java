package com.smartcon.domain.attendance.service;

import com.smartcon.domain.attendance.dto.AttendanceStatisticsResponse;
import com.smartcon.domain.attendance.dto.DashboardChartDataResponse;
import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.attendance.repository.AttendanceRecordRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 출역 통계 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    private final AttendanceRecordRepository attendanceRecordRepository;

    @Override
    public AttendanceStatisticsResponse getProjectStatistics(Long projectId, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[출역 통계 조회] 테넌트: {}, 프로젝트 ID: {}, 기간: {} ~ {}", 
                tenantId, projectId, startDate, endDate);

        // 기간별 출역 기록 조회
        List<AttendanceRecord> records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                        tenantId, startDate, endDate)
                .stream()
                .filter(r -> r.getProject().getId().equals(projectId))
                .collect(Collectors.toList());

        // 기본 통계 계산
        long totalWorkers = records.stream()
                .map(r -> r.getWorker().getId())
                .distinct()
                .count();

        LocalDate today = LocalDate.now();
        long todayWorkers = records.stream()
                .filter(r -> r.getWorkDate().equals(today))
                .map(r -> r.getWorker().getId())
                .distinct()
                .count();

        BigDecimal totalWorkHours = records.stream()
                .map(AttendanceRecord::getWorkHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageWorkHours = totalWorkers > 0 
                ? totalWorkHours.divide(BigDecimal.valueOf(totalWorkers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 월별 출역 현황
        List<AttendanceStatisticsResponse.MonthlyAttendanceData> monthlyData = calculateMonthlyAttendance(records);

        // 공종별 분포
        List<AttendanceStatisticsResponse.JobTypeDistribution> jobTypeDistribution = calculateJobTypeDistribution(records);

        // 데이터 소스별 통계
        AttendanceStatisticsResponse.DataSourceStatistics dataSourceStats = calculateDataSourceStatistics(records);

        // 팀별 통계
        List<AttendanceStatisticsResponse.TeamStatistics> teamStats = calculateTeamStatistics(records);

        return AttendanceStatisticsResponse.builder()
                .totalWorkers(totalWorkers)
                .todayWorkers(todayWorkers)
                .newWorkers(0L) // TODO: 신규 출역자 계산 로직 추가
                .averageWorkHours(averageWorkHours)
                .totalWorkHours(totalWorkHours)
                .monthlyAttendance(monthlyData)
                .jobTypeDistribution(jobTypeDistribution)
                .dataSourceStatistics(dataSourceStats)
                .teamStatistics(teamStats)
                .build();
    }

    @Override
    public DashboardChartDataResponse getDashboardChartData(Long projectId, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[대시보드 차트 데이터 생성] 테넌트: {}, 프로젝트 ID: {}, 기간: {} ~ {}", 
                tenantId, projectId, startDate, endDate);

        // 기간별 출역 기록 조회
        List<AttendanceRecord> records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                        tenantId, startDate, endDate)
                .stream()
                .filter(r -> r.getProject().getId().equals(projectId))
                .collect(Collectors.toList());

        // 일별 출역 추이
        List<DashboardChartDataResponse.DailyAttendanceChart> dailyChart = calculateDailyAttendanceChart(records);

        // 공종별 분포 파이 차트
        List<DashboardChartDataResponse.JobTypePieChart> jobTypePieChart = calculateJobTypePieChart(records);

        // 데이터 소스별 차트
        DashboardChartDataResponse.DataSourceChart dataSourceChart = calculateDataSourceChart(records);

        // 주간 출역 현황
        List<DashboardChartDataResponse.WeeklyAttendanceChart> weeklyChart = calculateWeeklyAttendanceChart(records);

        return DashboardChartDataResponse.builder()
                .dailyAttendanceChart(dailyChart)
                .jobTypePieChart(jobTypePieChart)
                .dataSourceChart(dataSourceChart)
                .weeklyAttendanceChart(weeklyChart)
                .build();
    }

    @Override
    public AttendanceStatisticsResponse getMonthlyStatistics(Long projectId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return getProjectStatistics(projectId, startDate, endDate);
    }

    @Override
    public AttendanceStatisticsResponse getJobTypeDistribution(Long projectId, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[공종별 분포 조회] 테넌트: {}, 프로젝트 ID: {}, 기간: {} ~ {}", 
                tenantId, projectId, startDate, endDate);

        List<AttendanceRecord> records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                        tenantId, startDate, endDate)
                .stream()
                .filter(r -> r.getProject().getId().equals(projectId))
                .collect(Collectors.toList());

        List<AttendanceStatisticsResponse.JobTypeDistribution> jobTypeDistribution = calculateJobTypeDistribution(records);

        return AttendanceStatisticsResponse.builder()
                .jobTypeDistribution(jobTypeDistribution)
                .build();
    }

    /**
     * 월별 출역 현황 계산
     */
    private List<AttendanceStatisticsResponse.MonthlyAttendanceData> calculateMonthlyAttendance(List<AttendanceRecord> records) {
        Map<String, List<AttendanceRecord>> monthlyRecords = records.stream()
                .collect(Collectors.groupingBy(r -> r.getWorkDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        return monthlyRecords.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<AttendanceRecord> monthRecords = entry.getValue();

                    long workerCount = monthRecords.stream()
                            .map(r -> r.getWorker().getId())
                            .distinct()
                            .count();

                    BigDecimal totalWorkHours = monthRecords.stream()
                            .map(AttendanceRecord::getWorkHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageWorkHours = workerCount > 0
                            ? totalWorkHours.divide(BigDecimal.valueOf(workerCount), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return AttendanceStatisticsResponse.MonthlyAttendanceData.builder()
                            .month(month)
                            .workerCount(workerCount)
                            .totalWorkHours(totalWorkHours)
                            .averageWorkHours(averageWorkHours)
                            .build();
                })
                .sorted(Comparator.comparing(AttendanceStatisticsResponse.MonthlyAttendanceData::getMonth))
                .collect(Collectors.toList());
    }

    /**
     * 공종별 분포 계산
     */
    private List<AttendanceStatisticsResponse.JobTypeDistribution> calculateJobTypeDistribution(List<AttendanceRecord> records) {
        long totalCount = records.size();

        Map<AttendanceRecord.JobType, Long> jobTypeCounts = records.stream()
                .filter(r -> r.getJobType() != null)
                .collect(Collectors.groupingBy(AttendanceRecord::getJobType, Collectors.counting()));

        return jobTypeCounts.entrySet().stream()
                .map(entry -> {
                    AttendanceRecord.JobType jobType = entry.getKey();
                    Long count = entry.getValue();
                    BigDecimal percentage = totalCount > 0
                            ? BigDecimal.valueOf(count * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return AttendanceStatisticsResponse.JobTypeDistribution.builder()
                            .jobType(jobType.name())
                            .jobTypeName(jobType.getDisplayName())
                            .workerCount(count)
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(AttendanceStatisticsResponse.JobTypeDistribution::getWorkerCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 데이터 소스별 통계 계산
     */
    private AttendanceStatisticsResponse.DataSourceStatistics calculateDataSourceStatistics(List<AttendanceRecord> records) {
        long totalCount = records.size();

        long faceRecognitionCount = records.stream()
                .filter(r -> r.getDataSource() == AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION)
                .count();

        long manualEntryCount = records.stream()
                .filter(r -> r.getDataSource() == AttendanceRecord.AttendanceDataSource.MANUAL_ENTRY)
                .count();

        long adminCorrectionCount = records.stream()
                .filter(r -> r.getDataSource() == AttendanceRecord.AttendanceDataSource.ADMIN_CORRECTION)
                .count();

        BigDecimal faceRecognitionPercentage = totalCount > 0
                ? BigDecimal.valueOf(faceRecognitionCount * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal manualEntryPercentage = totalCount > 0
                ? BigDecimal.valueOf(manualEntryCount * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal adminCorrectionPercentage = totalCount > 0
                ? BigDecimal.valueOf(adminCorrectionCount * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AttendanceStatisticsResponse.DataSourceStatistics.builder()
                .faceRecognitionCount(faceRecognitionCount)
                .manualEntryCount(manualEntryCount)
                .adminCorrectionCount(adminCorrectionCount)
                .faceRecognitionPercentage(faceRecognitionPercentage)
                .manualEntryPercentage(manualEntryPercentage)
                .adminCorrectionPercentage(adminCorrectionPercentage)
                .build();
    }

    /**
     * 팀별 통계 계산
     */
    private List<AttendanceStatisticsResponse.TeamStatistics> calculateTeamStatistics(List<AttendanceRecord> records) {
        Map<String, List<AttendanceRecord>> teamRecords = records.stream()
                .filter(r -> r.getTeamName() != null && !r.getTeamName().trim().isEmpty())
                .collect(Collectors.groupingBy(AttendanceRecord::getTeamName));

        return teamRecords.entrySet().stream()
                .map(entry -> {
                    String teamName = entry.getKey();
                    List<AttendanceRecord> teamRecordList = entry.getValue();

                    long workerCount = teamRecordList.stream()
                            .map(r -> r.getWorker().getId())
                            .distinct()
                            .count();

                    BigDecimal totalWorkHours = teamRecordList.stream()
                            .map(AttendanceRecord::getWorkHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageWorkHours = workerCount > 0
                            ? totalWorkHours.divide(BigDecimal.valueOf(workerCount), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return AttendanceStatisticsResponse.TeamStatistics.builder()
                            .teamName(teamName)
                            .workerCount(workerCount)
                            .totalWorkHours(totalWorkHours)
                            .averageWorkHours(averageWorkHours)
                            .build();
                })
                .sorted(Comparator.comparing(AttendanceStatisticsResponse.TeamStatistics::getWorkerCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 일별 출역 추이 차트 계산
     */
    private List<DashboardChartDataResponse.DailyAttendanceChart> calculateDailyAttendanceChart(List<AttendanceRecord> records) {
        Map<LocalDate, List<AttendanceRecord>> dailyRecords = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getWorkDate));

        return dailyRecords.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceRecord> dayRecords = entry.getValue();

                    long workerCount = dayRecords.stream()
                            .map(r -> r.getWorker().getId())
                            .distinct()
                            .count();

                    BigDecimal totalWorkHours = dayRecords.stream()
                            .map(AttendanceRecord::getWorkHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DashboardChartDataResponse.DailyAttendanceChart.builder()
                            .date(date.format(DateTimeFormatter.ISO_DATE))
                            .workerCount(workerCount)
                            .totalWorkHours(totalWorkHours)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardChartDataResponse.DailyAttendanceChart::getDate))
                .collect(Collectors.toList());
    }

    /**
     * 공종별 파이 차트 계산
     */
    private List<DashboardChartDataResponse.JobTypePieChart> calculateJobTypePieChart(List<AttendanceRecord> records) {
        long totalCount = records.size();

        Map<AttendanceRecord.JobType, Long> jobTypeCounts = records.stream()
                .filter(r -> r.getJobType() != null)
                .collect(Collectors.groupingBy(AttendanceRecord::getJobType, Collectors.counting()));

        return jobTypeCounts.entrySet().stream()
                .map(entry -> {
                    AttendanceRecord.JobType jobType = entry.getKey();
                    Long count = entry.getValue();
                    BigDecimal percentage = totalCount > 0
                            ? BigDecimal.valueOf(count * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return DashboardChartDataResponse.JobTypePieChart.builder()
                            .jobType(jobType.name())
                            .jobTypeName(jobType.getDisplayName())
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardChartDataResponse.JobTypePieChart::getCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 데이터 소스별 차트 계산
     */
    private DashboardChartDataResponse.DataSourceChart calculateDataSourceChart(List<AttendanceRecord> records) {
        AttendanceStatisticsResponse.DataSourceStatistics stats = calculateDataSourceStatistics(records);

        return DashboardChartDataResponse.DataSourceChart.builder()
                .faceRecognitionCount(stats.getFaceRecognitionCount())
                .manualEntryCount(stats.getManualEntryCount())
                .adminCorrectionCount(stats.getAdminCorrectionCount())
                .faceRecognitionPercentage(stats.getFaceRecognitionPercentage())
                .manualEntryPercentage(stats.getManualEntryPercentage())
                .adminCorrectionPercentage(stats.getAdminCorrectionPercentage())
                .build();
    }

    /**
     * 주간 출역 현황 차트 계산
     */
    private List<DashboardChartDataResponse.WeeklyAttendanceChart> calculateWeeklyAttendanceChart(List<AttendanceRecord> records) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        Map<String, List<AttendanceRecord>> weeklyRecords = records.stream()
                .collect(Collectors.groupingBy(r -> {
                    int weekOfYear = r.getWorkDate().get(weekFields.weekOfWeekBasedYear());
                    int year = r.getWorkDate().get(weekFields.weekBasedYear());
                    return year + "-W" + String.format("%02d", weekOfYear);
                }));

        return weeklyRecords.entrySet().stream()
                .map(entry -> {
                    String weekKey = entry.getKey();
                    List<AttendanceRecord> weekRecords = entry.getValue();

                    LocalDate startDate = weekRecords.stream()
                            .map(AttendanceRecord::getWorkDate)
                            .min(LocalDate::compareTo)
                            .orElse(LocalDate.now());

                    LocalDate endDate = weekRecords.stream()
                            .map(AttendanceRecord::getWorkDate)
                            .max(LocalDate::compareTo)
                            .orElse(LocalDate.now());

                    long workerCount = weekRecords.stream()
                            .map(r -> r.getWorker().getId())
                            .distinct()
                            .count();

                    BigDecimal totalWorkHours = weekRecords.stream()
                            .map(AttendanceRecord::getWorkHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DashboardChartDataResponse.WeeklyAttendanceChart.builder()
                            .weekLabel(weekKey)
                            .startDate(startDate.format(DateTimeFormatter.ISO_DATE))
                            .endDate(endDate.format(DateTimeFormatter.ISO_DATE))
                            .workerCount(workerCount)
                            .totalWorkHours(totalWorkHours)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardChartDataResponse.WeeklyAttendanceChart::getWeekLabel))
                .collect(Collectors.toList());
    }
}
