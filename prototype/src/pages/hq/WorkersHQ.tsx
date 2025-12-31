import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  Users,
  Search,
  Filter,
  Plus,
  Eye,
  UserCheck,
  UserX,
  MapPin,
  Phone,
  Mail
} from "lucide-react";
import { useState } from "react";

export default function WorkersHQ() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const workers = [
    {
      id: 1,
      name: "김철수",
      phone: "010-1234-5678",
      email: "kim@example.com",
      site: "강남 테헤란로 오피스 신축공사",
      team: "철근팀",
      role: "팀장",
      status: "active",
      joinDate: "2024-03-15",
      lastAttendance: "2025-01-01",
      totalDays: 245,
      isFaceRegistered: true
    },
    {
      id: 2,
      name: "이영희",
      phone: "010-2345-6789",
      email: "lee@example.com",
      site: "판교 데이터센터 건립공사",
      team: "콘크리트팀",
      role: "팀장",
      status: "active",
      joinDate: "2024-02-20",
      lastAttendance: "2025-01-01",
      totalDays: 280,
      isFaceRegistered: true
    },
    {
      id: 3,
      name: "박민수",
      phone: "010-3456-7890",
      email: "park@example.com",
      site: "강남 테헤란로 오피스 신축공사",
      team: "철근팀",
      role: "일반 노무자",
      status: "active",
      joinDate: "2024-06-01",
      lastAttendance: "2024-12-30",
      totalDays: 180,
      isFaceRegistered: false
    },
    {
      id: 4,
      name: "정수진",
      phone: "010-4567-8901",
      email: "jung@example.com",
      site: "부산 에코델타시티 조성공사",
      team: "전기팀",
      role: "팀장",
      status: "inactive",
      joinDate: "2023-11-10",
      lastAttendance: "2024-12-20",
      totalDays: 320,
      isFaceRegistered: true
    },
    {
      id: 5,
      name: "최동호",
      phone: "010-5678-9012",
      email: "choi@example.com",
      site: "인천공항 제2터미널 확장공사",
      team: "배관팀",
      role: "일반 노무자",
      status: "active",
      joinDate: "2024-08-01",
      lastAttendance: "2025-01-01",
      totalDays: 120,
      isFaceRegistered: true
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active": return "bg-green-100 text-green-700";
      case "inactive": return "bg-red-100 text-red-700";
      case "pending": return "bg-orange-100 text-orange-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "active": return "활성";
      case "inactive": return "비활성";
      case "pending": return "승인 대기";
      default: return "알 수 없음";
    }
  };

  const getRoleColor = (role: string) => {
    switch (role) {
      case "팀장": return "bg-blue-100 text-blue-700";
      case "일반 노무자": return "bg-gray-100 text-gray-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const filteredWorkers = workers.filter(worker =>
    worker.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    worker.site.toLowerCase().includes(searchTerm.toLowerCase()) ||
    worker.team.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        {/* 노무자 통계 */}
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 노무자</p>
                <div className="text-3xl font-bold mt-2">{workers.length}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Users className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">활성 노무자</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  {workers.filter(w => w.status === "active").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <UserCheck className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">팀장</p>
                <div className="text-3xl font-bold text-purple-600 mt-2">
                  {workers.filter(w => w.role === "팀장").length}
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
                <p className="text-sm font-medium text-muted-foreground">안면인식 등록</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">
                  {workers.filter(w => w.isFaceRegistered).length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <UserCheck className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 노무자 관리 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              노무자 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="이름, 현장명, 팀명으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Button variant="outline">
                <Filter className="h-4 w-4 mr-2" />
                필터
              </Button>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                노무자 등록
              </Button>
            </div>

            {/* 노무자 목록 */}
            <div className="space-y-4">
              {filteredWorkers.map((worker) => (
                <div key={worker.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-4">
                      <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold">
                        {worker.name.charAt(0)}
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="font-semibold text-lg">{worker.name}</h3>
                          <Badge className={getStatusColor(worker.status)}>
                            {getStatusText(worker.status)}
                          </Badge>
                          <Badge className={getRoleColor(worker.role)}>
                            {worker.role}
                          </Badge>
                          {worker.isFaceRegistered && (
                            <Badge variant="outline" className="text-green-600 border-green-600">
                              안면인식 등록
                            </Badge>
                          )}
                        </div>
                        
                        <div className="grid md:grid-cols-3 gap-4 text-sm text-muted-foreground">
                          <div className="flex items-center gap-2">
                            <Phone className="h-4 w-4" />
                            <span>{worker.phone}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Mail className="h-4 w-4" />
                            <span>{worker.email}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin className="h-4 w-4" />
                            <span>{worker.team}</span>
                          </div>
                        </div>

                        <div className="mt-2 text-sm text-muted-foreground">
                          <span className="font-medium">현장:</span> {worker.site}
                        </div>
                      </div>
                    </div>

                    <div className="flex gap-2 ml-4">
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4 mr-1" />
                        상세보기
                      </Button>

                      {worker.status === "active" ? (
                        <Button variant="destructive" size="sm">
                          <UserX className="h-4 w-4 mr-1" />
                          비활성화
                        </Button>
                      ) : (
                        <Button size="sm">
                          <UserCheck className="h-4 w-4 mr-1" />
                          활성화
                        </Button>
                      )}
                    </div>
                  </div>

                  <div className="mt-4 pt-4 border-t grid md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <div className="text-muted-foreground">입사일</div>
                      <div className="font-medium">{worker.joinDate}</div>
                    </div>
                    <div>
                      <div className="text-muted-foreground">최근 출역</div>
                      <div className="font-medium">{worker.lastAttendance}</div>
                    </div>
                    <div>
                      <div className="text-muted-foreground">총 출역일</div>
                      <div className="font-medium">{worker.totalDays}일</div>
                    </div>
                    <div>
                      <div className="text-muted-foreground">출역률</div>
                      <div className="font-medium text-green-600">
                        {Math.round((worker.totalDays / 300) * 100)}%
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredWorkers.length === 0 && (
              <div className="text-center py-12">
                <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">검색 결과가 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}