package com.smartcon.domain.contract.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 계약서 재발송 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendContractRequest {

    /**
     * 계약 ID
     */
    @NotNull(message = "계약 ID는 필수입니다")
    private Long contractId;

    /**
     * 재발송 사유 (선택사항)
     */
    private String reason;
}
