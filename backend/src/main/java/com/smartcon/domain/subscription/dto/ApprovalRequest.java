package com.smartcon.domain.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 구독 승인/거부 요청 DTO
 */
@Data
public class ApprovalRequest {
    
    @NotBlank(message = "사유는 필수입니다")
    @Size(max = 500, message = "사유는 500자 이내로 입력해주세요")
    private String reason;
}