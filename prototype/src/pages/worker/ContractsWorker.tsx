import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import {
  FileText,
  Download,
  Eye,
  PenTool,
  CheckCircle2,
  Clock,
  AlertCircle,
  Search
} from "lucide-react";
import { useState } from "react";

export default function ContractsWorker() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const contracts = [
    {
      id: 1,
      title: "강남 테헤란로 오피스 신축공사 근로계약서",
      site: "강남 테헤란로 현장",
      startDate: "2025-01-01",
      endDate: "2025-06-30",
      status: "active",
      statusText: "계약 중",
      wage: "250,000원/일",
      signedDate: "2024-12-28",
      contractType: "일용직"
    },
    {
      id: 2,
      title: "판교 데이터센터 건립공사 근로계약서",
      site: "판교 데이터센터 현장",
      startDate: "2024-10-01",
      endDate: "2024-12-31",
      status: "completed",
      statusText: "완료",
      wage: "230,000원/일",
      signedDate: "2024-09-25",
      contractType: "일용직"
    },
    {
      id: 3,
      title: "부산 에코델타시티 조성공사 근로계약서",
      site: "부산 에코델타시티 현장",
      startDate: "2025-02-01",
      endDate: "2025-08-31",
      status: "pending",
      statusText: "서명 대기",
      wage: "270,000원/일",
      signedDate: null,
      contractType: "일용직"
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active": return "bg-green-100 text-green-700";
      case "completed": return "bg-gray-100 text-gray-700";
      case "pending": return "bg-orange-100 text-orange-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "active": return <CheckCircle2 className="h-4 w-4" />;
      case "completed": return <CheckCircle2 className="h-4 w-4" />;
      case "pending": return <Clock className="h-4 w-4" />;
      default: return <AlertCircle className="h-4 w-4" />;
    }
  };

  const filteredContracts = contracts.filter(contract =>
    contract.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    contract.site.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        {/* 계약 현황 요약 */}
        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">진행 중인 계약</p>
                <div className="text-3xl font-bold text-green-600 mt-2">1</div>
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
                <div className="text-3xl font-bold text-orange-600 mt-2">1</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <Clock className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">완료된 계약</p>
                <div className="text-3xl font-bold text-gray-600 mt-2">1</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-gray-100 flex items-center justify-center text-gray-600">
                <FileText className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 검색 및 필터 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              전자 근로계약서 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="계약서 제목이나 현장명으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>

            {/* 계약서 목록 */}
            <div className="space-y-4">
              {filteredContracts.map((contract) => (
                <div key={contract.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="font-semibold">{contract.title}</h3>
                        <Badge className={getStatusColor(contract.status)}>
                          {getStatusIcon(contract.status)}
                          <span className="ml-1">{contract.statusText}</span>
                        </Badge>
                      </div>
                      
                      <div className="grid md:grid-cols-2 gap-4 text-sm text-muted-foreground">
                        <div>
                          <div className="mb-1">
                            <span className="font-medium">현장:</span> {contract.site}
                          </div>
                          <div className="mb-1">
                            <span className="font-medium">계약 기간:</span> {contract.startDate} ~ {contract.endDate}
                          </div>
                          <div>
                            <span className="font-medium">계약 형태:</span> {contract.contractType}
                          </div>
                        </div>
                        <div>
                          <div className="mb-1">
                            <span className="font-medium">일당:</span> <span className="text-green-600 font-semibold">{contract.wage}</span>
                          </div>
                          {contract.signedDate && (
                            <div>
                              <span className="font-medium">서명일:</span> {contract.signedDate}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex gap-2 ml-4">
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button variant="outline" size="sm">
                            <Eye className="h-4 w-4 mr-1" />
                            보기
                          </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
                          <DialogHeader>
                            <DialogTitle>{contract.title}</DialogTitle>
                          </DialogHeader>
                          <div className="space-y-4">
                            {/* 계약서 미리보기 */}
                            <div className="border rounded-lg p-6 bg-white">
                              <div className="text-center mb-6">
                                <h2 className="text-xl font-bold">근로계약서</h2>
                                <p className="text-sm text-muted-foreground mt-2">Contract No. {contract.id.toString().padStart(6, '0')}</p>
                              </div>
                              
                              <div className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                  <div>
                                    <Label className="text-sm font-medium">근로자 성명</Label>
                                    <div className="mt-1 p-2 border rounded">박민수</div>
                                  </div>
                                  <div>
                                    <Label className="text-sm font-medium">주민등록번호</Label>
                                    <div className="mt-1 p-2 border rounded">850101-1******</div>
                                  </div>
                                </div>
                                
                                <div>
                                  <Label className="text-sm font-medium">근무지</Label>
                                  <div className="mt-1 p-2 border rounded">{contract.site}</div>
                                </div>
                                
                                <div className="grid grid-cols-2 gap-4">
                                  <div>
                                    <Label className="text-sm font-medium">계약 시작일</Label>
                                    <div className="mt-1 p-2 border rounded">{contract.startDate}</div>
                                  </div>
                                  <div>
                                    <Label className="text-sm font-medium">계약 종료일</Label>
                                    <div className="mt-1 p-2 border rounded">{contract.endDate}</div>
                                  </div>
                                </div>
                                
                                <div>
                                  <Label className="text-sm font-medium">임금</Label>
                                  <div className="mt-1 p-2 border rounded">{contract.wage}</div>
                                </div>
                              </div>
                              
                              {contract.status === "pending" && (
                                <div className="mt-6 pt-6 border-t">
                                  <Button className="w-full" size="lg">
                                    <PenTool className="h-4 w-4 mr-2" />
                                    전자서명하기
                                  </Button>
                                </div>
                              )}
                            </div>
                          </div>
                        </DialogContent>
                      </Dialog>

                      {contract.status !== "pending" && (
                        <Button variant="outline" size="sm">
                          <Download className="h-4 w-4 mr-1" />
                          다운로드
                        </Button>
                      )}

                      {contract.status === "pending" && (
                        <Button size="sm">
                          <PenTool className="h-4 w-4 mr-1" />
                          서명하기
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

        {/* 계약 관련 안내 */}
        <Card>
          <CardHeader>
            <CardTitle>전자계약 이용 안내</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <h4 className="font-semibold mb-2">전자서명 방법</h4>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>1. 계약서 내용을 꼼꼼히 확인하세요</li>
                  <li>2. '전자서명하기' 버튼을 클릭하세요</li>
                  <li>3. 본인인증 후 서명을 완료하세요</li>
                  <li>4. 서명 완료 후 계약서를 다운로드할 수 있습니다</li>
                </ul>
              </div>
              <div>
                <h4 className="font-semibold mb-2">주의사항</h4>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>• 계약 내용을 반드시 확인 후 서명하세요</li>
                  <li>• 서명 후에는 계약 내용 변경이 어렵습니다</li>
                  <li>• 궁금한 사항은 현장 관리자에게 문의하세요</li>
                  <li>• 계약서는 안전한 곳에 보관하세요</li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}