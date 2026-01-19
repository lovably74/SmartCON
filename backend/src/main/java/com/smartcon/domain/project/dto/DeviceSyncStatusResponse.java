package com.smartcon.domain.project.dto;

import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 디바이스 동기화 상태 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSyncStatusResponse {

    private String serialNumber;
    private String deviceName;
    private FaceRecognitionDevice.DeviceSyncStatus syncStatus;
    private String syncStatusName;
    private LocalDateTime lastSyncAt;
    private Boolean isActive;
    private String message;

    /**
     * FaceRecognitionDevice 엔티티로부터 DTO 생성
     */
    public static DeviceSyncStatusResponse from(FaceRecognitionDevice device) {
        return DeviceSyncStatusResponse.builder()
                .serialNumber(device.getSerialNumber())
                .deviceName(device.getDeviceName())
                .syncStatus(device.getSyncStatus())
                .syncStatusName(device.getSyncStatus() != null ? device.getSyncStatus().getDisplayName() : null)
                .lastSyncAt(device.getLastSyncAt())
                .isActive(device.getIsActive())
                .build();
    }

    /**
     * 동기화 상태 메시지와 함께 DTO 생성
     */
    public static DeviceSyncStatusResponse from(FaceRecognitionDevice device, String message) {
        DeviceSyncStatusResponse response = from(device);
        response.setMessage(message);
        return response;
    }
}
