# Requirements Document

## Introduction

SmartCON Lite 역할 기반 시스템은 건설 현장의 인력 관리를 위한 SaaS 플랫폼입니다. 
변경된 요구사항에 따라 5단계 사용자 역할(본사관리자, 현장관리자, 노무팀장, 일반노무자, 슈퍼관리자)별로 
최적화된 기능을 제공하며, 모바일 우선 설계와 안면인식 기반 출역 관리를 핵심으로 합니다.

## Glossary

- **System**: SmartCON Lite 5단계 역할 기반 시스템
- **User**: 시스템을 사용하는 모든 사용자
- **Super_Admin**: 슈퍼관리자 (시스템 전체 관리, 구독 승인, 사업자번호 로그인)
- **HQ_Admin**: 본사관리자 (회사 전체 관리, 사업자번호 + 비밀번호 로그인)
- **Site_Manager**: 현장관리자 (현장별 관리, 사업자번호 + 비밀번호 로그인)
- **Team_Leader**: 노무팀장 (팀 단위 관리, CI값 기반 소셜 로그인)
- **Worker**: 일반노무자 (개인 정보 관리, CI값 기반 소셜 로그인)
- **Business_Login**: 사업자번호 + 비밀번호 로그인 (관리자용)
- **Social_Login**: 카카오, 네이버 소셜 로그인 (개인사용자용)
- **CI_Value**: 휴대폰 인증을 통한 연계정보 고유 키값 (개인 식별용)
- **Face_Recognition**: FaceNet 기반 얼굴인식 시스템
- **Multi_Platform**: PC웹, 모바일웹, 모바일앱(Android/iOS) 통합 지원
- **Project**: 건설 현장 프로젝트
- **Attendance**: 출역 관리
- **Contract**: 근로계약서
- **Dashboard**: 역할별 대시보드
- **Tenant**: 멀티테넌트 SaaS 구조의 회사 단위

## Requirements

### Requirement 1: 통합 로그인 시스템

**User Story:** As a user, I want to access the system through unified login methods based on my role type, so that I can securely authenticate with appropriate login method for my position.

#### Acceptance Criteria

1. THE System SHALL provide a unified login interface with two distinct login paths: "개인사용자" and "관리자"
2. WHEN a user selects "개인사용자", THE System SHALL provide social login options (Kakao, Naver) for Team Leaders and Workers
3. WHEN a user selects "관리자", THE System SHALL provide business number and password login for Super Admin, HQ Admin, and Site Manager
4. WHEN a user performs first-time social login, THE System SHALL require phone number verification to generate CI value
5. WHEN phone verification is completed, THE System SHALL generate a unique CI value as personal identifier
6. IF CI value does not exist in system, THE System SHALL create new user account with social login information
7. IF CI value exists, THE System SHALL link social account to existing user profile
8. WHEN business number and password are entered, THE System SHALL validate against registered business information
9. WHEN business login is successful, THE System SHALL determine user role (Super Admin, HQ Admin, Site Manager) based on business registration
10. IF user has multiple role assignments, THE System SHALL provide role selection interface
11. IF user is Site Manager with multiple site assignments, THE System SHALL provide site selection interface
12. THE System SHALL display site list ordered by: recent access, active assignments, construction timeline
13. THE System SHALL provide site filtering options: "전체현장", "진행중현장", "완료현장"
14. THE System SHALL provide site search functionality by name and location

### Requirement 2: 내정보 관리

**User Story:** As a user, I want to manage my personal information and connected social accounts, so that I can keep my profile updated and secure.

#### Acceptance Criteria

1. THE System SHALL allow users to manage personal information: name, phone number, email, salary account, photo, resident number, emergency contact, home address
2. THE System SHALL display connected social account information
3. THE System SHALL allow users to add additional social accounts
4. THE System SHALL validate and verify account information changes

### Requirement 3: 화면 UI 지원

**User Story:** As a user, I want to access the system across different platforms, so that I can use it on my preferred device.

#### Acceptance Criteria

1. THE System SHALL support PC web interface
2. THE System SHALL support mobile web interface  
3. THE System SHALL support mobile app for Android and iOS platforms
4. THE System SHALL provide responsive design across all platforms
5. THE System SHALL maintain consistent functionality across platforms

### Requirement 4: 슈퍼관리자 대시보드

**User Story:** As a Super Admin, I want to view system-wide statistics and manage all tenants, so that I can monitor overall platform health and business performance.

#### Acceptance Criteria

1. THE System SHALL display total number of active tenants (companies)
2. THE System SHALL display total number of active subscriptions
3. THE System SHALL display total number of registered users across all tenants
4. THE System SHALL display monthly revenue and billing statistics
5. THE System SHALL display system performance metrics and uptime status
6. THE System SHALL provide tenant growth trends over time
7. THE System SHALL display pending subscription approvals requiring attention
8. THE System SHALL provide quick access to critical system management functions

### Requirement 5: 슈퍼관리자 구독 승인 관리

**User Story:** As a Super Admin, I want to manage subscription approvals, so that I can control platform access and ensure quality tenants.

#### Acceptance Criteria

1. THE System SHALL display list of pending subscription approvals with company information
2. THE System SHALL provide detailed tenant information for approval decisions
3. THE System SHALL allow approval or rejection of subscription requests with reason tracking
4. THE System SHALL send automated notifications to tenants upon approval/rejection
5. THE System SHALL maintain complete audit trail of all approval decisions
6. THE System SHALL provide bulk approval functionality for qualified requests
7. THE System SHALL support auto-approval rules based on predefined criteria

### Requirement 6: 슈퍼관리자 테넌트 관리

**User Story:** As a Super Admin, I want to manage all tenant accounts, so that I can maintain platform integrity and provide support.

#### Acceptance Criteria

1. THE System SHALL display comprehensive tenant list with subscription status
2. THE System SHALL allow viewing detailed tenant information and usage statistics
3. THE System SHALL provide tenant account suspension and reactivation capabilities
4. THE System SHALL track tenant billing history and payment status
5. THE System SHALL provide tenant support tools and communication features
6. THE System SHALL allow modification of tenant subscription plans and pricing
7. THE System SHALL generate tenant activity and usage reports

### Requirement 7: 본사관리자 대시보드

**User Story:** As a HQ administrator, I want to view overall company statistics, so that I can monitor business performance and make informed decisions.

#### Acceptance Criteria

1. THE System SHALL display count of active projects
2. THE System SHALL display count of registered active workers
3. THE System SHALL display count of unsigned contracts across all sites
4. THE System SHALL provide monthly attendance status bar chart
5. THE System SHALL provide worker distribution by job type bar chart

### Requirement 8: 본사관리자 프로젝트 현황

**User Story:** As a HQ administrator, I want to manage all company projects, so that I can oversee project lifecycle and resource allocation.

#### Acceptance Criteria

1. THE System SHALL allow creation of new projects with: site name, construction period, site manager, face recognition device, site managers (multiple selection)
2. WHEN creating new site manager, THE System SHALL allow input of name, phone, email for invitation
3. THE System SHALL display project list with: site name, construction period, site manager, status, attendance count, registration date, face recognition device count
4. THE System SHALL provide site name search functionality
5. THE System SHALL provide filtering by "전체현장", "진행중현장", "완료현장"
6. THE System SHALL allow viewing and editing detailed project information

### Requirement 9: 본사관리자 출역현황

**User Story:** As a HQ administrator, I want to view attendance status across all sites, so that I can monitor workforce utilization.

#### Acceptance Criteria

1. THE System SHALL display attendance status for all sites
2. THE System SHALL show: project name, worker name, check-in time, check-out time, work hours, unit price, changes and reasons

### Requirement 7: 본사관리자 근로계약현황

**User Story:** As a HQ administrator, I want to monitor contract status across projects, so that I can ensure legal compliance.

#### Acceptance Criteria

1. THE System SHALL display contract list by project: project name, work date, worker name, worker contact, contract signature status, contract view
2. THE System SHALL provide project-based filtering
3. THE System SHALL provide monthly and daily inquiry functionality

### Requirement 8: 본사관리자 노무자현황

**User Story:** As a HQ administrator, I want to view all worker information, so that I can manage human resources effectively.

#### Acceptance Criteria

1. THE System SHALL display worker list with: worker name, contact, number of work sites, total work days, contract count, first work date, last work date

### Requirement 9: 본사관리자 관리자관리

**User Story:** As a HQ administrator, I want to manage other administrators, so that I can control system access and permissions.

#### Acceptance Criteria

1. THE System SHALL display HQ administrator list: name, phone, email, position, status (employed/resigned/on leave), hire date, resignation date
2. THE System SHALL allow inviting new HQ administrators with name, phone, email information

### Requirement 10: 본사관리자 구독관리

**User Story:** As a HQ administrator, I want to manage subscription and billing, so that I can maintain service continuity.

#### Acceptance Criteria

1. THE System SHALL provide company information management
2. THE System SHALL provide billing contact management
3. THE System SHALL provide invoice and bill inquiry
4. THE System SHALL provide payment history inquiry

### Requirement 11: 현장관리자 대시보드

**User Story:** As a site manager, I want to view site-specific statistics, so that I can monitor daily operations effectively.

#### Acceptance Criteria

1. THE System SHALL display count of new workers for today
2. THE System SHALL display count of total workers for today
3. THE System SHALL display count of unsigned contracts
4. THE System SHALL provide daily attendance status bar chart (recent 1 month)
5. THE System SHALL provide worker distribution by job type bar chart (recent 1 month)

### Requirement 12: 현장관리자 프로젝트 개요

**User Story:** As a site manager, I want to view project overview, so that I can understand current project status.

#### Acceptance Criteria

1. THE System SHALL display project overview: site name, construction period, site manager, status, attendance count, registration date, face recognition device count

### Requirement 13: 현장관리자 출역현황

**User Story:** As a site manager, I want to monitor attendance status, so that I can track worker productivity and manage schedules.

#### Acceptance Criteria

1. THE System SHALL display attendance status: worker name, team, check-in time, check-out time, work hours, unit price, changes and reasons
2. THE System SHALL provide search by period, name, job type, team
3. THE System SHALL allow modification of attendance data with reason tracking

### Requirement 14: 현장관리자 근로계약현황

**User Story:** As a site manager, I want to manage worker contracts, so that I can ensure legal compliance and proper documentation.

#### Acceptance Criteria

1. THE System SHALL display contract list: work date, worker name, worker contact, contract signature status, contract view
2. THE System SHALL provide monthly and daily inquiry functionality
3. THE System SHALL allow confirmation and resending of modification requests (job type and unit price only)
4. THE System SHALL reflect changes from attendance status modifications

### Requirement 15: 현장관리자 노무자현황

**User Story:** As a site manager, I want to manage worker information, so that I can maintain accurate worker records and pricing.

#### Acceptance Criteria

1. THE System SHALL display worker list: worker name, contact, job type, base unit price, total work days, contract count, first work date, last work date
2. THE System SHALL allow site managers to modify base unit prices
3. THE System SHALL apply base unit price to attendance status unit price
4. THE System SHALL apply job type changes to attendance status

### Requirement 16: 현장관리자 신규출역자 확인

**User Story:** As a site manager, I want to approve new workers, so that I can control site access and maintain security.

#### Acceptance Criteria

1. THE System SHALL provide new worker approval management: name, team, job type, contact, unit price
2. THE System SHALL allow site managers to modify unit price before approval
3. THE System SHALL require approval before worker can access site

### Requirement 17: 노무팀장 대시보드

**User Story:** As a team leader, I want to view team statistics, so that I can manage my team effectively.

#### Acceptance Criteria

1. THE System SHALL display team member count
2. THE System SHALL display today's attendance count
3. THE System SHALL display recent 1 week attendance status
4. THE System SHALL display team member status
5. THE System SHALL display worker distribution by job type

### Requirement 18: 노무팀장 프로젝트 개요

**User Story:** As a team leader, I want to view project information, so that I can understand work context.

#### Acceptance Criteria

1. THE System SHALL display project overview: site name, construction period, site manager, status, attendance count, registration date, face recognition device count

### Requirement 19: 노무팀장 출역조회

**User Story:** As a team leader, I want to view attendance records, so that I can track team performance.

#### Acceptance Criteria

1. THE System SHALL display attendance status: worker name, project, check-in time, check-out time, work hours
2. THE System SHALL provide search by period, name, job type, project

### Requirement 20: 노무팀장 근로계약현황

**User Story:** As a team leader, I want to monitor contract status, so that I can ensure team compliance.

#### Acceptance Criteria

1. THE System SHALL display contract list: work date, worker name, worker contact, contract signature status
2. THE System SHALL provide monthly and daily inquiry functionality
3. THE System SHALL provide signature request functionality for unsigned contracts

### Requirement 21: 노무팀장 노무자현황

**User Story:** As a team leader, I want to view team member information, so that I can manage team composition.

#### Acceptance Criteria

1. THE System SHALL display team member list: worker name, contact, total work days, contract count, first work date, last work date

### Requirement 22: 일반노무자 대시보드

**User Story:** As a worker, I want to view my work statistics, so that I can track my performance and earnings.

#### Acceptance Criteria

1. THE System SHALL display team member count
2. THE System SHALL display today's attendance count
3. THE System SHALL display recent 1 week attendance status
4. THE System SHALL display team member status
5. THE System SHALL display worker distribution by job type

### Requirement 23: 일반노무자 프로젝트 개요

**User Story:** As a worker, I want to view project information, so that I can understand my work environment.

#### Acceptance Criteria

1. THE System SHALL display project overview: site name, construction period, site manager, status, attendance count, registration date, face recognition device count

### Requirement 24: 일반노무자 출역조회

**User Story:** As a worker, I want to view my attendance records, so that I can track my work history and earnings.

#### Acceptance Criteria

1. THE System SHALL display attendance status: project, check-in time, check-out time, work hours
2. THE System SHALL provide search by period, name, job type, project

### Requirement 25: 일반노무자 근로계약현황

**User Story:** As a worker, I want to manage my contracts, so that I can ensure proper documentation and request changes when needed.

#### Acceptance Criteria

1. THE System SHALL display contract list: work date, project name, unit price, job type, contract signature status
2. THE System SHALL provide monthly and daily inquiry functionality
3. THE System SHALL provide contract modification request functionality with memo format

### Requirement 26: 일반노무자 안면인식 등록

**User Story:** As a worker, I want to register my face for attendance tracking, so that I can use automated check-in/check-out system.

#### Acceptance Criteria

1. THE System SHALL utilize face information registered in basic personal information
2. THE System SHALL provide site-specific face information modification functionality
3. THE System SHALL ensure face recognition data is properly synchronized with attendance devices

### Requirement 27: 시스템 보안 및 인증

**User Story:** As a system administrator, I want to ensure secure authentication and data protection, so that user information remains safe.

#### Acceptance Criteria

1. WHEN business number and password authentication fails 5 times, THE System SHALL lock account for 30 minutes
2. THE System SHALL log all authentication attempts
3. THE System SHALL enforce HTTPS for all communications
4. THE System SHALL encrypt sensitive personal information
5. THE System SHALL implement role-based access control
6. THE System SHALL validate CI values for user identification
7. THE System SHALL secure social login integration with proper OAuth2 implementation

### Requirement 28: 모바일 앱 지원

**User Story:** As a mobile user, I want native app features, so that I can have optimal mobile experience.

#### Acceptance Criteria

1. THE System SHALL support Android mobile app
2. THE System SHALL support iOS mobile app  
3. THE System SHALL provide camera access for face registration
4. THE System SHALL provide GPS location services
5. THE System SHALL support push notifications
6. THE System SHALL maintain offline capability for critical functions
7. THE System SHALL synchronize data when connection is restored

### Requirement 29: 데이터 동기화 및 백업

**User Story:** As a system administrator, I want reliable data management, so that information is always available and consistent.

#### Acceptance Criteria

1. THE System SHALL synchronize face recognition data with attendance devices
2. THE System SHALL backup user data regularly
3. THE System SHALL maintain data consistency across multiple sites
4. THE System SHALL provide data recovery mechanisms
5. THE System SHALL log all data modification activities

### Requirement 30: 성능 및 확장성

**User Story:** As a system user, I want fast and reliable system performance, so that I can work efficiently without delays.

#### Acceptance Criteria

1. THE System SHALL respond to user requests within 2 seconds under normal load
2. THE System SHALL support concurrent access by multiple users
3. THE System SHALL handle peak usage during check-in/check-out times
4. THE System SHALL scale to support growing number of sites and users
5. THE System SHALL maintain 99.9% uptime availability
6. THE System SHALL optimize mobile app performance for various device specifications