package com.smartcon.domain.tenant.repository;

import com.smartcon.domain.tenant.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 테넌트 데이터 접근 리포지토리
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * 회사명으로 검색 (부분 일치) - H2 호환성을 위한 사용자 정의 쿼리
     */
    @Query("SELECT t FROM Tenant t WHERE UPPER(t.companyName) LIKE UPPER(CONCAT('%', :companyName, '%'))")
    Page<Tenant> findByCompanyNameContainingIgnoreCase(@Param("companyName") String companyName, Pageable pageable);

    /**
     * 구독 상태별 조회
     */
    Page<Tenant> findByStatus(Tenant.SubscriptionStatus status, Pageable pageable);

    /**
     * 회사명과 상태로 검색 - H2 호환성을 위한 사용자 정의 쿼리
     */
    @Query("SELECT t FROM Tenant t WHERE UPPER(t.companyName) LIKE UPPER(CONCAT('%', :companyName, '%')) AND t.status = :status")
    Page<Tenant> findByCompanyNameContainingIgnoreCaseAndStatus(
            @Param("companyName") String companyName, 
            @Param("status") Tenant.SubscriptionStatus status, 
            Pageable pageable
    );

    /**
     * 전체 테넌트 수 조회
     */
    long count();

    /**
     * 상태별 테넌트 수 조회
     */
    long countByStatus(Tenant.SubscriptionStatus status);

    /**
     * 특정 기간 내 생성된 테넌트 수 조회
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 최근 생성된 테넌트 목록 조회
     */
    List<Tenant> findTop10ByOrderByCreatedAtDesc();
}