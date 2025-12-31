# SmartCON Lite 개발 사전 준비사항 (v2.0)

**문서 버전:** 2.0  
**작성일:** 2025년 12월 17일  
**작성자:** 경영기획실 이대영 이사  
**기반 문서:** `docs/PRD.txt`

---

## 1. 개요

SmartCON Lite 프로젝트(React + Spring Boot + MariaDB) 개발을 위한 환경 설정 가이드입니다.

---

## 2. 개발 환경 준비

### 2.1. 필수 소프트웨어

**1. Node.js 및 npm**
- **버전**: Node.js 18.x LTS 이상 (20.x 권장)
- **용도**: 프론트엔드 빌드 및 패키지 관리

**2. Java Development Kit (JDK)**
- **버전**: JDK 17 (LTS)
- **배포판**: OpenJDK, Corretto, Temurin 등 무관
- **용도**: 백엔드 실행

**3. Build Tool (Backend)**
- **Gradle**: 8.x 이상
- **설치**: 보통 프로젝트 내 `gradlew` 래퍼를 사용하므로 별도 설치 불필요 (단, 로컬에 설치해두면 편리)

**4. Database**
- **MariaDB**: 10.11 (LTS)
- **설치 방법**: Docker 권장

**5. IDE**
- **Frontend**: VS Code (ESLint, Prettier, Tailwind CSS IntelliSense 확장 필수)
- **Backend**: IntelliJ IDEA (Community or Ultimate)

---

## 3. 데이터베이스 설정 (Docker)

로컬 개발 환경에서는 Docker Compose를 사용하여 DB를 구동하는 것을 권장합니다.
프로젝트 루트의 `docker-compose.yml` (생성 예정)을 사용합니다.

```yaml
# docker-compose.yml 예시
version: '3.8'
services:
  mariadb:
    image: mariadb:10.11
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: smartcon
      MYSQL_USER: smartcon
      MYSQL_PASSWORD: smartcon_password
    ports:
      - "3306:3306"
    volumes:
      - ./data/mariadb:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

### DB 접속 정보
- **Host**: localhost
- **Port**: 3306
- **Database**: smartcon
- **User**: smartcon / smartcon_password
- **Root**: root / root

---

## 4. 프론트엔드 초기 설정

```bash
cd frontend/web
npm install
# 개발 서버 실행
npm run dev
```

### 주요 라이브러리 (참고)
- `react`, `react-dom`: UI 라이브러리
- `vite`: 빌드 도구
- `tailwindcss`, `postcss`, `autoprefixer`: 스타일링
- `lucide-react`: 아이콘
- `@tanstack/react-query`: 데이터 페칭
- `zustand`: 상태 관리
- `class-variance-authority`, `clsx`, `tailwind-merge`: Shadcn/UI 유틸리티

---

## 5. 백엔드 초기 설정

```bash
cd backend
# Gradle 의존성 설치 및 빌드
./gradlew clean build
# 실행
./gradlew bootRun
```

### 주요 설정 (`application.yml`)
- MariaDB 연결 설정
- JWT 시크릿 키 설정
- OAuth2 클라이언트 설정 (Kakao, Naver 등)

---

## 6. 외부 서비스 키 발급 (준비)
- **Kakao Developers**: REST API 키, Redirect URI 설정
- **Naver Developers**: Client ID/Secret
- **Face API**: (신한 또는 제휴사) API Key

---

**문서 꿑**
