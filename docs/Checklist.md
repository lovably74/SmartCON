# SmartCON Lite 통합 체크리스트 (v2.2)

**문서 버전:** 2.2  
**작성일:** 2025년 12월 17일  
**작성자:** 경영기획실 이대영 이사  
**기반 문서:** `docs/Functional-Specification.md` (v2.0)

---

## 🏗️ Phase 0: 초기 설정 및 프로토타입

### 환경 설정
- [ ] Git 저장소 초기화 및 `.gitignore` 설정 (Java/Node)
- [ ] Docker Compose 작성 (MariaDB, Redis, MinIO/S3-mock)
- [ ] Backend: Spring Boot 3.x 프로젝트 생성 (Gradle)
- [ ] Frontend: Vite + React + TS 프로젝트 생성
- [ ] UI 라이브러리 설치 (Shadcn/UI, Tailwind CSS, Lucide Icons)

### 공통 모듈
- [x] **Logging**: Logback 설정 (Console/File) - *설계 반영*
- [x] **Exception Handling**: GlobalExceptionHandler 구현 - *설계 반영*
- [x] **Response Format**: `ApiResponse<T>` 공통 래퍼 클래스 정의 - *설계 반영*
- [x] **Multi-tenancy**: Hibernate Filter 또는 AOP 기반 `tenant_id` 주입 로직 - *명세서 작성 완료*

---

## 🔐 Phase 1: 인증 및 구독 (SaaS Core)

### 테넌트 및 사용자 엔티티
- [x] `Tenant`, `Company` 엔티티 및 Repository - *Tenant 엔티티 구현 완료*
- [x] `User` 엔티티 및 Role(`SUPER`, `HQ`, `SITE`, `TEAM`, `WORKER`) 정의 - *User 엔티티 구현 완료*
- [ ] Spring Security 설정 (SecurityFilterChain)

### 인증 로직
- [x] JWT Provider (Generate, Validate, Refresh) 구현 - *명세 반영*
- [x] **[API]** `POST /api/auth/hq/login` (사업자번호 로그인) - *명세 반영*
- [x] **[API]** `POST /api/auth/social-login` (카카오/네이버 연동) - *명세 반영*
- [ ] 다중 프로필 선택 API (`List<UserContext>`)
- [x] **[ADM API]** `GET /api/v1/admin/tenants` (슈퍼관리자 테넌트 관리) - *추가 명세 작성*

### 구독 결제 (Super Admin)
- [ ] 결제 PG 연동 모듈 (아임포트/토스)
- [x] 구독 신청/해지 API - *명세 반영*
- [ ] 자동 결제 스케줄러 (Spring Batch)

---

## 🏢 Phase 2: 현장 및 작업 관리

### 현장 관리 (HQ/Site)
- [ ] **[API]** 현장(Site) CRUD (안면인식기 시리얼 포함)
- [ ] **[API]** 공종/직종/단가 표준 관리
- [ ] **[API]** 관리자(Site Manager) 초대 및 권한 부여

### 작업 프로세스 (Work Flow)
- [ ] **[API]** 작업 요청 (`WorkRequest`) 생성 (Site -> Team)
- [ ] **[API]** 작업 수락/거부 (`RequestStatus` 변경)
- [ ] **[API]** 작업 인원(`WorkAssignment`) 명단 제출 (Team -> Site)

### 공사일보 (Daily Report)
- [ ] **[API]** 날씨 API 연동 (OpenWeatherMap)
- [ ] **[API]** 일보 자동 생성/취합 (Batch/Trigger)
- [ ] **[API]** 일보 승인 및 마감 처리

---

## 🤖 Phase 3: 안면인식 및 출역

### 안면인식 파이프라인
- [ ] **[API]** 안면 사진 등록 (S3 업로드)
- [ ] **[Function]** FaceNet 임베딩 추출 (Java DL 라이브러리 or Python Bridge)
- [ ] **[Batch]** `FaceSyncJob`: 금일 투입 예정자(Active List) 벡터 동기화

### 출역 관리
- [ ] **[API]** 안면인식 로그 수신 및 매칭
- [ ] **[API]** 출역 기록 조회 및 수정(이의제기 처리)
- [ ] **[API]** 일일/월간 출역 통계 집계

---

## 📱 Phase 4: 모바일 및 부가기능

### Mobile App (Capacitor)
- [ ] Android/iOS 플랫폼 추가
- [ ] Camera Plugin 연동 (프로필/작업사진)
- [ ] Push Notification (FCM) 연동

### 전자계약 및 정산
- [ ] **[Lib]** PDF 생성 엔진(iText) 연동
- [ ] **[API]** 근로계약서 템플릿 바인딩 및 생성
- [ ] **[API]** 전자서명 이미지 합성 및 저장
- [ ] **[API]** 월간 급여 정산 및 명세서 생성
- [ ] **[API]** 세금계산서 홈택스 연동 (외부 API)

---

**체크리스트 끝**
