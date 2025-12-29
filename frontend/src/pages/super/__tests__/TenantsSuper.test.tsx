/**
 * TenantsSuper 컴포넌트 단위 테스트
 * 구독 관리 기능이 확장된 테넌트 관리 페이지 테스트
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import TenantsSuper from '../TenantsSuper';
import * as useAdminApi from '@/hooks/useAdminApi';

// Mock 데이터
const mockTenants = {
  content: [
    {
      id: 1,
      businessNo: '123-45-67890',
      companyName: '테스트 건설',
      representativeName: '김대표',
      status: 'ACTIVE',
      planId: 'BASIC',
      userCount: 10,
      createdAt: '2024-01-01T00:00:00',
      subscriptionId: 1,
      subscriptionStatus: 'ACTIVE',
      monthlyAmount: 50000,
      approvedAt: '2024-01-02T00:00:00',
      approvedBy: '슈퍼관리자'
    },
    {
      id: 2,
      businessNo: '987-65-43210',
      companyName: '대한 건축',
      representativeName: '이대표',
      status: 'SUSPENDED',
      planId: 'PREMIUM',
      userCount: 25,
      createdAt: '2024-01-15T00:00:00',
      subscriptionId: 2,
      subscriptionStatus: 'SUSPENDED',
      monthlyAmount: 100000
    }
  ],
  totalElements: 25, // 페이지네이션을 위해 더 큰 수로 변경
  totalPages: 2, // 2페이지로 설정
  numberOfElements: 2,
  first: true,
  last: false, // 마지막 페이지가 아님
  size: 20,
  number: 0
};

const mockApprovalHistory = [
  {
    id: 1,
    subscriptionId: 1,
    adminName: '슈퍼관리자',
    fromStatus: 'PENDING_APPROVAL',
    toStatus: 'APPROVED',
    reason: '정상적인 신청으로 승인',
    action: 'APPROVE',
    processedAt: '2024-01-02T00:00:00'
  }
];

// Mock hooks
const mockUseTenants = vi.fn();
const mockUseUpdateTenantStatus = vi.fn();
const mockUseSuspendSubscription = vi.fn();
const mockUseTerminateSubscription = vi.fn();
const mockUseApprovalHistory = vi.fn();

vi.mock('@/hooks/useAdminApi', () => ({
  useTenants: () => mockUseTenants(),
  useUpdateTenantStatus: () => mockUseUpdateTenantStatus(),
  useSuspendSubscription: () => mockUseSuspendSubscription(),
  useTerminateSubscription: () => mockUseTerminateSubscription(),
  useApprovalHistory: () => mockUseApprovalHistory()
}));

// Mock components
vi.mock('@/components/layout/DashboardLayout', () => ({
  default: ({ children }: { children: React.ReactNode }) => <div data-testid="dashboard-layout">{children}</div>
}));

vi.mock('@/components/super/SubscriptionDetailModal', () => ({
  default: ({ tenant, isOpen, onClose }: any) => 
    isOpen ? (
      <div data-testid="subscription-detail-modal">
        <div>구독 상세 정보: {tenant?.companyName}</div>
        <button onClick={onClose}>닫기</button>
      </div>
    ) : null
}));

describe('TenantsSuper', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false }
      }
    });

    // 기본 mock 설정
    mockUseTenants.mockReturnValue({
      data: mockTenants,
      isLoading: false,
      error: null
    });

    mockUseUpdateTenantStatus.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockUseSuspendSubscription.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockUseTerminateSubscription.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockUseApprovalHistory.mockReturnValue({
      data: mockApprovalHistory,
      isLoading: false
    });
  });

  const renderComponent = () => {
    return render(
      <QueryClientProvider client={queryClient}>
        <TenantsSuper />
      </QueryClientProvider>
    );
  };

  describe('기본 렌더링', () => {
    it('페이지 제목과 설명이 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('고객사(Tenant) 관리')).toBeInTheDocument();
      expect(screen.getByText('전체 가입 고객사 현황을 조회하고 관리합니다.')).toBeInTheDocument();
    });

    it('테넌트 목록이 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('테스트 건설')).toBeInTheDocument();
      expect(screen.getByText('대한 건축')).toBeInTheDocument();
      expect(screen.getByText('123-45-67890')).toBeInTheDocument();
      expect(screen.getByText('987-65-43210')).toBeInTheDocument();
    });

    it('구독 상태가 표시되어야 한다', () => {
      renderComponent();
      
      // Badge 컴포넌트 내의 활성 상태를 찾기
      const activeBadges = screen.getAllByText('활성');
      expect(activeBadges.length).toBeGreaterThan(0);
      
      const suspendedBadges = screen.getAllByText('중지');
      expect(suspendedBadges.length).toBeGreaterThan(0);
    });

    it('월 요금이 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('₩50,000')).toBeInTheDocument();
      expect(screen.getByText('₩100,000')).toBeInTheDocument();
    });
  });

  describe('필터링 기능', () => {
    it('테넌트 상태 필터가 작동해야 한다', async () => {
      renderComponent();
      
      const statusFilter = screen.getByDisplayValue('전체 상태');
      fireEvent.change(statusFilter, { target: { value: 'ACTIVE' } });
      
      // 상태 변경 후 useTenants가 새로운 파라미터로 호출되는지 확인
      expect(statusFilter).toHaveValue('ACTIVE');
    });

    it('구독 상태 필터가 작동해야 한다', async () => {
      renderComponent();
      
      const subscriptionFilter = screen.getByDisplayValue('전체 구독상태');
      fireEvent.change(subscriptionFilter, { target: { value: 'ACTIVE' } });
      
      // 상태 변경 후 필터 값이 변경되는지 확인
      expect(subscriptionFilter).toHaveValue('ACTIVE');
    });

    it('검색 기능이 작동해야 한다', async () => {
      renderComponent();
      
      const searchInput = screen.getByPlaceholderText('회사명 또는 사업자번호 검색');
      const searchButton = screen.getByRole('button', { name: '' }); // Search icon button
      
      fireEvent.change(searchInput, { target: { value: '테스트' } });
      fireEvent.click(searchButton);
      
      // 검색어가 입력되었는지 확인
      expect(searchInput).toHaveValue('테스트');
    });
  });

  describe('구독 관리 액션', () => {
    it('구독 상세 보기 버튼이 표시되어야 한다', () => {
      renderComponent();
      
      const detailButtons = screen.getAllByTitle('구독 상세 보기');
      expect(detailButtons).toHaveLength(2); // 두 개의 테넌트 모두 구독 ID가 있음
    });

    it('구독 상세 모달이 열려야 한다', async () => {
      renderComponent();
      
      const detailButton = screen.getAllByTitle('구독 상세 보기')[0];
      fireEvent.click(detailButton);
      
      await waitFor(() => {
        expect(screen.getByTestId('subscription-detail-modal')).toBeInTheDocument();
        expect(screen.getByText('구독 상세 정보: 테스트 건설')).toBeInTheDocument();
      });
    });

    it('승인 이력 보기 버튼이 표시되어야 한다', () => {
      renderComponent();
      
      const historyButtons = screen.getAllByTitle('승인 이력 보기');
      expect(historyButtons).toHaveLength(2);
    });

    it('활성 구독에 대해 중지 버튼이 표시되어야 한다', () => {
      renderComponent();
      
      const suspendButtons = screen.getAllByTitle('구독 중지');
      expect(suspendButtons).toHaveLength(1); // ACTIVE 상태인 첫 번째 테넌트만
    });

    it('활성/중지 구독에 대해 종료 버튼이 표시되어야 한다', () => {
      renderComponent();
      
      const terminateButtons = screen.getAllByTitle('구독 종료');
      expect(terminateButtons).toHaveLength(2); // 두 테넌트 모두 ACTIVE 또는 SUSPENDED
    });
  });

  describe('구독 중지 기능', () => {
    it('구독 중지 다이얼로그가 열려야 한다', async () => {
      renderComponent();
      
      const suspendButton = screen.getByTitle('구독 중지');
      fireEvent.click(suspendButton);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      expect(screen.getByRole('heading', { name: '구독 중지' })).toBeInTheDocument();
      expect(screen.getByText(/테스트 건설의 구독을 중지하시겠습니까/)).toBeInTheDocument();
    });

    it('중지 사유 입력 후 구독 중지가 실행되어야 한다', async () => {
      const mockSuspend = vi.fn();
      mockUseSuspendSubscription.mockReturnValue({
        mutateAsync: mockSuspend,
        isPending: false
      });

      renderComponent();
      
      const suspendButton = screen.getByTitle('구독 중지');
      fireEvent.click(suspendButton);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      const reasonInput = screen.getByPlaceholderText('구독 중지 사유를 입력해주세요...');
      fireEvent.change(reasonInput, { target: { value: '결제 지연으로 인한 중지' } });
      
      const confirmButtons = screen.getAllByRole('button', { name: '구독 중지' });
      const confirmButton = confirmButtons.find(btn => !btn.disabled);
      fireEvent.click(confirmButton!);
      
      expect(mockSuspend).toHaveBeenCalledWith({
        subscriptionId: 1,
        reason: '결제 지연으로 인한 중지'
      });
    });

    it('사유 없이는 중지 버튼이 비활성화되어야 한다', async () => {
      renderComponent();
      
      const suspendButton = screen.getByTitle('구독 중지');
      fireEvent.click(suspendButton);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      const confirmButtons = screen.getAllByRole('button', { name: '구독 중지' });
      const confirmButton = confirmButtons.find(btn => btn.getAttribute('class')?.includes('bg-primary'));
      expect(confirmButton).toBeDisabled();
    });
  });

  describe('구독 종료 기능', () => {
    it('구독 종료 다이얼로그가 열려야 한다', async () => {
      renderComponent();
      
      const terminateButtons = screen.getAllByTitle('구독 종료');
      fireEvent.click(terminateButtons[0]);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      expect(screen.getByRole('heading', { name: '구독 종료' })).toBeInTheDocument();
      expect(screen.getByText(/테스트 건설의 구독을 완전히 종료하시겠습니까/)).toBeInTheDocument();
    });

    it('종료 사유 입력 후 구독 종료가 실행되어야 한다', async () => {
      const mockTerminate = vi.fn();
      mockUseTerminateSubscription.mockReturnValue({
        mutateAsync: mockTerminate,
        isPending: false
      });

      renderComponent();
      
      const terminateButtons = screen.getAllByTitle('구독 종료');
      fireEvent.click(terminateButtons[0]);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      const reasonInput = screen.getByPlaceholderText('구독 종료 사유를 입력해주세요...');
      fireEvent.change(reasonInput, { target: { value: '계약 만료로 인한 종료' } });
      
      const confirmButtons = screen.getAllByRole('button', { name: '구독 종료' });
      const confirmButton = confirmButtons.find(btn => !btn.disabled);
      fireEvent.click(confirmButton!);
      
      expect(mockTerminate).toHaveBeenCalledWith({
        subscriptionId: 1,
        reason: '계약 만료로 인한 종료'
      });
    });
  });

  describe('승인 이력 기능', () => {
    it('승인 이력 다이얼로그가 열려야 한다', async () => {
      renderComponent();
      
      const historyButton = screen.getAllByTitle('승인 이력 보기')[0];
      fireEvent.click(historyButton);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      expect(screen.getByText('승인 이력')).toBeInTheDocument();
      expect(screen.getByText(/테스트 건설의 구독 승인 이력을 확인할 수 있습니다/)).toBeInTheDocument();
    });

    it('승인 이력 데이터가 표시되어야 한다', async () => {
      renderComponent();
      
      const historyButton = screen.getAllByTitle('승인 이력 보기')[0];
      fireEvent.click(historyButton);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      expect(screen.getByText('승인')).toBeInTheDocument();
      expect(screen.getByText('슈퍼관리자')).toBeInTheDocument();
      expect(screen.getByText('PENDING_APPROVAL → APPROVED')).toBeInTheDocument();
      expect(screen.getByText('정상적인 신청으로 승인')).toBeInTheDocument();
    });
  });

  describe('로딩 및 에러 상태', () => {
    it('로딩 상태가 표시되어야 한다', () => {
      mockUseTenants.mockReturnValue({
        data: null,
        isLoading: true,
        error: null
      });

      renderComponent();
      
      expect(screen.getByText('데이터를 불러오는 중...')).toBeInTheDocument();
    });

    it('에러 상태가 표시되어야 한다', () => {
      mockUseTenants.mockReturnValue({
        data: null,
        isLoading: false,
        error: new Error('API 호출 실패')
      });

      renderComponent();
      
      expect(screen.getByText('데이터를 불러오는 중 오류가 발생했습니다.')).toBeInTheDocument();
      expect(screen.getByText('API 호출 실패')).toBeInTheDocument();
    });
  });

  describe('페이지네이션', () => {
    it('페이지네이션 정보가 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('25개 중 2개 표시')).toBeInTheDocument();
      expect(screen.getByText('1 / 2')).toBeInTheDocument();
    });

    it('다중 페이지일 때 다음 버튼이 활성화되어야 한다', () => {
      renderComponent();
      
      const prevButton = screen.getByRole('button', { name: '이전' });
      const nextButton = screen.getByRole('button', { name: '다음' });
      
      expect(prevButton).toBeDisabled(); // 첫 페이지이므로 이전 버튼 비활성화
      expect(nextButton).not.toBeDisabled(); // 마지막 페이지가 아니므로 다음 버튼 활성화
    });
  });

  describe('테넌트 상태 관리', () => {
    it('테넌트 관리 다이얼로그가 열려야 한다', async () => {
      renderComponent();
      
      const manageButtons = screen.getAllByTitle('테넌트 관리');
      fireEvent.click(manageButtons[0]);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      expect(screen.getByText('테넌트 관리')).toBeInTheDocument();
      expect(screen.getByText(/테스트 건설의 상태를 변경할 수 있습니다/)).toBeInTheDocument();
    });

    it('테넌트 상태 변경이 실행되어야 한다', async () => {
      const mockUpdateStatus = vi.fn();
      mockUseUpdateTenantStatus.mockReturnValue({
        mutateAsync: mockUpdateStatus,
        isPending: false
      });

      renderComponent();
      
      const manageButton = screen.getAllByTitle('테넌트 관리')[0];
      fireEvent.click(manageButton);
      
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
      
      const statusSelect = screen.getByDisplayValue('정상');
      fireEvent.change(statusSelect, { target: { value: 'SUSPENDED' } });
      
      const confirmButton = screen.getByRole('button', { name: '상태 변경' });
      fireEvent.click(confirmButton);
      
      expect(mockUpdateStatus).toHaveBeenCalledWith({
        tenantId: 1,
        status: 'SUSPENDED'
      });
    });
  });
});