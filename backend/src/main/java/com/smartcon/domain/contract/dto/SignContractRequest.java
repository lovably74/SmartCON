package com.smartcon.domain.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 계약서 서명 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignContractRequest {

    /**
     * 계약 ID
     */
    @NotNull(message = "계약 ID는 필수입니다")
    private Long contractId;

    /**
     * 전자서명 데이터 (Base64 인코딩)
     */
    @NotBlank(message = "전자서명 데이터는 필수입니다")
    private String signatureData;
}
