import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import {
  Building2,
  Search,
  Filter,
  Plus,
  Eye,
  Edit,
  Trash2,
  Users,
  Calendar,
  CreditCard
} from "lucide-react";
import { useState } from "react";

export default function TenantsSuper() {
  const [searchTerm, setSearchTerm] = useState("");

  // Mock Data
  const tenants = [
    {
      id: 1,
      name: "주식회사 대한건설",
      businessNumber: "123-45-67890",
      ceo: "김대표",
      plan: "Enterprise",
      status: "active",
      users: 245,
      sites: 12,
      joinDate: "2024-01-15",
      lastLogin: "2025-01-01",
      monthlyFee: 2500000,
      email: "admin@daehan.co.kr",
      phone: "02-1234-5678"
    },
    {
      id: 2,
      name: "삼성건설 주식회사",
      businessNumber: "234-56-78901",
      ceo: "이대표",
      plan: "Pro",
      status: "active",
      users: 156,
      sites: 8,
      joinDate: "2024-02-20",
      lastLogin: "2024-12-31",
      monthlyFee: 1800000,
      email: "admin@samsung.co.kr",
      phone: "02-2345-6789"
    },
    {
      id: 3,
      name: "현대건설 주식회사",
      businessNumber: "345-67-89012",
      ceo: "박대표",
      plan: "Enterprise",
      status: "active",
      users: 320,
      sites: 15,
      joinDate: "2023-11-10",
      lastLogin: "2025-01-01",
      monthlyFee: 3200000,
      email: "admin@hyundai.co.kr",
      phone: "02-3456-7890"
    },
    {
      id: 4,
      name: "GS건설 주식회사",
      businessNumber: "456-78-90123",
      ceo: "최대표",
      plan: "Pro",
      status: "pending",
      users: 89,
      sites: 5,
      joinDate: "2024-12-01",
      lastLogin: "2024-12-30",
      monthlyFee: 1200000,
      email: "admin@gs.co.kr",
      phone: "02-4567-8901"
    },
    {
      id: 5,
      name: "포스코건설",
      businessNumber: "567-89-01234",
      ceo: "정대표",
      plan: "Basic",
      status: "suspended",
      users: 45,
      sites: 3,
      joinDate: "2024-03-15",
      lastLogin: "2024-12-15",
      monthlyFee: 450000,
      email: "admin@posco.co.kr",
      phone: "02-5678-9012"
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active": return "bg-green-100 text-green-700";
      case "pending": return "bg-orange-100 text-orange-700";
      case "suspended": return "bg-red-100 text-red-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "active": return "활성";
      case "pending": return "승인 대기";
      case "suspended": return "일시 정지";
      default: return "알 수 없음";
    }
  };

  const getPlanColor = (plan: string) => {
    switch (plan) {
      case "Enterprise": return "bg-purple-100 text-purple-700";
      case "Pro": return "bg-blue-100 text-blue-700";
      case "Basic": return "bg-gray-100 text-gray-700";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  const filteredTenants = tenants.filter(tenant =>
    tenant.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    tenant.businessNumber.includes(searchTerm) ||
    tenant.ceo.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        {/* 테넌트 통계 */}
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 테넌트</p>
                <div className="text-3xl font-bold mt-2">{tenants.length}</div>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <Building2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">활성 테넌트</p>
                <div className="text-3xl font-bold text-green-600 mt-2">
                  {tenants.filter(t => t.status === "active").length}
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                <Building2 className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">총 사용자</p>
                <div className="text-3xl font-bold mt-2">
                  {tenants.reduce((sum, t) => sum + t.users, 0)}
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
                <p className="text-sm font-medium text-muted-foreground">월 총 매출</p>
                <div className="text-3xl font-bold text-orange-600 mt-2">
                  ₩{(tenants.reduce((sum, t) => sum + t.monthlyFee, 0) / 1000000).toFixed(0)}M
                </div>
              </div>
              <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                <CreditCard className="h-6 w-6" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 테넌트 관리 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="h-5 w-5" />
              테넌트 관리
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="회사명, 사업자번호, 대표자명으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Button variant="outline">
                <Filter className="h-4 w-4 mr-2" />
                필터
              </Button>
              <Dialog>
                <DialogTrigger asChild>
                  <Button>
                    <Plus className="h-4 w-4 mr-2" />
                    신규 테넌트
                  </Button>
                </DialogTrigger>
                <DialogContent className="max-w-2xl">
                  <DialogHeader>
                    <DialogTitle>신규 테넌트 등록</DialogTitle>
                  </DialogHeader>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="company-name">회사명</Label>
                      <Input id="company-name" placeholder="주식회사 스마트콘" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="business-number">사업자등록번호</Label>
                      <Input id="business-number" placeholder="123-45-67890" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="ceo-name">대표자명</Label>
                      <Input id="ceo-name" placeholder="홍길동" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="email">관리자 이메일</Label>
                      <Input id="email" type="email" placeholder="admin@company.com" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="phone">연락처</Label>
                      <Input id="phone" placeholder="02-1234-5678" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="plan">요금제</Label>
                      <select id="plan" className="w-full p-2 border rounded-md">
                        <option value="Basic">Basic</option>
                        <option value="Pro">Pro</option>
                        <option value="Enterprise">Enterprise</option>
                      </select>
                    </div>
                  </div>
                  <div className="flex justify-end gap-2 mt-6">
                    <Button variant="outline">취소</Button>
                    <Button>등록</Button>
                  </div>
                </DialogContent>
              </Dialog>
            </div>

            {/* 테넌트 목록 */}
            <div className="space-y-4">
              {filteredTenants.map((tenant) => (
                <div key={tenant.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold">
                          {tenant.name.charAt(0)}
                        </div>
                        <div>
                          <h3 className="font-semibold text-lg">{tenant.name}</h3>
                          <p className="text-sm text-muted-foreground">
                            {tenant.businessNumber} • {tenant.ceo}
                          </p>
                        </div>
                        <div className="flex gap-2">
                          <Badge className={getStatusColor(tenant.status)}>
                            {getStatusText(tenant.status)}
                          </Badge>
                          <Badge className={getPlanColor(tenant.plan)}>
                            {tenant.plan}
                          </Badge>
                        </div>
                      </div>
                      
                      <div className="grid md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <div className="text-muted-foreground">사용자 수</div>
                          <div className="font-semibold">{tenant.users}명</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">현장 수</div>
                          <div className="font-semibold">{tenant.sites}개</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">월 구독료</div>
                          <div className="font-semibold text-green-600">₩{tenant.monthlyFee.toLocaleString()}</div>
                        </div>
                        <div>
                          <div className="text-muted-foreground">가입일</div>
                          <div className="font-semibold">{tenant.joinDate}</div>
                        </div>
                      </div>
                    </div>

                    <div className="flex gap-2 ml-4">
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button variant="outline" size="sm">
                            <Eye className="h-4 w-4 mr-1" />
                            상세
                          </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-2xl">
                          <DialogHeader>
                            <DialogTitle>{tenant.name} 상세 정보</DialogTitle>
                          </DialogHeader>
                          <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                              <div>
                                <Label className="text-sm font-medium">회사명</Label>
                                <div className="mt-1 p-2 border rounded">{tenant.name}</div>
                              </div>
                              <div>
                                <Label className="text-sm font-medium">사업자등록번호</Label>
                                <div className="mt-1 p-2 border rounded">{tenant.businessNumber}</div>
                              </div>
                              <div>
                                <Label className="text-sm font-medium">대표자</Label>
                                <div className="mt-1 p-2 border rounded">{tenant.ceo}</div>
                              </div>
                              <div>
                                <Label className="text-sm font-medium">이메일</Label>
                                <div className="mt-1 p-2 border rounded">{tenant.email}</div>
                              </div>
                              <div>
                                <Label className="text-sm font-medium">연락처</Label>
                                <div className="mt-1 p-2 border rounded">{tenant.phone}</div>
                              </div>
                              <div>
                                <Label className="text-sm font-medium">요금제</Label>
                                <div className="mt-1 p-2 border rounded">{tenant.plan}</div>
                              </div>
                            </div>
                          </div>
                        </DialogContent>
                      </Dialog>

                      <Button variant="outline" size="sm">
                        <Edit className="h-4 w-4 mr-1" />
                        수정
                      </Button>

                      {tenant.status === "pending" && (
                        <Button size="sm">
                          승인
                        </Button>
                      )}

                      {tenant.status === "active" && (
                        <Button variant="destructive" size="sm">
                          <Trash2 className="h-4 w-4 mr-1" />
                          정지
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredTenants.length === 0 && (
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