import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Users, TrendingUp, TrendingDown } from "lucide-react";

/**
 * AttendanceChart 공통 컴포넌트
 * Requirements: 3.4, 3.5
 * 
 * 기능:
 * - 출역 현황 차트 표시
 * - 일별/주별/월별 통계
 * - 공종별 분포
 * - 모바일 반응형
 */

export interface AttendanceData {
  date: string;
  count: number;
  target?: number;
}

export interface JobTypeDistribution {
  jobType: string;
  count: number;
  percentage: number;
}

export interface AttendanceChartProps {
  title?: string;
  data: AttendanceData[];
  jobTypeDistribution?: JobTypeDistribution[];
  showTrend?: boolean;
  className?: string;
}

export function AttendanceChart({
  title = "출역 현황",
  data,
  jobTypeDistribution,
  showTrend = true,
  className,
}: AttendanceChartProps) {
  // 최근 7일 데이터
  const recentData = data.slice(-7);
  
  // 최대값 계산 (차트 스케일링용)
  const maxCount = Math.max(...recentData.map((d) => d.count), ...recentData.map((d) => d.target || 0));
  
  // 평균 출역률 계산
  const averageAttendance = recentData.reduce((sum, d) => {
    if (d.target) {
      return sum + (d.count / d.target) * 100;
    }
    return sum;
  }, 0) / recentData.filter((d) => d.target).length;

  // 전주 대비 증감
  const lastWeekAvg = data.slice(-14, -7).reduce((sum, d) => sum + d.count, 0) / 7;
  const thisWeekAvg = recentData.reduce((sum, d) => sum + d.count, 0) / 7;
  const weeklyChange = ((thisWeekAvg - lastWeekAvg) / lastWeekAvg) * 100;

  const getJobTypeColor = (index: number) => {
    const colors = [
      "bg-blue-500",
      "bg-green-500",
      "bg-orange-500",
      "bg-purple-500",
      "bg-pink-500",
      "bg-yellow-500",
    ];
    return colors[index % colors.length];
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5" />
            {title}
          </CardTitle>
          {showTrend && !isNaN(weeklyChange) && (
            <Badge variant={weeklyChange >= 0 ? "default" : "destructive"} className="gap-1">
              {weeklyChange >= 0 ? (
                <TrendingUp className="h-3 w-3" />
              ) : (
                <TrendingDown className="h-3 w-3" />
              )}
              {Math.abs(weeklyChange).toFixed(1)}%
            </Badge>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* 막대 차트 */}
        <div className="space-y-3">
          {recentData.map((item, index) => {
            const percentage = (item.count / maxCount) * 100;
            const targetPercentage = item.target ? (item.target / maxCount) * 100 : 0;
            const achievementRate = item.target ? (item.count / item.target) * 100 : 100;
            
            return (
              <div key={index} className="space-y-1">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">
                    {new Date(item.date).toLocaleDateString("ko-KR", {
                      month: "short",
                      day: "numeric",
                    })}
                  </span>
                  <div className="flex items-center gap-2">
                    <span className="font-medium">{item.count}명</span>
                    {item.target && (
                      <span className="text-xs text-muted-foreground">
                        / {item.target}명 ({achievementRate.toFixed(0)}%)
                      </span>
                    )}
                  </div>
                </div>
                <div className="relative h-8 bg-muted rounded-full overflow-hidden">
                  {/* 목표선 */}
                  {item.target && (
                    <div
                      className="absolute top-0 h-full border-r-2 border-dashed border-muted-foreground/30"
                      style={{ left: `${targetPercentage}%` }}
                    />
                  )}
                  {/* 실제 출역 */}
                  <div
                    className={`h-full rounded-full transition-all ${
                      achievementRate >= 100
                        ? "bg-green-500"
                        : achievementRate >= 80
                        ? "bg-blue-500"
                        : "bg-orange-500"
                    }`}
                    style={{ width: `${percentage}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>

        {/* 통계 요약 */}
        <div className="grid grid-cols-2 gap-4 pt-4 border-t">
          <div className="space-y-1">
            <p className="text-xs text-muted-foreground">평균 출역률</p>
            <p className="text-2xl font-bold">
              {isNaN(averageAttendance) ? "-" : `${averageAttendance.toFixed(1)}%`}
            </p>
          </div>
          <div className="space-y-1">
            <p className="text-xs text-muted-foreground">주간 평균</p>
            <p className="text-2xl font-bold">{thisWeekAvg.toFixed(0)}명</p>
          </div>
        </div>

        {/* 공종별 분포 */}
        {jobTypeDistribution && jobTypeDistribution.length > 0 && (
          <div className="space-y-3 pt-4 border-t">
            <h4 className="text-sm font-medium">공종별 분포</h4>
            <div className="space-y-2">
              {jobTypeDistribution.map((job, index) => (
                <div key={index} className="flex items-center gap-3">
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center justify-between text-sm">
                      <span>{job.jobType}</span>
                      <span className="font-medium">
                        {job.count}명 ({job.percentage.toFixed(1)}%)
                      </span>
                    </div>
                    <div className="h-2 bg-muted rounded-full overflow-hidden">
                      <div
                        className={`h-full ${getJobTypeColor(index)}`}
                        style={{ width: `${job.percentage}%` }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
