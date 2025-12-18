# 프로토타입 개발 가이드

**문서 버전:** 3.7  
**작성일:** 2025년 12월 17일  
**최종 수정일:** 2025년 12월 18일  
**작성자:** 경영기획실 이대영 이사  
**기반 문서:** `docs/SmartCON_Lite_UI_Design_Guide.md` (v3.7), `docs/PRD.md` (v2.8), `docs/System-Architecture.md`  
**참고 화면:** 실제 SmartCON 운영 화면 (근로자 관리, 출역 관리, 인트로 페이지)  
**로고 이미지:** `docs/logo_main_white.png` (흰색 로고, 프로토타입 `/public/logo_main_white.png`에 복사)

---

## 1. 프로토타입 개발 목적

### 1.1. 목적

- 실제 데이터 없이 화면 위주로 구현
- 모든 기능을 링크/클릭으로 동작하도록 구현
- 실제 데이터가 있는 것처럼 보이게 목업 데이터 사용
- 사용자 피드백 수집 및 요구사항 검증
- 디자인 시스템 검증 및 UI/UX 개선점 도출

### 1.2. 개발 원칙

- **빠른 개발**: 실제 API 연동 없이 화면만 구현
- **실제처럼**: 목업 데이터로 실제 데이터처럼 보이게
- **모든 기능**: PRD의 모든 화면 구현
- **반응형**: Mobile First 접근으로 PC/태블릿/모바일 반응형 디자인 적용
- **디자인 시스템 준수**: SmartCON Lite UI 디자인 가이드 완전 준수
- **역할 선택**: 각 사용자 역할을 선택하여 해당 화면 확인 가능
- **다중 역할**: 한 사용자가 여러 역할을 가질 수 있도록 구현
- **접근성**: WCAG 2.1 AA 수준 준수

### 1.3. 최신 PC웹/모바일웹 UI 트렌드 적용 원칙 (프로토타입 기준)

- **눈이 편안한 배경/표면**: 큰 면적 배경은 중립색(예: `#FAFAFA`)으로 통일하고, **회사 주색상(#71AA44)은 CTA/활성/강조 포인트로 제한**합니다.
- **대시보드 = 위젯(Widget) 중심**: KPI 카드 + 리스트/요약 위젯 조합으로 “스캔 가능한 정보 구조”를 기본으로 합니다.
- **내비게이션 최소화/명확화**:
  - **PC**: 접기/펼치기 가능한 좌측 사이드바 + 상단 헤더(아이콘/컨텍스트) 패턴
  - **Mobile Web**: 하단 탭(핵심 4개) + 상단 스티키 헤더 + Bottom Sheet(상세/선택) 패턴
- **마이크로 인터랙션 표준화**: hover/active/focus/disabled 상태를 명확히 하고, 기본 전환은 200ms로 통일합니다.

### 1.3.1. 인트로 페이지(참고 사이트 동일 구성) 반응형 적용

- **구성 기준**: [`ismartcon.net 인트로`](https://www.ismartcon.net/login/smart/index)와 동일하게 “메뉴 라벨/섹션/문의 폼/푸터 정보”를 반영합니다.
- **PC**
  - 상단 메뉴 라벨: 비대면바우처 / 스마트콘 소개 / 스마트콘 핵심기능 / 서비스요금 / 문의하기 / 로그인
  - 문의 폼은 2컬럼(입력/문의내용) 레이아웃으로 구성
- **Mobile**
  - 상단 메뉴는 숨기고 로그인 버튼만 노출 (섹션 이동은 CTA/스크롤로 제공)
  - 문의 폼은 단일 컬럼(세로 스택)으로 재배치

### 1.4. 메뉴 정보구조(IA) 재편안 (프로토타입 적용 기준)

프로토타입은 문서 기준(Design Guide/PRD)과 동일하게 **안 A(역할 중심)**을 기본으로 적용하고, 필요 시 안 B(업무 중심)를 확장안으로 적용합니다.

#### 1.4.1. 안 A: 역할(Role) 중심 IA (권장)

- **PC Web (사이드바)**
  - **정책**: **대시보드는 로그인 후 최초 화면**이며 사이드바 메뉴에서 제거합니다. “홈” 동작은 **SmartCON 로고 클릭 = 대시보드**로 제공합니다.
  - **HQ 메뉴**: `/hq/sites`, `/hq/workers`, `/hq/contracts`, `/hq/settlements`, `/hq/settings`
  - **Site 메뉴**: `/site/attendance`, `/site/reports`, `/site/contracts`, `/site/teams`, `/site/salary`
  - **Worker 메뉴(PC 미사용 권장)**: `/worker/attendance`, `/worker/contracts`, `/worker/profile`
- **Mobile Web (하단 탭 4개)**
  - **홈 탭 = 대시보드** / 출역 / 계약 / 내정보
  - 나머지 기능은 홈의 위젯(Quick Actions) 또는 더보기/설정으로 제공

#### 1.4.2. 안 B: 업무(Task) 중심 IA (확장안)

- 상위 메뉴명을 “현장/출역/작업일보/계약/정산/관리”로 통일하고 역할별 접근 권한만 제한
- 동일 화면 구조를 유지하되, 역할별로 카드/액션/위젯 구성만 다르게 제공

---

## 2. 기술 스택 및 프로젝트 설정

### 2.1. 기술 스택

본 프로토타입은 실제 개발 환경과 동일한 기술 스택을 사용합니다:

- **프레임워크**: React 18+ (TypeScript)
- **빌드 도구**: Vite
- **UI 라이브러리**: Shadcn/UI (Radix UI 기반)
- **스타일링**: Tailwind CSS 4.x
- **라우팅**: React Router v7
- **상태 관리**: Zustand (클라이언트), TanStack Query (서버 상태 - 프로토타입에서는 미사용)
- **아이콘**: Lucide React
- **폰트**: Pretendard (시스템 폰트 스택), Roboto Mono (코드/데이터용)

### 2.2. 프로젝트 초기화

```bash
cd prototype
npm create vite@latest . -- --template react-ts
npm install
```

### 2.3. 필수 패키지 설치

```bash
# UI 라이브러리 및 스타일링
npm install tailwindcss@next @tailwindcss/postcss autoprefixer
npm install clsx tailwind-merge
npm install lucide-react

# 라우팅
npm install react-router-dom@latest

# 상태 관리
npm install zustand

# 유틸리티
npm install date-fns

# Shadcn/UI 설치 (필요한 컴포넌트만 추가)
npx shadcn@latest init
```

### 2.4. Tailwind CSS 설정

`tailwind.config.js`:

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "#71AA44", // Company Green (CMYK 55 10 95 00)
          foreground: "#FFFFFF",
        },
        secondary: {
          DEFAULT: "#333333", // Dark Gray
          foreground: "#FFFFFF",
        },
        accent: {
          DEFAULT: "#71AA44", // Company Green (accent도 primary와 동일)
          foreground: "#FFFFFF",
        },
        destructive: {
          DEFAULT: "#E63946", // Red
          foreground: "#FFFFFF",
        },
        muted: {
          DEFAULT: "#6B7280", // Gray-500
          foreground: "#FFFFFF",
        },
        success: "#71AA44", // Company Green (Primary와 동일)
        warning: "#FF8C42", // Orange
        info: "#71AA44", // Company Green
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
      },
      fontFamily: {
        sans: ["Pretendard", "-apple-system", "BlinkMacSystemFont", "system-ui", "sans-serif"],
        display: ["Pretendard", "-apple-system", "BlinkMacSystemFont", "system-ui", "sans-serif"],
        mono: ["Roboto Mono", "monospace"],
      },
      borderRadius: {
        lg: "8px", // Large
        md: "6px", // Medium
        sm: "4px", // Subtle
      },
      spacing: {
        // 8px 기반 간격 시스템
        'xs': '4px',
        's': '8px',
        'm': '16px',
        'l': '24px',
        'xl': '32px',
      },
      screens: {
        'mobile': '375px',
        'tablet': '768px',
        'desktop': '1024px',
        'wide': '1920px',
      },
    },
  },
  plugins: [],
}
```

### 2.5. 글로벌 스타일 설정

`src/index.css`:

```css
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css');
@import url('https://fonts.googleapis.com/css2?family=Roboto+Mono&display=swap');

@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 249 250 251; /* Gray-50 #F9FAFB */
    --foreground: 30 27 75; /* Slate-800 #1E293B */

    --card: 0 0% 100%;
    --card-foreground: 30 27 75;

    --popover: 0 0% 100%;
    --popover-foreground: 30 27 75;

    --primary: 95 46 47; /* Company Green #71AA44 (CMYK 55 10 95 00, RGB 113 170 68) */
    --primary-foreground: 0 0% 100%;

    --secondary: 0 0 20; /* Dark Gray #333333 */
    --secondary-foreground: 0 0% 100%;

    --muted: 215 16 47; /* Gray-500 #6B7280 */
    --muted-foreground: 215 16 47;

    --accent: 95 46 47; /* Company Green #71AA44 */
    --accent-foreground: 0 0% 100%;

    --destructive: 354 77 56; /* Red #E63946 */
    --destructive-foreground: 0 0% 100%;

    --border: 214 32 91; /* Gray-300 */
    --input: 214 32 91;
    --ring: 95 46 47; /* Company Green #71AA44 */

    --radius: 0.375rem; /* 6px rounded-md */
  }
}

@layer base {
  * {
    border-color: hsl(var(--border));
  }

  body {
    background-color: hsl(var(--background));
    color: hsl(var(--foreground));
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'system-ui', sans-serif;
    font-size: 16px; /* 기본 폰트 크기 16px로 상향 */
    line-height: 1.5;
  }

  h1, h2, h3, h4, h5, h6 {
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'system-ui', sans-serif;
    font-weight: 700;
    line-height: 1.25;
  }

  h1 {
    font-size: 24px; /* text-2xl */
    font-weight: 700; /* font-bold */
  }

  h2 {
    font-size: 20px; /* text-xl */
    font-weight: 600; /* font-semibold */
  }

  h3 {
    font-size: 18px; /* text-lg */
    font-weight: 500; /* font-medium */
  }

  code, pre {
    font-family: 'Roboto Mono', monospace;
  }
}
```

---

## 3. 목업 데이터 구조

### 3.1. 사용자 데이터

`src/mock-data/users.ts`:

```typescript
export interface UserRole {
  roleCode: 'ROLE_SUPER' | 'ROLE_HQ' | 'ROLE_SITE' | 'ROLE_TEAM' | 'ROLE_WORKER'
  roleName: string
  siteId?: number
  siteName?: string
  teamName?: string
}

export interface User {
  id: number
  name: string
  email: string
  phone: string
  roles: UserRole[] // 다중 역할 지원
  currentRole?: UserRole // 현재 선택된 역할
  isFaceRegistered: boolean
}

export const mockUsers: User[] = [
  {
    id: 1,
    name: '홍길동',
    email: 'admin@smartcon.com',
    phone: '010-1234-5678',
    roles: [
      {
        roleCode: 'ROLE_HQ',
        roleName: '본사 관리자',
      },
    ],
    isFaceRegistered: true,
  },
  {
    id: 2,
    name: '김철수',
    email: 'site@smartcon.com',
    phone: '010-2345-6789',
    roles: [
      {
        roleCode: 'ROLE_SITE',
        roleName: '현장 관리자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
      },
      {
        roleCode: 'ROLE_TEAM',
        roleName: '팀장',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '철근팀',
      },
    ],
    isFaceRegistered: true,
  },
  {
    id: 3,
    name: '이영희',
    email: 'team@smartcon.com',
    phone: '010-3456-7890',
    roles: [
      {
        roleCode: 'ROLE_TEAM',
        roleName: '팀장',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '콘크리트팀',
      },
      {
        roleCode: 'ROLE_WORKER',
        roleName: '일반 노무자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '콘크리트팀',
      },
    ],
    isFaceRegistered: true,
  },
  // ... 더 많은 사용자 데이터
]
```

### 3.2. 현장 데이터

`src/mock-data/sites.ts`:

```typescript
export interface Site {
  id: number
  name: string
  code: string
  address: string
  managerId: number
  managerName: string
  status: 'ACTIVE' | 'INACTIVE'
}

export const mockSites: Site[] = [
  {
    id: 1,
    name: '서울 강남구 건설 현장',
    code: 'SITE-001',
    address: '서울시 강남구 테헤란로 123',
    managerId: 2,
    managerName: '김철수',
    status: 'ACTIVE',
  },
  // ... 더 많은 현장 데이터
]
```

### 3.3. 출역 기록 데이터

`src/mock-data/attendance.ts`:

```typescript
export interface AttendanceLog {
  id: number
  userId: number
  userName: string
  siteId: number
  siteName: string
  checkInTime: string
  checkOutTime?: string
  status: 'NORMAL' | 'LATE' | 'ABSENT'
  authType: 'FACE' | 'MANUAL'
  dailyManDay: number
  manualReason?: string
}

export const mockAttendanceLogs: AttendanceLog[] = [
  {
    id: 1,
    userId: 3,
    userName: '이영희',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-17T08:30:00',
    checkOutTime: '2025-12-17T18:00:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
  },
  // ... 더 많은 출역 기록
]
```

### 3.4. 작업일보 데이터

`src/mock-data/reports.ts`:

```typescript
export interface DailyReport {
  id: number
  siteId: number
  siteName: string
  teamLeaderId: number
  teamLeaderName: string
  workDate: string
  workTypeCode: string
  workTypeName: string
  workContent: string
  photoUrls: string[]
  isApproved: boolean
  approvedAt?: string
}

export const mockReports: DailyReport[] = [
  {
    id: 1,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    teamLeaderId: 4,
    teamLeaderName: '박민수',
    workDate: '2025-12-17',
    workTypeCode: 'CONCRETE',
    workTypeName: '콘크리트',
    workContent: '1층 콘크리트 타설 작업 완료',
    photoUrls: ['/images/report1.jpg'],
    isApproved: false,
  },
  // ... 더 많은 작업일보
]
```

---

## 4. 화면 구현 가이드

### 4.1. 레이아웃 구조

#### PC 레이아웃 (`src/components/layout/PcLayout.tsx`)

```tsx
import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { Sidebar } from '@/components/layout/Sidebar'
import { Header } from '@/components/layout/Header'

export function PcLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(true)

  return (
    <div className="flex h-screen bg-background">
      <Sidebar open={sidebarOpen} onToggle={() => setSidebarOpen(!sidebarOpen)} />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

#### 모바일 레이아웃 (`src/components/layout/MobileLayout.tsx`)

```tsx
import { Outlet } from 'react-router-dom'
import { BottomNavigation } from '@/components/layout/BottomNavigation'
import { Header } from '@/components/layout/Header'

export function MobileLayout() {
  return (
    <div className="flex flex-col h-screen bg-background">
      <Header />
      <main className="flex-1 overflow-y-auto pb-16">
        <Outlet />
      </main>
      <BottomNavigation />
    </div>
  )
}
```

#### 반응형 레이아웃 래퍼 (`src/components/layout/ResponsiveLayout.tsx`)

```tsx
import { useMediaQuery } from '@/hooks/useMediaQuery'
import { PcLayout } from './PcLayout'
import { MobileLayout } from './MobileLayout'

export function ResponsiveLayout() {
  const isDesktop = useMediaQuery('(min-width: 1024px)')

  return isDesktop ? <PcLayout /> : <MobileLayout />
}
```

### 4.2. 반응형 처리

`src/hooks/useMediaQuery.ts`:

```typescript
import { useState, useEffect } from 'react'

export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(false)

  useEffect(() => {
    const media = window.matchMedia(query)
    if (media.matches !== matches) {
      setMatches(media.matches)
    }
    const listener = () => setMatches(media.matches)
    media.addEventListener('change', listener)
    return () => media.removeEventListener('change', listener)
  }, [matches, query])

  return matches
}
```

`src/hooks/useResponsive.ts`:

```typescript
import { useMediaQuery } from './useMediaQuery'

export function useResponsive() {
  const isMobile = useMediaQuery('(max-width: 767px)')
  const isTablet = useMediaQuery('(min-width: 768px) and (max-width: 1023px)')
  const isDesktop = useMediaQuery('(min-width: 1024px)')

  return {
    isMobile,
    isTablet,
    isDesktop,
  }
}
```

### 4.3. 라우터 설정

`src/router/index.tsx`:

```tsx
import { createBrowserRouter, Navigate } from 'react-router-dom'
import { ResponsiveLayout } from '@/components/layout/ResponsiveLayout'
import { IntroView } from '@/views/intro/IntroView'
import { LoginMethodSelectView } from '@/views/auth/LoginMethodSelectView'
import { HqLoginView } from '@/views/auth/HqLoginView'
import { SocialLoginView } from '@/views/auth/SocialLoginView'
import { RoleSelectView } from '@/views/auth/RoleSelectView'
import { DashboardView } from '@/views/dashboard/DashboardView'
import { useUserStore } from '@/stores/userStore'

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { currentUser } = useUserStore()
  
  if (!currentUser) {
    return <Navigate to="/" replace /> // 인트로 페이지로 리다이렉트
  }
  
  return <>{children}</>
}

const RoleRequiredRoute = ({ children }: { children: React.ReactNode }) => {
  const { currentUser, selectedRole } = useUserStore()
  
  if (!currentUser) {
    return <Navigate to="/" replace /> // 인트로 페이지로 리다이렉트
  }
  
  if (!selectedRole && currentUser.roles.length > 1) {
    return <Navigate to="/role-select" replace />
  }
  
  if (!selectedRole && currentUser.roles.length === 1) {
    // 단일 역할인 경우 자동 선택
    useUserStore.getState().selectRole(currentUser.roles[0])
  }
  
  return <>{children}</>
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <IntroView />, // 인트로 페이지
  },
  {
    path: '/login',
    element: <LoginMethodSelectView />, // 로그인 방식 선택
  },
  {
    path: '/login/hq',
    element: <HqLoginView />, // 본사 관리자 로그인
  },
  {
    path: '/login/social',
    element: <SocialLoginView />, // 소셜 로그인
  },
  {
    path: '/role-select',
    element: (
      <ProtectedRoute>
        <RoleSelectView />
      </ProtectedRoute>
    ),
  },
  {
    element: (
      <ProtectedRoute>
        <RoleRequiredRoute>
          <ResponsiveLayout />
        </RoleRequiredRoute>
      </ProtectedRoute>
    ),
    children: [
      {
        path: '/dashboard',
        element: <DashboardView />,
      },
      // ... 더 많은 라우트
    ],
  },
])
```

### 4.4. 상태 관리 (Zustand)

`src/stores/userStore.ts`:

```typescript
import { create } from 'zustand'
import { User, UserRole } from '@/mock-data/users'
import { mockUsers } from '@/mock-data/users'

interface UserState {
  currentUser: User | null
  selectedRole: UserRole | null
  setCurrentUser: (user: User | null) => void
  selectRole: (role: UserRole) => void
  hasMultipleRoles: () => boolean
}

export const useUserStore = create<UserState>((set, get) => ({
  currentUser: mockUsers[0], // 프로토타입: 첫 번째 사용자로 초기화
  selectedRole: null,
  setCurrentUser: (user) => set({ currentUser: user, selectedRole: null }),
  selectRole: (role) => set({ selectedRole: role }),
  hasMultipleRoles: () => {
    const { currentUser } = get()
    return currentUser ? currentUser.roles.length > 1 : false
  },
}))
```

---

## 5. 주요 화면 구현 예시

### 5.1. 대시보드 화면

`src/views/dashboard/DashboardView.tsx`:

```tsx
import { useMemo } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { mockSites } from '@/mock-data/sites'
import { mockAttendanceLogs } from '@/mock-data/attendance'
import { AttendanceList } from '@/components/features/AttendanceList'
import { format } from 'date-fns'

export function DashboardView() {
  const todayAttendanceCount = useMemo(() => {
    const today = format(new Date(), 'yyyy-MM-dd')
    return mockAttendanceLogs.filter(
      log => log.checkInTime.startsWith(today)
    ).length
  }, [])

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">대시보드</h1>
      
      {/* KPI 카드 그리드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              전체 현장 수
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono">{mockSites.length}</div>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              금일 출역 인원
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono text-[#71AA44]">{todayAttendanceCount}</div>
          </CardContent>
        </Card>
        
        {/* 더 많은 KPI 카드 */}
      </div>
      
      {/* 출역 현황 테이블 */}
      <Card>
        <CardHeader>
          <CardTitle>금일 출역 현황</CardTitle>
        </CardHeader>
        <CardContent>
          <AttendanceList items={mockAttendanceLogs} />
        </CardContent>
      </Card>
    </div>
  )
}
```

### 5.2. 출역 체크 화면

`src/views/attendance/CheckInView.tsx`:

```tsx
import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { format } from 'date-fns'

export function CheckInView() {
  const [showResult, setShowResult] = useState(false)
  const [checkInTime, setCheckInTime] = useState('')

  const handleCheckIn = () => {
    // 프로토타입: 실제 API 호출 없이 결과만 표시
    setCheckInTime(format(new Date(), 'HH:mm:ss'))
    setShowResult(true)
  }

  return (
    <div className="container mx-auto p-4 max-w-md">
      <Card>
        <CardHeader>
          <CardTitle>출역 체크</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button
            onClick={handleCheckIn}
            className="w-full h-12 text-base transition-all duration-200 hover:shadow-md active:scale-95"
          >
            출역 체크하기
          </Button>
          
          <Dialog open={showResult} onOpenChange={setShowResult}>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>출역 체크 완료</DialogTitle>
                <DialogDescription>
                  출역 시간: {checkInTime}
                  <br />
                  상태: 정상 출역
                </DialogDescription>
              </DialogHeader>
              <Button onClick={() => setShowResult(false)}>
                확인
              </Button>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 5.3. 안면 등록 화면

`src/views/face/FaceRegisterView.tsx`:

```tsx
import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Stepper,
  StepperContent,
  StepperItem,
  StepperTrigger,
} from '@/components/ui/stepper'

const steps = [
  { id: 1, title: '안내' },
  { id: 2, title: '권한 요청' },
  { id: 3, title: '촬영' },
  { id: 4, title: '전송/분석' },
  { id: 5, title: '결과' },
]

export function FaceRegisterView() {
  const [currentStep, setCurrentStep] = useState(1)

  return (
    <div className="container mx-auto p-4 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>안면 등록</CardTitle>
        </CardHeader>
        <CardContent>
          <Stepper value={currentStep}>
            {steps.map((step) => (
              <StepperItem key={step.id} value={step.id}>
                <StepperTrigger>{step.title}</StepperTrigger>
                <StepperContent>
                  {step.id === 1 && (
                    <div className="space-y-4">
                      <h3 className="text-lg font-semibold">안면 등록 가이드</h3>
                      <ul className="list-disc list-inside space-y-2">
                        <li>안경과 마스크를 제거해주세요</li>
                        <li>밝은 곳에서 촬영해주세요</li>
                        <li>얼굴이 프레임 안에 오도록 해주세요</li>
                      </ul>
                      <Button onClick={() => setCurrentStep(2)}>
                        다음
                      </Button>
                    </div>
                  )}
                  {/* 더 많은 스텝 내용 */}
                </StepperContent>
              </StepperItem>
            ))}
          </Stepper>
        </CardContent>
      </Card>
    </div>
  )
}
```

---

## 5.5. 팝업 및 모달 구현

프로토타입에서는 모든 버튼 및 링크 클릭 시 적절한 팝업/모달 화면을 구현해야 합니다.

### 5.5.1. 팝업/모달 유형

#### A. 확인 모달 (Confirmation Dialog)

삭제, 승인, 거부 등 확인이 필요한 액션:

```tsx
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui-basic'

function DeleteConfirmDialog({ open, onClose, onConfirm, itemName }) {
  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>삭제 확인</DialogTitle>
          <DialogDescription>
            {itemName}을(를) 정말 삭제하시겠습니까?
            <br />
            이 작업은 되돌릴 수 없습니다.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            취소
          </Button>
          <Button variant="destructive" onClick={onConfirm}>
            삭제
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
```

#### B. 등록/수정 모달 (Form Dialog)

현장 개설, 노무자 등록, 정보 수정 등:

```tsx
function SiteRegisterDialog({ open, onClose, onSave, initialData }) {
  const [formData, setFormData] = useState(initialData || {
    name: '',
    code: '',
    address: '',
  })

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{initialData ? '현장 수정' : '현장 개설'}</DialogTitle>
          <DialogDescription>
            현장 정보를 입력해주세요.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4 py-4">
          <div>
            <label className="text-sm font-medium mb-2 block">현장명 *</label>
            <Input
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="현장명을 입력하세요"
            />
          </div>
          <div>
            <label className="text-sm font-medium mb-2 block">현장 코드 *</label>
            <Input
              value={formData.code}
              onChange={(e) => setFormData({ ...formData, code: e.target.value })}
              placeholder="SITE-001"
            />
          </div>
          <div>
            <label className="text-sm font-medium mb-2 block">주소 *</label>
            <Input
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              placeholder="주소를 입력하세요"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            취소
          </Button>
          <Button onClick={() => onSave(formData)}>
            {initialData ? '수정' : '등록'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
```

#### C. 상세보기 모달 (Detail Dialog)

현장 상세, 노무자 상세, 출역 상세 등:

```tsx
function SiteDetailDialog({ open, onClose, siteId }) {
  const site = mockSites.find(s => s.id === siteId)

  if (!site) return null

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[700px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>현장 상세 정보</DialogTitle>
        </DialogHeader>
        <div className="space-y-4 py-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">현장 코드</label>
              <p className="text-base font-mono">{site.code}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">현장명</label>
              <p className="text-base font-semibold">{site.name}</p>
            </div>
            <div className="col-span-2">
              <label className="text-sm font-medium text-gray-500">주소</label>
              <p className="text-base">{site.address}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">관리자</label>
              <p className="text-base">{site.managerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">상태</label>
              <Badge variant={site.status === 'ACTIVE' ? 'success' : 'warning'}>
                {site.status === 'ACTIVE' ? '운영중' : '중지'}
              </Badge>
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            닫기
          </Button>
          <Button onClick={() => {/* 수정 모달 열기 */}}>
            수정
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
```

#### D. 알림 토스트 (Toast Notification)

성공, 오류, 정보 알림:

```tsx
import { toast } from '@/components/ui/use-toast'

// 사용 예시
function handleSave() {
  // 저장 로직...
  toast({
    title: "저장 완료",
    description: "현장 정보가 성공적으로 저장되었습니다.",
    variant: "success",
  })
}

function handleError() {
  toast({
    title: "오류 발생",
    description: "저장 중 오류가 발생했습니다. 다시 시도해주세요.",
    variant: "destructive",
  })
}
```

#### E. 모바일 Bottom Sheet

모바일에서는 복잡한 모달을 Bottom Sheet로 구현:

```tsx
function MobileBottomSheet({ open, onClose, children }) {
  return (
    <>
      {open && (
        <>
          {/* Overlay */}
          <div
            className="fixed inset-0 bg-black/50 z-40 animate-in fade-in"
            onClick={onClose}
          />
          {/* Bottom Sheet */}
          <div
            className="fixed bottom-0 left-0 right-0 bg-white rounded-t-lg shadow-xl z-50 animate-in slide-in-from-bottom duration-300"
            style={{ paddingBottom: 'env(safe-area-inset-bottom)' }}
          >
            {/* Handle */}
            <div className="flex justify-center pt-3 pb-2">
              <div className="w-12 h-1 bg-gray-300 rounded-full" />
            </div>
            <div className="max-h-[80vh] overflow-y-auto px-4 pb-4">
              {children}
            </div>
          </div>
        </>
      )}
    </>
  )
}
```

### 5.5.2. 팝업 구현 체크리스트

- [ ] 삭제 확인 팝업 (모든 삭제 버튼)
- [ ] 등록 팝업 (현장 개설, 노무자 등록 등)
- [ ] 수정 팝업 (정보 수정)
- [ ] 상세보기 팝업 (리스트 항목 클릭)
- [ ] 승인/거부 확인 팝업
- [ ] 성공/오류 알림 토스트
- [ ] 모바일 Bottom Sheet 적용

---

## 6. 헤더 아이콘 및 기능 구현

### 6.1. 헤더 구성 요소

모든 화면의 헤더에는 다음 아이콘 및 기능을 배치합니다:

#### PC 헤더 (Desktop Header)

```tsx
import { Globe, Building2, Bell } from 'lucide-react'

function Header() {
  const { selectedRole } = useUserStore()
  const [showLanguageMenu, setShowLanguageMenu] = useState(false)
  const [showSiteMenu, setShowSiteMenu] = useState(false)
  const [showNotificationPanel, setShowNotificationPanel] = useState(false)

  return (
    <header className="h-16 bg-white shadow-sm flex items-center justify-between px-6 border-b border-gray-200">
      <h2 className="text-xl font-semibold text-gray-900">
        {getPageTitle(location.pathname)}
      </h2>
      <div className="flex items-center gap-4">
        {/* 현장명 표시 (현장 관리자/팀장/노무자) */}
        {selectedRole?.siteName && (
          <span className="text-sm text-gray-500">{selectedRole.siteName}</span>
        )}
        
        {/* 다국어 변경 아이콘 */}
        <div className="relative">
          <button
            onClick={() => setShowLanguageMenu(!showLanguageMenu)}
            className="p-2 rounded-md hover:bg-gray-100 transition-colors"
            aria-label="언어 변경"
          >
            <Globe size={20} className="text-gray-600" />
          </button>
          {showLanguageMenu && (
            <div className="absolute right-0 mt-2 w-32 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50">
              <button
                className="w-full text-left px-4 py-2 hover:bg-gray-50 text-sm"
                onClick={() => {/* 한국어로 변경 */}}
              >
                한국어
              </button>
              <button
                className="w-full text-left px-4 py-2 hover:bg-gray-50 text-sm"
                onClick={() => {/* 영어로 변경 */}}
              >
                English
              </button>
            </div>
          )}
        </div>

        {/* 현장 변경 아이콘 (현장 관리자/팀장/노무자) */}
        {selectedRole && (selectedRole.roleCode === 'ROLE_SITE' || 
                         selectedRole.roleCode === 'ROLE_TEAM' || 
                         selectedRole.roleCode === 'ROLE_WORKER') && (
          <div className="relative">
            <button
              onClick={() => setShowSiteMenu(!showSiteMenu)}
              className="p-2 rounded-md hover:bg-gray-100 transition-colors"
              aria-label="현장 변경"
            >
              <Building2 size={20} className="text-gray-600" />
            </button>
            {showSiteMenu && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50 max-h-64 overflow-y-auto">
                {/* 현장 목록 */}
                {availableSites.map((site) => (
                  <button
                    key={site.id}
                    className="w-full text-left px-4 py-2 hover:bg-gray-50 text-sm"
                    onClick={() => {/* 현장 변경 로직 */}}
                  >
                    {site.name}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 알림 아이콘 */}
        <div className="relative">
          <button
            onClick={() => setShowNotificationPanel(!showNotificationPanel)}
            className="p-2 rounded-md hover:bg-gray-100 transition-colors relative"
            aria-label="알림"
          >
            <Bell size={20} className="text-gray-600" />
            {/* 알림 배지 */}
            <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>
          {showNotificationPanel && (
            <NotificationPanel
              onClose={() => setShowNotificationPanel(false)}
            />
          )}
        </div>
      </div>
    </header>
  )
}
```

#### 모바일 헤더 (Mobile Header)

```tsx
function MobileHeader() {
  return (
    <header className="sticky top-0 z-30 bg-white border-b border-gray-200 px-4 py-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(-1)}
            className="p-2 -ml-2"
            aria-label="뒤로가기"
          >
            <ChevronLeft size={24} className="text-gray-600" />
          </button>
          <h2 className="text-lg font-semibold">{getPageTitle(location.pathname)}</h2>
        </div>
        <div className="flex items-center gap-2">
          {/* 알림 아이콘 (모바일에서는 간소화) */}
          <button
            onClick={() => {/* 알림 화면으로 이동 */}}
            className="p-2 relative"
            aria-label="알림"
          >
            <Bell size={20} className="text-gray-600" />
            <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>
        </div>
      </div>
    </header>
  )
}
```

### 6.2. 알림 패널 컴포넌트

```tsx
function NotificationPanel({ onClose }) {
  const notifications = [
    { id: 1, type: 'info', title: '새로운 계약서', message: '서명이 필요한 계약서가 있습니다.', time: '5분 전' },
    { id: 2, type: 'warning', title: '출역 확인 필요', message: '미확인 출역 기록이 3건 있습니다.', time: '10분 전' },
    { id: 3, type: 'success', title: '승인 완료', message: '작업일보가 승인되었습니다.', time: '1시간 전' },
  ]

  return (
    <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-96 overflow-hidden flex flex-col">
      <div className="px-4 py-3 border-b border-gray-200 flex items-center justify-between">
        <h3 className="font-semibold text-sm">알림</h3>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600"
          aria-label="닫기"
        >
          <X size={16} />
        </button>
      </div>
      <div className="overflow-y-auto flex-1">
        {notifications.length === 0 ? (
          <div className="px-4 py-8 text-center text-gray-500 text-sm">
            알림이 없습니다.
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {notifications.map((notif) => (
              <button
                key={notif.id}
                className="w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors"
                onClick={() => {/* 알림 상세 처리 */}}
              >
                <div className="flex items-start gap-3">
                  <div className={`w-2 h-2 rounded-full mt-2 ${
                    notif.type === 'info' ? 'bg-blue-500' :
                    notif.type === 'warning' ? 'bg-amber-500' :
                    'bg-green-500'
                  }`} />
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-sm text-gray-900 truncate">
                      {notif.title}
                    </p>
                    <p className="text-xs text-gray-500 mt-1 line-clamp-2">
                      {notif.message}
                    </p>
                    <p className="text-xs text-gray-400 mt-1">
                      {notif.time}
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
      {notifications.length > 0 && (
        <div className="px-4 py-2 border-t border-gray-200">
          <button className="text-xs text-[#71AA44] hover:text-[#557C2C] w-full text-center py-1">
            모두 읽음 처리
          </button>
        </div>
      )}
    </div>
  )
}
```

### 6.3. 헤더 아이콘 배치 규칙

| 아이콘 | 위치 | 표시 조건 | 기능 |
|:---|:---|:---|:---|
| **다국어 변경** | 헤더 우측 | 모든 화면 | 언어 변경 드롭다운 |
| **현장 변경** | 헤더 우측 | 현장 관리자/팀장/노무자 | 현장 목록 선택 드롭다운 |
| **알림** | 헤더 우측 | 모든 화면 | 알림 패널 열기 (배지 표시) |

### 6.4. 아이콘 스타일

```tsx
// 기본 아이콘 버튼
<button className="p-2 rounded-md hover:bg-gray-100 transition-colors">
  <Icon size={20} className="text-gray-600" />
</button>

// 알림 배지가 있는 아이콘
<button className="p-2 rounded-md hover:bg-gray-100 transition-colors relative">
  <Icon size={20} className="text-gray-600" />
  <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
</button>
```

---

## 7. 디자인 시스템 적용

### 7.1. 색상 시스템

**회사 주색상 CMYK 55 10 95 00을 기반으로 구성된 색상 팔레트입니다.**

- **Primary (Company Green)**: `#71AA44` (CMYK 55 10 95 00, RGB 113 170 68) - 주요 버튼, 활성 상태, 브랜드 컬러
- **Primary Dark**: `#557C2C` - Primary 호버 상태, 강조 요소
- **Primary Light**: `#C2E199` - Primary 배경, 연한 강조
- **Secondary (Dark Gray)**: `#333333` - 사이드바, 헤더 텍스트
- **Background (Gray-50)**: `#F9FAFB` - 앱 배경색 (눈의 피로도 감소)
- **Surface (White)**: `#FFFFFF` - 카드, 모달, 컨테이너 배경
- **Destructive (Red)**: `#E63946` - 삭제, 미서명 알림, 오류 메시지
- **Success (Company Green)**: `#71AA44` - 서명 완료, 출역 정상, 승인 (Primary와 동일)
- **Warning (Orange)**: `#FF8C42` - 검토 대기, 진행 중

### 7.2. 타이포그래피

- **H1 (페이지 제목)**: `text-2xl font-bold` (24px, 700)
- **H2 (섹션 제목)**: `text-xl font-semibold` (20px, 600)
- **H3 (카드 제목)**: `text-lg font-medium` (18px, 500)
- **Body (본문)**: `text-base` (16px, 400) - 기본 크기 상향 조정
- **Caption**: `text-sm` (14px, 400) - 보조 텍스트, 라벨, `text-gray-500`

**폰트 패밀리**: Pretendard 시스템 폰트 스택 사용

### 7.3. 간격 시스템 (8px 기반)

- **XS**: `4px` - `p-1`, `gap-1` - 아이콘과 텍스트 사이
- **S**: `8px` - `p-2`, `gap-2` - 요소 내부 여백
- **M**: `16px` - `p-4`, `gap-4` - 요소 간 간격
- **L**: `24px` - `p-6`, `gap-6` - 섹션 간 간격
- **XL**: `32px` - `p-8`, `gap-8` - 주요 섹션 간 간격
- **2XL**: `48px` - `p-12`, `gap-12` - 페이지 섹션 간격

### 7.4. 반응형 브레이크포인트

- **Mobile**: `~767px` - 모바일 우선 디자인
- **Tablet**: `768px ~ 1023px` - 태블릿 레이아웃
- **Desktop**: `1024px ~` - PC 레이아웃

### 7.5. 버튼 스타일

#### 버튼 크기 및 상태

- **높이**: `h-12` (48px) - 모바일 터치 영역 확보
- **모서리**: `rounded-md` (6px)
- **전환**: `transition-all duration-200`

```tsx
// Primary 버튼 (회사 주색상)
<Button className="bg-[#71AA44] text-white hover:bg-[#557C2C] hover:shadow-md active:scale-95">
  저장
</Button>

// Secondary 버튼
<Button variant="outline" className="border-gray-300 hover:bg-gray-50 active:scale-95">
  취소
</Button>

// Destructive 버튼
<Button variant="destructive" className="bg-red-500 hover:bg-red-600 hover:shadow-md active:scale-95">
  삭제
</Button>
```

#### 버튼 상태별 스타일

- **Normal**: 기본 스타일
- **Hover**: `hover:bg-[색상]-[단계]`, `hover:shadow-md` (그림자 증가)
- **Active**: `active:scale-95` (클릭 시 약간 축소)
- **Focus**: `focus:outline-none focus:ring-2 focus:ring-[#71AA44] focus:ring-offset-2`
- **Disabled**: `disabled:opacity-50 disabled:cursor-not-allowed`

### 7.6. 링크 스타일

```tsx
// Primary Link (회사 주색상)
<a href="#" className="text-[#71AA44] hover:text-[#557C2C] hover:underline underline-offset-4">
  자세히 보기
</a>

// Secondary Link
<a href="#" className="text-gray-600 hover:text-gray-900 hover:underline underline-offset-4">
  외부 링크
</a>
```

### 7.7. 카드 스타일

```tsx
// 기본 카드
<Card className="rounded-lg border border-gray-200 bg-white shadow-sm p-5">
  <CardHeader>
    <CardTitle>제목</CardTitle>
  </CardHeader>
  <CardContent>
    내용
  </CardContent>
</Card>

// Action Card (호버 효과)
<Card className="rounded-lg border border-gray-200 bg-white shadow-sm p-5 cursor-pointer transition-all duration-200 hover:shadow-lg hover:-translate-y-1">
  클릭 가능한 카드
</Card>
```

### 7.8. 그림자 시스템

- **None**: `shadow-none` - 플랫한 요소
- **Small**: `shadow-sm` - 카드, 입력 필드 (기본)
- **Medium**: `shadow-md` - 호버 상태의 카드, 버튼
- **Large**: `shadow-lg` - 모달, 드롭다운 메뉴
- **XL**: `shadow-xl` - 중요한 모달, 토스트 알림

### 7.9. 전환 애니메이션

- **Fast**: `transition-all duration-150` - 즉각적인 피드백 (버튼 클릭)
- **Base**: `transition-all duration-200` - 기본 전환 (기본값)
- **Slow**: `transition-all duration-300` - 레이아웃 변화, 모달 열림/닫힘

### 7.10. 포커스 표시 (접근성)

```tsx
// 포커스 링 (회사 주색상)
className="focus:outline-none focus:ring-2 focus:ring-[#71AA44] focus:ring-offset-2"

// 포커스 테두리
className="focus:outline-none focus:border-[#71AA44] focus:ring-2 focus:ring-[#71AA44] focus:ring-offset-2"
```

---

## 8. 반응형 디자인 패턴

### 7.1. 모바일 퍼스트 접근

모든 컴포넌트는 모바일을 기준으로 작성하고, `md:`, `lg:` 프리픽스로 확장:

```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
  {/* 카드 그리드 */}
</div>
```

### 7.2. 터치 친화적 UI

- 버튼 최소 크기: `h-12` (48px) - 모바일 터치 영역 확보
- 터치 영역 간격: 최소 `8px`
- Safe Area 대응: `padding-top: env(safe-area-inset-top)`, `padding-bottom: env(safe-area-inset-bottom)`
- 스크롤 가능한 영역 명확히 표시

### 7.3. 네비게이션 패턴

- **PC**: 좌측 사이드바 + 상단 헤더
- **모바일**: 하단 탭 바 + 상단 헤더

---

## 9. 접근성 (Accessibility)

### 8.1. WCAG 2.1 AA 준수

- 색상 대비: 텍스트와 배경 4.5:1 이상
- 키보드 네비게이션: 모든 기능 키보드로 접근 가능
- 스크린 리더: 적절한 ARIA 레이블 사용

### 8.2. 접근성 체크리스트

- [ ] 모든 이미지에 alt 텍스트
- [ ] 버튼에 명확한 라벨
- [ ] 폼 필드에 라벨 연결
- [ ] 키보드 포커스 표시 (ring-2 ring-[#71AA44] ring-offset-2)
- [ ] 색상만으로 정보 전달하지 않음
- [ ] 색상 대비 WCAG AA 수준 (4.5:1 이상)
- [ ] 모든 인터랙티브 요소 키보드 접근 가능

---

## 10. 성능 최적화

### 9.1. 코드 스플리팅

```tsx
const DashboardView = lazy(() => import('@/views/dashboard/DashboardView'))
```

### 9.2. 이미지 최적화

- WebP 형식 사용
- Lazy loading 적용
- 반응형 이미지 (srcset)

### 9.3. 폰트 최적화

- Google Fonts preconnect
- font-display: swap

---

## 11. 완료 검증 체크리스트

### 11.1. 화면 구현

- [ ] 모든 화면 구현 완료
- [ ] PC/태블릿/모바일 반응형 적용
- [ ] 모든 링크/클릭 동작 확인
- [ ] 역할 선택 화면 구현
- [ ] 역할 전환 기능 구현
- [ ] 각 역할별 대시보드 화면 확인
- [ ] 등록 화면 구현 (모달/팝업)
- [ ] 상세보기 화면 구현 (모달/팝업)
- [ ] 삭제 확인 팝업 구현
- [ ] 승인/거부 확인 팝업 구현
- [ ] 알림 토스트 구현

### 11.2. 디자인 시스템

- [ ] 색상 팔레트 준수 (회사 주색상 #71AA44, Dark Gray #333333, Gray-50 등)
- [ ] 타이포그래피 규칙 준수 (Pretendard 폰트, H1: 24px, Body: 16px)
- [ ] 간격 시스템 (8px 기반) 적용
- [ ] 버튼 스타일 통일 (h-12, rounded-md, 전환 애니메이션)
- [ ] 카드/모달 스타일 통일 (shadow-sm, border, rounded-lg, p-5)
- [ ] 그림자 시스템 적용
- [ ] 링크 스타일 및 호버 효과 적용
- [ ] 포커스 표시 명확하게 구현

### 11.3. 반응형 디자인

- [ ] Mobile (375px) 테스트
- [ ] Tablet (768px) 테스트
- [ ] Desktop (1024px, 1920px) 테스트
- [ ] 터치 영역 최소 44px
- [ ] 스크롤 영역 명확히 표시

### 11.4. 접근성

- [ ] 색상 대비 WCAG AA 수준
- [ ] 키보드 네비게이션 가능
- [ ] 스크린 리더 테스트
- [ ] 모든 이미지 alt 텍스트

### 11.5. 데이터 표시

- [ ] 목업 데이터로 실제처럼 표시
- [ ] 날짜/시간 포맷팅 적용
- [ ] 상태별 색상 구분 적용
- [ ] 로딩 상태 표시 (선택사항)

### 11.6. 사용자 경험

- [ ] 화면 전환 부드러움 (전환 애니메이션 적용)
- [ ] 마이크로 인터랙션 적용 (버튼 호버/액티브, 카드 호버)
- [ ] 에러 메시지 표시 (선택사항)
- [ ] 성공 피드백 제공
- [ ] 로딩 상태 표시 (Skeleton UI)

### 11.7. 최신 웹 UI 트렌드

### 11.8. 팝업 및 모달

- [ ] 삭제 확인 팝업 모든 삭제 버튼에 구현
- [ ] 등록 팝업 구현 (현장, 노무자 등)
- [ ] 수정 팝업 구현
- [ ] 상세보기 팝업 구현 (리스트 항목 클릭)
- [ ] 승인/거부 확인 팝업 구현
- [ ] 성공/오류 알림 토스트 구현
- [ ] 모바일 Bottom Sheet 적용 (복잡한 모달)

### 11.9. 헤더 아이콘 및 기능

- [ ] 다국어 변경 아이콘 배치 (모든 화면)
- [ ] 현장 변경 아이콘 배치 (현장 관리자/팀장/노무자)
- [ ] 알림 아이콘 배치 (모든 화면, 배지 표시)
- [ ] 알림 패널 구현
- [ ] 드롭다운 메뉴 구현 (언어, 현장 선택)

- [ ] 마이크로 인터랙션 적용 (버튼 클릭, 호버 효과)
- [ ] 명확한 상태 표시 (Hover, Active, Focus, Disabled)
- [ ] 넉넉한 여백 제공 (카드 패딩 최소 20px, 섹션 간격 최소 24px)
- [ ] 부드러운 전환 효과 (200ms 기본)
- [ ] 명확한 계층 구조 (그림자 시스템 활용)

---

## 12. 다음 단계

프로토타입 완료 후:

1. **사용자 피드백 수집**
   - 실제 사용자에게 시연
   - 피드백 수집 및 정리
   - 디자인 시스템 검증

2. **요구사항 검증**
   - PRD와 프로토타입 비교
   - 누락된 화면/기능 확인
   - UI/UX 개선점 도출

3. **실제 개발 시작**
   - Phase 1부터 순차적으로 개발
   - 프로토타입 코드는 참고용으로 활용
   - 디자인 시스템을 실제 프로젝트에 적용

---

## 13. 참고 자료

- [SmartCON Lite UI 디자인 가이드](./SmartCON_Lite_UI_Design_Guide.md)
- [PRD (제품 요구사항 정의서)](./PRD.md)
- [시스템 아키텍처](./System-Architecture.md)
- [Shadcn/UI 공식 문서](https://ui.shadcn.com/)
- [Tailwind CSS 공식 문서](https://tailwindcss.com/)
- [React 공식 문서](https://react.dev/)
- [Material Design 3 (Navigation)](https://m3.material.io/foundations/navigation/overview)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [Microsoft Fluent 2](https://fluent2.microsoft.design/)

---

## 문서 정보

**문서 버전**: 3.7  
**최종 업데이트**: 2025년 12월 18일  
**작성자**: 경영기획실 이대영 이사  
**참고**: 본 가이드는 SmartCON_Lite_UI_Design_Guide v3.7 및 PRD v2.8를 기반으로 작성되었으며, 실제 SmartCON 운영 화면 구조를 반영하였습니다.
