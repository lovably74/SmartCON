package com.smartcon.domain.project.dto;

import com.smartcon.domain.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로젝트 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;
    private String name; // 현장명
    private LocalDate constructionPeriodStart; // 공사 시작일
    private LocalDate constructionPeriodEnd; // 공사 종료일
    private String siteManagerName; // 현장소장명
    private Project.ProjectStatus status; // 프로젝트 상태
    private String location; // 현장 위치
    private String description; // 프로젝트 설명
    private Long tenantId; // 테넌트 ID
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
    private List<FaceDeviceInfo> faceDevices; // 안면인식기 목록
    private List<ProjectManagerInfo> projectManagers; // 프로젝트 관리자 목록

    /**
     * 안면인식기 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FaceDeviceInfo {
        private Long id;
        private String serialNumber;
        private String deviceName;
        private Boolean isActive;
        private String syncStatus;
        private LocalDateTime lastSyncAt;
    }

    /**
     * 프로젝트 관리자 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectManagerInfo {
        private Long id;
        private Long userId;
        private String userName;
        private String userEmail;
        private Boolean isActive;
        private LocalDateTime assignedAt;
    }

    /**
     * Project 엔티티로부터 ProjectResponse 생성
     */
    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .constructionPeriodStart(project.getConstructionPeriodStart())
                .constructionPeriodEnd(project.getConstructionPeriodEnd())
                .siteManagerName(project.getSiteManagerName())
                .status(project.getStatus())
                .location(project.getLocation())
                .description(project.getDescription())
                .tenantId(project.getTenantId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .faceDevices(project.getFaceDevices().stream()
                        .map(device -> FaceDeviceInfo.builder()
                                .id(device.getId())
                                .serialNumber(device.getSerialNumber())
                                .deviceName(device.getDeviceName())
                                .isActive(device.getIsActive())
                                .syncStatus(device.getSyncStatus().name())
                                .lastSyncAt(device.getLastSyncAt())
                                .build())
                        .collect(Collectors.toList()))
                .projectManagers(project.getProjectManagers().stream()
                        .map(manager -> ProjectManagerInfo.builder()
                                .id(manager.getId())
                                .userId(manager.getUser().getId())
                                .userName(manager.getUser().getName())
                                .userEmail(manager.getUser().getEmail())
                                .isActive(manager.getIsActive())
                                .assignedAt(manager.getAssignedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Project 엔티티 목록으로부터 ProjectResponse 목록 생성
     */
    public static List<ProjectResponse> fromList(List<Project> projects) {
        return projects.stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }
}
