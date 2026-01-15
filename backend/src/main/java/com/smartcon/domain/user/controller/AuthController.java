package com.smartcon.domain.user.controller;

import com.smartcon.domain.user.dto.*;
import com.smartcon.domain.user.service.AuthService;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 * JWT 토큰 기반 로그인, 로그아웃, 토큰 갱신 처리
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 통합 로그인 (개인사용자/관리자 구분)
     */
    @PostMapping("/unified/login")
    public ResponseEntity<ApiResponse<LoginResponse>> unifiedLogin(@Valid @RequestBody UnifiedLoginRequest request) {
        log.info("통합 로그인 API 호출 - 로그인 유형: {}", request.getLoginType());

        try {
            LoginResponse response = authService.authenticateUnified(request);
            return ResponseEntity.ok(ApiResponse.success(response, "로그인이 성공했습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("통합 로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("LOGIN_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("통합 로그인 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "로그인 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 휴대폰 인증 및 CI값 생성
     */
    @PostMapping("/phone/verify")
    public ResponseEntity<ApiResponse<CiValueResponse>> verifyPhone(@Valid @RequestBody PhoneVerificationRequest request) {
        log.info("휴대폰 인증 API 호출 - 휴대폰: {}", request.getPhoneNumber());

        try {
            CiValueResponse response = authService.generateCiValue(request);
            return ResponseEntity.ok(ApiResponse.success(response, "CI값이 생성되었습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("휴대폰 인증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VERIFICATION_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("휴대폰 인증 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "휴대폰 인증 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 사용자 역할 목록 조회
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<UserRolesResponse>> getUserRoles(@RequestHeader("Authorization") String authHeader) {
        log.info("사용자 역할 조회 API 호출");

        try {
            // TODO: JWT 토큰에서 사용자 ID 추출
            // 현재는 임시로 하드코딩
            Long userId = 1L;
            
            UserRolesResponse response = authService.getUserRoles(userId);
            return ResponseEntity.ok(ApiResponse.success(response, "역할 목록을 조회했습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("역할 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ROLE_QUERY_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("역할 조회 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "역할 조회 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 역할 선택 및 토큰 재발급
     */
    @PostMapping("/roles/select")
    public ResponseEntity<ApiResponse<LoginResponse>> selectRole(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RoleSelectionRequest request) {
        log.info("역할 선택 API 호출 - 선택 역할: {}", request.getRole());

        try {
            // TODO: JWT 토큰에서 사용자 ID 추출
            // 현재는 임시로 하드코딩
            Long userId = 1L;
            
            LoginResponse response = authService.selectRole(userId, request);
            return ResponseEntity.ok(ApiResponse.success(response, "역할이 선택되었습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("역할 선택 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ROLE_SELECTION_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("역할 선택 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "역할 선택 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 사용자 로그인 (기존 방식 - 하위 호환성 유지)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 API 호출 - 이메일: {}", request.getEmail());

        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "로그인이 성공했습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("LOGIN_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "로그인 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 갱신 API 호출");

        try {
            LoginResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success(response, "토큰이 갱신되었습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("TOKEN_REFRESH_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 갱신 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "토큰 갱신 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("로그아웃 API 호출");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                authService.logout(accessToken);
            }
            return ResponseEntity.ok(ApiResponse.success(null, "로그아웃이 완료되었습니다"));
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "로그아웃 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 토큰 검증
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.debug("토큰 검증 API 호출");

        try {
            // Authorization 헤더가 없는 경우
            if (authHeader == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("MISSING_TOKEN", "Authorization 헤더가 누락되었습니다"));
            }

            // Authorization 헤더가 빈 문자열인 경우
            if (authHeader.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("MISSING_TOKEN", "Authorization 헤더가 비어있습니다"));
            }

            // Bearer 접두사가 없는 경우
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_TOKEN_FORMAT", "Bearer 토큰 형식이 아닙니다"));
            }

            String token = authHeader.substring(7).trim();
            
            // 토큰이 빈 문자열이거나 공백만 있는 경우
            if (token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("EMPTY_TOKEN", "토큰이 비어있습니다"));
            }

            boolean isValid = authService.validateToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success(true, "유효한 토큰입니다"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_TOKEN", "유효하지 않은 토큰입니다"));
            }
        } catch (Exception e) {
            log.error("토큰 검증 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "토큰 검증 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 개발용 테스트 토큰 생성
     */
    @PostMapping("/dev-token")
    public ResponseEntity<ApiResponse<LoginResponse>> generateDevToken(
            @RequestParam(defaultValue = "ROLE_SUPER") String role,
            @RequestParam(defaultValue = "dev-tenant") String tenantId) {
        
        log.info("개발용 토큰 생성 - 역할: {}, 테넌트: {}", role, tenantId);

        try {
            // 실제 JWT 토큰 생성을 위한 개발용 사용자 정보 생성
            LoginRequest devRequest = LoginRequest.builder()
                    .email("dev@smartcon.com")
                    .password("dev-password")
                    .build();
            
            // AuthService를 통해 실제 JWT 토큰 생성
            LoginResponse response = authService.generateDevToken(role, tenantId);

            return ResponseEntity.ok(ApiResponse.success(response, "개발용 토큰이 생성되었습니다"));
        } catch (Exception e) {
            log.error("개발용 토큰 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "토큰 생성 중 오류가 발생했습니다"));
        }
    }
}