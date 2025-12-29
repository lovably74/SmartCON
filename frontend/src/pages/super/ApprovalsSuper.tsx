import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
  CheckCircle, 
  XCircle, 
  Clock, 
  Loader2, 
  AlertTriangle,
  Eye,
  Building2,
  CreditCard,
  Calendar,
  User
} from "lucide-react";
import { 
  usePendingApprovals, 
  useApproveSubscription, 
  useRejectSubscription,
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
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";

export default function ApprovalsSuper() {
  const [page, setPage] = useState(0);
  const [selectedApproval, setSelectedApproval] = useState<any>(null);
  const [approvalReason, setApprovalReason] = useState("");
  const [rejectionReason, setRejectionReason] = useState("");
  const [showApprovalDialog, setShowApprovalDialog] = useState(false);
  const [showRejectionDialog, setShowRejectionDialog] = useState(false);
  const [showHistoryDialog, setShowHistoryDialog] = useState(false);

  const { data: approvalsData, isLoading, error } = usePendingApprovals({
    page,
    size: 20,
    sortBy: "requestedAt",
    sortDir: "desc"
  });

  const { data: historyData } = useApprovalHistory(selectedApproval?.subscriptionId);

  const approveMutation = useApproveSubscription();
  const rejectMutation = useRejectSubscription();

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING_APPROVAL': return '승인 대기';
      case 'APPROVED': return '승인됨';
      case 'REJECTED': return '거부됨';
      case 'AUTO_APPROVED': return '자동 승인';
      default: return status;
    }
  };

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'PENDING_APPROVAL': return 'secondary';
      case 'APPROVED': return 'default';
      case 'REJECTED': return 'destructive';
      case 'AUTO_APPROVED': return 'outline';
      default: return 'outline';
    }
  };

  const handleApprove = async () => {
    if (selectedApproval && approvalReason.trim()) {
      await approveMutation.mutateAsync({
        subscriptionId: selectedApproval.subscriptionId,
        reason: approvalReason
      });
      setShowApprovalDialog(false);
      setApprovalReason("");
      setSelectedApproval(null);
    }
  };

  const handleReject = async () => {
    if (selectedApproval && rejectionReason.trim()) {
      await rejectMutation.mutateAsync({
        subscriptionId: selectedApproval.subscriptionId,
        reason: rejectionReason
      });
      setShowRejectionDialog(false);
      setRejectionReason("");
      setSelectedApproval(null);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
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
            <h2 className="text-2xl font-bold tracking-tight">구독 승인 관리</h2>
            <p className="text-muted-foreground">
              승인 대기 중인 구독 신청을 검토하고 승인/거부할 수 있습니다.
            </p>
          </div>
          <div className="flex items-center space-x-2">
            <Badge variant="secondary" className="text-sm">
              <Clock className="h-3 w-3 mr-1" />
              대기 중: {approvalsData?.totalElements || 0}건
            </Badge>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>승인 대기 목록</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>회사 정보</TableHead>
                  <TableHead>요금제</TableHead>
                  <TableHead>결제 정보</TableHead>
                  <TableHead>신청일시</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {approvalsData?.content.map((approval) => (
                  <TableRow key={approval.id}>
                    <TableCell>
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <Building2 className="h-4 w-4 text-muted-foreground" />
                          <span className="font-medium">{approval.companyName}</span>
                          {approval.isVerifiedTenant && (
                            <Badge variant="outline" className="text-xs bg-green-50 text-green-700 border-green-200">
                              인증됨
                            </Badge>
                          )}
                        </div>
                        <div className="text-sm text-muted-foreground">
                          사업자번호: {approval.businessNo}
                        </div>
                        <div className="text-sm text-muted-foreground flex items-center gap-1">
                          <User className="h-3 w-3" />
                          {approval.representativeName}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="space-y-1">
                        <Badge variant="outline">{approval.planName}</Badge>
                        <div className="text-sm font-medium">
                          {formatCurrency(approval.monthlyAmount)}/월
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1 text-sm">
                        <CreditCard className="h-3 w-3 text-muted-foreground" />
                        {approval.paymentMethod}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1 text-sm text-muted-foreground">
                        <Calendar className="h-3 w-3" />
                        {formatDate(approval.requestedAt)}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge 
                        variant={getStatusVariant(approval.status)}
                        className={approval.status === "PENDING_APPROVAL" ? "bg-yellow-100 text-yellow-800 hover:bg-yellow-200" : ""}
                      >
                        {getStatusText(approval.status)}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        {/* 승인 이력 보기 */}
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedApproval(approval);
                            setShowHistoryDialog(true);
                          }}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>

                        {approval.status === 'PENDING_APPROVAL' && (
                          <>
                            {/* 승인 버튼 */}
                            <Button
                              variant="outline"
                              size="sm"
                              className="text-green-600 border-green-200 hover:bg-green-50"
                              onClick={() => {
                                setSelectedApproval(approval);
                                setShowApprovalDialog(true);
                              }}
                            >
                              <CheckCircle className="h-4 w-4 mr-1" />
                              승인
                            </Button>

                            {/* 거부 버튼 */}
                            <Button
                              variant="outline"
                              size="sm"
                              className="text-red-600 border-red-200 hover:bg-red-50"
                              onClick={() => {
                                setSelectedApproval(approval);
                                setShowRejectionDialog(true);
                              }}
                            >
                              <XCircle className="h-4 w-4 mr-1" />
                              거부
                            </Button>
                          </>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {/* 페이지네이션 */}
            {approvalsData && approvalsData.totalPages > 1 && (
              <div className="flex items-center justify-between mt-4">
                <div className="text-sm text-muted-foreground">
                  {approvalsData.totalElements}개 중 {approvalsData.numberOfElements}개 표시
                </div>
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page - 1)}
                    disabled={approvalsData.first}
                  >
                    이전
                  </Button>
                  <span className="text-sm">
                    {page + 1} / {approvalsData.totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page + 1)}
                    disabled={approvalsData.last}
                  >
                    다음
                  </Button>
                </div>
              </div>
            )}

            {/* 데이터가 없을 때 */}
            {approvalsData?.content.length === 0 && (
              <div className="text-center py-8">
                <Clock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-muted-foreground mb-2">
                  승인 대기 중인 구독이 없습니다
                </h3>
                <p className="text-sm text-muted-foreground">
                  새로운 구독 신청이 있으면 여기에 표시됩니다.
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 승인 다이얼로그 */}
        <Dialog open={showApprovalDialog} onOpenChange={setShowApprovalDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>구독 승인</DialogTitle>
              <DialogDescription>
                {selectedApproval?.companyName}의 구독 신청을 승인하시겠습니까?
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="font-medium">회사명:</span>
                  <p className="text-muted-foreground">{selectedApproval?.companyName}</p>
                </div>
                <div>
                  <span className="font-medium">요금제:</span>
                  <p className="text-muted-foreground">{selectedApproval?.planName}</p>
                </div>
                <div>
                  <span className="font-medium">월 요금:</span>
                  <p className="text-muted-foreground">
                    {selectedApproval && formatCurrency(selectedApproval.monthlyAmount)}
                  </p>
                </div>
                <div>
                  <span className="font-medium">결제 방법:</span>
                  <p className="text-muted-foreground">{selectedApproval?.paymentMethod}</p>
                </div>
              </div>
              <div>
                <Label htmlFor="approval-reason">승인 사유</Label>
                <Textarea
                  id="approval-reason"
                  placeholder="승인 사유를 입력해주세요..."
                  value={approvalReason}
                  onChange={(e) => setApprovalReason(e.target.value)}
                  className="mt-1"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowApprovalDialog(false)}>
                취소
              </Button>
              <Button 
                onClick={handleApprove}
                disabled={approveMutation.isPending || !approvalReason.trim()}
                className="bg-green-600 hover:bg-green-700"
              >
                {approveMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    승인 중...
                  </>
                ) : (
                  <>
                    <CheckCircle className="mr-2 h-4 w-4" />
                    승인
                  </>
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* 거부 다이얼로그 */}
        <Dialog open={showRejectionDialog} onOpenChange={setShowRejectionDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>구독 거부</DialogTitle>
              <DialogDescription>
                {selectedApproval?.companyName}의 구독 신청을 거부하시겠습니까?
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="font-medium">회사명:</span>
                  <p className="text-muted-foreground">{selectedApproval?.companyName}</p>
                </div>
                <div>
                  <span className="font-medium">요금제:</span>
                  <p className="text-muted-foreground">{selectedApproval?.planName}</p>
                </div>
              </div>
              <div>
                <Label htmlFor="rejection-reason">거부 사유 *</Label>
                <Textarea
                  id="rejection-reason"
                  placeholder="거부 사유를 입력해주세요..."
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                  className="mt-1"
                  required
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowRejectionDialog(false)}>
                취소
              </Button>
              <Button 
                onClick={handleReject}
                disabled={rejectMutation.isPending || !rejectionReason.trim()}
                variant="destructive"
              >
                {rejectMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    거부 중...
                  </>
                ) : (
                  <>
                    <XCircle className="mr-2 h-4 w-4" />
                    거부
                  </>
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
                {selectedApproval?.companyName}의 구독 승인 이력입니다.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              {historyData && historyData.length > 0 ? (
                <div className="space-y-3">
                  {historyData.map((history) => (
                    <div key={history.id} className="border rounded-lg p-3">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <Badge variant="outline">
                            {history.fromStatus} → {history.toStatus}
                          </Badge>
                          <span className="text-sm font-medium">{history.action}</span>
                        </div>
                        <span className="text-xs text-muted-foreground">
                          {formatDate(history.processedAt)}
                        </span>
                      </div>
                      <div className="text-sm text-muted-foreground">
                        처리자: {history.adminName}
                      </div>
                      {history.reason && (
                        <div className="text-sm mt-1">
                          사유: {history.reason}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-4 text-muted-foreground">
                  승인 이력이 없습니다.
                </div>
              )}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowHistoryDialog(false)}>
                닫기
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  );
}