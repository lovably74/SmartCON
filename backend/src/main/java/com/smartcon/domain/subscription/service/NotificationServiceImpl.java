package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.NotificationDto;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.NotificationRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    @Transactional
    public void sendSubscriptionRequestNotification(Long subscriptionId) {
        log.info("구독 신청 알림 발송 - 구독 ID: {}", subscriptionId);
        
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
    }
    
    @Override
    @Transactional
    public void sendApprovalResultNotification(Long subscriptionId, boolean approved, String reason) {
        log.info("승인 결과 알림 발송 - 구독 ID: {}, 승인 여부: {}", subscriptionId, approved);
        
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
    }
    
    @Override
    @Transactional
    public void sendReminderNotifications() {
        log.info("승인 대기 리마인더 알림 발송 시작");
        
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
    }
    
    @Override
    @Transactional
    public void sendSubscriptionSuspendedNotification(Long subscriptionId, String reason) {
        log.info("구독 중지 알림 발송 - 구독 ID: {}", subscriptionId);
        
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
    }
    
    @Override
    @Transactional
    public void sendSubscriptionTerminatedNotification(Long subscriptionId, String reason) {
        log.info("구독 종료 알림 발송 - 구독 ID: {}", subscriptionId);
        
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
    }
    
    @Override
    @Transactional
    public void sendSubscriptionReactivatedNotification(Long subscriptionId) {
        log.info("구독 재활성화 알림 발송 - 구독 ID: {}", subscriptionId);
        
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
    @Transactional
    public void cleanupOldNotifications() {
        log.info("오래된 알림 정리 시작");
        
        // 30일 이전에 읽은 알림들 삭제
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate);
        
        log.info("오래된 알림 정리 완료 - {} 개 알림 삭제", deletedCount);
    }
}