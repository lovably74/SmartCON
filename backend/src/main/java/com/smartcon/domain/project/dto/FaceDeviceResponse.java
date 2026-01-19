package com.smartcon.domain.project.dto;

import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 안면인식기 디바이스 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceDeviceResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String serialNumber;
    private String deviceName;
    private String apiEndpoint;
    private Boolean isActive;
    private LocalDateTime lastSyncAt;
    private FaceRecognitionDevice.DeviceSyncStatus syncStatus;
    private String syncStatusName;
    private String location;
    private String firmwareVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * FaceRecognitionDevice 엔티티로부터 DTO 생성
     */
    public static FaceDeviceResponse from(FaceRecognitionDevice device) {
        return FaceDeviceResponse.builder()
                .id(device.getId())
                .projectId(device.getProject().getId())
                .projectName(device.getProject().getName())
                .serialNumber(device.getSerialNumber())
                .deviceName(device.getDeviceName())
                .apiEndpoint(device.getApiEndpoint())
                .isActive(device.getIsActive())
                .lastSyncAt(device.getLastSyncAt())
                .syncStatus(device.getSyncStatus())
                .syncStatusName(device.getSyncStatus() != null ? device.getSyncStatus().getDisplayName() : null)
                .location(device.getLocation())
                .firmwareVersion(device.getFirmwareVersion())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
