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
import { Building2, MoreHorizontal, Search, Loader2, AlertTriangle } from "lucide-react";
import { useTenants, useUpdateTenantStatus } from "@/hooks/useAdminApi";
import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";

export default function TenantsSuper() {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [page, setPage] = useState(0);
  const [selectedTenant, setSelectedTenant] = useState<any>(null);
  const [newStatus, setNewStatus] = useState<string>("");

  const { data: tenantsData, isLoading, error } = useTenants({
    search: search || undefined,
    status: statusFilter || undefined,
    page,
    size: 20,
    sortBy: "createdAt",
    sortDir: "desc"
  });

  const updateTenantMutation = useUpdateTenantStatus();

  const getStatusText = (status: string) => {
    switch (status) {
      case 'ACTIVE': return '정상';
      case 'SUSPENDED': return '중지';
      case 'TERMINATED': return '해지';
      default: return status;
    }
  };

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'default';
      case 'SUSPENDED': return 'secondary';
      case 'TERMINATED': return 'destructive';
      default: return 'outline';
    }
  };

  const handleStatusChange = async () => {
    if (selectedTenant && newStatus) {
      await updateTenantMutation.mutateAsync({
        tenantId: selectedTenant.id,
        status: newStatus
      });
      setSelectedTenant(null);
      setNewStatus("");
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0); // 검색 시 첫 페이지로 이동
  };

  if (isLoading) {
    return (
      <DashboardLayout role="super">
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">데이터를 불러오는 중...</span>
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout role="super">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <AlertTriangle className="h-8 w-8 text-red-500 mx-auto mb-2" />
            <p className="text-red-600">데이터를 불러오는 중 오류가 발생했습니다.</p>
            <p className="text-sm text-muted-foreground mt-1">{error.message}</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

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
              <CardTitle>고객사 목록 ({tenantsData?.totalElements || 0})</CardTitle>
              <div className="flex items-center space-x-2">
                <form onSubmit={handleSearch} className="flex w-full max-w-sm items-center space-x-2">
                  <Input 
                    placeholder="회사명 또는 사업자번호 검색" 
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                  <Button type="submit" size="icon" variant="ghost">
                    <Search className="h-4 w-4" />
                  </Button>
                </form>
                <select 
                  className="px-3 py-2 border rounded-md text-sm"
                  value={statusFilter}
                  onChange={(e) => {
                    setStatusFilter(e.target.value);
                    setPage(0);
                  }}
                >
                  <option value="">전체 상태</option>
                  <option value="ACTIVE">정상</option>
                  <option value="SUSPENDED">중지</option>
                  <option value="TERMINATED">해지</option>
                </select>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>회사명</TableHead>
                  <TableHead>사업자번호</TableHead>
                  <TableHead>대표자</TableHead>
                  <TableHead>요금제</TableHead>
                  <TableHead>사용자 수</TableHead>
                  <TableHead>가입일</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tenantsData?.content.map((tenant) => (
                  <TableRow key={tenant.id}>
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        <Building2 className="h-4 w-4 text-muted-foreground" />
                        {tenant.companyName}
                      </div>
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {tenant.businessNo}
                    </TableCell>
                    <TableCell>{tenant.representativeName}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{tenant.planId}</Badge>
                    </TableCell>
                    <TableCell>{tenant.userCount}명</TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {new Date(tenant.createdAt).toLocaleDateString('ko-KR')}
                    </TableCell>
                    <TableCell>
                      <Badge 
                        variant={getStatusVariant(tenant.status)}
                        className={tenant.status === "ACTIVE" ? "bg-green-500 hover:bg-green-600" : ""}
                      >
                        {getStatusText(tenant.status)}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button 
                            variant="ghost" 
                            size="icon"
                            onClick={() => {
                              setSelectedTenant(tenant);
                              setNewStatus(tenant.status);
                            }}
                          >
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>테넌트 관리</DialogTitle>
                            <DialogDescription>
                              {tenant.companyName}의 상태를 변경할 수 있습니다.
                            </DialogDescription>
                          </DialogHeader>
                          <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4 text-sm">
                              <div>
                                <span className="font-medium">회사명:</span>
                                <p className="text-muted-foreground">{tenant.companyName}</p>
                              </div>
                              <div>
                                <span className="font-medium">사업자번호:</span>
                                <p className="text-muted-foreground">{tenant.businessNo}</p>
                              </div>
                              <div>
                                <span className="font-medium">대표자:</span>
                                <p className="text-muted-foreground">{tenant.representativeName}</p>
                              </div>
                              <div>
                                <span className="font-medium">사용자 수:</span>
                                <p className="text-muted-foreground">{tenant.userCount}명</p>
                              </div>
                            </div>
                            <div>
                              <label className="text-sm font-medium">상태 변경</label>
                              <select 
                                className="w-full mt-1 px-3 py-2 border rounded-md"
                                value={newStatus}
                                onChange={(e) => setNewStatus(e.target.value)}
                              >
                                <option value="ACTIVE">정상</option>
                                <option value="SUSPENDED">중지</option>
                                <option value="TERMINATED">해지</option>
                              </select>
                            </div>
                          </div>
                          <DialogFooter>
                            <Button 
                              onClick={handleStatusChange}
                              disabled={updateTenantMutation.isPending || newStatus === tenant.status}
                            >
                              {updateTenantMutation.isPending ? (
                                <>
                                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                  변경 중...
                                </>
                              ) : (
                                '상태 변경'
                              )}
                            </Button>
                          </DialogFooter>
                        </DialogContent>
                      </Dialog>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {/* 페이지네이션 */}
            {tenantsData && tenantsData.totalPages > 1 && (
              <div className="flex items-center justify-between mt-4">
                <div className="text-sm text-muted-foreground">
                  {tenantsData.totalElements}개 중 {tenantsData.numberOfElements}개 표시
                </div>
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page - 1)}
                    disabled={tenantsData.first}
                  >
                    이전
                  </Button>
                  <span className="text-sm">
                    {page + 1} / {tenantsData.totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page + 1)}
                    disabled={tenantsData.last}
                  >
                    다음
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}



