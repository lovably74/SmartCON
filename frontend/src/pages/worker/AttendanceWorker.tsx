import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Clock, Calendar } from "lucide-react";

export default function AttendanceWorker() {
  const records = [
    { date: "2025.12.18 (목)", timeIn: "06:45", timeOut: "17:00", gongsu: 1.0, status: "정상" },
    { date: "2025.12.17 (수)", time: "06:50", timeOut: "17:00", gongsu: 1.0, status: "정상" },
    { date: "2025.12.16 (화)", timeIn: "06:40", timeOut: "19:00", gongsu: 1.5, status: "연장" },
    { date: "2025.12.15 (월)", timeIn: "06:55", timeOut: "17:00", gongsu: 1.0, status: "정상" },
    { date: "2025.12.13 (토)", timeIn: "07:00", timeOut: "12:00", gongsu: 0.5, status: "오전" },
  ];

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">출역 조회</h2>
          <p className="text-muted-foreground">나의 출퇴근 기록 및 공수 내역을 확인합니다.</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>출퇴근 기록</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {records.map((record, i) => (
                <div key={i} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="space-y-1">
                    <div className="font-medium flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      {record.date}
                    </div>
                    <div className="text-sm text-muted-foreground flex items-center gap-2">
                      <Clock className="h-3 w-3" />
                      {record.timeIn} ~ {record.timeOut}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold">{record.gongsu} 공수</div>
                    <Badge variant="outline" className="mt-1">
                      {record.status}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}

