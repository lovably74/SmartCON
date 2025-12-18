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
import { ArrowUpRight, CreditCard, DollarSign, Download } from "lucide-react";

export default function BillingSuper() {
  const transactions = [
    { id: 1, tenant: "대우건설", plan: "Enterprise", amount: 3300000, date: "2025.12.18 14:30", status: "결제성공", method: "카드" },
    { id: 2, tenant: "현대건설", plan: "Enterprise", amount: 3300000, date: "2025.12.18 13:15", status: "결제성공", method: "CMS" },
    { id: 3, tenant: "한화 건설부문", plan: "Pro", amount: 330000, date: "2025.12.18 11:20", status: "결제실패", method: "카드" },
    { id: 4, tenant: "GS건설", plan: "Enterprise", amount: 3300000, date: "2025.12.18 10:05", status: "결제성공", method: "CMS" },
    { id: 5, tenant: "삼성물산", plan: "Enterprise", amount: 3300000, date: "2025.12.18 09:45", status: "결제성공", method: "카드" },
    { id: 6, tenant: "포스코이앤씨", plan: "Pro", amount: 330000, date: "2025.12.17 16:30", status: "결제성공", method: "카드" },
    { id: 7, tenant: "DL이앤씨", plan: "Pro", amount: 330000, date: "2025.12.17 15:20", status: "결제성공", method: "CMS" },
    { id: 8, tenant: "롯데건설", plan: "Pro", amount: 330000, date: "2025.12.17 14:10", status: "결제성공", method: "카드" },
    { id: 9, tenant: "SK에코플랜트", plan: "Enterprise", amount: 3300000, date: "2025.12.17 11:50", status: "결제성공", method: "CMS" },
    { id: 10, tenant: "HDC현대산업개발", plan: "Pro", amount: 330000, date: "2025.12.17 10:30", status: "결제성공", method: "카드" },
  ];

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">구독/결제 관리</h2>
            <p className="text-muted-foreground">전체 매출 현황 및 결제 내역을 모니터링합니다.</p>
          </div>
          <Button variant="outline">
            <Download className="mr-2 h-4 w-4" /> 매출 리포트 다운로드
          </Button>
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">이번 달 MRR</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 125,400,000</div>
              <p className="text-xs text-muted-foreground mt-1 flex items-center text-green-600">
                <ArrowUpRight className="h-3 w-3 mr-1" /> 전월 대비 +12.5%
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">결제 성공률</CardTitle>
              <CreditCard className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">98.2%</div>
              <p className="text-xs text-muted-foreground mt-1">최근 30일 기준</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">미수금 현황</CardTitle>
              <DollarSign className="h-4 w-4 text-red-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-red-600">₩ 3,300,000</div>
              <p className="text-xs text-muted-foreground mt-1">10건 (재결제 필요)</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>최근 결제 내역</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>고객사</TableHead>
                  <TableHead>요금제</TableHead>
                  <TableHead>결제 금액</TableHead>
                  <TableHead>결제 수단</TableHead>
                  <TableHead>일시</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">영수증</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {transactions.map((tx) => (
                  <TableRow key={tx.id}>
                    <TableCell className="font-medium">{tx.tenant}</TableCell>
                    <TableCell>{tx.plan}</TableCell>
                    <TableCell>₩ {tx.amount.toLocaleString()}</TableCell>
                    <TableCell>{tx.method}</TableCell>
                    <TableCell>{tx.date}</TableCell>
                    <TableCell>
                      <Badge variant={
                        tx.status === "결제성공" ? "outline" : "destructive"
                      } className={
                        tx.status === "결제성공" ? "text-green-600 border-green-200 bg-green-50" : ""
                      }>
                        {tx.status}
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

