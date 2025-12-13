# 프로젝트 구조 설계서

**문서 버전:** 1.0  
**작성일:** 2025년 12월 13일  
**작성자:** 개발 PM (Gemini)

---

## 1. 전체 프로젝트 구조

```
SmartCON/
├── docs/                          # 문서 폴더
│   ├── backup/                    # 문서 백업
│   ├── PRD.md                     # 요구사항 정의서
│   ├── Mobile-Native-App-Strategy.md
│   ├── Project-Structure.md       # 이 문서
│   ├── Development-Process.md      # 개발 절차서
│   └── Checklist.md               # 체크리스트
│
├── frontend/                      # 프론트엔드 (통합)
│   ├── web/                       # PC/모바일 웹앱 (반응형)
│   │   ├── src/
│   │   │   ├── assets/            # 정적 자원
│   │   │   ├── components/        # 공통 컴포넌트
│   │   │   │   ├── common/        # 공통 UI 컴포넌트
│   │   │   │   ├── layout/        # 레이아웃 컴포넌트
│   │   │   │   └── features/      # 기능별 컴포넌트
│   │   │   ├── views/             # 페이지 컴포넌트
│   │   │   │   ├── auth/          # 인증 관련
│   │   │   │   ├── dashboard/      # 대시보드
│   │   │   │   ├── attendance/    # 출역 관리
│   │   │   │   ├── face/          # 안면인식
│   │   │   │   ├── report/        # 작업일보
│   │   │   │   └── mypage/        # 마이페이지
│   │   │   ├── stores/            # Pinia 스토어
│   │   │   ├── services/          # API 서비스
│   │   │   ├── utils/             # 유틸리티
│   │   │   ├── types/             # TypeScript 타입
│   │   │   ├── router/            # 라우터 설정
│   │   │   └── App.vue
│   │   ├── public/                # 정적 파일
│   │   ├── tests/                 # 테스트 코드
│   │   │   ├── unit/              # 단위 테스트
│   │   │   ├── integration/       # 통합 테스트
│   │   │   └── e2e/               # E2E 테스트
│   │   ├── package.json
│   │   └── vite.config.ts
│   │
│   └── mobile/                    # 모바일 네이티브앱 (Capacitor)
│       ├── src/                  # 웹앱과 동일한 구조 (공유)
│       ├── android/               # Android 네이티브 프로젝트
│       ├── ios/                   # iOS 네이티브 프로젝트
│       ├── capacitor.config.ts    # Capacitor 설정
│       └── package.json
│
├── backend/                       # 백엔드 (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/smartcon/
│   │   │   │       ├── SmartConApplication.java
│   │   │   │       ├── config/    # 설정 클래스
│   │   │   │       ├── controller/# REST 컨트롤러
│   │   │   │       ├── service/   # 비즈니스 로직
│   │   │   │       ├── repository/# 데이터 접근
│   │   │   │       ├── entity/    # JPA 엔티티
│   │   │   │       ├── dto/       # 데이터 전송 객체
│   │   │   │       ├── security/  # 보안 설정
│   │   │   │       └── exception/ # 예외 처리
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/
│   │   │           └── migration/ # DB 마이그레이션
│   │   └── test/
│   │       └── java/              # 테스트 코드
│   │           ├── unit/         # 단위 테스트
│   │           ├── integration/  # 통합 테스트
│   │           └── e2e/           # E2E 테스트
│   ├── pom.xml                    # Maven 의존성
│   └── Dockerfile
│
├── prototype/                     # 프로토타입 (화면 위주)
│   ├── src/
│   │   ├── pages/                # 화면 페이지
│   │   ├── components/           # 컴포넌트
│   │   ├── mock-data/            # 목업 데이터
│   │   └── router/               # 라우터 (단순 링크)
│   ├── package.json
│   └── vite.config.ts
│
├── database/                      # 데이터베이스 관련
│   ├── schema/                   # 스키마 정의
│   ├── migration/                # 마이그레이션 스크립트
│   └── seed/                     # 초기 데이터
│
├── scripts/                       # 유틸리티 스크립트
│   ├── setup.sh                  # 초기 설정
│   ├── build.sh                  # 빌드 스크립트
│   └── deploy.sh                 # 배포 스크립트
│
├── .gitignore
├── README.md
└── docker-compose.yml             # 로컬 개발 환경
```

---

## 2. 폴더별 상세 설명

### 2.1. frontend/web (PC/모바일 웹앱)

#### src/components/
- **common/**: 버튼, 입력, 카드 등 재사용 가능한 UI 컴포넌트
- **layout/**: 헤더, 사이드바, 푸터 등 레이아웃 컴포넌트
- **features/**: 안면인식, 출역체크 등 기능별 컴포넌트

#### src/views/
- **auth/**: 로그인, 회원가입 페이지
- **dashboard/**: 대시보드 페이지 (역할별)
- **attendance/**: 출역 관리 페이지
- **face/**: 안면 등록/인식 페이지
- **report/**: 작업일보 페이지
- **mypage/**: 마이페이지, 이의제기

#### src/stores/
- Pinia 스토어 모듈
  - `auth.store.ts`: 인증 상태
  - `user.store.ts`: 사용자 정보
  - `attendance.store.ts`: 출역 관리
  - `face.store.ts`: 안면인식

#### src/services/
- API 호출 서비스
  - `api.service.ts`: 기본 HTTP 클라이언트
  - `auth.service.ts`: 인증 API
  - `attendance.service.ts`: 출역 API
  - `face.service.ts`: 안면인식 API

### 2.2. frontend/mobile (네이티브앱)

- 웹앱과 동일한 `src/` 구조 사용
- `android/`, `ios/`: Capacitor가 자동 생성하는 네이티브 프로젝트
- `capacitor.config.ts`: Capacitor 설정 파일

### 2.3. backend (Spring Boot)

#### controller/
- REST API 엔드포인트 정의
- 예: `AuthController`, `AttendanceController`, `FaceController`

#### service/
- 비즈니스 로직 구현
- 예: `AuthService`, `AttendanceService`, `FaceService`

#### repository/
- JPA Repository 인터페이스
- 예: `UserRepository`, `AttendanceLogRepository`

#### entity/
- JPA 엔티티 클래스
- DB 테이블과 매핑

#### dto/
- Request/Response DTO
- API 계약 정의

### 2.4. prototype (프로토타입)

- 실제 데이터 없이 화면만 구현
- 목업 데이터로 실제처럼 보이게 표시
- 라우터는 단순 링크로 처리
- 모든 기능을 클릭/링크로 동작하도록 구현

---

## 3. 공유 코드 전략

### 3.1. 웹앱과 모바일앱 코드 공유

```
frontend/
├── shared/                        # 공유 코드 (선택사항)
│   ├── components/                # 공통 컴포넌트
│   ├── stores/                    # 공통 스토어
│   ├── services/                 # 공통 서비스
│   └── utils/                    # 공통 유틸
│
├── web/                           # 웹 전용
│   └── src/
│       └── layouts/               # PC/모바일 레이아웃
│
└── mobile/                        # 모바일 전용
    └── src/
        └── plugins/               # Capacitor 플러그인 래퍼
```

**또는 단순화된 구조 (권장)**:
- 웹앱을 먼저 완성
- 모바일앱은 웹앱 코드를 복사하여 Capacitor 통합
- 이후 공통 부분만 추출하여 공유

---

## 4. 개발 환경 설정

### 4.1. 필수 도구

- **Node.js**: 18.x 이상
- **Java**: 17 이상
- **Maven**: 3.8 이상
- **Android Studio**: Android 개발용
- **Xcode**: iOS 개발용 (Mac만)
- **Docker**: 로컬 DB 실행용 (선택)

### 4.2. IDE 추천

- **VS Code**: 프론트엔드 개발
- **IntelliJ IDEA**: 백엔드 개발
- **Android Studio**: Android 네이티브 개발
- **Xcode**: iOS 네이티브 개발

---

## 5. 빌드 및 배포 구조

### 5.1. 빌드 산출물

```
dist/
├── web/                           # 웹앱 빌드 결과
│   └── index.html
│
└── mobile/                        # 모바일앱 빌드 결과
    ├── android/                   # Android APK/AAB
    └── ios/                       # iOS IPA
```

### 5.2. 배포 전략

- **웹앱**: 정적 파일 서버 (Nginx, S3 등)
- **Android**: Google Play Store
- **iOS**: App Store

---

## 6. 테스트 구조

### 6.1. 프론트엔드 테스트

```
frontend/web/tests/
├── unit/                          # 단위 테스트
│   ├── components/
│   └── stores/
├── integration/                  # 통합 테스트
│   └── api/
└── e2e/                          # E2E 테스트
    └── scenarios/
```

### 6.2. 백엔드 테스트

```
backend/src/test/java/
├── unit/                          # 단위 테스트
│   ├── service/
│   └── repository/
├── integration/                   # 통합 테스트
│   └── controller/
└── e2e/                          # E2E 테스트
    └── api/
```

---

## 7. 문서 구조

```
docs/
├── backup/                        # 문서 백업
├── PRD.md                         # 요구사항 정의서
├── Mobile-Native-App-Strategy.md  # 모바일앱 전략
├── Project-Structure.md           # 프로젝트 구조 (이 문서)
├── Development-Process.md         # 개발 절차서
├── Checklist.md                   # 체크리스트
├── API-Specification.md           # API 명세서 (추후 작성)
└── ERD.md                         # ERD (추후 작성)
```

---

## 8. Git 브랜치 전략

```
main                              # 프로덕션 브랜치
├── develop                        # 개발 브랜치
│   ├── feature/                  # 기능 개발 브랜치
│   │   ├── feature/auth
│   │   ├── feature/attendance
│   │   └── feature/face-recognition
│   ├── prototype/                # 프로토타입 브랜치
│   └── hotfix/                   # 긴급 수정 브랜치
```

---

**문서 끝**

