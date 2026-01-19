package com.smartcon.domain.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 안면인식기 디바이스 등록/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceDeviceRequest {

    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    @NotBlank(message = "시리얼 번호는 필수입니다")
    private String serialNumber;

    private String deviceName;

    private String apiEndpoint;

    private String apiKey;

    private Boolean isActive;

    private String location;

    private String firmwareVersion;
}
