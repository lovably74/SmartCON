import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Calendar } from "@/components/ui/calendar";
import {
  Clock,
  CheckCircle2,
  XCircle,
  Calendar as CalendarIcon,
  MapPin,
  Camera,
  AlertTriangle
} from "lucide-react";
import { useState } from "react";

export default function AttendanceWorker() {
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());

  // Mock Data
  const monthlyAttendance = [
    { date: "2025-01-01", checkIn: "07:30", checkOut: "17:30", hours: 8, status: "정상", overtime: 0 },
    { date: "2024-12-31", checkIn: "07:45", checkOut: "18:00", hours: 8.25, status: "정상", overtime: 0.25 },
    { date: "2024-12-30", checkIn: "08:00", checkOut: "17:30", hours: 7.5, status: "지각", overtime: 0 },
    { date: "2024-12-29", checkIn: "07:30", checkOut: "19:00", hours: 9.5, status: "정상", overtime: 1.5 },
    { date: "2024-12-28", checkIn: "07:30", checkOut: "17:30", hours: 8, status: "정상", overtime: 0 },
  ];

  const todayStatus = {
    isCheckedIn: true,
    checkInTime: "07:30",
    currentHours: "8시간 30분",
    location: "강남 테헤란로 현장"
  };

  const monthlyStats = {
    totalDays: 22,
    attendedDays: 20,
    lateDays: 2,
    totalHours: 168,
    overtimeHours: 12
  };

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        {/* 오늘 출역 상태 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              오늘 출역 상태
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div className="flex items-center gap-4">
                  <div className={`h-12 w-12 rounded-full flex items-center justify-center ${
                    todayStatus.isCheckedIn ? "bg-green-100 text-green-600" : "bg-gray-100 text-gray-400"
                  }`}>
                    {todayStatus.isCheckedIn ? <CheckCircle2 className="h-6 w-6" /> : <Clock className="h-6 w-6" />}
                  </div>
                  <div>
                    <div className="font-semibold">
                      {todayStatus.isCheckedIn ? "출근 완료" : "출근 대기"}
                    </div>
                    <div className="text-sm text-muted-foreground">
                      {todayStatus.isCheckedIn ? `${todayStatus.checkInTime} 출근` : "아직 출근하지 않았습니다"}
                    </div>
                  </div>
                </div>
                
                {todayStatus.isCheckedIn && (
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">현재 근무 시간</span>
                      <span className="font-semibold">{todayStatus.currentHours}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">근무 장소</span>
                      <span className="font-semibold">{todayStatus.location}</span>
                    </div>
                  </div>
                )}
              </div>

              <div className="flex flex-col justify-center">
                {todayStatus.isCheckedIn ? (
                  <Button size="lg" className="w-full h-16 text-lg">
                    <Camera className="h-6 w-6 mr-2" />
                    퇴근 체크하기
                  </Button>
                ) : (
                  <Button size="lg" className="w-full h-16 text-lg">
                    <Camera className="h-6 w-6 mr-2" />
                    출근 체크하기
                  </Button>
                )}
                <div className="text-xs text-center text-muted-foreground mt-2">
                  안면인식으로 정확한 출퇴근 기록
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* 월간 출역 통계 */}
          <Card className="lg:col-span-1">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CalendarIcon className="h-5 w-5" />
                이번 달 통계
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center p-3 bg-blue-50 rounded-lg">
                  <div className="text-2xl font-bold text-blue-600">{monthlyStats.attendedDays}</div>
                  <div className="text-xs text-muted-foreground">출역 일수</div>
                </div>
                <div className="text-center p-3 bg-green-50 rounded-lg">
                  <div className="text-2xl font-bold text-green-600">{monthlyStats.totalHours}</div>
                  <div className="text-xs text-muted-foreground">총 시간</div>
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">출역률</span>
                  <span className="font-semibold">
                    {Math.round((monthlyStats.attendedDays / monthlyStats.totalDays) * 100)}%
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">지각 일수</span>
                  <span className="font-semibold text-orange-600">{monthlyStats.lateDays}일</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">연장 근무</span>
                  <span className="font-semibold text-purple-600">{monthlyStats.overtimeHours}시간</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 출역 기록 */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>최근 출역 기록</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {monthlyAttendance.map((record, i) => (
                  <div key={i} className="flex items-center justify-between p-3 rounded-lg border hover:bg-muted/50 transition-colors">
                    <div className="flex items-center gap-3">
                      <div className={`h-8 w-8 rounded-full flex items-center justify-center text-xs font-medium ${
                        record.status === "정상" ? "bg-green-100 text-green-600" : 
                        record.status === "지각" ? "bg-orange-100 text-orange-600" : 
                        "bg-red-100 text-red-600"
                      }`}>
                        {record.status === "정상" ? <CheckCircle2 className="h-4 w-4" /> : 
                         record.status === "지각" ? <AlertTriangle className="h-4 w-4" /> : 
                         <XCircle className="h-4 w-4" />}
                      </div>
                      <div>
                        <div className="font-medium text-sm">{record.date}</div>
                        <div className="text-xs text-muted-foreground">
                          {record.checkIn} ~ {record.checkOut}
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold text-sm">{record.hours}시간</div>
                      {record.overtime > 0 && (
                        <div className="text-xs text-purple-600">+{record.overtime}h</div>
                      )}
                      <Badge 
                        variant={record.status === "정상" ? "default" : "destructive"} 
                        className="text-xs mt-1"
                      >
                        {record.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 출역 캘린더 */}
        <Card>
          <CardHeader>
            <CardTitle>출역 캘린더</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex justify-center">
              <Calendar
                mode="single"
                selected={selectedDate}
                onSelect={setSelectedDate}
                className="rounded-md border"
              />
            </div>
            <div className="mt-4 flex justify-center gap-4 text-sm">
              <div className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full bg-green-500"></div>
                <span>정상 출역</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full bg-orange-500"></div>
                <span>지각</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full bg-red-500"></div>
                <span>결근</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}