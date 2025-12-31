import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  CreditCard,
  DollarSign,
  TrendingUp,
  AlertTriangle,
  Download,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  Clock
} from "lucide-react";
import { useState } from "react";

export default function BillingSuper() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const billingStats = {
    totalRevenue: 45600000,
    monthlyRevenue: 45600000,
    dailyRevenue: 1520000,
    totalPayments: 156,
    completedPayments: 142,
    failedPayments: 8,
    pendingPayments: 6
  };

  const recentPayments = [
    {
      id: 1,
      tenantName: "주식회사 대한건설",
      amount: 2500000,
      status: "completed",
      date: "2025-01-01",
      plan: "Enterprise"
    },
    {
      id: 2,
      tenantName: "삼성건설 주식회사",
      amount: 1800000,
      status: "completed",
      date: "2025-01-01",
      plan: "Pro"
    },
    {
      id: 3,
      tenantName: "현대건설 주식회사",
      amount: 3200000,
      status: "failed",
      date: "2024-12-31",
      plan: "Enterprise",
      failureReason: "카드 한도 초과"
    },
    {
      id: 4,
      tenantName: "GS건설 주식회사",
      amount: 1200000,
      status: "pending",
      date: "2025-01-01",
      plan: "Pro"
    },
    {
      id: 5,
      tenantName: "포스코건설",
      amount: 450000,
      status: "completed",
      date: "2024-12-31",
      plan: "Basic"
    }
  ];

  const monthlyTrends = [
    { month: "8월", revenue: 38400000, payments: 128 },
    { month: "9월", revenue: 40500000, payments: 135 },
    { month: "10월", revenue: 42600000, payments: 142 },
    { month: "11월", revenue: 44400000, payments: 148 },
    { month: "12월", revenue: 45600000, payments: 156 }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed": return "bg-green-100 text-green-700";
      case "failed": return "bg-red-100 text-red-700";
      case "pending": return "bg-orange-100 text-orange-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "completed": return "완료";
      case "failed": return "실패";
      case "pending": return "대기";
      default: return "알 수 없음";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed": return <CheckCircle2 className="h-4 w-4" />;
      case "failed": return <XCircle className="h-4 w-4" />;
      case "pending": return <Clock className="h-4 w-4" />;
      default: return <AlertTriangle className="h-4 w-4" />;
    }
  };

  const filteredPayments = recentPayments.filter(payment =>
    payment.tenantName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        {/* 결제 통계 */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 매출</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  ₩{(billingStats.totalRevenue / 1000000).toFixed(0)}M
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <DollarSign className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">월 매출</p>
                <div className="text-3xl font-bold text-blue-600 mt-2">
                  ₩{(billingStats.monthlyRevenue / 1000000).toFixed(0)}M
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <TrendingUp className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">성공 결제</p>
                <div className="text-3xl font-bold text-purple-600 mt-2">
                  {billingStats.completedPayments}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <CheckCircle2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">실패 결제</p>
                <div className="text-3xl font-bold text-red-600 mt-2">
                  {billingStats.failedPayments}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-red-100 flex items-center justify-center text-red-600">
                <AlertTriangle className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* 최근 결제 내역 */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5" />
                최근 결제 내역
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex gap-4 mb-6">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="테넌트명으로 검색..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-10"
                  />
                </div>
                <Button variant="outline">
                  <Filter className="h-4 w-4 mr-2" />
                  필터
                </Button>
                <Button variant="outline">
                  <Download className="h-4 w-4 mr-2" />
                  엑셀 다운로드
                </Button>
              </div>

              <div className="space-y-4">
                {filteredPayments.map((payment) => (
                  <div key={payment.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex items-center gap-4">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm">
                        {payment.tenantName.charAt(0)}
                      </div>
                      <div>
                        <div className="font-semibold">{payment.tenantName}</div>
                        <div className="text-sm text-muted-foreground">
                          {payment.plan} • {payment.date}
                        </div>
                        {payment.status === "failed" && payment.failureReason && (
                          <div className="text-xs text-red-600 mt-1">
                            실패 사유: {payment.failureReason}
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <div className="font-semibold">₩{payment.amount.toLocaleString()}</div>
                        <div className="text-sm text-muted-foreground">{payment.plan}</div>
                      </div>
                      <Badge className={getStatusColor(payment.status)}>
                        {getStatusIcon(payment.status)}
                        <span className="ml-1">{getStatusText(payment.status)}</span>
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* 월별 매출 추이 */}
          <Card>
            <CardHeader>
              <CardTitle>월별 매출 추이</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {monthlyTrends.map((trend, i) => (
                  <div key={i} className="flex items-center justify-between p-3 rounded-lg border">
                    <div>
                      <div className="font-semibold">{trend.month}</div>
                      <div className="text-sm text-muted-foreground">{trend.payments}건</div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold text-green-600">
                        ₩{(trend.revenue / 1000000).toFixed(0)}M
                      </div>
                      <div className="text-xs text-muted-foreground">
                        {i > 0 && (
                          <span className={
                            trend.revenue > monthlyTrends[i-1].revenue 
                              ? "text-green-600" 
                              : "text-red-600"
                          }>
                            {trend.revenue > monthlyTrends[i-1].revenue ? "↗" : "↘"} 
                            {Math.abs(((trend.revenue - monthlyTrends[i-1].revenue) / monthlyTrends[i-1].revenue * 100)).toFixed(1)}%
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 결제 관리 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>결제 관리</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CreditCard className="h-6 w-6" />
                <span className="text-sm">결제 내역</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <AlertTriangle className="h-6 w-6" />
                <span className="text-sm">실패 결제 처리</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Download className="h-6 w-6" />
                <span className="text-sm">정산 보고서</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <TrendingUp className="h-6 w-6" />
                <span className="text-sm">매출 분석</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}