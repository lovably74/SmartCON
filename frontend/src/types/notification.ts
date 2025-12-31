/**
 * 알림 시스템 관련 타입 정의
 */

export type NotificationType = 
  | 'SUBSCRIPTION_REQUEST'
  | 'SUBSCRIPTION_APPROVED'
  | 'SUBSCRIPTION_REJECTED'
  | 'SUBSCRIPTION_SUSPENDED'
  | 'SUBSCRIPTION_TERMINATED'
  | 'APPROVAL_REMINDER';

export interface Notification {
  id: number;
  recipientId: number;
  type: NotificationType;
  title: string;
  message: string;
  relatedEntityType?: string;
  relatedEntityId?: number;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

export interface NotificationStats {
  totalCount: number;
  unreadCount: number;
}

export interface NotificationFilter {
  type?: NotificationType;
  isRead?: boolean;
  dateFrom?: string;
  dateTo?: string;
}

export interface NotificationPageParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  filter?: NotificationFilter;
}