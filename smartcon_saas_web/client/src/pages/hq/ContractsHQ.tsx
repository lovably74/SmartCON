import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { FileCheck, FileText, Search, Send, Download, Filter } from "lucide-react";
import { ResponsiveTable } from "@/components/ui/responsive-table";
import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export default function ContractsHQ() {
  const [selectedContract, setSelectedContract] = useState<any>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  const contracts = [
    { id: 1, title: "2025년 표준근로계약서", worker: "홍길동", site: "강남 테헤란로", date: "2025.12.18", status: "서명완료" },
    { id: 2, title: "개인정보 수집 이용 동의서", worker: "김철수", site: "강남 테헤란로", date: "2025.12.18", status: "서명대기" },
    { id: 3, title: "안전보건교육 이수 확인서", worker: "이영희", site: "판교 데이터센터", date: "2025.12.17", status: "서명완료" },
    { id: 4, title: "2025년 표준근로계약서", worker: "박민수", site: "부산 에코델타", date: "2025.12.17", status: "만료임박" },
    { id: 5, title: "보안 서약서", worker: "Zhang Wei", site: "인천공항", date: "2025.12.16", status: "서명완료" },
    { id: 6, title: "2025년 표준근로계약서", worker: "최준호", site: "세종 스마트시티", date: "2025.12.16", status: "서명완료" },
    { id: 7, title: "개인정보 수집 이용 동의서", worker: "정다은", site: "대구 수성구", date: "2025.12.15", status: "서명대기" },
    { id: 8, title: "안전보건교육 이수 확인서", worker: "Nguyen Van", site: "광주 첨단지구", date: "2025.12.15", status: "서명완료" },
    { id: 9, title: "2025년 표준근로계약서", worker: "강호동", site: "대전 사이언스", date: "2025.12.14", status: "서명완료" },
    { id: 10, title: "보안 서약서", worker: "유재석", site: "울산 석유화학", date: "2025.12.14", status: "서명완료" },
  ];

  const handleRowClick = (contract: any) => {
    setSelectedContract(contract);
    setIsDetailOpen(true);
  };

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">계약 관리</h2>
            <p className="text-muted-foreground">전자 근로계약 및 각종 동의서 현황을 관리합니다.</p>
          </div>
          <Button className="w-full sm:w-auto">
            <Send className="mr-2 h-4 w-4" /> 계약서 일괄 발송
          </Button>
        </div>

        <div className="grid gap-4 grid-cols-1 sm:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">체결 완료</CardTitle>
              <FileCheck className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">1,245건</div>
              <p className="text-xs text-muted-foreground mt-1">전체 대상의 92%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">서명 대기</CardTitle>
              <FileText className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">45건</div>
              <p className="text-xs text-muted-foreground mt-1">재발송 필요</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">만료 임박</CardTitle>
              <FileText className="h-4 w-4 text-red-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">12건</div>
              <p className="text-xs text-muted-foreground mt-1">30일 이내 만료</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <CardTitle>계약 문서 목록</CardTitle>
              <div className="flex w-full sm:max-w-md items-center space-x-2">
                <Button variant="outline" size="icon">
                  <Filter className="h-4 w-4" />
                </Button>
                <Input placeholder="문서명 또는 노무자명 검색" />
                <Button size="icon" variant="ghost">
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <ResponsiveTable
              data={contracts}
              keyExtractor={(item) => item.id}
              onRowClick={handleRowClick}
              columns={[
                {
                  header: "문서명",
                  cell: (item) => (
                    <div className="flex items-center gap-2 font-medium">
                      <FileText className="h-4 w-4 text-muted-foreground hidden sm:block" />
                      {item.title}
                    </div>
                  ),
                },
                {
                  header: "노무자",
                  accessorKey: "worker",
                },
                {
                  header: "현장",
                  accessorKey: "site",
                  className: "hidden md:table-cell",
                },
                {
                  header: "발송일",
                  accessorKey: "date",
                  className: "hidden sm:table-cell",
                },
                {
                  header: "상태",
                  cell: (item) => (
                    <Badge variant={
                      item.status === "서명완료" ? "outline" :
                      item.status === "서명대기" ? "secondary" : "destructive"
                    } className={
                      item.status === "서명완료" ? "text-green-600 border-green-200 bg-green-50" : ""
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
              <DialogTitle>계약 상세 정보</DialogTitle>
            </DialogHeader>
            {selectedContract && (
              <div className="grid gap-6 py-4">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-bold">{selectedContract.title}</h3>
                    <Badge variant={
                      selectedContract.status === "서명완료" ? "outline" :
                      selectedContract.status === "서명대기" ? "secondary" : "destructive"
                    } className={
                      selectedContract.status === "서명완료" ? "text-green-600 border-green-200 bg-green-50" : ""
                    }>
                      {selectedContract.status}
                    </Badge>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">근로자</span>
                      <span className="font-medium">{selectedContract.worker}</span>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">현장</span>
                      <span>{selectedContract.site}</span>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">발송일자</span>
                      <span>{selectedContract.date}</span>
                    </div>
                  </div>
                  <div className="border rounded-md p-4 bg-muted/50 h-40 flex items-center justify-center text-muted-foreground">
                    계약서 미리보기 영역
                  </div>
                </div>
                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setIsDetailOpen(false)}>닫기</Button>
                  <Button>
                    <Download className="mr-2 h-4 w-4" /> PDF 다운로드
                  </Button>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  );
}
