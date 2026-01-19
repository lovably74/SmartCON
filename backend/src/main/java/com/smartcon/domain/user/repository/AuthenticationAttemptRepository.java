package com.smartcon.domain.user.repository;

import com.smartcon.domain.user.entity.AuthenticationAttempt;
import com.smartcon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증 시도 로그 리포지토리
 */
@Repository
public interface AuthenticationAttemptRepository extends JpaRepository<AuthenticationAttempt, Long> {

    /**
     * 특정 사용자의 최근 인증 시도 조회
     */
    List<AuthenticationAttempt> findByUserOrderByAttemptTimeDesc(User user);

    /**
     * 특정 사용자의 특정 기간 내 인증 시도 조회
     */
    @Query("SELECT a FROM AuthenticationAttempt a WHERE a.user = :user " +
           "AND a.attemptTime >= :startTime ORDER BY a.attemptTime DESC")
    List<AuthenticationAttempt> findByUserAndAttemptTimeAfter(
            @Param("user") User user, 
            @Param("startTime") LocalDateTime startTime);

    /**
     * 특정 사용자의 특정 기간 내 실패한 인증 시도 수 조회
     */
    @Query("SELECT COUNT(a) FROM AuthenticationAttempt a WHERE a.user = :user " +
           "AND a.success = false AND a.attemptTime >= :startTime")
    long countFailedAttemptsByUserSince(
            @Param("user") User user, 
            @Param("startTime") LocalDateTime startTime);

    /**
     * 특정 IP 주소의 최근 인증 시도 조회
     */
    List<AuthenticationAttempt> findByIpAddressOrderByAttemptTimeDesc(String ipAddress);

    /**
     * 특정 IP 주소의 특정 기간 내 실패한 인증 시도 수 조회
     */
    @Query("SELECT COUNT(a) FROM AuthenticationAttempt a WHERE a.ipAddress = :ipAddress " +
           "AND a.success = false AND a.attemptTime >= :startTime")
    long countFailedAttemptsByIpSince(
            @Param("ipAddress") String ipAddress, 
            @Param("startTime") LocalDateTime startTime);

    /**
     * 특정 사용자명의 최근 인증 시도 조회
     */
    List<AuthenticationAttempt> findByUsernameOrderByAttemptTimeDesc(String username);

    /**
     * 특정 기간 내 모든 인증 시도 조회
     */
    @Query("SELECT a FROM AuthenticationAttempt a WHERE a.attemptTime >= :startTime " +
           "ORDER BY a.attemptTime DESC")
    List<AuthenticationAttempt> findAllSince(@Param("startTime") LocalDateTime startTime);

    /**
     * 특정 기간 내 실패한 인증 시도 조회
     */
    @Query("SELECT a FROM AuthenticationAttempt a WHERE a.success = false " +
           "AND a.attemptTime >= :startTime ORDER BY a.attemptTime DESC")
    List<AuthenticationAttempt> findFailedAttemptsSince(@Param("startTime") LocalDateTime startTime);

    /**
     * 테넌트별 특정 기간 내 인증 시도 조회
     */
    @Query("SELECT a FROM AuthenticationAttempt a WHERE a.tenantId = :tenantId " +
           "AND a.attemptTime >= :startTime ORDER BY a.attemptTime DESC")
    List<AuthenticationAttempt> findByTenantIdSince(
            @Param("tenantId") Long tenantId, 
            @Param("startTime") LocalDateTime startTime);
}
