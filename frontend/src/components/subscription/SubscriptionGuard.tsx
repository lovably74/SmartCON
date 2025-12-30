import React from 'react';
import { Navigate } from 'wouter';
import SubscriptionStatusDisplay, { SubscriptionStatus } from './SubscriptionStatusDisplay';

interface SubscriptionGuardProps {
  children: React.ReactNode;
  subscriptionStatus: SubscriptionStatus;
  rejectionReason?: string;
  suspensionReason?: string;
  terminationReason?: string;
  onReapply?: () => void;
  onNewSubscription?: () => void;
  redirectTo?: string;
  showStatusDisplay?: boolean;
}

/**
 * 구독 상태에 따른 서비스 접근을 제어하는 가드 컴포넌트
 * Requirements: 1.2, 6.1, 6.2, 6.3, 6.4
 */
const SubscriptionGuard: React.FC<SubscriptionGuardProps> = ({
  children,
  subscriptionStatus,
  rejectionReason,
  suspensionReason,
  terminationReason,
  onReapply,
  onNewSubscription,
  redirectTo = '/subscription',
  showStatusDisplay = true
}) => {
  // 서비스 접근이 허용되는 상태들
  const allowedStatuses: SubscriptionStatus[] = ['ACTIVE'];
  
  // 현재 상태가 허용된 상태인지 확인
  const isAccessAllowed = allowedStatuses.includes(subscriptionStatus);
  
  // 접근이 허용되지 않는 경우
  if (!isAccessAllowed) {
    // 상태 표시 컴포넌트를 보여줄지 결정
    if (showStatusDisplay) {
      return (
        <div className="container mx-auto p-6 max-w-2xl">
          <SubscriptionStatusDisplay
            status={subscriptionStatus}
            rejectionReason={rejectionReason}
            suspensionReason={suspensionReason}
            terminationReason={terminationReason}
            onReapply={onReapply}
            onNewSubscription={onNewSubscription}
          />
        </div>
      );
    } else {
      // 리다이렉션
      return <Navigate to={redirectTo} replace />;
    }
  }
  
  // 접근이 허용되는 경우 자식 컴포넌트 렌더링
  return <>{children}</>;
};

export default SubscriptionGuard;