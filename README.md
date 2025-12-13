# SmartCON Lite

건설 현장 노무 관리 시스템 - FaceNet 기반 안면인식 출역 관리 솔루션

---

## 프로젝트 개요

SmartCON Lite는 건설 현장의 노무 관리를 자동화하기 위한 시스템입니다. FaceNet 기반 안면인식 기술을 활용하여 출역 관리를 투명하고 효율적으로 수행합니다.

### 핵심 가치
**"현장의 신뢰, 기술로 완성하다"**

- **투명성**: 안면인식을 통한 객관적 출역 관리
- **편의성**: 모바일 중심의 직관적인 사용자 경험
- **유연성**: 다양한 현장 환경에 대응하는 유연한 시스템

---

## 기술 스택

### 프론트엔드
- **Framework**: Vue.js 3 (Composition API)
- **State Management**: Pinia
- **UI Library**: Vuetify 3
- **Build Tool**: Vite
- **Mobile**: Capacitor (Android/iOS)

### 백엔드
- **Framework**: Spring Boot 3.x (Java 17+)
- **ORM**: JPA (Hibernate)
- **Security**: Spring Security + JWT
- **Database**: MSSQL (SQL Server 2019+)

---

## 프로젝트 구조

```
SmartCON/
├── docs/              # 문서
├── frontend/          # 프론트엔드
│   ├── web/          # PC/모바일 웹앱 (반응형)
│   └── mobile/       # 모바일 네이티브앱 (Capacitor)
├── backend/          # 백엔드 (Spring Boot)
├── prototype/        # 프로토타입
└── database/         # 데이터베이스 스크립트
```

자세한 구조는 [프로젝트 구조 문서](docs/Project-Structure.md)를 참고하세요.

---

## 개발 단계

**⚠️ 본 프로젝트는 초급 개발자 1명이 Cursor AI와 함께 단독으로 개발합니다.**

### Phase 0: 프로토타입 개발 (3주)
- 화면 위주 프로토타입 개발
- 목업 데이터로 실제처럼 구현
- 사용자 피드백 수집
- Cursor AI 학습 및 적응

### Phase 1: 인증 및 사용자 관리 (6주)
- 소셜 로그인 (카카오/네이버)
- PASS 인증
- 안면 등록
- Cursor와 함께 백엔드/프론트엔드 개발

### Phase 2: 출역 체크 및 대시보드 (6주)
- 안면인식 출역 체크
- 실시간 대시보드
- 예외 승인 처리
- Face API 연동

### Phase 3: 작업일보 및 마이페이지 (4주)
- 작업일보 작성/관리
- 월간 출역 현황
- 이의 제기

### Phase 4: 테스트 및 최적화 (3주)
- 통합 테스트
- 성능 최적화
- 보안 점검

### Phase 5: 배포 및 안정화 (2주)
- 프로덕션 배포
- 모니터링 설정

**총 예상 기간**: 24주 (약 6개월)

---

## 문서

- [요구사항 정의서 (PRD)](docs/PRD.md)
- [모바일 네이티브앱 전략](docs/Mobile-Native-App-Strategy.md)
- [프로젝트 구조](docs/Project-Structure.md)
- [개발 절차서](docs/Development-Process.md)
- [개발 체크리스트](docs/Checklist.md)
- [프로토타입 개발 가이드](docs/Prototype-Guide.md)
- [**개발 사전 준비사항**](docs/Pre-Development-Setup.md) ⭐ **개발 시작 전 필수**
- [**바이브코딩(Pair Programming) 가이드**](docs/Pair-Programming-Guide.md) ⭐ **모든 개발은 바이브코딩으로 진행**
- [**Cursor 빠른 시작 가이드**](docs/Cursor-Quick-Start.md) ⭐ **초급 개발자 필수**

---

## 시작하기

### 필수 요구사항

- Node.js 18.x 이상
- Java 17 이상
- Maven 3.8 이상
- MSSQL (SQL Server 2019 이상)

### 개발 환경 설정

#### 프론트엔드 (웹앱)

```bash
cd frontend/web
npm install
npm run dev
```

#### 백엔드

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### 프로토타입

```bash
cd prototype
npm install
npm run dev
```

자세한 내용은 각 폴더의 README를 참고하세요.

---

## 주요 기능

### 1. 안면인식 출역 체크
- FaceNet 기반 안면인식
- 실시간 출역 기록 생성
- 지각/정상 자동 판별

### 2. 실시간 대시보드
- 역할별 맞춤 대시보드
- 실시간 출역 현황 모니터링
- 통계 및 그래프

### 3. 작업일보 관리
- 간편한 작업일보 작성
- 사진 업로드 및 관리
- 임시저장 기능

### 4. 출역 이력 조회
- 월간 출역 현황 (달력)
- 이의 제기 및 처리
- 상세 출역 기록 조회

---

## 테스트

### 백엔드 테스트
```bash
cd backend
mvn test
```

### 프론트엔드 테스트
```bash
cd frontend/web
npm run test
```

### E2E 테스트
```bash
npm run test:e2e
```

---

## 배포

### 웹앱 배포
```bash
cd frontend/web
npm run build
# dist/ 폴더를 정적 파일 서버에 배포
```

### 백엔드 배포
```bash
cd backend
mvn clean package
# target/*.jar 파일을 애플리케이션 서버에 배포
```

### 모바일앱 배포
```bash
cd frontend/mobile
npm run build
npx cap sync
# Android Studio / Xcode에서 빌드 및 배포
```

---

## 라이선스

이 프로젝트는 회사 내부 프로젝트입니다.

---

## 문의

프로젝트 관련 문의사항이 있으시면 개발팀에 연락해주세요.

---

**마지막 업데이트**: 2025-12-13

