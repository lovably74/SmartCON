package com.smartcon.domain.attendance.dto;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 출역 기록 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordRequest {

    @NotNull(message = "노무자 ID는 필수입니다")
    private Long workerId;

    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    @NotNull(message = "근무일자는 필수입니다")
    private LocalDate workDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private BigDecimal unitPrice;

    private AttendanceRecord.JobType jobType;

    private String teamName;

    private BigDecimal faceMatchConfidence;

    private AttendanceRecord.AttendanceDataSource dataSource;

    // 위치 정보
    private BigDecimal checkInLatitude;
    private BigDecimal checkInLongitude;
    private BigDecimal checkOutLatitude;
    private BigDecimal checkOutLongitude;
}
