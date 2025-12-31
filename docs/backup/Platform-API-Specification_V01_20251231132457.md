# SmartCON Lite 플랫폼 및 API 설계 명세서

**문서 버전:** 1.0  
**작성일:** 2025년 12월 23일  
**작성자:** Antigravity AI  

---

## 1. 플랫폼 아키텍처 개요 (Platform Architecture)

SmartCON Lite는 다중 고객사(Tenant)를 지원하는 **SaaS 아키텍처**를 기반으로 설계되었습니다. 모든 데이터는 테넌트 식별자(`tenant_id`)를 통해 논리적으로 격리되며, 역할 기반 권한 제어(RBAC)를 통해 보안을 유지합니다.

### 1.1 멀티테넌시 전략 (Multi-tenancy Strategy)
- **방식**: **Shared Database, Shared Schema**
- **식별자**: 모든 비즈니스 테이블에 `tenant_id` 컬럼 포함.
- **격리 메커니즘**: 
  - 인증 시 JWT에 `tenant_id` 포함.
  - 백엔드(Spring Data JPA)에서 Hibernate `@Filter` 또는 QueryDSL 등을 활용하여 모든 쿼리에 `WHERE tenant_id = ?` 구문을 강제로 주입.
  - 캐시(Redis) 및 파일 저장소(S3) 경로에도 테넌트 식별자를 활용하여 경로 격리.

### 1.2 역할 구성 (Roles)
- **ROLE_SUPER**: 플랫폼 본사 관리자 (가입 승인, 결제 모니터링, 시스템 설정).
- **ROLE_HQ**: 개별 고객사(Tenant) 본사 관리자 (현장 개설, 사내 표준 관리).
- **ROLE_SITE**: 건설 현장 관리자 (출역 마감, 작업 지시, 일보 승인).
- **ROLE_TEAM**: 노무 팀장 (팀원 관리, 작업 수합, 일보 제출).
- **ROLE_WORKER**: 노무 근로자 (출역 확인, 전자 계약 서명).

---

## 2. 공통 모듈 (Common Modules)

플랫폼의 기본 인프라 역할을 하는 공통 모듈입니다.

| 모듈 ID | 모듈명 | 주요 기능 |
| :--- | :--- | :--- |
| **CMN-AUTH** | 인증/인가 | JWT 발급, 소셜 로그인(카카오/네이버), 로그아웃, 토큰 갱신. |
| **CMN-TEN** | 테넌트 관리 | 고객사 프로필 관리, 구독 정보 연동, 온보딩 프로세스. |
| **CMN-USR** | 사용자 관리 | 테넌트 내 사용자 등록, 역할 부여, 프로필 관리. |
| **CMN-COD** | 공통 코드 | 시스템 기초 코드(공종, 직종, 지역코드 등) 마스터 관리. |
| **CMN-FIL** | 파일 관리 | S3 기반 파일 업로드/다운로드, 이미지 리사이징, 문서 보안 경로. |
| **CMN-NTF** | 알림 서비스 | Push 알림(FCM), SMS/알림톡(BizMsg), 이메일 발송. |
| **CMN-LOG** | 감사 로그 | 사용자 접속 이력, 데이터 중요 변경 이력 추적. |

---

## 3. 특화 기능 (Specialized Features)

SmartCON Lite만의 핵심 비즈니스 로직이 구현되는 모듈입니다.

| 모듈 ID | 모듈명 | 주요 기능 |
| :--- | :--- | :--- |
| **SVC-FACE** | 안면인식 | FaceNet 연동, 벡터 임베딩 추출, 현장 단말기 동기화 데이터 생성. |
| **SVC-WKF** | 출역 관리 | 안면인식 로그 수집, 수동 출역 보정, 일일 공수 집계 및 확정. |
| **SVC-CON** | 전자 계약 | 표준근로계약서 템플릿 바인딩, 캔버스 기반 전자서명, PDF 생성 및 해시 리포트. |
| **SVC-REP** | 공사 일보 | 기상청 API 연동 실시간 날씨 저장, 팀별 작업 내용 취합, 일보 PDF 출력. |
| **SVC-SET** | 정산 및 세무 | 노무비 집계, 요금제 기반 이용료 계산, 홈택스 API 연동 세금계산서 발행. |

---

## 4. API Specification

### 4.1 공통 API (Common)

#### [AUTH] 인증 관련
- `POST /api/v1/auth/login`
  - **Description**: 로그인 (HQ는 사업자번호, 일반 사용자는 소셜)
  - **Request**: `{ "type": "HQ", "username": "1234567890", "password": "..." }` 또는 `{ "type": "SOCIAL", "provider": "KAKAO", "token": "..." }`
  - **Response**: `{ "accessToken": "...", "refreshToken": "...", "roles": ["ROLE_HQ"] }`
- `POST /api/v1/auth/refresh`
  - **Request**: `{ "refreshToken": "..." }`
- `GET /api/v1/auth/me`
  - **Response**: `{ "id": 1, "name": "이대영", "email": "...", "tenantId": 101, "roles": ["ROLE_HQ"], "currentSiteId": null }`

#### [TENANT/SUB] 구독 및 온보딩 (구동신청 및 관리)
- `POST /api/v1/subscription/verify-business`
  - **Description**: 사업자 등록번호 진위 확인 (공공데이터 API 연동)
  - **Request**: `{ "businessNo": "123-45-67890" }`
- `POST /api/v1/subscription/onboarding`
  - **Description**: 테넌트(회사) 생성 및 구독 정보 등록
  - **Request**: `{ "companyName": "주식회사 스마트콘", "representative": "홍길동", "planId": "PREMIUM_MONTHLY", "paymentInfo": { "method": "CARD", "cardToken": "..." } }`
- `GET /api/v1/tenants/my-plan`
  - **Response**: `{ "planName": "프리미엄", "status": "ACTIVE", "nextBillingDate": "2026-01-01", "usage": { "sites": 5, "maxSites": 10 } }`

---

### 4.2 서비스 API (Specialized Service)

#### [ATTENDANCE] 출역 및 안면인식
- `GET /api/v1/attendance/logs`
  - **Params**: `siteId`, `date` (YYYY-MM-DD)
  - **Response**: `[ { "id": 1, "workerName": "김노무", "checkIn": "08:00", "checkOut": "18:00", "faceMatched": true }, ... ]`
- `POST /api/v1/attendance/finalize`
  - **Description**: 현장 관리자가 해당 일의 출역을 최종 승인(마감) 처리
  - **Request**: `{ "siteId": 1, "date": "2025-12-23", "ids": [1, 2, 3] }`

#### [CONTRACT] 전자 계약
- `POST /api/v1/contracts/template/bind`
  - **Description**: 근로자 정보를 템플릿에 매핑하여 계약서 초안 생성
  - **Request**: `{ "workerId": 50, "templateId": "STD_2025_01" }`
- `POST /api/v1/contracts/{id}/sign`
  - **Description**: 근로자 서명 데이터 저장 및 PDF 생성
  - **Request**: `{ "signatureBase64": "data:image/png;base64,...", "latitude": 37.5, "longitude": 127.0 }`

---

### 4.3 슈퍼관리자 API (Super Admin)

#### [ADM-TENANT] 테넌트 전체 제어
- `GET /api/v1/admin/tenants`
  - **Response**: `{ "content": [ { "id": 101, "companyName": "A건설", "status": "ACTIVE", "totalPaid": 550000 }, ... ], "totalElements": 150 }`
- `PATCH /api/v1/admin/tenants/{id}/status`
  - **Request**: `{ "status": "SUSPENDED", "reason": "이용료 체납" }`

#### [ADM-BILLING] 결제/정산 모니터링
- `GET /api/v1/admin/billing/stats`
  - **Response**: `{ "monthlyRevenue": 15000000, "failedPayments": 3, "pendingTaxInvoices": 12 }`
- `POST /api/v1/admin/billing/tax-invoice/retry`
  - **Description**: 실패한 세금계산서 발행 재시도
  - **Request**: `{ "invoiceId": 505 }`

---

## 5. 멀티테넌트 구현 기술 가이드 (Tenant-Key Implementation)

### 5.1 데이터베이스 계층 (JPA/Hibernate)
모든 비즈니스 엔티티는 `tenant_id`를 보유하며, Spring Data JPA의 Interceptor를 통해 세션별 테넌트를 강제합니다.

```java
// 1. CurrentTenantIdentifierResolver 구현
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenantId(); // ThreadLocal에서 추출
    }
}

// 2. BaseEntity 정의
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
}
```

### 5.2 보안 및 접근 제어
- **API Interceptor**: 모든 요청의 JWT에서 `tenant_id`를 추출하여 `TenantContext`에 설정.
- **Data Integrity**: 모든 Repository 쿼리 수행 시 `@Filter`가 활성화되어 있는지 확인하는 Aspect(AOP) 적용.
- **S3 Isolation**: 파일 업로드 시 경로를 `s3://bucket/{tenant_id}/{site_id}/...` 형태로 구성하여 데이터 혼선 방지.

---

**문서 끝**
