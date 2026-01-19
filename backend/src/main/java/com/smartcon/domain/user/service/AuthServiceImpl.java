package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.*;
import com.smartcon.domain.user.entity.CiValue;
import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.security.JwtTokenBlacklistService;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 인증 서비스 구현체
 * JWT 토큰 기반 인증 처리 및 다중 역할 지원
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final JwtTokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;
    private final BusinessNumberValidator businessNumberValidator;
    private final AuthenticationAuditService auditService;

    @Override
    public LoginResponse authenticateUnified(UnifiedLoginRequest request) {
        log.info("통합 로그인 시도 - 로그인 유형: {}", request.getLoginType());

        if (request.isBusinessLogin()) {
            request.validateBusinessLogin();
            if (!businessNumberValidator.validate(request.getBusinessNumber())) {
                throw new IllegalArgumentException("유효하지 않은 사업자번호입니다");
            }
            return authenticateBusiness(request);
        } else if (request.isSocialLogin()) {
            request.validateSocialLogin();
            return authenticateSocial(request);
        } else {
            throw new IllegalArgumentException("지원하지 않는 로그인 유형입니다");
        }
    }

    private LoginResponse authenticateBusiness(UnifiedLoginRequest request) {
        log.info("사업자 로그인 처리 - 사업자번호: {}", request.getBusinessNumber());
        String normalizedBusinessNumber = businessNumberValidator.normalize(request.getBusinessNumber());
        Optional<User> userOptional = userRepository.findByBusinessNumber(normalizedBusinessNumber);
        
        // IP 주소 및 User-Agent 추출 (실제 구현에서는 HttpServletRequest에서 가져옴)
        String ipAddress = extractIpAddress();
        String userAgent = extractUserAgent();
        
        if (userOptional.isEmpty()) {
            log.warn("사업자 로그인 실패 - 존재하지 않는 사업자번호: {}", normalizedBusinessNumber);
            
            // 사용자 없이 실패 로깅
            auditService.logFailedAuthenticationWithoutUser(
                    normalizedBusinessNumber, 
                    LoginType.BUSINESS, 
                    "존재하지 않는 사업자번호", 
                    ipAddress, 
                    userAgent, 
                    null);
            
            throw new IllegalArgumentException("사업자번호 또는 비밀번호가 올바르지 않습니다");
        }

        User user = userOptional.get();
        
        // 계정 잠금 확인
        if (user.isLocked()) {
            log.warn("사업자 로그인 실패 - 계정 잠금: {}", normalizedBusinessNumber);
            
            auditService.logFailedAuthentication(
                    user, 
                    normalizedBusinessNumber, 
                    LoginType.BUSINESS, 
                    "계정 잠금", 
                    ipAddress, 
                    userAgent, 
                    user.getTenantId());
            
            throw new IllegalArgumentException("계정이 잠겨있습니다. 30분 후 다시 시도하세요");
        }
        
        validateUserAccount(user);

        if (!validatePassword(request.getPassword(), user.getPasswordHash())) {
            user.incrementLoginFailureCount();
            userRepository.save(user);
            
            log.warn("사업자 로그인 실패 - 비밀번호 불일치 {} (실패 횟수: {})", 
                    normalizedBusinessNumber, user.getLoginFailureCount());
            
            // 실패 로깅
            auditService.logFailedAuthentication(
                    user, 
                    normalizedBusinessNumber, 
                    LoginType.BUSINESS, 
                    "비밀번호 불일치", 
                    ipAddress, 
                    userAgent, 
                    user.getTenantId());
            
            throw new IllegalArgumentException("사업자번호 또는 비밀번호가 올바르지 않습니다");
        }

        if (!user.canUseLoginType(LoginType.BUSINESS)) {
            log.warn("사업자 로그인 실패 - 권한 없음: {}", normalizedBusinessNumber);
            
            auditService.logFailedAuthentication(
                    user, 
                    normalizedBusinessNumber, 
                    LoginType.BUSINESS, 
                    "사업자 로그인 권한 없음", 
                    ipAddress, 
                    userAgent, 
                    user.getTenantId());
            
            throw new IllegalArgumentException("사업자 로그인 권한이 없습니다");
        }

        user.resetLoginFailureCount();
        userRepository.save(user);
        
        // 성공 로깅
        auditService.logSuccessfulAuthentication(
                user, 
                normalizedBusinessNumber, 
                LoginType.BUSINESS, 
                ipAddress, 
                userAgent, 
                user.getTenantId());
        
        return generateLoginResponse(user);
    }

    private LoginResponse authenticateSocial(UnifiedLoginRequest request) {
        log.info("소셜 로그인 처리 - 제공자: {}", request.getProvider());
        String tempCiValue = "CI_" + request.getPhoneNumber();
        Optional<User> userOptional = userRepository.findByCiValueValue(tempCiValue);
        
        // IP 주소 및 User-Agent 추출
        String ipAddress = extractIpAddress();
        String userAgent = extractUserAgent();
        
        User user;
        if (userOptional.isEmpty()) {
            log.info("신규 소셜 로그인 사용자 생성 - CI값: {}", tempCiValue);
            CiValue ciValue = CiValue.builder()
                    .value(tempCiValue)
                    .phoneNumber(request.getPhoneNumber())
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            user = User.builder()
                    .ciValue(ciValue)
                    .phoneNumber(request.getPhoneNumber())
                    .loginType(LoginType.SOCIAL)
                    .authProvider(convertToAuthProvider(request.getProvider()))
                    .isActive(true)
                    .isPhoneVerified(true)
                    .build();
            user.addRole(Role.ROLE_WORKER);
            user = userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        validateUserAccount(user);
        
        if (!user.canUseLoginType(LoginType.SOCIAL)) {
            log.warn("소셜 로그인 실패 - 권한 없음: {}", tempCiValue);
            
            auditService.logFailedAuthentication(
                    user, 
                    request.getPhoneNumber(), 
                    LoginType.SOCIAL, 
                    "소셜 로그인 권한 없음", 
                    ipAddress, 
                    userAgent, 
                    user.getTenantId());
            
            throw new IllegalArgumentException("소셜 로그인 권한이 없습니다");
        }
        
        // 성공 로깅
        auditService.logSuccessfulAuthentication(
                user, 
                request.getPhoneNumber(), 
                LoginType.SOCIAL, 
                ipAddress, 
                userAgent, 
                user.getTenantId());
        
        return generateLoginResponse(user);
    }

    @Override
    public CiValueResponse generateCiValue(PhoneVerificationRequest request) {
        log.info("CI값 생성 요청 - 전화번호: {}", request.getPhoneNumber());
        String ciValue = "CI_" + request.getPhoneNumber() + "_" + System.currentTimeMillis();
        Optional<User> existingUser = userRepository.findByCiValueValue(ciValue);
        boolean isNewUser = existingUser.isEmpty();
        log.info("CI값 생성 완료 - CI값: {}, 신규 사용자: {}", ciValue, isNewUser);

        return CiValueResponse.builder()
                .ciValue(ciValue)
                .generatedAt(LocalDateTime.now())
                .isNewUser(isNewUser)
                .message(isNewUser ? "신규 사용자입니다" : "기존 사용자입니다")
                .build();
    }

    @Override
    public UserRolesResponse getUserRoles(Long userId) {
        log.info("사용자 역할 조회 - 사용자 ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        List<UserRolesResponse.RoleInfo> roleInfos = user.getRoles().stream()
                .map(role -> UserRolesResponse.RoleInfo.builder()
                        .role(role)
                        .displayName(role.getDisplayName())
                        .sites(new ArrayList<>())
                        .build())
                .collect(Collectors.toList());

        return UserRolesResponse.builder()
                .roles(roleInfos)
                .requiresSelection(user.getRoles().size() > 1)
                .build();
    }

    @Override
    public LoginResponse selectRole(Long userId, RoleSelectionRequest request) {
        log.info("역할 선택 - 사용자 ID: {}, 선택 역할: {}", userId, request.getRole());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        if (!user.getRoles().contains(request.getRole())) {
            throw new IllegalArgumentException("해당 역할을 가지고 있지 않습니다");
        }
        return generateLoginResponse(user, request.getRole());
    }

    @Override
    public boolean validateLoginTypeForRole(Role role, LoginType loginType) {
        return loginType.isAllowedForRole(role);
    }

    private void validateUserAccount(User user) {
        if (user.isLocked()) {
            throw new IllegalArgumentException("계정이 잠겨있습니다. 관리자에게 문의하세요");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다");
        }
    }

    private LoginResponse generateLoginResponse(User user) {
        Role primaryRole = getPrimaryRole(user.getRoles());
        return generateLoginResponse(user, primaryRole);
    }

    private LoginResponse generateLoginResponse(User user, Role role) {
        Map<String, Object> permissions = generateUserPermissions(user.getRoles());
        String accessToken = jwtTokenService.generateAccessToken(
                user.getId().toString(),
                user.getTenantId() != null ? user.getTenantId().toString() : null,
                role.name(),
                permissions
        );
        String refreshToken = jwtTokenService.generateRefreshToken(
                user.getId().toString(),
                user.getTenantId() != null ? user.getTenantId().toString() : null
        );
        log.info("로그인 성공 - 사용자: {}, 역할: {}", user.getEmail(), role);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(role.name())
                        .tenantId(user.getTenantId() != null ? user.getTenantId().toString() : null)
                        .permissions(permissions)
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .build();
    }

    private com.smartcon.domain.user.entity.AuthProvider convertToAuthProvider(
            com.smartcon.domain.user.entity.SocialAccount.SocialProvider socialProvider) {
        return switch (socialProvider) {
            case KAKAO -> com.smartcon.domain.user.entity.AuthProvider.KAKAO;
            case NAVER -> com.smartcon.domain.user.entity.AuthProvider.NAVER;
        };
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도 - 이메일: {}, 테넌트: {}", request.getEmail(), request.getTenantId());
        if (request.getTenantId() != null) {
            TenantContext.setCurrentTenant(request.getTenantId());
        }

        // IP 주소 및 User-Agent 추출
        String ipAddress = extractIpAddress();
        String userAgent = extractUserAgent();

        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("로그인 실패 - 존재하지 않는 사용자: {}", request.getEmail());
                
                // 사용자 없이 실패 로깅
                auditService.logFailedAuthenticationWithoutUser(
                        request.getEmail(), 
                        LoginType.BUSINESS, 
                        "존재하지 않는 사용자", 
                        ipAddress, 
                        userAgent, 
                        request.getTenantId() != null ? Long.parseLong(request.getTenantId()) : null);
                
                throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
            }

            User user = userOptional.get();
            if (user.isLocked()) {
                log.warn("로그인 실패 - 계정 잠금: {}", request.getEmail());
                
                auditService.logFailedAuthentication(
                        user, 
                        request.getEmail(), 
                        LoginType.BUSINESS, 
                        "계정 잠금", 
                        ipAddress, 
                        userAgent, 
                        user.getTenantId());
                
                throw new IllegalArgumentException("계정이 잠겨있습니다. 30분 후 다시 시도하세요");
            }
            if (!user.isActive()) {
                log.warn("로그인 실패 - 비활성 계정: {}", request.getEmail());
                
                auditService.logFailedAuthentication(
                        user, 
                        request.getEmail(), 
                        LoginType.BUSINESS, 
                        "비활성 계정", 
                        ipAddress, 
                        userAgent, 
                        user.getTenantId());
                
                throw new IllegalArgumentException("비활성화된 계정입니다");
            }
            if (user.isBusinessUser() && !user.isEmailVerified()) {
                log.warn("로그인 실패 - 이메일 미인증: {}", request.getEmail());
                
                auditService.logFailedAuthentication(
                        user, 
                        request.getEmail(), 
                        LoginType.BUSINESS, 
                        "이메일 미인증", 
                        ipAddress, 
                        userAgent, 
                        user.getTenantId());
                
                throw new IllegalArgumentException("이메일 인증이 필요합니다");
            }

            if (!validatePassword(request.getPassword(), user.getPasswordHash())) {
                user.incrementLoginFailureCount();
                userRepository.save(user);
                log.warn("로그인 실패 - 비밀번호 불일치 {} (실패 횟수: {})", 
                        request.getEmail(), user.getLoginFailureCount());
                
                auditService.logFailedAuthentication(
                        user, 
                        request.getEmail(), 
                        LoginType.BUSINESS, 
                        "비밀번호 불일치", 
                        ipAddress, 
                        userAgent, 
                        user.getTenantId());
                
                throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
            }

            user.resetLoginFailureCount();
            userRepository.save(user);
            Role primaryRole = getPrimaryRole(user.getRoles());
            Map<String, Object> permissions = generateUserPermissions(user.getRoles());
            String accessToken = jwtTokenService.generateAccessToken(
                    user.getId().toString(),
                    user.getTenantId() != null ? user.getTenantId().toString() : null,
                    primaryRole.name(),
                    permissions
            );
            String refreshToken = jwtTokenService.generateRefreshToken(
                    user.getId().toString(),
                    user.getTenantId() != null ? user.getTenantId().toString() : null
            );
            log.info("로그인 성공 - 사용자: {}, 주역할: {}, 전체 역할: {}", 
                    user.getEmail(), primaryRole, user.getRoles());

            // 성공 로깅
            auditService.logSuccessfulAuthentication(
                    user, 
                    request.getEmail(), 
                    LoginType.BUSINESS, 
                    ipAddress, 
                    userAgent, 
                    user.getTenantId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .user(LoginResponse.UserInfo.builder()
                            .id(user.getId().toString())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(primaryRole.name())
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
            if (!jwtTokenService.validateToken(request.getRefreshToken())) {
                log.warn("토큰 갱신 실패 - 유효하지 않은 Refresh Token");
                throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다");
            }
            if (!jwtTokenService.isRefreshToken(request.getRefreshToken())) {
                log.warn("토큰 갱신 실패 - Access Token으로 갱신 시도");
                throw new IllegalArgumentException("Refresh Token이 아닙니다");
            }

            String userId = jwtTokenService.extractUserId(request.getRefreshToken());
            String tenantId = jwtTokenService.extractTenantId(request.getRefreshToken());
            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
            }

            Optional<User> userOptional;
            if (userId.startsWith("dev-user")) {
                User devUser = User.builder()
                    .name("개발용 사용자")
                    .email("dev@smartcon.com")
                    .isActive(true)
                    .build();
                devUser.addRole(Role.ROLE_WORKER);
                devUser.setId(1L);
                devUser.setTenantId(tenantId != null ? Long.parseLong(tenantId) : 1L);
                userOptional = Optional.of(devUser);
            } else {
                userOptional = userRepository.findById(Long.parseLong(userId));
            }
            
            if (userOptional.isEmpty()) {
                log.warn("토큰 갱신 실패 - 존재하지 않는 사용자: {}", userId);
                throw new IllegalArgumentException("존재하지 않는 사용자입니다");
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                log.warn("토큰 갱신 실패 - 비활성 계정: {}", user.getEmail());
                throw new IllegalArgumentException("비활성화된 계정입니다");
            }

            Role primaryRole = getPrimaryRole(user.getRoles());
            Map<String, Object> permissions = generateUserPermissions(user.getRoles());
            String newAccessToken = jwtTokenService.generateAccessToken(
                    userId,
                    user.getTenantId() != null ? user.getTenantId().toString() : null,
                    primaryRole.name(),
                    permissions
            );
            log.info("토큰 갱신 성공 - 사용자: {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .user(LoginResponse.UserInfo.builder()
                            .id(userId)
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(primaryRole.name())
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
        try {
            if (jwtTokenService.validateToken(accessToken)) {
                String userId = jwtTokenService.extractUserId(accessToken);
                blacklistService.blacklistToken(accessToken);
                log.info("로그아웃 성공 - 사용자 ID: {}", userId);
            }
        } catch (Exception e) {
            log.warn("로그아웃 처리 중 오류: {}", e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            String trimmedToken = token.trim();
            if (!jwtTokenService.validateToken(trimmedToken)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("토큰 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public LoginResponse generateDevToken(String role, String tenantId) {
        log.info("개발용 토큰 생성 - 역할: {}, 테넌트: {}", role, tenantId);
        try {
            String userId = "dev-user-1";
            String actualRole = role;
            String actualTenantId = tenantId;
            
            if (actualRole == null || actualRole.trim().isEmpty()) {
                actualRole = "ROLE_SUPER";
            }
            if (actualTenantId == null || actualTenantId.trim().isEmpty()) {
                actualTenantId = "dev-tenant";
            }
            
            Role userRole;
            try {
                userRole = Role.valueOf(actualRole);
            } catch (IllegalArgumentException e) {
                userRole = Role.ROLE_SUPER;
                actualRole = "ROLE_SUPER";
            }
            
            Set<Role> roles = Set.of(userRole);
            Map<String, Object> permissions = generateUserPermissions(roles);
            String accessToken = jwtTokenService.generateAccessToken(
                    userId,
                    actualTenantId,
                    actualRole,
                    permissions
            );
            String refreshToken = jwtTokenService.generateRefreshToken(
                    userId,
                    actualTenantId
            );

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .user(LoginResponse.UserInfo.builder()
                            .id(userId)
                            .name("개발용 사용자")
                            .email("dev@smartcon.com")
                            .role(actualRole)
                            .tenantId(actualTenantId)
                            .permissions(permissions)
                            .profileImageUrl(null)
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("개발용 토큰 생성 실패", e);
            throw new RuntimeException("개발용 토큰 생성에 실패했습니다", e);
        }
    }

    private boolean validatePassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            try {
                return passwordEncoder.matches(rawPassword, encodedPassword);
            } catch (Exception e) {
                log.warn("BCrypt 비밀번호 검증 실패: {}", e.getMessage());
                return false;
            }
        } else {
            return rawPassword.equals(encodedPassword);
        }
    }

    private Role getPrimaryRole(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Role.ROLE_WORKER;
        }
        if (roles.contains(Role.ROLE_SUPER)) return Role.ROLE_SUPER;
        if (roles.contains(Role.ROLE_HQ)) return Role.ROLE_HQ;
        if (roles.contains(Role.ROLE_SITE)) return Role.ROLE_SITE;
        if (roles.contains(Role.ROLE_TEAM)) return Role.ROLE_TEAM;
        return Role.ROLE_WORKER;
    }

    private Map<String, Object> generateUserPermissions(Set<Role> roles) {
        Map<String, Object> permissions = new HashMap<>();
        for (Role role : roles) {
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
                    permissions.put("project.read", true);
                    permissions.put("project.write", true);
                    break;
                case ROLE_SITE:
                    permissions.put("site.read", true);
                    permissions.put("site.write", true);
                    permissions.put("attendance.read", true);
                    permissions.put("attendance.write", true);
                    permissions.put("attendance.modify", true);
                    permissions.put("worker.read", true);
                    permissions.put("worker.write", true);
                    permissions.put("worker.approve", true);
                    permissions.put("contract.read", true);
                    permissions.put("face.device.manage", true);
                    break;
                case ROLE_TEAM:
                    permissions.put("team.read", true);
                    permissions.put("team.write", true);
                    permissions.put("attendance.read", true);
                    permissions.put("worker.read", true);
                    permissions.put("contract.read", true);
                    break;
                case ROLE_WORKER:
                    permissions.put("attendance.read", true);
                    permissions.put("contract.read", true);
                    permissions.put("contract.modify.request", true);
                    permissions.put("profile.read", true);
                    permissions.put("profile.write", true);
                    permissions.put("face.register", true);
                    break;
                default:
                    break;
            }
        }
        return permissions;
    }

    /**
     * IP 주소 추출 (실제 구현에서는 HttpServletRequest에서 가져옴)
     * TODO: HttpServletRequest를 통한 실제 IP 추출 구현 필요
     */
    private String extractIpAddress() {
        // 실제 구현에서는 HttpServletRequest에서 추출
        // X-Forwarded-For, X-Real-IP 헤더 고려
        return "127.0.0.1"; // 임시 값
    }

    /**
     * User-Agent 추출 (실제 구현에서는 HttpServletRequest에서 가져옴)
     * TODO: HttpServletRequest를 통한 실제 User-Agent 추출 구현 필요
     */
    private String extractUserAgent() {
        // 실제 구현에서는 HttpServletRequest에서 추출
        return "Unknown"; // 임시 값
    }
}
