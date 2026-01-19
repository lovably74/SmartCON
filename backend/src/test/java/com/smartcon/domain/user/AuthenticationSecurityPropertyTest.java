package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import com.smartcon.domain.user.repository.AuthenticationAttemptRepository;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.AuthenticationAuditService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 12: Authentication Security Enforcement
 * 인증 보안 강화 속성 테스트
 * 
 * Feature: smartcon-lite-role-based-system
 * Validates: Requirements 27.1, 27.2
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationSecurityPropertyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationAttemptRepository attemptRepository;

    @Autowired
    private AuthenticationAuditService auditService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        attemptRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Property 12.1: 5회 로그인 실패시 계정 잠금
     * For any user, after 5 consecutive failed login attempts, 
     * the account should be locked for 30 minutes
     */
    @Property(tries = 100)
    @Label("Property 12.1: 5회 로그인 실패시 계정 잠금")
    void accountShouldBeLockedAfter5FailedAttempts(
            @ForAll("validUser") User user) {
        
        // Given: 사용자 저장
        User savedUser = userRepository.save(user);
        
        // When: 5회 로그인 실패
        for (int i = 0; i < 5; i++) {
            savedUser.incrementLoginFailureCount();
            auditService.logFailedAuthentication(
                    savedUser,
                    savedUser.getEmail(),
                    LoginType.BUSINESS,
                    "비밀번호 불일치",
                    "127.0.0.1",
                    "Test Agent",
                    savedUser.getTenantId()
            );
        }
        userRepository.save(savedUser);
        
        // Then: 계정이 잠겨야 함
        User lockedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(lockedUser.isLocked()).isTrue();
        assertThat(lockedUser.getLoginFailureCount()).isEqualTo(5);
        assertThat(lockedUser.getAccountLockedUntil()).isNotNull();
        assertThat(lockedUser.getAccountLockedUntil()).isAfter(LocalDateTime.now());
    }

    /**
     * Property 12.2: 계정 잠금 30분 후 자동 해제
     * For any locked account, after 30 minutes, 
     * the account should be automatically unlocked
     */
    @Property(tries = 100)
    @Label("Property 12.2: 계정 잠금 30분 후 자동 해제")
    void accountShouldBeUnlockedAfter30Minutes(
            @ForAll("validUser") User user) {
        
        // Given: 계정이 잠긴 사용자 (30분 전에 잠김)
        user.setLoginFailureCount(5);
        user.setAccountLockedUntil(LocalDateTime.now().minusMinutes(31));
        User savedUser = userRepository.save(user);
        
        // When: 잠금 상태 확인
        boolean isLocked = savedUser.isLocked();
        
        // Then: 30분이 지났으므로 잠금이 해제되어야 함
        assertThat(isLocked).isFalse();
        
        // 잠금 해제 후 실패 횟수도 초기화되어야 함
        User unlockedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(unlockedUser.getAccountLockedUntil()).isNull();
        assertThat(unlockedUser.getLoginFailureCount()).isEqualTo(0);
    }

    /**
     * Property 12.3: 성공한 로그인 후 실패 횟수 초기화
     * For any user with failed login attempts, 
     * after a successful login, the failure count should be reset to 0
     */
    @Property(tries = 100)
    @Label("Property 12.3: 성공한 로그인 후 실패 횟수 초기화")
    void failureCountShouldBeResetAfterSuccessfulLogin(
            @ForAll("validUser") User user,
            @ForAll @IntRange(min = 1, max = 4) int failureCount) {
        
        // Given: 로그인 실패 횟수가 있는 사용자
        user.setLoginFailureCount(failureCount);
        User savedUser = userRepository.save(user);
        
        // When: 성공한 로그인
        savedUser.resetLoginFailureCount();
        auditService.logSuccessfulAuthentication(
                savedUser,
                savedUser.getEmail(),
                LoginType.BUSINESS,
                "127.0.0.1",
                "Test Agent",
                savedUser.getTenantId()
        );
        userRepository.save(savedUser);
        
        // Then: 실패 횟수가 0으로 초기화되어야 함
        User resetUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(resetUser.getLoginFailureCount()).isEqualTo(0);
        assertThat(resetUser.getAccountLockedUntil()).isNull();
    }

    /**
     * Property 12.4: 모든 인증 시도 로깅
     * For any authentication attempt (success or failure), 
     * the system should log the attempt with all relevant details
     */
    @Property(tries = 100)
    @Label("Property 12.4: 모든 인증 시도 로깅")
    void allAuthenticationAttemptsShouldBeLogged(
            @ForAll("validUser") User user,
            @ForAll boolean isSuccess) {
        
        // Given: 사용자 저장
        User savedUser = userRepository.save(user);
        
        // When: 인증 시도 로깅
        if (isSuccess) {
            auditService.logSuccessfulAuthentication(
                    savedUser,
                    savedUser.getEmail(),
                    LoginType.BUSINESS,
                    "192.168.1.1",
                    "Mozilla/5.0",
                    savedUser.getTenantId()
            );
        } else {
            auditService.logFailedAuthentication(
                    savedUser,
                    savedUser.getEmail(),
                    LoginType.BUSINESS,
                    "비밀번호 불일치",
                    "192.168.1.1",
                    "Mozilla/5.0",
                    savedUser.getTenantId()
            );
        }
        
        // Then: 로그가 저장되어야 함
        List<AuthenticationAttempt> attempts = attemptRepository.findByUserOrderByAttemptTimeDesc(savedUser);
        assertThat(attempts).isNotEmpty();
        
        AuthenticationAttempt attempt = attempts.get(0);
        assertThat(attempt.getUsername()).isEqualTo(savedUser.getEmail());
        assertThat(attempt.getSuccess()).isEqualTo(isSuccess);
        assertThat(attempt.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(attempt.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(attempt.getAttemptTime()).isNotNull();
    }

    /**
     * Property 12.5: 실패한 인증 시도 조회
     * For any user with failed authentication attempts, 
     * the system should be able to retrieve all failed attempts within a time range
     */
    @Property(tries = 100)
    @Label("Property 12.5: 실패한 인증 시도 조회")
    void failedAttemptsShouldBeRetrievable(
            @ForAll("validUser") User user,
            @ForAll @IntRange(min = 1, max = 10) int failureCount) {
        
        // Given: 여러 번의 실패한 인증 시도
        User savedUser = userRepository.save(user);
        
        for (int i = 0; i < failureCount; i++) {
            auditService.logFailedAuthentication(
                    savedUser,
                    savedUser.getEmail(),
                    LoginType.BUSINESS,
                    "비밀번호 불일치",
                    "127.0.0.1",
                    "Test Agent",
                    savedUser.getTenantId()
            );
        }
        
        // When: 특정 기간 내 실패한 시도 조회
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long count = auditService.countFailedAttemptsSince(savedUser, since);
        
        // Then: 실패 횟수가 정확해야 함
        assertThat(count).isEqualTo(failureCount);
    }

    /**
     * Property 12.6: IP 주소 기반 차단
     * For any IP address with excessive failed attempts, 
     * the system should identify it as requiring blocking
     */
    @Property(tries = 100)
    @Label("Property 12.6: IP 주소 기반 차단")
    void excessiveFailuresFromIpShouldBeDetected(
            @ForAll("validUser") User user,
            @ForAll @IntRange(min = 10, max = 20) int failureCount) {
        
        // Given: 동일 IP에서 여러 번의 실패한 인증 시도
        User savedUser = userRepository.save(user);
        String ipAddress = "203.0.113.1";
        
        for (int i = 0; i < failureCount; i++) {
            auditService.logFailedAuthentication(
                    savedUser,
                    savedUser.getEmail(),
                    LoginType.BUSINESS,
                    "비밀번호 불일치",
                    ipAddress,
                    "Test Agent",
                    savedUser.getTenantId()
            );
        }
        
        // When: IP 차단 여부 확인
        boolean shouldBlock = auditService.shouldBlockIp(ipAddress);
        
        // Then: 10회 이상 실패시 차단되어야 함
        assertThat(shouldBlock).isTrue();
    }

    /**
     * Property 12.7: 사용자 없이 실패한 인증 시도 로깅
     * For any authentication attempt with non-existent username, 
     * the system should log the attempt without a user reference
     */
    @Property(tries = 100)
    @Label("Property 12.7: 사용자 없이 실패한 인증 시도 로깅")
    void failedAttemptsWithoutUserShouldBeLogged(
            @ForAll @AlphaChars @StringLength(min = 5, max = 50) String username) {
        
        // When: 존재하지 않는 사용자로 인증 시도 로깅
        auditService.logFailedAuthenticationWithoutUser(
                username,
                LoginType.BUSINESS,
                "존재하지 않는 사용자",
                "192.168.1.100",
                "Mozilla/5.0",
                null
        );
        
        // Then: 로그가 저장되어야 함
        List<AuthenticationAttempt> attempts = attemptRepository.findByUsernameOrderByAttemptTimeDesc(username);
        assertThat(attempts).isNotEmpty();
        
        AuthenticationAttempt attempt = attempts.get(0);
        assertThat(attempt.getUser()).isNull();
        assertThat(attempt.getUsername()).isEqualTo(username);
        assertThat(attempt.getSuccess()).isFalse();
        assertThat(attempt.getFailureReason()).isEqualTo("존재하지 않는 사용자");
    }

    // ========== Arbitraries ==========

    @Provide
    Arbitrary<User> validUser() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(30),
                Arbitraries.longs().between(1L, 1000L)
        ).as((name, email, tenantId) -> {
            User user = User.builder()
                    .name(name)
                    .email(email + "@test.com")
                    .passwordHash("$2a$10$dummyHashForTesting")
                    .authProvider(AuthProvider.LOCAL)
                    .loginType(LoginType.BUSINESS)
                    .isActive(true)
                    .isEmailVerified(true)
                    .loginFailureCount(0)
                    .build();
            user.setTenantId(tenantId);
            user.addRole(Role.ROLE_HQ);
            return user;
        });
    }
}
