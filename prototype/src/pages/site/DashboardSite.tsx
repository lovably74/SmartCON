import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  Users,
  CheckCircle2,
  AlertTriangle,
  Clock,
  MapPin,
  Thermometer,
  Cloud,
  TrendingUp,
  Calendar
} from "lucide-react";

export default function DashboardSite() {
  // Mock Data
  const siteInfo = {
    name: "강남 테헤란로 오피스 신축공사",
    location: "서울시 강남구 테헤란로 123",
    progress: 45,
    startDate: "2024-10-01",
    endDate: "2025-08-31",
    manager: "김철수"
  };

  const todayStats = {
    totalWorkers: 124,
    presentWorkers: 98,
    lateWorkers: 8,
    absentWorkers: 18,
    attendanceRate: 79
  };

  const weatherInfo = {
    temperature: 12,
    condition: "맑음",
    humidity: 45,
    windSpeed: 3.2,
    workable: true
  };

  const teams = [
    { name: "철근팀", leader: "이철수", total: 25, present: 22, late: 2, absent: 1 },
    { name: "콘크리트팀", leader: "박영희", total: 30, present: 28, late: 1, absent: 1 },
    { name: "목공팀", leader: "김민수", total: 20, present: 18, late: 1, absent: 1 },
    { name: "전기팀", leader: "정수진", total: 15, present: 12, late: 2, absent: 1 },
    { name: "배관팀", leader: "최동호", total: 18, present: 15, late: 1, absent: 2 },
    { name: "도장팀", leader: "한지영", total: 16, present: 13, late: 1, absent: 2 }
  ];

  const recentAlerts = [
    { type: "warning", message: "철근팀 출역 인원 부족 (목표 대비 -12%)", time: "15분 전" },
    { type: "info", message: "오늘 안전교육 14:00 예정", time: "30분 전" },
    { type: "alert", message: "안면인식기 #3 통신 장애", time: "1시간 전" },
    { type: "info", message: "자재 입고 완료 (철근 20톤)", time: "2시간 전" }
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        {/* 현장 정보 헤더 */}
        <Card>
          <CardContent className="p-6">
            <div className="flex flex-col lg:flex-row gap-6">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <MapPin className="h-5 w-5 text-muted-foreground" />
                  <h1 className="text-2xl font-bold">{siteInfo.name}</h1>
                </div>
                <p className="text-muted-foreground mb-4">{siteInfo.location}</p>
                
                <div className="grid md:grid-cols-3 gap-4">
                  <div>
                    <div className="text-sm text-muted-foreground">공정률</div>
                    <div className="flex items-center gap-2 mt-1">
                      <Progress value={siteInfo.progress} className="flex-1" />
                      <span className="font-bold text-lg">{siteInfo.progress}%</span>
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-muted-foreground">공사 기간</div>
                    <div className="font-semibold mt-1">{siteInfo.startDate} ~ {siteInfo.endDate}</div>
                  </div>
                  <div>
                    <div className="text-sm text-muted-foreground">현장 관리자</div>
                    <div className="font-semibold mt-1">{siteInfo.manager}</div>
                  </div>
                </div>
              </div>

              {/* 날씨 정보 */}
              <Card className="lg:w-80">
                <CardContent className="p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <Thermometer className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm font-medium">현장 날씨</span>
                    </div>
                    <Badge variant={weatherInfo.workable ? "default" : "destructive"}>
                      {weatherInfo.workable ? "작업 가능" : "작업 주의"}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-center">
                      <Cloud className="h-8 w-8 text-blue-500 mx-auto mb-1" />
                      <div className="text-2xl font-bold">{weatherInfo.temperature}°C</div>
                      <div className="text-xs text-muted-foreground">{weatherInfo.condition}</div>
                    </div>
                    <div className="text-xs space-y-1">
                      <div>습도: {weatherInfo.humidity}%</div>
                      <div>풍속: {weatherInfo.windSpeed}m/s</div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>

        {/* 출역 현황 KPI */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 인원</p>
                <div className="text-3xl font-bold mt-2">{todayStats.totalWorkers}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Users className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">출근 인원</p>
                <div className="text-3xl font-bold text-green-600 mt-2">{todayStats.presentWorkers}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <CheckCircle2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">지각 인원</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">{todayStats.lateWorkers}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <Clock className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">출역률</p>
                <div className="text-3xl font-bold text-purple-600 mt-2">{todayStats.attendanceRate}%</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <TrendingUp className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* 팀별 출역 현황 */}
          <Card className="lg:col-span-2">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>팀별 출역 현황</CardTitle>
              <Button variant="ghost" size="sm">전체보기</Button>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {teams.map((team, i) => (
                  <div key={i} className="flex items-center justify-between p-3 rounded-lg border">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm">
                        {team.name.charAt(0)}
                      </div>
                      <div>
                        <div className="font-semibold">{team.name}</div>
                        <div className="text-sm text-muted-foreground">{team.leader}</div>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right text-sm">
                        <div className="font-semibold">{team.present}/{team.total}</div>
                        <div className="text-muted-foreground">출근/총원</div>
                      </div>
                      <div className="flex gap-1">
                        {team.late > 0 && (
                          <Badge variant="secondary" className="text-xs">
                            지각 {team.late}
                          </Badge>
                        )}
                        {team.absent > 0 && (
                          <Badge variant="destructive" className="text-xs">
                            결근 {team.absent}
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* 실시간 알림 */}
          <Card>
            <CardHeader>
              <CardTitle>실시간 알림</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentAlerts.map((alert, i) => (
                  <div key={i} className="flex gap-3 items-start">
                    <div className={`mt-0.5 h-2 w-2 rounded-full shrink-0 ${
                      alert.type === "alert" ? "bg-red-500" : 
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

        {/* 빠른 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>빠른 액션</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Users className="h-6 w-6" />
                <span className="text-sm">출역 관리</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Calendar className="h-6 w-6" />
                <span className="text-sm">작업 일보</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <AlertTriangle className="h-6 w-6" />
                <span className="text-sm">안전 점검</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CheckCircle2 className="h-6 w-6" />
                <span className="text-sm">품질 검사</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}