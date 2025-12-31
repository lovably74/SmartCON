import { test, expect } from '@playwright/test';

/**
 * 사용자 시나리오별 E2E 테스트
 * 
 * 실제 사용자 워크플로우를 기반으로 한 종합적인 E2E 테스트
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */

// 테스트 데이터
const USERS = {
  superAdmin: {
    email: 'super@smartcon.com',
    password: 'test123',
    name: '슈퍼관리자'
  },
  tenantAdmin: {
    email: 'tenant@test.com',
    password: 'test123',
    name: '테넌트관리자',
    company: '테스트 건설회사'
  },
  newTenant: {
    email: 'newtenant@example.com',
    password: 'test123',
    name: '신규 테넌트',
    company: '신규 건설회사'
  }
};

test.describe('실제 사용자 시나리오 E2E 테스트', () => {
  
  test('시나리오 1: 신규 고객의 구독 신청부터 승인까지 전체 플로우', async ({ page }) => {
    // === 1단계: 신규 고객 회원가입 및 구독 신청 ===
    
    // Given: 신규 고객이 서비스에 접속
    await page.goto('/');
    
    // When: 회원가입 페이지로 이동
    await page.click('[data-testid="signup-button"]');
    await expect(page).toHaveURL('/signup');
    
    // 회원가입 정보 입력
    await page.fill('[data-testid="company-name"]', USERS.newTenant.company);
    await page.fill('[data-testid="admin-name"]', USERS.newTenant.name);
    await page.fill('[data-testid="email"]', USERS.newTenant.email);
    await page.fill('[data-testid="password"]', USERS.newTenant.password);
    await page.fill('[data-testid="password-confirm"]', USERS.newTenant.password);
    
    // 이용약관 동의
    await page.check('[data-testid="terms-agreement"]');
    await page.check('[data-testid="privacy-agreement"]');
    
    // 회원가입 완료
    await page.click('[data-testid="signup-submit"]');
    
    // Then: 구독 플랜 선택 페이지로 이동
    await expect(page).toHaveURL('/subscribe');
    
    // When: 구독 플랜 선택
    await page.click('[data-testid="plan-basic"]');
    await page.click('[data-testid="billing-monthly"]');
    
    // 결제 정보 입력
    await page.fill('[data-testid="card-number"]', '4111111111111111');
    await page.fill('[data-testid="card-expiry"]', '12/25');
    await page.fill('[data-testid="card-cvc"]', '123');
    
    // 구독 신청 완료
    await page.click('[data-testid="subscribe-button"]');
    
    // Then: 승인 대기 상태 안내 페이지로 이동
    await expect(page).toHaveURL('/subscription/pending');
    await expect(page.locator('[data-testid="pending-message"]')).toContainText('승인 대기 중입니다');
    
    // === 2단계: 슈퍼관리자가 승인 요청 확인 ===
    
    // 새 탭에서 슈퍼관리자로 로그인
    const adminPage = await page.context().newPage();
    await adminPage.goto('/login');
    await adminPage.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await adminPage.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await adminPage.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지로 이동
    await adminPage.click('[data-testid="approvals-menu"]');
    
    // Then: 새로운 승인 요청이 목록에 표시됨
    const newApprovalItem = adminPage.locator('[data-testid="approval-item"]')
      .filter({ hasText: USERS.newTenant.company });
    await expect(newApprovalItem).toBeVisible();
    
    // 승인 요청 상세 확인
    await newApprovalItem.click();
    await expect(adminPage.locator('[data-testid="approval-detail-modal"]')).toBeVisible();
    await expect(adminPage.locator('[data-testid="company-name"]')).toContainText(USERS.newTenant.company);
    await expect(adminPage.locator('[data-testid="admin-name"]')).toContainText(USERS.newTenant.name);
    
    // === 3단계: 슈퍼관리자가 승인 처리 ===
    
    // When: 승인 버튼 클릭
    await adminPage.click('[data-testid="approve-button"]');
    
    // 승인 코멘트 입력
    await adminPage.fill('[data-testid="approval-comment"]', '신규 고객 승인 완료');
    await adminPage.click('[data-testid="confirm-approve-button"]');
    
    // Then: 승인 완료 메시지 표시
    await expect(adminPage.locator('[data-testid="success-message"]')).toContainText('승인이 완료되었습니다');
    
    // === 4단계: 테넌트가 승인 완료 확인 및 서비스 이용 ===
    
    // 원래 페이지로 돌아가서 새로고침
    await page.reload();
    
    // Then: 승인 완료 상태로 변경됨
    await expect(page).toHaveURL('/hq/dashboard');
    await expect(page.locator('[data-testid="welcome-message"]')).toContainText('환영합니다');
    
    // 모든 서비스 메뉴에 접근 가능
    const serviceMenus = ['dashboard-menu', 'attendance-menu', 'sites-menu', 'workers-menu'];
    for (const menu of serviceMenus) {
      const menuElement = page.locator(`[data-testid="${menu}"]`);
      await expect(menuElement).not.toHaveAttribute('aria-disabled', 'true');
    }
    
    // When: 출석 관리 페이지 접근 테스트
    await page.click('[data-testid="attendance-menu"]');
    
    // Then: 정상적으로 서비스 이용 가능
    await expect(page).toHaveURL('/hq/attendance');
    await expect(page.locator('[data-testid="attendance-dashboard"]')).toBeVisible();
    
    await adminPage.close();
  });

  test('시나리오 2: 구독 거부 후 재신청 플로우', async ({ page }) => {
    // === 1단계: 슈퍼관리자가 구독 요청 거부 ===
    
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 승인 관리 페이지에서 특정 요청 거부
    await page.click('[data-testid="approvals-menu"]');
    
    const pendingApproval = page.locator('[data-testid="approval-item"]').first();
    await pendingApproval.click();
    
    // 거부 처리
    await page.click('[data-testid="reject-button"]');
    await page.fill('[data-testid="rejection-reason"]', '제출 서류가 불완전합니다. 사업자등록증과 대표자 신분증을 추가로 제출해주세요.');
    await page.click('[data-testid="confirm-reject-button"]');
    
    // Then: 거부 완료 확인
    await expect(page.locator('[data-testid="success-message"]')).toContainText('거부가 완료되었습니다');
    
    // === 2단계: 테넌트가 거부 통지 확인 ===
    
    // 테넌트로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.tenantAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.tenantAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 거부 상태 메시지 확인
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('승인이 거부되었습니다');
    await expect(page.locator('[data-testid="rejection-reason"]')).toContainText('제출 서류가 불완전합니다');
    
    // === 3단계: 테넌트가 재신청 ===
    
    // When: 재신청 버튼 클릭
    await page.click('[data-testid="reapply-button"]');
    
    // Then: 재신청 모달이 열림
    await expect(page.locator('[data-testid="reapplication-modal"]')).toBeVisible();
    
    // 이전 거부 사유 확인
    await expect(page.locator('[data-testid="previous-rejection-reason"]')).toContainText('제출 서류가 불완전합니다');
    
    // When: 개선사항 입력 및 추가 서류 업로드
    await page.fill('[data-testid="improvement-notes"]', '요청하신 사업자등록증과 대표자 신분증을 추가로 첨부하였습니다.');
    
    // 파일 업로드 시뮬레이션
    const fileInput = page.locator('[data-testid="additional-documents"]');
    await fileInput.setInputFiles([
      { name: 'business-license.pdf', mimeType: 'application/pdf', buffer: Buffer.from('fake pdf content') },
      { name: 'id-card.pdf', mimeType: 'application/pdf', buffer: Buffer.from('fake pdf content') }
    ]);
    
    // 재신청 제출
    await page.click('[data-testid="submit-reapplication"]');
    
    // Then: 재신청 완료 메시지
    await expect(page.locator('[data-testid="success-message"]')).toContainText('재신청이 완료되었습니다');
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('승인 대기 중');
    
    // === 4단계: 슈퍼관리자가 재신청 승인 ===
    
    // 슈퍼관리자로 다시 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    await page.click('[data-testid="approvals-menu"]');
    
    // 재신청된 요청 확인 (재신청 표시가 있어야 함)
    const reapplicationItem = page.locator('[data-testid="approval-item"]')
      .filter({ hasText: '재신청' });
    await expect(reapplicationItem).toBeVisible();
    
    await reapplicationItem.click();
    
    // 개선사항 확인
    await expect(page.locator('[data-testid="improvement-notes"]')).toContainText('사업자등록증과 대표자 신분증을 추가로 첨부');
    
    // 추가 서류 확인
    await expect(page.locator('[data-testid="additional-documents-list"]')).toContainText('business-license.pdf');
    await expect(page.locator('[data-testid="additional-documents-list"]')).toContainText('id-card.pdf');
    
    // When: 재신청 승인
    await page.click('[data-testid="approve-button"]');
    await page.fill('[data-testid="approval-comment"]', '추가 서류 확인 완료. 승인합니다.');
    await page.click('[data-testid="confirm-approve-button"]');
    
    // Then: 승인 완료
    await expect(page.locator('[data-testid="success-message"]')).toContainText('승인이 완료되었습니다');
  });

  test('시나리오 3: 활성 구독의 일시 중지 및 재활성화 플로우', async ({ page }) => {
    // === 1단계: 슈퍼관리자가 활성 구독을 일시 중지 ===
    
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 테넌트 관리 페이지로 이동
    await page.click('[data-testid="tenants-menu"]');
    
    // 활성 구독 선택
    const activeSubscription = page.locator('[data-testid="subscription-item"]')
      .filter({ hasText: '활성' }).first();
    await activeSubscription.click();
    
    // 구독 상세 모달에서 중지 버튼 클릭
    await page.click('[data-testid="suspend-subscription-button"]');
    
    // 중지 사유 입력
    await page.fill('[data-testid="suspension-reason"]', '결제 연체로 인한 일시 중지');
    await page.click('[data-testid="confirm-suspend-button"]');
    
    // Then: 중지 완료 확인
    await expect(page.locator('[data-testid="success-message"]')).toContainText('구독이 일시 중지되었습니다');
    
    // === 2단계: 테넌트가 중지 상태 확인 ===
    
    // 테넌트로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.tenantAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.tenantAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 중지 상태 메시지 확인
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('서비스가 일시 중지되었습니다');
    await expect(page.locator('[data-testid="suspension-reason"]')).toContainText('결제 연체로 인한 일시 중지');
    
    // 고객센터 연락처 표시 확인
    await expect(page.locator('[data-testid="customer-service-contact"]')).toBeVisible();
    
    // When: 서비스 메뉴 접근 시도
    await page.click('[data-testid="dashboard-menu"]');
    
    // Then: 접근 차단 모달 표시
    await expect(page.locator('[data-testid="service-suspended-modal"]')).toBeVisible();
    await expect(page.locator('[data-testid="service-suspended-modal"]')).toContainText('서비스가 일시 중지된 상태입니다');
    
    // === 3단계: 테넌트가 문의 및 결제 완료 ===
    
    // When: 문의하기 버튼 클릭
    await page.click('[data-testid="contact-support-button"]');
    
    // 문의 양식 작성
    await page.fill('[data-testid="inquiry-subject"]', '결제 완료 및 서비스 재개 요청');
    await page.fill('[data-testid="inquiry-message"]', '연체된 결제를 완료하였습니다. 서비스 재개를 요청드립니다.');
    
    // 결제 완료 증빙 서류 업로드
    const receiptInput = page.locator('[data-testid="payment-receipt"]');
    await receiptInput.setInputFiles({
      name: 'payment-receipt.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('fake receipt content')
    });
    
    await page.click('[data-testid="submit-inquiry"]');
    
    // Then: 문의 접수 완료
    await expect(page.locator('[data-testid="success-message"]')).toContainText('문의가 접수되었습니다');
    
    // === 4단계: 슈퍼관리자가 재활성화 ===
    
    // 슈퍼관리자로 다시 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // 문의 사항 확인
    await page.click('[data-testid="inquiries-menu"]');
    const paymentInquiry = page.locator('[data-testid="inquiry-item"]')
      .filter({ hasText: '결제 완료 및 서비스 재개 요청' });
    await paymentInquiry.click();
    
    // 결제 증빙 서류 확인
    await expect(page.locator('[data-testid="attached-receipt"]')).toContainText('payment-receipt.pdf');
    
    // When: 테넌트 관리에서 재활성화
    await page.click('[data-testid="tenants-menu"]');
    
    const suspendedSubscription = page.locator('[data-testid="subscription-item"]')
      .filter({ hasText: '중지됨' }).first();
    await suspendedSubscription.click();
    
    await page.click('[data-testid="reactivate-subscription-button"]');
    await page.fill('[data-testid="reactivation-reason"]', '결제 완료 확인. 서비스 재활성화');
    await page.click('[data-testid="confirm-reactivate-button"]');
    
    // Then: 재활성화 완료
    await expect(page.locator('[data-testid="success-message"]')).toContainText('구독이 재활성화되었습니다');
    
    // === 5단계: 테넌트가 서비스 재개 확인 ===
    
    // 테넌트로 다시 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.tenantAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.tenantAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 정상 서비스 이용 가능
    await expect(page).toHaveURL('/hq/dashboard');
    await expect(page.locator('[data-testid="service-restored-notice"]')).toContainText('서비스가 재개되었습니다');
    
    // 모든 메뉴에 정상 접근 가능
    await page.click('[data-testid="attendance-menu"]');
    await expect(page).toHaveURL('/hq/attendance');
  });

  test('시나리오 4: 구독 종료 및 신규 구독 신청 플로우', async ({ page }) => {
    // === 1단계: 슈퍼관리자가 구독 종료 ===
    
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 테넌트 관리에서 구독 종료
    await page.click('[data-testid="tenants-menu"]');
    
    const activeSubscription = page.locator('[data-testid="subscription-item"]')
      .filter({ hasText: '활성' }).first();
    await activeSubscription.click();
    
    await page.click('[data-testid="terminate-subscription-button"]');
    
    // 종료 사유 입력
    await page.fill('[data-testid="termination-reason"]', '계약 위반으로 인한 서비스 종료');
    
    // 종료 확인 체크박스
    await page.check('[data-testid="termination-confirmation"]');
    
    await page.click('[data-testid="confirm-terminate-button"]');
    
    // Then: 종료 완료 확인
    await expect(page.locator('[data-testid="success-message"]')).toContainText('구독이 종료되었습니다');
    
    // === 2단계: 테넌트가 종료 상태 확인 ===
    
    // 테넌트로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.tenantAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.tenantAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // Then: 종료 상태 메시지 확인
    await expect(page.locator('[data-testid="subscription-status-message"]')).toContainText('구독이 종료되었습니다');
    await expect(page.locator('[data-testid="termination-reason"]')).toContainText('계약 위반으로 인한 서비스 종료');
    
    // 데이터 백업 안내 확인
    await expect(page.locator('[data-testid="data-backup-notice"]')).toContainText('기존 데이터는 30일간 보관됩니다');
    
    // === 3단계: 새 구독 신청 ===
    
    // When: 새 구독 신청 버튼 클릭
    await page.click('[data-testid="new-subscription-button"]');
    
    // Then: 구독 신청 페이지로 이동
    await expect(page).toHaveURL('/subscribe');
    
    // 이전 구독 정보 표시 확인
    await expect(page.locator('[data-testid="previous-subscription-info"]')).toBeVisible();
    
    // When: 새 구독 플랜 선택
    await page.click('[data-testid="plan-premium"]'); // 이전과 다른 플랜 선택
    await page.click('[data-testid="billing-annual"]');
    
    // 신규 신청 사유 입력
    await page.fill('[data-testid="new-subscription-reason"]', '이전 문제점을 개선하여 새로운 구독을 신청합니다.');
    
    // 결제 정보 입력
    await page.fill('[data-testid="card-number"]', '4111111111111111');
    await page.fill('[data-testid="card-expiry"]', '12/26');
    await page.fill('[data-testid="card-cvc"]', '456');
    
    await page.click('[data-testid="subscribe-button"]');
    
    // Then: 새 구독 신청 완료
    await expect(page).toHaveURL('/subscription/pending');
    await expect(page.locator('[data-testid="new-subscription-pending-message"]')).toContainText('새 구독 신청이 완료되었습니다');
    
    // === 4단계: 슈퍼관리자가 새 구독 검토 ===
    
    // 슈퍼관리자로 다시 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    await page.click('[data-testid="approvals-menu"]');
    
    // 새 구독 신청 확인 (재가입 표시가 있어야 함)
    const newSubscriptionItem = page.locator('[data-testid="approval-item"]')
      .filter({ hasText: '재가입' });
    await expect(newSubscriptionItem).toBeVisible();
    
    await newSubscriptionItem.click();
    
    // 이전 종료 이력 확인
    await expect(page.locator('[data-testid="previous-termination-history"]')).toBeVisible();
    await expect(page.locator('[data-testid="termination-reason-history"]')).toContainText('계약 위반으로 인한 서비스 종료');
    
    // 신규 신청 사유 확인
    await expect(page.locator('[data-testid="new-subscription-reason"]')).toContainText('이전 문제점을 개선하여');
    
    // When: 신중한 검토 후 승인
    await page.click('[data-testid="approve-button"]');
    await page.fill('[data-testid="approval-comment"]', '이전 문제점 개선 확인. 새 구독을 승인합니다.');
    await page.click('[data-testid="confirm-approve-button"]');
    
    // Then: 새 구독 승인 완료
    await expect(page.locator('[data-testid="success-message"]')).toContainText('새 구독이 승인되었습니다');
  });

  test('시나리오 5: 자동 승인 규칙 설정 및 적용 플로우', async ({ page }) => {
    // === 1단계: 슈퍼관리자가 자동 승인 규칙 설정 ===
    
    // Given: 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 자동 승인 규칙 관리 페이지로 이동
    await page.click('[data-testid="auto-approval-menu"]');
    
    // 새 규칙 생성
    await page.click('[data-testid="create-rule-button"]');
    
    // 규칙 설정
    await page.fill('[data-testid="rule-name"]', '검증된 기업 자동 승인');
    await page.check('[data-testid="verified-tenants-only"]');
    await page.selectOption('[data-testid="plan-types"]', ['basic', 'standard']);
    await page.selectOption('[data-testid="payment-methods"]', ['credit-card']);
    await page.fill('[data-testid="max-amount"]', '100000');
    
    await page.click('[data-testid="save-rule-button"]');
    
    // Then: 규칙 생성 완료
    await expect(page.locator('[data-testid="success-message"]')).toContainText('자동 승인 규칙이 생성되었습니다');
    
    // === 2단계: 자동 승인 조건에 맞는 구독 신청 ===
    
    // 검증된 테넌트로 로그인 (테스트 데이터에서 검증된 상태로 설정)
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'verified@company.com');
    await page.fill('[data-testid="password-input"]', 'test123');
    await page.click('[data-testid="login-button"]');
    
    // When: 자동 승인 조건에 맞는 구독 신청
    await page.click('[data-testid="subscription-menu"]');
    await page.click('[data-testid="new-subscription-button"]');
    
    // 자동 승인 조건에 맞는 플랜 선택
    await page.click('[data-testid="plan-basic"]'); // 규칙에 포함된 플랜
    await page.click('[data-testid="billing-monthly"]');
    
    // 신용카드 결제 선택 (규칙에 포함된 결제 방법)
    await page.click('[data-testid="payment-credit-card"]');
    await page.fill('[data-testid="card-number"]', '4111111111111111');
    await page.fill('[data-testid="card-expiry"]', '12/25');
    await page.fill('[data-testid="card-cvc"]', '123');
    
    await page.click('[data-testid="subscribe-button"]');
    
    // Then: 자동 승인되어 즉시 활성화
    await expect(page).toHaveURL('/hq/dashboard');
    await expect(page.locator('[data-testid="auto-approval-notice"]')).toContainText('구독이 자동으로 승인되었습니다');
    
    // === 3단계: 자동 승인 로그 확인 ===
    
    // 슈퍼관리자로 다시 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 자동 승인 통계 확인
    await page.click('[data-testid="auto-approval-menu"]');
    await page.click('[data-testid="approval-stats-tab"]');
    
    // Then: 자동 승인 통계에 반영됨
    await expect(page.locator('[data-testid="auto-approval-count"]')).toContainText('1');
    
    // 자동 승인 로그 확인
    await page.click('[data-testid="approval-logs-tab"]');
    const autoApprovalLog = page.locator('[data-testid="approval-log-item"]')
      .filter({ hasText: '자동 승인' }).first();
    
    await expect(autoApprovalLog).toBeVisible();
    await expect(autoApprovalLog).toContainText('검증된 기업 자동 승인');
    
    // === 4단계: 자동 승인 조건에 맞지 않는 구독 신청 ===
    
    // 미검증 테넌트로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'unverified@company.com');
    await page.fill('[data-testid="password-input"]', 'test123');
    await page.click('[data-testid="login-button"]');
    
    // When: 자동 승인 조건에 맞지 않는 구독 신청
    await page.click('[data-testid="subscription-menu"]');
    await page.click('[data-testid="new-subscription-button"]');
    
    await page.click('[data-testid="plan-basic"]');
    await page.click('[data-testid="billing-monthly"]');
    
    // 은행 이체 선택 (규칙에 포함되지 않은 결제 방법)
    await page.click('[data-testid="payment-bank-transfer"]');
    await page.fill('[data-testid="bank-account"]', '123-456-789');
    
    await page.click('[data-testid="subscribe-button"]');
    
    // Then: 수동 승인 대기 상태
    await expect(page).toHaveURL('/subscription/pending');
    await expect(page.locator('[data-testid="manual-approval-notice"]')).toContainText('승인 검토 중입니다');
  });

  test('시나리오 6: 알림 시스템 전체 플로우', async ({ page }) => {
    // === 1단계: 구독 신청 시 슈퍼관리자 알림 ===
    
    // Given: 테넌트가 구독 신청
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.tenantAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.tenantAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    await page.click('[data-testid="subscription-menu"]');
    await page.click('[data-testid="new-subscription-button"]');
    
    await page.click('[data-testid="plan-basic"]');
    await page.click('[data-testid="billing-monthly"]');
    await page.fill('[data-testid="card-number"]', '4111111111111111');
    await page.fill('[data-testid="card-expiry"]', '12/25');
    await page.fill('[data-testid="card-cvc"]', '123');
    
    await page.click('[data-testid="subscribe-button"]');
    
    // === 2단계: 슈퍼관리자가 알림 확인 ===
    
    // 슈퍼관리자로 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 알림 아이콘 확인
    const notificationBadge = page.locator('[data-testid="notification-badge"]');
    await expect(notificationBadge).toBeVisible();
    await expect(notificationBadge).toContainText('1'); // 새 알림 1개
    
    // 알림 드롭다운 열기
    await page.click('[data-testid="notification-icon"]');
    
    // Then: 구독 신청 알림 확인
    const subscriptionNotification = page.locator('[data-testid="notification-item"]')
      .filter({ hasText: '새로운 구독 신청' });
    await expect(subscriptionNotification).toBeVisible();
    
    // === 3단계: 알림을 통한 승인 처리 ===
    
    // When: 알림 클릭하여 승인 페이지로 이동
    await subscriptionNotification.click();
    
    // Then: 해당 구독 승인 상세 페이지로 이동
    await expect(page).toHaveURL(/.*approvals.*subscription.*/);
    
    // 승인 처리
    await page.click('[data-testid="approve-button"]');
    await page.fill('[data-testid="approval-comment"]', '승인 완료');
    await page.click('[data-testid="confirm-approve-button"]');
    
    // === 4단계: 테넌트가 승인 결과 알림 확인 ===
    
    // 테넌트로 다시 로그인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.tenantAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.tenantAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    // When: 알림 확인
    await page.click('[data-testid="notification-icon"]');
    
    // Then: 승인 완료 알림 확인
    const approvalNotification = page.locator('[data-testid="notification-item"]')
      .filter({ hasText: '구독이 승인되었습니다' });
    await expect(approvalNotification).toBeVisible();
    
    // === 5단계: 24시간 후 미처리 알림 리마인더 (시뮬레이션) ===
    
    // 새로운 구독 신청 생성 (미처리 상태로 유지)
    await page.goto('/subscribe');
    await page.click('[data-testid="plan-standard"]');
    await page.click('[data-testid="billing-monthly"]');
    await page.fill('[data-testid="card-number"]', '4111111111111111');
    await page.fill('[data-testid="card-expiry"]', '12/25');
    await page.fill('[data-testid="card-cvc"]', '123');
    await page.click('[data-testid="subscribe-button"]');
    
    // 시간 경과 시뮬레이션 (테스트 환경에서는 API 호출로 대체)
    await page.evaluate(() => {
      // 24시간 경과 시뮬레이션 API 호출
      fetch('/api/test/simulate-24h-reminder', { method: 'POST' });
    });
    
    // 슈퍼관리자로 로그인하여 리마인더 알림 확인
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.superAdmin.email);
    await page.fill('[data-testid="password-input"]', USERS.superAdmin.password);
    await page.click('[data-testid="login-button"]');
    
    await page.click('[data-testid="notification-icon"]');
    
    // Then: 리마인더 알림 확인
    const reminderNotification = page.locator('[data-testid="notification-item"]')
      .filter({ hasText: '승인 대기 중인 구독이 있습니다' });
    await expect(reminderNotification).toBeVisible();
  });
});