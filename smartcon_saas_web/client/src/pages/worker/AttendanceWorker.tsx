import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Calendar } from "@/components/ui/calendar";
import { useState } from "react";

export default function AttendanceWorker() {
  const [date, setDate] = useState<Date | undefined>(new Date());

  const attendanceHistory = [
    { date: "2025.12.18", site: "강남 테헤란로 오피스", timeIn: "06:45", timeOut: "-", status: "작업중", gongsu: 1.0 },
    { date: "2025.12.17", site: "강남 테헤란로 오피스", timeIn: "06:50", timeOut: "17:00", status: "정상", gongsu: 1.0 },
    { date: "2025.12.16", site: "강남 테헤란로 오피스", timeIn: "06:48", timeOut: "17:00", status: "정상", gongsu: 1.0 },
    { date: "2025.12.15", site: "강남 테헤란로 오피스", timeIn: "06:55", timeOut: "19:00", status: "연장", gongsu: 1.5 },
    { date: "2025.12.14", site: "강남 테헤란로 오피스", timeIn: "06:40", timeOut: "17:00", status: "정상", gongsu: 1.0 },
  ];

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">출역 조회</h2>
          <p className="text-muted-foreground">나의 출퇴근 기록과 공수를 확인합니다.</p>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>출역 달력</CardTitle>
            </CardHeader>
            <CardContent className="flex justify-center">
              <Calendar
                mode="single"
                selected={date}
                onSelect={setDate}
                className="rounded-md border"
              />
            </CardContent>
          </Card>

          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>이번 달 누적 공수</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-4xl font-bold text-primary">22.5 공수</div>
                <p className="text-sm text-muted-foreground mt-2">예상 급여: ₩ 4,500,000</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>최근 출역 이력</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {attendanceHistory.map((item, i) => (
                    <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
                      <div>
                        <div className="font-medium">{item.date}</div>
                        <div className="text-sm text-muted-foreground">{item.site}</div>
                      </div>
                      <div className="text-right">
                        <div className="flex items-center gap-2 justify-end mb-1">
                          <Badge variant={item.status === "작업중" ? "default" : "secondary"}>
                            {item.status}
                          </Badge>
                          <span className="font-bold">{item.gongsu}공수</span>
                        </div>
                        <div className="text-xs text-muted-foreground">
                          {item.timeIn} ~ {item.timeOut}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
