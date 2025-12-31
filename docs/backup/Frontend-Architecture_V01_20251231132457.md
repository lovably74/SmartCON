# SmartCON Lite 프론트엔드 아키텍처

**문서 버전:** 1.0  
**작성일:** 2025년 12월 23일  
**작성자:** Kiro AI Assistant  
**기반 문서:** PRD v2.8, 상세기능명세서 v3.0

---

## 1. 프론트엔드 아키텍처 개요

### 1.1 아키텍처 원칙
- **Component-Based Architecture**: 재사용 가능한 컴포넌트 중심 설계
- **Role-Based UI**: 사용자 역할별 최적화된 인터페이스
- **Mobile-First Design**: 모바일 우선 반응형 디자인
- **Progressive Web App (PWA)**: 네이티브 앱과 유사한 사용자 경험
- **Atomic Design**: 컴포넌트 계층 구조 (Atoms → Molecules → Organisms → Templates → Pages)

### 1.2 기술 스택
- **Framework**: React 18.2+ with TypeScript 5.0+
- **Build Tool**: Vite 5.0+ (HMR, Tree-shaking)
- **UI Library**: Shadcn/UI + Tailwind CSS 3.4+
- **State Management**: 
  - Zustand 4.4+ (클라이언트 상태)
  - TanStack Query 5.0+ (서버 상태, 캐싱)
- **Routing**: Wouter 3.0+ (경량 라우터)
- **Mobile Bridge**: Capacitor 6.0+ (카메라, GPS, 푸시알림)
- **Testing**: Vitest + React Testing Library
- **Build**: TypeScript 5.0+, ESLint, Prettier

---

## 2. 프로젝트 구조

### 2.1 전체 디렉토리 구조
```
frontend/
├── public/                         # 정적 파일
│   ├── icons/                      # PWA 아이콘
│   ├── manifest.json               # PWA 매니페스트
│   └── sw.js                       # 서비스 워커
├── src/
│   ├── components/                 # 재사용 가능한 컴포넌트
│   │   ├── ui/                     # Shadcn/UI 기본 컴포넌트
│   │   ├── layout/                 # 레이아웃 컴포넌트
│   │   ├── forms/                  # 폼 컴포넌트
│   │   ├── charts/                 # 차트 컴포넌트
│   │   └── common/                 # 공통 컴포넌트
│   ├── pages/                      # 페이지 컴포넌트 (역할별)
│   │   ├── auth/                   # 인증 페이지
│   │   ├── intro/                  # 인트로 페이지
│   │   ├── super/                  # 슈퍼 관리자 페이지
│   │   ├── hq/                     # 본사 관리자 페이지
│   │   ├── site/                   # 현장 관리자 페이지
│   │   ├── team/                   # 팀장 페이지
│   │   └── worker/                 # 노무자 페이지
│   ├── hooks/                      # 커스텀 React 훅
│   │   ├── api/                    # API 관련 훅
│   │   ├── auth/                   # 인증 관련 훅
│   │   └── common/                 # 공통 훅
│   ├── stores/                     # Zustand 상태 스토어
│   │   ├── authStore.ts            # 인증 상태
│   │   ├── tenantStore.ts          # 테넌트 상태
│   │   └── uiStore.ts              # UI 상태
│   ├── services/                   # API 서비스
│   │   ├── api/                    # API 클라이언트
│   │   ├── auth/                   # 인증 서비스
│   │   └── storage/                # 로컬 스토리지 서비스
│   ├── types/                      # TypeScript 타입 정의
│   │   ├── api/                    # API 타입
│   │   ├── auth/                   # 인증 타입
│   │   └── common/                 # 공통 타입
│   ├── utils/                      # 유틸리티 함수
│   │   ├── format/                 # 포맷팅 함수
│   │   ├── validation/             # 검증 함수
│   │   └── constants/              # 상수 정의
│   ├── styles/                     # 스타일 파일
│   │   ├── globals.css             # 전역 스타일
│   │   └── components.css          # 컴포넌트 스타일
│   ├── assets/                     # 정적 자산
│   │   ├── images/                 # 이미지 파일
│   │   ├── icons/                  # 아이콘 파일
│   │   └── fonts/                  # 폰트 파일
│   ├── contexts/                   # React Context
│   │   ├── ThemeContext.tsx        # 테마 컨텍스트
│   │   └── TenantContext.tsx       # 테넌트 컨텍스트
│   ├── App.tsx                     # 메인 앱 컴포넌트
│   ├── main.tsx                    # 앱 진입점
│   └── vite-env.d.ts              # Vite 타입 정의
├── capacitor/                      # Capacitor 설정 (모바일)
│   ├── capacitor.config.ts
│   ├── android/                    # Android 프로젝트
│   └── ios/                        # iOS 프로젝트
├── tests/                          # 테스트 파일
│   ├── __mocks__/                  # 모킹 파일
│   ├── setup.ts                    # 테스트 설정
│   └── utils/                      # 테스트 유틸리티
├── .env.example                    # 환경변수 예시
├── .env.local                      # 로컬 환경변수
├── package.json
├── tsconfig.json
├── tailwind.config.js
├── vite.config.ts
└── vitest.config.ts
```

### 2.2 컴포넌트 계층 구조 (Atomic Design)
```
components/
├── ui/                             # Atoms (기본 UI 컴포넌트)
│   ├── Button.tsx
│   ├── Input.tsx
│   ├── Card.tsx
│   ├── Badge.tsx
│   ├── Avatar.tsx
│   └── ...
├── forms/                          # Molecules (폼 관련 컴포넌트)
│   ├── LoginForm.tsx
│   ├── SearchForm.tsx
│   ├── FilterForm.tsx
│   └── ...
├── layout/                         # Organisms (레이아웃 컴포넌트)
│   ├── Header.tsx
│   ├── Sidebar.tsx
│   ├── Navigation.tsx
│   ├── Footer.tsx
│   └── ...
├── charts/                         # Molecules (차트 컴포넌트)
│   ├── AttendanceChart.tsx
│   ├── RevenueChart.tsx
│   └── ...
└── common/                         # Organisms (복합 컴포넌트)
    ├── DataTable.tsx
    ├── Dashboard.tsx
    ├── Modal.tsx
    └── ...
```

---

## 3. 핵심 컴포넌트 설계

### 3.1 라우팅 시스템
#### 3.1.1 역할 기반 라우팅
```typescript
// src/routes/index.tsx
import { Route, Switch, Redirect } from 'wouter';
import { useAuthStore } from '@/stores/authStore';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';

// 페이지 컴포넌트 lazy loading
const IntroPage = lazy(() => import('@/pages/intro/IntroPage'));
const LoginPage = lazy(() => import('@/pages/auth/LoginPage'));
const SuperDashboard = lazy(() => import('@/pages/super/Dashboard'));
const HQDashboard = lazy(() => import('@/pages/hq/Dashboard'));
const SiteDashboard = lazy(() => import('@/pages/site/Dashboard'));
const TeamDashboard = lazy(() => import('@/pages/team/Dashboard'));
const WorkerDashboard = lazy(() => import('@/pages/worker/Dashboard'));

export const AppRoutes: React.FC = () => {
  const { user, isAuthenticated } = useAuthStore();

  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Switch>
        {/* 공개 라우트 */}
        <Route path="/" component={IntroPage} />
        <Route path="/login" component={LoginPage} />
        <Route path="/login/hq" component={HQLoginPage} />
        <Route path="/login/social" component={SocialLoginPage} />
        
        {/* 보호된 라우트 - 역할별 */}
        <ProtectedRoute 
          path="/super/*" 
          component={SuperRoutes} 
          requiredRoles={['ROLE_SUPER']} 
        />
        <ProtectedRoute 
          path="/hq/*" 
          component={HQRoutes} 
          requiredRoles={['ROLE_HQ']} 
        />
        <ProtectedRoute 
          path="/site/*" 
          component={SiteRoutes} 
          requiredRoles={['ROLE_SITE']} 
        />
        <ProtectedRoute 
          path="/team/*" 
          component={TeamRoutes} 
          requiredRoles={['ROLE_TEAM']} 
        />
        <ProtectedRoute 
          path="/worker/*" 
          component={WorkerRoutes} 
          requiredRoles={['ROLE_WORKER']} 
        />
        
        {/* 기본 리다이렉트 */}
        <Route>
          {isAuthenticated ? (
            <RoleDashboardRedirect user={user} />
          ) : (
            <Redirect to="/" />
          )}
        </Route>
      </Switch>
    </Suspense>
  );
};

// 역할별 대시보드 리다이렉트
const RoleDashboardRedirect: React.FC<{ user: User }> = ({ user }) => {
  const primaryRole = user.roles[0]; // 첫 번째 역할을 기본으로 사용
  
  switch (primaryRole) {
    case 'ROLE_SUPER':
      return <Redirect to="/super/dashboard" />;
    case 'ROLE_HQ':
      return <Redirect to="/hq/dashboard" />;
    case 'ROLE_SITE':
      return <Redirect to="/site/dashboard" />;
    case 'ROLE_TEAM':
      return <Redirect to="/team/dashboard" />;
    case 'ROLE_WORKER':
      return <Redirect to="/worker/dashboard" />;
    default:
      return <Redirect to="/" />;
  }
};
```

#### 3.1.2 보호된 라우트 컴포넌트
```typescript
// src/components/auth/ProtectedRoute.tsx
interface ProtectedRouteProps {
  path: string;
  component: React.ComponentType;
  requiredRoles: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  path,
  component: Component,
  requiredRoles
}) => {
  const { user, isAuthenticated } = useAuthStore();
  const [location] = useLocation();

  if (!isAuthenticated) {
    return <Redirect to="/login" />;
  }

  const hasRequiredRole = user?.roles.some(role => requiredRoles.includes(role));
  
  if (!hasRequiredRole) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Card className="p-6">
          <h2 className="text-xl font-semibold mb-2">접근 권한이 없습니다</h2>
          <p className="text-gray-600 mb-4">이 페이지에 접근할 권한이 없습니다.</p>
          <Button onClick={() => window.history.back()}>
            이전 페이지로 돌아가기
          </Button>
        </Card>
      </div>
    );
  }

  return <Route path={path} component={Component} />;
};
```

### 3.2 상태 관리 (Zustand)
#### 3.2.1 인증 스토어
```typescript
// src/stores/authStore.ts
interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  refreshAccessToken: () => Promise<void>;
  updateUser: (user: Partial<User>) => void;
  switchRole: (role: string) => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: false,

  login: async (credentials) => {
    set({ isLoading: true });
    try {
      const response = await authService.login(credentials);
      const { user, accessToken, refreshToken } = response.data;
      
      // 토큰을 secure storage에 저장
      await secureStorage.setItem('accessToken', accessToken);
      await secureStorage.setItem('refreshToken', refreshToken);
      
      set({
        user,
        accessToken,
        refreshToken,
        isAuthenticated: true,
        isLoading: false
      });
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

  logout: () => {
    secureStorage.removeItem('accessToken');
    secureStorage.removeItem('refreshToken');
    
    set({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false
    });
  },

  refreshAccessToken: async () => {
    const { refreshToken } = get();
    if (!refreshToken) throw new Error('No refresh token');

    try {
      const response = await authService.refreshToken(refreshToken);
      const { accessToken: newAccessToken } = response.data;
      
      await secureStorage.setItem('accessToken', newAccessToken);
      set({ accessToken: newAccessToken });
    } catch (error) {
      get().logout();
      throw error;
    }
  },

  updateUser: (userData) => {
    set(state => ({
      user: state.user ? { ...state.user, ...userData } : null
    }));
  },

  switchRole: (role) => {
    set(state => ({
      user: state.user ? { ...state.user, currentRole: role } : null
    }));
  }
}));
```

#### 3.2.2 UI 상태 스토어
```typescript
// src/stores/uiStore.ts
interface UIState {
  theme: 'light' | 'dark';
  sidebarCollapsed: boolean;
  currentSite: Site | null;
  notifications: Notification[];
  
  // Actions
  toggleTheme: () => void;
  toggleSidebar: () => void;
  setCurrentSite: (site: Site) => void;
  addNotification: (notification: Omit<Notification, 'id'>) => void;
  removeNotification: (id: string) => void;
}

export const useUIStore = create<UIState>((set, get) => ({
  theme: 'light',
  sidebarCollapsed: false,
  currentSite: null,
  notifications: [],

  toggleTheme: () => {
    set(state => ({ theme: state.theme === 'light' ? 'dark' : 'light' }));
  },

  toggleSidebar: () => {
    set(state => ({ sidebarCollapsed: !state.sidebarCollapsed }));
  },

  setCurrentSite: (site) => {
    set({ currentSite: site });
  },

  addNotification: (notification) => {
    const id = Date.now().toString();
    set(state => ({
      notifications: [...state.notifications, { ...notification, id }]
    }));
    
    // 자동 제거 (5초 후)
    setTimeout(() => {
      get().removeNotification(id);
    }, 5000);
  },

  removeNotification: (id) => {
    set(state => ({
      notifications: state.notifications.filter(n => n.id !== id)
    }));
  }
}));
```

### 3.3 API 서비스 (TanStack Query)
#### 3.3.1 API 클라이언트 설정
```typescript
// src/services/api/client.ts
import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/authStore';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - 토큰 자동 추가
    this.client.interceptors.request.use(
      (config) => {
        const { accessToken } = useAuthStore.getState();
        if (accessToken) {
          config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor - 토큰 갱신 처리
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            await useAuthStore.getState().refreshAccessToken();
            const { accessToken } = useAuthStore.getState();
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            return this.client(originalRequest);
          } catch (refreshError) {
            useAuthStore.getState().logout();
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        }
        
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.get(url, config);
    return response.data;
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.post(url, data, config);
    return response.data;
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.put(url, data, config);
    return response.data;
  }

  async patch<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.patch(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.delete(url, config);
    return response.data;
  }
}

export const apiClient = new ApiClient();
```

#### 3.3.2 API 훅 (React Query)
```typescript
// src/hooks/api/useAttendance.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/services/api/client';

// 출역 기록 조회
export const useAttendanceLogs = (siteId: number, date: string) => {
  return useQuery({
    queryKey: ['attendance', 'logs', siteId, date],
    queryFn: () => apiClient.get<AttendanceLog[]>(`/attendance/logs?siteId=${siteId}&date=${date}`),
    enabled: !!siteId && !!date,
    staleTime: 5 * 60 * 1000, // 5분
  });
};

// 출역 마감
export const useAttendanceFinalize = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data: FinalizeAttendanceRequest) => 
      apiClient.post('/attendance/finalize', data),
    onSuccess: (_, variables) => {
      // 관련 쿼리 무효화
      queryClient.invalidateQueries({
        queryKey: ['attendance', 'logs', variables.siteId, variables.date]
      });
      queryClient.invalidateQueries({
        queryKey: ['attendance', 'stats', variables.siteId]
      });
    },
  });
};

// 실시간 출역 현황
export const useRealtimeAttendance = (siteId: number) => {
  return useQuery({
    queryKey: ['attendance', 'realtime', siteId],
    queryFn: () => apiClient.get<RealtimeAttendance>(`/attendance/realtime?siteId=${siteId}`),
    enabled: !!siteId,
    refetchInterval: 30 * 1000, // 30초마다 갱신
  });
};
```

### 3.4 레이아웃 컴포넌트
#### 3.4.1 메인 레이아웃
```typescript
// src/components/layout/MainLayout.tsx
interface MainLayoutProps {
  children: React.ReactNode;
  role: UserRole;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children, role }) => {
  const { sidebarCollapsed } = useUIStore();
  const isMobile = useMediaQuery('(max-width: 768px)');

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 */}
      <Header role={role} />
      
      <div className="flex">
        {/* 사이드바 (데스크톱만) */}
        {!isMobile && (
          <Sidebar 
            role={role} 
            collapsed={sidebarCollapsed}
            className="fixed left-0 top-16 h-[calc(100vh-4rem)] z-30"
          />
        )}
        
        {/* 메인 콘텐츠 */}
        <main 
          className={cn(
            "flex-1 p-6 transition-all duration-300",
            !isMobile && !sidebarCollapsed && "ml-64",
            !isMobile && sidebarCollapsed && "ml-16"
          )}
        >
          {children}
        </main>
      </div>
      
      {/* 모바일 하단 네비게이션 */}
      {isMobile && <BottomNavigation role={role} />}
      
      {/* 알림 토스트 */}
      <NotificationToast />
    </div>
  );
};
```

#### 3.4.2 역할별 사이드바
```typescript
// src/components/layout/Sidebar.tsx
interface SidebarProps {
  role: UserRole;
  collapsed: boolean;
  className?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ role, collapsed, className }) => {
  const menuItems = getMenuItemsByRole(role);
  
  return (
    <aside className={cn("bg-white border-r border-gray-200", className)}>
      <div className="p-4">
        {/* 로고 */}
        <div className="flex items-center space-x-2">
          <img src="/logo.png" alt="SmartCON" className="w-8 h-8" />
          {!collapsed && (
            <span className="font-bold text-lg text-gray-900">SmartCON</span>
          )}
        </div>
      </div>
      
      {/* 메뉴 항목 */}
      <nav className="mt-8">
        {menuItems.map((item) => (
          <SidebarItem
            key={item.path}
            item={item}
            collapsed={collapsed}
          />
        ))}
      </nav>
    </aside>
  );
};

// 역할별 메뉴 항목 정의
const getMenuItemsByRole = (role: UserRole): MenuItem[] => {
  const menuMap: Record<UserRole, MenuItem[]> = {
    ROLE_SUPER: [
      { path: '/super/dashboard', label: '대시보드', icon: LayoutDashboard },
      { path: '/super/tenants', label: '테넌트 관리', icon: Building },
      { path: '/super/billing', label: '결제 관리', icon: CreditCard },
      { path: '/super/system', label: '시스템 모니터링', icon: Monitor },
    ],
    ROLE_HQ: [
      { path: '/hq/dashboard', label: '대시보드', icon: LayoutDashboard },
      { path: '/hq/sites', label: '현장 관리', icon: MapPin },
      { path: '/hq/standards', label: '표준 관리', icon: Settings },
      { path: '/hq/reports', label: '전사 리포트', icon: FileText },
      { path: '/hq/subscription', label: '구독 관리', icon: CreditCard },
    ],
    ROLE_SITE: [
      { path: '/site/dashboard', label: '대시보드', icon: LayoutDashboard },
      { path: '/site/attendance', label: '출역 관리', icon: Clock },
      { path: '/site/work-assignments', label: '작업 배정', icon: Users },
      { path: '/site/daily-reports', label: '공사일보', icon: FileText },
      { path: '/site/teams', label: '팀 관리', icon: UserCheck },
      { path: '/site/contracts', label: '계약 관리', icon: FileSignature },
    ],
    ROLE_TEAM: [
      { path: '/team/dashboard', label: '홈', icon: Home },
      { path: '/team/work-requests', label: '작업 요청', icon: Briefcase },
      { path: '/team/members', label: '팀원 관리', icon: Users },
      { path: '/team/daily-report', label: '일보 작성', icon: Edit },
      { path: '/team/attendance', label: '출역 현황', icon: Clock },
    ],
    ROLE_WORKER: [
      { path: '/worker/dashboard', label: '홈', icon: Home },
      { path: '/worker/attendance', label: '출역 조회', icon: Clock },
      { path: '/worker/contracts', label: '계약서', icon: FileSignature },
      { path: '/worker/profile', label: '내 정보', icon: User },
    ],
  };
  
  return menuMap[role] || [];
};
```

### 3.5 폼 컴포넌트
#### 3.5.1 로그인 폼
```typescript
// src/components/forms/LoginForm.tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const loginSchema = z.object({
  businessNumber: z.string()
    .min(10, '사업자번호는 10자리여야 합니다.')
    .regex(/^\d{3}-?\d{2}-?\d{5}$/, '올바른 사업자번호 형식이 아닙니다.'),
  password: z.string()
    .min(8, '비밀번호는 8자 이상이어야 합니다.')
    .regex(/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
           '영문, 숫자, 특수문자를 포함해야 합니다.'),
});

type LoginFormData = z.infer<typeof loginSchema>;

interface LoginFormProps {
  onSubmit: (data: LoginFormData) => Promise<void>;
  isLoading?: boolean;
}

export const LoginForm: React.FC<LoginFormProps> = ({ onSubmit, isLoading }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  // 사업자번호 자동 하이픈 삽입
  const businessNumber = watch('businessNumber');
  useEffect(() => {
    if (businessNumber) {
      const formatted = businessNumber
        .replace(/\D/g, '')
        .replace(/(\d{3})(\d{2})(\d{5})/, '$1-$2-$3');
      if (formatted !== businessNumber) {
        setValue('businessNumber', formatted);
      }
    }
  }, [businessNumber, setValue]);

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader>
        <CardTitle>본사 관리자 로그인</CardTitle>
        <CardDescription>
          사업자번호와 비밀번호를 입력해주세요.
        </CardDescription>
      </CardHeader>
      
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <Label htmlFor="businessNumber">사업자번호</Label>
            <Input
              id="businessNumber"
              placeholder="123-45-67890"
              {...register('businessNumber')}
              error={errors.businessNumber?.message}
            />
          </div>
          
          <div>
            <Label htmlFor="password">비밀번호</Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              {...register('password')}
              error={errors.password?.message}
            />
          </div>
          
          <Button 
            type="submit" 
            className="w-full" 
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                로그인 중...
              </>
            ) : (
              '로그인'
            )}
          </Button>
        </form>
      </CardContent>
      
      <CardFooter className="text-center">
        <Link href="/login/social" className="text-sm text-blue-600 hover:underline">
          소셜 로그인으로 로그인하기
        </Link>
      </CardFooter>
    </Card>
  );
};
```

### 3.6 차트 컴포넌트
#### 3.6.1 출역 현황 차트
```typescript
// src/components/charts/AttendanceChart.tsx
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface AttendanceChartProps {
  data: AttendanceData[];
  className?: string;
}

export const AttendanceChart: React.FC<AttendanceChartProps> = ({ data, className }) => {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>일별 출역 현황</CardTitle>
        <CardDescription>최근 7일간 출역 인원 추이</CardDescription>
      </CardHeader>
      
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis 
              dataKey="date" 
              tickFormatter={(value) => format(new Date(value), 'MM/dd')}
            />
            <YAxis />
            <Tooltip 
              labelFormatter={(value) => format(new Date(value), 'yyyy년 MM월 dd일')}
              formatter={(value: number) => [`${value}명`, '출역 인원']}
            />
            <Line 
              type="monotone" 
              dataKey="attendanceCount" 
              stroke="#71AA44" 
              strokeWidth={2}
              dot={{ fill: '#71AA44', strokeWidth: 2, r: 4 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};
```

---

## 4. 모바일 최적화 (Capacitor)

### 4.1 Capacitor 설정
#### 4.1.1 capacitor.config.ts
```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.smartcon.lite',
  appName: 'SmartCON Lite',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    Camera: {
      permissions: ['camera', 'photos']
    },
    Geolocation: {
      permissions: ['location']
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert']
    },
    SplashScreen: {
      launchShowDuration: 2000,
      backgroundColor: '#71AA44',
      showSpinner: false
    }
  }
};

export default config;
```

### 4.2 네이티브 기능 훅
#### 4.2.1 카메라 훅
```typescript
// src/hooks/useCamera.ts
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { useState } from 'react';

export const useCamera = () => {
  const [isLoading, setIsLoading] = useState(false);

  const takePicture = async (options?: {
    quality?: number;
    allowEditing?: boolean;
    resultType?: CameraResultType;
    source?: CameraSource;
  }) => {
    setIsLoading(true);
    
    try {
      const image = await Camera.getPhoto({
        quality: options?.quality || 90,
        allowEditing: options?.allowEditing || false,
        resultType: options?.resultType || CameraResultType.DataUrl,
        source: options?.source || CameraSource.Camera,
      });

      return image;
    } catch (error) {
      console.error('Camera error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const requestPermissions = async () => {
    try {
      const permissions = await Camera.requestPermissions();
      return permissions.camera === 'granted';
    } catch (error) {
      console.error('Permission error:', error);
      return false;
    }
  };

  return {
    takePicture,
    requestPermissions,
    isLoading
  };
};
```

#### 4.2.2 위치 정보 훅
```typescript
// src/hooks/useGeolocation.ts
import { Geolocation, Position } from '@capacitor/geolocation';
import { useState, useEffect } from 'react';

export const useGeolocation = (options?: {
  enableHighAccuracy?: boolean;
  timeout?: number;
  maximumAge?: number;
}) => {
  const [position, setPosition] = useState<Position | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const getCurrentPosition = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const position = await Geolocation.getCurrentPosition({
        enableHighAccuracy: options?.enableHighAccuracy || true,
        timeout: options?.timeout || 10000,
        maximumAge: options?.maximumAge || 60000,
      });

      setPosition(position);
      return position;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '위치 정보를 가져올 수 없습니다.';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const requestPermissions = async () => {
    try {
      const permissions = await Geolocation.requestPermissions();
      return permissions.location === 'granted';
    } catch (error) {
      console.error('Location permission error:', error);
      return false;
    }
  };

  return {
    position,
    error,
    isLoading,
    getCurrentPosition,
    requestPermissions
  };
};
```

### 4.3 PWA 설정
#### 4.3.1 매니페스트 파일
```json
// public/manifest.json
{
  "name": "SmartCON Lite",
  "short_name": "SmartCON",
  "description": "건설 현장 인력 관리 플랫폼",
  "theme_color": "#71AA44",
  "background_color": "#FAFAFA",
  "display": "standalone",
  "orientation": "portrait",
  "scope": "/",
  "start_url": "/",
  "icons": [
    {
      "src": "icons/icon-72x72.png",
      "sizes": "72x72",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-96x96.png",
      "sizes": "96x96",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-128x128.png",
      "sizes": "128x128",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-144x144.png",
      "sizes": "144x144",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-152x152.png",
      "sizes": "152x152",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-384x384.png",
      "sizes": "384x384",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "maskable any"
    }
  ]
}
```

---

## 5. 설정 및 빌드

### 5.1 Vite 설정
#### 5.1.1 vite.config.ts
```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';
import path from 'path';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\.smartcon\.com\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365, // 1년
              },
              cacheKeyWillBeUsed: async ({ request }) => {
                return `${request.url}?version=1`;
              },
            },
          },
        ],
      },
      manifest: {
        name: 'SmartCON Lite',
        short_name: 'SmartCON',
        description: '건설 현장 인력 관리 플랫폼',
        theme_color: '#71AA44',
        icons: [
          {
            src: 'icons/icon-192x192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: 'icons/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png',
          },
        ],
      },
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          ui: ['@radix-ui/react-dialog', '@radix-ui/react-dropdown-menu'],
          charts: ['recharts'],
          utils: ['date-fns', 'clsx', 'tailwind-merge'],
        },
      },
    },
  },
});
```

### 5.2 TypeScript 설정
#### 5.2.1 tsconfig.json
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src", "tests"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 5.3 Tailwind CSS 설정
#### 5.3.1 tailwind.config.js
```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: ['class'],
  content: [
    './pages/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './src/**/*.{ts,tsx}',
  ],
  theme: {
    container: {
      center: true,
      padding: '2rem',
      screens: {
        '2xl': '1400px',
      },
    },
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: '#71AA44', // SmartCON 브랜드 컬러
          foreground: 'hsl(var(--primary-foreground))',
        },
        secondary: {
          DEFAULT: 'hsl(var(--secondary))',
          foreground: 'hsl(var(--secondary-foreground))',
        },
        destructive: {
          DEFAULT: 'hsl(var(--destructive))',
          foreground: 'hsl(var(--destructive-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--muted))',
          foreground: 'hsl(var(--muted-foreground))',
        },
        accent: {
          DEFAULT: 'hsl(var(--accent))',
          foreground: 'hsl(var(--accent-foreground))',
        },
        popover: {
          DEFAULT: 'hsl(var(--popover))',
          foreground: 'hsl(var(--popover-foreground))',
        },
        card: {
          DEFAULT: 'hsl(var(--card))',
          foreground: 'hsl(var(--card-foreground))',
        },
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
      keyframes: {
        'accordion-down': {
          from: { height: 0 },
          to: { height: 'var(--radix-accordion-content-height)' },
        },
        'accordion-up': {
          from: { height: 'var(--radix-accordion-content-height)' },
          to: { height: 0 },
        },
      },
      animation: {
        'accordion-down': 'accordion-down 0.2s ease-out',
        'accordion-up': 'accordion-up 0.2s ease-out',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
};
```

---

이 프론트엔드 아키텍처는 SmartCON Lite의 요구사항을 충족하는 현대적이고 확장 가능한 구조를 제공합니다. 다음으로 모바일 네이티브 앱 구조를 작성하겠습니다.