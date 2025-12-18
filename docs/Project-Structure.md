# 프로젝트 구조 설계서

**문서 버전:** 2.1  
**작성일:** 2025년 12월 17일  
**작성자:** 경영기획실 이대영 이사  
**기반 문서:** `docs/PRD.md` (v2.1)

---

## 1. 전체 프로젝트 구조

```
SmartCON/
├── docs/                          # 문서 폴더 (PRD, Checklist 등)
│
├── frontend/                      # 프론트엔드 (React + Vite)
│   ├── web/                       # PC/모바일 웹앱 통합
│   │   ├── src/
│   │   │   ├── components/ui/     # Shadcn/UI Base Components
│   │   │   ├── features/          # 기능별 모듈
│   │   │   │   ├── auth/          # 인증 (Login, Social, Split Flow)
│   │   │   │   ├── hq-admin/      # 본사 관리자 전용 기능 (Site Create, Dashboard)
│   │   │   │   ├── site-admin/    # 현장 관리자 전용 기능 (Team Assign, Report)
│   │   │   │   ├── team-lead/     # 팀장 전용 기능 (Work Accept, Team Mgmt)
│   │   │   │   ├── worker/        # 노무자 전용 기능 (Check-in, Contract)
│   │   │   │   └── common/        # 공통 기능 (Notification, Profile)
│   │   │   ├── lib/               # 유틸리티 (i18n, axios-client, weather-api)
│   │   │   ├── stores/            # Global State (Zustand: useAuthStore, useSiteStore)
│   │   │   └── ...
│   │
│   └── mobile/                    # 모바일 하이브리드 설정 (Capacitor)
│
├── backend/                       # 백엔드 (Spring Boot)
│   ├── src/main/java/com/smartcon/
│   │   ├── domain/                # 도메인 (User, Company, Work, Report...)
│   │   ├── global/                # 전역 설정 (Security, Config)
│   │   ├── infra/                 # 외부 연동 (FaceAPI, WeatherAPI, Notification)
│   │   └── batch/                 # 배치 작업 (FaceSyncJob)
│   └── ...
│
└── ...
```

---

## 2. 주요 모듈 설명

### 2.1. Frontend Features
- **auth/**: `/login/hq` (BusinessNo), `/login/site` (Social) 라우트 분리 구현
- **hq-admin/**: 본사 관리자용 대시보드, 현장 관리, 단가/공종 관리
- **site-admin/**: 현장 관리자용 출역 관리, 팀 작업 배정, 공사일보(날씨 연동)
- **lib/i18n.ts**: 다국어 지원 설정 (react-i18next 활용 권장)

### 2.2. Backend Batch
- **batch/FaceSyncJob**: 매일 자정 실행. `SiteUserMapping` 및 `WorkAssignment` 정보를 기반으로 금일 현장 출입 가능한 인원의 안면 정보를 Face API 서버로 전송.

### 2.3. External Interfaces
- **infra/weather/**: OpenWeatherMap or KMA API Client
- **infra/face/**: Face Recognition API Client

---

**문서 꿑**
