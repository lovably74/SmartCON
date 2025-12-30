import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { fc } from '@fast-check/vitest';
import SubscriptionStatusDisplay, { SubscriptionStatus } from '../SubscriptionStatusDisplay';

/**
 * 테넌트 UI 동작에 대한 속성 기반 테스트
 * Feature: subscription-approval-workflow
 * Properties: 23, 24, 25, 26
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4
 */
describe('SubscriptionStatusDisplay Property Tests', () => {
  // 구독 상태 생성기
  const subscriptionStatusArb = fc.constantFrom(
    'PENDING_APPROVAL',
    'REJECTED', 
    'SUSPENDED',
    'TERMINATED',
    'ACTIVE',
    'CANCELLED',
    'EXPIRED'
  ) as fc.Arbitrary<SubscriptionStatus>;

  // 사유 텍스트 생성기
  const reasonArb = fc.string({ minLength: 10, maxLength: 200 });

  // 연락처 정보 생성기
  const emailArb = fc.emailAddress();
  const phoneArb = fc.string({ minLength: 10, maxLength: 15 });

  /**
   * Property 23: Pending Status UI Behavior
   * For any PENDING_APPROVAL subscription, the system should display "승인 대기 중" message and block service access
   * Validates: Requirements 6.1
   */
  it('Property 23: PENDING_APPROVAL 상태는 항상 승인 대기 메시지를 표시해야 함', () => {
    fc.assert(
      fc.property(emailArb, phoneArb, (contactEmail, contactPhone) => {
        const { container } = render(
          <SubscriptionStatusDisplay
            status="PENDING_APPROVAL"
            contactEmail={contactEmail}
            contactPhone={contactPhone}
          />
        );

        // 승인 대기 중 제목이 표시되어야 함
        expect(screen.getByText('승인 대기 중')).toBeInTheDocument();
        
        // 승인 대기 관련 메시지가 포함되어야 함
        expect(screen.getByText(/관리자가 구독 신청을 검토 중입니다/)).toBeInTheDocument();
        
        // 시계 아이콘이 표시되어야 함 (Clock 컴포넌트)
        const clockIcon = container.querySelector('svg');
        expect(clockIcon).toBeInTheDocument();
        
        // 연락처 정보가 표시되어야 함
        expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
        expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
        
        // 노란색 테마가 적용되어야 함
        const card = container.querySelector('.bg-yellow-50');
        expect(card).toBeInTheDocument();
      })
    );
  });

  /**
   * Property 24: Rejected Status UI Behavior  
   * For any REJECTED subscription, the system should display rejection reason and provide reapplication option
   * Validates: Requirements 6.2
   */
  it('Property 24: REJECTED 상태는 항상 거부 사유와 재신청 옵션을 제공해야 함', () => {
    fc.assert(
      fc.property(reasonArb, emailArb, phoneArb, (rejectionReason, contactEmail, contactPhone) => {
        const mockReapply = vi.fn();
        
        const { container } = render(
          <SubscriptionStatusDisplay
            status="REJECTED"
            rejectionReason={rejectionReason}
            onReapply={mockReapply}
            contactEmail={contactEmail}
            contactPhone={contactPhone}
          />
        );

        // 승인 거부 제목이 표시되어야 함
        expect(screen.getByText('승인 거부')).toBeInTheDocument();
        
        // 거부 사유가 표시되어야 함
        expect(screen.getByText(rejectionReason)).toBeInTheDocument();
        
        // 재신청 버튼이 표시되어야 함
        const reapplyButton = screen.getByText('재신청하기');
        expect(reapplyButton).toBeInTheDocument();
        
        // 재신청 버튼 클릭 시 콜백이 호출되어야 함
        fireEvent.click(reapplyButton);
        expect(mockReapply).toHaveBeenCalledTimes(1);
        
        // 이메일/전화 문의 버튼이 표시되어야 함
        expect(screen.getByText('이메일 문의')).toBeInTheDocument();
        expect(screen.getByText('전화 문의')).toBeInTheDocument();
        
        // X 아이콘이 표시되어야 함 (XCircle 컴포넌트)
        const xIcon = container.querySelector('svg');
        expect(xIcon).toBeInTheDocument();
        
        // 빨간색 테마가 적용되어야 함
        const card = container.querySelector('.bg-red-50');
        expect(card).toBeInTheDocument();
      })
    );
  });

  /**
   * Property 25: Suspended Status UI Behavior
   * For any SUSPENDED subscription, the system should display suspension reason and contact information
   * Validates: Requirements 6.3
   */
  it('Property 25: SUSPENDED 상태는 항상 중지 사유와 연락처 정보를 표시해야 함', () => {
    fc.assert(
      fc.property(reasonArb, emailArb, phoneArb, (suspensionReason, contactEmail, contactPhone) => {
        const { container } = render(
          <SubscriptionStatusDisplay
            status="SUSPENDED"
            suspensionReason={suspensionReason}
            contactEmail={contactEmail}
            contactPhone={contactPhone}
          />
        );

        // 서비스 일시 중지 제목이 표시되어야 함
        expect(screen.getByText('서비스 일시 중지')).toBeInTheDocument();
        
        // 중지 사유가 표시되어야 함
        expect(screen.getByText(suspensionReason)).toBeInTheDocument();
        
        // 이메일/전화 문의 버튼이 표시되어야 함
        expect(screen.getByText('이메일 문의')).toBeInTheDocument();
        expect(screen.getByText('전화 문의')).toBeInTheDocument();
        
        // 연락처 정보가 표시되어야 함
        expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
        expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
        
        // 경고 아이콘이 표시되어야 함 (AlertTriangle 컴포넌트)
        const alertIcon = container.querySelector('svg');
        expect(alertIcon).toBeInTheDocument();
        
        // 주황색 테마가 적용되어야 함
        const card = container.querySelector('.bg-orange-50');
        expect(card).toBeInTheDocument();
      })
    );
  });

  /**
   * Property 26: Terminated Status UI Behavior
   * For any TERMINATED subscription, the system should display termination notice and new subscription option
   * Validates: Requirements 6.4
   */
  it('Property 26: TERMINATED 상태는 항상 종료 안내와 새 구독 옵션을 제공해야 함', () => {
    fc.assert(
      fc.property(reasonArb, emailArb, phoneArb, (terminationReason, contactEmail, contactPhone) => {
        const mockNewSubscription = vi.fn();
        
        const { container } = render(
          <SubscriptionStatusDisplay
            status="TERMINATED"
            terminationReason={terminationReason}
            onNewSubscription={mockNewSubscription}
            contactEmail={contactEmail}
            contactPhone={contactPhone}
          />
        );

        // 구독 종료 제목이 표시되어야 함
        expect(screen.getByText('구독 종료')).toBeInTheDocument();
        
        // 종료 사유가 표시되어야 함
        expect(screen.getByText(terminationReason)).toBeInTheDocument();
        
        // 새 구독 신청 버튼이 표시되어야 함
        const newSubscriptionButton = screen.getByText('새 구독 신청');
        expect(newSubscriptionButton).toBeInTheDocument();
        
        // 새 구독 신청 버튼 클릭 시 콜백이 호출되어야 함
        fireEvent.click(newSubscriptionButton);
        expect(mockNewSubscription).toHaveBeenCalledTimes(1);
        
        // 연락처 정보가 표시되어야 함
        expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
        expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
        
        // 금지 아이콘이 표시되어야 함 (Ban 컴포넌트)
        const banIcon = container.querySelector('svg');
        expect(banIcon).toBeInTheDocument();
        
        // 회색 테마가 적용되어야 함
        const card = container.querySelector('.bg-gray-50');
        expect(card).toBeInTheDocument();
      })
    );
  });

  /**
   * 추가 속성: 정상 상태에서는 컴포넌트가 렌더링되지 않아야 함
   */
  it('정상 상태(ACTIVE, CANCELLED, EXPIRED)에서는 컴포넌트가 렌더링되지 않아야 함', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ACTIVE', 'CANCELLED', 'EXPIRED') as fc.Arbitrary<SubscriptionStatus>,
        (status) => {
          const { container } = render(
            <SubscriptionStatusDisplay status={status} />
          );
          
          // 컴포넌트가 렌더링되지 않아야 함 (빈 컨테이너)
          expect(container.firstChild).toBeNull();
        }
      )
    );
  });

  /**
   * 추가 속성: 모든 비정상 상태에서는 연락처 정보가 표시되어야 함
   */
  it('모든 비정상 상태에서는 연락처 정보가 표시되어야 함', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('PENDING_APPROVAL', 'REJECTED', 'SUSPENDED', 'TERMINATED') as fc.Arbitrary<SubscriptionStatus>,
        emailArb,
        phoneArb,
        (status, contactEmail, contactPhone) => {
          render(
            <SubscriptionStatusDisplay
              status={status}
              contactEmail={contactEmail}
              contactPhone={contactPhone}
            />
          );
          
          // 연락처 정보가 항상 표시되어야 함
          expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
          expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
          expect(screen.getByText(/고객센터 운영시간/)).toBeInTheDocument();
        }
      )
    );
  });
});