import { Button } from "@/components/ui/button";
import { Link } from "wouter";

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen flex flex-col bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur">
        <div className="container flex h-16 items-center justify-between">
          <div className="flex items-center gap-8">
            <Link href="/">
              <a className="flex items-center gap-2 font-bold text-xl text-primary">
                <div className="w-8 h-8 bg-primary text-primary-foreground flex items-center justify-center rounded-md">S</div>
                SmartCON Lite
              </a>
            </Link>
            
            <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-muted-foreground">
              <Link href="/#features"><a className="hover:text-foreground transition-colors">주요 기능</a></Link>
              <Link href="/#pricing"><a className="hover:text-foreground transition-colors">요금제</a></Link>
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <Link href="/login">
              <Button variant="ghost" size="sm">로그인</Button>
            </Link>
            <Link href="/subscribe">
              <Button size="sm">무료 체험 시작</Button>
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        {children}
      </main>

      {/* Footer */}
      <footer className="border-t bg-muted/30">
        <div className="container py-10">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="space-y-4">
              <div className="flex items-center gap-2 font-bold text-lg text-primary">
                <div className="w-6 h-6 bg-primary text-primary-foreground flex items-center justify-center rounded-sm text-xs">S</div>
                SmartCON Lite
              </div>
              <p className="text-sm text-muted-foreground">
                건설 현장의 모든 것을 스마트하게 연결하다.<br/>
                SaaS 기반 건설 현장 인력 관리 솔루션
              </p>
            </div>
            
            <div>
              <h4 className="font-semibold mb-4">서비스</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><a href="#" className="hover:text-foreground">주요 기능</a></li>
                <li><a href="#" className="hover:text-foreground">요금제 안내</a></li>
                <li><a href="#" className="hover:text-foreground">도입 문의</a></li>
              </ul>
            </div>

            <div>
              <h4 className="font-semibold mb-4">고객 지원</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><a href="#" className="hover:text-foreground">공지사항</a></li>
                <li><a href="#" className="hover:text-foreground">자주 묻는 질문</a></li>
                <li><a href="#" className="hover:text-foreground">이용약관</a></li>
                <li><a href="#" className="hover:text-foreground">개인정보처리방침</a></li>
              </ul>
            </div>

            <div>
              <h4 className="font-semibold mb-4">Contact</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>1588-0000</li>
                <li>support@smartcon.net</li>
                <li>서울시 강남구 테헤란로 123</li>
              </ul>
            </div>
          </div>
          <div className="mt-10 pt-6 border-t text-center text-xs text-muted-foreground">
            © 2025 SmartCON Inc. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}

