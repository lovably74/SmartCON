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
import { CheckCircle2, FileText, RefreshCw, Send } from "lucide-react";

export default function TaxSuper() {
  const taxInvoices = [
    { id: 1, tenant: "대우건설", amount: 3300000, date: "2025.12.18", status: "발행완료", hometax: "전송성공" },
    { id: 2, tenant: "현대건설", amount: 3300000, date: "2025.12.18", status: "발행완료", hometax: "전송성공" },
    { id: 3, tenant: "GS건설", amount: 3300000, date: "2025.12.18", status: "발행완료", hometax: "전송대기" },
    { id: 4, tenant: "삼성물산", amount: 3300000, date: "2025.12.18", status: "발행완료", hometax: "전송성공" },
    { id: 5, tenant: "포스코이앤씨", amount: 330000, date: "2025.12.17", status: "발행완료", hometax: "전송성공" },
    { id: 6, tenant: "DL이앤씨", amount: 330000, date: "2025.12.17", status: "발행완료", hometax: "전송성공" },
    { id: 7, tenant: "롯데건설", amount: 330000, date: "2025.12.17", status: "발행완료", hometax: "전송성공" },
    { id: 8, tenant: "SK에코플랜트", amount: 3300000, date: "2025.12.17", status: "발행완료", hometax: "전송성공" },
    { id: 9, tenant: "HDC현대산업개발", amount: 330000, date: "2025.12.17", status: "발행완료", hometax: "전송성공" },
    { id: 10, tenant: "한화 건설부문", amount: 330000, date: "2025.12.16", status: "발행취소", hometax: "-" },
  ];

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">세금계산서 관리</h2>
            <p className="text-muted-foreground">전자세금계산서 발행 및 국세청 전송 현황을 관리합니다.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline">
              <RefreshCw className="mr-2 h-4 w-4" /> 국세청 연동 상태 확인
            </Button>
            <Button>
              <Send className="mr-2 h-4 w-4" /> 미전송 건 일괄 전송
            </Button>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">이번 달 발행 금액</CardTitle>
              <FileText className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 125,400,000</div>
              <p className="text-xs text-muted-foreground mt-1">공급가액 기준</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">국세청 전송 완료</CardTitle>
              <CheckCircle2 className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">452건</div>
              <p className="text-xs text-muted-foreground mt-1">전송 성공률 99.8%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">전송 대기 / 실패</CardTitle>
              <RefreshCw className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">3건</div>
              <p className="text-xs text-muted-foreground mt-1">재전송 필요</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>발행 내역</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>공급받는자</TableHead>
                  <TableHead>공급가액</TableHead>
                  <TableHead>작성일자</TableHead>
                  <TableHead>발행상태</TableHead>
                  <TableHead>국세청 전송</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {taxInvoices.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="font-medium">{item.tenant}</TableCell>
                    <TableCell>₩ {item.amount.toLocaleString()}</TableCell>
                    <TableCell>{item.date}</TableCell>
                    <TableCell>
                      <Badge variant={item.status === "발행완료" ? "outline" : "destructive"}>
                        {item.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge variant={
                        item.hometax === "전송성공" ? "secondary" : 
                        item.hometax === "전송대기" ? "outline" : "destructive"
                      } className={
                        item.hometax === "전송성공" ? "text-green-600 bg-green-50" : ""
                      }>
                        {item.hometax}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm">상세보기</Button>
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



