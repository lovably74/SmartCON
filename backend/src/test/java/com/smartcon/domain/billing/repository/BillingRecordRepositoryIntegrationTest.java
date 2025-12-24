package com.smartcon.domain.billing.repository;

import com.smartcon.domain.billing.entity.BillingRecord;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BillingRecordRepository MariaDB 통합 테스트
 */
@DisplayName("BillingRecordRepository MariaDB 통합 테스트")
class BillingRecordRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BillingRecordRepository billingRecordRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    @DisplayName("결제 기록 생성 및 조회 테스트")
    void createAndFindBillingRecord_ShouldWork() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        
        BillingRecord billingRecord = new BillingRecord();
        billingRecord.setTenantId(tenant.getId());
        billingRecord.setSubscriptionPlan("STANDARD");
        billingRecord.setBillingDate(LocalDate.now());
        billingRecord.setBillingPeriodStart(LocalDate.now().minusMonths(1));
        billingRecord.setBillingPeriodEnd(LocalDate.now());
        billingRecord.setBaseAmount(BigDecimal.valueOf(50000));
        billingRecord.setTaxAmount(BigDecimal.valueOf(5000));
        billingRecord.setTotalAmount(BigDecimal.valueOf(55000));
        billingRecord.setPaymentStatus(BillingRecord.PaymentStatus.SUCCESS);
        billingRecord.setPaymentMethod(BillingRecord.PaymentMethod.CARD);

        // When
        BillingRecord savedRecord = billingRecordRepository.save(billingRecord);

        // Then
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getTenantId()).isEqualTo(tenant.getId());
        assertThat(savedRecord.getTotalAmount()).isEqualTo(BigDecimal.valueOf(55000));
        assertThat(savedRecord.getPaymentStatus()).isEqualTo(BillingRecord.PaymentStatus.SUCCESS);
        assertThat(savedRecord.getCreatedAt()).isNotNull();
        assertThat(savedRecord.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 상태별 개수 조회 테스트")
    void countByPaymentStatus_ShouldReturnCorrectCount() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        
        // 성공한 결제 2건
        createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.SUCCESS);
        createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.SUCCESS);
        
        // 실패한 결제 1건
        createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.FAILED);

        // When
        long successCount = billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.SUCCESS);
        long failedCount = billingRecordRepository.countByPaymentStatus(BillingRecord.PaymentStatus.FAILED);

        // Then
        assertThat(successCount).isGreaterThanOrEqualTo(2);
        assertThat(failedCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("기간별 총 매출 조회 테스트")
    void getTotalRevenueByPeriod_ShouldReturnCorrectAmount() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        
        // 성공한 결제만 매출에 포함
        BillingRecord successRecord = createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.SUCCESS);
        successRecord.setTotalAmount(BigDecimal.valueOf(100000));
        successRecord.setPaymentCompletedAt(LocalDateTime.now().minusDays(15));
        billingRecordRepository.save(successRecord);
        
        // 실패한 결제는 매출에 포함되지 않음
        BillingRecord failedRecord = createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.FAILED);
        failedRecord.setTotalAmount(BigDecimal.valueOf(50000));
        billingRecordRepository.save(failedRecord);

        // When
        BigDecimal totalRevenue = billingRecordRepository.getTotalRevenueByPeriod(startDate, endDate);

        // Then
        assertThat(totalRevenue).isGreaterThanOrEqualTo(BigDecimal.valueOf(100000));
    }

    @Test
    @DisplayName("실패한 결제 기록 조회 테스트")
    void findByPaymentStatusOrderByCreatedAtDesc_ShouldReturnFailedPayments() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        
        // 실패한 결제 기록 생성
        for (int i = 0; i < 3; i++) {
            BillingRecord failedRecord = createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.FAILED);
            failedRecord.setFailureReason("카드 한도 초과");
            billingRecordRepository.save(failedRecord);
        }

        // When
        List<BillingRecord> failedPayments = billingRecordRepository
                .findByPaymentStatusOrderByCreatedAtDesc(BillingRecord.PaymentStatus.FAILED);

        // Then
        assertThat(failedPayments).isNotEmpty();
        assertThat(failedPayments.size()).isGreaterThanOrEqualTo(3);
        
        // 생성일 순서 확인 (최신순)
        for (int i = 0; i < failedPayments.size() - 1; i++) {
            assertThat(failedPayments.get(i).getCreatedAt())
                    .isAfterOrEqualTo(failedPayments.get(i + 1).getCreatedAt());
        }
        
        // 모든 결과가 실패 상태인지 확인
        assertThat(failedPayments.stream()
                .allMatch(record -> record.getPaymentStatus() == BillingRecord.PaymentStatus.FAILED))
                .isTrue();
    }

    @Test
    @DisplayName("월별 매출 통계 조회 테스트")
    void getMonthlyRevenueStats_ShouldReturnCorrectStats() {
        // Given
        Tenant tenant = createAndSaveTenant("테스트 회사");
        
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        
        // 최근 몇 개월간의 결제 기록 생성
        BillingRecord record1 = createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.SUCCESS);
        record1.setTotalAmount(BigDecimal.valueOf(100000));
        record1.setPaymentCompletedAt(LocalDateTime.now().minusMonths(2));
        billingRecordRepository.save(record1);
        
        BillingRecord record2 = createAndSaveBillingRecord(tenant.getId(), BillingRecord.PaymentStatus.SUCCESS);
        record2.setTotalAmount(BigDecimal.valueOf(150000));
        record2.setPaymentCompletedAt(LocalDateTime.now().minusMonths(1));
        billingRecordRepository.save(record2);

        // When
        List<Object[]> monthlyStats = billingRecordRepository.getMonthlyRevenueStats(sixMonthsAgo);

        // Then
        assertThat(monthlyStats).isNotEmpty();
        
        // 각 결과가 [year, month, revenue] 형태인지 확인
        for (Object[] stat : monthlyStats) {
            assertThat(stat).hasSize(3);
            assertThat(stat[0]).isInstanceOf(Integer.class); // year
            assertThat(stat[1]).isInstanceOf(Integer.class); // month
            assertThat(stat[2]).isInstanceOf(BigDecimal.class); // revenue
        }
    }

    private Tenant createAndSaveTenant(String companyName) {
        // 각 테스트마다 고유한 사업자번호 생성 (현재 시간 기반)
        String uniqueBusinessNo = String.format("%03d-%02d-%05d", 
            System.currentTimeMillis() % 1000,
            (int)(Math.random() * 100),
            (int)(Math.random() * 100000));
            
        Tenant tenant = new Tenant();
        tenant.setCompanyName(companyName);
        tenant.setBusinessNo(uniqueBusinessNo);
        tenant.setRepresentativeName("김대표");
        tenant.setEmail("test@company.com");
        tenant.setPhoneNumber("02-1234-5678");
        tenant.setRoadAddress("서울시 강남구");
        tenant.setPlanId("STANDARD");
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    private BillingRecord createAndSaveBillingRecord(Long tenantId, BillingRecord.PaymentStatus status) {
        BillingRecord billingRecord = new BillingRecord();
        billingRecord.setTenantId(tenantId);
        billingRecord.setSubscriptionPlan("STANDARD");
        billingRecord.setBillingDate(LocalDate.now());
        billingRecord.setBillingPeriodStart(LocalDate.now().minusMonths(1));
        billingRecord.setBillingPeriodEnd(LocalDate.now());
        billingRecord.setBaseAmount(BigDecimal.valueOf(50000));
        billingRecord.setTaxAmount(BigDecimal.valueOf(5000));
        billingRecord.setTotalAmount(BigDecimal.valueOf(55000));
        billingRecord.setPaymentStatus(status);
        billingRecord.setPaymentMethod(BillingRecord.PaymentMethod.CARD);
        if (status == BillingRecord.PaymentStatus.SUCCESS) {
            billingRecord.setPaymentCompletedAt(LocalDateTime.now());
        }
        return billingRecordRepository.save(billingRecord);
    }
}