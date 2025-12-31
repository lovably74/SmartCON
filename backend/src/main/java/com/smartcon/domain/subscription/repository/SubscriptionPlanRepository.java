package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 구독 요금제 Repository
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    
    /**
     * 활성화된 요금제 목록 조회 (정렬 순서대로)
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = true ORDER BY sp.sortOrder ASC")
    List<SubscriptionPlan> findActiveOrderBySortOrder();
    
    /**
     * 활성화된 요금제 조회
     */
    Optional<SubscriptionPlan> findByPlanIdAndIsActiveTrue(String planId);
    
    /**
     * 요금제 존재 여부 확인
     */
    boolean existsByPlanIdAndIsActiveTrue(String planId);
}