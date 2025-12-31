# SmartCON Lite 클래스 다이어그램

## 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph "Frontend (React + TypeScript)"
        FE_Pages[Pages]
        FE_Components[Components]
        FE_Hooks[Hooks]
        FE_Stores[Stores]
    end
    
    subgraph "Backend (Spring Boot + Java)"
        BE_Controllers[Controllers]
        BE_Services[Services]
        BE_Repositories[Repositories]
        BE_Entities[Entities]
    end
    
    subgraph "Database (MariaDB)"
        DB_Tables[Tables]
    end
    
    FE_Pages --> FE_Components
    FE_Components --> FE_Hooks
    FE_Hooks --> BE_Controllers
    BE_Controllers --> BE_Services
    BE_Services --> BE_Repositories
    BE_Repositories --> BE_Entities
    BE_Entities --> DB_Tables
    FE_Stores --> FE_Components
```

## 1. Global 패키지 클래스 다이어그램

```mermaid
classDiagram
    class BaseEntity {
        <<abstract>>
        -Long id
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +onCreate()
        +onUpdate()
        +equals(Object obj) boolean
        +hashCode() int
    }
    
    class BaseTenantEntity {
        <<abstract>>
        -Long tenantId
        +onCreate()
        +onUpdate()
        +equals(Object obj) boolean
        +hashCode() int
    }
    
    class TenantContext {
        <<utility>>
        +getCurrentTenantId() Long
        +setCurrentTenantId(Long tenantId)
        +clear()
    }
    
    class SecurityConfig {
        +filterChain(HttpSecurity http) SecurityFilterChain
        +passwordEncoder() PasswordEncoder
        +authenticationManager() AuthenticationManager
    }
    
    class JwtAuthenticationFilter {
        +doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        -extractToken(HttpServletRequest request) String
        -validateToken(String token) boolean
    }
    
    class SubscriptionAccessInterceptor {
        +preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) boolean
        -checkSubscriptionAccess(Long tenantId) boolean
    }
    
    class MultiTenantConfig {
        +hibernatePropertiesCustomizer() HibernatePropertiesCustomizer
        +tenantFilter() FilterRegistrationBean
    }
    
    class CacheConfig {
        +cacheManager() CacheManager
        +redisTemplate() RedisTemplate
    }
    
    class AsyncConfig {
        +taskExecutor() TaskExecutor
        +asyncUncaughtExceptionHandler() AsyncUncaughtExceptionHandler
    }
    
    BaseEntity <|-- BaseTenantEntity
    BaseTenantEntity --> TenantContext : uses
```

## 2. User Domain 클래스 다이어그램

```mermaid
classDiagram
    class User {
        -String name
        -String email
        -String phoneNumber
        -String socialId
        -Provider provider
        -String passwordHash
        -Boolean isActive
        -Boolean isEmailVerified
        -String profileImageUrl
        -String faceEmbedding
        -Integer loginFailureCount
        +isActive() boolean
        +isEmailVerified() boolean
        +incrementLoginFailureCount()
        +resetLoginFailureCount()
        +isLocked() boolean
    }
    
    class Provider {
        <<enumeration>>
        LOCAL
        KAKAO
        NAVER
    }
    
    class Role {
        <<enumeration>>
        ROLE_SUPER
        ROLE_HQ
        ROLE_SITE
        ROLE_TEAM
        ROLE_WORKER
    }
    
    class UserRepository {
        <<interface>>
        +findByEmail(String email) Optional~User~
        +findBySocialId(String socialId) Optional~User~
        +findByTenantId(Long tenantId) List~User~
        +countByTenantId(Long tenantId) long
    }
    
    class UserService {
        <<interface>>
        +createUser(CreateUserRequest request) UserDto
        +updateUser(Long userId, UpdateUserRequest request) UserDto
        +deleteUser(Long userId)
        +getUserById(Long userId) UserDto
        +getUsersByTenant(Long tenantId) List~UserDto~
    }
    
    class UserServiceImpl {
        -UserRepository userRepository
        +createUser(CreateUserRequest request) UserDto
        +updateUser(Long userId, UpdateUserRequest request) UserDto
        +deleteUser(Long userId)
        +getUserById(Long userId) UserDto
        +getUsersByTenant(Long tenantId) List~UserDto~
    }
    
    class UserController {
        -UserService userService
        +getUsers(Pageable pageable) ResponseEntity
        +getUser(Long userId) ResponseEntity
        +createUser(CreateUserRequest request) ResponseEntity
        +updateUser(Long userId, UpdateUserRequest request) ResponseEntity
        +deleteUser(Long userId) ResponseEntity
    }
    
    BaseTenantEntity <|-- User
    User --> Provider : uses
    User --> Role : uses
    UserService <|.. UserServiceImpl
    UserServiceImpl --> UserRepository : uses
    UserController --> UserService : uses
```

## 3. Subscription Domain 클래스 다이어그램

```mermaid
classDiagram
    class Subscription {
        -Tenant tenant
        -SubscriptionPlan plan
        -SubscriptionStatus status
        -LocalDate startDate
        -LocalDate endDate
        -LocalDate nextBillingDate
        -BillingCycle billingCycle
        -BigDecimal monthlyPrice
        -BigDecimal discountRate
        -Boolean autoRenewal
        -LocalDateTime approvalRequestedAt
        -LocalDateTime approvedAt
        -User approvedBy
        -String rejectionReason
        -Long version
        +updateStatus(SubscriptionStatus status)
        +approve(User approver)
        +reject(String reason)
        +suspend(String reason)
        +terminate(String reason)
        +isActive() boolean
        +isPendingApproval() boolean
    }
    
    class SubscriptionStatus {
        <<enumeration>>
        PENDING_APPROVAL
        ACTIVE
        AUTO_APPROVED
        REJECTED
        SUSPENDED
        TERMINATED
        CANCELLED
    }
    
    class SubscriptionApproval {
        -Long subscriptionId
        -User admin
        -SubscriptionStatus fromStatus
        -SubscriptionStatus toStatus
        -String reason
        -ApprovalAction action
        -LocalDateTime processedAt
        -boolean autoApproved
    }
    
    class ApprovalAction {
        <<enumeration>>
        APPROVE
        REJECT
        SUSPEND
        TERMINATE
        REACTIVATE
    }
    
    class AutoApprovalRule {
        -String ruleName
        -Boolean isActive
        -String planIds
        -Boolean verifiedTenantsOnly
        -String paymentMethods
        -BigDecimal maxAmount
        -Integer priority
        +updateActiveStatus(boolean isActive)
        +updatePriority(int priority)
        +updateRule(String ruleName, String planIds, Boolean verifiedTenantsOnly, String paymentMethods, BigDecimal maxAmount)
    }
    
    class SubscriptionPlan {
        -String planId
        -String planName
        -String description
        -BigDecimal monthlyPrice
        -BigDecimal yearlyPrice
        -Integer maxUsers
        -Integer maxSites
        -Boolean isActive
    }
    
    class SubscriptionApprovalService {
        <<interface>>
        +approveSubscription(Long subscriptionId, String reason) SubscriptionDto
        +rejectSubscription(Long subscriptionId, String reason) SubscriptionDto
        +suspendSubscription(Long subscriptionId, String reason) SubscriptionDto
        +terminateSubscription(Long subscriptionId, String reason) SubscriptionDto
        +reactivateSubscription(Long subscriptionId, String reason) SubscriptionDto
        +getPendingApprovals(Pageable pageable) Page~SubscriptionDto~
        +getApprovalHistory(Long subscriptionId) List~SubscriptionApprovalDto~
        +checkAutoApproval(CreateSubscriptionRequest request) boolean
    }
    
    class SubscriptionApprovalServiceImpl {
        -SubscriptionRepository subscriptionRepository
        -SubscriptionApprovalRepository approvalRepository
        -AutoApprovalRuleRepository ruleRepository
        -NotificationService notificationService
        +approveSubscription(Long subscriptionId, String reason) SubscriptionDto
        +rejectSubscription(Long subscriptionId, String reason) SubscriptionDto
        +suspendSubscription(Long subscriptionId, String reason) SubscriptionDto
        +terminateSubscription(Long subscriptionId, String reason) SubscriptionDto
        +reactivateSubscription(Long subscriptionId, String reason) SubscriptionDto
    }
    
    class SubscriptionApprovalController {
        -SubscriptionApprovalService subscriptionApprovalService
        +getPendingApprovals(Pageable pageable) ResponseEntity
        +approveSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity
        +rejectSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity
        +suspendSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity
        +terminateSubscription(Long subscriptionId, ApprovalRequest request) ResponseEntity
        +getApprovalHistory(Long subscriptionId) ResponseEntity
    }
    
    BaseEntity <|-- Subscription
    BaseEntity <|-- SubscriptionApproval
    BaseEntity <|-- AutoApprovalRule
    BaseEntity <|-- SubscriptionPlan
    
    Subscription --> SubscriptionStatus : uses
    SubscriptionApproval --> ApprovalAction : uses
    SubscriptionApproval --> SubscriptionStatus : uses
    
    SubscriptionApprovalService <|.. SubscriptionApprovalServiceImpl
    SubscriptionApprovalController --> SubscriptionApprovalService : uses
```

## 4. Admin Domain 클래스 다이어그램

```mermaid
classDiagram
    class SuperAdminService {
        -TenantRepository tenantRepository
        -UserRepository userRepository
        -BillingRecordRepository billingRecordRepository
        -SubscriptionRepository subscriptionRepository
        -SubscriptionApprovalRepository approvalRepository
        +getDashboardStats() DashboardStatsDto
        +getTenants(String search, SubscriptionStatus status, Pageable pageable) Page~TenantSummaryDto~
        +updateTenantStatus(Long tenantId, SubscriptionStatus status)
        +getBillingStats() BillingStatsDto
        +getApprovalStats() ApprovalStatsDto
        +exportSubscriptionData(SubscriptionStatus status, LocalDateTime startDate, LocalDateTime endDate) List~SubscriptionExportDto~
    }
    
    class SuperAdminController {
        -SuperAdminService superAdminService
        +getDashboardStats() ResponseEntity
        +getTenants(String search, String status, Pageable pageable) ResponseEntity
        +updateTenantStatus(Long tenantId, UpdateTenantStatusRequest request) ResponseEntity
        +getBillingStats() ResponseEntity
        +getApprovalStats() ResponseEntity
        +exportSubscriptions(String status, String startDate, String endDate) ResponseEntity
    }
    
    class DashboardStatsDto {
        -Long totalTenants
        -Long activeTenants
        -Long suspendedTenants
        -Long newTenantsThisMonth
        -Long totalUsers
        -Long newUsersThisMonth
        -BigDecimal totalRevenue
        -BigDecimal monthlyRevenue
        -Long completedPayments
        -Long failedPayments
        -Long pendingPayments
        -String systemStatus
        -Long activeConnections
    }
    
    class TenantSummaryDto {
        -Long id
        -String companyName
        -String businessNo
        -String representativeName
        -Long userCount
        -String subscriptionStatus
        -String planId
        -BigDecimal monthlyAmount
        -LocalDateTime createdAt
        -LocalDateTime approvedAt
        -String approvedBy
        +from(Tenant tenant, long userCount) TenantSummaryDto
    }
    
    class BillingStatsDto {
        -BigDecimal totalRevenue
        -BigDecimal monthlyRevenue
        -BigDecimal dailyRevenue
        -Long totalPayments
        -Long completedPayments
        -Long failedPayments
        -Long pendingPayments
        -List~MonthlyRevenueDto~ monthlyTrends
        -List~FailedPaymentDto~ recentFailedPayments
    }
    
    class ApprovalStatsDto {
        -Long totalSubscriptions
        -Long pendingApprovals
        -Long approvedSubscriptions
        -Long rejectedSubscriptions
        -Long suspendedSubscriptions
        -Long terminatedSubscriptions
        -Long autoApprovedCount
        -Long manualApprovalCount
        -Double autoApprovalRate
        -Double averageProcessingHours
        -Long pendingOverThreeDays
    }
    
    SuperAdminController --> SuperAdminService : uses
    SuperAdminService --> DashboardStatsDto : creates
    SuperAdminService --> TenantSummaryDto : creates
    SuperAdminService --> BillingStatsDto : creates
    SuperAdminService --> ApprovalStatsDto : creates
```

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