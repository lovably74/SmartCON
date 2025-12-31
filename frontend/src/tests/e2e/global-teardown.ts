import { chromium } from '@playwright/test';
import type { FullConfig } from '@playwright/test';

/**
 * Playwright 글로벌 정리
 * 
 * E2E 테스트 실행 후 정리 작업을 수행합니다.
 */
async function globalTeardown(_config: FullConfig) {
  console.log('🧹 E2E 테스트 환경 정리 시작...');
  
  // 브라우저 인스턴스 생성
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();
  
  try {
    // 테스트 데이터 정리
    console.log('🗑️ 테스트 데이터 정리...');
    await page.goto('http://localhost:8080/api/test/cleanup-data', { 
      waitUntil: 'networkidle' 
    });
    
    // 테스트 결과 요약 출력
    console.log('📊 테스트 결과 요약 생성...');
    
    // 테스트 결과 파일이 있다면 요약 정보 출력
    try {
      const fs = require('fs');
      const path = require('path');
      
      const resultsPath = path.join(process.cwd(), 'test-results', 'results.json');
      if (fs.existsSync(resultsPath)) {
        const results = JSON.parse(fs.readFileSync(resultsPath, 'utf8'));
        
        console.log('\n📈 E2E 테스트 결과 요약:');
        console.log(`✅ 성공: ${results.stats?.passed || 0}개`);
        console.log(`❌ 실패: ${results.stats?.failed || 0}개`);
        console.log(`⏭️ 건너뜀: ${results.stats?.skipped || 0}개`);
        console.log(`⏱️ 총 실행 시간: ${results.stats?.duration || 0}ms`);
        
        if (results.stats?.failed > 0) {
          console.log('\n❌ 실패한 테스트:');
          results.suites?.forEach((suite: any) => {
            suite.specs?.forEach((spec: any) => {
              spec.tests?.forEach((test: any) => {
                if (test.results?.[0]?.status === 'failed') {
                  console.log(`  - ${suite.title}: ${spec.title}`);
                }
              });
            });
          });
        }
      }
    } catch (error) {
      console.log('테스트 결과 요약 생성 중 오류:', error);
    }
    
    console.log('✅ E2E 테스트 환경 정리 완료');
    
  } catch (error) {
    console.error('❌ E2E 테스트 환경 정리 실패:', error);
  } finally {
    await browser.close();
  }
}

export default globalTeardown;