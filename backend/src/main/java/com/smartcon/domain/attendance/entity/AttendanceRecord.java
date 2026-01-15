package com.smartcon.domain.attendance.entity;

import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.user.entity.User;
import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 출역 기록 엔티티 (안면인식기 연동 강화)
 * 실시간 출역 데이터 처리 및 시간 수정 기능 지원
 */
@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker; // 출역 노무자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 소속 프로젝트

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate; // 근무일자

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime; // 출근시간

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime; // 퇴근시간

    @Column(name = "work_hours", precision = 4, scale = 2)
    private BigDecimal workHours; // 근무시간

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice; // 단가

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 20)
    private JobType jobType; // 공종

    @Column(name = "team_name", length = 50)
    private String teamName; // 소속 팀

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason; // 변경사항 및 사유

    // 안면인식 관련 필드
    @Column(name = "face_match_confidence", precision = 5, scale = 4)
    private BigDecimal faceMatchConfidence; // 안면인식 신뢰도 (0.0000 ~ 1.0000)

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 20)
    @Builder.Default
    private AttendanceDataSource dataSource = AttendanceDataSource.MANUAL_ENTRY; // 데이터 소스

    // 시간 수정 관련 필드
    @Column(name = "last_modified_by")
    private Long lastModifiedBy; // 마지막 수정자 ID

    @Column(name = "time_modification_reason", columnDefinition = "TEXT")
    private String timeModificationReason; // 시간 수정 사유

    @Column(name = "original_check_in_time")
    private LocalDateTime originalCheckInTime; // 원본 출근시간

    @Column(name = "original_check_out_time")
    private LocalDateTime originalCheckOutTime; // 원본 퇴근시간

    // 위치 정보
    @Column(name = "check_in_latitude", precision = 10, scale = 8)
    private BigDecimal checkInLatitude; // 출근 위도

    @Column(name = "check_in_longitude", precision = 11, scale = 8)
    private BigDecimal checkInLongitude; // 출근 경도

    @Column(name = "check_out_latitude", precision = 10, scale = 8)
    private BigDecimal checkOutLatitude; // 퇴근 위도

    @Column(name = "check_out_longitude", precision = 11, scale = 8)
    private BigDecimal checkOutLongitude; // 퇴근 경도

    /**
     * 공종 열거형
     */
    public enum JobType {
        CARPENTER("목수"),
        ELECTRICIAN("전기공"),
        PLUMBER("배관공"),
        PAINTER("도장공"),
        WELDER("용접공"),
        MASON("석공"),
        REBAR_WORKER("철근공"),
        CONCRETE_WORKER("콘크리트공"),
        GENERAL_LABORER("일반노무자"),
        EQUIPMENT_OPERATOR("장비운전원");

        private final String displayName;

        JobType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 출역 데이터 소스 구분
     */
    public enum AttendanceDataSource {
        FACE_RECOGNITION("안면인식기"),
        MANUAL_ENTRY("수동입력"),
        ADMIN_CORRECTION("관리자수정");

        private final String displayName;

        AttendanceDataSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 출근 여부 확인
     */
    public boolean isCheckedIn() {
        return checkInTime != null;
    }

    /**
     * 퇴근 여부 확인
     */
    public boolean isCheckedOut() {
        return checkOutTime != null;
    }

    /**
     * 근무 완료 여부 확인
     */
    public boolean isWorkCompleted() {
        return isCheckedIn() && isCheckedOut();
    }

    /**
     * 안면인식 데이터인지 확인
     */
    public boolean isFaceRecognitionData() {
        return AttendanceDataSource.FACE_RECOGNITION.equals(this.dataSource);
    }

    /**
     * 시간이 수정되었는지 확인
     */
    public boolean isTimeModified() {
        return originalCheckInTime != null || originalCheckOutTime != null;
    }

    /**
     * 출근/퇴근 시간 수정 (현장관리자 권한)
     */
    public void modifyTime(LocalDateTime newCheckInTime, LocalDateTime newCheckOutTime, 
                          String reason, Long modifiedBy) {
        // 원본 시간 백업 (최초 수정시에만)
        if (!isTimeModified()) {
            this.originalCheckInTime = this.checkInTime;
            this.originalCheckOutTime = this.checkOutTime;
        }
        
        // 새로운 시간 설정
        this.checkInTime = newCheckInTime;
        this.checkOutTime = newCheckOutTime;
        this.timeModificationReason = reason;
        this.lastModifiedBy = modifiedBy;
        this.dataSource = AttendanceDataSource.ADMIN_CORRECTION;
        
        // 근무시간 재계산
        calculateWorkHours();
    }

    /**
     * 근무시간 계산
     */
    public void calculateWorkHours() {
        if (isWorkCompleted()) {
            long minutes = java.time.Duration.between(checkInTime, checkOutTime).toMinutes();
            this.workHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * 안면인식 신뢰도 설정 (0.0 ~ 1.0 범위 검증)
     */
    public void setFaceMatchConfidence(BigDecimal confidence) {
        if (confidence != null) {
            if (confidence.compareTo(BigDecimal.ZERO) < 0 || 
                confidence.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("안면인식 신뢰도는 0.0과 1.0 사이의 값이어야 합니다: " + confidence);
            }
        }
        this.faceMatchConfidence = confidence;
    }

    /**
     * 안면인식 신뢰도가 임계값 이상인지 확인 (최소 0.85)
     */
    public boolean isConfidenceAboveThreshold() {
        return faceMatchConfidence != null && 
               faceMatchConfidence.compareTo(new BigDecimal("0.85")) >= 0;
    }
}