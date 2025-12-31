import { defineConfig, devices } from '@playwright/test';
import type { FullConfig } from '@playwright/test';

/**
 * Playwright E2E 테스트 설정
 * 
 * 구독 승인 워크플로우 E2E 테스트를 위한 설정
 */
export default defineConfig({
  // 테스트 파일 위치
  testDir: './src/tests/e2e',
  
  // 전역 설정
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  
  // 리포터 설정
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/results.xml' }]
  ],
  
  // 테스트 옵션
  use: {
    // 기본 URL
    baseURL: 'http://localhost:5173',
    
    // 브라우저 설정
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    
    // 타임아웃 설정
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  // 프로젝트별 설정 (브라우저별)
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    
    // 모바일 테스트
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
    
    // 태블릿 테스트
    {
      name: 'Tablet',
      use: { ...devices['iPad Pro'] },
    },
  ],

  // 개발 서버 설정
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
  
  // 테스트 결과 디렉토리
  outputDir: 'test-results/',
  
  // 글로벌 설정
  globalSetup: './src/tests/e2e/global-setup.ts',
  globalTeardown: './src/tests/e2e/global-teardown.ts',
});