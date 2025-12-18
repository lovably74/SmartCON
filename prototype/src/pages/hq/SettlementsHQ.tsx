import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { CreditCard, Download, Filter, Search, DollarSign, Calendar } from "lucide-react";
import { ResponsiveTable } from "@/components/ui/responsive-table";
import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export default function SettlementsHQ() {
  const [selectedSettlement, setSelectedSettlement] = useState<any>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  const settlements = [
    { id: 1, month: "2025-11", site: "강남 테헤란로 오피스", amount: 125000000, status: "지급완료", date: "2025.12.10" },
    { id: 2, month: "2025-11", site: "판교 데이터센터", amount: 85000000, status: "지급완료", date: "2025.12.10" },
    { id: 3, month: "2025-11", site: "부산 에코델타시티", amount: 210000000, status: "승인대기", date: "-" },
    { id: 4, month: "2025-11", site: "인천공항 제2터미널", amount: 156000000, status: "검토중", date: "-" },
    { id: 5, month: "2025-11", site: "세종 스마트시티", amount: 45000000, status: "지급완료", date: "2025.12.10" },
    { id: 6, month: "2025-10", site: "강남 테헤란로 오피스", amount: 118000000, status: "지급완료", date: "2025.11.10" },
    { id: 7, month: "2025-10", site: "판교 데이터센터", amount: 82000000, status: "지급완료", date: "2025.11.10" },
    { id: 8, month: "2025-10", site: "부산 에코델타시티", amount: 198000000, status: "지급완료", date: "2025.11.10" },
    { id: 9, month: "2025-10", site: "인천공항 제2터미널", amount: 145000000, status: "지급완료", date: "2025.11.10" },
    { id: 10, month: "2025-10", site: "세종 스마트시티", amount: 42000000, status: "지급완료", date: "2025.11.10" },
  ];

  const handleRowClick = (settlement: any) => {
    setSelectedSettlement(settlement);
    setIsDetailOpen(true);
  };

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">정산 관리</h2>
            <p className="text-muted-foreground">현장별 노무비 정산 및 지급 현황을 관리합니다.</p>
          </div>
          <Button className="w-full sm:w-auto" variant="outline">
            <Download className="mr-2 h-4 w-4" /> 월별 리포트 다운로드
          </Button>
        </div>

        <div className="grid gap-4 grid-cols-1 sm:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">이번 달 예상 지급액</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 621,000,000</div>
              <p className="text-xs text-muted-foreground mt-1">전월 대비 +5.2%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">지급 완료 (11월)</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₩ 252,000,000</div>
              <p className="text-xs text-muted-foreground mt-1">3개 현장 완료</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">미지급 / 대기</CardTitle>
              <DollarSign className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">₩ 369,000,000</div>
              <p className="text-xs text-muted-foreground mt-1">2개 현장 검토 중</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <CardTitle>정산 목록</CardTitle>
              <div className="flex w-full sm:max-w-md items-center space-x-2">
                <Button variant="outline" size="icon">
                  <Filter className="h-4 w-4" />
                </Button>
                <Input placeholder="현장명 검색" />
                <Button size="icon" variant="ghost">
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <ResponsiveTable
              data={settlements}
              keyExtractor={(item) => item.id}
              onRowClick={handleRowClick}
              columns={[
                {
                  header: "현장명",
                  cell: (item) => (
                    <div className="font-medium">{item.site}</div>
                  ),
                },
                {
                  header: "귀속월",
                  accessorKey: "month",
                  className: "hidden sm:table-cell",
                },
                {
                  header: "정산금액",
                  cell: (item) => (
                    <div className="font-bold">₩ {item.amount.toLocaleString()}</div>
                  ),
                },
                {
                  header: "지급일",
                  accessorKey: "date",
                  className: "hidden md:table-cell",
                },
                {
                  header: "상태",
                  cell: (item) => (
                    <Badge variant={
                      item.status === "지급완료" ? "outline" :
                      item.status === "승인대기" ? "destructive" : "secondary"
                    } className={
                      item.status === "지급완료" ? "text-green-600 border-green-200 bg-green-50" : ""
                    }>
                      {item.status}
                    </Badge>
                  ),
                },
              ]}
            />
          </CardContent>
        </Card>

        <Dialog open={isDetailOpen} onOpenChange={setIsDetailOpen}>
          <DialogContent className="sm:max-w-[600px]">
            <DialogHeader>
              <DialogTitle>정산 상세 정보</DialogTitle>
            </DialogHeader>
            {selectedSettlement && (
              <div className="grid gap-6 py-4">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-bold">{selectedSettlement.site}</h3>
                    <Badge variant={
                      selectedSettlement.status === "지급완료" ? "outline" :
                      selectedSettlement.status === "승인대기" ? "destructive" : "secondary"
                    } className={
                      selectedSettlement.status === "지급완료" ? "text-green-600 border-green-200 bg-green-50" : ""
                    }>
                      {selectedSettlement.status}
                    </Badge>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">귀속월</span>
                      <span className="font-medium">{selectedSettlement.month}</span>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">정산금액</span>
                      <span className="font-bold text-lg">₩ {selectedSettlement.amount.toLocaleString()}</span>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">지급일자</span>
                      <span>{selectedSettlement.date}</span>
                    </div>
                  </div>
                  <div className="border rounded-md p-4 bg-muted/50">
                    <h4 className="font-medium mb-2">상세 내역</h4>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>노무비</span>
                        <span>₩ {Math.floor(selectedSettlement.amount * 0.9).toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>4대보험료</span>
                        <span>₩ {Math.floor(selectedSettlement.amount * 0.08).toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>기타 공제</span>
                        <span>₩ {Math.floor(selectedSettlement.amount * 0.02).toLocaleString()}</span>
                      </div>
                      <div className="border-t pt-2 flex justify-between font-bold">
                        <span>실 지급액</span>
                        <span>₩ {selectedSettlement.amount.toLocaleString()}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setIsDetailOpen(false)}>닫기</Button>
                  <Button>명세서 출력</Button>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  );
}

