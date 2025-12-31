import { test, expect } from '@playwright/test';

/**
 * 기본 기능 E2E 테스트
 * 
 * Playwright 설정 및 기본 기능 검증
 */

test.describe('기본 기능 테스트', () => {
  
  test('페이지 로딩 테스트', async ({ page }) => {
    // Given: 애플리케이션에 접속
    await page.goto('http://localhost:3001', { waitUntil: 'networkidle' });
    
    // Then: 페이지가 정상적으로 로드됨
    await expect(page).toHaveTitle(/SmartCON/);
    
    // HTML 구조 확인
    const htmlContent = await page.content();
    console.log('Page HTML structure:', htmlContent.substring(0, 500));
    
    // 기본 요소들이 존재하는지 확인
    await expect(page.locator('html')).toBeVisible();
    await expect(page.locator('head')).toBeAttached();
    
    // React 앱 루트 요소가 존재하는지 확인
    const rootExists = await page.locator('#root').count();
    expect(rootExists).toBeGreaterThan(0);
  });

  test('브라우저 호환성 기본 테스트', async ({ page, browserName }) => {
    // Given: 각 브라우저에서 페이지 접속
    await page.goto('http://localhost:3001');
    
    // Then: 브라우저별로 정상 작동 확인
    console.log(`Testing on ${browserName}`);
    
    // 기본 JavaScript API 지원 확인
    const apiSupport = await page.evaluate(() => {
      return {
        fetch: typeof fetch !== 'undefined',
        localStorage: typeof localStorage !== 'undefined',
        sessionStorage: typeof sessionStorage !== 'undefined'
      };
    });
    
    expect(apiSupport.fetch).toBe(true);
    expect(apiSupport.localStorage).toBe(true);
    expect(apiSupport.sessionStorage).toBe(true);
  });

  test('반응형 디자인 기본 테스트', async ({ page }) => {
    // Given: 다양한 뷰포트 크기 테스트
    const viewports = [
      { width: 375, height: 667, name: 'Mobile' },
      { width: 768, height: 1024, name: 'Tablet' },
      { width: 1920, height: 1080, name: 'Desktop' }
    ];

    for (const viewport of viewports) {
      // When: 뷰포트 크기 변경
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto('http://localhost:3001', { waitUntil: 'networkidle' });
      
      // Then: 각 크기에서 페이지가 정상 렌더링됨
      await expect(page).toHaveTitle(/SmartCON/);
      
      // 뷰포트 크기가 올바르게 설정되었는지 확인
      const viewportSize = page.viewportSize();
      expect(viewportSize?.width).toBe(viewport.width);
      expect(viewportSize?.height).toBe(viewport.height);
      
      console.log(`${viewport.name} (${viewport.width}x${viewport.height}) - OK`);
    }
  });
});