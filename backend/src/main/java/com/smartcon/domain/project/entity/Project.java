package com.smartcon.domain.project.entity;

import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 건설 프로젝트(현장) 엔티티
 * 5단계 역할 기반 시스템의 핵심 도메인
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseTenantEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 현장명

    @Column(name = "construction_period_start")
    private LocalDate constructionPeriodStart; // 공사 시작일

    @Column(name = "construction_period_end")
    private LocalDate constructionPeriodEnd; // 공사 종료일

    @Column(name = "site_manager_name", length = 50)
    private String siteManagerName; // 현장소장명

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE; // 프로젝트 상태

    @Column(name = "location", length = 200)
    private String location; // 현장 위치

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 프로젝트 설명

    // 안면인식기 디바이스 목록
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FaceRecognitionDevice> faceDevices = new ArrayList<>();

    // 프로젝트 관리자 목록 (현장관리자들)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectManager> projectManagers = new ArrayList<>();

    /**
     * 프로젝트 상태 열거형
     */
    public enum ProjectStatus {
        ACTIVE("진행중"),
        PAUSED("일시중지"),
        COMPLETED("완료");

        private final String displayName;

        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 프로젝트가 활성 상태인지 확인
     */
    public boolean isActive() {
        return ProjectStatus.ACTIVE.equals(this.status);
    }

    /**
     * 안면인식기 디바이스 추가
     */
    public void addFaceDevice(FaceRecognitionDevice device) {
        this.faceDevices.add(device);
        device.setProject(this);
    }

    /**
     * 프로젝트 관리자 추가
     */
    public void addProjectManager(ProjectManager manager) {
        this.projectManagers.add(manager);
        manager.setProject(this);
    }
}