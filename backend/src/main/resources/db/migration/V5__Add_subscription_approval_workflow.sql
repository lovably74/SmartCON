-- 구독 승인 워크플로우 관련 테이블 및 필드 추가

-- 1. 구독 상태 enum에 새로운 상태 추가
ALTER TABLE subscriptions 
MODIFY COLUMN status ENUM('PENDING_APPROVAL', 'REJECTED', 'AUTO_APPROVED', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED', 'TERMINATED', 'TRIAL') DEFAULT 'PENDING_APPROVAL';

-- 2. 구독 테이블에 승인 관련 필드 추가
ALTER TABLE subscriptions 
ADD COLUMN approval_requested_at TIMESTAMP NULL,
ADD COLUMN approved_at TIMESTAMP NULL,
ADD COLUMN approved_by BIGINT NULL,
ADD COLUMN rejection_reason TEXT NULL,
ADD COLUMN suspension_reason TEXT NULL,
ADD COLUMN termination_reason TEXT NULL;

-- 3. 외래키 제약조건 추가
ALTER TABLE subscriptions 
ADD CONSTRAINT fk_subscriptions_approved_by 
FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL;

-- 4. 구독 승인 이력 테이블 생성
CREATE TABLE subscription_approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    from_status ENUM('PENDING_APPROVAL', 'REJECTED', 'AUTO_APPROVED', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED', 'TERMINATED', 'TRIAL') NOT NULL,
    to_status ENUM('PENDING_APPROVAL', 'REJECTED', 'AUTO_APPROVED', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED', 'TERMINATED', 'TRIAL') NOT NULL,
    reason TEXT,
    action ENUM('APPROVE', 'REJECT', 'SUSPEND', 'TERMINATE', 'REACTIVATE', 'AUTO_APPROVE') NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_subscription_approvals_subscription_id (subscription_id),
    INDEX idx_subscription_approvals_admin_id (admin_id),
    INDEX idx_subscription_approvals_processed_at (processed_at)
);

-- 5. 자동 승인 규칙 테이블 생성
CREATE TABLE auto_approval_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    plan_ids JSON,
    verified_tenants_only BOOLEAN DEFAULT FALSE,
    payment_methods JSON,
    max_amount DECIMAL(10,2),
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_auto_approval_rules_active_priority (is_active, priority)
);

-- 6. 알림 테이블 생성
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    type ENUM('SUBSCRIPTION_REQUEST', 'SUBSCRIPTION_APPROVED', 'SUBSCRIPTION_REJECTED', 'SUBSCRIPTION_SUSPENDED', 'SUBSCRIPTION_TERMINATED', 'APPROVAL_REMINDER', 'SUBSCRIPTION_REACTIVATED') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_recipient_read (recipient_id, is_read),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_created_at (created_at)
);

-- 7. 기존 구독 데이터의 approval_requested_at 필드 업데이트
UPDATE subscriptions 
SET approval_requested_at = created_at 
WHERE approval_requested_at IS NULL;

-- 8. 기본 자동 승인 규칙 추가 (예시)
INSERT INTO auto_approval_rules (rule_name, is_active, plan_ids, verified_tenants_only, payment_methods, max_amount, priority) VALUES
('기본 요금제 자동 승인', TRUE, '["basic"]', FALSE, '["CARD"]', 100000.00, 1),
('검증된 테넌트 자동 승인', TRUE, '["basic", "standard"]', TRUE, '["CARD", "BANK_TRANSFER"]', 200000.00, 2);