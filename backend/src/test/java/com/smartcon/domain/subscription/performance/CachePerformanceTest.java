package com.smartcon.domain.subscription.performance;

import com.smartcon.domain.subscription.dto.AutoApprovalRuleDto;
import com.smartcon.domain.subscription.dto.SubscriptionStatsDto;
import com.smartcon.domain.subscription.entity.AutoApprovalRule;
import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.AutoApprovalRuleRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.subscription.service.AutoApprovalRuleService;
import com.smartcon.domain.subscription.service.SubscriptionStatsService;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 캐시 성능 테스트
 * 
 * 캐시 적용 전후의 성능 차이와 캐시 효율성을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
class CachePerformanceTest {
    
    @Autowired
    private SubscriptionStatsService subscriptionStatsService;
    
    @Autowired
    private AutoApprovalRuleService autoApprovalRuleService;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private AutoApprovalRuleRepository autoApprovalRuleRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private CacheManager cacheManager;
    
    private Tenant testTenant;
    
    @BeforeEach
    void setUp() {
        // 캐시 초기화
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).clear();
            }
        });
        
        // 테스트용 테넌트 생성
        testTenant = Tenant.builder()
                .companyName("캐시테스트회사")
                .ceoName("테스트대표")
                .businessNumber("123-45-67890")
                .contactEmail("test@cache.com")
                .contactPhone("010-1234-5678")
                .build();
        testTenant = tenantRepository.save(testTenant);
    }
    
    @Test
    void testSubscriptionStatsCachePerformance() {
        // Given: 대량의 구독 데이터 생성
        createSubscriptionsWithVariousStatuses(10000);
        
        // When: 캐시 적용 전후 성능 비교
        log.info("구독 통계 캐시 성능 테스트 시작");
        
        // 첫 번째 호출 (캐시 없음)
        long startTime = System.currentTimeMillis();
        SubscriptionStatsDto stats1 = subscriptionStatsService.getSubscriptionStats();
        long firstCallTime = System.currentTimeMillis() - startTime;
        
        // 두 번째 호출 (캐시 있음)
        startTime = System.currentTimeMillis();
        SubscriptionStatsDto stats2 = subscriptionStatsService.getSubscriptionStats();
        long secondCallTime = System.currentTimeMillis() - startTime;
        
        // 세 번째 호출 (캐시 있음)
        startTime = System.currentTimeMillis();
        SubscriptionStatsDto stats3 = subscriptionStatsService.getSubscriptionStats();
        long thirdCallTime = System.currentTimeMillis() - startTime;
        
        log.info("첫 번째 호출 (캐시 없음): {} ms", firstCallTime);
        log.info("두 번째 호출 (캐시 있음): {} ms", secondCallTime);
        log.info("세 번째 호출 (캐시 있음): {} ms", thirdCallTime);
        
        // Then: 캐시 효과 검증
        assertThat(stats1).isEqualTo(stats2).isEqualTo(stats3); // 결과 일관성
        assertThat(firstCallTime).isGreaterThan(100); // 첫 번째 호출은 시간이 걸림
        assertThat(secondCallTime).isLessThan(50);    // 캐시된 호출은 빠름
        assertThat(thirdCallTime).isLessThan(50);     // 캐시된 호출은 빠름
        
        // 캐시로 인한 성능 향상 비율 계산
        double improvementRatio = (double) firstCallTime / secondCallTime;
        log.info("캐시 성능 향상 비율: {}배", improvementRatio);
        assertThat(improvementRatio).isGreaterThan(5.0); // 최소 5배 이상 성능 향상
    }
    
    @Test
    void testAutoApprovalRulesCachePerformance() {
        // Given: 자동 승인 규칙 데이터 생성
        createAutoApprovalRules(100);
        
        // When: 캐시 적용 전후 성능 비교
        log.info("자동 승인 규칙 캐시 성능 테스트 시작");
        
        // 첫 번째 호출 (캐시 없음)
        long startTime = System.currentTimeMillis();
        List<AutoApprovalRuleDto> rules1 = autoApprovalRuleService.getActiveRules();
        long firstCallTime = System.currentTimeMillis() - startTime;
        
        // 두 번째 호출 (캐시 있음)
        startTime = System.currentTimeMillis();
        List<AutoApprovalRuleDto> rules2 = autoApprovalRuleService.getActiveRules();
        long secondCallTime = System.currentTimeMillis() - startTime;
        
        log.info("첫 번째 호출 (캐시 없음): {} ms", firstCallTime);
        log.info("두 번째 호출 (캐시 있음): {} ms", secondCallTime);
        
        // Then: 캐시 효과 검증
        assertThat(rules1).hasSize(rules2.size()); // 결과 일관성
        assertThat(secondCallTime).isLessThan(firstCallTime / 3); // 최소 3배 이상 성능 향상
    }
    
    @Test
    void testConcurrentCacheAccess() throws InterruptedException {
        // Given: 테스트 데이터 준비
        createSubscriptionsWithVariousStatuses(5000);
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // When: 동시 캐시 접근 성능 측정
        log.info("동시 캐시 접근 성능 테스트 시작");
        
        for (int i = 0; i < 100; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                
                // 다양한 캐시된 메서드 호출
                subscriptionStatsService.getSubscriptionStats();
                subscriptionStatsService.getMonthlyApprovalStats();
                autoApprovalRuleService.getActiveRules();
                
                return System.currentTimeMillis() - startTime;
            }, executor);
            
            futures.add(future);
        }
        
        // 모든 작업 완료 대기
        List<Long> accessTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            accessTimes.add(future.join());
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Then: 동시 접근 성능 검증
        double avgAccessTime = accessTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        long maxAccessTime = accessTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        
        log.info("평균 캐시 접근 시간: {} ms", avgAccessTime);
        log.info("최대 캐시 접근 시간: {} ms", maxAccessTime);
        
        assertThat(avgAccessTime).isLessThan(100); // 평균 100ms 이내
        assertThat(maxAccessTime).isLessThan(500); // 최대 500ms 이내
    }
    
    @Test
    void testCacheEvictionPerformance() {
        // Given: 캐시된 데이터 준비
        createSubscriptionsWithVariousStatuses(1000);
        createAutoApprovalRules(50);
        
        // 캐시 워밍업
        subscriptionStatsService.getSubscriptionStats();
        autoApprovalRuleService.getActiveRules();
        
        // When: 캐시 무효화 성능 측정
        log.info("캐시 무효화 성능 테스트 시작");
        
        long startTime = System.currentTimeMillis();
        
        // 자동 승인 규칙 수정 (캐시 무효화 트리거)
        AutoApprovalRuleDto newRule = AutoApprovalRuleDto.builder()
                .ruleName("캐시 테스트 규칙")
                .isActive(true)
                .planIds(List.of("basic"))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(BigDecimal.valueOf(100000))
                .priority(1)
                .build();
        
        autoApprovalRuleService.createRule(newRule);
        
        long evictionTime = System.currentTimeMillis() - startTime;
        log.info("캐시 무효화 시간: {} ms", evictionTime);
        
        // 캐시 재구성 시간 측정
        startTime = System.currentTimeMillis();
        autoApprovalRuleService.getActiveRules();
        long rebuildTime = System.currentTimeMillis() - startTime;
        log.info("캐시 재구성 시간: {} ms", rebuildTime);
        
        // Then: 캐시 무효화 성능 검증
        assertThat(evictionTime).isLessThan(1000); // 1초 이내
        assertThat(rebuildTime).isLessThan(500);   // 500ms 이내
    }
    
    @Test
    void testCacheMemoryUsage() {
        // Given: 메모리 사용량 측정 시작
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // 가비지 컬렉션 수행
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        log.info("초기 메모리 사용량: {} MB", initialMemory / 1024 / 1024);
        
        // When: 대량 데이터로 캐시 채우기
        createSubscriptionsWithVariousStatuses(20000);
        createAutoApprovalRules(200);
        
        // 다양한 통계 데이터 캐싱
        for (int i = 0; i < 10; i++) {
            subscriptionStatsService.getSubscriptionStats();
            subscriptionStatsService.getMonthlyApprovalStats();
            subscriptionStatsService.getAutoApprovalEfficiencyStats();
            autoApprovalRuleService.getActiveRules();
        }
        
        System.gc(); // 가비지 컬렉션 수행
        Thread.sleep(1000);
        
        long cachedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = cachedMemory - initialMemory;
        
        log.info("캐시 적용 후 메모리 사용량: {} MB", cachedMemory / 1024 / 1024);
        log.info("캐시로 인한 메모리 증가량: {} MB", memoryIncrease / 1024 / 1024);
        
        // Then: 메모리 사용량 검증
        assertThat(memoryIncrease).isLessThan(100 * 1024 * 1024); // 100MB 이내 증가
        
        // 캐시 효율성 검증 (메모리 대비 성능 향상)
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            subscriptionStatsService.getSubscriptionStats();
        }
        long cachedCallsTime = System.currentTimeMillis() - startTime;
        
        log.info("캐시된 호출 100회 시간: {} ms", cachedCallsTime);
        assertThat(cachedCallsTime).isLessThan(1000); // 1초 이내
    }
    
    /**
     * 다양한 상태의 구독 데이터 생성
     */
    private void createSubscriptionsWithVariousStatuses(int totalCount) {
        List<Subscription> subscriptions = new ArrayList<>();
        SubscriptionStatus[] statuses = SubscriptionStatus.values();
        
        for (int i = 0; i < totalCount; i++) {
            SubscriptionStatus status = statuses[i % statuses.length];
            
            Subscription subscription = Subscription.builder()
                    .tenant(testTenant)
                    .status(status)
                    .approvalRequestedAt(LocalDateTime.now().minusHours(i % 168))
                    .build();
            
            subscriptions.add(subscription);
            
            if (i % 1000 == 999) {
                subscriptionRepository.saveAll(subscriptions);
                subscriptions.clear();
            }
        }
        
        if (!subscriptions.isEmpty()) {
            subscriptionRepository.saveAll(subscriptions);
        }
    }
    
    /**
     * 자동 승인 규칙 데이터 생성
     */
    private void createAutoApprovalRules(int count) {
        List<AutoApprovalRule> rules = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            AutoApprovalRule rule = AutoApprovalRule.builder()
                    .ruleName(String.format("테스트 규칙 %d", i))
                    .isActive(i % 2 == 0) // 절반만 활성화
                    .planIds("[\"basic\", \"standard\"]")
                    .verifiedTenantsOnly(false)
                    .paymentMethods("[\"CARD\"]")
                    .maxAmount(BigDecimal.valueOf(100000 + i * 1000))
                    .priority(i)
                    .build();
            
            rules.add(rule);
        }
        
        autoApprovalRuleRepository.saveAll(rules);
    }
}