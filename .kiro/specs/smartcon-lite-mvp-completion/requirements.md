# SmartCON Lite MVP 완성 요구사항 문서

## 소개

SmartCON Lite 프로젝트의 MVP(Minimum Viable Product) 완성을 위한 요구사항 문서입니다. 현재 44% 완성도에서 2025년 2월 15일까지 프로덕션 배포 가능한 수준으로 완성하는 것을 목표로 합니다.

## 용어 정의

- **MVP**: 최소 기능 제품 (Minimum Viable Product)
- **Backend_API**: Spring Boot 기반 백엔드 REST API
- **Frontend_App**: React + TypeScript 기반 웹 애플리케이션
- **Mobile_App**: Capacitor 기반 하이브리드 모바일 애플리케이션
- **JWT_Auth**: JWT 토큰 기반 인증 시스템
- **Multi_Tenant**: 다중 테넌트 SaaS 아키텍처
- **Face_Recognition**: FaceNet 기반 얼굴 인식 출입 관리

## 핵심 요구사항

### 요구사항 1: 백엔드 API 완성

**사용자 스토리:** 개발자로서, 프론트엔드에서 사용할 수 있는 완전한 REST API가 필요하므로, 모든 비즈니스 로직을 백엔드에서 처리하고자 합니다.

#### 승인 기준

1. THE Backend_API SHALL implement all authentication endpoints (/api/auth/login, /api/auth/refresh, /api/auth/logout)
2. WHEN user authentication is requested, THE Backend_API SHALL validate credentials and return JWT tokens
3. THE Backend_API SHALL implement all user management endpoints for CRUD operations
4. THE Backend_API SHALL implement tenant management endpoints with multi-tenant isolation
5. THE Backend_API SHALL implement attendance tracking endpoints with face recognition integration
6. THE Backend_API SHALL implement subscription management endpoints with approval workflow
7. THE Backend_API SHALL implement contract management endpoints with PDF generation
8. THE Backend_API SHALL implement dashboard data endpoints for all user roles
9. THE Backend_API SHALL include comprehensive error handling with standardized error responses
10. THE Backend_API SHALL include API documentation using Swagger/OpenAPI 3.0

### 요구사항 2: JWT 인증 시스템 구현

**사용자 스토리:** 사용자로서, 안전한 로그인과 세션 관리가 필요하므로, JWT 토큰 기반 인증을 통해 보안성을 확보하고자 합니다.

#### 승인 기준

1. THE JWT_Auth SHALL generate access tokens with 1-hour expiration and refresh tokens with 7-day expiration
2. WHEN user logs in successfully, THE JWT_Auth SHALL return both access and refresh tokens
3. THE JWT_Auth SHALL validate tokens on every protected API request
4. THE JWT_Auth SHALL implement token refresh mechanism for seamless user experience
5. THE JWT_Auth SHALL include role-based access control (RBAC) in token claims
6. THE JWT_Auth SHALL implement secure logout with token blacklisting
7. THE JWT_Auth SHALL use RSA256 algorithm for token signing and verification
8. THE JWT_Auth SHALL include tenant_id in token claims for multi-tenant isolation

### 요구사항 3: 프론트엔드 API 연동

**사용자 스토리:** 사용자로서, 실제 데이터로 동작하는 웹 애플리케이션이 필요하므로, 모든 화면이 백엔드 API와 연동되어야 합니다.

#### 승인 기준

1. THE Frontend_App SHALL replace all mock data with actual API calls
2. WHEN user interacts with any feature, THE Frontend_App SHALL communicate with Backend_API
3. THE Frontend_App SHALL implement proper error handling for all API responses
4. THE Frontend_App SHALL implement loading states for all async operations
5. THE Frontend_App SHALL implement JWT token management with automatic refresh
6. THE Frontend_App SHALL implement role-based routing with API-driven permissions
7. THE Frontend_App SHALL implement real-time data updates using polling or WebSocket
8. THE Frontend_App SHALL include comprehensive form validation with server-side validation

### 요구사항 4: 데이터베이스 마이그레이션

**사용자 스토리:** 개발자로서, 프로덕션 환경에서 사용할 수 있는 영구 데이터베이스가 필요하므로, H2에서 MariaDB로 전환하고자 합니다.

#### 승인 기준

1. THE Backend_API SHALL use MariaDB 10.11 instead of H2 in-memory database
2. WHEN database schema is created, THE Backend_API SHALL use Flyway migration scripts
3. THE Backend_API SHALL maintain all existing JPA entity relationships in MariaDB
4. THE Backend_API SHALL implement proper database connection pooling with HikariCP
5. THE Backend_API SHALL include database initialization scripts for development data
6. THE Backend_API SHALL implement database backup and restore procedures
7. THE Backend_API SHALL include database performance optimization with proper indexing
8. THE Backend_API SHALL maintain multi-tenant data isolation using tenant_id filtering

### 요구사항 5: 사업자등록번호 인증 API

**사용자 스토리:** 고객사로서, 구독 신청 시 사업자등록번호 진위 확인이 필요하므로, 국세청 API를 통한 자동 검증을 원합니다.

#### 승인 기준

1. THE Backend_API SHALL integrate with 국세청 사업자등록번호 진위확인 API
2. WHEN business registration number is submitted, THE Backend_API SHALL validate it in real-time
3. THE Backend_API SHALL store validation results with timestamp and status
4. THE Backend_API SHALL handle API failures gracefully with retry mechanism
5. THE Backend_API SHALL implement rate limiting for external API calls
6. THE Backend_API SHALL cache validation results to reduce external API calls
7. THE Backend_API SHALL provide clear error messages for invalid business numbers
8. THE Backend_API SHALL log all validation attempts for audit purposes

### 요구사항 6: 구독 승인 워크플로우

**사용자 스토리:** 슈퍼관리자로서, 구독 신청을 검토하고 승인/거부할 수 있어야 하므로, 서비스 품질 관리가 가능합니다.

#### 승인 기준

1. WHEN tenant submits subscription request, THE Backend_API SHALL create subscription with PENDING_APPROVAL status
2. THE Backend_API SHALL prevent service access for PENDING_APPROVAL subscriptions
3. THE Frontend_App SHALL provide super admin interface for subscription approval/rejection
4. WHEN subscription is approved, THE Backend_API SHALL activate tenant services immediately
5. WHEN subscription is rejected, THE Backend_API SHALL record rejection reason
6. THE Backend_API SHALL send email notifications for subscription status changes
7. THE Frontend_App SHALL display subscription status clearly to tenant users
8. THE Backend_API SHALL maintain audit log for all subscription status changes

### 요구사항 7: 모바일 앱 기본 기능

**사용자 스토리:** 현장 작업자로서, 모바일에서 출입 관리와 기본 기능을 사용할 수 있어야 하므로, 하이브리드 앱이 필요합니다.

#### 승인 기준

1. THE Mobile_App SHALL be built using Capacitor with React + TypeScript
2. THE Mobile_App SHALL implement camera access for face recognition attendance
3. THE Mobile_App SHALL implement geolocation for work site verification
4. THE Mobile_App SHALL implement push notifications for important alerts
5. THE Mobile_App SHALL work offline with local data synchronization
6. THE Mobile_App SHALL implement biometric authentication (fingerprint/face)
7. THE Mobile_App SHALL provide responsive design for various screen sizes
8. THE Mobile_App SHALL include app store deployment configurations

### 요구사항 8: 테스트 코드 작성

**사용자 스토리:** 개발자로서, 안정적인 서비스 운영을 위해 충분한 테스트 커버리지가 필요하므로, 자동화된 테스트를 구축하고자 합니다.

#### 승인 기준

1. THE Backend_API SHALL have unit tests for all service classes with 80%+ coverage
2. THE Backend_API SHALL have integration tests for all REST endpoints
3. THE Backend_API SHALL use Testcontainers for database integration tests
4. THE Frontend_App SHALL have unit tests for all custom hooks and utilities
5. THE Frontend_App SHALL have integration tests for critical user flows
6. THE Frontend_App SHALL have E2E tests using Playwright for main scenarios
7. THE Mobile_App SHALL have unit tests for core functionality
8. ALL tests SHALL pass in CI/CD pipeline before deployment

### 요구사항 9: 배포 및 운영 환경

**사용자 스토리:** 운영자로서, 안정적인 프로덕션 배포와 모니터링이 필요하므로, 완전한 DevOps 파이프라인을 구축하고자 합니다.

#### 승인 기준

1. THE Backend_API SHALL be containerized using Docker with multi-stage builds
2. THE Frontend_App SHALL be built for production with optimized bundle size
3. THE System SHALL include Docker Compose for local development environment
4. THE System SHALL include environment-specific configuration files
5. THE System SHALL implement health check endpoints for monitoring
6. THE System SHALL include logging configuration with structured logs
7. THE System SHALL include database migration scripts for production deployment
8. THE System SHALL include backup and disaster recovery procedures

### 요구사항 10: 인트로 페이지 및 랜딩 시스템

**사용자 스토리:** 방문자로서, 서비스를 소개하고 로그인할 수 있는 인트로 페이지가 필요하므로, 전문적인 서비스 소개와 문의 기능을 원합니다.

#### 승인 기준

1. THE Frontend_App SHALL implement landing page at root path (/) with service introduction
2. WHEN visitor accesses landing page, THE Frontend_App SHALL display main message "안전관리, 이제는 스마트하게!"
3. THE Frontend_App SHALL include top navigation menu with fixed labels (비대면바우처, 스마트콘 소개, 핵심기능, 서비스요금, 문의하기, 로그인)
4. THE Frontend_App SHALL provide inquiry form with required fields (회사명, 담당자성함, 연락처, 이메일, 문의내용)
5. THE Frontend_App SHALL implement responsive design for mobile and desktop inquiry forms
6. THE Frontend_App SHALL include privacy policy agreement checkboxes (필수/선택)
7. THE Frontend_App SHALL provide demo request functionality through inquiry form
8. THE Frontend_App SHALL implement robot verification for form submission

### 요구사항 11: 작업 배정 및 일보 시스템

**사용자 스토리:** 현장관리자로서, 작업을 배정하고 일보를 자동으로 취합할 수 있어야 하므로, 효율적인 현장 운영이 가능합니다.

#### 승인 기준

1. WHEN site manager creates work assignment, THE Backend_API SHALL send request to specific team with work details
2. WHEN team leader receives work request, THE Backend_API SHALL send push notification for immediate response
3. THE Backend_API SHALL allow team leaders to accept or reject work assignments with reasons
4. WHEN work assignment is accepted, THE Backend_API SHALL automatically generate daily work report template
5. THE Backend_API SHALL integrate weather API to automatically populate weather data in work reports
6. THE Backend_API SHALL collect team input data at 5 PM daily for work report aggregation
7. THE Backend_API SHALL allow site managers to review and approve aggregated work reports
8. THE Backend_API SHALL maintain work assignment history and status tracking

### 요구사항 12: 전자계약 시스템

**사용자 스토리:** 노무자로서, 디지털 방식으로 근로계약서에 서명할 수 있어야 하므로, 편리하고 법적 효력이 있는 계약 체결이 가능합니다.

#### 승인 기준

1. WHEN work assignment is confirmed, THE Backend_API SHALL automatically generate labor contract from template
2. THE Backend_API SHALL bind worker and site data to standard labor contract template
3. THE Frontend_App SHALL provide electronic signature pad using Canvas API for contract signing
4. WHEN contract is signed, THE Backend_API SHALL convert signed contract to PDF format
5. THE Backend_API SHALL store PDF files in S3 with tenant isolation and generate hash for integrity
6. THE Backend_API SHALL send contract signing notifications to workers via push and SMS
7. THE Backend_API SHALL track contract status (pending, signed, expired) and send reminders
8. THE Backend_API SHALL provide contract history and download functionality for all parties

### 요구사항 13: FaceNet 안면인식 연동

**사용자 스토리:** 작업자로서, 안면인식을 통해 자동으로 출입을 체크할 수 있어야 하므로, 편리하고 정확한 출역 관리가 가능합니다.

#### 승인 기준

1. THE Backend_API SHALL integrate with FaceNet server for face embedding generation
2. WHEN user uploads face photo, THE Backend_API SHALL send image to FaceNet API for embedding creation
3. THE Backend_API SHALL implement daily batch job at midnight to activate face embeddings for scheduled workers
4. WHEN worker checks in/out, THE Mobile_App SHALL capture face image and send to FaceNet for matching
5. THE Backend_API SHALL validate face matching confidence score (minimum 0.85) before recording attendance
6. THE Backend_API SHALL automatically deactivate face embeddings when work assignment ends
7. THE Backend_API SHALL handle FaceNet API failures gracefully with manual check-in fallback
8. THE Backend_API SHALL log all face recognition attempts for audit and debugging purposes

### 요구사항 14: 딥링크 및 모바일 최적화

**사용자 스토리:** 사용자로서, SMS 초대 링크를 통해 앱으로 직접 이동할 수 있어야 하므로, 원활한 사용자 온보딩이 가능합니다.

#### 승인 기준

1. THE Mobile_App SHALL implement deep linking for SMS invitation links
2. WHEN user clicks SMS link without app installed, THE System SHALL redirect to app store
3. WHEN user clicks SMS link with app installed, THE Mobile_App SHALL open directly to invitation acceptance screen
4. THE Mobile_App SHALL handle deep link parameters for automatic team joining and role assignment
5. THE Frontend_App SHALL implement responsive design for PC (1920px), Tablet (768px), Mobile (375px)
6. THE Frontend_App SHALL follow mobile-first design principles with touch-optimized UI
7. THE Mobile_App SHALL support offline functionality with local data synchronization
8. THE Mobile_App SHALL implement progressive web app (PWA) features for better mobile experience

### 요구사항 15: 다국어 및 접근성

**사용자 스토리:** 사용자로서, 다양한 언어와 접근성 기능이 필요하므로, 포용적인 서비스 이용이 가능합니다.

#### 승인 기준

1. THE Frontend_App SHALL implement i18n internationalization with Korean and English support
2. THE Frontend_App SHALL comply with WCAG 2.1 AA accessibility standards
3. THE Frontend_App SHALL provide keyboard navigation support for all interactive elements
4. THE Frontend_App SHALL implement proper color contrast ratios for text and background
5. THE Frontend_App SHALL provide screen reader support with proper ARIA labels
6. THE Frontend_App SHALL implement touch target minimum size of 44px for mobile devices
7. THE Frontend_App SHALL provide loading states, empty states, and error states with clear messaging
8. THE Frontend_App SHALL support browser zoom up to 200% without horizontal scrolling

### 요구사항 16: 문서화 및 사용자 가이드

**사용자 스토리:** 사용자와 개발자로서, 시스템 사용법과 개발 가이드가 필요하므로, 완전한 문서화를 원합니다.

#### 승인 기준

1. THE System SHALL include comprehensive API documentation with examples
2. THE System SHALL include user manuals for each role (super, hq, site, worker)
3. THE System SHALL include developer setup guide with step-by-step instructions
4. THE System SHALL include deployment guide for production environment
5. THE System SHALL include troubleshooting guide for common issues
6. THE System SHALL include database schema documentation
7. THE System SHALL include security guidelines and best practices
8. THE System SHALL include system architecture documentation with diagrams

## 우선순위 및 일정

### 1주차 (2025.01.06-01.12): 백엔드 API 기반 구축
- JWT 인증 시스템 구현
- 기본 CRUD API 엔드포인트 구현
- MariaDB 마이그레이션 완료

### 2주차 (2025.01.13-01.19): 프론트엔드 API 연동
- 모든 Mock 데이터를 실제 API 호출로 교체
- 인증 플로우 구현
- 에러 처리 및 로딩 상태 구현

### 3주차 (2025.01.20-01.26): 핵심 비즈니스 로직
- 사업자등록번호 인증 API 연동
- 구독 승인 워크플로우 구현
- 출입 관리 기본 기능 구현

### 4주차 (2025.01.27-02.02): 모바일 앱 개발
- Capacitor 설정 및 네이티브 기능 구현
- 카메라, 위치, 푸시 알림 기능
- 오프라인 동기화 구현

### 5주차 (2025.02.03-02.09): 테스트 및 최적화
- 백엔드/프론트엔드 테스트 코드 작성
- 성능 최적화 및 보안 강화
- 배포 환경 구성

### 6주차 (2025.02.10-02.15): 배포 및 문서화
- 프로덕션 배포
- 사용자 가이드 작성
- 최종 테스트 및 버그 수정

## 성공 기준

1. 모든 핵심 기능이 실제 데이터로 동작
2. 백엔드 API 테스트 커버리지 80% 이상
3. 프론트엔드 E2E 테스트 통과
4. 모바일 앱 기본 기능 동작
5. 프로덕션 환경 배포 완료
6. 사용자 가이드 문서 완성

## 위험 요소 및 대응 방안

1. **외부 API 연동 지연**: 사업자등록번호 API 대신 Mock 서비스로 우선 구현
2. **모바일 앱 개발 복잡성**: 핵심 기능만 우선 구현, 고급 기능은 차후 버전으로 연기
3. **테스트 코드 작성 시간 부족**: 핵심 비즈니스 로직 위주로 테스트 우선순위 설정
4. **성능 최적화 시간 부족**: 기본 성능 확보 후 점진적 개선 계획 수립