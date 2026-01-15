package com.smartcon.domain.project.entity;

import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 안면인식기 디바이스 엔티티
 * 현장별 안면인식기 관리 및 실시간 출역 데이터 연동
 */
@Entity
@Table(name = "face_recognition_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceRecognitionDevice extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 소속 프로젝트

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber; // 디바이스 시리얼 번호

    @Column(name = "device_name", length = 100)
    private String deviceName; // 디바이스 이름

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint; // 안면인식기 API 엔드포인트

    @Column(name = "api_key", length = 255)
    private String apiKey; // 안면인식기 API 키

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성 상태

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt; // 마지막 동기화 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    @Builder.Default
    private DeviceSyncStatus syncStatus = DeviceSyncStatus.PENDING; // 동기화 상태

    @Column(name = "location", length = 100)
    private String location; // 디바이스 설치 위치

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion; // 펌웨어 버전

    /**
     * 디바이스 동기화 상태 열거형
     */
    public enum DeviceSyncStatus {
        SYNCED("동기화완료"),
        PENDING("동기화대기"),
        FAILED("동기화실패");

        private final String displayName;

        DeviceSyncStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 디바이스가 활성 상태인지 확인
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * 동기화 상태 업데이트
     */
    public void updateSyncStatus(DeviceSyncStatus status) {
        this.syncStatus = status;
        if (DeviceSyncStatus.SYNCED.equals(status)) {
            this.lastSyncAt = LocalDateTime.now();
        }
    }

    /**
     * 동기화가 성공했는지 확인
     */
    public boolean isSynced() {
        return DeviceSyncStatus.SYNCED.equals(this.syncStatus);
    }
}