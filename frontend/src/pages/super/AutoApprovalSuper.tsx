import { useState, useEffect } from "react";
import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Plus, Settings, BarChart3, AlertCircle } from "lucide-react";
import { toast } from "sonner";

import AutoApprovalRuleForm, { AutoApprovalRuleFormData } from "@/components/super/AutoApprovalRuleForm";
import AutoApprovalRuleList, { AutoApprovalRule } from "@/components/super/AutoApprovalRuleList";
import AutoApprovalStats, { AutoApprovalStatsData } from "@/components/super/AutoApprovalStats";

export default function AutoApprovalSuper() {
  const [activeTab, setActiveTab] = useState("rules");
  const [showForm, setShowForm] = useState(false);
  const [editingRule, setEditingRule] = useState<AutoApprovalRule | null>(null);
  const [isAutoApprovalEnabled, setIsAutoApprovalEnabled] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  
  // Mock data - 실제 구현에서는 API 호출로 대체
  const [rules, setRules] = useState<AutoApprovalRule[]>([
    {
      id: 1,
      ruleName: "베이직 플랜 자동 승인",
      isActive: true,
      planIds: ["basic"],
      verifiedTenantsOnly: false,
      paymentMethods: ["credit_card"],
      maxAmount: 50000,
      priority: 10,
      createdAt: "2024-01-15T09:00:00Z",
      updatedAt: "2024-01-15T09:00:00Z",
      appliedCount: 45
    },
    {
      id: 2,
      ruleName: "검증된 테넌트 프리미엄",
      isActive: true,
      planIds: ["premium"],
      verifiedTenantsOnly: true,
      paymentMethods: ["credit_card", "bank_transfer"],
      priority: 20,
      createdAt: "2024-01-10T14:30:00Z",
      updatedAt: "2024-01-20T10:15:00Z",
      appliedCount: 12
    }
  ]);

  const [stats] = useState<AutoApprovalStatsData>({
    totalRules: 2,
    activeRules: 2,
    totalApplications: 150,
    autoApprovedCount: 57,
    manualApprovedCount: 78,
    rejectedCount: 15,
    autoApprovalRate: 38.0,
    recentApplications: [
      { date: "2024-01-20", autoApproved: 8, manualApproved: 12, rejected: 2 },
      { date: "2024-01-19", autoApproved: 5, manualApproved: 8, rejected: 1 },
      { date: "2024-01-18", autoApproved: 12, manualApproved: 6, rejected: 3 },
      { date: "2024-01-17", autoApproved: 9, manualApproved: 11, rejected: 2 },
      { date: "2024-01-16", autoApproved: 7, manualApproved: 15, rejected: 1 },
      { date: "2024-01-15", autoApproved: 10, manualApproved: 13, rejected: 4 },
      { date: "2024-01-14", autoApproved: 6, manualApproved: 13, rejected: 2 }
    ],
    topRules: [
      { id: 1, ruleName: "베이직 플랜 자동 승인", appliedCount: 45, successRate: 89.5 },
      { id: 2, ruleName: "검증된 테넌트 프리미엄", appliedCount: 12, successRate: 95.2 }
    ]
  });

  const handleCreateRule = async (formData: AutoApprovalRuleFormData) => {
    setIsLoading(true);
    try {
      // API 호출 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const newRule: AutoApprovalRule = {
        ...formData,
        id: Date.now(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        appliedCount: 0
      };
      
      setRules(prev => [...prev, newRule]);
      setShowForm(false);
      toast.success("자동 승인 규칙이 생성되었습니다.");
    } catch (error) {
      toast.error("규칙 생성 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateRule = async (formData: AutoApprovalRuleFormData) => {
    if (!editingRule) return;
    
    setIsLoading(true);
    try {
      // API 호출 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setRules(prev => prev.map(rule => 
        rule.id === editingRule.id 
          ? { ...rule, ...formData, updatedAt: new Date().toISOString() }
          : rule
      ));
      
      setEditingRule(null);
      setShowForm(false);
      toast.success("자동 승인 규칙이 수정되었습니다.");
    } catch (error) {
      toast.error("규칙 수정 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteRule = async (ruleId: number) => {
    try {
      // API 호출 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 500));
      
      setRules(prev => prev.filter(rule => rule.id !== ruleId));
      toast.success("자동 승인 규칙이 삭제되었습니다.");
    } catch (error) {
      toast.error("규칙 삭제 중 오류가 발생했습니다.");
    }
  };

  const handleToggleRuleStatus = async (ruleId: number, isActive: boolean) => {
    try {
      // API 호출 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 300));
      
      setRules(prev => prev.map(rule => 
        rule.id === ruleId 
          ? { ...rule, isActive, updatedAt: new Date().toISOString() }
          : rule
      ));
      
      toast.success(`규칙이 ${isActive ? '활성화' : '비활성화'}되었습니다.`);
    } catch (error) {
      toast.error("규칙 상태 변경 중 오류가 발생했습니다.");
    }
  };

  const handleToggleAutoApprovalSystem = async (enabled: boolean) => {
    try {
      // API 호출 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 500));
      
      setIsAutoApprovalEnabled(enabled);
      toast.success(`자동 승인 시스템이 ${enabled ? '활성화' : '비활성화'}되었습니다.`);
    } catch (error) {
      toast.error("시스템 설정 변경 중 오류가 발생했습니다.");
    }
  };

  const handleEditRule = (rule: AutoApprovalRule) => {
    setEditingRule(rule);
    setShowForm(true);
  };

  const handleViewStats = (ruleId: number) => {
    setActiveTab("stats");
    // 특정 규칙의 상세 통계를 보여주는 로직 추가 가능
  };

  const handleCancelForm = () => {
    setShowForm(false);
    setEditingRule(null);
  };

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">자동 승인 규칙 관리</h2>
            <p className="text-muted-foreground">
              구독 신청의 자동 승인 조건을 설정하고 관리합니다.
            </p>
          </div>
          
          {!showForm && (
            <Button onClick={() => setShowForm(true)}>
              <Plus className="mr-2 h-4 w-4" />
              새 규칙 생성
            </Button>
          )}
        </div>

        {/* 시스템 전체 설정 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Settings className="h-5 w-5" />
              <span>시스템 설정</span>
            </CardTitle>
            <CardDescription>
              자동 승인 시스템의 전체 활성화 상태를 관리합니다.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label className="text-base">자동 승인 시스템</Label>
                <p className="text-sm text-muted-foreground">
                  비활성화하면 모든 구독 신청이 수동 승인으로 처리됩니다.
                </p>
              </div>
              <Switch
                checked={isAutoApprovalEnabled}
                onCheckedChange={handleToggleAutoApprovalSystem}
              />
            </div>
            
            {!isAutoApprovalEnabled && (
              <div className="mt-4 p-3 bg-orange-50 border border-orange-200 rounded-lg flex items-center space-x-2">
                <AlertCircle className="h-4 w-4 text-orange-600" />
                <p className="text-sm text-orange-800">
                  자동 승인 시스템이 비활성화되어 있습니다. 모든 구독 신청이 수동 승인으로 처리됩니다.
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 폼 표시 */}
        {showForm && (
          <AutoApprovalRuleForm
            initialData={editingRule || undefined}
            onSubmit={editingRule ? handleUpdateRule : handleCreateRule}
            onCancel={handleCancelForm}
            isLoading={isLoading}
          />
        )}

        {/* 탭 컨텐츠 */}
        {!showForm && (
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList>
              <TabsTrigger value="rules">규칙 관리</TabsTrigger>
              <TabsTrigger value="stats">통계 및 모니터링</TabsTrigger>
            </TabsList>

            <TabsContent value="rules" className="space-y-4">
              <AutoApprovalRuleList
                rules={rules}
                onEdit={handleEditRule}
                onDelete={handleDeleteRule}
                onToggleStatus={handleToggleRuleStatus}
                onViewStats={handleViewStats}
              />
            </TabsContent>

            <TabsContent value="stats" className="space-y-4">
              <AutoApprovalStats data={stats} />
            </TabsContent>
          </Tabs>
        )}
      </div>
    </DashboardLayout>
  );
}