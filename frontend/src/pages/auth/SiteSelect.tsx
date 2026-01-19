import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Building, Calendar, MapPin, Search, Users } from "lucide-react";
import { useState } from "react";
import { useLocation, useSearch } from "wouter";
import { toast } from "sonner";

/**
 * 현장 선택 컴포넌트
 * Requirements: 1.11, 1.12, 1.13, 1.14
 * 
 * 기능:
 * - 사용자가 접근 가능한 현장 목록 표시
 * - 최근 로그인, 최근 배정, 공사기간 순 정렬
 * - 현장 상태별 필터링
 * - 현장명 검색
 */

interface Site {
  id: string;
  name: string;
  location: string;
  status: "ACTIVE" | "PAUSED" | "COMPLETED";
  startDate: string;
  endDate: string;
  remainingDays: number;
  workerCount: number;
  lastLoginDate?: string;
  assignedDate: string;
}

export default function SiteSelect() {
  const [, setLocation] = useLocation();
  const searchParams = useSearch();
  const role = new URLSearchParams(searchParams).get("role") || "SITE";
  
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL");

  // 사용자가 접근 가능한 현장 목록 (API에서 가져올 데이터)
  const sites: Site[] = [
    {
      id: "1",
      name: "강남 오피스텔 신축공사",
      location: "서울 강남구",
      status: "ACTIVE",
      startDate: "2024-01-15",
      endDate: "2024-12-31",
      remainingDays: 45,
      workerCount: 28,
      lastLoginDate: "2024-01-18",
      assignedDate: "2024-01-15"
    },
    {
      id: "2",
      name: "판교 테크노밸리 B동",
      location: "경기 성남시",
      status: "ACTIVE",
      startDate: "2023-11-01",
      endDate: "2024-06-30",
      remainingDays: 90,
      workerCount: 42,
      lastLoginDate: "2024-01-17",
      assignedDate: "2023-11-01"
    },
    {
      id: "3",
      name: "인천 물류센터 증축",
      location: "인천 서구",
      status: "PAUSED",
      startDate: "2023-09-01",
      endDate: "2024-08-31",
      remainingDays: 120,
      workerCount: 15,
      assignedDate: "2023-09-01"
    }
  ];

  // 현장 필터링 및 정렬
  const filteredSites = sites
    .filter(site => {
      const matchesSearch = site.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                           site.location.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesStatus = selectedStatus === "ALL" || site.status === selectedStatus;
      return matchesSearch && matchesStatus;
    })
    .sort((a, b) => {
      // 1순위: 최근 로그인
      if (a.lastLoginDate && b.lastLoginDate) {
        return new Date(b.lastLoginDate).getTime() - new Date(a.lastLoginDate).getTime();
      }
      if (a.lastLoginDate) return -1;
      if (b.lastLoginDate) return 1;
      
      // 2순위: 최근 배정
      const assignedDiff = new Date(b.assignedDate).getTime() - new Date(a.assignedDate).getTime();
      if (assignedDiff !== 0) return assignedDiff;
      
      // 3순위: 남은 공사기간
      return a.remainingDays - b.remainingDays;
    });

  const getStatusBadge = (status: Site["status"]) => {
    const statusConfig = {
      ACTIVE: { label: "진행중", variant: "default" as const },
      PAUSED: { label: "일시중지", variant: "secondary" as const },
      COMPLETED: { label: "완료", variant: "outline" as const }
    };
    const config = statusConfig[status];
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  const handleSiteSelect = (site: Site) => {
    if (site.status === "PAUSED") {
      toast.warning("일시중지된 현장입니다.");
      return;
    }
    
    if (site.status === "COMPLETED") {
      toast.warning("완료된 현장입니다.");
      return;
    }

    toast.success(`${site.name}에 접속합니다.`);
    
    // 역할에 따라 적절한 대시보드로 이동
    const dashboardRoutes = {
      SITE: "/site/dashboard",
      TEAM: "/worker/dashboard",
      WORKER: "/worker/dashboard"
    };
    
    setLocation(dashboardRoutes[role as keyof typeof dashboardRoutes] || "/site/dashboard");
  };

  return (
    <PublicLayout>
      <div className="container max-w-6xl py-20">
        <Card className="shadow-lg">
          <CardHeader className="space-y-4">
            <div>
              <CardTitle className="text-2xl font-bold">현장을 선택해주세요</CardTitle>
              <CardDescription>
                접속 가능한 현장 {filteredSites.length}개
              </CardDescription>
            </div>

            {/* 검색 및 필터 */}
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="현장명 또는 위치로 검색..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              <div className="flex gap-2">
                <Button
                  variant={selectedStatus === "ALL" ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSelectedStatus("ALL")}
                >
                  전체
                </Button>
                <Button
                  variant={selectedStatus === "ACTIVE" ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSelectedStatus("ACTIVE")}
                >
                  진행중
                </Button>
                <Button
                  variant={selectedStatus === "PAUSED" ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSelectedStatus("PAUSED")}
                >
                  일시중지
                </Button>
              </div>
            </div>
          </CardHeader>

          <CardContent className="space-y-3">
            {filteredSites.length === 0 ? (
              <div className="text-center py-12 text-muted-foreground">
                <Building className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>검색 결과가 없습니다.</p>
              </div>
            ) : (
              filteredSites.map((site) => (
                <Button
                  key={site.id}
                  variant="outline"
                  className="w-full h-auto p-4 hover:border-primary hover:bg-primary/5 transition-all group"
                  onClick={() => handleSiteSelect(site)}
                >
                  <div className="flex items-start gap-4 w-full">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center text-primary flex-shrink-0 group-hover:bg-primary group-hover:text-primary-foreground transition-colors">
                      <Building className="h-6 w-6" />
                    </div>
                    
                    <div className="flex-1 text-left space-y-2">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <h3 className="font-semibold text-base">{site.name}</h3>
                          <div className="flex items-center gap-2 text-sm text-muted-foreground mt-1">
                            <MapPin className="h-3 w-3" />
                            <span>{site.location}</span>
                          </div>
                        </div>
                        {getStatusBadge(site.status)}
                      </div>
                      
                      <div className="flex flex-wrap gap-3 text-xs text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          <span>D-{site.remainingDays}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Users className="h-3 w-3" />
                          <span>노무자 {site.workerCount}명</span>
                        </div>
                        {site.lastLoginDate && (
                          <Badge variant="secondary" className="text-xs">
                            최근 접속: {new Date(site.lastLoginDate).toLocaleDateString()}
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                </Button>
              ))
            )}
          </CardContent>
        </Card>
      </div>
    </PublicLayout>
  );
}
