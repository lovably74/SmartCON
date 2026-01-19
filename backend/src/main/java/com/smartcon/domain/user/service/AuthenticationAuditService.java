package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.AuthenticationAttempt;
import com.smartcon.domain.user.entity.LoginType;
import com.smartcon.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증 감사 서비스 인터페이스
 * 모든 인증 시도를 로깅하고 보안 감사 추적 제공
 */
public interface AuthenticationAuditService {

    /**
     * 성공한 인증 시도 로깅
     */
    void logSuccessfulAuthentication(User user, String username, LoginType loginType,
                                     String ipAddress, String userAgent, Long tenantId);

    /**
     * 실패한 인증 시도 로깅
     */
    void logFailedAuthentication(User user, String username, LoginType loginType,
                                 String failureReason, String ipAddress, 
                                 String userAgent, Long tenantId);

    /**
     * 사용자 없이 실패한 인증 시도 로깅 (존재하지 않는 사용자)
     */
    void logFailedAuthenticationWithoutUser(String username, LoginType loginType,
                                           String failureReason, String ipAddress,
                                           String userAgent, Long tenantId);

    /**
     * 특정 사용자의 최근 인증 시도 조회
     */
    List<AuthenticationAttempt> getRecentAttempts(User user, int limit);

    /**
     * 특정 사용자의 특정 기간 내 실패한 인증 시도 수 조회
     */
    long countFailedAttemptsSince(User user, LocalDateTime since);

    /**
     * 특정 IP 주소의 특정 기간 내 실패한 인증 시도 수 조회
     */
    long countFailedAttemptsByIpSince(String ipAddress, LocalDateTime since);

    /**
     * 특정 기간 내 모든 실패한 인증 시도 조회
     */
    List<AuthenticationAttempt> getFailedAttemptsSince(LocalDateTime since);

    /**
     * 테넌트별 특정 기간 내 인증 시도 조회
     */
    List<AuthenticationAttempt> getAttemptsByTenantSince(Long tenantId, LocalDateTime since);

    /**
     * 계정 잠금 여부 확인 (5회 실패시 30분 잠금)
     */
    boolean shouldLockAccount(User user);

    /**
     * IP 주소 기반 차단 여부 확인 (과도한 실패 시도)
     */
    boolean shouldBlockIp(String ipAddress);
}
