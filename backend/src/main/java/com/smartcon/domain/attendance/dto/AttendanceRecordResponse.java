package com.smartcon.domain.attendance.dto;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 출역 기록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordResponse {

    private Long id;
    private Long workerId;
    private String workerName;
    private Long projectId;
    private String projectName;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal workHours;
    private BigDecimal unitPrice;
    private AttendanceRecord.JobType jobType;
    private String jobTypeName;
    private String teamName;
    private String changeReason;
    private BigDecimal faceMatchConfidence;
    private AttendanceRecord.AttendanceDataSource dataSource;
    private String dataSourceName;
    private Long lastModifiedBy;
    private String timeModificationReason;
    private LocalDateTime originalCheckInTime;
    private LocalDateTime originalCheckOutTime;
    private boolean timeModified;
    private boolean workCompleted;
    private boolean confidenceAboveThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * AttendanceRecord 엔티티로부터 DTO 생성
     */
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .workerId(record.getWorker().getId())
                .workerName(record.getWorker().getName())
                .projectId(record.getProject().getId())
                .projectName(record.getProject().getName())
                .workDate(record.getWorkDate())
                .checkInTime(record.getCheckInTime())
                .checkOutTime(record.getCheckOutTime())
                .workHours(record.getWorkHours())
                .unitPrice(record.getUnitPrice())
                .jobType(record.getJobType())
                .jobTypeName(record.getJobType() != null ? record.getJobType().getDisplayName() : null)
                .teamName(record.getTeamName())
                .changeReason(record.getChangeReason())
                .faceMatchConfidence(record.getFaceMatchConfidence())
                .dataSource(record.getDataSource())
                .dataSourceName(record.getDataSource() != null ? record.getDataSource().getDisplayName() : null)
                .lastModifiedBy(record.getLastModifiedBy())
                .timeModificationReason(record.getTimeModificationReason())
                .originalCheckInTime(record.getOriginalCheckInTime())
                .originalCheckOutTime(record.getOriginalCheckOutTime())
                .timeModified(record.isTimeModified())
                .workCompleted(record.isWorkCompleted())
                .confidenceAboveThreshold(record.isConfidenceAboveThreshold())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
