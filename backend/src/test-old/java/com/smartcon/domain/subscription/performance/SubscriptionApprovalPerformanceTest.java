package com.smartcon.domain.subscription.performance;

import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.subscription.service.SubscriptionApprovalService;
import com.smartcon.domain.subscription.service.SubscriptionStatsService;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 구독 승인 성능 테스트
 * 
 * 대량 데이터 처리 성능과 동시 요청 처리 성능을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
class SubscriptionApprovalPerformanceTest {
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private SubscriptionApprovalService subscriptionApprovalService;
    
    @Autowired
    private SubscriptionStatsService subscriptionStatsService;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private Tenant testTenant;
    private User testAdmin;
    
    @BeforeEach
    void setUp() {
        // 테스트용 테넌트 생성
        testTenant = new Tenant();
        testTenant.setCompanyName("성능테스트회사");
        testTenant.setRepresentativeName("테스트대표");
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setEmail("test@performance.com");
        testTenant.setPhoneNumber("010-1234-5678");
        testTenant = tenantRepository.save(testTenant);
        
        // 테스트용 관리자 생성
        testAdmin = new User();
        testAdmin.setTenantId(testTenant.getId());
        testAdmin.setEmail("admin@performance.com");
        testAdmin.setName("성능테스트관리자");
        testAdmin.setIsActive(true);
        testAdmin = userRepository.save(testAdmin);
        
        // 테넌트 컨텍스트 설정
        TenantContext.setCurrentTenantId(testTenant.getId());
    }
    
    @Test
    void testLargeDatasetPendingApprovalsList() {
        // Given: 대량의 승인 대기 구독 데이터 생성
        log.info("대량 데이터 생성 시작");
        long startTime = System.currentTimeMillis();
        
        List<Subscription> subscriptions = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Subscription subscription = new Subscription();
            subscription.setTenantId(testTenant.getId());
            subscription.setStatus(SubscriptionStatus.PENDING_APPROVAL);
            // approvalRequestedAt 필드가 없으므로 createdAt 사용
            subscriptions.add(subscription);
            
            // 배치로 저장 (메모리 효율성)
            if (i % 1000 == 999) {
                subscriptionRepository.saveAll(subscriptions);
                subscriptions.clear();
                log.debug("배치 저장 완료: {} 개", i + 1);
            }
        }
        
        if (!subscriptions.isEmpty()) {
            subscriptionRepository.saveAll(subscriptions);
        }
        
        long dataCreationTime = System.currentTimeMillis() - startTime;
        log.info("대량 데이터 생성 완료: {} ms", dataCreationTime);
        
        // When: 승인 대기 목록 조회 성능 측정
        startTime = System.currentTimeMillis();
        
        // 최적화된 쿼리 사용
        List<Object[]> results = subscriptionRepository.findPendingApprovalsOptimized(100, 0);
        
        long queryTime = System.currentTimeMillis() - startTime;
        log.info("승인 대기 목록 조회 완료: {} ms, 결과 수: {}", queryTime, results.size());
        
        // Then: 성능 검증
        assertThat(queryTime).isLessThan(1000); // 1초 이내
        assertThat(results).hasSize(100);
        
        // 페이지네이션 성능 테스트
        startTime = System.currentTimeMillis();
        
        for (int page = 0; page < 10; page++) {
            subscriptionRepository.findPendingApprovalsOptimized(100, page * 100);
        }
        
        long paginationTime = System.currentTimeMillis() - startTime;
        log.info("페이지네이션 성능 테스트 완료: {} ms (10 페이지)", paginationTime);
        
        assertThat(paginationTime).isLessThan(2000); // 2초 이내
    }
    
    @Test
    void testConcurrentApprovalProcessing() throws InterruptedException {
        // Given: 동시 처리할 구독 데이터 생성
        List<Subscription> subscriptions = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Subscription subscription = new Subscription();
            subscription.setTenantId(testTenant.getId());
            subscription.setStatus(SubscriptionStatus.PENDING_APPROVAL);
            subscriptions.add(subscription);
        }
        subscriptionRepository.saveAll(subscriptions);
        
        // When: 동시 승인 처리 성능 측정
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (Subscription subscription : subscriptions) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    subscriptionApprovalService.approveSubscription(subscription.getId(), "성능 테스트 승인");
                } catch (Exception e) {
                    log.error("동시 승인 처리 실패: {}", subscription.getId(), e);
                }
            }, executor);
            futures.add(future);
        }
        
        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long concurrentProcessingTime = System.currentTimeMillis() - startTime;
        log.info("동시 승인 처리 완료: {} ms (100개 구독)", concurrentProcessingTime);
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Then: 성능 및 정확성 검증
        assertThat(concurrentProcessingTime).isLessThan(10000); // 10초 이내
        
        // 모든 구독이 승인되었는지 확인
        long approvedCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        assertThat(approvedCount).isEqualTo(100);
    }
    
    @Test
    void testDashboardStatsPerformance() {
        // Given: 다양한 상태의 구독 데이터 생성
        createSubscriptionsWithVariousStatuses(5000);
        
        // When: 대시보드 통계 조회 성능 측정
        long startTime = System.currentTimeMillis();
        
        // 첫 번째 호출 (캐시 없음)
        subscriptionStatsService.getSubscriptionStats();
        
        long firstCallTime = System.currentTimeMillis() - startTime;
        log.info("첫 번째 통계 조회 (캐시 없음): {} ms", firstCallTime);
        
        // 두 번째 호출 (캐시 있음)
        startTime = System.currentTimeMillis();
        
        subscriptionStatsService.getSubscriptionStats();
        
        long secondCallTime = System.currentTimeMillis() - startTime;
        log.info("두 번째 통계 조회 (캐시 있음): {} ms", secondCallTime);
        
        // Then: 캐시 효과 검증
        assertThat(firstCallTime).isLessThan(2000); // 첫 번째 호출은 2초 이내
        assertThat(secondCallTime).isLessThan(100);  // 캐시된 호출은 100ms 이내
        assertThat(secondCallTime).isLessThan(firstCallTime / 10); // 캐시로 인한 10배 이상 성능 향상
    }
    
    @Test
    void testMemoryUsageUnderLoad() throws InterruptedException {
        // Given: 메모리 사용량 측정 시작
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        log.info("초기 메모리 사용량: {} MB", initialMemory / 1024 / 1024);
        
        // When: 대량 데이터 처리
        createSubscriptionsWithVariousStatuses(20000);
        
        // 여러 통계 조회 수행
        for (int i = 0; i < 10; i++) {
            subscriptionStatsService.getSubscriptionStats();
            subscriptionStatsService.getMonthlyApprovalStats();
            subscriptionStatsService.getNotificationStats();
        }
        
        // 가비지 컬렉션 수행
        System.gc();
        Thread.sleep(1000);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        log.info("최종 메모리 사용량: {} MB", finalMemory / 1024 / 1024);
        log.info("메모리 증가량: {} MB", memoryIncrease / 1024 / 1024);
        
        // Then: 메모리 사용량 검증
        assertThat(memoryIncrease).isLessThan(500 * 1024 * 1024); // 500MB 이내 증가
    }
    
    @Test
    void testResponseTimeUnderHighLoad() throws InterruptedException {
        // Given: 고부하 상황 시뮬레이션을 위한 데이터 준비
        createSubscriptionsWithVariousStatuses(1000);
        
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Long> responseTimes = new ArrayList<>();
        
        // When: 고부하 상황에서 응답 시간 측정
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                
                try {
                    // 다양한 조회 작업 수행
                    subscriptionApprovalService.getPendingApprovalsOptimized(20, 0);
                    subscriptionStatsService.getSubscriptionStats();
                    
                    long responseTime = System.currentTimeMillis() - startTime;
                    synchronized (responseTimes) {
                        responseTimes.add(responseTime);
                    }
                } catch (Exception e) {
                    log.error("고부하 테스트 중 오류 발생", e);
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        
        // Then: 응답 시간 분석
        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        
        log.info("평균 응답 시간: {} ms", avgResponseTime);
        log.info("최대 응답 시간: {} ms", maxResponseTime);
        
        assertThat(avgResponseTime).isLessThan(1000); // 평균 1초 이내
        assertThat(maxResponseTime).isLessThan(5000);  // 최대 5초 이내
    }
    
    /**
     * 다양한 상태의 구독 데이터 생성
     */
    private void createSubscriptionsWithVariousStatuses(int totalCount) {
        log.info("다양한 상태의 구독 데이터 생성 시작: {} 개", totalCount);
        
        List<Subscription> subscriptions = new ArrayList<>();
        SubscriptionStatus[] statuses = SubscriptionStatus.values();
        
        for (int i = 0; i < totalCount; i++) {
            SubscriptionStatus status = statuses[i % statuses.length];
            
            Subscription subscription = new Subscription();
            subscription.setTenantId(testTenant.getId());
            subscription.setStatus(status);
            
            subscriptions.add(subscription);
            
            // 배치로 저장
            if (i % 1000 == 999) {
                subscriptionRepository.saveAll(subscriptions);
                subscriptions.clear();
            }
        }
        
        if (!subscriptions.isEmpty()) {
            subscriptionRepository.saveAll(subscriptions);
        }
        
        log.info("다양한 상태의 구독 데이터 생성 완료");
    }
}