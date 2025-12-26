# SmartCON Lite Frontend

SmartCON Lite의 프론트엔드 애플리케이션입니다. React 18 + TypeScript + Vite를 기반으로 구축되었습니다.

## 기술 스택

- **Framework**: React 18 + TypeScript + Vite
- **UI System**: Shadcn/UI + Tailwind CSS
- **State Management**: Zustand (client state) + TanStack Query (server state)
- **Routing**: Wouter (lightweight React router)
- **Mobile Bridge**: Capacitor 6.x (Camera, Geolocation, Push notifications)
- **Testing**: Vitest + React Testing Library

## 프로젝트 구조

```
frontend/src/
├── components/
│   ├── ui/                # Shadcn/UI base components
│   └── layout/            # Layout components
├── pages/                 # Route components organized by role
│   ├── auth/              # Authentication pages
│   ├── hq/                # HQ admin pages
│   ├── site/              # Site manager pages
│   ├── worker/            # Worker pages
│   └── super/             # Super admin pages
├── contexts/              # React contexts (Theme, etc.)
├── hooks/                 # Custom React hooks
├── stores/                # Zustand state stores
├── mock-data/             # Development mock data
└── lib/                   # Utilities and configurations
```

## 개발 명령어

```bash
# 의존성 설치
npm install

# 개발 서버 시작
npm run dev

# 프로덕션 빌드
npm run build

# 테스트 실행
npm run test

# 테스트 실행 (watch 모드 없이)
npm run test:run
```

## 역할 기반 라우팅

- **HQ Admin**: `/hq/*` - 본사 관리자 페이지
- **Site Manager**: `/site/*` - 현장 관리자 페이지  
- **Worker**: `/worker/*` - 근로자 페이지
- **Super Admin**: `/super/*` - 슈퍼 관리자 페이지
- **Auth**: `/login/*` - 인증 관련 페이지

## 개발 가이드

### 컴포넌트 구조
- **UI Components**: `components/ui/`에 재사용 가능한 기본 컴포넌트
- **Layout Components**: `components/layout/`에 레이아웃 관련 컴포넌트
- **Page Components**: `pages/`에 역할별로 구분된 페이지 컴포넌트

### 상태 관리
- **Client State**: Zustand를 사용한 클라이언트 상태 관리
- **Server State**: TanStack Query를 사용한 서버 상태 관리

### 스타일링
- **Tailwind CSS**: 유틸리티 기반 CSS 프레임워크
- **Shadcn/UI**: 커스텀 디자인 시스템 기반 컴포넌트

### 테스트
- **Unit Tests**: Vitest + React Testing Library
- **Component Tests**: 컴포넌트별 단위 테스트
- **Integration Tests**: 페이지 및 기능별 통합 테스트

## 프로덕션 배포

```bash
# 프로덕션 빌드
npm run build

# 빌드 결과물은 dist/ 폴더에 생성됩니다
```

## 개발 환경 설정

1. Node.js 18+ 설치
2. 의존성 설치: `npm install`
3. 개발 서버 시작: `npm run dev`
4. 브라우저에서 `http://localhost:5173` 접속

## 주요 특징

- **Multi-tenant SaaS**: 테넌트별 데이터 격리
- **Role-based Access**: 5단계 사용자 역할 지원
- **Mobile-first Design**: 모바일 우선 반응형 디자인
- **Real-time Updates**: 실시간 데이터 업데이트
- **Offline Support**: 오프라인 모드 지원 (Capacitor)