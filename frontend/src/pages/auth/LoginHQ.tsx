import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Building2, Loader2, Mail } from "lucide-react";
import { useState } from "react";
import { Link, useLocation } from "wouter";
import { toast } from "sonner";

/**
 * 사업자 로그인 폼 컴포넌트
 * Requirements: 1.7, 1.8, 1.9
 * 
 * 기능:
 * - 사업자번호 + 비밀번호 인증
 * - 사업자번호 유효성 검증
 * - 2FA 이메일 인증 (로그인 성공 후)
 * - 관리자 역할 자동 매핑 (슈퍼관리자, 본사관리자, 현장관리자)
 */
export default function LoginHQ() {
  const [, setLocation] = useLocation();
  const [isLoading, setIsLoading] = useState(false);
  const [show2FA, setShow2FA] = useState(false);
  const [businessNumber, setBusinessNumber] = useState("");
  const [password, setPassword] = useState("");
  const [verificationCode, setVerificationCode] = useState("");

  // 사업자번호 포맷팅 (000-00-00000)
  const formatBusinessNumber = (value: string) => {
    const numbers = value.replace(/[^\d]/g, "");
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 5) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 5)}-${numbers.slice(5, 10)}`;
  };

  const handleBusinessNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatBusinessNumber(e.target.value);
    setBusinessNumber(formatted);
  };

  // 사업자번호 유효성 검증 (체크섬 알고리즘)
  const validateBusinessNumber = (bizNum: string): boolean => {
    const numbers = bizNum.replace(/[^\d]/g, "");
    if (numbers.length !== 10) return false;

    const checksum = [1, 3, 7, 1, 3, 7, 1, 3, 5];
    let sum = 0;
    
    for (let i = 0; i < 9; i++) {
      sum += parseInt(numbers[i]) * checksum[i];
    }
    
    sum += Math.floor((parseInt(numbers[8]) * 5) / 10);
    const checkDigit = (10 - (sum % 10)) % 10;
    
    return checkDigit === parseInt(numbers[9]);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // 사업자번호 유효성 검증
    if (!validateBusinessNumber(businessNumber)) {
      toast.error("유효하지 않은 사업자번호입니다.");
      return;
    }

    setIsLoading(true);
    
    // API 호출 시뮬레이션
    setTimeout(() => {
      setIsLoading(false);
      // 2FA 화면으로 전환
      setShow2FA(true);
      toast.success("등록된 이메일로 인증코드를 발송했습니다.");
    }, 1500);
  };

  const handle2FAVerification = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    // 2FA 검증 API 호출 시뮬레이션
    setTimeout(() => {
      setIsLoading(false);
      
      if (verificationCode === "123456") {
        toast.success("로그인되었습니다.");
        // 역할에 따라 적절한 대시보드로 리다이렉트
        setLocation("/role-select");
      } else {
        toast.error("인증코드가 올바르지 않습니다.");
      }
    }, 1500);
  };

  if (show2FA) {
    return (
      <PublicLayout>
        <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
          <Card className="w-full max-w-md shadow-lg">
            <CardHeader className="space-y-2">
              <div className="flex justify-center mb-4">
                <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                  <Mail className="h-8 w-8" />
                </div>
              </div>
              <CardTitle className="text-2xl font-bold text-center">이메일 인증</CardTitle>
              <CardDescription className="text-center">
                등록된 이메일로 발송된 6자리 인증코드를 입력해주세요.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handle2FAVerification} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="verification-code">인증코드</Label>
                  <Input 
                    id="verification-code" 
                    placeholder="000000" 
                    maxLength={6}
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    required 
                  />
                </div>
                <Button type="submit" className="w-full h-11 text-base" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      인증 중...
                    </>
                  ) : (
                    "인증 완료"
                  )}
                </Button>
                <Button 
                  type="button" 
                  variant="outline" 
                  className="w-full"
                  onClick={() => setShow2FA(false)}
                >
                  뒤로가기
                </Button>
              </form>
              
              <div className="mt-6 text-center text-sm bg-muted/50 p-4 rounded-lg">
                <p className="font-medium mb-1">테스트 인증코드</p>
                <p className="text-muted-foreground">123456</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </PublicLayout>
    );
  }

  return (
    <PublicLayout>
      <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
        <Card className="w-full max-w-md shadow-lg">
          <CardHeader className="space-y-2">
            <div className="flex items-center gap-2 mb-2">
              <Link href="/login">
                <Button variant="ghost" size="icon" className="h-8 w-8 -ml-2">
                  <ArrowLeft className="h-4 w-4" />
                </Button>
              </Link>
              <span className="text-sm text-muted-foreground">뒤로가기</span>
            </div>
            <div className="flex justify-center mb-4">
              <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                <Building2 className="h-8 w-8" />
              </div>
            </div>
            <CardTitle className="text-2xl font-bold text-center">관리자 로그인</CardTitle>
            <CardDescription className="text-center">
              사업자번호와 비밀번호를 입력해주세요.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="biz-num">사업자등록번호</Label>
                <Input 
                  id="biz-num" 
                  placeholder="000-00-00000" 
                  value={businessNumber}
                  onChange={handleBusinessNumberChange}
                  maxLength={12}
                  required 
                />
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="password">비밀번호</Label>
                  <a href="#" className="text-xs text-secondary hover:underline">비밀번호 찾기</a>
                </div>
                <Input 
                  id="password" 
                  type="password" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required 
                />
              </div>
              <Button type="submit" className="w-full h-11 text-base" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    로그인 중...
                  </>
                ) : (
                  "로그인"
                )}
              </Button>
            </form>
            
            <div className="mt-6 text-center text-sm bg-muted/50 p-4 rounded-lg">
              <p className="font-medium mb-1">테스트 계정 정보</p>
              <p className="text-muted-foreground">ID: 123-45-67890 / PW: 1234</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </PublicLayout>
  );
}

