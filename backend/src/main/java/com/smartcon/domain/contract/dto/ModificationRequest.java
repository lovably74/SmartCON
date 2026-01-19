package com.smartcon.domain.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 계약 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModificationRequest {

    /**
     * 계약 ID
     */
    @NotNull(message = "계약 ID는 필수입니다")
    private Long contractId;

    /**
     * 수정 요청 내용
     */
    @NotBlank(message = "수정 요청 내용은 필수입니다")
    @Size(max = 1000, message = "수정 요청 내용은 최대 1000자까지 입력 가능합니다")
    private String modificationRequest;
}
