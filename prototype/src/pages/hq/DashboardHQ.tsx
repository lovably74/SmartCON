import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import {
  Building2,
  CheckCircle2,
  FileText,
  Users,
  AlertCircle
} from "lucide-react";
import { Button } from "@/components/ui/button";

export default function DashboardHQ() {
  // Mock Data
  const stats = [
    { label: "운영 현장", value: "12", unit: "개", icon: Building2, color: "text-blue-600", bg: "bg-blue-100" },
    { label: "총 노무자", value: "1,245", unit: "명", icon: Users, color: "text-green-600", bg: "bg-green-100" },
    { label: "금일 출역", value: "856", unit: "명", icon: CheckCircle2, color: "text-orange-600", bg: "bg-orange-100" },
    { label: "계약 체결률", value: "98.5", unit: "%", icon: FileText, color: "text-purple-600", bg: "bg-purple-100" },
  ];

  const sites = [
    { name: "강남 테헤란로 오피스 신축공사", progress: 45, workers: 124, status: "정상" },
    { name: "판교 데이터센터 건립공사", progress: 12, workers: 85, status: "정상" },
    { name: "부산 에코델타시티 조성공사", progress: 78, workers: 230, status: "지연" },
    { name: "인천공항 제2터미널 확장공사", progress: 34, workers: 156, status: "정상" },
    { name: "세종 스마트시티 시범단지", progress: 5, workers: 45, status: "정상" },
  ];

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        {/* KPI Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat, index) => (
            <Card key={index}>
              <CardContent className="p-6 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{stat.label}</p>
                  <div className="flex items-baseline gap-1 mt-2">
                    <span className="text-3xl font-bold">{stat.value}</span>
                    <span className="text-sm text-muted-foreground">{stat.unit}</span>
                  </div>
                </div>
                <div className={`h-12 w-12 rounded-full flex items-center justify-center ${stat.bg} ${stat.color}`}>
                  <stat.icon className="h-6 w-6" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
          {/* Site Status List */}
          <Card className="col-span-4">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>현장별 운영 현황</CardTitle>
              <Button variant="ghost" size="sm">전체보기</Button>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {sites.map((site, i) => (
                  <div key={i} className="flex items-center justify-between">
                    <div className="space-y-1 min-w-[200px]">
                      <p className="text-sm font-medium leading-none">{site.name}</p>
                      <div className="flex items-center text-xs text-muted-foreground">
                        <Users className="mr-1 h-3 w-3" /> {site.workers}명 출역 중
                      </div>
                    </div>
                    <div className="flex-1 px-4">
                      <div className="flex items-center justify-between text-xs mb-1">
                        <span className="text-muted-foreground">공정률</span>
                        <span className="font-medium">{site.progress}%</span>
                      </div>
                      <Progress value={site.progress} className="h-2" />
                    </div>
                    <div className={`px-2 py-1 rounded text-xs font-medium ${
                      site.status === "정상" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
                    }`}>
                      {site.status}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Recent Alerts / Issues */}
          <Card className="col-span-3">
            <CardHeader>
              <CardTitle>주요 알림 및 이슈</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {[
                  { type: "alert", msg: "부산 현장: 금일 출역 인원 부족 (목표 대비 -15%)", time: "10분 전" },
                  { type: "info", msg: "강남 현장: 10월 기성 청구서 승인 완료", time: "1시간 전" },
                  { type: "warn", msg: "판교 현장: 안면인식기 통신 장애 발생", time: "2시간 전" },
                  { type: "info", msg: "신규 근로계약 15건 체결 완료", time: "3시간 전" },
                  { type: "alert", msg: "세종 현장: 우천으로 인한 작업 중지 보고", time: "어제" },
                ].map((item, i) => (
                  <div key={i} className="flex gap-3 items-start">
                    <div className={`mt-0.5 h-2 w-2 rounded-full shrink-0 ${
                      item.type === "alert" ? "bg-red-500" : item.type === "warn" ? "bg-orange-500" : "bg-blue-500"
                    }`} />
                    <div className="space-y-1">
                      <p className="text-sm leading-none">{item.msg}</p>
                      <p className="text-xs text-muted-foreground">{item.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}

