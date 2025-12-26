package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.AutoApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 자동 승인 규칙 리포지토리
 */
@Repository
public interface AutoApprovalRuleRepository extends JpaRepository<AutoApprovalRule, Long> {
    
    /**
     * 활성화된 자동 승인 규칙 조회 (우선순위 순)
     */
    List<AutoApprovalRule> findByIsActiveTrueOrderByPriorityAsc();
    
    /**
     * 활성화된 자동 승인 규칙 조회 (우선순위 내림차순, ID 오름차순)
     */
    List<AutoApprovalRule> findByIsActiveTrueOrderByPriorityDescIdAsc();
    
    /**
     * 모든 자동 승인 규칙 조회 (우선순위 순)
     */
    List<AutoApprovalRule> findAllByOrderByPriorityAsc();
    
    /**
     * 규칙명으로 조회
     */
    AutoApprovalRule findByRuleName(String ruleName);
    
    /**
     * 활성화된 규칙 개수 조회
     */
    @Query("SELECT COUNT(r) FROM AutoApprovalRule r WHERE r.isActive = true")
    long countActiveRules();
    
    /**
     * 특정 우선순위보다 높은 규칙들 조회
     */
    List<AutoApprovalRule> findByPriorityGreaterThanOrderByPriorityAsc(Integer priority);
}