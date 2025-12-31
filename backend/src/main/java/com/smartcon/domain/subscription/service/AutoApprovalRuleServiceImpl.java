package com.smartcon.domain.subscription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcon.domain.subscription.dto.AutoApprovalRuleDto;
import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.entity.AutoApprovalRule;
import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import com.smartcon.domain.subscription.repository.AutoApprovalRuleRepository;
import com.smartcon.domain.subscription.repository.SubscriptionPlanRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 자동 승인 규칙 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AutoApprovalRuleServiceImpl implements AutoApprovalRuleService {
    
    private final AutoApprovalRuleRepository autoApprovalRuleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;
    
    // 자동 승인 시스템 전체 활성화 상태를 관리
    private static boolean autoApprovalSystemEnabled = true; // 기본값: 활성화
    
    @Override
    @Transactional
    @CacheEvict(value = "autoApprovalRules", allEntries = true)
    public AutoApprovalRuleDto createRule(AutoApprovalRuleDto ruleDto) {
        log.info("자동 승인 규칙 생성 시작: {}", ruleDto.getRuleName());
        
        AutoApprovalRule rule = AutoApprovalRule.builder()
                .ruleName(ruleDto.getRuleName())
                .isActive(ruleDto.getIsActive())
                .planIds(convertListToJson(ruleDto.getPlanIds()))
                .verifiedTenantsOnly(ruleDto.getVerifiedTenantsOnly())
                .paymentMethods(convertListToJson(ruleDto.getPaymentMethods()))
                .maxAmount(ruleDto.getMaxAmount())
                .priority(ruleDto.getPriority())
                .build();
        
        AutoApprovalRule savedRule = autoApprovalRuleRepository.save(rule);
        log.info("자동 승인 규칙 생성 완료: ID={}", savedRule.getId());
        
        return convertToDto(savedRule);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "autoApprovalRules", allEntries = true)
    public AutoApprovalRuleDto updateRule(Long ruleId, AutoApprovalRuleDto ruleDto) {
        log.info("자동 승인 규칙 수정 시작: ID={}", ruleId);
        
        AutoApprovalRule rule = autoApprovalRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("자동 승인 규칙을 찾을 수 없습니다: " + ruleId));
        
        rule.updateRule(
                ruleDto.getRuleName(),
                convertListToJson(ruleDto.getPlanIds()),
                ruleDto.getVerifiedTenantsOnly(),
                convertListToJson(ruleDto.getPaymentMethods()),
                ruleDto.getMaxAmount()
        );
        
        if (ruleDto.getPriority() != null) {
            rule.updatePriority(ruleDto.getPriority());
        }
        
        log.info("자동 승인 규칙 수정 완료: ID={}", ruleId);
        return convertToDto(rule);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "autoApprovalRules", allEntries = true)
    public void deleteRule(Long ruleId) {
        log.info("자동 승인 규칙 삭제 시작: ID={}", ruleId);
        
        if (!autoApprovalRuleRepository.existsById(ruleId)) {
            throw new IllegalArgumentException("자동 승인 규칙을 찾을 수 없습니다: " + ruleId);
        }
        
        autoApprovalRuleRepository.deleteById(ruleId);
        log.info("자동 승인 규칙 삭제 완료: ID={}", ruleId);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "autoApprovalRules", allEntries = true)
    public AutoApprovalRuleDto toggleRuleStatus(Long ruleId, boolean isActive) {
        log.info("자동 승인 규칙 상태 변경 시작: ID={}, 활성화={}", ruleId, isActive);
        
        AutoApprovalRule rule = autoApprovalRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("자동 승인 규칙을 찾을 수 없습니다: " + ruleId));
        
        rule.updateActiveStatus(isActive);
        log.info("자동 승인 규칙 상태 변경 완료: ID={}, 활성화={}", ruleId, isActive);
        
        return convertToDto(rule);
    }
    
    @Override
    public Page<AutoApprovalRuleDto> getAllRules(Pageable pageable) {
        // 우선순위 순으로 정렬
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "priority")
                        .and(Sort.by(Sort.Direction.ASC, "id"))
        );
        
        Page<AutoApprovalRule> rules = autoApprovalRuleRepository.findAll(sortedPageable);
        return rules.map(this::convertToDto);
    }
    
    @Override
    @Cacheable(value = "autoApprovalRules", key = "'active'")
    public List<AutoApprovalRuleDto> getActiveRules() {
        log.debug("활성 자동 승인 규칙 조회 (캐시 적용)");
        List<AutoApprovalRule> activeRules = autoApprovalRuleRepository
                .findByIsActiveTrueOrderByPriorityDescIdAsc();
        
        return activeRules.stream()
                .map(this::convertToDto)
                .toList();
    }
    
    @Override
    public boolean evaluateAutoApproval(CreateSubscriptionRequest request) {
        // 자동 승인 시스템이 비활성화된 경우
        if (!isAutoApprovalEnabled()) {
            log.info("자동 승인 시스템이 비활성화되어 있습니다");
            return false;
        }
        
        List<AutoApprovalRule> activeRules = autoApprovalRuleRepository
                .findByIsActiveTrueOrderByPriorityDescIdAsc();
        
        if (activeRules.isEmpty()) {
            log.info("활성화된 자동 승인 규칙이 없습니다");
            return false;
        }
        
        // 현재 테넌트 정보 조회
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Optional<Tenant> tenantOpt = tenantRepository.findById(currentTenantId);
        
        if (tenantOpt.isEmpty()) {
            log.warn("현재 테넌트 정보를 찾을 수 없습니다: {}", currentTenantId);
            return false;
        }
        
        Tenant tenant = tenantOpt.get();
        
        // 요금제 정보 조회
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(request.getPlanId());
        if (planOpt.isEmpty()) {
            log.warn("요금제 정보를 찾을 수 없습니다: {}", request.getPlanId());
            return false;
        }
        
        SubscriptionPlan plan = planOpt.get();
        
        // 우선순위 순으로 규칙 평가
        for (AutoApprovalRule rule : activeRules) {
            if (evaluateRule(rule, request, tenant, plan)) {
                log.info("자동 승인 규칙 적용됨: 규칙={}, 요청={}", rule.getRuleName(), request.getPlanId());
                return true;
            }
        }
        
        log.info("적용 가능한 자동 승인 규칙이 없습니다: 요청={}", request.getPlanId());
        return false;
    }
    
    @Override
    public AutoApprovalRuleDto getAppliedRule(CreateSubscriptionRequest request) {
        if (!isAutoApprovalEnabled()) {
            return null;
        }
        
        List<AutoApprovalRule> activeRules = autoApprovalRuleRepository
                .findByIsActiveTrueOrderByPriorityDescIdAsc();
        
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Optional<Tenant> tenantOpt = tenantRepository.findById(currentTenantId);
        
        if (tenantOpt.isEmpty()) {
            return null;
        }
        
        Tenant tenant = tenantOpt.get();
        
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(request.getPlanId());
        if (planOpt.isEmpty()) {
            return null;
        }
        
        SubscriptionPlan plan = planOpt.get();
        
        for (AutoApprovalRule rule : activeRules) {
            if (evaluateRule(rule, request, tenant, plan)) {
                return convertToDto(rule);
            }
        }
        
        return null;
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "autoApprovalRules", allEntries = true)
    public void toggleAutoApprovalSystem(boolean enabled) {
        log.info("자동 승인 시스템 상태 변경: {}", enabled ? "활성화" : "비활성화");
        autoApprovalSystemEnabled = enabled;
    }
    
    @Override
    public boolean isAutoApprovalEnabled() {
        return autoApprovalSystemEnabled;
    }
    
    /**
     * 개별 규칙 평가
     */
    private boolean evaluateRule(AutoApprovalRule rule, CreateSubscriptionRequest request, 
                                Tenant tenant, SubscriptionPlan plan) {
        
        // 요금제 조건 확인
        if (rule.getPlanIds() != null && !rule.getPlanIds().isEmpty()) {
            List<String> allowedPlanIds = convertJsonToStringList(rule.getPlanIds());
            if (!allowedPlanIds.contains(plan.getPlanId())) {
                return false;
            }
        }
        
        // 검증된 테넌트만 허용 조건 확인
        if (rule.getVerifiedTenantsOnly() != null && rule.getVerifiedTenantsOnly()) {
            // 테넌트 검증 상태 확인 (현재는 모든 테넌트를 검증된 것으로 간주)
            // 실제 구현에서는 tenant.isVerified() 같은 필드를 확인해야 함
        }
        
        // 결제 방법 조건 확인
        if (rule.getPaymentMethods() != null && !rule.getPaymentMethods().isEmpty()) {
            List<String> allowedPaymentMethods = convertJsonToStringList(rule.getPaymentMethods());
            // 현재 요청에서 결제 방법 정보가 없으므로 일단 통과
            // 실제 구현에서는 request.getPaymentMethod() 같은 필드를 확인해야 함
        }
        
        // 최대 금액 조건 확인
        if (rule.getMaxAmount() != null) {
            BigDecimal planAmount = plan.getMonthlyPrice();
            if (planAmount.compareTo(rule.getMaxAmount()) > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 엔티티를 DTO로 변환
     */
    private AutoApprovalRuleDto convertToDto(AutoApprovalRule rule) {
        return AutoApprovalRuleDto.builder()
                .id(rule.getId())
                .ruleName(rule.getRuleName())
                .isActive(rule.getIsActive())
                .planIds(convertJsonToStringList(rule.getPlanIds()))
                .verifiedTenantsOnly(rule.getVerifiedTenantsOnly())
                .paymentMethods(convertJsonToStringList(rule.getPaymentMethods()))
                .maxAmount(rule.getMaxAmount())
                .priority(rule.getPriority())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
    
    /**
     * List를 JSON 문자열로 변환
     */
    private String convertListToJson(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("List를 JSON으로 변환 실패", e);
            return null;
        }
    }
    
    /**
     * JSON 문자열을 Long List로 변환
     */
    private List<Long> convertJsonToLongList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON을 Long List로 변환 실패: {}", json, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * JSON 문자열을 String List로 변환
     */
    private List<String> convertJsonToStringList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON을 String List로 변환 실패: {}", json, e);
            return new ArrayList<>();
        }
    }
}