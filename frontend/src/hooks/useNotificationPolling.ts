import { useEffect, useRef, useCallback } from 'react';
import { useNotificationStore } from '@/stores/notificationStore';

interface UseNotificationPollingOptions {
  enabled?: boolean;
  interval?: number; // 폴링 간격 (밀리초)
  onNewNotification?: (notification: any) => void;
}

/**
 * 실시간 알림 업데이트를 위한 폴링 훅
 */
export const useNotificationPolling = (options: UseNotificationPollingOptions = {}) => {
  const {
    enabled = true,
    interval = 30000, // 30초 기본값
    onNewNotification
  } = options;

  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const lastCheckRef = useRef<string | null>(null);
  
  const { fetchStats, stats } = useNotificationStore();

  const startPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }

    intervalRef.current = setInterval(async () => {
      try {
        // 통계만 먼저 확인하여 새 알림이 있는지 체크
        await fetchStats();
        
        // 새 알림이 있으면 전체 목록 새로고침
        const currentStats = useNotificationStore.getState().stats;
        if (currentStats.unreadCount > stats.unreadCount) {
          const { fetchNotifications } = useNotificationStore.getState();
          await fetchNotifications();
          
          if (onNewNotification) {
            onNewNotification(currentStats);
          }
        }
      } catch (error) {
        console.error('알림 폴링 중 오류:', error);
      }
    }, interval);
  }, [interval, fetchStats, stats.unreadCount, onNewNotification]);

  const stopPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }, []);

  useEffect(() => {
    if (enabled) {
      startPolling();
    } else {
      stopPolling();
    }

    return () => {
      stopPolling();
    };
  }, [enabled, startPolling, stopPolling]);

  // 페이지 가시성 변경 시 폴링 제어
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.hidden) {
        stopPolling();
      } else if (enabled) {
        startPolling();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [enabled, startPolling, stopPolling]);

  return {
    startPolling,
    stopPolling,
    isPolling: intervalRef.current !== null,
  };
};