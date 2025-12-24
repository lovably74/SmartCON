-- SmartCON Lite 초기 스키마 생성 (MariaDB 10.11 최적화)
-- 슈퍼관리자 모니터링 기능을 위한 기본 테이블들

-- 테넌트 테이블
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
    updated_by BIGINT COMMENT '수정자',
    
    -- 인덱스
    INDEX idx_tenants_business_number (business_number),
    INDEX idx_tenants_status (status),
    INDEX idx_tenants_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테넌트(고객사) 정보';

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT COMMENT '테넌트 ID (슈퍼관리자는 NULL)',
    
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
    
    -- 안면인식 정보
    face_embedding TEXT COMMENT '안면인식 임베딩 데이터',
    profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 외래키 및 제약조건
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE KEY unique_email_per_tenant (IFNULL(tenant_id, 0), email),
    
    -- 인덱스
    INDEX idx_users_tenant_id (tenant_id),
    INDEX idx_users_email (email),
    INDEX idx_users_provider (provider, provider_id),
    INDEX idx_users_active (is_active),
    INDEX idx_users_last_login (last_login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시스템 사용자';

-- 사용자 역할 테이블
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role ENUM('ROLE_SUPER', 'ROLE_HQ', 'ROLE_SITE', 'ROLE_TEAM', 'ROLE_WORKER') NOT NULL COMMENT '역할',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '권한부여일시',
    granted_by BIGINT COMMENT '권한부여자',
    
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL,
    
    -- 인덱스
    INDEX idx_user_roles_role (role),
    INDEX idx_user_roles_granted_at (granted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 역할';

-- 구독 결제 테이블
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
    
    -- 외래키 및 제약조건
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE KEY unique_tenant_billing_period (tenant_id, billing_period_start, billing_period_end),
    
    -- 인덱스
    INDEX idx_subscription_billing_tenant_id (tenant_id),
    INDEX idx_subscription_billing_status (payment_status),
    INDEX idx_subscription_billing_date (billing_date),
    INDEX idx_subscription_billing_period (billing_period_start, billing_period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구독 결제 정보';

-- 출근 기록 테이블 (슈퍼관리자 모니터링용)
CREATE TABLE attendance_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    site_id BIGINT COMMENT '현장 ID',
    
    -- 출근 정보
    check_in_at TIMESTAMP NULL COMMENT '출근시간',
    check_out_at TIMESTAMP NULL COMMENT '퇴근시간',
    work_date DATE NOT NULL COMMENT '근무일자',
    
    -- 인증 정보
    auth_type ENUM('FACE', 'MANUAL') DEFAULT 'FACE' COMMENT '인증방식',
    confidence_score DECIMAL(5, 4) COMMENT '안면인식 신뢰도',
    
    -- 근무 정보
    man_day DECIMAL(3, 2) DEFAULT 1.00 COMMENT '공수',
    status ENUM('NORMAL', 'LATE', 'ABSENT', 'EARLY_LEAVE') DEFAULT 'NORMAL' COMMENT '출근상태',
    
    -- 위치 정보
    check_in_latitude DECIMAL(10, 8) COMMENT '출근 위도',
    check_in_longitude DECIMAL(11, 8) COMMENT '출근 경도',
    check_out_latitude DECIMAL(10, 8) COMMENT '퇴근 위도',
    check_out_longitude DECIMAL(11, 8) COMMENT '퇴근 경도',
    
    -- 감사 정보
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 외래키 및 제약조건
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_attendance_tenant_id (tenant_id),
    INDEX idx_attendance_user_id (user_id),
    INDEX idx_attendance_work_date (work_date),
    INDEX idx_attendance_status (status),
    INDEX idx_attendance_tenant_date (tenant_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='출근 기록';