package com.smartcon.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 대시보드 차트 데이터 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardChartDataResponse {

    // 일별 출역 추이 차트
    private List<DailyAttendanceChart> dailyAttendanceChart;

    // 공종별 분포 파이 차트
    private List<JobTypePieChart> jobTypePieChart;

    // 데이터 소스별 분포 (안면인식 vs 수동입력)
    private DataSourceChart dataSourceChart;

    // 주간 출역 현황
    private List<WeeklyAttendanceChart> weeklyAttendanceChart;

    /**
     * 일별 출역 추이 차트 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyAttendanceChart {
        private String date; // YYYY-MM-DD 형식
        private Long workerCount; // 출역자 수
        private BigDecimal totalWorkHours; // 총 근무시간
    }

    /**
     * 공종별 분포 파이 차트 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobTypePieChart {
        private String jobType; // 공종명
        private String jobTypeName; // 공종 표시명
        private Long count; // 건수
        private BigDecimal percentage; // 비율 (%)
    }

    /**
     * 데이터 소스별 차트 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSourceChart {
        private Long faceRecognitionCount; // 안면인식 건수
        private Long manualEntryCount; // 수동입력 건수
        private Long adminCorrectionCount; // 관리자수정 건수
        private BigDecimal faceRecognitionPercentage; // 안면인식 비율 (%)
        private BigDecimal manualEntryPercentage; // 수동입력 비율 (%)
        private BigDecimal adminCorrectionPercentage; // 관리자수정 비율 (%)
    }

    /**
     * 주간 출역 현황 차트 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyAttendanceChart {
        private String weekLabel; // 주차 레이블 (예: "1주차", "2주차")
        private String startDate; // 시작일 (YYYY-MM-DD)
        private String endDate; // 종료일 (YYYY-MM-DD)
        private Long workerCount; // 출역자 수
        private BigDecimal totalWorkHours; // 총 근무시간
    }
}
