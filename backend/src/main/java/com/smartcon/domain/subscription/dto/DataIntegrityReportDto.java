package com.smartcon.domain.subscription.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 데이터 무결성 검사 보고서 DTO
 */
@Data
@Builder
public class DataIntegrityReportDto {
    
    /**
     * 검사 실행 시간
     */
    private LocalDateTime checkTime;
    
    /**
     * 전체 검사 소요 시간 (밀리초)
     */
    private long durationMs;
    
    /**
     * 검사한 구독 수
     */
    private long totalSubscriptions;
    
    /**
     * 검사한 승인 이력 수
     */
    private long totalApprovals;
    
    /**
     * 검사한 알림 수
     */
    private long totalNotifications;
    
    /**
     * 발견된 구독 관련 문제 목록
     */
    private List<String> subscriptionIssues;
    
    /**
     * 발견된 승인 이력 관련 문제 목록
     */
    private List<String> approvalHistoryIssues;
    
    /**
     * 발견된 알림 관련 문제 목록
     */
    private List<String> notificationIssues;
    
    /**
     * 자동 복구된 문제 수
     */
    private int autoRecoveredCount;
    
    /**
     * 수동 복구가 필요한 문제 수
     */
    private int manualRecoveryRequiredCount;
    
    /**
     * 전체 문제 수
     */
    public int getTotalIssuesCount() {
        return subscriptionIssues.size() + approvalHistoryIssues.size() + notificationIssues.size();
    }
    
    /**
     * 검사 결과가 정상인지 확인
     */
    public boolean isHealthy() {
        return getTotalIssuesCount() == 0;
    }
}