/**
 * 슈퍼관리자 API 관련 React Query 훅들
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api';
import { toast } from 'sonner';

// 대시보드 통계 조회
export function useDashboardStats() {
  return useQuery({
    queryKey: ['admin', 'dashboard', 'stats'],
    queryFn: async () => {
      const response = await apiClient.getDashboardStats();
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });
}

// 테넌트 목록 조회
export function useTenants(params?: {
  search?: string;
  status?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}) {
  return useQuery({
    queryKey: ['admin', 'tenants', params],
    queryFn: async () => {
      const response = await apiClient.getTenants(params);
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
  });
}

// 최근 테넌트 목록 조회
export function useRecentTenants() {
  return useQuery({
    queryKey: ['admin', 'tenants', 'recent'],
    queryFn: async () => {
      const response = await apiClient.getRecentTenants();
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
  });
}

// 결제 통계 조회
export function useBillingStats() {
  return useQuery({
    queryKey: ['admin', 'billing', 'stats'],
    queryFn: async () => {
      const response = await apiClient.getBillingStats();
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    refetchInterval: 60000, // 1분마다 자동 새로고침
  });
}

// 시스템 상태 확인
export function useSystemHealth() {
  return useQuery({
    queryKey: ['admin', 'system', 'health'],
    queryFn: async () => {
      const response = await apiClient.getSystemHealth();
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    refetchInterval: 10000, // 10초마다 자동 새로고침
  });
}

// 테넌트 상태 변경
export function useUpdateTenantStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ tenantId, status }: { tenantId: number; status: string }) => {
      const response = await apiClient.updateTenantStatus(tenantId, status);
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    onSuccess: () => {
      // 테넌트 관련 쿼리들을 무효화하여 새로고침
      queryClient.invalidateQueries({ queryKey: ['admin', 'tenants'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'stats'] });
      toast.success('테넌트 상태가 성공적으로 변경되었습니다.');
    },
    onError: (error: Error) => {
      toast.error(`테넌트 상태 변경 실패: ${error.message}`);
    },
  });
}

// 승인 대기 중인 구독 목록 조회
export function usePendingApprovals(params?: {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}) {
  return useQuery({
    queryKey: ['admin', 'subscriptions', 'pending', params],
    queryFn: async () => {
      const response = await apiClient.getPendingApprovals(params);
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
  });
}

// 구독 승인
export function useApproveSubscription() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ subscriptionId, reason }: { subscriptionId: number; reason: string }) => {
      const response = await apiClient.approveSubscription(subscriptionId, { reason });
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    onSuccess: () => {
      // 승인 관련 쿼리들을 무효화하여 새로고침
      queryClient.invalidateQueries({ queryKey: ['admin', 'subscriptions', 'pending'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'stats'] });
      toast.success('구독이 성공적으로 승인되었습니다.');
    },
    onError: (error: Error) => {
      toast.error(`구독 승인 실패: ${error.message}`);
    },
  });
}

// 구독 거부
export function useRejectSubscription() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ subscriptionId, reason }: { subscriptionId: number; reason: string }) => {
      const response = await apiClient.rejectSubscription(subscriptionId, { reason });
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    onSuccess: () => {
      // 승인 관련 쿼리들을 무효화하여 새로고침
      queryClient.invalidateQueries({ queryKey: ['admin', 'subscriptions', 'pending'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'stats'] });
      toast.success('구독이 거부되었습니다.');
    },
    onError: (error: Error) => {
      toast.error(`구독 거부 실패: ${error.message}`);
    },
  });
}

// 승인 이력 조회
export function useApprovalHistory(subscriptionId: number) {
  return useQuery({
    queryKey: ['admin', 'subscriptions', subscriptionId, 'history'],
    queryFn: async () => {
      const response = await apiClient.getApprovalHistory(subscriptionId);
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    enabled: !!subscriptionId,
  });
}

// 구독 중지
export function useSuspendSubscription() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ subscriptionId, reason }: { subscriptionId: number; reason: string }) => {
      const response = await apiClient.suspendSubscription(subscriptionId, { reason });
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    onSuccess: () => {
      // 관련 쿼리들을 무효화하여 새로고침
      queryClient.invalidateQueries({ queryKey: ['admin', 'tenants'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'subscriptions'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'stats'] });
      toast.success('구독이 중지되었습니다.');
    },
    onError: (error: Error) => {
      toast.error(`구독 중지 실패: ${error.message}`);
    },
  });
}

// 구독 종료
export function useTerminateSubscription() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ subscriptionId, reason }: { subscriptionId: number; reason: string }) => {
      const response = await apiClient.terminateSubscription(subscriptionId, { reason });
      if (!response.success) {
        throw new Error(response.message);
      }
      return response.data;
    },
    onSuccess: () => {
      // 관련 쿼리들을 무효화하여 새로고침
      queryClient.invalidateQueries({ queryKey: ['admin', 'tenants'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'subscriptions'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'stats'] });
      toast.success('구독이 종료되었습니다.');
    },
    onError: (error: Error) => {
      toast.error(`구독 종료 실패: ${error.message}`);
    },
  });
}