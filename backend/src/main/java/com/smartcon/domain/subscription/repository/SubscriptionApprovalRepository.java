package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.SubscriptionApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 구독 승인 이력 리포지토리
 */
@Repository
public interface SubscriptionApprovalRepository extends JpaRepository<SubscriptionApproval, Long> {
    
    /**
     * 특정 구독의 승인 이력 조회 (시간순 정렬)
     */
    List<SubscriptionApproval> findBySubscriptionIdOrderByProcessedAtDesc(Long subscriptionId);
    
    /**
     * 특정 관리자의 승인 이력 조회
     */
    Page<SubscriptionApproval> findByAdminIdOrderByProcessedAtDesc(Long adminId, Pageable pageable);
    
    /**
     * 기간별 승인 이력 조회
     */
    @Query("SELECT sa FROM SubscriptionApproval sa " +
           "WHERE sa.processedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.processedAt DESC")
    Page<SubscriptionApproval> findByProcessedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * 특정 액션 타입별 승인 이력 조회
     */
    @Query("SELECT sa FROM SubscriptionApproval sa " +
           "WHERE sa.action = :action " +
           "ORDER BY sa.processedAt DESC")
    Page<SubscriptionApproval> findByAction(
            @Param("action") com.smartcon.domain.subscription.entity.ApprovalAction action,
            Pageable pageable);
    
    /**
     * 특정 구독의 최근 승인 이력 조회
     */
    @Query("SELECT sa FROM SubscriptionApproval sa " +
           "WHERE sa.subscriptionId = :subscriptionId " +
           "ORDER BY sa.processedAt DESC " +
           "LIMIT 1")
    SubscriptionApproval findLatestBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
    
    /**
     * 관리자별 승인 통계 조회
     */
    @Query("SELECT sa.admin.id, COUNT(sa) FROM SubscriptionApproval sa " +
           "WHERE sa.processedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY sa.admin.id " +
           "ORDER BY COUNT(sa) DESC")
    List<Object[]> getApprovalStatsByAdmin(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 액션별 승인 통계 조회
     */
    @Query("SELECT sa.action, COUNT(sa) FROM SubscriptionApproval sa " +
           "WHERE sa.processedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY sa.action " +
           "ORDER BY COUNT(sa) DESC")
    List<Object[]> getApprovalStatsByAction(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 자동 승인 여부별 승인 수 조회
     */
    long countByAutoApproved(boolean autoApproved);
    
    /**
     * 평균 처리 시간 조회 (시간 단위)
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, s.createdAt, sa.processedAt)) " +
           "FROM SubscriptionApproval sa " +
           "JOIN Subscription s ON s.id = sa.subscriptionId " +
           "WHERE sa.action IN ('APPROVE', 'AUTO_APPROVE')")
    List<Object[]> getAverageProcessingTime();
    
    // =============================================================================
    // 성능 최적화된 쿼리 메서드들
    // =============================================================================
    
    /**
     * 승인 이력 조회 (성능 최적화)
     * 인덱스 활용: idx_subscription_approvals_subscription_id
     */
    @Query(value = "SELECT sa.id, sa.from_status, sa.to_status, sa.reason, sa.action, " +
                   "sa.processed_at, u.name as admin_name, u.email as admin_email " +
                   "FROM subscription_approvals sa " +
                   "FORCE INDEX (idx_subscription_approvals_subscription_id) " +
                   "JOIN users u ON sa.admin_id = u.id " +
                   "WHERE sa.subscription_id = :subscriptionId " +
                   "ORDER BY sa.processed_at DESC", 
           nativeQuery = true)
    List<Object[]> findApprovalHistoryOptimized(@Param("subscriptionId") Long subscriptionId);
    
    /**
     * 관리자별 승인 통계 조회 (성능 최적화)
     * 인덱스 활용: idx_subscription_approvals_admin_action_date
     */
    @Query(value = "SELECT u.name, u.email, sa.action, COUNT(*) as approval_count, " +
                   "AVG(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as avg_processing_minutes " +
                   "FROM subscription_approvals sa " +
                   "FORCE INDEX (idx_subscription_approvals_admin_action_date) " +
                   "JOIN users u ON sa.admin_id = u.id " +
                   "JOIN subscriptions s ON sa.subscription_id = s.id " +
                   "WHERE sa.processed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                   "GROUP BY u.id, u.name, u.email, sa.action " +
                   "ORDER BY approval_count DESC", 
           nativeQuery = true)
    List<Object[]> getAdminApprovalStatsOptimized();
    
    /**
     * 일별 승인 통계 조회 (성능 최적화)
     * 인덱스 활용: idx_subscription_approvals_action_date
     */
    @Query(value = "SELECT DATE(sa.processed_at) as approval_date, " +
                   "sa.action, " +
                   "COUNT(*) as total_processed, " +
                   "AVG(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as avg_processing_minutes " +
                   "FROM subscription_approvals sa " +
                   "FORCE INDEX (idx_subscription_approvals_action_date) " +
                   "JOIN subscriptions s ON sa.subscription_id = s.id " +
                   "WHERE sa.processed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                   "GROUP BY DATE(sa.processed_at), sa.action " +
                   "ORDER BY approval_date DESC, sa.action", 
           nativeQuery = true)
    List<Object[]> getDailyApprovalStatsOptimized();
    
    /**
     * 자동 승인 효율성 통계 조회 (성능 최적화)
     */
    @Query(value = "SELECT " +
                   "COUNT(CASE WHEN sa.action = 'AUTO_APPROVE' THEN 1 END) as auto_approved, " +
                   "COUNT(CASE WHEN sa.action IN ('APPROVE', 'REJECT') THEN 1 END) as manual_processed, " +
                   "ROUND(COUNT(CASE WHEN sa.action = 'AUTO_APPROVE' THEN 1 END) * 100.0 / COUNT(*), 2) as auto_approval_rate " +
                   "FROM subscription_approvals sa " +
                   "WHERE sa.processed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)", 
           nativeQuery = true)
    Object[] getAutoApprovalEfficiencyStats();
    
    /**
     * 커서 기반 페이지네이션을 위한 승인 이력 조회
     * 인덱스 활용: idx_approvals_cursor_pagination
     */
    @Query(value = "SELECT sa.* FROM subscription_approvals sa " +
                   "FORCE INDEX (idx_approvals_cursor_pagination) " +
                   "WHERE sa.subscription_id = :subscriptionId " +
                   "AND (:cursorId IS NULL OR sa.id > :cursorId) " +
                   "ORDER BY sa.id ASC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<SubscriptionApproval> findApprovalHistoryCursorBased(
            @Param("subscriptionId") Long subscriptionId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);
    
    /**
     * 승인 처리 성능 분석 쿼리 (성능 최적화)
     */
    @Query(value = "SELECT " +
                   "sa.action, " +
                   "COUNT(*) as total_count, " +
                   "MIN(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as min_processing_minutes, " +
                   "MAX(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as max_processing_minutes, " +
                   "AVG(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as avg_processing_minutes, " +
                   "STDDEV(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as stddev_processing_minutes " +
                   "FROM subscription_approvals sa " +
                   "JOIN subscriptions s ON sa.subscription_id = s.id " +
                   "WHERE sa.processed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                   "GROUP BY sa.action " +
                   "ORDER BY avg_processing_minutes ASC", 
           nativeQuery = true)
    List<Object[]> getApprovalPerformanceAnalysis();
}