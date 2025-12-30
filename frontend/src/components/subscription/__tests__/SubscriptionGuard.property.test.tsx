import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { fc } from '@fast-check/vitest';
import SubscriptionGuard from '../SubscriptionGuard';
import { SubscriptionStatus } from '../SubscriptionStatusDisplay';

// Mock wouter Navigate component
vi.mock('wouter', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to}>Redirecting to {to}</div>
}));

/**
 * 구독 가드 컴포넌트에 대한 속성 기반 테스트
 * Feature: subscription-approval-workflow
 * Properties: 2, 23, 24, 25, 26
 * Validates: Requirements 1.2, 6.1, 6.2, 6.3, 6.4
 */
describe('SubscriptionGuard Property Tests', () => {
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
        render(
          <SubscriptionGuard subscriptionStatus={status}>
            <TestChild />
          </SubscriptionGuard>
        );

        // 보호된 콘텐츠가 렌더링되지 않아야 함
        expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
        
        // 상태 표시 컴포넌트가 렌더링되어야 함
        // (각 상태별 특정 텍스트는 SubscriptionStatusDisplay 테스트에서 검증)
        const statusDisplay = screen.getByRole('region', { hidden: true }) || 
                             screen.getByText(new RegExp(status.toLowerCase().replace('_', ' '), 'i'));
        expect(statusDisplay).toBeInTheDocument();
      })
    );
  });

  /**
   * 활성 구독 상태에서는 서비스 접근이 허용되어야 함
   */
  it('활성 구독 상태에서는 서비스 접근이 허용되어야 함', () => {
    fc.assert(
      fc.property(allowedStatusArb, (status) => {
        render(
          <SubscriptionGuard subscriptionStatus={status}>
            <TestChild />
          </SubscriptionGuard>
        );

        // 보호된 콘텐츠가 렌더링되어야 함
        expect(screen.getByTestId('protected-content')).toBeInTheDocument();
      })
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
          render(
            <SubscriptionGuard 
              subscriptionStatus={status}
              showStatusDisplay={false}
              redirectTo={redirectPath}
            >
              <TestChild />
            </SubscriptionGuard>
          );

          // 리다이렉션 컴포넌트가 렌더링되어야 함
          const navigate = screen.getByTestId('navigate');
          expect(navigate).toBeInTheDocument();
          expect(navigate).toHaveAttribute('data-to', redirectPath);
          
          // 보호된 콘텐츠가 렌더링되지 않아야 함
          expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
        }
      )
    );
  });

  /**
   * Property 23-26: 각 상태별 UI 동작 검증
   * PENDING_APPROVAL, REJECTED, SUSPENDED, TERMINATED 상태에서 적절한 UI가 표시되어야 함
   */
  it('Property 23-26: 각 상태별로 적절한 UI가 표시되어야 함', () => {
    const statusMessages = {
      'PENDING_APPROVAL': '승인 대기',
      'REJECTED': '승인 거부',
      'SUSPENDED': '일시 중지',
      'TERMINATED': '종료'
    };

    fc.assert(
      fc.property(blockedStatusArb, (status) => {
        render(
          <SubscriptionGuard subscriptionStatus={status}>
            <TestChild />
          </SubscriptionGuard>
        );

        // 각 상태에 해당하는 메시지가 표시되어야 함
        const expectedMessage = statusMessages[status as keyof typeof statusMessages];
        if (expectedMessage) {
          expect(screen.getByText(new RegExp(expectedMessage, 'i'))).toBeInTheDocument();
        }
        
        // 보호된 콘텐츠는 표시되지 않아야 함
        expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
      })
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

          render(
            <SubscriptionGuard 
              subscriptionStatus={status}
              {...props}
            >
              <TestChild />
            </SubscriptionGuard>
          );

          // 해당 상태에 맞는 버튼이 표시되어야 함
          if (status === 'REJECTED') {
            expect(screen.getByText('재신청하기')).toBeInTheDocument();
          } else if (status === 'TERMINATED') {
            expect(screen.getByText('새 구독 신청')).toBeInTheDocument();
          }
        }
      )
    );
  });
});