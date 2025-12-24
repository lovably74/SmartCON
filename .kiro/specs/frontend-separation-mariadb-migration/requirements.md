# Requirements Document

## Introduction

SmartCON Lite 프로젝트에서 현재 프로토타입으로 사용되고 있는 React 애플리케이션을 별도의 프로덕션 프론트엔드로 분리하고, 백엔드에서 H2 인메모리 데이터베이스를 MariaDB로 전환하는 작업입니다.

## Glossary

- **Frontend**: 별도로 분리될 프로덕션용 React 애플리케이션
- **Backend**: Spring Boot 기반 백엔드 애플리케이션
- **Prototype**: 현재 존재하는 프로토타입 React 애플리케이션
- **MariaDB**: 프로덕션용 관계형 데이터베이스 시스템
- **H2_Database**: 현재 사용 중인 인메모리 데이터베이스

## Requirements

### Requirement 1: 프론트엔드 분리

**User Story:** 개발자로서, 프로토타입과 별도의 프로덕션 프론트엔드를 가지고 싶어서, 유지보수성과 확장성을 향상시키고자 합니다.

#### Acceptance Criteria

1. THE Frontend SHALL be created as a separate directory structure from the prototype
2. WHEN the frontend is created, THE Frontend SHALL use the same technology stack as the prototype (React 18 + TypeScript + Vite)
3. THE Frontend SHALL include all UI components from Shadcn/UI library
4. THE Frontend SHALL implement the same role-based routing structure as the prototype
5. THE Frontend SHALL use Zustand for state management and TanStack Query for server state
6. THE Frontend SHALL be configured for production deployment with proper build optimization

### Requirement 2: 백엔드 MariaDB 전환

**User Story:** 개발자로서, H2 인메모리 데이터베이스를 MariaDB로 전환하고 싶어서, 프로덕션 환경에서 데이터 영속성과 성능을 확보하고자 합니다.

#### Acceptance Criteria

1. THE Backend SHALL be configured to use MariaDB instead of H2 database
2. WHEN MariaDB is configured, THE Backend SHALL maintain all existing entity relationships and constraints
3. THE Backend SHALL include Flyway migration scripts for database schema management
4. THE Backend SHALL support local MariaDB installation for development
5. THE Backend SHALL include proper connection pooling configuration for MariaDB
6. THE Backend SHALL maintain multi-tenant data isolation with MariaDB

### Requirement 3: 로컬 개발 환경 설정

**User Story:** 개발자로서, 로컬 PC에 MariaDB를 설치하고 설정하고 싶어서, 개발 환경에서 프로덕션과 동일한 데이터베이스를 사용하고자 합니다.

#### Acceptance Criteria

1. THE System SHALL provide Docker Compose configuration for local MariaDB setup
2. WHEN MariaDB is installed locally, THE System SHALL create appropriate databases and users
3. THE System SHALL include initialization scripts for local development data
4. THE System SHALL provide clear documentation for local MariaDB installation and configuration
5. THE Backend SHALL connect to local MariaDB with proper credentials and connection settings

### Requirement 4: 프로젝트 구조 재구성

**User Story:** 개발자로서, 프로젝트 구조를 명확하게 분리하고 싶어서, 프론트엔드와 백엔드의 독립적인 개발과 배포를 가능하게 하고자 합니다.

#### Acceptance Criteria

1. THE Project SHALL maintain the prototype directory for reference and testing
2. THE Project SHALL create a new frontend directory with production-ready structure
3. THE Project SHALL update documentation to reflect the new project structure
4. THE Project SHALL include separate build and deployment configurations for frontend and backend
5. THE Project SHALL maintain backward compatibility with existing API endpoints

### Requirement 5: 데이터 마이그레이션 및 초기화

**User Story:** 개발자로서, 기존 H2 데이터베이스의 스키마와 초기 데이터를 MariaDB로 마이그레이션하고 싶어서, 데이터 손실 없이 데이터베이스를 전환하고자 합니다.

#### Acceptance Criteria

1. THE System SHALL create Flyway migration scripts based on existing H2 schema
2. WHEN migrations are executed, THE System SHALL create all necessary tables and indexes
3. THE System SHALL include initial data seeding for development and testing
4. THE System SHALL validate that all existing JPA entities work correctly with MariaDB
5. THE System SHALL maintain referential integrity and constraints in MariaDB

### Requirement 6: 환경 설정 및 구성

**User Story:** 개발자로서, 개발, 스테이징, 프로덕션 환경별로 적절한 설정을 가지고 싶어서, 환경에 따른 유연한 배포와 관리를 하고자 합니다.

#### Acceptance Criteria

1. THE Backend SHALL include separate configuration files for different environments
2. WHEN environment configurations are set, THE Backend SHALL use appropriate database connections
3. THE Frontend SHALL include environment-specific API endpoint configurations
4. THE System SHALL include Docker configurations for containerized deployment
5. THE System SHALL provide clear environment variable documentation

### Requirement 7: 테스트 환경 구성

**User Story:** 개발자로서, MariaDB를 사용하는 테스트 환경을 구성하고 싶어서, 실제 데이터베이스와 동일한 환경에서 테스트를 수행하고자 합니다.

#### Acceptance Criteria

1. THE Backend SHALL use Testcontainers for MariaDB integration tests
2. WHEN tests are executed, THE System SHALL use isolated MariaDB instances
3. THE Backend SHALL maintain all existing unit and integration tests
4. THE Backend SHALL include database-specific test configurations
5. THE System SHALL provide test data fixtures for MariaDB testing