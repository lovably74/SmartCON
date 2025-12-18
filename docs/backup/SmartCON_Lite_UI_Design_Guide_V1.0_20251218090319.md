# 스마트콘 라이트(SmartCON Lite) UI 설계 원칙 및 시스템

## 목차
1. [UI 설계 원칙](#1-ui-설계-원칙)
2. [디자인 시스템](#2-디자인-시스템)
3. [전체 메뉴 구조도](#3-전체-메뉴-구조도)
4. [네비게이션 패턴](#4-네비게이션-패턴)
5. [공통 컴포넌트](#5-공통-컴포넌트)

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

---

## 2. 디자인 시스템 (Design System)

### 2.1. 색상 팔레트 (Color Palette)

#### 주요 색상

| 역할 | 색상명 | HEX 코드 | RGB | 사용 사례 |
|------|--------|---------|-----|----------|
| Primary | Navy | `#1A2B3C` | 26, 43, 60 | 배경, 기본 텍스트, 주요 구조 요소 |
| Secondary | Teal | `#4ECDC4` | 78, 205, 196 | 버튼, 활성 상태, 강조 요소, 링크 |
| Accent | Orange | `#FF6B00` | 255, 107, 0 | 알림, 경고, 페이지 번호, 강조 테두리 |
| Text | White | `#FFFFFF` | 255, 255, 255 | 기본 텍스트 |
| Sub-text | Gray | `#B0B0B0` | 176, 176, 176 | 보조 텍스트, 비활성 요소 |
| Border | Dark Gray | `#37474F` | 55, 71, 79 | 입력 필드 테두리, 구분선 |

#### 상태별 색상

| 상태 | 색상 | HEX 코드 | 설명 |
|------|------|---------|------|
| Success | Green | `#009688` | 완료, 성공, 활성 상태 |
| Warning | Orange | `#FF9800` | 주의 필요, 검토 중, Phase 2 기능 |
| Error | Red | `#F44336` | 오류, 실패, 삭제 |
| Info | Blue | `#2196F3` | 정보, 안내 |

### 2.2. 타이포그래피 (Typography)

#### 폰트 패밀리

| 언어 | 용도 | 글꼴 | 출처 |
|------|------|------|------|
| 영문 | 헤드라인, 제목 | Montserrat | Google Fonts |
| 국문 | 본문, 일반 텍스트 | Noto Sans KR | Google Fonts |
| 코드/데이터 | 기술 정보, ID, 숫자 | Roboto Mono | Google Fonts |

#### 폰트 크기 및 스타일

| 역할 | 크기 | 굵기 | 자간 | 사용 사례 |
|------|------|------|------|----------|
| H1 (페이지 제목) | 36px | 800 (Bold) | -0.5px | 슬라이드 제목, 페이지 헤더 |
| H2 (섹션 제목) | 28px | 700 (Bold) | -0.5px | 섹션 헤더, 모달 제목 |
| H3 (소제목) | 18px | 700 (Bold) | -0.25px | 카드 제목, 폼 그룹 제목 |
| Body (본문) | 14px | 400 (Regular) | 0px | 일반 텍스트, 설명 |
| Small (작은 텍스트) | 12px | 400 (Regular) | 0px | 보조 텍스트, 라벨 |
| Extra Small | 11px | 400 (Regular) | 0px | 힌트 텍스트, 메타 정보 |

#### 행간 (Line Height)

- **제목**: 1.2 (타이트한 간격으로 시각적 임팩트 강조)
- **본문**: 1.6 (가독성을 위한 충분한 여백)
- **작은 텍스트**: 1.4 (균형잡힌 간격)

### 2.3. 아이콘 시스템 (Icon System)

- **아이콘 라이브러리**: Font Awesome 6.0
- **크기**: 16px (작음), 20px (중간), 24px (크음), 32px (매우 큼)
- **색상**: Teal(`#4ECDC4`) 또는 Gray(`#B0B0B0`)로 통일
- **사용 원칙**: 텍스트 라벨과 함께 사용하여 명확성 확보

### 2.4. 간격 시스템 (Spacing System)

스마트콘 라이트는 8px 기반의 간격 시스템을 사용합니다.

| 크기 | 픽셀 | 사용 사례 |
|------|------|----------|
| XS | 4px | 아이콘과 텍스트 사이 |
| S | 8px | 요소 내부 여백 |
| M | 16px | 요소 간 간격 |
| L | 24px | 섹션 간 간격 |
| XL | 32px | 주요 섹션 간 간격 |

### 2.5. 모서리 반경 (Border Radius)

| 크기 | 픽셀 | 사용 사례 |
|------|------|----------|
| Sharp | 0px | 테이블, 엄격한 레이아웃 |
| Subtle | 4px | 입력 필드, 작은 버튼 |
| Medium | 6px | 카드, 일반 버튼 |
| Large | 8px | 모달, 주요 컴포넌트 |

---

## 3. 전체 메뉴 구조도 (Information Architecture)

### 3.1. 본사 관리자 (PC Web) - Role A

본사 관리자는 전체 회사의 현황을 모니터링하고 전략적 의사결정을 지원하는 대시보드에 접근합니다.

```
📊 대시보드 (Dashboard)
├─ 전체 현황 (Overview)
│  ├─ 다중 현장 KPI
│  ├─ 실시간 출역 현황
│  └─ 공지사항
├─ 현장별 상세 정보
└─ 기간별 통계

🏗️ 현장 관리 (Site Management)
├─ 현장 목록 (Site List)
│  ├─ 현장 검색/필터링
│  └─ 현장별 상세 정보
├─ 현장 개설 (Create Site)
│  ├─ 기본 정보 입력
│  ├─ 안면인식 단말기 설정
│  └─ 현장 관리자 초대
├─ 현장 수정 (Edit Site)
└─ 단말기 관리 (Device Management)
   ├─ 단말기 목록
   ├─ 단말기 설정
   └─ 단말기 상태 모니터링

👥 노무 관리 (Labor Management)
├─ 전체 노무자 목록 (Worker List)
│  ├─ 검색/필터링
│  └─ 상세 정보 조회
├─ 노무자 이력 (Worker History)
│  ├─ 출역 이력
│  ├─ 계약 이력
│  └─ 급여 이력
└─ 노무팀장 관리 (Team Lead Management)

📋 계약 관리 (Contract Management)
├─ 전체 계약 현황 (Contract Overview)
│  ├─ 계약 상태별 집계
│  └─ 기간별 현황
├─ 미서명 계약 (Unsigned Contracts)
│  ├─ 미서명 리스트
│  ├─ 일괄 서명 요청
│  └─ 서명 현황 추적
└─ 계약서 조회 (Contract Details)

💰 정산 관리 (Settlement Management)
├─ 정산 현황 (Settlement Overview)
├─ 기간별 정산 내역 (Settlement History)
│  ├─ 월별 정산
│  ├─ 현장별 정산
│  └─ 노무자별 정산
├─ 정산 보고서 (Settlement Report)
└─ 정산 승인 (Settlement Approval)

⚙️ 설정 (Settings)
├─ 회사 정보 (Company Info)
├─ 계정 관리 (Account Management)
├─ 권한 설정 (Permission Settings)
└─ 시스템 설정 (System Settings)
```

### 3.2. 현장 관리자 (PC/Mobile) - Role B

현장 관리자는 특정 현장의 일일 운영을 관리하고 실시간 현황을 모니터링합니다.

```
📊 대시보드 (Dashboard)
├─ 금일 출역 현황 (Today's Attendance)
├─ 미서명 계약 (Unsigned Contracts)
├─ 작업일보 승인 대기 (Pending Daily Reports)
└─ 최근 활동 (Recent Activities)

👁️ 출역 관리 (Attendance Management)
├─ 실시간 출역 현황 (Real-time Attendance)
│  ├─ 안면인식 기반 출입 로그
│  ├─ 미확인 건 승인/거부
│  └─ 사진 비교 검증
├─ 출역 기록 (Attendance Records)
│  ├─ 일일 출역 현황
│  ├─ 월별 출역 통계
│  └─ 출역 기록 수정/승인
└─ 출역 현황 조회 (Attendance Inquiry)

📝 작업일보 (Daily Report)
├─ 일보 현황 (Report Overview)
├─ 팀별 일보 (Team Reports)
│  ├─ 팀장 일지 취합
│  ├─ 투입 인원 및 공수 확인
│  ├─ 현장 사진 검토
│  └─ 승인/반려
└─ 일보 기록 (Report History)

📋 계약 관리 (Contract Management)
├─ 계약 현황 (Contract Status)
├─ 미서명 계약 (Unsigned Contracts)
│  ├─ 미서명자 리스트
│  ├─ 일괄 서명 요청
│  └─ 서명 현황 추적
└─ 계약서 조회 (Contract Details)

👥 팀/노무자 관리 (Team & Labor Management)
├─ 현장 노무자 목록 (Worker List)
├─ 팀별 인원 현황 (Team Composition)
├─ 공종별 배치 (Trade Assignment)
└─ 노무자 추가/제거 (Add/Remove Workers)

💰 급여 정산 (Salary Settlement)
├─ 월별 정산 현황 (Monthly Settlement)
├─ 노무자별 명세서 (Worker Payslips)
└─ 정산 승인 (Settlement Approval)
```

### 3.3. 노무 팀장 (Mobile App) - Role C

노무 팀장은 소속 팀원의 출역 현황을 관리하고 일일 작업일보를 작성합니다.

```
📊 대시보드 (Dashboard)
├─ 팀원 출역 현황 (Team Attendance)
├─ 작업일보 작성 알림 (Daily Report Reminder)
├─ 미서명 계약 (Unsigned Contracts)
└─ 공지사항 (Announcements)

👥 팀원 관리 (Team Management)
├─ 팀원 목록 (Team Member List)
├─ 팀원 초대 (Invite Members)
│  ├─ 초대 링크 생성
│  ├─ SMS 발송
│  └─ 초대 상태 추적
├─ 초대 수락/거부 (Accept/Decline Invitation)
└─ 공종 매핑 (Trade Assignment)

📝 작업일보 (Daily Report)
├─ 일보 작성 (Create Report)
│  ├─ 투입 인원 입력
│  ├─ 공수 계산
│  ├─ 현장 사진 첨부
│  └─ 특이사항 기록
├─ 일보 제출 (Submit Report)
└─ 일보 이력 (Report History)

📋 계약 관리 (Contract Management)
├─ 나의 계약 (My Contracts)
├─ 계약서 서명 (Sign Contract)
└─ 계약 이력 (Contract History)

💰 급여 조회 (Salary Inquiry)
├─ 월별 급여 명세 (Monthly Payslip)
├─ 출역 기록 (Attendance Records)
└─ 정산 내역 (Settlement Details)
```

### 3.4. 노무자 (Mobile App) - Role D

노무자는 개인의 출역 현황을 확인하고 전자계약에 서명합니다.

```
🏠 홈 (Home/Dashboard)
├─ 오늘의 현장 정보 (Today's Site Info)
├─ 출퇴근 시간 (Check-in/Check-out)
├─ 미서명 계약 알림 (Unsigned Contract Alert)
└─ 공지사항 (Announcements)

📅 출역 조회 (Attendance Inquiry)
├─ 월별 출역 캘린더 (Monthly Calendar)
│  ├─ 출역 일수
│  ├─ 미출역 일수
│  └─ 공휴일
├─ 상세 출역 내역 (Attendance Details)
│  ├─ 출역 날짜
│  ├─ 현장명
│  ├─ 공종
│  ├─ 출퇴근 시간
│  └─ 공수
└─ 월별 통계 (Monthly Statistics)

📋 계약서 (Contracts)
├─ 나의 계약서 목록 (My Contracts)
│  ├─ 미서명 계약
│  ├─ 서명 완료 계약
│  └─ 만료된 계약
├─ 계약서 상세 조회 (Contract Details)
├─ 전자 서명 (E-Signature)
│  ├─ 서명 입력
│  └─ 서명 완료
└─ 계약서 다운로드 (Download Contract)

👤 내 정보 (My Profile)
├─ 개인정보 (Personal Info)
│  ├─ 이름, 전화번호
│  ├─ 주민등록번호
│  └─ 계좌 정보
├─ 안면인식 (Face Recognition)
│  ├─ 등록된 사진 조회
│  ├─ 사진 재등록
│  └─ 사진 삭제
├─ 로그아웃 (Logout)
└─ 앱 설정 (App Settings)
```

---

## 4. 네비게이션 패턴 (Navigation Patterns)

### 4.1. PC Web 네비게이션

#### 상단 GNB (Global Navigation Bar)
- **위치**: 페이지 상단, 고정
- **구성**: 로고, 주요 모듈 메뉴(현장, 노무, 계약, 정산 등), 사용자 프로필
- **특징**: 모든 페이지에서 접근 가능하며, 현재 위치를 시각적으로 표시

#### 좌측 LNB (Local Navigation Bar)
- **위치**: 페이지 좌측, 스크롤 가능
- **구성**: 선택된 모듈의 하위 메뉴
- **특징**: 계층적 구조를 명확히 표현하며, 활성 메뉴를 강조

#### 페이지 콘텐츠
- **위치**: 메인 콘텐츠 영역
- **구성**: 제목, 필터/검색, 데이터 테이블/카드, 액션 버튼
- **특징**: 반응형 레이아웃으로 다양한 해상도 지원

### 4.2. Mobile App 네비게이션

#### 하단 탭 바 (Bottom Tab Bar)
- **위치**: 화면 하단, 고정
- **구성**: 홈, 출역 조회, 계약서, 내 정보 (4개 탭)
- **특징**: 엄지손가락으로 쉽게 접근 가능하며, 현재 탭을 강조

#### 상단 헤더
- **위치**: 화면 상단, 고정
- **구성**: 페이지 제목, 뒤로가기 버튼, 액션 버튼
- **특징**: 현재 페이지 컨텍스트를 명확히 표시

#### 페이지 콘텐츠
- **위치**: 헤더와 탭 바 사이
- **구성**: 카드, 리스트, 폼 등 모바일 최적화된 레이아웃
- **특징**: 스크롤 가능하며, 터치 영역은 최소 44px 이상

---

## 5. 공통 컴포넌트 (Common Components)

### 5.1. 버튼 (Button)

#### 버튼 유형

| 유형 | 배경색 | 텍스트색 | 테두리 | 사용 사례 |
|------|--------|---------|--------|----------|
| **Primary** | Teal (#4ECDC4) | Navy (#1A2B3C) | None | 주요 액션 (저장, 제출, 승인) |
| **Secondary** | Gray (rgba) | White (#FFFFFF) | Gray (#37474F) | 보조 액션 (취소, 닫기) |
| **Destructive** | Red (#F44336) | White (#FFFFFF) | None | 위험한 액션 (삭제, 거부) |
| **Disabled** | Gray (rgba) | Gray (#90A4AE) | None | 비활성 상태 |

#### 버튼 크기

| 크기 | 패딩 | 폰트 크기 | 사용 사례 |
|------|------|----------|----------|
| **Large** | 16px 24px | 16px | 페이지 주요 버튼 |
| **Medium** | 12px 20px | 14px | 일반 버튼 |
| **Small** | 8px 16px | 12px | 테이블 액션, 보조 버튼 |

### 5.2. 입력 폼 (Input Form)

#### 구성 요소
- **라벨 (Label)**: 입력 필드의 목적을 설명하는 텍스트
- **입력 필드 (Input Field)**: 사용자 입력을 받는 영역
- **도움말 텍스트 (Helper Text)**: 입력 형식 또는 예시를 제공
- **유효성 검사 피드백 (Validation Feedback)**: 오류 또는 성공 메시지

#### 입력 필드 상태

| 상태 | 테두리색 | 배경색 | 텍스트색 | 설명 |
|------|---------|--------|---------|------|
| **Normal** | #37474F | rgba(0,0,0,0.2) | #FFFFFF | 기본 상태 |
| **Focus** | #4ECDC4 | rgba(78,205,196,0.05) | #FFFFFF | 포커스 상태 |
| **Error** | #F44336 | rgba(244,67,54,0.05) | #FFFFFF | 유효성 검사 실패 |
| **Success** | #009688 | rgba(0,150,136,0.05) | #FFFFFF | 유효성 검사 성공 |
| **Disabled** | #607D8B | rgba(0,0,0,0.1) | #90A4AE | 비활성 상태 |

### 5.3. 카드 (Card)

#### 구성 요소
- **헤더**: 카드 제목 또는 상태 정보
- **본문**: 주요 콘텐츠 (텍스트, 수치, 그래프 등)
- **푸터**: 추가 정보 또는 액션 버튼

#### 카드 유형

| 유형 | 사용 사례 | 배경색 | 테두리 |
|------|----------|--------|--------|
| **KPI Card** | 주요 지표 표시 | rgba(255,255,255,0.05) | #37474F |
| **Info Card** | 정보 요약 | rgba(255,255,255,0.05) | #37474F |
| **Action Card** | 클릭 가능한 카드 | rgba(255,255,255,0.05) | #4ECDC4 (hover) |

### 5.4. 모달 (Modal)

#### 구성 요소
- **오버레이 (Overlay)**: 배경 어두워짐 (rgba(0,0,0,0.5))
- **모달 박스 (Modal Box)**: 흰색 배경, 그림자 포함
- **헤더**: 모달 제목 및 닫기 버튼
- **본문**: 모달 콘텐츠
- **푸터**: 액션 버튼 (확인, 취소 등)

#### 모달 유형

| 유형 | 목적 | 버튼 |
|------|------|------|
| **Confirmation** | 사용자 확인 요청 | 확인, 취소 |
| **Input** | 정보 입력 | 제출, 취소 |
| **Alert** | 알림 메시지 | 확인 |
| **Details** | 상세 정보 표시 | 닫기 |

### 5.5. 테이블 (Table)

#### 구성 요소
- **헤더 행 (Header Row)**: 열 제목, 정렬 아이콘
- **데이터 행 (Data Row)**: 실제 데이터
- **푸터**: 페이지네이션, 행 수 선택

#### 테이블 기능

| 기능 | 설명 |
|------|------|
| **정렬 (Sorting)** | 열 헤더 클릭으로 오름차순/내림차순 정렬 |
| **필터링 (Filtering)** | 특정 조건으로 데이터 필터링 |
| **페이지네이션 (Pagination)** | 페이지 단위로 데이터 표시 |
| **행 선택 (Row Selection)** | 체크박스로 여러 행 선택 |

#### 테이블 행 상태

| 상태 | 배경색 | 설명 |
|------|--------|------|
| **Normal** | Transparent | 기본 상태 |
| **Hover** | rgba(78,205,196,0.1) | 마우스 오버 |
| **Selected** | rgba(78,205,196,0.2) | 선택된 행 |
| **Disabled** | rgba(0,0,0,0.1) | 비활성 행 |

### 5.6. 상태 배지 (Status Badge)

#### 상태별 색상

| 상태 | 배경색 | 텍스트색 | 사용 사례 |
|------|--------|---------|----------|
| **완료 (Complete)** | rgba(0,150,136,0.3) | #009688 | 승인 완료, 서명 완료 |
| **진행 중 (In Progress)** | rgba(33,150,243,0.3) | #2196F3 | 검토 중, 작성 중 |
| **대기 (Pending)** | rgba(255,107,0,0.3) | #FF6B00 | 승인 대기, 미서명 |
| **오류 (Error)** | rgba(244,67,54,0.3) | #F44336 | 오류, 거부 |

---

## 6. Phase별 기능 구분

### Phase 1 (초기 개발) - Core Features
- 로그인 및 인증
- 본사 관리자 대시보드
- 현장 개설 및 관리
- 안면인식 기반 출역 관리
- 근로계약 관리 및 전자서명
- 작업일보 승인
- 노무자 초대 및 관리

### Phase 2 (향후 확장) - Enhancement Features
- 노무 팀장 대시보드 고도화
- 급여 정산 자동화
- GPS 기반 위치 추적
- QR 코드 기반 출역 인증
- AI 기반 일보 분석
- 모바일 앱 고급 기능

---

## 7. 접근성 가이드 (Accessibility Guidelines)

### 7.1. 색상 대비 (Color Contrast)

모든 텍스트와 배경의 색상 대비는 WCAG 2.1 AA 수준을 만족합니다.

| 요소 | 대비 비율 | 기준 |
|------|----------|------|
| 일반 텍스트 | 4.5:1 이상 | AA 레벨 |
| 큰 텍스트 (18pt+) | 3:1 이상 | AA 레벨 |
| UI 컴포넌트 | 3:1 이상 | AA 레벨 |

### 7.2. 폰트 크기 (Font Size)

- **최소 폰트 크기**: 12px (작은 텍스트 제외)
- **권장 폰트 크기**: 14px (일반 텍스트)
- **모바일**: 16px 이상 (줌 없이 읽을 수 있어야 함)

### 7.3. 터치 영역 (Touch Target Size)

- **최소 크기**: 44px × 44px (모바일)
- **권장 크기**: 48px × 48px (모바일)
- **최소 간격**: 8px (터치 영역 간)

### 7.4. 키보드 네비게이션 (Keyboard Navigation)

- 모든 기능은 키보드로 접근 가능해야 함
- Tab 키로 순차적으로 이동 가능
- Enter 키로 버튼 활성화 가능
- Escape 키로 모달 닫기 가능

---

## 8. 성능 최적화 (Performance Optimization)

### 8.1. 로딩 성능 (Loading Performance)

| 메트릭 | 목표 | 설명 |
|--------|------|------|
| **LCP (Largest Contentful Paint)** | < 2.5s | 가장 큰 콘텐츠 요소의 로딩 시간 |
| **FID (First Input Delay)** | < 100ms | 사용자 입력에 대한 반응 시간 |
| **CLS (Cumulative Layout Shift)** | < 0.1 | 예상치 못한 레이아웃 변경 |

### 8.2. 이미지 최적화 (Image Optimization)

- WebP 형식 사용 (PNG/JPG 폴백)
- 반응형 이미지 (srcset 사용)
- 지연 로딩 (Lazy Loading)
- 이미지 압축 (최대 100KB)

### 8.3. 캐싱 전략 (Caching Strategy)

- 정적 자산: 1년 캐시
- API 응답: 5분 캐시
- 사용자 데이터: 세션 캐시

---

## 9. 보안 가이드 (Security Guidelines)

### 9.1. 데이터 마스킹 (Data Masking)

- **주민등록번호**: 앞 6자리만 표시 (예: 123456-*****)
- **계좌번호**: 뒤 4자리만 표시 (예: ****-****-****-1234)
- **전화번호**: 중간 4자리 마스킹 (예: 010-****-1234)

### 9.2. 세션 관리 (Session Management)

- **세션 타임아웃**: 30분 (비활성 시)
- **자동 로그아웃**: 타임아웃 후 자동 로그아웃
- **로그아웃 경고**: 타임아웃 5분 전 경고 메시지

### 9.3. 입력 검증 (Input Validation)

- 클라이언트 측 검증 (UX 개선)
- 서버 측 검증 (보안)
- XSS 방지 (HTML 이스케이프)
- SQL Injection 방지 (파라미터화된 쿼리)

---

## 10. 개발 체크리스트 (Development Checklist)

### 10.1. 디자인 일관성
- [ ] 모든 버튼이 디자인 시스템의 버튼 스타일을 따르는가?
- [ ] 모든 입력 필드가 표준화된 스타일을 사용하는가?
- [ ] 색상이 정의된 팔레트에서만 사용되는가?
- [ ] 타이포그래피가 정의된 크기와 굵기를 따르는가?

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

### 10.5. 보안
- [ ] 민감한 데이터가 마스킹되어 있는가?
- [ ] 세션 타임아웃이 구현되어 있는가?
- [ ] 입력 검증이 클라이언트와 서버에서 모두 수행되는가?

---

## 11. 추가 리소스 (Additional Resources)

### 11.1. 참고 문서
- WCAG 2.1 Accessibility Guidelines: https://www.w3.org/WAI/WCAG21/quickref/
- Google Material Design: https://material.io/design/
- Apple Human Interface Guidelines: https://developer.apple.com/design/human-interface-guidelines/

### 11.2. 디자인 도구
- Figma (디자인 협업 도구)
- Adobe XD (프로토타입 도구)
- Storybook (컴포넌트 라이브러리)

### 11.3. 개발 라이브러리
- Font Awesome (아이콘)
- Tailwind CSS (스타일링)
- React (UI 프레임워크)
- TypeScript (타입 안정성)

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025년 12월 17일  
**작성자**: Manus AI  
**상태**: Phase 1 - Core Features 기준
