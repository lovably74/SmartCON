import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Building2, MoreHorizontal, Search } from "lucide-react";

export default function TenantsSuper() {
  const tenants = [
    { id: 1, name: "대우건설", plan: "Enterprise", sites: 15, users: 450, status: "정상", joined: "2024.01.15" },
    { id: 2, name: "현대건설", plan: "Enterprise", sites: 22, users: 890, status: "정상", joined: "2024.02.01" },
    { id: 3, name: "GS건설", plan: "Enterprise", sites: 18, users: 620, status: "정상", joined: "2024.02.10" },
    { id: 4, name: "삼성물산", plan: "Enterprise", sites: 30, users: 1200, status: "정상", joined: "2024.01.05" },
    { id: 5, name: "포스코이앤씨", plan: "Pro", sites: 8, users: 210, status: "정상", joined: "2024.03.15" },
    { id: 6, name: "DL이앤씨", plan: "Pro", sites: 12, users: 340, status: "주의", joined: "2024.03.20" },
    { id: 7, name: "롯데건설", plan: "Pro", sites: 10, users: 280, status: "정상", joined: "2024.04.01" },
    { id: 8, name: "SK에코플랜트", plan: "Enterprise", sites: 14, users: 410, status: "정상", joined: "2024.04.15" },
    { id: 9, name: "한화 건설부문", plan: "Pro", sites: 6, users: 150, status: "정지", joined: "2024.05.01" },
    { id: 10, name: "HDC현대산업개발", plan: "Pro", sites: 9, users: 230, status: "정상", joined: "2024.05.10" },
  ];

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">고객사(Tenant) 관리</h2>
            <p className="text-muted-foreground">전체 가입 고객사 현황을 조회하고 관리합니다.</p>
          </div>
          <Button>+ 고객사 수동 등록</Button>
        </div>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>고객사 목록 ({tenants.length})</CardTitle>
              <div className="flex w-full max-w-sm items-center space-x-2">
                <Input placeholder="회사명 또는 사업자번호 검색" />
                <Button size="icon" variant="ghost">
                  <Search className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>회사명</TableHead>
                  <TableHead>요금제</TableHead>
                  <TableHead>운영 현장</TableHead>
                  <TableHead>사용자 수</TableHead>
                  <TableHead>가입일</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tenants.map((tenant) => (
                  <TableRow key={tenant.id}>
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        <Building2 className="h-4 w-4 text-muted-foreground" />
                        {tenant.name}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{tenant.plan}</Badge>
                    </TableCell>
                    <TableCell>{tenant.sites}개</TableCell>
                    <TableCell>{tenant.users}명</TableCell>
                    <TableCell>{tenant.joined}</TableCell>
                    <TableCell>
                      <Badge variant={
                        tenant.status === "정상" ? "default" :
                        tenant.status === "정지" ? "destructive" : "secondary"
                      } className={
                        tenant.status === "정상" ? "bg-green-500 hover:bg-green-600" : ""
                      }>
                        {tenant.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
