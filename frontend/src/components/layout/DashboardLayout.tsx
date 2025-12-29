import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { cn } from "@/lib/utils";
import {
  Building2,
  ChevronLeft,
  CreditCard,
  FileText,
  HardHat,
  LogOut,
  Menu,
  Settings,
  Users,
  UserCircle2,
  Bell,
  Home,
  CheckCircle
} from "lucide-react";
import { useState, useEffect } from "react";
import { Link, useLocation } from "wouter";

interface SidebarItem {
  icon: React.ElementType;
  label: string;
  href: string;
}

interface DashboardLayoutProps {
  children: React.ReactNode;
  role: "hq" | "site" | "worker" | "super";
}

export default function DashboardLayout({ children, role }: DashboardLayoutProps) {
  const [location] = useLocation();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [isMobile, setIsMobile] = useState(false);

  // Check for mobile viewport
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
      if (window.innerWidth < 1024) {
        setIsSidebarOpen(false);
      } else {
        setIsSidebarOpen(true);
      }
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  const getMenuItems = (role: string): SidebarItem[] => {
    switch (role) {
      case "hq":
        return [
          { icon: Building2, label: "현장 관리", href: "/hq/sites" },
          { icon: Users, label: "노무자 관리", href: "/hq/workers" },
          { icon: FileText, label: "계약 관리", href: "/hq/contracts" },
          { icon: CreditCard, label: "정산 관리", href: "/hq/settlements" },
          { icon: Settings, label: "설정", href: "/hq/settings" },
        ];
      case "site":
        return [
          { icon: Users, label: "출역 관리", href: "/site/attendance" },
          { icon: FileText, label: "작업 일보", href: "/site/reports" },
          { icon: FileText, label: "계약 관리", href: "/site/contracts" },
          { icon: HardHat, label: "팀/노무자", href: "/site/teams" },
          { icon: CreditCard, label: "급여/정산", href: "/site/salary" },
        ];
      case "worker":
        return [
          { icon: Users, label: "출역 조회", href: "/worker/attendance" },
          { icon: FileText, label: "전자 계약", href: "/worker/contracts" },
          { icon: UserCircle2, label: "내 정보", href: "/worker/profile" },
        ];
      case "super":
        return [
          { icon: Building2, label: "고객사(Tenant)", href: "/super/tenants" },
          { icon: CheckCircle, label: "구독 승인", href: "/super/approvals" },
          { icon: CreditCard, label: "구독/결제", href: "/super/billing" },
          { icon: FileText, label: "세금계산서", href: "/super/tax" },
          { icon: Settings, label: "시스템 설정", href: "/super/settings" },
        ];
      default:
        return [];
    }
  };

  const menuItems = getMenuItems(role);

  // Mobile Bottom Navigation
  const MobileNav = () => (
    <div className="md:hidden fixed bottom-0 left-0 right-0 bg-background border-t z-50 flex justify-around items-center h-16 px-2">
      <Link href={`/${role}/dashboard`}>
        <a className={cn(
          "flex flex-col items-center justify-center w-full h-full space-y-1",
          location === `/${role}/dashboard` ? "text-secondary" : "text-muted-foreground"
        )}>
          <Home className="h-5 w-5" />
          <span className="text-[10px] font-medium">홈</span>
        </a>
      </Link>
      {menuItems.slice(0, 3).map((item) => (
        <Link key={item.href} href={item.href}>
          <a className={cn(
            "flex flex-col items-center justify-center w-full h-full space-y-1",
            location === item.href ? "text-secondary" : "text-muted-foreground"
          )}>
            <item.icon className="h-5 w-5" />
            <span className="text-[10px] font-medium">{item.label}</span>
          </a>
        </Link>
      ))}
      <Sheet>
        <SheetTrigger asChild>
          <button className="flex flex-col items-center justify-center w-full h-full space-y-1 text-muted-foreground">
            <Menu className="h-5 w-5" />
            <span className="text-[10px] font-medium">전체</span>
          </button>
        </SheetTrigger>
        <SheetContent side="right" className="w-[80%] sm:w-[385px]">
          <div className="flex flex-col h-full py-6">
            <div className="flex items-center gap-3 mb-8 px-2">
              <div className="h-10 w-10 rounded-full bg-secondary/10 flex items-center justify-center text-secondary font-bold text-lg">
                홍
              </div>
              <div>
                <div className="font-bold text-lg">홍길동 관리자</div>
                <div className="text-sm text-muted-foreground">주식회사 스마트콘</div>
              </div>
            </div>
            <nav className="space-y-2 flex-1">
              <Link href={`/${role}/dashboard`}>
                <a className={cn(
                  "flex items-center gap-3 px-4 py-3 rounded-md text-base font-medium transition-colors",
                  location === `/${role}/dashboard` ? "bg-accent text-accent-foreground" : "hover:bg-accent/50"
                )}>
                  <Home className="h-5 w-5" />
                  홈
                </a>
              </Link>
              {menuItems.map((item) => (
                <Link key={item.href} href={item.href}>
                  <a className={cn(
                    "flex items-center gap-3 px-4 py-3 rounded-md text-base font-medium transition-colors",
                    location === item.href ? "bg-accent text-accent-foreground" : "hover:bg-accent/50"
                  )}>
                    <item.icon className="h-5 w-5" />
                    {item.label}
                  </a>
                </Link>
              ))}
            </nav>
            <div className="border-t pt-4 mt-auto">
              <Link href="/login">
                <a className="flex items-center gap-3 px-4 py-3 rounded-md text-base font-medium text-destructive hover:bg-destructive/10 transition-colors">
                  <LogOut className="h-5 w-5" />
                  로그아웃
                </a>
              </Link>
            </div>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );

  return (
    <div className="min-h-screen flex bg-muted/10">
      {/* Desktop Sidebar */}
      <aside
        className={cn(
          "hidden md:flex fixed inset-y-0 left-0 z-50 bg-sidebar text-sidebar-foreground border-r border-sidebar-border transition-all duration-300 flex-col",
          isSidebarOpen ? "w-64" : "w-16"
        )}
      >
        <div className="h-16 flex items-center px-4 border-b border-sidebar-border/50">
          <Link href={`/${role}/dashboard`}>
            <a className={cn("flex items-center gap-2 font-bold text-xl text-sidebar-foreground transition-opacity", !isSidebarOpen && "opacity-0 w-0 overflow-hidden")}>
              <div className="w-8 h-8 bg-secondary text-secondary-foreground flex items-center justify-center rounded-md shrink-0">S</div>
              <span className="whitespace-nowrap">SmartCON</span>
            </a>
          </Link>
          {!isSidebarOpen && (
             <div className="w-8 h-8 bg-secondary text-secondary-foreground flex items-center justify-center rounded-md shrink-0 mx-auto">S</div>
          )}
        </div>

        <ScrollArea className="flex-1 py-4">
          <nav className="space-y-1 px-2">
            {menuItems.map((item) => (
              <Link key={item.href} href={item.href}>
                <a
                  className={cn(
                    "flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors",
                    location === item.href
                      ? "bg-sidebar-accent text-sidebar-accent-foreground"
                      : "text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground",
                    !isSidebarOpen && "justify-center px-2"
                  )}
                  title={!isSidebarOpen ? item.label : undefined}
                >
                  <item.icon className="h-5 w-5 shrink-0" />
                  {isSidebarOpen && <span>{item.label}</span>}
                </a>
              </Link>
            ))}
          </nav>
        </ScrollArea>

        <div className="p-4 border-t border-sidebar-border/50">
          <Button
            variant="ghost"
            className={cn("w-full justify-start text-sidebar-foreground/70 hover:text-sidebar-foreground hover:bg-sidebar-accent/50", !isSidebarOpen && "justify-center px-0")}
            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
          >
            {isSidebarOpen ? (
              <>
                <ChevronLeft className="h-5 w-5 mr-2" />
                <span>접기</span>
              </>
            ) : (
              <Menu className="h-5 w-5" />
            )}
          </Button>
          <Link href="/login">
            <Button
              variant="ghost"
              className={cn("w-full justify-start text-destructive hover:text-destructive hover:bg-destructive/10 mt-2", !isSidebarOpen && "justify-center px-0")}
            >
              <LogOut className="h-5 w-5 shrink-0" />
              {isSidebarOpen && <span className="ml-2">로그아웃</span>}
            </Button>
          </Link>
        </div>
      </aside>

      {/* Main Content */}
      <div className={cn(
        "flex-1 flex flex-col transition-all duration-300 min-h-screen",
        !isMobile && isSidebarOpen ? "ml-64" : !isMobile ? "ml-16" : "ml-0",
        "pb-16 md:pb-0"
      )}>
        <header className="h-14 md:h-16 border-b bg-background/95 backdrop-blur px-4 md:px-6 flex items-center justify-between sticky top-0 z-40">
          <div className="flex items-center gap-3">
            {isMobile && (
              <div className="w-8 h-8 bg-secondary text-secondary-foreground flex items-center justify-center rounded-md shrink-0">S</div>
            )}
            <h1 className="text-lg font-semibold truncate max-w-[200px] md:max-w-none">
              {menuItems.find((item) => item.href === location)?.label || "Dashboard"}
            </h1>
          </div>
          <div className="flex items-center gap-2 md:gap-4">
            <Button variant="ghost" size="icon" className="relative">
              <Bell className="h-5 w-5" />
              <span className="absolute top-2 right-2 h-2 w-2 bg-destructive rounded-full"></span>
            </Button>
            <div className="hidden md:flex items-center gap-3 pl-4 border-l">
              <div className="text-right">
                <div className="text-sm font-medium">홍길동 관리자</div>
                <div className="text-xs text-muted-foreground">주식회사 스마트콘</div>
              </div>
              <div className="h-9 w-9 rounded-full bg-secondary/10 flex items-center justify-center text-secondary font-medium">
                홍
              </div>
            </div>
          </div>
        </header>
        <main className="flex-1 p-4 md:p-6 overflow-auto w-full">
          <div className="container max-w-7xl mx-auto p-0">
            {children}
          </div>
        </main>
      </div>

      {/* Mobile Navigation */}
      <MobileNav />
    </div>
  );
}

