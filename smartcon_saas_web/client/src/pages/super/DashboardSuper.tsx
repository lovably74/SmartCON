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
  AlertTriangle
} from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export default function DashboardSuper() {
  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold tracking-tight">SaaS 운영 현황</h2>
          <div className="flex items-center gap-2">
            <span className="flex h-2 w-2 rounded-full bg-green-500"></span>
            <span className="text-sm text-muted-foreground">All Systems Operational</span>
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
              <div className="text-2xl font-bold">1,240</div>
              <p className="text-xs text-muted-foreground mt-1 flex items-center text-green-600">
                <TrendingUp className="h-3 w-3 mr-1" /> +12% (전월 대비)
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">월간 반복 매출 (MRR)</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 345.2M</div>
              <p className="text-xs text-muted-foreground mt-1 flex items-center text-green-600">
                <TrendingUp className="h-3 w-3 mr-1" /> +8.5% (전월 대비)
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">활성 사용자 (MAU)</CardTitle>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">45,231</div>
              <p className="text-xs text-muted-foreground mt-1">일일 활성(DAU): 32,100</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">결제 실패율</CardTitle>
              <AlertTriangle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">2.4%</div>
              <p className="text-xs text-muted-foreground mt-1">전월 대비 -0.5% 개선</p>
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
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>회사명</TableHead>
                    <TableHead>요금제</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>신청일</TableHead>
                    <TableHead className="text-right">액션</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {[
                    { name: "(주)태영건설", plan: "Enterprise", status: "승인대기", date: "2025.12.18" },
                    { name: "대림산업개발", plan: "Pro", status: "활성", date: "2025.12.18" },
                    { name: "한화건설부문", plan: "Pro", status: "활성", date: "2025.12.17" },
                    { name: "동부건설", plan: "Basic", status: "활성", date: "2025.12.17" },
                    { name: "계룡건설산업", plan: "Enterprise", status: "상담중", date: "2025.12.16" },
                  ].map((item, i) => (
                    <TableRow key={i}>
                      <TableCell className="font-medium">{item.name}</TableCell>
                      <TableCell>
                        <span className={`px-2 py-1 rounded text-xs font-medium ${
                          item.plan === "Enterprise" ? "bg-purple-100 text-purple-700" :
                          item.plan === "Pro" ? "bg-blue-100 text-blue-700" : "bg-gray-100 text-gray-700"
                        }`}>{item.plan}</span>
                      </TableCell>
                      <TableCell>
                        <span className={`flex items-center gap-1.5 text-xs ${
                          item.status === "활성" ? "text-green-600" : "text-orange-600"
                        }`}>
                          <span className={`h-1.5 w-1.5 rounded-full ${
                            item.status === "활성" ? "bg-green-600" : "bg-orange-600"
                          }`}></span>
                          {item.status}
                        </span>
                      </TableCell>
                      <TableCell className="text-muted-foreground text-sm">{item.date}</TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm">관리</Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
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
                  <span className="flex items-center gap-2"><Users className="h-4 w-4" /> FaceNet Engine</span>
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
                <h4 className="text-sm font-medium mb-3">최근 시스템 알림</h4>
                <div className="space-y-3 text-sm">
                  <div className="flex gap-2 text-muted-foreground">
                    <span className="text-xs bg-muted px-1.5 py-0.5 rounded">10:42</span>
                    <span>DB 백업 완료 (Duration: 45s)</span>
                  </div>
                  <div className="flex gap-2 text-muted-foreground">
                    <span className="text-xs bg-muted px-1.5 py-0.5 rounded">09:00</span>
                    <span>일일 배치 작업 정상 종료</span>
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

function Building2(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z" />
      <path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2" />
      <path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2" />
      <path d="M10 6h4" />
      <path d="M10 10h4" />
      <path d="M10 14h4" />
      <path d="M10 18h4" />
    </svg>
  )
}
