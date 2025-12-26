import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Calendar as CalendarIcon, Download, Filter, Search, UserCheck } from "lucide-react";

export default function AttendanceSite() {
  const attendance = [
    { id: 1, name: "홍길동", team: "A팀(형틀)", timeIn: "06:45", timeOut: "-", status: "작업중", gongsu: 1.0, method: "안면인식" },
    { id: 2, name: "김철수", team: "A팀(형틀)", timeIn: "06:50", timeOut: "-", status: "작업중", gongsu: 1.0, method: "안면인식" },
    { id: 3, name: "이영희", team: "B팀(철근)", timeIn: "06:40", timeOut: "17:00", status: "퇴근", gongsu: 1.0, method: "안면인식" },
    { id: 4, name: "박민수", team: "B팀(철근)", timeIn: "06:55", timeOut: "19:00", status: "퇴근", gongsu: 1.5, method: "수기입력" },
    { id: 5, name: "Zhang Wei", team: "C팀(조적)", timeIn: "07:00", timeOut: "-", status: "작업중", gongsu: 1.0, method: "안면인식" },
    { id: 6, name: "최준호", team: "C팀(조적)", timeIn: "06:30", timeOut: "-", status: "작업중", gongsu: 1.0, method: "안면인식" },
    { id: 7, name: "정다은", team: "안전팀", timeIn: "08:00", timeOut: "-", status: "작업중", gongsu: 1.0, method: "안면인식" },
    { id: 8, name: "Nguyen Van", team: "D팀(비계)", timeIn: "07:10", timeOut: "12:00", status: "조퇴", gongsu: 0.5, method: "안면인식" },
    { id: 9, name: "강호동", team: "장비팀", timeIn: "06:20", timeOut: "-", status: "작업중", gongsu: 1.0, method: "안면인식" },
    { id: 10, name: "유재석", team: "안전팀", timeIn: "07:50", timeOut: "17:00", status: "퇴근", gongsu: 1.0, method: "안면인식" },
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">출역 관리</h2>
            <p className="text-muted-foreground">금일 현장 출역 현황을 실시간으로 모니터링합니다.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline">
              <CalendarIcon className="mr-2 h-4 w-4" /> 날짜 선택
            </Button>
            <Button variant="outline">
              <Download className="mr-2 h-4 w-4" /> 엑셀 다운로드
            </Button>
            <Button>
              <UserCheck className="mr-2 h-4 w-4" /> 수기 출근 등록
            </Button>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">총 출역 인원</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">145명</div>
              <p className="text-xs text-muted-foreground">계획 대비 96%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">작업 중</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-600">112명</div>
              <p className="text-xs text-muted-foreground">현재 현장 체류</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">퇴근 완료</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-blue-600">33명</div>
              <p className="text-xs text-muted-foreground">정상 퇴근 처리</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">예외 발생</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">2건</div>
              <p className="text-xs text-muted-foreground">미인식 / 중복 등</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>출역 상세 내역</CardTitle>
              <div className="flex w-full max-w-md items-center space-x-2">
                <Button variant="outline" size="icon">
                  <Filter className="h-4 w-4" />
                </Button>
                <Input placeholder="이름 또는 팀명 검색" />
                <Button size="icon" variant="ghost">
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>이름</TableHead>
                  <TableHead>소속 팀</TableHead>
                  <TableHead>출근 시간</TableHead>
                  <TableHead>퇴근 시간</TableHead>
                  <TableHead>인증 방식</TableHead>
                  <TableHead>공수</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {attendance.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        <Avatar className="h-8 w-8">
                          <AvatarImage src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${item.id}`} />
                          <AvatarFallback>{item.name.charAt(0)}</AvatarFallback>
                        </Avatar>
                        {item.name}
                      </div>
                    </TableCell>
                    <TableCell>{item.team}</TableCell>
                    <TableCell>{item.timeIn}</TableCell>
                    <TableCell>{item.timeOut}</TableCell>
                    <TableCell>
                      <span className="text-xs text-muted-foreground">{item.method}</span>
                    </TableCell>
                    <TableCell>{item.gongsu}</TableCell>
                    <TableCell>
                      <Badge variant={
                        item.status === "작업중" ? "default" :
                        item.status === "퇴근" ? "secondary" : "outline"
                      } className={
                        item.status === "작업중" ? "bg-green-500 hover:bg-green-600" : ""
                      }>
                        {item.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm">수정</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}

