# SmartCON Lite 개발 체크리스트

**문서 버전:** 1.0  
**작성일:** 2025년 12월 13일  
**작성자:** 개발 PM (Gemini)

---

## 사용 방법

- [ ] 체크박스를 사용하여 작업 완료 여부를 표시합니다.
- 각 항목은 개발 완료 후 체크합니다.
- 테스트 코드 작성 및 통과는 필수입니다.

---

## Phase 0: 프로토타입 개발

### 환경 설정
- [ ] 프로토타입 프로젝트 초기화 (Vue 3 + Vite)
- [ ] Vuetify 설치 및 설정
- [ ] Vue Router 설정
- [ ] Pinia 설치 (선택사항)

### 목업 데이터
- [ ] 사용자 데이터 (4가지 역할)
- [ ] 현장 데이터
- [ ] 출역 기록 데이터
- [ ] 작업일보 데이터
- [ ] 이의 제기 데이터

### 레이아웃
- [ ] PC 레이아웃 (좌측 LNB)
- [ ] 모바일 레이아웃 (하단 Bottom Tab + Drawer)
- [ ] 반응형 브레이크포인트 적용

### 화면 구현
- [ ] 로그인 화면 (PC/모바일)
- [ ] 회원가입 화면
- [ ] 본사 관리자 대시보드
- [ ] 현장 관리자 대시보드
- [ ] 팀장 대시보드
- [ ] 노무자 대시보드
- [ ] 안면 등록 화면 (5단계)
- [ ] 출역 체크 화면
- [ ] 출역 현황 리스트 (PC/모바일)
- [ ] 예외 승인 화면
- [ ] 작업일보 작성 화면
- [ ] 작업일보 목록
- [ ] 월간 출역 현황 (달력)
- [ ] 이의 제기 화면

### 라우팅
- [ ] 모든 화면 라우팅 설정
- [ ] 역할별 라우팅 가드 (프로토타입용)

### 완료 검증
- [ ] 모든 화면 링크/클릭으로 동작 확인
- [ ] 반응형 디자인 확인 (PC/모바일)
- [ ] 사용자 피드백 수집

---

## Phase 1: 인증 및 사용자 관리

### 백엔드 개발

#### 프로젝트 설정
- [ ] Spring Boot 프로젝트 생성
- [ ] 의존성 추가 (Spring Security, JWT, OAuth2, JPA, MSSQL)
- [ ] application.yml 설정

#### 데이터베이스
- [ ] MSSQL 연결 설정
- [ ] Users 테이블 생성
- [ ] Social_Login 테이블 생성
- [ ] Pass_Verification 테이블 생성
- [ ] JPA Entity 생성
- [ ] Repository 생성

#### 인증 API
- [ ] `POST /api/auth/login` (소셜 로그인)
- [ ] `GET /api/auth/callback/{provider}` (OAuth 콜백)
- [ ] `POST /api/auth/pass-verify` (PASS 인증)
- [ ] `POST /api/auth/refresh` (토큰 갱신)
- [ ] `POST /api/auth/logout` (로그아웃)

#### 사용자 관리 API
- [ ] `GET /api/users/me` (내 정보 조회)
- [ ] `GET /api/users/{id}` (사용자 조회)
- [ ] `GET /api/users` (사용자 목록)
- [ ] `PUT /api/users/{id}` (사용자 수정)

#### 안면 등록 API
- [ ] `POST /api/face/register` (안면 등록)
- [ ] `GET /api/face/status` (안면 등록 여부 확인)

#### 보안
- [ ] JWT 토큰 생성/검증
- [ ] Spring Security 설정
- [ ] 역할 기반 접근 제어 (RBAC)
- [ ] CORS 설정

#### 테스트 코드
- [ ] `AuthControllerTest` 작성 및 통과
- [ ] `UserServiceTest` 작성 및 통과
- [ ] `FaceServiceTest` 작성 및 통과
- [ ] 통합 테스트 작성 및 통과

### 프론트엔드 개발

#### 프로젝트 설정
- [ ] Vue 3 + Vite 프로젝트 생성
- [ ] Vuetify 설치 및 설정
- [ ] Pinia 설치 및 설정
- [ ] Vue Router 설정
- [ ] Axios 설치 및 설정

#### 인증 화면
- [ ] 로그인 페이지 (PC/모바일 반응형)
- [ ] 회원가입 페이지
- [ ] PASS 인증 페이지
- [ ] 소셜 로그인 버튼 (카카오/네이버)

#### 상태 관리
- [ ] `auth.store.ts` (인증 상태)
- [ ] `user.store.ts` (사용자 정보)

#### API 서비스
- [ ] `api.service.ts` (HTTP 클라이언트)
- [ ] `auth.service.ts` (인증 API)
- [ ] `user.service.ts` (사용자 API)
- [ ] `face.service.ts` (안면 API)

#### 안면 등록
- [ ] 안면 등록 화면 (5단계)
- [ ] 카메라 접근 권한 요청
- [ ] 안면 촬영 기능
- [ ] 이미지 업로드
- [ ] 에러 처리 및 재시도

#### 라우터 가드
- [ ] 인증 필요 페이지 보호
- [ ] 역할별 접근 제어

#### 테스트 코드
- [ ] `LoginView.spec.ts` 작성 및 통과
- [ ] `auth.store.spec.ts` 작성 및 통과
- [ ] `FaceRegister.spec.ts` 작성 및 통과
- [ ] E2E 테스트 작성 및 통과

---

## Phase 2: 출역 체크 및 대시보드

### 백엔드 개발

#### 데이터베이스
- [ ] Sites 테이블 생성
- [ ] User_Sites 테이블 생성
- [ ] Attendance_Log 테이블 생성
- [ ] JPA Entity 생성
- [ ] Repository 생성

#### 출역 관리 API
- [ ] `POST /api/attendance/check-in` (출역 체크)
- [ ] `GET /api/attendance/list` (출역 목록)
- [ ] `GET /api/attendance/{id}` (출역 상세)
- [ ] `POST /api/attendance/manual-approve` (수동 승인)

#### 대시보드 API
- [ ] `GET /api/dashboard/admin` (본사 관리자)
- [ ] `GET /api/dashboard/site` (현장 관리자)
- [ ] `GET /api/dashboard/team` (팀장)
- [ ] `GET /api/dashboard/worker` (노무자)

#### 안면인식 API 연동
- [ ] 신한홀딩스 Face API 연동
- [ ] 비동기 처리 구현
- [ ] 재시도 로직 구현
- [ ] 타임아웃 처리
- [ ] 에러 처리

#### 실시간 알림
- [ ] WebSocket 또는 Server-Sent Events 설정
- [ ] 예외 승인 요청 알림
- [ ] 푸시 알림 설정 (선택사항)

#### 테스트 코드
- [ ] `AttendanceControllerTest` 작성 및 통과
- [ ] `DashboardControllerTest` 작성 및 통과
- [ ] `FaceRecognitionServiceTest` 작성 및 통과
- [ ] 통합 테스트 작성 및 통과

### 프론트엔드 개발

#### 출역 체크 화면
- [ ] 출역 체크 버튼
- [ ] 안면인식 촬영
- [ ] 로딩 인디케이터
- [ ] 결과 표시 (성공/실패)
- [ ] 에러 처리

#### 대시보드 화면
- [ ] 본사 관리자 대시보드
  - [ ] 전체 현장 수
  - [ ] 금일 총 출역 인원
  - [ ] 전자계약 체결률
  - [ ] 안면 미등록자 Top 5
  - [ ] 월별 출역 통계 (그래프)
  - [ ] 현장별 출역 현황 (테이블)
  - [ ] 엑셀 다운로드
- [ ] 현장 관리자 대시보드
  - [ ] PC: 테이블 형태
  - [ ] 모바일: 카드 형태
  - [ ] 실시간 업데이트
  - [ ] 필터 및 정렬
  - [ ] 엑셀 다운로드
- [ ] 팀장 대시보드
- [ ] 노무자 대시보드

#### 출역 관리 화면
- [ ] 출역 현황 리스트
- [ ] PC: 테이블 형태 (정렬, 필터)
- [ ] 모바일: 카드 형태 (Pull-to-refresh)
- [ ] 상태별 색상 구분 (정상/지각/미출역)

#### 예외 승인 화면
- [ ] PC: 모달 팝업
- [ ] 모바일: 전체 화면
- [ ] 승인 사유 선택
- [ ] 승인/반려 처리

#### 실시간 업데이트
- [ ] WebSocket 또는 Server-Sent Events 연결
- [ ] 새 출역 기록 자동 갱신

#### 테스트 코드
- [ ] `AttendanceCheck.spec.ts` 작성 및 통과
- [ ] `Dashboard.spec.ts` 작성 및 통과
- [ ] E2E 테스트 작성 및 통과

---

## Phase 3: 작업일보 및 마이페이지

### 백엔드 개발

#### 데이터베이스
- [ ] Daily_Report 테이블 생성
- [ ] Attendance_Dispute 테이블 생성
- [ ] Master_Code 테이블 생성
- [ ] JPA Entity 생성
- [ ] Repository 생성

#### 작업일보 API
- [ ] `GET /api/reports` (작업일보 목록)
- [ ] `POST /api/reports` (작업일보 작성)
- [ ] `GET /api/reports/{id}` (작업일보 상세)
- [ ] `PUT /api/reports/{id}` (작업일보 수정)
- [ ] `DELETE /api/reports/{id}` (작업일보 삭제)
- [ ] `POST /api/reports/{id}/approve` (작업일보 승인)

#### 마이페이지 API
- [ ] `GET /api/mypage/attendance` (출역 현황)
- [ ] `GET /api/mypage/dispute` (이의 제기 목록)
- [ ] `POST /api/mypage/dispute` (이의 제기 신청)
- [ ] `POST /api/mypage/dispute/{id}/approve` (이의 제기 승인)
- [ ] `POST /api/mypage/dispute/{id}/reject` (이의 제기 반려)

#### 파일 업로드
- [ ] 이미지 업로드 API
- [ ] 이미지 리사이징/압축
- [ ] 파일 저장 (로컬 또는 S3)

#### 테스트 코드
- [ ] `ReportControllerTest` 작성 및 통과
- [ ] `MypageControllerTest` 작성 및 통과
- [ ] 통합 테스트 작성 및 통과

### 프론트엔드 개발

#### 작업일보 화면
- [ ] 작업일보 작성 화면
  - [ ] 작업일자 선택
  - [ ] 공종 선택
  - [ ] 작업내용 입력
  - [ ] 사진 업로드 (카메라/갤러리)
  - [ ] 이미지 리사이징/압축
  - [ ] 임시저장 기능
- [ ] 작업일보 목록
- [ ] 작업일보 상세
- [ ] 작업일보 수정/삭제

#### 마이페이지 화면
- [ ] 월간 출역 현황
  - [ ] PC: Full Calendar
  - [ ] 모바일: 주간 달력 또는 리스트
  - [ ] 일별 상세 정보
  - [ ] 색상 구분 (정상/지각/미출역/수동승인)
- [ ] 이의 제기 화면
  - [ ] 이의 제기 신청
  - [ ] 이의 제기 목록
  - [ ] 상태 표시 (요청/처리중/승인/반려)
- [ ] 안면 정보 관리

#### 테스트 코드
- [ ] `ReportForm.spec.ts` 작성 및 통과
- [ ] `MypageAttendance.spec.ts` 작성 및 통과
- [ ] E2E 테스트 작성 및 통과

---

## Phase 4: 테스트 및 최적화

### 통합 테스트
- [ ] E2E 테스트 시나리오 작성
- [ ] 주요 사용자 플로우 테스트
  - [ ] 로그인 → 안면 등록 → 출역 체크
  - [ ] 작업일보 작성 → 승인
  - [ ] 이의 제기 → 승인/반려
- [ ] 크로스 브라우저 테스트
  - [ ] Chrome
  - [ ] Firefox
  - [ ] Safari
  - [ ] Edge
  - [ ] 모바일 브라우저 (iOS Safari, Chrome)

### 성능 최적화

#### 프론트엔드
- [ ] 번들 크기 최적화
- [ ] 코드 스플리팅
- [ ] 이미지 최적화 (WebP, Lazy Loading)
- [ ] API 호출 최적화
- [ ] 캐싱 전략

#### 백엔드
- [ ] 쿼리 최적화
- [ ] 인덱스 최적화
- [ ] 캐싱 전략 (Redis 등)
- [ ] API 응답 시간 개선 (목표: 500ms 이내)

### 보안 점검
- [ ] JWT 토큰 보안 검증
- [ ] 권한 검증 확인
- [ ] 민감 정보 암호화 확인
- [ ] SQL Injection 방지 확인
- [ ] XSS 방지 확인
- [ ] CSRF 방지 확인

### 접근성
- [ ] 키보드 네비게이션 지원
- [ ] 스크린 리더 호환성
- [ ] 색상 대비 확인

---

## Phase 5: 배포 및 안정화

### 배포 준비
- [ ] 프로덕션 환경 변수 설정
- [ ] 데이터베이스 마이그레이션 스크립트
- [ ] SSL 인증서 설정
- [ ] 빌드 스크립트 작성

### 웹앱 배포
- [ ] 프론트엔드 빌드
- [ ] 정적 파일 서버 배포 (Nginx, S3 등)
- [ ] CDN 설정 (선택사항)

### 백엔드 배포
- [ ] Spring Boot JAR 빌드
- [ ] 애플리케이션 서버 배포
- [ ] 데이터베이스 연결 확인

### 모바일앱 배포
- [ ] Capacitor 프로젝트 설정
- [ ] Android 빌드 (APK/AAB)
- [ ] iOS 빌드 (IPA)
- [ ] Google Play Store 배포
- [ ] App Store 배포

### 모니터링
- [ ] 로그 수집 설정
- [ ] 에러 추적 설정 (Sentry 등)
- [ ] 성능 모니터링 설정
- [ ] 알림 설정

### 안정화
- [ ] 모니터링 기간 운영 (1-2주)
- [ ] 버그 수정
- [ ] 성능 튜닝
- [ ] 사용자 피드백 수집 및 반영

---

## 공통 체크리스트

### 코드 품질
- [ ] 코드 리뷰 완료
- [ ] 코딩 컨벤션 준수
- [ ] 주석 작성 (복잡한 로직)
- [ ] 에러 처리 구현

### 문서화
- [ ] API 문서 작성 (Swagger)
- [ ] README 작성
- [ ] 개발 가이드 작성
- [ ] 배포 가이드 작성

### Git 관리
- [ ] 브랜치 전략 준수
- [ ] 커밋 메시지 규칙 준수
- [ ] PR 템플릿 사용

### 테스트 커버리지
- [ ] 백엔드 테스트 커버리지 80% 이상
- [ ] 프론트엔드 테스트 커버리지 70% 이상

---

## 우선순위별 체크리스트

### P0 (최우선)
- [ ] 소셜 로그인 (카카오/네이버)
- [ ] PASS 인증
- [ ] 반응형 레이아웃
- [ ] 안면 등록
- [ ] 안면인식 출역 체크
- [ ] 현장 관리자 대시보드
- [ ] 예외 승인 처리

### P1 (높음)
- [ ] 본사 관리자 대시보드
- [ ] 작업일보 작성/관리
- [ ] 월간 출역 현황
- [ ] 이의 제기

### P2 (중간)
- [ ] 안면 정보 관리
- [ ] 전자계약 관리 (추후)

---

**문서 끝**

