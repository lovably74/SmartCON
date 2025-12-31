import { useEffect, useState } from 'react';
import { useLocation } from 'wouter';
import { SubscriptionAccessError } from '@/lib/api';

interface SubscriptionAccessState {
  isBlocked: boolean;
  subscriptionStatus?: string;
  message?: string;
  redirectUrl?: string;
}

/**
 * 구독 접근 제어를 위한 훅
 * API 요청 시 발생하는 구독 상태 오류를 전역적으로 처리합니다.
 */
export const useSubscriptionAccessControl = () => {
  const [, setLocation] = useLocation();
  const [accessState, setAccessState] = useState<SubscriptionAccessState>({
    isBlocked: false
  });

  useEffect(() => {
    const handleSubscriptionAccessDenied = (event: CustomEvent) => {
      const { subscriptionStatus, message, redirectUrl } = event.detail;
      
      console.warn('구독 접근 거부:', { subscriptionStatus, message, redirectUrl });
      
      setAccessState({
        isBlocked: true,
        subscriptionStatus,
        message,
        redirectUrl
      });

      // 리다이렉션 URL이 있으면 이동
      if (redirectUrl) {
        setLocation(redirectUrl);
      }
    };

    // 구독 접근 거부 이벤트 리스너 등록
    window.addEventListener('subscriptionAccessDenied', handleSubscriptionAccessDenied as EventListener);

    return () => {
      window.removeEventListener('subscriptionAccessDenied', handleSubscriptionAccessDenied as EventListener);
    };
  }, [setLocation]);

  /**
   * 접근 차단 상태 해제
   */
  const clearAccessBlock = () => {
    setAccessState({ isBlocked: false });
  };

  /**
   * 구독 접근 오류 처리
   */
  const handleSubscriptionError = (error: SubscriptionAccessError) => {
    setAccessState({
      isBlocked: true,
      subscriptionStatus: error.subscriptionStatus,
      message: error.message,
      redirectUrl: error.redirectUrl
    });

    if (error.redirectUrl) {
      setLocation(error.redirectUrl);
    }
  };

  return {
    accessState,
    clearAccessBlock,
    handleSubscriptionError
  };
};