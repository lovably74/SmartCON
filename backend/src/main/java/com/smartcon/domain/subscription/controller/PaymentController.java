package com.smartcon.domain.subscription.controller;

import com.smartcon.domain.subscription.dto.PaymentDto;
import com.smartcon.domain.subscription.dto.PaymentMethodDto;
import com.smartcon.domain.subscription.entity.PaymentType;
import com.smartcon.domain.subscription.service.PaymentService;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 결제 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 결제 수단 목록 조회
     */
    @GetMapping("/methods")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<List<PaymentMethodDto>>> getPaymentMethods() {
        log.info("결제 수단 목록 조회 요청");
        
        List<PaymentMethodDto> paymentMethods = paymentService.getPaymentMethods();
        
        return ResponseEntity.ok(ApiResponse.success(paymentMethods, "결제 수단 목록을 조회했습니다"));
    }
    
    /**
     * 기본 결제 수단 조회
     */
    @GetMapping("/methods/default")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<PaymentMethodDto>> getDefaultPaymentMethod() {
        log.info("기본 결제 수단 조회 요청");
        
        PaymentMethodDto paymentMethod = paymentService.getDefaultPaymentMethod();
        
        return ResponseEntity.ok(ApiResponse.success(paymentMethod, "기본 결제 수단을 조회했습니다"));
    }
    
    /**
     * 결제 수단 등록 (신용카드)
     */
    @PostMapping("/methods/card")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<PaymentMethodDto>> registerCardPaymentMethod(
            @RequestParam String billingKey,
            @RequestParam String cardNumberMasked,
            @RequestParam String cardHolderName,
            @RequestParam String expiryDate) {
        log.info("신용카드 결제 수단 등록 요청");
        
        PaymentMethodDto paymentMethod = paymentService.registerPaymentMethod(
                PaymentType.CARD, billingKey, cardNumberMasked, cardHolderName, 
                expiryDate, null, null, null);
        
        return ResponseEntity.ok(ApiResponse.success(paymentMethod, "신용카드 결제 수단이 등록되었습니다"));
    }
    
    /**
     * 결제 수단 등록 (계좌이체)
     */
    @PostMapping("/methods/account")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<PaymentMethodDto>> registerAccountPaymentMethod(
            @RequestParam String billingKey,
            @RequestParam String bankName,
            @RequestParam String accountNumberMasked,
            @RequestParam String accountHolderName) {
        log.info("계좌이체 결제 수단 등록 요청");
        
        PaymentMethodDto paymentMethod = paymentService.registerPaymentMethod(
                PaymentType.BANK_TRANSFER, billingKey, null, null, 
                null, bankName, accountNumberMasked, accountHolderName);
        
        return ResponseEntity.ok(ApiResponse.success(paymentMethod, "계좌이체 결제 수단이 등록되었습니다"));
    }
    
    /**
     * 기본 결제 수단 변경
     */
    @PutMapping("/methods/{paymentMethodId}/default")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            @PathVariable Long paymentMethodId) {
        log.info("기본 결제 수단 변경 요청 - ID: {}", paymentMethodId);
        
        paymentService.setDefaultPaymentMethod(paymentMethodId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "기본 결제 수단이 변경되었습니다"));
    }
    
    /**
     * 결제 수단 삭제
     */
    @DeleteMapping("/methods/{paymentMethodId}")
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @PathVariable Long paymentMethodId) {
        log.info("결제 수단 삭제 요청 - ID: {}", paymentMethodId);
        
        paymentService.deletePaymentMethod(paymentMethodId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "결제 수단이 삭제되었습니다"));
    }
    
    /**
     * 구독 결제 생성
     */
    @PostMapping("/subscription")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<ApiResponse<PaymentDto>> createSubscriptionPayment(
            @RequestParam Long subscriptionId,
            @RequestParam Long paymentMethodId,
            @RequestParam LocalDate billingPeriodStart,
            @RequestParam LocalDate billingPeriodEnd) {
        log.info("구독 결제 생성 요청 - 구독 ID: {}", subscriptionId);
        
        PaymentDto payment = paymentService.createSubscriptionPayment(
                subscriptionId, paymentMethodId, billingPeriodStart, billingPeriodEnd);
        
        return ResponseEntity.ok(ApiResponse.success(payment, "구독 결제가 생성되었습니다"));
    }
    
    /**
     * 결제 성공 처리 (PG사 콜백)
     */
    @PostMapping("/success")
    public ResponseEntity<ApiResponse<PaymentDto>> processPaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String pgResponse) {
        log.info("결제 성공 처리 요청 - 결제키: {}", paymentKey);
        
        PaymentDto payment = paymentService.processPaymentSuccess(paymentKey, pgResponse);
        
        return ResponseEntity.ok(ApiResponse.success(payment, "결제가 성공적으로 처리되었습니다"));
    }
    
    /**
     * 결제 실패 처리 (PG사 콜백)
     */
    @PostMapping("/failure")
    public ResponseEntity<ApiResponse<PaymentDto>> processPaymentFailure(
            @RequestParam String paymentKey,
            @RequestParam String failedReason,
            @RequestParam String pgResponse) {
        log.info("결제 실패 처리 요청 - 결제키: {}", paymentKey);
        
        PaymentDto payment = paymentService.processPaymentFailure(paymentKey, failedReason, pgResponse);
        
        return ResponseEntity.ok(ApiResponse.success(payment, "결제 실패가 처리되었습니다"));
    }
    
    /**
     * 결제 재시도
     */
    @PostMapping("/{paymentId}/retry")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<ApiResponse<PaymentDto>> retryFailedPayment(
            @PathVariable Long paymentId) {
        log.info("결제 재시도 요청 - ID: {}", paymentId);
        
        PaymentDto payment = paymentService.retryFailedPayment(paymentId);
        
        return ResponseEntity.ok(ApiResponse.success(payment, "결제 재시도가 요청되었습니다"));
    }
}