package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.AuthenticationAttempt;
import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.AuthenticationAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증 감사 서비스 구현체
 * 모든 인증 시도를 로깅하고 보안 감사 추적 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationAuditServiceImpl implements AuthenticationAuditService {

    private final AuthenticationAttemptRepository attemptRepository;

    // 계정 잠금 임계값: 5회 실패
    private static final int ACCOUNT_LOCK_THRESHOLD = 5;
    
    // 계정 잠금 시간 범위: 30분
    private static final int ACCOUNT_LOCK_TIME_WINDOW_MINUTES = 30;
    
    // IP 차단 임계값: 10회 실패
    private static final int IP_BLOCK_THRESHOLD = 10;
    
    // IP 차단 시간 범위: 1시간
    private static final int IP_BLOCK_TIME_WINDOW_MINUTES = 60;

    @Override
    public void logSuccessfulAuthentication(User user, String username, LoginType loginType,
                                           String ipAddress, String userAgent, Long tenantId) {
        try {
            AuthenticationAttempt attempt = AuthenticationAttempt.success(
                    user, username, loginType, ipAddress, userAgent, tenantId);
            attemptRepository.save(attempt);
            
            log.info("인증 성공 로깅 - 사용자: {}, 로그인 유형: {}, IP: {}", 
                    username, loginType, ipAddress);
        } catch (Exception e) {
            log.error("인증 성공 로깅 실패 - 사용자: {}", username, e);
        }
    }

    @Override
    public void logFailedAuthentication(User user, String username, LoginType loginType,
                                       String failureReason, String ipAddress,
                                       String userAgent, Long tenantId) {
        try {
            AuthenticationAttempt attempt = AuthenticationAttempt.failure(
                    user, username, loginType, failureReason, ipAddress, userAgent, tenantId);
            attemptRepository.save(attempt);
            
            log.warn("인증 실패 로깅 - 사용자: {}, 로그인 유형: {}, 사유: {}, IP: {}", 
                    username, loginType, failureReason, ipAddress);
        } catch (Exception e) {
            log.error("인증 실패 로깅 실패 - 사용자: {}", username, e);
        }
    }

    @Override
    public void logFailedAuthenticationWithoutUser(String username, LoginType loginType,
                                                  String failureReason, String ipAddress,
                                                  String userAgent, Long tenantId) {
        try {
            AuthenticationAttempt attempt = AuthenticationAttempt.failureWithoutUser(
                    username, loginType, failureReason, ipAddress, userAgent, tenantId);
            attemptRepository.save(attempt);
            
            log.warn("인증 실패 로깅 (사용자 없음) - 사용자명: {}, 로그인 유형: {}, 사유: {}, IP: {}", 
                    username, loginType, failureReason, ipAddress);
        } catch (Exception e) {
            log.error("인증 실패 로깅 실패 (사용자 없음) - 사용자명: {}", username, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthenticationAttempt> getRecentAttempts(User user, int limit) {
        List<AuthenticationAttempt> attempts = attemptRepository.findByUserOrderByAttemptTimeDesc(user);
        return attempts.stream().limit(limit).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countFailedAttemptsSince(User user, LocalDateTime since) {
        return attemptRepository.countFailedAttemptsByUserSince(user, since);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFailedAttemptsByIpSince(String ipAddress, LocalDateTime since) {
        return attemptRepository.countFailedAttemptsByIpSince(ipAddress, since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthenticationAttempt> getFailedAttemptsSince(LocalDateTime since) {
        return attemptRepository.findFailedAttemptsSince(since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthenticationAttempt> getAttemptsByTenantSince(Long tenantId, LocalDateTime since) {
        return attemptRepository.findByTenantIdSince(tenantId, since);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean shouldLockAccount(User user) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(ACCOUNT_LOCK_TIME_WINDOW_MINUTES);
        long failedAttempts = countFailedAttemptsSince(user, since);
        
        boolean shouldLock = failedAttempts >= ACCOUNT_LOCK_THRESHOLD;
        
        if (shouldLock) {
            log.warn("계정 잠금 필요 - 사용자: {}, 실패 횟수: {}/{} ({}분 내)", 
                    user.getEmail(), failedAttempts, ACCOUNT_LOCK_THRESHOLD, 
                    ACCOUNT_LOCK_TIME_WINDOW_MINUTES);
        }
        
        return shouldLock;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean shouldBlockIp(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(IP_BLOCK_TIME_WINDOW_MINUTES);
        long failedAttempts = countFailedAttemptsByIpSince(ipAddress, since);
        
        boolean shouldBlock = failedAttempts >= IP_BLOCK_THRESHOLD;
        
        if (shouldBlock) {
            log.warn("IP 차단 필요 - IP: {}, 실패 횟수: {}/{} ({}분 내)", 
                    ipAddress, failedAttempts, IP_BLOCK_THRESHOLD, 
                    IP_BLOCK_TIME_WINDOW_MINUTES);
        }
        
        return shouldBlock;
    }
}
