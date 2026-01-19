package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import com.smartcon.domain.user.repository.AuthenticationAttemptRepository;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.AuthenticationAuditService;
import com.smartcon.domain.user.service.AuthenticationAuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 12: Authentication Security Enforcement
 * 인증 보안 강화 테스트
 * 
 * Feature: smartcon-lite-role-based-system
 * Validates: Requirements 27.1, 27.2
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationSecurityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationAttemptRepository attemptRepository;

    @Autowired
    private AuthenticationAuditService auditService;

    @BeforeEach
    public void setUp() {
        attemptRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Property 12.1: 5회 로그인 실패시 계정 잠금")
    void accountShouldBeLockedAfter5FailedAttempts() {
        // Given: 사용자 생성
        User user = createTestUser("test@example.com");
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

    @Test
    @DisplayName("Property 12.2: 계정 잠금 30분 후 자동 해제")
    void accountShouldBeUnlockedAfter30Minutes() {
        // Given: 계정이 잠긴 사용자 (30분 전에 잠김)
        User user = createTestUser("locked@example.com");
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

    @Test
    @DisplayName("Property 12.3: 성공한 로그인 후 실패 횟수 초기화")
    void failureCountShouldBeResetAfterSuccessfulLogin() {
        // Given: 로그인 실패 횟수가 있는 사용자
        User user = createTestUser("reset@example.com");
        user.setLoginFailureCount(3);
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

    @Test
    @DisplayName("Property 12.4: 모든 인증 시도 로깅 - 성공")
    void successfulAuthenticationAttemptsShouldBeLogged() {
        // Given: 사용자 저장
        User user = createTestUser("success@example.com");
        User savedUser = userRepository.save(user);
        
        // When: 성공한 인증 시도 로깅
        auditService.logSuccessfulAuthentication(
                savedUser,
                savedUser.getEmail(),
                LoginType.BUSINESS,
                "192.168.1.1",
                "Mozilla/5.0",
                savedUser.getTenantId()
        );
        
        // Then: 로그가 저장되어야 함
        List<AuthenticationAttempt> attempts = attemptRepository.findByUserOrderByAttemptTimeDesc(savedUser);
        assertThat(attempts).isNotEmpty();
        
        AuthenticationAttempt attempt = attempts.get(0);
        assertThat(attempt.getUsername()).isEqualTo(savedUser.getEmail());
        assertThat(attempt.getSuccess()).isTrue();
        assertThat(attempt.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(attempt.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(attempt.getAttemptTime()).isNotNull();
    }

    @Test
    @DisplayName("Property 12.4: 모든 인증 시도 로깅 - 실패")
    void failedAuthenticationAttemptsShouldBeLogged() {
        // Given: 사용자 저장
        User user = createTestUser("failure@example.com");
        User savedUser = userRepository.save(user);
        
        // When: 실패한 인증 시도 로깅
        auditService.logFailedAuthentication(
                savedUser,
                savedUser.getEmail(),
                LoginType.BUSINESS,
                "비밀번호 불일치",
                "192.168.1.1",
                "Mozilla/5.0",
                savedUser.getTenantId()
        );
        
        // Then: 로그가 저장되어야 함
        List<AuthenticationAttempt> attempts = attemptRepository.findByUserOrderByAttemptTimeDesc(savedUser);
        assertThat(attempts).isNotEmpty();
        
        AuthenticationAttempt attempt = attempts.get(0);
        assertThat(attempt.getUsername()).isEqualTo(savedUser.getEmail());
        assertThat(attempt.getSuccess()).isFalse();
        assertThat(attempt.getFailureReason()).isEqualTo("비밀번호 불일치");
        assertThat(attempt.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(attempt.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(attempt.getAttemptTime()).isNotNull();
    }

    @Test
    @DisplayName("Property 12.5: 실패한 인증 시도 조회")
    void failedAttemptsShouldBeRetrievable() {
        // Given: 여러 번의 실패한 인증 시도
        User user = createTestUser("retrieve@example.com");
        User savedUser = userRepository.save(user);
        
        int failureCount = 7;
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

    @Test
    @DisplayName("Property 12.6: IP 주소 기반 차단")
    void excessiveFailuresFromIpShouldBeDetected() {
        // Given: 동일 IP에서 여러 번의 실패한 인증 시도
        User user = createTestUser("ipblock@example.com");
        User savedUser = userRepository.save(user);
        String ipAddress = "203.0.113.1";
        
        int failureCount = 15;
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

    @Test
    @DisplayName("Property 12.7: 사용자 없이 실패한 인증 시도 로깅")
    void failedAttemptsWithoutUserShouldBeLogged() {
        // Given: 존재하지 않는 사용자명
        String username = "nonexistent@example.com";
        
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

    @Test
    @DisplayName("Property 12.8: 4회 실패 후에는 계정이 잠기지 않음")
    void accountShouldNotBeLockedAfter4FailedAttempts() {
        // Given: 사용자 생성
        User user = createTestUser("notlocked@example.com");
        User savedUser = userRepository.save(user);
        
        // When: 4회 로그인 실패
        for (int i = 0; i < 4; i++) {
            savedUser.incrementLoginFailureCount();
        }
        userRepository.save(savedUser);
        
        // Then: 계정이 잠기지 않아야 함
        User notLockedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(notLockedUser.isLocked()).isFalse();
        assertThat(notLockedUser.getLoginFailureCount()).isEqualTo(4);
        assertThat(notLockedUser.getAccountLockedUntil()).isNull();
    }

    @Test
    @DisplayName("Property 12.9: 계정 잠금 시간이 정확히 30분")
    void accountLockDurationShouldBe30Minutes() {
        // Given: 사용자 생성
        User user = createTestUser("locktime@example.com");
        User savedUser = userRepository.save(user);
        
        // When: 5회 로그인 실패로 계정 잠금
        LocalDateTime beforeLock = LocalDateTime.now();
        for (int i = 0; i < 5; i++) {
            savedUser.incrementLoginFailureCount();
        }
        userRepository.save(savedUser);
        LocalDateTime afterLock = LocalDateTime.now();
        
        // Then: 잠금 해제 시간이 현재 시간 + 30분이어야 함
        User lockedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(lockedUser.getAccountLockedUntil()).isNotNull();
        assertThat(lockedUser.getAccountLockedUntil()).isBetween(
                beforeLock.plusMinutes(30).minusSeconds(1),
                afterLock.plusMinutes(30).plusSeconds(1)
        );
    }

    // Helper method
    private User createTestUser(String email) {
        User user = User.builder()
                .name("Test User")
                .email(email)
                .passwordHash("$2a$10$dummyHashForTesting")
                .authProvider(AuthProvider.LOCAL)
                .loginType(LoginType.BUSINESS)
                .isActive(true)
                .isEmailVerified(true)
                .loginFailureCount(0)
                .build();
        user.setTenantId(1L);
        user.addRole(Role.ROLE_HQ);
        return user;
    }
}
