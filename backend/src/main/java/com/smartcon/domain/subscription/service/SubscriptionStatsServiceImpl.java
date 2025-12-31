package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.*;
import com.smartcon.domain.subscription.repository.NotificationRepository;
import com.smartcon.domain.subscription.repository.SubscriptionApprovalRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독 통계 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionStatsServiceImpl implements SubscriptionStatsService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionApprovalRepository subscriptionApprovalRepository;
    private final NotificationRepository notificationRepository;
    
    @Override
    @Cacheable(value = "subscriptionStats", key = "'overall'")
    public SubscriptionStatsDto getSubscriptionStats() {
        log.debug("구독 통계 조회 (캐시 적용)");
        
        Object[] stats = subscriptionRepository.getSubscriptionStatsOptimized();
        
        return SubscriptionStatsDto.builder()
                .pendingCount(((Number) stats[0]).longValue())
                .activeCount(((Number) stats[1]).longValue())
                .suspendedCount(((Number) stats[2]).longValue())
                .terminatedCount(((Number) stats[3]).longValue())
                .rejectedCount(((Number) stats[4]).longValue())
                .build();
    }
    
    @Override
    @Cacheable(value = "approvalStats", key = "'monthly'")
    public List<MonthlyApprovalStatsDto> getMonthlyApprovalStats() {
        log.debug("월별 승인 통계 조회 (캐시 적용)");
        
        List<Object[]> results = subscriptionRepository.getMonthlyApprovalStats();
        
        return results.stream()
                .map(result -> MonthlyApprovalStatsDto.builder()
                        .year(((Number) result[0]).intValue())
                        .month(((Number) result[1]).intValue())
                        .totalRequests(((Number) result[2]).longValue())
                        .approvedCount(((Number) result[3]).longValue())
                        .rejectedCount(((Number) result[4]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "approvalStats", key = "'admin'")
    public List<AdminApprovalStatsDto> getAdminApprovalStats() {
        log.debug("관리자별 승인 통계 조회 (캐시 적용)");
        
        List<Object[]> results = subscriptionApprovalRepository.getAdminApprovalStatsOptimized();
        
        return results.stream()
                .map(result -> AdminApprovalStatsDto.builder()
                        .adminName((String) result[0])
                        .adminEmail((String) result[1])
                        .action(com.smartcon.domain.subscription.entity.ApprovalAction.valueOf((String) result[2]))
                        .approvalCount(((Number) result[3]).longValue())
                        .avgProcessingMinutes(result[4] != null ? ((Number) result[4]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "approvalStats", key = "'daily'")
    public List<DailyApprovalStatsDto> getDailyApprovalStats() {
        log.debug("일별 승인 통계 조회 (캐시 적용)");
        
        List<Object[]> results = subscriptionApprovalRepository.getDailyApprovalStatsOptimized();
        
        return results.stream()
                .map(result -> DailyApprovalStatsDto.builder()
                        .approvalDate(((java.sql.Date) result[0]).toLocalDate())
                        .action(com.smartcon.domain.subscription.entity.ApprovalAction.valueOf((String) result[1]))
                        .totalProcessed(((Number) result[2]).longValue())
                        .avgProcessingMinutes(result[3] != null ? ((Number) result[3]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "approvalStats", key = "'autoEfficiency'")
    public AutoApprovalEfficiencyDto getAutoApprovalEfficiencyStats() {
        log.debug("자동 승인 효율성 통계 조회 (캐시 적용)");
        
        Object[] result = subscriptionApprovalRepository.getAutoApprovalEfficiencyStats();
        
        return AutoApprovalEfficiencyDto.builder()
                .autoApproved(((Number) result[0]).longValue())
                .manualProcessed(((Number) result[1]).longValue())
                .autoApprovalRate(result[2] != null ? ((Number) result[2]).doubleValue() : 0.0)
                .build();
    }
    
    @Override
    @Cacheable(value = "approvalStats", key = "'performance'")
    public List<ApprovalPerformanceDto> getApprovalPerformanceAnalysis() {
        log.debug("승인 처리 성능 분석 조회 (캐시 적용)");
        
        List<Object[]> results = subscriptionApprovalRepository.getApprovalPerformanceAnalysis();
        
        return results.stream()
                .map(result -> ApprovalPerformanceDto.builder()
                        .action(com.smartcon.domain.subscription.entity.ApprovalAction.valueOf((String) result[0]))
                        .totalCount(((Number) result[1]).longValue())
                        .minProcessingMinutes(result[2] != null ? ((Number) result[2]).doubleValue() : 0.0)
                        .maxProcessingMinutes(result[3] != null ? ((Number) result[3]).doubleValue() : 0.0)
                        .avgProcessingMinutes(result[4] != null ? ((Number) result[4]).doubleValue() : 0.0)
                        .stddevProcessingMinutes(result[5] != null ? ((Number) result[5]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "notificationStats", key = "'overall'")
    public List<NotificationStatsDto> getNotificationStats() {
        log.debug("알림 발송 통계 조회 (캐시 적용)");
        
        List<Object[]> results = notificationRepository.getNotificationStatsOptimized();
        
        return results.stream()
                .map(result -> NotificationStatsDto.builder()
                        .type(com.smartcon.domain.subscription.entity.NotificationType.valueOf((String) result[0]))
                        .totalSent(((Number) result[1]).longValue())
                        .totalRead(((Number) result[2]).longValue())
                        .readRate(result[3] != null ? ((Number) result[3]).doubleValue() : 0.0)
                        .avgReadTimeMinutes(result[4] != null ? ((Number) result[4]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "notificationStats", key = "'daily'")
    public List<DailyNotificationStatsDto> getDailyNotificationStats() {
        log.debug("일별 알림 발송 통계 조회 (캐시 적용)");
        
        List<Object[]> results = notificationRepository.getDailyNotificationStats();
        
        return results.stream()
                .map(result -> DailyNotificationStatsDto.builder()
                        .notificationDate(((java.sql.Date) result[0]).toLocalDate())
                        .type(com.smartcon.domain.subscription.entity.NotificationType.valueOf((String) result[1]))
                        .totalSent(((Number) result[2]).longValue())
                        .totalRead(((Number) result[3]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public long getOverduePendingApprovalsCount() {
        // 실시간 데이터이므로 캐시하지 않음
        return subscriptionRepository.countOverduePendingApprovals();
    }
    
    @Override
    @CacheEvict(value = {"subscriptionStats", "approvalStats", "notificationStats"}, allEntries = true)
    public void refreshStatsCache() {
        log.info("통계 캐시 전체 갱신");
    }
    
    @Override
    @CacheEvict(value = {"subscriptionStats", "approvalStats", "notificationStats"}, key = "#cacheKey")
    public void evictStatsCache(String cacheKey) {
        log.info("통계 캐시 삭제: {}", cacheKey);
    }
    
    /**
     * 매시간 통계 캐시 갱신 (스케줄링)
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    @Async("statisticsExecutor")
    public void scheduledStatsRefresh() {
        log.info("스케줄링된 통계 캐시 갱신 시작");
        
        try {
            // 캐시 갱신
            refreshStatsCache();
            
            // 주요 통계 미리 로드
            getSubscriptionStats();
            getMonthlyApprovalStats();
            getAutoApprovalEfficiencyStats();
            getNotificationStats();
            
            log.info("스케줄링된 통계 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("스케줄링된 통계 캐시 갱신 실패", e);
        }
    }
    
    /**
     * 매일 자정에 오래된 통계 데이터 정리
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    @Async("statisticsExecutor")
    public void scheduledStatsCleanup() {
        log.info("스케줄링된 통계 데이터 정리 시작");
        
        try {
            // 90일 이전 통계 데이터 정리 로직
            // 실제 구현에서는 별도의 통계 테이블이 있다면 해당 테이블 정리
            
            // 캐시 갱신
            refreshStatsCache();
            
            log.info("스케줄링된 통계 데이터 정리 완료");
        } catch (Exception e) {
            log.error("스케줄링된 통계 데이터 정리 실패", e);
        }
    }
}