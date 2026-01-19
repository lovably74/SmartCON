package com.smartcon.domain.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 출근/퇴근 시간 수정 요청 DTO (현장관리자 권한)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeModificationRequest {

    @NotNull(message = "출근시간은 필수입니다")
    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @NotBlank(message = "수정 사유는 필수입니다")
    private String reason;
}
