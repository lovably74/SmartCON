import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Filter, Plus, Search } from "lucide-react";
import { ResponsiveTable } from "@/components/ui/responsive-table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { useState } from "react";

export default function WorkersHQ() {
  const [selectedWorker, setSelectedWorker] = useState<any>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  const workers = [
    { id: 1, name: "홍길동", job: "형틀목공", site: "강남 테헤란로 오피스", type: "내국인", status: "출역중", phone: "010-1234-5678", birth: "1980.05.15" },
    { id: 2, name: "김철수", job: "철근공", site: "판교 데이터센터", type: "내국인", status: "퇴근", phone: "010-2345-6789", birth: "1982.08.20" },
    { id: 3, name: "이영희", job: "전기공", site: "부산 해운대 주상복합", type: "내국인", status: "출역중", phone: "010-3456-7890", birth: "1985.03.10" },
    { id: 4, name: "Zhang Wei", job: "조적공", site: "인천 송도 물류센터", type: "외국인(H2)", status: "출역중", phone: "010-4567-8901", birth: "1978.11.25" },
    { id: 5, name: "박민수", job: "배관공", site: "제주 서귀포 호텔", type: "내국인", status: "결근", phone: "010-5678-9012", birth: "1990.01.05" },
    { id: 6, name: "최준호", job: "용접공", site: "강남 테헤란로 오피스", type: "내국인", status: "출역중", phone: "010-6789-0123", birth: "1988.07.12" },
    { id: 7, name: "Nguyen Van", job: "비계공", site: "판교 데이터센터", type: "외국인(E9)", status: "휴가", phone: "010-7890-1234", birth: "1995.09.30" },
    { id: 8, name: "정다은", job: "안전감시단", site: "부산 해운대 주상복합", type: "내국인", status: "출역중", phone: "010-8901-2345", birth: "1992.04.18" },
    { id: 9, name: "강호동", job: "크레인", site: "인천 송도 물류센터", type: "내국인", status: "출역중", phone: "010-9012-3456", birth: "1975.12.25" },
    { id: 10, name: "유재석", job: "신호수", site: "제주 서귀포 호텔", type: "내국인", status: "퇴근", phone: "010-0123-4567", birth: "1972.08.14" },
  ];

  const handleRowClick = (worker: any) => {
    setSelectedWorker(worker);
    setIsDetailOpen(true);
  };

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">노무자 관리</h2>
            <p className="text-muted-foreground">전체 등록된 노무자 인력 풀을 관리합니다.</p>
          </div>
          <Dialog>
            <DialogTrigger asChild>
              <Button className="w-full sm:w-auto">
                <Plus className="mr-2 h-4 w-4" /> 노무자 등록
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle>신규 노무자 등록</DialogTitle>
                <DialogDescription>
                  새로운 노무자의 기본 정보를 입력하여 등록합니다.
                </DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4">
                <div className="grid grid-cols-4 items-center gap-4">
                  <Label htmlFor="name" className="text-right">이름</Label>
                  <Input id="name" className="col-span-3" />
                </div>
                <div className="grid grid-cols-4 items-center gap-4">
                  <Label htmlFor="phone" className="text-right">연락처</Label>
                  <Input id="phone" className="col-span-3" />
                </div>
                <div className="grid grid-cols-4 items-center gap-4">
                  <Label htmlFor="job" className="text-right">직종</Label>
                  <Input id="job" className="col-span-3" />
                </div>
              </div>
              <div className="flex justify-end">
                <Button type="submit">등록하기</Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardHeader>
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <CardTitle>노무자 목록 ({workers.length})</CardTitle>
              <div className="flex w-full sm:max-w-md items-center space-x-2">
                <Button variant="outline" size="icon">
                  <Filter className="h-4 w-4" />
                </Button>
                <Input placeholder="이름, 직종 또는 현장 검색" />
                <Button size="icon" variant="ghost">
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <ResponsiveTable
              data={workers}
              keyExtractor={(item) => item.id}
              onRowClick={handleRowClick}
              columns={[
                {
                  header: "이름",
                  cell: (item) => (
                    <div className="flex items-center gap-2">
                      <Avatar className="h-8 w-8">
                        <AvatarImage src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${item.id}`} />
                        <AvatarFallback>{item.name.charAt(0)}</AvatarFallback>
                      </Avatar>
                      <span className="font-medium">{item.name}</span>
                    </div>
                  ),
                },
                {
                  header: "직종",
                  accessorKey: "job",
                },
                {
                  header: "구분",
                  cell: (item) => (
                    <Badge variant="secondary" className="font-normal">
                      {item.type}
                    </Badge>
                  ),
                },
                {
                  header: "배정 현장",
                  accessorKey: "site",
                  className: "hidden md:table-cell",
                },
                {
                  header: "상태",
                  cell: (item) => (
                    <Badge variant={
                      item.status === "출역중" ? "default" :
                      item.status === "퇴근" ? "secondary" : "destructive"
                    } className={
                      item.status === "출역중" ? "bg-green-500 hover:bg-green-600" : ""
                    }>
                      {item.status}
                    </Badge>
                  ),
                },
              ]}
            />
          </CardContent>
        </Card>

        {/* 상세 보기 다이얼로그 */}
        <Dialog open={isDetailOpen} onOpenChange={setIsDetailOpen}>
          <DialogContent className="sm:max-w-[600px]">
            <DialogHeader>
              <DialogTitle>노무자 상세 정보</DialogTitle>
            </DialogHeader>
            {selectedWorker && (
              <div className="grid gap-6 py-4">
                <div className="flex items-center gap-4">
                  <Avatar className="h-20 w-20">
                    <AvatarImage src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${selectedWorker.id}`} />
                    <AvatarFallback>{selectedWorker.name.charAt(0)}</AvatarFallback>
                  </Avatar>
                  <div>
                    <h3 className="text-xl font-bold">{selectedWorker.name}</h3>
                    <p className="text-muted-foreground">{selectedWorker.job} | {selectedWorker.type}</p>
                    <Badge className="mt-2">{selectedWorker.status}</Badge>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4 text-sm border-t pt-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-muted-foreground">연락처</span>
                    <span>{selectedWorker.phone}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-muted-foreground">생년월일</span>
                    <span>{selectedWorker.birth}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-muted-foreground">현재 배정 현장</span>
                    <span>{selectedWorker.site}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-muted-foreground">기초안전보건교육</span>
                    <span className="text-green-600 font-medium">이수 완료 (2023.05.10)</span>
                  </div>
                </div>

                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setIsDetailOpen(false)}>닫기</Button>
                  <Button>정보 수정</Button>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  );
}

