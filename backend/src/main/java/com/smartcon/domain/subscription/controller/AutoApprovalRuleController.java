package com.smartcon.domain.subscription.controller;

import com.smartcon.domain.subscription.dto.AutoApprovalRuleDto;
import com.smartcon.domain.subscription.service.AutoApprovalRuleService;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 자동 승인 규칙 관리 컨트롤러
 * 
 * 슈퍼관리자가 자동 승인 규칙을 생성, 수정, 삭제, 조회할 수 있는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/auto-approval-rules")
@RequiredArgsConstructor
@Slf4j
public class AutoApprovalRuleController {
    
    private final AutoApprovalRuleService autoApprovalRuleService;
    
    /**
     * 자동 승인 규칙 목록 조회
     */
    @GetMapping
    public ApiResponse<Page<AutoApprovalRuleDto>> getAllRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("자동 승인 규칙 목록 조회 요청 - 페이지: {}/{}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priority"));
            Page<AutoApprovalRuleDto> rules = autoApprovalRuleService.getAllRules(pageable);
            return ApiResponse.success(rules);
        } catch (Exception e) {
            log.error("자동 승인 규칙 목록 조회 중 오류 발생", e);
            return ApiResponse.error("자동 승인 규칙 목록을 조회할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 활성화된 자동 승인 규칙 목록 조회
     */
    @GetMapping("/active")
    public ApiResponse<List<AutoApprovalRuleDto>> getActiveRules() {
        log.info("활성화된 자동 승인 규칙 목록 조회 요청");
        
        try {
            List<AutoApprovalRuleDto> activeRules = autoApprovalRuleService.getActiveRules();
            return ApiResponse.success(activeRules);
        } catch (Exception e) {
            log.error("활성화된 자동 승인 규칙 목록 조회 중 오류 발생", e);
            return ApiResponse.error("활성화된 자동 승인 규칙 목록을 조회할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동 승인 규칙 생성
     */
    @PostMapping
    public ApiResponse<AutoApprovalRuleDto> createRule(@RequestBody @Valid AutoApprovalRuleDto ruleDto) {
        log.info("자동 승인 규칙 생성 요청 - 규칙명: {}", ruleDto.getRuleName());
        
        try {
            AutoApprovalRuleDto createdRule = autoApprovalRuleService.createRule(ruleDto);
            return ApiResponse.success(createdRule);
        } catch (Exception e) {
            log.error("자동 승인 규칙 생성 중 오류 발생", e);
            return ApiResponse.error("자동 승인 규칙을 생성할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동 승인 규칙 수정
     */
    @PutMapping("/{ruleId}")
    public ApiResponse<AutoApprovalRuleDto> updateRule(
            @PathVariable Long ruleId,
            @RequestBody @Valid AutoApprovalRuleDto ruleDto) {
        
        log.info("자동 승인 규칙 수정 요청 - ID: {}, 규칙명: {}", ruleId, ruleDto.getRuleName());
        
        try {
            AutoApprovalRuleDto updatedRule = autoApprovalRuleService.updateRule(ruleId, ruleDto);
            return ApiResponse.success(updatedRule);
        } catch (IllegalArgumentException e) {
            log.warn("자동 승인 규칙 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("자동 승인 규칙 수정 중 오류 발생", e);
            return ApiResponse.error("자동 승인 규칙을 수정할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동 승인 규칙 삭제
     */
    @DeleteMapping("/{ruleId}")
    public ApiResponse<Void> deleteRule(@PathVariable Long ruleId) {
        log.info("자동 승인 규칙 삭제 요청 - ID: {}", ruleId);
        
        try {
            autoApprovalRuleService.deleteRule(ruleId);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            log.warn("자동 승인 규칙 삭제 실패 - 잘못된 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("자동 승인 규칙 삭제 중 오류 발생", e);
            return ApiResponse.error("자동 승인 규칙을 삭제할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동 승인 규칙 활성화/비활성화
     */
    @PatchMapping("/{ruleId}/status")
    public ApiResponse<AutoApprovalRuleDto> toggleRuleStatus(
            @PathVariable Long ruleId,
            @RequestParam boolean isActive) {
        
        log.info("자동 승인 규칙 상태 변경 요청 - ID: {}, 활성화: {}", ruleId, isActive);
        
        try {
            AutoApprovalRuleDto updatedRule = autoApprovalRuleService.toggleRuleStatus(ruleId, isActive);
            return ApiResponse.success(updatedRule);
        } catch (IllegalArgumentException e) {
            log.warn("자동 승인 규칙 상태 변경 실패 - 잘못된 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("자동 승인 규칙 상태 변경 중 오류 발생", e);
            return ApiResponse.error("자동 승인 규칙 상태를 변경할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동 승인 시스템 전체 활성화/비활성화
     */
    @PatchMapping("/system/toggle")
    public ApiResponse<Void> toggleAutoApprovalSystem(@RequestParam boolean enabled) {
        log.info("자동 승인 시스템 상태 변경 요청 - 활성화: {}", enabled);
        
        try {
            autoApprovalRuleService.toggleAutoApprovalSystem(enabled);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("자동 승인 시스템 상태 변경 중 오류 발생", e);
            return ApiResponse.error("자동 승인 시스템 상태를 변경할 수 없습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동 승인 시스템 활성화 상태 조회
     */
    @GetMapping("/system/status")
    public ApiResponse<Boolean> getAutoApprovalSystemStatus() {
        log.info("자동 승인 시스템 상태 조회 요청");
        
        try {
            boolean enabled = autoApprovalRuleService.isAutoApprovalEnabled();
            return ApiResponse.success(enabled);
        } catch (Exception e) {
            log.error("자동 승인 시스템 상태 조회 중 오류 발생", e);
            return ApiResponse.error("자동 승인 시스템 상태를 조회할 수 없습니다: " + e.getMessage());
        }
    }
}
