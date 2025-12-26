package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.*;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TenantRepository tenantRepository;
    
    // 승인 워크플로우 관련 서비스 추가
    private final AutoApprovalRuleService autoApprovalRuleService;
    private final NotificationService notificationService;
    
    /**
     * 활성 요금제 목록 조회
     */
    public List<SubscriptionPlanDto> getActivePlans() {
        log.info("활성 요금제 목록 조회");
        
        return subscriptionPlanRepository.findActiveOrderBySortOrder()
                .stream()
                .map(SubscriptionPlanDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 현재 테넌트의 활성 구독 조회
     */
    public SubscriptionDto getCurrentSubscription() {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 현재 구독 조회", tenantId);
        
        Tenant tenant = getTenant(tenantId);
        
        return subscriptionRepository.findActiveByTenant(tenant)
                .map(SubscriptionDto::from)
                .orElse(null);
    }
    
    /**
     * 구독 신청 (승인 워크플로우 적용)
     */
    @Transactional
    public SubscriptionDto createSubscription(CreateSubscriptionRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 구독 신청 - 요금제: {}", tenantId, request.getPlanId());
        
        Tenant tenant = getTenant(tenantId);
        
        // 기존 활성 구독 확인
        if (subscriptionRepository.existsActiveByTenant(tenant)) {
            throw new IllegalStateException("이미 활성 구독이 존재합니다");
        }
        
        // 요금제 조회
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndIsActiveTrue(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제입니다"));
        
        // 구독 생성 (PENDING_APPROVAL 상태로 시작)
        LocalDate startDate = LocalDate.now();
        LocalDate trialEndDate = request.getStartTrial() ? 
            startDate.plusDays(request.getTrialDays()) : null;
        
        BigDecimal monthlyPrice = calculatePrice(plan, request.getBillingCycle(), request.getDiscountRate());
        
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.PENDING_APPROVAL)  // 승인 대기 상태로 생성
                .startDate(startDate)
                .nextBillingDate(trialEndDate != null ? trialEndDate : startDate.plusMonths(1))
                .billingCycle(request.getBillingCycle())
                .monthlyPrice(monthlyPrice)
                .discountRate(request.getDiscountRate())
                .autoRenewal(request.getAutoRenewal())
                .trialEndDate(trialEndDate)
                .build();
        
        subscription = subscriptionRepository.save(subscription);
        
        // 자동 승인 규칙 평가
        boolean autoApproved = evaluateAutoApprovalRules(request);
        
        if (autoApproved) {
            // 자동 승인 처리
            subscription.updateStatus(SubscriptionStatus.ACTIVE);
            log.info("구독 {} 자동 승인 완료", subscription.getId());
            
            // 승인 알림 발송
            notificationService.sendApprovalResultNotification(subscription.getId(), true, "자동 승인 규칙에 의해 승인되었습니다.");
        } else {
            // 수동 승인 대기 알림 발송
            notificationService.sendSubscriptionRequestNotification(subscription.getId());
            log.info("구독 {} 수동 승인 대기 상태", subscription.getId());
        }
        
        log.info("구독 생성 완료 - ID: {}, 상태: {}", subscription.getId(), subscription.getStatus());
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 자동 승인 규칙 평가
     */
    private boolean evaluateAutoApprovalRules(CreateSubscriptionRequest request) {
        try {
            return autoApprovalRuleService.evaluateAutoApproval(request);
        } catch (Exception e) {
            log.warn("자동 승인 규칙 평가 중 오류 발생: {}", e.getMessage());
            return false; // 오류 시 수동 승인으로 처리
        }
    }
    
    /**
     * 구독 이력 조회
     */
    public List<SubscriptionDto> getSubscriptionHistory() {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 구독 이력 조회", tenantId);
        
        Tenant tenant = getTenant(tenantId);
        
        return subscriptionRepository.findByTenantOrderByCreatedAtDesc(tenant)
                .stream()
                .map(SubscriptionDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 구독 상태 변경
     */
    @Transactional
    public SubscriptionDto updateSubscriptionStatus(Long subscriptionId, SubscriptionStatus status) {
        log.info("구독 {} 상태 변경: {}", subscriptionId, status);
        
        Subscription subscription = getSubscription(subscriptionId);
        subscription.updateStatus(status);
        
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 구독 해지
     */
    @Transactional
    public SubscriptionDto cancelSubscription(Long subscriptionId, String reason) {
        log.info("구독 {} 해지 - 사유: {}", subscriptionId, reason);
        
        Subscription subscription = getSubscription(subscriptionId);
        subscription.cancel(LocalDate.now(), reason);
        
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 요금제 변경
     */
    @Transactional
    public SubscriptionDto changePlan(Long subscriptionId, String newPlanId) {
        log.info("구독 {} 요금제 변경: {}", subscriptionId, newPlanId);
        
        Subscription subscription = getSubscription(subscriptionId);
        SubscriptionPlan newPlan = subscriptionPlanRepository.findByIdAndIsActiveTrue(newPlanId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제입니다"));
        
        BigDecimal newPrice = calculatePrice(newPlan, subscription.getBillingCycle(), subscription.getDiscountRate());
        subscription.changePlan(newPlan, newPrice);
        
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 결제 내역 조회
     */
    public Page<PaymentDto> getPaymentHistory(Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 결제 내역 조회", tenantId);
        
        Tenant tenant = getTenant(tenantId);
        
        return paymentRepository.findByTenantOrderByCreatedAtDesc(tenant, pageable)
                .map(PaymentDto::from);
    }
    
    /**
     * 구독 사용량 모니터링 정보 조회
     */
    public SubscriptionUsageDto getUsageMonitoring() {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 사용량 모니터링 정보 조회", tenantId);
        
        SubscriptionDto subscription = getCurrentSubscription();
        if (subscription == null) {
            throw new IllegalStateException("활성 구독이 없습니다");
        }
        
        // TODO: 실제 사용량 데이터 조회 로직 구현
        // 현재는 더미 데이터 반환
        return SubscriptionUsageDto.builder()
                .subscription(subscription)
                .currentSites(5)
                .currentUsers(25)
                .currentStorageGb(10)
                .usagePercentage(calculateUsagePercentage(subscription, 5, 25, 10))
                .build();
    }
    
    /**
     * 가격 계산
     */
    private BigDecimal calculatePrice(SubscriptionPlan plan, BillingCycle billingCycle, BigDecimal discountRate) {
        BigDecimal basePrice = BillingCycle.YEARLY.equals(billingCycle) && plan.getYearlyPrice() != null ?
                plan.getYearlyPrice().divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP) : plan.getMonthlyPrice();
        
        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = basePrice.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            return basePrice.subtract(discount);
        }
        
        return basePrice;
    }
    
    /**
     * 사용량 비율 계산
     */
    private int calculateUsagePercentage(SubscriptionDto subscription, int currentSites, int currentUsers, int currentStorageGb) {
        int siteUsage = (currentSites * 100) / subscription.getPlan().getMaxSites();
        int userUsage = (currentUsers * 100) / subscription.getPlan().getMaxUsers();
        int storageUsage = subscription.getPlan().getMaxStorageGb() != null ?
                (currentStorageGb * 100) / subscription.getPlan().getMaxStorageGb() : 0;
        
        return Math.max(Math.max(siteUsage, userUsage), storageUsage);
    }
    
    /**
     * 테넌트 조회
     */
    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테넌트입니다"));
    }
    
    /**
     * 구독 조회
     */
    private Subscription getSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
    }
}