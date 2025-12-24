package com.smartcon.domain.attendance.entity;

import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 출근 기록 엔티티
 * MariaDB 최적화 적용
 */
@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceLog extends BaseTenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate; // 근무일자

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt; // 출근시간

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt; // 퇴근시간

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", length = 10)
    private AuthType authType = AuthType.FACE; // 인증방식

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore; // 안면인식 신뢰도 (0.0000 ~ 1.0000)

    @Column(name = "man_day", precision = 3, scale = 2)
    private BigDecimal manDay = BigDecimal.ONE; // 공수 (예: 1.00, 0.50)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15)
    private AttendanceStatus status = AttendanceStatus.NORMAL; // 출근상태

    // 위치 정보 (GPS 좌표)
    @Column(name = "check_in_latitude", precision = 10, scale = 8)
    private BigDecimal checkInLatitude; // 출근 위도

    @Column(name = "check_in_longitude", precision = 11, scale = 8)
    private BigDecimal checkInLongitude; // 출근 경도

    @Column(name = "check_out_latitude", precision = 10, scale = 8)
    private BigDecimal checkOutLatitude; // 퇴근 위도

    @Column(name = "check_out_longitude", precision = 11, scale = 8)
    private BigDecimal checkOutLongitude; // 퇴근 경도

    /**
     * 인증 방식 열거형
     */
    public enum AuthType {
        FACE,   // 안면인식
        MANUAL  // 수동입력
    }

    /**
     * 출근 상태 열거형
     */
    public enum AttendanceStatus {
        NORMAL,      // 정상
        LATE,        // 지각
        ABSENT,      // 결근
        EARLY_LEAVE  // 조퇴
    }

    /**
     * 출근 여부 확인
     */
    public boolean isCheckedIn() {
        return checkInAt != null;
    }

    /**
     * 퇴근 여부 확인
     */
    public boolean isCheckedOut() {
        return checkOutAt != null;
    }

    /**
     * 근무 완료 여부 확인 (출근 + 퇴근)
     */
    public boolean isWorkCompleted() {
        return isCheckedIn() && isCheckedOut();
    }

    /**
     * 안면인식 사용 여부 확인
     */
    public boolean isFaceAuthUsed() {
        return AuthType.FACE.equals(authType);
    }

    /**
     * 정상 출근 여부 확인
     */
    public boolean isNormalAttendance() {
        return AttendanceStatus.NORMAL.equals(status);
    }

    /**
     * 출근 처리
     */
    public void checkIn(LocalDateTime checkInTime, AuthType authType, BigDecimal latitude, BigDecimal longitude) {
        this.checkInAt = checkInTime;
        this.authType = authType;
        this.checkInLatitude = latitude;
        this.checkInLongitude = longitude;
        
        if (this.workDate == null) {
            this.workDate = checkInTime.toLocalDate();
        }
    }

    /**
     * 퇴근 처리
     */
    public void checkOut(LocalDateTime checkOutTime, BigDecimal latitude, BigDecimal longitude) {
        this.checkOutAt = checkOutTime;
        this.checkOutLatitude = latitude;
        this.checkOutLongitude = longitude;
    }

    /**
     * 안면인식 신뢰도 설정 (0.0 ~ 1.0 범위 검증)
     */
    public void setConfidenceScore(BigDecimal confidenceScore) {
        if (confidenceScore != null) {
            if (confidenceScore.compareTo(BigDecimal.ZERO) < 0 || 
                confidenceScore.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("신뢰도는 0.0과 1.0 사이의 값이어야 합니다: " + confidenceScore);
            }
        }
        this.confidenceScore = confidenceScore;
    }
}
