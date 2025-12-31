package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.NotificationDto;
import com.smartcon.domain.subscription.dto.NotificationStatsDto;
import com.smartcon.domain.subscription.dto.DailyNotificationStatsDto;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.NotificationRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 알림 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void sendSubscriptionRequestNotification(Long subscriptionId) {
        log.info("구독 신청 알림 발송 (비동기) - 구독 ID: {}", subscriptionId);
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
            
            // 모든 슈퍼관리자에게 알림 발송
            List<User> superAdmins = userRepository.findSuperAdmins();
            
            for (User admin : superAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(NotificationType.SUBSCRIPTION_REQUEST)
                        .title("새로운 구독 신청")
                        .message(String.format("%s에서 %s 요금제 구독을 신청했습니다.", 
                                subscription.getTenant().getCompanyName(),
                                subscription.getPlan().getName()))
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("구독 신청 알림 발송 완료 - {} 명의 관리자에게 발송", superAdmins.size());
        } catch (Exception e) {
            log.error("구독 신청 알림 발송 실패 - 구독 ID: {}", subscriptionId, e);
        }
    }
    
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void sendApprovalResultNotification(Long subscriptionId, boolean approved, String reason) {
        log.info("승인 결과 알림 발송 (비동기) - 구독 ID: {}, 승인 여부: {}", subscriptionId, approved);
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
            
            // 테넌트의 관리자들에게 알림 발송
            List<User> tenantAdmins = userRepository.findByTenantId(subscription.getTenant().getId());
            
            NotificationType type = approved ? NotificationType.SUBSCRIPTION_APPROVED : NotificationType.SUBSCRIPTION_REJECTED;
            String title = approved ? "구독 승인 완료" : "구독 승인 거부";
            String message = approved ? 
                    String.format("%s 요금제 구독이 승인되었습니다. 이제 서비스를 이용하실 수 있습니다.", subscription.getPlan().getName()) :
                    String.format("%s 요금제 구독이 거부되었습니다. 사유: %s", subscription.getPlan().getName(), reason);
            
            for (User admin : tenantAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(type)
                        .title(title)
                        .message(message)
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("승인 결과 알림 발송 완료 - {} 명의 테넌트 관리자에게 발송", tenantAdmins.size());
        } catch (Exception e) {
            log.error("승인 결과 알림 발송 실패 - 구독 ID: {}", subscriptionId, e);
        }
    }
    
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void sendReminderNotifications() {
        log.info("승인 대기 리마인더 알림 발송 시작 (비동기)");
        
        try {
            // 24시간 이상 대기 중인 구독 조회
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            List<Subscription> pendingSubscriptions = subscriptionRepository
                    .findPendingSubscriptionsOlderThan(cutoffTime);
            
            if (pendingSubscriptions.isEmpty()) {
                log.info("승인 대기 중인 구독이 없습니다");
                return;
            }
            
            // 슈퍼관리자들에게 리마인더 발송
            List<User> superAdmins = userRepository.findSuperAdmins();
            
            for (Subscription subscription : pendingSubscriptions) {
                for (User admin : superAdmins) {
                    Notification notification = Notification.builder()
                            .recipient(admin)
                            .type(NotificationType.APPROVAL_REMINDER)
                            .title("승인 대기 리마인더")
                            .message(String.format("%s의 %s 요금제 구독이 24시간 이상 승인 대기 중입니다.", 
                                    subscription.getTenant().getCompanyName(),
                                    subscription.getPlan().getName()))
                            .relatedEntityType("Subscription")
                            .relatedEntityId(subscription.getId())
                            .build();
                    
                    notificationRepository.save(notification);
                }
            }
            
            log.info("승인 대기 리마인더 알림 발송 완료 - {} 개 구독, {} 명 관리자", 
                    pendingSubscriptions.size(), superAdmins.size());
        } catch (Exception e) {
            log.error("승인 대기 리마인더 알림 발송 실패", e);
        }
    }
    
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void sendSubscriptionSuspendedNotification(Long subscriptionId, String reason) {
        log.info("구독 중지 알림 발송 (비동기) - 구독 ID: {}", subscriptionId);
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
            
            List<User> tenantAdmins = userRepository.findByTenantId(subscription.getTenant().getId());
            
            for (User admin : tenantAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(NotificationType.SUBSCRIPTION_SUSPENDED)
                        .title("구독 서비스 중지")
                        .message(String.format("구독 서비스가 중지되었습니다. 사유: %s", reason))
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("구독 중지 알림 발송 완료");
        } catch (Exception e) {
            log.error("구독 중지 알림 발송 실패 - 구독 ID: {}", subscriptionId, e);
        }
    }
    
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void sendSubscriptionTerminatedNotification(Long subscriptionId, String reason) {
        log.info("구독 종료 알림 발송 (비동기) - 구독 ID: {}", subscriptionId);
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
            
            List<User> tenantAdmins = userRepository.findByTenantId(subscription.getTenant().getId());
            
            for (User admin : tenantAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(NotificationType.SUBSCRIPTION_TERMINATED)
                        .title("구독 서비스 종료")
                        .message(String.format("구독 서비스가 종료되었습니다. 사유: %s", reason))
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("구독 종료 알림 발송 완료");
        } catch (Exception e) {
            log.error("구독 종료 알림 발송 실패 - 구독 ID: {}", subscriptionId, e);
        }
    }
    
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void sendSubscriptionReactivatedNotification(Long subscriptionId) {
        log.info("구독 재활성화 알림 발송 (비동기) - 구독 ID: {}", subscriptionId);
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다"));
            
            List<User> tenantAdmins = userRepository.findByTenantId(subscription.getTenant().getId());
            
            for (User admin : tenantAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(NotificationType.SUBSCRIPTION_REACTIVATED)
                        .title("구독 서비스 재활성화")
                        .message("구독 서비스가 재활성화되었습니다. 이제 서비스를 다시 이용하실 수 있습니다.")
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("구독 재활성화 알림 발송 완료");
        } catch (Exception e) {
            log.error("구독 재활성화 알림 발송 실패 - 구독 ID: {}", subscriptionId, e);
        }
    }
    
    @Override
    public Page<NotificationDto> getNotifications(Long userId, Pageable pageable) {
        log.debug("사용자 알림 조회 - 사용자 ID: {}", userId);
        
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(NotificationDto::from);
    }
    
    @Override
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        log.debug("읽지 않은 알림 조회 - 사용자 ID: {}", userId);
        
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
    }
    
    @Override
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("알림 읽음 처리 - 알림 ID: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다"));
        
        notification.markAsRead();
        notificationRepository.save(notification);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("모든 알림 읽음 처리 - 사용자 ID: {}", userId);
        
        int updatedCount = notificationRepository.markAllAsReadByRecipientId(userId, LocalDateTime.now());
        
        log.info("알림 읽음 처리 완료 - {} 개 알림 처리", updatedCount);
    }
    
    @Override
    @Async("taskExecutor")
    @Transactional
    public void cleanupOldNotifications() {
        log.info("오래된 알림 정리 시작 (비동기)");
        
        try {
            // 30일 이전에 읽은 알림들을 배치로 삭제
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int totalDeleted = 0;
            int batchSize = 1000;
            int deletedCount;
            
            do {
                deletedCount = notificationRepository.deleteOldReadNotificationsBatch(cutoffDate, batchSize);
                totalDeleted += deletedCount;
                log.debug("배치 삭제 완료: {} 개 알림", deletedCount);
                
                // 배치 간 잠시 대기 (DB 부하 방지)
                if (deletedCount > 0) {
                    Thread.sleep(100);
                }
            } while (deletedCount > 0);
            
            log.info("오래된 알림 정리 완료 - {} 개 알림 삭제", totalDeleted);
        } catch (Exception e) {
            log.error("오래된 알림 정리 실패", e);
        }
    }
    
    // =============================================================================
    // 성능 최적화된 메서드들
    // =============================================================================
    
    /**
     * 사용자 알림 조회 (성능 최적화)
     */
    public List<NotificationDto> getNotificationsOptimized(Long userId, int limit, int offset) {
        List<Object[]> results = notificationRepository.findNotificationsByRecipientOptimized(userId, limit, offset);
        
        return results.stream()
                .map(this::convertToNotificationDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 읽지 않은 알림 조회 (성능 최적화)
     */
    public List<NotificationDto> getUnreadNotificationsOptimized(Long userId, int limit) {
        List<Notification> notifications = notificationRepository.findUnreadNotificationsOptimized(userId, limit);
        
        return notifications.stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 읽지 않은 알림 개수 조회 (성능 최적화)
     */
    public long getUnreadNotificationCountOptimized(Long userId) {
        return notificationRepository.countUnreadNotificationsOptimized(userId);
    }
    
    /**
     * 알림 발송 통계 조회 (성능 최적화)
     */
    public List<NotificationStatsDto> getNotificationStatsOptimized() {
        List<Object[]> results = notificationRepository.getNotificationStatsOptimized();
        
        return results.stream()
                .map(this::convertToNotificationStatsDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 커서 기반 페이지네이션을 위한 알림 조회
     */
    public List<NotificationDto> getNotificationsCursorBased(Long userId, Long cursorId, int limit) {
        List<Notification> notifications = notificationRepository.findNotificationsCursorBased(userId, cursorId, limit);
        
        return notifications.stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 일별 알림 발송 통계 조회
     */
    public List<DailyNotificationStatsDto> getDailyNotificationStats() {
        List<Object[]> results = notificationRepository.getDailyNotificationStats();
        
        return results.stream()
                .map(this::convertToDailyNotificationStatsDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 관련 엔티티별 알림 조회 (성능 최적화)
     */
    public List<NotificationDto> getNotificationsByRelatedEntityOptimized(String entityType, Long entityId, int limit) {
        List<Notification> notifications = notificationRepository.findByRelatedEntityOptimized(entityType, entityId, limit);
        
        return notifications.stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 대량 읽음 처리 (성능 최적화)
     */
    @Transactional
    public CompletableFuture<Integer> markMultipleAsReadAsync(Long userId, List<Long> notificationIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int updatedCount = notificationRepository.markMultipleAsRead(userId, notificationIds, LocalDateTime.now());
                log.info("대량 읽음 처리 완료 - 사용자 ID: {}, 처리 건수: {}", userId, updatedCount);
                return updatedCount;
            } catch (Exception e) {
                log.error("대량 읽음 처리 실패 - 사용자 ID: {}", userId, e);
                return 0;
            }
        });
    }
    
    // =============================================================================
    // 변환 메서드들
    // =============================================================================
    
    /**
     * Object[] 배열을 NotificationDto로 변환
     */
    private NotificationDto convertToNotificationDto(Object[] result) {
        return NotificationDto.builder()
                .id((Long) result[0])
                .type(NotificationType.valueOf((String) result[1]))
                .title((String) result[2])
                .message((String) result[3])
                .relatedEntityType((String) result[4])
                .relatedEntityId((Long) result[5])
                .isRead((Boolean) result[6])
                .createdAt((LocalDateTime) result[7])
                .readAt((LocalDateTime) result[8])
                .build();
    }
    
    /**
     * Object[] 배열을 NotificationStatsDto로 변환
     */
    private NotificationStatsDto convertToNotificationStatsDto(Object[] result) {
        return NotificationStatsDto.builder()
                .type(NotificationType.valueOf((String) result[0]))
                .totalSent(((Number) result[1]).longValue())
                .totalRead(((Number) result[2]).longValue())
                .readRate(((Number) result[3]).doubleValue())
                .avgReadTimeMinutes(result[4] != null ? ((Number) result[4]).doubleValue() : 0.0)
                .build();
    }
    
    /**
     * Object[] 배열을 DailyNotificationStatsDto로 변환
     */
    private DailyNotificationStatsDto convertToDailyNotificationStatsDto(Object[] result) {
        return DailyNotificationStatsDto.builder()
                .notificationDate(((java.sql.Date) result[0]).toLocalDate())
                .type(NotificationType.valueOf((String) result[1]))
                .totalSent(((Number) result[2]).longValue())
                .totalRead(((Number) result[3]).longValue())
                .build();
    }
}