package com.smartcon.domain.contract.dto;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 계약 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRequest {

    /**
     * 노무자 ID
     */
    @NotNull(message = "노무자 ID는 필수입니다")
    private Long workerId;

    /**
     * 프로젝트 ID
     */
    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    /**
     * 근무일자
     */
    @NotNull(message = "근무일자는 필수입니다")
    @FutureOrPresent(message = "근무일자는 현재 또는 미래 날짜여야 합니다")
    private LocalDate workDate;

    /**
     * 공종 (직종)
     */
    @NotNull(message = "공종은 필수입니다")
    private AttendanceRecord.JobType jobType;

    /**
     * 단가 (일당)
     */
    @NotNull(message = "단가는 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "단가는 0보다 커야 합니다")
    @Digits(integer = 8, fraction = 2, message = "단가는 최대 8자리 정수와 2자리 소수로 입력해야 합니다")
    private BigDecimal unitPrice;

    /**
     * 계약 만료일 (선택사항)
     */
    @Future(message = "만료일은 미래 날짜여야 합니다")
    private LocalDate expiryDate;
}
