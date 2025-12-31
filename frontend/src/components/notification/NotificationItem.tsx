import React from 'react';
import { formatDistanceToNow } from 'date-fns';
import { ko } from 'date-fns/locale';
import { cn } from '@/lib/utils';
import { NotificationIcon } from '@/components/ui/notification-icon';
import type { Notification } from '@/types/notification';

interface NotificationItemProps {
  notification: Notification;
  onClick?: (notification: Notification) => void;
  onMarkAsRead?: (notificationId: number) => void;
  className?: string;
}

/**
 * 개별 알림 아이템을 표시하는 컴포넌트
 */
export const NotificationItem: React.FC<NotificationItemProps> = ({
  notification,
  onClick,
  onMarkAsRead,
  className
}) => {
  const handleClick = () => {
    if (!notification.isRead && onMarkAsRead) {
      onMarkAsRead(notification.id);
    }
    if (onClick) {
      onClick(notification);
    }
  };

  const formatTime = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), {
        addSuffix: true,
        locale: ko
      });
    } catch (error) {
      return dateString;
    }
  };

  return (
    <div
      className={cn(
        "flex items-start gap-3 p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors",
        !notification.isRead && "bg-blue-50 border-blue-100",
        className
      )}
      onClick={handleClick}
    >
      {/* 알림 아이콘 */}
      <div className="flex-shrink-0 mt-1">
        <NotificationIcon type={notification.type} size={20} />
      </div>

      {/* 알림 내용 */}
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <h4 className={cn(
            "text-sm font-medium text-gray-900 line-clamp-1",
            !notification.isRead && "font-semibold"
          )}>
            {notification.title}
          </h4>
          <span className="text-xs text-gray-500 flex-shrink-0">
            {formatTime(notification.createdAt)}
          </span>
        </div>
        
        <p className="text-sm text-gray-600 mt-1 line-clamp-2">
          {notification.message}
        </p>

        {/* 읽지 않은 알림 표시 */}
        {!notification.isRead && (
          <div className="flex items-center mt-2">
            <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
            <span className="text-xs text-blue-600 ml-2">읽지 않음</span>
          </div>
        )}
      </div>
    </div>
  );
};