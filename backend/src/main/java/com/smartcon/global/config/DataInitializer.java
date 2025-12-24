package com.smartcon.global.config;

import com.smartcon.domain.billing.entity.BillingRecord;
import com.smartcon.domain.billing.repository.BillingRecordRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 개발용 테스트 데이터 초기화 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final BillingRecordRepository billingRecordRepository;

    @Override
    public void run(String... args) throws Exception {
        if (tenantRepository.count() == 0) {
            log.info("테스트 데이터 초기화 시작");
            initializeTestData();
            log.info("테스트 데이터 초기화 완료");
        } else {
            log.info("기존 데이터가 존재하여 초기화를 건너뜁니다");
        }
    }

    private void initializeTestData() {
        // 테넌트 데이터 생성
        List<Tenant> tenants = createTenants();
        tenantRepository.saveAll(tenants);

        // 사용자 데이터 생성
        List<User> users = createUsers(tenants);
        userRepository.saveAll(users);

        // 결제 데이터 생성
        List<BillingRecord> billingRecords = createBillingRecords(tenants);
        billingRecordRepository.saveAll(billingRecords);
    }

    private List<Tenant> createTenants() {
        String[] companyNames = {
            "대한건설", "현대건축", "삼성토목", "LG건설", "SK건설",
            "포스코건설", "GS건설", "롯데건설", "한화건설", "두산건설",
            "태영건설", "대우건설", "코오롱건설", "금호건설", "계룡건설"
        };

        String[] representativeNames = {
            "김철수", "이영희", "박민수", "정수진", "최동훈",
            "강미영", "윤성호", "임지현", "오준석", "한소영",
            "배정민", "신혜원", "조성우", "노은정", "서태준"
        };

        Tenant.SubscriptionStatus[] statuses = Tenant.SubscriptionStatus.values();
        Random random = new Random();

        return Arrays.stream(companyNames)
                .map(companyName -> {
                    Tenant tenant = new Tenant();
                    tenant.setBusinessNo(generateBusinessNo());
                    tenant.setCompanyName(companyName);
                    tenant.setRepresentativeName(representativeNames[random.nextInt(representativeNames.length)]);
                    tenant.setStatus(statuses[random.nextInt(statuses.length)]);
                    tenant.setPlanId("BASIC_PLAN");
                    
                    // 생성일을 랜덤하게 설정 (최근 6개월 내)
                    LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(180));
                    tenant.setCreatedAt(createdAt);
                    tenant.setUpdatedAt(createdAt);
                    
                    return tenant;
                })
                .toList();
    }

    private List<User> createUsers(List<Tenant> tenants) {
        String[] names = {
            "관리자", "현장소장", "팀장", "작업자1", "작업자2",
            "안전관리자", "품질관리자", "기술자", "노무자", "운전기사"
        };

        Random random = new Random();

        return tenants.stream()
                .flatMap(tenant -> Arrays.stream(names)
                        .map(name -> {
                            User user = new User();
                            user.setTenantId(tenant.getId());
                            user.setName(name);
                            user.setEmail(name.toLowerCase() + "@" + tenant.getCompanyName().toLowerCase() + ".com");
                            
                            // 생성일을 테넌트 생성일 이후로 설정
                            LocalDateTime createdAt = tenant.getCreatedAt().plusDays(random.nextInt(30));
                            user.setCreatedAt(createdAt);
                            user.setUpdatedAt(createdAt);
                            
                            return user;
                        }))
                .toList();
    }

    private List<BillingRecord> createBillingRecords(List<Tenant> tenants) {
        BillingRecord.PaymentStatus[] statuses = BillingRecord.PaymentStatus.values();
        BillingRecord.PaymentMethod[] methods = BillingRecord.PaymentMethod.values();
        Random random = new Random();

        return tenants.stream()
                .flatMap(tenant -> {
                    // 각 테넌트당 3-8개의 결제 기록 생성
                    int recordCount = 3 + random.nextInt(6);
                    return java.util.stream.IntStream.range(0, recordCount)
                            .mapToObj(i -> {
                                BillingRecord record = new BillingRecord();
                                record.setTenantId(tenant.getId());
                                record.setTotalAmount(BigDecimal.valueOf(50000 + random.nextInt(200000))); // 5만원 ~ 25만원
                                record.setPaymentStatus(statuses[random.nextInt(statuses.length)]);
                                record.setPaymentMethod(methods[random.nextInt(methods.length)]);
                                record.setPgTransactionId("TXN" + System.currentTimeMillis() + random.nextInt(1000));
                                
                                if (record.getPaymentStatus() == BillingRecord.PaymentStatus.FAILED) {
                                    record.setFailureReason("카드 한도 초과");
                                }
                                
                                // 결제일을 테넌트 생성일 이후로 설정
                                LocalDateTime billingDateTime = tenant.getCreatedAt().plusDays(random.nextInt(150));
                                record.setBillingDate(billingDateTime.toLocalDate());
                                record.setCreatedAt(billingDateTime);
                                record.setUpdatedAt(billingDateTime);
                                
                                if (record.getPaymentStatus() == BillingRecord.PaymentStatus.SUCCESS) {
                                    record.setPaymentCompletedAt(billingDateTime.plusHours(random.nextInt(24)));
                                }
                                
                                // 필수 필드들 설정
                                record.setBillingPeriodStart(billingDateTime.toLocalDate().minusMonths(1));
                                record.setBillingPeriodEnd(billingDateTime.toLocalDate());
                                record.setSubscriptionPlan("BASIC_PLAN");
                                record.setPlanAmount(BigDecimal.valueOf(50000));
                                record.setBaseAmount(BigDecimal.valueOf(50000));
                                
                                return record;
                            });
                })
                .toList();
    }

    private String generateBusinessNo() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        // 사업자번호 형식: XXX-XX-XXXXX
        for (int i = 0; i < 3; i++) {
            sb.append(random.nextInt(10));
        }
        sb.append("-");
        for (int i = 0; i < 2; i++) {
            sb.append(random.nextInt(10));
        }
        sb.append("-");
        for (int i = 0; i < 5; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
}