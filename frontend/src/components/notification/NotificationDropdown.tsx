import React, { useState } from 'react';
import { Bell } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { NotificationList } from './NotificationList';
import { NotificationBadge } from '@/components/ui/notification-icon';
import type { Notification } from '@/types/notification';

interface NotificationDropdownProps {
  notifications: Notification[];
  unreadCount: number;
  loading?: boolean;
  onMarkAsRead?: (notificationId: number) => void;
  onMarkAllAsRead?: () => void;
  onNotificationClick?: (notification: Notification) => void;
  onLoadMore?: () => void;
  hasMore?: boolean;
}

/**
 * 헤더에 표시되는 알림 드롭다운 컴포넌트
 */
export const NotificationDropdown: React.FC<NotificationDropdownProps> = ({
  notifications,
  unreadCount,
  loading = false,
  onMarkAsRead,
  onMarkAllAsRead,
  onNotificationClick,
  onLoadMore,
  hasMore = false
}) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleNotificationClick = (notification: Notification) => {
    if (onNotificationClick) {
      onNotificationClick(notification);
    }
    setIsOpen(false); // 드롭다운 닫기
  };

  return (
    <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          className="relative p-2 hover:bg-gray-100"
          aria-label={`알림 ${unreadCount > 0 ? `(${unreadCount}개 읽지 않음)` : ''}`}
        >
          <Bell className="w-5 h-5" />
          <NotificationBadge count={unreadCount} />
        </Button>
      </DropdownMenuTrigger>
      
      <DropdownMenuContent
        align="end"
        className="w-96 p-0"
        sideOffset={8}
      >
        <NotificationList
          notifications={notifications}
          unreadCount={unreadCount}
          loading={loading}
          onMarkAsRead={onMarkAsRead}
          onMarkAllAsRead={onMarkAllAsRead}
          onNotificationClick={handleNotificationClick}
          onLoadMore={onLoadMore}
          hasMore={hasMore}
          className="border-0 shadow-none"
        />
      </DropdownMenuContent>
    </DropdownMenu>
  );
};