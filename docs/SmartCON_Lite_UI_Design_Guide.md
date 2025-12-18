# 스마트콘 라이트(SmartCON Lite) UI 설계 원칙 및 시스템

> **Context:** 본 문서는 '스마트콘 라이트' SaaS 플랫폼 및 모바일 앱 개발을 위한 UI 디자인 가이드라인입니다. Tailwind CSS 및 Shadcn/UI 라이브러리를 기준으로 작성되었으며, **실제 운영 중인 SmartCON 화면 구조**를 참고하여 작성되었습니다.

**문서 버전:** 3.8  
**최종 업데이트:** 2025년 12월 18일  
**작성자:** 경영기획실 이대영 이사  
**참고 화면:** 실제 SmartCON 운영 화면 (근로자 관리, 출역 관리, 인트로 페이지)  
**최적화:** PC웹(max-width: 1400px) / 모바일(전체 너비) 각각 최적화, 플랫 디자인 적용

## 목차
1. [UI 설계 원칙](#1-ui-설계-원칙)
2. [디자인 시스템](#2-디자인-시스템)
3. [레이아웃 및 그리드 시스템](#3-레이아웃-및-그리드-시스템)
4. [페이지별 UI 명세](#4-페이지별-ui-명세)
5. [공통 컴포넌트](#5-공통-컴포넌트)
6. [모바일 최적화](#6-모바일-최적화)
7. [접근성 가이드](#7-접근성-가이드)
8. [성능 최적화](#8-성능-최적화)
9. [보안 가이드](#9-보안-가이드)

---

## 1. UI 설계 원칙 (Design Principles)

스마트콘 라이트의 사용자 인터페이스는 다음의 핵심 원칙을 기반으로 설계되었습니다.

### 1.1. 직관성 (Intuitive)
사용자가 별도의 학습 없이 즉시 사용할 수 있도록 명확하고 일관된 인터페이스를 제공합니다. 모든 버튼, 아이콘, 레이블은 사용자의 멘탈 모델과 일치하도록 설계되어 있습니다.

### 1.2. 효율성 (Efficient)
건설 현장의 빠른 의사결정을 지원하기 위해 반복적인 작업을 최소화하고, 몇 번의 클릭만으로 핵심 과업을 완료할 수 있도록 설계합니다. 대시보드는 가장 중요한 정보를 상단에 배치하여 스캔 시간을 단축합니다.

### 1.3. 명확성 (Clear)
중요한 정보를 시각적으로 강조하고, 모호함 없는 레이블과 아이콘을 사용하여 오해를 방지합니다. 색상, 크기, 위치를 통해 정보의 계층구조를 명확히 표현합니다.

### 1.4. 일관성 (Consistent)
플랫폼 전체에 걸쳐 동일한 디자인 패턴과 컴포넌트를 사용하여 예측 가능한 사용자 경험을 제공합니다. 사용자가 한 곳에서 학습한 패턴을 다른 곳에서도 동일하게 적용할 수 있습니다.

### 1.5. 접근성 (Accessible)
WCAG 2.1 AA 수준을 준수하여 색상 대비, 폰트 크기, 키보드 탐색 등을 보장합니다. 모든 사용자, 특히 시각 또는 운동 능력이 제한된 사용자도 동등하게 서비스를 이용할 수 있도록 합니다.

### 1.6. 가독성 (Readability)
현장 사용자(40~50대)의 가독성을 최우선으로 고려하여 고대비 컬러 시스템과 기본 폰트 크기를 상향 조정합니다.

---

## 2. 디자인 시스템 (Design System)

### 2.1. 색상 팔레트 (Color Palette - Tailwind CSS Base)

현장 사용자(40~50대)의 가독성을 최우선으로 고려한 고대비 컬러 시스템입니다.
**회사 주색상 CMYK 55 10 95 00을 기반으로 구성된 색상 팔레트입니다.**

| 역할 | 색상명 | CMYK | Hex 코드 | RGB | 사용 사례 |
|:---|:---|:---|:---|:---|:---|
| **Primary** | Company Green | 55 10 95 00 | `#71AA44` | RGB(113, 170, 68) | 주요 버튼, 활성 상태, 브랜드 컬러 |
| **Primary Dark** | Dark Green | 70 30 100 10 | `#557C2C` | RGB(85, 124, 44) | Primary 호버 상태, 강조 요소 |
| **Primary Light** | Light Green | 30 0 60 0 | `#C2E199` | RGB(194, 225, 153) | Primary 배경, 연한 강조 |
| **Secondary** | Dark Gray | 0 0 0 80 | `#333333` | RGB(51, 51, 51) | 사이드바, 헤더 텍스트 |
| **Background** | Off-White | 0 0 98 | `#FAFAFA` | RGB(250, 250, 250) | 앱 배경색 (눈의 피로도 감소, 최적화된 밝기) |
| **Surface** | White | 0 0 0 0 | `#FFFFFF` | RGB(255, 255, 255) | 카드, 모달, 컨테이너 배경 |
| **Destructive** | Red | 0 100 100 0 | `#E63946` | RGB(230, 57, 70) | 삭제, 미서명 알림, 오류 메시지 |
| **Success** | Success Green | 55 10 95 00 | `#71AA44` | RGB(113, 170, 68) | 서명 완료, 출역 정상, 승인 (Primary와 동일) |
| **Warning** | Orange | 0 60 100 0 | `#FF8C42` | RGB(255, 140, 66) | 검토 대기, 진행 중 |

#### 상세 색상 정의

```css
/* 회사 주색상 기반 색상 팔레트 */
Primary: #71AA44 (CMYK 55 10 95 00) - 회사 주색상
Primary Dark: #557C2C - Primary 호버/강조
Primary Light: #C2E199 - Primary 배경/연한 강조
Secondary: #333333 - 텍스트, 사이드바
Background: #FAFAFA - 앱 배경 (눈의 피로를 최소화하는 부드러운 회색)
Surface: #FFFFFF - 카드, 모달 배경
Destructive: #E63946 - 오류, 삭제
Success: #71AA44 - 성공 (Primary와 동일)
Warning: #FF8C42 - 경고
```

#### 2.1.1. 눈이 편안한 색상 운영 원칙 (PC Web / Mobile Web 공통)

- **배경은 중립색(Neutral) 중심**: 큰 면적 배경은 `#FAFAFA`(Off-White)로 통일하고, **주색상(#71AA44)은 ‘포인트’로만 사용**합니다.
- **고채도 색상(노랑/형광/순색)은 큰 면적 금지**: 눈부심/피로를 유발하므로 배경/카드 전체 채움에 사용하지 않습니다.
- **주색상 사용 규칙(권장)**:
  - **Primary 버튼/핵심 CTA**: `#71AA44`
  - **Hover/Pressed**: `#557C2C`
  - **강조 배경(칩/배지/하이라이트)**: `#71AA44`에 5~12% 투명도(`/#71AA44/10` 등)만 사용
- **텍스트/아이콘은 회색 스케일 우선**: 본문은 `#262626` 수준의 다크 그레이, 보조 텍스트는 `#737373` 수준으로 대비를 확보합니다.
- **상태 색상은 의미 단위로 제한**: Success/Warning/Destructive만 운영하고, 의미가 겹치는 임의 색상 추가를 금지합니다.

### 2.2. 타이포그래피 (Typography)

가독성을 위해 기본 폰트 사이즈를 키우고, 시스템 폰트 스택을 사용합니다.

#### 폰트 패밀리

```css
font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'system-ui', sans-serif;
```

* **Font Stack**: `Pretendard`, `-apple-system`, `BlinkMacSystemFont`, `system-ui`, `sans-serif`
* **출처**: Pretendard는 한국어 가독성에 최적화된 폰트이며, 시스템 폰트로 폴백합니다.

#### 폰트 크기 및 스타일

| 역할 | 크기 | Tailwind 클래스 | 굵기 | 사용 사례 |
|:---|:---|:---|:---|:---|
| **H1 (Page Title)** | 24px | `text-2xl` | Bold (`font-bold`) | 페이지 제목, 메인 헤더 |
| **H2 (Section Title)** | 20px | `text-xl` | SemiBold (`font-semibold`) | 섹션 제목, 카드 제목 |
| **H3 (Card Title)** | 18px | `text-lg` | Medium (`font-medium`) | 카드 제목, 폼 그룹 제목 |
| **Body (Default)** | 16px | `text-base` | Normal (`font-normal`) | 일반 텍스트, 설명 (기본 크기 상향 조정) |
| **Caption** | 14px | `text-sm` | Normal (`font-normal`) | 보조 텍스트, 라벨, `text-gray-500` |

#### 행간 (Line Height)

* **제목**: 1.25 (타이트한 간격)
* **본문**: 1.5 (가독성을 위한 적절한 여백)
* **작은 텍스트**: 1.4 (균형잡힌 간격)

### 2.3. 아이콘 시스템 (Icon System)

* **아이콘 라이브러리**: Lucide React (Shadcn/UI 기본)
* **크기**: 16px (작음), 20px (중간), 24px (크음), 32px (매우 큼)
* **색상**: Primary 또는 Gray로 통일
* **사용 원칙**: 텍스트 라벨과 함께 사용하여 명확성 확보

### 2.4. 간격 시스템 (Spacing System)

스마트콘 라이트는 8px 기반의 간격 시스템을 사용합니다. 최신 웹 트렌드에 따라 넉넉한 여백을 제공하여 가독성과 시각적 편안함을 높입니다.

| 크기 | 픽셀 | Tailwind 클래스 | 사용 사례 |
|:---|:---|:---|:---|
| XS | 4px | `gap-1`, `p-1` | 아이콘과 텍스트 사이, 매우 작은 간격 |
| S | 8px | `gap-2`, `p-2` | 요소 내부 여백, 관련 요소 간 |
| M | 16px | `gap-4`, `p-4` | 요소 간 간격, 카드 내부 패딩 |
| L | 24px | `gap-6`, `p-6` | 섹션 간 간격, 카드 외부 패딩 |
| XL | 32px | `gap-8`, `p-8` | 주요 섹션 간 간격, 큰 여백 필요 시 |
| 2XL | 48px | `gap-12`, `p-12` | 페이지 섹션 간격, 대형 여백 |

#### 간격 사용 원칙

* **카드 내부**: `p-5` (20px) 또는 `p-6` (24px) - 넉넉한 여백
* **카드 간격**: `gap-4` (16px) 모바일, `gap-6` (24px) 데스크톱
* **섹션 간격**: `gap-6` (24px) 모바일, `gap-8` (32px) 데스크톱
* **텍스트 간격**: `space-y-4` (16px) - 읽기 편한 간격

### 2.5. 모서리 반경 (Border Radius)

| 크기 | 픽셀 | Tailwind 클래스 | 사용 사례 |
|:---|:---|:---|:---|
| Rounded-md | 6px | `rounded-md` | 버튼, 입력 필드, 카드 |
| Rounded-lg | 8px | `rounded-lg` | 카드, 모달 |
| Rounded-xl | 12px | `rounded-xl` | 큰 카드, 프로필 이미지 |

### 2.6. 그림자 시스템 (Shadow System)

깊이감과 계층 구조를 표현하기 위한 그림자 시스템입니다.

| 레벨 | Tailwind 클래스 | 사용 사례 |
|:---|:---|:---|
| **None** | `shadow-none` | 플랫한 요소 |
| **Small** | `shadow-sm` | 카드, 입력 필드 (기본) |
| **Medium** | `shadow-md` | 호버 상태의 카드, 버튼 |
| **Large** | `shadow-lg` | 모달, 드롭다운 메뉴 |
| **XL** | `shadow-xl` | 중요한 모달, 토스트 알림 |

### 2.7. 전환 애니메이션 (Transitions & Animations)

사용자 경험을 향상시키기 위한 부드러운 전환 효과입니다.

#### 전환 시간

| 효과 | 시간 | Tailwind 클래스 | 사용 사례 |
|:---|:---|:---|:---|
| **Fast** | 150ms | `transition-all duration-150` | 즉각적인 피드백 (버튼 클릭) |
| **Base** | 200ms | `transition-all duration-200` | 기본 전환 (기본값) |
| **Slow** | 300ms | `transition-all duration-300` | 레이아웃 변화, 모달 열림/닫힘 |

#### 전환 효과

```css
/* 기본 전환 */
transition-all duration-200 ease-in-out

/* 색상 전환만 */
transition-colors duration-200

/* 변환 전환만 (transform) */
transition-transform duration-200
```

#### 마이크로 인터랙션

* **버튼 호버**: `hover:scale-105` (약간 확대) 또는 `hover:shadow-md` (그림자 증가)
* **카드 호버**: `hover:shadow-lg hover:-translate-y-1` (위로 살짝 이동)
* **링크 호버**: `hover:underline` 또는 색상 변화
* **로딩 스피너**: `animate-spin` (회전 애니메이션)

---

## 3. 레이아웃 및 그리드 시스템 (Layout & Grid System)

반응형 웹(Responsive Web)을 지원하며, PC와 모바일 뷰를 명확히 구분합니다. 최신 CSS Grid와 Flexbox를 활용한 유연한 레이아웃 시스템을 사용합니다.

### 3.1. 그리드 시스템 (Grid System)

#### 반응형 그리드

| 화면 크기 | Breakpoint | Grid Columns | Gap | 사용 사례 |
|:---|:---|:---|:---|:---|
| **Mobile** | < 768px | `grid-cols-1` | `gap-4` | 단일 열 레이아웃 |
| **Tablet** | 768px ~ 1023px | `grid-cols-2` | `gap-6` | 2열 레이아웃 |
| **Desktop** | ≥ 1024px | `grid-cols-3` ~ `grid-cols-4` | `gap-6` ~ `gap-8` | 다열 레이아웃 |

#### 그리드 예시

```css
/* 대시보드 위젯 그리드 */
grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6

/* 카드 레이아웃 */
grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6

/* 리스트 레이아웃 */
flex flex-col gap-4
```

### 3.2. PC View (`md` breakpoint 이상, 768px~)

**디자인 기준**: 실제 SmartCON 운영 화면 (근로자 관리, 출역 관리 등) 구조를 기반으로 작성

#### 구조 (Structure)

* **Layout**: Top Header (고정) + Left Sidebar (고정 200~220px) + Main Content (Fluid) + Right Widget Area (Optional, 300~350px)
* **전체 레이아웃**: `flex flex-col h-screen` (상단 헤더 + 하단 메인 영역)
* **메인 영역**: `flex flex-1 overflow-hidden` (사이드바 + 콘텐츠 + 위젯)

#### 상단 헤더 (Top Header) - 실제 화면 기준

* **위치**: 최상단 고정 (`fixed top-0 w-full z-50`)
* **높이**: 50~60px
* **배경색**: `bg-[#71AA44]` (회사 주색상 - 녹색)
* **구성 요소** (좌→우):
  1. **햄버거 메뉴** (모바일용, PC에서는 숨김 가능)
  2. **SmartCON 로고** (좌측, 클릭 시 대시보드로 이동)
  3. **빈 공간** (flex-1)
  4. **검색 아이콘** (우측)
  5. **알림 아이콘** (우측, 배지 표시 가능)
  6. **사용자 프로필 또는 로그아웃** (최우측)

**헤더 스타일 예시**:
**로고 이미지**:
- **파일 경로**: `/logo_main_white.png` (흰색 로고, 녹색 배경용)
- **원본 위치**: `docs/logo_main_white.png`
- **사용 위치**: 상단 헤더, 사이드바, 인트로 페이지
- **권장 높이**: 32px~40px (h-8 ~ h-10)

```tsx
<header className="h-14 bg-[#71AA44] text-white flex items-center px-4 gap-4 shadow-md fixed top-0 w-full z-50">
  <button className="p-2 hover:bg-white/10 rounded-md lg:hidden">
    <Menu size={24} />
  </button>
  <button 
    className="flex items-center hover:opacity-90 transition-opacity" 
    onClick={() => navigate('/dashboard')}
    aria-label="대시보드로 이동"
  >
    <img 
      src="/logo_main_white.png" 
      alt="SmartCON" 
      className="h-9 object-contain"
    />
  </button>
  <div className="flex-1" />
  <button className="p-2 hover:bg-white/10 rounded-md">
    <Search size={20} />
  </button>
  <button className="p-2 hover:bg-white/10 rounded-md relative">
    <Bell size={20} />
    <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
  </button>
</header>
```

#### 좌측 사이드바 (Left Sidebar) - 실제 화면 기준

* **위치**: 좌측 고정 (`fixed left-0`, 상단 헤더 아래)
* **너비**: 200~220px (고정)
* **배경색**: `bg-white` 또는 `bg-gray-50`
* **테두리**: `border-r border-gray-200`
* **높이**: `calc(100vh - 헤더높이)` (스크롤 가능)

**구성 요소** (상→하):
1. **회사/프로필 섹션**
   - 회사명 표시
   - 사용자 프로필 사진 + 이름
   - 역할 표시 (예: "상무 관리자")
   
2. **다단계 메뉴 (Nested Menu)**
   - 대메뉴 (예: 업무협업, 전자결재, 문서관리, 사업관리, **작업관리**, 안전관리, 시스템관리)
   - 소메뉴 (대메뉴 클릭 시 펼쳐짐, 예: 작업관리 > **근로자 관리**, **출역 관리**, 출역일보 등)
   - 활성 메뉴는 배경색/테두리로 강조 (`bg-[#71AA44]/10`, `border-l-4 border-[#71AA44]`)

**사이드바 스타일 예시**:
```tsx
<aside className="w-[220px] bg-white border-r border-gray-200 fixed left-0 top-14 h-[calc(100vh-3.5rem)] overflow-y-auto">
  {/* 프로필 섹션 */}
  <div className="p-4 border-b border-gray-200">
    <div className="flex items-center gap-3">
      <div className="w-12 h-12 bg-gray-200 rounded-full" />
      <div>
        <div className="text-sm font-bold text-gray-900">홍길동</div>
        <div className="text-xs text-gray-500">상무 관리자</div>
      </div>
    </div>
  </div>
  
  {/* 메뉴 섹션 */}
  <nav className="p-2">
    {/* 대메뉴 */}
    <div className="mb-1">
      <button className="w-full text-left px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-md flex items-center justify-between">
        <span>작업관리</span>
        <ChevronDown size={16} />
      </button>
      {/* 소메뉴 (펼쳐진 상태) */}
      <div className="ml-4 mt-1 space-y-1">
        <button className="w-full text-left px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-md">
          근로자 관리
        </button>
        <button className="w-full text-left px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-md bg-[#71AA44]/10 border-l-4 border-[#71AA44]">
          출역 관리
        </button>
      </div>
    </div>
  </nav>
</aside>
```

##### 헤더 아이콘 배치

| 아이콘 | 위치 | 표시 조건 | 기능 |
|:---|:---|:---|:---|
| **다국어 변경** | 헤더 우측 | 모든 화면 | 언어 변경 드롭다운 (Globe 아이콘) |
| **현장 변경** | 헤더 우측 | 현장 관리자/팀장/노무자 | 현장 목록 선택 (Building2 아이콘) |
| **알림** | 헤더 우측 | 모든 화면 | 알림 패널 열기 (Bell 아이콘, 배지 표시) |

##### 헤더 아이콘 스타일

```tsx
// 기본 아이콘 버튼
<button className="p-2 rounded-md hover:bg-gray-100 transition-colors">
  <Icon size={20} className="text-gray-600" />
</button>

// 알림 배지가 있는 아이콘
<button className="p-2 rounded-md hover:bg-gray-100 transition-colors relative">
  <Bell size={20} className="text-gray-600" />
  <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
</button>
```

#### 메인 콘텐츠 영역 (Main Content Area) - 실제 화면 기준

* **위치**: 사이드바 우측, 상단 헤더 아래
* **레이아웃**: `ml-[220px] mt-14 flex-1 overflow-auto` (사이드바/헤더 고려)
* **패딩**: `p-4` ~ `p-6`
* **배경색**: `bg-gray-50` 또는 `bg-[#FAFAFA]`

**메인 콘텐츠 구조** (상→하):

1. **프로젝트 정보 헤더 (Project Info Header)**
   - 프로젝트명 또는 페이지 제목 (좌측)
   - D-Day 카운터, 진행률 등 (우측)
   - 배경: `bg-white`, 테두리: `border-b border-gray-200`
   
2. **필터/액션 버튼 영역 (Filter & Action Bar)**
   - 날짜 선택기, 드롭다운 필터 (좌측)
   - 액션 버튼 그룹 (우측, 예: 인원사세, 액셀업로드, 액셀다운로드, 일괄수정, 출역삭제 등)
   - 배경: `bg-white`, 패딩: `p-4`
   
3. **데이터 테이블 (Data Table)**
   - 테이블 헤더 (고정, `sticky top-0`)
   - 테이블 행 (스크롤 가능)
   - 행 호버 효과: `hover:bg-gray-50`
   - 셀 패딩: `px-4 py-3`

**메인 콘텐츠 예시**:
```tsx
<main className="ml-[220px] mt-14 flex-1 overflow-auto bg-gray-50">
  <div className="p-6 space-y-4">
    {/* 프로젝트 정보 헤더 */}
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">근로자 계약서 목록</h1>
          <p className="text-sm text-gray-500 mt-1">계약서 관리 조회</p>
        </div>
        <div className="flex items-center gap-6">
          <div className="text-right">
            <div className="text-sm text-gray-500">D-Day</div>
            <div className="text-2xl font-bold text-[#E63946]">-84일</div>
            <div className="text-xs text-gray-500">(147일 경과)</div>
          </div>
          <div className="text-right">
            <div className="text-3xl font-bold text-[#71AA44]">233.00%</div>
            <div className="text-xs text-gray-500">진행율</div>
          </div>
        </div>
      </div>
      {/* 날짜 슬라이더 */}
      <div className="mt-4 flex items-center gap-4">
        <span className="text-sm text-gray-600">시작일</span>
        <input type="range" className="flex-1" />
        <span className="text-sm text-gray-600">종료일</span>
      </div>
    </div>
    
    {/* 필터/액션 버튼 영역 */}
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          {/* 필터 드롭다운들 */}
          <select className="border border-gray-300 rounded-md px-3 py-2 text-sm">
            <option>구분</option>
          </select>
          <select className="border border-gray-300 rounded-md px-3 py-2 text-sm">
            <option>공종</option>
          </select>
          <input type="text" placeholder="성명 검색" className="border border-gray-300 rounded-md px-3 py-2 text-sm" />
          <Button size="sm">검색</Button>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm">인원사세</Button>
          <Button variant="outline" size="sm">액셀업로드</Button>
          <Button variant="outline" size="sm">액셀다운로드</Button>
          <Button variant="destructive" size="sm">출역삭제</Button>
        </div>
      </div>
    </div>
    
    {/* 데이터 테이블 */}
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      <table className="w-full">
        <thead className="bg-gray-50 border-b border-gray-200 sticky top-0">
          <tr>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">성명</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">직종</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">휴대전화</th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">생년월일</th>
            <th className="px-4 py-3 text-center text-sm font-semibold text-gray-700">계약서 보기</th>
          </tr>
        </thead>
        <tbody>
          {/* 테이블 행들 */}
          <tr className="border-b border-gray-100 hover:bg-gray-50">
            <td className="px-4 py-3 text-sm text-gray-900">김연호</td>
            <td className="px-4 py-3 text-sm text-gray-600">-</td>
            <td className="px-4 py-3 text-sm text-gray-600">-</td>
            <td className="px-4 py-3 text-sm text-gray-600">890605</td>
            <td className="px-4 py-3 text-center">
              <button className="p-1 hover:bg-gray-100 rounded">
                <Search size={16} className="text-gray-600" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</main>
```

#### 우측 위젯 영역 (Right Widget Area) - 실제 화면 기준

* **위치**: 메인 콘텐츠 우측 (Optional, 특정 화면에서만 표시)
* **너비**: 300~350px (고정)
* **배경색**: `bg-white`
* **테두리**: `border-l border-gray-200`
* **용도**: 신규출역 목록, 미서명 근로자 목록, 알림 등

**우측 위젯 예시**:
```tsx
<aside className="w-[350px] bg-white border-l border-gray-200 overflow-y-auto">
  <div className="p-4 space-y-4">
    {/* 위젯 헤더 */}
    <div className="bg-[#71AA44] text-white rounded-lg p-3">
      <h3 className="text-sm font-semibold">신규출역 및 미서명 근로자목록</h3>
      <button className="text-xs underline mt-1">더보기 &gt;</button>
    </div>
    
    {/* 위젯 테이블 */}
    <div className="border border-gray-200 rounded-lg overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-3 py-2 text-left text-xs font-semibold text-gray-700">성명</th>
            <th className="px-3 py-2 text-left text-xs font-semibold text-gray-700">생년월일</th>
            <th className="px-3 py-2 text-center text-xs font-semibold text-gray-700">사용여부</th>
          </tr>
        </thead>
        <tbody>
          <tr className="border-b border-gray-100">
            <td className="px-3 py-2 text-gray-900">김연호</td>
            <td className="px-3 py-2 text-gray-600">890605</td>
            <td className="px-3 py-2 text-center">
              <button className="p-1">
                <Edit size={14} className="text-[#71AA44]" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</aside>
```

### 3.3. Mobile View (`md` breakpoint 미만, ~767px)

#### 구조 (Structure)

* **Layout**: Top Header (Sticky) + Main Content + Bottom Navigation Bar (Fixed 60px)
* **Safe Area**: 노치 및 홈 인디케이터 대응
  * `padding-top: env(safe-area-inset-top)`
  * `padding-bottom: env(safe-area-inset-bottom)`
* **Container**: `w-full`, `px-4`, `py-4`

#### 헤더 (Header)

* **위치**: 상단 Sticky
* **배경색**: `bg-white`
* **구성**: 뒤로가기 버튼, 페이지 제목, 액션 버튼

#### 하단 네비게이션 (Bottom Navigation Bar)

* **위치**: 하단 고정
* **높이**: 60px
* **배경색**: `bg-white`
* **구성**: 4개 탭 (홈, 출역 조회, 계약서, 내 정보)

#### 메인 콘텐츠 (Main Content)

* **패딩**: `px-4`, `py-4`
* **Safe Area 적용**: `pb-20` (하단 네비게이션 높이 + Safe Area)

### 3.4. 메뉴 정보구조(IA) 가이드 (PC Web / Mobile Web)

#### 3.4.1. 공통 원칙

- **PC Web**: 사이드바는 “대메뉴 5~7개”만 노출하고, 상세 기능은 화면 내부 탭/필터/카드에서 확장
- **PC Web 대시보드 접근 정책**: 대시보드는 **로그인 후 최초 화면**이며 **사이드바 메뉴에는 노출하지 않음**. 대신 좌측 상단 **SmartCON 로고 클릭 = 대시보드(홈)**로 동작
- **Mobile Web**: **하단 탭 4개 고정**(홈/출역/계약/내정보), 나머지 기능은 홈의 위젯 또는 더보기/설정으로 제공
- **Mobile Web 홈 탭 정책**: 홈 탭은 항상 **대시보드로 이동** (첫 화면 복귀)
- **라벨 규칙**: 메뉴명은 동사보다 명사(예: “출역 관리”, “계약 관리”)를 우선하여 의미가 안정적으로 유지되도록 함
- **아이콘+라벨 병행**: PC/모바일 모두 아이콘만 사용하지 않고 라벨을 병기하여 오해 방지

#### 3.4.2. IA 재편안 (안 A/안 B)

##### 안 A: 역할(Role) 중심 IA (권장)

- **정책**: 대시보드는 **로그인 후 최초 화면**이며 메뉴에는 표시하지 않습니다. (PC: **SmartCON 로고 클릭 = 대시보드** / Mobile: **홈 탭 = 대시보드**)
- **본사 관리자(HQ)**: 현장 / 노무 / 계약 / 정산 / 설정
- **현장 관리자(Site)**: 출역 / 작업일보 / 계약 / 팀·노무자 / 정산
- **팀장(Team)**: 작업요청 / 일보제출 / 출역 / 계약 / 내정보
- **노무자(Worker)**: 출역조회 / 계약서 / 내정보

##### 안 B: 업무(Task) 중심 IA (확장안)

- **공통 상위 메뉴(PC)**: 홈(대시보드) / 현장 / 출역 / 작업일보 / 계약 / 정산 / 관리(설정/권한)
- **모바일 탭은 동일(4탭)**을 유지하되, 역할별로 홈 위젯/빠른 작업 구성을 다르게 제공

---

## 4. 페이지별 UI 명세 (Feature UI Specifications)

### 4.1. 인트로 페이지 (Landing Page)

**디자인 기준**: 실제 SmartCON 인트로 화면 구조 반영

#### 4.1.1. PC View

**레이아웃 구조**

* **배경**: 그라데이션 (녹색 → 청록 → 파랑)
  * `background: linear-gradient(135deg, #71AA44 0%, #4ECDC4 50%, #44A8F2 100%)`
  * 또는 Tailwind: `bg-gradient-to-r from-[#71AA44] via-[#4ECDC4] to-[#44A8F2]`
* **헤더**: 상단 고정, 로고 + 네비게이션 메뉴 + 로그인 버튼
  * 배경: 투명 또는 `bg-white/10 backdrop-blur`
  * 높이: `py-3` ~ `py-4`
  * 패딩: `px-4 sm:px-6`
  * 텍스트 색상: `text-white` (그라데이션 배경 위)
  * 메뉴 라벨(고정): **비대면바우처**, **스마트콘 소개**, **스마트콘 핵심기능**, **서비스요금**, **문의하기**, **로그인**
* **메인 콘텐츠**: 히어로(메인/서브 메시지 + CTA) → 소개/핵심기능/요금/문의 섹션
* **푸터**: 회사 정보(텍스트) + 연락처/주소 + Copyright

**메인 콘텐츠 영역** (실제 화면 기준)

* **레이아웃**: 2열 그리드 (좌측: 텍스트, 우측: 디바이스 이미지)
  * `grid grid-cols-1 lg:grid-cols-2 gap-12 items-center`
  * 패딩: `px-6 py-16 lg:py-24`
  * 최대 너비: `max-w-7xl mx-auto`

**좌측 텍스트 영역**:

1. **메인 타이틀**: "안전관리, 이제는 **스마트**하게!"
   * 폰트: `text-4xl lg:text-5xl xl:text-6xl font-bold`
   * 색상: `text-white`
   * "스마트" 부분: 강조 (볼드체 또는 다른 색상)
   * 행간: `leading-tight`

2. **서브 메시지**:
   * "본사와 **현장관리**까지"
   * "한 번에 **업무 끝**!"
   * 폰트: `text-xl lg:text-2xl`
   * 색상: `text-white/90`
   * "현장관리", "업무 끝" 강조

3. **브랜드명**: "**스마트콘** [SmartCON]"
   * 폰트: `text-2xl lg:text-3xl font-bold`
   * 색상: `text-yellow-400` 또는 `text-white`

**CTA 버튼**

* **문의 및 데모신청 버튼**:
  * 배경: `bg-gradient-to-r from-yellow-400 to-orange-500` (그라데이션 노란색→주황색)
  * 호버: `hover:shadow-2xl hover:scale-105`
  * 크기: `h-14 px-10 text-lg font-bold`
  * 텍스트 색상: `text-white` 또는 `text-gray-900`
  * 둥근 모서리: `rounded-full`
  * 애니메이션: `transition-all duration-300`

**우측 디바이스 이미지 영역**:

* **구성**: 데스크톱(큰 화면) + 모바일(작은 화면) 목업
* **데스크톱 목업**:
  * 화면 캡처 또는 스크린샷 (대시보드 화면)
  * 그림자: `shadow-2xl`
  * 테두리: `border-8 border-gray-800` (디바이스 프레임)
* **모바일 목업**:
  * 데스크톱 우측 하단에 겹쳐 배치 (`absolute bottom-0 right-0`)
  * 화면 캡처 (모바일 대시보드)
  * 그림자: `shadow-xl`
  * 크기: 데스크톱의 30~40% 정도

**예시 코드**:
```tsx
<div className="min-h-screen bg-gradient-to-r from-[#71AA44] via-[#4ECDC4] to-[#44A8F2] overflow-hidden">
  <div className="max-w-7xl mx-auto px-6 py-16 lg:py-24">
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
      {/* 좌측 텍스트 */}
      <div className="text-white space-y-8">
        <h1 className="text-4xl lg:text-5xl xl:text-6xl font-bold leading-tight">
          안전관리, 이제는 <span className="block mt-2">스마트하게!</span>
        </h1>
        <div className="text-xl lg:text-2xl space-y-2">
          <p>본사와 <strong>현장관리</strong>까지</p>
          <p>한 번에 <strong>업무 끝</strong>!</p>
        </div>
        <div className="text-2xl lg:text-3xl font-bold text-yellow-400">
          스마트콘 [SmartCON]
        </div>
        <button className="bg-gradient-to-r from-yellow-400 to-orange-500 text-gray-900 px-10 py-4 rounded-full text-lg font-bold shadow-xl hover:shadow-2xl hover:scale-105 transition-all duration-300">
          문의 및 데모신청
        </button>
      </div>
      
      {/* 우측 디바이스 목업 */}
      <div className="relative">
        {/* 데스크톱 */}
        <div className="relative z-10 border-8 border-gray-800 rounded-lg shadow-2xl overflow-hidden">
          <img src="/mockup-desktop.png" alt="Desktop" className="w-full" />
        </div>
        {/* 모바일 (겹침) */}
        <div className="absolute bottom-0 right-0 w-[40%] border-4 border-gray-800 rounded-xl shadow-xl overflow-hidden">
          <img src="/mockup-mobile.png" alt="Mobile" className="w-full" />
        </div>
        {/* 말풍선 (옵션) */}
        <div className="absolute bottom-12 right-12 bg-white text-gray-900 px-4 py-2 rounded-lg shadow-lg text-sm font-medium">
          스마트콘은 EPMS의 중소기업 입니다.
        </div>
      </div>
    </div>
  </div>
</div>
```

#### 4.1.2. Mobile View

* 헤더는 **로그인 버튼 중심**으로 단순화 (PC 메뉴 라벨은 모바일에서 숨김)
* 섹션 이동은 CTA/스크롤/앵커 방식으로 제공
* 메인 메시지 폰트 크기 조정 (`text-3xl`)
* 버튼 전체 너비 (`w-full`)
* 문의 폼: **단일 컬럼(세로 스택)**, 입력 간격 `gap-4`
* 푸터 정보: 2~3줄로 자연스럽게 줄바꿈되도록 구성

#### 4.1.3. 참고 사이트

* 디자인 참고: [`ismartcon.net 인트로`](https://www.ismartcon.net/login/smart/index)
* 주요 요소: 메인 메시지, 서브 메시지, 로그인 버튼, 문의 섹션

### 4.2. 로그인 방식 선택 페이지

**레이아웃**

* 중앙 정렬 카드 (`max-w-md`)
* 배경: `bg-[#FAFAFA]` (눈이 편안한 중립 배경)
* 카드: `shadow-xl`

**구성 요소**

* 제목: "로그인 방식 선택"
* 설명: "사용하실 로그인 방식을 선택해주세요"
* 두 가지 로그인 방식 선택 버튼
  * 본사 관리자 로그인: 아이콘 + 제목 + 설명
  * 소셜 로그인: 아이콘 + 제목 + 설명
* 뒤로가기 버튼

**버튼 스타일**

* 높이: `h-16`
* 레이아웃: `flex items-center justify-start gap-4`
* 테두리: `border-2`
* 호버: `hover:border-[#71AA44] hover:bg-[#71AA44]/5`
* 아이콘: `size={24} text-[#71AA44]`

### 4.3. Authentication (로그인/회원가입)

#### 레이아웃

* **PC**: Centered Card Layout (카드 중앙 배치)
* **Mobile**: Full Screen

#### 구성 요소

* **Logo**: 상단 중앙 배치
* **Form**: ID/PW 입력 필드
  * **Floating Label** 권장 (라벨이 입력 시 상단으로 이동)
* **Social Login**: 카카오/네이버/애플 아이콘 버튼
  * 가로 배치, `gap-4`
* **Action**: [로그인] 버튼
  * Full Width (`w-full`)
  * Primary Color (`bg-[#71AA44]`)
  * 높이: `h-12` (모바일 터치 영역 확보)

#### 인터랙션

* **로그인 실패 시**:
  * Input Border Red 색상 변경 (`border-red-500`)
  * 하단 에러 메시지 노출 (`text-red-500`)
* **로딩 시**:
  * 버튼 내 Spinner 표시
  * `disabled` 처리

### 4.2. Dashboard (Home)

사용자 권한에 따라 위젯 구성이 달라지는 그리드 레이아웃입니다.

#### A. 본사/현장 관리자용 (PC Web)

**Top Widgets (4 Cards)**

1. **출역 현황**
   * 금일 출역 인원 / 계획 인원
   * Progress Bar 표시
2. **계약 현황**
   * 체결 완료 수 / 미체결 수
   * 미체결 시 Red Badge (`bg-red-500`)
3. **안면인식기**
   * 기기 상태 표시
   * Online: Green (`text-green-600`) / Offline: Gray (`text-gray-500`)
4. **공정률**
   * 전체 공정률 %
   * Donut Chart 표시

**Main Content**

* **Tab**: [출역 현황] | [미서명 리스트] | [작업일보]
* **Data Table**: 주요 데이터 리스트
  * Pagination 포함
  * 정렬 기능

#### B. 노무자/팀장용 (Mobile App)

**Top Summary**

* 오늘 날짜 및 날씨 아이콘
* "오늘의 출역: **1.0 공수** (확인됨)" 카드

**Quick Actions (Grid Icon Menu)**

* [전자계약서 서명] (미서명 시 Red Dot 표시)
* [내 출역 조회]
* [급여 명세서]
* [작업일보 작성] (팀장 전용)

### 4.3. Contract Management (전자계약)

가장 중요한 '일괄 서명'과 '전자 서명' 프로세스입니다.

#### A. 관리자 뷰 (일괄 요청)

**List Item 구성**

* 체크박스 + 이름 + 공종 + 휴대폰번호 + 상태 뱃지

**Floating Action**

* 체크박스 선택 시 하단에 **[N명에게 일괄 서명 요청 보내기]** 플로팅 바 노출
* 배경색: `bg-[#71AA44]`
* 고정 위치: 하단 중앙

**Filter**

* [전체] | [요청 대기] | [서명 완료]
* Tab 스타일로 구현

#### B. 근로자 뷰 (서명 하기)

**PDF Viewer**

* 모바일 화면 너비에 맞춘 PDF 렌더링
* `react-pdf` 등 활용

**Sign Pad**

* 하단 고정 영역 또는 모달
* `Canvas` 영역 (손가락 서명)
* [지우기], [서명 완료] 버튼
* 가로 모드(Landscape) 지원 고려

### 4.4. Attendance (출역 관리)

#### Calendar View

* 월간 달력 표시
* **출역일**: 날짜 하단에 파란색 점(Dot) 또는 공수(1.0) 텍스트 표시
* **결근/누락**: 붉은색 표시 (`text-red-500`)

#### Detail Modal (날짜 클릭 시)

* 현장명, 출/퇴근 시간, 인정 공수, 단가 표시
* **Action**: [이의 제기] 버튼
  * Textarea가 포함된 모달 호출

### 4.5. Daily Report (작업 일보)

#### Step-by-Step Form (Mobile Team Leader)

* **Step 1**: 금일 작업 내용 (TextArea)
* **Step 2**: 현장 사진 업로드 (Image Picker + Preview)
* **Step 3**: 명일 계획 입력
* **Step 4**: 제출 확인

#### Admin View (Aggregation)

* 공종별 팀장들이 작성한 내용을 Accordion UI로 나열
* 각 섹션별 [승인] / [반려] 버튼 제공

---

## 5. 공통 컴포넌트 (Common Components - Shadcn/UI Base)

### 5.1. 버튼 (Button)

#### 버튼 크기 및 스타일

| 속성 | 값 | Tailwind 클래스 | 설명 |
|:---|:---|:---|:---|
| **높이** | 48px (12) | `h-12` | 모바일 터치 영역 확보 |
| **모서리** | 6px | `rounded-md` | 둥근 모서리 |
| **패딩** | 12px 24px | `px-6 py-3` | 적절한 클릭 영역 |
| **전환** | 200ms | `transition-all duration-200` | 부드러운 상태 변화 |

#### 버튼 유형

| 유형 | 배경색 | 텍스트색 | 호버 상태 | 사용 사례 |
|:---|:---|:---|:---|:---|
| **Primary** | `bg-[#71AA44]` | `text-white` | `hover:bg-[#557C2C] hover:shadow-md active:scale-95` | 주요 액션 (저장, 제출, 승인) |
| **Secondary** | `bg-gray-200` | `text-gray-900` | `hover:bg-gray-300 active:scale-95` | 보조 액션 (취소, 닫기) |
| **Destructive** | `bg-[#E63946]` | `text-white` | `hover:bg-[#D62839] hover:shadow-md active:scale-95` | 위험한 액션 (삭제, 거부) |
| **Outline** | `bg-white border` | `text-gray-900` | `hover:bg-gray-50 active:scale-95` | 보조 버튼, 테이블 액션 |
| **Ghost** | `transparent` | `text-gray-900` | `hover:bg-gray-100 active:scale-95` | 미니멀한 버튼 |
| **Disabled** | `bg-gray-300` | `text-gray-500` | `cursor-not-allowed` | 비활성 상태 |

#### 버튼 상태 (States)

* **Normal**: 기본 상태
* **Hover**: `hover:bg-[색상]-[단계]`, `hover:shadow-md` (약간의 그림자 증가)
* **Active**: `active:scale-95` (클릭 시 약간 축소)
* **Focus**: `focus:outline-none focus:ring-2 focus:ring-[#71AA44] focus:ring-offset-2` (키보드 포커스)
* **Disabled**: `disabled:opacity-50 disabled:cursor-not-allowed`

### 5.1.1. 링크 (Link)

#### 링크 스타일

| 유형 | 스타일 | Tailwind 클래스 | 사용 사례 |
|:---|:---|:---|:---|
| **Primary Link** | `text-[#71AA44] underline-offset-4` | `hover:underline hover:text-[#557C2C]` | 주요 링크, 내부 네비게이션 |
| **Secondary Link** | `text-gray-600 underline-offset-4` | `hover:underline hover:text-gray-900` | 보조 링크, 외부 링크 |
| **Button Link** | 버튼 스타일과 동일 | `hover:shadow-md` | 버튼처럼 보이는 링크 |

#### 링크 상태

* **Normal**: `text-[#71AA44]`
* **Hover**: `hover:text-[#557C2C] hover:underline`
* **Visited**: `visited:text-[#557C2C]` (선택적)
* **Focus**: `focus:outline-none focus:ring-2 focus:ring-[#71AA44] focus:ring-offset-2 rounded`

#### 접근성 고려사항

* 모든 링크는 명확한 텍스트를 포함해야 함 (예: "자세히 보기" vs "여기")
* 외부 링크는 아이콘으로 표시 권장 (`<ExternalLink />`)
* 링크 텍스트는 최소 16px 유지

### 5.2. 카드 (Card)

#### 카드 스타일

* **Shadow**: `shadow-sm` (기본), `shadow-md` (호버 시)
* **Border**: `border border-gray-200` (1px 테두리)
* **모서리**: `rounded-lg` (8px)
* **패딩**: `p-5` (20px) 또는 `p-6` (24px)
* **배경**: `bg-white`
* **전환**: `transition-all duration-200`

#### 카드 유형

| 유형 | 스타일 | 호버 효과 | 사용 사례 |
|:---|:---|:---|:---|
| **KPI Card** | 기본 스타일 | 없음 | 주요 지표 표시 (대시보드 위젯) |
| **Info Card** | 기본 스타일 | `hover:shadow-md` | 정보 요약 |
| **Action Card** | `cursor-pointer` | `hover:shadow-lg hover:-translate-y-1` | 클릭 가능한 카드 |
| **Interactive Card** | `cursor-pointer border-2 border-transparent` | `hover:border-blue-200 hover:shadow-lg hover:-translate-y-1` | 강조된 인터랙티브 카드 |

#### 카드 상태

* **Normal**: 기본 스타일
* **Hover**: 그림자 증가 + 약간의 위로 이동 (Action Card만)
* **Selected**: `border-[#71AA44] bg-[#C2E199]/20` (선택된 상태)

### 5.3. 입력 필드 (Input)

#### 입력 필드 스타일

| 속성 | 값 | Tailwind 클래스 | 설명 |
|:---|:---|:---|:---|
| **높이** | 48px (12) | `h-12` | 모바일 터치 영역 확보 |
| **폰트 크기** | 16px | `text-base` | 모바일 줌인 방지 (최소 16px) |
| **모서리** | 6px | `rounded-md` | 둥근 모서리 |
| **패딩** | 12px 16px | `px-4 py-3` | 적절한 내부 여백 |
| **전환** | 200ms | `transition-colors duration-200` | 부드러운 상태 변화 |

#### 입력 필드 상태

| 상태 | 테두리색 | 배경색 | 포커스 링 | 설명 |
|:---|:---|:---|:---|:---|
| **Normal** | `border-gray-300` | `bg-white` | - | 기본 상태 |
| **Hover** | `border-gray-400` | `bg-white` | - | 마우스 오버 |
| **Focus** | `border-[#71AA44]` | `bg-white` | `ring-2 ring-[#71AA44] ring-offset-2` | 포커스 상태 (접근성) |
| **Error** | `border-red-500` | `bg-white` | `ring-2 ring-red-500 ring-offset-2` | 유효성 검사 실패 |
| **Success** | `border-green-600` | `bg-white` | - | 검증 성공 |
| **Disabled** | `border-gray-200` | `bg-gray-100` | - | 비활성 상태 |

#### 입력 필드 인터랙션

* **Focus 시**: 테두리 색상 변화 + 링 효과 (`ring-2 ring-[#71AA44] ring-offset-2`)
* **에러 시**: 빨간색 테두리 + 에러 메시지 표시
* **성공 시**: 초록색 테두리 (선택적)

### 5.4. 모달 (Modal / Dialog)

#### 모달 유형

* **PC**: 중앙 팝업 (Modal)
* **Mobile**: Bottom Sheet 형태 권장 (아래에서 올라오는 시트)

#### 모달 구성 요소

* **Overlay**: 배경 어두워짐 (`bg-black/50`)
* **Modal Box**: 흰색 배경 (`bg-white`), 그림자 포함 (`shadow-xl`)
* **Header**: 모달 제목 및 닫기 버튼
* **Body**: 모달 콘텐츠
* **Footer**: 액션 버튼 (확인, 취소 등)

#### 모달 스타일

```tsx
// PC 모달
<DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
  <DialogHeader>
    <DialogTitle>제목</DialogTitle>
    <DialogDescription>설명</DialogDescription>
  </DialogHeader>
  {/* Body */}
  <DialogFooter>
    <Button variant="outline">취소</Button>
    <Button>확인</Button>
  </DialogFooter>
</DialogContent>

// 모바일 Bottom Sheet
<div className="fixed bottom-0 left-0 right-0 bg-white rounded-t-lg shadow-xl z-50">
  {/* Handle */}
  <div className="flex justify-center pt-3 pb-2">
    <div className="w-12 h-1 bg-gray-300 rounded-full" />
  </div>
  {/* Content */}
</div>
```

#### 모달 유형별 스타일

| 유형 | 너비 | 사용 사례 |
|:---|:---|:---|
| **Small** | `sm:max-w-[400px]` | 확인 모달, 간단한 알림 |
| **Medium** | `sm:max-w-[600px]` | 등록/수정 폼, 상세보기 |
| **Large** | `sm:max-w-[800px]` | 복잡한 폼, 데이터 테이블 |
| **Full** | `max-w-full` | 모바일 전용, 전체 화면 |

#### 모달 애니메이션

* **PC**: Fade in + Scale (중앙에서 나타남)
* **Mobile**: Slide up (아래에서 올라옴)
* **전환 시간**: 300ms

### 5.5. 테이블 (Table)

#### 테이블 구성 요소

* **Header Row**: 열 제목, 정렬 아이콘
  * 배경색: `bg-gray-50`
  * 텍스트: `text-gray-900 font-semibold`
  * 패딩: `px-4 py-3`
* **Data Row**: 실제 데이터
  * 패딩: `px-4 py-3`
  * 전환: `transition-colors duration-150`
* **Footer**: 페이지네이션, 행 수 선택

#### 테이블 기능

* 정렬 (Sorting): 열 헤더 클릭으로 오름차순/내림차순 정렬
  * 정렬 가능 헤더: `cursor-pointer hover:bg-gray-100`
  * 정렬 아이콘: `hover:text-[#71AA44]`
* 필터링 (Filtering): 특정 조건으로 데이터 필터링
* 페이지네이션 (Pagination): 페이지 단위로 데이터 표시
* 행 선택 (Row Selection): 체크박스로 여러 행 선택

#### 테이블 행 상태

| 상태 | 배경색 | 테두리 | 설명 |
|:---|:---|:---|:---|
| **Normal** | Transparent | - | 기본 상태 |
| **Hover** | `bg-gray-50` | - | 마우스 오버 (부드러운 전환) |
| **Selected** | `bg-[#C2E199]/20` | `border-l-4 border-[#71AA44]` | 선택된 행 (왼쪽 테두리 강조) |
| **Disabled** | `bg-gray-100` | - | 비활성 행 (투명도 감소) |

#### 테이블 스타일

```css
/* 기본 테이블 */
table {
  width: 100%;
  border-collapse: collapse;
}

/* 헤더 */
thead tr {
  background-color: rgb(249 250 251); /* gray-50 */
  border-bottom: 2px solid rgb(229 231 235); /* gray-200 */
}

/* 행 호버 효과 */
tbody tr {
  transition: background-color 150ms ease-in-out;
}

tbody tr:hover {
  background-color: rgb(249 250 251); /* gray-50 */
}
```

### 5.6. 상태 배지 (Status Badge)

| 상태 | 배경색 | 텍스트색 | 사용 사례 |
|:---|:---|:---|:---|
| **완료 (Complete)** | `bg-green-100` | `text-green-600` | 승인 완료, 서명 완료 |
| **진행 중 (In Progress)** | `bg-[#C2E199]/30` | `text-[#71AA44]` | 검토 중, 작성 중 |
| **대기 (Pending)** | `bg-amber-100` | `text-amber-500` | 승인 대기, 미서명 |
| **오류 (Error)** | `bg-red-100` | `text-red-500` | 오류, 거부 |

### 5.7. 알림 및 토스트 (Toast Notification)

#### 토스트 유형

| 유형 | 배경색 | 아이콘 | 사용 사례 |
|:---|:---|:---|:---|
| **Success** | `bg-green-600` | CheckCircle | 저장 완료, 승인 완료 |
| **Error** | `bg-red-500` | XCircle | 오류 발생, 저장 실패 |
| **Warning** | `bg-amber-500` | AlertTriangle | 경고 메시지 |
| **Info** | `bg-[#71AA44]` | Info | 정보 알림 |

#### 토스트 스타일

```tsx
// 토스트 예시
<div className="fixed top-4 right-4 bg-green-600 text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-2 z-50">
  <CheckCircle size={20} />
  <div>
    <p className="font-semibold">저장 완료</p>
    <p className="text-sm opacity-90">변경사항이 저장되었습니다.</p>
  </div>
</div>
```

#### 토스트 위치

* **PC**: 상단 우측 (`top-4 right-4`)
* **Mobile**: 상단 중앙 (`top-4 left-4 right-4`)
* **자동 사라짐**: 3초 후 (설정 가능)
* **스택**: 여러 토스트가 있을 경우 수직으로 쌓임

### 5.8. 로딩 및 스켈레톤 (Loading & Skeleton)

데이터 로딩 중에는 빈 화면 대신 **Skeleton UI (Shimmer Effect)**를 표시하여 이탈을 방지합니다.

* **대시보드 위젯**: Skeleton 카드 표시
* **리스트 뷰**: Skeleton 행 표시
* **이미지**: Skeleton 이미지 플레이스홀더 표시

#### Skeleton 스타일

```css
/* 기본 Skeleton */
.skeleton {
  background: linear-gradient(90deg, 
    rgb(243 244 246) 25%, 
    rgb(229 231 235) 50%, 
    rgb(243 244 246) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
```

### 5.9. 포커스 표시 (Focus Indicators)

접근성을 위한 명확한 포커스 표시입니다.

#### 포커스 스타일

```css
/* 기본 포커스 링 (회사 주색상) */
focus:outline-none
focus:ring-2
focus:ring-[#71AA44]
focus:ring-offset-2

/* 포커스 테두리 */
focus:outline-none
focus:border-[#71AA44]
focus:ring-2
focus:ring-[#71AA44]
focus:ring-offset-2
```

#### 포커스 사용 원칙

* 모든 인터랙티브 요소는 키보드 포커스 가능해야 함
* 포커스 링은 명확하게 보여야 함 (2px 링, 2px offset)
* 포커스 색상: Primary 색상 사용 (`ring-[#71AA44]`)

---

## 6. 모바일 최적화 (Mobile Optimization - 필수)

### 6.1. 터치 영역 (Touch Target)

모든 버튼 및 클릭 요소는 최소 **44x44px** 이상이어야 합니다.

* **권장 크기**: 48px × 48px (Tailwind `h-12`)
* **최소 간격**: 8px (터치 영역 간)

### 6.2. 가상 키보드 (Virtual Keyboard)

입력 필드 포커스 시 키보드가 UI를 가리지 않도록 `ScrollIntoView` 처리 구현

* 입력 필드에 포커스 시 자동 스크롤
* 키보드가 UI 요소를 가리지 않도록 레이아웃 조정

### 6.3. Bottom Sheet

모바일에서 복잡한 모달 대신 아래에서 올라오는 Bottom Sheet 사용 (UX 표준)

* **애니메이션**: 아래에서 위로 슬라이드
* **배경**: 반투명 오버레이 (`bg-black/50`)
* **높이**: 콘텐츠에 따라 동적 조정 (최대 화면 높이의 90%)

### 6.4. Deep Link Handling UX

초대 링크로 진입 시 **[앱 설치 유도 배너]** 또는 **[웹으로 계속하기]** 선택 인터스티셜(Interstitial) 페이지 구현

* 앱이 설치되어 있으면 앱으로 이동
* 앱이 설치되어 있지 않으면 웹으로 계속하기 또는 앱 다운로드 유도

### 6.5. Safe Area 대응

노치 및 홈 인디케이터 대응

```css
padding-top: env(safe-area-inset-top);
padding-bottom: env(safe-area-inset-bottom);
padding-left: env(safe-area-inset-left);
padding-right: env(safe-area-inset-right);
```

---

## 7. 접근성 가이드 (Accessibility Guidelines)

### 7.1. 색상 대비 (Color Contrast)

모든 텍스트와 배경의 색상 대비는 WCAG 2.1 AA 수준을 만족합니다.

| 요소 | 대비 비율 | 기준 |
|:---|:---|:---|
| 일반 텍스트 | 4.5:1 이상 | AA 레벨 |
| 큰 텍스트 (18pt+) | 3:1 이상 | AA 레벨 |
| UI 컴포넌트 | 3:1 이상 | AA 레벨 |

### 7.2. 폰트 크기 (Font Size)

* **최소 폰트 크기**: 14px (Caption)
* **권장 폰트 크기**: 16px (일반 텍스트)
* **모바일**: 16px 이상 (줌 없이 읽을 수 있어야 함)

### 7.3. 터치 영역 (Touch Target Size)

* **최소 크기**: 44px × 44px (모바일)
* **권장 크기**: 48px × 48px (모바일, Tailwind `h-12`)
* **최소 간격**: 8px (터치 영역 간)

### 7.4. 키보드 네비게이션 (Keyboard Navigation)

* 모든 기능은 키보드로 접근 가능해야 함
* Tab 키로 순차적으로 이동 가능
* Enter 키로 버튼 활성화 가능
* Escape 키로 모달 닫기 가능

---

## 8. 성능 최적화 (Performance Optimization)

### 8.1. 로딩 성능 (Loading Performance)

| 메트릭 | 목표 | 설명 |
|:---|:---|:---|
| **LCP (Largest Contentful Paint)** | < 2.5s | 가장 큰 콘텐츠 요소의 로딩 시간 |
| **FID (First Input Delay)** | < 100ms | 사용자 입력에 대한 반응 시간 |
| **CLS (Cumulative Layout Shift)** | < 0.1 | 예상치 못한 레이아웃 변경 |

### 8.2. 이미지 최적화 (Image Optimization)

* WebP 형식 사용 (PNG/JPG 폴백)
* 반응형 이미지 (srcset 사용)
* 지연 로딩 (Lazy Loading)
* 이미지 압축 (최대 100KB)

### 8.3. 캐싱 전략 (Caching Strategy)

* 정적 자산: 1년 캐시
* API 응답: 5분 캐시
* 사용자 데이터: 세션 캐시

---

## 9. 보안 가이드 (Security Guidelines)

### 9.1. 데이터 마스킹 (Data Masking)

* **주민등록번호**: 앞 6자리만 표시 (예: 123456-*****)
* **계좌번호**: 뒤 4자리만 표시 (예: ****-****-****-1234)
* **전화번호**: 중간 4자리 마스킹 (예: 010-****-1234)

### 9.2. 세션 관리 (Session Management)

* **세션 타임아웃**: 30분 (비활성 시)
* **자동 로그아웃**: 타임아웃 후 자동 로그아웃
* **로그아웃 경고**: 타임아웃 5분 전 경고 메시지

### 9.3. 입력 검증 (Input Validation)

* 클라이언트 측 검증 (UX 개선)
* 서버 측 검증 (보안)
* XSS 방지 (HTML 이스케이프)
* SQL Injection 방지 (파라미터화된 쿼리)

---

## 10. 개발 체크리스트 (Development Checklist)

### 10.1. 디자인 일관성

- [ ] 모든 버튼이 디자인 시스템의 버튼 스타일을 따르는가? (`h-12`, `rounded-md`)
- [ ] 모든 입력 필드가 표준화된 스타일을 사용하는가? (`h-12`, `text-base`)
- [ ] 색상이 정의된 팔레트에서만 사용되는가? (회사 주색상 #71AA44, Dark Gray #333333, Gray-50 등)
- [ ] 타이포그래피가 정의된 크기와 굵기를 따르는가? (H1: 24px, Body: 16px)

### 10.2. 반응형 설계

- [ ] PC(1920px), 태블릿(768px), 모바일(375px)에서 테스트했는가?
- [ ] 모든 요소가 적절히 스케일되는가?
- [ ] 터치 영역이 모바일에서 최소 44px 이상인가?

### 10.3. 접근성

- [ ] 색상 대비가 WCAG AA 수준을 만족하는가?
- [ ] 모든 이미지에 alt 텍스트가 있는가?
- [ ] 키보드 네비게이션이 가능한가?
- [ ] 스크린 리더에서 올바르게 읽혀지는가?

### 10.4. 성능

- [ ] LCP가 2.5초 이내인가?
- [ ] 이미지가 최적화되어 있는가?
- [ ] 캐싱 전략이 구현되어 있는가?
- [ ] Skeleton UI가 로딩 중에 표시되는가?

### 10.5. 보안

- [ ] 민감한 데이터가 마스킹되어 있는가?
- [ ] 세션 타임아웃이 구현되어 있는가?
- [ ] 입력 검증이 클라이언트와 서버에서 모두 수행되는가?

### 10.6. 모바일 최적화

- [ ] 모든 버튼이 최소 44x44px 이상인가?
- [ ] 가상 키보드가 UI를 가리지 않는가?
- [ ] Bottom Sheet를 모바일 모달로 사용했는가?
- [ ] Safe Area가 적용되어 있는가?

### 10.7. 인터랙션 및 애니메이션

- [ ] 버튼에 호버/액티브 상태가 구현되어 있는가?
- [ ] 전환 애니메이션이 부드럽게 작동하는가? (200ms 권장)
- [ ] 링크에 명확한 호버 상태가 있는가?
- [ ] 카드에 적절한 호버 효과가 있는가? (Action Card만)
- [ ] 포커스 표시가 명확하게 보이는가?

### 10.8. 그리드 및 레이아웃

- [ ] 반응형 그리드가 올바르게 작동하는가?
- [ ] 간격 시스템이 일관되게 적용되어 있는가?
- [ ] 섹션 간 여백이 충분한가? (최소 24px)

### 10.9. 팝업 및 모달

- [ ] 삭제 확인 팝업이 모든 삭제 버튼에 구현되어 있는가?
- [ ] 등록/수정 팝업이 구현되어 있는가?
- [ ] 상세보기 팝업이 리스트 항목 클릭 시 열리는가?
- [ ] 승인/거부 확인 팝업이 구현되어 있는가?
- [ ] 모바일에서 Bottom Sheet가 적용되어 있는가? (복잡한 모달)
- [ ] 모달 오버레이 클릭 시 닫힘 기능이 구현되어 있는가?
- [ ] Escape 키로 모달 닫기가 가능한가?

### 10.10. 헤더 아이콘 및 기능

- [ ] 다국어 변경 아이콘이 모든 헤더에 배치되어 있는가?
- [ ] 현장 변경 아이콘이 현장 관리자/팀장/노무자 헤더에 배치되어 있는가?
- [ ] 알림 아이콘이 모든 헤더에 배치되어 있는가?
- [ ] 알림 배지가 미읽음 알림이 있을 때 표시되는가?
- [ ] 알림 패널이 올바르게 구현되어 있는가?
- [ ] 드롭다운 메뉴(언어, 현장 선택)가 올바르게 작동하는가?
- [ ] 토스트 알림이 성공/오류 상황에 표시되는가?

---

## 11. 추가 리소스 (Additional Resources)

### 11.1. 참고 문서

* WCAG 2.1 Accessibility Guidelines: https://www.w3.org/WAI/WCAG21/quickref/
* Tailwind CSS Documentation: https://tailwindcss.com/docs
* Shadcn/UI Documentation: https://ui.shadcn.com/

### 11.2. 디자인 도구

* Figma (디자인 협업 도구)
* Storybook (컴포넌트 라이브러리)

### 11.3. 개발 라이브러리

* Tailwind CSS (스타일링)
* Shadcn/UI (컴포넌트 라이브러리)
* React (UI 프레임워크)
* TypeScript (타입 안정성)

---

---

## 12. 최신 웹 UI 트렌드 반영 (2025 Web UI Trends)

### 12.1. 마이크로 인터랙션 (Micro-interactions)

작은 애니메이션과 피드백을 통해 사용자 경험을 향상시킵니다.

* **버튼 클릭**: `active:scale-95` (약간 축소)
* **카드 호버**: `hover:-translate-y-1` (위로 이동) + 그림자 증가
* **로딩 스피너**: `animate-spin` (회전)
* **성공 피드백**: 체크 아이콘 애니메이션

### 12.2. 명확한 상태 표시

모든 인터랙티브 요소는 명확한 상태를 표시합니다.

* **Hover**: 색상 변화 + 그림자 증가
* **Active**: 약간의 축소 효과
* **Focus**: 명확한 링 표시 (접근성)
* **Disabled**: 투명도 감소 + 커서 변경

### 12.3. 넉넉한 여백 (Breathing Room)

최신 트렌드는 더 넉넉한 여백을 선호합니다.

* 카드 패딩: 최소 20px (모바일) ~ 24px (데스크톱)
* 섹션 간격: 최소 24px (모바일) ~ 32px (데스크톱)
* 요소 간격: 최소 16px

### 12.4. 부드러운 전환

모든 상태 변화는 부드러운 전환 효과를 가집니다.

* 기본 전환: 200ms
* 즉각 피드백: 150ms (버튼 클릭)
* 레이아웃 변화: 300ms (모달 열림/닫힘)

### 12.5. 명확한 계층 구조

그림자와 색상을 활용한 명확한 시각적 계층 구조.

* **Level 1**: 플랫 (그림자 없음) - 기본 요소
* **Level 2**: `shadow-sm` - 카드
* **Level 3**: `shadow-md` - 호버 상태
* **Level 4**: `shadow-lg` - 모달, 드롭다운

### 12.6. 참고 레퍼런스 (PC Web / Mobile Web 내비게이션 패턴)

- [Material Design 3 (Navigation)](https://m3.material.io/foundations/navigation/overview)
- [Apple Human Interface Guidelines (Navigation)](https://developer.apple.com/design/human-interface-guidelines/)
- [Microsoft Fluent 2 (Design)](https://fluent2.microsoft.design/)

---

**문서 버전**: 3.8  
**최종 업데이트**: 2025년 12월 18일  
**작성자**: 경영기획실 이대영 이사  
**참고**: 실제 SmartCON 운영 화면 구조 반영 (근로자 관리, 출역 관리, 인트로 페이지)  
**최적화**: PC웹(max-width: 1400px) / 모바일(전체 너비) 각각 최적화, 플랫 디자인 적용  
**상태**: Phase 1 - Core Features 기준
**기준 라이브러리**: Tailwind CSS + Shadcn/UI  
**최신 트렌드 반영**: 2025년 웹 UI 트렌드 반영
