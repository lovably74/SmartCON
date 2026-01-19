package com.smartcon.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 출역 통계 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatisticsResponse {

    // 기본 통계
    private Long totalWorkers; // 총 출역자 수
    private Long todayWorkers; // 오늘 출역자 수
    private Long newWorkers; // 신규 출역자 수
    private BigDecimal averageWorkHours; // 평균 근무시간
    private BigDecimal totalWorkHours; // 총 근무시간

    // 월별 출역 현황
    private List<MonthlyAttendanceData> monthlyAttendance;

    // 공종별 분포
    private List<JobTypeDistribution> jobTypeDistribution;

    // 데이터 소스별 통계 (안면인식 vs 수동입력)
    private DataSourceStatistics dataSourceStatistics;

    // 팀별 통계
    private List<TeamStatistics> teamStatistics;

    /**
     * 월별 출역 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyAttendanceData {
        private String month; // YYYY-MM 형식
        private Long workerCount; // 출역자 수
        private BigDecimal totalWorkHours; // 총 근무시간
        private BigDecimal averageWorkHours; // 평균 근무시간
    }

    /**
     * 공종별 분포
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobTypeDistribution {
        private String jobType; // 공종명
        private String jobTypeName; // 공종 표시명
        private Long workerCount; // 노무자 수
        private BigDecimal percentage; // 비율 (%)
    }

    /**
     * 데이터 소스별 통계
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSourceStatistics {
        private Long faceRecognitionCount; // 안면인식 데이터 수
        private Long manualEntryCount; // 수동입력 데이터 수
        private Long adminCorrectionCount; // 관리자수정 데이터 수
        private BigDecimal faceRecognitionPercentage; // 안면인식 비율 (%)
        private BigDecimal manualEntryPercentage; // 수동입력 비율 (%)
        private BigDecimal adminCorrectionPercentage; // 관리자수정 비율 (%)
    }

    /**
     * 팀별 통계
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamStatistics {
        private String teamName; // 팀명
        private Long workerCount; // 노무자 수
        private BigDecimal totalWorkHours; // 총 근무시간
        private BigDecimal averageWorkHours; // 평균 근무시간
    }
}
