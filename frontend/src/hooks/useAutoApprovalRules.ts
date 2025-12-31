import { useState, useEffect } from 'react';
import { AutoApprovalRule } from '@/components/super/AutoApprovalRuleList';
import { AutoApprovalRuleFormData } from '@/components/super/AutoApprovalRuleForm';
import { AutoApprovalStatsData } from '@/components/super/AutoApprovalStats';

// API 응답 타입 정의
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

// 자동 승인 규칙 관리 훅
export function useAutoApprovalRules() {
  const [rules, setRules] = useState<AutoApprovalRule[]>([]);
  const [stats, setStats] = useState<AutoApprovalStatsData | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 규칙 목록 조회
  const fetchRules = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error('규칙 목록을 불러오는데 실패했습니다.');
      }
      
      const result: ApiResponse<AutoApprovalRule[]> = await response.json();
      
      if (result.success) {
        setRules(result.data);
      } else {
        throw new Error(result.message || '규칙 목록 조회 실패');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  // 통계 데이터 조회
  const fetchStats = async () => {
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules/stats', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error('통계 데이터를 불러오는데 실패했습니다.');
      }
      
      const result: ApiResponse<AutoApprovalStatsData> = await response.json();
      
      if (result.success) {
        setStats(result.data);
      }
    } catch (err) {
      console.error('통계 데이터 조회 실패:', err);
    }
  };

  // 규칙 생성
  const createRule = async (ruleData: AutoApprovalRuleFormData): Promise<boolean> => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(ruleData)
      });
      
      if (!response.ok) {
        throw new Error('규칙 생성에 실패했습니다.');
      }
      
      const result: ApiResponse<AutoApprovalRule> = await response.json();
      
      if (result.success) {
        setRules(prev => [...prev, result.data]);
        return true;
      } else {
        throw new Error(result.message || '규칙 생성 실패');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // 규칙 수정
  const updateRule = async (ruleId: number, ruleData: AutoApprovalRuleFormData): Promise<boolean> => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch(`/api/v1/admin/auto-approval-rules/${ruleId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(ruleData)
      });
      
      if (!response.ok) {
        throw new Error('규칙 수정에 실패했습니다.');
      }
      
      const result: ApiResponse<AutoApprovalRule> = await response.json();
      
      if (result.success) {
        setRules(prev => prev.map(rule => 
          rule.id === ruleId ? result.data : rule
        ));
        return true;
      } else {
        throw new Error(result.message || '규칙 수정 실패');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // 규칙 삭제
  const deleteRule = async (ruleId: number): Promise<boolean> => {
    try {
      const response = await fetch(`/api/v1/admin/auto-approval-rules/${ruleId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error('규칙 삭제에 실패했습니다.');
      }
      
      const result: ApiResponse<void> = await response.json();
      
      if (result.success) {
        setRules(prev => prev.filter(rule => rule.id !== ruleId));
        return true;
      } else {
        throw new Error(result.message || '규칙 삭제 실패');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      return false;
    }
  };

  // 규칙 상태 토글
  const toggleRuleStatus = async (ruleId: number, isActive: boolean): Promise<boolean> => {
    try {
      const response = await fetch(`/api/v1/admin/auto-approval-rules/${ruleId}/toggle`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ isActive })
      });
      
      if (!response.ok) {
        throw new Error('규칙 상태 변경에 실패했습니다.');
      }
      
      const result: ApiResponse<AutoApprovalRule> = await response.json();
      
      if (result.success) {
        setRules(prev => prev.map(rule => 
          rule.id === ruleId ? { ...rule, isActive } : rule
        ));
        return true;
      } else {
        throw new Error(result.message || '규칙 상태 변경 실패');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      return false;
    }
  };

  // 자동 승인 시스템 전체 토글
  const toggleAutoApprovalSystem = async (enabled: boolean): Promise<boolean> => {
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules/system/toggle', {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled })
      });
      
      if (!response.ok) {
        throw new Error('시스템 설정 변경에 실패했습니다.');
      }
      
      const result: ApiResponse<{ enabled: boolean }> = await response.json();
      
      if (result.success) {
        return true;
      } else {
        throw new Error(result.message || '시스템 설정 변경 실패');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      return false;
    }
  };

  // 자동 승인 시스템 상태 조회
  const checkAutoApprovalSystemStatus = async (): Promise<boolean> => {
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules/system/status', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        return true; // 기본값으로 활성화 상태 반환
      }
      
      const result: ApiResponse<{ enabled: boolean }> = await response.json();
      return result.success ? result.data.enabled : true;
    } catch (err) {
      console.error('시스템 상태 조회 실패:', err);
      return true; // 기본값으로 활성화 상태 반환
    }
  };

  // 컴포넌트 마운트 시 데이터 로드
  useEffect(() => {
    fetchRules();
    fetchStats();
  }, []);

  return {
    rules,
    stats,
    isLoading,
    error,
    fetchRules,
    fetchStats,
    createRule,
    updateRule,
    deleteRule,
    toggleRuleStatus,
    toggleAutoApprovalSystem,
    checkAutoApprovalSystemStatus
  };
}

// 자동 승인 시스템 상태 관리 훅
export function useAutoApprovalSystemStatus() {
  const [isEnabled, setIsEnabled] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const checkStatus = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules/system/status', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const result: ApiResponse<{ enabled: boolean }> = await response.json();
        if (result.success) {
          setIsEnabled(result.data.enabled);
        }
      }
    } catch (err) {
      console.error('시스템 상태 조회 실패:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleSystem = async (enabled: boolean): Promise<boolean> => {
    try {
      const response = await fetch('/api/v1/admin/auto-approval-rules/system/toggle', {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled })
      });
      
      if (response.ok) {
        const result: ApiResponse<{ enabled: boolean }> = await response.json();
        if (result.success) {
          setIsEnabled(enabled);
          return true;
        }
      }
      return false;
    } catch (err) {
      console.error('시스템 토글 실패:', err);
      return false;
    }
  };

  useEffect(() => {
    checkStatus();
  }, []);

  return {
    isEnabled,
    isLoading,
    toggleSystem,
    checkStatus
  };
}