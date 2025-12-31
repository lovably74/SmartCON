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
    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    /**
     * 특정 사용자의 읽지 않은 알림 조회
     */
    List<Notification> findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
    
    /**
     * 특정 사용자의 읽지 않은 알림 개수 조회
     */
    long countByRecipient_IdAndIsReadFalse(Long recipientId);
    
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
    
    // =============================================================================
    // 성능 최적화된 쿼리 메서드들
    // =============================================================================
    
    /**
     * 사용자 알림 조회 (성능 최적화)
     * 인덱스 활용: idx_notifications_recipient_read
     */
    @Query(value = "SELECT n.id, n.type, n.title, n.message, n.related_entity_type, " +
                   "n.related_entity_id, n.is_read, n.created_at, n.read_at " +
                   "FROM notifications n " +
                   "FORCE INDEX (idx_notifications_recipient_read) " +
                   "WHERE n.recipient_id = :recipientId " +
                   "ORDER BY n.created_at DESC " +
                   "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Object[]> findNotificationsByRecipientOptimized(
            @Param("recipientId") Long recipientId,
            @Param("limit") int limit,
            @Param("offset") int offset);
    
    /**
     * 읽지 않은 알림 조회 (성능 최적화)
     * 인덱스 활용: idx_notifications_recipient_read
     */
    @Query(value = "SELECT n.* FROM notifications n " +
                   "FORCE INDEX (idx_notifications_recipient_read) " +
                   "WHERE n.recipient_id = :recipientId AND n.is_read = FALSE " +
                   "ORDER BY n.created_at DESC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Notification> findUnreadNotificationsOptimized(
            @Param("recipientId") Long recipientId,
            @Param("limit") int limit);
    
    /**
     * 읽지 않은 알림 개수 조회 (성능 최적화)
     * 인덱스 활용: idx_notifications_recipient_read
     */
    @Query(value = "SELECT COUNT(*) FROM notifications " +
                   "FORCE INDEX (idx_notifications_recipient_read) " +
                   "WHERE recipient_id = :recipientId AND is_read = FALSE", 
           nativeQuery = true)
    long countUnreadNotificationsOptimized(@Param("recipientId") Long recipientId);
    
    /**
     * 알림 발송 통계 조회 (성능 최적화)
     * 인덱스 활용: idx_notifications_type, idx_notifications_created_at
     */
    @Query(value = "SELECT " +
                   "n.type, " +
                   "COUNT(*) as total_sent, " +
                   "COUNT(CASE WHEN n.is_read = TRUE THEN 1 END) as total_read, " +
                   "ROUND(COUNT(CASE WHEN n.is_read = TRUE THEN 1 END) * 100.0 / COUNT(*), 2) as read_rate, " +
                   "AVG(CASE WHEN n.read_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, n.created_at, n.read_at) END) as avg_read_time_minutes " +
                   "FROM notifications n " +
                   "WHERE n.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                   "GROUP BY n.type " +
                   "ORDER BY total_sent DESC", 
           nativeQuery = true)
    List<Object[]> getNotificationStatsOptimized();
    
    /**
     * 커서 기반 페이지네이션을 위한 알림 조회
     * 인덱스 활용: idx_notifications_cursor_pagination
     */
    @Query(value = "SELECT n.* FROM notifications n " +
                   "FORCE INDEX (idx_notifications_cursor_pagination) " +
                   "WHERE n.recipient_id = :recipientId " +
                   "AND (:cursorId IS NULL OR n.id > :cursorId) " +
                   "ORDER BY n.id ASC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Notification> findNotificationsCursorBased(
            @Param("recipientId") Long recipientId,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);
    
    /**
     * 일별 알림 발송 통계 조회 (성능 최적화)
     */
    @Query(value = "SELECT " +
                   "DATE(n.created_at) as notification_date, " +
                   "n.type, " +
                   "COUNT(*) as total_sent, " +
                   "COUNT(CASE WHEN n.is_read = TRUE THEN 1 END) as total_read " +
                   "FROM notifications n " +
                   "WHERE n.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                   "GROUP BY DATE(n.created_at), n.type " +
                   "ORDER BY notification_date DESC, n.type", 
           nativeQuery = true)
    List<Object[]> getDailyNotificationStats();
    
    /**
     * 관련 엔티티별 알림 조회 (성능 최적화)
     * 인덱스 활용: idx_notifications_entity_relation
     */
    @Query(value = "SELECT n.* FROM notifications n " +
                   "FORCE INDEX (idx_notifications_entity_relation) " +
                   "WHERE n.related_entity_type = :entityType " +
                   "AND n.related_entity_id = :entityId " +
                   "ORDER BY n.created_at DESC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Notification> findByRelatedEntityOptimized(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("limit") int limit);
    
    /**
     * 대량 읽음 처리 (성능 최적화)
     */
    @Modifying
    @Query(value = "UPDATE notifications " +
                   "SET is_read = TRUE, read_at = :readAt " +
                   "WHERE recipient_id = :recipientId " +
                   "AND is_read = FALSE " +
                   "AND id IN (:notificationIds)", 
           nativeQuery = true)
    int markMultipleAsRead(
            @Param("recipientId") Long recipientId,
            @Param("notificationIds") List<Long> notificationIds,
            @Param("readAt") LocalDateTime readAt);
    
    /**
     * 오래된 알림 정리 (성능 최적화)
     */
    @Modifying
    @Query(value = "DELETE FROM notifications " +
                   "WHERE is_read = TRUE " +
                   "AND read_at < :cutoffDate " +
                   "LIMIT :batchSize", 
           nativeQuery = true)
    int deleteOldReadNotificationsBatch(
            @Param("cutoffDate") LocalDateTime cutoffDate,
            @Param("batchSize") int batchSize);
}