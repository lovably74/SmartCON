import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ApprovalsSuper from '../ApprovalsSuper';
import * as useAdminApi from '@/hooks/useAdminApi';

// Mock the hooks
vi.mock('@/hooks/useAdminApi');
vi.mock('wouter', () => ({
  useLocation: () => ['/super/approvals'],
  Link: ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  ),
}));

// Mock DashboardLayout
vi.mock('@/components/layout/DashboardLayout', () => ({
  default: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="dashboard-layout">{children}</div>
  ),
}));

// Mock toast
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockApprovalData = {
  content: [
    {
      id: 1,
      subscriptionId: 101,
      tenantId: 1,
      companyName: '테스트 회사',
      businessNo: '123-45-67890',
      representativeName: '홍길동',
      planId: 'basic',
      planName: '베이직 플랜',
      monthlyAmount: 50000,
      status: 'PENDING_APPROVAL',
      requestedAt: '2024-01-01T10:00:00',
      paymentMethod: '신용카드',
      isVerifiedTenant: true,
    },
    {
      id: 2,
      subscriptionId: 102,
      tenantId: 2,
      companyName: '테스트 회사2',
      businessNo: '987-65-43210',
      representativeName: '김철수',
      planId: 'premium',
      planName: '프리미엄 플랜',
      monthlyAmount: 100000,
      status: 'PENDING_APPROVAL',
      requestedAt: '2024-01-02T11:00:00',
      paymentMethod: '계좌이체',
      isVerifiedTenant: false,
    },
  ],
  totalElements: 2,
  totalPages: 1,
  first: true,
  last: true,
  numberOfElements: 2,
};

const mockHistoryData = [
  {
    id: 1,
    subscriptionId: 101,
    adminName: '관리자',
    fromStatus: 'PENDING_APPROVAL',
    toStatus: 'APPROVED',
    reason: '승인 완료',
    action: 'APPROVE',
    processedAt: '2024-01-01T12:00:00',
  },
];

describe('ApprovalsSuper', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    // Reset all mocks
    vi.clearAllMocks();
  });

  const renderComponent = () => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ApprovalsSuper />
      </QueryClientProvider>
    );
  };

  it('승인 대기 목록이 올바르게 렌더링되어야 한다', async () => {
    // Mock the hooks
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: mockApprovalData,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(useAdminApi.useApprovalHistory).mockReturnValue({
      data: mockHistoryData,
    } as any);

    vi.mocked(useAdminApi.useApproveSubscription).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    vi.mocked(useAdminApi.useRejectSubscription).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    renderComponent();

    // 페이지 제목 확인
    expect(screen.getByText('구독 승인 관리')).toBeInTheDocument();
    expect(screen.getByText('승인 대기 중인 구독 신청을 검토하고 승인/거부할 수 있습니다.')).toBeInTheDocument();

    // 대기 중인 구독 수 확인
    expect(screen.getByText('대기 중: 2건')).toBeInTheDocument();

    // 테이블 헤더 확인
    expect(screen.getByText('회사 정보')).toBeInTheDocument();
    expect(screen.getByText('요금제')).toBeInTheDocument();
    expect(screen.getByText('결제 정보')).toBeInTheDocument();
    expect(screen.getByText('신청일시')).toBeInTheDocument();
    expect(screen.getByText('상태')).toBeInTheDocument();
    expect(screen.getByText('관리')).toBeInTheDocument();

    // 첫 번째 승인 항목 확인
    expect(screen.getByText('테스트 회사')).toBeInTheDocument();
    expect(screen.getByText((content, element) => {
      return element?.textContent === '사업자번호: 123-45-67890';
    })).toBeInTheDocument();
    expect(screen.getByText('홍길동')).toBeInTheDocument();
    expect(screen.getByText('베이직 플랜')).toBeInTheDocument();
    expect(screen.getByText('₩50,000/월')).toBeInTheDocument();
    expect(screen.getByText('신용카드')).toBeInTheDocument();
    expect(screen.getByText('인증됨')).toBeInTheDocument();

    // 두 번째 승인 항목 확인
    expect(screen.getByText('테스트 회사2')).toBeInTheDocument();
    expect(screen.getByText((content, element) => {
      return element?.textContent === '사업자번호: 987-65-43210';
    })).toBeInTheDocument();
    expect(screen.getByText('김철수')).toBeInTheDocument();
    expect(screen.getByText('프리미엄 플랜')).toBeInTheDocument();
    expect(screen.getByText('₩100,000/월')).toBeInTheDocument();
    expect(screen.getByText('계좌이체')).toBeInTheDocument();

    // 승인/거부 버튼 확인
    const approveButtons = screen.getAllByText('승인');
    const rejectButtons = screen.getAllByText('거부');
    expect(approveButtons).toHaveLength(2);
    expect(rejectButtons).toHaveLength(2);
  });

  it('로딩 상태가 올바르게 표시되어야 한다', () => {
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as any);

    renderComponent();

    expect(screen.getByText('데이터를 불러오는 중...')).toBeInTheDocument();
  });

  it('에러 상태가 올바르게 표시되어야 한다', () => {
    const errorMessage = '데이터를 불러올 수 없습니다';
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: undefined,
      isLoading: false,
      error: new Error(errorMessage),
    } as any);

    renderComponent();

    expect(screen.getByText('데이터를 불러오는 중 오류가 발생했습니다.')).toBeInTheDocument();
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('승인 대기 목록이 비어있을 때 올바른 메시지가 표시되어야 한다', () => {
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: {
        ...mockApprovalData,
        content: [],
        totalElements: 0,
      },
      isLoading: false,
      error: null,
    } as any);

    renderComponent();

    expect(screen.getByText('승인 대기 중인 구독이 없습니다')).toBeInTheDocument();
    expect(screen.getByText('새로운 구독 신청이 있으면 여기에 표시됩니다.')).toBeInTheDocument();
  });

  it('승인 다이얼로그가 올바르게 작동해야 한다', async () => {
    const mockApprove = vi.fn().mockResolvedValue({});
    
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: mockApprovalData,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(useAdminApi.useApproveSubscription).mockReturnValue({
      mutateAsync: mockApprove,
      isPending: false,
    } as any);

    vi.mocked(useAdminApi.useRejectSubscription).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    renderComponent();

    // 첫 번째 승인 버튼 클릭
    const approveButtons = screen.getAllByText('승인');
    fireEvent.click(approveButtons[0]);

    // 승인 다이얼로그가 열렸는지 확인
    await waitFor(() => {
      expect(screen.getByText('구독 승인')).toBeInTheDocument();
      expect(screen.getByText('테스트 회사의 구독 신청을 승인하시겠습니까?')).toBeInTheDocument();
    });

    // 승인 사유 입력
    const reasonTextarea = screen.getByPlaceholderText('승인 사유를 입력해주세요...');
    fireEvent.change(reasonTextarea, { target: { value: '검토 완료 후 승인' } });

    // 승인 버튼 클릭
    const confirmButton = screen.getByRole('button', { name: /승인/ });
    fireEvent.click(confirmButton);

    // API 호출 확인
    await waitFor(() => {
      expect(mockApprove).toHaveBeenCalledWith({
        subscriptionId: 101,
        reason: '검토 완료 후 승인',
      });
    });
  });

  it('거부 다이얼로그가 올바르게 작동해야 한다', async () => {
    const mockReject = vi.fn().mockResolvedValue({});
    
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: mockApprovalData,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(useAdminApi.useApproveSubscription).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    vi.mocked(useAdminApi.useRejectSubscription).mockReturnValue({
      mutateAsync: mockReject,
      isPending: false,
    } as any);

    renderComponent();

    // 첫 번째 거부 버튼 클릭
    const rejectButtons = screen.getAllByText('거부');
    fireEvent.click(rejectButtons[0]);

    // 거부 다이얼로그가 열렸는지 확인
    await waitFor(() => {
      expect(screen.getByText('구독 거부')).toBeInTheDocument();
      expect(screen.getByText('테스트 회사의 구독 신청을 거부하시겠습니까?')).toBeInTheDocument();
    });

    // 거부 사유 입력
    const reasonTextarea = screen.getByPlaceholderText('거부 사유를 입력해주세요...');
    fireEvent.change(reasonTextarea, { target: { value: '서류 미비로 인한 거부' } });

    // 거부 버튼 클릭
    const confirmButton = screen.getByRole('button', { name: /거부/ });
    fireEvent.click(confirmButton);

    // API 호출 확인
    await waitFor(() => {
      expect(mockReject).toHaveBeenCalledWith({
        subscriptionId: 101,
        reason: '서류 미비로 인한 거부',
      });
    });
  });

  it('승인 이력 다이얼로그가 올바르게 작동해야 한다', async () => {
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: mockApprovalData,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(useAdminApi.useApprovalHistory).mockReturnValue({
      data: mockHistoryData,
    } as any);

    renderComponent();

    // 첫 번째 이력 보기 버튼 클릭 (Eye 아이콘)
    const historyButtons = screen.getAllByRole('button');
    const eyeButton = historyButtons.find(button => 
      button.querySelector('svg')?.getAttribute('data-testid') === 'eye-icon' ||
      button.textContent === ''
    );
    
    if (eyeButton) {
      fireEvent.click(eyeButton);
    }

    // 승인 이력 다이얼로그가 열렸는지 확인
    await waitFor(() => {
      expect(screen.getByText('승인 이력')).toBeInTheDocument();
      expect(screen.getByText('테스트 회사의 구독 승인 이력입니다.')).toBeInTheDocument();
    });

    // 이력 내용 확인
    expect(screen.getByText('PENDING_APPROVAL → APPROVED')).toBeInTheDocument();
    expect(screen.getByText('처리자: 관리자')).toBeInTheDocument();
    expect(screen.getByText('사유: 승인 완료')).toBeInTheDocument();
  });

  it('폼 유효성 검사가 올바르게 작동해야 한다', async () => {
    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: mockApprovalData,
      isLoading: false,
      error: null,
    } as any);

    vi.mocked(useAdminApi.useApproveSubscription).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    renderComponent();

    // 승인 버튼 클릭
    const approveButtons = screen.getAllByText('승인');
    fireEvent.click(approveButtons[0]);

    // 승인 다이얼로그에서 사유 없이 승인 시도
    await waitFor(() => {
      const confirmButton = screen.getByRole('button', { name: /승인/ });
      expect(confirmButton).toBeDisabled(); // 사유가 없으면 버튼이 비활성화되어야 함
    });

    // 사유 입력 후 버튼 활성화 확인
    const reasonTextarea = screen.getByPlaceholderText('승인 사유를 입력해주세요...');
    fireEvent.change(reasonTextarea, { target: { value: '승인 사유' } });

    await waitFor(() => {
      const confirmButton = screen.getByRole('button', { name: /승인/ });
      expect(confirmButton).not.toBeDisabled(); // 사유가 있으면 버튼이 활성화되어야 함
    });
  });

  it('페이지네이션이 올바르게 작동해야 한다', () => {
    const paginatedData = {
      ...mockApprovalData,
      totalPages: 3,
      first: false,
      last: false,
      number: 1, // 현재 페이지 (0-based)
    };

    vi.mocked(useAdminApi.usePendingApprovals).mockReturnValue({
      data: paginatedData,
      isLoading: false,
      error: null,
    } as any);

    renderComponent();

    // 페이지네이션 정보 확인
    expect(screen.getByText('2개 중 2개 표시')).toBeInTheDocument();

    // 이전/다음 버튼 확인
    expect(screen.getByText('이전')).toBeInTheDocument();
    expect(screen.getByText('다음')).toBeInTheDocument();
  });
});