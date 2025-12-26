package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.Notification;
import com.smartcon.domain.subscription.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 리포지토리
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 특정 사용자의 알림 조회 (최신순)
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    /**
     * 특정 사용자의 읽지 않은 알림 조회
     */
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
    
    /**
     * 특정 사용자의 읽지 않은 알림 개수 조회
     */
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    
    /**
     * 특정 타입의 알림 조회
     */
    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);
    
    /**
     * 특정 관련 엔티티의 알림 조회
     */
    List<Notification> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(
            String relatedEntityType, Long relatedEntityId);
    
    /**
     * 특정 사용자의 모든 알림을 읽음으로 표시
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId, 
                                   @Param("readAt") LocalDateTime readAt);
    
    /**
     * 특정 기간보다 오래된 읽은 알림 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n " +
           "WHERE n.isRead = true AND n.readAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 특정 기간 내 알림 조회
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * 알림 타입별 통계 조회
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY n.type " +
           "ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationStatsByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}