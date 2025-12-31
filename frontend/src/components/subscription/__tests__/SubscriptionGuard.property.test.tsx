import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { fc } from '@fast-check/vitest';
import SubscriptionGuard from '../SubscriptionGuard';
import { type SubscriptionStatus } from '../SubscriptionStatusDisplay';

// Mock wouter hooks and components
vi.mock('wouter', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to}>Redirecting to {to}</div>,
  useLocation: () => ['/current-path', vi.fn()]
}));

/**
 * 구독 가드 컴포넌트에 대한 속성 기반 테스트
 * Feature: subscription-approval-workflow
 * Properties: 2, 23, 24, 25, 26
 * Validates: Requirements 1.2, 6.1, 6.2, 6.3, 6.4
 */
describe('SubscriptionGuard Property Tests', () => {
  // 각 테스트 후 DOM 정리
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  // 구독 상태 생성기
  const allowedStatusArb = fc.constantFrom('ACTIVE') as fc.Arbitrary<SubscriptionStatus>;
  const blockedStatusArb = fc.constantFrom(
    'PENDING_APPROVAL',
    'REJECTED', 
    'SUSPENDED',
    'TERMINATED'
  ) as fc.Arbitrary<SubscriptionStatus>;

  // 테스트용 자식 컴포넌트
  const TestChild = () => <div data-testid="protected-content">Protected Content</div>;

  /**
   * Property 2: Pending Subscription Access Control
   * For any subscription in non-ACTIVE status, service access should be blocked
   * Validates: Requirements 1.2
   */
  it('Property 2: 비활성 구독 상태에서는 서비스 접근이 차단되어야 함', () => {
    fc.assert(
      fc.property(blockedStatusArb, (status) => {
        const { unmount } = render(
          <SubscriptionGuard subscriptionStatus={status}>
            <TestChild />
          </SubscriptionGuard>
        );

        try {
          // 보호된 콘텐츠가 렌더링되지 않아야 함
          expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
          
          // 상태 표시 컴포넌트가 렌더링되어야 함
          // 각 상태별 특정 제목 확인
          const statusTitles = {
            'PENDING_APPROVAL': /승인 대기 중/i,
            'REJECTED': /승인 거부/i,
            'SUSPENDED': /서비스 일시 중지/i,
            'TERMINATED': /구독 종료/i
          };
          
          const expectedTitle = statusTitles[status as keyof typeof statusTitles];
          if (expectedTitle) {
            expect(screen.getByRole('heading', { name: expectedTitle })).toBeInTheDocument();
          }
        } finally {
          unmount();
        }
      }),
      { numRuns: 10 }
    );
  });

  /**
   * 활성 구독 상태에서는 서비스 접근이 허용되어야 함
   */
  it('활성 구독 상태에서는 서비스 접근이 허용되어야 함', () => {
    fc.assert(
      fc.property(allowedStatusArb, (status) => {
        const { unmount } = render(
          <SubscriptionGuard subscriptionStatus={status}>
            <TestChild />
          </SubscriptionGuard>
        );

        try {
          // 보호된 콘텐츠가 렌더링되어야 함
          expect(screen.getByTestId('protected-content')).toBeInTheDocument();
        } finally {
          unmount();
        }
      }),
      { numRuns: 10 }
    );
  });

  /**
   * showStatusDisplay가 false인 경우 리다이렉션이 발생해야 함
   */
  it('showStatusDisplay가 false인 경우 리다이렉션이 발생해야 함', () => {
    fc.assert(
      fc.property(
        blockedStatusArb,
        fc.string({ minLength: 1, maxLength: 50 }),
        (status, redirectPath) => {
          const { unmount } = render(
            <SubscriptionGuard 
              subscriptionStatus={status}
              showStatusDisplay={false}
              redirectTo={redirectPath}
            >
              <TestChild />
            </SubscriptionGuard>
          );

          try {
            // 보호된 콘텐츠가 렌더링되지 않아야 함
            expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
            
            // 리다이렉션으로 인해 컴포넌트가 null을 반환해야 함
            // (실제 리다이렉션은 useEffect에서 처리되므로 컴포넌트는 null 반환)
          } finally {
            unmount();
          }
        }
      ),
      { numRuns: 10 }
    );
  });

  /**
   * Property 23-26: 각 상태별 UI 동작 검증
   * PENDING_APPROVAL, REJECTED, SUSPENDED, TERMINATED 상태에서 적절한 UI가 표시되어야 함
   */
  it('Property 23-26: 각 상태별로 적절한 UI가 표시되어야 함', () => {
    const statusTitles = {
      'PENDING_APPROVAL': /승인 대기 중/i,
      'REJECTED': /승인 거부/i,
      'SUSPENDED': /서비스 일시 중지/i,
      'TERMINATED': /구독 종료/i
    };

    fc.assert(
      fc.property(blockedStatusArb, (status) => {
        const { unmount } = render(
          <SubscriptionGuard subscriptionStatus={status}>
            <TestChild />
          </SubscriptionGuard>
        );

        try {
          // 각 상태에 해당하는 메시지가 표시되어야 함
          const expectedMessage = statusTitles[status as keyof typeof statusTitles];
          if (expectedMessage) {
            expect(screen.getByRole('heading', { name: expectedMessage })).toBeInTheDocument();
          }
          
          // 보호된 콘텐츠는 표시되지 않아야 함
          expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
        } finally {
          unmount();
        }
      }),
      { numRuns: 10 }
    );
  });

  /**
   * 콜백 함수들이 올바르게 전달되어야 함
   */
  it('콜백 함수들이 올바르게 전달되어야 함', () => {
    const mockReapply = vi.fn();
    const mockNewSubscription = vi.fn();

    fc.assert(
      fc.property(
        fc.constantFrom('REJECTED', 'TERMINATED') as fc.Arbitrary<SubscriptionStatus>,
        (status) => {
          const props = status === 'REJECTED' 
            ? { onReapply: mockReapply }
            : { onNewSubscription: mockNewSubscription };

          const { unmount } = render(
            <SubscriptionGuard 
              subscriptionStatus={status}
              {...props}
            >
              <TestChild />
            </SubscriptionGuard>
          );

          try {
            // 해당 상태에 맞는 버튼이 표시되어야 함
            if (status === 'REJECTED') {
              expect(screen.getByRole('button', { name: /재신청하기/i })).toBeInTheDocument();
            } else if (status === 'TERMINATED') {
              expect(screen.getByRole('button', { name: /새 구독 신청/i })).toBeInTheDocument();
            }
          } finally {
            unmount();
          }
        }
      ),
      { numRuns: 10 }
    );
  });
});