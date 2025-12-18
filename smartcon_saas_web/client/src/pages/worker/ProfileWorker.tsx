import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Camera, CreditCard, FileText, ShieldCheck, User } from "lucide-react";

export default function ProfileWorker() {
  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">내 정보</h2>
          <p className="text-muted-foreground">개인 정보 및 자격 증명, 계좌 정보를 관리합니다.</p>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          <Card className="md:col-span-1">
            <CardContent className="pt-6 flex flex-col items-center text-center space-y-4">
              <div className="relative">
                <Avatar className="h-32 w-32">
                  <AvatarImage src="https://api.dicebear.com/7.x/avataaars/svg?seed=1" />
                  <AvatarFallback>홍길동</AvatarFallback>
                </Avatar>
                <Button size="icon" variant="secondary" className="absolute bottom-0 right-0 rounded-full">
                  <Camera className="h-4 w-4" />
                </Button>
              </div>
              <div>
                <h3 className="text-xl font-bold">홍길동</h3>
                <p className="text-muted-foreground">형틀목공 (15년차)</p>
              </div>
              <div className="flex gap-2">
                <Badge>내국인</Badge>
                <Badge variant="outline">건설기초안전교육 이수</Badge>
              </div>
            </CardContent>
          </Card>

          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle>기본 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>이름</Label>
                  <Input defaultValue="홍길동" readOnly />
                </div>
                <div className="space-y-2">
                  <Label>생년월일</Label>
                  <Input defaultValue="1980.05.15" readOnly />
                </div>
                <div className="space-y-2">
                  <Label>연락처</Label>
                  <Input defaultValue="010-1234-5678" />
                </div>
                <div className="space-y-2">
                  <Label>비상연락처</Label>
                  <Input defaultValue="010-9876-5432 (배우자)" />
                </div>
                <div className="col-span-2 space-y-2">
                  <Label>주소</Label>
                  <Input defaultValue="서울시 강동구 천호동 123-45" />
                </div>
              </div>
              <div className="flex justify-end">
                <Button>저장하기</Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5" /> 계좌 정보
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="p-4 border rounded-lg bg-muted/20">
                <div className="font-bold">국민은행</div>
                <div className="text-lg mt-1">123-456-789012</div>
                <div className="text-sm text-muted-foreground mt-1">예금주: 홍길동</div>
              </div>
              <Button variant="outline" className="w-full">계좌 변경 요청</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShieldCheck className="h-5 w-5" /> 자격 증명
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between p-3 border rounded-lg">
                <div className="flex items-center gap-3">
                  <FileText className="h-5 w-5 text-blue-600" />
                  <div>
                    <div className="font-medium">건설기초안전교육 이수증</div>
                    <div className="text-xs text-muted-foreground">2020.03.15 발급</div>
                  </div>
                </div>
                <Badge variant="outline" className="text-green-600 border-green-200">유효함</Badge>
              </div>
              <Button variant="outline" className="w-full">+ 자격증 추가</Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}
