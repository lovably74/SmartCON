package com.smartcon.domain.tenant.controller;

import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 구독 신청 및 온보딩 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    // private final SubscriptionService subscriptionService;

    @PostMapping("/verify-business")
    public ApiResponse<Boolean> verifyBusiness(@RequestBody String businessNo) {
        // 사업자 번호 유효성 검사 로직 (공공데이터 API 연동 예정)
        return ApiResponse.success(true);
    }

    @PostMapping("/onboarding")
    public ApiResponse<Tenant> onboardTenant(@RequestBody Tenant onboardingData) {
        // 테넌트 생성 및 초기 본사 관리자 설정 로직
        return ApiResponse.success(onboardingData);
    }

    @GetMapping("/my-plan")
    public ApiResponse<Object> getMyPlan(@RequestHeader("X-Tenant-Id") String tenantId) {
        // 현재 구독 중인 플랜 정보 조회
        return ApiResponse.success(null);
    }
}
