/**
 * 구독 상세 정보 모달 컴포넌트
 * 구독의 상세 정보, 승인 이력, 상태 변경 액션을 제공합니다.
 */

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { 
  Loader2, 
  Calendar, 
  CreditCard, 
  Building2, 
  User, 
  Phone, 
  Mail,
  CheckCircle,
  XCircle,
  Pause,
  Square,
  RotateCcw
} from "lucide-react";
import { 
  useApprovalHistory,
  useSuspendSubscription,
  useTerminateSubscription,
  useApproveSubscription,
  useRejectSubscription
} from "@/hooks/useAdminApi";
import { TenantSummary, ApprovalHistory } from "@/lib/api";

interface SubscriptionDetailModalProps {
  tenant: TenantSummary | null;
  isOpen: boolean;
  onClose: () => void;
}

export default function SubscriptionDetailModal({ 
  tenant, 
  isOpen, 
  onClose 
}: SubscriptionDetailModalProps) {
  const [actionType, setActionType] = useState<string>("");
  const [actionReason, setActionReason] = useState("");
  const [showActionDialog, setShowActionDialog] = useState(false);

  const { data: historyData, isLoading: historyLoading } = useApprovalHistory(
    tenant?.subscriptionId || 0
  );

  const suspendMutation = useSuspendSubscription();
  const terminateMutation = useTerminateSubscription();
  const approveMutation = useApproveSubscription();
  const rejectMutation = useRejectSubscription();

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

  const getActionText = (action: string) => {
    switch (action) {
      case 'approve': return '승인';
      case 'reject': return '거부';
      case 'suspend': return '중지';
      case 'terminate': return '종료';
      case 'reactivate': return '재활성화';
      default: return action;
    }
  };

  const handleAction = async () => {
    if (!tenant?.subscriptionId || !actionReason.trim()) return;

    try {
      switch (actionType) {
        case 'approve':
          await approveMutation.mutateAsync({
            subscriptionId: tenant.subscriptionId,
            reason: actionReason
          });
          break;
        case 'reject':
          await rejectMutation.mutateAsync({
            subscriptionId: tenant.subscriptionId,
            reason: actionReason
          });
          break;
        case 'suspend':
          await suspendMutation.mutateAsync({
            subscriptionId: tenant.subscriptionId,
            reason: actionReason
          });
          break;
        case 'terminate':
          await terminateMutation.mutateAsync({
            subscriptionId: tenant.subscriptionId,
            reason: actionReason
          });
          break;
      }
      
      setShowActionDialog(false);
      setActionType("");
      setActionReason("");
      onClose();
    } catch (error) {
      console.error('Action failed:', error);
    }
  };

  const openActionDialog = (type: string) => {
    setActionType(type);
    setShowActionDialog(true);
  };

  const getAvailableActions = () => {
    if (!tenant?.subscriptionStatus) return [];
    
    const actions = [];
    const status = tenant.subscriptionStatus;
    
    if (status === 'PENDING_APPROVAL') {
      actions.push('approve', 'reject');
    }
    
    if (status === 'ACTIVE') {
      actions.push('suspend', 'terminate');
    }
    
    if (status === 'SUSPENDED') {
      actions.push('reactivate', 'terminate');
    }
    
    return actions;
  };

  if (!tenant) return null;

  return (
    <>
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Building2 className="h-5 w-5" />
              {tenant.companyName} - 구독 상세 정보
            </DialogTitle>
            <DialogDescription>
              구독 정보, 승인 이력 및 관리 기능을 제공합니다.
            </DialogDescription>
          </DialogHeader>

          <Tabs defaultValue="details" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="details">구독 정보</TabsTrigger>
              <TabsTrigger value="history">승인 이력</TabsTrigger>
              <TabsTrigger value="actions">관리 액션</TabsTrigger>
            </TabsList>

            <TabsContent value="details" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* 기본 정보 */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg flex items-center gap-2">
                      <Building2 className="h-4 w-4" />
                      기본 정보
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">회사명</span>
                      <p className="font-medium">{tenant.companyName}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">사업자번호</span>
                      <p>{tenant.businessNo}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">대표자</span>
                      <p>{tenant.representativeName}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">사용자 수</span>
                      <p>{tenant.userCount}명</p>
                    </div>
                  </CardContent>
                </Card>

                {/* 구독 정보 */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg flex items-center gap-2">
                      <CreditCard className="h-4 w-4" />
                      구독 정보
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">구독 ID</span>
                      <p>{tenant.subscriptionId || '-'}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">요금제</span>
                      <Badge variant="outline">{tenant.planId}</Badge>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">월 요금</span>
                      <p className="font-medium">
                        {tenant.monthlyAmount ? formatCurrency(tenant.monthlyAmount) : '-'}
                      </p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">구독 상태</span>
                      <div className="mt-1">
                        {tenant.subscriptionStatus ? (
                          <Badge variant={getSubscriptionStatusVariant(tenant.subscriptionStatus)}>
                            {getSubscriptionStatusText(tenant.subscriptionStatus)}
                          </Badge>
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* 날짜 정보 */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg flex items-center gap-2">
                      <Calendar className="h-4 w-4" />
                      날짜 정보
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div>
                      <span className="text-sm font-medium text-muted-foreground">가입일</span>
                      <p>{formatDate(tenant.createdAt)}</p>
                    </div>
                    {tenant.approvedAt && (
                      <div>
                        <span className="text-sm font-medium text-muted-foreground">승인일</span>
                        <p>{formatDate(tenant.approvedAt)}</p>
                      </div>
                    )}
                    {tenant.lastLoginAt && (
                      <div>
                        <span className="text-sm font-medium text-muted-foreground">최근 로그인</span>
                        <p>{formatDate(tenant.lastLoginAt)}</p>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* 승인자 정보 */}
                {tenant.approvedBy && (
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-lg flex items-center gap-2">
                        <User className="h-4 w-4" />
                        승인자 정보
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div>
                        <span className="text-sm font-medium text-muted-foreground">승인자</span>
                        <p>{tenant.approvedBy}</p>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </div>
            </TabsContent>

            <TabsContent value="history" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>승인 이력</CardTitle>
                </CardHeader>
                <CardContent>
                  {historyLoading ? (
                    <div className="flex items-center justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin" />
                      <span className="ml-2">이력을 불러오는 중...</span>
                    </div>
                  ) : historyData && historyData.length > 0 ? (
                    <div className="space-y-4">
                      {historyData.map((history: ApprovalHistory) => (
                        <div key={history.id} className="border rounded-lg p-4">
                          <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center gap-3">
                              <Badge variant="outline">
                                {history.action === 'APPROVE' ? '승인' :
                                 history.action === 'REJECT' ? '거부' :
                                 history.action === 'SUSPEND' ? '중지' :
                                 history.action === 'TERMINATE' ? '종료' :
                                 history.action === 'REACTIVATE' ? '재활성화' : history.action}
                              </Badge>
                              <span className="font-medium">{history.adminName}</span>
                            </div>
                            <span className="text-sm text-muted-foreground">
                              {formatDate(history.processedAt)}
                            </span>
                          </div>
                          <div className="text-sm text-muted-foreground mb-2">
                            상태 변경: <span className="font-medium">{history.fromStatus}</span> → <span className="font-medium">{history.toStatus}</span>
                          </div>
                          {history.reason && (
                            <div className="text-sm p-3 bg-muted rounded-md">
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
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="actions" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>관리 액션</CardTitle>
                  <DialogDescription>
                    현재 구독 상태에 따라 수행 가능한 액션들입니다.
                  </DialogDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    {getAvailableActions().map((action) => (
                      <Button
                        key={action}
                        variant={action === 'terminate' ? 'destructive' : 'outline'}
                        onClick={() => openActionDialog(action)}
                        className="flex items-center gap-2"
                      >
                        {action === 'approve' && <CheckCircle className="h-4 w-4" />}
                        {action === 'reject' && <XCircle className="h-4 w-4" />}
                        {action === 'suspend' && <Pause className="h-4 w-4" />}
                        {action === 'terminate' && <Square className="h-4 w-4" />}
                        {action === 'reactivate' && <RotateCcw className="h-4 w-4" />}
                        {getActionText(action)}
                      </Button>
                    ))}
                  </div>
                  
                  {getAvailableActions().length === 0 && (
                    <div className="text-center py-8 text-muted-foreground">
                      현재 상태에서 수행 가능한 액션이 없습니다.
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          <DialogFooter>
            <Button variant="outline" onClick={onClose}>
              닫기
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 액션 확인 다이얼로그 */}
      <Dialog open={showActionDialog} onOpenChange={setShowActionDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{getActionText(actionType)} 확인</DialogTitle>
            <DialogDescription>
              {tenant.companyName}의 구독을 {getActionText(actionType).toLowerCase()}하시겠습니까?
              {actionType === 'terminate' && ' 이 작업은 되돌릴 수 없습니다.'}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="action-reason">{getActionText(actionType)} 사유</Label>
              <Textarea
                id="action-reason"
                placeholder={`${getActionText(actionType)} 사유를 입력해주세요...`}
                value={actionReason}
                onChange={(e) => setActionReason(e.target.value)}
                className="mt-1"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setShowActionDialog(false);
                setActionType("");
                setActionReason("");
              }}
            >
              취소
            </Button>
            <Button
              variant={actionType === 'terminate' ? 'destructive' : 'default'}
              onClick={handleAction}
              disabled={
                !actionReason.trim() ||
                suspendMutation.isPending ||
                terminateMutation.isPending ||
                approveMutation.isPending ||
                rejectMutation.isPending
              }
            >
              {(suspendMutation.isPending || terminateMutation.isPending || 
                approveMutation.isPending || rejectMutation.isPending) ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  처리 중...
                </>
              ) : (
                getActionText(actionType)
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}