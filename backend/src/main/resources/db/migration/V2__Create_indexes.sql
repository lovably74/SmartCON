-- MariaDB 성능 최적화를 위한 추가 인덱스 및 제약조건
-- V2: 인덱스 및 제약조건 추가

-- =============================================================================
-- 복합 인덱스 (Composite Indexes) - 자주 함께 사용되는 컬럼들
-- =============================================================================

-- 테넌트 관련 복합 인덱스
ALTER TABLE tenants 
ADD INDEX idx_tenants_status_created (status, created_at),
ADD INDEX idx_tenants_plan_status (subscription_plan, status);

-- 사용자 관련 복합 인덱스
ALTER TABLE users 
ADD INDEX idx_users_tenant_active (tenant_id, is_active),
ADD INDEX idx_users_provider_active (provider, is_active),
ADD INDEX idx_users_email_active (email, is_active),
ADD INDEX idx_users_tenant_role_lookup (tenant_id, email, is_active);

-- 사용자 역할 관련 인덱스
ALTER TABLE user_roles
ADD INDEX idx_user_roles_role_granted (role, granted_at),
ADD INDEX idx_user_roles_granted_by (granted_by);

-- 구독 결제 관련 복합 인덱스
ALTER TABLE subscription_billing
ADD INDEX idx_billing_tenant_status (tenant_id, payment_status),
ADD INDEX idx_billing_status_date (payment_status, billing_date),
ADD INDEX idx_billing_tenant_period_status (tenant_id, billing_period_start, payment_status),
ADD INDEX idx_billing_pg_transaction (pg_provider, pg_transaction_id);

-- 출근 기록 관련 복합 인덱스
ALTER TABLE attendance_logs
ADD INDEX idx_attendance_user_date (user_id, work_date),
ADD INDEX idx_attendance_tenant_user_date (tenant_id, user_id, work_date),
ADD INDEX idx_attendance_site_date (site_id, work_date),
ADD INDEX idx_attendance_auth_type (auth_type, created_at);

-- =============================================================================
-- 전문 검색 인덱스 (Full-Text Search Indexes)
-- =============================================================================

-- 테넌트 검색용 전문 인덱스 (회사명, 대표자명으로 검색)
ALTER TABLE tenants 
ADD FULLTEXT INDEX ft_tenants_search (company_name, ceo_name);

-- 사용자 검색용 전문 인덱스 (이름, 이메일로 검색)
ALTER TABLE users 
ADD FULLTEXT INDEX ft_users_search (name, email);

-- =============================================================================
-- 성능 최적화를 위한 추가 제약조건
-- =============================================================================

-- 테넌트 테이블 제약조건
ALTER TABLE tenants
ADD CONSTRAINT chk_tenants_max_sites CHECK (max_sites > 0 AND max_sites <= 1000),
ADD CONSTRAINT chk_tenants_max_users CHECK (max_users > 0 AND max_users <= 10000),
ADD CONSTRAINT chk_tenants_business_number_format CHECK (business_number REGEXP '^[0-9]{3}-[0-9]{2}-[0-9]{5}$');

-- 사용자 테이블 제약조건
ALTER TABLE users
ADD CONSTRAINT chk_users_email_format CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
ADD CONSTRAINT chk_users_phone_format CHECK (phone_number IS NULL OR phone_number REGEXP '^[0-9-+()\\s]+$'),
ADD CONSTRAINT chk_users_login_failure_count CHECK (login_failure_count >= 0 AND login_failure_count <= 10);

-- 구독 결제 테이블 제약조건
ALTER TABLE subscription_billing
ADD CONSTRAINT chk_billing_period_valid CHECK (billing_period_start <= billing_period_end),
ADD CONSTRAINT chk_billing_amounts_positive CHECK (
    plan_amount >= 0 AND 
    base_amount >= 0 AND 
    usage_amount >= 0 AND 
    total_amount >= 0
),
ADD CONSTRAINT chk_billing_counts_valid CHECK (
    sites_count >= 0 AND 
    users_count >= 0 AND 
    storage_usage_gb >= 0
),
ADD CONSTRAINT chk_billing_payment_attempts CHECK (payment_attempts >= 0 AND payment_attempts <= 10);

-- 출근 기록 테이블 제약조건
ALTER TABLE attendance_logs
ADD CONSTRAINT chk_attendance_check_times CHECK (
    check_in_at IS NULL OR check_out_at IS NULL OR check_in_at <= check_out_at
),
ADD CONSTRAINT chk_attendance_man_day CHECK (man_day >= 0 AND man_day <= 2.0),
ADD CONSTRAINT chk_attendance_confidence CHECK (
    confidence_score IS NULL OR (confidence_score >= 0.0 AND confidence_score <= 1.0)
),
ADD CONSTRAINT chk_attendance_coordinates CHECK (
    (check_in_latitude IS NULL AND check_in_longitude IS NULL) OR
    (check_in_latitude BETWEEN -90 AND 90 AND check_in_longitude BETWEEN -180 AND 180)
);

-- =============================================================================
-- 파티셔닝을 위한 준비 (대용량 데이터 처리)
-- =============================================================================

-- 출근 기록 테이블의 월별 파티셔닝을 위한 인덱스
-- (실제 파티셔닝은 운영 환경에서 데이터 증가에 따라 적용)
ALTER TABLE attendance_logs
ADD INDEX idx_attendance_partition_helper (work_date, tenant_id);

-- 구독 결제 테이블의 연도별 파티셔닝을 위한 인덱스
ALTER TABLE subscription_billing
ADD INDEX idx_billing_partition_helper (billing_date, tenant_id);

-- =============================================================================
-- 통계 및 분석을 위한 인덱스
-- =============================================================================

-- 슈퍼관리자 대시보드 통계용 인덱스
ALTER TABLE tenants
ADD INDEX idx_tenants_stats (status, created_at, subscription_plan);

-- 월별 결제 통계용 인덱스
ALTER TABLE subscription_billing
ADD INDEX idx_billing_monthly_stats (
    YEAR(billing_date), 
    MONTH(billing_date), 
    payment_status
);

-- 일별 출근 통계용 인덱스
ALTER TABLE attendance_logs
ADD INDEX idx_attendance_daily_stats (
    work_date, 
    tenant_id, 
    status
);

-- =============================================================================
-- 외래키 제약조건 강화
-- =============================================================================

-- 사용자 역할 테이블의 외래키 제약조건 수정 (CASCADE 동작 명시)
ALTER TABLE user_roles
DROP FOREIGN KEY user_roles_ibfk_2;

ALTER TABLE user_roles
ADD CONSTRAINT fk_user_roles_granted_by 
FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE;

-- 구독 결제 테이블의 외래키 제약조건 강화
ALTER TABLE subscription_billing
DROP FOREIGN KEY subscription_billing_ibfk_1;

ALTER TABLE subscription_billing
ADD CONSTRAINT fk_subscription_billing_tenant 
FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 출근 기록 테이블의 외래키 제약조건 강화
ALTER TABLE attendance_logs
DROP FOREIGN KEY attendance_logs_ibfk_1,
DROP FOREIGN KEY attendance_logs_ibfk_2;

ALTER TABLE attendance_logs
ADD CONSTRAINT fk_attendance_logs_tenant 
FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT fk_attendance_logs_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE;

-- =============================================================================
-- 성능 모니터링을 위한 뷰 생성
-- =============================================================================

-- 테넌트별 사용자 수 통계 뷰
CREATE VIEW v_tenant_user_stats AS
SELECT 
    t.id as tenant_id,
    t.company_name,
    t.status as tenant_status,
    COUNT(u.id) as total_users,
    COUNT(CASE WHEN u.is_active = TRUE THEN 1 END) as active_users,
    COUNT(CASE WHEN ur.role = 'ROLE_HQ' THEN 1 END) as hq_users,
    COUNT(CASE WHEN ur.role = 'ROLE_SITE' THEN 1 END) as site_users,
    COUNT(CASE WHEN ur.role = 'ROLE_WORKER' THEN 1 END) as worker_users
FROM tenants t
LEFT JOIN users u ON t.id = u.tenant_id
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY t.id, t.company_name, t.status;

-- 월별 결제 통계 뷰
CREATE VIEW v_monthly_billing_stats AS
SELECT 
    YEAR(billing_date) as billing_year,
    MONTH(billing_date) as billing_month,
    COUNT(*) as total_billings,
    COUNT(CASE WHEN payment_status = 'SUCCESS' THEN 1 END) as successful_payments,
    COUNT(CASE WHEN payment_status = 'FAILED' THEN 1 END) as failed_payments,
    SUM(CASE WHEN payment_status = 'SUCCESS' THEN total_amount ELSE 0 END) as total_revenue,
    AVG(CASE WHEN payment_status = 'SUCCESS' THEN total_amount END) as avg_payment_amount
FROM subscription_billing
GROUP BY YEAR(billing_date), MONTH(billing_date)
ORDER BY billing_year DESC, billing_month DESC;

-- 일별 출근 통계 뷰
CREATE VIEW v_daily_attendance_stats AS
SELECT 
    work_date,
    tenant_id,
    COUNT(*) as total_records,
    COUNT(CASE WHEN status = 'NORMAL' THEN 1 END) as normal_attendance,
    COUNT(CASE WHEN status = 'LATE' THEN 1 END) as late_attendance,
    COUNT(CASE WHEN status = 'ABSENT' THEN 1 END) as absent_count,
    COUNT(CASE WHEN auth_type = 'FACE' THEN 1 END) as face_auth_count,
    COUNT(CASE WHEN auth_type = 'MANUAL' THEN 1 END) as manual_auth_count,
    SUM(man_day) as total_man_days
FROM attendance_logs
GROUP BY work_date, tenant_id
ORDER BY work_date DESC;