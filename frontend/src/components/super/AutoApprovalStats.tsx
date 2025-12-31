import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { TrendingUp, TrendingDown, CheckCircle, XCircle } from "lucide-react";

export interface AutoApprovalStatsData {
  totalRules: number;
  activeRules: number;
  totalApplications: number;
  autoApprovedCount: number;
  manualApprovedCount: number;
  rejectedCount: number;
  autoApprovalRate: number;
  recentApplications: {
    date: string;
    autoApproved: number;
    manualApproved: number;
    rejected: number;
  }[];
  topRules: {
    id: number;
    ruleName: string;
    appliedCount: number;
    successRate: number;
  }[];
}

interface AutoApprovalStatsProps {
  data: AutoApprovalStatsData;
  isLoading?: boolean;
}

export default function AutoApprovalStats({ data, isLoading = false }: AutoApprovalStatsProps) {
  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[...Array(4)].map((_, i) => (
          <Card key={i}>
            <CardContent className="p-6">
              <div className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-gray-200 rounded w-1/2"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 주요 지표 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">활성 규칙</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {data.activeRules} / {data.totalRules}
            </div>
            <p className="text-xs text-muted-foreground">
              전체 규칙 중 활성화됨
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">자동 승인률</CardTitle>
            <TrendingUp className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {data.autoApprovalRate.toFixed(1)}%
            </div>
            <p className="text-xs text-muted-foreground">
              전체 신청 중 자동 승인
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">자동 승인</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {data.autoApprovedCount}
            </div>
            <p className="text-xs text-muted-foreground">
              총 {data.totalApplications}건 중
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">수동 처리</CardTitle>
            <XCircle className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {data.manualApprovedCount + data.rejectedCount}
            </div>
            <p className="text-xs text-muted-foreground">
              승인 {data.manualApprovedCount} + 거부 {data.rejectedCount}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 상위 규칙 성과 */}
      <Card>
        <CardHeader>
          <CardTitle>상위 성과 규칙</CardTitle>
          <CardDescription>
            가장 많이 적용된 자동 승인 규칙들의 성과입니다.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {data.topRules.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">
                아직 적용된 규칙이 없습니다.
              </p>
            ) : (
              data.topRules.map((rule, index) => (
                <div key={rule.id} className="flex items-center justify-between p-3 border rounded-lg">
                  <div className="flex items-center space-x-3">
                    <Badge variant="outline">#{index + 1}</Badge>
                    <div>
                      <p className="font-medium">{rule.ruleName}</p>
                      <p className="text-sm text-muted-foreground">
                        {rule.appliedCount}회 적용
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-medium">
                      성공률 {rule.successRate.toFixed(1)}%
                    </div>
                    <Progress 
                      value={rule.successRate} 
                      className="w-20 h-2 mt-1"
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      {/* 최근 신청 현황 */}
      <Card>
        <CardHeader>
          <CardTitle>최근 7일 신청 현황</CardTitle>
          <CardDescription>
            일별 구독 신청 및 처리 현황입니다.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {data.recentApplications.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">
                최근 신청 데이터가 없습니다.
              </p>
            ) : (
              data.recentApplications.map((day, index) => {
                const total = day.autoApproved + day.manualApproved + day.rejected;
                const autoRate = total > 0 ? (day.autoApproved / total) * 100 : 0;
                
                return (
                  <div key={index} className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center space-x-3">
                      <div className="text-sm font-medium w-20">
                        {new Date(day.date).toLocaleDateString('ko-KR', { 
                          month: 'short', 
                          day: 'numeric' 
                        })}
                      </div>
                      <div className="flex space-x-4 text-sm">
                        <span className="text-green-600">
                          자동 {day.autoApproved}
                        </span>
                        <span className="text-blue-600">
                          수동 {day.manualApproved}
                        </span>
                        <span className="text-red-600">
                          거부 {day.rejected}
                        </span>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-medium">
                        자동화율 {autoRate.toFixed(0)}%
                      </div>
                      <Progress 
                        value={autoRate} 
                        className="w-20 h-2 mt-1"
                      />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}