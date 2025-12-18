import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowRight, BarChart3, CheckCircle2, FileText, LayoutDashboard, ShieldCheck, Users } from "lucide-react";
import { Link } from "wouter";

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      {/* Header / Navigation */}
      <header className="border-b border-border sticky top-0 z-50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-16 items-center justify-between">
          <div className="flex items-center gap-2 font-bold text-xl tracking-tighter text-primary">
            <div className="w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center rounded-sm">S</div>
            SmartCON Lite
          </div>
          <nav className="hidden md:flex items-center gap-6 text-sm font-medium">
            <a href="#features" className="hover:text-primary transition-colors">주요 기능</a>
            <a href="#process" className="hover:text-primary transition-colors">구독 프로세스</a>
            <a href="#monitoring" className="hover:text-primary transition-colors">운영 모니터링</a>
            <a href="#pricing" className="hover:text-primary transition-colors">요금제</a>
          </nav>
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="sm" className="hidden sm:flex">로그인</Button>
            <Button size="sm" className="bg-primary text-primary-foreground hover:bg-primary/90">무료 체험 시작</Button>
          </div>
        </div>
      </header>

      <main className="flex-1">
        {/* Hero Section */}
        <section className="py-20 md:py-32 border-b border-border bg-secondary/30">
          <div className="container swiss-grid items-center">
            <div className="col-span-1 md:col-span-7 space-y-6">
              <div className="inline-flex items-center rounded-full border border-primary/20 bg-primary/5 px-3 py-1 text-sm font-medium text-primary">
                <span className="flex h-2 w-2 rounded-full bg-primary mr-2"></span>
                SaaS 기반 건설 현장 관리 솔루션
              </div>
              <h1 className="text-4xl md:text-6xl font-black tracking-tight leading-[1.1]">
                건설 현장의 모든 것을 <br/>
                <span className="text-primary">스마트하게 연결하다</span>
              </h1>
              <p className="text-xl text-muted-foreground max-w-[600px] leading-relaxed">
                복잡한 현장 관리를 하나의 플랫폼으로. 구독형 서비스로 초기 비용 부담 없이, 
                실시간 모니터링과 자동화된 정산 시스템을 경험하세요.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 pt-4">
                <Button size="lg" className="text-base px-8 h-12 bg-primary hover:bg-primary/90">
                  구독 신청하기 <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
                <Button size="lg" variant="outline" className="text-base px-8 h-12 border-2">
                  제안서 다운로드
                </Button>
              </div>
            </div>
            <div className="col-span-1 md:col-span-5 mt-10 md:mt-0 relative">
              <div className="aspect-square md:aspect-[4/3] bg-gradient-to-br from-primary/10 to-accent/10 rounded-lg border border-border p-8 flex items-center justify-center relative overflow-hidden">
                <div className="absolute inset-0 grid grid-cols-6 grid-rows-6 gap-1 opacity-20">
                    {Array.from({ length: 36 }).map((_, i) => (
                        <div key={i} className="border border-primary/30"></div>
                    ))}
                </div>
                <div className="relative z-10 bg-card shadow-xl border border-border p-6 rounded-md w-full max-w-sm">
                    <div className="flex items-center justify-between mb-6 border-b border-border pb-4">
                        <div className="font-bold text-lg">현장 현황 대시보드</div>
                        <div className="text-xs text-muted-foreground">2025.12.17 기준</div>
                    </div>
                    <div className="space-y-4">
                        <div className="flex justify-between items-center">
                            <span className="text-sm text-muted-foreground">가동 현장</span>
                            <span className="font-bold text-primary">12개</span>
                        </div>
                        <div className="h-2 bg-secondary rounded-full overflow-hidden">
                            <div className="h-full bg-primary w-[75%]"></div>
                        </div>
                        <div className="flex justify-between items-center pt-2">
                            <span className="text-sm text-muted-foreground">금일 출력 인원</span>
                            <span className="font-bold">145명</span>
                        </div>
                        <div className="h-2 bg-secondary rounded-full overflow-hidden">
                            <div className="h-full bg-accent w-[60%]"></div>
                        </div>
                    </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Key Features */}
        <section id="features" className="py-20 md:py-32 container">
          <div className="mb-16 md:mb-24">
            <h2 className="text-3xl md:text-5xl font-bold mb-6">핵심 기능</h2>
            <div className="w-24 h-2 bg-accent"></div>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card className="swiss-card border-t-4 border-t-primary">
              <CardHeader>
                <FileText className="h-10 w-10 text-primary mb-4" />
                <CardTitle className="text-2xl">간편한 구독 신청</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base">
                  사업자번호 인증만으로 즉시 가입. 법인, 개인사업자 모두 지원하며 복잡한 서류 절차 없이 
                  온라인으로 모든 계약이 완료됩니다.
                </CardDescription>
              </CardContent>
            </Card>

            <Card className="swiss-card border-t-4 border-t-primary">
              <CardHeader>
                <LayoutDashboard className="h-10 w-10 text-primary mb-4" />
                <CardTitle className="text-2xl">통합 모니터링</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base">
                  전국 현장의 데이터를 한눈에. 실시간 인력 현황, 장비 가동률, 안전 이슈를 
                  직관적인 대시보드로 제공합니다.
                </CardDescription>
              </CardContent>
            </Card>

            <Card className="swiss-card border-t-4 border-t-primary">
              <CardHeader>
                <ShieldCheck className="h-10 w-10 text-primary mb-4" />
                <CardTitle className="text-2xl">자동화된 정산</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base">
                  매월 사용량에 따른 자동 과금 및 세금계산서 발행. 홈택스 연동으로 
                  세무 처리의 번거로움을 획기적으로 줄였습니다.
                </CardDescription>
              </CardContent>
            </Card>
          </div>
        </section>

        {/* Subscription Process */}
        <section id="process" className="py-20 md:py-32 bg-secondary/50 border-y border-border">
          <div className="container">
            <div className="flex flex-col md:flex-row gap-16">
              <div className="md:w-1/3">
                <h2 className="text-3xl md:text-5xl font-bold mb-6">구독 프로세스</h2>
                <div className="w-24 h-2 bg-accent mb-8"></div>
                <p className="text-lg text-muted-foreground">
                  복잡한 절차는 줄이고, 꼭 필요한 검증만 남겼습니다. 
                  단 4단계로 스마트콘 라이트를 시작해보세요.
                </p>
              </div>
              
              <div className="md:w-2/3 grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div className="bg-background p-6 border border-border relative">
                  <div className="absolute -top-3 -left-3 w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center font-bold text-lg">1</div>
                  <h3 className="text-xl font-bold mb-2 mt-2">사업자 인증</h3>
                  <p className="text-muted-foreground">국세청 API 연동을 통해 사업자번호 및 상태(휴/폐업)를 실시간으로 검증합니다.</p>
                </div>
                <div className="bg-background p-6 border border-border relative">
                  <div className="absolute -top-3 -left-3 w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center font-bold text-lg">2</div>
                  <h3 className="text-xl font-bold mb-2 mt-2">계정 생성</h3>
                  <p className="text-muted-foreground">인증된 사업자번호를 ID로 사용하여 보안성이 강화된 기업 계정을 생성합니다.</p>
                </div>
                <div className="bg-background p-6 border border-border relative">
                  <div className="absolute -top-3 -left-3 w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center font-bold text-lg">3</div>
                  <h3 className="text-xl font-bold mb-2 mt-2">요금제 선택</h3>
                  <p className="text-muted-foreground">현장 규모와 사용자 수에 맞는 최적의 요금제를 선택하고 결제 수단을 등록합니다.</p>
                </div>
                <div className="bg-background p-6 border border-border relative">
                  <div className="absolute -top-3 -left-3 w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center font-bold text-lg">4</div>
                  <h3 className="text-xl font-bold mb-2 mt-2">서비스 시작</h3>
                  <p className="text-muted-foreground">즉시 워크스페이스가 생성되며, 관리자 대시보드 접근 권한이 부여됩니다.</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Monitoring & Admin */}
        <section id="monitoring" className="py-20 md:py-32 container">
          <div className="swiss-grid items-center">
            <div className="col-span-1 md:col-span-6 order-2 md:order-1">
               <div className="bg-card border border-border shadow-lg p-1 rounded-lg">
                 <div className="bg-secondary/20 p-8 rounded border border-border/50">
                    {/* Abstract UI Representation */}
                    <div className="flex gap-4 mb-6">
                        <div className="w-1/4 h-32 bg-background border border-border p-4">
                            <div className="w-8 h-8 bg-primary/10 rounded mb-2"></div>
                            <div className="h-4 w-16 bg-secondary mb-2"></div>
                            <div className="h-6 w-12 bg-primary/20"></div>
                        </div>
                        <div className="w-1/4 h-32 bg-background border border-border p-4">
                            <div className="w-8 h-8 bg-accent/10 rounded mb-2"></div>
                            <div className="h-4 w-16 bg-secondary mb-2"></div>
                            <div className="h-6 w-12 bg-accent/20"></div>
                        </div>
                        <div className="w-2/4 h-32 bg-background border border-border p-4">
                            <div className="h-4 w-24 bg-secondary mb-4"></div>
                            <div className="flex items-end gap-2 h-16">
                                <div className="w-4 h-[40%] bg-primary"></div>
                                <div className="w-4 h-[60%] bg-primary"></div>
                                <div className="w-4 h-[80%] bg-primary"></div>
                                <div className="w-4 h-[50%] bg-primary"></div>
                                <div className="w-4 h-[90%] bg-primary"></div>
                            </div>
                        </div>
                    </div>
                    <div className="space-y-2">
                        <div className="h-10 bg-background border border-border w-full flex items-center px-4 justify-between">
                            <div className="h-3 w-24 bg-secondary"></div>
                            <div className="h-3 w-12 bg-secondary"></div>
                        </div>
                        <div className="h-10 bg-background border border-border w-full flex items-center px-4 justify-between">
                            <div className="h-3 w-32 bg-secondary"></div>
                            <div className="h-3 w-12 bg-secondary"></div>
                        </div>
                        <div className="h-10 bg-background border border-border w-full flex items-center px-4 justify-between">
                            <div className="h-3 w-20 bg-secondary"></div>
                            <div className="h-3 w-12 bg-secondary"></div>
                        </div>
                    </div>
                 </div>
               </div>
            </div>
            <div className="col-span-1 md:col-span-6 order-1 md:order-2 md:pl-12 mb-10 md:mb-0">
              <h2 className="text-3xl md:text-5xl font-bold mb-6">운영자를 위한<br/>강력한 모니터링</h2>
              <div className="w-24 h-2 bg-accent mb-8"></div>
              <p className="text-lg text-muted-foreground mb-8">
                서비스 운영사는 전체 고객사의 현황을 실시간으로 파악할 수 있습니다. 
                과금 현황, 시스템 부하, 고객 문의까지 한 곳에서 관리하세요.
              </p>
              <ul className="space-y-4">
                <li className="flex items-start">
                    <CheckCircle2 className="h-6 w-6 text-primary mr-3 mt-0.5" />
                    <div>
                        <strong className="block text-foreground">자동 과금 및 계산서 발행</strong>
                        <span className="text-muted-foreground">매월 정해진 날짜에 자동 결제 및 국세청 연동 세금계산서 발행</span>
                    </div>
                </li>
                <li className="flex items-start">
                    <CheckCircle2 className="h-6 w-6 text-primary mr-3 mt-0.5" />
                    <div>
                        <strong className="block text-foreground">계층형 관리자 권한</strong>
                        <span className="text-muted-foreground">Super Admin부터 Viewer까지 세분화된 권한 관리 (RBAC)</span>
                    </div>
                </li>
                <li className="flex items-start">
                    <CheckCircle2 className="h-6 w-6 text-primary mr-3 mt-0.5" />
                    <div>
                        <strong className="block text-foreground">실시간 리소스 모니터링</strong>
                        <span className="text-muted-foreground">서버 부하 및 API 호출량을 실시간으로 추적하여 안정성 확보</span>
                    </div>
                </li>
              </ul>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-20 bg-primary text-primary-foreground">
          <div className="container text-center">
            <h2 className="text-3xl md:text-5xl font-bold mb-6">지금 바로 시작하세요</h2>
            <p className="text-xl opacity-90 mb-10 max-w-2xl mx-auto">
              스마트콘 라이트는 건설 현장의 디지털 전환을 위한 가장 빠르고 확실한 선택입니다.
              30일 무료 체험으로 그 효과를 직접 확인해보세요.
            </p>
            <Button size="lg" variant="secondary" className="text-primary font-bold text-lg px-10 h-14">
              무료 체험 신청하기
            </Button>
          </div>
        </section>
      </main>

      <footer className="bg-secondary py-12 border-t border-border">
        <div className="container grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center gap-2 font-bold text-xl tracking-tighter text-primary mb-4">
              <div className="w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center rounded-sm">S</div>
              SmartCON Lite
            </div>
            <p className="text-muted-foreground max-w-xs">
              건설 현장 관리의 새로운 표준.<br/>
              더 안전하고 효율적인 현장을 만듭니다.
            </p>
          </div>
          <div>
            <h4 className="font-bold mb-4">Product</h4>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><a href="#" className="hover:text-primary">기능 소개</a></li>
              <li><a href="#" className="hover:text-primary">요금제</a></li>
              <li><a href="#" className="hover:text-primary">도입 사례</a></li>
              <li><a href="#" className="hover:text-primary">업데이트 로그</a></li>
            </ul>
          </div>
          <div>
            <h4 className="font-bold mb-4">Company</h4>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><a href="#" className="hover:text-primary">회사 소개</a></li>
              <li><a href="#" className="hover:text-primary">채용 정보</a></li>
              <li><a href="#" className="hover:text-primary">문의하기</a></li>
              <li><a href="#" className="hover:text-primary">개인정보처리방침</a></li>
            </ul>
          </div>
        </div>
        <div className="container mt-12 pt-8 border-t border-border/50 text-center text-sm text-muted-foreground">
          © 2025 SmartCON Lite. All rights reserved.
        </div>
      </footer>
    </div>
  );
}
