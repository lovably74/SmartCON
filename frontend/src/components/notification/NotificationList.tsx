import React, { useState, useEffect } from 'react';
import { Bell, Settings, Check, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { NotificationItem } from './NotificationItem';
import { NotificationBadge } from '@/components/ui/notification-icon';
import type { Notification, NotificationFilter } from '@/types/notification';

interface NotificationListProps {
  notifications: Notification[];
  unreadCount: number;
  loading?: boolean;
  onMarkAsRead?: (notificationId: number) => void;
  onMarkAllAsRead?: () => void;
  onDeleteNotification?: (notificationId: number) => void;
  onNotificationClick?: (notification: Notification) => void;
  onLoadMore?: () => void;
  hasMore?: boolean;
  className?: string;
}

/**
 * 알림 목록을 표시하는 메인 컴포넌트
 */
export const NotificationList: React.FC<NotificationListProps> = ({
  notifications,
  unreadCount,
  loading = false,
  onMarkAsRead,
  onMarkAllAsRead,
  onDeleteNotification,
  onNotificationClick,
  onLoadMore,
  hasMore = false,
  className
}) => {
  const [activeTab, setActiveTab] = useState<'all' | 'unread'>('all');

  // 탭별 알림 필터링
  const filteredNotifications = notifications.filter(notification => {
    if (activeTab === 'unread') {
      return !notification.isRead;
    }
    return true;
  });

  // 빈 상태 렌더링
  const renderEmptyState = () => (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <Bell className="w-12 h-12 text-gray-300 mb-4" />
      <h3 className="text-lg font-medium text-gray-900 mb-2">
        {activeTab === 'unread' ? '읽지 않은 알림이 없습니다' : '알림이 없습니다'}
      </h3>
      <p className="text-gray-500">
        {activeTab === 'unread' 
          ? '모든 알림을 확인했습니다.' 
          : '새로운 알림이 도착하면 여기에 표시됩니다.'
        }
      </p>
    </div>
  );

  // 로딩 상태 렌더링
  const renderLoadingState = () => (
    <div className="space-y-4">
      {[...Array(3)].map((_, index) => (
        <div key={index} className="flex items-start gap-3 p-4 border-b border-gray-100">
          <div className="w-5 h-5 bg-gray-200 rounded-full animate-pulse"></div>
          <div className="flex-1 space-y-2">
            <div className="h-4 bg-gray-200 rounded animate-pulse"></div>
            <div className="h-3 bg-gray-200 rounded w-3/4 animate-pulse"></div>
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <Card className={className}>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Bell className="w-5 h-5" />
            알림
            {unreadCount > 0 && (
              <Badge variant="destructive" className="ml-2">
                {unreadCount}
              </Badge>
            )}
          </CardTitle>
          
          <div className="flex items-center gap-2">
            {unreadCount > 0 && onMarkAllAsRead && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onMarkAllAsRead}
                className="text-blue-600 hover:text-blue-700"
              >
                <Check className="w-4 h-4 mr-1" />
                모두 읽음
              </Button>
            )}
            <Button variant="ghost" size="sm">
              <Settings className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as 'all' | 'unread')}>
          <div className="px-6 pb-3">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="all" className="relative">
                전체
                {notifications.length > 0 && (
                  <Badge variant="secondary" className="ml-2">
                    {notifications.length}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="unread" className="relative">
                읽지 않음
                {unreadCount > 0 && (
                  <NotificationBadge count={unreadCount} className="ml-2 relative top-0 right-0" />
                )}
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent value="all" className="mt-0">
            {loading ? (
              renderLoadingState()
            ) : filteredNotifications.length === 0 ? (
              renderEmptyState()
            ) : (
              <div className="max-h-96 overflow-y-auto">
                {filteredNotifications.map((notification) => (
                  <NotificationItem
                    key={notification.id}
                    notification={notification}
                    onClick={onNotificationClick}
                    onMarkAsRead={onMarkAsRead}
                  />
                ))}
                
                {hasMore && onLoadMore && (
                  <div className="p-4 text-center border-t">
                    <Button
                      variant="ghost"
                      onClick={onLoadMore}
                      disabled={loading}
                    >
                      {loading ? '로딩 중...' : '더 보기'}
                    </Button>
                  </div>
                )}
              </div>
            )}
          </TabsContent>

          <TabsContent value="unread" className="mt-0">
            {loading ? (
              renderLoadingState()
            ) : filteredNotifications.length === 0 ? (
              renderEmptyState()
            ) : (
              <div className="max-h-96 overflow-y-auto">
                {filteredNotifications.map((notification) => (
                  <NotificationItem
                    key={notification.id}
                    notification={notification}
                    onClick={onNotificationClick}
                    onMarkAsRead={onMarkAsRead}
                  />
                ))}
              </div>
            )}
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
};