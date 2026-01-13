# 프로덕션 빌드 최적화 가이드

## 개요

SmartCON Lite 프론트엔드의 프로덕션 빌드 최적화 설정에 대한 문서입니다.

## 최적화 기능

### 1. 코드 스플리팅 (Code Splitting)

#### 자동 청크 분리
- **react-vendor**: React, React-DOM 라이브러리
- **radix-ui**: Radix UI 컴포넌트들
- **state-management**: Zustand, TanStack Query
- **utilities**: clsx, tailwind-merge, class-variance-authority, date-fns
- **icons**: lucide-react
- **routing**: wouter
- **notifications**: sonner
- **vendor**: 기타 node_modules 라이브러리

#### 페이지별 청크 분리
- **page-auth**: 인증 관련 페이지
- **page-super**: 슈퍼관리자 페이지
- **page-hq**: 본사관리자 페이지
- **page-site**: 현장관리자 페이지
- **page-worker**: 근로자 페이지

#### 컴포넌트별 청크 분리
- **ui-components**: UI 컴포넌트들
- **components**: 일반 컴포넌트들
- **stores**: Zustand 스토어들
- **hooks**: 커스텀 훅들
- **lib-utils**: 라이브러리 및 유틸리티

### 2. 번들 크기 최적화

#### ESBuild 최적화
- **console.log 제거**: 프로덕션에서 자동 제거
- **debugger 제거**: 프로덕션에서 자동 제거
- **라이센스 주석 제거**: 번들 크기 감소
- **식별자 최소화**: 변수명 단축
- **구문 최소화**: 불필요한 공백 제거
- **화이트스페이스 최소화**: 공백 문자 제거

#### 파일명 최적화
- **해시 길이**: 8자리로 단축 (기본 16자리)
- **디렉토리 구조**: js/, css/, images/, fonts/, assets/로 분류
- **압축 형식**: ES 모듈 형식 사용

### 3. 에셋 최적화

#### 인라인 임계값
- **4KB 미만**: base64로 인라인 처리
- **4KB 이상**: 별도 파일로 분리

#### 이미지 최적화
- **지원 형식**: PNG, JPEG, SVG, GIF, TIFF, BMP, ICO, WebP
- **파일명**: `images/[name]-[hash:8][extname]` 형식

#### 폰트 최적화
- **지원 형식**: WOFF, WOFF2, EOT, TTF, OTF
- **파일명**: `fonts/[name]-[hash:8][extname]` 형식

### 4. CSS 최적화

#### CSS 코드 스플리팅
- **활성화**: 페이지별 CSS 분리
- **최소화**: ESBuild를 사용한 CSS 압축
- **해시**: CSS 파일명에 해시 추가

### 5. Tree-shaking 최적화

#### 설정
- **모듈 사이드 이펙트**: 비활성화
- **프로퍼티 읽기 사이드 이펙트**: 비활성화
- **알 수 없는 글로벌 사이드 이펙트**: 비활성화

### 6. 의존성 사전 최적화

#### 포함된 의존성
```javascript
[
  "react",
  "react-dom", 
  "react/jsx-runtime",
  "zustand",
  "@tanstack/react-query",
  "wouter",
  "clsx",
  "tailwind-merge",
  "date-fns",
  "lucide-react",
  "sonner"
]
```

## 빌드 스크립트

### 기본 빌드
```bash
npm run build                    # 기본 빌드
npm run build:production         # 프로덕션 빌드
npm run build:staging           # 스테이징 빌드
npm run build:analyze           # 번들 분석 포함 빌드
```

### 분석 및 최적화
```bash
npm run build:analyze           # 번들 분석기 실행
npm run build:size-check        # 번들 크기 체크
npm run build:clean             # 클린 빌드
npm run build:with-typecheck    # TypeScript 체크 포함 빌드
```

### 미리보기
```bash
npm run preview                 # 빌드 결과 미리보기
npm run preview:production      # 프로덕션 빌드 미리보기
```

## 성능 지표

### 번들 크기 (gzip 압축 후)
- **메인 번들**: ~2KB (index.html)
- **CSS**: ~10KB (전체 스타일)
- **React 벤더**: ~76KB (React 라이브러리)
- **기타 벤더**: ~22KB (서드파티 라이브러리)
- **페이지별 청크**: 2-9KB (페이지당)

### 청크 크기 경고
- **임계값**: 800KB
- **권장사항**: 800KB를 초과하는 청크는 추가 분리 고려

## 환경별 설정

### 프로덕션 환경 변수
```env
NODE_ENV=production
VITE_APP_VERSION=1.0.0
VITE_API_BASE_URL=https://api.smartcon.kr/api/v1
VITE_ENABLE_DEVTOOLS=false
VITE_ENABLE_CONSOLE_LOGS=false
```

### 최적화 플래그
- **minify**: esbuild 사용
- **sourcemap**: 비활성화
- **reportCompressedSize**: 활성화
- **cssCodeSplit**: 활성화

## 모니터링

### 번들 분석기
- **도구**: rollup-plugin-visualizer
- **출력**: `dist/stats.html`
- **형식**: treemap, sunburst, network 지원

### 성능 모니터링
- **빌드 시간**: ~27초 (평균)
- **변환 모듈**: ~1927개
- **압축률**: ~70% (gzip)

## 권장사항

### 개발 시 고려사항
1. **동적 임포트**: 큰 라이브러리는 동적 임포트 사용
2. **코드 스플리팅**: 페이지별 분리 유지
3. **번들 크기**: 정기적인 번들 분석 실행
4. **의존성 관리**: 불필요한 의존성 제거

### 배포 전 체크리스트
1. `npm run build:analyze` 실행
2. 번들 크기 확인 (800KB 이하)
3. `npm run preview:production` 테스트
4. 성능 지표 확인

## 문제 해결

### 순환 청크 경고
```
Circular chunk: react-vendor -> vendor -> react-vendor
```
- **원인**: 청크 간 순환 의존성
- **해결**: manualChunks 로직 조정 필요

### 큰 번들 크기
- **분석**: `npm run build:analyze` 실행
- **최적화**: 불필요한 의존성 제거
- **분리**: 큰 컴포넌트 동적 임포트로 변경

### 빌드 실패
- **TypeScript**: `npm run typecheck` 실행
- **의존성**: `npm install` 재실행
- **캐시**: `npm run clean` 후 재빌드