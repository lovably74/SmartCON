import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Calendar, Cloud, CloudRain, Save, Sun } from "lucide-react";

export default function ReportsSite() {
  const dailyWork = [
    { id: 1, team: "A팀(형틀)", work: "지하 1층 옹벽 거푸집 조립", workers: 12, equipment: "크레인 1대" },
    { id: 2, team: "B팀(철근)", work: "지상 1층 슬라브 철근 배근", workers: 8, equipment: "-" },
    { id: 3, team: "C팀(조적)", work: "2층 조적 쌓기 및 미장", workers: 6, equipment: "지게차 1대" },
    { id: 4, team: "D팀(비계)", work: "외부 비계 해체 작업", workers: 5, equipment: "-" },
    { id: 5, team: "전기팀", work: "지하층 배관 배선 작업", workers: 4, equipment: "-" },
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">작업 일보</h2>
            <p className="text-muted-foreground">금일 현장 작업 내용 및 특이사항을 기록합니다.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline">
              <Calendar className="mr-2 h-4 w-4" /> 2025.12.18 (목)
            </Button>
            <Button>
              <Save className="mr-2 h-4 w-4" /> 일보 저장
            </Button>
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle>공종별 작업 내용</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[120px]">팀명</TableHead>
                    <TableHead>작업 내용</TableHead>
                    <TableHead className="w-[80px]">인원</TableHead>
                    <TableHead className="w-[150px]">장비 사용</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {dailyWork.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">{item.team}</TableCell>
                      <TableCell>
                        <Input defaultValue={item.work} className="h-8" />
                      </TableCell>
                      <TableCell>{item.workers}명</TableCell>
                      <TableCell>
                        <Input defaultValue={item.equipment} className="h-8" />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <div className="mt-4">
                <Button variant="outline" size="sm" className="w-full">
                  + 작업 내용 추가
                </Button>
              </div>
            </CardContent>
          </Card>

          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>날씨 및 환경</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Sun className="h-5 w-5 text-orange-500" />
                    <span className="font-medium">맑음</span>
                  </div>
                  <div className="text-sm text-muted-foreground">최저 -2°C / 최고 5°C</div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label className="text-xs text-muted-foreground">오전 날씨</Label>
                    <div className="flex items-center gap-2 p-2 border rounded bg-muted/50">
                      <Cloud className="h-4 w-4" /> 흐림
                    </div>
                  </div>
                  <div className="space-y-1">
                    <Label className="text-xs text-muted-foreground">오후 날씨</Label>
                    <div className="flex items-center gap-2 p-2 border rounded bg-muted/50">
                      <Sun className="h-4 w-4 text-orange-500" /> 맑음
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>특이사항</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea 
                  placeholder="안전 사고, 자재 입고 지연 등 특이사항을 입력하세요." 
                  className="min-h-[150px]"
                />
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
