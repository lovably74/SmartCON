package com.smartcon.domain.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 안면인식기로부터 수신하는 실시간 출역 데이터 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceNetAttendanceData {

    @NotBlank(message = "디바이스 시리얼 번호는 필수입니다")
    private String deviceSerialNumber;

    @NotNull(message = "노무자 ID는 필수입니다")
    private Long workerId;

    @NotNull(message = "인식 시간은 필수입니다")
    private LocalDateTime recognitionTime;

    @NotNull(message = "안면인식 신뢰도는 필수입니다")
    private BigDecimal confidence;

    @NotBlank(message = "이벤트 타입은 필수입니다")
    private String eventType; // CHECK_IN, CHECK_OUT

    // 위치 정보 (선택)
    private BigDecimal latitude;
    private BigDecimal longitude;

    // 안면인식 이미지 URL (선택)
    private String faceImageUrl;

    /**
     * 출근 이벤트인지 확인
     */
    public boolean isCheckInEvent() {
        return "CHECK_IN".equalsIgnoreCase(eventType);
    }

    /**
     * 퇴근 이벤트인지 확인
     */
    public boolean isCheckOutEvent() {
        return "CHECK_OUT".equalsIgnoreCase(eventType);
    }

    /**
     * 신뢰도가 임계값 이상인지 확인 (최소 0.85)
     */
    public boolean isConfidenceAboveThreshold() {
        return confidence != null && 
               confidence.compareTo(new BigDecimal("0.85")) >= 0;
    }
}
