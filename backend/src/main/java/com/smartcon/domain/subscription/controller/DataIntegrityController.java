package com.smartcon.domain.subscription.controller;

import com.smartcon.domain.subscription.dto.DataIntegrityReportDto;
import com.smartcon.domain.subscription.service.DataIntegrityService;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 데이터 무결성 관리 컨트롤러
 * 
 * 슈퍼관리자가 데이터 무결성 검사 및 복구 기능을 사용할 수 있도록 합니다.
 */
//@RestController  // JWT 테스트를 위해 임시 비활성화
@RequestMapping("/api/v1/admin/data-integrity")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class DataIntegrityController {
    
    private final DataIntegrityService dataIntegrityService;
    
    /**
     * 전체 데이터 무결성 검사 실행
     */
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<DataIntegrityReportDto>> performIntegrityCheck() {
        log.info("데이터 무결성 검사 요청");
        
        try {
            DataIntegrityReportDto report = dataIntegrityService.performFullIntegrityCheck();
            
            return ResponseEntity.ok(ApiResponse.success(
                report,
                "데이터 무결성 검사가 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("데이터 무결성 검사 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("데이터 무결성 검사 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 구독 데이터 무결성 검사
     */
    @GetMapping("/check/subscriptions")
    public ResponseEntity<ApiResponse<List<String>>> checkSubscriptionIntegrity() {
        log.info("구독 데이터 무결성 검사 요청");
        
        try {
            List<String> issues = dataIntegrityService.checkSubscriptionIntegrity();
            
            return ResponseEntity.ok(ApiResponse.success(
                issues,
                "구독 데이터 무결성 검사가 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("구독 데이터 무결성 검사 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("구독 데이터 무결성 검사 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 승인 이력 데이터 무결성 검사
     */
    @GetMapping("/check/approvals")
    public ResponseEntity<ApiResponse<List<String>>> checkApprovalHistoryIntegrity() {
        log.info("승인 이력 데이터 무결성 검사 요청");
        
        try {
            List<String> issues = dataIntegrityService.checkApprovalHistoryIntegrity();
            
            return ResponseEntity.ok(ApiResponse.success(
                issues,
                "승인 이력 데이터 무결성 검사가 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("승인 이력 데이터 무결성 검사 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("승인 이력 데이터 무결성 검사 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 알림 데이터 무결성 검사
     */
    @GetMapping("/check/notifications")
    public ResponseEntity<ApiResponse<List<String>>> checkNotificationIntegrity() {
        log.info("알림 데이터 무결성 검사 요청");
        
        try {
            List<String> issues = dataIntegrityService.checkNotificationIntegrity();
            
            return ResponseEntity.ok(ApiResponse.success(
                issues,
                "알림 데이터 무결성 검사가 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("알림 데이터 무결성 검사 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("알림 데이터 무결성 검사 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 자동 복구 실행
     */
    @PostMapping("/recover/auto")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> performAutoRecovery() {
        log.info("자동 복구 요청");
        
        try {
            int recoveredCount = dataIntegrityService.performAutoRecovery();
            
            Map<String, Integer> result = Map.of("recoveredCount", recoveredCount);
            
            return ResponseEntity.ok(ApiResponse.success(
                result,
                String.format("자동 복구가 완료되었습니다. 복구된 문제: %d개", recoveredCount)
            ));
        } catch (Exception e) {
            log.error("자동 복구 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("자동 복구 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 특정 구독 데이터 복구
     */
    @PostMapping("/recover/subscription/{subscriptionId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> recoverSubscriptionData(
            @PathVariable Long subscriptionId) {
        log.info("구독 데이터 복구 요청 - subscriptionId: {}", subscriptionId);
        
        try {
            boolean recovered = dataIntegrityService.recoverSubscriptionData(subscriptionId);
            
            Map<String, Boolean> result = Map.of("recovered", recovered);
            
            String message = recovered ? 
                    "구독 데이터가 성공적으로 복구되었습니다" : 
                    "복구할 문제가 없거나 구독을 찾을 수 없습니다";
            
            return ResponseEntity.ok(ApiResponse.success(result, message));
        } catch (Exception e) {
            log.error("구독 데이터 복구 중 오류 발생 - subscriptionId: {}", subscriptionId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("구독 데이터 복구 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 고아 데이터 정리
     */
    @PostMapping("/cleanup/orphaned")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cleanupOrphanedData() {
        log.info("고아 데이터 정리 요청");
        
        try {
            int cleanedCount = dataIntegrityService.cleanupOrphanedData();
            
            Map<String, Integer> result = Map.of("cleanedCount", cleanedCount);
            
            return ResponseEntity.ok(ApiResponse.success(
                result,
                String.format("고아 데이터 정리가 완료되었습니다. 정리된 레코드: %d개", cleanedCount)
            ));
        } catch (Exception e) {
            log.error("고아 데이터 정리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("고아 데이터 정리 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 스케줄된 검사 활성화/비활성화
     */
    @PutMapping("/schedule")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> setScheduledCheckEnabled(
            @RequestParam boolean enabled) {
        log.info("스케줄된 검사 설정 변경 요청 - enabled: {}", enabled);
        
        try {
            dataIntegrityService.setScheduledCheckEnabled(enabled);
            
            Map<String, Boolean> result = Map.of("enabled", enabled);
            
            String message = enabled ? 
                    "스케줄된 데이터 무결성 검사가 활성화되었습니다" : 
                    "스케줄된 데이터 무결성 검사가 비활성화되었습니다";
            
            return ResponseEntity.ok(ApiResponse.success(result, message));
        } catch (Exception e) {
            log.error("스케줄된 검사 설정 변경 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("스케줄된 검사 설정 변경 중 오류가 발생했습니다"));
        }
    }
}