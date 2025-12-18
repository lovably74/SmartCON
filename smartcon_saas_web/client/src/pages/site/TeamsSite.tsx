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
import { Filter, Plus, Search, Users } from "lucide-react";

export default function TeamsSite() {
  const workers = [
    { id: 1, name: "홍길동", job: "형틀목공", team: "A팀", status: "출역중", type: "내국인" },
    { id: 2, name: "김철수", job: "철근공", team: "B팀", status: "퇴근", type: "내국인" },
    { id: 3, name: "이영희", job: "전기공", team: "C팀", status: "출역중", type: "내국인" },
    { id: 4, name: "박민수", job: "배관공", team: "D팀", status: "결근", type: "내국인" },
    { id: 5, name: "Zhang Wei", job: "조적공", team: "E팀", status: "출역중", type: "외국인(H2)" },
    { id: 6, name: "최준호", job: "용접공", team: "F팀", status: "출역중", type: "내국인" },
    { id: 7, name: "정다은", job: "안전감시단", team: "안전팀", status: "출역중", type: "내국인" },
    { id: 8, name: "Nguyen Van", job: "비계공", team: "G팀", status: "휴가", type: "외국인(E9)" },
    { id: 9, name: "강호동", job: "크레인", team: "장비팀", status: "출역중", type: "내국인" },
    { id: 10, name: "유재석", job: "신호수", team: "안전팀", status: "퇴근", type: "내국인" },
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">팀/노무자 관리</h2>
            <p className="text-muted-foreground">현장 내 작업 팀과 노무자를 편성하고 관리합니다.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline">
              <Users className="mr-2 h-4 w-4" /> 팀 편성
            </Button>
            <Button>
              <Plus className="mr-2 h-4 w-4" /> 노무자 추가
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>노무자 목록 ({workers.length})</CardTitle>
              <div className="flex w-full max-w-md items-center space-x-2">
                <Button variant="outline" size="icon">
                  <Filter className="h-4 w-4" />
                </Button>
                <Input placeholder="이름, 직종 또는 소속팀 검색" />
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
                  <TableHead>직종</TableHead>
                  <TableHead>구분</TableHead>
                  <TableHead>소속 팀</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {workers.map((worker) => (
                  <TableRow key={worker.id}>
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        <Avatar className="h-8 w-8">
                          <AvatarImage src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${worker.id}`} />
                          <AvatarFallback>{worker.name.charAt(0)}</AvatarFallback>
                        </Avatar>
                        {worker.name}
                      </div>
                    </TableCell>
                    <TableCell>{worker.job}</TableCell>
                    <TableCell>
                      <Badge variant="secondary" className="font-normal">
                        {worker.type}
                      </Badge>
                    </TableCell>
                    <TableCell>{worker.team}</TableCell>
                    <TableCell>
                      <Badge variant={
                        worker.status === "출역중" ? "default" :
                        worker.status === "퇴근" ? "secondary" : "destructive"
                      } className={
                        worker.status === "출역중" ? "bg-green-500 hover:bg-green-600" : ""
                      }>
                        {worker.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm">상세</Button>
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
