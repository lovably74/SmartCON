import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { X, Plus } from "lucide-react";

export interface AutoApprovalRuleFormData {
  id?: number;
  ruleName: string;
  isActive: boolean;
  planIds: string[];
  verifiedTenantsOnly: boolean;
  paymentMethods: string[];
  maxAmount?: number;
  priority: number;
}

interface AutoApprovalRuleFormProps {
  initialData?: AutoApprovalRuleFormData;
  onSubmit: (data: AutoApprovalRuleFormData) => void;
  onCancel: () => void;
  isLoading?: boolean;
}

const AVAILABLE_PLANS = [
  { id: "basic", name: "베이직 플랜" },
  { id: "standard", name: "스탠다드 플랜" },
  { id: "premium", name: "프리미엄 플랜" },
  { id: "enterprise", name: "엔터프라이즈 플랜" }
];

const AVAILABLE_PAYMENT_METHODS = [
  { id: "credit_card", name: "신용카드" },
  { id: "bank_transfer", name: "계좌이체" },
  { id: "virtual_account", name: "가상계좌" }
];

export default function AutoApprovalRuleForm({
  initialData,
  onSubmit,
  onCancel,
  isLoading = false
}: AutoApprovalRuleFormProps) {
  const [formData, setFormData] = useState<AutoApprovalRuleFormData>({
    ruleName: initialData?.ruleName || "",
    isActive: initialData?.isActive ?? true,
    planIds: initialData?.planIds || [],
    verifiedTenantsOnly: initialData?.verifiedTenantsOnly ?? false,
    paymentMethods: initialData?.paymentMethods || [],
    maxAmount: initialData?.maxAmount,
    priority: initialData?.priority || 0,
    ...initialData
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const togglePlan = (planId: string) => {
    setFormData(prev => ({
      ...prev,
      planIds: prev.planIds.includes(planId)
        ? prev.planIds.filter(id => id !== planId)
        : [...prev.planIds, planId]
    }));
  };

  const togglePaymentMethod = (methodId: string) => {
    setFormData(prev => ({
      ...prev,
      paymentMethods: prev.paymentMethods.includes(methodId)
        ? prev.paymentMethods.filter(id => id !== methodId)
        : [...prev.paymentMethods, methodId]
    }));
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          {initialData?.id ? "자동 승인 규칙 수정" : "새 자동 승인 규칙 생성"}
        </CardTitle>
        <CardDescription>
          구독 신청을 자동으로 승인할 조건을 설정합니다.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 기본 정보 */}
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="ruleName">규칙 이름 *</Label>
              <Input
                id="ruleName"
                value={formData.ruleName}
                onChange={(e) => setFormData(prev => ({ ...prev, ruleName: e.target.value }))}
                placeholder="예: 베이직 플랜 자동 승인"
                required
              />
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="isActive"
                checked={formData.isActive}
                onCheckedChange={(checked) => setFormData(prev => ({ ...prev, isActive: checked }))}
              />
              <Label htmlFor="isActive">규칙 활성화</Label>
            </div>

            <div className="space-y-2">
              <Label htmlFor="priority">우선순위</Label>
              <Input
                id="priority"
                type="number"
                value={formData.priority}
                onChange={(e) => setFormData(prev => ({ ...prev, priority: parseInt(e.target.value) || 0 }))}
                placeholder="0 (높은 숫자가 높은 우선순위)"
                min="0"
              />
              <p className="text-sm text-muted-foreground">
                높은 숫자가 높은 우선순위입니다. 여러 규칙이 적용될 때 우선순위가 높은 규칙이 먼저 평가됩니다.
              </p>
            </div>
          </div>

          {/* 승인 조건 */}
          <div className="space-y-4">
            <h3 className="text-lg font-medium">승인 조건</h3>
            
            {/* 요금제 선택 */}
            <div className="space-y-2">
              <Label>적용 요금제</Label>
              <div className="flex flex-wrap gap-2">
                {AVAILABLE_PLANS.map(plan => (
                  <Badge
                    key={plan.id}
                    variant={formData.planIds.includes(plan.id) ? "default" : "outline"}
                    className="cursor-pointer"
                    onClick={() => togglePlan(plan.id)}
                  >
                    {plan.name}
                    {formData.planIds.includes(plan.id) && (
                      <X className="ml-1 h-3 w-3" />
                    )}
                  </Badge>
                ))}
              </div>
              <p className="text-sm text-muted-foreground">
                선택하지 않으면 모든 요금제에 적용됩니다.
              </p>
            </div>

            {/* 결제 방법 선택 */}
            <div className="space-y-2">
              <Label>허용 결제 방법</Label>
              <div className="flex flex-wrap gap-2">
                {AVAILABLE_PAYMENT_METHODS.map(method => (
                  <Badge
                    key={method.id}
                    variant={formData.paymentMethods.includes(method.id) ? "default" : "outline"}
                    className="cursor-pointer"
                    onClick={() => togglePaymentMethod(method.id)}
                  >
                    {method.name}
                    {formData.paymentMethods.includes(method.id) && (
                      <X className="ml-1 h-3 w-3" />
                    )}
                  </Badge>
                ))}
              </div>
              <p className="text-sm text-muted-foreground">
                선택하지 않으면 모든 결제 방법에 적용됩니다.
              </p>
            </div>

            {/* 검증된 테넌트만 */}
            <div className="flex items-center space-x-2">
              <Switch
                id="verifiedTenantsOnly"
                checked={formData.verifiedTenantsOnly}
                onCheckedChange={(checked) => setFormData(prev => ({ ...prev, verifiedTenantsOnly: checked }))}
              />
              <Label htmlFor="verifiedTenantsOnly">검증된 테넌트만 자동 승인</Label>
            </div>

            {/* 최대 금액 */}
            <div className="space-y-2">
              <Label htmlFor="maxAmount">최대 승인 금액 (원)</Label>
              <Input
                id="maxAmount"
                type="number"
                value={formData.maxAmount || ""}
                onChange={(e) => setFormData(prev => ({ 
                  ...prev, 
                  maxAmount: e.target.value ? parseInt(e.target.value) : undefined 
                }))}
                placeholder="예: 100000 (비워두면 금액 제한 없음)"
                min="0"
              />
              <p className="text-sm text-muted-foreground">
                이 금액을 초과하는 구독은 수동 승인으로 처리됩니다.
              </p>
            </div>
          </div>

          {/* 버튼 */}
          <div className="flex justify-end space-x-2">
            <Button type="button" variant="outline" onClick={onCancel}>
              취소
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? "저장 중..." : initialData?.id ? "수정하기" : "생성하기"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}