import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { CheckCircle2, CreditCard, Building, Loader2 } from "lucide-react";
import { useState } from "react";
import { useLocation } from "wouter";
import { toast } from "sonner";

export default function Subscribe() {
  const [, setLocation] = useLocation();
  const [step, setStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [plan, setPlan] = useState("pro");

  const handleNext = () => {
    if (step < 3) setStep(step + 1);
    else handleSubmit();
  };

  const handleSubmit = () => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      toast.success("구독 신청이 완료되었습니다.");
      setLocation("/login/hq");
    }, 2000);
  };

  return (
    <PublicLayout>
      <div className="container max-w-3xl py-12">
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">서비스 구독 신청</h1>
          <p className="text-muted-foreground">
            간단한 정보 입력으로 스마트콘 라이트를 바로 시작해보세요.
          </p>
        </div>

        {/* Progress Steps */}
        <div className="flex items-center justify-between mb-8 relative">
          <div className="absolute top-1/2 left-0 w-full h-0.5 bg-muted -z-10"></div>
          {[1, 2, 3].map((s) => (
            <div key={s} className={`flex flex-col items-center gap-2 bg-background px-2 ${step >= s ? "text-secondary" : "text-muted-foreground"}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold border-2 transition-colors ${
                step >= s ? "border-secondary bg-secondary text-secondary-foreground" : "border-muted bg-background"
              }`}>
                {s}
              </div>
              <span className="text-xs font-medium">
                {s === 1 ? "사업자 인증" : s === 2 ? "요금제 선택" : "결제 정보"}
              </span>
            </div>
          ))}
        </div>

        <Card>
          <CardContent className="p-6">
            {step === 1 && (
              <div className="space-y-6">
                <div className="space-y-4">
                  <h2 className="text-xl font-semibold flex items-center gap-2">
                    <Building className="h-5 w-5" /> 사업자 정보 입력
                  </h2>
                  <div className="grid gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="biz-no">사업자등록번호</Label>
                      <div className="flex gap-2">
                        <Input id="biz-no" placeholder="000-00-00000" />
                        <Button variant="secondary">인증하기</Button>
                      </div>
                      <p className="text-xs text-green-600 flex items-center gap-1">
                        <CheckCircle2 className="h-3 w-3" /> 인증되었습니다. (주식회사 스마트콘)
                      </p>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="company-name">회사명</Label>
                      <Input id="company-name" defaultValue="주식회사 스마트콘" disabled />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="ceo-name">대표자명</Label>
                      <Input id="ceo-name" defaultValue="홍길동" disabled />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="email">관리자 이메일 (ID로 사용됨)</Label>
                      <Input id="email" type="email" placeholder="admin@company.com" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="password">비밀번호 설정</Label>
                      <Input id="password" type="password" />
                    </div>
                  </div>
                </div>
              </div>
            )}

            {step === 2 && (
              <div className="space-y-6">
                <h2 className="text-xl font-semibold">요금제 선택</h2>
                <RadioGroup value={plan} onValueChange={setPlan} className="grid md:grid-cols-2 gap-4">
                  <div>
                    <RadioGroupItem value="basic" id="basic" className="peer sr-only" />
                    <Label
                      htmlFor="basic"
                      className="flex flex-col justify-between rounded-md border-2 border-muted bg-popover p-4 hover:bg-accent hover:text-accent-foreground peer-data-[state=checked]:border-secondary [&:has([data-state=checked])]:border-secondary cursor-pointer h-full"
                    >
                      <div className="mb-4">
                        <div className="font-semibold text-lg">Basic</div>
                        <div className="text-sm text-muted-foreground">소규모 현장용</div>
                      </div>
                      <div className="text-2xl font-bold">₩100,000<span className="text-sm font-normal text-muted-foreground">/월</span></div>
                      <ul className="mt-4 space-y-2 text-sm text-muted-foreground">
                        <li>• 현장 1개</li>
                        <li>• 관리자 2명</li>
                      </ul>
                    </Label>
                  </div>
                  <div>
                    <RadioGroupItem value="pro" id="pro" className="peer sr-only" />
                    <Label
                      htmlFor="pro"
                      className="flex flex-col justify-between rounded-md border-2 border-muted bg-popover p-4 hover:bg-accent hover:text-accent-foreground peer-data-[state=checked]:border-secondary [&:has([data-state=checked])]:border-secondary cursor-pointer h-full relative overflow-hidden"
                    >
                      <div className="absolute top-0 right-0 bg-secondary text-secondary-foreground text-xs px-2 py-1 rounded-bl">추천</div>
                      <div className="mb-4">
                        <div className="font-semibold text-lg text-secondary">Pro</div>
                        <div className="text-sm text-muted-foreground">전문건설사용</div>
                      </div>
                      <div className="text-2xl font-bold">₩300,000<span className="text-sm font-normal text-muted-foreground">/월</span></div>
                      <ul className="mt-4 space-y-2 text-sm text-muted-foreground">
                        <li>• 현장 5개</li>
                        <li>• 관리자 10명</li>
                        <li>• 안면인식 연동</li>
                      </ul>
                    </Label>
                  </div>
                </RadioGroup>
              </div>
            )}

            {step === 3 && (
              <div className="space-y-6">
                <h2 className="text-xl font-semibold flex items-center gap-2">
                  <CreditCard className="h-5 w-5" /> 결제 정보 등록
                </h2>
                
                <div className="bg-muted/30 p-4 rounded-lg space-y-2 mb-6">
                  <div className="flex justify-between font-medium">
                    <span>선택한 요금제</span>
                    <span className="text-secondary">Pro Plan</span>
                  </div>
                  <div className="flex justify-between text-sm text-muted-foreground">
                    <span>월 결제 금액 (VAT 포함)</span>
                    <span>₩330,000</span>
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label>카드 번호</Label>
                    <div className="flex gap-2">
                      <Input placeholder="0000" className="text-center" maxLength={4} />
                      <Input placeholder="0000" className="text-center" maxLength={4} />
                      <Input placeholder="0000" className="text-center" maxLength={4} />
                      <Input placeholder="0000" className="text-center" maxLength={4} />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label>유효기간</Label>
                      <Input placeholder="MM/YY" className="text-center" />
                    </div>
                    <div className="space-y-2">
                      <Label>CVC</Label>
                      <Input placeholder="***" type="password" className="text-center" maxLength={3} />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label>생년월일 (6자리) 또는 사업자번호 (10자리)</Label>
                    <Input placeholder="주민번호 앞 6자리 또는 사업자번호" />
                  </div>
                </div>
              </div>
            )}

            <div className="flex justify-between mt-8 pt-4 border-t">
              <Button
                variant="outline"
                onClick={() => setStep(step - 1)}
                disabled={step === 1 || isLoading}
              >
                이전
              </Button>
              <Button onClick={handleNext} disabled={isLoading} className="min-w-[100px]">
                {isLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : step === 3 ? (
                  "구독 완료"
                ) : (
                  "다음"
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </PublicLayout>
  );
}



