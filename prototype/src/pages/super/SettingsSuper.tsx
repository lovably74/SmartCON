import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Settings,
  Server,
  Mail,
  Shield,
  Database,
  Bell,
  Users,
  CreditCard,
  Save,
  RefreshCw
} from "lucide-react";
import { useState } from "react";

export default function SettingsSuper() {
  const [isLoading, setIsLoading] = useState(false);

  // Mock Data
  const systemSettings = {
    serverStatus: "running",
    databaseStatus: "connected",
    emailService: "active",
    backupStatus: "completed",
    lastBackup: "2025-01-01 02:00:00"
  };

  const handleSave = () => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
    }, 1000);
  };

  return (
    <DashboardLayout role="super">
      <div className="space-y-6">
        {/* 시스템 상태 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Server className="h-5 w-5" />
              시스템 상태
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-4 gap-6">
              <div className="flex items-center gap-3">
                <div className="h-3 w-3 rounded-full bg-green-500"></div>
                <div>
                  <div className="font-semibold">서버 상태</div>
                  <div className="text-sm text-muted-foreground">정상 운영</div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="h-3 w-3 rounded-full bg-green-500"></div>
                <div>
                  <div className="font-semibold">데이터베이스</div>
                  <div className="text-sm text-muted-foreground">연결됨</div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="h-3 w-3 rounded-full bg-green-500"></div>
                <div>
                  <div className="font-semibold">이메일 서비스</div>
                  <div className="text-sm text-muted-foreground">활성</div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="h-3 w-3 rounded-full bg-blue-500"></div>
                <div>
                  <div className="font-semibold">백업</div>
                  <div className="text-sm text-muted-foreground">완료 (2시간 전)</div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 설정 탭 */}
        <Tabs defaultValue="system" className="space-y-6">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="system">시스템</TabsTrigger>
            <TabsTrigger value="email">이메일</TabsTrigger>
            <TabsTrigger value="security">보안</TabsTrigger>
            <TabsTrigger value="billing">결제</TabsTrigger>
            <TabsTrigger value="notifications">알림</TabsTrigger>
          </TabsList>

          <TabsContent value="system" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>시스템 설정</CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="system-name">시스템 이름</Label>
                    <Input id="system-name" defaultValue="SmartCON Lite" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="system-version">시스템 버전</Label>
                    <Input id="system-version" defaultValue="1.0.0" disabled />
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">자동 백업</Label>
                      <p className="text-sm text-muted-foreground">매일 새벽 2시에 자동으로 백업을 수행합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">시스템 모니터링</Label>
                      <p className="text-sm text-muted-foreground">시스템 성능을 실시간으로 모니터링합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">자동 업데이트</Label>
                      <p className="text-sm text-muted-foreground">보안 패치를 자동으로 적용합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>
                </div>

                <div className="flex gap-4 pt-4 border-t">
                  <Button onClick={handleSave} disabled={isLoading}>
                    {isLoading ? <RefreshCw className="h-4 w-4 mr-2 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
                    저장
                  </Button>
                  <Button variant="outline">
                    <RefreshCw className="h-4 w-4 mr-2" />
                    시스템 재시작
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="email" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Mail className="h-5 w-5" />
                  이메일 설정
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="smtp-server">SMTP 서버</Label>
                    <Input id="smtp-server" defaultValue="smtp.gmail.com" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="smtp-port">포트</Label>
                    <Input id="smtp-port" defaultValue="587" />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="email-username">사용자명</Label>
                    <Input id="email-username" defaultValue="noreply@smartcon.net" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email-password">비밀번호</Label>
                    <Input id="email-password" type="password" defaultValue="••••••••" />
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">SSL/TLS 사용</Label>
                      <p className="text-sm text-muted-foreground">보안 연결을 사용합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">이메일 로그</Label>
                      <p className="text-sm text-muted-foreground">발송된 이메일을 로그로 기록합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>
                </div>

                <div className="flex gap-4 pt-4 border-t">
                  <Button onClick={handleSave} disabled={isLoading}>
                    {isLoading ? <RefreshCw className="h-4 w-4 mr-2 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
                    저장
                  </Button>
                  <Button variant="outline">
                    <Mail className="h-4 w-4 mr-2" />
                    테스트 발송
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="security" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Shield className="h-5 w-5" />
                  보안 설정
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">2단계 인증 강제</Label>
                      <p className="text-sm text-muted-foreground">모든 관리자 계정에 2단계 인증을 강제합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">IP 화이트리스트</Label>
                      <p className="text-sm text-muted-foreground">허용된 IP에서만 관리자 접근을 허용합니다.</p>
                    </div>
                    <Switch />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">세션 타임아웃</Label>
                      <p className="text-sm text-muted-foreground">비활성 시간 후 자동 로그아웃합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="session-timeout">세션 타임아웃 (분)</Label>
                    <Input id="session-timeout" defaultValue="30" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="password-policy">비밀번호 최소 길이</Label>
                    <Input id="password-policy" defaultValue="8" />
                  </div>
                </div>

                <div className="flex gap-4 pt-4 border-t">
                  <Button onClick={handleSave} disabled={isLoading}>
                    {isLoading ? <RefreshCw className="h-4 w-4 mr-2 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
                    저장
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="billing" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <CreditCard className="h-5 w-5" />
                  결제 설정
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="payment-gateway">결제 게이트웨이</Label>
                    <select id="payment-gateway" className="w-full p-2 border rounded-md">
                      <option value="toss">토스페이먼츠</option>
                      <option value="iamport">아임포트</option>
                      <option value="inicis">이니시스</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="currency">기본 통화</Label>
                    <select id="currency" className="w-full p-2 border rounded-md">
                      <option value="KRW">KRW (원)</option>
                      <option value="USD">USD (달러)</option>
                    </select>
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">자동 결제</Label>
                      <p className="text-sm text-muted-foreground">매월 자동으로 구독료를 결제합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">결제 실패 알림</Label>
                      <p className="text-sm text-muted-foreground">결제 실패 시 관리자에게 알림을 발송합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">세금계산서 자동 발행</Label>
                      <p className="text-sm text-muted-foreground">결제 완료 시 자동으로 세금계산서를 발행합니다.</p>
                    </div>
                    <Switch defaultChecked />
                  </div>
                </div>

                <div className="flex gap-4 pt-4 border-t">
                  <Button onClick={handleSave} disabled={isLoading}>
                    {isLoading ? <RefreshCw className="h-4 w-4 mr-2 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
                    저장
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="notifications" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Bell className="h-5 w-5" />
                  알림 설정
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">시스템 알림</Label>
                      <p className="text-sm text-muted-foreground">시스템 오류 및 중요 이벤트 알림</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">결제 알림</Label>
                      <p className="text-sm text-muted-foreground">결제 성공/실패 알림</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">신규 가입 알림</Label>
                      <p className="text-sm text-muted-foreground">새로운 테넌트 가입 알림</p>
                    </div>
                    <Switch defaultChecked />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <Label className="text-base font-medium">일일 리포트</Label>
                      <p className="text-sm text-muted-foreground">매일 시스템 현황 리포트 발송</p>
                    </div>
                    <Switch />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="admin-email">관리자 이메일</Label>
                  <Input id="admin-email" defaultValue="admin@smartcon.net" />
                </div>

                <div className="flex gap-4 pt-4 border-t">
                  <Button onClick={handleSave} disabled={isLoading}>
                    {isLoading ? <RefreshCw className="h-4 w-4 mr-2 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
                    저장
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}