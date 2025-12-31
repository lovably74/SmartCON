# SmartCON Lite 클래스 명세서

## 개요

SmartCON Lite는 건설 현장 인력 관리를 위한 SaaS 플랫폼으로, 멀티테넌트 아키텍처를 기반으로 구독 관리, 사용자 관리, 출석 관리 등의 기능을 제공합니다. 본 문서는 현재 구현된 클래스와 향후 구현 예정인 클래스들의 명세를 정리합니다.

## 아키텍처 개요

### 기술 스택
- **Backend**: Java 17 + Spring Boot 3.3.x + JPA/Hibernate + MariaDB
- **Frontend**: React 18 + TypeScript + Vite + Tailwind CSS
- **상태 관리**: Zustand + TanStack Query
- **테스트**: Vitest + React Testing Library (Frontend), JUnit 5 + jqwik (Backend)

### 패키지 구조
```
backend/src/main/java/com/smartcon/
├── global/                 # 전역 설정 및 공통 기능
├── domain/                 # 도메인별 비즈니스 로직
│   ├── admin/             # 슈퍼관리자 기능
│   ├── subscription/      # 구독 관리
│   ├── tenant/            # 테넌트 관리
│   ├── user/              # 사용자 관리
│   ├── attendance/        # 출석 관리
│   └── billing/           # 결제 관리
└── infra/                 # 외부 연동 (계획)
```

---

## 1. Global 패키지 (전역 공통)

### 1.1 Entity 계층

#### BaseEntity (추상 클래스)
**파일**: `global/entity/BaseEntity.java`
**목적**: 모든 엔티티의 공통 부모 클래스
**주요 기능**:
- ID, 생성일시, 수정일시 관리
- MariaDB 최적화된 TIMESTAMP 타입 사용
- JPA Auditing 지원

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
```

#### BaseTenantEntity (추상 클래스)
**파일**: `global/tenant/BaseTenantEntity.java`
**목적**: 멀티테넌트 데이터 격리를 위한 기본 클래스
**주요 기능**:
- 테넌트 ID 자동 관리
- Hibernate Filter를 통한 데이터 격리
- 테넌트 컨텍스트 자동 설정

```java
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity extends BaseEntity {
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;
}
```

### 1.2 Configuration 계층

#### SecurityConfig
**파일**: `global/config/SecurityConfig.java`
**목적**: Spring Security 설정
**주요 기능**:
- JWT 인증 설정
- 역할 기반 접근 제어
- CORS 설정

#### MultiTenantConfig
**파일**: `global/config/MultiTenantConfig.java`
**목적**: 멀티테넌트 설정
**주요 기능**:
- Hibernate Filter 설정
- 테넌트 컨텍스트 관리

#### CacheConfig
**파일**: `global/config/CacheConfig.java`
**목적**: 캐시 설정
**주요 기능**:
- Redis 캐시 설정
- 캐시 전략 정의

#### AsyncConfig
**파일**: `global/config/AsyncConfig.java`
**목적**: 비동기 처리 설정
**주요 기능**:
- 스레드 풀 설정
- 비동기 메서드 실행 환경

### 1.3 Security 계층

#### JwtAuthenticationFilter
**파일**: `global/security/JwtAuthenticationFilter.java`
**목적**: JWT 토큰 인증 필터
**주요 기능**:
- JWT 토큰 검증
- 사용자 인증 정보 설정

#### SubscriptionAccessInterceptor
**파일**: `global/security/SubscriptionAccessInterceptor.java`
**목적**: 구독 상태 기반 접근 제어
**주요 기능**:
- API 요청 시 구독 상태 검증
- 접근 권한 제어

#### ApiAuditInterceptor
**파일**: `global/security/ApiAuditInterceptor.java`
**목적**: API 호출 감사 로그
**주요 기능**:
- API 호출 이력 기록
- 보안 감사 추적

### 1.4 Common 계층

#### ApiResponse (계획)
**파일**: `global/common/ApiResponse.java`
**목적**: 표준 API 응답 형식
**주요 기능**:
- 성공/실패 응답 통일
- 에러 코드 관리

#### TenantContext
**파일**: `global/tenant/TenantContext.java`
**목적**: 테넌트 컨텍스트 관리
**주요 기능**:
- ThreadLocal 기반 테넌트 ID 관리
- 요청별 테넌트 격리

---

## 2. Domain 패키지 (도메인별)

### 2.1 User Domain (사용자 관리)

#### User Entity
**파일**: `domain/user/entity/User.java`
**목적**: 시스템 사용자 정보 관리
**주요 기능**:
- 사용자 기본 정보 (이름, 이메일, 전화번호)
- 소셜 로그인 연동 (카카오, 네이버)
- 안면인식 임베딩 데이터
- 계정 잠금 관리

```java
@Entity
@Table(name = "users")
public class User extends BaseTenantEntity {
    private String name;
    private String email;
    private String phoneNumber;
    private String socialId;
    private Provider provider;
    private String passwordHash;
    private Boolean isActive;
    private String faceEmbedding;
    private Integer loginFailureCount;
    
    public enum Provider { LOCAL, KAKAO, NAVER }
    public enum Role { ROLE_SUPER, ROLE_HQ, ROLE_SITE, ROLE_TEAM, ROLE_WORKER }
}
```

#### UserRepository (계획)
**파일**: `domain/user/repository/UserRepository.java`
**목적**: 사용자 데이터 접근
**주요 기능**:
- 사용자 CRUD 작업
- 테넌트별 사용자 조회
- 소셜 ID 기반 조회

#### UserService (계획)
**파일**: `domain/user/service/UserService.java`
**목적**: 사용자 비즈니스 로직
**주요 기능**:
- 사용자 등록/수정/삭제
- 로그인 처리
- 계정 잠금/해제

### 2.2 Tenant Domain (테넌트 관리)

#### Tenant Entity (계획)
**파일**: `domain/tenant/entity/Tenant.java`
**목적**: 테넌트(회사) 정보 관리
**주요 기능**:
- 회사 기본 정보
- 구독 상태 관리
- 인증 상태 관리

```java
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {
    private String companyName;
    private String businessNo;
    private String representativeName;
    private String address;
    private String phoneNumber;
    private String email;
    private SubscriptionStatus status;
    private Boolean isVerified;
    
    public enum SubscriptionStatus { 
        PENDING, ACTIVE, SUSPENDED, TERMINATED 
    }
}
```

#### TenantOnboardingController
**파일**: `domain/tenant/controller/TenantOnboardingController.java`
**목적**: 테넌트 온보딩 API
**주요 기능**:
- 테넌트 등록
- 인증 처리

### 2.3 Subscription Domain (구독 관리)

#### Subscription Entity
**파일**: `domain/subscription/entity/Subscription.java`
**목적**: 구독 정보 관리
**주요 기능**:
- 구독 상태 관리
- 요금제 정보
- 결제 주기 관리
- 승인 관련 정보

```java
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
    private Tenant tenant;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BillingCycle billingCycle;
    private BigDecimal monthlyPrice;
    private LocalDateTime approvalRequestedAt;
    private LocalDateTime approvedAt;
    private User approvedBy;
}
```

#### SubscriptionApproval Entity
**파일**: `domain/subscription/entity/SubscriptionApproval.java`
**목적**: 구독 승인 이력 관리
**주요 기능**:
- 승인/거부/중지/종료 이력
- 관리자 정보
- 처리 사유

#### AutoApprovalRule Entity
**파일**: `domain/subscription/entity/AutoApprovalRule.java`
**목적**: 자동 승인 규칙 관리
**주요 기능**:
- 자동 승인 조건 설정
- 규칙 우선순위
- 활성화/비활성화

#### SubscriptionApprovalService
**파일**: `domain/subscription/service/SubscriptionApprovalService.java`
**목적**: 구독 승인 비즈니스 로직
**주요 기능**:
- 구독 승인/거부/중지/종료
- 자동 승인 처리
- 승인 이력 관리
- 성능 최적화된 조회

#### SubscriptionApprovalController
**파일**: `domain/subscription/controller/SubscriptionApprovalController.java`
**목적**: 구독 승인 관리 API
**주요 기능**:
- 승인 대기 목록 조회
- 승인/거부 처리
- 승인 이력 조회

### 2.4 Admin Domain (관리자 기능)

#### SuperAdminService
**파일**: `domain/admin/service/SuperAdminService.java`
**목적**: 슈퍼관리자 기능
**주요 기능**:
- 대시보드 통계
- 테넌트 관리
- 결제 통계
- 승인 통계

#### SuperAdminController
**파일**: `domain/admin/controller/SuperAdminController.java`
**목적**: 슈퍼관리자 API
**주요 기능**:
- 대시보드 데이터 제공
- 테넌트 상태 변경
- 통계 데이터 조회

### 2.5 Attendance Domain (출석 관리) - 계획

#### AttendanceLog Entity
**파일**: `domain/attendance/entity/AttendanceLog.java`
**목적**: 출석 기록 관리
**주요 기능**:
- 출입 시간 기록
- 안면인식 결과
- GPS 위치 정보

```java
@Entity
@Table(name = "attendance_logs")
public class AttendanceLog extends BaseTenantEntity {
    private User user;
    private Site site;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceType type;
    private String faceMatchResult;
    private String gpsLocation;
    
    public enum AttendanceType { CHECK_IN, CHECK_OUT, BREAK_START, BREAK_END }
}
```

### 2.6 Billing Domain (결제 관리) - 계획

#### Payment Entity
**파일**: `domain/subscription/entity/Payment.java`
**목적**: 결제 정보 관리
**주요 기능**:
- 결제 내역
- 결제 상태
- 결제 방법

#### BillingRecord Entity (계획)
**파일**: `domain/billing/entity/BillingRecord.java`
**목적**: 청구 기록 관리
**주요 기능**:
- 월별 청구 내역
- 세금계산서 정보
- 결제 상태

---

## 3. Frontend 구조

### 3.1 Pages (페이지 컴포넌트)

#### Super Admin Pages
- **ApprovalsSuper.tsx**: 구독 승인 관리 페이지
- **TenantsSuper.tsx**: 테넌트 관리 페이지
- **AutoApprovalSuper.tsx**: 자동 승인 규칙 관리 페이지

#### HQ Admin Pages (계획)
- **DashboardHQ.tsx**: 본사 관리자 대시보드
- **SubscriptionHQ.tsx**: 구독 관리 페이지
- **UsersHQ.tsx**: 사용자 관리 페이지

#### Site Manager Pages (계획)
- **DashboardSite.tsx**: 현장 관리자 대시보드
- **AttendanceSite.tsx**: 출석 관리 페이지
- **TeamsSite.tsx**: 팀 관리 페이지

#### Worker Pages (계획)
- **DashboardWorker.tsx**: 노무자 대시보드
- **AttendanceWorker.tsx**: 출석 체크 페이지
- **ProfileWorker.tsx**: 프로필 관리 페이지

### 3.2 Components (컴포넌트)

#### Super Admin Components
- **SubscriptionDetailModal.tsx**: 구독 상세 정보 모달
- **AutoApprovalRuleForm.tsx**: 자동 승인 규칙 폼
- **AutoApprovalRuleList.tsx**: 자동 승인 규칙 목록
- **AutoApprovalStats.tsx**: 자동 승인 통계

#### Subscription Components
- **SubscriptionGuard.tsx**: 구독 상태 기반 접근 제어
- **SubscriptionStatusDisplay.tsx**: 구독 상태 표시
- **SubscriptionStatusIcon.tsx**: 구독 상태 아이콘

#### Notification Components
- **NotificationDropdown.tsx**: 알림 드롭다운
- **NotificationList.tsx**: 알림 목록
- **NotificationItem.tsx**: 개별 알림 아이템

### 3.3 Hooks (커스텀 훅)

#### API Hooks
- **useAdminApi.ts**: 관리자 API 호출 훅
- **useAutoApprovalRules.ts**: 자동 승인 규칙 관리 훅
- **useNotifications.ts**: 알림 관리 훅

#### Utility Hooks
- **useSubscriptionAccessControl.ts**: 구독 접근 제어 훅
- **useMediaQuery.ts**: 반응형 디자인 훅
- **useResponsive.ts**: 화면 크기 감지 훅

### 3.4 Stores (상태 관리)

#### notificationStore.ts
**목적**: 알림 상태 관리
**주요 기능**:
- 알림 목록 관리
- 읽음/안읽음 상태
- 실시간 알림 수신

---

## 4. 향후 구현 예정 클래스

### 4.1 Backend 추가 구현 예정

#### Face Recognition Domain
```java
// domain/face/service/FaceRecognitionService.java
public interface FaceRecognitionService {
    String generateEmbedding(MultipartFile faceImage);
    boolean matchFace(String embedding1, String embedding2);
    List<User> findSimilarFaces(String embedding, double threshold);
}
```

#### Weather Integration Domain
```java
// domain/weather/service/WeatherService.java
public interface WeatherService {
    WeatherInfo getCurrentWeather(String location);
    List<WeatherForecast> getWeatherForecast(String location, int days);
}
```

#### Report Domain
```java
// domain/report/entity/DailyReport.java
@Entity
public class DailyReport extends BaseTenantEntity {
    private Site site;
    private LocalDate reportDate;
    private String weatherCondition;
    private Integer totalWorkers;
    private Integer presentWorkers;
    private String workDescription;
    private String issues;
}
```

#### Contract Domain
```java
// domain/contract/entity/Contract.java
@Entity
public class Contract extends BaseTenantEntity {
    private User worker;
    private Site site;
    private ContractType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal dailyWage;
    private String contractTerms;
    private ContractStatus status;
}
```

### 4.2 Frontend 추가 구현 예정

#### Site Management Components
- **SiteList.tsx**: 현장 목록
- **SiteForm.tsx**: 현장 등록/수정 폼
- **SiteDetail.tsx**: 현장 상세 정보

#### Contract Management Components
- **ContractList.tsx**: 계약 목록
- **ContractForm.tsx**: 계약 등록/수정 폼
- **ContractSigning.tsx**: 전자 계약 서명

#### Report Components
- **DailyReportForm.tsx**: 일일 보고서 작성
- **ReportList.tsx**: 보고서 목록
- **ReportAnalytics.tsx**: 보고서 분석

#### Face Recognition Components
- **FaceCapture.tsx**: 얼굴 촬영 컴포넌트
- **FaceVerification.tsx**: 얼굴 인증 컴포넌트

---

## 5. 데이터베이스 스키마

### 5.1 현재 구현된 테이블

#### 사용자 관련
- `users`: 사용자 정보
- `user_roles`: 사용자 역할 (계획)

#### 테넌트 관련
- `tenants`: 테넌트 정보 (계획)
- `tenant_settings`: 테넌트 설정 (계획)

#### 구독 관련
- `subscription_plans`: 구독 요금제
- `subscriptions`: 구독 정보
- `subscription_approvals`: 구독 승인 이력
- `auto_approval_rules`: 자동 승인 규칙
- `notifications`: 알림 정보
- `payments`: 결제 정보
- `payment_methods`: 결제 수단

### 5.2 향후 구현 예정 테이블

#### 현장 관리
- `sites`: 현장 정보
- `site_workers`: 현장 배정 노무자
- `teams`: 팀 정보
- `team_members`: 팀 구성원

#### 출석 관리
- `attendance_logs`: 출석 기록
- `attendance_rules`: 출석 규칙
- `break_logs`: 휴게 기록

#### 계약 관리
- `contracts`: 계약 정보
- `contract_templates`: 계약 템플릿
- `contract_signatures`: 전자 서명

#### 보고서
- `daily_reports`: 일일 보고서
- `monthly_reports`: 월간 보고서
- `report_attachments`: 보고서 첨부파일

---

## 6. API 명세

### 6.1 구독 승인 관리 API

```
GET    /api/v1/admin/subscription-approvals/pending     # 승인 대기 목록
POST   /api/v1/admin/subscription-approvals/{id}/approve # 구독 승인
POST   /api/v1/admin/subscription-approvals/{id}/reject  # 구독 거부
POST   /api/v1/admin/subscription-approvals/{id}/suspend # 구독 중지
POST   /api/v1/admin/subscription-approvals/{id}/terminate # 구독 종료
GET    /api/v1/admin/subscription-approvals/{id}/history # 승인 이력
```

### 6.2 자동 승인 규칙 API (계획)

```
GET    /api/v1/admin/auto-approval-rules        # 규칙 목록
POST   /api/v1/admin/auto-approval-rules        # 규칙 생성
PUT    /api/v1/admin/auto-approval-rules/{id}   # 규칙 수정
DELETE /api/v1/admin/auto-approval-rules/{id}   # 규칙 삭제
```

### 6.3 테넌트 관리 API (계획)

```
GET    /api/v1/admin/tenants                    # 테넌트 목록
GET    /api/v1/admin/tenants/{id}               # 테넌트 상세
PUT    /api/v1/admin/tenants/{id}/status        # 상태 변경
```

---

## 7. 테스트 전략

### 7.1 Backend 테스트

#### Unit Tests
- 각 서비스 클래스별 단위 테스트
- Repository 계층 테스트
- Entity 검증 테스트

#### Property-Based Tests (jqwik)
- 구독 상태 전환 검증
- 자동 승인 규칙 검증
- 데이터 무결성 검증

#### Integration Tests
- API 엔드포인트 테스트
- 데이터베이스 연동 테스트
- 멀티테넌트 격리 테스트

### 7.2 Frontend 테스트

#### Component Tests (Vitest + React Testing Library)
- 각 컴포넌트별 렌더링 테스트
- 사용자 상호작용 테스트
- 상태 변경 테스트

#### E2E Tests (Playwright)
- 구독 승인 워크플로우 테스트
- 사용자 시나리오 테스트
- 브라우저 호환성 테스트

---

## 8. 보안 고려사항

### 8.1 인증 및 인가
- JWT 기반 인증
- 역할 기반 접근 제어 (RBAC)
- API 레벨 권한 검증

### 8.2 데이터 보호
- 멀티테넌트 데이터 격리
- 개인정보 암호화
- 안면인식 데이터 보안

### 8.3 API 보안
- HTTPS 강제
- CORS 설정
- Rate Limiting
- API 감사 로그

---

## 9. 성능 최적화

### 9.1 데이터베이스 최적화
- 인덱스 최적화
- 쿼리 성능 튜닝
- 커넥션 풀 관리

### 9.2 캐싱 전략
- Redis 기반 캐싱
- 쿼리 결과 캐싱
- 세션 캐싱

### 9.3 프론트엔드 최적화
- 코드 스플리팅
- 이미지 최적화
- 번들 크기 최적화

---

## 10. 배포 및 운영

### 10.1 배포 전략
- Docker 컨테이너화
- CI/CD 파이프라인
- 무중단 배포

### 10.2 모니터링
- 애플리케이션 성능 모니터링
- 에러 추적
- 사용자 행동 분석

### 10.3 백업 및 복구
- 데이터베이스 백업
- 파일 시스템 백업
- 재해 복구 계획

---

이 명세서는 SmartCON Lite 프로젝트의 현재 상태와 향후 계획을 포괄적으로 정리한 것입니다. 프로젝트 진행에 따라 지속적으로 업데이트될 예정입니다.