# Requirements Document

## Introduction

SmartCON Lite 프로젝트의 개발환경에서 H2 인메모리 데이터베이스를 MariaDB로 완전히 전환하여 프로덕션 환경과 동일한 데이터베이스를 사용하도록 합니다. 이를 통해 개발 단계에서부터 프로덕션과 동일한 환경에서 테스트하여 데이터베이스 관련 이슈를 사전에 방지하고, 개발 생산성을 향상시킵니다.

## Glossary

- **Development_Environment**: 로컬 PC에서 개발자가 코드를 작성하고 테스트하는 환경
- **MariaDB_Server**: 로컬 PC에 직접 설치된 MariaDB 10.11 서버
- **Local_Database**: 개발용으로 생성된 MariaDB 데이터베이스 (smartcon_dev)
- **Application_Configuration**: Spring Boot의 application-local.yml 설정 파일
- **Flyway_Migration**: 데이터베이스 스키마 버전 관리 및 마이그레이션 도구
- **Test_Environment**: 단위 테스트 및 통합 테스트 실행 환경
- **Connection_Pool**: 데이터베이스 연결 풀 관리 시스템

## Requirements

### Requirement 1: 로컬 PC MariaDB 서버 설치 및 설정

**User Story:** 개발자로서 로컬 PC에 MariaDB 서버를 설치하고 설정하여, 개발환경에서 프로덕션과 동일한 데이터베이스를 사용하고 싶습니다.

#### Acceptance Criteria

1. WHEN MariaDB 10.11 서버를 로컬 PC에 설치할 때, THE System SHALL 정상적으로 설치되고 서비스가 시작되어야 합니다
2. WHEN MariaDB 서버에 연결을 시도할 때, THE System SHALL root 계정으로 정상 접속이 가능해야 합니다
3. WHEN MariaDB 서버 상태를 확인할 때, THE System SHALL 서비스가 활성 상태로 실행 중이어야 합니다
4. WHEN MariaDB 서버 포트를 확인할 때, THE System SHALL 기본 포트 3306에서 수신 대기 중이어야 합니다

### Requirement 2: 개발용 데이터베이스 및 사용자 생성

**User Story:** 개발자로서 MariaDB에 개발 전용 데이터베이스와 사용자를 생성하여, 보안이 적용된 개발환경을 구축하고 싶습니다.

#### Acceptance Criteria

1. WHEN 개발용 데이터베이스를 생성할 때, THE System SHALL smartcon_dev 데이터베이스가 UTF8MB4 문자셋으로 생성되어야 합니다
2. WHEN 개발용 사용자를 생성할 때, THE System SHALL smartcon_user 계정이 생성되고 적절한 권한이 부여되어야 합니다
3. WHEN 개발용 사용자로 데이터베이스에 접속할 때, THE System SHALL smartcon_dev 데이터베이스에 대한 모든 권한을 가져야 합니다
4. WHEN 데이터베이스 권한을 확인할 때, THE System SHALL 개발용 사용자가 테이블 생성, 수정, 삭제 권한을 가져야 합니다

### Requirement 3: Spring Boot 개발환경 설정 변경

**User Story:** 개발자로서 Spring Boot 애플리케이션이 개발환경에서 MariaDB를 사용하도록 설정을 변경하여, H2 대신 MariaDB로 개발하고 싶습니다.

#### Acceptance Criteria

1. WHEN application-local.yml 파일을 수정할 때, THE Application_Configuration SHALL MariaDB 연결 정보로 업데이트되어야 합니다
2. WHEN 데이터소스 설정을 변경할 때, THE Application_Configuration SHALL MariaDB JDBC URL과 드라이버를 사용해야 합니다
3. WHEN JPA 설정을 변경할 때, THE Application_Configuration SHALL MariaDB Dialect를 사용해야 합니다
4. WHEN 연결 풀 설정을 구성할 때, THE Application_Configuration SHALL 개발환경에 적합한 연결 풀 크기를 설정해야 합니다

### Requirement 4: Flyway 마이그레이션 개발환경 적용

**User Story:** 개발자로서 Flyway 마이그레이션을 개발환경에서 활성화하여, 데이터베이스 스키마를 자동으로 관리하고 싶습니다.

#### Acceptance Criteria

1. WHEN Flyway를 개발환경에서 활성화할 때, THE Flyway_Migration SHALL 애플리케이션 시작 시 자동으로 실행되어야 합니다
2. WHEN 마이그레이션 스크립트를 실행할 때, THE Flyway_Migration SHALL 기존 V1, V2, V3 스크립트를 순차적으로 적용해야 합니다
3. WHEN 마이그레이션 완료 후 스키마를 확인할 때, THE Local_Database SHALL 모든 테이블과 인덱스가 정상 생성되어야 합니다
4. WHEN 초기 데이터를 확인할 때, THE Local_Database SHALL 개발용 시드 데이터가 삽입되어야 합니다

### Requirement 5: 테스트 환경 MariaDB 전환

**User Story:** 개발자로서 단위 테스트와 통합 테스트에서도 MariaDB를 사용하여, 개발과 테스트 환경의 일관성을 유지하고 싶습니다.

#### Acceptance Criteria

1. WHEN 테스트 프로파일을 설정할 때, THE Test_Environment SHALL Testcontainers MariaDB를 사용해야 합니다
2. WHEN 통합 테스트를 실행할 때, THE Test_Environment SHALL 각 테스트마다 독립적인 MariaDB 컨테이너를 생성해야 합니다
3. WHEN 테스트 데이터베이스를 초기화할 때, THE Test_Environment SHALL Flyway 마이그레이션을 자동으로 실행해야 합니다
4. WHEN 테스트 완료 후 정리할 때, THE Test_Environment SHALL 테스트 컨테이너를 자동으로 종료하고 정리해야 합니다

### Requirement 6: 개발환경 성능 최적화

**User Story:** 개발자로서 MariaDB 개발환경에서 최적의 성능을 얻어, 빠른 개발 사이클을 유지하고 싶습니다.

#### Acceptance Criteria

1. WHEN 연결 풀을 설정할 때, THE Connection_Pool SHALL 개발환경에 적합한 최소/최대 연결 수를 설정해야 합니다
2. WHEN 쿼리 로깅을 설정할 때, THE Development_Environment SHALL SQL 쿼리와 실행 시간을 로그로 출력해야 합니다
3. WHEN 데이터베이스 인덱스를 확인할 때, THE Local_Database SHALL 성능 최적화를 위한 인덱스가 적용되어야 합니다
4. WHEN 애플리케이션 시작 시간을 측정할 때, THE Development_Environment SHALL 합리적인 시작 시간을 유지해야 합니다

### Requirement 7: 개발 데이터 관리

**User Story:** 개발자로서 개발용 테스트 데이터를 쉽게 관리하여, 다양한 시나리오를 테스트하고 싶습니다.

#### Acceptance Criteria

1. WHEN 개발용 시드 데이터를 생성할 때, THE Local_Database SHALL 테스트에 필요한 기본 데이터가 삽입되어야 합니다
2. WHEN 데이터베이스를 초기화할 때, THE Development_Environment SHALL 스크립트를 통해 쉽게 데이터를 재설정할 수 있어야 합니다
3. WHEN 데이터 백업을 생성할 때, THE Local_Database SHALL 개발 데이터를 SQL 파일로 내보낼 수 있어야 합니다
4. WHEN 데이터를 복원할 때, THE Local_Database SHALL 백업된 SQL 파일을 통해 데이터를 복원할 수 있어야 합니다

### Requirement 8: 환경 전환 검증

**User Story:** 개발자로서 H2에서 MariaDB로의 전환이 성공적으로 완료되었는지 검증하여, 기존 기능이 정상 동작함을 확인하고 싶습니다.

#### Acceptance Criteria

1. WHEN 애플리케이션을 시작할 때, THE Development_Environment SHALL MariaDB 연결 없이 H2 연결 시도를 하지 않아야 합니다
2. WHEN 기존 API 엔드포인트를 테스트할 때, THE Development_Environment SHALL 모든 기능이 MariaDB에서 정상 동작해야 합니다
3. WHEN 멀티테넌트 기능을 테스트할 때, THE Development_Environment SHALL 테넌트별 데이터 격리가 정상 동작해야 합니다
4. WHEN 전체 테스트 스위트를 실행할 때, THE Test_Environment SHALL 모든 테스트가 MariaDB 환경에서 통과해야 합니다