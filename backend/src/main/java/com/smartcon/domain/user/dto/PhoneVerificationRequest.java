package com.smartcon.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 휴대폰 인증 요청 DTO
 * CI값 생성을 위한 휴대폰 인증
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneVerificationRequest {

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber; // 휴대폰 번호 (01012345678 형식)

    @NotBlank(message = "인증 코드는 필수입니다")
    private String verificationCode; // SMS 인증 코드
}
