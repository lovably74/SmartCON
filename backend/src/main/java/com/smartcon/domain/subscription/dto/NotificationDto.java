package com.smartcon.domain.subscription.dto;

import com.smartcon.domain.subscription.entity.Notification;
import com.smartcon.domain.subscription.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 알림 DTO
 */
@Data
@Builder
public class NotificationDto {
    
    private Long id;
    private Long recipientId;
    private String recipientName;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    /**
     * 엔티티를 DTO로 변환
     */
    public static NotificationDto from(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient().getId())
                .recipientName(notification.getRecipient().getName())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}