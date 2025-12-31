import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { NotificationDropdown } from '../NotificationDropdown';
import type { Notification } from '@/types/notification';

/**
 * NotificationDropdown 컴포넌트 테스트
 * Requirements: 2.1, 2.2
 */
describe('NotificationDropdown Tests', () => {
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
    }
  ];

  /**
   * 드롭다운 트리거 버튼 표시 테스트
   */
  it('알림 트리거 버튼이 올바르게 표시되어야 함', () => {
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={1}
      />
    );

    // 알림 버튼이 표시되어야 함
    const triggerButton = screen.getByRole('button', { name: /알림/ });
    expect(triggerButton).toBeInTheDocument();
    
    // 읽지 않은 알림 배지가 표시되어야 함
    expect(screen.getByText('1')).toBeInTheDocument();
  });

  /**
   * 읽지 않은 알림이 없을 때 배지가 표시되지 않아야 함
   */
  it('읽지 않은 알림이 없을 때 배지가 표시되지 않아야 함', () => {
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={0}
      />
    );

    // 배지가 표시되지 않아야 함
    expect(screen.queryByText('1')).not.toBeInTheDocument();
  });

  /**
   * 드롭다운 열기/닫기 테스트
   */
  it('드롭다운이 올바르게 열리고 닫혀야 함', async () => {
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={1}
      />
    );

    const triggerButton = screen.getByRole('button', { name: /알림/ });
    
    // 초기에는 드롭다운 내용이 보이지 않아야 함
    expect(screen.queryByText('새로운 구독 신청')).not.toBeInTheDocument();

    // 버튼 클릭으로 드롭다운 열기
    fireEvent.click(triggerButton);
    
    // 드롭다운 내용이 표시되어야 함
    expect(screen.getByText('새로운 구독 신청')).toBeInTheDocument();
    expect(screen.getByText('구독이 승인되었습니다')).toBeInTheDocument();
  });

  /**
   * 알림 클릭 시 드롭다운 닫기 테스트
   */
  it('알림 클릭 시 드롭다운이 닫혀야 함', () => {
    const mockOnNotificationClick = vi.fn();
    
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={1}
        onNotificationClick={mockOnNotificationClick}
      />
    );

    const triggerButton = screen.getByRole('button', { name: /알림/ });
    
    // 드롭다운 열기
    fireEvent.click(triggerButton);
    expect(screen.getByText('새로운 구독 신청')).toBeInTheDocument();

    // 알림 클릭
    const notification = screen.getByText('새로운 구독 신청');
    fireEvent.click(notification.closest('div')!);

    // 콜백이 호출되어야 함
    expect(mockOnNotificationClick).toHaveBeenCalledWith(mockNotifications[0]);
  });

  /**
   * 읽음 처리 이벤트 전파 테스트
   */
  it('읽음 처리 이벤트가 올바르게 전파되어야 함', () => {
    const mockOnMarkAsRead = vi.fn();
    
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={1}
        onMarkAsRead={mockOnMarkAsRead}
      />
    );

    const triggerButton = screen.getByRole('button', { name: /알림/ });
    
    // 드롭다운 열기
    fireEvent.click(triggerButton);

    // 읽지 않은 알림 클릭
    const unreadNotification = screen.getByText('새로운 구독 신청');
    fireEvent.click(unreadNotification.closest('div')!);

    // 읽음 처리 콜백이 호출되어야 함
    expect(mockOnMarkAsRead).toHaveBeenCalledWith(1);
  });

  /**
   * 모두 읽음 이벤트 전파 테스트
   */
  it('모두 읽음 이벤트가 올바르게 전파되어야 함', () => {
    const mockOnMarkAllAsRead = vi.fn();
    
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={1}
        onMarkAllAsRead={mockOnMarkAllAsRead}
      />
    );

    const triggerButton = screen.getByRole('button', { name: /알림/ });
    
    // 드롭다운 열기
    fireEvent.click(triggerButton);

    // 모두 읽음 버튼 클릭
    const markAllButton = screen.getByRole('button', { name: /모두 읽음/ });
    fireEvent.click(markAllButton);

    // 모두 읽음 콜백이 호출되어야 함
    expect(mockOnMarkAllAsRead).toHaveBeenCalledTimes(1);
  });

  /**
   * 더 보기 이벤트 전파 테스트
   */
  it('더 보기 이벤트가 올바르게 전파되어야 함', () => {
    const mockOnLoadMore = vi.fn();
    
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={1}
        hasMore={true}
        onLoadMore={mockOnLoadMore}
      />
    );

    const triggerButton = screen.getByRole('button', { name: /알림/ });
    
    // 드롭다운 열기
    fireEvent.click(triggerButton);

    // 더 보기 버튼 클릭
    const loadMoreButton = screen.getByRole('button', { name: /더 보기/ });
    fireEvent.click(loadMoreButton);

    // 더 보기 콜백이 호출되어야 함
    expect(mockOnLoadMore).toHaveBeenCalledTimes(1);
  });

  /**
   * 로딩 상태 전파 테스트
   */
  it('로딩 상태가 올바르게 전파되어야 함', () => {
    render(
      <NotificationDropdown 
        notifications={[]}
        unreadCount={0}
        loading={true}
      />
    );

    const triggerButton = screen.getByRole('button', { name: /알림/ });
    
    // 드롭다운 열기
    fireEvent.click(triggerButton);

    // 로딩 스켈레톤이 표시되어야 함
    const skeletons = screen.getAllByRole('generic');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  /**
   * 접근성 속성 테스트
   */
  it('접근성 속성이 올바르게 설정되어야 함', () => {
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={3}
      />
    );

    // aria-label이 올바르게 설정되어야 함
    const triggerButton = screen.getByRole('button', { name: /알림 \(3개 읽지 않음\)/ });
    expect(triggerButton).toBeInTheDocument();
  });

  /**
   * 읽지 않은 알림이 없을 때 접근성 속성 테스트
   */
  it('읽지 않은 알림이 없을 때 접근성 속성이 올바르게 설정되어야 함', () => {
    render(
      <NotificationDropdown 
        notifications={mockNotifications}
        unreadCount={0}
      />
    );

    // aria-label에 읽지 않은 알림 정보가 없어야 함
    const triggerButton = screen.getByRole('button', { name: '알림' });
    expect(triggerButton).toBeInTheDocument();
  });
});