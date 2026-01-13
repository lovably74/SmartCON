# Design Document

## Overview

SmartCON Lite 프로젝트의 개발환경을 H2 인메모리 데이터베이스에서 MariaDB로 전환하는 설계입니다. 이 전환을 통해 개발 단계부터 프로덕션과 동일한 데이터베이스 환경을 사용하여 데이터베이스 관련 이슈를 사전에 방지하고, 개발 생산성을 향상시킵니다.

주요 설계 원칙:
- **환경 일관성**: 개발, 테스트, 프로덕션 환경에서 동일한 MariaDB 사용
- **점진적 전환**: 기존 기능을 유지하면서 단계적으로 전환
- **성능 최적화**: 개발환경에 적합한 성능 설정 적용
- **테스트 격리**: Testcontainers를 활용한 독립적인 테스트 환경

## Architecture

### 현재 아키텍처 (AS-IS)
```
개발환경: Spring Boot App → H2 In-Memory DB
테스트환경: Spring Boot App → H2 In-Memory DB (각 테스트)
프로덕션환경: Spring Boot App → MariaDB Server
```

### 목표 아키텍처 (TO-BE)
```
개발환경: Spring Boot App → Local MariaDB Server
테스트환경: Spring Boot App → Testcontainers MariaDB (각 테스트)
프로덕션환경: Spring Boot App → MariaDB Server (기존 유지)
```

### 데이터베이스 설치 구조
```
Local PC
├── MariaDB 10.11 Server (Port 3306)
│   ├── smartcon_dev (개발용 데이터베이스)
│   ├── smartcon_user (개발용 사용자)
│   └── root (관리자 계정)
└── Spring Boot Application
    ├── application-local.yml (MariaDB 설정)
    └── Flyway Migrations (자동 스키마 관리)
```

## Components and Interfaces

### 1. MariaDB 서버 설치 컴포넌트

**MariaDBInstaller**
- 역할: 로컬 PC에 MariaDB 10.11 서버 설치 및 초기 설정
- 기능:
  - Windows 환경에서 MariaDB 설치 패키지 다운로드 및 설치
  - 서비스 등록 및 자동 시작 설정
  - 기본 보안 설정 적용
  - 포트 3306 방화벽 설정

**DatabaseSetup**
- 역할: 개발용 데이터베이스 및 사용자 생성
- 기능:
  - smartcon_dev 데이터베이스 생성 (UTF8MB4 문자셋)
  - smartcon_user 계정 생성 및 권한 부여
  - 개발용 설정 최적화

### 2. Spring Boot 설정 컴포넌트

**ApplicationConfiguration**
- 파일: `application-local.yml`
- 역할: 개발환경 MariaDB 연결 설정
- 주요 설정:
  ```yaml
  spring:
    datasource:
      url: jdbc:mariadb://localhost:3306/smartcon_dev
      username: smartcon_user
      password: smartcon_dev_password
      driver-class-name: org.mariadb.jdbc.Driver
    jpa:
      hibernate:
        ddl-auto: validate
      properties:
        hibernate:
          dialect: org.hibernate.dialect.MariaDBDialect
    flyway:
      enabled: true
  ```

**ConnectionPoolConfiguration**
- 역할: 개발환경에 최적화된 연결 풀 설정
- 설정값:
  - maximum-pool-size: 5 (개발환경 적정 수준)
  - minimum-idle: 2
  - connection-timeout: 30000ms
  - idle-timeout: 300000ms (5분)

### 3. Flyway 마이그레이션 컴포넌트

**MigrationManager**
- 역할: 데이터베이스 스키마 버전 관리
- 기능:
  - 기존 V1, V2, V3 마이그레이션 스크립트 재사용
  - 개발환경에서 자동 마이그레이션 실행
  - 스키마 버전 추적 및 검증

**마이그레이션 스크립트 구조:**
```
src/main/resources/db/migration/
├── V1__Create_initial_schema.sql (기존)
├── V2__Create_indexes.sql (기존)
└── V3__Insert_initial_data.sql (기존)
```

### 4. 테스트 환경 컴포넌트

**TestcontainersConfiguration**
- 역할: 테스트용 MariaDB 컨테이너 관리
- 기능:
  - 각 테스트 클래스마다 독립적인 MariaDB 컨테이너 생성
  - 테스트 완료 후 자동 정리
  - Flyway 마이그레이션 자동 실행

**TestDatabaseInitializer**
- 역할: 테스트 데이터베이스 초기화
- 기능:
  - 테스트용 스키마 생성
  - 테스트 데이터 삽입
  - 테스트 간 데이터 격리

## Data Models

### 데이터베이스 연결 정보 모델
```java
@ConfigurationProperties("spring.datasource")
public class DatabaseProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private HikariProperties hikari;
}
```

### 연결 풀 설정 모델
```java
public class HikariProperties {
    private int maximumPoolSize = 5;
    private int minimumIdle = 2;
    private long connectionTimeout = 30000;
    private long idleTimeout = 300000;
    private long maxLifetime = 1800000;
    private String poolName = "SmartCON-Dev-Pool";
}
```

### Flyway 설정 모델
```java
@ConfigurationProperties("spring.flyway")
public class FlywayProperties {
    private boolean enabled = true;
    private String[] locations = {"classpath:db/migration"};
    private boolean baselineOnMigrate = true;
    private String baselineVersion = "0";
    private boolean validateOnMigrate = true;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Database Connection Reliability
*For any* database connection attempt with valid credentials, the system should successfully establish a connection to the MariaDB server and be able to execute basic operations
**Validates: Requirements 1.2, 2.3**

### Property 2: Migration Script Sequential Execution
*For any* set of Flyway migration scripts, they should be executed in the correct version order (V1 → V2 → V3) regardless of the execution environment
**Validates: Requirements 4.2**

### Property 3: Test Environment Isolation
*For any* test execution, each test should run in a completely isolated MariaDB container that is automatically created and destroyed, ensuring no data contamination between tests
**Validates: Requirements 5.2, 5.4**

### Property 4: API Functionality Preservation
*For any* existing API endpoint and multi-tenant operation, all functionality should work identically in the MariaDB environment as it did in the H2 environment, maintaining data isolation and business logic integrity
**Validates: Requirements 8.2, 8.3**

## Error Handling

### 데이터베이스 연결 오류 처리
- **연결 실패 시나리오**: MariaDB 서버가 중지되었거나 접근 불가능한 경우
- **처리 방법**: 명확한 오류 메시지와 함께 애플리케이션 시작 실패
- **복구 전략**: 연결 재시도 로직 및 헬스체크 엔드포인트 제공

### Flyway 마이그레이션 오류 처리
- **마이그레이션 실패 시나리오**: 스크립트 오류, 권한 부족, 스키마 충돌
- **처리 방법**: 마이그레이션 중단 및 상세 오류 로그 출력
- **복구 전략**: 수동 마이그레이션 복구 가이드 제공

### 테스트 환경 오류 처리
- **컨테이너 생성 실패**: Docker 환경 문제 또는 리소스 부족
- **처리 방법**: 테스트 스킵 및 대체 테스트 환경 안내
- **복구 전략**: Testcontainers 설정 검증 및 Docker 상태 확인

### 성능 관련 오류 처리
- **연결 풀 고갈**: 동시 연결 수 초과
- **처리 방법**: 연결 대기 및 타임아웃 설정
- **복구 전략**: 연결 풀 크기 조정 및 모니터링

## Testing Strategy

### 이중 테스트 접근법
이 프로젝트는 **단위 테스트**와 **속성 기반 테스트**를 모두 활용하여 포괄적인 테스트 커버리지를 제공합니다:

- **단위 테스트**: 특정 예시, 엣지 케이스, 오류 조건 검증
- **속성 기반 테스트**: 모든 입력에 대한 범용 속성 검증
- **통합 테스트**: 실제 MariaDB 환경에서의 전체 시스템 동작 검증

### 단위 테스트 전략
단위 테스트는 다음 영역에 집중합니다:
- MariaDB 설치 및 설정 검증
- 설정 파일 변경 사항 확인
- Flyway 마이그레이션 스크립트 실행 결과
- 데이터베이스 스키마 및 초기 데이터 검증
- 개발 도구 및 스크립트 기능 테스트

### 속성 기반 테스트 전략
속성 기반 테스트는 **jqwik** 라이브러리를 사용하여 구현하며, 다음 설정을 적용합니다:
- **최소 100회 반복 실행**: 무작위 입력을 통한 포괄적 검증
- **태그 형식**: **Feature: dev-environment-mariadb-migration, Property {number}: {property_text}**
- **각 correctness property마다 하나의 속성 기반 테스트 구현**

### 테스트 환경 구성
- **개발 테스트**: 로컬 MariaDB 서버 사용
- **CI/CD 테스트**: Testcontainers MariaDB 컨테이너 사용
- **통합 테스트**: 실제 애플리케이션 시나리오 검증
- **성능 테스트**: 연결 풀 및 쿼리 성능 측정

### 테스트 데이터 관리
- **시드 데이터**: V3 마이그레이션 스크립트를 통한 일관된 테스트 데이터
- **데이터 격리**: 각 테스트 클래스마다 독립적인 데이터베이스 상태
- **정리 전략**: 테스트 완료 후 자동 데이터 정리 및 컨테이너 종료