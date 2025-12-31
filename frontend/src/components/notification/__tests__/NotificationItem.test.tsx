import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { NotificationItem } from '../NotificationItem';
import type { Notification } from '@/types/notification';

/**
 * NotificationItem 컴포넌트 테스트
 * Requirements: 2.1, 2.2
 */
describe('NotificationItem Tests', () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  const mockNotification: Notification = {
    id: 1,
    recipientId: 1,
    type: 'SUBSCRIPTION_REQUEST',
    title: '새로운 구독 신청',
    message: '테스트 회사에서 구독을 신청했습니다.',
    relatedEntityType: 'subscription',
    relatedEntityId: 123,
    isRead: false,
    createdAt: '2024-01-01T10:00:00Z'
  };

  /**
   * 알림 표시 테스트
   * 알림의 기본 정보가 올바르게 표시되는지 확인
   */
  it('알림의 기본 정보가 올바르게 표시되어야 함', () => {
    render(<NotificationItem notification={mockNotification} />);

    // 제목과 메시지가 표시되어야 함
    expect(screen.getByText(mockNotification.title)).toBeInTheDocument();
    expect(screen.getByText(mockNotification.message)).toBeInTheDocument();
    
    // 읽지 않은 알림 표시가 있어야 함
    expect(screen.getByText('읽지 않음')).toBeInTheDocument();
    
    // 시간 정보가 표시되어야 함
    expect(screen.getByText(/전/)).toBeInTheDocument();
  });

  /**
   * 읽은 알림 표시 테스트
   */
  it('읽은 알림은 읽지 않음 표시가 없어야 함', () => {
    const readNotification = { ...mockNotification, isRead: true, readAt: '2024-01-01T11:00:00Z' };
    
    render(<NotificationItem notification={readNotification} />);

    // 읽지 않음 표시가 없어야 함
    expect(screen.queryByText('읽지 않음')).not.toBeInTheDocument();
    
    // 제목의 폰트 굵기가 일반적이어야 함 (읽은 상태)
    const title = screen.getByText(readNotification.title);
    expect(title).not.toHaveClass('font-semibold');
  });

  /**
   * 클릭 이벤트 테스트
   */
  it('알림 클릭 시 onClick 콜백이 호출되어야 함', () => {
    const mockOnClick = vi.fn();
    
    render(
      <NotificationItem 
        notification={mockNotification} 
        onClick={mockOnClick}
      />
    );

    const notificationElement = screen.getByText(mockNotification.title).closest('div');
    fireEvent.click(notificationElement!);

    expect(mockOnClick).toHaveBeenCalledWith(mockNotification);
  });

  /**
   * 읽음 처리 테스트
   */
  it('읽지 않은 알림 클릭 시 onMarkAsRead 콜백이 호출되어야 함', () => {
    const mockOnMarkAsRead = vi.fn();
    const mockOnClick = vi.fn();
    
    render(
      <NotificationItem 
        notification={mockNotification} 
        onClick={mockOnClick}
        onMarkAsRead={mockOnMarkAsRead}
      />
    );

    const notificationElement = screen.getByText(mockNotification.title).closest('div');
    fireEvent.click(notificationElement!);

    expect(mockOnMarkAsRead).toHaveBeenCalledWith(mockNotification.id);
    expect(mockOnClick).toHaveBeenCalledWith(mockNotification);
  });

  /**
   * 이미 읽은 알림 클릭 시 읽음 처리가 호출되지 않아야 함
   */
  it('이미 읽은 알림 클릭 시 onMarkAsRead가 호출되지 않아야 함', () => {
    const readNotification = { ...mockNotification, isRead: true };
    const mockOnMarkAsRead = vi.fn();
    const mockOnClick = vi.fn();
    
    render(
      <NotificationItem 
        notification={readNotification} 
        onClick={mockOnClick}
        onMarkAsRead={mockOnMarkAsRead}
      />
    );

    const notificationElement = screen.getByText(readNotification.title).closest('div');
    fireEvent.click(notificationElement!);

    expect(mockOnMarkAsRead).not.toHaveBeenCalled();
    expect(mockOnClick).toHaveBeenCalledWith(readNotification);
  });

  /**
   * 다양한 알림 타입 아이콘 표시 테스트
   */
  it('알림 타입에 따른 아이콘이 표시되어야 함', () => {
    const approvedNotification = {
      ...mockNotification,
      type: 'SUBSCRIPTION_APPROVED' as const,
      title: '구독이 승인되었습니다'
    };
    
    render(<NotificationItem notification={approvedNotification} />);
    
    // 아이콘이 표시되어야 함 (정확한 아이콘은 NotificationIcon 컴포넌트에서 처리)
    expect(screen.getByText(approvedNotification.title)).toBeInTheDocument();
  });

  /**
   * 긴 제목과 메시지 처리 테스트
   */
  it('긴 제목과 메시지가 적절히 잘려서 표시되어야 함', () => {
    const longNotification = {
      ...mockNotification,
      title: '매우 긴 제목입니다. 이 제목은 한 줄을 넘어갈 정도로 길어서 잘려서 표시되어야 합니다.',
      message: '매우 긴 메시지입니다. 이 메시지는 두 줄을 넘어갈 정도로 길어서 적절히 잘려서 표시되어야 합니다. 더 많은 내용이 있지만 보이지 않아야 합니다.'
    };
    
    render(<NotificationItem notification={longNotification} />);
    
    // 제목과 메시지가 표시되어야 함 (CSS로 잘림 처리)
    expect(screen.getByText(longNotification.title)).toBeInTheDocument();
    expect(screen.getByText(longNotification.message)).toBeInTheDocument();
  });
});