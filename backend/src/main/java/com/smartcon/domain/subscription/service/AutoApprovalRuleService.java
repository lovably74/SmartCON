package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.AutoApprovalRuleDto;
import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 자동 승인 규칙 서비스 인터페이스
 * 
 * 구독 신청에 대한 자동 승인 규칙을 관리하고 평가하는 기능을 제공합니다.
 */
public interface AutoApprovalRuleService {
    
    /**
     * 자동 승인 규칙 생성
     */
    AutoApprovalRuleDto createRule(AutoApprovalRuleDto ruleDto);
    
    /**
     * 자동 승인 규칙 수정
     */
    AutoApprovalRuleDto updateRule(Long ruleId, AutoApprovalRuleDto ruleDto);
    
    /**
     * 자동 승인 규칙 삭제
     */
    void deleteRule(Long ruleId);
    
    /**
     * 자동 승인 규칙 활성화/비활성화
     */
    AutoApprovalRuleDto toggleRuleStatus(Long ruleId, boolean isActive);
    
    /**
     * 모든 자동 승인 규칙 조회 (우선순위 순)
     */
    Page<AutoApprovalRuleDto> getAllRules(Pageable pageable);
    
    /**
     * 활성화된 자동 승인 규칙 조회 (우선순위 순)
     */
    List<AutoApprovalRuleDto> getActiveRules();
    
    /**
     * 구독 요청에 대한 자동 승인 여부 평가
     */
    boolean evaluateAutoApproval(CreateSubscriptionRequest request);
    
    /**
     * 구독 요청에 적용된 자동 승인 규칙 조회
     */
    AutoApprovalRuleDto getAppliedRule(CreateSubscriptionRequest request);
    
    /**
     * 자동 승인 기능 전체 활성화/비활성화
     */
    void toggleAutoApprovalSystem(boolean enabled);
    
    /**
     * 자동 승인 시스템 활성화 상태 조회
     */
    boolean isAutoApprovalEnabled();
}