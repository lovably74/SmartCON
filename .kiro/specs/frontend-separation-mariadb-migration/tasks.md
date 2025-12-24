# Implementation Plan: Frontend Separation and MariaDB Migration

## Overview

이 구현 계획은 SmartCON Lite 프로젝트에서 프로토타입과 별도의 프로덕션 프론트엔드를 분리하고, 백엔드의 H2 인메모리 데이터베이스를 MariaDB로 전환하는 작업을 단계별로 수행합니다. 각 작업은 점진적으로 진행되며, 기존 기능의 호환성을 유지하면서 새로운 아키텍처로 전환합니다.

## Tasks

- [x] 1. 로컬 PC MariaDB 직접 설치 및 설정
  - 로컬 PC에 MariaDB 10.11 직접 설치
  - 로컬 개발용 MariaDB 데이터베이스 및 사용자 생성
  - MariaDB 서비스 시작 및 연결 확인
  - _Requirements: 3.1, 3.2, 3.5_

- [ ]* 1.1 로컬 MariaDB 연결 검증 테스트
  - **Property 15: Test Database Isolation**
  - **Validates: Requirements 7.2**

- [ ] 2. 백엔드 MariaDB 설정 및 의존성 업데이트
  - [x] 2.1 Maven 의존성 업데이트 및 MariaDB 드라이버 추가
    - pom.xml에 MariaDB 의존성 추가
    - Testcontainers MariaDB 의존성 추가
    - _Requirements: 2.1, 7.1_

  - [x] 2.2 환경별 application.yml 설정 파일 생성
    - application-local.yml (로컬 MariaDB 연결)
    - application-dev.yml (개발 환경)
    - application-prod.yml (프로덕션 환경)
    - _Requirements: 2.1, 6.1, 6.2_

  - [ ]* 2.3 MariaDB 연결 설정 검증 테스트
    - **Property 13: Environment-specific Database Connectivity**
    - **Validates: Requirements 6.2**

- [ ] 3. Flyway 마이그레이션 스크립트 작성
  - [x] 3.1 기존 H2 스키마 분석 및 MariaDB 호환 스키마 생성
    - V1__Create_initial_schema.sql 작성
    - 테넌트, 사용자, 역할 테이블 생성
    - _Requirements: 5.1, 5.2_

  - [x] 3.2 인덱스 및 제약조건 마이그레이션 스크립트 작성
    - V2__Create_indexes.sql 작성
    - 성능 최적화를 위한 인덱스 생성
    - _Requirements: 5.2, 5.5_

  - [x] 3.3 초기 데이터 시딩 스크립트 작성
    - V3__Insert_initial_data.sql 작성
    - 개발 및 테스트용 초기 데이터 삽입
    - _Requirements: 5.3_

  - [ ]* 3.4 Flyway 마이그레이션 실행 테스트
    - **Property 6: Flyway Migration Execution**
    - **Validates: Requirements 2.3**

  - [ ]* 3.5 데이터베이스 스키마 완성도 검증 테스트
    - **Property 10: Database Schema Migration Completeness**
    - **Validates: Requirements 5.2**

- [ ] 4. JPA 엔티티 MariaDB 호환성 업데이트
  - [x] 4.1 Base 엔티티 클래스 MariaDB 최적화
    - BaseEntity 및 BaseTenantEntity 수정
    - MariaDB 방언 설정 적용
    - _Requirements: 2.2, 5.4_

  - [x] 4.2 기존 엔티티 MariaDB 호환성 검증 및 수정
    - 모든 도메인 엔티티 MariaDB 테스트
    - 데이터 타입 및 제약조건 검증
    - _Requirements: 2.2, 5.4_

  - [ ]* 4.3 JPA 엔티티 MariaDB 호환성 테스트
    - **Property 5: Entity-MariaDB Compatibility**
    - **Validates: Requirements 2.2**

  - [ ]* 4.4 JPA 엔티티 CRUD 작업 검증 테스트
    - **Property 11: JPA Entity MariaDB Validation**
    - **Validates: Requirements 5.4**

- [ ] 5. 멀티테넌트 데이터 격리 MariaDB 적용
  - [x] 5.1 TenantContext 및 Hibernate Filter MariaDB 호환성 확인
    - 기존 멀티테넌트 로직 MariaDB 테스트
    - 테넌트별 데이터 격리 검증
    - _Requirements: 2.6_

  - [ ]* 5.2 멀티테넌트 데이터 격리 검증 테스트
    - **Property 7: Multi-tenant Data Isolation**
    - **Validates: Requirements 2.6**

  - [ ]* 5.3 데이터베이스 참조 무결성 검증 테스트
    - **Property 12: Database Referential Integrity**
    - **Validates: Requirements 5.5**

- [-] 6. Testcontainers MariaDB 테스트 환경 구성
  - [x] 6.1 Testcontainers MariaDB 설정 클래스 작성
    - MariaDBTestContainer 설정
    - 테스트용 데이터베이스 초기화
    - _Requirements: 7.1, 7.4_

  - [x] 6.2 기존 테스트 MariaDB 호환성 업데이트
    - Repository 테스트 MariaDB 적용
    - Service 테스트 MariaDB 적용
    - _Requirements: 7.3_

  - [ ]* 6.3 기존 테스트 스위트 보존 검증
    - **Property 16: Test Suite Preservation**
    - **Validates: Requirements 7.3**

- [ ] 7. 체크포인트 - 백엔드 MariaDB 전환 완료 검증
  - 모든 백엔드 테스트 통과 확인
  - MariaDB 연결 및 데이터 작업 정상 동작 확인
  - 사용자에게 질문이 있으면 문의

- [ ] 8. 슈퍼관리자용 프론트엔드 디렉토리 구조 생성
  - [ ] 8.1 frontend 디렉토리 생성 및 기본 구조 설정
    - 프로덕션용 frontend/ 디렉토리 생성
    - 기본 디렉토리 구조 (src/, public/, tests/) 생성
    - _Requirements: 1.1, 4.2_

  - [ ] 8.2 프로토타입에서 슈퍼관리자 소스 코드 복사 및 정리
    - prototype/src/pages/super 내용을 frontend/src/pages/super로 복사
    - 슈퍼관리자 관련 컴포넌트 및 훅 복사
    - 불필요한 파일 제거 및 정리
    - _Requirements: 1.1, 4.1_

- [ ] 9. 슈퍼관리자용 프론트엔드 의존성 및 설정 구성
  - [ ] 9.1 package.json 및 기본 설정 파일 생성
    - React 18, TypeScript, Vite 설정
    - Zustand, TanStack Query 의존성 추가
    - 슈퍼관리자 페이지에 필요한 의존성만 포함
    - _Requirements: 1.2, 1.5_

  - [ ] 9.2 Vite 빌드 설정 프로덕션 최적화
    - vite.config.ts 프로덕션 최적화 설정
    - 번들 크기 최적화 및 코드 스플리팅
    - _Requirements: 1.6_

  - [ ]* 9.3 프론트엔드 기술 스택 일관성 검증 테스트
    - **Property 1: Frontend Technology Stack Consistency**
    - **Validates: Requirements 1.2, 1.5**

  - [ ]* 9.4 프론트엔드 프로덕션 빌드 최적화 검증 테스트
    - **Property 4: Frontend Production Build Optimization**
    - **Validates: Requirements 1.6**

- [ ] 10. 슈퍼관리자용 UI 컴포넌트 및 라우팅 설정
  - [ ] 10.1 Shadcn/UI 컴포넌트 라이브러리 설정
    - 슈퍼관리자 페이지에 필요한 UI 컴포넌트 설치 및 설정
    - 컴포넌트 임포트 및 사용 가능성 확인
    - _Requirements: 1.3_

  - [ ] 10.2 슈퍼관리자 라우팅 시스템 구현
    - 슈퍼관리자 전용 라우팅 구조 구현 (/super/dashboard, /super/tenants, /super/billing)
    - 기존 프로토타입과 동일한 라우팅 구조 유지
    - _Requirements: 1.4_

  - [ ]* 10.3 UI 컴포넌트 완성도 검증 테스트
    - **Property 2: Frontend UI Component Completeness**
    - **Validates: Requirements 1.3**

  - [ ]* 10.4 슈퍼관리자 라우팅 보존 검증 테스트
    - **Property 3: Role-based Routing Preservation**
    - **Validates: Requirements 1.4**

- [ ] 11. 슈퍼관리자용 환경별 프론트엔드 설정 구성
  - [ ] 11.1 환경별 설정 파일 및 API 엔드포인트 구성
    - .env.development, .env.staging, .env.production 생성
    - 환경별 API 베이스 URL 설정
    - _Requirements: 6.3_

  - [ ] 11.2 슈퍼관리자 API 클라이언트 환경별 설정 적용
    - 환경에 따른 API 엔드포인트 자동 선택
    - 슈퍼관리자 전용 API 호출 설정
    - _Requirements: 6.3_

  - [ ]* 11.3 프론트엔드 환경 설정 검증 테스트
    - **Property 14: Frontend Environment Configuration**
    - **Validates: Requirements 6.3**

- [ ] 12. 슈퍼관리자용 빌드 및 배포 설정 분리
  - [ ] 12.1 슈퍼관리자 프론트엔드 독립적인 빌드 설정
    - 슈퍼관리자 프론트엔드 전용 빌드 스크립트 작성
    - 백엔드와 독립적인 배포 가능 설정
    - _Requirements: 4.4_

  - [ ] 12.2 백엔드 빌드 설정 검증 및 최적화
    - Maven 빌드 프로세스 독립성 확인
    - 프론트엔드 의존성 제거
    - _Requirements: 4.4_

  - [ ]* 12.3 빌드 설정 독립성 검증 테스트
    - **Property 8: Build Configuration Independence**
    - **Validates: Requirements 4.4**

- [ ] 13. 슈퍼관리자 API 호환성 검증 및 통합 테스트
  - [ ] 13.1 슈퍼관리자 API 엔드포인트 호환성 검증
    - 슈퍼관리자 관련 API 엔드포인트 테스트
    - 요청/응답 형식 호환성 확인
    - _Requirements: 4.5_

  - [ ] 13.2 슈퍼관리자 프론트엔드-백엔드 통합 테스트
    - 새로운 슈퍼관리자 프론트엔드에서 백엔드 API 호출 테스트
    - 인증, 권한, 데이터 처리 통합 검증
    - _Requirements: 4.5_

  - [ ]* 13.3 API 하위 호환성 검증 테스트
    - **Property 9: API Backward Compatibility**
    - **Validates: Requirements 4.5**

- [ ] 14. 체크포인트 - 슈퍼관리자 프론트엔드 분리 완료 검증
  - 새로운 슈퍼관리자 프론트엔드 빌드 및 실행 확인
  - 슈퍼관리자 모든 기능 정상 동작 확인
  - 사용자에게 질문이 있으면 문의

- [ ] 15. 슈퍼관리자 통합 테스트 및 E2E 테스트 실행
  - [ ] 15.1 슈퍼관리자 시스템 통합 테스트
    - 슈퍼관리자 프론트엔드 + 백엔드 + MariaDB 통합 테스트
    - 슈퍼관리자 주요 사용자 플로우 테스트
    - _Requirements: 슈퍼관리자 요구사항 통합 검증_

  - [ ] 15.2 슈퍼관리자 성능 테스트 및 최적화
    - MariaDB 성능 테스트
    - 슈퍼관리자 프론트엔드 빌드 크기 및 로딩 시간 최적화
    - _Requirements: 성능 요구사항 검증_

- [ ] 16. 문서 업데이트 및 배포 가이드 작성
  - [ ] 16.1 README 및 개발 환경 설정 가이드 업데이트
    - 새로운 프로젝트 구조 반영
    - 로컬 PC MariaDB 설치 및 설정 가이드
    - _Requirements: 3.4, 4.3_

  - [ ] 16.2 슈퍼관리자 배포 가이드 및 환경 변수 문서 작성
    - 슈퍼관리자 프론트엔드/백엔드 별도 배포 가이드
    - 환경 변수 설정 문서
    - _Requirements: 6.5_

- [ ] 17. 최종 검증 및 정리
  - 모든 테스트 통과 확인
  - 프로토타입과 새로운 슈퍼관리자 프론트엔드 기능 동등성 확인
  - 사용자에게 질문이 있으면 문의

## Notes

- 작업 중 `*` 표시된 항목은 선택적 테스트 작업으로, 빠른 MVP를 위해 건너뛸 수 있습니다
- 각 작업은 특정 요구사항을 참조하여 추적 가능성을 보장합니다
- 체크포인트에서 점진적 검증을 통해 안정적인 전환을 보장합니다
- Property 테스트는 범용 정확성 속성을 검증합니다
- 기존 기능의 호환성을 유지하면서 새로운 아키텍처로 전환합니다
- **Docker 대신 로컬 PC에 MariaDB를 직접 설치하여 사용합니다**
- **슈퍼관리자 페이지만 우선 작업하여 빠른 검증을 진행합니다**