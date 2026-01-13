# Implementation Plan: Development Environment MariaDB Migration

## Overview

SmartCON Lite 프로젝트의 개발환경을 H2 인메모리 데이터베이스에서 MariaDB로 전환하는 구현 계획입니다. 각 작업은 점진적으로 진행되며, 기존 기능의 호환성을 유지하면서 새로운 데이터베이스 환경으로 안전하게 전환합니다.

## Tasks

- [ ] 1. 로컬 PC MariaDB 서버 설치 및 기본 설정
  - Windows 환경에서 MariaDB 10.11 설치 패키지 다운로드 및 설치
  - MariaDB 서비스 등록 및 자동 시작 설정
  - 기본 보안 설정 적용 (root 패스워드 설정)
  - 포트 3306 방화벽 설정 및 연결 테스트
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 1.1 MariaDB 서버 설치 검증 테스트
  - MariaDB 서비스 상태 확인
  - 포트 3306 수신 대기 상태 확인
  - root 계정 연결 테스트
  - _Requirements: 1.1, 1.3, 1.4_

- [ ] 2. 개발용 데이터베이스 및 사용자 생성
  - smartcon_dev 데이터베이스 생성 (UTF8MB4 문자셋)
  - smartcon_user 계정 생성 및 패스워드 설정
  - smartcon_user에게 smartcon_dev 데이터베이스 모든 권한 부여
  - 권한 설정 검증 (CREATE, SELECT, INSERT, UPDATE, DELETE, INDEX)
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 2.1 데이터베이스 사용자 권한 검증 테스트
  - smartcon_user 계정으로 데이터베이스 연결 테스트
  - 테이블 생성, 수정, 삭제 권한 확인
  - 데이터 CRUD 작업 권한 확인
  - _Requirements: 2.3, 2.4_

- [ ] 3. Spring Boot 개발환경 설정 파일 수정
  - application-local.yml 파일에서 H2 설정 제거
  - MariaDB 연결 정보 추가 (URL, 사용자명, 패스워드, 드라이버)
  - JPA Hibernate 설정을 MariaDB Dialect로 변경
  - ddl-auto를 validate로 설정 (Flyway 사용을 위해)
  - _Requirements: 3.1, 3.2, 3.3_

- [ ] 3.1 HikariCP 연결 풀 개발환경 최적화 설정
  - maximum-pool-size: 5 (개발환경 적정 수준)
  - minimum-idle: 2
  - connection-timeout: 30000ms
  - idle-timeout: 300000ms (5분)
  - pool-name: SmartCON-Dev-Pool
  - _Requirements: 3.4, 6.1_

- [ ] 3.2 Spring Boot MariaDB 연결 설정 검증 테스트
  - **Property 1: Database Connection Reliability**
  - **Validates: Requirements 1.2, 2.3**

- [ ] 4. Flyway 마이그레이션 개발환경 활성화
  - application-local.yml에서 Flyway enabled: true 설정
  - 기존 V1, V2, V3 마이그레이션 스크립트 검증
  - baseline-on-migrate: true 설정으로 기존 데이터베이스 호환성 확보
  - validate-on-migrate: true로 스키마 검증 활성화
  - _Requirements: 4.1, 4.2_

- [ ] 4.1 Flyway 마이그레이션 순서 검증 테스트
  - **Property 2: Migration Script Sequential Execution**
  - **Validates: Requirements 4.2**

- [ ] 4.2 마이그레이션 실행 및 스키마 검증
  - 애플리케이션 시작하여 Flyway 자동 마이그레이션 실행
  - 모든 테이블과 인덱스 생성 확인
  - 초기 시드 데이터 삽입 확인
  - flyway_schema_history 테이블에서 마이그레이션 이력 확인
  - _Requirements: 4.3, 4.4_

- [x] 5. 체크포인트 - 개발환경 MariaDB 전환 완료 검증
  - 애플리케이션 정상 시작 확인
  - H2 관련 설정 완전 제거 확인
  - MariaDB 연결 및 기본 CRUD 작업 테스트
  - 사용자에게 질문이 있으면 문의

- [x] 6. 테스트 환경 Testcontainers MariaDB 설정
  - 테스트용 application-test.yml 파일 생성
  - Testcontainers MariaDB 설정 클래스 작성
  - @Testcontainers 어노테이션을 사용한 테스트 베이스 클래스 생성
  - 테스트 시작 시 MariaDB 컨테이너 자동 생성 설정
  - _Requirements: 5.1, 5.2_

- [x] 6.1 테스트 환경 격리 검증 테스트
  - **Property 3: Test Environment Isolation**
  - **Validates: Requirements 5.2, 5.4**
  - 참고: Docker 환경이 없는 경우 기존 MariaDB 연결 사용

- [ ] 6.2 기존 테스트 클래스 Testcontainers 적용
  - Repository 테스트 클래스에 @Testcontainers 적용
  - Service 테스트 클래스에 MariaDB 컨테이너 설정 적용
  - 테스트 데이터베이스 초기화를 위한 Flyway 자동 실행 설정
  - 테스트 완료 후 컨테이너 자동 정리 확인
  - _Requirements: 5.3, 5.4_

- [ ] 7. 개발환경 성능 최적화 및 로깅 설정
  - SQL 쿼리 로깅 활성화 (show-sql: true, format_sql: true)
  - 쿼리 실행 시간 로깅 설정
  - 개발용 로그 레벨 조정 (DEBUG for com.smartcon)
  - MariaDB 성능 모니터링을 위한 추가 로깅 설정
  - _Requirements: 6.2, 6.3, 6.4_

- [ ] 7.1 개발환경 성능 검증 테스트
  - 애플리케이션 시작 시간 측정
  - 기본 CRUD 작업 성능 측정
  - 연결 풀 동작 확인
  - _Requirements: 6.4_

- [ ] 8. 개발 데이터 관리 도구 및 스크립트 작성
  - 개발용 데이터베이스 초기화 스크립트 작성
  - 테스트 데이터 생성 스크립트 작성
  - 데이터베이스 백업/복원 스크립트 작성
  - 개발자를 위한 데이터 관리 가이드 문서 작성
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [ ] 9. 기존 기능 호환성 검증 및 통합 테스트
  - 모든 기존 API 엔드포인트 MariaDB 환경에서 테스트
  - 멀티테넌트 데이터 격리 기능 검증
  - 인증 및 권한 관리 기능 테스트
  - JWT 토큰 생성 및 검증 기능 테스트
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 9.1 API 기능 보존 검증 테스트
  - **Property 4: API Functionality Preservation**
  - **Validates: Requirements 8.2, 8.3**

- [ ] 9.2 전체 테스트 스위트 MariaDB 환경 실행
  - 모든 단위 테스트 MariaDB 환경에서 실행
  - 모든 통합 테스트 MariaDB 환경에서 실행
  - 테스트 실패 시 원인 분석 및 수정
  - 테스트 커버리지 확인
  - _Requirements: 8.4_

- [ ] 10. H2 관련 설정 완전 제거 및 정리
  - pom.xml에서 H2 의존성 제거 (테스트 스코프 포함)
  - H2 Console 설정 제거
  - H2 관련 모든 설정 파일 정리
  - 코드에서 H2 관련 주석 및 설정 제거
  - _Requirements: 8.1_

- [ ] 11. 문서 업데이트 및 개발 가이드 작성
  - README.md 파일에 MariaDB 설치 및 설정 가이드 추가
  - 개발 환경 설정 가이드 업데이트
  - 트러블슈팅 가이드 작성
  - 데이터베이스 관리 가이드 작성
  - _Requirements: 전체 요구사항 문서화_

- [ ] 12. 최종 검증 및 정리
  - 모든 테스트 통과 확인
  - 개발환경에서 애플리케이션 정상 동작 확인
  - MariaDB 성능 및 안정성 확인
  - 사용자에게 질문이 있으면 문의

## Notes

- 모든 작업이 필수로 설정되어 포괄적인 구현을 보장합니다
- 각 작업은 특정 요구사항을 참조하여 추적 가능성을 보장합니다
- 체크포인트에서 점진적 검증을 통해 안정적인 전환을 보장합니다
- Property 테스트는 범용 정확성 속성을 검증합니다
- 기존 기능의 호환성을 유지하면서 새로운 데이터베이스 환경으로 전환합니다
- **H2 데이터베이스를 완전히 제거하고 MariaDB로 완전 전환합니다**
- **개발환경과 프로덕션환경의 데이터베이스 일관성을 확보합니다**