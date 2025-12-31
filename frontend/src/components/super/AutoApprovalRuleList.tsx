import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from "@/components/ui/table";
import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import { MoreHorizontal, Edit, Trash2, TrendingUp } from "lucide-react";

export interface AutoApprovalRule {
  id: number;
  ruleName: string;
  isActive: boolean;
  planIds: string[];
  verifiedTenantsOnly: boolean;
  paymentMethods: string[];
  maxAmount?: number;
  priority: number;
  createdAt: string;
  updatedAt: string;
  appliedCount?: number; // 적용된 횟수 (통계)
}

interface AutoApprovalRuleListProps {
  rules: AutoApprovalRule[];
  onEdit: (rule: AutoApprovalRule) => void;
  onDelete: (ruleId: number) => void;
  onToggleStatus: (ruleId: number, isActive: boolean) => void;
  onViewStats: (ruleId: number) => void;
  isLoading?: boolean;
}

const PLAN_NAMES: Record<string, string> = {
  basic: "베이직",
  standard: "스탠다드", 
  premium: "프리미엄",
  enterprise: "엔터프라이즈"
};

const PAYMENT_METHOD_NAMES: Record<string, string> = {
  credit_card: "신용카드",
  bank_transfer: "계좌이체",
  virtual_account: "가상계좌"
};

export default function AutoApprovalRuleList({
  rules,
  onEdit,
  onDelete,
  onToggleStatus,
  onViewStats,
  isLoading = false
}: AutoApprovalRuleListProps) {
  const [deletingRuleId, setDeletingRuleId] = useState<number | null>(null);

  const handleDelete = async (ruleId: number) => {
    if (window.confirm("이 자동 승인 규칙을 삭제하시겠습니까?")) {
      setDeletingRuleId(ruleId);
      try {
        await onDelete(ruleId);
      } finally {
        setDeletingRuleId(null);
      }
    }
  };

  const formatConditions = (rule: AutoApprovalRule) => {
    const conditions: string[] = [];
    
    if (rule.planIds.length > 0) {
      const planNames = rule.planIds.map(id => PLAN_NAMES[id] || id).join(", ");
      conditions.push(`요금제: ${planNames}`);
    }
    
    if (rule.paymentMethods.length > 0) {
      const methodNames = rule.paymentMethods.map(id => PAYMENT_METHOD_NAMES[id] || id).join(", ");
      conditions.push(`결제: ${methodNames}`);
    }
    
    if (rule.verifiedTenantsOnly) {
      conditions.push("검증된 테넌트만");
    }
    
    if (rule.maxAmount) {
      conditions.push(`최대 ${rule.maxAmount.toLocaleString()}원`);
    }
    
    return conditions.length > 0 ? conditions.join(" | ") : "모든 조건";
  };

  if (isLoading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center text-muted-foreground">
            자동 승인 규칙을 불러오는 중...
          </div>
        </CardContent>
      </Card>
    );
  }

  if (rules.length === 0) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center text-muted-foreground">
            <p>설정된 자동 승인 규칙이 없습니다.</p>
            <p className="text-sm mt-1">새 규칙을 생성하여 구독 승인 프로세스를 자동화하세요.</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>자동 승인 규칙 목록</CardTitle>
        <CardDescription>
          우선순위 순으로 정렬되어 표시됩니다. 높은 우선순위 규칙이 먼저 평가됩니다.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>규칙명</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>우선순위</TableHead>
              <TableHead>적용 조건</TableHead>
              <TableHead>적용 횟수</TableHead>
              <TableHead className="w-[100px]">작업</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {rules
              .sort((a, b) => b.priority - a.priority) // 우선순위 내림차순 정렬
              .map((rule) => (
                <TableRow key={rule.id}>
                  <TableCell className="font-medium">
                    {rule.ruleName}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center space-x-2">
                      <Switch
                        checked={rule.isActive}
                        onCheckedChange={(checked) => onToggleStatus(rule.id, checked)}
                        size="sm"
                      />
                      <Badge variant={rule.isActive ? "default" : "secondary"}>
                        {rule.isActive ? "활성" : "비활성"}
                      </Badge>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {rule.priority}
                    </Badge>
                  </TableCell>
                  <TableCell className="max-w-xs">
                    <div className="text-sm text-muted-foreground truncate">
                      {formatConditions(rule)}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center space-x-1">
                      <span className="text-sm font-medium">
                        {rule.appliedCount || 0}회
                      </span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onViewStats(rule.id)}
                        className="h-6 w-6 p-0"
                      >
                        <TrendingUp className="h-3 w-3" />
                      </Button>
                    </div>
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="h-8 w-8 p-0">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => onEdit(rule)}>
                          <Edit className="mr-2 h-4 w-4" />
                          수정
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          onClick={() => handleDelete(rule.id)}
                          disabled={deletingRuleId === rule.id}
                          className="text-red-600"
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          {deletingRuleId === rule.id ? "삭제 중..." : "삭제"}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}