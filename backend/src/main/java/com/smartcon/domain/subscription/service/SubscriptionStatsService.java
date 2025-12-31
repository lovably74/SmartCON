package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.*;

import java.util.List;

/**
 * 구독 통계 서비스 인터페이스
 * 
 * 대시보드에서 사용되는 각종 통계 데이터를 제공합니다.
 * 캐싱을 통해 성능을 최적화합니다.
 */
public interface SubscriptionStatsService {
    
    /**
     * 구독 상태별 통계 조회 (캐시 적용)
     * 
     * @return 구독 통계
     */
    SubscriptionStatsDto getSubscriptionStats();
    
    /**
     * 월별 승인 통계 조회 (캐시 적용)
     * 
     * @return 월별 승인 통계 목록
     */
    List<MonthlyApprovalStatsDto> getMonthlyApprovalStats();
    
    /**
     * 관리자별 승인 통계 조회 (캐시 적용)
     * 
     * @return 관리자별 승인 통계 목록
     */
    List<AdminApprovalStatsDto> getAdminApprovalStats();
    
    /**
     * 일별 승인 통계 조회 (캐시 적용)
     * 
     * @return 일별 승인 통계 목록
     */
    List<DailyApprovalStatsDto> getDailyApprovalStats();
    
    /**
     * 자동 승인 효율성 통계 조회 (캐시 적용)
     * 
     * @return 자동 승인 효율성 통계
     */
    AutoApprovalEfficiencyDto getAutoApprovalEfficiencyStats();
    
    /**
     * 승인 처리 성능 분석 (캐시 적용)
     * 
     * @return 승인 처리 성능 분석 결과
     */
    List<ApprovalPerformanceDto> getApprovalPerformanceAnalysis();
    
    /**
     * 알림 발송 통계 조회 (캐시 적용)
     * 
     * @return 알림 통계 목록
     */
    List<NotificationStatsDto> getNotificationStats();
    
    /**
     * 일별 알림 발송 통계 조회 (캐시 적용)
     * 
     * @return 일별 알림 통계 목록
     */
    List<DailyNotificationStatsDto> getDailyNotificationStats();
    
    /**
     * 24시간 이상 대기 중인 승인 건수 조회
     * 
     * @return 지연된 승인 건수
     */
    long getOverduePendingApprovalsCount();
    
    /**
     * 통계 캐시 갱신
     */
    void refreshStatsCache();
    
    /**
     * 특정 통계 캐시 삭제
     * 
     * @param cacheKey 캐시 키
     */
    void evictStatsCache(String cacheKey);
}