import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  CreditCard,
  Search,
  Filter,
  Download,
  Eye,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Calculator
} from "lucide-react";
import { useState } from "react";

export default function SettlementsHQ() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const settlements = [
    {
      id: 1,
      period: "2024-12",
      site: "강남 테헤란로 오피스 신축공사",
      totalWorkers: 124,
      totalDays: 2480,
      totalAmount: 620000000,
      status: "completed",
      processedDate: "2025-01-05"
    },
    {
      id: 2,
      period: "2024-12",
      site: "판교 데이터센터 건립공사",
      totalWorkers: 85,
      totalDays: 1700,
      totalAmount: 391000000,
      status: "completed",
      processedDate: "2025-01-05"
    },
    {
      id: 3,
      period: "2025-01",
      site: "강남 테헤란로 오피스 신축공사",
      totalWorkers: 124,
      totalDays: 1240,
      totalAmount: 310000000,
      status: "processing",
      processedDate: null
    },
    {
      id: 4,
      period: "2024-12",
      site: "부산 에코델타시티 조성공사",
      totalWorkers: 230,
      totalDays: 4600,
      totalAmount: 1150000000,
      status: "pending",
      processedDate: null
    },
    {
      id: 5,
      period: "2025-01",
      site: "인천공항 제2터미널 확장공사",
      totalWorkers: 156,
      totalDays: 780,
      totalAmount: 202800000,
      status: "draft",
      processedDate: null
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed": return "bg-green-100 text-green-700";
      case "processing": return "bg-blue-100 text-blue-700";
      case "pending": return "bg-orange-100 text-orange-700";
      case "draft": return "bg-gray-100 text-gray-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "completed": return "완료";
      case "processing": return "처리중";
      case "pending": return "승인 대기";
      case "draft": return "작성중";
      default: return "알 수 없음";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed": return <CheckCircle2 className="h-4 w-4" />;
      case "processing": return <Clock className="h-4 w-4" />;
      case "pending": return <AlertTriangle className="h-4 w-4" />;
      case "draft": return <Calculator className="h-4 w-4" />;
      default: return <AlertTriangle className="h-4 w-4" />;
    }
  };

  const filteredSettlements = settlements.filter(settlement =>
    settlement.site.toLowerCase().includes(searchTerm.toLowerCase()) ||
    settlement.period.includes(searchTerm)
  );

  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        {/* 정산 통계 */}
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 정산</p>
                <div className="text-3xl font-bold mt-2">{settlements.length}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Calculator className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">완료</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  {settlements.filter(s => s.status === "completed").length}
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
                <p className="text-sm font-medium text-muted-foreground">처리중</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">
                  {settlements.filter(s => s.status === "processing" || s.status === "pending").length}
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
                <p className="text-sm font-medium text-muted-foreground">이번 달 총액</p>
                <div className="text-3xl font-bold text-purple-600 mt-2">
                  ₩{(settlements.reduce((sum, s) => sum + s.totalAmount, 0) / 100000000).toFixed(0)}억
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <CreditCard className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 정산 관리 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              노무비 정산 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="현장명, 정산 기간으로 검색..."
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
              <Button>
                <Calculator className="h-4 w-4 mr-2" />
                신규 정산
              </Button>
            </div>

            {/* 정산 목록 */}
            <div className="space-y-4">
              {filteredSettlements.map((settlement) => (
                <div key={settlement.id} className="border rounded-lg p-6 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold">
                          {settlement.period.split('-')[1]}
                        </div>
                        <div>
                          <h3 className="font-semibold text-lg">{settlement.site}</h3>
                          <p className="text-sm text-muted-foreground">정산 기간: {settlement.period}</p>
                        </div>
                        <Badge className={getStatusColor(settlement.status)}>
                          {getStatusIcon(settlement.status)}
                          <span className="ml-1">{getStatusText(settlement.status)}</span>
                        </Badge>
                      </div>
                      
                      <div className="grid md:grid-cols-4 gap-6">
                        <div className="text-center p-4 bg-blue-50 rounded-lg">
                          <div className="text-2xl font-bold text-blue-600">{settlement.totalWorkers}</div>
                          <div className="text-sm text-muted-foreground">총 노무자</div>
                        </div>
                        <div className="text-center p-4 bg-green-50 rounded-lg">
                          <div className="text-2xl font-bold text-green-600">{settlement.totalDays.toLocaleString()}</div>
                          <div className="text-sm text-muted-foreground">총 출역일</div>
                        </div>
                        <div className="text-center p-4 bg-orange-50 rounded-lg">
                          <div className="text-2xl font-bold text-orange-600">
                            ₩{(settlement.totalAmount / 100000000).toFixed(1)}억
                          </div>
                          <div className="text-sm text-muted-foreground">총 정산액</div>
                        </div>
                        <div className="text-center p-4 bg-purple-50 rounded-lg">
                          <div className="text-2xl font-bold text-purple-600">
                            ₩{Math.round(settlement.totalAmount / settlement.totalDays).toLocaleString()}
                          </div>
                          <div className="text-sm text-muted-foreground">평균 일당</div>
                        </div>
                      </div>

                      {settlement.processedDate && (
                        <div className="mt-4 text-sm text-muted-foreground">
                          <span className="font-medium">처리일:</span> {settlement.processedDate}
                        </div>
                      )}
                    </div>

                    <div className="flex gap-2 ml-6">
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4 mr-1" />
                        상세보기
                      </Button>

                      {settlement.status === "completed" && (
                        <Button variant="outline" size="sm">
                          <Download className="h-4 w-4 mr-1" />
                          다운로드
                        </Button>
                      )}

                      {settlement.status === "draft" && (
                        <Button size="sm">
                          <Calculator className="h-4 w-4 mr-1" />
                          계산하기
                        </Button>
                      )}

                      {settlement.status === "pending" && (
                        <Button size="sm">
                          <CheckCircle2 className="h-4 w-4 mr-1" />
                          승인
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredSettlements.length === 0 && (
              <div className="text-center py-12">
                <Calculator className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">검색 결과가 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 정산 관리 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>정산 관리</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Calculator className="h-6 w-6" />
                <span className="text-sm">월별 정산</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Download className="h-6 w-6" />
                <span className="text-sm">정산 보고서</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CheckCircle2 className="h-6 w-6" />
                <span className="text-sm">일괄 승인</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <CreditCard className="h-6 w-6" />
                <span className="text-sm">지급 내역</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}