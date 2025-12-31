# Implementation Plan: Subscription Approval Workflow

## Overview

구독 승인 워크플로우를 기존 SmartCON Lite 시스템에 통합하여 구현합니다. 백엔드는 Java/Spring Boot, 프론트엔드는 TypeScript/React를 사용하며, 기존 구독 시스템을 확장하는 방식으로 점진적으로 구현합니다.

## Tasks

- [x] 1. 데이터베이스 스키마 확장 및 엔티티 업데이트
  - 구독 상태 enum에 새로운 상태 추가 (PENDING_APPROVAL, REJECTED, AUTO_APPROVED, TERMINATED)
  - Subscription 엔티티에 승인 관련 필드 추가
  - 새로운 엔티티 생성 (SubscriptionApproval, AutoApprovalRule, Notification)
  - 데이터베이스 마이그레이션 스크립트 작성
  - _Requirements: 1.1, 1.5, 3.3, 5.1, 7.1_

- [x] 1.1 Write property test for subscription status enum
  - **Property 1: Subscription Request Creation**
  - **Validates: Requirements 1.1**

- [x] 2. 승인 이력 및 알림 시스템 구현
- [x] 2.1 SubscriptionApproval 엔티티 및 리포지토리 구현
  - 승인 이력 저장을 위한 엔티티 및 JPA 리포지토리 구현
  - 승인 액션 enum 정의 및 관련 메서드 구현
  - _Requirements: 5.1, 5.2_

- [x] 2.2 Write property test for approval history recording
  - **Property 19: Audit Log Creation**
  - **Validates: Requirements 5.1**

- [x] 2.3 Notification 엔티티 및 서비스 구현
  - 알림 엔티티 및 리포지토리 구현
  - NotificationService 인터페이스 및 구현체 작성
  - 이메일 및 인앱 알림 발송 로직 구현
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 2.4 Write property test for notification system
  - **Property 6: New Request Notification**
  - **Property 7: Approval Result Notification**
  - **Validates: Requirements 2.1, 2.2**

- [x] 3. 자동 승인 규칙 시스템 구현
- [x] 3.1 AutoApprovalRule 엔티티 및 서비스 구현
  - 자동 승인 규칙 엔티티 및 리포지토리 구현
  - 규칙 평가 로직 및 우선순위 처리 구현
  - JSON 기반 조건 설정 및 평가 시스템 구현
  - _Requirements: 7.1, 7.2, 7.4, 7.5_

- [x] 3.2 Write property test for auto-approval rules
  - **Property 27: Auto-Approval Rule Configuration**
  - **Property 28: Auto-Approval Processing and Logging**
  - **Validates: Requirements 7.1, 7.2, 7.3**

- [-] 4. 구독 승인 서비스 구현
- [x] 4.1 SubscriptionApprovalService 인터페이스 및 구현체 작성
  - 승인, 거부, 중지, 종료, 재활성화 메서드 구현
  - 상태 전환 검증 로직 구현
  - 트랜잭션 관리 및 동시성 제어 구현
  - _Requirements: 1.4, 1.5, 3.1, 3.2, 3.4, 3.5_

- [x] 4.2 Write property test for subscription approval operations
  - **Property 4: Subscription Approval State Transition**
  - **Property 5: Subscription Rejection State Transition**
  - **Property 9: Subscription Suspension**
  - **Property 10: Subscription Termination**
  - **Validates: Requirements 1.4, 1.5, 3.1, 3.2**

- [x] 4.3 기존 SubscriptionService 확장
  - createSubscription 메서드 수정하여 PENDING_APPROVAL 상태로 생성
  - 자동 승인 규칙 평가 및 적용 로직 추가
  - 알림 발송 연동 구현
  - _Requirements: 1.1, 1.2, 7.2_

- [x] 4.4 Write property test for modified subscription creation
  - **Property 1: Subscription Request Creation**
  - **Property 2: Pending Subscription Access Control**
  - **Validates: Requirements 1.1, 1.2**

- [-] 5. 승인 관리 API 컨트롤러 구현
- [x] 5.1 SubscriptionApprovalController 구현
  - 대기 중인 구독 목록 조회 API
  - 승인/거부/중지/종료 API 엔드포인트 구현
  - 승인 이력 조회 API 구현
  - 권한 검증 및 입력 유효성 검사 구현
  - _Requirements: 1.3, 1.4, 1.5, 3.1, 3.2, 5.2_

- [x] 5.2 Write unit tests for approval controller
  - 권한 검증 테스트
  - 입력 유효성 검사 테스트
  - API 응답 형식 테스트
  - _Requirements: 1.3, 1.4, 1.5_

- [x] 5.3 기존 SuperAdminController 확장
  - 승인 대시보드 통계 API 추가
  - 구독 필터링 및 검색 API 확장
  - 데이터 내보내기 API 구현
  - _Requirements: 4.1, 4.3, 4.4_

- [x] 5.4 Write property test for dashboard and filtering
  - **Property 14: Dashboard Subscription Counts**
  - **Property 16: Subscription Filtering**
  - **Validates: Requirements 4.1, 4.3**

- [x] 6. Checkpoint - 백엔드 핵심 기능 검증
  - 모든 백엔드 테스트 실행 및 통과 확인
  - API 엔드포인트 동작 검증
  - 데이터베이스 스키마 및 마이그레이션 검증
  - 사용자에게 진행 상황 보고 및 질문 사항 확인

- [x] 7. 프론트엔드 승인 대시보드 구현
- [x] 7.1 승인 대기 목록 컴포넌트 구현
  - 대기 중인 구독 목록 표시 컴포넌트
  - 필터링 및 검색 기능 구현
  - 페이지네이션 구현
  - _Requirements: 1.3, 4.3_

- [x] 7.2 승인 액션 컴포넌트 구현
  - 승인/거부 모달 다이얼로그 구현
  - 사유 입력 폼 및 유효성 검사
  - 액션 버튼 및 상태 표시 구현
  - _Requirements: 1.4, 1.5, 3.3_

- [x] 7.3 Write unit tests for approval components
  - 컴포넌트 렌더링 테스트
  - 사용자 인터랙션 테스트
  - 폼 유효성 검사 테스트
  - _Requirements: 1.4, 1.5_

- [x] 8. 구독 관리 대시보드 확장
- [x] 8.1 기존 TenantsSuper 컴포넌트 확장
  - 구독 상태별 필터링 기능 추가
  - 중지/종료 액션 버튼 추가
  - 승인 이력 표시 기능 구현
  - _Requirements: 3.1, 3.2, 5.2_

- [x] 8.2 구독 상세 정보 모달 구현
  - 구독 상세 정보 표시 컴포넌트
  - 승인 이력 타임라인 구현
  - 상태 변경 액션 인터페이스
  - _Requirements: 4.2, 4.5, 5.2_

- [x] 8.3 Write unit tests for subscription management components
  - 상태 표시 테스트
  - 액션 버튼 동작 테스트
  - 모달 다이얼로그 테스트
  - _Requirements: 3.1, 3.2_

- [x] 9. 테넌트 사용자 인터페이스 구현
- [x] 9.1 구독 상태 표시 컴포넌트 구현
  - 상태별 메시지 및 안내 표시
  - 재신청 버튼 및 연락처 정보 표시
  - 상태 아이콘 및 시각적 피드백 구현
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 9.2 Write property test for tenant UI behavior
  - **Property 23: Pending Status UI Behavior**
  - **Property 24: Rejected Status UI Behavior**
  - **Property 25: Suspended Status UI Behavior**
  - **Property 26: Terminated Status UI Behavior**
  - **Validates: Requirements 6.1, 6.2, 6.3, 6.4**

- [x] 9.3 기존 SubscriptionHQ 컴포넌트 수정
  - 구독 신청 시 승인 대기 상태 안내 추가
  - 구독 이력에 승인 정보 표시
  - 상태별 액션 버튼 조건부 표시
  - _Requirements: 1.1, 4.5_

- [x] 10. 알림 시스템 프론트엔드 구현
- [x] 10.1 인앱 알림 컴포넌트 구현
  - 알림 목록 표시 컴포넌트
  - 실시간 알림 수신 및 표시
  - 읽음 처리 및 알림 관리 기능
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 10.2 알림 API 연동 및 상태 관리
  - 알림 조회 및 읽음 처리 API 연동
  - Zustand 스토어를 통한 알림 상태 관리
  - 실시간 업데이트를 위한 폴링 또는 WebSocket 연동
  - _Requirements: 2.1, 2.2_

- [x] 10.3 Write unit tests for notification components
  - 알림 표시 테스트
  - 읽음 처리 테스트
  - 실시간 업데이트 테스트
  - _Requirements: 2.1, 2.2_

- [x] 11. 자동 승인 규칙 관리 인터페이스 구현
- [x] 11.1 자동 승인 규칙 설정 컴포넌트 구현
  - 규칙 생성 및 편집 폼 구현
  - 조건 설정 인터페이스 (요금제, 결제 방법 등)
  - 규칙 우선순위 및 활성화 상태 관리
  - _Requirements: 7.1, 7.4, 7.5_

- [x] 11.2 규칙 목록 및 관리 대시보드 구현
  - 기존 규칙 목록 표시 및 편집 기능
  - 규칙 적용 통계 및 모니터링 정보 표시
  - 자동 승인 기능 전체 활성화/비활성화 토글
  - _Requirements: 7.4, 7.5_

- [x] 11.3 Write property test for auto-approval rule management
  - **Property 29: Auto-Approval Rule Application Timing**
  - **Property 30: Auto-Approval Disable Functionality**
  - **Validates: Requirements 7.4, 7.5**

- [-] 12. 접근 제어 및 보안 강화
- [x] 12.1 서비스 접근 제어 로직 구현
  - 구독 상태별 서비스 접근 권한 검증
  - 미들웨어 또는 인터셉터를 통한 접근 제어
  - 상태별 리다이렉션 및 안내 메시지 처리
  - _Requirements: 1.2, 3.1, 3.2_

- [x] 12.2 Write property test for access control
  - **Property 2: Pending Subscription Access Control**
  - **Validates: Requirements 1.2**

- [x] 12.3 권한 기반 API 접근 제어 강화
  - 슈퍼관리자 전용 API 엔드포인트 보안 강화
  - JWT 토큰 검증 및 역할 기반 접근 제어
  - API 요청 로깅 및 감사 추적 구현
  - _Requirements: 1.4, 1.5, 3.1, 3.2_

- [x] 13. 데이터 무결성 및 상태 전환 검증
- [x] 13.1 상태 전환 검증 로직 구현
  - 비즈니스 규칙에 따른 상태 전환 매트릭스 정의
  - 잘못된 상태 전환 시도 차단 및 오류 처리
  - 동시성 제어를 위한 낙관적 잠금 구현
  - _Requirements: 8.3_

- [x] 13.2 Write property test for state transition validation
  - **Property 31: State Transition Validation**
  - **Validates: Requirements 8.3**

- [x] 13.3 데이터 일관성 검증 및 복구 메커니즘
  - 주기적 데이터 무결성 검사 배치 작업 구현
  - 불일치 데이터 감지 및 알림 시스템
  - 자동 복구 로직 및 수동 복구 도구 구현
  - _Requirements: 5.3, 8.5_

- [x] 14. 성능 최적화 및 모니터링
- [x] 14.1 데이터베이스 쿼리 최적화
  - 승인 대기 목록 조회 성능 최적화
  - 인덱스 추가 및 쿼리 튜닝
  - 페이지네이션 성능 개선
  - _Requirements: 1.3, 4.3_

- [x] 14.2 캐싱 및 비동기 처리 구현
  - 자동 승인 규칙 캐싱 구현
  - 알림 발송 비동기 처리
  - 대시보드 통계 데이터 캐싱
  - _Requirements: 2.1, 7.2, 4.1_

- [x] 14.3 Write performance tests
  - 대량 데이터 처리 성능 테스트
  - 동시 요청 처리 성능 테스트
  - 메모리 사용량 및 응답 시간 측정
  - _Requirements: 1.3, 2.1_

- [-] 15. 통합 테스트 및 E2E 테스트
- [x] 15.1 승인 워크플로우 통합 테스트
  - 전체 승인 프로세스 end-to-end 테스트
  - 다양한 시나리오별 통합 테스트
  - API와 데이터베이스 연동 테스트
  - _Requirements: 1.1, 1.4, 1.5, 2.1, 2.2_

- [x] 15.2 Write integration tests for approval workflow
  - 승인 프로세스 전체 플로우 테스트
  - 알림 발송 연동 테스트
  - 상태 변경 및 접근 제어 통합 테스트
  - _Requirements: 1.4, 2.1, 3.1_

- [-] 15.3 프론트엔드 E2E 테스트
  - Cypress 또는 Playwright를 사용한 E2E 테스트
  - 사용자 시나리오별 테스트 케이스 작성
  - 브라우저 호환성 및 반응형 디자인 테스트
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 16. 최종 검증 및 배포 준비
- [ ] 16.1 전체 시스템 검증
  - 모든 테스트 실행 및 통과 확인
  - 기존 기능 회귀 테스트
  - 성능 및 보안 검증
  - 데이터 마이그레이션 검증

- [ ] 16.2 문서화 및 배포 가이드 작성
  - API 문서 업데이트
  - 운영 가이드 및 트러블슈팅 문서 작성
  - 배포 스크립트 및 롤백 계획 수립
  - 사용자 매뉴얼 업데이트

- [ ] 17. Final checkpoint - 전체 시스템 검증 완료
  - 모든 테스트 통과 확인
  - 사용자 승인 및 피드백 수집
  - 배포 준비 완료 확인

## Notes

- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation and user feedback
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Integration tests ensure system components work together correctly
- All tests are required for comprehensive quality assurance