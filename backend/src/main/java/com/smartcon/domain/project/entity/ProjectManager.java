package com.smartcon.domain.project.entity;

import com.smartcon.domain.user.entity.User;
import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 프로젝트 관리자 엔티티
 * 현장별 관리자 배정 및 권한 관리
 */
@Entity
@Table(name = "project_managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectManager extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 소속 프로젝트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 관리자 사용자

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ManagerRole role; // 관리자 역할

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now(); // 배정 일시

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성 상태

    @Column(name = "last_access_at")
    private LocalDateTime lastAccessAt; // 마지막 접근 시간

    /**
     * 관리자 역할 열거형
     */
    public enum ManagerRole {
        SITE_MANAGER("현장관리자"),
        TEAM_LEADER("노무팀장");

        private final String displayName;

        ManagerRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 관리자가 활성 상태인지 확인
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * 마지막 접근 시간 업데이트
     */
    public void updateLastAccessTime() {
        this.lastAccessAt = LocalDateTime.now();
    }

    /**
     * 현장관리자인지 확인
     */
    public boolean isSiteManager() {
        return ManagerRole.SITE_MANAGER.equals(this.role);
    }

    /**
     * 팀장인지 확인
     */
    public boolean isTeamLeader() {
        return ManagerRole.TEAM_LEADER.equals(this.role);
    }
}