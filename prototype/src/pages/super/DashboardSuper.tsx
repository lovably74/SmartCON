import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  Building2,
  Users,
  CreditCard,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  DollarSign,
  Activity
} from "lucide-react";

export default function DashboardSuper() {
  // Mock Data
  const systemStats = {
    totalTenants: 156,
    activeTenants: 142,
    totalUsers: 12450,
    monthlyRevenue: 45600000,
    systemUptime: 99.9
  };

  const recentTenants = [
    { name: "주식회사 대한건설", plan: "Enterprise", status: "active", users: 245, revenue: 2500000 },
    { name: "삼성건설 주식회사", plan: "Pro", status: "active", users: 156, revenue: 1800000 },
    { name: "현대건설 주식회사", plan: "Enterprise", status: "active", users: 320, revenue: 3200000 },
    { name: "GS건설 주식회사", plan: "Pro", status: "pending", users: 89, revenue: 1200000 },
    { name: "포스코건설", plan: "Basic", status: "active", users: 45, revenue: 450000 }
  ];

  const systemAlerts = [
    { type: "critical", message: "서버 #3 CPU 사용률 90% 초과", time: "5분 전" },
    { type: "warning", message: "데이터베이스 연결 지연 감지", time: "15분 전" },
    { type: "info", message: "신규 테넌트 가입: 주식회사 미래건설", time: "1시간 전" },
    { type: "info", message: "월간 정산 완료 (142개 테넌트)", time: "2시간 전" }
  ];

  const monthlyGrowth = [
    { month: "8월", tenants: 128, revenue: 38400000 },
    { month: "9월", tenants: 135, revenue: 40500000 },
    { month: "10월", tenants: 142, revenue: 42600000 },
    { month: "11월", tenants: 148, revenue: 44400000 },
    { month: "12월", tenants: 156, revenue: 45600000 }
  ];

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        {/* 시스템 KPI */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 테넌트</p>
                <div className="text-3xl font-bold mt-2">{systemStats.totalTenants}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Building2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">활성 테넌트</p>
                <div className="text-3xl font-bold text-green-600 mt-2">{systemStats.activeTenants}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <CheckCircle2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 사용자</p>
                <div className="text-3xl font-bold mt-2">{systemStats.totalUsers.toLocaleString()}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <Users className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">월 매출</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">
                  ₩{(systemStats.monthlyRevenue / 1000000).toFixed(0)}M
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <DollarSign className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">시스템 가동률</p>
                <div className="text-3xl font-bold text-cyan-600 mt-2">{systemStats.systemUptime}%</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-cyan-100 flex items-center justify-center text-cyan-600">
                <Activity className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* 최근 테넌트 현황 */}
          <Card className="lg:col-span-2">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>테넌트 현황</CardTitle>
              <Button variant="ghost" size="sm">전체보기</Button>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentTenants.map((tenant, i) => (
                  <div key={i} className="flex items-center justify-between p-3 rounded-lg border">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm">
                        {tenant.name.charAt(0)}
                      </div>
                      <div>
                        <div className="font-semibold">{tenant.name}</div>
                        <div className="text-sm text-muted-foreground">
                          {tenant.plan} • {tenant.users}명 사용자
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right text-sm">
                        <div className="font-semibold">₩{tenant.revenue.toLocaleString()}</div>
                        <div className="text-muted-foreground">월 구독료</div>
                      </div>
                      <Badge variant={tenant.status === "active" ? "default" : "secondary"}>
                        {tenant.status === "active" ? "활성" : "대기"}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* 시스템 알림 */}
          <Card>
            <CardHeader>
              <CardTitle>시스템 알림</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {systemAlerts.map((alert, i) => (
                  <div key={i} className="flex gap-3 items-start">
                    <div className={`mt-0.5 h-2 w-2 rounded-full shrink-0 ${
                      alert.type === "critical" ? "bg-red-500" : 
                      alert.type === "warning" ? "bg-orange-500" : "bg-blue-500"
                    }`} />
                    <div className="space-y-1">
                      <p className="text-sm leading-none">{alert.message}</p>
                      <p className="text-xs text-muted-foreground">{alert.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 성장 추이 */}
        <Card>
          <CardHeader>
            <CardTitle>월별 성장 추이</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-5 gap-4">
              {monthlyGrowth.map((data, i) => (
                <div key={i} className="text-center p-4 border rounded-lg">
                  <div className="text-sm text-muted-foreground mb-2">{data.month}</div>
                  <div className="space-y-2">
                    <div>
                      <div className="text-2xl font-bold text-blue-600">{data.tenants}</div>
                      <div className="text-xs text-muted-foreground">테넌트</div>
                    </div>
                    <div>
                      <div className="text-lg font-semibold text-green-600">
                        ₩{(data.revenue / 1000000).toFixed(0)}M
                      </div>
                      <div className="text-xs text-muted-foreground">매출</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* 빠른 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>시스템 관리</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Building2 className="h-6 w-6" />
                <span className="text-sm">테넌트 관리</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CreditCard className="h-6 w-6" />
                <span className="text-sm">결제 관리</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Activity className="h-6 w-6" />
                <span className="text-sm">시스템 모니터링</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <TrendingUp className="h-6 w-6" />
                <span className="text-sm">분석 리포트</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}