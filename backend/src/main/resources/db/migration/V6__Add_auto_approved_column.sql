-- 구독 승인 이력 테이블에 auto_approved 컬럼 추가

ALTER TABLE subscription_approvals 
ADD COLUMN auto_approved BOOLEAN NOT NULL DEFAULT FALSE;

-- 기존 데이터에서 AUTO_APPROVE 액션인 경우 auto_approved를 TRUE로 설정
UPDATE subscription_approvals 
SET auto_approved = TRUE 
WHERE action = 'AUTO_APPROVE';