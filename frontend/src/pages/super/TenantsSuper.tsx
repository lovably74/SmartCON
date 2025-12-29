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
import { 
  Building2, 
  MoreHorizontal, 
  Search, 
  Loader2, 
  AlertTriangle,
  Pause,
  Square,
  History,
  Eye
} from "lucide-react";
import { 
  useTenants, 
  useUpdateTenantStatus,
  useSuspendSubscription,
  useTerminateSubscription,
  useApprovalHistory
} from "@/hooks/useAdminApi";
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
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import SubscriptionDetailModal from "@/components/super/SubscriptionDetailModal";

export default function TenantsSuper() {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [subscriptionStatusFilter, setSubscriptionStatusFilter] = useState<string>("");
  const [page, setPage] = useState(0);
  const [selectedTenant, setSelectedTenant] = useState<any>(null);
  const [newStatus, setNewStatus] = useState<string>("");
  
  // 구독 관리 관련 상태
  const [showSuspendDialog, setShowSuspendDialog] = useState(false);
  const [showTerminateDialog, setShowTerminateDialog] = useState(false);
  const [showHistoryDialog, setShowHistoryDialog] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [suspendReason, setSuspendReason] = useState("");
  const [terminateReason, setTerminateReason] = useState("");

  const { data: tenantsData, isLoading, error } = useTenants({
    search: search || undefined,
    status: statusFilter || undefined,
    subscriptionStatus: subscriptionStatusFilter || undefined,
    page,
    size: 20,
    sortBy: "createdAt",
    sortDir: "desc"
  });

  const { data: historyData } = useApprovalHistory(selectedTenant?.subscriptionId);

  const updateTenantMutation = useUpdateTenantStatus();
  const suspendMutation = useSuspendSubscription();
  const terminateMutation = useTerminateSubscription();

  const getStatusText = (status: string) => {
    switch (status) {
      case 'ACTIVE': return '정상';
      case 'SUSPENDED': return '중지';
      case 'TERMINATED': return '해지';
      default: return status;
    }
  };

  const getSubscriptionStatusText = (status: string) => {
    switch (status) {
      case 'PENDING_APPROVAL': return '승인 대기';
      case 'APPROVED': return '승인됨';
      case 'REJECTED': return '거부됨';
      case 'AUTO_APPROVED': return '자동 승인';
      case 'ACTIVE': return '활성';
      case 'SUSPENDED': return '중지';
      case 'TERMINATED': return '종료';
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

  const getSubscriptionStatusVariant = (status: string) => {
    switch (status) {
      case 'PENDING_APPROVAL': return 'secondary';
      case 'APPROVED': 
      case 'AUTO_APPROVED':
      case 'ACTIVE': return 'default';
      case 'REJECTED': 
      case 'TERMINATED': return 'destructive';
      case 'SUSPENDED': return 'secondary';
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

  const handleSuspendSubscription = async () => {
    if (selectedTenant?.subscriptionId && suspendReason.trim()) {
      await suspendMutation.mutateAsync({
        subscriptionId: selectedTenant.subscriptionId,
        reason: suspendReason
      });
      setShowSuspendDialog(false);
      setSuspendReason("");
      setSelectedTenant(null);
    }
  };

  const handleTerminateSubscription = async () => {
    if (selectedTenant?.subscriptionId && terminateReason.trim()) {
      await terminateMutation.mutateAsync({
        subscriptionId: selectedTenant.subscriptionId,
        reason: terminateReason
      });
      setShowTerminateDialog(false);
      setTerminateReason("");
      setSelectedTenant(null);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
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
                <select 
                  className="px-3 py-2 border rounded-md text-sm"
                  value={subscriptionStatusFilter}
                  onChange={(e) => {
                    setSubscriptionStatusFilter(e.target.value);
                    setPage(0);
                  }}
                >
                  <option value="">전체 구독상태</option>
                  <option value="PENDING_APPROVAL">승인 대기</option>
                  <option value="APPROVED">승인됨</option>
                  <option value="REJECTED">거부됨</option>
                  <option value="AUTO_APPROVED">자동 승인</option>
                  <option value="ACTIVE">활성</option>
                  <option value="SUSPENDED">중지</option>
                  <option value="TERMINATED">종료</option>
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
                  <TableHead>월 요금</TableHead>
                  <TableHead>사용자 수</TableHead>
                  <TableHead>가입일</TableHead>
                  <TableHead>테넌트 상태</TableHead>
                  <TableHead>구독 상태</TableHead>
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
                    <TableCell className="text-sm">
                      {tenant.monthlyAmount ? formatCurrency(tenant.monthlyAmount) : '-'}
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
                    <TableCell>
                      {tenant.subscriptionStatus ? (
                        <Badge variant={getSubscriptionStatusVariant(tenant.subscriptionStatus)}>
                          {getSubscriptionStatusText(tenant.subscriptionStatus)}
                        </Badge>
                      ) : (
                        <span className="text-muted-foreground text-sm">-</span>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        {/* 구독 상세 보기 버튼 */}
                        {tenant.subscriptionId && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTenant(tenant);
                              setShowDetailModal(true);
                            }}
                            title="구독 상세 보기"
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                        )}
                        
                        {/* 승인 이력 보기 버튼 */}
                        {tenant.subscriptionId && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTenant(tenant);
                              setShowHistoryDialog(true);
                            }}
                            title="승인 이력 보기"
                          >
                            <History className="h-4 w-4" />
                          </Button>
                        )}
                        
                        {/* 구독 중지 버튼 */}
                        {tenant.subscriptionId && 
                         tenant.subscriptionStatus === 'ACTIVE' && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTenant(tenant);
                              setShowSuspendDialog(true);
                            }}
                            title="구독 중지"
                          >
                            <Pause className="h-4 w-4" />
                          </Button>
                        )}
                        
                        {/* 구독 종료 버튼 */}
                        {tenant.subscriptionId && 
                         ['ACTIVE', 'SUSPENDED'].includes(tenant.subscriptionStatus || '') && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTenant(tenant);
                              setShowTerminateDialog(true);
                            }}
                            title="구독 종료"
                          >
                            <Square className="h-4 w-4" />
                          </Button>
                        )}
                        
                        {/* 테넌트 관리 버튼 */}
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button 
                              variant="ghost" 
                              size="sm"
                              onClick={() => {
                                setSelectedTenant(tenant);
                                setNewStatus(tenant.status);
                              }}
                              title="테넌트 관리"
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
                                {tenant.subscriptionStatus && (
                                  <>
                                    <div>
                                      <span className="font-medium">구독 상태:</span>
                                      <p className="text-muted-foreground">
                                        {getSubscriptionStatusText(tenant.subscriptionStatus)}
                                      </p>
                                    </div>
                                    <div>
                                      <span className="font-medium">월 요금:</span>
                                      <p className="text-muted-foreground">
                                        {tenant.monthlyAmount ? formatCurrency(tenant.monthlyAmount) : '-'}
                                      </p>
                                    </div>
                                  </>
                                )}
                              </div>
                              <div>
                                <label className="text-sm font-medium">테넌트 상태 변경</label>
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
                      </div>
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

        {/* 구독 중지 다이얼로그 */}
        <Dialog open={showSuspendDialog} onOpenChange={setShowSuspendDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>구독 중지</DialogTitle>
              <DialogDescription>
                {selectedTenant?.companyName}의 구독을 중지하시겠습니까?
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="suspend-reason">중지 사유</Label>
                <Textarea
                  id="suspend-reason"
                  placeholder="구독 중지 사유를 입력해주세요..."
                  value={suspendReason}
                  onChange={(e) => setSuspendReason(e.target.value)}
                  className="mt-1"
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => {
                  setShowSuspendDialog(false);
                  setSuspendReason("");
                }}
              >
                취소
              </Button>
              <Button
                onClick={handleSuspendSubscription}
                disabled={suspendMutation.isPending || !suspendReason.trim()}
              >
                {suspendMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    중지 중...
                  </>
                ) : (
                  '구독 중지'
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* 구독 종료 다이얼로그 */}
        <Dialog open={showTerminateDialog} onOpenChange={setShowTerminateDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>구독 종료</DialogTitle>
              <DialogDescription>
                {selectedTenant?.companyName}의 구독을 완전히 종료하시겠습니까? 이 작업은 되돌릴 수 없습니다.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="terminate-reason">종료 사유</Label>
                <Textarea
                  id="terminate-reason"
                  placeholder="구독 종료 사유를 입력해주세요..."
                  value={terminateReason}
                  onChange={(e) => setTerminateReason(e.target.value)}
                  className="mt-1"
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => {
                  setShowTerminateDialog(false);
                  setTerminateReason("");
                }}
              >
                취소
              </Button>
              <Button
                variant="destructive"
                onClick={handleTerminateSubscription}
                disabled={terminateMutation.isPending || !terminateReason.trim()}
              >
                {terminateMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    종료 중...
                  </>
                ) : (
                  '구독 종료'
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* 승인 이력 다이얼로그 */}
        <Dialog open={showHistoryDialog} onOpenChange={setShowHistoryDialog}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>승인 이력</DialogTitle>
              <DialogDescription>
                {selectedTenant?.companyName}의 구독 승인 이력을 확인할 수 있습니다.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              {selectedTenant?.subscriptionId && (
                <div className="grid grid-cols-2 gap-4 text-sm bg-muted p-4 rounded-lg">
                  <div>
                    <span className="font-medium">구독 ID:</span>
                    <p className="text-muted-foreground">{selectedTenant.subscriptionId}</p>
                  </div>
                  <div>
                    <span className="font-medium">현재 상태:</span>
                    <p className="text-muted-foreground">
                      {selectedTenant.subscriptionStatus ? 
                        getSubscriptionStatusText(selectedTenant.subscriptionStatus) : '-'}
                    </p>
                  </div>
                  <div>
                    <span className="font-medium">월 요금:</span>
                    <p className="text-muted-foreground">
                      {selectedTenant.monthlyAmount ? formatCurrency(selectedTenant.monthlyAmount) : '-'}
                    </p>
                  </div>
                  <div>
                    <span className="font-medium">승인일:</span>
                    <p className="text-muted-foreground">
                      {selectedTenant.approvedAt ? formatDate(selectedTenant.approvedAt) : '-'}
                    </p>
                  </div>
                </div>
              )}
              
              <div className="max-h-96 overflow-y-auto">
                {historyData && historyData.length > 0 ? (
                  <div className="space-y-3">
                    {historyData.map((history) => (
                      <div key={history.id} className="border rounded-lg p-3">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <Badge variant="outline">
                              {history.action === 'APPROVE' ? '승인' :
                               history.action === 'REJECT' ? '거부' :
                               history.action === 'SUSPEND' ? '중지' :
                               history.action === 'TERMINATE' ? '종료' :
                               history.action === 'REACTIVATE' ? '재활성화' : history.action}
                            </Badge>
                            <span className="text-sm font-medium">{history.adminName}</span>
                          </div>
                          <span className="text-xs text-muted-foreground">
                            {formatDate(history.processedAt)}
                          </span>
                        </div>
                        <div className="text-sm text-muted-foreground">
                          {history.fromStatus} → {history.toStatus}
                        </div>
                        {history.reason && (
                          <div className="text-sm mt-2 p-2 bg-muted rounded">
                            <span className="font-medium">사유: </span>
                            {history.reason}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">
                    승인 이력이 없습니다.
                  </div>
                )}
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setShowHistoryDialog(false)}
              >
                닫기
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* 구독 상세 정보 모달 */}
        <SubscriptionDetailModal
          tenant={selectedTenant}
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedTenant(null);
          }}
        />
      </div>
    </DashboardLayout>
  );
}

