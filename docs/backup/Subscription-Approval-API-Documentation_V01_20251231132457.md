# 구독 승인 워크플로우 API 문서

## 개요

SmartCON Lite 구독 승인 워크플로우 관련 API 엔드포인트 문서입니다.

## 인증 및 권한

모든 API는 JWT 토큰 기반 인증이 필요하며, 슈퍼관리자 권한이 요구됩니다.

```http
Authorization: Bearer <JWT_TOKEN>
```

## API 엔드포인트

### 1. 구독 승인 관리

#### 1.1 대기 중인 구독 목록 조회

```http
GET /api/v1/admin/subscriptions/pending
```

**설명**: 승인 대기 중인 구독 목록을 페이지네이션으로 조회합니다.

**요청 파라미터**:
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)
- `sort` (optional): 정렬 기준 (기본값: approvalRequestedAt,desc)

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "tenant": {
          "id": 1,
          "companyName": "테스트 건설",
          "businessNo": "123-45-67890",
          "representativeName": "김대표"
        },
        "plan": {
          "planId": "STANDARD",
          "name": "표준 플랜",
          "monthlyPrice": 50000
        },
        "status": "PENDING_APPROVAL",
        "approvalRequestedAt": "2025-12-31T10:30:00",
        "startDate": "2025-12-31",
        "monthlyPrice": 50000
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 5,
    "totalPages": 1
  },
  "message": "대기 중인 구독 목록을 성공적으로 조회했습니다."
}
```

#### 1.2 구독 승인

```http
POST /api/v1/admin/subscriptions/{subscriptionId}/approve
```

**설명**: 대기 중인 구독을 승인합니다.

**경로 파라미터**:
- `subscriptionId`: 구독 ID

**요청 본문**:
```json
{
  "reason": "정상적인 신청으로 승인합니다."
}
```

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "ACTIVE",
    "approvedAt": "2025-12-31T11:00:00",
    "approvedBy": {
      "id": 1,
      "name": "슈퍼관리자"
    }
  },
  "message": "구독이 성공적으로 승인되었습니다."
}
```

#### 1.3 구독 거부

```http
POST /api/v1/admin/subscriptions/{subscriptionId}/reject
```

**설명**: 대기 중인 구독을 거부합니다.

**경로 파라미터**:
- `subscriptionId`: 구독 ID

**요청 본문**:
```json
{
  "reason": "서류가 미비하여 거부합니다."
}
```

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "REJECTED",
    "rejectionReason": "서류가 미비하여 거부합니다."
  },
  "message": "구독이 거부되었습니다."
}
```

#### 1.4 구독 일시 중지

```http
POST /api/v1/admin/subscriptions/{subscriptionId}/suspend
```

**설명**: 활성 구독을 일시 중지합니다.

**경로 파라미터**:
- `subscriptionId`: 구독 ID

**요청 본문**:
```json
{
  "reason": "결제 문제로 인한 일시 중지"
}
```

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "SUSPENDED",
    "suspensionReason": "결제 문제로 인한 일시 중지"
  },
  "message": "구독이 일시 중지되었습니다."
}
```

#### 1.5 구독 종료

```http
POST /api/v1/admin/subscriptions/{subscriptionId}/terminate
```

**설명**: 구독을 영구적으로 종료합니다.

**경로 파라미터**:
- `subscriptionId`: 구독 ID

**요청 본문**:
```json
{
  "reason": "서비스 위반으로 인한 종료"
}
```

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "TERMINATED",
    "terminationReason": "서비스 위반으로 인한 종료"
  },
  "message": "구독이 종료되었습니다."
}
```

#### 1.6 구독 재활성화

```http
POST /api/v1/admin/subscriptions/{subscriptionId}/reactivate
```

**설명**: 중지된 구독을 재활성화합니다.

**경로 파라미터**:
- `subscriptionId`: 구독 ID

**요청 본문**:
```json
{
  "reason": "문제 해결로 인한 재활성화"
}
```

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "ACTIVE",
    "suspensionReason": null
  },
  "message": "구독이 재활성화되었습니다."
}
```

#### 1.7 승인 이력 조회

```http
GET /api/v1/admin/subscriptions/{subscriptionId}/history
```

**설명**: 특정 구독의 승인 이력을 조회합니다.

**경로 파라미터**:
- `subscriptionId`: 구독 ID

**응답 예시**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "fromStatus": "PENDING_APPROVAL",
      "toStatus": "ACTIVE",
      "action": "APPROVE",
      "reason": "정상적인 신청으로 승인",
      "adminName": "슈퍼관리자",
      "processedAt": "2025-12-31T11:00:00"
    }
  ],
  "message": "승인 이력을 성공적으로 조회했습니다."
}
```

### 2. 자동 승인 규칙 관리

#### 2.1 자동 승인 규칙 목록 조회

```http
GET /api/v1/admin/auto-approval-rules
```

**설명**: 자동 승인 규칙 목록을 조회합니다.

**응답 예시**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "ruleName": "기본 플랜 자동 승인",
      "isActive": true,
      "planIds": ["BASIC", "STANDARD"],
      "verifiedTenantsOnly": true,
      "paymentMethods": ["CARD"],
      "maxAmount": 100000,
      "priority": 1,
      "createdAt": "2025-12-31T10:00:00"
    }
  ],
  "message": "자동 승인 규칙 목록을 성공적으로 조회했습니다."
}
```

#### 2.2 자동 승인 규칙 생성

```http
POST /api/v1/admin/auto-approval-rules
```

**설명**: 새로운 자동 승인 규칙을 생성합니다.

**요청 본문**:
```json
{
  "ruleName": "프리미엄 플랜 자동 승인",
  "isActive": true,
  "planIds": ["PREMIUM"],
  "verifiedTenantsOnly": true,
  "paymentMethods": ["CARD", "BANK_TRANSFER"],
  "maxAmount": 200000,
  "priority": 2
}
```

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "id": 2,
    "ruleName": "프리미엄 플랜 자동 승인",
    "isActive": true,
    "priority": 2
  },
  "message": "자동 승인 규칙이 성공적으로 생성되었습니다."
}
```

#### 2.3 자동 승인 규칙 수정

```http
PUT /api/v1/admin/auto-approval-rules/{ruleId}
```

**설명**: 기존 자동 승인 규칙을 수정합니다.

**경로 파라미터**:
- `ruleId`: 규칙 ID

**요청 본문**:
```json
{
  "ruleName": "수정된 규칙명",
  "isActive": false,
  "maxAmount": 150000
}
```

#### 2.4 자동 승인 규칙 삭제

```http
DELETE /api/v1/admin/auto-approval-rules/{ruleId}
```

**설명**: 자동 승인 규칙을 삭제합니다.

**경로 파라미터**:
- `ruleId`: 규칙 ID

### 3. 알림 관리

#### 3.1 알림 목록 조회

```http
GET /api/v1/notifications
```

**설명**: 현재 사용자의 알림 목록을 조회합니다.

**요청 파라미터**:
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)
- `unreadOnly` (optional): 읽지 않은 알림만 조회 (기본값: false)

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "type": "SUBSCRIPTION_REQUEST",
        "title": "새로운 구독 신청",
        "message": "테스트 건설에서 새로운 구독을 신청했습니다.",
        "isRead": false,
        "relatedEntityType": "SUBSCRIPTION",
        "relatedEntityId": 1,
        "createdAt": "2025-12-31T10:30:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1
  },
  "message": "알림 목록을 성공적으로 조회했습니다."
}
```

#### 3.2 알림 읽음 처리

```http
PUT /api/v1/notifications/{notificationId}/read
```

**설명**: 특정 알림을 읽음으로 처리합니다.

**경로 파라미터**:
- `notificationId`: 알림 ID

#### 3.3 모든 알림 읽음 처리

```http
PUT /api/v1/notifications/read-all
```

**설명**: 현재 사용자의 모든 알림을 읽음으로 처리합니다.

### 4. 대시보드 통계

#### 4.1 승인 대시보드 통계

```http
GET /api/v1/admin/dashboard/approval-stats
```

**설명**: 승인 관련 대시보드 통계를 조회합니다.

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "pendingCount": 5,
    "approvedToday": 3,
    "rejectedToday": 1,
    "averageApprovalTime": "2.5 hours",
    "autoApprovalRate": 65.5,
    "monthlyStats": [
      {
        "month": "2025-12",
        "totalRequests": 45,
        "approved": 38,
        "rejected": 7,
        "autoApproved": 25
      }
    ]
  },
  "message": "대시보드 통계를 성공적으로 조회했습니다."
}
```

#### 4.2 구독 필터링

```http
GET /api/v1/admin/subscriptions
```

**설명**: 다양한 조건으로 구독을 필터링하여 조회합니다.

**요청 파라미터**:
- `status` (optional): 구독 상태 필터
- `planId` (optional): 플랜 ID 필터
- `startDate` (optional): 시작일 필터 (YYYY-MM-DD)
- `endDate` (optional): 종료일 필터 (YYYY-MM-DD)
- `tenantName` (optional): 테넌트명 검색
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "tenant": {
          "companyName": "테스트 건설",
          "businessNo": "123-45-67890"
        },
        "plan": {
          "planId": "STANDARD",
          "name": "표준 플랜"
        },
        "status": "ACTIVE",
        "startDate": "2025-12-31",
        "monthlyPrice": 50000,
        "approvedAt": "2025-12-31T11:00:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 2
  },
  "message": "구독 목록을 성공적으로 조회했습니다."
}
```

### 5. 데이터 무결성 관리

#### 5.1 데이터 무결성 검사

```http
POST /api/v1/admin/data-integrity/check
```

**설명**: 시스템 데이터 무결성을 검사합니다.

**응답 예시**:
```json
{
  "success": true,
  "data": {
    "checkId": "check_20251231_120000",
    "status": "COMPLETED",
    "totalChecks": 5,
    "passedChecks": 5,
    "failedChecks": 0,
    "issues": [],
    "executedAt": "2025-12-31T12:00:00"
  },
  "message": "데이터 무결성 검사가 완료되었습니다."
}
```

#### 5.2 데이터 무결성 리포트 조회

```http
GET /api/v1/admin/data-integrity/reports
```

**설명**: 데이터 무결성 검사 리포트 목록을 조회합니다.

## 오류 코드

### 일반 오류

| 코드 | 메시지 | 설명 |
|------|--------|------|
| 400 | Bad Request | 잘못된 요청 파라미터 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 부족 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 500 | Internal Server Error | 서버 내부 오류 |

### 구독 승인 관련 오류

| 코드 | 메시지 | 설명 |
|------|--------|------|
| 4001 | SUBSCRIPTION_NOT_FOUND | 구독을 찾을 수 없음 |
| 4002 | INVALID_STATUS_TRANSITION | 잘못된 상태 전환 |
| 4003 | APPROVAL_REASON_REQUIRED | 승인 사유 필수 |
| 4004 | ALREADY_PROCESSED | 이미 처리된 구독 |
| 4005 | AUTO_APPROVAL_FAILED | 자동 승인 실패 |

### 자동 승인 규칙 관련 오류

| 코드 | 메시지 | 설명 |
|------|--------|------|
| 5001 | RULE_NOT_FOUND | 규칙을 찾을 수 없음 |
| 5002 | DUPLICATE_RULE_NAME | 중복된 규칙명 |
| 5003 | INVALID_RULE_CONFIGURATION | 잘못된 규칙 설정 |
| 5004 | RULE_IN_USE | 사용 중인 규칙 삭제 불가 |

## 사용 예시

### 구독 승인 프로세스

```javascript
// 1. 대기 중인 구독 목록 조회
const pendingSubscriptions = await fetch('/api/v1/admin/subscriptions/pending', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

// 2. 구독 승인
const approvalResult = await fetch(`/api/v1/admin/subscriptions/${subscriptionId}/approve`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    reason: '정상적인 신청으로 승인합니다.'
  })
});

// 3. 승인 이력 확인
const history = await fetch(`/api/v1/admin/subscriptions/${subscriptionId}/history`, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### 자동 승인 규칙 설정

```javascript
// 자동 승인 규칙 생성
const newRule = await fetch('/api/v1/admin/auto-approval-rules', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    ruleName: '기본 플랜 자동 승인',
    isActive: true,
    planIds: ['BASIC', 'STANDARD'],
    verifiedTenantsOnly: true,
    paymentMethods: ['CARD'],
    maxAmount: 100000,
    priority: 1
  })
});
```

## 버전 정보

- **API 버전**: v1
- **문서 버전**: 1.0
- **최종 업데이트**: 2025-12-31
- **호환성**: SmartCON Lite v2.0+

## 지원 및 문의

기술 지원이 필요한 경우 다음으로 연락하시기 바랍니다:

- **이메일**: tech-support@smartcon.co.kr
- **문서**: https://docs.smartcon.co.kr
- **이슈 트래커**: https://github.com/smartcon/issues