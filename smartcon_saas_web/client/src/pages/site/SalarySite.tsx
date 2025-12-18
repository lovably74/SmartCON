import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Download, DollarSign, Calendar } from "lucide-react";

export default function SalarySite() {
  const salary = [
    { id: 1, name: "홍길동", days: 22, amount: 4400000, tax: 145200, net: 4254800, status: "지급완료" },
    { id: 2, name: "김철수", days: 20, amount: 4000000, tax: 132000, net: 3868000, status: "지급완료" },
    { id: 3, name: "이영희", days: 24, amount: 4800000, tax: 158400, net: 4641600, status: "지급대기" },
    { id: 4, name: "박민수", days: 15, amount: 3000000, tax: 99000, net: 2901000, status: "지급대기" },
    { id: 5, name: "Zhang Wei", days: 25, amount: 5000000, tax: 165000, net: 4835000, status: "지급완료" },
    { id: 6, name: "최준호", days: 21, amount: 4200000, tax: 138600, net: 4061400, status: "지급완료" },
    { id: 7, name: "정다은", days: 22, amount: 4400000, tax: 145200, net: 4254800, status: "지급완료" },
    { id: 8, name: "Nguyen Van", days: 18, amount: 3600000, tax: 118800, net: 3481200, status: "지급대기" },
    { id: 9, name: "강호동", days: 23, amount: 4600000, tax: 151800, net: 4448200, status: "지급완료" },
    { id: 10, name: "유재석", days: 20, amount: 4000000, tax: 132000, net: 3868000, status: "지급완료" },
  ];

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">급여/정산</h2>
            <p className="text-muted-foreground">노무자별 공수 기반 급여 산출 및 지급 현황을 관리합니다.</p>
          </div>
          <Button variant="outline">
            <Download className="mr-2 h-4 w-4" /> 급여대장 다운로드
          </Button>
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">이번 달 총 급여액</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 42,000,000</div>
              <p className="text-xs text-muted-foreground mt-1">전월 대비 +2.5%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">지급 완료</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 30,600,000</div>
              <p className="text-xs text-muted-foreground mt-1">7명 지급 완료</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">지급 대기</CardTitle>
              <DollarSign className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">₩ 11,400,000</div>
              <p className="text-xs text-muted-foreground mt-1">3명 대기 중</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>급여 상세 내역 (2025년 12월)</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>이름</TableHead>
                  <TableHead>출역일수</TableHead>
                  <TableHead>총 급여액</TableHead>
                  <TableHead>공제액(세금 등)</TableHead>
                  <TableHead>실 지급액</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">명세서</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {salary.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="font-medium">{item.name}</TableCell>
                    <TableCell>{item.days}일</TableCell>
                    <TableCell>₩ {item.amount.toLocaleString()}</TableCell>
                    <TableCell className="text-red-500">- ₩ {item.tax.toLocaleString()}</TableCell>
                    <TableCell className="font-bold">₩ {item.net.toLocaleString()}</TableCell>
                    <TableCell>
                      <Badge variant={
                        item.status === "지급완료" ? "outline" : "secondary"
                      } className={
                        item.status === "지급완료" ? "text-green-600 border-green-200 bg-green-50" : ""
                      }>
                        {item.status}
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
