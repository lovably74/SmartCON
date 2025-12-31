import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Activity,
  CreditCard,
  DollarSign,
  Users,
  TrendingUp,
  Server,
  AlertTriangle,
  Building2,
  Loader2
} from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useDashboardStats, useRecentTenants, useSystemHealth } from "@/hooks/useAdminApi";

export default function DashboardSuper() {
  const { data: stats, isLoading: statsLoading, error: statsError } = useDashboardStats();
  const { data: recentTenants, isLoading: tenantsLoading } = useRecentTenants();
  const { data: systemHealth } = useSystemHealth();

  if (statsLoading) {
    return (
      <DashboardLayout role="super">
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">데이터를 불러오는 중...</span>
        </div>
      </DashboardLayout>
    );
  }

  if (statsError) {
    return (
      <DashboardLayout role="super">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <AlertTriangle className="h-8 w-8 text-red-500 mx-auto mb-2" />
            <p className="text-red-600">데이터를 불러오는 중 오류가 발생했습니다.</p>
            <p className="text-sm text-muted-foreground mt-1">{statsError.message}</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'ACTIVE': return '활성';
      case 'SUSPENDED': return '중지';
      case 'TERMINATED': return '해지';
      default: return status;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'text-green-600';
      case 'SUSPENDED': return 'text-orange-600';
      case 'TERMINATED': return 'text-red-600';
      default: return 'text-gray-600';
    }
  };

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold tracking-tight">SaaS 운영 현황</h2>
          <div className="flex items-center gap-2">
            <span className={`flex h-2 w-2 rounded-full ${
              systemHealth === 'HEALTHY' ? 'bg-green-500' : 'bg-red-500'
            }`}></span>
            <span className="text-sm text-muted-foreground">
              {systemHealth === 'HEALTHY' ? 'All Systems Operational' : 'System Issues Detected'}
            </span>
          </div>
        </div>

        {/* SaaS Metrics */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">총 가입 고객사 (Tenant)</CardTitle>
              <Building2 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats?.totalTenants.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1 flex items-center text-green-600">
                <TrendingUp className="h-3 w-3 mr-1" /> 
                이번 달 신규: {stats?.newTenantsThisMonth}개
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">월간 반복 매출 (MRR)</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(stats?.monthlyRevenue || 0)}</div>
              <p className="text-xs text-muted-foreground mt-1">
                총 매출: {formatCurrency(stats?.totalRevenue || 0)}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">활성 사용자</CardTitle>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats?.totalUsers.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">
                이번 달 신규: {stats?.newUsersThisMonth}명
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">결제 현황</CardTitle>
              <CreditCard className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-600">{stats?.completedPayments}</div>
              <p className="text-xs text-muted-foreground mt-1">
                실패: {stats?.failedPayments} / 대기: {stats?.pendingPayments}
              </p>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {/* Recent Subscriptions */}
          <Card className="col-span-2">
            <CardHeader>
              <CardTitle>최근 구독 신청 현황</CardTitle>
            </CardHeader>
            <CardContent>
              {tenantsLoading ? (
                <div className="flex items-center justify-center h-32">
                  <Loader2 className="h-6 w-6 animate-spin" />
                  <span className="ml-2">데이터 로딩 중...</span>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>회사명</TableHead>
                      <TableHead>요금제</TableHead>
                      <TableHead>상태</TableHead>
                      <TableHead>신청일</TableHead>
                      <TableHead>사용자 수</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {recentTenants?.slice(0, 5).map((tenant) => (
                      <TableRow key={tenant.id}>
                        <TableCell className="font-medium">{tenant.companyName}</TableCell>
                        <TableCell>
                          <span className="px-2 py-1 rounded text-xs font-medium bg-blue-100 text-blue-700">
                            {tenant.planId}
                          </span>
                        </TableCell>
                        <TableCell>
                          <span className={`flex items-center gap-1.5 text-xs ${getStatusColor(tenant.status)}`}>
                            <span className={`h-1.5 w-1.5 rounded-full ${
                              tenant.status === 'ACTIVE' ? 'bg-green-600' : 
                              tenant.status === 'SUSPENDED' ? 'bg-orange-600' : 'bg-red-600'
                            }`}></span>
                            {getStatusText(tenant.status)}
                          </span>
                        </TableCell>
                        <TableCell className="text-muted-foreground text-sm">
                          {new Date(tenant.createdAt).toLocaleDateString('ko-KR')}
                        </TableCell>
                        <TableCell>{tenant.userCount}명</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>

          {/* System Health */}
          <Card>
            <CardHeader>
              <CardTitle>시스템 상태</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="flex items-center gap-2"><Server className="h-4 w-4" /> API Server</span>
                  <span className="text-green-600 font-medium">Normal</span>
                </div>
                <div className="h-2 bg-muted rounded-full overflow-hidden">
                  <div className="h-full bg-green-500 w-[98%]"></div>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="flex items-center gap-2"><Users className="h-4 w-4" /> Database</span>
                  <span className="text-green-600 font-medium">Normal</span>
                </div>
                <div className="h-2 bg-muted rounded-full overflow-hidden">
                  <div className="h-full bg-green-500 w-[95%]"></div>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="flex items-center gap-2"><CreditCard className="h-4 w-4" /> Payment Gateway</span>
                  <span className="text-green-600 font-medium">Normal</span>
                </div>
                <div className="h-2 bg-muted rounded-full overflow-hidden">
                  <div className="h-full bg-green-500 w-[100%]"></div>
                </div>
              </div>
              
              <div className="pt-4 border-t">
                <h4 className="text-sm font-medium mb-3">시스템 통계</h4>
                <div className="space-y-3 text-sm">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">활성 테넌트</span>
                    <span className="font-medium">{stats?.activeTenants}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">중지된 테넌트</span>
                    <span className="font-medium">{stats?.suspendedTenants}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">시스템 상태</span>
                    <span className="font-medium text-green-600">{systemHealth}</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}



