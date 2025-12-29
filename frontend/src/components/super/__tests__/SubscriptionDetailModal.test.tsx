/**
 * SubscriptionDetailModal 컴포넌트 단위 테스트
 * 구독 상세 정보 모달의 기능 테스트
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import SubscriptionDetailModal from '../SubscriptionDetailModal';
import { TenantSummary } from '@/lib/api';

// Mock 데이터
const mockTenant: TenantSummary = {
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
  approvedBy: '슈퍼관리자',
  lastLoginAt: '2024-01-15T10:30:00'
};

const mockPendingTenant: TenantSummary = {
  ...mockTenant,
  id: 2,
  companyName: '대기 중 건설',
  subscriptionStatus: 'PENDING_APPROVAL',
  approvedAt: undefined,
  approvedBy: undefined
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
  },
  {
    id: 2,
    subscriptionId: 1,
    adminName: '슈퍼관리자',
    fromStatus: 'APPROVED',
    toStatus: 'ACTIVE',
    reason: '결제 완료로 활성화',
    action: 'ACTIVATE',
    processedAt: '2024-01-03T00:00:00'
  }
];

// Mock hooks
const mockUseApprovalHistory = vi.fn();
const mockUseSuspendSubscription = vi.fn();
const mockUseTerminateSubscription = vi.fn();
const mockUseApproveSubscription = vi.fn();
const mockUseRejectSubscription = vi.fn();

vi.mock('@/hooks/useAdminApi', () => ({
  useApprovalHistory: () => mockUseApprovalHistory(),
  useSuspendSubscription: () => mockUseSuspendSubscription(),
  useTerminateSubscription: () => mockUseTerminateSubscription(),
  useApproveSubscription: () => mockUseApproveSubscription(),
  useRejectSubscription: () => mockUseRejectSubscription()
}));

// Mock Radix UI components
vi.mock('@radix-ui/react-tabs', () => ({
  Root: ({ children, ...props }: any) => <div data-testid="tabs-root" {...props}>{children}</div>,
  List: ({ children, ...props }: any) => <div data-testid="tabs-list" {...props}>{children}</div>,
  Trigger: ({ children, value, ...props }: any) => (
    <button 
      data-testid="tabs-trigger" 
      role="tab"
      aria-selected={value === 'details'}
      {...props}
    >
      {children}
    </button>
  ),
  Content: ({ children, value, ...props }: any) => (
    <div 
      data-testid="tabs-content" 
      role="tabpanel"
      {...props}
    >
      {children}
    </div>
  )
}));

describe('SubscriptionDetailModal', () => {
  let queryClient: QueryClient;
  const mockOnClose = vi.fn();

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false }
      }
    });

    // 기본 mock 설정
    mockUseApprovalHistory.mockReturnValue({
      data: mockApprovalHistory,
      isLoading: false
    });

    mockUseSuspendSubscription.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockUseTerminateSubscription.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockUseApproveSubscription.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockUseRejectSubscription.mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false
    });

    mockOnClose.mockClear();
  });

  const renderComponent = (tenant: TenantSummary | null = mockTenant, isOpen = true) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <SubscriptionDetailModal
          tenant={tenant}
          isOpen={isOpen}
          onClose={mockOnClose}
        />
      </QueryClientProvider>
    );
  };

  describe('기본 렌더링', () => {
    it('모달이 열렸을 때 제목과 설명이 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('테스트 건설 - 구독 상세 정보')).toBeInTheDocument();
      expect(screen.getByText('구독 정보, 승인 이력 및 관리 기능을 제공합니다.')).toBeInTheDocument();
    });

    it('모달이 닫혔을 때 렌더링되지 않아야 한다', () => {
      renderComponent(mockTenant, false);
      
      expect(screen.queryByText('테스트 건설 - 구독 상세 정보')).not.toBeInTheDocument();
    });

    it('tenant가 null일 때 렌더링되지 않아야 한다', () => {
      renderComponent(null);
      
      expect(screen.queryByText('구독 상세 정보')).not.toBeInTheDocument();
    });

    it('탭 메뉴가 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByRole('tab', { name: '구독 정보' })).toBeInTheDocument();
      expect(screen.getByRole('tab', { name: '승인 이력' })).toBeInTheDocument();
      expect(screen.getByRole('tab', { name: '관리 액션' })).toBeInTheDocument();
    });
  });

  describe('구독 정보 탭', () => {
    it('기본 정보가 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('테스트 건설')).toBeInTheDocument();
      expect(screen.getByText('123-45-67890')).toBeInTheDocument();
      expect(screen.getByText('김대표')).toBeInTheDocument();
      expect(screen.getByText('10명')).toBeInTheDocument();
    });

    it('구독 정보가 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('1')).toBeInTheDocument(); // 구독 ID
      expect(screen.getByText('BASIC')).toBeInTheDocument(); // 요금제
      expect(screen.getByText('₩50,000')).toBeInTheDocument(); // 월 요금
      expect(screen.getByText('활성')).toBeInTheDocument(); // 구독 상태
    });

    it('날짜 정보가 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('2024. 1. 1. 오전 9:00')).toBeInTheDocument(); // 가입일
      expect(screen.getByText('2024. 1. 2. 오전 9:00')).toBeInTheDocument(); // 승인일
      expect(screen.getByText('2024. 1. 15. 오후 7:30')).toBeInTheDocument(); // 최근 로그인
    });

    it('승인자 정보가 표시되어야 한다', () => {
      renderComponent();
      
      expect(screen.getByText('슈퍼관리자')).toBeInTheDocument();
    });

    it('승인되지 않은 구독의 경우 승인 정보가 표시되지 않아야 한다', () => {
      renderComponent(mockPendingTenant);
      
      expect(screen.queryByText('승인자 정보')).not.toBeInTheDocument();
    });
  });

  describe('승인 이력 탭', () => {
    it('승인 이력 탭으로 전환할 수 있어야 한다', async () => {
      renderComponent();
      
      const historyTab = screen.getByRole('tab', { name: '승인 이력' });
      fireEvent.click(historyTab);
      
      await waitFor(() => {
        expect(screen.getByText('승인 이력')).toBeInTheDocument();
      });
    });

    it('승인 이력 데이터가 표시되어야 한다', async () => {
      renderComponent();
      
      const historyTab = screen.getByRole('tab', { name: '승인 이력' });
      fireEvent.click(historyTab);
      
      await waitFor(() => {
        expect(screen.getByText('승인')).toBeInTheDocument();
        expect(screen.getByText('슈퍼관리자')).toBeInTheDocument();
        expect(screen.getByText('상태 변경: PENDING_APPROVAL → APPROVED')).toBeInTheDocument();
        expect(screen.getByText('정상적인 신청으로 승인')).toBeInTheDocument();
      });
    });

    it('승인 이력이 없을 때 안내 메시지가 표시되어야 한다', async () => {
      mockUseApprovalHistory.mockReturnValue({
        data: [],
        isLoading: false
      });

      renderComponent();
      
      const historyTab = screen.getByRole('tab', { name: '승인 이력' });
      fireEvent.click(historyTab);
      
      await waitFor(() => {
        expect(screen.getByText('승인 이력이 없습니다.')).toBeInTheDocument();
      });
    });

    it('승인 이력 로딩 상태가 표시되어야 한다', async () => {
      mockUseApprovalHistory.mockReturnValue({
        data: null,
        isLoading: true
      });

      renderComponent();
      
      const historyTab = screen.getByRole('tab', { name: '승인 이력' });
      fireEvent.click(historyTab);
      
      await waitFor(() => {
        expect(screen.getByText('이력을 불러오는 중...')).toBeInTheDocument();
      });
    });
  });

  describe('관리 액션 탭', () => {
    it('관리 액션 탭으로 전환할 수 있어야 한다', async () => {
      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        expect(screen.getByText('관리 액션')).toBeInTheDocument();
        expect(screen.getByText('현재 구독 상태에 따라 수행 가능한 액션들입니다.')).toBeInTheDocument();
      });
    });

    it('ACTIVE 상태에서 중지와 종료 액션이 표시되어야 한다', async () => {
      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        expect(screen.getByRole('button', { name: '중지' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: '종료' })).toBeInTheDocument();
      });
    });

    it('PENDING_APPROVAL 상태에서 승인과 거부 액션이 표시되어야 한다', async () => {
      renderComponent(mockPendingTenant);
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        expect(screen.getByRole('button', { name: '승인' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: '거부' })).toBeInTheDocument();
      });
    });

    it('액션이 없을 때 안내 메시지가 표시되어야 한다', async () => {
      const terminatedTenant = { ...mockTenant, subscriptionStatus: 'TERMINATED' as const };
      renderComponent(terminatedTenant);
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        expect(screen.getByText('현재 상태에서 수행 가능한 액션이 없습니다.')).toBeInTheDocument();
      });
    });
  });

  describe('액션 실행', () => {
    it('구독 승인 액션이 실행되어야 한다', async () => {
      const mockApprove = vi.fn();
      mockUseApproveSubscription.mockReturnValue({
        mutateAsync: mockApprove,
        isPending: false
      });

      renderComponent(mockPendingTenant);
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const approveButton = screen.getByRole('button', { name: '승인' });
        fireEvent.click(approveButton);
      });
      
      await waitFor(() => {
        expect(screen.getByText('승인 확인')).toBeInTheDocument();
        
        const reasonInput = screen.getByPlaceholderText('승인 사유를 입력해주세요...');
        fireEvent.change(reasonInput, { target: { value: '정상적인 신청으로 승인' } });
        
        const confirmButton = screen.getByRole('button', { name: '승인' });
        fireEvent.click(confirmButton);
      });
      
      expect(mockApprove).toHaveBeenCalledWith({
        subscriptionId: 2,
        reason: '정상적인 신청으로 승인'
      });
    });

    it('구독 거부 액션이 실행되어야 한다', async () => {
      const mockReject = vi.fn();
      mockUseRejectSubscription.mockReturnValue({
        mutateAsync: mockReject,
        isPending: false
      });

      renderComponent(mockPendingTenant);
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const rejectButton = screen.getByRole('button', { name: '거부' });
        fireEvent.click(rejectButton);
      });
      
      await waitFor(() => {
        expect(screen.getByText('거부 확인')).toBeInTheDocument();
        
        const reasonInput = screen.getByPlaceholderText('거부 사유를 입력해주세요...');
        fireEvent.change(reasonInput, { target: { value: '서류 미비로 거부' } });
        
        const confirmButton = screen.getByRole('button', { name: '거부' });
        fireEvent.click(confirmButton);
      });
      
      expect(mockReject).toHaveBeenCalledWith({
        subscriptionId: 2,
        reason: '서류 미비로 거부'
      });
    });

    it('구독 중지 액션이 실행되어야 한다', async () => {
      const mockSuspend = vi.fn();
      mockUseSuspendSubscription.mockReturnValue({
        mutateAsync: mockSuspend,
        isPending: false
      });

      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const suspendButton = screen.getByRole('button', { name: '중지' });
        fireEvent.click(suspendButton);
      });
      
      await waitFor(() => {
        expect(screen.getByText('중지 확인')).toBeInTheDocument();
        
        const reasonInput = screen.getByPlaceholderText('중지 사유를 입력해주세요...');
        fireEvent.change(reasonInput, { target: { value: '결제 지연으로 중지' } });
        
        const confirmButton = screen.getByRole('button', { name: '중지' });
        fireEvent.click(confirmButton);
      });
      
      expect(mockSuspend).toHaveBeenCalledWith({
        subscriptionId: 1,
        reason: '결제 지연으로 중지'
      });
    });

    it('구독 종료 액션이 실행되어야 한다', async () => {
      const mockTerminate = vi.fn();
      mockUseTerminateSubscription.mockReturnValue({
        mutateAsync: mockTerminate,
        isPending: false
      });

      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const terminateButton = screen.getByRole('button', { name: '종료' });
        fireEvent.click(terminateButton);
      });
      
      await waitFor(() => {
        expect(screen.getByText('종료 확인')).toBeInTheDocument();
        expect(screen.getByText('이 작업은 되돌릴 수 없습니다.')).toBeInTheDocument();
        
        const reasonInput = screen.getByPlaceholderText('종료 사유를 입력해주세요...');
        fireEvent.change(reasonInput, { target: { value: '계약 만료로 종료' } });
        
        const confirmButton = screen.getByRole('button', { name: '종료' });
        fireEvent.click(confirmButton);
      });
      
      expect(mockTerminate).toHaveBeenCalledWith({
        subscriptionId: 1,
        reason: '계약 만료로 종료'
      });
    });

    it('사유 없이는 액션 버튼이 비활성화되어야 한다', async () => {
      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const suspendButton = screen.getByRole('button', { name: '중지' });
        fireEvent.click(suspendButton);
      });
      
      await waitFor(() => {
        const confirmButton = screen.getByRole('button', { name: '중지' });
        expect(confirmButton).toBeDisabled();
      });
    });
  });

  describe('모달 닫기', () => {
    it('닫기 버튼 클릭 시 onClose가 호출되어야 한다', () => {
      renderComponent();
      
      const closeButton = screen.getByRole('button', { name: '닫기' });
      fireEvent.click(closeButton);
      
      expect(mockOnClose).toHaveBeenCalled();
    });

    it('액션 완료 후 모달이 닫혀야 한다', async () => {
      const mockSuspend = vi.fn().mockResolvedValue({});
      mockUseSuspendSubscription.mockReturnValue({
        mutateAsync: mockSuspend,
        isPending: false
      });

      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const suspendButton = screen.getByRole('button', { name: '중지' });
        fireEvent.click(suspendButton);
      });
      
      await waitFor(() => {
        const reasonInput = screen.getByPlaceholderText('중지 사유를 입력해주세요...');
        fireEvent.change(reasonInput, { target: { value: '결제 지연으로 중지' } });
        
        const confirmButton = screen.getByRole('button', { name: '중지' });
        fireEvent.click(confirmButton);
      });
      
      await waitFor(() => {
        expect(mockOnClose).toHaveBeenCalled();
      });
    });
  });

  describe('로딩 상태', () => {
    it('액션 실행 중 로딩 상태가 표시되어야 한다', async () => {
      mockUseSuspendSubscription.mockReturnValue({
        mutateAsync: vi.fn(),
        isPending: true
      });

      renderComponent();
      
      const actionsTab = screen.getByRole('tab', { name: '관리 액션' });
      fireEvent.click(actionsTab);
      
      await waitFor(() => {
        const suspendButton = screen.getByRole('button', { name: '중지' });
        fireEvent.click(suspendButton);
      });
      
      await waitFor(() => {
        const reasonInput = screen.getByPlaceholderText('중지 사유를 입력해주세요...');
        fireEvent.change(reasonInput, { target: { value: '결제 지연으로 중지' } });
        
        const confirmButton = screen.getByRole('button', { name: '처리 중...' });
        expect(confirmButton).toBeDisabled();
      });
    });
  });
});