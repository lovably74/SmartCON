package com.smartcon.domain.billing.repository;

import com.smartcon.domain.billing.entity.BillingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 기록 데이터 접근 리포지토리
 */
@Repository
public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {

    /**
     * 결제 상태별 조회
     */
    List<BillingRecord> findByPaymentStatus(BillingRecord.PaymentStatus paymentStatus);

    /**
     * 실패한 결제 건 조회
     */
    List<BillingRecord> findByPaymentStatusOrderByCreatedAtDesc(BillingRecord.PaymentStatus paymentStatus);

    /**
     * 특정 기간 내 총 매출 조회
     */
    @Query("SELECT COALESCE(SUM(br.totalAmount), 0) FROM BillingRecord br WHERE br.paymentStatus = 'SUCCESS' AND br.paymentCompletedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 월별 매출 통계 조회
     */
    @Query("SELECT YEAR(br.paymentCompletedAt) as year, MONTH(br.paymentCompletedAt) as month, SUM(br.totalAmount) as revenue " +
           "FROM BillingRecord br WHERE br.paymentStatus = 'SUCCESS' AND br.paymentCompletedAt >= :startDate " +
           "GROUP BY YEAR(br.paymentCompletedAt), MONTH(br.paymentCompletedAt) ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyRevenueStats(@Param("startDate") LocalDateTime startDate);

    /**
     * 결제 상태별 건수 조회
     */
    long countByPaymentStatus(BillingRecord.PaymentStatus paymentStatus);

    /**
     * 테넌트별 결제 기록 조회
     */
    List<BillingRecord> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}