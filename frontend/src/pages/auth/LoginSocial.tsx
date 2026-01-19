import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Loader2, MessageCircle, Phone, Shield } from "lucide-react";
import { useState, useEffect } from "react";
import { Link, useLocation, useSearch } from "wouter";
import { toast } from "sonner";

/**
 * 소셜 로그인 컴포넌트 (개인사용자용)
 * Requirements: 1.2, 1.3, 1.4, 2.2, 2.3
 * 
 * 기능:
 * - 카카오/네이버 OAuth2 로그인
 * - 최초 로그인시 휴대폰 인증
 * - CI값 생성 및 사용자 식별
 * - 소셜 계정 연동
 */

type SocialProvider = "kakao" | "naver";
type LoginStep = "social" | "phone-verify" | "phone-confirm";

export default function LoginSocial() {
  const [, setLocation] = useLocation();
  const searchParams = useSearch();
  const provider = new URLSearchParams(searchParams).get("provider") as SocialProvider;
  
  const [isLoading, setIsLoading] = useState(false);
  const [step, setStep] = useState<LoginStep>("social");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [verificationCode, setVerificationCode] = useState("");
  const [isNewUser, setIsNewUser] = useState(false);

  useEffect(() => {
    if (!provider || (provider !== "kakao" && provider !== "naver")) {
      toast.error("잘못된 접근입니다.");
      setLocation("/login");
    }
  }, [provider, setLocation]);

  const getProviderInfo = () => {
    if (provider === "kakao") {
      return {
        name: "카카오",
        color: "bg-[#FEE500] text-[#000000] hover:bg-[#FEE500]/90",
        icon: <MessageCircle className="h-6 w-6 fill-current" />
      };
    }
    return {
      name: "네이버",
      color: "bg-[#03C75A] text-white hover:bg-[#03C75A]/90",
      icon: <span className="font-bold text-xl">N</span>
    };
  };

  const providerInfo = getProviderInfo();

  // 휴대폰 번호 포맷팅 (000-0000-0000)
  const formatPhoneNumber = (value: string) => {
    const numbers = value.replace(/[^\d]/g, "");
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  };

  const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatPhoneNumber(e.target.value);
    setPhoneNumber(formatted);
  };

  const handleSocialLogin = async () => {
    setIsLoading(true);
    
    // OAuth2 로그인 시뮬레이션
    setTimeout(() => {
      setIsLoading(false);
      // 최초 로그인 사용자인 경우 휴대폰 인증 필요
      const isFirstTime = Math.random() > 0.5; // 시뮬레이션
      
      if (isFirstTime) {
        setIsNewUser(true);
        setStep("phone-verify");
        toast.info("최초 로그인입니다. 휴대폰 인증을 진행해주세요.");
      } else {
        toast.success("로그인되었습니다.");
        setLocation("/role-select");
      }
    }, 1500);
  };

  const handlePhoneVerification = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (phoneNumber.replace(/[^\d]/g, "").length !== 11) {
      toast.error("올바른 휴대폰 번호를 입력해주세요.");
      return;
    }

    setIsLoading(true);
    
    // 인증번호 발송 API 호출 시뮬레이션
    setTimeout(() => {
      setIsLoading(false);
      setStep("phone-confirm");
      toast.success("인증번호가 발송되었습니다.");
    }, 1500);
  };

  const handlePhoneConfirmation = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    // CI값 생성 및 계정 연동 API 호출 시뮬레이션
    setTimeout(() => {
      setIsLoading(false);
      
      if (verificationCode === "123456") {
        toast.success("휴대폰 인증이 완료되었습니다. CI값이 생성되었습니다.");
        setLocation("/role-select");
      } else {
        toast.error("인증번호가 올바르지 않습니다.");
      }
    }, 1500);
  };

  // 소셜 로그인 단계
  if (step === "social") {
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
                <div className={`h-16 w-16 rounded-full flex items-center justify-center ${providerInfo.color}`}>
                  {providerInfo.icon}
                </div>
              </div>
              <CardTitle className="text-2xl font-bold text-center">{providerInfo.name} 로그인</CardTitle>
              <CardDescription className="text-center">
                {providerInfo.name} 계정으로 간편하게 로그인하세요.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button 
                onClick={handleSocialLogin}
                className={`w-full h-12 ${providerInfo.color}`}
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    로그인 중...
                  </>
                ) : (
                  <>
                    {providerInfo.icon}
                    <span className="ml-2">{providerInfo.name}로 계속하기</span>
                  </>
                )}
              </Button>

              <div className="text-xs text-muted-foreground text-center p-4 bg-muted/50 rounded-lg">
                <p className="mb-2">개인사용자 전용 로그인입니다.</p>
                <p>노무팀장 및 일반노무자만 이용 가능합니다.</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </PublicLayout>
    );
  }

  // 휴대폰 인증 단계
  if (step === "phone-verify") {
    return (
      <PublicLayout>
        <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
          <Card className="w-full max-w-md shadow-lg">
            <CardHeader className="space-y-2">
              <div className="flex justify-center mb-4">
                <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                  <Phone className="h-8 w-8" />
                </div>
              </div>
              <CardTitle className="text-2xl font-bold text-center">휴대폰 인증</CardTitle>
              <CardDescription className="text-center">
                본인 확인을 위해 휴대폰 번호를 입력해주세요.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handlePhoneVerification} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="phone">휴대폰 번호</Label>
                  <Input 
                    id="phone" 
                    placeholder="010-0000-0000" 
                    value={phoneNumber}
                    onChange={handlePhoneNumberChange}
                    maxLength={13}
                    required 
                  />
                </div>
                
                <div className="text-xs text-muted-foreground bg-blue-50 dark:bg-blue-950 p-3 rounded-lg">
                  <div className="flex items-start gap-2">
                    <Shield className="h-4 w-4 mt-0.5 flex-shrink-0" />
                    <div>
                      <p className="font-medium mb-1">CI값 생성 안내</p>
                      <p>휴대폰 인증을 통해 고유 식별값(CI)이 생성됩니다. CI값은 개인정보 보호를 위한 암호화된 식별자입니다.</p>
                    </div>
                  </div>
                </div>

                <Button type="submit" className="w-full h-11 text-base" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      인증번호 발송 중...
                    </>
                  ) : (
                    "인증번호 받기"
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </PublicLayout>
    );
  }

  // 인증번호 확인 단계
  return (
    <PublicLayout>
      <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
        <Card className="w-full max-w-md shadow-lg">
          <CardHeader className="space-y-2">
            <div className="flex justify-center mb-4">
              <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                <Shield className="h-8 w-8" />
              </div>
            </div>
            <CardTitle className="text-2xl font-bold text-center">인증번호 확인</CardTitle>
            <CardDescription className="text-center">
              {phoneNumber}로 발송된 인증번호를 입력해주세요.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handlePhoneConfirmation} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="verification-code">인증번호</Label>
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
                onClick={() => setStep("phone-verify")}
              >
                다시 입력하기
              </Button>
            </form>
            
            <div className="mt-6 text-center text-sm bg-muted/50 p-4 rounded-lg">
              <p className="font-medium mb-1">테스트 인증번호</p>
              <p className="text-muted-foreground">123456</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </PublicLayout>
  );
}
