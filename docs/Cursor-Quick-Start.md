# Cursor 빠른 시작 가이드

**문서 버전:** 2.0  
**작성일:** 2025년 12월 13일  
**최종 수정일:** 2025년 12월 18일  
**작성자:** 경영기획실 이대영 이사  
**대상 독자:** 초급 개발자  
**기반 문서:** `docs/PRD.md` (v2.3), `docs/Prototype-Guide.md` (v3.1), `docs/System-Architecture.md` (v2.0)

---

## 목차

1. [Cursor란?](#1-cursor란)
2. [설치 및 설정](#2-설치-및-설정)
3. [기본 사용법](#3-기본-사용법)
4. [바이브코딩 워크플로우](#4-바이브코딩-워크플로우)
5. [효과적인 프롬프트 작성법](#5-효과적인-프롬프트-작성법)
6. [자주 사용하는 기능](#6-자주-사용하는-기능)

---

## 1. Cursor란?

**Cursor**는 AI 기반 코드 에디터로, 개발자가 AI와 함께 코딩할 수 있도록 도와주는 도구입니다.

**주요 기능**:
- **Chat**: AI와 대화하며 코드 질문 및 생성
- **Composer**: 여러 파일을 동시에 편집하고 코드 생성
- **Inline Suggestions**: 타이핑 중 자동 코드 제안
- **Code Actions**: 코드 리뷰, 리팩토링, 테스트 생성 등

---

## 2. 설치 및 설정

### 2.1. 설치

1. **다운로드**: https://cursor.sh/
2. **설치**: 다운로드한 파일 실행
3. **계정 생성**: 설치 후 계정 생성 또는 로그인
4. **구독 선택**: 
   - Free: 기본 기능 (제한적)
   - Pro (권장): 더 나은 AI 성능

### 2.2. 초기 설정

**설정 열기**: `Ctrl+,` (Windows/Linux) 또는 `Cmd+,` (Mac)

**권장 설정**:
```json
{
  "cursor.aiModel": "gpt-4",
  "cursor.enableCodeActions": true,
  "cursor.enableInlineSuggestions": true,
  "cursor.enableChat": true,
  "cursor.enableComposer": true
}
```

### 2.3. 확장 프로그램 설치

**필수 확장 프로그램**:
1. ESLint - 코드 검사
2. Prettier - 코드 포맷팅
3. GitLens - Git 기능 강화
4. Tailwind CSS IntelliSense - Tailwind CSS 자동완성
5. TypeScript Vue Plugin (Volar) - TypeScript 지원 (선택)

**설치 방법**:
1. `Ctrl+Shift+X` (확장 프로그램)
2. 검색 후 설치

---

## 3. 기본 사용법

### 3.1. 주요 단축키

| 기능 | 단축키 (Windows/Linux) | 단축키 (Mac) |
|------|----------------------|-------------|
| Chat 열기 | `Ctrl+L` | `Cmd+L` |
| Composer 열기 | `Ctrl+I` | `Cmd+I` |
| Code Actions | `Ctrl+K` | `Cmd+K` |
| 제안 수용 | `Tab` | `Tab` |
| 제안 거부 | `Esc` | `Esc` |

### 3.2. Chat 사용법

**Chat 열기**:
1. `Ctrl+L` (또는 `Cmd+L`) 누르기
2. 오른쪽에 Chat 패널 열림

**질문 예시**:
```
"React 18 + TypeScript를 사용하여 사용자 로그인 폼을 만들어주세요.
Zustand store와 연동하여 로그인 상태를 관리해야 합니다.
Shadcn/UI의 Button과 Input 컴포넌트를 사용하고, Tailwind CSS로 스타일링하세요."
```

**응답 확인**:
- Cursor가 코드를 생성하면 검토
- 마음에 들면 "Accept" 클릭
- 수정이 필요하면 "Modify" 요청

### 3.3. Inline Suggestions

**사용법**:
1. 코드를 타이핑하면 자동으로 제안
2. `Tab` 키로 수용
3. `Esc` 키로 거부
4. `Ctrl+→` (또는 `Cmd+→`)로 부분 수용

---

## 4. 바이브코딩 워크플로우

### 4.1. 세션 시작

**1. 프로젝트 열기**:
```
File > Open Folder > 프로젝트 폴더 선택
```

**2. Chat 초기화**:
```
Ctrl+L로 Chat 열기
프로젝트 컨텍스트 설정:
"안녕하세요. SmartCON Lite 프로젝트를 개발하고 있습니다.
프로젝트는 React 18 + TypeScript + Vite (프론트엔드)와 
Spring Boot 3.3.x + Java 17 + MariaDB (백엔드)를 사용합니다.
UI는 Shadcn/UI + Tailwind CSS를 사용하고, 상태 관리는 Zustand를 사용합니다.
현재 [작업할 기능]을 구현하려고 합니다."
```

**3. 작업 목표 설정**:
```
"오늘은 [구체적인 기능]을 구현하겠습니다.
- 목표: [명확한 목표]
- 제약사항: [기술 스택, 요구사항 등]
- 참고 문서: [PRD, API 명세 등]"
```

### 4.2. 코딩 진행

**1. 코드 작성 시작**:
- 필요한 파일 생성 또는 열기
- 기본 구조부터 시작

**2. Cursor 제안 활용**:
- 타이핑하면 자동 제안
- 막히는 부분은 Chat으로 질문

**3. 코드 리뷰 요청**:
- 코드 선택 후 `Ctrl+K`
- "Review this code" 선택

**4. 테스트 코드 생성**:
- 함수/클래스 선택
- `Ctrl+K` > "Generate unit tests"

### 4.3. 세션 종료

**1. 코드 정리**:
```
Chat에 요청:
"오늘 작성한 코드를 정리하고 리팩토링해주세요."
```

**2. 커밋**:
```bash
git add .
git commit -m "feat: [기능명] 구현 (Cursor AI 협업)"
git push
```

**3. 다음 작업 계획**:
```
Chat에 요청:
"오늘 작업한 내용을 요약해주세요.
다음에 할 작업도 제안해주세요."
```

---

## 5. 효과적인 프롬프트 작성법

### 5.1. 좋은 프롬프트 예시

**✅ 좋은 예 (백엔드)**:
```
"Spring Boot에서 JWT 토큰을 생성하는 서비스를 만들어주세요.
- 토큰에는 사용자 ID와 역할 정보를 포함해야 합니다.
- 토큰 만료 시간은 1시간입니다.
- 예외 처리를 포함해주세요.
- JWT 라이브러리는 java-jwt를 사용합니다."
```

**✅ 좋은 예 (프론트엔드)**:
```
"React 18 + TypeScript로 인트로 페이지를 만들어주세요.
- 인트로 페이지는 루트 경로(/)에 배치됩니다.
- 메인 메시지: '안전관리, 이제는 스마트하게!'
- 서브 메시지: '본사와 현장관리까지 한 번에 업무 끝! 스마트콘 [SmartCON]'
- 로그인 버튼 클릭 시 /login으로 이동
- Shadcn/UI의 Button 컴포넌트 사용
- Tailwind CSS로 반응형 디자인 적용 (Mobile First)
- 회사 주색상 #71AA44 사용"
```

**❌ 나쁜 예**:
```
"JWT 토큰 만들어줘"  // 너무 모호함
```

### 5.2. 프롬프트 작성 원칙

1. **명확한 목표**: 무엇을 만들고 싶은지 명확히
2. **기술 스택 명시**: 사용하는 프레임워크, 라이브러리
3. **제약사항**: 요구사항, 제약사항 명시
4. **컨텍스트 제공**: 관련 파일이나 코드 참조
5. **예시 제공**: 원하는 결과의 예시 (가능한 경우)

### 5.3. 컨텍스트 제공 방법

**파일 참조**:
```
"이 파일을 참고하여 비슷한 구조로 만들어주세요:
@prototype/src/stores/userStore.ts"
또는
"이 파일을 참고하여 비슷한 구조로 만들어주세요:
@backend/src/main/java/com/smartcon/service/AuthService.java"
```

**코드 선택 후 질문**:
```
1. 코드 선택
2. Ctrl+L로 Chat 열기
3. "이 코드를 개선해주세요" 또는 "이 코드의 문제점을 찾아주세요"
```

---

## 6. 자주 사용하는 기능

### 6.1. 코드 생성

**Chat 사용 (프론트엔드)**:
```
"React 18 + TypeScript로 사용자 목록 컴포넌트를 만들어주세요.
- Shadcn/UI의 Table 컴포넌트 사용
- 페이지네이션 포함 (Shadcn/UI Pagination)
- 검색 기능 포함 (Shadcn/UI Input)
- Zustand store에서 사용자 데이터 가져오기
- 반응형 디자인 적용 (Mobile First)"
```

**Chat 사용 (백엔드)**:
```
"Spring Boot 3.3.x로 사용자 목록 조회 API를 만들어주세요.
- JPA Repository 사용
- 페이지네이션 지원 (Pageable)
- 검색 필터링 (이름, 이메일)
- JWT 인증 필수
- Multi-tenant 지원 (Tenant ID 필터링)"
```

**Composer 사용** (여러 파일 동시 편집):
```
Ctrl+I로 Composer 열기
"사용자 인증 기능을 구현해주세요:
1. prototype/src/stores/authStore.ts - Zustand store (인증 상태 관리)
2. prototype/src/views/IntroView.tsx - 인트로 페이지
3. prototype/src/views/LoginMethodSelectView.tsx - 로그인 방식 선택
4. prototype/src/views/HqLoginView.tsx - 본사 관리자 로그인
5. prototype/src/views/SocialLoginView.tsx - 소셜 로그인
Shadcn/UI 컴포넌트와 Tailwind CSS를 사용하고, React Router v7로 라우팅하세요."
```

### 6.2. 코드 리뷰

**방법 1: Code Actions**:
```
1. 코드 선택
2. Ctrl+K
3. "Review this code" 선택
```

**방법 2: Chat**:
```
"이 코드를 리뷰해주세요:
[코드 붙여넣기 또는 @파일명]
개선 사항도 제안해주세요."
```

### 6.3. 에러 해결

**에러 메시지와 함께 질문**:
```
"이 에러가 발생했습니다:
[에러 메시지]

이 코드에서 발생했습니다:
[코드 또는 @파일명]

해결 방법을 알려주세요."
```

### 6.4. 테스트 코드 생성

**단위 테스트**:
```
1. 테스트할 함수/클래스 선택
2. Ctrl+K
3. "Generate unit tests" 선택
```

**통합 테스트**:
```
Chat에 요청:
"이 API 엔드포인트에 대한 통합 테스트를 작성해주세요:
@src/controllers/AuthController.java
- 성공 케이스
- 실패 케이스
- 예외 케이스 포함"
```

### 6.5. 리팩토링

**Code Actions 사용**:
```
1. 리팩토링할 코드 선택
2. Ctrl+K
3. "Refactor this code" 선택
```

**Chat 사용 (프론트엔드)**:
```
"이 코드를 리팩토링해주세요:
@prototype/src/stores/userStore.ts
- 함수 분리
- 중복 코드 제거
- 네이밍 개선
- TypeScript 타입 안정성 향상"
```

**Chat 사용 (백엔드)**:
```
"이 코드를 리팩토링해주세요:
@backend/src/main/java/com/smartcon/service/UserService.java
- 함수 분리
- 중복 코드 제거
- 네이밍 개선
- 예외 처리 개선"
```

---

## 7. 팁과 주의사항

### 7.1. 효과적인 사용 팁

1. **작은 단위로 요청**: 한 번에 너무 많은 것을 요청하지 않기
2. **검토 필수**: Cursor가 생성한 코드는 반드시 검토
3. **이해하며 진행**: 코드를 이해하고 학습하면서 진행
4. **정기적인 커밋**: 작은 단위로 자주 커밋
5. **에러는 즉시 질문**: 막히는 부분은 바로 Chat으로 질문

### 7.2. 주의사항

1. **보안 코드**: 보안 관련 코드는 특히 주의 깊게 검토
2. **성능**: 성능이 중요한 부분은 직접 최적화 검토
3. **비즈니스 로직**: 비즈니스 로직은 요구사항과 일치하는지 확인
4. **의존성**: 새로운 라이브러리 추가 시 프로젝트 정책 확인

### 7.3. 학습 전략

1. **코드 이해**: Cursor가 생성한 코드를 읽고 이해
2. **문서 참고**: Cursor가 제안한 라이브러리나 패턴의 공식 문서 확인
3. **실험**: 작은 프로젝트로 먼저 실험
4. **질문**: 모르는 부분은 Chat으로 질문

---

## 8. 문제 해결

### 8.1. Cursor가 제안을 하지 않을 때

**해결 방법**:
1. 설정 확인: `cursor.enableInlineSuggestions`가 true인지 확인
2. 파일 저장: 파일을 저장한 후 다시 시도
3. 재시작: Cursor 재시작

### 8.2. Chat이 응답하지 않을 때

**해결 방법**:
1. 인터넷 연결 확인
2. 구독 상태 확인 (Pro 구독 필요할 수 있음)
3. 질문을 더 구체적으로 작성
4. Cursor 재시작

### 8.3. 생성된 코드가 원하는 것과 다를 때

**해결 방법**:
1. 더 구체적인 프롬프트 작성
2. 예시 코드 제공
3. 단계별로 나누어 요청
4. "Modify" 버튼으로 수정 요청

---

## 9. SmartCON Lite 프로젝트 컨텍스트

### 9.1. 프로젝트 구조

```
SmartCON/
├── docs/                          # 문서 폴더
│   ├── PRD.md                     # 상세 요구사항 정의서 (v2.3)
│   ├── Prototype-Guide.md         # 프로토타입 개발 가이드 (v3.1)
│   ├── SmartCON_Lite_UI_Design_Guide.md  # UI 디자인 가이드 (v3.1)
│   ├── System-Architecture.md     # 시스템 아키텍처 (v2.0)
│   └── ...
│
├── prototype/                     # 프로토타입 (React + Vite)
│   ├── src/
│   │   ├── components/            # 공통 컴포넌트
│   │   ├── views/                 # 페이지 뷰
│   │   ├── stores/                # Zustand stores
│   │   └── router/                # React Router 설정
│   └── ...
│
└── backend/                       # 백엔드 (Spring Boot) - 향후 구현
```

### 9.2. 기술 스택 요약

**프론트엔드 (프로토타입)**:
- React 18 + TypeScript
- Vite (빌드 도구)
- Shadcn/UI + Tailwind CSS
- Zustand (상태 관리)
- React Router v7 (라우팅)
- Lucide React (아이콘)

**백엔드 (향후 구현)**:
- Spring Boot 3.3.x
- Java 17 (LTS)
- MariaDB 10.11
- Spring Security 6.x + JWT
- Spring Batch (배치 작업)

### 9.3. 주요 기능 및 인증 플로우

**인증 플로우**:
1. 인트로 페이지 (`/`) - 서비스 소개
2. 로그인 방식 선택 (`/login`) - 본사 관리자 vs 소셜 로그인
3. 본사 관리자 로그인 (`/login/hq`) - 사업자번호 + 비밀번호
4. 소셜 로그인 (`/login/social`) - Kakao, Naver OAuth2
5. 역할 선택 (`/role-select`) - 다중 역할 보유 시
6. 대시보드 - 역할별 대시보드로 이동

**사용자 역할**:
- **Super Admin**: SaaS 플랫폼 운영 관리
- **HQ Admin**: 본사 관리자 (현장 개설, 전사 통계)
- **Site Manager**: 현장 관리자 (작업 지시, 출역 마감)
- **Team Leader**: 노무 팀장 (작업 수락, 일보 제출)
- **Worker**: 노무자 (출역 조회, 전자계약)

### 9.4. 참고 문서

프로젝트 개발 시 다음 문서를 참고하세요:

- **PRD.md** (v2.3): 상세 요구사항 및 기능 명세
- **Prototype-Guide.md** (v3.1): 프로토타입 개발 가이드 및 컴포넌트 예시
- **SmartCON_Lite_UI_Design_Guide.md** (v3.1): UI 디자인 시스템 및 가이드라인
- **System-Architecture.md** (v2.0): 시스템 아키텍처 및 기술 스택
- **Functional-Specification.md** (v2.1): 기능 명세서
- **Development-Process.md** (v2.1): 개발 절차 및 워크플로우

### 9.5. 디자인 시스템

**주요 색상**:
- Primary: `#71AA44` (회사 주색상, CMYK 55 10 95 00)
- Secondary: `#333333` (Dark Gray)
- Destructive: `#E63946` (Red)
- Success: `#71AA44` (Primary와 동일)
- Warning: `#FF8C42` (Orange)

**디자인 원칙**:
- Mobile First 접근
- 반응형 디자인 (Mobile: ~767px, Tablet: 768px~1023px, Desktop: 1024px~)
- WCAG 2.1 AA 수준 접근성 준수
- 직관적이고 효율적인 UI/UX

---

## 10. 추가 자료

- **Cursor 공식 문서**: https://cursor.sh/docs
- **프롬프트 가이드**: Cursor 내장 튜토리얼
- **커뮤니티**: Cursor Discord 서버
- **React 공식 문서**: https://react.dev
- **Shadcn/UI 문서**: https://ui.shadcn.com
- **Tailwind CSS 문서**: https://tailwindcss.com
- **Spring Boot 공식 문서**: https://spring.io/projects/spring-boot

---

**문서 끝**

