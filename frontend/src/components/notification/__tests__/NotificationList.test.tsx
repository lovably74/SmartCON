import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { NotificationList } from '../NotificationList';
import type { Notification } from '@/types/notification';

/**
 * NotificationList 컴포넌트 테스트
 * Requirements: 2.1, 2.2
 */
describe('NotificationList Tests', () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  const mockNotifications: Notification[] = [
    {
      id: 1,
      recipientId: 1,
      type: 'SUBSCRIPTION_REQUEST',
      title: '새로운 구독 신청',
      message: '테스트 회사에서 구독을 신청했습니다.',
      isRead: false,
      createdAt: '2024-01-01T10:00:00Z'
    },
    {
      id: 2,
      recipientId: 1,
      type: 'SUBSCRIPTION_APPROVED',
      title: '구독이 승인되었습니다',
      message: '구독 신청이 승인되어 서비스를 이용하실 수 있습니다.',
      isRead: true,
      readAt: '2024-01-01T11:00:00Z',
      createdAt: '2024-01-01T09:00:00Z'
    },
    {
      id: 3,
      recipientId: 1,
      type: 'APPROVAL_REMINDER',
      title: '승인 대기 중인 구독이 있습니다',
      message: '24시간 이상 대기 중인 구독 신청이 있습니다.',
      isRead: false,
      createdAt: '2024-01-01T08:00:00Z'
    }
  ];

  /**
   * 알림 목록 표시 테스트
   */
  it('알림 목록이 올바르게 표시되어야 함', () => {
    render(
      <NotificationList 
        notifications={mockNotifications}
        unreadCount={2}
      />
    );

    // 제목이 표시되어야 함
    expect(screen.getByText('알림')).toBeInTheDocument();
    
    // 읽지 않은 알림 개수 배지가 표시되어야 함
    expect(screen.getAllByText('2')[0]).toBeInTheDocument();
    
    // 모든 알림이 표시되어야 함
    mockNotifications.forEach(notification => {
      expect(screen.getByText(notification.title)).toBeInTheDocument();
    });
  });

  /**
   * 전체/읽지 않음 탭 전환 테스트
   */
  it('전체/읽지 않음 탭이 올바르게 작동해야 함', async () => {
    render(
      <NotificationList 
        notifications={mockNotifications}
        unreadCount={2}
      />
    );

    // 전체 탭에서는 모든 알림이 표시되어야 함
    expect(screen.getByText('새로운 구독 신청')).toBeInTheDocument();
    expect(screen.getByText('구독이 승인되었습니다')).toBeInTheDocument();
    expect(screen.getByText('승인 대기 중인 구독이 있습니다')).toBeInTheDocument();

    // 읽지 않음 탭 클릭
    const unreadTab = screen.getByRole('tab', { name: /읽지 않음/ });
    fireEvent.click(unreadTab);

    // 탭 전환 후 잠시 대기
    await new Promise(resolve => setTimeout(resolve, 100));

    // 읽지 않은 알림만 표시되어야 함
    expect(screen.getByText('새로운 구독 신청')).toBeInTheDocument();
    expect(screen.queryByText('구독이 승인되었습니다')).not.toBeInTheDocument();
    expect(screen.getByText('승인 대기 중인 구독이 있습니다')).toBeInTheDocument();
  });

  /**
   * 모두 읽음 버튼 테스트
   */
  it('모두 읽음 버튼이 올바르게 작동해야 함', () => {
    const mockOnMarkAllAsRead = vi.fn();
    
    render(
      <NotificationList 
        notifications={mockNotifications}
        unreadCount={2}
        onMarkAllAsRead={mockOnMarkAllAsRead}
      />
    );

    // 모두 읽음 버튼이 표시되어야 함
    const markAllButton = screen.getByRole('button', { name: /모두 읽음/ });
    expect(markAllButton).toBeInTheDocument();

    // 버튼 클릭 시 콜백이 호출되어야 함
    fireEvent.click(markAllButton);
    expect(mockOnMarkAllAsRead).toHaveBeenCalledTimes(1);
  });

  /**
   * 읽지 않은 알림이 없을 때 모두 읽음 버튼이 표시되지 않아야 함
   */
  it('읽지 않은 알림이 없을 때 모두 읽음 버튼이 표시되지 않아야 함', () => {
    const readNotifications = mockNotifications.map(n => ({ ...n, isRead: true }));
    
    render(
      <NotificationList 
        notifications={readNotifications}
        unreadCount={0}
        onMarkAllAsRead={vi.fn()}
      />
    );

    // 모두 읽음 버튼이 표시되지 않아야 함
    expect(screen.queryByRole('button', { name: /모두 읽음/ })).not.toBeInTheDocument();
  });

  /**
   * 빈 상태 표시 테스트
   */
  it('알림이 없을 때 빈 상태가 표시되어야 함', () => {
    render(
      <NotificationList 
        notifications={[]}
        unreadCount={0}
      />
    );

    // 빈 상태 메시지가 표시되어야 함
    expect(screen.getByText('알림이 없습니다')).toBeInTheDocument();
    expect(screen.getByText('새로운 알림이 도착하면 여기에 표시됩니다.')).toBeInTheDocument();
  });

  /**
   * 읽지 않음 탭에서 빈 상태 표시 테스트
   */
  it('읽지 않음 탭에서 알림이 없을 때 적절한 빈 상태가 표시되어야 함', async () => {
    const readNotifications = mockNotifications.map(n => ({ ...n, isRead: true }));
    
    render(
      <NotificationList 
        notifications={readNotifications}
        unreadCount={0}
      />
    );

    // 읽지 않음 탭 클릭
    const unreadTab = screen.getByRole('tab', { name: /읽지 않음/ });
    fireEvent.click(unreadTab);

    // 탭 전환 후 잠시 대기
    await new Promise(resolve => setTimeout(resolve, 100));

    // 읽지 않은 알림이 없다는 메시지가 표시되어야 함
    expect(screen.getByText('읽지 않은 알림이 없습니다')).toBeInTheDocument();
    expect(screen.getByText('모든 알림을 확인했습니다.')).toBeInTheDocument();
  });

  /**
   * 로딩 상태 표시 테스트
   */
  it('로딩 상태가 올바르게 표시되어야 함', () => {
    render(
      <NotificationList 
        notifications={[]}
        unreadCount={0}
        loading={true}
      />
    );

    // 로딩 스켈레톤이 표시되어야 함
    const skeletons = screen.getAllByRole('generic');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  /**
   * 더 보기 버튼 테스트
   */
  it('더 보기 버튼이 올바르게 작동해야 함', () => {
    const mockOnLoadMore = vi.fn();
    
    render(
      <NotificationList 
        notifications={mockNotifications}
        unreadCount={2}
        hasMore={true}
        onLoadMore={mockOnLoadMore}
      />
    );

    // 더 보기 버튼이 표시되어야 함
    const loadMoreButton = screen.getByRole('button', { name: /더 보기/ });
    expect(loadMoreButton).toBeInTheDocument();

    // 버튼 클릭 시 콜백이 호출되어야 함
    fireEvent.click(loadMoreButton);
    expect(mockOnLoadMore).toHaveBeenCalledTimes(1);
  });

  /**
   * 알림 클릭 이벤트 전파 테스트
   */
  it('알림 클릭 시 이벤트가 올바르게 전파되어야 함', () => {
    const mockOnNotificationClick = vi.fn();
    const mockOnMarkAsRead = vi.fn();
    
    render(
      <NotificationList 
        notifications={mockNotifications}
        unreadCount={2}
        onNotificationClick={mockOnNotificationClick}
        onMarkAsRead={mockOnMarkAsRead}
      />
    );

    // 첫 번째 알림 클릭 (읽지 않은 상태)
    const firstNotification = screen.getByText('새로운 구독 신청');
    fireEvent.click(firstNotification.closest('div')!);

    expect(mockOnMarkAsRead).toHaveBeenCalledWith(1);
    expect(mockOnNotificationClick).toHaveBeenCalledWith(mockNotifications[0]);
  });
});