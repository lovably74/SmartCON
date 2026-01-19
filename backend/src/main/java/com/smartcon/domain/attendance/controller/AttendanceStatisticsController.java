package com.smartcon.domain.attendance.controller;

import com.smartcon.domain.attendance.dto.AttendanceStatisticsResponse;
import com.smartcon.domain.attendance.dto.DashboardChartDataResponse;
import com.smartcon.domain.attendance.service.AttendanceStatisticsService;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 출역 통계 컨트롤러
 * 대시보드용 출역 통계 및 차트 데이터 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/statistics")
@RequiredArgsConstructor
public class AttendanceStatisticsController {

    private final AttendanceStatisticsService statisticsService;

    /**
     * 프로젝트별 출역 통계 조회
     */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getProjectStatistics(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[API] 프로젝트 출역 통계 조회: 프로젝트 ID={}, 기간={} ~ {}", 
                projectId, startDate, endDate);

        AttendanceStatisticsResponse response = statisticsService.getProjectStatistics(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "출역 통계 조회 성공"));
    }

    /**
     * 대시보드 차트 데이터 조회
     */
    @GetMapping("/projects/{projectId}/chart")
    public ResponseEntity<ApiResponse<DashboardChartDataResponse>> getDashboardChartData(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[API] 대시보드 차트 데이터 조회: 프로젝트 ID={}, 기간={} ~ {}", 
                projectId, startDate, endDate);

        DashboardChartDataResponse response = statisticsService.getDashboardChartData(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "차트 데이터 조회 성공"));
    }

    /**
     * 월별 출역 통계 조회
     */
    @GetMapping("/projects/{projectId}/monthly")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getMonthlyStatistics(
            @PathVariable Long projectId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("[API] 월별 출역 통계 조회: 프로젝트 ID={}, 년월={}-{}", projectId, year, month);

        AttendanceStatisticsResponse response = statisticsService.getMonthlyStatistics(projectId, year, month);
        return ResponseEntity.ok(ApiResponse.success(response, "월별 통계 조회 성공"));
    }

    /**
     * 공종별 분포 조회
     */
    @GetMapping("/projects/{projectId}/job-type-distribution")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getJobTypeDistribution(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[API] 공종별 분포 조회: 프로젝트 ID={}, 기간={} ~ {}", projectId, startDate, endDate);

        AttendanceStatisticsResponse response = statisticsService.getJobTypeDistribution(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "공종별 분포 조회 성공"));
    }
}
