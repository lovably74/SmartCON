/**
 * API 클라이언트 유틸리티
 */

const API_BASE_URL = 'http://localhost:8080/api';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface DashboardStats {
  totalTenants: number;
  activeTenants: number;
  suspendedTenants: number;
  newTenantsThisMonth: number;
  totalUsers: number;
  newUsersThisMonth: number;
  totalRevenue: number;
  monthlyRevenue: number;
  completedPayments: number;
  failedPayments: number;
  pendingPayments: number;
  systemStatus: string;
  activeConnections: number;
}

export interface TenantSummary {
  id: number;
  businessNo: string;
  companyName: string;
  representativeName: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'TERMINATED';
  planId: string;
  userCount: number;
  createdAt: string;
  lastLoginAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

export interface BillingStats {
  totalRevenue: number;
  monthlyRevenue: number;
  dailyRevenue: number;
  totalPayments: number;
  completedPayments: number;
  failedPayments: number;
  pendingPayments: number;
  monthlyTrends: MonthlyRevenue[];
  recentFailedPayments: FailedPayment[];
}

export interface MonthlyRevenue {
  year: number;
  month: number;
  revenue: number;
}

export interface FailedPayment {
  id: number;
  tenantId: string;
  companyName: string;
  amount: number;
  failureReason: string;
  createdAt: string;
}

class ApiClient {
  private async request<T>(endpoint: string, options?: RequestInit): Promise<ApiResponse<T>> {
    const url = `${API_BASE_URL}${endpoint}`;
    
    try {
      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...options?.headers,
        },
        ...options,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // 대시보드 통계 조회
  async getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
    return this.request<DashboardStats>('/api/v1/admin/dashboard/stats');
  }

  // 테넌트 목록 조회
  async getTenants(params?: {
    search?: string;
    status?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<ApiResponse<PageResponse<TenantSummary>>> {
    const searchParams = new URLSearchParams();
    
    if (params?.search) searchParams.append('search', params.search);
    if (params?.status) searchParams.append('status', params.status);
    if (params?.page !== undefined) searchParams.append('page', params.page.toString());
    if (params?.size !== undefined) searchParams.append('size', params.size.toString());
    if (params?.sortBy) searchParams.append('sortBy', params.sortBy);
    if (params?.sortDir) searchParams.append('sortDir', params.sortDir);

    const queryString = searchParams.toString();
    const endpoint = `/api/v1/admin/tenants${queryString ? `?${queryString}` : ''}`;
    
    return this.request<PageResponse<TenantSummary>>(endpoint);
  }

  // 테넌트 상태 변경
  async updateTenantStatus(tenantId: number, status: string): Promise<ApiResponse<void>> {
    return this.request<void>(`/api/v1/admin/tenants/${tenantId}/status?status=${status}`, {
      method: 'PATCH',
    });
  }

  // 결제 통계 조회
  async getBillingStats(): Promise<ApiResponse<BillingStats>> {
    return this.request<BillingStats>('/api/v1/admin/billing/stats');
  }

  // 최근 테넌트 목록 조회
  async getRecentTenants(): Promise<ApiResponse<TenantSummary[]>> {
    return this.request<TenantSummary[]>('/api/v1/admin/tenants/recent');
  }

  // 시스템 상태 확인
  async getSystemHealth(): Promise<ApiResponse<string>> {
    return this.request<string>('/api/v1/admin/system/health');
  }
}

export const apiClient = new ApiClient();