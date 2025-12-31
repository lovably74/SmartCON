import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  FileText,
  Download,
  Send,
  Search,
  Filter,
  Calendar,
  Building2,
  CheckCircle2,
  Clock,
  AlertTriangle
} from "lucide-react";
import { useState } from "react";

export default function TaxSuper() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const taxStats = {
    totalInvoices: 156,
    sentInvoices: 142,
    pendingInvoices: 8,
    failedInvoices: 6,
    totalAmount: 45600000,
    thisMonthAmount: 45600000
  };

  const taxInvoices = [
    {
      id: 1,
      invoiceNumber: "2025-001",
      tenantName: "주식회사 대한건설",
      businessNumber: "123-45-67890",
      amount: 2500000,
      vatAmount: 250000,
      totalAmount: 2750000,
      issueDate: "2025-01-01",
      status: "sent",
      plan: "Enterprise"
    },
    {
      id: 2,
      invoiceNumber: "2025-002",
      tenantName: "삼성건설 주식회사",
      businessNumber: "234-56-78901",
      amount: 1800000,
      vatAmount: 180000,
      totalAmount: 1980000,
      issueDate: "2025-01-01",
      status: "sent",
      plan: "Pro"
    },
    {
      id: 3,
      invoiceNumber: "2024-156",
      tenantName: "현대건설 주식회사",
      businessNumber: "345-67-89012",
      amount: 3200000,
      vatAmount: 320000,
      totalAmount: 3520000,
      issueDate: "2024-12-31",
      status: "failed",
      plan: "Enterprise",
      failureReason: "이메일 전송 실패"
    },
    {
      id: 4,
      invoiceNumber: "2025-003",
      tenantName: "GS건설 주식회사",
      businessNumber: "456-78-90123",
      amount: 1200000,
      vatAmount: 120000,
      totalAmount: 1320000,
      issueDate: "2025-01-01",
      status: "pending",
      plan: "Pro"
    },
    {
      id: 5,
      invoiceNumber: "2024-155",
      tenantName: "포스코건설",
      businessNumber: "567-89-01234",
      amount: 450000,
      vatAmount: 45000,
      totalAmount: 495000,
      issueDate: "2024-12-31",
      status: "sent",
      plan: "Basic"
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "sent": return "bg-green-100 text-green-700";
      case "failed": return "bg-red-100 text-red-700";
      case "pending": return "bg-orange-100 text-orange-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "sent": return "발송 완료";
      case "failed": return "발송 실패";
      case "pending": return "발송 대기";
      default: return "알 수 없음";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "sent": return <CheckCircle2 className="h-4 w-4" />;
      case "failed": return <AlertTriangle className="h-4 w-4" />;
      case "pending": return <Clock className="h-4 w-4" />;
      default: return <FileText className="h-4 w-4" />;
    }
  };

  const filteredInvoices = taxInvoices.filter(invoice =>
    invoice.tenantName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    invoice.invoiceNumber.includes(searchTerm) ||
    invoice.businessNumber.includes(searchTerm)
  );

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        {/* 세금계산서 통계 */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 발행</p>
                <div className="text-3xl font-bold mt-2">{taxStats.totalInvoices}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <FileText className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">발송 완료</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  {taxStats.sentInvoices}
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
                <p className="text-sm font-medium text-muted-foreground">발송 대기</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">
                  {taxStats.pendingInvoices}
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
                <p className="text-sm font-medium text-muted-foreground">이번 달 금액</p>
                <div className="text-3xl font-bold text-purple-600 mt-2">
                  ₩{(taxStats.thisMonthAmount / 1000000).toFixed(0)}M
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                <Building2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 세금계산서 관리 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              세금계산서 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="회사명, 계산서번호, 사업자번호로 검색..."
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
                <Calendar className="h-4 w-4 mr-2" />
                기간 선택
              </Button>
              <Button>
                <Send className="h-4 w-4 mr-2" />
                일괄 발송
              </Button>
            </div>

            {/* 세금계산서 목록 */}
            <div className="space-y-4">
              {filteredInvoices.map((invoice) => (
                <div key={invoice.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm">
                          {invoice.tenantName.charAt(0)}
                        </div>
                        <div>
                          <h3 className="font-semibold">{invoice.tenantName}</h3>
                          <p className="text-sm text-muted-foreground">
                            {invoice.businessNumber} • {invoice.invoiceNumber}
                          </p>
                        </div>
                        <Badge className={getStatusColor(invoice.status)}>
                          {getStatusIcon(invoice.status)}
                          <span className="ml-1">{getStatusText(invoice.status)}</span>
                        </Badge>
                      </div>
                      
                      <div className="grid md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <div className="text-muted-foreground">공급가액</div>
                          <div className="font-semibold">₩{invoice.amount.toLocaleString()}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">부가세</div>
                          <div className="font-semibold">₩{invoice.vatAmount.toLocaleString()}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">총 금액</div>
                          <div className="font-semibold text-green-600">₩{invoice.totalAmount.toLocaleString()}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">발행일</div>
                          <div className="font-semibold">{invoice.issueDate}</div>
                        </div>
                      </div>

                      {invoice.status === "failed" && invoice.failureReason && (
                        <div className="mt-3 p-2 bg-red-50 border border-red-200 rounded text-sm text-red-700">
                          <AlertTriangle className="h-4 w-4 inline mr-1" />
                          실패 사유: {invoice.failureReason}
                        </div>
                      )}
                    </div>

                    <div className="flex gap-2 ml-4">
                      <Button variant="outline" size="sm">
                        <Download className="h-4 w-4 mr-1" />
                        다운로드
                      </Button>

                      {invoice.status === "pending" && (
                        <Button size="sm">
                          <Send className="h-4 w-4 mr-1" />
                          발송
                        </Button>
                      )}

                      {invoice.status === "failed" && (
                        <Button size="sm">
                          <Send className="h-4 w-4 mr-1" />
                          재발송
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredInvoices.length === 0 && (
              <div className="text-center py-12">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">검색 결과가 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 세금계산서 관리 액션 */}
        <Card>
          <CardHeader>
            <CardTitle>세금계산서 관리</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <FileText className="h-6 w-6" />
                <span className="text-sm">계산서 발행</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Send className="h-6 w-6" />
                <span className="text-sm">일괄 발송</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Download className="h-6 w-6" />
                <span className="text-sm">발행 내역</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Calendar className="h-6 w-6" />
                <span className="text-sm">발행 일정</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}