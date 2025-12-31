package com.smartcon.domain.subscription.performance;

import com.smartcon.domain.subscription.entity.Notification;
import com.smartcon.domain.subscription.entity.NotificationType;
import com.smartcon.domain.subscription.repository.NotificationRepository;
import com.smartcon.domain.subscription.service.NotificationService;
import com.smartcon.domain.subscription.service.NotificationServiceImpl;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 * 알림 시스템 성능 테스트
 * 
 * 대량 알림 발송 성능과 동시 처리 성능을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
class NotificationPerformanceTest {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationServiceImpl notificationServiceImpl;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    private List<User> testUsers;
    private Tenant testTenant;
    
    @BeforeEach
    void setUp() {
        // 테스트용 테넌트 생성
        testTenant = Tenant.builder()
                .companyName("알림테스트회사")
                .ceoName("테스트대표")
                .businessNumber("123-45-67890")
                .contactEmail("test@notification.com")
                .contactPhone("010-1234-5678")
                .build();
        testTenant = tenantRepository.save(testTenant);
        
        // 테스트용 사용자들 생성
        testUsers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user = User.builder()
                    .tenant(testTenant)
                    .email(String.format("user%d@notification.com", i))
                    .name(String.format("테스트사용자%d", i))
                    .isActive(true)
                    .build();
            testUsers.add(user);
        }
        userRepository.saveAll(testUsers);
    }
    
    @Test
    void testBulkNotificationCreation() {
        // Given: 대량 알림 생성 준비
        log.info("대량 알림 생성 성능 테스트 시작");
        
        // When: 대량 알림 생성 성능 측정
        long startTime = System.currentTimeMillis();
        
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            User recipient = testUsers.get(i % testUsers.size());
            
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .type(NotificationType.SUBSCRIPTION_REQUEST)
                    .title("성능 테스트 알림")
                    .message(String.format("성능 테스트용 알림 메시지 %d", i))
                    .relatedEntityType("Test")
                    .relatedEntityId((long) i)
                    .build();
            
            notifications.add(notification);
            
            // 배치로 저장 (메모리 효율성)
            if (i % 1000 == 999) {
                notificationRepository.saveAll(notifications);
                notifications.clear();
                log.debug("배치 저장 완료: {} 개", i + 1);
            }
        }
        
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
        
        long creationTime = System.currentTimeMillis() - startTime;
        log.info("대량 알림 생성 완료: {} ms (10,000개)", creationTime);
        
        // Then: 성능 검증
        assertThat(creationTime).isLessThan(5000); // 5초 이내
        
        long totalNotifications = notificationRepository.count();
        assertThat(totalNotifications).isEqualTo(10000);
    }
    
    @Test
    void testConcurrentNotificationQueries() throws InterruptedException {
        // Given: 테스트용 알림 데이터 생성
        createTestNotifications(5000);
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // When: 동시 알림 조회 성능 측정
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            final int userIndex = i % testUsers.size();
            
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                User user = testUsers.get(userIndex);
                
                // 다양한 조회 작업 수행
                long queryStartTime = System.currentTimeMillis();
                
                // 읽지 않은 알림 개수 조회 (최적화된 쿼리)
                long unreadCount = notificationServiceImpl.getUnreadNotificationCountOptimized(user.getId());
                
                // 알림 목록 조회 (최적화된 쿼리)
                notificationServiceImpl.getNotificationsOptimized(user.getId(), 20, 0);
                
                return System.currentTimeMillis() - queryStartTime;
            }, executor);
            
            futures.add(future);
        }
        
        // 모든 작업 완료 대기
        List<Long> queryTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            queryTimes.add(future.join());
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("동시 알림 조회 완료: {} ms (100개 동시 요청)", totalTime);
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Then: 성능 검증
        double avgQueryTime = queryTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        long maxQueryTime = queryTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        
        log.info("평균 쿼리 시간: {} ms", avgQueryTime);
        log.info("최대 쿼리 시간: {} ms", maxQueryTime);
        
        assertThat(totalTime).isLessThan(10000); // 전체 10초 이내
        assertThat(avgQueryTime).isLessThan(500); // 평균 500ms 이내
        assertThat(maxQueryTime).isLessThan(2000); // 최대 2초 이내
    }
    
    @Test
    void testNotificationPaginationPerformance() {
        // Given: 대량 알림 데이터 생성
        createTestNotifications(20000);
        User testUser = testUsers.get(0);
        
        // When: 페이지네이션 성능 측정
        log.info("알림 페이지네이션 성능 테스트 시작");
        
        // 일반 페이지네이션 (OFFSET 기반)
        long startTime = System.currentTimeMillis();
        
        for (int page = 0; page < 20; page++) {
            notificationServiceImpl.getNotificationsOptimized(testUser.getId(), 100, page * 100);
        }
        
        long offsetPaginationTime = System.currentTimeMillis() - startTime;
        log.info("OFFSET 기반 페이지네이션: {} ms (20 페이지)", offsetPaginationTime);
        
        // 커서 기반 페이지네이션
        startTime = System.currentTimeMillis();
        
        Long cursorId = null;
        for (int page = 0; page < 20; page++) {
            List<com.smartcon.domain.subscription.dto.NotificationDto> notifications = 
                    notificationServiceImpl.getNotificationsCursorBased(testUser.getId(), cursorId, 100);
            
            if (!notifications.isEmpty()) {
                cursorId = notifications.get(notifications.size() - 1).getId();
            }
        }
        
        long cursorPaginationTime = System.currentTimeMillis() - startTime;
        log.info("커서 기반 페이지네이션: {} ms (20 페이지)", cursorPaginationTime);
        
        // Then: 성능 비교 검증
        assertThat(offsetPaginationTime).isLessThan(3000); // 3초 이내
        assertThat(cursorPaginationTime).isLessThan(2000); // 2초 이내
        assertThat(cursorPaginationTime).isLessThan(offsetPaginationTime); // 커서 기반이 더 빠름
    }
    
    @Test
    void testBulkNotificationMarkAsRead() throws InterruptedException {
        // Given: 읽지 않은 알림 대량 생성
        createTestNotifications(10000);
        User testUser = testUsers.get(0);
        
        // 해당 사용자의 알림 ID 목록 조회
        List<Notification> userNotifications = notificationRepository
                .findUnreadNotificationsOptimized(testUser.getId(), 5000);
        
        List<Long> notificationIds = userNotifications.stream()
                .map(Notification::getId)
                .toList();
        
        log.info("대량 읽음 처리 테스트 시작: {} 개 알림", notificationIds.size());
        
        // When: 대량 읽음 처리 성능 측정
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Integer> future = notificationServiceImpl
                .markMultipleAsReadAsync(testUser.getId(), notificationIds);
        
        Integer updatedCount = future.get(10, TimeUnit.SECONDS);
        
        long bulkUpdateTime = System.currentTimeMillis() - startTime;
        log.info("대량 읽음 처리 완료: {} ms, 처리 건수: {}", bulkUpdateTime, updatedCount);
        
        // Then: 성능 및 정확성 검증
        assertThat(bulkUpdateTime).isLessThan(3000); // 3초 이내
        assertThat(updatedCount).isEqualTo(notificationIds.size());
        
        // 실제로 읽음 처리되었는지 확인
        long remainingUnreadCount = notificationServiceImpl
                .getUnreadNotificationCountOptimized(testUser.getId());
        assertThat(remainingUnreadCount).isLessThan(notificationIds.size());
    }
    
    @Test
    void testNotificationCleanupPerformance() {
        // Given: 오래된 읽은 알림 데이터 생성
        List<Notification> oldNotifications = new ArrayList<>();
        
        for (int i = 0; i < 5000; i++) {
            User recipient = testUsers.get(i % testUsers.size());
            
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .type(NotificationType.SUBSCRIPTION_APPROVED)
                    .title("오래된 알림")
                    .message("정리 대상 알림")
                    .isRead(true)
                    .readAt(LocalDateTime.now().minusDays(35)) // 35일 전에 읽음
                    .build();
            
            oldNotifications.add(notification);
        }
        
        notificationRepository.saveAll(oldNotifications);
        
        long initialCount = notificationRepository.count();
        log.info("정리 전 알림 총 개수: {}", initialCount);
        
        // When: 알림 정리 성능 측정
        long startTime = System.currentTimeMillis();
        
        notificationService.cleanupOldNotifications();
        
        // 비동기 처리 완료 대기
        Thread.sleep(2000);
        
        long cleanupTime = System.currentTimeMillis() - startTime;
        long finalCount = notificationRepository.count();
        
        log.info("알림 정리 완료: {} ms", cleanupTime);
        log.info("정리 후 알림 총 개수: {}", finalCount);
        log.info("정리된 알림 개수: {}", initialCount - finalCount);
        
        // Then: 성능 및 정확성 검증
        assertThat(cleanupTime).isLessThan(5000); // 5초 이내
        assertThat(finalCount).isLessThan(initialCount); // 일부 알림이 정리됨
    }
    
    /**
     * 테스트용 알림 데이터 생성
     */
    private void createTestNotifications(int count) {
        log.info("테스트용 알림 데이터 생성 시작: {} 개", count);
        
        List<Notification> notifications = new ArrayList<>();
        NotificationType[] types = NotificationType.values();
        
        for (int i = 0; i < count; i++) {
            User recipient = testUsers.get(i % testUsers.size());
            NotificationType type = types[i % types.length];
            
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .type(type)
                    .title(String.format("테스트 알림 %d", i))
                    .message(String.format("테스트용 알림 메시지 %d", i))
                    .relatedEntityType("Test")
                    .relatedEntityId((long) i)
                    .isRead(i % 3 == 0) // 1/3은 읽음 처리
                    .readAt(i % 3 == 0 ? LocalDateTime.now().minusHours(i % 24) : null)
                    .build();
            
            notifications.add(notification);
            
            // 배치로 저장
            if (i % 1000 == 999) {
                notificationRepository.saveAll(notifications);
                notifications.clear();
            }
        }
        
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
        
        log.info("테스트용 알림 데이터 생성 완료");
    }
}