import { useState, useEffect } from 'react';
import { Routes, Route, useNavigate, Navigate, useLocation } from 'react-router-dom';
import { Button, Card, CardHeader, CardTitle, CardContent, Input, Badge } from './components/ui-basic';
import { USERS } from './data/mockData';
import {
  Briefcase,
  Users,
  FileText,
  DollarSign,
  Settings,
  Menu,
  Bell,
  LogOut,
  Home,
  Calendar,
  User as UserIcon,
  ChevronRight,
  ClipboardList
} from 'lucide-react';

// --- Types ---
type User = typeof USERS[0];

// --- Layouts ---

const HQLayout = ({ children }: { children: React.ReactNode }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('currentUser') || '{}');

  const menuItems = [
    { label: '대시보드', icon: Home, path: '/hq/dashboard' },
    { label: '현장 관리', icon: Briefcase, path: '/hq/sites' },
    { label: '노무 관리', icon: Users, path: '/hq/workers' },
    { label: '계약 관리', icon: FileText, path: '/hq/contracts' },
    { label: '정산 관리', icon: DollarSign, path: '/hq/settlements' },
    { label: '설정', icon: Settings, path: '/hq/settings' },
  ];

  return (
    <div className="flex h-screen bg-gray-50">
      {/* LNB */}
      <aside className="w-64 bg-primary text-white flex flex-col">
        <div className="p-6">
          <h1 className="text-2xl font-bold font-display">SmartCON</h1>
          <p className="text-xs text-gray-400 mt-1">Total Management</p>
        </div>
        <nav className="flex-1 px-4 space-y-2">
          {menuItems.map((item) => (
            <div
              key={item.path}
              onClick={() => navigate(item.path)}
              className={`flex items-center gap-3 px-4 py-3 rounded-md cursor-pointer transition-colors ${location.pathname === item.path ? 'bg-secondary text-secondary-foreground font-bold' : 'hover:bg-white/10'
                }`}
            >
              <item.icon size={20} />
              <span>{item.label}</span>
            </div>
          ))}
        </nav>
        <div className="p-4 border-t border-white/10">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gray-300 flex items-center justify-center text-gray-700 font-bold">
              {user.name?.[0]}
            </div>
            <div>
              <div className="font-bold text-sm">{user.name}</div>
              <div className="text-xs text-gray-400">본사 관리자</div>
            </div>
          </div>
          <Button variant="ghost" className="w-full mt-4 justify-start text-red-300 hover:text-red-400 hover:bg-white/5" onClick={() => navigate('/')}>
            <LogOut size={16} className="mr-2" /> 로그아웃
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-16 bg-white shadow-sm flex items-center justify-between px-8">
          <h2 className="text-lg font-bold text-gray-800">{menuItems.find(m => m.path === location.pathname)?.label}</h2>
          <div className="flex items-center gap-4">
            <div className="relative cursor-pointer">
              <Bell size={20} className="text-gray-500" />
              <span className="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </div>
          </div>
        </header>
        <main className="flex-1 overflow-auto p-8">
          {children}
        </main>
      </div>
    </div>
  );
};

const SiteLayout = ({ children }: { children: React.ReactNode }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('currentUser') || '{}');

  const menuItems = [
    { label: '대시보드', icon: Home, path: '/site/dashboard' },
    { label: '출역 관리', icon: Users, path: '/site/attendance' },
    { label: '작업일보', icon: ClipboardList, path: '/site/reports' },
    { label: '계약 관리', icon: FileText, path: '/site/contracts' },
    { label: '팀/노무자', icon: Briefcase, path: '/site/teams' },
    { label: '급여 정산', icon: DollarSign, path: '/site/salary' },
  ];

  return (
    <div className="flex h-screen bg-gray-50">
      {/* LNB */}
      <aside className="w-64 bg-white border-r flex flex-col">
        <div className="p-6">
          <h1 className="text-xl font-bold font-display text-primary">SmartCON <span className="text-secondary">Lite</span></h1>
          <p className="text-xs text-gray-500 mt-1">현장 관리자 모드</p>
        </div>
        <nav className="flex-1 px-4 space-y-1">
          {menuItems.map((item) => (
            <div
              key={item.path}
              onClick={() => navigate(item.path)}
              className={`flex items-center gap-3 px-4 py-3 rounded-md cursor-pointer transition-colors ${location.pathname === item.path
                  ? 'bg-primary/5 text-primary font-bold border-l-4 border-primary'
                  : 'text-gray-600 hover:bg-gray-100'
                }`}
            >
              <item.icon size={20} />
              <span>{item.label}</span>
            </div>
          ))}
        </nav>
        <div className="p-4 border-t">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-8 h-8 rounded-full bg-secondary/20 flex items-center justify-center text-primary font-bold text-xs">
              {user.name?.[0]}
            </div>
            <div className="overflow-hidden">
              <div className="font-bold text-sm truncate">{user.name}</div>
              <div className="text-xs text-gray-500 truncate">{user.siteId === 101 ? '서울 강남 아파트' : '부산 신항만'}</div>
            </div>
          </div>
          <Button variant="outline" className="w-full text-xs" onClick={() => navigate('/')}>
            로그아웃
          </Button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-16 bg-white border-b flex items-center justify-between px-8">
          <h2 className="text-lg font-bold text-gray-800">{menuItems.find(m => m.path === location.pathname)?.label}</h2>
          <div className="flex items-center gap-4">
            <Button size="sm" variant="default">현장 변경</Button>
            <Bell size={20} className="text-gray-500 cursor-pointer" />
          </div>
        </header>
        <main className="flex-1 overflow-auto p-8 bg-gray-50/50">
          {children}
        </main>
      </div>
    </div>
  );
};

const MobileLayout = ({ children, title, back = false }: { children: React.ReactNode, title?: string, back?: boolean }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const tabs = [
    { label: '홈', icon: Home, path: '/worker/dashboard' },
    { label: '출역', icon: Calendar, path: '/worker/attendance' },
    { label: '계약서', icon: FileText, path: '/worker/contracts' },
    { label: '내 정보', icon: UserIcon, path: '/worker/profile' },
  ];

  return (
    <div className="flex flex-col h-screen bg-gray-50 max-w-md mx-auto border-x shadow-2xl relative">
      {/* Header */}
      <header className="h-14 bg-white flex items-center justify-between px-4 border-b sticky top-0 z-10">
        <div className="flex items-center">
          {back && <Button variant="ghost" size="icon" onClick={() => navigate(-1)}><ChevronRight className="rotate-180" /></Button>}
          <h1 className="text-lg font-bold text-primary ml-1">{title || 'SmartCON'}</h1>
        </div>
        <Bell size={20} className="text-gray-400" />
      </header>

      {/* Content */}
      <main className="flex-1 overflow-auto p-4 pb-20">
        {children}
      </main>

      {/* Bottom Tab Bar */}
      <nav className="h-16 bg-white border-t flex items-center justify-around fixed bottom-0 w-full max-w-md">
        {tabs.map((tab) => (
          <div
            key={tab.path}
            className={`flex flex-col items-center justify-center w-full h-full cursor-pointer ${location.pathname === tab.path ? 'text-secondary' : 'text-gray-400'
              }`}
            onClick={() => navigate(tab.path)}
          >
            <tab.icon size={24} strokeWidth={location.pathname === tab.path ? 2.5 : 2} />
            <span className="text-[10px] mt-1 font-medium">{tab.label}</span>
          </div>
        ))}
      </nav>
    </div>
  );
};

// --- Page Components ---

// HQ Pages
function HQDashboard() {
  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">전체 현장</CardTitle></CardHeader>
          <CardContent><div className="text-3xl font-bold font-mono">12</div></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">금일 전체 출역</CardTitle></CardHeader>
          <CardContent className="flex justify-between items-end">
            <div className="text-3xl font-bold font-mono text-secondary-foreground">1,245</div>
            <Badge variant="success" className="mb-1">+4.5%</Badge>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">계약 체결률</CardTitle></CardHeader>
          <CardContent><div className="text-3xl font-bold font-mono text-primary">98.2%</div></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">안전 사고</CardTitle></CardHeader>
          <CardContent><div className="text-3xl font-bold font-mono text-green-500">0</div></CardContent>
        </Card>
      </div>

      {/* Tables */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="col-span-1">
          <CardHeader><CardTitle>현장별 출역 현황</CardTitle></CardHeader>
          <CardContent>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left text-gray-500">
                  <th className="py-2">현장명</th>
                  <th className="py-2 text-right">출역인원</th>
                  <th className="py-2 text-right">공정률</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b">
                  <td className="py-3">서울 강남 아파트 재건축</td>
                  <td className="py-3 text-right font-mono">142</td>
                  <td className="py-3 text-right font-mono">32%</td>
                </tr>
                <tr className="border-b">
                  <td className="py-3">부산 신항만 공사</td>
                  <td className="py-3 text-right font-mono">85</td>
                  <td className="py-3 text-right font-mono">15%</td>
                </tr>
                <tr>
                  <td className="py-3">인천 물류센터 신축</td>
                  <td className="py-3 text-right font-mono">210</td>
                  <td className="py-3 text-right font-mono">68%</td>
                </tr>
              </tbody>
            </table>
          </CardContent>
        </Card>
        <Card className="col-span-1">
          <CardHeader><CardTitle>미서명 계약 현황</CardTitle></CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[1, 2, 3].map(i => (
                <div key={i} className="flex items-center justify-between p-3 bg-red-50 rounded border border-red-100">
                  <div>
                    <div className="font-bold text-sm">김미서 (철근팀)</div>
                    <div className="text-xs text-gray-500">강남 아파트 현장</div>
                  </div>
                  <Button size="sm" variant="destructive" className="h-8">재요청</Button>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

// Site Pages
function SiteDashboard() {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="border-l-4 border-l-secondary">
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium">금일 출역</CardTitle></CardHeader>
          <CardContent>
            <div className="text-4xl font-bold font-mono">142 / 150</div>
            <div className="text-xs text-gray-500 mt-1">예정 대비 94.6% 출근</div>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-destructive">
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium">미확인 로그</CardTitle></CardHeader>
          <CardContent>
            <div className="text-4xl font-bold font-mono text-destructive">5</div>
            <div className="text-xs text-gray-500 mt-1">안면인식 실패 등</div>
          </CardContent>
        </Card>
        <Card className="border-l-4 border-l-warning">
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium">작업일보 미마감</CardTitle></CardHeader>
          <CardContent>
            <div className="text-4xl font-bold font-mono text-warning">2</div>
            <div className="text-xs text-gray-500 mt-1">철근팀, 전기팀</div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>내일 작업 요청 현황</CardTitle>
          <Button>+ 작업 요청</Button>
        </CardHeader>
        <CardContent>
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="p-3 rounded-l">팀명</th>
                <th className="p-3">공종</th>
                <th className="p-3">작업위치</th>
                <th className="p-3">요청인원</th>
                <th className="p-3">상태</th>
                <th className="p-3 rounded-r">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b">
                <td className="p-3 font-medium">철근1팀 (최철근)</td>
                <td className="p-3">철근조립</td>
                <td className="p-3">101동 3층 벽체</td>
                <td className="p-3 font-mono">15</td>
                <td className="p-3"><Badge variant="success">수락됨</Badge></td>
                <td className="p-3"><Button variant="outline" size="sm" className="h-7 text-xs">수정</Button></td>
              </tr>
              <tr className="border-b">
                <td className="p-3 font-medium">거푸집2팀 (정이사)</td>
                <td className="p-3">알폼설치</td>
                <td className="p-3">102동 1층 슬라브</td>
                <td className="p-3 font-mono">12</td>
                <td className="p-3"><Badge variant="warning">대기중</Badge></td>
                <td className="p-3"><Button variant="outline" size="sm" className="h-7 text-xs">수정</Button></td>
              </tr>
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}

// Mobile Pages
function WorkerDashboard() {
  const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
  return (
    <div className="space-y-4">
      <Card className="bg-primary text-white border-none shadow-lg">
        <CardContent className="pt-6">
          <div className="flex justify-between items-start">
            <div>
              <p className="opacity-80 text-sm">반갑습니다,</p>
              <h2 className="text-2xl font-bold">{user.name}님</h2>
            </div>
            <div className="bg-secondary text-primary px-2 py-1 rounded text-xs font-bold">출근완료</div>
          </div>
          <div className="mt-6 flex gap-4">
            <div className="flex-1 bg-white/10 rounded p-3 text-center">
              <div className="text-xs opacity-70">이번 달 공수</div>
              <div className="text-xl font-bold font-mono">14.5</div>
            </div>
            <div className="flex-1 bg-white/10 rounded p-3 text-center">
              <div className="text-xs opacity-70">예상 급여</div>
              <div className="text-xl font-bold font-mono">3.2M</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-2 gap-3">
        <Button variant="outline" className="h-24 flex-col gap-2 bg-white">
          <FileText size={24} className="text-secondary" />
          <span className="text-normal font-bold">근로계약서</span>
        </Button>
        <Button variant="outline" className="h-24 flex-col gap-2 bg-white">
          <Calendar size={24} className="text-secondary" />
          <span className="text-normal font-bold">출역조회</span>
        </Button>
      </div>

      <Card>
        <CardHeader className="pb-2"><CardTitle className="text-sm">최근 공지사항</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          <div className="text-sm border-b pb-2 cursor-pointer">
            <div className="font-bold truncate">동절기 안전보호구 착용 강조</div>
            <div className="text-xs text-gray-500">2025.12.16 | 안전팀</div>
          </div>
          <div className="text-sm border-b pb-2 cursor-pointer">
            <div className="font-bold truncate">설 연휴 작업 일정 안내</div>
            <div className="text-xs text-gray-500">2025.12.15 | 공무팀</div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

// 4. Login Pages (Simplified)
function HQLogin() {
  const navigate = useNavigate();
  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem('currentUser', JSON.stringify(USERS[0])); // Park (HQ)
    navigate('/hq/dashboard');
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <Card className="w-[400px] shadow-xl">
        <CardHeader className="text-center">
          <h1 className="text-xl font-bold font-display text-primary">SmartCON <span className="text-secondary">Admin</span></h1>
          <p className="text-sm text-gray-500">본사 관리자 로그인</p>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleLogin} className="space-y-4">
            <Input placeholder="사업자등록번호 (10자리)" defaultValue="123-45-67890" className="h-12" />
            <Input type="password" placeholder="비밀번호" defaultValue="password" className="h-12" />
            <Button type="submit" className="w-full h-12 text-lg">로그인</Button>
            <div className="text-center">
              <span className="text-sm text-gray-500 cursor-pointer hover:text-primary" onClick={() => navigate('/login/site')}>
                현장 사용자 로그인으로 이동 &rarr;
              </span>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

function SiteLogin() {
  const navigate = useNavigate();
  const handleLogin = (roleIndex: number, path: string) => {
    localStorage.setItem('currentUser', JSON.stringify(USERS[roleIndex]));
    navigate(path);
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <Card className="w-[400px] shadow-xl">
        <CardHeader className="text-center">
          <h1 className="text-2xl font-bold font-display text-primary">SmartCON <span className="text-secondary">Lite</span></h1>
          <p className="text-sm text-gray-500">통합 로그인 (현장/팀장/노무자)</p>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button onClick={() => handleLogin(1, '/site/dashboard')} className="w-full h-12 bg-[#FEE500] text-black hover:bg-[#FEE500]/90 relative">
            <span className="absolute left-4 font-bold">N</span> 카카오로 시작하기 (현장소장)
          </Button>
          <Button onClick={() => handleLogin(4, '/site/dashboard')} className="w-full h-12 bg-[#03C75A] text-white hover:bg-[#03C75A]/90 relative">
            <span className="font-bold absolute left-4">N</span> 네이버로 시작하기 (팀장 - 모바일뷰 가정)
          </Button>
          <Button onClick={() => handleLogin(6, '/worker/dashboard')} className="w-full h-12 bg-black text-white hover:bg-black/90 relative">
            <span className="font-bold absolute left-4"></span> Apple로 시작하기 (노무자)
          </Button>
          <div className="text-center mt-4">
            <span className="text-sm text-gray-500 cursor-pointer hover:text-primary" onClick={() => navigate('/login/hq')}>
              본사 관리자 로그인 &rarr;
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

// --- Main App Route Config ---

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login/hq" replace />} />
      <Route path="/login/hq" element={<HQLogin />} />
      <Route path="/login/site" element={<SiteLogin />} />

      {/* HQ Routes */}
      <Route path="/hq/*" element={
        <HQLayout>
          <Routes>
            <Route path="dashboard" element={<HQDashboard />} />
            <Route path="sites" element={<div>현장 관리 페이지 (준비중)</div>} />
            <Route path="workers" element={<div>노무 관리 페이지 (준비중)</div>} />
            <Route path="contracts" element={<div>계약 관리 페이지 (준비중)</div>} />
            <Route path="settlements" element={<div>정산 관리 페이지 (준비중)</div>} />
            <Route path="settings" element={<div>설정 페이지 (준비중)</div>} />
          </Routes>
        </HQLayout>
      } />

      {/* Site Routes */}
      <Route path="/site/*" element={
        <SiteLayout>
          <Routes>
            <Route path="dashboard" element={<SiteDashboard />} />
            <Route path="attendance" element={<div>출역 관리 페이지 (준비중)</div>} />
            <Route path="reports" element={<div>작업일보 페이지 (준비중)</div>} />
            <Route path="contracts" element={<div>계약 관리 페이지 (준비중)</div>} />
            <Route path="teams" element={<div>팀/노무자 관리 페이지 (준비중)</div>} />
            <Route path="salary" element={<div>급여 정산 페이지 (준비중)</div>} />
          </Routes>
        </SiteLayout>
      } />

      {/* Worker Routes (Mobile) */}
      <Route path="/worker/*" element={
        <MobileLayout title="SmartCON">
          <Routes>
            <Route path="dashboard" element={<WorkerDashboard />} />
            <Route path="attendance" element={<div className="p-4">출역 캘린더 (준비중)</div>} />
            <Route path="contracts" element={<div className="p-4">전자계약서 (준비중)</div>} />
            <Route path="profile" element={<div className="p-4">내 정보 (준비중)</div>} />
          </Routes>
        </MobileLayout>
      } />
    </Routes>
  );
}

export default App;
