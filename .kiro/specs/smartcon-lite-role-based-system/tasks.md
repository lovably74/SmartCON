# Implementation Plan: SmartCON Lite 역할 기반 시스템

## Overview

SmartCON Lite 역할 기반 시스템의 구현 계획입니다. 멀티테넌트 아키텍처 기반의 SaaS 플랫폼으로, 
5단계 사용자 역할별 최적화된 기능을 제공합니다. 백엔드는 Java 17 + Spring Boot, 
프론트엔드는 React + TypeScript + Capacitor로 구현됩니다.

## Tasks

- [ ] 1. 프로젝트 구조 및 핵심 인프라 설정
  - 멀티테넌트 아키텍처 기반 프로젝트 구조 생성
  - Spring Boot 백엔드 및 React 프론트엔드 초기 설정
  - MariaDB 멀티테넌트 스키마 및 기본 엔티티 구성
  - JWT 기반 인증 시스템 기본 구조 설정
  - _Requirements: 1.1, 3.1, 3.2, 3.3_

- [ ] 1.1 프로젝트 구조 설정 테스트
  - 프로젝트 빌드 및 기본 구조 검증 테스트
  - _Requirements: 1.1_

- [ ] 2. 멀티테넌트 기반 사용자 관리 시스템 구현
  - [ ] 2.1 기본 엔티티 및 멀티테넌트 구조 구현
    - User, Tenant, BaseTenantEntity 엔티티 구현
    - 테넌트 컨텍스트 관리 및 자동 필터링 구현
    - _Requirements: 1.4, 1.5, 1.6_

  - [ ] 2.2 CI 값 생성 및 계정 관리 속성 테스트
    - **Property 2: CI Value Uniqueness and Account Management**
    - **Validates: Requirements 1.4, 1.5, 1.6**

  - [ ] 2.3 개인정보 관리 기능 구현
    - PersonalInfo, BankAccount 임베디드 엔티티 구현
    - 개인정보 암호화 및 마스킹 처리
    - _Requirements: 2.1, 2.4_

  - [ ] 2.4 개인정보 관리 속성 테스트
    - **Property 7: Personal Information Management**
    - **Validates: Requirements 2.1, 2.4**

- [ ] 3. 인증 및 소셜 로그인 시스템 구현
  - [ ] 3.1 소셜 로그인 통합 구현
    - Kakao, Naver OAuth2 클라이언트 구현
    - SocialAccount 엔티티 및 연동 로직 구현
    - _Requirements: 1.2, 1.3, 2.2, 2.3_

  - [ ] 3.2 소셜 로그인 플로우 속성 테스트
    - **Property 1: Social Login Flow Consistency**
    - **Validates: Requirements 1.2, 1.3**

  - [ ] 3.3 소셜 계정 연동 속성 테스트
    - **Property 8: Social Account Linking**
    - **Validates: Requirements 2.2, 2.3**

  - [ ] 3.4 사업자 로그인 및 2차 인증 구현
    - 사업자번호 + 비밀번호 인증 로직 구현
    - 이메일 기반 2차 인증 시스템 구현
    - _Requirements: 1.7, 1.8, 1.9_

  - [ ] 3.5 사업자 로그인 및 2FA 속성 테스트
    - **Property 3: Business Login and 2FA Flow**
    - **Validates: Requirements 1.7, 1.8, 1.9**

- [ ] 4. 역할 기반 접근 제어 시스템 구현
  - [ ] 4.1 역할 및 권한 관리 구현
    - Role enum 및 권한 체크 로직 구현
    - Spring Security 기반 역할별 접근 제어 구현
    - _Requirements: 1.9, 1.10, 27.5_

  - [ ] 4.2 역할 기반 접근 제어 속성 테스트
    - **Property 14: Role-Based Access Control**
    - **Validates: Requirements 27.5**

  - [ ] 4.3 다중 역할 및 현장 선택 로직 구현
    - 사용자 다중 역할 처리 로직 구현
    - 현장별 역할 매핑 및 선택 인터페이스 구현
    - _Requirements: 1.10, 1.11_

  - [ ] 4.4 역할 및 현장 선택 속성 테스트
    - **Property 4: Role and Site Selection Logic**
    - **Validates: Requirements 1.10, 1.11**

- [ ] 5. 프로젝트 및 현장 관리 시스템 구현
  - [ ] 5.1 Project 엔티티 및 관리 기능 구현
    - Project, ConstructionPeriod 엔티티 구현
    - 프로젝트 생성, 수정, 조회 API 구현
    - _Requirements: 5.1, 5.2, 12.1_

  - [ ] 5.2 현장 목록 정렬 및 필터링 구현
    - 현장 목록 정렬 로직 (최근 로그인, 배정, 공사기간) 구현
    - 현장 상태별 필터링 및 검색 기능 구현
    - _Requirements: 1.12, 1.13, 1.14_

  - [ ] 5.3 현장 목록 정렬 및 검색 속성 테스트
    - **Property 5: Site List Ordering and Filtering**
    - **Property 6: Search Functionality Accuracy**
    - **Validates: Requirements 1.12, 1.13, 1.14**

- [ ] 6. 출역 관리 시스템 구현
  - [ ] 6.1 AttendanceRecord 엔티티 및 기본 기능 구현
    - AttendanceRecord 엔티티 및 관련 enum 구현
    - 출역 기록 생성, 조회, 수정 API 구현
    - _Requirements: 6.1, 13.1, 19.1, 24.1_

  - [ ] 6.2 출역 통계 및 차트 데이터 생성 구현
    - 대시보드용 출역 통계 계산 로직 구현
    - 월별 출역 현황 및 공종별 분포 데이터 생성
    - _Requirements: 4.4, 4.5, 11.4, 11.5_

  - [ ] 6.3 출역 데이터 정확성 속성 테스트
    - **Property 11: Chart Data Integrity**
    - **Validates: Requirements 4.4, 4.5**

- [ ] 7. 근로계약 관리 시스템 구현
  - [ ] 7.1 Contract 엔티티 및 기본 기능 구현
    - Contract 엔티티 및 ContractStatus enum 구현
    - 계약서 생성, 서명, 조회 API 구현
    - _Requirements: 7.1, 14.1, 20.1, 25.1_

  - [ ] 7.2 전자서명 및 계약 수정 요청 구현
    - 전자서명 데이터 처리 및 저장 구현
    - 계약 수정 요청 및 재발송 기능 구현
    - _Requirements: 14.3, 25.3_

- [ ] 8. 대시보드 및 통계 시스템 구현
  - [ ] 8.1 역할별 대시보드 데이터 생성 구현
    - 본사관리자, 현장관리자, 팀장, 노무자별 대시보드 데이터 계산
    - KPI 지표 및 통계 데이터 생성 로직 구현
    - _Requirements: 4.1, 4.2, 4.3, 11.1, 11.2, 11.3, 17.1, 22.1_

  - [ ] 8.2 대시보드 데이터 정확성 속성 테스트
    - **Property 10: Dashboard Data Accuracy**
    - **Validates: Requirements 4.1, 4.2, 4.3**

- [ ] 9. 보안 및 인증 강화 구현
  - [ ] 9.1 계정 잠금 및 로깅 시스템 구현
    - 5회 실패시 30분 계정 잠금 로직 구현
    - 모든 인증 시도 로깅 시스템 구현
    - _Requirements: 27.1, 27.2_

  - [ ] 9.2 인증 보안 강화 속성 테스트
    - **Property 12: Authentication Security Enforcement**
    - **Validates: Requirements 27.1, 27.2**

  - [ ] 9.3 데이터 암호화 및 검증 구현
    - 민감 정보 AES 암호화 구현
    - CI 값 및 OAuth2 토큰 검증 로직 구현
    - _Requirements: 27.4, 27.6, 27.7_

  - [ ] 9.4 데이터 보안 및 검증 속성 테스트
    - **Property 13: Data Encryption and Security**
    - **Property 15: CI Value and OAuth2 Validation**
    - **Validates: Requirements 27.4, 27.6, 27.7**

- [ ] 10. 프론트엔드 React 컴포넌트 구현
  - [ ] 10.1 인증 관련 컴포넌트 구현
    - 로그인 방식 선택, 소셜 로그인, 사업자 로그인 컴포넌트
    - 역할 선택, 현장 선택 컴포넌트 구현
    - _Requirements: 1.1, 1.2, 1.7, 1.10, 1.11_

  - [ ] 10.2 역할별 대시보드 컴포넌트 구현
    - HQDashboard, SiteDashboard, TeamDashboard, WorkerDashboard 구현
    - 차트 및 통계 표시 컴포넌트 구현
    - _Requirements: 4.1-4.5, 11.1-11.5, 17.1-17.5, 22.1-22.5_

  - [ ] 10.3 공통 UI 컴포넌트 구현
    - DataTable, SearchFilter, AttendanceChart 등 공통 컴포넌트
    - 반응형 레이아웃 및 모바일 최적화 구현
    - _Requirements: 3.4, 3.5_

  - [ ] 10.4 크로스 플랫폼 기능 일관성 속성 테스트
    - **Property 9: Cross-Platform Functionality Consistency**
    - **Validates: Requirements 3.5**

- [ ] 11. 모바일 앱 Capacitor 통합 구현
  - [ ] 11.1 Capacitor 프로젝트 설정 및 기본 기능 구현
    - Android/iOS Capacitor 프로젝트 설정
    - 카메라, GPS, 푸시 알림 플러그인 통합
    - _Requirements: 3.3, 26.1, 28.1, 28.2_

  - [ ] 11.2 안면인식 등록 기능 구현
    - 카메라 연동 안면 사진 촬영 기능
    - FaceNet API 연동 및 임베딩 생성
    - _Requirements: 26.1, 26.2, 26.3_

- [ ] 12. 동시성 및 데이터 무결성 구현
  - [ ] 12.1 동시 접근 처리 및 트랜잭션 관리 구현
    - 낙관적 잠금 및 동시성 제어 구현
    - 멀티테넌트 환경에서의 데이터 무결성 보장
    - _Requirements: 30.2_

  - [ ] 12.2 동시 접근 데이터 무결성 속성 테스트
    - **Property 16: Concurrent Access Data Integrity**
    - **Validates: Requirements 30.2**

- [ ] 13. API 통합 및 외부 서비스 연동
  - [ ] 13.1 외부 API 클라이언트 구현
    - FaceNet API 클라이언트 구현
    - Kakao/Naver OAuth2 클라이언트 구현
    - SMS 게이트웨이 연동 구현
    - _Requirements: 1.2, 1.3, 26.3_

  - [ ] 13.2 API 오류 처리 및 폴백 메커니즘 구현
    - 외부 API 실패시 폴백 처리
    - 네트워크 오류 및 타임아웃 처리
    - _Requirements: 28.5, 28.6, 28.7_

- [ ] 14. 최종 통합 및 검증
  - [ ] 14.1 전체 시스템 통합 테스트
    - 역할별 전체 워크플로우 통합 테스트
    - 멀티테넌트 데이터 격리 검증
    - _Requirements: All Requirements_

  - [ ] 14.2 전체 시스템 속성 테스트 실행
    - 모든 정확성 속성 테스트 실행 및 검증
    - 성능 및 보안 요구사항 검증

- [ ] 15. 최종 체크포인트 - 모든 테스트 통과 확인
  - 모든 테스트가 통과하는지 확인하고, 문제가 있으면 사용자에게 질문

## Notes

- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- 멀티테넌트 아키텍처의 데이터 격리가 모든 구현에서 보장되어야 함
- 모바일 앱과 웹 앱 간의 기능 일관성 유지 필요