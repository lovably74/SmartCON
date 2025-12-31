# SmartCON Lite 클래스 다이어그램

## 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph "Frontend (React + TypeScript)"
        FE_Pages[Pages - 페이지 컴포넌트]
        FE_Components[Components - UI 컴포넌트]
        FE_Hooks[Hooks - 커스텀 훅]
        FE_Stores[Stores - 상태 관리]
    end
    
    subgraph "Backend (Spring Boot + Java)"
        BE_Controllers[Controllers - REST API 컨트롤러]
        BE_Services[Services - 비즈니스 로직]
        BE_Repositories[Repositories - 데이터 접근 계층]
        BE_Entities[Entities - 도메인 엔티티]
    end
    
    subgraph "Database (MariaDB)"
        DB_Tables[Tables - 데이터베이스 테이블]
    end
    
    %% 프론트엔드 내부 의존성
    FE_Pages --> FE_Components
    FE_Components --> FE_Hooks
    FE_Stores --> FE_Components
    
    %% 프론트엔드-백엔드 통신
    FE_Hooks --> BE_Controllers
    
    %% 백엔드 내부 의존성
    BE_Controllers --> BE_Services
    BE_Services --> BE_Repositories
    BE_Repositories --> BE_Entities
    BE_Entities --> DB_Tables
```

### 아키텍처 설명
- **Frontend Layer**: React 기반의 사용자 인터페이스 계층
  - Pages: 라우팅 기반의 페이지 컴포넌트들
  - Components: 재사용 가능한 UI 컴포넌트들
  - Hooks: API 호출 및 상태 관리를 위한 커스텀 훅들
  - Stores: Zustand 기반의 전역 상태 관리

- **Backend Layer**: Spring Boot 기반의 서버 애플리케이션 계층
  - Controllers: HTTP 요청을 처리하는 REST API 엔드포인트
  - Services: 핵심 비즈니스 로직을 담당하는 서비스 계층
  - Repositories: JPA 기반의 데이터 접근 계층
  - Entities: 도메인 객체 및 데이터베이스 매핑 엔티티

- **Database Layer**: MariaDB 기반의 데이터 저장소

## 1. Global 패키지 클래스 다이어그램

### 전역 공통 클래스 및 설정
이 패키지는 시스템 전반에서 사용되는 공통 기능들을 제공합니다.

```mermaid
classDiagram
    %% 기본 엔티티 클래스들
    class BaseEntity {
        <<abstract>>
        -Long id "기본키 (자동 생성)"
        -LocalDateTime createdAt "생성 일시"
        -LocalDateTime updatedAt "수정 일시"
        +onCreate() "엔티티 생성 시 호출"
        +onUpdate() "엔티티 수정 시 호출"
        +equals(Object obj) boolean "동등성 비교"
        +hashCode() int "해시코드 생성"
    }
    
    class BaseTenantEntity {
        <<abstract>>
        -Long tenantId "테넌트 ID (멀티테넌트 지원)"
        +onCreate() "생성 시 테넌트 ID 자동 설정"
        +onUpdate() "수정 시 테넌트 검증"
        +equals(Object obj) boolean "테넌트 포함 동등성 비교"
        +hashCode() int "테넌트 포함 해시코드"
    }
    
    %% 테넌트 컨텍스트 관리
    class TenantContext {
        <<utility>>
        +getCurrentTenantId() Long "현재 요청의 테넌트 ID 조회"
        +setCurrentTenantId(Long tenantId) "테넌트 ID 설정 (ThreadLocal)"
        +clear() "컨텍스트 정리 (메모리 누수 방지)"
    }
    
    %% 보안 설정
    class SecurityConfig {
        +filterChain(HttpSecurity http) SecurityFilterChain "Spring Security 필터 체인 설정"
        +passwordEncoder() PasswordEncoder "비밀번호 암호화 설정 (BCrypt)"
        +authenticationManager() AuthenticationManager "인증 관리자 설정"
    }
    
    %% JWT 인증 필터
    class JwtAuthenticationFilter {
        +doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) "JWT 토큰 검증 및 인증 처리"
        -extractToken(HttpServletRequest request) String "요청 헤더에서 JWT 토큰 추출"
        -validateToken(String token) boolean "JWT 토큰 유효성 검증"
    }
    
    %% 구독 접근 제어 인터셉터
    class SubscriptionAccessInterceptor {
        +preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) boolean "구독 상태 기반 접근 제어"
        -checkSubscriptionAccess(Long tenantId) boolean "테넌트의 구독 상태 확인"
    }
    
    %% 멀티테넌트 설정
    class MultiTenantConfig {
        +hibernatePropertiesCustomizer() HibernatePropertiesCustomizer "Hibernate 멀티테넌트 설정"
        +tenantFilter() FilterRegistrationBean "테넌트 필터 등록"
    }
    
    %% 캐시 설정
    class CacheConfig {
        +cacheManager() CacheManager "Redis 기반 캐시 매니저 설정"
        +redisTemplate() RedisTemplate "Redis 템플릿 설정"
    }
    
    %% 비동기 처리 설정
    class AsyncConfig {
        +taskExecutor() TaskExecutor "비동기 작업 실행자 설정"
        +asyncUncaughtExceptionHandler() AsyncUncaughtExceptionHandler "비동기 예외 처리기"
    }
    
    %% 상속 관계
    BaseEntity <|-- BaseTenantEntity : "멀티테넌트 지원을 위한 확장"
    BaseTenantEntity --> TenantContext : "테넌트 컨텍스트 사용"
```

### 주요 기능 설명

#### BaseEntity
- 모든 엔티티의 기본 클래스로 공통 필드와 메서드 제공
- JPA Auditing을 통한 생성/수정 시간 자동 관리
- equals/hashCode 메서드를 통한 엔티티 동등성 보장

#### BaseTenantEntity
- 멀티테넌트 아키텍처를 위한 기본 엔티티
- 모든 데이터 조회 시 자동으로 테넌트 필터링 적용
- 데이터 격리를 통한 보안 강화

#### TenantContext
- ThreadLocal을 사용한 요청별 테넌트 컨텍스트 관리
- 각 HTTP 요청마다 독립적인 테넌트 정보 유지
- 메모리 누수 방지를 위한 컨텍스트 정리 기능

#### 보안 및 인증
- JWT 기반 무상태 인증 시스템
- 구독 상태에 따른 세밀한 접근 제어
- Spring Security와 통합된 보안 설정

## 2. User Domain 클래스 다이어그램

### 사용자 관리 도메인
사용자 인증, 권한 관리, 프로필 관리를 담당하는 핵심 도메인입니다.

```mermaid
classDiagram
    %% 사용자 엔티티
    class User {
        -String name "사용자 실명"
        -String email "이메일 주소 (로그인 ID)"
        -String phoneNumber "휴대폰 번호"
        -String socialId "소셜 로그인 고유 ID"
        -Provider provider "로그인 제공자 (카카오/네이버/로컬)"
        -String passwordHash "암호화된 비밀번호"
        -Boolean isActive "계정 활성화 상태"
        -Boolean isEmailVerified "이메일 인증 완료 여부"
        -String profileImageUrl "프로필 이미지 URL"
        -String faceEmbedding "얼굴 인식용 임베딩 벡터"
        -Integer loginFailureCount "로그인 실패 횟수"
        +isActive() boolean "계정 활성화 상태 확인"
        +isEmailVerified() boolean "이메일 인증 상태 확인"
        +incrementLoginFailureCount() "로그인 실패 횟수 증가"
        +resetLoginFailureCount() "로그인 실패 횟수 초기화"
        +isLocked() boolean "계정 잠금 상태 확인 (5회 실패 시)"
    }
    
    %% 로그인 제공자 열거형
    class Provider {
        <<enumeration>>
        LOCAL "일반 회원가입"
        KAKAO "카카오 소셜 로그인"
        NAVER "네이버 소셜 로그인"
    }
    
    %% 사용자 역할 열거형
    class Role {
        <<enumeration>>
        ROLE_SUPER "슈퍼 관리자 (시스템 전체 관리)"
        ROLE_HQ "본사 관리자 (회사 전체 관리)"
        ROLE_SITE "현장 관리자 (현장별 관리)"
        ROLE_TEAM "팀장 (팀 단위 관리)"
        ROLE_WORKER "작업자 (개인 정보만 접근)"
    }
    
    %% 사용자 데이터 접근 계층
    class UserRepository {
        <<interface>>
        +findByEmail(String email) Optional~User~ "이메일로 사용자 조회"
        +findBySocialId(String socialId) Optional~User~ "소셜 ID로 사용자 조회"
        +findByTenantId(Long tenantId) List~User~ "테넌트별 사용자 목록 조회"
        +countByTenantId(Long tenantId) long "테넌트별 사용자 수 조회"
    }
    
    %% 사용자 서비스 인터페이스
    class UserService {
        <<interface>>
        +createUser(CreateUserRequest request) UserDto "새 사용자 생성"
        +updateUser(Long userId, UpdateUserRequest request) UserDto "사용자 정보 수정"
        +deleteUser(Long userId) "사용자 삭제 (소프트 삭제)"
        +getUserById(Long userId) UserDto "사용자 ID로 조회"
        +getUsersByTenant(Long tenantId) List~UserDto~ "테넌트별 사용자 목록 조회"
    }
    
    %% 사용자 서비스 구현체
    class UserServiceImpl {
        -UserRepository userRepository "사용자 데이터 접근 객체"
        +createUser(CreateUserRequest request) UserDto "사용자 생성 로직 구현"
        +updateUser(Long userId, UpdateUserRequest request) UserDto "사용자 수정 로직 구현"
        +deleteUser(Long userId) "사용자 삭제 로직 구현"
        +getUserById(Long userId) UserDto "사용자 조회 로직 구현"
        +getUsersByTenant(Long tenantId) List~UserDto~ "테넌트별 조회 로직 구현"
    }
    
    %% 사용자 REST API 컨트롤러
    class UserController {
        -UserService userService "사용자 서비스 의존성"
        +getUsers(Pageable pageable) ResponseEntity "사용자 목록 조회 API"
        +getUser(Long userId) ResponseEntity "특정 사용자 조회 API"
        +createUser(CreateUserRequest request) ResponseEntity "사용자 생성 API"
        +updateUser(Long userId, UpdateUserRequest request) ResponseEntity "사용자 수정 API"
        +deleteUser(Long userId) ResponseEntity "사용자 삭제 API"
    }
    
    %% 관계 설정
    BaseTenantEntity <|-- User : "멀티테넌트 지원"
    User --> Provider : "로그인 제공자 정보"
    User --> Role : "사용자 권한 정보"
    UserService <|.. UserServiceImpl : "서비스 인터페이스 구현"
    UserServiceImpl --> UserRepository : "데이터 접근 계층 사용"
    UserController --> UserService : "비즈니스 로직 호출"
```

### 주요 기능 설명

#### User 엔티티
- **멀티 로그인 지원**: 일반 회원가입, 카카오, 네이버 소셜 로그인
- **얼굴 인식 연동**: FaceNet 임베딩 벡터 저장으로 출입 관리 지원
- **보안 강화**: 로그인 실패 횟수 추적 및 계정 잠금 기능
- **이메일 인증**: 계정 활성화를 위한 이메일 인증 프로세스

#### 역할 기반 접근 제어 (RBAC)
- **5단계 권한 체계**: 슈퍼 관리자부터 작업자까지 세분화된 권한
- **계층적 권한 구조**: 상위 권한이 하위 권한의 기능 포함
- **테넌트별 격리**: 각 회사별로 독립적인 사용자 관리

#### 서비스 계층 패턴
- **인터페이스 분리**: 비즈니스 로직과 구현체 분리로 테스트 용이성 향상
- **트랜잭션 관리**: @Transactional을 통한 데이터 일관성 보장
- **예외 처리**: 도메인별 커스텀 예외를 통한 명확한 오류 처리

## 3. Subscription Domain 클래스 다이어그램

### 구독 관리 및 승인 워크플로우 도메인
SaaS 서비스의 핵심인 구독 관리, 승인 프로세스, 자동 승인 규칙을 담당합니다.

```mermaid
classDiagram
    %% 구독 엔티티
    class Subscription {
        -Tenant tenant "구독하는 테넌트 (회사)"
        -SubscriptionPlan plan "구독 플랜 정보"
        -SubscriptionStatus status "구독 상태"
        -LocalDate startDate "구독 시작일"
        -LocalDate endDate "구독 종료일"
        -LocalDate nextBillingDate "다음 결제일"
        -BillingCycle billingCycle "결제 주기 (월/년)"
        -BigDecimal monthlyPrice "월 결제 금액"
        -BigDecimal discountRate "할인율 (0.0~1.0)"
        -Boolean autoRenewal "자동 갱신 여부"
        -LocalDateTime approvalRequestedAt "승인 요청 시간"
        -LocalDateTime approvedAt "승인 완료 시간"
        -User approvedBy "승인 처리자"
        -String rejectionReason "거부 사유"
        -Long version "낙관적 락을 위한 버전"
        +updateStatus(SubscriptionStatus status) "구독 상태 변경"
        +approve(User approver) "구독 승인 처리"
        +reject(String reason) "구독 거부 처리"
        +suspend(String reason) "구독 일시 중지"
        +terminate(String reason) "구독 종료 처리"
        +isActive() boolean "활성 구독 여부 확인"
        +isPendingApproval() boolean "승인 대기 상태 확인"
    }
    
    %% 구독 상태 열거형
    class SubscriptionStatus {
        <<enumeration>>
        PENDING_APPROVAL "승인 대기 (신규 신청)"
        ACTIVE "활성 구독 (정상 서비스 이용 가능)"
        AUTO_APPROVED "자동 승인 (규칙에 의한 즉시 승인)"
        REJECTED "승인 거부 (서비스 이용 불가)"
        SUSPENDED "일시 중지 (결제 실패, 정책 위반 등)"
        TERMINATED "종료 (계약 만료, 해지)"
        CANCELLED "취소 (사용자 요청에 의한 취소)"
    }
    
    %% 구독 승인 이력
    class SubscriptionApproval {
        -Long subscriptionId "대상 구독 ID"
        -User admin "처리한 관리자"
        -SubscriptionStatus fromStatus "변경 전 상태"
        -SubscriptionStatus toStatus "변경 후 상태"
        -String reason "처리 사유"
        -ApprovalAction action "수행된 액션"
        -LocalDateTime processedAt "처리 완료 시간"
        -boolean autoApproved "자동 승인 여부"
    }
    
    %% 승인 액션 열거형
    class ApprovalAction {
        <<enumeration>>
        APPROVE "승인 (서비스 활성화)"
        REJECT "거부 (서비스 차단)"
        SUSPEND "일시 중지 (임시 차단)"
        TERMINATE "종료 (영구 차단)"
        REACTIVATE "재활성화 (서비스 복구)"
    }
    
    %% 자동 승인 규칙
    class AutoApprovalRule {
        -String ruleName "규칙 이름"
        -Boolean isActive "규칙 활성화 상태"
        -String planIds "적용 대상 플랜 ID 목록 (JSON)"
        -Boolean verifiedTenantsOnly "인증된 테넌트만 대상"
        -String paymentMethods "허용 결제 수단 (JSON)"
        -BigDecimal maxAmount "자동 승인 최대 금액"
        -Integer priority "규칙 우선순위 (낮을수록 우선)"
        +updateActiveStatus(boolean isActive) "규칙 활성화 상태 변경"
        +updatePriority(int priority) "우선순위 변경"
        +updateRule(String ruleName, String planIds, Boolean verifiedTenantsOnly, String paymentMethods, BigDecimal maxAmount) "규칙 내용 수정"
    }
    
    %% 구독 플랜
    class SubscriptionPlan {
        -String planId "플랜 고유 ID"
        -String planName "플랜 이름"
        -String description "플랜 설명"
        -BigDecimal monthlyPrice "월 요금"
        -BigDecimal yearlyPrice "연 요금"
        -Integer maxUsers "최대 사용자 수"
        -Integer maxSites "최대 현장 수"
        -Boolean isActive "플랜 활성화 상태"
    }
    
    %% 구독 승인 서비스 인터페이스
    class SubscriptionApprovalService {
        <<interface>>
        +approveSubscription(Long subscriptionId, String reason) SubscriptionDto "구독 승인 처리"
        +rejectSubscription(Long subscriptionId, String reason) SubscriptionDto "구독 거부 처리"
        +suspendSubscription(Long subscriptionId, String reason) SubscriptionDto "구독 일시 중지"
        +terminateSubscription(Long subscriptionId, String reason) SubscriptionDto "구독 종료 처리"
        +reactivateSubscription(Long subscriptionId, String reason) SubscriptionDto "구독 재활성화"
        +getPendingApprovals(Pageable pageable) Page~SubscriptionDto~ "승인 대기 목록 조회"
        +getApprovalHistory(Long subscriptionId) List~SubscriptionApprovalDto~ "승인 이력 조회"
        +checkAutoApproval(CreateSubscriptionRequest request) boolean "자동 승인 가능 여부 확인"
    }
    
    %% 구독 승인 서비스 구현체
    class SubscriptionApprovalServiceImpl {
        -SubscriptionRepository subscriptionRepository "구독 데이터 접근"
        -SubscriptionApprovalRepository approvalRepository "승인 이력 데이터 접근"
        -AutoApprovalRuleRepository ruleRepository "자동 승인 규칙 데이터 접근"
        -NotificationService notificationService "알림 서비스"
        +approveSubscription(Long subscriptionId, String reason) SubscriptionDto "승인 로직 구현"
        +rejectSubscription(Long subscriptionId, String reason) SubscriptionDto "거부 로직 구현"
        +suspendSubscription(Long subscriptionId, String reason) SubscriptionDto "중지 로직 구현"
        +terminateSubscription(Long subscriptionId, String reason) SubscriptionDto "종료 로직 구현"
        +reactivateSubscription(Long subscriptionId, String reason) SubscriptionDto "재활성화 로직 구현"
    }
    
    %% 구독 승인 REST API 컨트롤러
    class SubscriptionApprovalController {
        -SubscriptionApprovalService subscriptionApprovalService "승인 서비스 의존성"
        +getPendingApprovals(Pageable pageable) ResponseEntity "승인 대기 목록 API"
        +approveSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity "구독 승인 API"
        +rejectSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity "구독 거부 API"
        +suspendSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity "구독 중지 API"
        +terminateSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity "구독 종료 API"
        +getApprovalHistory(Long subscriptionId) ResponseEntity "승인 이력 조회 API"
    }
    
    %% 관계 설정
    BaseEntity <|-- Subscription : "기본 엔티티 상속"
    BaseEntity <|-- SubscriptionApproval : "기본 엔티티 상속"
    BaseEntity <|-- AutoApprovalRule : "기본 엔티티 상속"
    BaseEntity <|-- SubscriptionPlan : "기본 엔티티 상속"
    
    Subscription --> SubscriptionStatus : "구독 상태 정보"
    SubscriptionApproval --> ApprovalAction : "승인 액션 정보"
    SubscriptionApproval --> SubscriptionStatus : "상태 변경 정보"
    
    SubscriptionApprovalService <|.. SubscriptionApprovalServiceImpl : "서비스 인터페이스 구현"
    SubscriptionApprovalController --> SubscriptionApprovalService : "비즈니스 로직 호출"
```

### 주요 기능 설명

#### 구독 생명주기 관리
- **신청 → 승인 → 활성화**: 체계적인 구독 승인 워크플로우
- **상태 추적**: 각 구독의 상태 변화를 상세히 기록
- **버전 관리**: 낙관적 락을 통한 동시성 제어

#### 자동 승인 시스템
- **규칙 기반 승인**: 미리 정의된 규칙에 따른 자동 승인
- **우선순위 처리**: 여러 규칙 중 우선순위에 따른 적용
- **유연한 조건**: 플랜, 금액, 결제 수단, 테넌트 인증 상태 등 다양한 조건 지원

#### 승인 이력 관리
- **완전한 추적성**: 모든 승인 관련 액션의 상세 기록
- **책임 추적**: 누가, 언제, 왜 승인/거부했는지 명확한 기록
- **감사 지원**: 규제 준수 및 내부 감사를 위한 완전한 로그

#### 알림 연동
- **실시간 알림**: 구독 상태 변경 시 관련자에게 즉시 알림
- **다채널 지원**: 이메일, SMS, 푸시 알림 등 다양한 채널 지원

## 4. Admin Domain 클래스 다이어그램

### 슈퍼 관리자 도메인
시스템 전체를 관리하는 슈퍼 관리자의 대시보드, 통계, 모니터링 기능을 제공합니다.

```mermaid
classDiagram
    %% 슈퍼 관리자 서비스
    class SuperAdminService {
        -TenantRepository tenantRepository "테넌트 데이터 접근"
        -UserRepository userRepository "사용자 데이터 접근"
        -BillingRecordRepository billingRecordRepository "결제 기록 데이터 접근"
        -SubscriptionRepository subscriptionRepository "구독 데이터 접근"
        -SubscriptionApprovalRepository approvalRepository "승인 이력 데이터 접근"
        +getDashboardStats() DashboardStatsDto "대시보드 통계 데이터 조회"
        +getTenants(String search, SubscriptionStatus status, Pageable pageable) Page~TenantSummaryDto~ "테넌트 목록 조회 (검색/필터링)"
        +updateTenantStatus(Long tenantId, SubscriptionStatus status) "테넌트 구독 상태 변경"
        +getBillingStats() BillingStatsDto "결제 관련 통계 조회"
        +getApprovalStats() ApprovalStatsDto "승인 관련 통계 조회"
        +exportSubscriptionData(SubscriptionStatus status, LocalDateTime startDate, LocalDateTime endDate) List~SubscriptionExportDto~ "구독 데이터 내보내기"
    }
    
    %% 슈퍼 관리자 REST API 컨트롤러
    class SuperAdminController {
        -SuperAdminService superAdminService "슈퍼 관리자 서비스 의존성"
        +getDashboardStats() ResponseEntity "대시보드 통계 API"
        +getTenants(String search, String status, Pageable pageable) ResponseEntity "테넌트 목록 조회 API"
        +updateTenantStatus(Long tenantId, UpdateTenantStatusRequest request) ResponseEntity "테넌트 상태 변경 API"
        +getBillingStats() ResponseEntity "결제 통계 API"
        +getApprovalStats() ResponseEntity "승인 통계 API"
        +exportSubscriptions(String status, String startDate, String endDate) ResponseEntity "구독 데이터 내보내기 API"
    }
    
    %% 대시보드 통계 DTO
    class DashboardStatsDto {
        -Long totalTenants "전체 테넌트 수"
        -Long activeTenants "활성 테넌트 수"
        -Long suspendedTenants "중지된 테넌트 수"
        -Long newTenantsThisMonth "이번 달 신규 테넌트 수"
        -Long totalUsers "전체 사용자 수"
        -Long newUsersThisMonth "이번 달 신규 사용자 수"
        -BigDecimal totalRevenue "총 매출액"
        -BigDecimal monthlyRevenue "월 매출액"
        -Long completedPayments "완료된 결제 건수"
        -Long failedPayments "실패한 결제 건수"
        -Long pendingPayments "대기 중인 결제 건수"
        -String systemStatus "시스템 상태 (정상/점검/장애)"
        -Long activeConnections "현재 활성 연결 수"
    }
    
    %% 테넌트 요약 DTO
    class TenantSummaryDto {
        -Long id "테넌트 ID"
        -String companyName "회사명"
        -String businessNo "사업자등록번호"
        -String representativeName "대표자명"
        -Long userCount "사용자 수"
        -String subscriptionStatus "구독 상태"
        -String planId "구독 플랜 ID"
        -BigDecimal monthlyAmount "월 결제 금액"
        -LocalDateTime createdAt "가입일"
        -LocalDateTime approvedAt "승인일"
        -String approvedBy "승인자"
        +from(Tenant tenant, long userCount) TenantSummaryDto "엔티티에서 DTO 변환"
    }
    
    %% 결제 통계 DTO
    class BillingStatsDto {
        -BigDecimal totalRevenue "총 매출액"
        -BigDecimal monthlyRevenue "월 매출액"
        -BigDecimal dailyRevenue "일 매출액"
        -Long totalPayments "총 결제 건수"
        -Long completedPayments "완료된 결제 건수"
        -Long failedPayments "실패한 결제 건수"
        -Long pendingPayments "대기 중인 결제 건수"
        -List~MonthlyRevenueDto~ monthlyTrends "월별 매출 추이"
        -List~FailedPaymentDto~ recentFailedPayments "최근 결제 실패 목록"
    }
    
    %% 승인 통계 DTO
    class ApprovalStatsDto {
        -Long totalSubscriptions "전체 구독 수"
        -Long pendingApprovals "승인 대기 중인 구독 수"
        -Long approvedSubscriptions "승인된 구독 수"
        -Long rejectedSubscriptions "거부된 구독 수"
        -Long suspendedSubscriptions "중지된 구독 수"
        -Long terminatedSubscriptions "종료된 구독 수"
        -Long autoApprovedCount "자동 승인된 구독 수"
        -Long manualApprovalCount "수동 승인된 구독 수"
        -Double autoApprovalRate "자동 승인율 (0.0~1.0)"
        -Double averageProcessingHours "평균 승인 처리 시간 (시간)"
        -Long pendingOverThreeDays "3일 이상 대기 중인 승인 건수"
    }
    
    %% 관계 설정
    SuperAdminController --> SuperAdminService : "비즈니스 로직 호출"
    SuperAdminService --> DashboardStatsDto : "대시보드 통계 생성"
    SuperAdminService --> TenantSummaryDto : "테넌트 요약 정보 생성"
    SuperAdminService --> BillingStatsDto : "결제 통계 생성"
    SuperAdminService --> ApprovalStatsDto : "승인 통계 생성"
```

### 주요 기능 설명

#### 대시보드 통계 (DashboardStatsDto)
- **실시간 모니터링**: 시스템 전체의 핵심 지표를 실시간으로 제공
- **성장 지표**: 신규 테넌트 및 사용자 증가 추이 분석
- **수익 관리**: 총 매출, 월 매출 등 재무 지표 추적
- **시스템 상태**: 서버 상태 및 활성 연결 수 모니터링

#### 테넌트 관리 (TenantSummaryDto)
- **통합 뷰**: 모든 테넌트의 핵심 정보를 한눈에 조회
- **검색 및 필터링**: 회사명, 구독 상태 등으로 효율적인 검색
- **상태 관리**: 테넌트별 구독 상태 실시간 변경 가능
- **사용자 추적**: 테넌트별 사용자 수 및 활동 현황 파악

#### 결제 통계 (BillingStatsDto)
- **매출 분석**: 일/월/총 매출 현황 및 추이 분석
- **결제 현황**: 성공/실패/대기 중인 결제 건수 추적
- **트렌드 분석**: 월별 매출 추이를 통한 성장 패턴 파악
- **실패 분석**: 결제 실패 원인 분석 및 대응 방안 수립

#### 승인 통계 (ApprovalStatsDto)
- **승인 효율성**: 자동 승인율 및 평균 처리 시간 측정
- **워크플로우 최적화**: 승인 프로세스의 병목 지점 식별
- **SLA 관리**: 3일 이상 대기 건수를 통한 서비스 품질 관리
- **의사결정 지원**: 승인 패턴 분석을 통한 정책 개선 근거 제공

#### 데이터 내보내기
- **규제 준수**: 감사 및 규제 요구사항에 따른 데이터 추출
- **분석 지원**: 외부 분석 도구와의 연동을 위한 데이터 제공
- **백업 및 아카이브**: 중요 데이터의 정기적 백업 지원

## 5. Frontend 컴포넌트 클래스 다이어그램

```mermaid
classDiagram
    class ApprovalsSuper {
        -page: number
        -selectedApproval: any
        -approvalReason: string
        -rejectionReason: string
        -showApprovalDialog: boolean
        -showRejectionDialog: boolean
        -showHistoryDialog: boolean
        +handleApprove()
        +handleReject()
        +getStatusText(status: string) string
        +formatCurrency(amount: number) string
        +formatDate(dateString: string) string
    }
    
    class SubscriptionDetailModal {
        -actionType: string
        -actionReason: string
        -showActionDialog: boolean
        +formatDate(dateString: string) string
        +formatCurrency(amount: number) string
        +getSubscriptionStatusText(status: string) string
        +handleAction()
        +openActionDialog(type: string)
        +getAvailableActions() string[]
    }
    
    class AutoApprovalSuper {
        -selectedRule: AutoApprovalRule
        -showRuleDialog: boolean
        -ruleForm: AutoApprovalRuleForm
        +handleCreateRule()
        +handleUpdateRule()
        +handleDeleteRule()
        +handleToggleRule()
    }
    
    class SubscriptionGuard {
        -subscriptionStatus: string
        -allowedStatuses: string[]
        +checkAccess() boolean
        +renderAccessDenied() JSX.Element
    }
    
    class SubscriptionStatusDisplay {
        -status: string
        -showDetails: boolean
        +getStatusColor(status: string) string
        +getStatusIcon(status: string) JSX.Element
        +getStatusMessage(status: string) string
    }
    
    class NotificationDropdown {
        -isOpen: boolean
        -notifications: Notification[]
        -unreadCount: number
        +toggleDropdown()
        +markAsRead(notificationId: number)
        +markAllAsRead()
    }
    
    class useAdminApi {
        +usePendingApprovals(params: PaginationParams) QueryResult
        +useApproveSubscription() MutationResult
        +useRejectSubscription() MutationResult
        +useSuspendSubscription() MutationResult
        +useTerminateSubscription() MutationResult
        +useApprovalHistory(subscriptionId: number) QueryResult
    }
    
    class useSubscriptionAccessControl {
        -accessState: SubscriptionAccessState
        +clearAccessBlock()
        +handleSubscriptionError(error: SubscriptionAccessError)
    }
    
    class useAutoApprovalRules {
        +useAutoApprovalRules() QueryResult
        +useCreateAutoApprovalRule() MutationResult
        +useUpdateAutoApprovalRule() MutationResult
        +useDeleteAutoApprovalRule() MutationResult
        +useToggleAutoApprovalRule() MutationResult
    }
    
    class notificationStore {
        -notifications: Notification[]
        -unreadCount: number
        -isPolling: boolean
        +addNotification(notification: Notification)
        +markAsRead(notificationId: number)
        +markAllAsRead()
        +startPolling()
        +stopPolling()
    }
    
    ApprovalsSuper --> useAdminApi : uses
    ApprovalsSuper --> SubscriptionDetailModal : renders
    SubscriptionDetailModal --> useAdminApi : uses
    AutoApprovalSuper --> useAutoApprovalRules : uses
    SubscriptionGuard --> useSubscriptionAccessControl : uses
    NotificationDropdown --> notificationStore : uses
```

## 6. 데이터베이스 ERD

```mermaid
erDiagram
    TENANTS {
        bigint id PK
        varchar company_name
        varchar business_no
        varchar representative_name
        varchar address
        varchar phone_number
        varchar email
        enum status
        boolean is_verified
        timestamp created_at
        timestamp updated_at
    }
    
    USERS {
        bigint id PK
        bigint tenant_id FK
        varchar name
        varchar email
        varchar phone_number
        varchar provider_id
        enum provider
        varchar password_hash
        boolean is_active
        boolean is_email_verified
        varchar profile_image_url
        text face_embedding
        int login_failure_count
        timestamp created_at
        timestamp updated_at
    }
    
    SUBSCRIPTION_PLANS {
        varchar plan_id PK
        varchar plan_name
        text description
        decimal monthly_price
        decimal yearly_price
        int max_users
        int max_sites
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    SUBSCRIPTIONS {
        bigint id PK
        bigint tenant_id FK
        varchar plan_id FK
        enum status
        date start_date
        date end_date
        date next_billing_date
        enum billing_cycle
        decimal monthly_price
        decimal discount_rate
        boolean auto_renewal
        date trial_end_date
        timestamp approval_requested_at
        timestamp approved_at
        bigint approved_by FK
        text rejection_reason
        text suspension_reason
        text termination_reason
        bigint version
        timestamp created_at
        timestamp updated_at
    }
    
    SUBSCRIPTION_APPROVALS {
        bigint id PK
        bigint subscription_id FK
        bigint admin_id FK
        enum from_status
        enum to_status
        text reason
        enum action
        timestamp processed_at
        boolean auto_approved
        timestamp created_at
        timestamp updated_at
    }
    
    AUTO_APPROVAL_RULES {
        bigint id PK
        varchar rule_name
        boolean is_active
        json plan_ids
        boolean verified_tenants_only
        json payment_methods
        decimal max_amount
        int priority
        timestamp created_at
        timestamp updated_at
    }
    
    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        bigint tenant_id FK
        enum type
        varchar title
        text message
        json data
        boolean is_read
        timestamp read_at
        timestamp created_at
        timestamp updated_at
    }
    
    PAYMENTS {
        bigint id PK
        bigint subscription_id FK
        bigint tenant_id FK
        decimal amount
        enum payment_type
        enum status
        varchar payment_method_id
        varchar transaction_id
        text failure_reason
        timestamp payment_date
        timestamp created_at
        timestamp updated_at
    }
    
    PAYMENT_METHODS {
        varchar id PK
        bigint tenant_id FK
        enum type
        varchar provider
        varchar account_info
        boolean is_default
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    ATTENDANCE_LOGS {
        bigint id PK
        bigint user_id FK
        bigint tenant_id FK
        bigint site_id FK
        timestamp check_in_time
        timestamp check_out_time
        enum type
        varchar face_match_result
        varchar gps_location
        text notes
        timestamp created_at
        timestamp updated_at
    }
    
    SITES {
        bigint id PK
        bigint tenant_id FK
        varchar site_name
        varchar address
        varchar gps_coordinates
        varchar manager_name
        varchar manager_phone
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    TENANTS ||--o{ USERS : "has"
    TENANTS ||--o{ SUBSCRIPTIONS : "has"
    TENANTS ||--o{ NOTIFICATIONS : "receives"
    TENANTS ||--o{ PAYMENTS : "makes"
    TENANTS ||--o{ PAYMENT_METHODS : "has"
    TENANTS ||--o{ ATTENDANCE_LOGS : "tracks"
    TENANTS ||--o{ SITES : "manages"
    
    SUBSCRIPTION_PLANS ||--o{ SUBSCRIPTIONS : "defines"
    
    SUBSCRIPTIONS ||--o{ SUBSCRIPTION_APPROVALS : "has"
    SUBSCRIPTIONS ||--o{ PAYMENTS : "generates"
    
    USERS ||--o{ SUBSCRIPTION_APPROVALS : "processes"
    USERS ||--o{ SUBSCRIPTIONS : "approves"
    USERS ||--o{ NOTIFICATIONS : "receives"
    USERS ||--o{ ATTENDANCE_LOGS : "creates"
    
    SITES ||--o{ ATTENDANCE_LOGS : "records"
```

## 7. 시퀀스 다이어그램 - 구독 승인 프로세스

```mermaid
sequenceDiagram
    participant U as User (Frontend)
    participant C as SubscriptionApprovalController
    participant S as SubscriptionApprovalService
    participant SR as SubscriptionRepository
    participant AR as SubscriptionApprovalRepository
    participant NS as NotificationService
    participant DB as Database
    
    U->>C: GET /api/v1/admin/subscription-approvals/pending
    C->>S: getPendingApprovals(pageable)
    S->>SR: findByStatusOrderByCreatedAtDesc(PENDING_APPROVAL, pageable)
    SR->>DB: SELECT * FROM subscriptions WHERE status = 'PENDING_APPROVAL'
    DB-->>SR: Subscription records
    SR-->>S: Page<Subscription>
    S-->>C: Page<SubscriptionDto>
    C-->>U: ResponseEntity<Page<SubscriptionDto>>
    
    U->>C: POST /api/v1/admin/subscription-approvals/{id}/approve
    C->>S: approveSubscription(subscriptionId, reason)
    S->>SR: findById(subscriptionId)
    SR->>DB: SELECT * FROM subscriptions WHERE id = ?
    DB-->>SR: Subscription
    SR-->>S: Subscription
    
    S->>S: subscription.approve(approver)
    S->>SR: save(subscription)
    SR->>DB: UPDATE subscriptions SET status = 'ACTIVE', approved_at = ?, approved_by = ?
    
    S->>AR: save(approvalHistory)
    AR->>DB: INSERT INTO subscription_approvals
    
    S->>NS: sendApprovalNotification(subscription)
    NS->>DB: INSERT INTO notifications
    
    S-->>C: SubscriptionDto
    C-->>U: ResponseEntity<SubscriptionDto>
```

## 8. 컴포넌트 상호작용 다이어그램

```mermaid
graph TD
    subgraph "Super Admin Pages"
        A[ApprovalsSuper]
        T[TenantsSuper]
        AA[AutoApprovalSuper]
    
    subgraph "Shared Components"
        SDM[SubscriptionDetailModal]
        SG[SubscriptionGuard]
        SSD[SubscriptionStatusDisplay]
        ND[NotificationDropdown]
    end
    
    subgraph "Custom Hooks"
        UAA[useAdminApi]
        USAC[useSubscriptionAccessControl]
        UAAR[useAutoApprovalRules]
        UN[useNotifications]
    end
    
    subgraph "Stores"
        NS[notificationStore]
    end
    
    subgraph "Backend APIs"
        SAC[SubscriptionApprovalController]
        SC[SuperAdminController]
        AAC[AutoApprovalController]
    end
    
    A --> SDM
    A --> UAA
    T --> SDM
    T --> UAA
    AA --> UAAR
    
    SDM --> UAA
    SG --> USAC
    ND --> UN
    ND --> NS
    
    UAA --> SAC
    UAA --> SC
    UAAR --> AAC
    UN --> SAC
    
    USAC --> SG
    NS --> ND
```

이 클래스 다이어그램들은 SmartCON Lite 시스템의 전체적인 구조와 각 컴포넌트 간의 관계를 시각적으로 보여줍니다. 각 다이어그램은 특정 도메인이나 계층에 초점을 맞춰 설계되었으며, 시스템의 복잡성을 관리 가능한 단위로 분해하여 표현했습니다.