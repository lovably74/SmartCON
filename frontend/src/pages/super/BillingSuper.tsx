import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { CreditCard, DollarSign, Download, Loader2, AlertTriangle } from "lucide-react";
import { useBillingStats } from "@/hooks/useAdminApi";

export default function BillingSuper() {
  const { data: billingStats, isLoading, error } = useBillingStats();

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const calculateSuccessRate = () => {
    if (!billingStats) return 0;
    const total = billingStats.totalPayments;
    if (total === 0) return 0;
    return ((billingStats.completedPayments / total) * 100).toFixed(1);
  };

  const calculateOutstanding = () => {
    if (!billingStats) return 0;
    return billingStats.failedPayments + billingStats.pendingPayments;
  };

  if (isLoading) {
    return (
      <DashboardLayout role="super">
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">데이터를 불러오는 중...</span>
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout role="super">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <AlertTriangle className="h-8 w-8 text-red-500 mx-auto mb-2" />
            <p className="text-red-600">데이터를 불러오는 중 오류가 발생했습니다.</p>
            <p className="text-sm text-muted-foreground mt-1">{error.message}</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">구독/결제 관리</h2>
            <p className="text-muted-foreground">전체 매출 현황 및 결제 내역을 모니터링합니다.</p>
          </div>
          <Button variant="outline">
            <Download className="mr-2 h-4 w-4" /> 매출 리포트 다운로드
          </Button>
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">이번 달 MRR</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(billingStats?.monthlyRevenue || 0)}</div>
              <p className="text-xs text-muted-foreground mt-1">
                오늘: {formatCurrency(billingStats?.dailyRevenue || 0)}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">결제 성공률</CardTitle>
              <CreditCard className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{calculateSuccessRate()}%</div>
              <p className="text-xs text-muted-foreground mt-1">
                총 {billingStats?.totalPayments}건 중 {billingStats?.completedPayments}건 성공
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">미수금 현황</CardTitle>
              <DollarSign className="h-4 w-4 text-red-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-red-600">{calculateOutstanding()}건</div>
              <p className="text-xs text-muted-foreground mt-1">
                실패: {billingStats?.failedPayments}건 / 대기: {billingStats?.pendingPayments}건
              </p>
            </CardContent>
          </Card>
        </div>

        {/* 월별 매출 추이 */}
        {billingStats?.monthlyTrends && billingStats.monthlyTrends.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>월별 매출 추이</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {billingStats.monthlyTrends.map((trend, index) => (
                  <div key={index} className="flex items-center justify-between">
                    <span className="text-sm font-medium">
                      {trend.year}년 {trend.month}월
                    </span>
                    <span className="text-lg font-bold">
                      {formatCurrency(trend.revenue)}
                    </span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* 최근 실패한 결제 내역 */}
        <Card>
          <CardHeader>
            <CardTitle>최근 결제 실패 내역</CardTitle>
          </CardHeader>
          <CardContent>
            {billingStats?.recentFailedPayments && billingStats.recentFailedPayments.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>고객사</TableHead>
                    <TableHead>결제 금액</TableHead>
                    <TableHead>실패 사유</TableHead>
                    <TableHead>일시</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead className="text-right">액션</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {billingStats.recentFailedPayments.map((payment) => (
                    <TableRow key={payment.id}>
                      <TableCell className="font-medium">{payment.companyName}</TableCell>
                      <TableCell>{formatCurrency(payment.amount)}</TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {payment.failureReason}
                      </TableCell>
                      <TableCell className="text-sm">
                        {formatDate(payment.createdAt)}
                      </TableCell>
                      <TableCell>
                        <Badge variant="destructive">결제실패</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">재시도</Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                최근 결제 실패 내역이 없습니다.
              </div>
            )}
          </CardContent>
        </Card>

        {/* 전체 매출 통계 */}
        <Card>
          <CardHeader>
            <CardTitle>전체 매출 통계</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {formatCurrency(billingStats?.totalRevenue || 0)}
                </div>
                <p className="text-sm text-muted-foreground">총 매출</p>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold">
                  {billingStats?.totalPayments || 0}
                </div>
                <p className="text-sm text-muted-foreground">총 결제 건수</p>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {billingStats?.completedPayments || 0}
                </div>
                <p className="text-sm text-muted-foreground">성공한 결제</p>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">
                  {billingStats?.failedPayments || 0}
                </div>
                <p className="text-sm text-muted-foreground">실패한 결제</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}

