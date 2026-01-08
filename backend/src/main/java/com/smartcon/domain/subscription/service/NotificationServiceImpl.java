package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.NotificationDto;
import com.smartcon.domain.subscription.entity.Notification;
import com.smartcon.domain.subscription.entity.NotificationType;
import com.smartcon.domain.subscription.entity.Subscription;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 알림 서비스 구현체
 * JWT 인증 시스템 테스트를 위한 기본 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Override
    public void sendSubscriptionRequestNotification(Long subscriptionId) {
        log.info("구독 신청 알림 발송 - 구독 ID: {}", subscriptionId);
        
        if (subscriptionId == null) {
            log.warn("구독 ID가 null입니다. 알림을 발송하지 않습니다.");
            return;
        }

        try {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                log.warn("존재하지 않는 구독입니다 - ID: {}", subscriptionId);
                return;
            }

            Subscription subscription = subscriptionOpt.get();
            
            // 슈퍼관리자들에게 알림 발송
            List<User> superAdmins = userRepository.findByRole(User.Role.ROLE_SUPER);
            
            for (User admin : superAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(NotificationType.SUBSCRIPTION_REQUEST)
                        .title("새로운 구독 신청")
                        .message(String.format("새로운 구독 신청이 있습니다. (구독 ID: %d)", subscriptionId))
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
                log.debug("구독 신청 알림 생성 완료 - 수신자: {}", admin.getEmail());
            }
            
        } catch (Exception e) {
            log.error("구독 신청 알림 발송 중 오류 발생 - 구독 ID: {}", subscriptionId, e);
        }
    }

    @Override
    public void sendApprovalResultNotification(Long subscriptionId, boolean approved, String reason) {
        log.info("승인 결과 알림 발송 - 구독 ID: {}, 승인 여부: {}", subscriptionId, approved);
        
        if (subscriptionId == null) {
            log.warn("구독 ID가 null입니다. 알림을 발송하지 않습니다.");
            return;
        }

        try {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                log.warn("존재하지 않는 구독입니다 - ID: {}", subscriptionId);
                return;
            }

            Subscription subscription = subscriptionOpt.get();
            
            // 테넌트의 관리자들에게 알림 발송
            List<User> tenantAdmins = userRepository.findByTenantIdAndRole(
                    subscription.getTenant().getId(), User.Role.ROLE_HQ);
            
            NotificationType notificationType = approved ? 
                    NotificationType.SUBSCRIPTION_APPROVED : 
                    NotificationType.SUBSCRIPTION_REJECTED;
            
            String title = approved ? "구독 승인 완료" : "구독 신청 거부";
            String message = approved ? 
                    String.format("구독이 승인되었습니다. %s", reason) :
                    String.format("구독 신청이 거부되었습니다. 사유: %s", reason);
            
            for (User admin : tenantAdmins) {
                Notification notification = Notification.builder()
                        .recipient(admin)
                        .type(notificationType)
                        .title(title)
                        .message(message)
                        .relatedEntityType("Subscription")
                        .relatedEntityId(subscriptionId)
                        .build();
                
                notificationRepository.save(notification);
                log.debug("승인 결과 알림 생성 완료 - 수신자: {}", admin.getEmail());
            }
            
        } catch (Exception e) {
            log.error("승인 결과 알림 발송 중 오류 발생 - 구독 ID: {}", subscriptionId, e);
        }
    }

    @Override
    public void sendReminderNotifications() {
        log.info("리마인더 알림 발송 시작");
        
        try {
            // 승인 대기 중인 구독들에 대한 리마인더
            List<Subscription> pendingSubscriptions = subscriptionRepository.findPendingApprovalSubscriptions();
            
            for (Subscription subscription : pendingSubscriptions) {
                // 7일 이상 대기 중인 구독에 대해서만 리마인더 발송
                if (subscription.getApprovalRequestedAt() != null &&
                    subscription.getApprovalRequestedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                    
                    List<User> superAdmins = userRepository.findByRole(User.Role.ROLE_SUPER);
                    
                    for (User admin : superAdmins) {
                        Notification notification = Notification.builder()
                                .recipient(admin)
                                .type(NotificationType.APPROVAL_REMINDER)
                                .title("구독 승인 대기 리마인더")
                                .message(String.format("구독 승인이 7일 이상 대기 중입니다. (구독 ID: %d)", 
                                        subscription.getId()))
                                .relatedEntityType("Subscription")
                                .relatedEntityId(subscription.getId())
                                .build();
                        
                        notificationRepository.save(notification);
                    }
                }
            }
            
            log.info("리마인더 알림 발송 완료");
            
        } catch (Exception e) {
            log.error("리마인더 알림 발송 중 오류 발생", e);
        }
    }

    @Override
    public void sendSubscriptionSuspendedNotification(Long subscriptionId, String reason) {
        log.info("구독 중지 알림 발송 - 구독 ID: {}", subscriptionId);
        sendStatusChangeNotification(subscriptionId, NotificationType.SUBSCRIPTION_SUSPENDED, 
                "구독 중지 안내", String.format("구독이 중지되었습니다. 사유: %s", reason));
    }

    @Override
    public void sendSubscriptionTerminatedNotification(Long subscriptionId, String reason) {
        log.info("구독 종료 알림 발송 - 구독 ID: {}", subscriptionId);
        sendStatusChangeNotification(subscriptionId, NotificationType.SUBSCRIPTION_TERMINATED, 
                "구독 종료 안내", String.format("구독이 종료되었습니다. 사유: %s", reason));
    }

    @Override
    public void sendSubscriptionReactivatedNotification(Long subscriptionId) {
        log.info("구독 재활성화 알림 발송 - 구독 ID: {}", subscriptionId);
        sendStatusChangeNotification(subscriptionId, NotificationType.SUBSCRIPTION_REACTIVATED, 
                "구독 재활성화 안내", "구독이 재활성화되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(Long userId, Pageable pageable) {
        log.debug("사용자 알림 조회 - 사용자 ID: {}", userId);
        
        Page<Notification> notifications = notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        log.debug("읽지 않은 알림 조회 - 사용자 ID: {}", userId);
        
        List<Notification> notifications = notificationRepository.findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        log.debug("읽지 않은 알림 개수 조회 - 사용자 ID: {}", userId);
        
        return notificationRepository.countByRecipient_IdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        log.debug("알림 읽음 처리 - 알림 ID: {}", notificationId);
        
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        log.debug("모든 알림 읽음 처리 - 사용자 ID: {}", userId);
        
        List<Notification> unreadNotifications = notificationRepository.findByRecipient_IdAndIsReadFalse(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void cleanupOldNotifications() {
        log.info("오래된 알림 정리 시작");
        
        try {
            // 30일 이상 된 읽은 알림들 삭제
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate);
            
            log.info("오래된 알림 {} 건 정리 완료", deletedCount);
            
        } catch (Exception e) {
            log.error("오래된 알림 정리 중 오류 발생", e);
        }
    }

    /**
     * 상태 변경 알림 발송 공통 메서드
     */
    private void sendStatusChangeNotification(Long subscriptionId, NotificationType type, String title, String message) {
        if (subscriptionId == null) {
            log.warn("구독 ID가 null입니다. 알림을 발송하지 않습니다.");
            return;
        }

        try {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                log.warn("존재하지 않는 구독입니다 - ID: {}", subscriptionId);
                return;
            }

            Subscription subscription = subscriptionOpt.get();
            
            // 테넌트의 관리자들에게 알림 발송
            List<User> tenantAdmins = userRepository.findByTenantIdAndRole(
                    subscription.getTenant().getId(), User.Role.ROLE_HQ);
            
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
                log.debug("상태 변경 알림 생성 완료 - 수신자: {}", admin.getEmail());
            }
            
        } catch (Exception e) {
            log.error("상태 변경 알림 발송 중 오류 발생 - 구독 ID: {}", subscriptionId, e);
        }
    }

    /**
     * Notification 엔티티를 DTO로 변환
     */
    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}