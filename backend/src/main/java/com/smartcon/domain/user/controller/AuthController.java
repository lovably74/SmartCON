package com.smartcon.domain.user.controller;

import com.smartcon.domain.user.dto.LoginRequest;
import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.RefreshTokenRequest;
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
     * 사용자 로그인
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
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("토큰 검증 API 호출");

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_TOKEN", "유효하지 않은 토큰 형식입니다"));
            }

            String token = authHeader.substring(7);
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