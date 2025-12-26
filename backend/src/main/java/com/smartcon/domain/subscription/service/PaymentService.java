package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.PaymentDto;
import com.smartcon.domain.subscription.dto.PaymentMethodDto;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 결제 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    
    /**
     * 결제 수단 목록 조회
     */
    public List<PaymentMethodDto> getPaymentMethods() {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 결제 수단 목록 조회", tenantId);
        
        Tenant tenant = getTenant(tenantId);
        
        return paymentMethodRepository.findActiveByTenant(tenant)
                .stream()
                .map(PaymentMethodDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 기본 결제 수단 조회
     */
    public PaymentMethodDto getDefaultPaymentMethod() {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 기본 결제 수단 조회", tenantId);
        
        Tenant tenant = getTenant(tenantId);
        
        return paymentMethodRepository.findDefaultByTenant(tenant)
                .map(PaymentMethodDto::from)
                .orElse(null);
    }
    
    /**
     * 결제 수단 등록
     */
    @Transactional
    public PaymentMethodDto registerPaymentMethod(PaymentType type, String billingKey, 
                                                 String cardNumberMasked, String cardHolderName, 
                                                 String expiryDate, String bankName, 
                                                 String accountNumberMasked, String accountHolderName) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 결제 수단 등록 - 타입: {}", tenantId, type);
        
        Tenant tenant = getTenant(tenantId);
        
        // 첫 번째 결제 수단인 경우 기본으로 설정
        boolean isFirstPaymentMethod = paymentMethodRepository.countActiveByTenant(tenant) == 0;
        
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .tenant(tenant)
                .type(type)
                .cardNumberMasked(cardNumberMasked)
                .cardHolderName(cardHolderName)
                .expiryDate(expiryDate)
                .bankName(bankName)
                .accountNumberMasked(accountNumberMasked)
                .accountHolderName(accountHolderName)
                .billingKey(billingKey)
                .isDefault(isFirstPaymentMethod)
                .isActive(true)
                .build();
        
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        log.info("결제 수단 등록 완료 - ID: {}", paymentMethod.getId());
        return PaymentMethodDto.from(paymentMethod);
    }
    
    /**
     * 기본 결제 수단 변경
     */
    @Transactional
    public void setDefaultPaymentMethod(Long paymentMethodId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 기본 결제 수단 변경 - ID: {}", tenantId, paymentMethodId);
        
        Tenant tenant = getTenant(tenantId);
        
        // 기존 기본 결제 수단 해제
        paymentMethodRepository.findDefaultByTenant(tenant)
                .ifPresent(PaymentMethod::unsetDefault);
        
        // 새로운 기본 결제 수단 설정
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 수단입니다"));
        
        if (!paymentMethod.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("권한이 없는 결제 수단입니다");
        }
        
        paymentMethod.setAsDefault();
    }
    
    /**
     * 결제 수단 삭제
     */
    @Transactional
    public void deletePaymentMethod(Long paymentMethodId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("테넌트 {}의 결제 수단 삭제 - ID: {}", tenantId, paymentMethodId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 수단입니다"));
        
        if (!paymentMethod.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("권한이 없는 결제 수단입니다");
        }
        
        paymentMethod.deactivate();
    }
    
    /**
     * 구독 결제 생성
     */
    @Transactional
    public PaymentDto createSubscriptionPayment(Long subscriptionId, Long paymentMethodId, 
                                               LocalDate billingPeriodStart, LocalDate billingPeriodEnd) {
        log.info("구독 {} 결제 생성", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 수단입니다"));
        
        String orderId = generateOrderId();
        BigDecimal amount = subscription.getMonthlyPrice();
        BigDecimal taxAmount = amount.multiply(BigDecimal.valueOf(0.1)); // 10% 부가세
        
        Payment payment = Payment.builder()
                .tenant(subscription.getTenant())
                .subscription(subscription)
                .paymentMethod(paymentMethod)
                .orderId(orderId)
                .amount(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(taxAmount)
                .status(PaymentStatus.PENDING)
                .billingPeriodStart(billingPeriodStart)
                .billingPeriodEnd(billingPeriodEnd)
                .invoiceNumber(generateInvoiceNumber())
                .build();
        
        payment = paymentRepository.save(payment);
        
        log.info("결제 생성 완료 - ID: {}, 주문번호: {}", payment.getId(), orderId);
        return PaymentDto.from(payment);
    }
    
    /**
     * 결제 성공 처리
     */
    @Transactional
    public PaymentDto processPaymentSuccess(String paymentKey, String pgResponse) {
        log.info("결제 성공 처리 - 결제키: {}", paymentKey);
        
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다"));
        
        payment.markAsSuccess(LocalDateTime.now(), pgResponse);
        
        // 구독의 다음 결제일 업데이트
        Subscription subscription = payment.getSubscription();
        LocalDate nextBillingDate = payment.getBillingPeriodEnd().plusDays(1);
        subscription.updateNextBillingDate(nextBillingDate);
        
        // 결제 수단 마지막 사용 시간 업데이트
        if (payment.getPaymentMethod() != null) {
            payment.getPaymentMethod().updateLastUsedAt();
        }
        
        log.info("결제 성공 처리 완료 - ID: {}", payment.getId());
        return PaymentDto.from(payment);
    }
    
    /**
     * 결제 실패 처리
     */
    @Transactional
    public PaymentDto processPaymentFailure(String paymentKey, String failedReason, String pgResponse) {
        log.info("결제 실패 처리 - 결제키: {}, 사유: {}", paymentKey, failedReason);
        
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다"));
        
        payment.markAsFailed(failedReason, pgResponse);
        
        log.info("결제 실패 처리 완료 - ID: {}", payment.getId());
        return PaymentDto.from(payment);
    }
    
    /**
     * 결제 실패 건 재시도
     */
    @Transactional
    public PaymentDto retryFailedPayment(Long paymentId) {
        log.info("결제 재시도 - ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다"));
        
        if (!payment.isFailed()) {
            throw new IllegalStateException("실패한 결제만 재시도할 수 있습니다");
        }
        
        // 새로운 주문번호 생성
        String newOrderId = generateOrderId();
        
        // TODO: PG사 결제 API 호출 로직 구현
        // 현재는 상태만 PENDING으로 변경
        payment.updatePgResponse("재시도 요청");
        
        log.info("결제 재시도 완료 - ID: {}, 새 주문번호: {}", paymentId, newOrderId);
        return PaymentDto.from(payment);
    }
    
    /**
     * 주문번호 생성
     */
    private String generateOrderId() {
        return "ORD_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 송장번호 생성
     */
    private String generateInvoiceNumber() {
        return "INV_" + LocalDate.now().toString().replace("-", "") + "_" + 
               UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    /**
     * 테넌트 조회
     */
    private Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테넌트입니다"));
    }
}