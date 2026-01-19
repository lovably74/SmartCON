package com.smartcon.domain.attendance;

import com.smartcon.domain.attendance.dto.AttendanceStatisticsResponse;
import com.smartcon.domain.attendance.dto.DashboardChartDataResponse;
import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.attendance.repository.AttendanceRecordRepository;
import com.smartcon.domain.attendance.service.AttendanceStatisticsServiceImpl;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.global.tenant.TenantContext;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 출역 데이터 정확성 속성 테스트
 * Feature: smartcon-lite-role-based-system, Property 11
 * 
 * Property 11: Chart Data Integrity
 * For any chart generation request, the system should provide accurate attendance 
 * and job distribution data that matches the underlying database records
 * Validates: Requirements 4.4, 4.5
 */
class ChartDataIntegrityPropertyTest {

    private TestContext createTestContext() {
        TenantContext.setCurrentTenantId(1L);
        
        AttendanceRecordRepository attendanceRecordRepository = Mockito.mock(AttendanceRecordRepository.class);
        
        AttendanceStatisticsServiceImpl attendanceStatisticsService = new AttendanceStatisticsServiceImpl(
            attendanceRecordRepository
        );
        
        return new TestContext(attendanceRecordRepository, attendanceStatisticsService);
    }
    
    private void cleanupTestContext() {
        TenantContext.clear();
    }
    
    private static class TestContext {
        final AttendanceRecordRepository attendanceRecordRepository;
        final AttendanceStatisticsServiceImpl attendanceStatisticsService;
        
        TestContext(AttendanceRecordRepository attendanceRecordRepository,
                   AttendanceStatisticsServiceImpl attendanceStatisticsService) {
            this.attendanceRecordRepository = attendanceRecordRepository;
            this.attendanceStatisticsService = attendanceStatisticsService;
        }
    }

    /**
     * Property 11.1: 일별 출역 차트 데이터 정확성
     * 
     * 모든 출역 기록에 대해:
     * 일별 출역 차트 데이터의 노무자 수와 총 작업 시간이 실제 데이터베이스 기록과 일치해야 함
     */
    @Property(tries = 100)
    @Label("Property 11.1: 일별 출역 차트 데이터가 실제 기록과 일치한다")
    void dailyAttendanceChartDataMatchesActualRecords(
            @ForAll("attendanceRecordsForChart") List<AttendanceRecord> records) {

        TestContext ctx = createTestContext();
        try {
            // Given: 출역 기록 목록
            Long projectId = 1L;
            String tenantId = "1";
            LocalDate startDate = records.stream()
                    .map(AttendanceRecord::getWorkDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now().minusDays(7));
            LocalDate endDate = records.stream()
                    .map(AttendanceRecord::getWorkDate)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());

            // Mock 설정
            when(ctx.attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                    eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(records);

            // When: 대시보드 차트 데이터 생성
            DashboardChartDataResponse chartData = ctx.attendanceStatisticsService
                    .getDashboardChartData(projectId, startDate, endDate);

            // Then: 일별 차트 데이터가 실제 기록과 일치해야 함
            Map<LocalDate, List<AttendanceRecord>> recordsByDate = records.stream()
                    .collect(Collectors.groupingBy(AttendanceRecord::getWorkDate));

            for (DashboardChartDataResponse.DailyAttendanceChart dailyChart : chartData.getDailyAttendanceChart()) {
                LocalDate date = LocalDate.parse(dailyChart.getDate());
                List<AttendanceRecord> dayRecords = recordsByDate.getOrDefault(date, Collections.emptyList());

                // 노무자 수 검증
                long expectedWorkerCount = dayRecords.stream()
                        .map(r -> r.getWorker().getId())
                        .distinct()
                        .count();
                assertThat(dailyChart.getWorkerCount())
                        .as("날짜 %s의 노무자 수가 일치해야 함", date)
                        .isEqualTo(expectedWorkerCount);

                // 총 작업 시간 검증
                BigDecimal expectedTotalHours = dayRecords.stream()
                        .map(AttendanceRecord::getWorkHours)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                assertThat(dailyChart.getTotalWorkHours())
                        .as("날짜 %s의 총 작업 시간이 일치해야 함", date)
                        .isEqualByComparingTo(expectedTotalHours);
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 11.2: 공종별 분포 차트 데이터 정확성
     * 
     * 모든 출역 기록에 대해:
     * 공종별 분포 차트의 노무자 수와 비율이 실제 데이터베이스 기록과 일치해야 함
     */
    @Property(tries = 100)
    @Label("Property 11.2: 공종별 분포 차트 데이터가 실제 기록과 일치한다")
    void jobTypeDistributionChartDataMatchesActualRecords(
            @ForAll("attendanceRecordsWithJobTypes") List<AttendanceRecord> records) {

        TestContext ctx = createTestContext();
        try {
            // Given: 다양한 공종의 출역 기록 목록
            Long projectId = 1L;
            String tenantId = "1";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // Mock 설정
            when(ctx.attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                    eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(records);

            // When: 대시보드 차트 데이터 생성
            DashboardChartDataResponse chartData = ctx.attendanceStatisticsService
                    .getDashboardChartData(projectId, startDate, endDate);

            // Then: 공종별 차트 데이터가 실제 기록과 일치해야 함
            long totalCount = records.size();
            Map<AttendanceRecord.JobType, Long> jobTypeCounts = records.stream()
                    .filter(r -> r.getJobType() != null)
                    .collect(Collectors.groupingBy(AttendanceRecord::getJobType, Collectors.counting()));

            for (DashboardChartDataResponse.JobTypePieChart jobTypeChart : chartData.getJobTypePieChart()) {
                AttendanceRecord.JobType jobType = AttendanceRecord.JobType.valueOf(jobTypeChart.getJobType());
                Long expectedCount = jobTypeCounts.getOrDefault(jobType, 0L);

                // 노무자 수 검증
                assertThat(jobTypeChart.getCount())
                        .as("공종 %s의 노무자 수가 일치해야 함", jobType)
                        .isEqualTo(expectedCount);

                // 비율 검증
                BigDecimal expectedPercentage = totalCount > 0
                        ? BigDecimal.valueOf(expectedCount * 100.0 / totalCount)
                                .setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                assertThat(jobTypeChart.getPercentage())
                        .as("공종 %s의 비율이 일치해야 함", jobType)
                        .isEqualByComparingTo(expectedPercentage);
            }
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 11.3: 데이터 소스별 차트 데이터 정확성
     * 
     * 모든 출역 기록에 대해:
     * 데이터 소스별 차트의 카운트와 비율이 실제 데이터베이스 기록과 일치해야 함
     */
    @Property(tries = 100)
    @Label("Property 11.3: 데이터 소스별 차트 데이터가 실제 기록과 일치한다")
    void dataSourceChartDataMatchesActualRecords(
            @ForAll("attendanceRecordsWithDataSources") List<AttendanceRecord> records) {

        TestContext ctx = createTestContext();
        try {
            // Given: 다양한 데이터 소스의 출역 기록 목록
            Long projectId = 1L;
            String tenantId = "1";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // Mock 설정
            when(ctx.attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                    eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(records);

            // When: 대시보드 차트 데이터 생성
            DashboardChartDataResponse chartData = ctx.attendanceStatisticsService
                    .getDashboardChartData(projectId, startDate, endDate);

            // Then: 데이터 소스별 차트 데이터가 실제 기록과 일치해야 함
            long totalCount = records.size();

            long expectedFaceRecognitionCount = records.stream()
                    .filter(r -> r.getDataSource() == AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION)
                    .count();
            long expectedManualEntryCount = records.stream()
                    .filter(r -> r.getDataSource() == AttendanceRecord.AttendanceDataSource.MANUAL_ENTRY)
                    .count();
            long expectedAdminCorrectionCount = records.stream()
                    .filter(r -> r.getDataSource() == AttendanceRecord.AttendanceDataSource.ADMIN_CORRECTION)
                    .count();

            DashboardChartDataResponse.DataSourceChart dataSourceChart = chartData.getDataSourceChart();

            // 카운트 검증
            assertThat(dataSourceChart.getFaceRecognitionCount())
                    .as("안면인식 데이터 카운트가 일치해야 함")
                    .isEqualTo(expectedFaceRecognitionCount);
            assertThat(dataSourceChart.getManualEntryCount())
                    .as("수동입력 데이터 카운트가 일치해야 함")
                    .isEqualTo(expectedManualEntryCount);
            assertThat(dataSourceChart.getAdminCorrectionCount())
                    .as("관리자수정 데이터 카운트가 일치해야 함")
                    .isEqualTo(expectedAdminCorrectionCount);

            // 비율 검증
            BigDecimal expectedFaceRecognitionPercentage = totalCount > 0
                    ? BigDecimal.valueOf(expectedFaceRecognitionCount * 100.0 / totalCount)
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            assertThat(dataSourceChart.getFaceRecognitionPercentage())
                    .as("안면인식 데이터 비율이 일치해야 함")
                    .isEqualByComparingTo(expectedFaceRecognitionPercentage);
        } finally {
            cleanupTestContext();
        }
    }

    /**
     * Property 11.4: 월별 출역 통계 데이터 정확성
     * 
     * 모든 출역 기록에 대해:
     * 월별 출역 통계의 노무자 수와 작업 시간이 실제 데이터베이스 기록과 일치해야 함
     */
    @Property(tries = 100)
    @Label("Property 11.4: 월별 출역 통계 데이터가 실제 기록과 일치한다")
    void monthlyStatisticsDataMatchesActualRecords(
            @ForAll("attendanceRecordsForMonthlyStats") List<AttendanceRecord> records) {

        TestContext ctx = createTestContext();
        try {
            // Given: 여러 달에 걸친 출역 기록 목록
            Long projectId = 1L;
            String tenantId = "1";
            LocalDate startDate = records.stream()
                    .map(AttendanceRecord::getWorkDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now().minusMonths(3));
            LocalDate endDate = records.stream()
                    .map(AttendanceRecord::getWorkDate)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());

            // Mock 설정
            when(ctx.attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                    eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(records);

            // When: 프로젝트 통계 조회
            AttendanceStatisticsResponse statistics = ctx.attendanceStatisticsService
                    .getProjectStatistics(projectId, startDate, endDate);

            // Then: 월별 통계 데이터가 실제 기록과 일치해야 함
            Map<String, List<AttendanceRecord>> recordsByMonth = records.stream()
                    .collect(Collectors.groupingBy(r -> 
                            r.getWorkDate().getYear() + "-" + 
                            String.format("%02d", r.getWorkDate().getMonthValue())));

            for (AttendanceStatisticsResponse.MonthlyAttendanceData monthlyData : statistics.getMonthlyAttendance()) {
                List<AttendanceRecord> monthRecords = recordsByMonth.getOrDefault(
                        monthlyData.getMonth(), Collections.emptyList());

                // 노무자 수 검증
                long expectedWorkerCount = monthRecords.stream()
                        .map(r -> r.getWorker().getId())
                        .distinct()
                        .count();
                assertThat(monthlyData.getWorkerCount())
                        .as("월 %s의 노무자 수가 일치해야 함", monthlyData.getMonth())
                        .isEqualTo(expectedWorkerCount);

                // 총 작업 시간 검증
                BigDecimal expectedTotalHours = monthRecords.stream()
                        .map(AttendanceRecord::getWorkHours)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                assertThat(monthlyData.getTotalWorkHours())
                        .as("월 %s의 총 작업 시간이 일치해야 함", monthlyData.getMonth())
                        .isEqualByComparingTo(expectedTotalHours);

                // 평균 작업 시간 검증
                BigDecimal expectedAverageHours = expectedWorkerCount > 0
                        ? expectedTotalHours.divide(BigDecimal.valueOf(expectedWorkerCount), 
                                2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                assertThat(monthlyData.getAverageWorkHours())
                        .as("월 %s의 평균 작업 시간이 일치해야 함", monthlyData.getMonth())
                        .isEqualByComparingTo(expectedAverageHours);
            }
        } finally {
            cleanupTestContext();
        }
    }

    // ========== Arbitraries (데이터 생성기) ==========

    @Provide
    Arbitrary<List<AttendanceRecord>> attendanceRecordsForChart() {
        return Arbitraries.integers().between(5, 20).flatMap(size -> {
            // 날짜별로 고유한 worker ID를 보장하기 위해 Map 사용
            return Arbitraries.integers().between(0, 6).list().ofSize(size).flatMap(dayOffsets -> {
                // 각 날짜별로 사용된 worker ID를 추적
                Map<LocalDate, Set<Long>> usedWorkerIdsByDate = new HashMap<>();
                List<AttendanceRecord> records = new ArrayList<>();
                
                for (int dayOffset : dayOffsets) {
                    LocalDate workDate = LocalDate.now().minusDays(dayOffset);
                    
                    // 이 날짜에 아직 사용되지 않은 worker ID 생성
                    Set<Long> usedIds = usedWorkerIdsByDate.computeIfAbsent(workDate, k -> new HashSet<>());
                    long workerId = 1L;
                    while (usedIds.contains(workerId) && workerId <= 100L) {
                        workerId++;
                    }
                    usedIds.add(workerId);
                    
                    // 랜덤 속성들
                    Long projectId = 1L;
                    AttendanceRecord.JobType jobType = AttendanceRecord.JobType.values()[
                            (int)(Math.random() * AttendanceRecord.JobType.values().length)];
                    BigDecimal workHours = BigDecimal.valueOf(4.0 + Math.random() * 8.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    records.add(createAttendanceRecord(workerId, projectId, workDate, jobType, workHours,
                            AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION));
                }
                
                return Arbitraries.just(records);
            });
        });
    }

    @Provide
    Arbitrary<List<AttendanceRecord>> attendanceRecordsWithJobTypes() {
        return Arbitraries.integers().between(10, 30).flatMap(size -> {
            Arbitrary<AttendanceRecord> recordArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 100L),
                Arbitraries.of(AttendanceRecord.JobType.values()),
                Arbitraries.doubles().between(4.0, 12.0).map(d -> BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP))
            ).as((workerId, jobType, workHours) -> 
                    createAttendanceRecord(workerId, 1L, LocalDate.now(), jobType, workHours,
                            AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION));
            
            return recordArbitrary.list().ofSize(size);
        });
    }

    @Provide
    Arbitrary<List<AttendanceRecord>> attendanceRecordsWithDataSources() {
        return Arbitraries.integers().between(10, 30).flatMap(size -> {
            Arbitrary<AttendanceRecord> recordArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 100L),
                Arbitraries.of(AttendanceRecord.AttendanceDataSource.values()),
                Arbitraries.doubles().between(4.0, 12.0).map(d -> BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP))
            ).as((workerId, dataSource, workHours) -> 
                    createAttendanceRecord(workerId, 1L, LocalDate.now(), 
                            AttendanceRecord.JobType.GENERAL_LABORER, workHours, dataSource));
            
            return recordArbitrary.list().ofSize(size);
        });
    }

    @Provide
    Arbitrary<List<AttendanceRecord>> attendanceRecordsForMonthlyStats() {
        return Arbitraries.integers().between(15, 40).flatMap(size -> {
            Arbitrary<AttendanceRecord> recordArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 50L),
                Arbitraries.integers().between(0, 90).map(i -> LocalDate.now().minusDays(i)),
                Arbitraries.of(AttendanceRecord.JobType.values()),
                Arbitraries.doubles().between(4.0, 12.0).map(d -> BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP))
            ).as((workerId, workDate, jobType, workHours) -> 
                    createAttendanceRecord(workerId, 1L, workDate, jobType, workHours,
                            AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION));
            
            return recordArbitrary.list().ofSize(size);
        });
    }

    // ========== Helper Methods ==========

    private AttendanceRecord createAttendanceRecord(Long workerId, Long projectId, LocalDate workDate,
                                                   AttendanceRecord.JobType jobType, BigDecimal workHours,
                                                   AttendanceRecord.AttendanceDataSource dataSource) {
        User worker = User.builder()
                .name("Worker" + workerId)
                .email("worker" + workerId + "@test.com")
                .roles(Set.of(Role.ROLE_WORKER))
                .build();
        worker.setId(workerId);

        Project project = Project.builder()
                .name("Project" + projectId)
                .constructionPeriodStart(LocalDate.now().minusMonths(1))
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .status(Project.ProjectStatus.ACTIVE)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(projectId);
        project.setTenantId(1L);

        AttendanceRecord record = AttendanceRecord.builder()
                .worker(worker)
                .project(project)
                .workDate(workDate)
                .checkInTime(workDate.atTime(8, 0))
                .checkOutTime(workDate.atTime(17, 0))
                .workHours(workHours)
                .jobType(jobType)
                .dataSource(dataSource)
                .build();
        record.setId(workerId * 1000 + workDate.getDayOfMonth());
        record.setTenantId(1L);

        return record;
    }
}
