package com.smartcon.domain.attendance.service;

import com.smartcon.domain.attendance.dto.AttendanceStatisticsResponse;
import com.smartcon.domain.attendance.dto.DashboardChartDataResponse;

import java.time.LocalDate;

/**
 * 출역 통계 서비스 인터페이스
 */
public interface AttendanceStatisticsService {

    /**
     * 프로젝트별 출역 통계 조회
     */
    AttendanceStatisticsResponse getProjectStatistics(Long projectId, LocalDate startDate, LocalDate endDate);

    /**
     * 대시보드 차트 데이터 생성
     */
    DashboardChartDataResponse getDashboardChartData(Long projectId, LocalDate startDate, LocalDate endDate);

    /**
     * 월별 출역 현황 조회
     */
    AttendanceStatisticsResponse getMonthlyStatistics(Long projectId, int year, int month);

    /**
     * 공종별 분포 조회
     */
    AttendanceStatisticsResponse getJobTypeDistribution(Long projectId, LocalDate startDate, LocalDate endDate);
}
