import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Building2, Loader2 } from "lucide-react";
import { useState } from "react";
import { Link, useLocation } from "wouter";
import { toast } from "sonner";

export default function LoginHQ() {
  const [, setLocation] = useLocation();
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    // Simulate API call
    setTimeout(() => {
      setIsLoading(false);
      toast.success("로그인되었습니다.");
      setLocation("/hq/dashboard");
    }, 1500);
  };

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
            <CardTitle className="text-2xl font-bold text-center">본사 관리자 로그인</CardTitle>
            <CardDescription className="text-center">
              사업자번호와 비밀번호를 입력해주세요.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="biz-num">사업자등록번호</Label>
                <Input id="biz-num" placeholder="000-00-00000" required />
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="password">비밀번호</Label>
                  <a href="#" className="text-xs text-primary hover:underline">비밀번호 찾기</a>
                </div>
                <Input id="password" type="password" required />
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
