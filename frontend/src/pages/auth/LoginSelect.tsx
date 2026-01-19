import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Building2, MessageCircle, Users } from "lucide-react";
import { Link } from "wouter";

/**
 * 통합 로그인 페이지 - 개인사용자와 관리자를 구분하는 진입점
 * Requirements: 1.1, 1.2, 1.7
 * 
 * 로그인 유형:
 * - 관리자 (슈퍼관리자, 본사관리자, 현장관리자): 사업자번호 + 비밀번호
 * - 개인사용자 (노무팀장, 일반노무자): 소셜 로그인 (카카오, 네이버)
 */
export default function LoginSelect() {
  return (
    <PublicLayout>
      <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
        <Card className="w-full max-w-md shadow-lg">
          <CardHeader className="text-center space-y-2">
            <CardTitle className="text-2xl font-bold">로그인 방식 선택</CardTitle>
            <CardDescription>
              서비스 이용을 위해 로그인 방식을 선택해주세요.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* 관리자 로그인 (사업자번호) */}
            <Link href="/login/hq">
              <Button variant="outline" className="w-full h-16 justify-start px-6 text-lg hover:border-secondary hover:bg-secondary/5 transition-all group mb-4">
                <div className="h-10 w-10 rounded-full bg-secondary/10 flex items-center justify-center mr-4 group-hover:bg-secondary group-hover:text-secondary-foreground transition-colors">
                  <Building2 className="h-5 w-5" />
                </div>
                <div className="flex flex-col items-start">
                  <span className="font-semibold">관리자 로그인</span>
                  <span className="text-xs text-muted-foreground font-normal">사업자번호로 로그인 (슈퍼/본사/현장관리자)</span>
                </div>
              </Button>
            </Link>

            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background px-2 text-muted-foreground">
                  또는
                </span>
              </div>
            </div>

            {/* 개인사용자 소셜 로그인 */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
                <Users className="h-4 w-4" />
                <span>개인사용자 (노무팀장, 일반노무자)</span>
              </div>
              
              <Link href="/login/social?provider=kakao">
                <Button className="w-full h-12 bg-[#FEE500] text-[#000000] hover:bg-[#FEE500]/90 border-none">
                  <MessageCircle className="mr-2 h-5 w-5 fill-current" />
                  카카오로 시작하기
                </Button>
              </Link>
              
              <Link href="/login/social?provider=naver">
                <Button className="w-full h-12 bg-[#03C75A] text-white hover:bg-[#03C75A]/90 border-none">
                  <span className="mr-2 font-bold text-lg">N</span>
                  네이버로 시작하기
                </Button>
              </Link>
            </div>

            <div className="text-center text-sm text-muted-foreground mt-6">
              아직 계정이 없으신가요?{" "}
              <Link href="/subscribe">
                <a className="text-secondary hover:underline font-medium">구독 신청하기</a>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </PublicLayout>
  );
}

