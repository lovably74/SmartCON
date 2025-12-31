import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  FileText,
  Search,
  Filter,
  Download,
  Eye,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Users
} from "lucide-react";
import { useState } from "react";

export default function ContractsHQ() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const contracts = [
    {
      id: 1,
      contractNumber: "CON-2025-001",
      workerName: "김철수",
      site: "강남 테헤란로 오피스 신축공사",
      team: "철근팀",
      startDate: "2025-01-01",
      endDate: "2025-06-30",
      dailyWage: 250000,
      status: "active",
      signedDate: "2024-12-28"
    },
    {
      id: 2,
      contractNumber: "CON-2025-002",
      workerName: "이영희",
      site: "판교 데이터센터 건립공사",
      team: "콘크리트팀",
      startDate: "2025-01-01",
      endDate: "2025-08-31",
      dailyWage: 230000,
      status: "active",
      signedDate: "2024-12-29"
    },
    {
      id: 3,
      contractNumber: "CON-2025-003",
      workerName: "박민수",
      site: "강남 테헤란로 오피스 신축공사",
      team: "철근팀",
      startDate: "2025-02-01",
      endDate: "2025-07-31",
      dailyWage: 220000,
      status: "pending",
      signedDate: null
    },
    {
      id: 4,
      contractNumber: "CON-2024-156",
      workerName: "정수진",
      site: "부산 에코델타시티 조성공사",
      team: "전기팀",
      startDate: "2024-06-01",
      endDate: "2024-12-31",
      dailyWage: 280000,
      status: "completed",
      signedDate: "2024-05-25"
    },
    {
      id: 5,
      contractNumber: "CON-2025-004",
      workerName: "최동호",
      site: "인천공항 제2터미널 확장공사",
      team: "배관팀",
      startDate: "2025-01-15",
      endDate: "2025-12-31",
      dailyWage: 260000,
      status: "draft",
      signedDate: null
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active": return "bg-green-100 text-green-700";
      case "completed": return "bg-blue-100 text-blue-700";
      case "pending": return "bg-orange-100 text-orange-700";
      case "draft": return "bg-gray-100 text-gray-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "active": return "진행중";
      case "completed": return "완료";
      case "pending": return "서명 대기";
      case "draft": return "작성중";
      default: return "알 수 없음";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "active": return <CheckCircle2 className="h-4 w-4" />;
      case "completed": return <CheckCircle2 className="h-4 w-4" />;
      case "pending": return <Clock className="h-4 w-4" />;
      case "draft": return <FileText className="h-4 w-4" />;
      default: return <AlertTriangle className="h-4 w-4" />;
    }
  };

  const filteredContracts = contracts.filter(contract =>
    contract.workerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    contract.site.toLowerCase().includes(searchTerm.toLowerCase()) ||
    contract.contractNumber.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        {/* 계약 통계 */}
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 계약</p>
                <div className="text-3xl font-bold mt-2">{contracts.length}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <FileText className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">진행중</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  {contracts.filter(c => c.status === "active").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <CheckCircle2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">서명 대기</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">
                  {contracts.filter(c => c.status === "pending").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <Clock className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">완료</p>
                <div className="text-3xl font-bold text-purple-600 mt-2">
                  {contracts.filter(c => c.status === "completed").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <Users className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 계약 관리 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              전자근로계약 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="노무자명, 현장명, 계약번호로 검색..."
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

            {/* 계약 목록 */}
            <div className="space-y-4">
              {filteredContracts.map((contract) => (
                <div key={contract.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm">
                          {contract.workerName.charAt(0)}
                        </div>
                        <div>
                          <h3 className="font-semibold">{contract.contractNumber}</h3>
                          <p className="text-sm text-muted-foreground">{contract.workerName}</p>
                        </div>
                        <Badge className={getStatusColor(contract.status)}>
                          {getStatusIcon(contract.status)}
                          <span className="ml-1">{getStatusText(contract.status)}</span>
                        </Badge>
                      </div>
                      
                      <div className="grid md:grid-cols-2 gap-4 text-sm">
                        <div>
                          <div className="text-muted-foreground">현장</div>
                          <div className="font-medium">{contract.site}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">팀</div>
                          <div className="font-medium">{contract.team}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">계약 기간</div>
                          <div className="font-medium">{contract.startDate} ~ {contract.endDate}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">일당</div>
                          <div className="font-medium text-green-600">₩{contract.dailyWage.toLocaleString()}</div>
                        </div>
                      </div>

                      {contract.signedDate && (
                        <div className="mt-2 text-sm text-muted-foreground">
                          <span className="font-medium">서명일:</span> {contract.signedDate}
                        </div>
                      )}
                    </div>

                    <div className="flex gap-2 ml-4">
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4 mr-1" />
                        상세보기
                      </Button>

                      <Button variant="outline" size="sm">
                        <Download className="h-4 w-4 mr-1" />
                        다운로드
                      </Button>

                      {contract.status === "draft" && (
                        <Button size="sm">
                          편집
                        </Button>
                      )}

                      {contract.status === "pending" && (
                        <Button size="sm">
                          알림 발송
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredContracts.length === 0 && (
              <div className="text-center py-12">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">검색 결과가 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 계약 관리 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>계약 관리</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <FileText className="h-6 w-6" />
                <span className="text-sm">신규 계약 작성</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Download className="h-6 w-6" />
                <span className="text-sm">계약서 템플릿</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CheckCircle2 className="h-6 w-6" />
                <span className="text-sm">일괄 승인</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <AlertTriangle className="h-6 w-6" />
                <span className="text-sm">만료 예정 계약</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}