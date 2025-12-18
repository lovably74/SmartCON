import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import {
  Users,
  Clock,
  FileCheck,
  AlertTriangle,
  Sun,
  CloudRain,
  MoreVertical,
  CalendarDays
} from "lucide-react";

export default function DashboardSite() {
  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        {/* Header Info */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">강남 테헤란로 오피스 신축공사</h2>
            <p className="text-muted-foreground">현장 소장: 김철수 | 공사 기간: 2024.01.01 ~ 2025.12.31 (D-345)</p>
          </div>
          <div className="flex items-center gap-2 bg-white p-2 rounded-lg shadow-sm border">
            <Sun className="h-8 w-8 text-orange-500" />
            <div className="text-sm">
              <div className="font-medium">맑음 24°C</div>
              <div className="text-muted-foreground">습도 45% | 미세먼지 좋음</div>
            </div>
          </div>
        </div>

        {/* Daily Status Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">금일 출역 인원</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">145 / 150</div>
              <p className="text-xs text-muted-foreground mt-1">계획 대비 96.6% 투입</p>
              <Progress value={96.6} className="h-1.5 mt-2" />
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">출역 마감 현황</CardTitle>
              <Clock className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">12 / 15 팀</div>
              <p className="text-xs text-muted-foreground mt-1">3개 팀 마감 대기 중</p>
              <Progress value={80} className="h-1.5 mt-2 bg-orange-100 [&>div]:bg-orange-500"  />
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">근로계약 체결</CardTitle>
              <FileCheck className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">142 / 145</div>
              <p className="text-xs text-muted-foreground mt-1">미체결 3명 (서명 요청 중)</p>
              <Progress value={98} className="h-1.5 mt-2 bg-green-100 [&>div]:bg-green-500"  />
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">안전 교육 이수</CardTitle>
              <AlertTriangle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">100%</div>
              <p className="text-xs text-muted-foreground mt-1">신규자 교육 완료</p>
              <Progress value={100} className="h-1.5 mt-2 bg-blue-100 [&>div]:bg-blue-500"  />
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {/* Today's Work Teams */}
          <Card className="col-span-2">
            <CardHeader>
              <CardTitle>금일 작업 팀 현황</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {[
                  { team: "A팀 (철근)", leader: "박반장", count: 25, location: "101동 지하 2층", status: "작업중" },
                  { team: "B팀 (형틀)", leader: "이반장", count: 30, location: "102동 지상 3층", status: "작업중" },
                  { team: "C팀 (전기)", leader: "최반장", count: 12, location: "상가동 전체", status: "마감완료" },
                  { team: "D팀 (설비)", leader: "정반장", count: 15, location: "지하 주차장", status: "작업중" },
                  { team: "E팀 (미장)", leader: "강반장", count: 8, location: "101동 1층 로비", status: "작업중" },
                ].map((item, i) => (
                  <div key={i} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
                    <div className="flex items-center gap-4">
                      <div className="h-10 w-10 rounded-full bg-white border flex items-center justify-center font-bold text-muted-foreground">
                        {item.team.charAt(0)}
                      </div>
                      <div>
                        <div className="font-medium">{item.team}</div>
                        <div className="text-xs text-muted-foreground">
                          {item.leader} | {item.count}명 | {item.location}
                        </div>
                      </div>
                    </div>
                    <div className={`px-2 py-1 rounded text-xs font-medium ${
                      item.status === "마감완료" ? "bg-blue-100 text-blue-700" : "bg-green-100 text-green-700"
                    }`}>
                      {item.status}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>빠른 작업</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-2">
              <Button variant="outline" className="justify-start h-12">
                <CalendarDays className="mr-2 h-4 w-4" />
                명일 작업 계획 수립
              </Button>
              <Button variant="outline" className="justify-start h-12">
                <Users className="mr-2 h-4 w-4" />
                신규 팀장 초대하기
              </Button>
              <Button variant="outline" className="justify-start h-12">
                <FileCheck className="mr-2 h-4 w-4" />
                미서명 계약 일괄 요청
              </Button>
              <Button variant="outline" className="justify-start h-12">
                <MoreVertical className="mr-2 h-4 w-4" />
                공지사항 등록
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}

