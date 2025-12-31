import { create } from 'zustand';
import { apiClient } from '@/lib/api';
import type { Notification, NotificationStats, NotificationPageParams } from '@/types/notification';

interface NotificationState {
  // 상태
  notifications: Notification[];
  stats: NotificationStats;
  loading: boolean;
  error: string | null;
  hasMore: boolean;
  currentPage: number;
  
  // 액션
  fetchNotifications: (params?: NotificationPageParams) => Promise<void>;
  loadMoreNotifications: () => Promise<void>;
  fetchStats: () => Promise<void>;
  markAsRead: (notificationId: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  deleteNotification: (notificationId: number) => Promise<void>;
  addNotification: (notification: Notification) => void;
  clearError: () => void;
  reset: () => void;
}

const initialState = {
  notifications: [],
  stats: { totalCount: 0, unreadCount: 0 },
  loading: false,
  error: null,
  hasMore: false,
  currentPage: 0,
};

export const useNotificationStore = create<NotificationState>((set, get) => ({
  ...initialState,

  fetchNotifications: async (params?: NotificationPageParams) => {
    set({ loading: true, error: null });
    
    try {
      const response = await apiClient.getNotifications({
        page: 0,
        size: 20,
        sortBy: 'createdAt',
        sortDir: 'desc',
        ...params,
      });

      if (response.success) {
        set({
          notifications: response.data.content,
          hasMore: !response.data.last,
          currentPage: response.data.number,
          loading: false,
        });
      } else {
        set({ error: response.message, loading: false });
      }
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '알림을 불러오는데 실패했습니다.',
        loading: false 
      });
    }
  },

  loadMoreNotifications: async () => {
    const { currentPage, loading, hasMore } = get();
    
    if (loading || !hasMore) return;

    set({ loading: true });

    try {
      const response = await apiClient.getNotifications({
        page: currentPage + 1,
        size: 20,
        sortBy: 'createdAt',
        sortDir: 'desc',
      });

      if (response.success) {
        set(state => ({
          notifications: [...state.notifications, ...response.data.content],
          hasMore: !response.data.last,
          currentPage: response.data.number,
          loading: false,
        }));
      } else {
        set({ error: response.message, loading: false });
      }
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '알림을 불러오는데 실패했습니다.',
        loading: false 
      });
    }
  },

  fetchStats: async () => {
    try {
      const response = await apiClient.getNotificationStats();
      
      if (response.success) {
        set({ stats: response.data });
      }
    } catch (error) {
      console.error('알림 통계 조회 실패:', error);
    }
  },

  markAsRead: async (notificationId: number) => {
    try {
      const response = await apiClient.markNotificationAsRead(notificationId);
      
      if (response.success) {
        set(state => ({
          notifications: state.notifications.map(notification =>
            notification.id === notificationId
              ? { ...notification, isRead: true, readAt: new Date().toISOString() }
              : notification
          ),
          stats: {
            ...state.stats,
            unreadCount: Math.max(0, state.stats.unreadCount - 1)
          }
        }));
      }
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  },

  markAllAsRead: async () => {
    try {
      const response = await apiClient.markAllNotificationsAsRead();
      
      if (response.success) {
        const now = new Date().toISOString();
        set(state => ({
          notifications: state.notifications.map(notification => ({
            ...notification,
            isRead: true,
            readAt: notification.readAt || now
          })),
          stats: {
            ...state.stats,
            unreadCount: 0
          }
        }));
      }
    } catch (error) {
      console.error('모든 알림 읽음 처리 실패:', error);
    }
  },

  deleteNotification: async (notificationId: number) => {
    try {
      const response = await apiClient.deleteNotification(notificationId);
      
      if (response.success) {
        set(state => {
          const deletedNotification = state.notifications.find(n => n.id === notificationId);
          const wasUnread = deletedNotification && !deletedNotification.isRead;
          
          return {
            notifications: state.notifications.filter(n => n.id !== notificationId),
            stats: {
              totalCount: Math.max(0, state.stats.totalCount - 1),
              unreadCount: wasUnread ? Math.max(0, state.stats.unreadCount - 1) : state.stats.unreadCount
            }
          };
        });
      }
    } catch (error) {
      console.error('알림 삭제 실패:', error);
    }
  },

  addNotification: (notification: Notification) => {
    set(state => ({
      notifications: [notification, ...state.notifications],
      stats: {
        totalCount: state.stats.totalCount + 1,
        unreadCount: notification.isRead ? state.stats.unreadCount : state.stats.unreadCount + 1
      }
    }));
  },

  clearError: () => {
    set({ error: null });
  },

  reset: () => {
    set(initialState);
  },
}));