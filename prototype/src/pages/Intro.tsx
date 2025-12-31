import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CheckCircle2, BarChart3, Users, ShieldCheck, ArrowRight } from "lucide-react";
import { Link } from "wouter";

export default function Intro() {
  return (
    <PublicLayout>
      {/* Hero Section */}
      <section className="relative py-20 lg:py-32 overflow-hidden">
        <div className="container relative z-10">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="space-y-8">
              <div className="inline-flex items-center rounded-full border px-4 py-1.5 text-sm font-semibold text-primary bg-primary/5">
                <span className="flex h-2 w-2 rounded-full bg-primary mr-2"></span>
                SaaS 기반 건설 현장 관리 솔루션
              </div>
              <h1 className="text-4xl lg:text-6xl font-bold tracking-tight leading-tight">
                건설 현장의 모든 것을<br />
                <span className="text-primary">스마트하게 연결하다</span>
              </h1>
              <p className="text-xl text-muted-foreground leading-relaxed max-w-lg">
                복잡한 현장 관리를 하나의 플랫폼으로. 구독형 서비스로 초기 비용 부담 없이, 
                실시간 모니터링과 자동화된 정산 시스템을 경험하세요.
              </p>
              <div className="flex flex-col sm:flex-row gap-4">
                <Link href="/subscribe">
                  <Button size="lg" className="text-lg px-8 h-14">
                    구독 신청하기 <ArrowRight className="ml-2 h-5 w-5" />
                  </Button>
                </Link>
                <Button variant="outline" size="lg" className="text-lg px-8 h-14">
                  제안서 다운로드
                </Button>
              </div>
            </div>
            
            {/* Hero Image / Dashboard Preview */}
            <div className="relative rounded-xl border bg-background shadow-2xl p-2 lg:p-4 rotate-1 hover:rotate-0 transition-transform duration-500">
              <div className="rounded-lg bg-muted/50 aspect-[16/10] flex items-center justify-center overflow-hidden relative">
                {/* Mock UI Elements */}
                <div className="absolute inset-0 bg-gradient-to-br from-background to-muted p-6 flex flex-col gap-4">
                  <div className="flex gap-4">
                    <div className="w-1/3 h-32 bg-white rounded-lg shadow-sm p-4 space-y-2">
                      <div className="h-4 w-20 bg-muted rounded"></div>
                      <div className="h-8 w-12 bg-primary/20 rounded"></div>
                    </div>
                    <div className="w-1/3 h-32 bg-white rounded-lg shadow-sm p-4 space-y-2">
                      <div className="h-4 w-20 bg-muted rounded"></div>
                      <div className="h-8 w-12 bg-orange-100 rounded"></div>
                    </div>
                    <div className="w-1/3 h-32 bg-white rounded-lg shadow-sm p-4 space-y-2">
                      <div className="h-4 w-20 bg-muted rounded"></div>
                      <div className="h-8 w-12 bg-blue-100 rounded"></div>
                    </div>
                  </div>
                  <div className="flex-1 bg-white rounded-lg shadow-sm p-4 flex gap-4">
                    <div className="w-1/4 bg-muted/30 rounded h-full"></div>
                    <div className="flex-1 space-y-3">
                      <div className="h-4 w-1/3 bg-muted rounded"></div>
                      <div className="h-4 w-full bg-muted/50 rounded"></div>
                      <div className="h-4 w-full bg-muted/50 rounded"></div>
                      <div className="h-4 w-2/3 bg-muted/50 rounded"></div>
                    </div>
                  </div>
                </div>
                
                {/* Floating Badge */}
                <div className="absolute bottom-8 right-8 bg-white p-4 rounded-lg shadow-lg border">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                      <CheckCircle2 className="h-6 w-6" />
                    </div>
                    <div>
                      <div className="text-sm font-semibold">실시간 출역 마감</div>
                      <div className="text-xs text-muted-foreground">방금 전 완료</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Background Decoration */}
        <div className="absolute top-0 right-0 -z-10 w-1/2 h-full bg-gradient-to-l from-primary/5 to-transparent"></div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-muted/30">
        <div className="container">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-3xl font-bold mb-4">현장 관리의 새로운 기준</h2>
            <p className="text-muted-foreground">
              본사 관리자부터 현장 노무자까지, 모든 이해관계자를 위한 최적화된 기능을 제공합니다.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <Card className="border-none shadow-lg">
              <CardHeader>
                <div className="w-12 h-12 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-4">
                  <BarChart3 className="h-6 w-6" />
                </div>
                <CardTitle>실시간 통합 모니터링</CardTitle>
              </CardHeader>
              <CardContent className="text-muted-foreground">
                전국 현장의 공정률, 출역 현황, 예산 집행 내역을 한눈에 파악하고 데이터 기반의 의사결정을 지원합니다.
              </CardContent>
            </Card>

            <Card className="border-none shadow-lg">
              <CardHeader>
                <div className="w-12 h-12 rounded-lg bg-green-100 text-green-600 flex items-center justify-center mb-4">
                  <ShieldCheck className="h-6 w-6" />
                </div>
                <CardTitle>안면인식 출역 관리</CardTitle>
              </CardHeader>
              <CardContent className="text-muted-foreground">
                AI 안면인식 기술로 대리 출석을 방지하고, 정확한 출퇴근 기록을 기반으로 노무비를 자동 정산합니다.
              </CardContent>
            </Card>

            <Card className="border-none shadow-lg">
              <CardHeader>
                <div className="w-12 h-12 rounded-lg bg-orange-100 text-orange-600 flex items-center justify-center mb-4">
                  <Users className="h-6 w-6" />
                </div>
                <CardTitle>간편한 인력 소싱</CardTitle>
              </CardHeader>
              <CardContent className="text-muted-foreground">
                검증된 노무 팀장과 전문 인력을 쉽고 빠르게 매칭하고, 전자근로계약까지 원스톱으로 처리합니다.
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20">
        <div className="container">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-3xl font-bold mb-4">합리적인 요금제</h2>
            <p className="text-muted-foreground">
              현장 규모와 필요 기능에 맞춰 최적의 요금제를 선택하세요.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {/* Basic Plan */}
            <Card>
              <CardHeader>
                <CardTitle className="text-xl">Basic</CardTitle>
                <div className="text-3xl font-bold mt-4">₩ 100,000 <span className="text-sm font-normal text-muted-foreground">/ 월</span></div>
                <p className="text-sm text-muted-foreground mt-2">소규모 현장 관리용</p>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm mb-6">
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 현장 1개 관리</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 관리자 2명</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 출역 관리 기본</li>
                </ul>
                <Button className="w-full" variant="outline">선택하기</Button>
              </CardContent>
            </Card>

            {/* Pro Plan */}
            <Card className="border-secondary shadow-xl relative overflow-hidden">
              <div className="absolute top-0 right-0 bg-secondary text-secondary-foreground text-xs px-3 py-1 rounded-bl-lg font-medium">
                인기
              </div>
              <CardHeader>
                <CardTitle className="text-xl text-secondary">Pro</CardTitle>
                <div className="text-3xl font-bold mt-4">₩ 300,000 <span className="text-sm font-normal text-muted-foreground">/ 월</span></div>
                <p className="text-sm text-muted-foreground mt-2">중형 현장 및 전문건설사용</p>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm mb-6">
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 현장 5개 관리</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 관리자 10명</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 안면인식 연동</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 전자근로계약 무제한</li>
                </ul>
                <Button className="w-full">선택하기</Button>
              </CardContent>
            </Card>

            {/* Enterprise Plan */}
            <Card>
              <CardHeader>
                <CardTitle className="text-xl">Enterprise</CardTitle>
                <div className="text-3xl font-bold mt-4">문의 <span className="text-sm font-normal text-muted-foreground">/ 별도 협의</span></div>
                <p className="text-sm text-muted-foreground mt-2">대형 건설사 및 공공기관용</p>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm mb-6">
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 현장 무제한</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 관리자 무제한</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> ERP 연동 지원</li>
                  <li className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-secondary" /> 전담 기술 지원</li>
                </ul>
                <Button className="w-full" variant="outline">문의하기</Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>
    </PublicLayout>
  );
}



