-- 테스트 환경 초기 데이터 설정
-- MariaDB 호환 H2 인메모리 데이터베이스용 초기화 스크립트

-- 테스트용 테넌트 데이터 (각각 고유한 사업자번호 사용)
INSERT INTO tenants (id, business_number, company_name, ceo_name, status, subscription_plan, max_sites, max_users, phone_number, email, road_address, detail_address, zip_code, billing_email, payment_method, created_at, updated_at) VALUES
(100, '100-00-00001', '초기테스트회사1', '김대표', 'ACTIVE', 'STANDARD', 5, 50, '02-1111-1111', 'test1@testcompany.com', '서울시 강남구 테헤란로 100', '100호', '06100', 'billing1@testcompany.com', 'CARD', NOW(), NOW()),
(101, '100-00-00002', '초기테스트회사2', '이대표', 'ACTIVE', 'PREMIUM', 10, 100, '02-2222-2222', 'test2@testcompany.com', '서울시 서초구 서초대로 200', '200호', '06200', 'billing2@testcompany.com', 'BANK_TRANSFER', NOW(), NOW());

-- 테스트용 사용자 데이터
INSERT INTO users (id, tenant_id, name, email, phone_number, provider_id, provider, password_hash, is_active, is_email_verified, profile_image_url, face_embedding, login_failure_count, created_at, updated_at) VALUES
(100, 100, '관리자1', 'admin1@testcompany.com', '010-1111-1111', NULL, 'LOCAL', '$2a$10$dummyhashfortest', true, true, NULL, NULL, 0, NOW(), NOW()),
(101, 100, '현장관리자1', 'site1@testcompany.com', '010-1111-2222', NULL, 'LOCAL', '$2a$10$dummyhashfortest', true, true, NULL, NULL, 0, NOW(), NOW()),
(102, 101, '관리자2', 'admin2@testcompany.com', '010-2222-1111', NULL, 'LOCAL', '$2a$10$dummyhashfortest', true, true, NULL, NULL, 0, NOW(), NOW());

-- 테스트용 결제 기록 데이터
INSERT INTO subscription_billing (id, tenant_id, billing_period_start, billing_period_end, billing_date, subscription_plan, plan_amount, sites_count, users_count, storage_usage_gb, base_amount, usage_amount, discount_amount, tax_amount, total_amount, payment_method, payment_status, pg_provider, pg_transaction_id, pg_approval_number, payment_attempts, last_payment_attempt_at, payment_completed_at, failure_reason, created_at, updated_at) VALUES
(100, 100, '2024-11-01', '2024-11-30', '2024-12-01', 'STANDARD', 50000.00, 3, 25, 2.5, 50000.00, 5000.00, 0.00, 5500.00, 60500.00, 'CARD', 'SUCCESS', 'TOSS', 'TXN_20241201_001', 'APPR_001', 1, '2024-12-01 10:00:00', '2024-12-01 10:05:00', NULL, NOW(), NOW()),
(101, 100, '2024-12-01', '2024-12-31', '2025-01-01', 'STANDARD', 50000.00, 3, 28, 3.2, 50000.00, 6000.00, 0.00, 5600.00, 61600.00, 'CARD', 'PENDING', NULL, NULL, NULL, 0, NULL, NULL, NULL, NOW(), NOW()),
(102, 101, '2024-11-01', '2024-11-30', '2024-12-01', 'PREMIUM', 100000.00, 8, 75, 15.8, 100000.00, 25000.00, 5000.00, 12000.00, 132000.00, 'BANK_TRANSFER', 'SUCCESS', 'NICE', 'TXN_20241201_002', 'APPR_002', 1, '2024-12-01 14:00:00', '2024-12-01 14:30:00', NULL, NOW(), NOW());