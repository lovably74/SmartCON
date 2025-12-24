-- 개발 및 테스트용 초기 데이터 삽입
-- V3: 초기 데이터 시딩

-- =============================================================================
-- 슈퍼관리자 사용자 생성
-- =============================================================================

-- 슈퍼관리자 사용자 (tenant_id = NULL)
INSERT INTO users (
    tenant_id, email, name, phone_number, 
    provider, password_hash, is_active, is_email_verified,
    created_at, updated_at
) VALUES (
    NULL, 'super@smartcon.kr', '슈퍼관리자', '02-1234-5678',
    'LOCAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password: secret
    TRUE, TRUE,
    NOW(), NOW()
);

-- 슈퍼관리자 역할 부여
INSERT INTO user_roles (user_id, role, granted_at, granted_by) 
SELECT id, 'ROLE_SUPER', NOW(), NULL 
FROM users WHERE email = 'super@smartcon.kr';

-- =============================================================================
-- 테스트용 테넌트 데이터
-- =============================================================================

-- 테스트 테넌트 1: 활성 상태
INSERT INTO tenants (
    business_number, company_name, ceo_name, business_type, business_item,
    road_address, detail_address, zip_code,
    phone_number, email,
    status, subscription_plan, max_sites, max_users,
    billing_email, payment_method, next_billing_date,
    created_at, updated_at
) VALUES (
    '123-45-67890', '(주)스마트건설', '김대표', '건설업', '일반건축공사업',
    '서울특별시 강남구 테헤란로 123', '스마트빌딩 10층', '06142',
    '02-1234-5678', 'info@smartcon-test.kr',
    'ACTIVE', 'STANDARD', 5, 50,
    'billing@smartcon-test.kr', 'CARD', '2025-01-24',
    NOW(), NOW()
);

-- 테스트 테넌트 2: 체험 상태
INSERT INTO tenants (
    business_number, company_name, ceo_name, business_type, business_item,
    road_address, detail_address, zip_code,
    phone_number, email,
    status, subscription_plan, max_sites, max_users,
    billing_email, payment_method, next_billing_date,
    created_at, updated_at
) VALUES (
    '987-65-43210', '대한건설(주)', '박대표', '건설업', '토목공사업',
    '부산광역시 해운대구 센텀중앙로 456', '센텀타워 15층', '48058',
    '051-9876-5432', 'contact@daehan-const.kr',
    'TRIAL', 'BASIC', 1, 10,
    'finance@daehan-const.kr', 'BANK_TRANSFER', '2025-01-10',
    NOW(), NOW()
);

-- 테스트 테넌트 3: 일시정지 상태
INSERT INTO tenants (
    business_number, company_name, ceo_name, business_type, business_item,
    road_address, detail_address, zip_code,
    phone_number, email,
    status, subscription_plan, max_sites, max_users,
    billing_email, payment_method,
    created_at, updated_at
) VALUES (
    '555-66-77888', '미래건축', '이대표', '건설업', '건축공사업',
    '대구광역시 수성구 동대구로 789', '미래빌딩 5층', '42190',
    '053-5555-6666', 'info@future-arch.kr',
    'SUSPENDED', 'PREMIUM', 10, 100,
    'admin@future-arch.kr', 'CARD',
    NOW(), NOW()
);

-- =============================================================================
-- 테스트용 사용자 데이터
-- =============================================================================

-- 테넌트 1의 사용자들
INSERT INTO users (
    tenant_id, email, name, phone_number,
    provider, password_hash, is_active, is_email_verified,
    created_at, updated_at
) VALUES 
-- HQ 관리자
((SELECT id FROM tenants WHERE business_number = '123-45-67890'), 
 'hq@smartcon-test.kr', '본사관리자', '02-1234-5679',
 'LOCAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
 TRUE, TRUE, NOW(), NOW()),

-- 현장 관리자
((SELECT id FROM tenants WHERE business_number = '123-45-67890'), 
 'site@smartcon-test.kr', '현장관리자', '010-1111-2222',
 'LOCAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
 TRUE, TRUE, NOW(), NOW()),

-- 팀장
((SELECT id FROM tenants WHERE business_number = '123-45-67890'), 
 'team@smartcon-test.kr', '김팀장', '010-3333-4444',
 'LOCAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
 TRUE, TRUE, NOW(), NOW()),

-- 작업자들
((SELECT id FROM tenants WHERE business_number = '123-45-67890'), 
 'worker1@smartcon-test.kr', '박작업자', '010-5555-6666',
 'LOCAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
 TRUE, TRUE, NOW(), NOW()),

((SELECT id FROM tenants WHERE business_number = '123-45-67890'), 
 'worker2@smartcon-test.kr', '최작업자', '010-7777-8888',
 'KAKAO', 'kakao_12345', TRUE, TRUE, NOW(), NOW());

-- 테넌트 2의 사용자들
INSERT INTO users (
    tenant_id, email, name, phone_number,
    provider, password_hash, is_active, is_email_verified,
    created_at, updated_at
) VALUES 
-- HQ 관리자
((SELECT id FROM tenants WHERE business_number = '987-65-43210'), 
 'admin@daehan-const.kr', '대한관리자', '051-9876-5433',
 'LOCAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
 TRUE, TRUE, NOW(), NOW()),

-- 현장 관리자
((SELECT id FROM tenants WHERE business_number = '987-65-43210'), 
 'field@daehan-const.kr', '현장책임자', '010-2222-3333',
 'NAVER', 'naver_67890', TRUE, TRUE, NOW(), NOW());

-- =============================================================================
-- 사용자 역할 부여
-- =============================================================================

-- 테넌트 1 사용자 역할
INSERT INTO user_roles (user_id, role, granted_at, granted_by) VALUES
((SELECT id FROM users WHERE email = 'hq@smartcon-test.kr'), 'ROLE_HQ', NOW(), 
 (SELECT id FROM users WHERE email = 'super@smartcon.kr')),
((SELECT id FROM users WHERE email = 'site@smartcon-test.kr'), 'ROLE_SITE', NOW(), 
 (SELECT id FROM users WHERE email = 'hq@smartcon-test.kr')),
((SELECT id FROM users WHERE email = 'team@smartcon-test.kr'), 'ROLE_TEAM', NOW(), 
 (SELECT id FROM users WHERE email = 'site@smartcon-test.kr')),
((SELECT id FROM users WHERE email = 'worker1@smartcon-test.kr'), 'ROLE_WORKER', NOW(), 
 (SELECT id FROM users WHERE email = 'team@smartcon-test.kr')),
((SELECT id FROM users WHERE email = 'worker2@smartcon-test.kr'), 'ROLE_WORKER', NOW(), 
 (SELECT id FROM users WHERE email = 'team@smartcon-test.kr'));

-- 테넌트 2 사용자 역할
INSERT INTO user_roles (user_id, role, granted_at, granted_by) VALUES
((SELECT id FROM users WHERE email = 'admin@daehan-const.kr'), 'ROLE_HQ', NOW(), 
 (SELECT id FROM users WHERE email = 'super@smartcon.kr')),
((SELECT id FROM users WHERE email = 'field@daehan-const.kr'), 'ROLE_SITE', NOW(), 
 (SELECT id FROM users WHERE email = 'admin@daehan-const.kr'));

-- =============================================================================
-- 테스트용 구독 결제 데이터
-- =============================================================================

-- 테넌트 1의 성공적인 결제 기록들
INSERT INTO subscription_billing (
    tenant_id, billing_period_start, billing_period_end, billing_date,
    subscription_plan, plan_amount, sites_count, users_count,
    base_amount, usage_amount, tax_amount, total_amount,
    payment_method, payment_status, pg_provider, pg_transaction_id, pg_approval_number,
    payment_completed_at, created_at, updated_at
) VALUES 
-- 12월 결제 (성공)
((SELECT id FROM tenants WHERE business_number = '123-45-67890'),
 '2024-12-01', '2024-12-31', '2024-12-24',
 'STANDARD', 150000.00, 3, 25,
 150000.00, 50000.00, 20000.00, 220000.00,
 'CARD', 'SUCCESS', 'TOSS', 'toss_20241224_001', 'TOSS123456',
 '2024-12-24 09:15:30', NOW(), NOW()),

-- 11월 결제 (성공)
((SELECT id FROM tenants WHERE business_number = '123-45-67890'),
 '2024-11-01', '2024-11-30', '2024-11-24',
 'STANDARD', 150000.00, 2, 20,
 150000.00, 30000.00, 18000.00, 198000.00,
 'CARD', 'SUCCESS', 'TOSS', 'toss_20241124_001', 'TOSS123455',
 '2024-11-24 10:22:15', NOW(), NOW());

-- 테넌트 2의 결제 기록들
INSERT INTO subscription_billing (
    tenant_id, billing_period_start, billing_period_end, billing_date,
    subscription_plan, plan_amount, sites_count, users_count,
    base_amount, usage_amount, tax_amount, total_amount,
    payment_method, payment_status, payment_attempts,
    created_at, updated_at
) VALUES 
-- 12월 결제 (대기중 - 체험판)
((SELECT id FROM tenants WHERE business_number = '987-65-43210'),
 '2024-12-01', '2024-12-31', '2025-01-10',
 'BASIC', 50000.00, 1, 5,
 50000.00, 0.00, 5000.00, 55000.00,
 'BANK_TRANSFER', 'PENDING', 0,
 NOW(), NOW());

-- 테넌트 3의 실패한 결제 기록
INSERT INTO subscription_billing (
    tenant_id, billing_period_start, billing_period_end, billing_date,
    subscription_plan, plan_amount, sites_count, users_count,
    base_amount, usage_amount, tax_amount, total_amount,
    payment_method, payment_status, payment_attempts, last_payment_attempt_at,
    created_at, updated_at
) VALUES 
-- 12월 결제 (실패)
((SELECT id FROM tenants WHERE business_number = '555-66-77888'),
 '2024-12-01', '2024-12-31', '2024-12-24',
 'PREMIUM', 300000.00, 5, 50,
 300000.00, 100000.00, 40000.00, 440000.00,
 'CARD', 'FAILED', 3, '2024-12-24 14:30:00',
 NOW(), NOW());

-- =============================================================================
-- 테스트용 출근 기록 데이터
-- =============================================================================

-- 최근 일주일간의 출근 기록 (테넌트 1 사용자들)
INSERT INTO attendance_logs (
    tenant_id, user_id, work_date, 
    check_in_at, check_out_at, auth_type, confidence_score,
    man_day, status, created_at, updated_at
) VALUES 
-- 오늘 출근 기록
((SELECT id FROM tenants WHERE business_number = '123-45-67890'),
 (SELECT id FROM users WHERE email = 'worker1@smartcon-test.kr'),
 CURDATE(),
 CONCAT(CURDATE(), ' 08:30:00'), CONCAT(CURDATE(), ' 17:30:00'),
 'FACE', 0.9850, 1.00, 'NORMAL', NOW(), NOW()),

((SELECT id FROM tenants WHERE business_number = '123-45-67890'),
 (SELECT id FROM users WHERE email = 'worker2@smartcon-test.kr'),
 CURDATE(),
 CONCAT(CURDATE(), ' 08:45:00'), NULL,
 'FACE', 0.9720, 1.00, 'LATE', NOW(), NOW()),

-- 어제 출근 기록
((SELECT id FROM tenants WHERE business_number = '123-45-67890'),
 (SELECT id FROM users WHERE email = 'worker1@smartcon-test.kr'),
 DATE_SUB(CURDATE(), INTERVAL 1 DAY),
 CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 08:15:00'), 
 CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 17:45:00'),
 'FACE', 0.9890, 1.00, 'NORMAL', NOW(), NOW()),

((SELECT id FROM tenants WHERE business_number = '123-45-67890'),
 (SELECT id FROM users WHERE email = 'worker2@smartcon-test.kr'),
 DATE_SUB(CURDATE(), INTERVAL 1 DAY),
 CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 08:20:00'), 
 CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 17:20:00'),
 'FACE', 0.9650, 1.00, 'NORMAL', NOW(), NOW());

-- 테넌트 2의 출근 기록
INSERT INTO attendance_logs (
    tenant_id, user_id, work_date, 
    check_in_at, check_out_at, auth_type, confidence_score,
    man_day, status, created_at, updated_at
) VALUES 
-- 오늘 출근 기록
((SELECT id FROM tenants WHERE business_number = '987-65-43210'),
 (SELECT id FROM users WHERE email = 'field@daehan-const.kr'),
 CURDATE(),
 CONCAT(CURDATE(), ' 07:30:00'), CONCAT(CURDATE(), ' 18:00:00'),
 'MANUAL', NULL, 1.00, 'NORMAL', NOW(), NOW());

-- =============================================================================
-- 개발 환경 확인용 데이터 요약
-- =============================================================================

-- 생성된 데이터 요약을 위한 임시 뷰 (개발 확인용)
CREATE TEMPORARY TABLE temp_data_summary AS
SELECT 
    'Tenants' as entity_type,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT status) as statuses
FROM tenants
UNION ALL
SELECT 
    'Users' as entity_type,
    COUNT(*) as count,
    CONCAT(
        COUNT(CASE WHEN tenant_id IS NULL THEN 1 END), ' super, ',
        COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END), ' tenant users'
    ) as statuses
FROM users
UNION ALL
SELECT 
    'User Roles' as entity_type,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT role) as statuses
FROM user_roles
UNION ALL
SELECT 
    'Billing Records' as entity_type,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT payment_status) as statuses
FROM subscription_billing
UNION ALL
SELECT 
    'Attendance Logs' as entity_type,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT status) as statuses
FROM attendance_logs;

-- 개발자를 위한 로그인 정보 주석
/*
=============================================================================
개발 및 테스트용 로그인 정보
=============================================================================

슈퍼관리자:
- 이메일: super@smartcon.kr
- 비밀번호: secret

테넌트 1 ((주)스마트건설):
- HQ 관리자: hq@smartcon-test.kr / secret
- 현장 관리자: site@smartcon-test.kr / secret  
- 팀장: team@smartcon-test.kr / secret
- 작업자: worker1@smartcon-test.kr / secret
- 작업자: worker2@smartcon-test.kr (카카오 로그인)

테넌트 2 (대한건설(주)):
- HQ 관리자: admin@daehan-const.kr / secret
- 현장 관리자: field@daehan-const.kr (네이버 로그인)

모든 LOCAL 계정의 비밀번호는 'secret' 입니다.
=============================================================================
*/