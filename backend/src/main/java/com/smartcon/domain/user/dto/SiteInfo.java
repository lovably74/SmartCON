package com.smartcon.domain.user.dto;

import com.smartcon.domain.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 현장 정보 DTO
 * 역할별 현장 선택을 위한 현장 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfo {
    
    private Long siteId; // 현장 ID
    private String siteName; // 현장명
    private String location; // 현장 위치
    private String status; // 현장 상태
    private LocalDate constructionPeriodStart; // 공사 시작일
    private LocalDate constructionPeriodEnd; // 공사 종료일
    private LocalDateTime lastAccessAt; // 마지막 접근 시간
    private LocalDateTime assignedAt; // 배정 일시
    private Integer remainingDays; // 남은 공사 일수
    
    /**
     * Project 엔티티로부터 SiteInfo 생성
     */
    public static SiteInfo from(Project project) {
        return SiteInfo.builder()
                .siteId(project.getId())
                .siteName(project.getName())
                .location(project.getLocation())
                .status(project.getStatus().getDisplayName())
                .constructionPeriodStart(project.getConstructionPeriodStart())
                .constructionPeriodEnd(project.getConstructionPeriodEnd())
                .remainingDays(calculateRemainingDays(project.getConstructionPeriodEnd()))
                .build();
    }
    
    /**
     * Project와 마지막 접근 시간으로부터 SiteInfo 생성
     */
    public static SiteInfo from(Project project, LocalDateTime lastAccessAt, LocalDateTime assignedAt) {
        return SiteInfo.builder()
                .siteId(project.getId())
                .siteName(project.getName())
                .location(project.getLocation())
                .status(project.getStatus().getDisplayName())
                .constructionPeriodStart(project.getConstructionPeriodStart())
                .constructionPeriodEnd(project.getConstructionPeriodEnd())
                .lastAccessAt(lastAccessAt)
                .assignedAt(assignedAt)
                .remainingDays(calculateRemainingDays(project.getConstructionPeriodEnd()))
                .build();
    }
    
    /**
     * 남은 공사 일수 계산
     */
    private static Integer calculateRemainingDays(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
    }
}
