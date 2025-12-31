import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import SubscriptionStatusDisplay, { type SubscriptionStatus } from '../SubscriptionStatusDisplay';

/**
 * 테넌트 UI 동작에 대한 테스트
 * Feature: subscription-approval-workflow
 * Properties: 23, 24, 25, 26
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4
 */
describe('SubscriptionStatusDisplay Tests', () => {
  // 각 테스트 후 DOM 정리
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  /**
   * Property 23: Pending Status UI Behavior
   * For any PENDING_APPROVAL subscription, the system should display "승인 대기 중" message and block service access
   * Validates: Requirements 6.1
   */
  it('Property 23: PENDING_APPROVAL 상태는 항상 승인 대기 메시지를 표시해야 함', () => {
    const contactEmail = 'test@example.com';
    const contactPhone = '010-1234-5678';

    render(
      <SubscriptionStatusDisplay
        status="PENDING_APPROVAL"
        contactEmail={contactEmail}
        contactPhone={contactPhone}
      />
    );

    // 승인 대기 중 제목이 표시되어야 함
    expect(screen.getByRole('heading', { name: /승인 대기 중/i })).toBeInTheDocument();
    
    // 승인 대기 관련 메시지가 포함되어야 함
    expect(screen.getByText(/관리자가 구독 신청을 검토 중입니다/)).toBeInTheDocument();
    
    // 연락처 정보가 표시되어야 함
    expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
    expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
  });

  /**
   * Property 24: Rejected Status UI Behavior  
   * For any REJECTED subscription, the system should display rejection reason and provide reapplication option
   * Validates: Requirements 6.2
   */
  it('Property 24: REJECTED 상태는 항상 거부 사유와 재신청 옵션을 제공해야 함', () => {
    const rejectionReason = '서류 미비로 인한 승인 거부';
    const contactEmail = 'test@example.com';
    const contactPhone = '010-1234-5678';
    const mockReapply = vi.fn();
    
    render(
      <SubscriptionStatusDisplay
        status="REJECTED"
        rejectionReason={rejectionReason}
        onReapply={mockReapply}
        contactEmail={contactEmail}
        contactPhone={contactPhone}
      />
    );

    // 승인 거부 제목이 표시되어야 함
    expect(screen.getByRole('heading', { name: /승인 거부/i })).toBeInTheDocument();
    
    // 거부 사유가 표시되어야 함
    expect(screen.getByText(rejectionReason)).toBeInTheDocument();
    
    // 재신청 버튼이 표시되어야 함
    const reapplyButton = screen.getByRole('button', { name: /재신청하기/i });
    expect(reapplyButton).toBeInTheDocument();
    
    // 재신청 버튼 클릭 시 콜백이 호출되어야 함
    fireEvent.click(reapplyButton);
    expect(mockReapply).toHaveBeenCalledTimes(1);
    
    // 이메일/전화 문의 버튼이 표시되어야 함
    expect(screen.getByRole('button', { name: /이메일 문의/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /전화 문의/i })).toBeInTheDocument();
  });

  /**
   * Property 25: Suspended Status UI Behavior
   * For any SUSPENDED subscription, the system should display suspension reason and contact information
   * Validates: Requirements 6.3
   */
  it('Property 25: SUSPENDED 상태는 항상 중지 사유와 연락처 정보를 표시해야 함', () => {
    const suspensionReason = '결제 문제로 인한 서비스 중지';
    const contactEmail = 'test@example.com';
    const contactPhone = '010-1234-5678';

    render(
      <SubscriptionStatusDisplay
        status="SUSPENDED"
        suspensionReason={suspensionReason}
        contactEmail={contactEmail}
        contactPhone={contactPhone}
      />
    );

    // 서비스 일시 중지 제목이 표시되어야 함
    expect(screen.getByRole('heading', { name: /서비스 일시 중지/i })).toBeInTheDocument();
    
    // 중지 사유가 표시되어야 함
    expect(screen.getByText(suspensionReason)).toBeInTheDocument();
    
    // 이메일/전화 문의 버튼이 표시되어야 함
    expect(screen.getByRole('button', { name: /이메일 문의/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /전화 문의/i })).toBeInTheDocument();
    
    // 연락처 정보가 표시되어야 함
    expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
    expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
  });

  /**
   * Property 26: Terminated Status UI Behavior
   * For any TERMINATED subscription, the system should display termination notice and new subscription option
   * Validates: Requirements 6.4
   */
  it('Property 26: TERMINATED 상태는 항상 종료 안내와 새 구독 옵션을 제공해야 함', () => {
    const terminationReason = '약관 위반으로 인한 서비스 종료';
    const contactEmail = 'test@example.com';
    const contactPhone = '010-1234-5678';
    const mockNewSubscription = vi.fn();
    
    render(
      <SubscriptionStatusDisplay
        status="TERMINATED"
        terminationReason={terminationReason}
        onNewSubscription={mockNewSubscription}
        contactEmail={contactEmail}
        contactPhone={contactPhone}
      />
    );

    // 구독 종료 제목이 표시되어야 함
    expect(screen.getByRole('heading', { name: /구독 종료/i })).toBeInTheDocument();
    
    // 종료 사유가 표시되어야 함
    expect(screen.getByText(terminationReason)).toBeInTheDocument();
    
    // 새 구독 신청 버튼이 표시되어야 함
    const newSubscriptionButton = screen.getByRole('button', { name: /새 구독 신청/i });
    expect(newSubscriptionButton).toBeInTheDocument();
    
    // 새 구독 신청 버튼 클릭 시 콜백이 호출되어야 함
    fireEvent.click(newSubscriptionButton);
    expect(mockNewSubscription).toHaveBeenCalledTimes(1);
    
    // 연락처 정보가 표시되어야 함
    expect(screen.getByText(`이메일: ${contactEmail}`)).toBeInTheDocument();
    expect(screen.getByText(`전화: ${contactPhone}`)).toBeInTheDocument();
  });

  /**
   * 정상 상태에서는 컴포넌트가 렌더링되지 않아야 함
   */
  it('정상 상태(ACTIVE, CANCELLED, EXPIRED)에서는 컴포넌트가 렌더링되지 않아야 함', () => {
    const statuses: SubscriptionStatus[] = ['ACTIVE', 'CANCELLED', 'EXPIRED'];
    
    statuses.forEach(status => {
      const { container, unmount } = render(
        <SubscriptionStatusDisplay status={status} />
      );
      
      // 컴포넌트가 렌더링되지 않아야 함 (빈 컨테이너)
      expect(container.firstChild).toBeNull();
      
      unmount();
    });
  });

  /**
   * 모든 비정상 상태에서는 연락처 정보가 표시되어야 함
   */
  it('모든 비정상 상태에서는 연락처 정보가 표시되어야 함', () => {
    const statuses: SubscriptionStatus[] = ['PENDING_APPROVAL', 'REJECTED', 'SUSPENDED', 'TERMINATED'];
    const contactEmail = 'test@example.com';
    const contactPhone = '010-1234-5678';
    
    statuses.forEach(status => {
      const { unmount } = render(
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
      
      unmount();
    });
  });
});