import { test, expect, devices } from '@playwright/test';

/**
 * 브라우저 호환성 및 반응형 디자인 E2E 테스트
 * 
 * 다양한 브라우저와 디바이스에서의 구독 승인 워크플로우 동작 검증
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

// 다양한 뷰포트 크기 정의
const VIEWPORTS = {
  mobile: { width: 375, height: 667 },
  tablet: { width: 768, height: 1024 },
  desktop: { width: 1920, height: 1080 },
  ultrawide: { width: 2560, height: 1440 }
};

test.describe('브라우저 호환성 테스트', () => {
  
  test('Chrome에서 구독 승인 워크플로우', async ({ page, browserName }) => {
    test.skip(browserName !== 'chromium', 'Chrome 전용 테스트');
    
    // Given: Chrome에서 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: Chrome에서 정상 동작 확인
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    
    // CSS Grid 레이아웃이 올바르게 렌더링됨
    const approvalGrid = page.locator('[data-testid="approval-grid"]');
    await expect(approvalGrid).toHaveCSS('display', 'grid');
    
    // Flexbox 레이아웃이 올바르게 작동함
    const approvalItem = page.locator('[data-testid="approval-item"]').first();
    await expect(approvalItem).toHaveCSS('display', 'flex');
    
    // CSS 변수가 올바르게 적용됨
    await expect(page.locator('body')).toHaveCSS('--primary-color', /rgb\(\d+,\s*\d+,\s*\d+\)/);
  });

  test('Firefox에서 구독 승인 워크플로우', async ({ page, browserName }) => {
    test.skip(browserName !== 'firefox', 'Firefox 전용 테스트');
    
    // Given: Firefox에서 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: Firefox에서 정상 동작 확인
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    
    // Firefox 특정 CSS 속성 확인
    const modal = page.locator('[data-testid="approval-modal"]');
    await page.click('[data-testid="approval-item"]');
    await expect(modal).toBeVisible();
    
    // Firefox에서 backdrop-filter 지원 확인
    await expect(modal).toHaveCSS('backdrop-filter', /.*/);
  });

  test('Safari에서 구독 승인 워크플로우', async ({ page, browserName }) => {
    test.skip(browserName !== 'webkit', 'Safari 전용 테스트');
    
    // Given: Safari에서 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: Safari에서 정상 동작 확인
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    
    // Safari 특정 이슈 확인 (예: 날짜 입력 필드)
    await page.click('[data-testid="filter-button"]');
    const dateInput = page.locator('[data-testid="date-filter"]');
    await expect(dateInput).toBeVisible();
    
    // Safari에서 position: sticky 지원 확인
    const stickyHeader = page.locator('[data-testid="sticky-header"]');
    await expect(stickyHeader).toHaveCSS('position', 'sticky');
  });

  test('브라우저별 JavaScript API 호환성', async ({ page, browserName }) => {
    // Given: 각 브라우저에서 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: JavaScript API 사용 기능 테스트
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 브라우저별 API 지원 확인
    const apiSupport = await page.evaluate(() => {
      return {
        fetch: typeof fetch !== 'undefined',
        localStorage: typeof localStorage !== 'undefined',
        sessionStorage: typeof sessionStorage !== 'undefined',
        webSocket: typeof WebSocket !== 'undefined',
        notification: typeof Notification !== 'undefined',
        geolocation: typeof navigator.geolocation !== 'undefined',
        intersectionObserver: typeof IntersectionObserver !== 'undefined'
      };
    });
    
    // 모든 필수 API가 지원되는지 확인
    expect(apiSupport.fetch).toBe(true);
    expect(apiSupport.localStorage).toBe(true);
    expect(apiSupport.sessionStorage).toBe(true);
    
    console.log(`${browserName} API 지원 현황:`, apiSupport);
  });
});

test.describe('반응형 디자인 테스트', () => {
  
  test('모바일 디바이스에서 구독 승인 워크플로우', async ({ page }) => {
    // Given: 모바일 뷰포트 설정
    await page.setViewportSize(VIEWPORTS.mobile);
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 모바일에서 승인 관리 접근
    await page.click('[data-testid="mobile-menu-toggle"]');
    await expect(page.locator('[data-testid="mobile-sidebar"]')).toBeVisible();
    
    await page.click('[data-testid="mobile-approvals-menu"]');
    
    // Then: 모바일 최적화된 레이아웃 확인
    await expect(page.locator('[data-testid="mobile-approval-list"]')).toBeVisible();
    
    // 카드 레이아웃이 세로로 배치됨
    const approvalCards = page.locator('[data-testid="approval-card"]');
    const firstCard = approvalCards.first();
    const secondCard = approvalCards.nth(1);
    
    const firstCardBox = await firstCard.boundingBox();
    const secondCardBox = await secondCard.boundingBox();
    
    // 카드들이 세로로 배치되었는지 확인
    if (firstCardBox && secondCardBox) {
      expect(secondCardBox.y).toBeGreaterThan(firstCardBox.y + firstCardBox.height);
    }
    
    // 터치 친화적인 버튼 크기 확인
    const approveButton = page.locator('[data-testid="mobile-approve-button"]').first();
    const buttonBox = await approveButton.boundingBox();
    expect(buttonBox?.height).toBeGreaterThanOrEqual(44); // 최소 터치 타겟 크기
    
    // 스와이프 제스처 테스트
    await firstCard.click();
    await expect(page.locator('[data-testid="mobile-approval-detail"]')).toBeVisible();
    
    // 뒤로 가기 제스처 (스와이프)
    await page.touchscreen.tap(50, 300);
    await page.mouse.move(50, 300);
    await page.mouse.down();
    await page.mouse.move(300, 300);
    await page.mouse.up();
  });

  test('태블릿에서 구독 승인 워크플로우', async ({ page }) => {
    // Given: 태블릿 뷰포트 설정
    await page.setViewportSize(VIEWPORTS.tablet);
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 태블릿에서 승인 관리 접근
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 태블릿 최적화된 레이아웃 확인
    await expect(page.locator('[data-testid="tablet-layout"]')).toBeVisible();
    
    // 2열 그리드 레이아웃 확인
    const approvalGrid = page.locator('[data-testid="approval-grid"]');
    await expect(approvalGrid).toHaveCSS('grid-template-columns', /repeat\(2,/);
    
    // 사이드바가 축소된 형태로 표시됨
    const sidebar = page.locator('[data-testid="sidebar"]');
    await expect(sidebar).toHaveClass(/collapsed/);
    
    // 터치와 마우스 모두 지원
    const approvalItem = page.locator('[data-testid="approval-item"]').first();
    
    // 터치 이벤트
    await approvalItem.tap();
    await expect(page.locator('[data-testid="approval-detail-modal"]')).toBeVisible();
    
    await page.click('[data-testid="close-modal"]');
    
    // 마우스 호버 이벤트
    await approvalItem.hover();
    await expect(approvalItem).toHaveClass(/hover/);
  });

  test('데스크톱에서 구독 승인 워크플로우', async ({ page }) => {
    // Given: 데스크톱 뷰포트 설정
    await page.setViewportSize(VIEWPORTS.desktop);
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 데스크톱에서 승인 관리 접근
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 데스크톱 최적화된 레이아웃 확인
    await expect(page.locator('[data-testid="desktop-layout"]')).toBeVisible();
    
    // 3열 이상 그리드 레이아웃 확인
    const approvalGrid = page.locator('[data-testid="approval-grid"]');
    await expect(approvalGrid).toHaveCSS('grid-template-columns', /repeat\([3-9],/);
    
    // 전체 사이드바가 표시됨
    const sidebar = page.locator('[data-testid="sidebar"]');
    await expect(sidebar).toBeVisible();
    await expect(sidebar).not.toHaveClass(/collapsed/);
    
    // 키보드 단축키 지원
    await page.keyboard.press('Control+k');
    await expect(page.locator('[data-testid="search-modal"]')).toBeVisible();
    
    await page.keyboard.press('Escape');
    await expect(page.locator('[data-testid="search-modal"]')).not.toBeVisible();
  });

  test('울트라와이드 모니터에서 레이아웃 테스트', async ({ page }) => {
    // Given: 울트라와이드 뷰포트 설정
    await page.setViewportSize(VIEWPORTS.ultrawide);
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 울트라와이드에서 승인 관리 접근
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 울트라와이드 최적화된 레이아웃 확인
    const container = page.locator('[data-testid="main-container"]');
    
    // 최대 너비 제한이 적용되어 있는지 확인
    const containerBox = await container.boundingBox();
    expect(containerBox?.width).toBeLessThanOrEqual(1400); // max-width 제한
    
    // 컨텐츠가 중앙 정렬되어 있는지 확인
    await expect(container).toHaveCSS('margin-left', 'auto');
    await expect(container).toHaveCSS('margin-right', 'auto');
  });

  test('뷰포트 크기 변경 시 동적 레이아웃 조정', async ({ page }) => {
    // Given: 데스크톱 크기로 시작
    await page.setViewportSize(VIEWPORTS.desktop);
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    await page.click('[data-testid="approvals-menu"]');
    
    // When: 태블릿 크기로 변경
    await page.setViewportSize(VIEWPORTS.tablet);
    
    // Then: 레이아웃이 동적으로 조정됨
    await expect(page.locator('[data-testid="tablet-layout"]')).toBeVisible();
    
    // When: 모바일 크기로 변경
    await page.setViewportSize(VIEWPORTS.mobile);
    
    // Then: 모바일 레이아웃으로 전환됨
    await expect(page.locator('[data-testid="mobile-layout"]')).toBeVisible();
    await expect(page.locator('[data-testid="mobile-menu-toggle"]')).toBeVisible();
    
    // When: 다시 데스크톱 크기로 변경
    await page.setViewportSize(VIEWPORTS.desktop);
    
    // Then: 데스크톱 레이아웃으로 복원됨
    await expect(page.locator('[data-testid="desktop-layout"]')).toBeVisible();
    await expect(page.locator('[data-testid="mobile-menu-toggle"]')).not.toBeVisible();
  });

  test('다크 모드 지원 테스트', async ({ page }) => {
    // Given: 시스템 다크 모드 설정
    await page.emulateMedia({ colorScheme: 'dark' });
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 다크 모드 스타일이 적용됨
    await expect(page.locator('body')).toHaveClass(/dark/);
    
    // 다크 모드 색상 확인
    const backgroundColor = await page.locator('body').evaluate(el => 
      getComputedStyle(el).backgroundColor
    );
    
    // 다크 모드에서는 배경색이 어두워야 함
    expect(backgroundColor).toMatch(/rgb\(([0-4]\d|[0-9]),\s*([0-4]\d|[0-9]),\s*([0-4]\d|[0-9])\)/);
    
    // When: 라이트 모드로 전환
    await page.emulateMedia({ colorScheme: 'light' });
    
    // Then: 라이트 모드 스타일이 적용됨
    await expect(page.locator('body')).not.toHaveClass(/dark/);
  });

  test('고해상도 디스플레이 지원 테스트', async ({ page }) => {
    // Given: 고해상도 디스플레이 설정 (Retina)
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.emulateMedia({ reducedMotion: 'no-preference' });
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 고해상도 이미지가 로드됨
    const logoImage = page.locator('[data-testid="logo-image"]');
    await expect(logoImage).toHaveAttribute('src', /.*@2x\.(png|jpg|webp)/);
    
    // SVG 아이콘이 선명하게 렌더링됨
    const statusIcon = page.locator('[data-testid="status-icon"]');
    await expect(statusIcon).toBeVisible();
    
    // 텍스트가 선명하게 렌더링됨 (subpixel rendering)
    const textElement = page.locator('[data-testid="approval-title"]').first();
    const textRendering = await textElement.evaluate(el => 
      getComputedStyle(el).textRendering
    );
    expect(textRendering).toBe('optimizeLegibility');
  });

  test('접근성 - 키보드 네비게이션 테스트', async ({ page }) => {
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 키보드만으로 네비게이션
    await page.keyboard.press('Tab'); // 첫 번째 포커스 가능한 요소로
    
    let focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();
    
    // Tab으로 승인 관리 메뉴까지 이동
    for (let i = 0; i < 10; i++) {
      await page.keyboard.press('Tab');
      focusedElement = page.locator(':focus');
      
      const elementText = await focusedElement.textContent();
      if (elementText?.includes('승인 관리')) {
        break;
      }
    }
    
    // Enter로 승인 관리 페이지 진입
    await page.keyboard.press('Enter');
    await expect(page).toHaveURL(/.*approvals.*/);
    
    // 승인 목록에서 키보드 네비게이션
    await page.keyboard.press('Tab');
    focusedElement = page.locator(':focus');
    
    // 화살표 키로 목록 항목 간 이동
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('ArrowUp');
    
    // Enter로 승인 상세 모달 열기
    await page.keyboard.press('Enter');
    await expect(page.locator('[data-testid="approval-detail-modal"]')).toBeVisible();
    
    // Escape로 모달 닫기
    await page.keyboard.press('Escape');
    await expect(page.locator('[data-testid="approval-detail-modal"]')).not.toBeVisible();
  });

  test('성능 - 반응형 이미지 로딩 테스트', async ({ page }) => {
    // Given: 모바일 뷰포트로 시작
    await page.setViewportSize(VIEWPORTS.mobile);
    
    // 네트워크 속도 제한 (3G 시뮬레이션)
    await page.route('**/*', route => {
      setTimeout(() => route.continue(), 100); // 100ms 지연
    });
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', TEST_SUPER_ADMIN.email);
    await page.fill('[data-testid="password-input"]', TEST_SUPER_ADMIN.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    const startTime = Date.now();
    await page.click('[data-testid="approvals-menu"]');
    
    // Then: 모바일에서 빠른 로딩 확인
    await expect(page.locator('[data-testid="approval-list"]')).toBeVisible();
    const loadTime = Date.now() - startTime;
    
    // 모바일에서 5초 이내 로딩 완료
    expect(loadTime).toBeLessThan(5000);
    
    // 이미지 lazy loading 확인
    const images = page.locator('img[loading="lazy"]');
    const imageCount = await images.count();
    expect(imageCount).toBeGreaterThan(0);
    
    // When: 데스크톱으로 변경
    await page.setViewportSize(VIEWPORTS.desktop);
    
    // Then: 고해상도 이미지가 로드됨
    const highResImages = page.locator('img[srcset]');
    const highResCount = await highResImages.count();
    expect(highResCount).toBeGreaterThan(0);
  });
});