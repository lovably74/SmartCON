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
}