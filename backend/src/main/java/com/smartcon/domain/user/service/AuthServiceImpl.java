package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.LoginRequest;
import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.RefreshTokenRequest;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 인증 서비스 구현체
 * JWT 토큰 기반 인증 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도 - 이메일: {}, 테넌트: {}", request.getEmail(), request.getTenantId());

        // 테넌트 컨텍스트 설정
        if (request.getTenantId() != null) {
            TenantContext.setCurrentTenant(request.getTenantId());
        }

        try {
            // 사용자 조회
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("로그인 실패 - 존재하지 않는 사용자: {}", request.getEmail());
                throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
            }

            User user = userOptional.get();

            // 계정 잠금 확인
            if (user.isLocked()) {
                log.warn("로그인 실패 - 계정 잠금: {}", request.getEmail());
                throw new IllegalArgumentException("계정이 잠겨있습니다. 관리자에게 문의하세요");
            }

            // 계정 활성화 확인
            if (!user.isActive()) {
                log.warn("로그인 실패 - 비활성 계정: {}", request.getEmail());
                throw new IllegalArgumentException("비활성화된 계정입니다");
            }

            // 비밀번호 검증 (개발 단계에서는 간단한 검증)
            if (!validatePassword(request.getPassword(), user.getPasswordHash())) {
                user.incrementLoginFailureCount();
                userRepository.save(user);
                log.warn("로그인 실패 - 비밀번호 불일치: {} (실패 횟수: {})", 
                        request.getEmail(), user.getLoginFailureCount());
                throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
            }

            // 로그인 성공 - 실패 횟수 초기화
            user.resetLoginFailureCount();
            userRepository.save(user);

            // 권한 정보 생성
            Map<String, Object> permissions = generateUserPermissions(user.getRole());

            // JWT 토큰 생성
            String accessToken = jwtTokenService.generateAccessToken(
                    user.getId().toString(),
                    user.getTenantId() != null ? user.getTenantId().toString() : null,
                    user.getRole().name(),
                    permissions
            );

            String refreshToken = jwtTokenService.generateRefreshToken(
                    user.getId().toString(),
                    user.getTenantId() != null ? user.getTenantId().toString() : null
            );

            log.info("로그인 성공 - 사용자: {}, 역할: {}", user.getEmail(), user.getRole());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1시간
                    .user(LoginResponse.UserInfo.builder()
                            .id(user.getId().toString())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole().name())
                            .tenantId(user.getTenantId() != null ? user.getTenantId().toString() : null)
                            .permissions(permissions)
                            .profileImageUrl(user.getProfileImageUrl())
                            .build())
                    .build();

        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("토큰 갱신 요청");

        try {
            // Refresh Token 검증
            if (!jwtTokenService.validateToken(request.getRefreshToken())) {
                log.warn("토큰 갱신 실패 - 유효하지 않은 Refresh Token");
                throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다");
            }

            if (!jwtTokenService.isRefreshToken(request.getRefreshToken())) {
                log.warn("토큰 갱신 실패 - Access Token으로 갱신 시도");
                throw new IllegalArgumentException("Refresh Token이 아닙니다");
            }

            // 토큰에서 사용자 정보 추출
            String userId = jwtTokenService.extractUserId(request.getRefreshToken());
            String tenantId = jwtTokenService.extractTenantId(request.getRefreshToken());

            // 테넌트 컨텍스트 설정
            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
            }

            // 사용자 조회
            Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
            if (userOptional.isEmpty()) {
                log.warn("토큰 갱신 실패 - 존재하지 않는 사용자: {}", userId);
                throw new IllegalArgumentException("존재하지 않는 사용자입니다");
            }

            User user = userOptional.get();

            // 계정 상태 확인
            if (!user.isActive()) {
                log.warn("토큰 갱신 실패 - 비활성 계정: {}", user.getEmail());
                throw new IllegalArgumentException("비활성화된 계정입니다");
            }

            // 권한 정보 생성
            Map<String, Object> permissions = generateUserPermissions(user.getRole());

            // 새로운 Access Token 생성
            String newAccessToken = jwtTokenService.generateAccessToken(
                    userId,
                    user.getTenantId() != null ? user.getTenantId().toString() : null,
                    user.getRole().name(),
                    permissions
            );

            log.info("토큰 갱신 성공 - 사용자: {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken()) // 기존 Refresh Token 재사용
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1시간
                    .user(LoginResponse.UserInfo.builder()
                            .id(userId)
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole().name())
                            .tenantId(user.getTenantId() != null ? user.getTenantId().toString() : null)
                            .permissions(permissions)
                            .profileImageUrl(user.getProfileImageUrl())
                            .build())
                    .build();

        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public void logout(String accessToken) {
        log.info("로그아웃 요청");
        
        // 실제 운영에서는 토큰 블랙리스트에 추가하거나 Redis에서 토큰 무효화
        // 현재는 로그만 남김
        try {
            if (jwtTokenService.validateToken(accessToken)) {
                String userId = jwtTokenService.extractUserId(accessToken);
                log.info("로그아웃 성공 - 사용자 ID: {}", userId);
            }
        } catch (Exception e) {
            log.warn("로그아웃 처리 중 오류: {}", e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenService.validateToken(token);
    }

    @Override
    public LoginResponse generateDevToken(String role, String tenantId) {
        log.info("개발용 토큰 생성 - 역할: {}, 테넌트: {}", role, tenantId);

        try {
            // 개발용 사용자 정보 생성
            String userId = "dev-user-1";
            
            // 역할에 따른 권한 정보 생성
            User.Role userRole;
            try {
                userRole = User.Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                userRole = User.Role.ROLE_SUPER; // 기본값
            }
            
            Map<String, Object> permissions = generateUserPermissions(userRole);

            // JWT 토큰 생성
            String accessToken = jwtTokenService.generateAccessToken(
                    userId,
                    tenantId,
                    role,
                    permissions
            );

            String refreshToken = jwtTokenService.generateRefreshToken(
                    userId,
                    tenantId
            );

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1시간
                    .user(LoginResponse.UserInfo.builder()
                            .id(userId)
                            .name("개발용 사용자")
                            .email("dev@smartcon.com")
                            .role(role)
                            .tenantId(tenantId)
                            .permissions(permissions)
                            .profileImageUrl(null)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("개발용 토큰 생성 실패", e);
            throw new RuntimeException("개발용 토큰 생성에 실패했습니다", e);
        }
    }

    /**
     * 비밀번호 검증 (개발용 간단한 구현)
     */
    private boolean validatePassword(String rawPassword, String encodedPassword) {
        // 개발 단계에서는 간단한 비밀번호 검증
        // 실제 운영에서는 BCrypt 등을 사용해야 함
        
        if (encodedPassword == null) {
            // 비밀번호가 설정되지 않은 경우 (소셜 로그인 전용 계정)
            return false;
        }

        // 개발용 간단한 검증 (실제로는 passwordEncoder.matches 사용)
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            // BCrypt 해시인 경우
            try {
                return passwordEncoder.matches(rawPassword, encodedPassword);
            } catch (Exception e) {
                log.warn("BCrypt 비밀번호 검증 실패: {}", e.getMessage());
                return false;
            }
        } else {
            // 개발용 평문 비교 (보안상 위험하므로 운영에서는 사용 금지)
            return rawPassword.equals(encodedPassword);
        }
    }

    /**
     * 사용자 역할에 따른 권한 정보 생성
     */
    private Map<String, Object> generateUserPermissions(User.Role role) {
        Map<String, Object> permissions = new HashMap<>();

        switch (role) {
            case ROLE_SUPER:
                permissions.put("admin.read", true);
                permissions.put("admin.write", true);
                permissions.put("subscription.approve", true);
                permissions.put("subscription.reject", true);
                permissions.put("tenant.manage", true);
                permissions.put("user.manage", true);
                permissions.put("system.monitor", true);
                break;

            case ROLE_HQ:
                permissions.put("tenant.read", true);
                permissions.put("tenant.write", true);
                permissions.put("user.read", true);
                permissions.put("user.write", true);
                permissions.put("attendance.read", true);
                permissions.put("contract.read", true);
                permissions.put("contract.write", true);
                break;

            case ROLE_SITE:
                permissions.put("site.read", true);
                permissions.put("site.write", true);
                permissions.put("attendance.read", true);
                permissions.put("attendance.write", true);
                permissions.put("worker.read", true);
                permissions.put("worker.write", true);
                permissions.put("contract.read", true);
                break;

            case ROLE_TEAM:
                permissions.put("team.read", true);
                permissions.put("team.write", true);
                permissions.put("attendance.read", true);
                permissions.put("worker.read", true);
                break;

            case ROLE_WORKER:
                permissions.put("attendance.read", true);
                permissions.put("contract.read", true);
                permissions.put("profile.read", true);
                permissions.put("profile.write", true);
                break;

            default:
                // 기본 권한 없음
                break;
        }

        return permissions;
    }
}