import React from 'react';
import { Bell, BellRing, CheckCircle, XCircle, AlertTriangle, Ban, Trash2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { NotificationType } from '@/types/notification';

interface NotificationIconProps {
  type: NotificationType;
  className?: string;
  size?: number;
}

/**
 * 알림 타입별 아이콘을 표시하는 컴포넌트
 */
export const NotificationIcon: React.FC<NotificationIconProps> = ({
  type,
  className,
  size = 16
}) => {
  const getIcon = () => {
    switch (type) {
      case 'SUBSCRIPTION_REQUEST':
        return <Bell size={size} className="text-blue-500" />;
      case 'SUBSCRIPTION_APPROVED':
        return <CheckCircle size={size} className="text-green-500" />;
      case 'SUBSCRIPTION_REJECTED':
        return <XCircle size={size} className="text-red-500" />;
      case 'SUBSCRIPTION_SUSPENDED':
        return <Ban size={size} className="text-orange-500" />;
      case 'SUBSCRIPTION_TERMINATED':
        return <Trash2 size={size} className="text-red-600" />;
      case 'APPROVAL_REMINDER':
        return <AlertTriangle size={size} className="text-yellow-500" />;
      default:
        return <BellRing size={size} className="text-gray-500" />;
    }
  };

  return (
    <div className={cn("flex items-center justify-center", className)}>
      {getIcon()}
    </div>
  );
};

interface NotificationBadgeProps {
  count: number;
  className?: string;
}

/**
 * 읽지 않은 알림 개수를 표시하는 배지 컴포넌트
 */
export const NotificationBadge: React.FC<NotificationBadgeProps> = ({
  count,
  className
}) => {
  if (count === 0) return null;

  return (
    <span className={cn(
      "absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1",
      className
    )}>
      {count > 99 ? '99+' : count}
    </span>
  );
};