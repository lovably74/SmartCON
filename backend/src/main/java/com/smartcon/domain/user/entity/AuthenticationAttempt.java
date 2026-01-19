package com.smartcon.domain.user.entity;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인증 시도 로그 엔티티
 * 모든 로그인 시도를 기록하여 보안 감사 추적 제공
 */
@Entity
@Table(name = "authentication_attempts", indexes = {
    @Index(name = "idx_auth_user_id", columnList = "user_id"),
    @Index(name = "idx_auth_attempt_time", columnList = "attempt_time"),
    @Index(name = "idx_auth_success", columnList = "success"),
    @Index(name = "idx_auth_ip_address", columnList = "ip_address")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "username", nullable = false, length = 100)
    private String username; // 로그인 시도한 사용자명 (이메일 또는 사업자번호)

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType; // 로그인 유형 (BUSINESS, SOCIAL)

    @Column(name = "success", nullable = false)
    private Boolean success; // 성공 여부

    @Column(name = "failure_reason", length = 500)
    private String failureReason; // 실패 사유

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IPv4/IPv6 주소

    @Column(name = "user_agent", length = 500)
    private String userAgent; // 브라우저/클라이언트 정보

    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime; // 시도 시간

    @Column(name = "tenant_id")
    private Long tenantId; // 테넌트 ID (멀티테넌트 환경)

    /**
     * 성공한 인증 시도 생성
     */
    public static AuthenticationAttempt success(User user, String username, LoginType loginType, 
                                                String ipAddress, String userAgent, Long tenantId) {
        return AuthenticationAttempt.builder()
                .user(user)
                .username(username)
                .loginType(loginType)
                .success(true)
                .failureReason(null)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .attemptTime(LocalDateTime.now())
                .tenantId(tenantId)
                .build();
    }

    /**
     * 실패한 인증 시도 생성
     */
    public static AuthenticationAttempt failure(User user, String username, LoginType loginType,
                                                String failureReason, String ipAddress, 
                                                String userAgent, Long tenantId) {
        return AuthenticationAttempt.builder()
                .user(user)
                .username(username)
                .loginType(loginType)
                .success(false)
                .failureReason(failureReason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .attemptTime(LocalDateTime.now())
                .tenantId(tenantId)
                .build();
    }

    /**
     * 사용자 없이 실패한 인증 시도 생성 (존재하지 않는 사용자)
     */
    public static AuthenticationAttempt failureWithoutUser(String username, LoginType loginType,
                                                           String failureReason, String ipAddress,
                                                           String userAgent, Long tenantId) {
        return AuthenticationAttempt.builder()
                .user(null)
                .username(username)
                .loginType(loginType)
                .success(false)
                .failureReason(failureReason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .attemptTime(LocalDateTime.now())
                .tenantId(tenantId)
                .build();
    }
}
