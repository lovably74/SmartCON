import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { FileText, Plus, Calendar } from "lucide-react";

export default function ReportsSite() {
  const reports = [
    { id: 1, date: "2025.12.18", team: "A팀(형틀)", status: "제출완료", worker: 25 },
    { id: 2, date: "2025.12.18", team: "B팀(철근)", status: "제출완료", worker: 30 },
    { id: 3, date: "2025.12.18", team: "C팀(전기)", status: "작성중", worker: 12 },
    { id: 4, date: "2025.12.17", team: "A팀(형틀)", status: "제출완료", worker: 25 },
    { id: 5, date: "2025.12.17", team: "B팀(철근)", status: "제출완료", worker: 30 },
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">작업 일보</h2>
            <p className="text-muted-foreground">팀별 일일 작업 현황 및 보고서를 관리합니다.</p>
          </div>
          <Button>
            <Plus className="mr-2 h-4 w-4" /> 일보 작성
          </Button>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>작업 일보 목록</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {reports.map((report) => (
                <div key={report.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-4">
                    <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                      <FileText className="h-5 w-5 text-primary" />
                    </div>
                    <div>
                      <div className="font-medium">{report.date} - {report.team}</div>
                      <div className="text-sm text-muted-foreground">{report.worker}명 작업</div>
                    </div>
                  </div>
                  <Badge variant={report.status === "제출완료" ? "outline" : "secondary"}>
                    {report.status}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}

