package com.smartcon.domain.admin.controller;

import com.smartcon.domain.admin.dto.ApprovalStatsDto;
import com.smartcon.domain.admin.dto.BillingStatsDto;
import com.smartcon.domain.admin.dto.DashboardStatsDto;
import com.smartcon.domain.admin.dto.SubscriptionExportDto;
import com.smartcon.domain.admin.dto.TenantSummaryDto;
import com.smartcon.domain.admin.service.SuperAdminService;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 슈퍼 관리자 전용 제어 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    /**
     * 대시보드 통계 정보 조회
     */
    @GetMapping("/dashboard/stats")
    public ApiResponse<DashboardStatsDto> getDashboardStats() {
        log.info("대시보드 통계 정보 조회 요청");
        try {
            DashboardStatsDto stats = superAdminService.getDashboardStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("대시보드 통계 정보 조회 중 오류 발생", e);
            return ApiResponse.error("대시보드 통계 정보를 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 테넌트 목록 조회 (페이징, 검색, 필터링)
     */
    @GetMapping("/tenants")
    public ApiResponse<Page<TenantSummaryDto>> getAllTenants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Tenant.SubscriptionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("테넌트 목록 조회 요청 - 검색어: {}, 상태: {}, 페이지: {}/{}", search, status, page, size);
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TenantSummaryDto> tenants = superAdminService.getTenants(search, status, pageable);
            return ApiResponse.success(tenants);
        } catch (Exception e) {
            log.error("테넌트 목록 조회 중 오류 발생", e);
            return ApiResponse.error("테넌트 목록을 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 테넌트 상태 변경
     */
    @PatchMapping("/tenants/{id}/status")
    public ApiResponse<Void> updateTenantStatus(
            @PathVariable Long id,
            @RequestParam Tenant.SubscriptionStatus status) {
        
        log.info("테넌트 상태 변경 요청 - ID: {}, 새 상태: {}", id, status);
        
        try {
            superAdminService.updateTenantStatus(id, status);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            log.warn("테넌트 상태 변경 실패 - 잘못된 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("테넌트 상태 변경 중 오류 발생", e);
            return ApiResponse.error("테넌트 상태를 변경할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 결제 통계 및 매출 현황 조회
     */
    @GetMapping("/billing/stats")
    public ApiResponse<BillingStatsDto> getBillingStats() {
        log.info("결제 통계 정보 조회 요청");
        
        try {
            BillingStatsDto stats = superAdminService.getBillingStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("결제 통계 정보 조회 중 오류 발생", e);
            return ApiResponse.error("결제 통계 정보를 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 최근 생성된 테넌트 목록 조회
     */
    @GetMapping("/tenants/recent")
    public ApiResponse<List<TenantSummaryDto>> getRecentTenants() {
        log.info("최근 생성된 테넌트 목록 조회 요청");
        
        try {
            List<TenantSummaryDto> recentTenants = superAdminService.getRecentTenants();
            return ApiResponse.success(recentTenants);
        } catch (Exception e) {
            log.error("최근 테넌트 목록 조회 중 오류 발생", e);
            return ApiResponse.error("최근 테넌트 목록을 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 시스템 상태 확인
     */
    @GetMapping("/system/health")
    public ApiResponse<String> getSystemHealth() {
        log.info("시스템 상태 확인 요청");
        
        try {
            // 간단한 시스템 상태 체크 (실제 구현 시 더 상세한 체크 필요)
            return ApiResponse.success("HEALTHY");
        } catch (Exception e) {
            log.error("시스템 상태 확인 중 오류 발생", e);
            return ApiResponse.error("시스템 상태를 확인할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 승인 대시보드 통계 정보 조회
     */
    @GetMapping("/approval/stats")
    public ApiResponse<ApprovalStatsDto> getApprovalStats() {
        log.info("승인 대시보드 통계 정보 조회 요청");
        
        try {
            ApprovalStatsDto stats = superAdminService.getApprovalStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("승인 대시보드 통계 정보 조회 중 오류 발생", e);
            return ApiResponse.error("승인 통계 정보를 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 구독 필터링 및 검색 API 확장
     */
    @GetMapping("/subscriptions")
    public ApiResponse<Page<SubscriptionExportDto>> getSubscriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("구독 목록 조회 요청 - 검색어: {}, 상태: {}, 기간: {} ~ {}, 페이지: {}/{}", 
                search, status, startDate, endDate, page, size);
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 실제 구현에서는 검색어와 날짜 필터링을 포함한 복잡한 쿼리가 필요
            // 현재는 기본 데이터 내보내기 메서드를 활용
            List<SubscriptionExportDto> allData = superAdminService.exportSubscriptionData(status, startDate, endDate);
            
            // 검색어 필터링 (테넌트명 기준)
            if (search != null && !search.trim().isEmpty()) {
                allData = allData.stream()
                        .filter(dto -> dto.getTenantName().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            }
            
            // 페이지네이션 처리 (간단한 구현)
            int start = page * size;
            int end = Math.min(start + size, allData.size());
            List<SubscriptionExportDto> pageData = allData.subList(start, end);
            
            // Page 객체 생성을 위한 간단한 구현
            org.springframework.data.domain.PageImpl<SubscriptionExportDto> pageResult = 
                    new org.springframework.data.domain.PageImpl<>(pageData, pageable, allData.size());
            
            return ApiResponse.success(pageResult);
        } catch (Exception e) {
            log.error("구독 목록 조회 중 오류 발생", e);
            return ApiResponse.error("구독 목록을 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 구독 데이터 내보내기 API
     */
    @GetMapping("/subscriptions/export")
    public ResponseEntity<List<SubscriptionExportDto>> exportSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("구독 데이터 내보내기 요청 - 상태: {}, 기간: {} ~ {}", status, startDate, endDate);
        
        try {
            List<SubscriptionExportDto> exportData = superAdminService.exportSubscriptionData(status, startDate, endDate);
            
            // CSV 다운로드를 위한 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subscriptions_export.json");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);
        } catch (Exception e) {
            log.error("구독 데이터 내보내기 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
