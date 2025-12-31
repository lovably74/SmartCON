import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useNotifications } from '../useNotifications';
import { useNotificationStore } from '@/stores/notificationStore';

// 스토어 모킹
vi.mock('@/stores/notificationStore');
vi.mock('../useNotificationPolling', () => ({
  useNotificationPolling: vi.fn(() => ({
    startPolling: vi.fn(),
    stopPolling: vi.fn(),
    isPolling: false,
  }))
}));

/**
 * useNotifications 훅 테스트
 * Requirements: 2.1, 2.2
 */
describe('useNotifications Hook Tests', () => {
  const mockStore = {
    notifications: [],
    stats: { totalCount: 0, unreadCount: 0 },
    loading: false,
    error: null,
    hasMore: false,
    fetchNotifications: vi.fn(),
    loadMoreNotifications: vi.fn(),
    fetchStats: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    deleteNotification: vi.fn(),
    clearError: vi.fn(),
    reset: vi.fn(),
  };

  beforeEach(() => {
    vi.mocked(useNotificationStore).mockReturnValue(mockStore);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  /**
   * 초기 데이터 로드 테스트
   */
  it('autoFetch가 true일 때 초기 데이터를 자동으로 로드해야 함', () => {
    renderHook(() => useNotifications({ autoFetch: true }));

    expect(mockStore.fetchNotifications).toHaveBeenCalledTimes(1);
    expect(mockStore.fetchStats).toHaveBeenCalledTimes(1);
  });

  /**
   * autoFetch가 false일 때 초기 데이터를 로드하지 않아야 함
   */
  it('autoFetch가 false일 때 초기 데이터를 로드하지 않아야 함', () => {
    renderHook(() => useNotifications({ autoFetch: false }));

    expect(mockStore.fetchNotifications).not.toHaveBeenCalled();
    expect(mockStore.fetchStats).not.toHaveBeenCalled();
  });

  /**
   * 알림 새로고침 테스트
   */
  it('refreshNotifications가 올바르게 작동해야 함', async () => {
    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.refreshNotifications();
    });

    expect(mockStore.fetchNotifications).toHaveBeenCalledTimes(2); // 초기 로드 + 새로고침
    expect(mockStore.fetchStats).toHaveBeenCalledTimes(2); // 초기 로드 + 새로고침
  });

  /**
   * 읽지 않은 알림만 조회 테스트
   */
  it('fetchUnreadNotifications가 올바른 필터로 호출되어야 함', async () => {
    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.fetchUnreadNotifications();
    });

    expect(mockStore.fetchNotifications).toHaveBeenCalledWith({
      filter: { isRead: false },
      sortBy: 'createdAt',
      sortDir: 'desc'
    });
  });

  /**
   * 알림 읽음 처리 테스트
   */
  it('markAsRead가 올바르게 작동해야 함', async () => {
    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.markAsRead(1);
    });

    expect(mockStore.markAsRead).toHaveBeenCalledWith(1);
  });

  /**
   * 모든 알림 읽음 처리 테스트
   */
  it('markAllAsRead가 올바르게 작동해야 함', async () => {
    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.markAllAsRead();
    });

    expect(mockStore.markAllAsRead).toHaveBeenCalledTimes(1);
  });

  /**
   * 알림 삭제 테스트
   */
  it('deleteNotification이 올바르게 작동해야 함', async () => {
    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.deleteNotification(1);
    });

    expect(mockStore.deleteNotification).toHaveBeenCalledWith(1);
  });

  /**
   * 더 많은 알림 로드 테스트
   */
  it('loadMore가 올바른 조건에서만 작동해야 함', async () => {
    // hasMore가 true이고 loading이 false일 때만 로드
    vi.mocked(useNotificationStore).mockReturnValue({
      ...mockStore,
      hasMore: true,
      loading: false,
    });

    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.loadMore();
    });

    expect(mockStore.loadMoreNotifications).toHaveBeenCalledTimes(1);
  });

  /**
   * 로딩 중일 때 더 많은 알림 로드가 차단되어야 함
   */
  it('로딩 중일 때 loadMore가 차단되어야 함', async () => {
    vi.mocked(useNotificationStore).mockReturnValue({
      ...mockStore,
      hasMore: true,
      loading: true, // 로딩 중
    });

    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.loadMore();
    });

    expect(mockStore.loadMoreNotifications).not.toHaveBeenCalled();
  });

  /**
   * hasMore가 false일 때 더 많은 알림 로드가 차단되어야 함
   */
  it('hasMore가 false일 때 loadMore가 차단되어야 함', async () => {
    vi.mocked(useNotificationStore).mockReturnValue({
      ...mockStore,
      hasMore: false, // 더 이상 로드할 데이터 없음
      loading: false,
    });

    const { result } = renderHook(() => useNotifications());

    await act(async () => {
      await result.current.loadMore();
    });

    expect(mockStore.loadMoreNotifications).not.toHaveBeenCalled();
  });

  /**
   * 에러 처리 테스트
   */
  it('clearError가 올바르게 작동해야 함', () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      result.current.clearError();
    });

    expect(mockStore.clearError).toHaveBeenCalledTimes(1);
  });

  /**
   * 정리 함수 테스트
   */
  it('cleanup이 올바르게 작동해야 함', () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      result.current.cleanup();
    });

    expect(mockStore.reset).toHaveBeenCalledTimes(1);
  });

  /**
   * 상태 반환 테스트
   */
  it('올바른 상태를 반환해야 함', () => {
    const testStats = { totalCount: 10, unreadCount: 3 };
    const testNotifications = [
      { id: 1, title: 'Test', isRead: false },
      { id: 2, title: 'Test 2', isRead: true },
    ];

    vi.mocked(useNotificationStore).mockReturnValue({
      ...mockStore,
      notifications: testNotifications as any,
      stats: testStats,
      loading: true,
      error: 'Test error',
      hasMore: true,
    });

    const { result } = renderHook(() => useNotifications());

    expect(result.current.notifications).toEqual(testNotifications);
    expect(result.current.stats).toEqual(testStats);
    expect(result.current.unreadCount).toBe(3);
    expect(result.current.loading).toBe(true);
    expect(result.current.error).toBe('Test error');
    expect(result.current.hasMore).toBe(true);
  });
});