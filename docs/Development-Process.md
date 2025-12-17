# SmartCON Lite 개발 절차서 (Development Process)

**문서 버전:** 2.1  
**작성일:** 2025년 12월 17일  
**작성자:** 개발 PM (Gemini)  
**기반 문서:** `docs/PRD.md`, `docs/System-Architecture.md`

---

## 1. 개발 철학 (Development Philosophy)
- **AI-Driven**: Cursor, Copilot 등 AI 도구를 적극 활용하여 보일러플레이트 코드를 최소화하고 비즈니스 로직에 집중합니다.
- **Iterative**: 완벽한 설계보다 빠른 프로토타이핑과 피드백 루프를 지향합니다.
- **Quality First**: TDD(Test Driven Development)를 원칙으로 하며, 모든 주요 기능은 테스트 코드를 동반해야 합니다.

---

## 2. 개발 단계 (Phases)

### Phase 0: 프로젝트 셋업 (Environment Setup)
- **개발 환경 구축**: IDE(VS Code/IntelliJ), Docker(MariaDB/Redis), Java 17, Node.js 18+.
- **프로토타입 고도화**: React + Shadcn/UI 기반의 와이어프레임 UI 완성.
- **CI/CD 파이프라인**: Github Actions 기본 워크플로우(Build/Test) 설정.

### Phase 1: 코어 & 인증 (Core & Auth)
- **DB 설계 및 구축**: Tenant, Company, User 등 핵심 엔티티 설계.
- **인증 시스템**: Spring Security + JWT 구현, 소셜 로그인 연동.
- **SaaS 온보딩**: 구독 신청 및 테넌트 생성 로직 구현.

### Phase 2: 현장 및 작업 관리 (Site & Work)
- **현장 관리**: 현장 개설, 공종 설정, 관리자 초대 기능.
- **작업 프로세스**: `작업 요청(Site) -> 수락(Team) -> 배정(Worker)` 플로우 구현.
- **일보 시스템**: 날씨 API 연동 및 일보 집계/승인 로직.

### Phase 3: 안면인식 및 출역 (FaceNet & Attendance)
- **안면인식 파이프라인**: 사진 등록, 임베딩 추출, 벡터 저장 로직.
- **동기화 배치**: Spring Batch를 이용한 Daily Sync Job 구현.
- **출역 마감**: 안면 인식 로그와 일보 대조 로직.

### Phase 4: 모바일 앱 및 부가기능 (Mobile & Extras)
- **Capacitor 연동**: 카메라, GPS, Push Notification 네이티브 기능 연동.
- **전자계약**: PDF 생성 엔진(iText) 연동 및 서명/저장.
- **정산/세무**: 결제 모듈 및 홈택스 연동.

---

## 3. 형상 관리 및 배포 전략

### 3.1. Branch Strategy (Git Flow Lite)
- **main**: 프로덕션 배포 가능한 안정 버전.
- **develop**: 개발 진행 중인 브랜치.
- **feature/**: 개별 기능 개발 브랜치 (예: `feature/auth-login`).

### 3.2. Commit Strategy
- **Conventional Commits** 준수
  - `feat: 새로운 기능 추가`
  - `fix: 버그 수정`
  - `docs: 문서 수정`
  - `refactor: 코드 리팩토링`

### 3.3. Deploy
- **Dev**: Docker Compose로 로컬/개발 서버 배포.
- **Prod**: AWS ECS 또는 Kubernetes (추후 확장 시).

---

**문서 끝**
