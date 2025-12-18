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
import { FileCheck, FileText, Search, Send } from "lucide-react";

export default function ContractsSite() {
  const contracts = [
    { id: 1, title: "2025년 표준근로계약서", worker: "홍길동", team: "A팀", date: "2025.12.18", status: "서명완료" },
    { id: 2, title: "개인정보 수집 이용 동의서", worker: "김철수", team: "A팀", date: "2025.12.18", status: "서명대기" },
    { id: 3, title: "안전보건교육 이수 확인서", worker: "이영희", team: "B팀", date: "2025.12.17", status: "서명완료" },
    { id: 4, title: "2025년 표준근로계약서", worker: "박민수", team: "B팀", date: "2025.12.17", status: "만료임박" },
    { id: 5, title: "보안 서약서", worker: "Zhang Wei", team: "C팀", date: "2025.12.16", status: "서명완료" },
    { id: 6, title: "2025년 표준근로계약서", worker: "최준호", team: "C팀", date: "2025.12.16", status: "서명완료" },
    { id: 7, title: "개인정보 수집 이용 동의서", worker: "정다은", team: "안전팀", date: "2025.12.15", status: "서명대기" },
    { id: 8, title: "안전보건교육 이수 확인서", worker: "Nguyen Van", team: "D팀", date: "2025.12.15", status: "서명완료" },
    { id: 9, title: "2025년 표준근로계약서", worker: "강호동", team: "장비팀", date: "2025.12.14", status: "서명완료" },
    { id: 10, title: "보안 서약서", worker: "유재석", team: "안전팀", date: "2025.12.14", status: "서명완료" },
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">계약 관리</h2>
            <p className="text-muted-foreground">현장 노무자의 근로계약 및 동의서를 관리합니다.</p>
          </div>
          <Button>
            <Send className="mr-2 h-4 w-4" /> 계약서 발송
          </Button>
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">체결 완료</CardTitle>
              <FileCheck className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">132건</div>
              <p className="text-xs text-muted-foreground mt-1">전체 대상의 91%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">서명 대기</CardTitle>
              <FileText className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">13건</div>
              <p className="text-xs text-muted-foreground mt-1">재발송 필요</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">만료 임박</CardTitle>
              <FileText className="h-4 w-4 text-red-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">4건</div>
              <p className="text-xs text-muted-foreground mt-1">30일 이내 만료</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>계약 문서 목록</CardTitle>
              <div className="flex w-full max-w-sm items-center space-x-2">
                <Input placeholder="문서명 또는 노무자명 검색" />
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
                  <TableHead>문서명</TableHead>
                  <TableHead>노무자</TableHead>
                  <TableHead>소속 팀</TableHead>
                  <TableHead>발송일</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {contracts.map((contract) => (
                  <TableRow key={contract.id}>
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        <FileText className="h-4 w-4 text-muted-foreground" />
                        {contract.title}
                      </div>
                    </TableCell>
                    <TableCell>{contract.worker}</TableCell>
                    <TableCell>{contract.team}</TableCell>
                    <TableCell>{contract.date}</TableCell>
                    <TableCell>
                      <Badge variant={
                        contract.status === "서명완료" ? "outline" :
                        contract.status === "서명대기" ? "secondary" : "destructive"
                      } className={
                        contract.status === "서명완료" ? "text-green-600 border-green-200 bg-green-50" : ""
                      }>
                        {contract.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm">보기</Button>
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
