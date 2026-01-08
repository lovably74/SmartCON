package com.smartcon.domain.subscription.controller;

import com.smartcon.domain.subscription.dto.SubscriptionApprovalDto;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.subscription.service.SubscriptionApprovalService;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 구독 승인 관리 컨트롤러
 * 
 * 슈퍼관리자가 구독 신청을 승인, 거부, 중지, 종료할 수 있는 API를 제공합니다.
 * 모든 엔드포인트는 슈퍼관리자 권한이 필요합니다.
 */
// JWT 인증 시스템 테스트를 위해 임시로 비활성화
// @RestController
@RequestMapping("/api/v1/admin/subscription-approvals")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SubscriptionApprovalController {
    
    private final SubscriptionApprovalService subscriptionApprovalService;
    
    /**
     * 승인 대기 중인 구독 목록 조회
     * 
     * @param pageable 페이지 정보
     * @return 승인 대기 구독 목록
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<SubscriptionDto>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("승인 대기 구독 목록 조회 요청 - 페이지: {}, 크기: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<SubscriptionDto> pendingApprovals = subscriptionApprovalService.getPendingApprovals(pageable);
        
        log.info("승인 대기 구독 {} 건 조회 완료", pendingApprovals.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(pendingApprovals));
    }
    
    /**
     * 구독 승인
     * 
     * @param subscriptionId 구독 ID
     * @param request 승인 요청
     * @return 승인된 구독 정보
     */
    @PostMapping("/{subscriptionId}/approve")
    public ResponseEntity<ApiResponse<SubscriptionDto>> approveSubscription(
            @PathVariable @NotNull Long subscriptionId,
            @RequestBody @Valid ApprovalRequest request) {
        
        log.info("구독 승인 요청 - 구독 ID: {}, 사유: {}", subscriptionId, request.getReason());
        
        SubscriptionDto approvedSubscription = subscriptionApprovalService.approveSubscription(
                subscriptionId, request.getReason());
        
        log.info("구독 승인 완료 - 구독 ID: {}", subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.success(approvedSubscription));
    }
    
    /**
     * 구독 거부
     * 
     * @param subscriptionId 구독 ID
     * @param request 거부 요청
     * @return 거부된 구독 정보
     */
    @PostMapping("/{subscriptionId}/reject")
    public ResponseEntity<ApiResponse<SubscriptionDto>> rejectSubscription(
            @PathVariable @NotNull Long subscriptionId,
            @RequestBody @Valid ApprovalRequest request) {
        
        log.info("구독 거부 요청 - 구독 ID: {}, 사유: {}", subscriptionId, request.getReason());
        
        SubscriptionDto rejectedSubscription = subscriptionApprovalService.rejectSubscription(
                subscriptionId, request.getReason());
        
        log.info("구독 거부 완료 - 구독 ID: {}", subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.success(rejectedSubscription));
    }
    
    /**
     * 구독 중지
     * 
     * @param subscriptionId 구독 ID
     * @param request 중지 요청
     * @return 중지된 구독 정보
     */
    @PostMapping("/{subscriptionId}/suspend")
    public ResponseEntity<ApiResponse<SubscriptionDto>> suspendSubscription(
            @PathVariable @NotNull Long subscriptionId,
            @RequestBody @Valid ApprovalRequest request) {
        
        log.info("구독 중지 요청 - 구독 ID: {}, 사유: {}", subscriptionId, request.getReason());
        
        SubscriptionDto suspendedSubscription = subscriptionApprovalService.suspendSubscription(
                subscriptionId, request.getReason());
        
        log.info("구독 중지 완료 - 구독 ID: {}", subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.success(suspendedSubscription));
    }
    
    /**
     * 구독 종료
     * 
     * @param subscriptionId 구독 ID
     * @param request 종료 요청
     * @return 종료된 구독 정보
     */
    @PostMapping("/{subscriptionId}/terminate")
    public ResponseEntity<ApiResponse<SubscriptionDto>> terminateSubscription(
            @PathVariable @NotNull Long subscriptionId,
            @RequestBody @Valid ApprovalRequest request) {
        
        log.info("구독 종료 요청 - 구독 ID: {}, 사유: {}", subscriptionId, request.getReason());
        
        SubscriptionDto terminatedSubscription = subscriptionApprovalService.terminateSubscription(
                subscriptionId, request.getReason());
        
        log.info("구독 종료 완료 - 구독 ID: {}", subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.success(terminatedSubscription));
    }
    
    /**
     * 구독 재활성화
     * 
     * @param subscriptionId 구독 ID
     * @param request 재활성화 요청
     * @return 재활성화된 구독 정보
     */
    @PostMapping("/{subscriptionId}/reactivate")
    public ResponseEntity<ApiResponse<SubscriptionDto>> reactivateSubscription(
            @PathVariable @NotNull Long subscriptionId,
            @RequestBody @Valid ApprovalRequest request) {
        
        log.info("구독 재활성화 요청 - 구독 ID: {}, 사유: {}", subscriptionId, request.getReason());
        
        SubscriptionDto reactivatedSubscription = subscriptionApprovalService.reactivateSubscription(
                subscriptionId, request.getReason());
        
        log.info("구독 재활성화 완료 - 구독 ID: {}", subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.success(reactivatedSubscription));
    }
    
    /**
     * 구독 승인 이력 조회
     * 
     * @param subscriptionId 구독 ID
     * @return 승인 이력 목록
     */
    @GetMapping("/{subscriptionId}/history")
    public ResponseEntity<ApiResponse<List<SubscriptionApprovalDto>>> getApprovalHistory(
            @PathVariable @NotNull Long subscriptionId) {
        
        log.info("구독 승인 이력 조회 요청 - 구독 ID: {}", subscriptionId);
        
        List<SubscriptionApprovalDto> approvalHistory = subscriptionApprovalService.getApprovalHistory(subscriptionId);
        
        log.info("구독 승인 이력 {} 건 조회 완료", approvalHistory.size());
        
        return ResponseEntity.ok(ApiResponse.success(approvalHistory));
    }
    
    /**
     * 승인/거부/중지/종료/재활성화 요청 DTO
     */
    public static class ApprovalRequest {
        
        @NotBlank(message = "사유는 필수입니다")
        @Size(min = 10, max = 500, message = "사유는 10자 이상 500자 이하로 입력해주세요")
        private String reason;
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}