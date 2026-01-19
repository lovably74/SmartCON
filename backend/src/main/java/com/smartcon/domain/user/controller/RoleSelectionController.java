package com.smartcon.domain.user.controller;

import com.smartcon.domain.user.dto.*;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.service.RoleSelectionService;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 역할 및 현장 선택 API 컨트롤러
 * 다중 역할 및 현장 선택 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RoleSelectionController {

    private final RoleSelectionService roleSelectionService;

    /**
     * 사용자의 사용 가능한 역할 및 현장 목록 조회
     * 
     * GET /api/v1/users/{userId}/roles-and-sites
     * 
     * @param userId 사용자 ID
     * @return 역할 및 현장 목록
     */
    @GetMapping("/{userId}/roles-and-sites")
    public ResponseEntity<ApiResponse<RoleSelectionResponse>> getAvailableRolesAndSites(
            @PathVariable Long userId) {
        log.info("역할 및 현장 목록 조회 API 호출 - userId: {}", userId);

        try {
            RoleSelectionResponse response = roleSelectionService.getAvailableRolesAndSites(userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("역할 및 현장 목록 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            log.error("역할 및 현장 목록 조회 중 오류 발생 - userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "역할 및 현장 목록 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 특정 역할로 접근 가능한 현장 목록 조회
     * 
     * GET /api/v1/users/{userId}/sites?role={role}
     * 
     * @param userId 사용자 ID
     * @param role 역할
     * @return 현장 목록
     */
    @GetMapping("/{userId}/sites")
    public ResponseEntity<ApiResponse<List<SiteInfo>>> getAvailableSitesForRole(
            @PathVariable Long userId,
            @RequestParam Role role) {
        log.info("역할별 현장 목록 조회 API 호출 - userId: {}, role: {}", userId, role);

        try {
            List<SiteInfo> sites = roleSelectionService.getAvailableSitesForRole(userId, role);
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (IllegalArgumentException e) {
            log.error("역할별 현장 목록 조회 실패 - userId: {}, role: {}, error: {}", 
                    userId, role, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            log.error("역할별 현장 목록 조회 중 오류 발생 - userId: {}, role: {}", userId, role, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "현장 목록 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 역할 및 현장 선택
     * 
     * POST /api/v1/users/{userId}/select-role-and-site
     * 
     * @param userId 사용자 ID
     * @param request 현장 선택 요청
     * @return 현장 선택 응답 (새로운 토큰 포함)
     */
    @PostMapping("/{userId}/select-role-and-site")
    public ResponseEntity<ApiResponse<SiteSelectionResponse>> selectRoleAndSite(
            @PathVariable Long userId,
            @RequestBody SiteSelectionRequest request) {
        log.info("역할 및 현장 선택 API 호출 - userId: {}, role: {}, siteId: {}", 
                userId, request.getSelectedRole(), request.getSelectedSiteId());

        try {
            SiteSelectionResponse response = roleSelectionService.selectRoleAndSite(userId, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("역할 및 현장 선택 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            log.error("역할 및 현장 선택 중 오류 발생 - userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "역할 및 현장 선택 중 오류가 발생했습니다"));
        }
    }

    /**
     * 역할 및 현장 접근 권한 검증
     * 
     * GET /api/v1/users/{userId}/validate-access?role={role}&siteId={siteId}
     * 
     * @param userId 사용자 ID
     * @param role 역할
     * @param siteId 현장 ID
     * @return 접근 권한 여부
     */
    @GetMapping("/{userId}/validate-access")
    public ResponseEntity<ApiResponse<Boolean>> validateRoleAndSiteAccess(
            @PathVariable Long userId,
            @RequestParam Role role,
            @RequestParam Long siteId) {
        log.info("역할 및 현장 접근 권한 검증 API 호출 - userId: {}, role: {}, siteId: {}", 
                userId, role, siteId);

        try {
            boolean hasAccess = roleSelectionService.validateRoleAndSiteAccess(userId, role, siteId);
            return ResponseEntity.ok(ApiResponse.success(hasAccess));
        } catch (IllegalArgumentException e) {
            log.error("역할 및 현장 접근 권한 검증 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            log.error("역할 및 현장 접근 권한 검증 중 오류 발생 - userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "접근 권한 검증 중 오류가 발생했습니다"));
        }
    }
}
