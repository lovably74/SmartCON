package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 구독 Repository
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * 테넌트의 현재 활성 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.tenant = :tenant AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveByTenant(@Param("tenant") Tenant tenant);
    
    /**
     * 테넌트의 현재 구독 조회 (모든 상태)
     */
    @Query("SELECT s FROM Subscription s WHERE s.tenant = :tenant ORDER BY s.createdAt DESC")
    Optional<Subscription> findCurrentByTenant(@Param("tenant") Tenant tenant);
    
    /**
     * 테넌트의 모든 구독 이력 조회 (최신순)
     */
    @Query("SELECT s FROM Subscription s WHERE s.tenant = :tenant ORDER BY s.createdAt DESC")
    List<Subscription> findByTenantOrderByCreatedAtDesc(@Param("tenant") Tenant tenant);
    
    /**
     * 특정 상태의 구독 목록 조회
     */
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    /**
     * 다음 결제일이 특정 날짜인 구독 목록 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate = :date AND s.status = 'ACTIVE'")
    List<Subscription> findByNextBillingDate(@Param("date") LocalDate date);
    
    /**
     * 체험판 만료 예정 구독 목록 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.trialEndDate = :date AND s.status = 'ACTIVE'")
    List<Subscription> findTrialEndingOn(@Param("date") LocalDate date);
    
    /**
     * 만료 예정 구독 목록 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate = :date AND s.status = 'ACTIVE'")
    List<Subscription> findExpiringOn(@Param("date") LocalDate date);
    
    /**
     * 테넌트의 활성 구독 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.tenant = :tenant AND s.status = 'ACTIVE'")
    boolean existsActiveByTenant(@Param("tenant") Tenant tenant);
    
    /**
     * 특정 시간 이전에 생성된 승인 대기 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'PENDING_APPROVAL' AND s.approvalRequestedAt < :cutoffTime")
    List<Subscription> findPendingSubscriptionsOlderThan(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
    
    /**
     * 특정 상태의 구독 목록을 페이지네이션으로 조회 (생성일 오름차순)
     */
    org.springframework.data.domain.Page<Subscription> findByStatusOrderByCreatedAtAsc(
        SubscriptionStatus status, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 특정 상태의 구독 수 조회
     */
    long countByStatus(SubscriptionStatus status);
    
    /**
     * 특정 상태이면서 특정 시간 이전에 생성된 구독 수 조회
     */
    long countByStatusAndCreatedAtBefore(SubscriptionStatus status, java.time.LocalDateTime createdAt);
    
    /**
     * 구독 데이터 내보내기용 쿼리
     */
    @Query("SELECT s.id, t.companyName, sp.name, s.status, s.monthlyPrice, s.createdAt, " +
           "sa.processedAt, p.createdAt, sa.reason, u.name, sa.autoApproved " +
           "FROM Subscription s " +
           "LEFT JOIN s.tenant t " +
           "LEFT JOIN s.plan sp " +
           "LEFT JOIN SubscriptionApproval sa ON sa.subscriptionId = s.id " +
           "LEFT JOIN sa.admin u " +
           "LEFT JOIN Payment p ON p.subscription = s " +
           "WHERE (:status IS NULL OR s.status = :status) " +
           "AND (:startDate IS NULL OR s.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR s.createdAt <= :endDate) " +
           "ORDER BY s.createdAt DESC")
    List<Object[]> getSubscriptionExportData(
            @Param("status") SubscriptionStatus status,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
    
    // =============================================================================
    // 성능 최적화된 쿼리 메서드들
    // =============================================================================
    
    /**
     * 승인 대기 목록 조회 (성능 최적화된 쿼리)
     * 인덱스 활용: idx_subscriptions_status_requested_at
     */
    @Query(value = "SELECT s.id, s.tenant_id, s.plan_id, s.status, s.approval_requested_at, " +
                   "t.company_name, t.ceo_name, t.contact_email, " +
                   "sp.plan_name, sp.monthly_price " +
                   "FROM subscriptions s " +
                   "FORCE INDEX (idx_subscriptions_status_requested_at) " +
                   "JOIN tenants t ON s.tenant_id = t.id " +
                   "JOIN subscription_plans sp ON s.plan_id = sp.id " +
                   "WHERE s.status = 'PENDING_APPROVAL' " +
                   "ORDER BY s.approval_requested_at DESC " +
                   "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Object[]> findPendingApprovalsOptimized(@Param("limit") int limit, @Param("offset") int offset);
    
    /**
     * 승인 대기 목록 총 개수 조회 (성능 최적화)
     * 인덱스 활용: idx_subscriptions_status_requested_at
     */
    @Query(value = "SELECT COUNT(*) FROM subscriptions " +
                   "FORCE INDEX (idx_subscriptions_status_requested_at) " +
                   "WHERE status = 'PENDING_APPROVAL'", 
           nativeQuery = true)
    long countPendingApprovalsOptimized();
    
    /**
     * 대시보드 통계 조회 (성능 최적화)
     * 인덱스 활용: idx_subscriptions_stats_status_date
     */
    @Query(value = "SELECT " +
                   "COUNT(CASE WHEN status = 'PENDING_APPROVAL' THEN 1 END) as pending_count, " +
                   "COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count, " +
                   "COUNT(CASE WHEN status = 'SUSPENDED' THEN 1 END) as suspended_count, " +
                   "COUNT(CASE WHEN status = 'TERMINATED' THEN 1 END) as terminated_count, " +
                   "COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_count " +
                   "FROM subscriptions " +
                   "FORCE INDEX (idx_subscriptions_stats_status_date)", 
           nativeQuery = true)
    Object[] getSubscriptionStatsOptimized();
    
    /**
     * 24시간 이상 대기 중인 승인 건수 조회 (성능 최적화)
     */
    @Query(value = "SELECT COUNT(*) FROM subscriptions " +
                   "FORCE INDEX (idx_subscriptions_status_requested_at) " +
                   "WHERE status = 'PENDING_APPROVAL' " +
                   "AND approval_requested_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)", 
           nativeQuery = true)
    long countOverduePendingApprovals();
    
    /**
     * 테넌트별 구독 필터링 (성능 최적화)
     * 인덱스 활용: idx_subscriptions_tenant_status
     */
    @Query(value = "SELECT s.* FROM subscriptions s " +
                   "FORCE INDEX (idx_subscriptions_tenant_status) " +
                   "JOIN tenants t ON s.tenant_id = t.id " +
                   "WHERE (:status IS NULL OR s.status = :status) " +
                   "AND (:tenantName IS NULL OR t.company_name LIKE CONCAT('%', :tenantName, '%')) " +
                   "ORDER BY s.created_at DESC " +
                   "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Subscription> findSubscriptionsFilteredOptimized(
            @Param("status") String status,
            @Param("tenantName") String tenantName,
            @Param("limit") int limit,
            @Param("offset") int offset);
    
    /**
     * 필터링된 구독 총 개수 조회
     */
    @Query(value = "SELECT COUNT(*) FROM subscriptions s " +
                   "FORCE INDEX (idx_subscriptions_tenant_status) " +
                   "JOIN tenants t ON s.tenant_id = t.id " +
                   "WHERE (:status IS NULL OR s.status = :status) " +
                   "AND (:tenantName IS NULL OR t.company_name LIKE CONCAT('%', :tenantName, '%'))", 
           nativeQuery = true)
    long countSubscriptionsFilteredOptimized(
            @Param("status") String status,
            @Param("tenantName") String tenantName);
    
    /**
     * 커서 기반 페이지네이션을 위한 승인 대기 목록 조회
     * 인덱스 활용: idx_subscriptions_cursor_pagination
     */
    @Query(value = "SELECT s.* FROM subscriptions s " +
                   "FORCE INDEX (idx_subscriptions_cursor_pagination) " +
                   "WHERE s.status = 'PENDING_APPROVAL' " +
                   "AND (:cursorId IS NULL OR s.id > :cursorId) " +
                   "ORDER BY s.id ASC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Subscription> findPendingApprovalsCursorBased(
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);
    
    /**
     * 월별 승인 통계 조회 (성능 최적화)
     */
    @Query(value = "SELECT " +
                   "YEAR(s.created_at) as year, " +
                   "MONTH(s.created_at) as month, " +
                   "COUNT(*) as total_requests, " +
                   "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) as approved_count, " +
                   "COUNT(CASE WHEN s.status = 'REJECTED' THEN 1 END) as rejected_count " +
                   "FROM subscriptions s " +
                   "WHERE s.created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                   "GROUP BY YEAR(s.created_at), MONTH(s.created_at) " +
                   "ORDER BY year DESC, month DESC", 
           nativeQuery = true)
    List<Object[]> getMonthlyApprovalStats();
}