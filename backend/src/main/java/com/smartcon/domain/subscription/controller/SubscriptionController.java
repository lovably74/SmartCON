package com.smartcon.domain.subscription.controller;

import com.smartcon.domain.subscription.dto.*;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.service.SubscriptionService;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 구독 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * 활성 요금제 목록 조회
     */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDto>>> getActivePlans() {
        log.info("활성 요금제 목록 조회 요청");
        
        List<SubscriptionPlanDto> plans = subscriptionService.getActivePlans();
        
        return ResponseEntity.ok(ApiResponse.success(plans, "요금제 목록을 조회했습니다"));
    }
    
    /**
     * 현재 구독 정보 조회
     */
    @GetMapping("/current")
    @PreAuthorize("hasRole('HQ') or hasRole('SUPER')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getCurrentSubscription() {
        log.info("현재 구독 정보 조회 요청");
        
        SubscriptionDto subscription = subscriptionService.getCurrentSubscription();
        
        if (subscription == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "활성 구독이 없습니다"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(subscription, "현재 구독 정보를 조회했습니다"));
    }
    
    /**
     * 구독 신청
     */
    @PostMapping
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("구독 신청 요청 - 요금제: {}", request.getPlanId());
        
        SubscriptionDto subscription = subscriptionService.createSubscription(request);
        
        return ResponseEntity.ok(ApiResponse.success(subscription, "구독 신청이 완료되었습니다"));
    }
    
    /**
     * 구독 이력 조회
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('HQ') or hasRole('SUPER')")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getSubscriptionHistory() {
        log.info("구독 이력 조회 요청");
        
        List<SubscriptionDto> history = subscriptionService.getSubscriptionHistory();
        
        return ResponseEntity.ok(ApiResponse.success(history, "구독 이력을 조회했습니다"));
    }
    
    /**
     * 구독 상태 변경
     */
    @PutMapping("/{subscriptionId}/status")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> updateSubscriptionStatus(
            @PathVariable Long subscriptionId,
            @RequestParam SubscriptionStatus status) {
        log.info("구독 상태 변경 요청 - ID: {}, 상태: {}", subscriptionId, status);
        
        SubscriptionDto subscription = subscriptionService.updateSubscriptionStatus(subscriptionId, status);
        
        return ResponseEntity.ok(ApiResponse.success(subscription, "구독 상태가 변경되었습니다"));
    }
    
    /**
     * 구독 해지
     */
    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> cancelSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam String reason) {
        log.info("구독 해지 요청 - ID: {}, 사유: {}", subscriptionId, reason);
        
        SubscriptionDto subscription = subscriptionService.cancelSubscription(subscriptionId, reason);
        
        return ResponseEntity.ok(ApiResponse.success(subscription, "구독이 해지되었습니다"));
    }
    
    /**
     * 요금제 변경
     */
    @PutMapping("/{subscriptionId}/plan")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> changePlan(
            @PathVariable Long subscriptionId,
            @RequestParam String planId) {
        log.info("요금제 변경 요청 - 구독 ID: {}, 새 요금제: {}", subscriptionId, planId);
        
        SubscriptionDto subscription = subscriptionService.changePlan(subscriptionId, planId);
        
        return ResponseEntity.ok(ApiResponse.success(subscription, "요금제가 변경되었습니다"));
    }
    
    /**
     * 결제 내역 조회
     */
    @GetMapping("/payments")
    @PreAuthorize("hasRole('HQ') or hasRole('SUPER')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getPaymentHistory(Pageable pageable) {
        log.info("결제 내역 조회 요청");
        
        Page<PaymentDto> payments = subscriptionService.getPaymentHistory(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(payments, "결제 내역을 조회했습니다"));
    }
    
    /**
     * 사용량 모니터링 정보 조회
     */
    @GetMapping("/usage")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<SubscriptionUsageDto>> getUsageMonitoring() {
        log.info("사용량 모니터링 정보 조회 요청");
        
        SubscriptionUsageDto usage = subscriptionService.getUsageMonitoring();
        
        return ResponseEntity.ok(ApiResponse.success(usage, "사용량 정보를 조회했습니다"));
    }
}