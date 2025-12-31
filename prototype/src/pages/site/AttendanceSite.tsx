import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Users,
  CheckCircle2,
  XCircle,
  Clock,
  Search,
  Filter,
  Download,
  Camera,
  AlertTriangle
} from "lucide-react";
import { useState } from "react";

export default function AttendanceSite() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const todayAttendance = [
    { id: 1, name: "김철수", team: "철근팀", checkIn: "07:30", checkOut: null, status: "출근", photo: true },
    { id: 2, name: "이영희", team: "콘크리트팀", checkIn: "07:45", checkOut: null, status: "지각", photo: true },
    { id: 3, name: "박민수", team: "철근팀", checkIn: "07:25", checkOut: "17:30", status: "퇴근", photo: true },
    { id: 4, name: "정수진", team: "전기팀", checkIn: null, checkOut: null, status: "결근", photo: false },
    { id: 5, name: "최동호", team: "배관팀", checkIn: "08:00", checkOut: null, status: "지각", photo: true },
  ];

  const teamStats = [
    { team: "철근팀", total: 25, present: 22, late: 2, absent: 1, rate: 88 },
    { team: "콘크리트팀", total: 30, present: 28, late: 1, absent: 1, rate: 93 },
    { team: "목공팀", total: 20, present: 18, late: 1, absent: 1, rate: 90 },
    { team: "전기팀", total: 15, present: 12, late: 2, absent: 1, rate: 80 },
    { team: "배관팀", total: 18, present: 15, late: 1, absent: 2, rate: 83 },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "출근": return "bg-green-100 text-green-700";
      case "퇴근": return "bg-blue-100 text-blue-700";
      case "지각": return "bg-orange-100 text-orange-700";
      case "결근": return "bg-red-100 text-red-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "출근": return <CheckCircle2 className="h-4 w-4" />;
      case "퇴근": return <CheckCircle2 className="h-4 w-4" />;
      case "지각": return <Clock className="h-4 w-4" />;
      case "결근": return <XCircle className="h-4 w-4" />;
      default: return <AlertTriangle className="h-4 w-4" />;
    }
  };

  const filteredAttendance = todayAttendance.filter(worker =>
    worker.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    worker.team.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        {/* 출역 현황 요약 */}
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 인원</p>
                <div className="text-3xl font-bold mt-2">108</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Users className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">출근</p>
                <div className="text-3xl font-bold text-green-600 mt-2">95</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <CheckCircle2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">지각</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">7</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <Clock className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">결근</p>
                <div className="text-3xl font-bold text-red-600 mt-2">6</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-red-100 flex items-center justify-center text-red-600">
                <XCircle className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="individual" className="space-y-6">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="individual">개별 출역 현황</TabsTrigger>
            <TabsTrigger value="team">팀별 출역 현황</TabsTrigger>
          </TabsList>

          <TabsContent value="individual" className="space-y-6">
            {/* 검색 및 필터 */}
            <Card>
              <CardHeader>
                <CardTitle>개별 출역 관리</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex gap-4 mb-6">
                  <div className="flex-1 relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="이름이나 팀명으로 검색..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                  <Button variant="outline">
                    <Filter className="h-4 w-4 mr-2" />
                    필터
                  </Button>
                  <Button variant="outline">
                    <Download className="h-4 w-4 mr-2" />
                    엑셀 다운로드
                  </Button>
                </div>

                {/* 출역 목록 */}
                <div className="space-y-3">
                  {filteredAttendance.map((worker) => (
                    <div key={worker.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors">
                      <div className="flex items-center gap-4">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold">
                          {worker.name.charAt(0)}
                        </div>
                        <div>
                          <div className="font-semibold">{worker.name}</div>
                          <div className="text-sm text-muted-foreground">{worker.team}</div>
                        </div>
                      </div>

                      <div className="flex items-center gap-4">
                        <div className="text-right text-sm">
                          <div className="font-medium">
                            {worker.checkIn || "--:--"} ~ {worker.checkOut || "--:--"}
                          </div>
                          <div className="text-muted-foreground">
                            {worker.photo ? "안면인식 완료" : "수동 입력"}
                          </div>
                        </div>

                        <Badge className={getStatusColor(worker.status)}>
                          {getStatusIcon(worker.status)}
                          <span className="ml-1">{worker.status}</span>
                        </Badge>

                        <div className="flex gap-2">
                          {worker.status === "출근" && (
                            <Button size="sm" variant="outline">
                              <Camera className="h-4 w-4 mr-1" />
                              퇴근 처리
                            </Button>
                          )}
                          {worker.status === "결근" && (
                            <Button size="sm">
                              <CheckCircle2 className="h-4 w-4 mr-1" />
                              출근 처리
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="team" className="space-y-6">
            {/* 팀별 출역 현황 */}
            <Card>
              <CardHeader>
                <CardTitle>팀별 출역 현황</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {teamStats.map((team, i) => (
                    <div key={i} className="p-4 border rounded-lg">
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center gap-3">
                          <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold">
                            {team.team.charAt(0)}
                          </div>
                          <div>
                            <div className="font-semibold">{team.team}</div>
                            <div className="text-sm text-muted-foreground">총 {team.total}명</div>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-2xl font-bold">{team.rate}%</div>
                          <div className="text-sm text-muted-foreground">출역률</div>
                        </div>
                      </div>

                      <div className="grid grid-cols-3 gap-4 text-center">
                        <div className="p-3 bg-green-50 rounded-lg">
                          <div className="text-lg font-bold text-green-600">{team.present}</div>
                          <div className="text-xs text-muted-foreground">출근</div>
                        </div>
                        <div className="p-3 bg-orange-50 rounded-lg">
                          <div className="text-lg font-bold text-orange-600">{team.late}</div>
                          <div className="text-xs text-muted-foreground">지각</div>
                        </div>
                        <div className="p-3 bg-red-50 rounded-lg">
                          <div className="text-lg font-bold text-red-600">{team.absent}</div>
                          <div className="text-xs text-muted-foreground">결근</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>

        {/* 빠른 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>빠른 액션</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Camera className="h-6 w-6" />
                <span className="text-sm">일괄 출근 처리</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Download className="h-6 w-6" />
                <span className="text-sm">출역 보고서</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <AlertTriangle className="h-6 w-6" />
                <span className="text-sm">결근자 알림</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CheckCircle2 className="h-6 w-6" />
                <span className="text-sm">출역 승인</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}