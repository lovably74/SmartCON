# SmartCON Lite 구축 상세 요구사항 정의서 (통합본)

**문서 버전:** 3.0  
**작성일:** 2025년 12월 31일  
**최종 수정일:** 2025년 12월 31일  
**작성자:** Kiro AI Assistant  
**통합 기반 문서:** PRD v2.8, Functional-Specification v2.1, Platform-API-Specification v1.0

---

## 1. 프로젝트 개요 (Project Overview)

본 프로젝트는 건설 현장의 인력 및 노무 관리를 위한 **'스마트콘 라이트(SmartCON Lite)'** SaaS 플랫폼을 구축하는 것을 목표로 합니다.  
구독형 모델을 기반으로 하며, 회사(Tenant) 단위의 데이터 격리와 역할별 최적화된 기능을 제공합니다.

### 1.1 핵심 목표
- **SaaS Platform**: 멀티테넌시(Multi-tenancy) 및 구독 결제/관리 시스템 구축
- **Role-Based Optimization**: 본사/현장/팀장/노무자 + **시스템 관리자(Super Admin)** 5단계 역할 지원
- **Automation**: 안면인식(FaceNet) 출역 자동화, 전자계약 원스톱 처리, 세금계산서 발행(API)
- **Mobile First**: 현장 중심의 하이브리드 앱 (React + Capacitor)

### 1.2 핵심 가치 제안
**"현장의 신뢰, 기술로 완성하다" (Building Trust Through Technology)**

- **Transparency**: 객관적인 안면인식 기반 출역 관리
- **Convenience**: 모바일 우선의 직관적 사용자 경험  
- **Flexibility**: 다양한 건설 현장 환경에 적응 가능한 시스템

### 1.3 주요 기능
- **안면인식 출역**: FaceNet 기반 자동 출퇴근 체크
- **멀티테넌트 SaaS**: 회사별 데이터 격리 및 구독 결제
- **역할 기반 접근**: 5단계 사용자 역할 (슈퍼관리자, 본사관리자, 현장관리자, 팀장, 노무자)
- **전자계약**: PDF 생성 및 디지털 서명
- **실시간 대시보드**: 역할별 맞춤 대시보드 및 실시간 출역 모니터링
- **작업일보 관리**: 날씨 API 연동 자동 일보 작성
- **모바일 우선 설계**: React + Capacitor 하이브리드 앱

### 1.4 대상 사용자
- 인력 관리가 필요한 건설업체
- 실시간 출역 추적이 필요한 현장 관리자
- 작업자 배정을 관리하는 팀장
- 간편한 출역 및 계약 관리가 필요한 노무자

### 1.5 비즈니스 모델
- 멀티테넌트 아키텍처 기반 구독형 SaaS
- 현장별 또는 작업자별 요금제 모델
- 세금계산서 자동 발행을 통한 자동화된 결제

---

## 2. 기술 스택 (Tech Stack)

### 2.1 Backend
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 3.3.x
- **Database**: MariaDB 10.11 (JPA/Hibernate) - Multi-tenant Schema
- **Batch**: Spring Batch (안면인식 데이터 동기화, 구독 결제/정산)
- **Security**: Spring Security 6.x + JWT + OAuth2 Client
- **Infrastructure**: Docker, AWS (S3 for files)

### 2.2 Frontend (Web & Mobile WebApp)
- **Framework**: React 18 + TypeScript + Vite
- **State Mgmt**: TanStack Query + Zustand
- **UI System**: **Shadcn/UI** + **Tailwind CSS** (Custom Design System applied)
- **Mobile Bridge**: Capacitor (Camera, Geolocation, Push, Deep Link)

### 2.3 External Interfaces
- **Face Recognition**: FaceNet (Face Embedding & Matching Server)
- **Weather**: OpenWeatherMap / KMA API
- **Direct Messaging**: SMS/AlimTalk Gateway (BizMsg etc.)
- **Payment & Tax**: PG사 연동 (Subscription) + 홈택스 연동 API (Tax Invoice)

### 2.4 UI/UX 요구사항 (PC Web & Mobile WebApp)

#### 2.4.1. 색상 원칙 (눈이 편안한 화면)

- **배경(큰 면적)**: `#FAFAFA`(Off-White) 기반의 중립 배경을 기본값으로 사용
- **Surface(카드/모달)**: `#FFFFFF`
- **Primary(회사 주색상)**: `#71AA44`(CMYK 55 10 95 00 기반)  
  - **사용 위치 제한**: 핵심 CTA/활성 상태/중요 강조(포인트) 중심으로만 사용  
  - **대면적 채움 금지**: 강한 색(노랑/형광/순색) 및 Primary 풀배경은 눈부심 유발 → 금지
- **텍스트 대비**: 본문은 다크 그레이 계열(예: `#262626`), 보조 텍스트는 중간 회색(예: `#737373`)로 가독성 확보

#### 2.4.2. 내비게이션 구조 (직관성)

- **PC Web**:
  - 좌측 **접기/펼치기 가능한 사이드바**(아이콘+라벨, 역할별 메뉴) + 상단 헤더(페이지 타이틀/아이콘) 구조
  - **대시보드는 메뉴에서 제거**하고, **로그인 후 최초 진입 화면은 대시보드**로 고정
  - 좌측 상단 **SmartCON 로고 클릭 시 현재 역할의 대시보드로 이동** (빠른 "홈" 동작)
  - 상단 헤더 우측에 **다국어/현장변경/알림** 아이콘 클러스터 배치 (일관된 위치)
- **Mobile Web**:
  - 하단 **Bottom Navigation(핵심 4개 탭)** + 상단 **Sticky Header** + 선택/상세는 **Bottom Sheet** 패턴 권장
  - **홈 탭은 대시보드로 이동** (항상 "첫 화면"으로 복귀)

#### 2.4.3. 대시보드(위젯형) 구성

- 대시보드는 "정보 스캔"에 최적화된 **위젯(Widget) 조합**으로 구성
  - KPI 카드(아이콘+수치+보조지표)
  - 리스트/요약 위젯(예: 현장별 출역 현황, 미서명 계약 현황)
  - 상태 색상은 Success/Warning/Destructive 범위로 제한

#### 2.4.4. 접근성/사용성 (필수)

- **키보드 포커스/링** 제공, 터치 타겟 최소 44px 권장
- **WCAG 2.1 AA** 대비 준수 (텍스트/아이콘/상태색)
- 로딩/빈 상태/오류 상태 UI 제공 (스켈레톤/토스트/에러 안내)

---

## 3. 사용자 역할 및 권한 (User Roles)

### 3.1. 슈퍼 어드민 (Super Admin / SaaS Admin)
- **권한**: 플랫폼 운영의 최상위 관리자
- **주요 기능**:
  - **Tenant 관리**: 가입 회사 현황, 서비스 상태(활성/중지) 관리
  - **구독/결제 모니터링**: 자동 결제 현황, 미수금 관리
  - **정산/세무**: 세금계산서 자동 발행 내역 확인, 국세청 전송 상태 모니터링

### 3.2. 본사 관리자 (HQ Administrator / Tenant Admin)
- **인증**: 인트로 페이지 -> 로그인 버튼 -> 본사 관리자 로그인 선택 -> ID(사업자번호) + 비밀번호
- **주요 기능**:
  - **회사/현장 관리**: 현장 개설 및 관리자 초대
  - **기준 정보**: 공종, 장비, 노무 단가 표준 설정
  - **구독 관리**: 요금제 변경, 결제 수단 등록, 청구서 조회
  - **전사 대시보드**: 현장별 공정률, 출역 인원, 계약 체결률 모니터링

### 3.3. 현장 관리자 (Site Manager)
- **인증**: 인트로 페이지 -> 로그인 버튼 -> 소셜 로그인 선택 -> 소셜 로그인 (Kakao, Naver) -> 현장 선택
- **주요 기능**:
  - **운영/설정**: 현장 전용 공종/장비 설정, 안면인식기 관리
  - **작업 관리**: 작업 팀 배정(Request), 출역 마감(Approval)
  - **공사일보**: 날씨 자동 연동, 공종별 인원/장비 취합 및 승인
  - **노무 관리**: 미서명 근로계약서 일괄 요청

### 3.4. 노무 팀장 (Team Leader)
- **인증**: 인트로 페이지 -> 로그인 버튼 -> 소셜 로그인 선택 -> 소셜 로그인 -> 현장 선택
- **주요 기능**:
  - **팀 관리**: 팀원 초대(Link/SMS) 및 승인
  - **작업 수행**: 현장 작업 요청 수락/거부, 내일 투입 인원 보고
  - **출역 관리**: 팀원 출역 현황 확인 및 이의 제기, 근로계약 독려

### 3.5. 노무자 (Worker)
- **인증**: 인트로 페이지 -> 로그인 버튼 -> 소셜 로그인 선택 -> 소셜 로그인 -> 현장 선택
- **주요 기능**:
  - **출역 조회**: 일별/월별 공수 및 예상 노임 확인
  - **전자 계약**: 근로계약서 전자 서명 (Canvas)
  - **내 정보**: 안면인식 사진 등록, 계좌번호 관리

---

## 4. 핵심 프로세스 및 기능 명세

### 4.1. 구독 및 테넌트 온보딩 (SaaS Onboarding)
1.  **신청**: 사업자번호 인증 -> 회원가입 (법인/개인사업자)
2.  **구독 설정**: 요금제 선택 -> 결제 수단(카드/CMS) 등록
3.  **개통**: Tenant 생성 및 HQ Admin 계정 활성화 -> 즉시 이용 가능

### 4.2. 안면인식 데이터 파이프라인 (FaceNet Flow)
- **등록**: 사용자 앱에서 셀카 등록 -> SmartCON 서버 저장 -> FaceNet 서버로 임베딩 요청
- **활성화 (Daily Batch)**:
  - 매일 자정 (`00:00`) 실행
  - 금일 작업 투입 예정인 사용자(`WorkAssignment` 승인 건) 필터링
  - 해당 사용자의 Face Embedding 데이터를 안면인식기(Edge/Cloud) 활성 리스트에 로드
- **삭제**: 투입 기간 종료 시 활성 리스트에서 제거 (개인정보 보호)

### 4.3. 작업 배정 및 일보 취합 (Work & Report)
- **작업 요청**: Site Mgr가 '내일'의 공종/위치/인원을 특정 팀에게 요청
- **수락/거부**: Team Lead가 요청 확인 후 수락 여부 결정 (Push 알림)
- **일보 자동화**:
  - 오후 5시 등 특정 시점에 Team Lead가 제출한 투입 내역 집계
  - 기상청 API로 날씨/기온 자동 입력
  - Site Mgr는 집계된 데이터를 바탕으로 수정/보완 후 최종 승인

### 4.4. 전자근로계약 (E-Contract)
- **자동 생성**: 출역 확정 시 표준근로계약서 템플릿에 데이터 바인딩
- **서명 프로세스**:
  - 노무자 앱에서 '서명 필요' 알림 수신
  - 계약서 내용 확인 후 전자 서명 패드 서명
  - PDF 변환 후 S3 저장 및 Hash 값 DB 기록 (위변조 방지)

---

## 5. 인증 플로우 (Authentication Flow)

### 5.1. 인트로 페이지 (Landing Page)
- **경로**: `/` (루트 경로)
- **목적**: 서비스 소개 및 로그인 진입점 제공
- **구성 요소**:
  - 메인 메시지: "안전관리, 이제는 스마트하게!"
  - 서브 메시지: "본사와 현장관리까지 한 번에 업무 끝! 스마트콘 [SmartCON]"
  - 로그인 버튼: 로그인 방식 선택 페이지로 이동
  - 문의 및 데모신청 섹션(필수): 문의 폼 제공
- **디자인 기준**: [`ismartcon.net 인트로`](https://www.ismartcon.net/login/smart/index) 구성(메뉴 라벨/섹션/푸터 정보 포함)과 동일하게 구현

#### 5.1.1. 인트로 상단 메뉴(PC)

- **메뉴 라벨(고정)**:
  - 비대면바우처(배지)
  - 스마트콘 소개
  - 스마트콘 핵심기능
  - 서비스요금
  - 문의하기
  - 로그인
- **모바일**: 상단은 로그인 버튼 중심으로 단순화하고, 섹션 이동은 스크롤/CTA로 제공

#### 5.1.2. 문의 및 데모신청(폼) 요구사항

- **필드(동일)**:
  - 회사명
  - 담당자성함/직책
  - 연락처
  - 이메일
  - 로봇방지질문(간단 합계)
  - 개인정보 수집 및 이용 동의(필수)
  - 마케팅 수집 및 활용동의(선택)
  - 문의내용
- **반응형**:
  - Mobile: 단일 컬럼(세로 스택)
  - Desktop: 2컬럼(입력/문의내용 분리)

### 5.2. 로그인 방식 선택
- **경로**: `/login`
- **기능**: 사용자가 로그인 방식을 선택
  - **본사 관리자 로그인**: ID(사업자번호) + 비밀번호 방식
  - **소셜 로그인**: Kakao, Naver 소셜 로그인

### 5.3. 본사 관리자 로그인
- **경로**: `/login/hq`
- **인증 방식**: 사업자번호(ID) + 비밀번호

### 5.4. 소셜 로그인
- **경로**: `/login/social`
- **인증 방식**: Kakao, Naver OAuth2
- **후속 처리**: 로그인 성공 후 현장 선택 또는 역할 선택

---

## 6. 시스템 아키텍처 및 데이터 모델

### 6.1 멀티테넌트 아키텍처
- **격리 방식**: Shared Database, Shared Schema with tenant_id
- **테넌트 식별**: JWT 토큰 내 tenant_id 포함
- **데이터 격리**: 모든 비즈니스 테이블에 tenant_id 컬럼 강제
- **파일 격리**: S3 경로에 tenant_id 포함 (s3://bucket/{tenant_id}/...)

### 6.2 핵심 엔티티 설계

#### 6.2.1 사용자 및 권한 관련
```java
// 사용자 엔티티
@Entity
@Table(name = "users")
public class User extends BaseTenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // LOCAL, KAKAO, NAVER
}

// 테넌트 엔티티
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "business_number", nullable = false, unique = true)
    private String businessNumber;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Enumerated(EnumType.STRING)
    private TenantStatus status; // TRIAL, ACTIVE, SUSPENDED, TERMINATED
}
```

#### 6.2.2 현장 및 작업 관련
```java
// 현장 엔티티
@Entity
@Table(name = "sites")
public class Site extends BaseTenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    
    @Enumerated(EnumType.STRING)
    private SiteStatus status; // ACTIVE, PAUSED, COMPLETED
}

// 작업 배정 엔티티
@Entity
@Table(name = "work_assignments")
public class WorkAssignment extends BaseTenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status; // REQUESTED, ACCEPTED, REJECTED, APPROVED
}
```

### 6.3 REST API 설계 원칙

#### 6.3.1 URL 구조
```
/api/v1/{domain}/{resource}[/{id}][/{sub-resource}]

예시:
- GET /api/v1/sites - 현장 목록 조회
- GET /api/v1/sites/123 - 특정 현장 조회
- POST /api/v1/attendance/logs - 출역 기록 생성
- PUT /api/v1/contracts/456/sign - 계약서 서명
```

#### 6.3.2 응답 형식 표준화
```json
// 성공 응답
{
  "success": true,
  "data": {
    // 실제 데이터
  },
  "message": "요청이 성공적으로 처리되었습니다.",
  "timestamp": "2025-12-31T10:30:00Z"
}

// 오류 응답
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 데이터가 올바르지 않습니다.",
    "details": [
      {
        "field": "email",
        "message": "이메일 형식이 올바르지 않습니다."
      }
    ]
  },
  "timestamp": "2025-12-31T10:30:00Z"
}
```

---

## 7. 외부 시스템 연동 명세

### 7.1 FaceNet 안면인식 시스템
#### 7.1.1 연동 개요
FaceNet 기반 안면인식 서버와의 API 연동을 통해 얼굴 임베딩 생성 및 매칭을 수행합니다.

#### 7.1.2 API 명세
```http
POST /api/v1/face/embedding
Content-Type: multipart/form-data

Parameters:
- image: 얼굴 이미지 파일 (JPG, PNG)
- quality: 품질 검증 여부 (true/false)

Response:
{
  "success": true,
  "embedding": [0.123, -0.456, ...], // 512차원 벡터
  "confidence": 0.95,
  "face_detected": true,
  "quality_score": 0.87
}
```

### 7.2 기상청 날씨 API
#### 7.2.1 연동 개요
공사일보 작성 시 현장 위치 기반 날씨 정보를 자동으로 수집합니다.

### 7.3 PG사 결제 시스템
#### 7.3.1 연동 개요
구독 결제 및 자동 결제를 위한 PG사 API 연동입니다.

### 7.4 홈택스 세금계산서 API
#### 7.4.1 연동 개요
매월 자동으로 세금계산서를 발행하고 국세청에 전송합니다.

---

## 8. 비기능 요구사항

### 8.1 성능 요구사항
- **API 응답 시간**: 평균 200ms 이하, 95% 1초 이하
- **페이지 로딩**: 초기 로딩 3초 이하
- **안면인식**: 1초 이내 인식 완료
- **동시 사용자**: 1,000명 이상 지원
- **가용성**: 99.9% 이상 (월 기준)

### 8.2 보안 요구사항
- **인증**: JWT 토큰 기반 인증 (Access Token 15분, Refresh Token 7일)
- **데이터 암호화**: 개인정보 AES-256 암호화
- **전송 보안**: HTTPS 강제, TLS 1.3 이상
- **접근 제어**: 역할 기반 접근 제어 (RBAC)
- **API 보안**: Rate Limiting (IP당 분당 100회)

### 8.3 사용성 요구사항
- **딥링크 (Deep Linking)**: SMS 초대 링크 클릭 시 -> 앱 미설치 시 스토어 이동 / 설치 시 앱 실행 후 해당 화면(초대 수락)으로 직행
- **반응형 웹**: PC(1920px), Tablet(768px), Mobile(375px) 대응 (Mobile First Design)
- **다국어**: i18n 적용 (한국어, 영어 기본 지원)
- **접근성**: WCAG 2.1 AA 준수

---

## 9. 개발 및 배포 계획

### 9.1 개발 단계
1. **Phase 0**: 프로젝트 셋업 및 프로토타입 완성 (2주)
2. **Phase 1**: 핵심 인프라 및 인증 시스템 (3주)
3. **Phase 2**: 핵심 비즈니스 로직 (4주)
4. **Phase 3**: 안면인식 및 고급 기능 (4주)
5. **Phase 4**: 모바일 앱 및 최적화 (3주)
6. **Phase 5**: 테스트 및 배포 (2주)

### 9.2 기술적 위험 요소
- **안면인식 API 연동**: 대체 API 준비, 수동 출역 백업
- **멀티테넌트 성능**: 쿼리 최적화, 캐싱 전략
- **모바일 앱 스토어 승인**: 웹앱 우선 배포, PWA 대안

### 9.3 성공 지표 (KPI)
- **기술적 지표**: 응답시간 < 200ms, 가동률 > 99.9%
- **비즈니스 지표**: 월간 활성 사용자 1,000명, 고객 만족도 4.5/5.0
- **운영 지표**: 자동화율 90%, 장애 복구 시간 < 1시간

---

## 10. 결론 및 다음 단계

### 10.1 프로젝트 요약
SmartCON Lite는 건설 현장의 디지털 전환을 위한 종합적인 SaaS 플랫폼입니다. 안면인식 기술을 활용한 자동화된 출역 관리와 전자계약 시스템을 통해 현장 운영의 효율성과 투명성을 크게 향상시킬 것입니다.

### 10.2 핵심 차별화 요소
1. **FaceNet 기반 안면인식**: 정확하고 신뢰할 수 있는 출역 관리
2. **멀티테넌트 SaaS**: 확장 가능한 구독형 비즈니스 모델
3. **모바일 우선**: 현장 중심의 사용자 경험
4. **완전 자동화**: 일보 작성부터 세금계산서 발행까지 자동화

### 10.3 기대 효과
- **운영 효율성**: 수작업 대비 70% 시간 단축
- **데이터 정확성**: 안면인식을 통한 99.5% 정확도
- **비용 절감**: 관리 비용 50% 절감
- **규정 준수**: 근로기준법 및 개인정보보호법 완전 준수

---

**문서 정보**  
- **버전**: 3.0
- **최종 업데이트**: 2025년 12월 31일
- **다음 리뷰**: 2026년 1월 7일
- **승인자**: 프로젝트 매니저 및 기술 책임자 검토 필요

*본 문서는 SmartCON Lite 프로젝트의 전체 요구사항을 정의하는 핵심 문서입니다. 모든 개발 활동은 본 문서의 요구사항을 기반으로 수행되어야 합니다.*