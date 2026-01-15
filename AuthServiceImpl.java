package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.LoginRequest;
import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.RefreshTokenRequest;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ?몄쬆 ?쒕퉬??援ы쁽泥?
 * JWT ?좏겙 湲곕컲 ?몄쬆 泥섎━
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

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("濡쒓렇???쒕룄 - ?대찓?? {}, ?뚮꼳?? {}", request.getEmail(), request.getTenantId());

        // ?뚮꼳??而⑦뀓?ㅽ듃 ?ㅼ젙
        if (request.getTenantId() != null) {
            TenantContext.setCurrentTenant(request.getTenantId());
        }

        try {
            // ?ъ슜??議고쉶
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("濡쒓렇???ㅽ뙣 - 議댁옱?섏? ?딅뒗 ?ъ슜?? {}", request.getEmail());
                throw new IllegalArgumentException("?대찓???먮뒗 鍮꾨?踰덊샇媛 ?щ컮瑜댁? ?딆뒿?덈떎");
            }

            User user = userOptional.get();

            // 怨꾩젙 ?좉툑 ?뺤씤
            if (user.isLocked()) {
                log.warn("濡쒓렇???ㅽ뙣 - 怨꾩젙 ?좉툑: {}", request.getEmail());
                throw new IllegalArgumentException("怨꾩젙???좉꺼?덉뒿?덈떎. 愿由ъ옄?먭쾶 臾몄쓽?섏꽭??);
            }

            // 怨꾩젙 ?쒖꽦???뺤씤
            if (!user.isActive()) {
                log.warn("濡쒓렇???ㅽ뙣 - 鍮꾪솢??怨꾩젙: {}", request.getEmail());
                throw new IllegalArgumentException("鍮꾪솢?깊솕??怨꾩젙?낅땲??);
            }

            // ?대찓???몄쬆 ?뺤씤 (濡쒖뺄 怨꾩젙留?
            if (user.getProvider() == User.Provider.LOCAL && !user.isEmailVerified()) {
                log.warn("濡쒓렇???ㅽ뙣 - ?대찓??誘몄씤利? {}", request.getEmail());
                throw new IllegalArgumentException("?대찓???몄쬆???꾩슂?⑸땲??);
            }

            // 鍮꾨?踰덊샇 寃利?(媛쒕컻 ?④퀎?먯꽌??媛꾨떒??寃利?
            if (!validatePassword(request.getPassword(), user.getPasswordHash())) {
                user.incrementLoginFailureCount();
                userRepository.save(user);
                log.warn("濡쒓렇???ㅽ뙣 - 鍮꾨?踰덊샇 遺덉씪移? {} (?ㅽ뙣 ?잛닔: {})", 
                        request.getEmail(), user.getLoginFailureCount());
                throw new IllegalArgumentException("?대찓???먮뒗 鍮꾨?踰덊샇媛 ?щ컮瑜댁? ?딆뒿?덈떎");
            }

            // 濡쒓렇???깃났 - ?ㅽ뙣 ?잛닔 珥덇린??
            user.resetLoginFailureCount();
            userRepository.save(user);

            // 沅뚰븳 ?뺣낫 ?앹꽦
            Map<String, Object> permissions = generateUserPermissions(user.getRole());

            // JWT ?좏겙 ?앹꽦
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

            log.info("濡쒓렇???깃났 - ?ъ슜?? {}, ??븷: {}", user.getEmail(), user.getRole());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1?쒓컙
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
        log.info("?좏겙 媛깆떊 ?붿껌");

        try {
            // Refresh Token 寃利?
            if (!jwtTokenService.validateToken(request.getRefreshToken())) {
                log.warn("?좏겙 媛깆떊 ?ㅽ뙣 - ?좏슚?섏? ?딆? Refresh Token");
                throw new IllegalArgumentException("?좏슚?섏? ?딆? Refresh Token?낅땲??);
            }

            if (!jwtTokenService.isRefreshToken(request.getRefreshToken())) {
                log.warn("?좏겙 媛깆떊 ?ㅽ뙣 - Access Token?쇰줈 媛깆떊 ?쒕룄");
                throw new IllegalArgumentException("Refresh Token???꾨떃?덈떎");
            }

            // ?좏겙?먯꽌 ?ъ슜???뺣낫 異붿텧
            String userId = jwtTokenService.extractUserId(request.getRefreshToken());
            String tenantId = jwtTokenService.extractTenantId(request.getRefreshToken());

            // ?뚮꼳??而⑦뀓?ㅽ듃 ?ㅼ젙
            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
            }

            // ?ъ슜??議고쉶 (媛쒕컻???좏겙??寃쎌슦 臾몄옄??ID 泥섎━)
            Optional<User> userOptional;
            if (userId.startsWith("dev-user")) {
                // 媛쒕컻???좏겙??寃쎌슦 ?ㅼ젣 ?ъ슜??議고쉶 ???湲곕낯 ?ъ슜???앹꽦
                User devUser = User.builder()
                    .name("媛쒕컻???ъ슜??)
                    .email("dev@smartcon.com")
                    .role(Role.ROLE_WORKER)
                    .isActive(true)
                    .build();
                devUser.setId(1L);
                devUser.setTenantId(tenantId != null ? Long.parseLong(tenantId) : 1L);
                userOptional = Optional.of(devUser);
            } else {
                userOptional = userRepository.findById(Long.parseLong(userId));
            }
            if (userOptional.isEmpty()) {
                log.warn("?좏겙 媛깆떊 ?ㅽ뙣 - 議댁옱?섏? ?딅뒗 ?ъ슜?? {}", userId);
                throw new IllegalArgumentException("議댁옱?섏? ?딅뒗 ?ъ슜?먯엯?덈떎");
            }

            User user = userOptional.get();

            // 怨꾩젙 ?곹깭 ?뺤씤
            if (!user.isActive()) {
                log.warn("?좏겙 媛깆떊 ?ㅽ뙣 - 鍮꾪솢??怨꾩젙: {}", user.getEmail());
                throw new IllegalArgumentException("鍮꾪솢?깊솕??怨꾩젙?낅땲??);
            }

            // 沅뚰븳 ?뺣낫 ?앹꽦
            Map<String, Object> permissions = generateUserPermissions(user.getRole());

            // ?덈줈??Access Token ?앹꽦
            String newAccessToken = jwtTokenService.generateAccessToken(
                    userId,
                    user.getTenantId() != null ? user.getTenantId().toString() : null,
                    user.getRole().name(),
                    permissions
            );

            log.info("?좏겙 媛깆떊 ?깃났 - ?ъ슜?? {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken()) // 湲곗〈 Refresh Token ?ъ궗??
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1?쒓컙
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
        log.info("濡쒓렇?꾩썐 ?붿껌");
        
        try {
            if (jwtTokenService.validateToken(accessToken)) {
                String userId = jwtTokenService.extractUserId(accessToken);
                
                // ?좏겙??釉붾옓由ъ뒪?몄뿉 異붽?
                blacklistService.blacklistToken(accessToken);
                
                log.info("濡쒓렇?꾩썐 ?깃났 - ?ъ슜??ID: {}", userId);
            }
        } catch (Exception e) {
            log.warn("濡쒓렇?꾩썐 泥섎━ 以??ㅻ쪟: {}", e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        // null, 鍮?臾몄옄?? 怨듬갚留??덈뒗 ?좏겙 泥섎━
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            // ?좏겙 ?욌뮘 怨듬갚 ?쒓굅
            String trimmedToken = token.trim();
            
            // JWT ?좏겙 ?좏슚??寃利?
            if (!jwtTokenService.validateToken(trimmedToken)) {
                return false;
            }
            
            // 釉붾옓由ъ뒪???뺤씤 (?꾩옱??援ы쁽?섏? ?딆븯?쇰?濡?二쇱꽍 泥섎━)
            // if (jwtTokenBlacklistService.isTokenBlacklisted(trimmedToken)) {
            //     return false;
            // }
            
            return true;
        } catch (Exception e) {
            log.warn("?좏겙 寃利?以??ㅻ쪟 諛쒖깮: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public LoginResponse generateDevToken(String role, String tenantId) {
        log.info("媛쒕컻???좏겙 ?앹꽦 - ??븷: {}, ?뚮꼳?? {}", role, tenantId);

        try {
            // 媛쒕컻???ъ슜???뺣낫 ?앹꽦
            String userId = "dev-user-1";
            
            // 湲곕낯媛?泥섎━
            String actualRole = role;
            String actualTenantId = tenantId;
            
            if (actualRole == null || actualRole.trim().isEmpty()) {
                actualRole = "ROLE_SUPER"; // 湲곕낯媛?
            }
            
            if (actualTenantId == null || actualTenantId.trim().isEmpty()) {
                actualTenantId = "dev-tenant"; // 湲곕낯媛?
            }
            
            // ??븷???곕Ⅸ 沅뚰븳 ?뺣낫 ?앹꽦
            Role userRole;
            try {
                userRole = Role.valueOf(actualRole);
            } catch (IllegalArgumentException e) {
                userRole = Role.ROLE_SUPER; // 湲곕낯媛?
                actualRole = "ROLE_SUPER";
            }
            
            Map<String, Object> permissions = generateUserPermissions(userRole);

            // JWT ?좏겙 ?앹꽦
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
                    .expiresIn(3600L) // 1?쒓컙
                    .user(LoginResponse.UserInfo.builder()
                            .id(userId)
                            .name("媛쒕컻???ъ슜??)
                            .email("dev@smartcon.com")
                            .role(actualRole)
                            .tenantId(actualTenantId)
                            .permissions(permissions)
                            .profileImageUrl(null)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("媛쒕컻???좏겙 ?앹꽦 ?ㅽ뙣", e);
            throw new RuntimeException("媛쒕컻???좏겙 ?앹꽦???ㅽ뙣?덉뒿?덈떎", e);
        }
    }

    /**
     * 鍮꾨?踰덊샇 寃利?(媛쒕컻??媛꾨떒??援ы쁽)
     */
    private boolean validatePassword(String rawPassword, String encodedPassword) {
        // 媛쒕컻 ?④퀎?먯꽌??媛꾨떒??鍮꾨?踰덊샇 寃利?
        // ?ㅼ젣 ?댁쁺?먯꽌??BCrypt ?깆쓣 ?ъ슜?댁빞 ??
        
        if (encodedPassword == null) {
            // 鍮꾨?踰덊샇媛 ?ㅼ젙?섏? ?딆? 寃쎌슦 (?뚯뀥 濡쒓렇???꾩슜 怨꾩젙)
            return false;
        }

        // 媛쒕컻??媛꾨떒??寃利?(?ㅼ젣濡쒕뒗 passwordEncoder.matches ?ъ슜)
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            // BCrypt ?댁떆??寃쎌슦
            try {
                return passwordEncoder.matches(rawPassword, encodedPassword);
            } catch (Exception e) {
                log.warn("BCrypt 鍮꾨?踰덊샇 寃利??ㅽ뙣: {}", e.getMessage());
                return false;
            }
        } else {
            // 媛쒕컻???됰Ц 鍮꾧탳 (蹂댁븞???꾪뿕?섎?濡??댁쁺?먯꽌???ъ슜 湲덉?)
            return rawPassword.equals(encodedPassword);
        }
    }

    /**
     * ?ъ슜????븷???곕Ⅸ 沅뚰븳 ?뺣낫 ?앹꽦
     */
    private Map<String, Object> generateUserPermissions(Role role) {
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
                // 湲곕낯 沅뚰븳 ?놁쓬
                break;
        }

        return permissions;
    }
}