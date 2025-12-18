import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Building2, MapPin, Plus, Search, Users } from "lucide-react";
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

export default function SitesHQ() {
  const [selectedSite, setSelectedSite] = useState<any>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  const sites = [
    { id: 1, name: "강남 테헤란로 오피스 신축공사", address: "서울시 강남구 테헤란로 123", manager: "김현장", workers: 45, progress: 35, status: "진행중", startDate: "2024.01.01", endDate: "2025.12.31" },
    { id: 2, name: "판교 데이터센터 건립공사", address: "경기도 성남시 분당구 판교로 456", manager: "이판교", workers: 120, progress: 12, status: "진행중", startDate: "2024.03.15", endDate: "2026.06.30" },
    { id: 3, name: "부산 해운대 주상복합", address: "부산시 해운대구 마린시티로 789", manager: "박부산", workers: 85, progress: 68, status: "진행중", startDate: "2023.05.01", endDate: "2025.04.30" },
    { id: 4, name: "인천 송도 물류센터", address: "인천시 연수구 송도동 101", manager: "최송도", workers: 30, progress: 95, status: "준공임박", startDate: "2023.01.01", endDate: "2024.12.31" },
    { id: 5, name: "제주 서귀포 호텔 리모델링", address: "제주도 서귀포시 중문관광로 202", manager: "정제주", workers: 15, progress: 5, status: "착공준비", startDate: "2024.12.01", endDate: "2025.05.31" },
  ];

  const handleRowClick = (site: any) => {
    setSelectedSite(site);
    setIsDetailOpen(true);
  };

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">현장 관리</h2>
            <p className="text-muted-foreground">전체 운영 중인 건설 현장 목록을 조회하고 관리합니다.</p>
          </div>
          <Dialog>
            <DialogTrigger asChild>
              <Button className="w-full sm:w-auto">
                <Plus className="mr-2 h-4 w-4" /> 현장 개설
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle>새 현장 개설</DialogTitle>
                <DialogDescription>
                  새로운 건설 현장 정보를 입력하여 개설합니다.
                </DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4">
                <div className="grid grid-cols-4 items-center gap-4">
                  <Label htmlFor="name" className="text-right">현장명</Label>
                  <Input id="name" className="col-span-3" />
                </div>
                <div className="grid grid-cols-4 items-center gap-4">
                  <Label htmlFor="address" className="text-right">주소</Label>
                  <Input id="address" className="col-span-3" />
                </div>
                <div className="grid grid-cols-4 items-center gap-4">
                  <Label htmlFor="manager" className="text-right">소장명</Label>
                  <Input id="manager" className="col-span-3" />
                </div>
              </div>
              <div className="flex justify-end">
                <Button type="submit">개설하기</Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardHeader>
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <CardTitle>현장 목록 ({sites.length})</CardTitle>
              <div className="flex w-full sm:max-w-sm items-center space-x-2">
                <Input placeholder="현장명 또는 주소 검색" />
                <Button size="icon" variant="ghost">
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <ResponsiveTable
              data={sites}
              keyExtractor={(item) => item.id}
              onRowClick={handleRowClick}
              columns={[
                {
                  header: "현장명",
                  cell: (item) => (
                    <div className="flex items-center gap-2">
                      <Building2 className="h-4 w-4 text-muted-foreground hidden sm:block" />
                      <span className="font-medium">{item.name}</span>
                    </div>
                  ),
                },
                {
                  header: "현장 소장",
                  accessorKey: "manager",
                },
                {
                  header: "출역 인원",
                  cell: (item) => (
                    <div className="flex items-center gap-1">
                      <Users className="h-3 w-3 text-muted-foreground" />
                      {item.workers}명
                    </div>
                  ),
                },
                {
                  header: "공정률",
                  cell: (item) => (
                    <div className="flex items-center gap-2">
                      <div className="w-16 h-2 bg-secondary rounded-full overflow-hidden">
                        <div 
                          className="h-full bg-primary" 
                          style={{ width: `${item.progress}%` }}
                        />
                      </div>
                      <span className="text-xs text-muted-foreground">{item.progress}%</span>
                    </div>
                  ),
                },
                {
                  header: "상태",
                  cell: (item) => (
                    <Badge variant={
                      item.status === "진행중" ? "default" :
                      item.status === "준공임박" ? "secondary" : "outline"
                    } className={
                      item.status === "진행중" ? "bg-green-500 hover:bg-green-600" : ""
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
              <DialogTitle>현장 상세 정보</DialogTitle>
            </DialogHeader>
            {selectedSite && (
              <div className="grid gap-6 py-4">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-bold">{selectedSite.name}</h3>
                    <Badge>{selectedSite.status}</Badge>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">현장 주소</span>
                      <div className="flex items-center gap-1">
                        <MapPin className="h-3 w-3" /> {selectedSite.address}
                      </div>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">현장 소장</span>
                      <span>{selectedSite.manager}</span>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">공사 기간</span>
                      <span>{selectedSite.startDate} ~ {selectedSite.endDate}</span>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-muted-foreground">현재 공정률</span>
                      <span className="font-bold text-primary">{selectedSite.progress}%</span>
                    </div>
                  </div>
                </div>
                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setIsDetailOpen(false)}>닫기</Button>
                  <Button>현장 정보 수정</Button>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  );
}

