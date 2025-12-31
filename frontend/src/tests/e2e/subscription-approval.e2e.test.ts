import { test, expect } from '@playwright/test';

/**
 * 구독 승인 워크플로우 E2E 테스트
 * 
 * Playwright를 사용한 프론트엔드 E2E 테스트
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */

// 테스트 데이터
const TEST_SUPER_ADMIN = {
  email: 'super@smartcon.com',
  password: 'test123'
};

const TEST_TENANT_ADMIN = {
  email: 'tenant@test.com',
  password: 'test123'
};

test.describe('구독 승인 워크플로우 E2E 테스트', () => {
  
  test.beforeEach(async ({ page }) => {
    // 테스트 환경 설정
    await page.goto('/');
  });

  test('슈퍼관리자 구독 승인 전체 플로우', async ({ page }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // 로그인 성공 확인
    await expect(page).toHaveURL('/super/dashboard');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    await expect(page).toHaveURL('/super/approvals');
    
    // Then: 승인 대기 목록이 표시됨
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    
    // When: 첫 번째 승인 요청 클릭
    const firstApprovalItem = page.locator('[data-testid="approval-item"]').first();
    await firstApprovalItem.click();
    
    // Then: 승인 상세 모달이 열림
    await expect(page.locator('[data-testid="approval-detail-modal"]')).toBeVisible();
    
    // When: 승인 버튼 클릭
    await page.click('[data-testid="approve-button"]');
    
    // 승인 코멘트 입력
    await page.fill('[data-testid="approval-comment"]', '승인 완료');
    await page.click('[data-testid="confirm-approve-button"]');
    
    // Then: 승인 완료 메시지 표시
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="success-message"]')).toContainText('승인이 완료되었습니다');
    
    // 승인 목록에서 해당 항목이 승인됨 상태로 변경
    await expect(firstApprovalItem.locator('[data-testid="approval-status"]')).toContainText('승인됨');
  });

  test('슈퍼관리자 구독 거부 플로우', async ({ page }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // 첫 번째 승인 요청 선택
    const firstApprovalItem = page.locator('[data-testid="approval-item"]').first();
    await firstApprovalItem.click();
    
    // When: 거부 버튼 클릭
    await page.click('[data-testid="reject-button"]');
    
    // 거부 사유 입력
    await page.fill('[data-testid="rejection-reason"]', '서류 미비');
    await page.click('[data-testid="confirm-reject-button"]');
    
    // Then: 거부 완료 메시지 표시
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="success-message"]')).toContainText('거부가 완료되었습니다');
    
    // 승인 목록에서 해당 항목이 거부됨 상태로 변경
    await expect(firstApprovalItem.locator('[data-testid="approval-status"]')).toContainText('거부됨');
  });

  test('테넌트 관리자 구독 신청 플로우', async ({ page }) => {
    // Given: 테넌트 관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // 로그인 성공 확인
    await expect(page).toHaveURL('/hq/dashboard');
    
    // When: 구독 관리 페이지로 이동
    await page.click('[data-testid="subscription-menu"]');
    await expect(page).toHaveURL('/hq/subscription');
    
    // When: 새 구독 신청 버튼 클릭
    await page.click('[data-testid="new-subscription-button"]');
    
    // Then: 구독 신청 모달이 열림
    await expect(page.locator('[data-testid="subscription-request-modal"]')).toBeVisible();
    
    // When: 구독 플랜 선택
    await page.click('[data-testid="plan-basic"]');
    
    // 청구 주기 선택
    await page.click('[data-testid="billing-monthly"]');
    
    // 신청 사유 입력
    await page.fill('[data-testid="request-reason"]', '신규 구독 신청');
    
    // 신청 버튼 클릭
    await page.click('[data-testid="submit-request-button"]');
    
    // Then: 신청 완료 메시지 표시
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="success-message"]')).toContainText('구독 신청이 완료되었습니다');
    
    // 구독 상태가 승인 대기로 표시됨
    await expect(page.locator('[data-testid="subscription-status"]')).toContainText('승인 대기');
  });

  test('알림 시스템 E2E 테스트', async ({ page }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 알림 아이콘 클릭
    await page.click('[data-testid="notification-icon"]');
    
    // Then: 알림 드롭다운이 열림
    await expect(page.locator('[data-testid="notification-dropdown"]')).toBeVisible();
    
    // 승인 요청 알림이 표시됨
    const approvalNotification = page.locator('[data-testid="notification-item"]')
      .filter({ hasText: '구독 승인 요청' }).first();
    await expect(approvalNotification).toBeVisible();
    
    // When: 알림 클릭
    await approvalNotification.click();
    
    // Then: 해당 승인 요청 상세 페이지로 이동
    await expect(page).toHaveURL(/\/super\/approvals\/\d+/);
    
    // 승인 요청 상세 정보가 표시됨
    await expect(page.locator('[data-testid="approval-detail"]')).toBeVisible();
  });

  test('반응형 디자인 테스트 - 모바일', async ({ page }) => {
    // Given: 모바일 뷰포트 설정
    await page.setViewportSize({ width: 375, height: 667 });
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 모바일 메뉴 버튼 클릭
    await page.click('[data-testid="mobile-menu-button"]');
    
    // Then: 모바일 네비게이션이 표시됨
    await expect(page.locator('[data-testid="mobile-navigation"]')).toBeVisible();
    
    // When: 승인 관리 메뉴 클릭
    await page.click('[data-testid="mobile-approvals-menu"]');
    
    // Then: 승인 목록이 모바일에 최적화되어 표시됨
    await expect(page.locator('[data-testid="mobile-approval-list"]')).toBeVisible();
    
    // 승인 카드가 모바일 레이아웃으로 표시됨
    const approvalCard = page.locator('[data-testid="mobile-approval-card"]').first();
    await expect(approvalCard).toBeVisible();
    
    // 카드 클릭 시 전체 화면 모달이 열림
    await approvalCard.click();
    await expect(page.locator('[data-testid="fullscreen-approval-modal"]')).toBeVisible();
  });

  test('반응형 디자인 테스트 - 태블릿', async ({ page }) => {
    // Given: 태블릿 뷰포트 설정
    await page.setViewportSize({ width: 768, height: 1024 });
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 태블릿 레이아웃이 적용됨
    await expect(page.locator('[data-testid="tablet-layout"]')).toBeVisible();
    
    // 승인 목록이 그리드 형태로 표시됨
    await expect(page.locator('[data-testid="approval-grid"]')).toBeVisible();
    
    // 사이드바가 축소된 형태로 표시됨
    await expect(page.locator('[data-testid="collapsed-sidebar"]')).toBeVisible();
  });

  test('브라우저 호환성 테스트', async ({ page, browserName }) => {
    // Given: 브라우저별 테스트 실행
    console.log(`Testing on ${browserName}`);
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지 기능 테스트
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 모든 브라우저에서 동일하게 작동
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    
    // CSS 애니메이션이 정상 작동하는지 확인
    const approvalItem = page.locator('[data-testid="approval-item"]').first();
    await approvalItem.hover();
    
    // 호버 효과가 적용되는지 확인 (브라우저별로 다를 수 있음)
    await expect(approvalItem).toHaveClass(/hover/);
  });

  test('접근성 테스트', async ({ page }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 접근성 요소들이 올바르게 설정됨
    
    // ARIA 레이블 확인
    await expect(page.locator('[data-testid="approval-list"]')).toHaveAttribute('aria-label', '승인 요청 목록');
    
    // 키보드 네비게이션 테스트
    await page.keyboard.press('Tab');
    await expect(page.locator(':focus')).toBeVisible();
    
    // 스크린 리더용 텍스트 확인
    await expect(page.locator('[aria-live="polite"]')).toBeVisible();
    
    // 색상 대비 확인 (시각적 요소)
    const approvalStatus = page.locator('[data-testid="approval-status"]').first();
    await expect(approvalStatus).toHaveCSS('color', /rgb\(\d+,\s*\d+,\s*\d+\)/);
  });

  test('성능 테스트 - 대량 데이터 로딩', async ({ page }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동 (대량 데이터 가정)
    const startTime = Date.now();
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 페이지 로딩 시간이 합리적인 범위 내
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    const loadTime = Date.now() - startTime;
    
    // 3초 이내 로딩 완료 확인
    expect(loadTime).toBeLessThan(3000);
    
    // 가상 스크롤링이 정상 작동하는지 확인
    await page.evaluate(() => {
      const list = document.querySelector('[data-testid="approval-list"]');
      if (list) {
        list.scrollTop = list.scrollHeight;
      }
    });
    
    // 추가 데이터가 로드되는지 확인
    await expect(page.locator('[data-testid="loading-more"]')).toBeVisible();
  });

  test('오프라인 상태 처리 테스트', async ({ page, context }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 네트워크를 오프라인으로 설정
    await context.setOffline(true);
    
    // 승인 관리 페이지로 이동 시도
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 오프라인 상태 메시지가 표시됨
    await expect(page.locator('[data-testid="offline-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="offline-message"]')).toContainText('인터넷 연결을 확인해주세요');
    
    // When: 네트워크를 다시 온라인으로 설정
    await context.setOffline(false);
    
    // 재시도 버튼 클릭
    await page.click('[data-testid="retry-button"]');
    
    // Then: 정상적으로 데이터가 로드됨
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
  });
});