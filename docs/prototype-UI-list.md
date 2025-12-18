# SmartCON Lite 프로토타입 UI 화면 목록

본 문서는 SmartCON Lite 프로토타입에서 구현된 주요 화면들의 목록, 캡쳐 이미지 및 주요 기능을 정의합니다.
캡쳐된 이미지 파일들은 `docs/prototype-UI-sample/` 폴더에 위치합니다.

## 1. 인증 및 공통 화면 (Auth & Common)

| 메뉴/화면명 | 경로(Route) | 캡쳐 이미지 | 주요 기능 및 이벤트 |
| :--- | :--- | :--- | :--- |
| 인트로 페이지 | `/` | `intro.png` | - 서비스 소개 및 주요 기능 브리핑<br>- **[시작하기/로그인]**: 로그인 방식 선택 모달 호출<br>- **[데모 신청]**: 서비스 문의 폼 제출 이벤트 |
| 로그인 방식 선택 | `/login` (Modal) | `login_select.png` | - **[본사 관리자 로그인]**: 본사 전용 로그인 폼으로 전환<br>- **[소셜 로그인]**: 카카오/네이버 선택 화면으로 전환 |
| 본사 관리자 로그인 | `/login/hq` (Modal) | `login_hq.png` | - 사업자번호 및 비밀번호 입력<br>- **[로그인]**: HQ 대시보드로 이동 (Mock 인증) |
| 소셜 로그인 | `/login/social` (Modal) | `login_social.png` | - **[카카오/네이버로 시작하기]**: 소셜 인증 후 역할 선택 화면으로 이동 |
| 역할 선택 | `/role-select` (Modal) | `role_select.png` | - 보유한 권한(본사/현장/노무자) 중 하나를 선택하여 진입<br>- **[역할 버튼]**: 해당 역할의 대시보드로 리다이렉트 |

## 2. 본사 관리자 모드 (HQ Admin)

| 메뉴/화면명 | 경로(Route) | 캡쳐 이미지 | 주요 기능 및 이벤트 |
| :--- | :--- | :--- | :--- |
| 본사 대시보드 | `/hq/dashboard` | `hq_dashboard.png` | - 전사 현장 요약 정보(운영 현장, 총 노무자 등) 위젯<br>- **[위젯 클릭]**: 상세 관리 페이지로 이동 |
| 현장 관리 | `/hq/sites` | `hq_sites.png` | - **[현장 개설]**: 신규 현장 등록 폼 호출<br>- **[수정]**: 기존 현장 정보 수정<br>- **[설정]**: 현장별 별도 정책 설정 |
| 노무자 관리 | `/hq/workers` | `hq_workers.png` | - 전사 통합 노무자 DB 검색<br>- **[상세]**: 노무자 개인 프로필 및 투입 이력 조회 |
| 계약 관리 | `/hq/contracts` | `hq_contracts.png` | - 전자계약 체결 요약 현황<br>- **[템플릿 관리]**: 표준근로계약서 양식 편집 |
| 정산 관리 | `/hq/settlements` | `hq_settlements.png` | - 현장별 노무비 집계 정보 확인<br>- **[세금계산서 발행]**: 정산 확정 및 API 연동 발행 |
| 설정 | `/hq/settings` | `hq_settings.png` | - 회사 정보 관리, 표준 공종/단가 마스터 관리 |

## 3. 현장 관리자 모드 (Site Manager)

| 메뉴/화면명 | 경로(Route) | 캡쳐 이미지 | 주요 기능 및 이벤트 |
| :--- | :--- | :--- | :--- |
| 현장 대시보드 | `/site/dashboard` | `site_dashboard.png` | - 금일 출역 인원 및 주요 공종 실시간 현황<br>- **[출역 현황 클릭]**: 출역 관리 페이지로 이동 |
| 출역 관리 | `/site/attendance` | `site_attendance.png` | - 안면인식 기반 출역 로그 확인<br>- **[출역 확정]**: 일일 공수 데이터 마감 및 승인 |
| 작업일보 | `/site/reports` | `site_reports.png` | - **[일보 작성/수정]**: 날씨 자동 연동 및 투입 내역 편집<br>- **[PDF 생성]**: 공사일보 문서화 |
| 현장 계약 관리 | `/site/contracts` | `site_contracts.png` | - 현장 투입 인원별 계약 체결 상태 모니터링<br>- **[서명 요청]**: 미체결자 대상 알림 발송 |
| 팀/노무자 관리 | `/site/teams` | `site_teams.png` | - **[팀 초대]**: 신규 팀장 초대 링크(SMS) 발송<br>- **[팀원 승인]**: 팀원의 현장 투입 권한 승인 |
| 급여/정산 | `/site/salary` | `site_salary.png` | - 개인별/팀별 월간 노무비 집계 내역 조회 |

## 4. 노무자 모드 (Worker)

| 메뉴/화면명 | 경로(Route) | 캡쳐 이미지 | 주요 기능 및 이벤트 |
| :--- | :--- | :--- | :--- |
| 노무자 대시보드 | `/worker/dashboard` | `worker_dashboard.png` | - 금일 출근 인증 상태 및 이달의 누적 공수 정보<br>- **[출퇴근 버튼]**: GPS 기반 보조 출퇴근 체크 |
| 출역 조회 | `/worker/attendance` | `worker_attendance.png` | - 캘린더 기반 개인 출역 이력 확인<br>- **[상세]**: 특정 일자 작업 내용 및 예상 노임 조회 |
| 전자 계약 | `/worker/contracts` | `worker_contracts.png` | - 미서명 계약서 목록 확인<br>- **[서명하기]**: 전자 서명 패드 호출 및 서명(Canvas) |
| 내 정보 | `/worker/profile` | `worker_profile.png` | - **[사진 등록]**: 안면인식용 가이드라인 기반 셀카 등록<br>- **[계좌 변경]**: 노무비 수령 계좌 관리 |
