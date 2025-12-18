import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AlertTriangle, Database, Lock, Server, Shield } from "lucide-react";

export default function SettingsSuper() {
  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">시스템 설정</h2>
          <p className="text-muted-foreground">SaaS 플랫폼 전체 환경설정 및 시스템 상태를 관리합니다.</p>
        </div>

        <Tabs defaultValue="general" className="space-y-4">
          <TabsList>
            <TabsTrigger value="general">일반 설정</TabsTrigger>
            <TabsTrigger value="security">보안 정책</TabsTrigger>
            <TabsTrigger value="system">시스템 상태</TabsTrigger>
          </TabsList>

          <TabsContent value="general" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>플랫폼 기본 정보</CardTitle>
                <CardDescription>서비스 명칭 및 운영자 정보를 설정합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>서비스명</Label>
                    <Input defaultValue="SmartCON Lite SaaS" />
                  </div>
                  <div className="space-y-2">
                    <Label>관리자 이메일</Label>
                    <Input defaultValue="admin@ismartcon.net" />
                  </div>
                  <div className="space-y-2">
                    <Label>고객센터 전화번호</Label>
                    <Input defaultValue="1588-1234" />
                  </div>
                  <div className="space-y-2">
                    <Label>서비스 도메인</Label>
                    <Input defaultValue="https://lite.ismartcon.net" disabled />
                  </div>
                </div>
                <div className="flex justify-end">
                  <Button>저장하기</Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="security" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>보안 정책 설정</CardTitle>
                <CardDescription>전체 테넌트에 적용되는 보안 정책을 관리합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">강력한 비밀번호 정책 강제</Label>
                    <p className="text-sm text-muted-foreground">모든 사용자의 비밀번호 복잡도 규칙을 강제합니다.</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">다중 기기 로그인 제한</Label>
                    <p className="text-sm text-muted-foreground">동일 계정의 동시 접속을 차단합니다.</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">관리자 2FA 필수화</Label>
                    <p className="text-sm text-muted-foreground">모든 테넌트 관리자의 2단계 인증을 의무화합니다.</p>
                  </div>
                  <Switch />
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="system" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">CPU 사용량</CardTitle>
                  <Server className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">24%</div>
                  <p className="text-xs text-muted-foreground">정상 범위</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">메모리 사용량</CardTitle>
                  <Server className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">4.2 GB</div>
                  <p className="text-xs text-muted-foreground">전체 16GB 중</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">DB 연결</CardTitle>
                  <Database className="h-4 w-4 text-green-600" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold text-green-600">정상</div>
                  <p className="text-xs text-muted-foreground">Latency: 12ms</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">시스템 경고</CardTitle>
                  <AlertTriangle className="h-4 w-4 text-orange-600" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold text-orange-600">0건</div>
                  <p className="text-xs text-muted-foreground">최근 24시간</p>
                </CardContent>
              </Card>
            </div>
            
            <Card>
              <CardHeader>
                <CardTitle>시스템 로그</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="bg-black text-green-400 p-4 rounded-md font-mono text-sm h-48 overflow-y-auto">
                  <div>[2025-12-18 14:30:05] INFO: System backup completed successfully.</div>
                  <div>[2025-12-18 14:15:22] INFO: New tenant registered (ID: 1045).</div>
                  <div>[2025-12-18 13:45:10] WARN: High traffic detected on API server-02.</div>
                  <div>[2025-12-18 13:00:00] INFO: Scheduled maintenance task started.</div>
                  <div>[2025-12-18 12:30:00] INFO: Daily report generation completed.</div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}
