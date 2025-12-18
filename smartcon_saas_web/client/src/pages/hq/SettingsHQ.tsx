import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Bell, Building, CreditCard, Lock, Shield, User } from "lucide-react";

export default function SettingsHQ() {
  return (
    <DashboardLayout role="hq">
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">설정</h2>
          <p className="text-muted-foreground">회사 정보 및 시스템 환경설정을 관리합니다.</p>
        </div>

        <Tabs defaultValue="company" className="space-y-4">
          <TabsList>
            <TabsTrigger value="company">회사 정보</TabsTrigger>
            <TabsTrigger value="billing">구독 및 결제</TabsTrigger>
            <TabsTrigger value="security">보안 및 권한</TabsTrigger>
            <TabsTrigger value="notifications">알림 설정</TabsTrigger>
          </TabsList>

          <TabsContent value="company" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>기본 정보</CardTitle>
                <CardDescription>회사 기본 정보 및 사업자 정보를 관리합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>회사명</Label>
                    <Input defaultValue="주식회사 스마트콘" />
                  </div>
                  <div className="space-y-2">
                    <Label>대표자명</Label>
                    <Input defaultValue="홍길동" />
                  </div>
                  <div className="space-y-2">
                    <Label>사업자등록번호</Label>
                    <Input defaultValue="123-45-67890" disabled />
                  </div>
                  <div className="space-y-2">
                    <Label>법인등록번호</Label>
                    <Input defaultValue="110111-1234567" />
                  </div>
                  <div className="space-y-2">
                    <Label>대표 전화번호</Label>
                    <Input defaultValue="02-1234-5678" />
                  </div>
                  <div className="space-y-2">
                    <Label>팩스 번호</Label>
                    <Input defaultValue="02-1234-5679" />
                  </div>
                  <div className="col-span-2 space-y-2">
                    <Label>사업장 주소</Label>
                    <Input defaultValue="서울시 강남구 테헤란로 123, 스마트타워 10층" />
                  </div>
                </div>
                <div className="flex justify-end">
                  <Button>저장하기</Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="billing" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>현재 구독 정보</CardTitle>
                <CardDescription>이용 중인 요금제 및 결제 수단을 관리합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex items-center justify-between p-4 border rounded-lg bg-muted/20">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                      <CreditCard className="h-6 w-6" />
                    </div>
                    <div>
                      <div className="font-bold text-lg">Pro Plan</div>
                      <div className="text-sm text-muted-foreground">월 330,000원 (VAT 포함)</div>
                    </div>
                  </div>
                  <Badge variant="outline" className="text-green-600 border-green-200 bg-green-50">이용중</Badge>
                </div>
                
                <div className="space-y-4">
                  <h3 className="font-medium">결제 수단</h3>
                  <div className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-6 bg-blue-600 rounded text-white text-xs flex items-center justify-center font-bold">VISA</div>
                      <span>국민카드 (****-****-****-1234)</span>
                    </div>
                    <Button variant="ghost" size="sm">변경</Button>
                  </div>
                </div>

                <div className="space-y-4">
                  <h3 className="font-medium">결제 이력</h3>
                  <div className="border rounded-lg divide-y">
                    {[
                      { date: "2025.12.01", amount: "330,000원", status: "결제성공" },
                      { date: "2025.11.01", amount: "330,000원", status: "결제성공" },
                      { date: "2025.10.01", amount: "330,000원", status: "결제성공" },
                    ].map((item, i) => (
                      <div key={i} className="flex items-center justify-between p-3 text-sm">
                        <span>{item.date}</span>
                        <div className="flex items-center gap-4">
                          <span>{item.amount}</span>
                          <span className="text-green-600">{item.status}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="security" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>보안 설정</CardTitle>
                <CardDescription>계정 보안 및 접근 권한을 설정합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">2단계 인증 (2FA)</Label>
                    <p className="text-sm text-muted-foreground">로그인 시 추가 인증을 요구하여 보안을 강화합니다.</p>
                  </div>
                  <Switch />
                </div>
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">세션 타임아웃</Label>
                    <p className="text-sm text-muted-foreground">30분 동안 활동이 없으면 자동으로 로그아웃합니다.</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">IP 접속 제한</Label>
                    <p className="text-sm text-muted-foreground">지정된 IP 대역에서만 관리자 페이지 접속을 허용합니다.</p>
                  </div>
                  <Switch />
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="notifications" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>알림 설정</CardTitle>
                <CardDescription>이메일 및 푸시 알림 수신 여부를 설정합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">일일 리포트 수신</Label>
                    <p className="text-sm text-muted-foreground">매일 아침 전일 현장 운영 현황을 이메일로 받습니다.</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">계약 체결 알림</Label>
                    <p className="text-sm text-muted-foreground">근로계약 체결 시 실시간 알림을 받습니다.</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label className="text-base">현장 이슈 알림</Label>
                    <p className="text-sm text-muted-foreground">안전 사고 등 긴급 이슈 발생 시 알림을 받습니다.</p>
                  </div>
                  <Switch defaultChecked />
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}

function Badge({ className, variant, children }: any) {
  return (
    <span className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 ${className}`}>
      {children}
    </span>
  );
}
