# SmartCON Lite 클래스 다이어그램

## 문서 개요
이 문서는 SmartCON Lite 시스템의 전체 클래스 구조와 컴포넌트 간의 관계를 시각적으로 표현한 클래스 다이어그램입니다.
각 도메인별로 세분화하여 시스템의 복잡성을 관리 가능한 단위로 분해하여 설명합니다.

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
    
    %% 프론트엔드 내부 의존성 (컴포넌트 계층 구조)
    FE_Pages --> FE_Components
    FE_Components --> FE_Hooks
    FE_Stores --> FE_Components
    
    %% 프론트엔드-백엔드 통신 (HTTP API 호출)
    FE_Hooks --> BE_Controllers
    
    %% 백엔드 내부 의존성 (레이어드 아키텍처)
    BE_Controllers --> BE_Services
    BE_Services --> BE_Repositories
    BE_Repositories --> BE_Entities
    BE_Entities --> DB_Tables
```

### 아키텍처 설명

#### Frontend Layer (프론트엔드 계층)
React 18 + TypeScript 기반의 사용자 인터페이스 계층으로, 모바일 우선 반응형 웹 애플리케이션을 구성합니다.

- **Pages (페이지 컴포넌트)**: 
  - 역할별 라우팅 기반의 페이지 컴포넌트들 (/hq, /site, /worker, /super)
  - 각 사용자 역할에 맞는 전용 대시보드 및 기능 페이지 제공
  - Wouter 라이브러리를 사용한 경량 클라이언트 사이드 라우팅

- **Components (UI 컴포넌트)**: 
  - Shadcn/UI + Tailwind CSS 기반의 재사용 가능한 UI 컴포넌트들
  - 디자인 시스템을 준수하는 일관된 사용자 인터페이스 요소
  - 접근성(Accessibility) 표준을 준수하는 컴포넌트 설계

- **Hooks (커스텀 훅)**: 
  - TanStack Query를 활용한 서버 상태 관리 및 API 호출 로직
  - 비즈니스 로직과 UI 로직의 분리를 통한 재사용성 향상
  - 캐싱, 에러 처리, 로딩 상태 관리 등의 공통 기능 제공

- **Stores (상태 관리)**: 
  - Zustand 기반의 경량 전역 상태 관리
  - 클라이언트 사이드 상태 (UI 상태, 사용자 설정 등) 관리
  - 서버 상태는 TanStack Query로 별도 관리하여 관심사 분리

#### Backend Layer (백엔드 계층)
Spring Boot 3.3.x + Java 17 기반의 서버 애플리케이션 계층으로, 레이어드 아키텍처 패턴을 적용합니다.

- **Controllers (REST API 컨트롤러)**: 
  - HTTP 요청을 처리하는 REST API 엔드포인트
  - 요청 검증, 응답 포맷팅, 예외 처리 등의 웹 계층 책임
  - Spring Security를 통한 인증/인가 처리 및 역할 기반 접근 제어

- **Services (비즈니스 로직)**: 
  - 핵심 비즈니스 로직을 담당하는 서비스 계층
  - 트랜잭션 관리, 도메인 규칙 적용, 외부 시스템 연동
  - 인터페이스와 구현체 분리를 통한 테스트 용이성 및 확장성 확보

- **Repositories (데이터 접근 계층)**: 
  - Spring Data JPA 기반의 데이터 접근 계층
  - 데이터베이스 CRUD 연산 및 복잡한 쿼리 처리
  - 멀티테넌트 지원을 위한 자동 테넌트 필터링 적용

- **Entities (도메인 엔티티)**: 
  - JPA 엔티티로 구현된 도메인 객체
  - 비즈니스 규칙과 데이터 무결성 제약 조건 포함
  - 멀티테넌트 아키텍처를 위한 BaseTenantEntity 상속 구조

#### Database Layer (데이터베이스 계층)
MariaDB 10.11 기반의 관계형 데이터베이스로, 멀티테넌트 SaaS 아키텍처를 지원합니다.

- **Tables (데이터베이스 테이블)**: 
  - 테넌트별 데이터 격리를 위한 tenant_id 기반 파티셔닝
  - 감사 추적을 위한 생성/수정 시간 자동 관리
  - 성능 최적화를 위한 적절한 인덱스 설계

## 1. Global 패키지 클래스 다이어그램

### 전역 공통 클래스 및 설정
이 패키지는 시스템 전반에서 사용되는 공통 기능들을 제공하며, 멀티테넌트 SaaS 아키텍처의 핵심 인프라를 담당합니다.

```mermaid
classDiagram
    %% 기본 엔티티 클래스들 - 모든 도메인 엔티티의 기반
    class BaseEntity {
        <<abstract>>
        -Long id "기본키 (자동 생성, @GeneratedValue)"
        -LocalDateTime createdAt "생성 일시 (@CreationTimestamp)"
        -LocalDateTime updatedAt "수정 일시 (@UpdateTimestamp)"
        +onCreate() "엔티티 생성 시 호출 (@PrePersist)"
        +onUpdate() "엔티티 수정 시 호출 (@PreUpdate)"
        +equals(Object obj) boolean "ID 기반 동등성 비교"
        +hashCode() int "ID 기반 해시코드 생성"
        +toString() String "디버깅용 문자열 표현"
    }
    
    class BaseTenantEntity {
        <<abstract>>
        -Long tenantId "테넌트 ID (멀티테넌트 지원, @Column(nullable = false))"
        +onCreate() "생성 시 테넌트 ID 자동 설정 (TenantContext에서 조회)"
        +onUpdate() "수정 시 테넌트 검증 (보안 강화)"
        +equals(Object obj) boolean "ID + 테넌트 ID 기반 동등성 비교"
        +hashCode() int "ID + 테넌트 ID 기반 해시코드"
        +validateTenantAccess() "테넌트 접근 권한 검증"
    }
    
    %% 테넌트 컨텍스트 관리 - 요청별 테넌트 정보 관리
    class TenantContext {
        <<utility>>
        -ThreadLocal~Long~ currentTenantId "스레드별 테넌트 ID 저장소"
        +getCurrentTenantId() Long "현재 요청의 테넌트 ID 조회"
        +setCurrentTenantId(Long tenantId) "테넌트 ID 설정 (필터에서 호출)"
        +clear() "컨텍스트 정리 (메모리 누수 방지, 요청 완료 시)"
        +hasCurrentTenant() boolean "현재 테넌트 설정 여부 확인"
        +validateTenantId(Long tenantId) "테넌트 ID 유효성 검증"
    }
    
    %% 보안 설정 - Spring Security 통합 설정
    class SecurityConfig {
        -JwtAuthenticationFilter jwtAuthenticationFilter "JWT 인증 필터"
        -SubscriptionAccessInterceptor subscriptionInterceptor "구독 상태 검증 인터셉터"
        +filterChain(HttpSecurity http) SecurityFilterChain "Spring Security 필터 체인 설정"
        +passwordEncoder() PasswordEncoder "BCrypt 비밀번호 암호화 설정"
        +authenticationManager() AuthenticationManager "인증 관리자 설정"
        +corsConfigurationSource() CorsConfigurationSource "CORS 정책 설정"
        +sessionManagement() SessionCreationPolicy "무상태 세션 정책 설정"
    }
    
    %% JWT 인증 필터 - 토큰 기반 인증 처리
    class JwtAuthenticationFilter {
        -JwtTokenProvider jwtTokenProvider "JWT 토큰 생성/검증 서비스"
        -UserDetailsService userDetailsService "사용자 정보 로드 서비스"
        +doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) "JWT 토큰 검증 및 인증 처리"
        -extractToken(HttpServletRequest request) String "Authorization 헤더에서 Bearer 토큰 추출"
        -validateToken(String token) boolean "JWT 토큰 유효성 검증 (만료, 서명 등)"
        -setAuthentication(String token) "인증 정보를 SecurityContext에 설정"
        -handleAuthenticationException(HttpServletResponse response, Exception e) "인증 실패 시 에러 응답 처리"
    }
    
    %% 구독 접근 제어 인터셉터 - 구독 상태 기반 접근 제어
    class SubscriptionAccessInterceptor {
        -SubscriptionService subscriptionService "구독 상태 조회 서비스"
        +preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) boolean "요청 전 구독 상태 검증"
        -checkSubscriptionAccess(Long tenantId) boolean "테넌트의 구독 상태 확인 (ACTIVE, AUTO_APPROVED만 허용)"
        -isExemptPath(String path) boolean "구독 검증 제외 경로 확인 (/auth, /public 등)"
        -handleAccessDenied(HttpServletResponse response, String reason) "접근 거부 시 에러 응답 처리"
        -extractTenantId(HttpServletRequest request) Long "요청에서 테넌트 ID 추출"
    }
    
    %% 멀티테넌트 설정 - Hibernate 멀티테넌트 지원
    class MultiTenantConfig {
        +hibernatePropertiesCustomizer() HibernatePropertiesCustomizer "Hibernate 멀티테넌트 설정"
        +tenantFilter() FilterRegistrationBean "테넌트 필터 등록 (모든 요청에 적용)"
        +multiTenantConnectionProvider() MultiTenantConnectionProvider "테넌트별 DB 연결 제공자"
        +currentTenantIdentifierResolver() CurrentTenantIdentifierResolver "현재 테넌트 식별자 해결자"
        +tenantInterceptor() TenantInterceptor "JPA 쿼리에 테넌트 조건 자동 추가"
    }
    
    %% 캐시 설정 - Redis 기반 분산 캐시
    class CacheConfig {
        -RedisConnectionFactory redisConnectionFactory "Redis 연결 팩토리"
        +cacheManager() CacheManager "Redis 기반 캐시 매니저 설정"
        +redisTemplate() RedisTemplate "Redis 템플릿 설정 (직렬화 포함)"
        +cacheKeyGenerator() KeyGenerator "캐시 키 생성 전략 (테넌트 ID 포함)"
        +cacheErrorHandler() CacheErrorHandler "캐시 오류 처리기 (장애 시 DB 폴백)"
        +cacheResolver() CacheResolver "동적 캐시 해결자"
    }
    
    %% 비동기 처리 설정 - 백그라운드 작업 처리
    class AsyncConfig {
        +taskExecutor() TaskExecutor "비동기 작업 실행자 설정 (스레드 풀 관리)"
        +asyncUncaughtExceptionHandler() AsyncUncaughtExceptionHandler "비동기 예외 처리기"
        +scheduledTaskExecutor() ScheduledExecutorService "스케줄링 작업 실행자"
        +asyncConfigurer() AsyncConfigurer "비동기 설정 커스터마이저"
    }
    
    %% API 응답 표준화 - 일관된 API 응답 형식
    class ApiResponse {
        <<generic>>
        -boolean success "요청 성공 여부"
        -String message "응답 메시지"
        -T data "응답 데이터 (제네릭)"
        -String errorCode "에러 코드 (실패 시)"
        -LocalDateTime timestamp "응답 시간"
        +success(T data) ApiResponse~T~ "성공 응답 생성"
        +success(T data, String message) ApiResponse~T~ "메시지 포함 성공 응답"
        +error(String message) ApiResponse~Void~ "에러 응답 생성"
        +error(String message, String errorCode) ApiResponse~Void~ "에러 코드 포함 에러 응답"
    }
    
    %% 전역 예외 처리기 - 통합 예외 처리
    class GlobalExceptionHandler {
        +handleValidationException(MethodArgumentNotValidException e) ResponseEntity "입력 검증 예외 처리"
        +handleBusinessException(BusinessException e) ResponseEntity "비즈니스 로직 예외 처리"
        +handleSecurityException(AccessDeniedException e) ResponseEntity "보안 예외 처리"
        +handleSubscriptionException(SubscriptionAccessException e) ResponseEntity "구독 관련 예외 처리"
        +handleGenericException(Exception e) ResponseEntity "일반 예외 처리 (로깅 포함)"
        -logException(Exception e, String context) "예외 로깅 (레벨별 분류)"
    }
    
    %% 상속 관계 및 의존성
    BaseEntity <|-- BaseTenantEntity : "멀티테넌트 지원을 위한 확장"
    BaseTenantEntity --> TenantContext : "테넌트 컨텍스트 사용"
    SecurityConfig --> JwtAuthenticationFilter : "JWT 필터 등록"
    SecurityConfig --> SubscriptionAccessInterceptor : "구독 인터셉터 등록"
    JwtAuthenticationFilter --> TenantContext : "인증 후 테넌트 설정"
    SubscriptionAccessInterceptor --> TenantContext : "테넌트 기반 접근 제어"
    MultiTenantConfig --> TenantContext : "멀티테넌트 식별자 해결"
```

### 주요 기능 설명

#### BaseEntity (기본 엔티티)
모든 도메인 엔티티의 공통 기반 클래스로, JPA Auditing과 기본적인 엔티티 동작을 제공합니다.

- **자동 ID 생성**: @GeneratedValue를 통한 기본키 자동 생성
- **감사 추적**: @CreationTimestamp, @UpdateTimestamp를 통한 생성/수정 시간 자동 관리
- **동등성 보장**: ID 기반의 equals/hashCode 구현으로 엔티티 동등성 보장
- **생명주기 콜백**: @PrePersist, @PreUpdate를 통한 엔티티 생명주기 관리

#### BaseTenantEntity (멀티테넌트 기본 엔티티)
멀티테넌트 SaaS 아키텍처의 핵심으로, 테넌트별 데이터 격리를 자동으로 처리합니다.

- **자동 테넌트 설정**: 엔티티 생성 시 현재 요청의 테넌트 ID 자동 설정
- **데이터 격리**: 모든 데이터 조회 시 자동으로 테넌트 필터링 적용
- **보안 강화**: 테넌트 간 데이터 접근 차단을 통한 보안 강화
- **무결성 검증**: 엔티티 수정 시 테넌트 일치성 검증

#### TenantContext (테넌트 컨텍스트)
ThreadLocal을 사용하여 HTTP 요청별로 독립적인 테넌트 정보를 관리합니다.

- **요청별 격리**: 각 HTTP 요청마다 독립적인 테넌트 컨텍스트 유지
- **자동 설정**: 인증 필터에서 JWT 토큰 기반으로 테넌트 ID 자동 설정
- **메모리 안전**: 요청 완료 시 컨텍스트 자동 정리로 메모리 누수 방지
- **검증 기능**: 테넌트 ID 유효성 검증 및 접근 권한 확인

#### 보안 및 인증 시스템
JWT 기반의 무상태 인증과 구독 상태 기반 접근 제어를 제공합니다.

- **JWT 인증**: 무상태 토큰 기반 인증으로 확장성 확보
- **역할 기반 접근 제어**: 5단계 사용자 역할에 따른 세밀한 권한 관리
- **구독 상태 검증**: 활성 구독 상태의 테넌트만 서비스 이용 허용
- **보안 예외 처리**: 인증/인가 실패 시 적절한 에러 응답 제공

#### 멀티테넌트 인프라
Hibernate 기반의 멀티테넌트 데이터베이스 접근을 지원합니다.

- **자동 필터링**: 모든 JPA 쿼리에 테넌트 조건 자동 추가
- **연결 관리**: 테넌트별 데이터베이스 연결 관리 (필요시 확장 가능)
- **성능 최적화**: 테넌트 기반 인덱스 활용으로 쿼리 성능 최적화
- **데이터 무결성**: 테넌트 간 데이터 혼재 방지

#### 캐시 및 비동기 처리
Redis 기반 분산 캐시와 비동기 작업 처리를 지원합니다.

- **분산 캐시**: Redis를 활용한 멀티 인스턴스 환경 지원
- **테넌트별 캐시**: 캐시 키에 테넌트 ID 포함으로 데이터 격리
- **장애 복구**: 캐시 장애 시 데이터베이스 폴백 처리
- **비동기 처리**: 백그라운드 작업 (이메일 발송, 배치 처리 등) 지원

## 2. User Domain 클래스 다이어그램

### 사용자 관리 도메인
사용자 인증, 권한 관리, 프로필 관리를 담당하는 핵심 도메인으로, 멀티 로그인 지원과 얼굴 인식 기반 출입 관리를 제공합니다.

```mermaid
classDiagram
    %% 사용자 엔티티 - 시스템의 핵심 사용자 정보
    class User {
        -String name "사용자 실명 (필수, 최대 50자)"
        -String email "이메일 주소 (로그인 ID, 유니크 제약)"
        -String phoneNumber "휴대폰 번호 (국제 형식, 선택사항)"
        -String socialId "소셜 로그인 고유 ID (카카오/네이버 연동 시)"
        -Provider provider "로그인 제공자 (LOCAL/KAKAO/NAVER)"
        -String passwordHash "BCrypt 암호화된 비밀번호 (소셜 로그인 시 null)"
        -Role role "사용자 역할 (SUPER/HQ/SITE/TEAM/WORKER)"
        -Boolean isActive "계정 활성화 상태 (기본값: true)"
        -Boolean isEmailVerified "이메일 인증 완료 여부 (기본값: false)"
        -String profileImageUrl "프로필 이미지 URL (S3 저장소 경로)"
        -String faceEmbedding "FaceNet 얼굴 임베딩 벡터 (Base64 인코딩)"
        -Integer loginFailureCount "연속 로그인 실패 횟수 (기본값: 0)"
        -LocalDateTime lastLoginAt "마지막 로그인 시간"
        -LocalDateTime passwordChangedAt "비밀번호 변경 시간"
        +isActive() boolean "계정 활성화 상태 확인"
        +isEmailVerified() boolean "이메일 인증 상태 확인"
        +incrementLoginFailureCount() "로그인 실패 횟수 증가 (최대 10회)"
        +resetLoginFailureCount() "로그인 실패 횟수 초기화 (성공 로그인 시)"
        +isLocked() boolean "계정 잠금 상태 확인 (5회 실패 시 30분 잠금)"
        +updateLastLogin() "마지막 로그인 시간 업데이트"
        +changePassword(String newPassword) "비밀번호 변경 (해시화 포함)"
        +updateFaceEmbedding(String embedding) "얼굴 임베딩 벡터 업데이트"
        +hasRole(Role requiredRole) boolean "특정 역할 권한 확인"
        +canAccessTenant(Long tenantId) boolean "테넌트 접근 권한 확인"
    }
    
    %% 로그인 제공자 열거형 - 다양한 인증 방식 지원
    class Provider {
        <<enumeration>>
        LOCAL "일반 회원가입 (이메일 + 비밀번호)"
        KAKAO "카카오 소셜 로그인 (OAuth 2.0)"
        NAVER "네이버 소셜 로그인 (OAuth 2.0)"
        GOOGLE "구글 소셜 로그인 (OAuth 2.0, 향후 지원)"
        +getDisplayName() String "화면 표시용 이름 반환"
        +isSocialProvider() boolean "소셜 로그인 여부 확인"
        +getOAuthEndpoint() String "OAuth 인증 엔드포인트 URL"
    }
    
    %% 사용자 역할 열거형 - 계층적 권한 구조
    class Role {
        <<enumeration>>
        ROLE_SUPER "슈퍼 관리자 (시스템 전체 관리, 모든 테넌트 접근)"
        ROLE_HQ "본사 관리자 (회사 전체 관리, 단일 테넌트 내 모든 권한)"
        ROLE_SITE "현장 관리자 (현장별 관리, 배정된 현장의 모든 권한)"
        ROLE_TEAM "팀장 (팀 단위 관리, 소속 팀원 및 출입 관리)"
        ROLE_WORKER "작업자 (개인 정보만 접근, 출입 기록 조회)"
        +getLevel() int "권한 레벨 반환 (숫자가 낮을수록 높은 권한)"
        +canAccess(Role targetRole) boolean "대상 역할에 대한 접근 권한 확인"
        +getPermissions() Set~String~ "역할별 권한 목록 반환"
        +getDisplayName() String "화면 표시용 역할명"
        +isAdminRole() boolean "관리자 역할 여부 확인"
    }
    
    %% 사용자 데이터 접근 계층 - JPA Repository 인터페이스
    class UserRepository {
        <<interface>>
        +findByEmail(String email) Optional~User~ "이메일로 사용자 조회 (로그인 시 사용)"
        +findBySocialId(String socialId) Optional~User~ "소셜 ID로 사용자 조회 (소셜 로그인 시)"
        +findByTenantId(Long tenantId) List~User~ "테넌트별 사용자 목록 조회"
        +findByTenantIdAndRole(Long tenantId, Role role) List~User~ "테넌트 + 역할별 사용자 조회"
        +countByTenantId(Long tenantId) long "테넌트별 사용자 수 조회"
        +findActiveUsersByTenantId(Long tenantId) List~User~ "활성 사용자만 조회"
        +findByEmailAndProvider(String email, Provider provider) Optional~User~ "이메일 + 제공자로 조회"
        +existsByEmailAndTenantId(String email, Long tenantId) boolean "이메일 중복 확인"
        +findUsersWithFaceEmbedding(Long tenantId) List~User~ "얼굴 임베딩이 있는 사용자 조회"
        +findByRoleIn(Collection~Role~ roles) List~User~ "여러 역할의 사용자 조회"
    }
    
    %% 사용자 서비스 인터페이스 - 비즈니스 로직 정의
    class UserService {
        <<interface>>
        +createUser(CreateUserRequest request) UserDto "새 사용자 생성 (이메일 중복 검증 포함)"
        +updateUser(Long userId, UpdateUserRequest request) UserDto "사용자 정보 수정 (권한 검증 포함)"
        +deleteUser(Long userId) "사용자 삭제 (소프트 삭제, 관련 데이터 정리)"
        +getUserById(Long userId) UserDto "사용자 ID로 조회 (권한 검증 포함)"
        +getUsersByTenant(Long tenantId, Pageable pageable) Page~UserDto~ "테넌트별 사용자 목록 조회"
        +authenticateUser(String email, String password) AuthenticationResult "사용자 인증 처리"
        +registerSocialUser(SocialUserInfo socialInfo) UserDto "소셜 로그인 사용자 등록"
        +updateFaceEmbedding(Long userId, String embedding) "얼굴 임베딩 업데이트"
        +resetPassword(String email) "비밀번호 재설정 (이메일 발송)"
        +changePassword(Long userId, ChangePasswordRequest request) "비밀번호 변경"
        +verifyEmail(String token) "이메일 인증 처리"
        +getUsersByRole(Role role, Long tenantId) List~UserDto~ "역할별 사용자 조회"
    }
    
    %% 사용자 서비스 구현체 - 실제 비즈니스 로직 구현
    class UserServiceImpl {
        -UserRepository userRepository "사용자 데이터 접근 객체"
        -PasswordEncoder passwordEncoder "비밀번호 암호화 서비스"
        -EmailService emailService "이메일 발송 서비스"
        -FaceRecognitionService faceService "얼굴 인식 서비스 (FaceNet 연동)"
        -TenantService tenantService "테넌트 관리 서비스"
        -NotificationService notificationService "알림 서비스"
        +createUser(CreateUserRequest request) UserDto "사용자 생성 로직 구현"
        +updateUser(Long userId, UpdateUserRequest request) UserDto "사용자 수정 로직 구현"
        +deleteUser(Long userId) "사용자 삭제 로직 구현 (관련 데이터 정리)"
        +getUserById(Long userId) UserDto "사용자 조회 로직 구현"
        +getUsersByTenant(Long tenantId, Pageable pageable) Page~UserDto~ "테넌트별 조회 로직 구현"
        -validateUserCreation(CreateUserRequest request) "사용자 생성 유효성 검증"
        -sendWelcomeEmail(User user) "환영 이메일 발송"
        -generateEmailVerificationToken(User user) String "이메일 인증 토큰 생성"
        -checkDuplicateEmail(String email, Long tenantId) "이메일 중복 검증"
        -updateUserFaceData(User user, String embedding) "얼굴 데이터 업데이트"
    }
    
    %% 사용자 REST API 컨트롤러 - HTTP 요청 처리
    class UserController {
        -UserService userService "사용자 서비스 의존성"
        -AuthenticationService authService "인증 서비스"
        +getUsers(Pageable pageable, String search, Role role) ResponseEntity "사용자 목록 조회 API"
        +getUser(Long userId) ResponseEntity "특정 사용자 조회 API"
        +createUser(CreateUserRequest request) ResponseEntity "사용자 생성 API"
        +updateUser(Long userId, UpdateUserRequest request) ResponseEntity "사용자 수정 API"
        +deleteUser(Long userId) ResponseEntity "사용자 삭제 API"
        +uploadProfileImage(Long userId, MultipartFile file) ResponseEntity "프로필 이미지 업로드 API"
        +updateFaceEmbedding(Long userId, FaceEmbeddingRequest request) ResponseEntity "얼굴 임베딩 업데이트 API"
        +changePassword(Long userId, ChangePasswordRequest request) ResponseEntity "비밀번호 변경 API"
        +resetPassword(ResetPasswordRequest request) ResponseEntity "비밀번호 재설정 API"
        +verifyEmail(String token) ResponseEntity "이메일 인증 API"
        +getUserProfile() ResponseEntity "현재 사용자 프로필 조회 API"
    }
    
    %% 인증 서비스 - 로그인/로그아웃 처리
    class AuthenticationService {
        -UserRepository userRepository "사용자 데이터 접근"
        -JwtTokenProvider jwtTokenProvider "JWT 토큰 생성/검증"
        -PasswordEncoder passwordEncoder "비밀번호 검증"
        -SocialLoginService socialLoginService "소셜 로그인 처리"
        +login(LoginRequest request) AuthenticationResponse "일반 로그인 처리"
        +socialLogin(SocialLoginRequest request) AuthenticationResponse "소셜 로그인 처리"
        +logout(String token) "로그아웃 처리 (토큰 무효화)"
        +refreshToken(String refreshToken) TokenResponse "토큰 갱신"
        +validateToken(String token) boolean "토큰 유효성 검증"
        -handleLoginFailure(User user) "로그인 실패 처리"
        -handleLoginSuccess(User user) "로그인 성공 처리"
        -generateTokens(User user) TokenPair "액세스/리프레시 토큰 생성"
    }
    
    %% 관계 설정 및 의존성
    BaseTenantEntity <|-- User : "멀티테넌트 지원 (테넌트별 사용자 격리)"
    User --> Provider : "로그인 제공자 정보 (enum 연관)"
    User --> Role : "사용자 권한 정보 (enum 연관)"
    UserService <|.. UserServiceImpl : "서비스 인터페이스 구현"
    UserServiceImpl --> UserRepository : "데이터 접근 계층 사용"
    UserController --> UserService : "비즈니스 로직 호출"
    UserController --> AuthenticationService : "인증 관련 기능 호출"
    UserServiceImpl --> AuthenticationService : "인증 로직 연동"
```

### 주요 기능 설명

#### User 엔티티 (사용자 정보 관리)
시스템의 핵심 사용자 정보를 관리하며, 다양한 인증 방식과 보안 기능을 지원합니다.

- **멀티 로그인 지원**: 
  - 일반 회원가입 (이메일 + 비밀번호)
  - 소셜 로그인 (카카오, 네이버, 향후 구글 지원)
  - 각 제공자별 고유 ID 관리로 계정 연동 지원

- **얼굴 인식 연동**: 
  - FaceNet 기반 얼굴 임베딩 벡터 저장
  - 출입 관리 시스템과 연동하여 자동 출입 기록
  - 임베딩 벡터 업데이트를 통한 인식 정확도 향상

- **보안 강화 기능**: 
  - 로그인 실패 횟수 추적 (5회 실패 시 30분 계정 잠금)
  - 비밀번호 변경 이력 관리
  - 이메일 인증을 통한 계정 활성화
  - BCrypt를 사용한 안전한 비밀번호 해시화

- **사용자 상태 관리**: 
  - 계정 활성화/비활성화 상태 관리
  - 마지막 로그인 시간 추적
  - 프로필 이미지 관리 (S3 연동)

#### 역할 기반 접근 제어 (RBAC)
5단계 계층적 권한 구조로 세밀한 접근 제어를 제공합니다.

- **계층적 권한 구조**: 
  - SUPER > HQ > SITE > TEAM > WORKER 순서의 권한 레벨
  - 상위 권한이 하위 권한의 모든 기능 포함
  - 권한 레벨 기반 접근 제어 로직

- **역할별 권한 범위**: 
  - **SUPER**: 모든 테넌트 접근, 시스템 관리
  - **HQ**: 단일 테넌트 내 모든 권한, 회사 전체 관리
  - **SITE**: 배정된 현장의 모든 권한, 현장별 관리
  - **TEAM**: 소속 팀원 관리, 팀 단위 출입 관리
  - **WORKER**: 개인 정보 조회, 출입 기록 확인

- **테넌트별 격리**: 
  - 각 회사(테넌트)별로 독립적인 사용자 관리
  - 테넌트 간 데이터 접근 차단
  - 멀티테넌트 환경에서의 보안 강화

#### 서비스 계층 패턴
인터페이스와 구현체 분리를 통한 유연한 아키텍처를 제공합니다.

- **인터페이스 분리 원칙**: 
  - 비즈니스 로직과 구현체 분리
  - 테스트 용이성 향상 (Mock 객체 활용)
  - 향후 구현체 교체 시 유연성 확보

- **트랜잭션 관리**: 
  - @Transactional을 통한 데이터 일관성 보장
  - 롤백 처리를 통한 데이터 무결성 유지
  - 복합 작업의 원자성 보장

- **예외 처리**: 
  - 도메인별 커스텀 예외 정의
  - 명확한 오류 메시지 제공
  - 로깅을 통한 문제 추적 지원

#### 인증 및 보안 서비스
JWT 기반 무상태 인증과 소셜 로그인을 지원합니다.

- **JWT 토큰 관리**: 
  - 액세스 토큰 (15분) + 리프레시 토큰 (7일) 구조
  - 토큰 갱신을 통한 지속적인 인증 유지
  - 로그아웃 시 토큰 블랙리스트 처리

- **소셜 로그인 연동**: 
  - OAuth 2.0 표준 준수
  - 카카오, 네이버 API 연동
  - 소셜 계정 정보 동기화

- **보안 정책**: 
  - 비밀번호 복잡도 검증
  - 계정 잠금 정책 (5회 실패 시 30분)
  - 이메일 인증을 통한 계정 활성화

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

### 프론트엔드 아키텍처 개요
React 18 + TypeScript 기반의 모던 프론트엔드 아키텍처로, 역할별 페이지 구성과 재사용 가능한 컴포넌트 시스템을 제공합니다.

```mermaid
classDiagram
    %% 슈퍼 관리자 페이지 컴포넌트들
    class ApprovalsSuper {
        -page: number "현재 페이지 번호 (페이지네이션)"
        -selectedApproval: SubscriptionDto "선택된 승인 대상 구독"
        -approvalReason: string "승인 사유 입력값"
        -rejectionReason: string "거부 사유 입력값"
        -showApprovalDialog: boolean "승인 다이얼로그 표시 상태"
        -showRejectionDialog: boolean "거부 다이얼로그 표시 상태"
        -showHistoryDialog: boolean "승인 이력 다이얼로그 표시 상태"
        -searchTerm: string "검색어 (회사명, 사업자번호 등)"
        -statusFilter: SubscriptionStatus "상태 필터 (전체/대기/승인/거부)"
        +handleApprove(subscriptionId: number, reason: string) "구독 승인 처리 (API 호출 + UI 업데이트)"
        +handleReject(subscriptionId: number, reason: string) "구독 거부 처리 (API 호출 + UI 업데이트)"
        +handleSearch(term: string) "검색 처리 (디바운싱 적용)"
        +handleFilterChange(status: SubscriptionStatus) "상태 필터 변경"
        +getStatusText(status: string) string "상태 코드를 한글 텍스트로 변환"
        +formatCurrency(amount: number) string "금액을 한국 원화 형식으로 포맷팅"
        +formatDate(dateString: string) string "날짜를 한국 형식으로 포맷팅"
        +openApprovalDialog(approval: SubscriptionDto) "승인 다이얼로그 열기"
        +openRejectionDialog(approval: SubscriptionDto) "거부 다이얼로그 열기"
        +openHistoryDialog(subscriptionId: number) "승인 이력 다이얼로그 열기"
    }
    
    class SubscriptionDetailModal {
        -subscription: SubscriptionDto "구독 상세 정보"
        -actionType: string "수행할 액션 타입 (approve/reject/suspend/terminate)"
        -actionReason: string "액션 수행 사유"
        -showActionDialog: boolean "액션 확인 다이얼로그 표시 상태"
        -isLoading: boolean "API 호출 중 로딩 상태"
        +formatDate(dateString: string) string "날짜 포맷팅 (YYYY-MM-DD HH:mm)"
        +formatCurrency(amount: number) string "통화 포맷팅 (₩1,000,000)"
        +getSubscriptionStatusText(status: string) string "구독 상태 한글 변환"
        +getSubscriptionStatusColor(status: string) string "상태별 색상 클래스 반환"
        +handleAction(actionType: string, reason: string) "액션 실행 (승인/거부/중지/종료)"
        +openActionDialog(type: string) "액션 다이얼로그 열기"
        +getAvailableActions() ActionOption[] "현재 상태에서 가능한 액션 목록"
        +calculateNextBillingDate() string "다음 결제일 계산"
        +getRemainingDays() number "구독 만료까지 남은 일수"
    }
    
    class AutoApprovalSuper {
        -rules: AutoApprovalRule[] "자동 승인 규칙 목록"
        -selectedRule: AutoApprovalRule "선택된 규칙 (수정 시)"
        -showRuleDialog: boolean "규칙 생성/수정 다이얼로그 표시 상태"
        -ruleForm: AutoApprovalRuleForm "규칙 폼 데이터"
        -sortBy: string "정렬 기준 (priority/createdAt/ruleName)"
        -sortOrder: string "정렬 순서 (asc/desc)"
        +handleCreateRule(ruleData: AutoApprovalRuleForm) "새 규칙 생성"
        +handleUpdateRule(ruleId: number, ruleData: AutoApprovalRuleForm) "기존 규칙 수정"
        +handleDeleteRule(ruleId: number) "규칙 삭제 (확인 다이얼로그 포함)"
        +handleToggleRule(ruleId: number, isActive: boolean) "규칙 활성화/비활성화 토글"
        +handlePriorityChange(ruleId: number, newPriority: number) "규칙 우선순위 변경"
        +validateRuleForm(formData: AutoApprovalRuleForm) ValidationResult "규칙 폼 유효성 검증"
        +openRuleDialog(rule?: AutoApprovalRule) "규칙 다이얼로그 열기 (생성/수정)"
        +handleSort(column: string) "테이블 정렬 처리"
        +exportRules() "규칙 목록 CSV 내보내기"
    }
    
    %% 공통 컴포넌트들
    class SubscriptionGuard {
        -subscriptionStatus: string "현재 테넌트의 구독 상태"
        -allowedStatuses: string[] "접근 허용 상태 목록"
        -children: ReactNode "보호할 자식 컴포넌트"
        -fallbackComponent?: ReactNode "접근 거부 시 표시할 컴포넌트"
        +checkAccess() boolean "구독 상태 기반 접근 권한 확인"
        +renderAccessDenied() JSX.Element "접근 거부 화면 렌더링"
        +getAccessDeniedMessage() string "접근 거부 메시지 생성"
        +redirectToSubscription() "구독 페이지로 리다이렉트"
    }
    
    class SubscriptionStatusDisplay {
        -status: SubscriptionStatus "표시할 구독 상태"
        -showDetails: boolean "상세 정보 표시 여부"
        -size: 'sm' | 'md' | 'lg' "컴포넌트 크기"
        -interactive: boolean "클릭 가능 여부"
        +getStatusColor(status: string) string "상태별 색상 클래스 (bg-green-100, text-green-800 등)"
        +getStatusIcon(status: string) JSX.Element "상태별 아이콘 컴포넌트"
        +getStatusMessage(status: string) string "상태별 설명 메시지"
        +getStatusBadgeClass(status: string) string "배지 스타일 클래스"
        +handleStatusClick() "상태 클릭 시 상세 정보 토글"
    }
    
    class NotificationDropdown {
        -isOpen: boolean "드롭다운 열림 상태"
        -notifications: Notification[] "알림 목록"
        -unreadCount: number "읽지 않은 알림 수"
        -isLoading: boolean "알림 로딩 상태"
        -hasMore: boolean "더 많은 알림 존재 여부"
        +toggleDropdown() "드롭다운 열기/닫기 토글"
        +markAsRead(notificationId: number) "특정 알림을 읽음으로 표시"
        +markAllAsRead() "모든 알림을 읽음으로 표시"
        +loadMoreNotifications() "추가 알림 로드 (무한 스크롤)"
        +handleNotificationClick(notification: Notification) "알림 클릭 처리 (페이지 이동)"
        +formatNotificationTime(timestamp: string) string "알림 시간 상대적 표시 (5분 전, 1시간 전)"
        +getNotificationIcon(type: NotificationType) JSX.Element "알림 타입별 아이콘"
    }
    
    %% 커스텀 훅들 - API 연동 및 상태 관리
    class useAdminApi {
        +usePendingApprovals(params: PaginationParams) QueryResult~Page~SubscriptionDto~~ "승인 대기 목록 조회 (TanStack Query)"
        +useApproveSubscription() MutationResult~SubscriptionDto~ "구독 승인 뮤테이션"
        +useRejectSubscription() MutationResult~SubscriptionDto~ "구독 거부 뮤테이션"
        +useSuspendSubscription() MutationResult~SubscriptionDto~ "구독 일시 중지 뮤테이션"
        +useTerminateSubscription() MutationResult~SubscriptionDto~ "구독 종료 뮤테이션"
        +useApprovalHistory(subscriptionId: number) QueryResult~SubscriptionApprovalDto[]~ "승인 이력 조회"
        +useSubscriptionStats() QueryResult~ApprovalStatsDto~ "승인 통계 조회"
        -handleApiError(error: ApiError) "API 에러 처리 (토스트 알림)"
        -invalidateQueries(queryKeys: string[]) "관련 쿼리 무효화 (캐시 갱신)"
    }
    
    class useSubscriptionAccessControl {
        -accessState: SubscriptionAccessState "구독 접근 상태"
        -blockReason: string "접근 차단 사유"
        -retryCount: number "재시도 횟수"
        +clearAccessBlock() "접근 차단 상태 해제"
        +handleSubscriptionError(error: SubscriptionAccessError) "구독 관련 에러 처리"
        +checkSubscriptionStatus() Promise~SubscriptionStatus~ "구독 상태 확인"
        +retryAccess() "접근 재시도"
        +getAccessBlockMessage() string "접근 차단 메시지 생성"
        -scheduleStatusCheck() "주기적 상태 확인 스케줄링"
    }
    
    class useAutoApprovalRules {
        +useAutoApprovalRules(params?: QueryParams) QueryResult~AutoApprovalRule[]~ "자동 승인 규칙 목록 조회"
        +useCreateAutoApprovalRule() MutationResult~AutoApprovalRule~ "새 규칙 생성 뮤테이션"
        +useUpdateAutoApprovalRule() MutationResult~AutoApprovalRule~ "규칙 수정 뮤테이션"
        +useDeleteAutoApprovalRule() MutationResult~void~ "규칙 삭제 뮤테이션"
        +useToggleAutoApprovalRule() MutationResult~AutoApprovalRule~ "규칙 활성화 토글 뮤테이션"
        +useRuleValidation() ValidationHook "규칙 유효성 검증 훅"
        -optimisticUpdate(ruleId: number, updates: Partial~AutoApprovalRule~) "낙관적 업데이트"
        -rollbackUpdate(ruleId: number) "업데이트 롤백"
    }
    
    %% 상태 관리 스토어 (Zustand)
    class notificationStore {
        -notifications: Notification[] "알림 목록 상태"
        -unreadCount: number "읽지 않은 알림 수"
        -isPolling: boolean "폴링 활성화 상태"
        -pollingInterval: number "폴링 간격 (밀리초)"
        -lastFetchTime: Date "마지막 알림 조회 시간"
        +addNotification(notification: Notification) "새 알림 추가"
        +markAsRead(notificationId: number) "알림 읽음 처리"
        +markAllAsRead() "모든 알림 읽음 처리"
        +removeNotification(notificationId: number) "알림 제거"
        +startPolling() "실시간 알림 폴링 시작"
        +stopPolling() "실시간 알림 폴링 중지"
        +updateUnreadCount() "읽지 않은 알림 수 업데이트"
        +clearNotifications() "모든 알림 제거"
        -fetchNotifications() "서버에서 알림 조회"
        -schedulePolling() "폴링 스케줄 설정"
    }
    
    %% 타입 정의들
    class SubscriptionDto {
        +id: number
        +tenantId: number
        +companyName: string
        +planId: string
        +status: SubscriptionStatus
        +monthlyPrice: number
        +startDate: string
        +endDate: string
        +approvalRequestedAt: string
        +approvedAt?: string
        +approvedBy?: string
        +rejectionReason?: string
    }
    
    class AutoApprovalRule {
        +id: number
        +ruleName: string
        +isActive: boolean
        +planIds: string[]
        +verifiedTenantsOnly: boolean
        +paymentMethods: string[]
        +maxAmount: number
        +priority: number
        +createdAt: string
        +updatedAt: string
    }
    
    class Notification {
        +id: number
        +type: NotificationType
        +title: string
        +message: string
        +isRead: boolean
        +createdAt: string
        +data?: Record~string, any~
    }
    
    %% 컴포넌트 간 관계 및 의존성
    ApprovalsSuper --> useAdminApi : "승인 관련 API 호출"
    ApprovalsSuper --> SubscriptionDetailModal : "구독 상세 모달 렌더링"
    SubscriptionDetailModal --> useAdminApi : "구독 액션 API 호출"
    AutoApprovalSuper --> useAutoApprovalRules : "자동 승인 규칙 관리"
    SubscriptionGuard --> useSubscriptionAccessControl : "접근 제어 로직"
    NotificationDropdown --> notificationStore : "알림 상태 관리"
    NotificationDropdown --> useAdminApi : "알림 관련 API 호출"
    
    %% 데이터 흐름
    useAdminApi --> SubscriptionDto : "구독 데이터 반환"
    useAutoApprovalRules --> AutoApprovalRule : "규칙 데이터 반환"
    notificationStore --> Notification : "알림 데이터 관리"
```

### 주요 기능 설명

#### 슈퍼 관리자 페이지 컴포넌트

**ApprovalsSuper (구독 승인 관리)**
- **실시간 승인 처리**: 구독 신청에 대한 즉시 승인/거부 처리
- **검색 및 필터링**: 회사명, 사업자번호, 상태별 검색 지원
- **배치 처리**: 여러 구독을 한 번에 승인/거부 (향후 지원)
- **승인 이력 추적**: 각 구독의 상세한 승인 과정 기록 조회

**AutoApprovalSuper (자동 승인 규칙 관리)**
- **규칙 기반 자동화**: 조건에 맞는 구독 자동 승인 처리
- **우선순위 관리**: 여러 규칙 간 우선순위 설정 및 관리
- **실시간 토글**: 규칙 활성화/비활성화 즉시 적용
- **규칙 검증**: 생성/수정 시 규칙 충돌 및 유효성 검증

#### 공통 컴포넌트 시스템

**SubscriptionGuard (구독 상태 기반 접근 제어)**
- **자동 접근 제어**: 구독 상태에 따른 페이지/기능 접근 차단
- **우아한 폴백**: 접근 거부 시 적절한 안내 메시지 표시
- **리다이렉트 처리**: 구독 갱신 페이지로 자동 안내
- **실시간 상태 확인**: 구독 상태 변경 시 즉시 반영

**SubscriptionStatusDisplay (구독 상태 표시)**
- **시각적 상태 표현**: 색상과 아이콘을 통한 직관적 상태 표시
- **다양한 크기 지원**: 컨텍스트에 맞는 크기 조절 가능
- **상호작용 지원**: 클릭 시 상세 정보 표시 옵션
- **접근성 준수**: 스크린 리더 지원 및 키보드 네비게이션

**NotificationDropdown (실시간 알림)**
- **실시간 폴링**: 새로운 알림 자동 수신 및 표시
- **무한 스크롤**: 대량의 알림 효율적 로딩
- **타입별 분류**: 알림 타입에 따른 아이콘 및 색상 구분
- **액션 연동**: 알림 클릭 시 관련 페이지로 자동 이동

#### 커스텀 훅 시스템

**useAdminApi (관리자 API 연동)**
- **TanStack Query 활용**: 서버 상태 캐싱 및 동기화
- **낙관적 업데이트**: UI 반응성 향상을 위한 즉시 UI 업데이트
- **에러 처리**: API 에러 시 사용자 친화적 메시지 표시
- **캐시 무효화**: 데이터 변경 시 관련 캐시 자동 갱신

**useSubscriptionAccessControl (구독 접근 제어)**
- **상태 모니터링**: 구독 상태 실시간 모니터링
- **자동 재시도**: 일시적 네트워크 오류 시 자동 재시도
- **에러 복구**: 구독 관련 에러 발생 시 복구 로직
- **사용자 안내**: 접근 제한 시 명확한 안내 메시지

**useAutoApprovalRules (자동 승인 규칙 관리)**
- **CRUD 연산**: 규칙 생성, 조회, 수정, 삭제 통합 관리
- **실시간 검증**: 규칙 설정 시 실시간 유효성 검증
- **충돌 감지**: 기존 규칙과의 충돌 자동 감지
- **성능 최적화**: 규칙 변경 시 최소한의 API 호출

#### 상태 관리 아키텍처

**notificationStore (알림 상태 관리)**
- **Zustand 기반**: 경량 상태 관리로 성능 최적화
- **실시간 동기화**: 서버와 클라이언트 알림 상태 동기화
- **메모리 관리**: 오래된 알림 자동 정리로 메모리 사용량 최적화
- **오프라인 지원**: 네트워크 연결 복구 시 알림 동기화

#### 성능 최적화 전략

**컴포넌트 최적화**
- **React.memo**: 불필요한 리렌더링 방지
- **useMemo/useCallback**: 계산 비용이 높은 연산 메모이제이션
- **코드 스플리팅**: 페이지별 번들 분할로 초기 로딩 시간 단축
- **가상화**: 대량 데이터 렌더링 시 가상 스크롤 적용

**API 최적화**
- **쿼리 배칭**: 여러 API 호출을 하나로 묶어 네트워크 요청 최소화
- **캐시 전략**: 적절한 캐시 TTL 설정으로 서버 부하 감소
- **백그라운드 업데이트**: 사용자 경험을 해치지 않는 백그라운드 데이터 갱신
- **에러 바운더리**: 컴포넌트 에러 격리로 전체 앱 안정성 확보

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
    end
    
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

## 9. 시스템 설계 원칙 및 패턴

### 아키텍처 설계 원칙

#### 1. 도메인 주도 설계 (Domain-Driven Design)
- **도메인별 패키지 구조**: User, Subscription, Admin 등 비즈니스 도메인별로 코드 구조화
- **유비쿼터스 언어**: 비즈니스 용어와 코드 용어의 일치로 의사소통 향상
- **경계 컨텍스트**: 각 도메인 간의 명확한 경계 설정으로 복잡성 관리

#### 2. 관심사의 분리 (Separation of Concerns)
- **계층형 아키텍처**: Presentation → Business → Data Access → Database 계층 분리
- **인터페이스 분리**: 구현체와 인터페이스 분리로 의존성 역전 원칙 적용
- **단일 책임 원칙**: 각 클래스와 메서드는 하나의 책임만 가지도록 설계

#### 3. 확장성과 유지보수성
- **멀티테넌트 아키텍처**: 테넌트별 데이터 격리로 SaaS 확장성 확보
- **캐시 전략**: Redis 기반 분산 캐시로 성능 최적화
- **비동기 처리**: 백그라운드 작업 처리로 사용자 경험 향상

### 주요 디자인 패턴

#### 1. Repository 패턴
```java
// 데이터 접근 로직을 캡슐화하여 비즈니스 로직과 분리
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByTenantId(Long tenantId);
}
```

#### 2. Service Layer 패턴
```java
// 비즈니스 로직을 서비스 계층에 집중
@Service
@Transactional
public class UserServiceImpl implements UserService {
    // 복잡한 비즈니스 로직 구현
}
```

#### 3. DTO 패턴
```java
// 계층 간 데이터 전송을 위한 전용 객체
public class UserDto {
    // 필요한 데이터만 포함하여 보안 및 성능 향상
}
```

#### 4. Factory 패턴
```typescript
// 프론트엔드에서 API 응답 객체 생성
class ApiResponseFactory {
    static success<T>(data: T): ApiResponse<T> {
        return { success: true, data, timestamp: new Date() };
    }
}
```

#### 5. Observer 패턴
```typescript
// 상태 변경 시 관련 컴포넌트 자동 업데이트
const notificationStore = create((set) => ({
    notifications: [],
    addNotification: (notification) => 
        set((state) => ({ notifications: [...state.notifications, notification] }))
}));
```

### 보안 설계 원칙

#### 1. 다층 보안 (Defense in Depth)
- **네트워크 레벨**: HTTPS 강제, CORS 정책 적용
- **애플리케이션 레벨**: JWT 인증, 역할 기반 접근 제어
- **데이터 레벨**: 테넌트별 데이터 격리, 암호화

#### 2. 최소 권한 원칙 (Principle of Least Privilege)
- **역할별 권한**: 사용자 역할에 따른 최소한의 권한만 부여
- **API 접근 제어**: 엔드포인트별 세밀한 권한 검증
- **데이터 접근**: 테넌트 컨텍스트 기반 자동 필터링

#### 3. 보안 감사 (Security Auditing)
- **접근 로그**: 모든 API 호출 및 데이터 접근 기록
- **변경 추적**: 중요 데이터 변경 시 이력 관리
- **실패 모니터링**: 로그인 실패, 권한 위반 등 보안 이벤트 추적

### 성능 최적화 전략

#### 1. 데이터베이스 최적화
- **인덱스 전략**: 테넌트 ID 기반 복합 인덱스로 쿼리 성능 향상
- **쿼리 최적화**: N+1 문제 해결을 위한 Fetch Join 활용
- **연결 풀 관리**: HikariCP를 통한 효율적인 DB 연결 관리

#### 2. 캐시 전략
- **L1 캐시**: JPA 1차 캐시 활용
- **L2 캐시**: Redis 기반 분산 캐시
- **CDN**: 정적 자원 캐싱으로 로딩 속도 향상

#### 3. 프론트엔드 최적화
- **코드 스플리팅**: 라우트별 번들 분할
- **지연 로딩**: 필요한 시점에 컴포넌트 로드
- **메모이제이션**: React.memo, useMemo 활용

### 확장성 고려사항

#### 1. 수평 확장 (Horizontal Scaling)
- **무상태 설계**: JWT 기반 인증으로 서버 간 세션 공유 불필요
- **로드 밸런싱**: 여러 인스턴스 간 트래픽 분산
- **데이터베이스 샤딩**: 테넌트별 데이터 분산 (향후 고려)

#### 2. 마이크로서비스 전환 준비
- **도메인별 모듈화**: 향후 서비스 분리 시 용이한 구조
- **API 게이트웨이**: 단일 진입점을 통한 라우팅 및 인증
- **이벤트 기반 아키텍처**: 서비스 간 느슨한 결합

#### 3. 클라우드 네이티브
- **컨테이너화**: Docker 기반 배포
- **오케스트레이션**: Kubernetes를 통한 자동 스케일링
- **모니터링**: 분산 추적 및 메트릭 수집

### 테스트 전략

#### 1. 테스트 피라미드
- **단위 테스트**: 개별 메서드 및 컴포넌트 테스트
- **통합 테스트**: 계층 간 상호작용 테스트
- **E2E 테스트**: 사용자 시나리오 기반 전체 플로우 테스트

#### 2. 테스트 자동화
- **CI/CD 파이프라인**: 코드 변경 시 자동 테스트 실행
- **테스트 커버리지**: 80% 이상 코드 커버리지 목표
- **성능 테스트**: 부하 테스트를 통한 성능 검증

### 모니터링 및 관찰성

#### 1. 로깅 전략
- **구조화된 로깅**: JSON 형태의 로그로 분석 용이성 향상
- **로그 레벨**: DEBUG, INFO, WARN, ERROR 적절한 분류
- **중앙 집중식 로깅**: ELK 스택을 통한 로그 수집 및 분석

#### 2. 메트릭 수집
- **비즈니스 메트릭**: 사용자 수, 구독 수, 매출 등
- **기술 메트릭**: 응답 시간, 에러율, 처리량 등
- **인프라 메트릭**: CPU, 메모리, 디스크 사용률

#### 3. 알림 시스템
- **임계값 기반 알림**: 성능 지표 임계값 초과 시 알림
- **에러 추적**: 실시간 에러 발생 모니터링
- **사용자 행동 추적**: 비정상적인 사용 패턴 감지

이러한 설계 원칙과 패턴들은 SmartCON Lite 시스템의 안정성, 확장성, 유지보수성을 보장하며, 향후 비즈니스 요구사항 변화에 유연하게 대응할 수 있는 기반을 제공합니다.