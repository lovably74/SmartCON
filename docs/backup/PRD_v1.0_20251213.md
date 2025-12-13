# SmartCON Lite 구축 상세 요구사항 정의서

**문서 버전:** 1.0  
**작성일:** 2025년 12월 13일  
**작성자:** 개발 PM (Gemini)  
**대상 독자:** 백엔드/프론트엔드 개발자, QA

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요-project-overview)
2. [기술 스택 및 아키텍처](#2-기술-스택-및-아키텍처-tech-stack)
3. [사용자 역할 및 권한](#3-사용자-역할-및-권한-user-roles)
4. [상세 기능 요구사항](#4-상세-기능-요구사항-functional-requirements)
5. [데이터베이스 설계 가이드](#5-데이터베이스-설계-가이드-database-schema-draft)
6. [비기능 요구사항](#6-비기능-요구사항-non-functional-requirements)
7. [개발 시 고려사항](#7-개발-시-고려사항-tips-for-junior-devs)
8. [다음 단계](#8-다음-단계-next-steps)

---

## 1. 프로젝트 개요 (Project Overview)

### 1.1. 배경 및 목적

#### 배경
건설 현장의 노무 관리가 수기/엑셀에 의존하여 비효율적이며, 허위 출역(일명 '가라 출근') 등의 문제가 발생하고 있습니다.

#### 목적
FaceNet 기반 안면인식 기술을 활용하여 출역 관리를 자동화하고, 모바일 중심의 워크플로우를 통해 현장 관리의 투명성과 효율성을 확보합니다.

#### 핵심 가치
**"현장의 신뢰, 기술로 완성하다"**
- **투명성**: 안면인식을 통한 객관적 출역 관리
- **편의성**: 모바일 중심의 직관적인 사용자 경험
- **유연성**: 다양한 현장 환경에 대응하는 유연한 시스템

### 1.2. 목표 시스템 범위

#### 플랫폼
- **PC Web**: 관리자용 (본사 관리자, 현장 관리자)
- **Mobile Web/App**: 현장 관리자, 팀장, 노무자용 (반응형 디자인)

#### 주요 기능
1. 안면인식 출역 체크
2. 작업일보 작성 및 관리
3. 실시간 대시보드
4. 전자계약 관리
5. 출역 이력 조회 및 이의 제기

---

## 2. 기술 스택 및 아키텍처 (Tech Stack)

한국 개발자 시장에서의 채용 용이성, 커뮤니티 활성도, 그리고 유지보수 편의성을 고려하여 선정했습니다.

### 2.1. 프론트엔드 (Frontend)

#### Framework
- **Vue.js 3 (Composition API)**
  - **선정 이유**: 
    - 한국 SI 및 솔루션 시장에서 점유율이 매우 높음
    - 러닝 커브가 낮아 초급 개발자도 쉽게 적응 가능
    - 기존 기술 스택과 일치

#### State Management
- **Pinia**
  - Vuex보다 직관적이고 가벼움
  - TypeScript 지원 우수

#### UI Library
- **Vuetify 3** 또는 **Element Plus**
  - 반응형 그리드 시스템 및 기성 컴포넌트 활용 용이
  - Material Design 기반의 일관된 UI/UX

#### Build Tool
- **Vite**
  - 빠른 개발 서버 및 빌드 속도
  - HMR(Hot Module Replacement) 지원

### 2.2. 백엔드 (Backend)

#### Framework
- **Spring Boot 3.x (Java 17 이상)**
  - 엔터프라이즈급 안정성
  - 풍부한 생태계 및 커뮤니티 지원

#### ORM
- **JPA (Hibernate)**
  - 생산성 향상
  - 객체-관계 매핑 자동화

#### Security
- **Spring Security + JWT**
  - Stateless 인증 방식
  - 확장 가능한 인증/인가 구조

### 2.3. 데이터베이스 (DBMS)

#### Main DB
- **MSSQL (SQL Server 2019 이상)**
  - **필수 사항**: 
    - T-SQL 문법 준수
    - Stored Procedure는 복잡한 통계 쿼리에만 제한적 사용 권장

### 2.4. 외부 인터페이스 (Interface)

#### 안면인식
- **신한홀딩스(또는 제휴사) Face API**
  - REST API 방식 연동 예상
  - 비동기 처리 및 타임아웃 처리 필수

#### 인증
- **카카오/네이버 OAuth 2.0**
- **PASS 인증** (실명 확인용)

---

## 3. 사용자 역할 및 권한 (User Roles)

설계서 기반으로 4가지 역할을 정의합니다.

| 역할 (Role) | 주요 권한 및 기능 | 사용 플랫폼 | 비고 |
| :--- | :--- | :--- | :--- |
| **본사 관리자 (Super Admin)** | 전사 현장 생성/관리, 전체 통계 조회, 마스터 코드 관리 | PC 웹 주사용 | 최고 관리자 권한 |
| **현장 관리자 (Site Admin)** | 담당 현장 출역 모니터링, 예외(미인식) 승인 처리, 작업일보 취합 | PC/모바일 병행 | 현장 단위 관리 |
| **팀장 (Team Leader)** | 소속 팀원 관리, 팀원 안면 등록 요청, 작업일보 작성 | 모바일 주사용 | 팀 단위 관리 |
| **일반 노무자 (Worker)** | 본인 안면 등록, 출역 이력 조회, 이의 제기 신청 | 모바일 주사용 | 기본 사용자 |

### 3.1. 권한 매트릭스

| 기능 | 본사 관리자 | 현장 관리자 | 팀장 | 노무자 |
| :--- | :---: | :---: | :---: | :---: |
| 현장 생성/수정 | ✅ | ❌ | ❌ | ❌ |
| 전체 통계 조회 | ✅ | ❌ | ❌ | ❌ |
| 현장 출역 모니터링 | ✅ | ✅ | ❌ | ❌ |
| 예외 승인 처리 | ✅ | ✅ | ❌ | ❌ |
| 작업일보 작성 | ✅ | ✅ | ✅ | ❌ |
| 안면 등록 | ✅ | ✅ | ✅ | ✅ |
| 출역 이력 조회 | ✅ | ✅ | ✅ | ✅ |
| 이의 제기 | ✅ | ✅ | ✅ | ✅ |

---

## 4. 상세 기능 요구사항 (Functional Requirements)

### 4.1. 공통 및 인증 (Common & Auth)

#### FR-COM-01: 소셜 로그인 및 회원가입

**설명**: 카카오, 네이버 OAuth 연동 및 PASS 인증을 통한 실명 확인

**상세 요구사항**:
- 카카오, 네이버 OAuth 2.0 연동
- PASS 인증을 통한 실명 확인 필수 (노무비 지급을 위한 실명 확보)
- 회원가입 시 역할(본사 관리자, 현장 관리자, 팀장, 노무자) 선택
- 현장 관리자는 담당 현장 선택 필수

**반응형 요건**:
- **PC**: 중앙 카드 형태의 로그인 화면
- **Mobile**: 전체 화면 채움 형태의 로그인 화면

**우선순위**: P0 (최우선)

---

#### FR-COM-02: 반응형 레이아웃

**설명**: 다양한 디바이스에 대응하는 반응형 레이아웃 구현

**Breakpoints**:
- **Mobile**: ~767px
- **Tablet**: 768px ~ 1023px
- **Desktop**: 1024px ~

**Navigation**:
- **PC**: 좌측 LNB(Left Navigation Bar)
- **Mobile**: 하단 Bottom Tab + Drawer 메뉴

**구현 요건**:
- 모든 화면에서 반응형 디자인 적용
- 터치 친화적인 UI 요소 크기 (모바일 최소 44x44px)
- 가로/세로 모드 모두 지원

**우선순위**: P0 (최우선)

---

### 4.2. 안면 인식 및 등록 (Face Recognition)

#### FR-FACE-01: 안면 등록 프로세스 (5단계)

**설명**: 사용자의 안면을 등록하는 프로세스

**프로세스 Flow**:
1. **안내(가이드)**: 안면 등록 가이드 화면 표시
2. **권한 요청(카메라)**: 카메라 접근 권한 요청
3. **촬영**: 안면 촬영 (전면 카메라 사용)
4. **전송/분석**: 서버로 이미지 전송 및 분석
5. **결과**: 등록 성공/실패 결과 표시

**UI 가이드**:
- 안경/마스크 제거 안내 메시지
- 밝은 곳 권장 가이드 표시
- 얼굴 프레임 가이드라인 표시
- 촬영 카운트다운 (3, 2, 1)

**Fail-Safe**:
- 촬영 실패 시 재촬영 버튼 노출
- 최대 3회 재시도 가능
- 실패 시 관리자에게 수동 등록 요청 가능

**우선순위**: P0 (최우선)

---

#### FR-FACE-02: 안면 인식 API 연동 (신한홀딩스 연계)

**설명**: 신한홀딩스 Face API를 통한 안면 인식 기능

**API 명세 (가상 인터페이스)**:

```json
// Request (POST /api/external/face/compare)
{
  "sourceImage": "Base64String...", // 등록된 이미지
  "targetImage": "Base64String...", // 현장 촬영 이미지
  "threshold": 0.85 // 유사도 임계값
}

// Response
{
  "match": true,
  "score": 0.92, // 유사도 점수
  "transactionId": "SH-12345"
}
```

**요구사항**:
- 신한 API 응답 속도가 느릴 수 있으므로, 비동기 처리 필수
- 로딩 인디케이터(Spinner) 필수 적용
- 타임아웃 설정 (기본 30초)
- API 실패 시 재시도 로직 (최대 3회)
- 실패 시 사용자에게 명확한 에러 메시지 표시

**에러 처리**:
- 네트워크 오류: "네트워크 연결을 확인해주세요"
- API 오류: "인식 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요"
- 인식 실패: "인식에 실패했습니다. 다시 시도해주세요"

**우선순위**: P0 (최우선)

---

#### FR-FACE-03: 출역 체크 (Face Check-in)

**설명**: 안면인식을 통한 출역 체크 기능

**프로세스**:
1. 현장 도착 시 모바일 앱 실행
2. 출역 체크 버튼 클릭
3. 카메라를 통해 안면 촬영
4. 등록된 안면 데이터와 비교
5. 일치 시 출역 기록 생성
6. 불일치 시 관리자 승인 요청

**비즈니스 로직**:
- 출역 시간 기준: 09:00 이전 → 정상, 09:00 이후 → 지각
- 중복 체크 방지: 당일 출역 기록이 이미 존재하는 경우 경고 메시지
- 위치 정보 수집 (선택사항): GPS 좌표 저장

**우선순위**: P0 (최우선)

---

### 4.3. 대시보드 및 출역 관리 (Attendance)

#### FR-DASH-01: 본사 관리자 대시보드

**설명**: 전사 현장 현황을 한눈에 볼 수 있는 대시보드

**표시 항목**:
- 전체 현장 수
- 금일 총 출역 인원
- 전자계약 체결률
- 안면 미등록자 현황 Top 5 리스트
- 월별 출역 통계 (그래프)
- 현장별 출역 현황 (테이블)

**UI 요구사항**:
- PC 화면에 최적화된 레이아웃
- 실시간 데이터 갱신 (30초 간격)
- 엑셀 다운로드 기능

**우선순위**: P1 (높음)

---

#### FR-DASH-02: 현장 관리자 대시보드 (실시간 현황판)

**설명**: 담당 현장의 출역 현황을 실시간으로 모니터링

**PC 화면**:
- 테이블(Grid) 형태로 많은 데이터를 한눈에 조회
- 컬럼: 이름, 출역 시간, 상태, 공수, 비고
- 정렬 기능 (이름, 시간, 상태별)
- 필터 기능 (날짜, 상태별)
- 엑셀 다운로드 기능 필수

**Mobile 화면**:
- 리스트(Card) 형태로 변환
- 지각/정상 상태에 따른 색상 구분
  - 정상: Green
  - 지각: Orange
  - 미출역: Gray
- Pull-to-refresh 기능
- 무한 스크롤 또는 페이지네이션

**실시간 업데이트**:
- WebSocket 또는 Server-Sent Events 활용
- 새 출역 기록 발생 시 자동 갱신

**우선순위**: P0 (최우선)

---

#### FR-DASH-03: 예외 처리 (수동 승인)

**설명**: 안면인식 실패 시 관리자가 수동으로 승인하는 기능

**프로세스**:
1. 안면인식 실패 또는 미등록 사용자 출역 시도
2. 관리자에게 알림 전송 (Push Notification)
3. 관리자가 승인 화면에서 검토
4. 승인/반려 결정
5. 승인 사유 선택 필수
6. 사용자에게 결과 알림

**UI 요구사항**:
- **PC**: 모달(Modal) 팝업
- **Mobile**: 전체 화면(Full Page)으로 처리

**승인 사유 목록**:
- 기기 오류
- 안면 손상/변형
- 조명 부족
- 기타 (직접 입력)

**데이터 저장**:
- 수동 승인 시 '승인 사유'를 반드시 선택해야 함
- 승인자 정보 기록
- 승인 시간 기록

**우선순위**: P0 (최우선)

---

### 4.4. 작업일보 (Daily Report)

#### FR-RPT-01: 팀장용 간편 작성

**설명**: 팀장이 작업일보를 간편하게 작성하는 기능

**Input 항목**:
- 작업일자 (자동 설정, 수정 가능)
- 공종 (Select 드롭다운)
- 작업내용 (Textarea, 최대 1000자)
- 현장사진 (Upload, 최대 5장)

**UX 요구사항**:
- 모바일 환경을 고려한 설계
- 사진 업로드 옵션:
  - 카메라 바로 실행
  - 갤러리 선택
- 이미지 리사이징/압축 (최대 1MB 이하)
- 진행률 표시 (업로드 중)

**임시저장 기능**:
- 네트워크 불안정 현장을 대비하여 로컬 스토리지에 내용 임시 저장
- 앱 재실행 시 임시저장 데이터 복원 제안
- 서버 동기화 시 임시저장 데이터 자동 삭제

**우선순위**: P1 (높음)

---

#### FR-RPT-02: 작업일보 조회 및 관리

**설명**: 작성된 작업일보를 조회하고 관리하는 기능

**기능**:
- 작업일보 목록 조회 (날짜별, 현장별)
- 작업일보 상세 조회
- 작업일보 수정/삭제 (작성자만 가능)
- 현장 관리자의 작업일보 취합 및 승인

**우선순위**: P1 (높음)

---

### 4.5. 마이페이지 및 이의제기 (Worker)

#### FR-MY-01: 월간 출역 현황 (Calendar)

**설명**: 노무자가 본인의 출역 현황을 달력 형태로 조회

**PC 화면**:
- Full Calendar 라이브러리 활용
- 월 전체 조망 가능
- 일별 상세 정보 호버 시 표시

**Mobile 화면**:
- 주간 달력 또는 일별 리스트 뷰
- 상세 정보는 바텀시트(Bottom Sheet)로 표시

**표시 데이터**:
- 일별 인정 공수 (1.0, 0.5 등)
- 출역 시간
- 상태 (정상, 지각, 미출역)
- 수동 승인 여부

**색상 구분**:
- 정상 출역: Green
- 지각: Orange
- 미출역: Gray
- 수동 승인: Blue

**우선순위**: P1 (높음)

---

#### FR-MY-02: 이의 제기 프로세스

**설명**: 출역 기록에 대한 이의 제기 및 처리 프로세스

**프로세스 Flow**:
1. **노무자 요청**: 출역 누락/오류 신청
2. **관리자 알림**: Push Notification 전송
3. **관리자 검토**: 출역 기록 및 관련 자료 확인
4. **승인/반려**: 관리자 결정
5. **노무자 알림**: 결과 알림 전송

**상태값**:
- **REQUEST**: 요청 (노무자가 신청)
- **PENDING**: 처리중 (관리자 검토 중)
- **APPROVED**: 승인 (관리자 승인)
- **REJECTED**: 반려 (관리자 반려)

**Input 항목**:
- 이의 제기 날짜
- 이의 제기 사유 (Textarea)
- 첨부 자료 (선택사항, 사진 등)

**관리자 화면**:
- 이의 제기 목록 조회
- 상세 내용 확인
- 승인/반려 처리
- 반려 사유 입력 (반려 시 필수)

**우선순위**: P1 (높음)

---

#### FR-MY-03: 안면 정보 관리

**설명**: 사용자가 본인의 안면 등록 정보를 관리

**기능**:
- 안면 등록 여부 확인
- 안면 재등록 (변경 시)
- 안면 삭제 요청 (관리자 승인 필요)

**우선순위**: P2 (중간)

---

## 5. 데이터베이스 설계 가이드 (Database Schema Draft)

MSSQL 기준으로 핵심 테이블 구조를 제안합니다.

### 5.1. 핵심 테이블 구조

```sql
-- 1. 사용자 테이블 (Users)
CREATE TABLE Users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE, -- 로그인 ID
    password_hash VARCHAR(255) NULL, -- 소셜 로그인 시 NULL
    name NVARCHAR(50) NOT NULL, -- 실명
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    role_code VARCHAR(10) NOT NULL, -- ROLE_ADMIN, ROLE_SITE, ROLE_TEAM, ROLE_WORKER
    face_vector VARBINARY(MAX) NULL, -- 안면 인식용 벡터 데이터 (또는 외부 ID)
    face_image_url VARCHAR(500) NULL, -- 안면 이미지 URL (암호화 저장)
    is_face_registered BIT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    created_by BIGINT NULL,
    updated_by BIGINT NULL
);

-- 인덱스
CREATE INDEX IX_Users_Username ON Users(username);
CREATE INDEX IX_Users_RoleCode ON Users(role_code);

-- 2. 현장 테이블 (Sites)
CREATE TABLE Sites (
    site_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    site_name NVARCHAR(100) NOT NULL,
    site_code VARCHAR(50) NOT NULL UNIQUE,
    address NVARCHAR(200) NULL,
    manager_id BIGINT FOREIGN KEY REFERENCES Users(user_id),
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 인덱스
CREATE INDEX IX_Sites_ManagerId ON Sites(manager_id);
CREATE INDEX IX_Sites_SiteCode ON Sites(site_code);

-- 3. 사용자-현장 매핑 테이블 (User_Sites)
CREATE TABLE User_Sites (
    user_site_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    site_id BIGINT NOT NULL FOREIGN KEY REFERENCES Sites(site_id),
    team_name NVARCHAR(50) NULL, -- 팀명
    is_active BIT DEFAULT 1,
    assigned_at DATETIME DEFAULT GETDATE(),
    UNIQUE(user_id, site_id)
);

-- 인덱스
CREATE INDEX IX_User_Sites_UserId ON User_Sites(user_id);
CREATE INDEX IX_User_Sites_SiteId ON User_Sites(site_id);

-- 4. 출역 기록 테이블 (Attendance_Log)
CREATE TABLE Attendance_Log (
    log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    site_id BIGINT NOT NULL FOREIGN KEY REFERENCES Sites(site_id),
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    check_in_time DATETIME NOT NULL,
    check_out_time DATETIME NULL,
    auth_type VARCHAR(20) NOT NULL, -- FACE, MANUAL(수동)
    status VARCHAR(20) NOT NULL, -- NORMAL, LATE
    daily_man_day DECIMAL(3, 1) DEFAULT 1.0, -- 공수 (1.0, 0.5)
    manual_reason NVARCHAR(200) NULL, -- 수동 승인 사유
    manual_approved_by BIGINT NULL FOREIGN KEY REFERENCES Users(user_id), -- 수동 승인자
    manual_approved_at DATETIME NULL, -- 수동 승인 시간
    face_match_score DECIMAL(5, 3) NULL, -- 안면 인식 유사도 점수
    location_lat DECIMAL(10, 8) NULL, -- GPS 위도
    location_lng DECIMAL(11, 8) NULL, -- GPS 경도
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 인덱스
CREATE INDEX IX_Attendance_Log_SiteId ON Attendance_Log(site_id);
CREATE INDEX IX_Attendance_Log_UserId ON Attendance_Log(user_id);
CREATE INDEX IX_Attendance_Log_CheckInTime ON Attendance_Log(check_in_time);
CREATE INDEX IX_Attendance_Log_SiteId_CheckInTime ON Attendance_Log(site_id, check_in_time);

-- 5. 작업일보 테이블 (Daily_Report)
CREATE TABLE Daily_Report (
    report_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    site_id BIGINT NOT NULL FOREIGN KEY REFERENCES Sites(site_id),
    team_leader_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    work_date DATE NOT NULL,
    work_type_code VARCHAR(20) NULL, -- 공종 코드
    work_content NVARCHAR(MAX) NULL,
    photo_urls NVARCHAR(MAX) NULL, -- JSON 배열 형태로 여러 URL 저장
    is_temporary BIT DEFAULT 0, -- 임시저장 여부
    is_approved BIT DEFAULT 0, -- 현장 관리자 승인 여부
    approved_by BIGINT NULL FOREIGN KEY REFERENCES Users(user_id),
    approved_at DATETIME NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 인덱스
CREATE INDEX IX_Daily_Report_SiteId ON Daily_Report(site_id);
CREATE INDEX IX_Daily_Report_TeamLeaderId ON Daily_Report(team_leader_id);
CREATE INDEX IX_Daily_Report_WorkDate ON Daily_Report(work_date);
CREATE INDEX IX_Daily_Report_SiteId_WorkDate ON Daily_Report(site_id, work_date);

-- 6. 이의 제기 테이블 (Attendance_Dispute)
CREATE TABLE Attendance_Dispute (
    dispute_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    attendance_log_id BIGINT NULL FOREIGN KEY REFERENCES Attendance_Log(log_id),
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    site_id BIGINT NOT NULL FOREIGN KEY REFERENCES Sites(site_id),
    dispute_date DATE NOT NULL, -- 이의 제기 대상 날짜
    dispute_reason NVARCHAR(500) NOT NULL, -- 이의 제기 사유
    status VARCHAR(20) NOT NULL DEFAULT 'REQUEST', -- REQUEST, PENDING, APPROVED, REJECTED
    attachment_urls NVARCHAR(MAX) NULL, -- 첨부 자료 URL (JSON 배열)
    reviewed_by BIGINT NULL FOREIGN KEY REFERENCES Users(user_id), -- 검토자
    reviewed_at DATETIME NULL, -- 검토 시간
    review_comment NVARCHAR(500) NULL, -- 검토 의견 (반려 사유 등)
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 인덱스
CREATE INDEX IX_Attendance_Dispute_UserId ON Attendance_Dispute(user_id);
CREATE INDEX IX_Attendance_Dispute_SiteId ON Attendance_Dispute(site_id);
CREATE INDEX IX_Attendance_Dispute_Status ON Attendance_Dispute(status);
CREATE INDEX IX_Attendance_Dispute_DisputeDate ON Attendance_Dispute(dispute_date);

-- 7. 소셜 로그인 연동 테이블 (Social_Login)
CREATE TABLE Social_Login (
    social_login_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    provider VARCHAR(20) NOT NULL, -- KAKAO, NAVER
    provider_user_id VARCHAR(100) NOT NULL, -- 소셜 플랫폼의 사용자 ID
    access_token VARCHAR(500) NULL,
    refresh_token VARCHAR(500) NULL,
    token_expires_at DATETIME NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    UNIQUE(provider, provider_user_id)
);

-- 인덱스
CREATE INDEX IX_Social_Login_UserId ON Social_Login(user_id);
CREATE INDEX IX_Social_Login_Provider ON Social_Login(provider, provider_user_id);

-- 8. PASS 인증 테이블 (Pass_Verification)
CREATE TABLE Pass_Verification (
    verification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    pass_ci VARCHAR(200) NOT NULL, -- PASS CI (연계정보)
    verified_at DATETIME NOT NULL,
    is_verified BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE()
);

-- 인덱스
CREATE INDEX IX_Pass_Verification_UserId ON Pass_Verification(user_id);
CREATE INDEX IX_Pass_Verification_PassCi ON Pass_Verification(pass_ci);

-- 9. 마스터 코드 테이블 (Master_Code)
CREATE TABLE Master_Code (
    code_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code_group VARCHAR(50) NOT NULL, -- 코드 그룹 (예: WORK_TYPE, MANUAL_REASON)
    code_value VARCHAR(50) NOT NULL, -- 코드 값
    code_name NVARCHAR(100) NOT NULL, -- 코드 명
    code_order INT DEFAULT 0, -- 정렬 순서
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    UNIQUE(code_group, code_value)
);

-- 인덱스
CREATE INDEX IX_Master_Code_CodeGroup ON Master_Code(code_group);
```

### 5.2. 데이터 타입 가이드

- **날짜/시간**: `DATETIME` 사용 (Timezone 고려 필요)
- **문자열**: 
  - 한글 포함 시 `NVARCHAR` 사용
  - 영문/숫자만 사용 시 `VARCHAR` 사용
- **숫자**: 
  - 정수: `BIGINT` (ID), `INT` (일반 숫자)
  - 소수: `DECIMAL(정밀도, 소수점)`
- **불린**: `BIT` (0: false, 1: true)

### 5.3. 보안 고려사항

- **민감 정보 암호화**: 
  - `face_vector`, `face_image_url`: 암호화 저장
  - `pass_ci`: 암호화 저장
- **인덱스 최적화**: 자주 조회되는 컬럼에 인덱스 생성
- **Soft Delete**: 중요한 데이터는 `is_active` 플래그로 관리

---

## 6. 비기능 요구사항 (Non-Functional Requirements)

### 6.1. 성능 및 안정성

#### 응답 속도
- **API 응답**: 500ms 이내 (안면 인식 외부 API 제외)
- **페이지 로딩**: 초기 로딩 2초 이내
- **이미지 로딩**: Lazy Loading 적용

#### 오프라인 대응
- 현장 네트워크가 끊겨도 앱이 죽지 않아야 함
- 네트워크 복구 시 데이터 동기화 로직 구현 권장
- 로컬 스토리지 활용한 임시 데이터 저장

#### 이미지 최적화
- 모바일에서 사진 업로드 시 클라이언트 사이드에서 이미지 리사이징/압축 후 전송
- 최대 1MB 이하로 제한
- 서버 사이드에서도 추가 압축 처리

#### 동시 접속자
- 현장당 최대 100명 동시 접속 지원
- 전체 시스템 최대 1,000명 동시 접속 지원

### 6.2. 보안 (Security)

#### 데이터 보안
- **민감 정보**: 안면 데이터, 주민번호(전자계약 시) 등 민감 정보는 반드시 **DB 암호화** 저장
- **비밀번호**: 해시 알고리즘 사용 (BCrypt 등)
- **세션 관리**: JWT 토큰 만료 시간 설정 (Access Token: 1시간, Refresh Token: 7일)

#### 통신 보안
- 전 구간 SSL (HTTPS) 적용 필수
- API 통신 시 인증 토큰 필수
- CORS 정책 적용

#### 접근 제어
- 역할 기반 접근 제어 (RBAC) 구현
- API 엔드포인트별 권한 검증
- 관리자 기능은 추가 인증 요구

### 6.3. API 통신 규약

#### RESTful API 표준 준수
- HTTP 메서드: GET, POST, PUT, DELETE, PATCH
- 리소스 명명: 명사 사용 (예: `/api/users`, `/api/sites`)
- 버전 관리: `/api/v1/` 형태로 관리

#### 에러 핸들링
- HTTP Status Code 명확히 구분:
  - `200`: 성공
  - `400`: 잘못된 요청
  - `401`: 인증 실패
  - `403`: 권한 없음
  - `404`: 리소스 없음
  - `500`: 서버 오류
- 클라이언트에게는 알기 쉬운 메시지로 변환하여 전달
  - 예: "네트워크를 확인해주세요"
  - 예: "로그인이 필요합니다"
  - 예: "권한이 없습니다"

#### 응답 형식
```json
// 성공 응답
{
  "success": true,
  "data": { ... },
  "message": "처리되었습니다"
}

// 에러 응답
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": { ... }
  }
}
```

### 6.4. 로깅 및 모니터링

#### 로깅
- 모든 API 요청/응답 로깅
- 에러 발생 시 상세 로그 기록
- 민감 정보는 로그에서 제외

#### 모니터링
- 서버 리소스 모니터링 (CPU, Memory, Disk)
- API 응답 시간 모니터링
- 에러율 모니터링

### 6.5. 확장성

#### 수평 확장
- 무상태(Stateless) 아키텍처로 설계
- 로드 밸런싱 지원

#### 데이터베이스
- 인덱스 최적화
- 파티셔닝 고려 (대용량 데이터 시)

---

## 7. 개발 시 고려사항 (Tips for Junior Devs)

### 7.1. 프론트엔드 개발 팁

#### 반응형 UI 구현
- CSS Media Query보다는 Vue의 `computed` 속성이나 `Vuetify`의 `display` 헬퍼 클래스를 활용
- 컴포넌트 레벨에서 렌더링을 제어하는 것이 유지보수에 좋음
- 예: `v-if="mdAndUp"`, `v-show="$vuetify.display.mdAndUp"`

#### 상태 관리
- Pinia Store를 활용하여 전역 상태 관리
- API 호출은 Store의 Action에서 처리
- 컴포넌트는 Store의 State를 구독하여 UI 업데이트

#### 이미지 처리
- `canvas` API를 활용한 클라이언트 사이드 이미지 리사이징
- FileReader API로 Base64 변환
- 업로드 전 파일 크기 검증

### 7.2. 백엔드 개발 팁

#### MSSQL 날짜 처리
- MSSQL의 `DATETIME`과 자바의 `LocalDateTime` 매핑 시 Timezone 이슈 발생 가능
- DB 연결 URL에 `sendTimeAsDatetime=false` 옵션 확인
- 또는 `java.sql.Timestamp` 사용 고려

#### JPA 사용 시 주의사항
- N+1 문제 방지를 위한 `@EntityGraph` 또는 `JOIN FETCH` 활용
- 대용량 데이터 조회 시 `PagingAndSortingRepository` 활용
- 트랜잭션 범위 최소화

#### 예외 처리
- 전역 예외 핸들러(`@ControllerAdvice`) 구현
- 커스텀 예외 클래스 정의
- 사용자 친화적인 에러 메시지 반환

### 7.3. 외부 API 연동 팁

#### 페이스넷 연동
- 외부 API는 언제든 실패할 수 있음
- `try-catch`로 감싸고, 실패 시 사용자에게 명확한 피드백 제공
- 예: "잠시 후 다시 시도해주세요"
- 재시도 로직 구현 (Exponential Backoff)
- Circuit Breaker 패턴 고려

#### 비동기 처리
- 외부 API 호출은 비동기로 처리
- `@Async` 또는 `CompletableFuture` 활용
- 로딩 인디케이터 필수 표시

### 7.4. 테스트

#### 단위 테스트
- 핵심 비즈니스 로직에 대한 단위 테스트 작성
- Mock 객체 활용하여 외부 의존성 제거

#### 통합 테스트
- API 엔드포인트에 대한 통합 테스트 작성
- Test Containers를 활용한 DB 테스트

#### E2E 테스트
- 주요 사용자 시나리오에 대한 E2E 테스트 작성
- Cypress 또는 Playwright 활용

---

## 8. 다음 단계 (Next Steps)

### 8.1. 즉시 진행 사항

1. **DB ERD 설계 (물리 모델링)**
   - 위의 스키마를 기반으로 ERD 도구를 사용하여 시각화
   - 테이블 간 관계 명확히 정의
   - 인덱스 전략 수립

2. **API 명세서 작성 (Swagger)**
   - 모든 API 엔드포인트 정의
   - Request/Response 스키마 정의
   - 인증/인가 방식 명시
   - 에러 응답 정의

3. **안면인식 예외 처리 시퀀스 다이어그램**
   - 정상 플로우
   - 실패 플로우 (인식 실패, API 오류 등)
   - 수동 승인 플로우

### 8.2. 개발 전 준비 사항

1. **개발 환경 구축**
   - 로컬 개발 환경 설정 가이드 작성
   - Docker Compose를 활용한 로컬 환경 구성 (선택사항)

2. **코딩 컨벤션 정의**
   - Java 코딩 스타일 가이드
   - JavaScript/Vue 코딩 스타일 가이드
   - Git 브랜치 전략

3. **CI/CD 파이프라인 구축**
   - 자동 빌드/테스트
   - 자동 배포 (개발/스테이징/운영)

### 8.3. 프로젝트 일정 (예상)

- **Phase 1 (4주)**: 인증/인가, 사용자 관리, 안면 등록
- **Phase 2 (4주)**: 출역 체크, 대시보드, 예외 처리
- **Phase 3 (3주)**: 작업일보, 마이페이지, 이의 제기
- **Phase 4 (2주)**: 통합 테스트, 버그 수정, 성능 최적화
- **Phase 5 (1주)**: 배포 및 안정화

**총 예상 기간**: 14주 (약 3.5개월)

---

## 부록 (Appendix)

### A. 용어 정의

| 용어 | 설명 |
| :--- | :--- |
| **안면 등록** | 사용자의 안면 이미지를 시스템에 등록하는 과정 |
| **안면 인식** | 등록된 안면과 촬영된 안면을 비교하여 일치 여부를 판단 |
| **출역 체크** | 현장 도착 시 안면인식을 통한 출역 기록 생성 |
| **공수** | 작업량을 나타내는 단위 (1.0 = 1일, 0.5 = 반일) |
| **이의 제기** | 출역 기록에 대한 노무자의 이의 신청 |

### B. 참고 자료

- Vue.js 3 공식 문서: https://vuejs.org/
- Spring Boot 공식 문서: https://spring.io/projects/spring-boot
- MSSQL 공식 문서: https://docs.microsoft.com/ko-kr/sql/
- Vuetify 공식 문서: https://vuetifyjs.com/

### C. 변경 이력

| 버전 | 일자 | 작성자 | 변경 내용 |
| :--- | :--- | :--- | :--- |
| 1.0 | 2025-12-13 | 개발 PM (Gemini) | 초안 작성 |

---

**문서 끝**

