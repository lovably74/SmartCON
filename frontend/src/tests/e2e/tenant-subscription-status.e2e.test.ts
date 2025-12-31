import { test, expect } from '@playwright/test';

/**
 * 테넌트 구독 상태별 UI 동작 E2E 테스트
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 * 테넌트 사용자가 구독 상태에 따라 올바른 UI와 메시지를 보는지 검증
 */

// 테스트 데이터
const TEST_TENANT_USERS = {
  pending: {
    email: 'pending@test.com',
    password: 'test123'
  },
  rejected: {
    email: 'rejected@test.com',
    password: 'test123'
  },
  suspended: {
    email: 'suspended@test.com',
    password: 'test123'
  },
  terminated: {
    email: 'terminated@test.com',
    password: 'test123'
  }
};

test.describe('테넌트 구독 상태별 UI 동작 테스트', () => {
  
  test.beforeEach(async ({ page }) => {
    // 테스트 환경 설정
    await page.goto('/');
  });

  test('승인 대기 상태 UI 동작 테스트 (Requirement 6.1)', async ({ page }) => {
    // Given: 승인 대기 상태의 테넌트 사용자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_USERS.pending.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_USERS.pending.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 승인 대기 메시지가 표시됨
    await expect(page.locator('[data-testid="subscription-status-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('승인 대기 중');
    
    // 서비스 접근이 차단됨을 확인
    await expect(page.locator('[data-testid="service-blocked-notice"]')).toBeVisible();
    await expect(page.locator('[data-testid="service-blocked-notice"]')).toContainText('승인이 완료되면 서비스를 이용하실 수 있습니다');
    
    // 대시보드 메뉴가 비활성화됨
    const dashboardMenu = page.locator('[data-testid="dashboard-menu"]');
    await expect(dashboardMenu).toHaveAttribute('aria-disabled', 'true');
    
    // 승인 대기 상태 아이콘이 표시됨
    await expect(page.locator('[data-testid="pending-status-icon"]')).toBeVisible();
    
    // When: 서비스 메뉴 클릭 시도
    await page.click('[data-testid="attendance-menu"]');
    
    // Then: 접근 차단 모달이 표시됨
    await expect(page.locator('[data-testid="access-denied-modal"]')).toBeVisible();
    await expect(page.locator('[data-testid="access-denied-modal"]')).toContainText('승인 대기 중인 구독입니다');
    
    // 고객센터 연락처가 표시됨
    await expect(page.locator('[data-testid="contact-info"]')).toBeVisible();
    await expect(page.locator('[data-testid="contact-info"]')).toContainText('문의사항이 있으시면 고객센터로 연락해주세요');
  });

  test('승인 거부 상태 UI 동작 테스트 (Requirement 6.2)', async ({ page }) => {
    // Given: 승인 거부 상태의 테넌트 사용자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_USERS.rejected.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_USERS.rejected.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 승인 거부 메시지가 표시됨
    await expect(page.locator('[data-testid="subscription-status-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('승인이 거부되었습니다');
    
    // 거부 사유가 표시됨
    await expect(page.locator('[data-testid="rejection-reason"]')).toBeVisible();
    await expect(page.locator('[data-testid="rejection-reason"]')).toContainText('거부 사유:');
    
    // 재신청 버튼이 표시됨
    await expect(page.locator('[data-testid="reapply-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="reapply-button"]')).toContainText('재신청하기');
    
    // 거부 상태 아이콘이 표시됨
    await expect(page.locator('[data-testid="rejected-status-icon"]')).toBeVisible();
    
    // When: 재신청 버튼 클릭
    await page.click('[data-testid="reapply-button"]');
    
    // Then: 재신청 모달이 열림
    await expect(page.locator('[data-testid="reapplication-modal"]')).toBeVisible();
    await expect(page.locator('[data-testid="reapplication-modal"]')).toContainText('구독 재신청');
    
    // 이전 거부 사유가 참고용으로 표시됨
    await expect(page.locator('[data-testid="previous-rejection-info"]')).toBeVisible();
    
    // 개선사항 입력 필드가 있음
    await expect(page.locator('[data-testid="improvement-notes"]')).toBeVisible();
    
    // When: 재신청 양식 작성 및 제출
    await page.fill('[data-testid="improvement-notes"]', '요청사항을 개선하여 재신청합니다');
    await page.click('[data-testid="submit-reapplication"]');
    
    // Then: 재신청 완료 메시지 표시
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="success-message"]')).toContainText('재신청이 완료되었습니다');
    
    // 상태가 승인 대기로 변경됨
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('승인 대기 중');
  });

  test('일시 중지 상태 UI 동작 테스트 (Requirement 6.3)', async ({ page }) => {
    // Given: 일시 중지 상태의 테넌트 사용자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_USERS.suspended.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_USERS.suspended.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 일시 중지 메시지가 표시됨
    await expect(page.locator('[data-testid="subscription-status-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('서비스가 일시 중지되었습니다');
    
    // 중지 사유가 표시됨
    await expect(page.locator('[data-testid="suspension-reason"]')).toBeVisible();
    await expect(page.locator('[data-testid="suspension-reason"]')).toContainText('중지 사유:');
    
    // 고객센터 연락처가 표시됨
    await expect(page.locator('[data-testid="customer-service-contact"]')).toBeVisible();
    await expect(page.locator('[data-testid="customer-service-contact"]')).toContainText('고객센터: 1588-1234');
    
    // 이메일 연락처가 표시됨
    await expect(page.locator('[data-testid="support-email"]')).toBeVisible();
    await expect(page.locator('[data-testid="support-email"]')).toContainText('support@smartcon.com');
    
    // 중지 상태 아이콘이 표시됨
    await expect(page.locator('[data-testid="suspended-status-icon"]')).toBeVisible();
    
    // When: 서비스 메뉴 클릭 시도
    await page.click('[data-testid="sites-menu"]');
    
    // Then: 서비스 중지 안내 모달이 표시됨
    await expect(page.locator('[data-testid="service-suspended-modal"]')).toBeVisible();
    await expect(page.locator('[data-testid="service-suspended-modal"]')).toContainText('서비스가 일시 중지된 상태입니다');
    
    // 문의하기 버튼이 있음
    await expect(page.locator('[data-testid="contact-support-button"]')).toBeVisible();
    
    // When: 문의하기 버튼 클릭
    await page.click('[data-testid="contact-support-button"]');
    
    // Then: 문의 양식이 열림
    await expect(page.locator('[data-testid="support-inquiry-form"]')).toBeVisible();
    
    // 구독 정보가 자동으로 포함됨
    await expect(page.locator('[data-testid="subscription-info-readonly"]')).toBeVisible();
  });

  test('종료 상태 UI 동작 테스트 (Requirement 6.4)', async ({ page }) => {
    // Given: 종료 상태의 테넌트 사용자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_USERS.terminated.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_USERS.terminated.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 종료 안내 메시지가 표시됨
    await expect(page.locator('[data-testid="subscription-status-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('구독이 종료되었습니다');
    
    // 종료 사유가 표시됨
    await expect(page.locator('[data-testid="termination-reason"]')).toBeVisible();
    await expect(page.locator('[data-testid="termination-reason"]')).toContainText('종료 사유:');
    
    // 새 구독 신청 버튼이 표시됨
    await expect(page.locator('[data-testid="new-subscription-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="new-subscription-button"]')).toContainText('새 구독 신청');
    
    // 데이터 백업 안내가 표시됨
    await expect(page.locator('[data-testid="data-backup-notice"]')).toBeVisible();
    await expect(page.locator('[data-testid="data-backup-notice"]')).toContainText('기존 데이터는 30일간 보관됩니다');
    
    // 종료 상태 아이콘이 표시됨
    await expect(page.locator('[data-testid="terminated-status-icon"]')).toBeVisible();
    
    // When: 새 구독 신청 버튼 클릭
    await page.click('[data-testid="new-subscription-button"]');
    
    // Then: 새 구독 신청 페이지로 이동
    await expect(page).toHaveURL('/subscribe');
    
    // 이전 구독 정보가 참고용으로 표시됨
    await expect(page.locator('[data-testid="previous-subscription-info"]')).toBeVisible();
    await expect(page.locator('[data-testid="previous-subscription-info"]')).toContainText('이전 구독 정보');
    
    // 신규 고객 할인 안내가 표시됨 (재가입 혜택)
    await expect(page.locator('[data-testid="returning-customer-benefits"]')).toBeVisible();
    
    // When: 모든 서비스 메뉴 접근 시도
    const serviceMenus = ['dashboard-menu', 'attendance-menu', 'sites-menu', 'workers-menu'];
    
    for (const menu of serviceMenus) {
      await page.click(`[data-testid="${menu}"]`);
      
      // Then: 구독 종료 안내 모달이 표시됨
      await expect(page.locator('[data-testid="subscription-terminated-modal"]')).toBeVisible();
      await expect(page.locator('[data-testid="subscription-terminated-modal"]')).toContainText('구독이 종료되어 서비스를 이용할 수 없습니다');
      
      // 모달 닫기
      await page.click('[data-testid="close-modal"]');
    }
  });

  test('상태별 네비게이션 메뉴 접근 제어 테스트', async ({ page }) => {
    const statusTests = [
      { user: TEST_TENANT_USERS.pending, expectedStatus: 'pending' },
      { user: TEST_TENANT_USERS.rejected, expectedStatus: 'rejected' },
      { user: TEST_TENANT_USERS.suspended, expectedStatus: 'suspended' },
      { user: TEST_TENANT_USERS.terminated, expectedStatus: 'terminated' }
    ];

    for (const { user, expectedStatus } of statusTests) {
      // Given: 각 상태의 사용자로 로그인
      await page.goto('/login');
      await page.fill('[data-testid="email-input"]', user.email);
      await page.fill('[data-testid="password-input"]', user.password);
      await page.click('[data-testid="login-button"]');
      
      // Then: 상태에 따른 메뉴 접근 제어 확인
      const restrictedMenus = [
        'dashboard-menu',
        'attendance-menu', 
        'sites-menu',
        'workers-menu',
        'contracts-menu',
        'settlements-menu'
      ];
      
      for (const menu of restrictedMenus) {
        const menuElement = page.locator(`[data-testid="${menu}"]`);
        
        if (expectedStatus === 'pending' || expectedStatus === 'suspended' || expectedStatus === 'terminated') {
          // 메뉴가 비활성화되거나 클릭 시 차단 메시지 표시
          await expect(menuElement).toHaveAttribute('aria-disabled', 'true');
        }
      }
      
      // 로그아웃
      await page.click('[data-testid="user-menu"]');
      await page.click('[data-testid="logout-button"]');
    }
  });

  test('상태별 알림 및 안내 메시지 표시 테스트', async ({ page }) => {
    const statusMessages = {
      pending: {
        user: TEST_TENANT_USERS.pending,
        expectedMessages: [
          '승인 대기 중입니다',
          '승인이 완료되면 알림을 보내드립니다',
          '문의사항이 있으시면 고객센터로 연락해주세요'
        ]
      },
      rejected: {
        user: TEST_TENANT_USERS.rejected,
        expectedMessages: [
          '승인이 거부되었습니다',
          '거부 사유를 확인하시고 재신청해주세요',
          '재신청하기'
        ]
      },
      suspended: {
        user: TEST_TENANT_USERS.suspended,
        expectedMessages: [
          '서비스가 일시 중지되었습니다',
          '고객센터로 문의해주세요',
          '1588-1234'
        ]
      },
      terminated: {
        user: TEST_TENANT_USERS.terminated,
        expectedMessages: [
          '구독이 종료되었습니다',
          '새 구독을 신청하실 수 있습니다',
          '기존 데이터는 30일간 보관됩니다'
        ]
      }
    };

    for (const [status, config] of Object.entries(statusMessages)) {
      // Given: 각 상태의 사용자로 로그인
      await page.goto('/login');
      await page.fill('[data-testid="email-input"]', config.user.email);
      await page.fill('[data-testid="password-input"]', config.user.password);
      await page.click('[data-testid="login-button"]');
      
      // Then: 상태별 메시지가 올바르게 표시됨
      for (const message of config.expectedMessages) {
        await expect(page.locator('body')).toContainText(message);
      }
      
      // 상태별 아이콘이 표시됨
      await expect(page.locator(`[data-testid="${status}-status-icon"]`)).toBeVisible();
      
      // 로그아웃
      await page.click('[data-testid="user-menu"]');
      await page.click('[data-testid="logout-button"]');
    }
  });

  test('반응형 디자인 - 상태 메시지 모바일 최적화', async ({ page }) => {
    // Given: 모바일 뷰포트 설정
    await page.setViewportSize({ width: 375, height: 667 });
    
    // 승인 대기 상태 사용자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_USERS.pending.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_USERS.pending.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 모바일에서 상태 메시지가 적절히 표시됨
    const statusMessage = page.locator('[data-testid="mobile-status-banner"]');
    await expect(statusMessage).toBeVisible();
    
    // 모바일 전용 상태 카드가 표시됨
    await expect(page.locator('[data-testid="mobile-status-card"]')).toBeVisible();
    
    // 터치 친화적인 버튼들이 표시됨
    const contactButton = page.locator('[data-testid="mobile-contact-button"]');
    await expect(contactButton).toBeVisible();
    
    // 버튼 크기가 터치에 적합한지 확인 (최소 44px)
    const buttonBox = await contactButton.boundingBox();
    expect(buttonBox?.height).toBeGreaterThanOrEqual(44);
  });

  test('접근성 - 상태별 스크린 리더 지원', async ({ page }) => {
    // Given: 승인 대기 상태 사용자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_TENANT_USERS.pending.email);
    await page.fill('[data-testid="password-input"]', TEST_TENANT_USERS.pending.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 스크린 리더용 상태 안내가 있음
    await expect(page.locator('[aria-live="polite"]')).toContainText('구독 상태: 승인 대기 중');
    
    // 상태 아이콘에 적절한 alt 텍스트가 있음
    await expect(page.locator('[data-testid="pending-status-icon"]')).toHaveAttribute('aria-label', '승인 대기 중 상태');
    
    // 키보드 네비게이션으로 모든 중요 요소에 접근 가능
    await page.keyboard.press('Tab');
    await expect(page.locator(':focus')).toBeVisible();
    
    // 상태 메시지가 적절한 heading 레벨을 가짐
    await expect(page.locator('[data-testid="status-heading"]')).toHaveAttribute('role', 'heading');
    await expect(page.locator('[data-testid="status-heading"]')).toHaveAttribute('aria-level', '2');
  });
});