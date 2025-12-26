package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.Payment;
import com.smartcon.domain.subscription.entity.PaymentStatus;
import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.tenant.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 결제 내역 Repository
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 테넌트의 결제 내역 조회 (페이징)
     */
    @Query("SELECT p FROM Payment p WHERE p.tenant = :tenant ORDER BY p.createdAt DESC")
    Page<Payment> findByTenantOrderByCreatedAtDesc(@Param("tenant") Tenant tenant, Pageable pageable);
    
    /**
     * 구독의 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription = :subscription ORDER BY p.createdAt DESC")
    List<Payment> findBySubscriptionOrderByCreatedAtDesc(@Param("subscription") Subscription subscription);
    
    /**
     * 결제 키로 결제 내역 조회
     */
    Optional<Payment> findByPaymentKey(String paymentKey);
    
    /**
     * 주문 ID로 결제 내역 조회
     */
    Optional<Payment> findByOrderId(String orderId);
    
    /**
     * 특정 상태의 결제 내역 조회
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * 결제 실패 건 조회 (재시도 대상)
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Payment> findFailedPaymentsSince(@Param("since") LocalDateTime since);
    
    /**
     * 특정 기간 매출 합계 조회
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal sumSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 테넌트의 특정 기간 결제 금액 합계
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenant = :tenant AND p.status = 'SUCCESS' AND p.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal sumSuccessfulPaymentsByTenantBetween(@Param("tenant") Tenant tenant, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 세금계산서 미발행 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCESS' AND p.taxInvoiceIssued = false AND p.billingPeriodEnd < :date")
    List<Payment> findTaxInvoicePendingBefore(@Param("date") LocalDate date);
    
    /**
     * 월별 매출 통계 조회
     */
    @Query("SELECT YEAR(p.paidAt) as year, MONTH(p.paidAt) as month, COUNT(p) as count, SUM(p.amount) as total " +
           "FROM Payment p WHERE p.status = 'SUCCESS' AND p.paidAt >= :since " +
           "GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) ORDER BY year DESC, month DESC")
    List<Object[]> findMonthlyRevenueStatsSince(@Param("since") LocalDateTime since);
}