-- 구독 관련 테이블 생성

-- 구독 요금제 테이블
CREATE TABLE subscription_plans (
    plan_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    monthly_price DECIMAL(10,2) NOT NULL,
    yearly_price DECIMAL(10,2),
    max_sites INT NOT NULL,
    max_users INT NOT NULL,
    max_storage_gb INT,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 요금제 기능 테이블
CREATE TABLE plan_features (
    plan_id VARCHAR(50) NOT NULL,
    feature VARCHAR(200) NOT NULL,
    PRIMARY KEY (plan_id, feature),
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id) ON DELETE CASCADE
);

-- 구독 테이블
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    plan_id VARCHAR(50) NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED') DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE,
    next_billing_date DATE,
    billing_cycle ENUM('MONTHLY', 'YEARLY') DEFAULT 'MONTHLY',
    monthly_price DECIMAL(10,2),
    discount_rate DECIMAL(5,2) DEFAULT 0.00,
    auto_renewal BOOLEAN DEFAULT TRUE,
    trial_end_date DATE,
    cancellation_date DATE,
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id),
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_next_billing_date (next_billing_date),
    INDEX idx_trial_end_date (trial_end_date)
);

-- 결제 수단 테이블
CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    type ENUM('CARD', 'BANK_TRANSFER') NOT NULL,
    card_number_masked VARCHAR(20),
    card_holder_name VARCHAR(100),
    expiry_date VARCHAR(5),
    bank_name VARCHAR(50),
    account_number_masked VARCHAR(20),
    account_holder_name VARCHAR(100),
    billing_key VARCHAR(200),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_tenant_active (tenant_id, is_active),
    INDEX idx_billing_key (billing_key)
);

-- 결제 내역 테이블
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    subscription_id BIGINT NOT NULL,
    payment_method_id BIGINT,
    payment_key VARCHAR(200),
    order_id VARCHAR(100) UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2),
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
    billing_period_start DATE,
    billing_period_end DATE,
    paid_at TIMESTAMP,
    failed_reason TEXT,
    pg_response TEXT,
    invoice_number VARCHAR(50),
    tax_invoice_issued BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id) ON DELETE SET NULL,
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_payment_key (payment_key),
    INDEX idx_billing_period (billing_period_start, billing_period_end),
    INDEX idx_tax_invoice (tax_invoice_issued, billing_period_end)
);

-- 기본 요금제 데이터 삽입
INSERT INTO subscription_plans (plan_id, name, description, monthly_price, yearly_price, max_sites, max_users, max_storage_gb, sort_order) VALUES
('basic', 'Basic', '소규모 현장을 위한 기본 요금제', 50000.00, 540000.00, 3, 50, 10, 1),
('standard', 'Standard', '중규모 현장을 위한 표준 요금제', 100000.00, 1080000.00, 10, 200, 50, 2),
('premium', 'Premium', '대규모 현장을 위한 프리미엄 요금제', 200000.00, 2160000.00, 30, 500, 200, 3),
('enterprise', 'Enterprise', '기업용 맞춤형 요금제', 500000.00, 5400000.00, 100, 2000, 1000, 4);

-- 요금제별 기능 삽입
INSERT INTO plan_features (plan_id, feature) VALUES
('basic', '안면인식 출입관리'),
('basic', '기본 대시보드'),
('basic', '모바일 앱'),
('basic', '이메일 지원'),
('standard', '안면인식 출입관리'),
('standard', '고급 대시보드'),
('standard', '모바일 앱'),
('standard', '전자계약'),
('standard', '공사일보'),
('standard', '이메일/전화 지원'),
('premium', '안면인식 출입관리'),
('premium', '고급 대시보드'),
('premium', '모바일 앱'),
('premium', '전자계약'),
('premium', '공사일보'),
('premium', '급여 정산'),
('premium', 'API 연동'),
('premium', '우선 지원'),
('enterprise', '모든 기능'),
('enterprise', '맞춤형 개발'),
('enterprise', '전담 매니저'),
('enterprise', '24/7 지원'),
('enterprise', 'SLA 보장');