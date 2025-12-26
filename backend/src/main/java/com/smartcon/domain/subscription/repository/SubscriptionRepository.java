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
}