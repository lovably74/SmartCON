-- 성능 최적화를 위한 추가 인덱스 및 쿼리 최적화
-- V6: 승인 워크플로우 성능 최적화

-- =============================================================================
-- 승인 대기 목록 조회 성능 최적화 인덱스
-- =============================================================================

-- 구독 테이블 성능 최적화 인덱스
-- 승인 대기 목록 조회 시 가장 자주 사용되는 쿼리 패턴에 최적화
ALTER TABLE subscriptions 
ADD INDEX idx_subscriptions_status_requested_at (status, approval_requested_at),
ADD INDEX idx_subscriptions_tenant_status (tenant_id, status),
ADD INDEX idx_subscriptions_approved_by_date (approved_by, approved_at),
ADD INDEX idx_subscriptions_status_created_tenant (status, created_at, tenant_id);

-- 승인 이력 테이블 추가 성능 인덱스
ALTER TABLE subscription_approvals
ADD INDEX idx_subscription_approvals_action_date (action, processed_at),
ADD INDEX idx_subscription_approvals_status_transition (from_status, to_status),
ADD INDEX idx_subscription_approvals_admin_action_date (admin_id, action, processed_at);

-- 알림 테이블 성능 최적화 인덱스
ALTER TABLE notifications
ADD INDEX idx_notifications_recipient_type_read (recipient_id, type, is_read),
ADD INDEX idx_notifications_entity_relation (related_entity_type, related_entity_id),
ADD INDEX idx_notifications_unread_created (is_read, created_at);

-- =============================================================================
-- 대시보드 통계 조회 성능 최적화
-- =============================================================================

-- 구독 상태별 통계 조회 최적화
ALTER TABLE subscriptions
ADD INDEX idx_subscriptions_stats_status_date (status, created_at, tenant_id),
ADD INDEX idx_subscriptions_plan_status_stats (plan_id, status, created_at);

-- 승인 처리 통계 최적화
ALTER TABLE subscription_approvals
ADD INDEX idx_approvals_stats_date_action (processed_at, action),
ADD INDEX idx_approvals_monthly_stats (YEAR(processed_at), MONTH(processed_at), action);

-- =============================================================================
-- 자동 승인 규칙 성능 최적화
-- =============================================================================

-- 자동 승인 규칙 평가 성능 최적화
ALTER TABLE auto_approval_rules
ADD INDEX idx_auto_approval_evaluation (is_active, priority, verified_tenants_only);

-- =============================================================================
-- 페이지네이션 성능 최적화
-- =============================================================================

-- 승인 대기 목록 페이지네이션 최적화 (LIMIT OFFSET 대신 커서 기반 페이지네이션 지원)
ALTER TABLE subscriptions
ADD INDEX idx_subscriptions_cursor_pagination (status, id, approval_requested_at);

-- 승인 이력 페이지네이션 최적화
ALTER TABLE subscription_approvals
ADD INDEX idx_approvals_cursor_pagination (subscription_id, id, processed_at);

-- 알림 목록 페이지네이션 최적화
ALTER TABLE notifications
ADD INDEX idx_notifications_cursor_pagination (recipient_id, id, created_at);

-- =============================================================================
-- 검색 기능 성능 최적화
-- =============================================================================

-- 테넌트 정보 검색 최적화 (구독 승인 시 테넌트 정보 조회)
ALTER TABLE tenants
ADD INDEX idx_tenants_search_optimization (company_name, ceo_name, business_number),
ADD INDEX idx_tenants_contact_search (contact_email, contact_phone);

-- 사용자 검색 최적화 (승인 담당자 검색)
ALTER TABLE users
ADD INDEX idx_users_admin_search (name, email, is_active);

-- =============================================================================
-- 성능 모니터링을 위한 뷰 생성
-- =============================================================================

-- 승인 대기 현황 통계 뷰
CREATE VIEW v_approval_pending_stats AS
SELECT 
    COUNT(*) as total_pending,
    COUNT(CASE WHEN TIMESTAMPDIFF(HOUR, approval_requested_at, NOW()) > 24 THEN 1 END) as overdue_24h,
    COUNT(CASE WHEN TIMESTAMPDIFF(HOUR, approval_requested_at, NOW()) > 72 THEN 1 END) as overdue_72h,
    AVG(TIMESTAMPDIFF(HOUR, approval_requested_at, NOW())) as avg_pending_hours,
    MIN(approval_requested_at) as oldest_request,
    MAX(approval_requested_at) as newest_request
FROM subscriptions 
WHERE status = 'PENDING_APPROVAL';

-- 승인 처리 성능 통계 뷰
CREATE VIEW v_approval_performance_stats AS
SELECT 
    DATE(sa.processed_at) as approval_date,
    sa.action,
    COUNT(*) as total_processed,
    AVG(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as avg_processing_minutes,
    MIN(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as min_processing_minutes,
    MAX(TIMESTAMPDIFF(MINUTE, s.approval_requested_at, sa.processed_at)) as max_processing_minutes
FROM subscription_approvals sa
JOIN subscriptions s ON sa.subscription_id = s.id
WHERE sa.processed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(sa.processed_at), sa.action
ORDER BY approval_date DESC, sa.action;

-- 자동 승인 효율성 통계 뷰
CREATE VIEW v_auto_approval_efficiency AS
SELECT 
    DATE(processed_at) as approval_date,
    COUNT(CASE WHEN action = 'AUTO_APPROVE' THEN 1 END) as auto_approved,
    COUNT(CASE WHEN action IN ('APPROVE', 'REJECT') THEN 1 END) as manual_processed,
    ROUND(
        COUNT(CASE WHEN action = 'AUTO_APPROVE' THEN 1 END) * 100.0 / 
        COUNT(*), 2
    ) as auto_approval_rate
FROM subscription_approvals
WHERE processed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(processed_at)
ORDER BY approval_date DESC;

-- 알림 발송 통계 뷰
CREATE VIEW v_notification_stats AS
SELECT 
    DATE(created_at) as notification_date,
    type,
    COUNT(*) as total_sent,
    COUNT(CASE WHEN is_read = TRUE THEN 1 END) as total_read,
    ROUND(COUNT(CASE WHEN is_read = TRUE THEN 1 END) * 100.0 / COUNT(*), 2) as read_rate,
    AVG(CASE WHEN read_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, created_at, read_at) END) as avg_read_time_minutes
FROM notifications
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(created_at), type
ORDER BY notification_date DESC, type;

-- =============================================================================
-- 쿼리 성능 최적화를 위한 저장 프로시저
-- =============================================================================

-- 승인 대기 목록 조회 최적화 프로시저
DELIMITER //
CREATE PROCEDURE GetPendingApprovals(
    IN p_limit INT DEFAULT 20,
    IN p_offset INT DEFAULT 0,
    IN p_sort_by VARCHAR(50) DEFAULT 'approval_requested_at',
    IN p_sort_order VARCHAR(4) DEFAULT 'DESC'
)
BEGIN
    DECLARE sql_query TEXT;
    
    SET sql_query = CONCAT(
        'SELECT s.id, s.tenant_id, s.plan_id, s.status, s.approval_requested_at, ',
        't.company_name, t.ceo_name, t.contact_email, ',
        'sp.plan_name, sp.monthly_price ',
        'FROM subscriptions s ',
        'JOIN tenants t ON s.tenant_id = t.id ',
        'JOIN subscription_plans sp ON s.plan_id = sp.id ',
        'WHERE s.status = ''PENDING_APPROVAL'' ',
        'ORDER BY s.', p_sort_by, ' ', p_sort_order, ' ',
        'LIMIT ', p_limit, ' OFFSET ', p_offset
    );
    
    SET @sql = sql_query;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- 승인 이력 조회 최적화 프로시저
DELIMITER //
CREATE PROCEDURE GetApprovalHistory(
    IN p_subscription_id BIGINT
)
BEGIN
    SELECT 
        sa.id,
        sa.from_status,
        sa.to_status,
        sa.reason,
        sa.action,
        sa.processed_at,
        u.name as admin_name,
        u.email as admin_email
    FROM subscription_approvals sa
    JOIN users u ON sa.admin_id = u.id
    WHERE sa.subscription_id = p_subscription_id
    ORDER BY sa.processed_at DESC;
END //
DELIMITER ;

-- =============================================================================
-- 성능 모니터링을 위한 이벤트 스케줄러 설정
-- =============================================================================

-- 매일 자정에 승인 대기 알림 통계 업데이트
CREATE EVENT IF NOT EXISTS evt_update_approval_stats
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY)
DO
BEGIN
    -- 24시간 이상 대기 중인 승인 건수 로깅
    INSERT INTO system_logs (log_type, message, created_at)
    SELECT 
        'APPROVAL_OVERDUE',
        CONCAT('Overdue approvals: ', COUNT(*), ' subscriptions pending over 24 hours'),
        NOW()
    FROM subscriptions 
    WHERE status = 'PENDING_APPROVAL' 
    AND TIMESTAMPDIFF(HOUR, approval_requested_at, NOW()) > 24;
END;

-- 매주 월요일에 승인 성능 통계 정리
CREATE EVENT IF NOT EXISTS evt_cleanup_old_stats
ON SCHEDULE EVERY 1 WEEK
STARTS TIMESTAMP(CURRENT_DATE + INTERVAL (2 - WEEKDAY(CURRENT_DATE)) DAY)
DO
BEGIN
    -- 90일 이전의 읽은 알림 정리
    DELETE FROM notifications 
    WHERE is_read = TRUE 
    AND read_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    -- 1년 이전의 승인 이력 아카이브 (실제 운영에서는 별도 아카이브 테이블로 이동)
    -- DELETE FROM subscription_approvals 
    -- WHERE processed_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);
END;