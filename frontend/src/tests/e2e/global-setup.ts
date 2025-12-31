import { chromium } from '@playwright/test';
import type { FullConfig } from '@playwright/test';

/**
 * Playwright 글로벌 설정
 * 
 * E2E 테스트 실행 전 전역 설정을 수행합니다.
 */
async function globalSetup(_config: FullConfig) {
  console.log('🚀 E2E 테스트 환경 설정 시작...');
  
  // 브라우저 인스턴스 생성
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();
  
  try {
    // 백엔드 서버 상태 확인 (선택적)
    console.log('📡 백엔드 서버 연결 확인...');
    try {
      const response = await page.goto('http://localhost:8080/actuator/health');
      
      if (response?.status() !== 200) {
        console.log('⚠️ 백엔드 서버가 실행되지 않았습니다. 일부 테스트가 제한될 수 있습니다.');
      } else {
        console.log('✅ 백엔드 서버 연결 확인 완료');
        
        // 테스트 데이터 초기화 (백엔드가 실행 중일 때만)
        console.log('🗄️ 테스트 데이터 초기화...');
        await page.goto('http://localhost:8080/api/test/reset-data', { 
          waitUntil: 'networkidle' 
        });
        
        // 테스트용 사용자 생성
        console.log('👤 테스트 사용자 생성...');
        await page.evaluate(async () => {
          // 슈퍼관리자 생성
          await fetch('http://localhost:8080/api/test/create-super-admin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              email: 'super@smartcon.com',
              password: 'test123',
              name: '테스트 슈퍼관리자'
            })
          });
          
          // 테넌트 관리자 생성
          await fetch('http://localhost:8080/api/test/create-tenant-admin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              email: 'tenant@test.com',
              password: 'test123',
              name: '테스트 테넌트관리자',
              companyName: '테스트 회사'
            })
          });
        });
        
        // 테스트용 구독 데이터 생성
        console.log('📋 테스트 구독 데이터 생성...');
        await page.evaluate(async () => {
          await fetch('http://localhost:8080/api/test/create-test-subscriptions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              count: 5 // 5개의 테스트 구독 생성
            })
          });
        });
      }
    } catch (backendError) {
      console.log('⚠️ 백엔드 서버에 연결할 수 없습니다. 프론트엔드 전용 테스트만 실행됩니다.');
    }
    
    console.log('✅ E2E 테스트 환경 설정 완료');
    
  } catch (error) {
    console.error('❌ E2E 테스트 환경 설정 실패:', error);
    // 백엔드 연결 실패는 치명적이지 않으므로 에러를 던지지 않음
  } finally {
    await browser.close();
  }
}

export default globalSetup;