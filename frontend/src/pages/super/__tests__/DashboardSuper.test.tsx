/**
 * 슈퍼관리자 대시보드 컴포넌트 테스트
 */

import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import DashboardSuper from '../DashboardSuper';
import * as adminApi from '@/hooks/useAdminApi';

// API 훅 모킹
vi.mock('@/hooks/useAdminApi');

// DashboardLayout 모킹
vi.mock('@/components/layout/DashboardLayout', () => ({
  default: ({ children }: { children: React.ReactNode }) => <div data-testid="dashboard-layout">{children}</div>
}));

describe('DashboardSuper', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
    vi.clearAllMocks();
  });

  const renderWithQueryClient = (component: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        {component}
      </QueryClientProvider>
    );
  };

  it('로딩 상태를 올바르게 표시한다', () => {
    // Given
    vi.mocked(adminApi.useDashboardStats).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as any);

    vi.mocked(adminApi.useRecentTenants).mockReturnValue({
      data: undefined,
      isLoading: true,
    } as any);

    vi.mocked(adminApi.useSystemHealth).mockReturnValue({
      data: undefined,
    } as any);

    // When
    renderWithQueryClient(<DashboardSuper />);

    // Then
    expect(screen.getByText('데이터를 불러오는 중...')).toBeInTheDocument();
  });

  it('대시보드 통계를 올바르게 표시한다', async () => {
    // Given
    const mockStats = {
      totalTenants: 15,
      activeTenants: 10,
      suspendedTenants: 3,
      newTenantsThisMonth: 5,
      totalUsers: 150,
      newUsersThisMonth: 25,
      totalRevenue: 1000000,
      monthlyRevenue: 100000,
      completedPayments: 100,
      failedPayments: 5,
      pendingPayments: 10,
      systemStatus: 'HEALTHY',
      activeConnections: 0,
    };

    const mockRecentTenants = [
      {
        id: 1,
        companyName: '대한건설',
        status: 'ACTIVE',
        planId: 'BASIC_PLAN',
        userCount: 10,
        createdAt: '2025-12-24T10:00:00',
      },
    ];

    vi.mocked(adminApi.useDashboardStats).mockReturnValue({
      data: mockStats,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(adminApi.useRecentTenants).mockReturnValue({
      data: mockRecentTenants,
      isLoading: false,
    } as any);

    vi.mocked(adminApi.useSystemHealth).mockReturnValue({
      data: 'HEALTHY',
    } as any);

    // When
    renderWithQueryClient(<DashboardSuper />);

    // Then
    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument(); // 총 테넌트 수
      expect(screen.getByText('150')).toBeInTheDocument(); // 총 사용자 수
      expect(screen.getByText('100')).toBeInTheDocument(); // 완료된 결제
      expect(screen.getByText('대한건설')).toBeInTheDocument(); // 최근 테넌트
    });
  });

  it('오류 상태를 올바르게 표시한다', () => {
    // Given
    const mockError = new Error('API 오류');

    vi.mocked(adminApi.useDashboardStats).mockReturnValue({
      data: undefined,
      isLoading: false,
      error: mockError,
    } as any);

    vi.mocked(adminApi.useRecentTenants).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    vi.mocked(adminApi.useSystemHealth).mockReturnValue({
      data: undefined,
    } as any);

    // When
    renderWithQueryClient(<DashboardSuper />);

    // Then
    expect(screen.getByText('데이터를 불러오는 중 오류가 발생했습니다.')).toBeInTheDocument();
    expect(screen.getByText('API 오류')).toBeInTheDocument();
  });

  it('시스템 상태를 올바르게 표시한다', async () => {
    // Given
    const mockStats = {
      totalTenants: 15,
      activeTenants: 10,
      suspendedTenants: 3,
      newTenantsThisMonth: 5,
      totalUsers: 150,
      newUsersThisMonth: 25,
      totalRevenue: 1000000,
      monthlyRevenue: 100000,
      completedPayments: 100,
      failedPayments: 5,
      pendingPayments: 10,
      systemStatus: 'HEALTHY',
      activeConnections: 0,
    };

    vi.mocked(adminApi.useDashboardStats).mockReturnValue({
      data: mockStats,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(adminApi.useRecentTenants).mockReturnValue({
      data: [],
      isLoading: false,
    } as any);

    vi.mocked(adminApi.useSystemHealth).mockReturnValue({
      data: 'HEALTHY',
    } as any);

    // When
    renderWithQueryClient(<DashboardSuper />);

    // Then
    await waitFor(() => {
      expect(screen.getByText('All Systems Operational')).toBeInTheDocument();
      expect(screen.getByText('HEALTHY')).toBeInTheDocument();
    });
  });
});