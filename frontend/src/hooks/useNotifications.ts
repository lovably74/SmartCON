import { useEffect, useCallback } from 'react';
import { useNotificationStore } from '@/stores/notificationStore';
import { useNotificationPolling } from './useNotificationPolling';
import type { NotificationPageParams } from '@/types/notification';

interface UseNotificationsOptions {
  autoFetch?: boolean;
  enablePolling?: boolean;
  pollingInterval?: number;
  onNewNotification?: (stats: any) => void;
}

/**
 * 알림 시스템을 사용하기 위한 메인 훅
 */
export const useNotifications = (options: UseNotificationsOptions = {}) => {
  const {
    autoFetch = true,
    enablePolling = true,
    pollingInterval = 30000,
    onNewNotification
  } = options;

  const {
    notifications,
    stats,
    loading,
    error,
    hasMore,
    fetchNotifications,
    loadMoreNotifications,
    fetchStats,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearError,
    reset
  } = useNotificationStore();

  // 실시간 폴링 설정
  const { startPolling, stopPolling, isPolling } = useNotificationPolling({
    enabled: enablePolling,
    interval: pollingInterval,
    onNewNotification: (newStats) => {
      if (onNewNotification) {
        onNewNotification(newStats);
      }
    }
  });

  // 초기 데이터 로드
  useEffect(() => {
    if (autoFetch) {
      fetchNotifications();
      fetchStats();
    }
  }, [autoFetch, fetchNotifications, fetchStats]);

  // 알림 새로고침
  const refreshNotifications = useCallback(async (params?: NotificationPageParams) => {
    await fetchNotifications(params);
    await fetchStats();
  }, [fetchNotifications, fetchStats]);

  // 읽지 않은 알림만 조회
  const fetchUnreadNotifications = useCallback(async () => {
    await fetchNotifications({
      filter: { isRead: false },
      sortBy: 'createdAt',
      sortDir: 'desc'
    });
  }, [fetchNotifications]);

  // 알림 읽음 처리 (낙관적 업데이트)
  const handleMarkAsRead = useCallback(async (notificationId: number) => {
    await markAsRead(notificationId);
  }, [markAsRead]);

  // 모든 알림 읽음 처리
  const handleMarkAllAsRead = useCallback(async () => {
    await markAllAsRead();
  }, [markAllAsRead]);

  // 알림 삭제
  const handleDeleteNotification = useCallback(async (notificationId: number) => {
    await deleteNotification(notificationId);
  }, [deleteNotification]);

  // 더 많은 알림 로드
  const handleLoadMore = useCallback(async () => {
    if (!loading && hasMore) {
      await loadMoreNotifications();
    }
  }, [loading, hasMore, loadMoreNotifications]);

  // 에러 처리
  const handleClearError = useCallback(() => {
    clearError();
  }, [clearError]);

  // 정리
  const cleanup = useCallback(() => {
    stopPolling();
    reset();
  }, [stopPolling, reset]);

  return {
    // 상태
    notifications,
    stats,
    loading,
    error,
    hasMore,
    isPolling,
    
    // 읽지 않은 알림 개수 (편의성)
    unreadCount: stats.unreadCount,
    
    // 액션
    refreshNotifications,
    fetchUnreadNotifications,
    markAsRead: handleMarkAsRead,
    markAllAsRead: handleMarkAllAsRead,
    deleteNotification: handleDeleteNotification,
    loadMore: handleLoadMore,
    clearError: handleClearError,
    cleanup,
    
    // 폴링 제어
    startPolling,
    stopPolling,
  };
};