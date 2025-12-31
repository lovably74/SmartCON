import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Clock,
  CheckCircle2,
  AlertCircle,
  Calendar,
  MapPin,
  Users,
  FileText,
  Camera
} from "lucide-react";

export default function DashboardWorker() {
  // Mock Data
  const todayAttendance = {
    checkIn: "07:30",
    checkOut: null,
    workHours: "8시간 30분",
    status: "출근 중"
  };

  const weeklyStats = {
    totalDays: 5,
    attendedDays: 4,
    totalHours: 34,
    overtime: 2
  };

  const currentSite = {
    name: "강남 테헤란로 오피스 신축공사",
    location: "서울시 강남구 테헤란로 123",
    team: "철근팀",
    teamLeader: "김철수 팀장"
  };

  const recentNotices = [
    { title: "안전교육 필수 이수 안내", date: "2025-01-01", type: "important" },
    { title: "금주 작업 일정 변경 공지", date: "2024-12-30", type: "info" },
    { title: "연말 정산 서류 제출 안내", date: "2024-12-28", type: "info" },
  ];

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        {/* 오늘 출역 현황 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              오늘 출역 현황
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-4 gap-4">
              <div className="text-center p-4 bg-green-50 rounded-lg">
                <div className="text-2xl font-bold text-green-600">{todayAttendance.checkIn}</div>
                <div className="text-sm text-muted-foreground">출근 시간</div>
              </div>
              <div className="text-center p-4 bg-muted/50 rounded-lg">
                <div className="text-2xl font-bold text-muted-foreground">
                  {todayAttendance.checkOut || "--:--"}
                </div>
                <div className="text-sm text-muted-foreground">퇴근 시간</div>
              </div>
              <div className="text-center p-4 bg-blue-50 rounded-lg">
                <div className="text-2xl font-bold text-blue-600">{todayAttendance.workHours}</div>
                <div className="text-sm text-muted-foreground">근무 시간</div>
              </div>
              <div className="text-center p-4">
                <Badge variant={todayAttendance.status === "출근 중" ? "default" : "secondary"} className="text-sm px-3 py-1">
                  {todayAttendance.status}
                </Badge>
                <div className="mt-2">
                  <Button size="sm" className="w-full">
                    <Camera className="h-4 w-4 mr-2" />
                    퇴근 체크
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid gap-6 md:grid-cols-2">
          {/* 주간 출역 통계 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="h-5 w-5" />
                이번 주 출역 통계
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">출역 일수</span>
                <span className="font-semibold">{weeklyStats.attendedDays}/{weeklyStats.totalDays}일</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">총 근무 시간</span>
                <span className="font-semibold">{weeklyStats.totalHours}시간</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">연장 근무</span>
                <span className="font-semibold text-orange-600">{weeklyStats.overtime}시간</span>
              </div>
              <div className="pt-2 border-t">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">출역률</span>
                  <span className="text-lg font-bold text-green-600">
                    {Math.round((weeklyStats.attendedDays / weeklyStats.totalDays) * 100)}%
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 현재 배정 현장 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="h-5 w-5" />
                현재 배정 현장
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <div className="font-semibold">{currentSite.name}</div>
                <div className="text-sm text-muted-foreground mt-1">{currentSite.location}</div>
              </div>
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm">{currentSite.team}</span>
                <Badge variant="outline" className="text-xs">{currentSite.teamLeader}</Badge>
              </div>
              <div className="pt-2 border-t">
                <Button variant="outline" size="sm" className="w-full">
                  <MapPin className="h-4 w-4 mr-2" />
                  현장 위치 보기
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 공지사항 및 알림 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              공지사항 및 알림
            </CardTitle>
            <Button variant="ghost" size="sm">전체보기</Button>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {recentNotices.map((notice, i) => (
                <div key={i} className="flex items-center justify-between p-3 rounded-lg border hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className={`h-2 w-2 rounded-full ${
                      notice.type === "important" ? "bg-red-500" : "bg-blue-500"
                    }`} />
                    <div>
                      <div className="font-medium text-sm">{notice.title}</div>
                      <div className="text-xs text-muted-foreground">{notice.date}</div>
                    </div>
                  </div>
                  {notice.type === "important" && (
                    <AlertCircle className="h-4 w-4 text-red-500" />
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* 빠른 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>빠른 액션</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Calendar className="h-6 w-6" />
                <span className="text-sm">출역 조회</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <FileText className="h-6 w-6" />
                <span className="text-sm">전자 계약</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Users className="h-6 w-6" />
                <span className="text-sm">팀원 현황</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CheckCircle2 className="h-6 w-6" />
                <span className="text-sm">안전교육</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}