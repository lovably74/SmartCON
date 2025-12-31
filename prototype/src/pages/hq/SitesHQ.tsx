import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Progress } from "@/components/ui/progress";
import {
  Building2,
  Search,
  Plus,
  MapPin,
  Users,
  Calendar,
  TrendingUp,
  AlertTriangle,
  Eye
} from "lucide-react";
import { useState } from "react";

export default function SitesHQ() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const sites = [
    {
      id: 1,
      name: "강남 테헤란로 오피스 신축공사",
      location: "서울시 강남구 테헤란로 123",
      manager: "김철수",
      progress: 45,
      startDate: "2024-10-01",
      endDate: "2025-08-31",
      totalWorkers: 124,
      presentWorkers: 98,
      status: "진행중",
      budget: 15000000000,
      spent: 6750000000
    },
    {
      id: 2,
      name: "판교 데이터센터 건립공사",
      location: "경기도 성남시 분당구 판교역로 235",
      manager: "이영희",
      progress: 12,
      startDate: "2024-12-01",
      endDate: "2025-11-30",
      totalWorkers: 85,
      presentWorkers: 72,
      status: "진행중",
      budget: 25000000000,
      spent: 3000000000
    },
    {
      id: 3,
      name: "부산 에코델타시티 조성공사",
      location: "부산시 강서구 에코델타시티",
      manager: "박민수",
      progress: 78,
      startDate: "2023-06-01",
      endDate: "2025-05-31",
      totalWorkers: 230,
      presentWorkers: 195,
      status: "지연",
      budget: 45000000000,
      spent: 35100000000
    },
    {
      id: 4,
      name: "인천공항 제2터미널 확장공사",
      location: "인천시 중구 공항로 272",
      manager: "정수진",
      progress: 34,
      startDate: "2024-08-01",
      endDate: "2026-07-31",
      totalWorkers: 156,
      presentWorkers: 142,
      status: "진행중",
      budget: 35000000000,
      spent: 11900000000
    },
    {
      id: 5,
      name: "세종 스마트시티 시범단지",
      location: "세종시 조치원읍 세종로 300",
      manager: "최동호",
      progress: 5,
      startDate: "2024-11-01",
      endDate: "2026-10-31",
      totalWorkers: 45,
      presentWorkers: 38,
      status: "진행중",
      budget: 20000000000,
      spent: 1000000000
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "진행중": return "bg-green-100 text-green-700";
      case "지연": return "bg-red-100 text-red-700";
      case "완료": return "bg-blue-100 text-blue-700";
      case "중단": return "bg-gray-100 text-gray-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const filteredSites = sites.filter(site =>
    site.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    site.location.toLowerCase().includes(searchTerm.toLowerCase()) ||
    site.manager.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        {/* 현장 통계 */}
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 현장</p>
                <div className="text-3xl font-bold mt-2">{sites.length}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Building2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">진행 중</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  {sites.filter(s => s.status === "진행중").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <TrendingUp className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 인력</p>
                <div className="text-3xl font-bold mt-2">
                  {sites.reduce((sum, s) => sum + s.totalWorkers, 0)}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <Users className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">지연 현장</p>
                <div className="text-3xl font-bold text-red-600 mt-2">
                  {sites.filter(s => s.status === "지연").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-red-100 flex items-center justify-center text-red-600">
                <AlertTriangle className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 현장 관리 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="h-5 w-5" />
              현장 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="현장명, 위치, 관리자명으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                신규 현장 등록
              </Button>
            </div>

            {/* 현장 목록 */}
            <div className="space-y-4">
              {filteredSites.map((site) => (
                <div key={site.id} className="border rounded-lg p-6 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="font-semibold text-lg">{site.name}</h3>
                        <Badge className={getStatusColor(site.status)}>
                          {site.status}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground mb-3">
                        <MapPin className="h-4 w-4" />
                        <span>{site.location}</span>
                        <span>•</span>
                        <span>관리자: {site.manager}</span>
                      </div>
                    </div>
                    <Button variant="outline" size="sm">
                      <Eye className="h-4 w-4 mr-1" />
                      상세보기
                    </Button>
                  </div>

                  <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {/* 공정률 */}
                    <div>
                      <div className="text-sm text-muted-foreground mb-2">공정률</div>
                      <div className="flex items-center gap-2">
                        <Progress value={site.progress} className="flex-1" />
                        <span className="font-bold text-lg">{site.progress}%</span>
                      </div>
                    </div>

                    {/* 인력 현황 */}
                    <div>
                      <div className="text-sm text-muted-foreground mb-2">인력 현황</div>
                      <div className="flex items-center gap-2">
                        <Users className="h-4 w-4 text-muted-foreground" />
                        <span className="font-semibold">{site.presentWorkers}/{site.totalWorkers}</span>
                        <span className="text-sm text-muted-foreground">
                          ({Math.round((site.presentWorkers / site.totalWorkers) * 100)}%)
                        </span>
                      </div>
                    </div>

                    {/* 공사 기간 */}
                    <div>
                      <div className="text-sm text-muted-foreground mb-2">공사 기간</div>
                      <div className="flex items-center gap-2">
                        <Calendar className="h-4 w-4 text-muted-foreground" />
                        <span className="text-sm font-medium">
                          {site.startDate} ~ {site.endDate}
                        </span>
                      </div>
                    </div>

                    {/* 예산 집행률 */}
                    <div>
                      <div className="text-sm text-muted-foreground mb-2">예산 집행률</div>
                      <div>
                        <div className="font-semibold text-green-600">
                          {Math.round((site.spent / site.budget) * 100)}%
                        </div>
                        <div className="text-xs text-muted-foreground">
                          {(site.spent / 100000000).toFixed(0)}억 / {(site.budget / 100000000).toFixed(0)}억
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredSites.length === 0 && (
              <div className="text-center py-12">
                <Building2 className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">검색 결과가 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}