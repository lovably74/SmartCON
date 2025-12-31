# SmartCON Lite 데이터베이스 스키마 설계

**문서 버전:** 1.0  
**작성일:** 2025년 12월 23일  
**작성자:** Kiro AI Assistant  
**기반 문서:** 상세기능명세서 v3.0, Backend-Architecture v1.0, Platform-API-Specification v1.0

---

## 1. 데이터베이스 설계 개요

### 1.1 설계 원칙
- **멀티테넌트 아키텍처**: 모든 비즈니스 테이블에 `tenant_id` 포함
- **데이터 무결성**: 외래키 제약조건 및 체크 제약조건 적용
- **성능 최적화**: 적절한 인덱스 설계 및 파티셔닝 고려
- **확장성**: 수평/수직 확장을 고려한 정규화 설계
- **보안**: 민감정보 암호화 및 접근 제어

### 1.2 기술 스택
- **DBMS**: MariaDB 10.11 (Primary)
- **캐시**: Redis 7.x (Session, Temporary Data)
- **파일 저장소**: AWS S3 (Documents, Images)
- **마이그레이션**: Flyway
- **ORM**: JPA/Hibernate 6.x

### 1.3 명명 규칙
- **테이블명**: 복수형, snake_case (예: `users`, `attendance_logs`)
- **컬럼명**: snake_case (예: `created_at`, `business_number`)
- **인덱스명**: `idx_{table}_{columns}` (예: `idx_users_tenant_id`)
- **외래키명**: `fk_{table}_{referenced_table}` (예: `fk_users_tenants`)

---

## 2. 핵심 테이블 설계

### 2.1 시스템 관리 테이블

#### 2.1.1 tenants (테넌트/회사)
```sql
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_number VARCHAR(12) NOT NULL UNIQUE COMMENT '사업자등록번호',
    company_name VARCHAR(100) NOT NULL COMMENT '회사명',
    ceo_name VARCHAR(50) COMMENT '대표자명',
    business_type VARCHAR(50) COMMENT '업종',
    business_item VARCHAR(100) COMMENT '업태',
    
    -- 주소 정보
    road_address VARCHAR(200) COMMENT '도로명주소',
    detail_address VARCHAR(100) COMMENT '상세주소',
    zip_code VARCHAR(10) COMMENT '우편번호',
    
    -- 연락처 정보
    phone_number VARCHAR(20) COMMENT '대표전화',
    fax_number VARCHAR(20) COMMENT '팩스번호',
    email VARCHAR(100) COMMENT '대표이메일',
    
    -- 구독 정보
    status ENUM('TRIAL', 'ACTIVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'TRIAL' COMMENT '상태',
    subscription_plan VARCHAR(50) COMMENT '구독플랜',
    max_sites INT DEFAULT 1 COMMENT '최대현장수',
    max_users INT DEFAULT 10 COMMENT '최대사용자수',
    
    -- 결제 정보
    billing_email VARCHAR(100) COMMENT '세금계산서 이메일',
    payment_method ENUM('CARD', 'BANK_TRANSFER') COMMENT '결제방법',
    next_billing_date DATE COMMENT '다음결제일',
    
    -- 파일 정보
    logo_url VARCHAR(500) COMMENT '회사로고 URL',
    stamp_url VARCHAR(500) COMMENT '직인 이미지 URL',
    business_license_url VARCHAR(500) COMMENT '사업자등록증 URL',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자'
);
```

#### 2.1.2 users (사용자)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    email VARCHAR(100) NOT NULL COMMENT '이메일',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    phone_number VARCHAR(20) COMMENT '연락처',
    
    -- 인증 정보
    provider ENUM('LOCAL', 'KAKAO', 'NAVER') NOT NULL DEFAULT 'LOCAL' COMMENT '인증제공자',
    provider_id VARCHAR(100) COMMENT '소셜로그인 ID',
    password_hash VARCHAR(255) COMMENT '비밀번호 해시',
    
    -- 상태 정보
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성상태',
    is_email_verified BOOLEAN DEFAULT FALSE COMMENT '이메일인증여부',
    last_login_at TIMESTAMP NULL COMMENT '마지막로그인시간',
    login_failure_count INT DEFAULT 0 COMMENT '로그인실패횟수',
    locked_until TIMESTAMP NULL COMMENT '계정잠금해제시간',
    
    -- 개인정보 (암호화 저장)
    encrypted_ssn VARCHAR(255) COMMENT '주민번호(암호화)',
    encrypted_bank_account VARCHAR(255) COMMENT '계좌번호(암호화)',
    bank_name VARCHAR(50) COMMENT '은행명',
    account_holder VARCHAR(50) COMMENT '예금주',
    
    -- 주소 정보
    road_address VARCHAR(200) COMMENT '도로명주소',
    detail_address VARCHAR(100) COMMENT '상세주소',
    zip_code VARCHAR(10) COMMENT '우편번호',
    
    -- 프로필 정보
    profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    UNIQUE KEY unique_email_per_tenant (tenant_id, email),
    UNIQUE KEY unique_provider_per_tenant (tenant_id, provider, provider_id)
);
```
#### 2.1.3 user_roles (사용자 역할)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role ENUM('ROLE_SUPER', 'ROLE_HQ', 'ROLE_SITE', 'ROLE_TEAM', 'ROLE_WORKER') NOT NULL COMMENT '역할',
    site_id BIGINT NULL COMMENT '현장 ID (현장별 역할인 경우)',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '권한부여일시',
    granted_by BIGINT COMMENT '권한부여자',
    
    PRIMARY KEY (user_id, role, IFNULL(site_id, 0)),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id)
);
```

#### 2.1.4 refresh_tokens (리프레시 토큰)
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE COMMENT '토큰 해시',
    device_info VARCHAR(200) COMMENT '디바이스 정보',
    ip_address VARCHAR(45) COMMENT 'IP 주소',
    expires_at TIMESTAMP NOT NULL COMMENT '만료시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 2.2 현장 관리 테이블

#### 2.2.1 sites (현장)
```sql
CREATE TABLE sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    name VARCHAR(100) NOT NULL COMMENT '현장명',
    project_name VARCHAR(100) COMMENT '공사명',
    description TEXT COMMENT '현장설명',
    
    -- 주소 정보
    road_address VARCHAR(200) NOT NULL COMMENT '도로명주소',
    detail_address VARCHAR(100) COMMENT '상세주소',
    zip_code VARCHAR(10) COMMENT '우편번호',
    gps_latitude DECIMAL(10, 8) COMMENT 'GPS 위도',
    gps_longitude DECIMAL(11, 8) COMMENT 'GPS 경도',
    
    -- 공사 기간
    start_date DATE NOT NULL COMMENT '공사시작일',
    end_date DATE NOT NULL COMMENT '공사종료일',
    
    -- 관리자 정보
    manager_id BIGINT NOT NULL COMMENT '현장관리자 ID',
    
    -- 운영 설정
    work_start_time TIME DEFAULT '08:00:00' COMMENT '작업시작시간',
    work_end_time TIME DEFAULT '18:00:00' COMMENT '작업종료시간',
    lunch_start_time TIME DEFAULT '12:00:00' COMMENT '점심시작시간',
    lunch_end_time TIME DEFAULT '13:00:00' COMMENT '점심종료시간',
    overtime_allowed BOOLEAN DEFAULT TRUE COMMENT '연장근무허용여부',
    
    -- 안면인식 설정
    face_device_serial VARCHAR(100) COMMENT '안면인식기 시리얼번호',
    face_recognition_threshold DECIMAL(3, 2) DEFAULT 0.80 COMMENT '안면인식 임계값',
    face_sync_time TIME DEFAULT '00:00:00' COMMENT '안면데이터 동기화 시간',
    
    -- 상태 정보
    status ENUM('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '현장상태',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (manager_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);
```

#### 2.2.2 teams (팀)
```sql
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    site_id BIGINT NOT NULL COMMENT '현장 ID',
    
    -- 기본 정보
    name VARCHAR(100) NOT NULL COMMENT '팀명',
    description TEXT COMMENT '팀설명',
    
    -- 팀장 정보
    leader_id BIGINT NOT NULL COMMENT '팀장 ID',
    
    -- 전문 분야
    primary_work_type VARCHAR(50) COMMENT '주요공종',
    secondary_work_types JSON COMMENT '보조공종 목록',
    
    -- 팀 규모
    max_members INT DEFAULT 10 COMMENT '최대팀원수',
    current_members INT DEFAULT 0 COMMENT '현재팀원수',
    
    -- 상태 정보
    status ENUM('ACTIVE', 'INACTIVE', 'DISBANDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '팀상태',
    
    -- 성과 정보
    total_work_days INT DEFAULT 0 COMMENT '총작업일수',
    average_rating DECIMAL(3, 2) DEFAULT 0.00 COMMENT '평균평점',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (leader_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);
```

#### 2.2.3 team_members (팀원)
```sql
CREATE TABLE team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL COMMENT '팀 ID',
    worker_id BIGINT NOT NULL COMMENT '노무자 ID',
    
    -- 가입 정보
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '팀가입일시',
    left_at TIMESTAMP NULL COMMENT '팀탈퇴일시',
    
    -- 역할 정보
    position VARCHAR(50) DEFAULT 'MEMBER' COMMENT '팀내역할',
    work_types JSON COMMENT '담당공종 목록',
    skill_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') DEFAULT 'INTERMEDIATE' COMMENT '기술수준',
    
    -- 상태 정보
    status ENUM('ACTIVE', 'INACTIVE', 'LEFT') NOT NULL DEFAULT 'ACTIVE' COMMENT '멤버상태',
    
    -- 성과 정보
    work_days INT DEFAULT 0 COMMENT '작업일수',
    attendance_rate DECIMAL(5, 2) DEFAULT 0.00 COMMENT '출석률',
    performance_rating DECIMAL(3, 2) DEFAULT 0.00 COMMENT '성과평점',
    
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES users(id),
    UNIQUE KEY unique_active_member (team_id, worker_id, status)
);
```

### 2.3 작업 관리 테이블

#### 2.3.1 work_types (공종 마스터)
```sql
CREATE TABLE work_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NULL COMMENT '테넌트 ID (NULL이면 시스템 기본)',
    
    -- 기본 정보
    code VARCHAR(20) NOT NULL COMMENT '공종코드',
    name VARCHAR(100) NOT NULL COMMENT '공종명',
    description TEXT COMMENT '공종설명',
    category VARCHAR(50) COMMENT '공종분류',
    
    -- 작업 설정
    standard_hours INT DEFAULT 8 COMMENT '표준작업시간',
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM' COMMENT '위험도',
    required_certifications JSON COMMENT '필요자격증 목록',
    
    -- 단가 정보
    base_hourly_rate DECIMAL(10, 2) COMMENT '기본시급',
    overtime_rate_multiplier DECIMAL(3, 2) DEFAULT 1.50 COMMENT '연장근무 배율',
    night_rate_multiplier DECIMAL(3, 2) DEFAULT 1.30 COMMENT '야간근무 배율',
    holiday_rate_multiplier DECIMAL(3, 2) DEFAULT 2.00 COMMENT '휴일근무 배율',
    
    -- 상태 정보
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성상태',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    UNIQUE KEY unique_code_per_tenant (IFNULL(tenant_id, 0), code)
);
```

#### 2.3.2 work_assignments (작업 배정)
```sql
CREATE TABLE work_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    site_id BIGINT NOT NULL COMMENT '현장 ID',
    team_id BIGINT NOT NULL COMMENT '팀 ID',
    work_date DATE NOT NULL COMMENT '작업일',
    
    -- 작업 내용
    work_type_id BIGINT NOT NULL COMMENT '공종 ID',
    work_location VARCHAR(200) COMMENT '작업위치',
    description TEXT COMMENT '작업내용',
    special_instructions TEXT COMMENT '특별지시사항',
    
    -- 인력 정보
    required_workers INT NOT NULL COMMENT '필요인원',
    assigned_workers INT DEFAULT 0 COMMENT '배정인원',
    confirmed_workers INT DEFAULT 0 COMMENT '확정인원',
    
    -- 시간 정보
    planned_start_time TIME COMMENT '계획시작시간',
    planned_end_time TIME COMMENT '계획종료시간',
    estimated_hours DECIMAL(4, 2) COMMENT '예상작업시간',
    
    -- 단가 정보
    hourly_rate DECIMAL(10, 2) NOT NULL COMMENT '시급',
    total_estimated_cost DECIMAL(12, 2) COMMENT '총예상비용',
    
    -- 상태 관리
    status ENUM('REQUESTED', 'VIEWED', 'ACCEPTED', 'REJECTED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') 
           NOT NULL DEFAULT 'REQUESTED' COMMENT '배정상태',
    
    -- 응답 정보
    response_deadline TIMESTAMP COMMENT '응답기한',
    team_response TEXT COMMENT '팀장응답내용',
    rejection_reason TEXT COMMENT '거부사유',
    
    -- 타임스탬프
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '요청일시',
    responded_at TIMESTAMP NULL COMMENT '응답일시',
    approved_at TIMESTAMP NULL COMMENT '승인일시',
    completed_at TIMESTAMP NULL COMMENT '완료일시',
    
    -- 감사 정보
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (team_id) REFERENCES teams(id),
    FOREIGN KEY (work_type_id) REFERENCES work_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);
```

#### 2.3.3 work_assignment_workers (작업 배정 노무자)
```sql
CREATE TABLE work_assignment_workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_assignment_id BIGINT NOT NULL COMMENT '작업배정 ID',
    worker_id BIGINT NOT NULL COMMENT '노무자 ID',
    
    -- 배정 정보
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '배정일시',
    confirmed_at TIMESTAMP NULL COMMENT '확정일시',
    
    -- 상태 정보
    status ENUM('ASSIGNED', 'CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'ASSIGNED' COMMENT '배정상태',
    
    -- 실제 작업 정보
    actual_start_time TIMESTAMP NULL COMMENT '실제시작시간',
    actual_end_time TIMESTAMP NULL COMMENT '실제종료시간',
    actual_hours DECIMAL(4, 2) COMMENT '실제작업시간',
    
    FOREIGN KEY (work_assignment_id) REFERENCES work_assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES users(id),
    UNIQUE KEY unique_assignment_worker (work_assignment_id, worker_id)
);
```

### 2.4 출역 관리 테이블

#### 2.4.1 attendance_logs (출역 기록)
```sql
CREATE TABLE attendance_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    worker_id BIGINT NOT NULL COMMENT '노무자 ID',
    site_id BIGINT NOT NULL COMMENT '현장 ID',
    work_date DATE NOT NULL COMMENT '작업일',
    
    -- 출퇴근 시간
    check_in_time TIMESTAMP NULL COMMENT '출근시간',
    check_out_time TIMESTAMP NULL COMMENT '퇴근시간',
    
    -- 계산된 시간
    total_hours DECIMAL(4, 2) DEFAULT 0.00 COMMENT '총근무시간',
    regular_hours DECIMAL(4, 2) DEFAULT 0.00 COMMENT '정규근무시간',
    overtime_hours DECIMAL(4, 2) DEFAULT 0.00 COMMENT '연장근무시간',
    night_hours DECIMAL(4, 2) DEFAULT 0.00 COMMENT '야간근무시간',
    holiday_hours DECIMAL(4, 2) DEFAULT 0.00 COMMENT '휴일근무시간',
    
    -- 안면인식 정보
    face_matched BOOLEAN DEFAULT FALSE COMMENT '안면인식성공여부',
    face_confidence DECIMAL(4, 3) COMMENT '안면인식정확도',
    face_check_in_image_url VARCHAR(500) COMMENT '출근시 안면인식 이미지',
    face_check_out_image_url VARCHAR(500) COMMENT '퇴근시 안면인식 이미지',
    
    -- GPS 정보
    check_in_gps_latitude DECIMAL(10, 8) COMMENT '출근시 GPS 위도',
    check_in_gps_longitude DECIMAL(11, 8) COMMENT '출근시 GPS 경도',
    check_out_gps_latitude DECIMAL(10, 8) COMMENT '퇴근시 GPS 위도',
    check_out_gps_longitude DECIMAL(11, 8) COMMENT '퇴근시 GPS 경도',
    
    -- 상태 정보
    status ENUM('PENDING', 'CONFIRMED', 'FINALIZED', 'DISPUTED') NOT NULL DEFAULT 'PENDING' COMMENT '출역상태',
    
    -- 수동 보정 정보
    manual_adjustment BOOLEAN DEFAULT FALSE COMMENT '수동보정여부',
    adjustment_reason TEXT COMMENT '보정사유',
    adjusted_by BIGINT COMMENT '보정자',
    adjusted_at TIMESTAMP NULL COMMENT '보정일시',
    
    -- 급여 계산 정보
    hourly_rate DECIMAL(10, 2) COMMENT '적용시급',
    calculated_pay DECIMAL(12, 2) COMMENT '계산된급여',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (worker_id) REFERENCES users(id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (adjusted_by) REFERENCES users(id),
    
    UNIQUE KEY unique_worker_site_date (worker_id, site_id, work_date)
);
```

#### 2.4.2 face_embeddings (안면 임베딩)
```sql
CREATE TABLE face_embeddings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    worker_id BIGINT NOT NULL COMMENT '노무자 ID',
    
    -- 임베딩 데이터
    embedding_vector JSON NOT NULL COMMENT '512차원 임베딩 벡터',
    embedding_hash VARCHAR(64) NOT NULL COMMENT '임베딩 해시값',
    
    -- 원본 이미지 정보
    original_image_url VARCHAR(500) NOT NULL COMMENT '원본 이미지 URL',
    processed_image_url VARCHAR(500) COMMENT '처리된 이미지 URL',
    
    -- 품질 정보
    quality_score DECIMAL(4, 3) NOT NULL COMMENT '이미지 품질 점수',
    confidence_score DECIMAL(4, 3) NOT NULL COMMENT '임베딩 신뢰도',
    
    -- 메타데이터
    image_width INT COMMENT '이미지 너비',
    image_height INT COMMENT '이미지 높이',
    face_bbox JSON COMMENT '얼굴 경계박스 좌표',
    
    -- 상태 정보
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성상태',
    is_primary BOOLEAN DEFAULT FALSE COMMENT '주요임베딩여부',
    
    -- 동기화 정보
    last_synced_at TIMESTAMP NULL COMMENT '마지막동기화시간',
    sync_status ENUM('PENDING', 'SYNCED', 'FAILED') DEFAULT 'PENDING' COMMENT '동기화상태',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (worker_id) REFERENCES users(id),
    
    UNIQUE KEY unique_embedding_hash (embedding_hash)
);
```

### 2.5 계약 관리 테이블

#### 2.5.1 contract_templates (계약서 템플릿)
```sql
CREATE TABLE contract_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NULL COMMENT '테넌트 ID (NULL이면 시스템 기본)',
    
    -- 기본 정보
    template_id VARCHAR(50) NOT NULL COMMENT '템플릿 ID',
    name VARCHAR(100) NOT NULL COMMENT '템플릿명',
    description TEXT COMMENT '템플릿설명',
    
    -- 템플릿 분류
    type ENUM('LABOR', 'SAFETY', 'PRIVACY', 'NDA') NOT NULL COMMENT '계약서유형',
    category VARCHAR(50) COMMENT '계약서분류',
    
    -- 템플릿 내용
    template_content LONGTEXT NOT NULL COMMENT '템플릿 내용 (HTML)',
    template_variables JSON COMMENT '템플릿 변수 정의',
    
    -- 서명 설정
    signature_positions JSON COMMENT '서명 위치 좌표',
    required_signatures JSON COMMENT '필요한 서명 목록',
    
    -- 버전 관리
    version VARCHAR(20) NOT NULL DEFAULT '1.0' COMMENT '템플릿 버전',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성상태',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    
    UNIQUE KEY unique_template_per_tenant (IFNULL(tenant_id, 0), template_id, version)
);
```
#### 2.5.2 contracts (계약서)
```sql
CREATE TABLE contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    contract_number VARCHAR(50) NOT NULL COMMENT '계약서번호',
    worker_id BIGINT NOT NULL COMMENT '노무자 ID',
    site_id BIGINT NOT NULL COMMENT '현장 ID',
    template_id BIGINT NOT NULL COMMENT '템플릿 ID',
    
    -- 계약 내용
    type ENUM('LABOR', 'SAFETY', 'PRIVACY', 'NDA') NOT NULL COMMENT '계약서유형',
    title VARCHAR(200) NOT NULL COMMENT '계약서제목',
    
    -- 계약 기간
    contract_start_date DATE NOT NULL COMMENT '계약시작일',
    contract_end_date DATE NOT NULL COMMENT '계약종료일',
    
    -- 근무 조건
    work_type_id BIGINT COMMENT '공종 ID',
    daily_wage DECIMAL(10, 2) COMMENT '일당',
    hourly_rate DECIMAL(10, 2) COMMENT '시급',
    standard_work_hours INT DEFAULT 8 COMMENT '표준근무시간',
    overtime_rate DECIMAL(10, 2) COMMENT '연장근무시급',
    
    -- 계약서 파일
    original_pdf_url VARCHAR(500) COMMENT '원본 PDF URL',
    signed_pdf_url VARCHAR(500) COMMENT '서명된 PDF URL',
    
    -- 서명 정보
    signature_data LONGTEXT COMMENT '서명 데이터 (Base64)',
    signature_hash VARCHAR(255) COMMENT '서명 해시값',
    signature_timestamp TIMESTAMP NULL COMMENT '서명시간',
    signature_ip_address VARCHAR(45) COMMENT '서명시 IP주소',
    signature_gps_latitude DECIMAL(10, 8) COMMENT '서명시 GPS 위도',
    signature_gps_longitude DECIMAL(11, 8) COMMENT '서명시 GPS 경도',
    signature_device_info VARCHAR(200) COMMENT '서명 디바이스 정보',
    
    -- 상태 관리
    status ENUM('DRAFT', 'PENDING_SIGNATURE', 'SIGNED', 'EXPIRED', 'CANCELLED', 'TERMINATED') 
           NOT NULL DEFAULT 'DRAFT' COMMENT '계약상태',
    
    -- 알림 정보
    notification_sent_at TIMESTAMP NULL COMMENT '알림발송시간',
    reminder_count INT DEFAULT 0 COMMENT '리마인더발송횟수',
    signature_deadline TIMESTAMP NULL COMMENT '서명기한',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (worker_id) REFERENCES users(id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (template_id) REFERENCES contract_templates(id),
    FOREIGN KEY (work_type_id) REFERENCES work_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    
    UNIQUE KEY unique_contract_number (tenant_id, contract_number)
);
```

### 2.6 일보 및 보고서 테이블

#### 2.6.1 daily_reports (공사일보)
```sql
CREATE TABLE daily_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 기본 정보
    site_id BIGINT NOT NULL COMMENT '현장 ID',
    report_date DATE NOT NULL COMMENT '일보날짜',
    report_number VARCHAR(50) COMMENT '일보번호',
    
    -- 날씨 정보 (기상청 API 연동)
    weather_condition VARCHAR(50) COMMENT '날씨상태',
    temperature_min DECIMAL(4, 1) COMMENT '최저기온',
    temperature_max DECIMAL(4, 1) COMMENT '최고기온',
    humidity INT COMMENT '습도',
    wind_speed DECIMAL(4, 1) COMMENT '풍속',
    precipitation DECIMAL(5, 2) COMMENT '강수량',
    weather_api_response JSON COMMENT '기상청 API 응답 원본',
    
    -- 인력 현황
    total_workers INT DEFAULT 0 COMMENT '총투입인원',
    present_workers INT DEFAULT 0 COMMENT '출근인원',
    absent_workers INT DEFAULT 0 COMMENT '결근인원',
    late_workers INT DEFAULT 0 COMMENT '지각인원',
    
    -- 작업 현황
    work_progress_rate DECIMAL(5, 2) COMMENT '작업진행률',
    completed_tasks TEXT COMMENT '완료작업',
    ongoing_tasks TEXT COMMENT '진행중작업',
    planned_tasks TEXT COMMENT '계획작업',
    
    -- 장비 현황
    equipment_status JSON COMMENT '장비현황',
    material_usage JSON COMMENT '자재사용현황',
    
    -- 안전 관리
    safety_incidents INT DEFAULT 0 COMMENT '안전사고건수',
    safety_meeting_held BOOLEAN DEFAULT FALSE COMMENT '안전교육실시여부',
    safety_notes TEXT COMMENT '안전관리사항',
    
    -- 특이사항
    special_notes TEXT COMMENT '특이사항',
    issues TEXT COMMENT '문제사항',
    improvements TEXT COMMENT '개선사항',
    
    -- 첨부 파일
    photos JSON COMMENT '작업사진 URL 목록',
    documents JSON COMMENT '첨부문서 URL 목록',
    
    -- 승인 정보
    status ENUM('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'DRAFT' COMMENT '일보상태',
    submitted_at TIMESTAMP NULL COMMENT '제출일시',
    approved_at TIMESTAMP NULL COMMENT '승인일시',
    approved_by BIGINT COMMENT '승인자',
    rejection_reason TEXT COMMENT '반려사유',
    
    -- PDF 생성
    pdf_url VARCHAR(500) COMMENT '일보 PDF URL',
    pdf_generated_at TIMESTAMP NULL COMMENT 'PDF 생성일시',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    updated_by BIGINT COMMENT '수정자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    
    UNIQUE KEY unique_site_report_date (site_id, report_date)
);
```

#### 2.6.2 daily_report_team_submissions (팀별 일보 제출)
```sql
CREATE TABLE daily_report_team_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_report_id BIGINT NOT NULL COMMENT '공사일보 ID',
    team_id BIGINT NOT NULL COMMENT '팀 ID',
    
    -- 작업 내용
    work_type_id BIGINT NOT NULL COMMENT '공종 ID',
    work_location VARCHAR(200) COMMENT '작업위치',
    work_description TEXT NOT NULL COMMENT '작업내용',
    work_progress_rate DECIMAL(5, 2) COMMENT '작업진행률',
    
    -- 인력 정보
    assigned_workers INT NOT NULL COMMENT '배정인원',
    present_workers INT NOT NULL COMMENT '출근인원',
    work_hours DECIMAL(4, 2) COMMENT '작업시간',
    
    -- 작업 사진
    before_photos JSON COMMENT '작업전 사진 URL 목록',
    after_photos JSON COMMENT '작업후 사진 URL 목록',
    progress_photos JSON COMMENT '진행중 사진 URL 목록',
    
    -- 특이사항
    issues TEXT COMMENT '문제사항',
    safety_incidents TEXT COMMENT '안전사고',
    equipment_problems TEXT COMMENT '장비문제',
    weather_impact TEXT COMMENT '날씨영향',
    
    -- 제출 정보
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '제출일시',
    submitted_by BIGINT NOT NULL COMMENT '제출자',
    
    FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id),
    FOREIGN KEY (work_type_id) REFERENCES work_types(id),
    FOREIGN KEY (submitted_by) REFERENCES users(id),
    
    UNIQUE KEY unique_report_team (daily_report_id, team_id)
);
```

### 2.7 정산 및 결제 테이블

#### 2.7.1 payroll_periods (급여 정산 기간)
```sql
CREATE TABLE payroll_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 정산 기간
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL COMMENT '정산주기',
    period_start_date DATE NOT NULL COMMENT '정산시작일',
    period_end_date DATE NOT NULL COMMENT '정산종료일',
    period_name VARCHAR(50) NOT NULL COMMENT '정산기간명',
    
    -- 정산 상태
    status ENUM('OPEN', 'CALCULATING', 'CALCULATED', 'APPROVED', 'PAID', 'CLOSED') 
           NOT NULL DEFAULT 'OPEN' COMMENT '정산상태',
    
    -- 집계 정보
    total_workers INT DEFAULT 0 COMMENT '총대상자수',
    total_work_days DECIMAL(10, 2) DEFAULT 0.00 COMMENT '총공수',
    total_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT '총급여액',
    total_deductions DECIMAL(15, 2) DEFAULT 0.00 COMMENT '총공제액',
    total_net_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT '총실수령액',
    
    -- 처리 일정
    calculation_started_at TIMESTAMP NULL COMMENT '계산시작일시',
    calculation_completed_at TIMESTAMP NULL COMMENT '계산완료일시',
    approved_at TIMESTAMP NULL COMMENT '승인일시',
    approved_by BIGINT COMMENT '승인자',
    payment_date DATE COMMENT '지급일',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자',
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    
    UNIQUE KEY unique_tenant_period (tenant_id, period_start_date, period_end_date)
);
```

#### 2.7.2 payroll_details (급여 명세)
```sql
CREATE TABLE payroll_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_period_id BIGINT NOT NULL COMMENT '급여정산기간 ID',
    worker_id BIGINT NOT NULL COMMENT '노무자 ID',
    site_id BIGINT NOT NULL COMMENT '현장 ID',
    
    -- 근무 집계
    total_work_days DECIMAL(4, 2) DEFAULT 0.00 COMMENT '총근무일수',
    regular_hours DECIMAL(6, 2) DEFAULT 0.00 COMMENT '정규근무시간',
    overtime_hours DECIMAL(6, 2) DEFAULT 0.00 COMMENT '연장근무시간',
    night_hours DECIMAL(6, 2) DEFAULT 0.00 COMMENT '야간근무시간',
    holiday_hours DECIMAL(6, 2) DEFAULT 0.00 COMMENT '휴일근무시간',
    
    -- 급여 계산
    base_hourly_rate DECIMAL(10, 2) NOT NULL COMMENT '기본시급',
    regular_pay DECIMAL(12, 2) DEFAULT 0.00 COMMENT '기본급',
    overtime_pay DECIMAL(12, 2) DEFAULT 0.00 COMMENT '연장수당',
    night_pay DECIMAL(12, 2) DEFAULT 0.00 COMMENT '야간수당',
    holiday_pay DECIMAL(12, 2) DEFAULT 0.00 COMMENT '휴일수당',
    bonus DECIMAL(12, 2) DEFAULT 0.00 COMMENT '상여금',
    allowances DECIMAL(12, 2) DEFAULT 0.00 COMMENT '각종수당',
    gross_pay DECIMAL(12, 2) DEFAULT 0.00 COMMENT '총급여',
    
    -- 공제 항목
    income_tax DECIMAL(12, 2) DEFAULT 0.00 COMMENT '소득세',
    local_tax DECIMAL(12, 2) DEFAULT 0.00 COMMENT '지방소득세',
    national_pension DECIMAL(12, 2) DEFAULT 0.00 COMMENT '국민연금',
    health_insurance DECIMAL(12, 2) DEFAULT 0.00 COMMENT '건강보험',
    employment_insurance DECIMAL(12, 2) DEFAULT 0.00 COMMENT '고용보험',
    other_deductions DECIMAL(12, 2) DEFAULT 0.00 COMMENT '기타공제',
    total_deductions DECIMAL(12, 2) DEFAULT 0.00 COMMENT '총공제액',
    
    -- 실수령액
    net_pay DECIMAL(12, 2) DEFAULT 0.00 COMMENT '실수령액',
    
    -- 지급 정보
    payment_method ENUM('BANK_TRANSFER', 'CASH', 'CHECK') DEFAULT 'BANK_TRANSFER' COMMENT '지급방법',
    bank_name VARCHAR(50) COMMENT '은행명',
    account_number VARCHAR(50) COMMENT '계좌번호',
    account_holder VARCHAR(50) COMMENT '예금주',
    
    -- 급여명세서
    payslip_pdf_url VARCHAR(500) COMMENT '급여명세서 PDF URL',
    payslip_generated_at TIMESTAMP NULL COMMENT '명세서생성일시',
    
    -- 상태 정보
    status ENUM('CALCULATED', 'APPROVED', 'PAID', 'DISPUTED') NOT NULL DEFAULT 'CALCULATED' COMMENT '지급상태',
    paid_at TIMESTAMP NULL COMMENT '지급일시',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (payroll_period_id) REFERENCES payroll_periods(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES users(id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    
    UNIQUE KEY unique_payroll_worker (payroll_period_id, worker_id, site_id)
);
```

#### 2.7.3 subscription_billing (구독 결제)
```sql
CREATE TABLE subscription_billing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 결제 기간
    billing_period_start DATE NOT NULL COMMENT '결제기간시작일',
    billing_period_end DATE NOT NULL COMMENT '결제기간종료일',
    billing_date DATE NOT NULL COMMENT '결제일',
    
    -- 구독 정보
    subscription_plan VARCHAR(50) NOT NULL COMMENT '구독플랜',
    plan_amount DECIMAL(10, 2) NOT NULL COMMENT '플랜금액',
    
    -- 사용량 기반 요금
    sites_count INT DEFAULT 0 COMMENT '현장수',
    users_count INT DEFAULT 0 COMMENT '사용자수',
    storage_usage_gb DECIMAL(10, 2) DEFAULT 0.00 COMMENT '저장공간사용량(GB)',
    api_calls_count BIGINT DEFAULT 0 COMMENT 'API 호출수',
    
    -- 요금 계산
    base_amount DECIMAL(10, 2) NOT NULL COMMENT '기본요금',
    usage_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '사용량요금',
    discount_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '할인금액',
    tax_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '부가세',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '총결제금액',
    
    -- 결제 정보
    payment_method ENUM('CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT') NOT NULL COMMENT '결제수단',
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED') 
                   NOT NULL DEFAULT 'PENDING' COMMENT '결제상태',
    
    -- PG사 연동 정보
    pg_provider VARCHAR(50) COMMENT 'PG사명',
    pg_transaction_id VARCHAR(100) COMMENT 'PG거래번호',
    pg_approval_number VARCHAR(50) COMMENT 'PG승인번호',
    pg_response JSON COMMENT 'PG응답데이터',
    
    -- 결제 시도 정보
    payment_attempts INT DEFAULT 0 COMMENT '결제시도횟수',
    last_payment_attempt_at TIMESTAMP NULL COMMENT '마지막결제시도일시',
    payment_completed_at TIMESTAMP NULL COMMENT '결제완료일시',
    
    -- 세금계산서
    tax_invoice_issued BOOLEAN DEFAULT FALSE COMMENT '세금계산서발행여부',
    tax_invoice_number VARCHAR(50) COMMENT '세금계산서번호',
    tax_invoice_issued_at TIMESTAMP NULL COMMENT '세금계산서발행일시',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    
    UNIQUE KEY unique_tenant_billing_period (tenant_id, billing_period_start, billing_period_end)
);
```

### 2.8 알림 및 로그 테이블

#### 2.8.1 notifications (알림)
```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    
    -- 수신자 정보
    user_id BIGINT NOT NULL COMMENT '수신자 ID',
    
    -- 알림 내용
    type ENUM('WORK_ASSIGNMENT', 'ATTENDANCE', 'CONTRACT', 'PAYMENT', 'SYSTEM', 'SAFETY') 
         NOT NULL COMMENT '알림유형',
    title VARCHAR(200) NOT NULL COMMENT '알림제목',
    message TEXT NOT NULL COMMENT '알림내용',
    
    -- 알림 채널
    channels JSON NOT NULL COMMENT '발송채널 (PUSH, SMS, EMAIL)',
    
    -- 관련 엔티티
    related_entity_type VARCHAR(50) COMMENT '관련엔티티타입',
    related_entity_id BIGINT COMMENT '관련엔티티ID',
    
    -- 발송 정보
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '발송예정시간',
    sent_at TIMESTAMP NULL COMMENT '발송완료시간',
    
    -- 상태 정보
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED') 
           NOT NULL DEFAULT 'PENDING' COMMENT '알림상태',
    
    -- 읽음 정보
    read_at TIMESTAMP NULL COMMENT '읽음시간',
    
    -- 발송 결과
    delivery_results JSON COMMENT '채널별 발송결과',
    failure_reason TEXT COMMENT '실패사유',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 2.8.2 audit_logs (감사 로그)
```sql
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT COMMENT '테넌트 ID',
    
    -- 사용자 정보
    user_id BIGINT COMMENT '사용자 ID',
    user_email VARCHAR(100) COMMENT '사용자 이메일',
    user_name VARCHAR(50) COMMENT '사용자 이름',
    
    -- 액션 정보
    action VARCHAR(50) NOT NULL COMMENT '액션 (CREATE, UPDATE, DELETE, LOGIN, etc.)',
    entity_type VARCHAR(50) NOT NULL COMMENT '대상 엔티티 타입',
    entity_id BIGINT COMMENT '대상 엔티티 ID',
    
    -- 변경 내용
    old_values JSON COMMENT '변경전 값',
    new_values JSON COMMENT '변경후 값',
    
    -- 요청 정보
    ip_address VARCHAR(45) COMMENT 'IP 주소',
    user_agent VARCHAR(500) COMMENT 'User Agent',
    request_method VARCHAR(10) COMMENT 'HTTP 메서드',
    request_url VARCHAR(500) COMMENT '요청 URL',
    
    -- 결과 정보
    success BOOLEAN NOT NULL DEFAULT TRUE COMMENT '성공여부',
    error_message TEXT COMMENT '오류메시지',
    
    -- 시간 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 2.8.3 system_logs (시스템 로그)
```sql
CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- 로그 기본 정보
    level ENUM('TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL') NOT NULL COMMENT '로그레벨',
    logger_name VARCHAR(200) NOT NULL COMMENT '로거명',
    message TEXT NOT NULL COMMENT '로그메시지',
    
    -- 예외 정보
    exception_class VARCHAR(200) COMMENT '예외클래스',
    exception_message TEXT COMMENT '예외메시지',
    stack_trace LONGTEXT COMMENT '스택트레이스',
    
    -- 컨텍스트 정보
    tenant_id BIGINT COMMENT '테넌트 ID',
    user_id BIGINT COMMENT '사용자 ID',
    session_id VARCHAR(100) COMMENT '세션 ID',
    request_id VARCHAR(100) COMMENT '요청 ID',
    
    -- 서버 정보
    server_name VARCHAR(100) COMMENT '서버명',
    server_ip VARCHAR(45) COMMENT '서버 IP',
    thread_name VARCHAR(100) COMMENT '스레드명',
    
    -- 시간 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 3. 인덱스 설계

### 3.1 성능 최적화 인덱스
```sql
-- 테넌트별 데이터 조회 최적화
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_sites_tenant_id ON sites(tenant_id);
CREATE INDEX idx_teams_tenant_id ON teams(tenant_id);
CREATE INDEX idx_work_assignments_tenant_id ON work_assignments(tenant_id);
CREATE INDEX idx_attendance_logs_tenant_id ON attendance_logs(tenant_id);
CREATE INDEX idx_contracts_tenant_id ON contracts(tenant_id);
CREATE INDEX idx_daily_reports_tenant_id ON daily_reports(tenant_id);

-- 출역 관련 조회 최적화
CREATE INDEX idx_attendance_logs_worker_date ON attendance_logs(worker_id, work_date);
CREATE INDEX idx_attendance_logs_site_date ON attendance_logs(site_id, work_date);
CREATE INDEX idx_attendance_logs_status ON attendance_logs(status);
CREATE INDEX idx_attendance_logs_face_matched ON attendance_logs(face_matched);

-- 작업 배정 조회 최적화
CREATE INDEX idx_work_assignments_site_date ON work_assignments(site_id, work_date);
CREATE INDEX idx_work_assignments_team_status ON work_assignments(team_id, status);
CREATE INDEX idx_work_assignments_status_date ON work_assignments(status, work_date);

-- 계약서 조회 최적화
CREATE INDEX idx_contracts_worker_status ON contracts(worker_id, status);
CREATE INDEX idx_contracts_site_status ON contracts(site_id, status);
CREATE INDEX idx_contracts_signature_deadline ON contracts(signature_deadline);

-- 안면인식 최적화
CREATE INDEX idx_face_embeddings_worker_active ON face_embeddings(worker_id, is_active);
CREATE INDEX idx_face_embeddings_sync_status ON face_embeddings(sync_status);

-- 알림 조회 최적화
CREATE INDEX idx_notifications_user_status ON notifications(user_id, status);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);

-- 감사 로그 조회 최적화
CREATE INDEX idx_audit_logs_tenant_created ON audit_logs(tenant_id, created_at);
CREATE INDEX idx_audit_logs_user_created ON audit_logs(user_id, created_at);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- 시스템 로그 조회 최적화
CREATE INDEX idx_system_logs_level_created ON system_logs(level, created_at);
CREATE INDEX idx_system_logs_tenant_created ON system_logs(tenant_id, created_at);
```

### 3.2 복합 인덱스
```sql
-- 멀티테넌트 + 날짜 범위 조회
CREATE INDEX idx_attendance_tenant_site_date ON attendance_logs(tenant_id, site_id, work_date);
CREATE INDEX idx_work_assignments_tenant_site_date ON work_assignments(tenant_id, site_id, work_date);
CREATE INDEX idx_daily_reports_tenant_site_date ON daily_reports(tenant_id, site_id, report_date);

-- 상태 + 날짜 조회
CREATE INDEX idx_contracts_status_deadline ON contracts(status, signature_deadline);
CREATE INDEX idx_notifications_status_scheduled ON notifications(status, scheduled_at);

-- 사용자 + 현장 + 날짜 조회
CREATE INDEX idx_attendance_worker_site_date ON attendance_logs(worker_id, site_id, work_date);
```

---

## 4. 파티셔닝 전략

### 4.1 날짜 기반 파티셔닝
```sql
-- attendance_logs 테이블 월별 파티셔닝
ALTER TABLE attendance_logs 
PARTITION BY RANGE (YEAR(work_date) * 100 + MONTH(work_date)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    -- ... 계속
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- audit_logs 테이블 월별 파티셔닝
ALTER TABLE audit_logs 
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    -- ... 계속
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- system_logs 테이블 일별 파티셔닝 (로그 데이터가 많은 경우)
ALTER TABLE system_logs 
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p20250101 VALUES LESS THAN (TO_DAYS('2025-01-02')),
    PARTITION p20250102 VALUES LESS THAN (TO_DAYS('2025-01-03')),
    -- ... 계속
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 4.2 테넌트 기반 파티셔닝 (선택적)
```sql
-- 대용량 테넌트가 있는 경우 테넌트별 파티셔닝 고려
-- 예: attendance_logs를 주요 테넌트별로 분할
ALTER TABLE attendance_logs 
PARTITION BY LIST (tenant_id) (
    PARTITION p_tenant_1 VALUES IN (1),
    PARTITION p_tenant_2 VALUES IN (2),
    PARTITION p_tenant_3 VALUES IN (3),
    PARTITION p_others VALUES IN (DEFAULT)
);
```

---

## 5. 데이터 보안 및 암호화

### 5.1 컬럼 레벨 암호화
```sql
-- 민감정보 암호화 함수 (MariaDB 10.1.3+)
-- 주민번호 암호화 저장
INSERT INTO users (encrypted_ssn) VALUES (AES_ENCRYPT('123-45-67890', 'encryption_key'));

-- 주민번호 복호화 조회
SELECT AES_DECRYPT(encrypted_ssn, 'encryption_key') as ssn FROM users WHERE id = 1;

-- 계좌번호 암호화 저장
INSERT INTO users (encrypted_bank_account) VALUES (AES_ENCRYPT('1234-567-890123', 'encryption_key'));
```

### 5.2 접근 제어
```sql
-- 테넌트별 데이터 접근 제어를 위한 뷰 생성
CREATE VIEW v_tenant_users AS
SELECT * FROM users 
WHERE tenant_id = @current_tenant_id;

-- 민감정보 마스킹 뷰
CREATE VIEW v_users_masked AS
SELECT 
    id,
    tenant_id,
    email,
    name,
    phone_number,
    CONCAT(LEFT(AES_DECRYPT(encrypted_ssn, 'key'), 6), '-*******') as masked_ssn,
    bank_name,
    CONCAT(LEFT(AES_DECRYPT(encrypted_bank_account, 'key'), 4), '-***-******') as masked_account,
    created_at,
    updated_at
FROM users;
```

---

## 6. 백업 및 복구 전략

### 6.1 백업 정책
```sql
-- 일일 전체 백업 (스크립트 예시)
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/smartcon"
DB_NAME="smartcon_prod"

# 전체 데이터베이스 백업
mysqldump --single-transaction --routines --triggers \
  --user=backup_user --password=backup_pass \
  $DB_NAME > $BACKUP_DIR/full_backup_$DATE.sql

# 압축
gzip $BACKUP_DIR/full_backup_$DATE.sql

# 7일 이상 된 백업 파일 삭제
find $BACKUP_DIR -name "full_backup_*.sql.gz" -mtime +7 -delete
```

### 6.2 증분 백업
```sql
-- 바이너리 로그 기반 증분 백업
-- my.cnf 설정
[mysqld]
log-bin=mysql-bin
binlog-format=ROW
expire_logs_days=7

-- 증분 백업 스크립트
#!/bin/bash
BACKUP_DIR="/backup/smartcon/incremental"
LAST_BACKUP_FILE="$BACKUP_DIR/last_backup_position.txt"

if [ -f $LAST_BACKUP_FILE ]; then
    LAST_POSITION=$(cat $LAST_BACKUP_FILE)
    mysqlbinlog --start-position=$LAST_POSITION /var/lib/mysql/mysql-bin.* > $BACKUP_DIR/incremental_$(date +%Y%m%d_%H%M%S).sql
fi

# 현재 바이너리 로그 위치 저장
mysql -e "SHOW MASTER STATUS\G" | grep Position | awk '{print $2}' > $LAST_BACKUP_FILE
```

---

## 7. 모니터링 및 성능 튜닝

### 7.1 성능 모니터링 쿼리
```sql
-- 슬로우 쿼리 확인
SELECT 
    query_time,
    lock_time,
    rows_sent,
    rows_examined,
    sql_text
FROM mysql.slow_log 
WHERE start_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY query_time DESC
LIMIT 10;

-- 테이블별 사용량 통계
SELECT 
    table_schema,
    table_name,
    table_rows,
    data_length,
    index_length,
    (data_length + index_length) as total_size
FROM information_schema.tables 
WHERE table_schema = 'smartcon_prod'
ORDER BY total_size DESC;

-- 인덱스 사용률 확인
SELECT 
    object_schema,
    object_name,
    index_name,
    count_read,
    count_write,
    count_read / (count_read + count_write) * 100 as read_ratio
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE object_schema = 'smartcon_prod'
ORDER BY count_read DESC;
```

### 7.2 성능 최적화 설정
```sql
-- MariaDB 성능 최적화 설정 (my.cnf)
[mysqld]
# 메모리 설정
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_log_buffer_size = 64M
query_cache_size = 128M
query_cache_type = 1

# 연결 설정
max_connections = 200
max_connect_errors = 10000
connect_timeout = 10
wait_timeout = 600
interactive_timeout = 600

# InnoDB 설정
innodb_file_per_table = 1
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT
innodb_thread_concurrency = 8

# 슬로우 쿼리 로그
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
log_queries_not_using_indexes = 1
```

---

## 8. 마이그레이션 스크립트

### 8.1 초기 스키마 생성 (V1__Initial_schema.sql)
```sql
-- Flyway 마이그레이션 스크립트 예시
-- V1__Initial_schema.sql

-- 테넌트 테이블 생성
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_number VARCHAR(12) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    -- ... (위에서 정의한 전체 스키마)
);

-- 초기 데이터 삽입
INSERT INTO tenants (business_number, company_name, status) 
VALUES ('000-00-00000', 'SmartCON 시스템', 'ACTIVE');

-- 시스템 기본 공종 데이터
INSERT INTO work_types (code, name, category, base_hourly_rate) VALUES
('CONCRETE', '콘크리트공', '구조공사', 25000),
('REBAR', '철근공', '구조공사', 28000),
('FORMWORK', '거푸집공', '구조공사', 24000),
('MASONRY', '조적공', '마감공사', 22000),
('PLASTER', '미장공', '마감공사', 20000);

-- 시스템 기본 계약서 템플릿
INSERT INTO contract_templates (template_id, name, type, template_content) VALUES
('STD_LABOR_2025', '표준 근로계약서 2025', 'LABOR', '<html>...</html>'),
('STD_SAFETY_2025', '안전교육 서약서 2025', 'SAFETY', '<html>...</html>');
```

### 8.2 인덱스 추가 (V2__Add_indexes.sql)
```sql
-- V2__Add_indexes.sql
-- 성능 최적화를 위한 인덱스 추가

-- 기본 인덱스
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);

-- 출역 관련 인덱스
CREATE INDEX idx_attendance_logs_worker_date ON attendance_logs(worker_id, work_date);
CREATE INDEX idx_attendance_logs_site_date ON attendance_logs(site_id, work_date);

-- 작업 배정 인덱스
CREATE INDEX idx_work_assignments_site_date ON work_assignments(site_id, work_date);
CREATE INDEX idx_work_assignments_team_status ON work_assignments(team_id, status);

-- 계약서 인덱스
CREATE INDEX idx_contracts_worker_status ON contracts(worker_id, status);
CREATE INDEX idx_contracts_deadline ON contracts(signature_deadline);
```

---

## 9. 결론

이 데이터베이스 스키마 설계는 SmartCON Lite의 모든 기능 요구사항을 충족하도록 설계되었습니다:

### 9.1 주요 특징
- **멀티테넌트 지원**: 모든 비즈니스 테이블에 tenant_id 포함
- **확장성**: 파티셔닝과 인덱스 최적화로 대용량 데이터 처리
- **보안**: 민감정보 암호화 및 접근 제어
- **성능**: 적절한 인덱스와 쿼리 최적화
- **감사**: 모든 중요 작업에 대한 로그 기록

### 9.2 테이블 요약
- **시스템 관리**: 4개 테이블 (tenants, users, user_roles, refresh_tokens)
- **현장 관리**: 3개 테이블 (sites, teams, team_members)
- **작업 관리**: 3개 테이블 (work_types, work_assignments, work_assignment_workers)
- **출역 관리**: 2개 테이블 (attendance_logs, face_embeddings)
- **계약 관리**: 2개 테이블 (contract_templates, contracts)
- **일보 관리**: 2개 테이블 (daily_reports, daily_report_team_submissions)
- **정산 관리**: 3개 테이블 (payroll_periods, payroll_details, subscription_billing)
- **시스템 로그**: 3개 테이블 (notifications, audit_logs, system_logs)

### 9.3 다음 단계
1. **개발 환경 구축**: Docker를 이용한 로컬 개발 환경 설정
2. **마이그레이션 실행**: Flyway를 이용한 스키마 생성
3. **테스트 데이터**: 개발 및 테스트용 샘플 데이터 생성
4. **성능 테스트**: 대용량 데이터 환경에서의 성능 검증
5. **보안 검토**: 데이터 암호화 및 접근 제어 검증

이 스키마는 SmartCON Lite의 성공적인 구현을 위한 견고한 기반을 제공합니다.

---

**문서 끝**