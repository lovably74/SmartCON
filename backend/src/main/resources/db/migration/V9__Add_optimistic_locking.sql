-- 낙관적 잠금을 위한 버전 필드 추가

-- 1. 구독 테이블에 버전 필드 추가
ALTER TABLE subscriptions 
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- 2. 기존 데이터의 버전을 0으로 초기화 (이미 DEFAULT로 설정되어 있음)
-- UPDATE subscriptions SET version = 0 WHERE version IS NULL;

-- 3. 구독 승인 이력 테이블에도 버전 필드 추가 (필요시)
ALTER TABLE subscription_approvals 
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- 4. 자동 승인 규칙 테이블에도 버전 필드 추가 (필요시)
ALTER TABLE auto_approval_rules 
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- 5. 알림 테이블에도 버전 필드 추가 (필요시)
ALTER TABLE notifications 
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;