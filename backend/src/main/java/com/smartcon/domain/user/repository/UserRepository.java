package com.smartcon.domain.user.repository;

import com.smartcon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 사용자 데이터 접근 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 전체 사용자 수 조회
     */
    long count();

    /**
     * 특정 기간 내 생성된 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 테넌트별 사용자 수 조회
     */
    long countByTenantId(Long tenantId);
}