# SmartCON Lite 개발 사전 준비사항

**문서 버전:** 1.0  
**작성일:** 2025년 12월 13일  
**작성자:** 개발 PM (Gemini)  
**대상 독자:** 개발팀, 인프라 담당자, 프로젝트 매니저

---

## 목차

1. [개요](#1-개요)
2. [개발 환경 준비](#2-개발-환경-준비)
3. [테스트 환경 준비](#3-테스트-환경-준비)
4. [운영 환경 준비](#4-운영-환경-준비)
5. [사설 인증 방법](#5-사설-인증-방법)
6. [외부 서비스 연동 준비](#6-외부-서비스-연동-준비)
7. [프로젝트 초기 설정](#7-프로젝트-초기-설정)
8. [바이브코딩 환경 준비](#8-바이브코딩-환경-준비)
9. [체크리스트](#9-체크리스트)

---

## 1. 개요

### 1.1. 문서 목적

SmartCON Lite 솔루션 개발을 시작하기 전에 필요한 모든 환경 및 인프라 준비사항을 정리한 문서입니다.

### 1.2. 준비 단계

```
1. 개발 환경 구축 (개발자 PC)
   ↓
2. 테스트 환경 구축 (테스트 서버)
   ↓
3. 외부 서비스 연동 준비 (OAuth, PASS, Face API)
   ↓
4. 프로젝트 초기 설정 (Git, CI/CD)
   ↓
5. 운영 환경 준비 (운영 서버)
```

---

## 2. 개발 환경 준비

### 2.1. 하드웨어 요구사항

#### 개발자 PC 최소 사양

**Windows/Mac/Linux 공통**:
- **CPU**: Intel i5 이상 또는 동급 AMD 프로세서
- **RAM**: 16GB 이상 (권장: 32GB)
- **Storage**: SSD 256GB 이상 (권장: 512GB)
- **Network**: 인터넷 연결 필수

**모바일 개발 추가 요구사항**:
- **Android 개발**: Windows/Mac/Linux 모두 가능
- **iOS 개발**: **Mac 필수** (Xcode는 macOS 전용)

### 2.2. 소프트웨어 설치

#### 2.2.1. 필수 소프트웨어

**1. Node.js 및 npm**
```bash
# 설치 버전: Node.js 18.x LTS 이상
# 다운로드: https://nodejs.org/

# 설치 확인
node --version  # v18.x.x 이상
npm --version   # 9.x.x 이상
```

**2. Java Development Kit (JDK)**
```bash
# 설치 버전: Java 17 이상 (OpenJDK 또는 Oracle JDK)
# 다운로드: 
# - OpenJDK: https://adoptium.net/
# - Oracle JDK: https://www.oracle.com/java/technologies/downloads/

# 설치 확인
java -version  # openjdk version "17.x.x" 이상
javac -version
```

**3. Maven**
```bash
# 설치 버전: Maven 3.8 이상
# 다운로드: https://maven.apache.org/download.cgi

# 설치 확인
mvn -version  # Apache Maven 3.8.x 이상
```

**4. Git**
```bash
# 설치 버전: Git 2.30 이상
# 다운로드: https://git-scm.com/downloads

# 설치 확인
git --version  # git version 2.30.x 이상

# Git 설정
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

**5. MSSQL (SQL Server)**
```bash
# 개발용: SQL Server 2019 Express Edition (무료)
# 다운로드: https://www.microsoft.com/ko-kr/sql-server/sql-server-downloads

# 또는 Docker로 실행
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourPassword123!" \
  -p 1433:1433 --name sqlserver \
  -d mcr.microsoft.com/mssql/server:2019-latest

# 연결 확인
sqlcmd -S localhost -U sa -P YourPassword123!
```

**6. Docker (선택사항, 권장)**
```bash
# 설치 버전: Docker Desktop 최신 버전
# 다운로드: https://www.docker.com/products/docker-desktop

# 설치 확인
docker --version
docker-compose --version
```

#### 2.2.2. IDE 및 개발 도구

**1. Visual Studio Code (프론트엔드 개발)**
```bash
# 다운로드: https://code.visualstudio.com/

# 필수 확장 프로그램
- Volar (Vue 3 지원)
- ESLint
- Prettier
- GitLens
- Vuetify Snippets
```

**2. IntelliJ IDEA (백엔드 개발)**
```bash
# 다운로드: https://www.jetbrains.com/idea/
# 버전: IntelliJ IDEA Ultimate (유료) 또는 Community Edition (무료)

# 필수 플러그인
- Spring Boot
- Lombok
- Database Navigator
- Git Integration
```

**3. Android Studio (모바일 개발)**
```bash
# 다운로드: https://developer.android.com/studio
# 버전: 최신 버전

# 필수 구성 요소
- Android SDK (API Level 33 이상)
- Android SDK Build-Tools
- Android Emulator
```

**4. Xcode (iOS 개발, Mac만)**
```bash
# 설치: Mac App Store에서 설치
# 버전: Xcode 14 이상

# 필수 구성 요소
- Command Line Tools
- iOS Simulator
```

#### 2.2.3. 브라우저 및 개발 도구

**필수 브라우저**:
- Chrome (최신 버전)
- Firefox (최신 버전)
- Safari (Mac)
- Edge (최신 버전)

**개발자 도구**:
- Chrome DevTools
- Vue DevTools (브라우저 확장)
- React DevTools (필요시)

### 2.3. 개발 환경 설정

#### 2.3.1. 환경 변수 설정

**Windows (PowerShell)**:
```powershell
# 시스템 환경 변수 설정
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Apache\maven", "Machine")
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Users\YourName\AppData\Local\Android\Sdk", "User")

# PATH에 추가
$env:Path += ";$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:ANDROID_HOME\platform-tools"
```

**Mac/Linux (Bash/Zsh)**:
```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
export MAVEN_HOME=/usr/local/apache-maven
export ANDROID_HOME=$HOME/Library/Android/sdk

export PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin:$ANDROID_HOME/platform-tools
```

#### 2.3.2. 프로젝트별 설정

**프론트엔드 (Vue.js)**:
```bash
cd frontend/web
npm install
npm run dev
```

**백엔드 (Spring Boot)**:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**프로토타입**:
```bash
cd prototype
npm install
npm run dev
```

### 2.4. 로컬 데이터베이스 설정

#### 2.4.1. MSSQL 로컬 설치

**방법 1: 직접 설치**
1. SQL Server 2019 Express Edition 다운로드 및 설치
2. SQL Server Management Studio (SSMS) 설치
3. 로컬 인스턴스 연결 설정

**방법 2: Docker 사용 (권장)**
```bash
# Docker Compose 파일 생성: docker-compose.yml
version: '3.8'
services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2019-latest
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=SmartCON123!
      - MSSQL_PID=Express
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql
    restart: unless-stopped

volumes:
  sqlserver_data:

# 실행
docker-compose up -d
```

#### 2.4.2. 데이터베이스 초기화

```sql
-- 1. 데이터베이스 생성
CREATE DATABASE SmartCON_Dev;
GO

USE SmartCON_Dev;
GO

-- 2. 사용자 생성 (선택사항)
CREATE LOGIN smartcon_user WITH PASSWORD = 'SmartCON123!';
CREATE USER smartcon_user FOR LOGIN smartcon_user;
ALTER ROLE db_owner ADD MEMBER smartcon_user;
GO

-- 3. 스키마 생성 (docs/PRD.md의 스키마 참고)
-- Users, User_Roles, Sites 등 테이블 생성
```

---

## 3. 테스트 환경 준비

### 3.1. 테스트 서버 구성

#### 3.1.1. 서버 사양

**최소 사양**:
- **CPU**: 4 Core
- **RAM**: 8GB
- **Storage**: 100GB SSD
- **OS**: Ubuntu 22.04 LTS 또는 Windows Server 2019

**권장 사양**:
- **CPU**: 8 Core
- **RAM**: 16GB
- **Storage**: 200GB SSD
- **OS**: Ubuntu 22.04 LTS

#### 3.1.2. 서버 소프트웨어 설치

**1. 웹 서버 (Nginx)**
```bash
# Ubuntu
sudo apt update
sudo apt install nginx

# 설정 파일: /etc/nginx/sites-available/smartcon-test
# SSL 인증서 설정 (Let's Encrypt 또는 사설 인증서)
```

**2. 애플리케이션 서버 (Tomcat 또는 직접 실행)**
```bash
# Spring Boot는 내장 Tomcat 사용 가능
# 또는 별도 Tomcat 설치
sudo apt install tomcat9
```

**3. 데이터베이스 서버**
```bash
# MSSQL 설치 (프로덕션 버전)
# 또는 Docker 사용
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=TestPassword123!" \
  -p 1433:1433 --name sqlserver-test \
  -d mcr.microsoft.com/mssql/server:2019-latest
```

### 3.2. 테스트 데이터베이스 설정

```sql
-- 테스트 데이터베이스 생성
CREATE DATABASE SmartCON_Test;
GO

USE SmartCON_Test;
GO

-- 테스트 데이터 초기화 스크립트 실행
-- seed/ 디렉토리의 스크립트 참고
```

### 3.3. 테스트 도구 설정

**1. API 테스트 도구**
- Postman 또는 Insomnia
- Swagger UI (Spring Boot 자동 생성)

**2. 성능 테스트 도구**
- Apache JMeter
- k6

**3. E2E 테스트 도구**
- Cypress
- Playwright

---

## 4. 운영 환경 준비

### 4.1. 운영 서버 구성

#### 4.1.1. 서버 아키텍처

```
┌─────────────────┐
│   Load Balancer │
│   (Nginx/HAProxy)│
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌───▼───┐
│ App 1 │ │ App 2 │  (Spring Boot)
└───┬───┘ └───┬───┘
    │         │
    └────┬────┘
         │
    ┌────▼────┐
    │   MSSQL │  (Always On 또는 Replication)
    │  Primary│
    └─────────┘
```

#### 4.1.2. 서버 사양

**애플리케이션 서버**:
- **CPU**: 8 Core 이상
- **RAM**: 16GB 이상
- **Storage**: 200GB SSD
- **OS**: Ubuntu 22.04 LTS

**데이터베이스 서버**:
- **CPU**: 16 Core 이상
- **RAM**: 32GB 이상
- **Storage**: 500GB SSD (RAID 구성 권장)
- **OS**: Windows Server 2019 또는 Linux

**로드 밸런서**:
- **CPU**: 4 Core
- **RAM**: 8GB
- **Storage**: 50GB

### 4.2. 보안 설정

#### 4.2.1. 방화벽 설정

**필수 포트**:
- **80**: HTTP (리다이렉트용)
- **443**: HTTPS
- **1433**: MSSQL (내부 네트워크만)
- **22**: SSH (관리용)

**방화벽 규칙 예시 (Ubuntu)**:
```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

#### 4.2.2. SSL/TLS 인증서

**운영 환경**:
- 공인 인증기관(CA)에서 발급받은 SSL 인증서 사용
- Let's Encrypt (무료) 또는 상용 인증서

**인증서 설치**:
```bash
# Let's Encrypt 사용 시
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

#### 4.2.3. 데이터베이스 보안

**1. 암호화 설정**
- TDE (Transparent Data Encryption) 활성화
- 민감 정보 컬럼 암호화 (face_vector, pass_ci 등)

**2. 접근 제어**
- 최소 권한 원칙 적용
- IP 화이트리스트 설정
- 강력한 비밀번호 정책

### 4.3. 모니터링 및 로깅

#### 4.3.1. 모니터링 도구

**1. 애플리케이션 모니터링**
- Spring Boot Actuator
- Prometheus + Grafana
- New Relic 또는 Datadog (상용)

**2. 서버 모니터링**
- Zabbix
- Nagios
- CloudWatch (AWS 사용 시)

**3. 로그 수집**
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Fluentd
- CloudWatch Logs (AWS 사용 시)

#### 4.3.2. 알림 설정

**알림 채널**:
- 이메일
- Slack
- SMS (긴급 상황)

**알림 조건**:
- 서버 다운
- 디스크 사용률 80% 이상
- 메모리 사용률 90% 이상
- API 에러율 증가
- 데이터베이스 연결 실패

### 4.4. 백업 및 복구

#### 4.4.1. 데이터베이스 백업

**백업 전략**:
- **전체 백업**: 매일 새벽 2시
- **차등 백업**: 6시간마다
- **트랜잭션 로그 백업**: 1시간마다

**백업 스크립트 예시**:
```sql
-- 전체 백업
BACKUP DATABASE SmartCON_Prod
TO DISK = 'C:\Backup\SmartCON_Full.bak'
WITH COMPRESSION, INIT;

-- 차등 백업
BACKUP DATABASE SmartCON_Prod
TO DISK = 'C:\Backup\SmartCON_Diff.bak'
WITH DIFFERENTIAL, COMPRESSION;
```

#### 4.4.2. 파일 백업

**백업 대상**:
- 업로드된 이미지 파일
- 설정 파일
- 로그 파일

**백업 방법**:
- AWS S3 또는 Azure Blob Storage
- 로컬 스토리지 + 원격 복제

---

## 5. 사설 인증 방법

### 5.1. 개발 환경용 사설 인증서

#### 5.1.1. OpenSSL을 사용한 사설 CA 생성

**1. CA 키 및 인증서 생성**
```bash
# CA 개인 키 생성
openssl genrsa -out ca-key.pem 4096

# CA 인증서 생성
openssl req -new -x509 -days 365 -key ca-key.pem -out ca-cert.pem \
  -subj "/C=KR/ST=Seoul/L=Seoul/O=SmartCON/CN=SmartCON CA"
```

**2. 서버 인증서 생성**
```bash
# 서버 개인 키 생성
openssl genrsa -out server-key.pem 4096

# 서버 인증서 요청서(CSR) 생성
openssl req -new -key server-key.pem -out server.csr \
  -subj "/C=KR/ST=Seoul/L=Seoul/O=SmartCON/CN=localhost"

# CA로 서버 인증서 서명
openssl x509 -req -days 365 -in server.csr -CA ca-cert.pem \
  -CAkey ca-key.pem -CAcreateserial -out server-cert.pem
```

**3. 인증서 설치**
```bash
# Windows: 인증서 저장소에 CA 인증서 추가
certutil -addstore -f "ROOT" ca-cert.pem

# Mac: Keychain에 CA 인증서 추가
sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain ca-cert.pem

# Linux: 시스템 인증서 저장소에 추가
sudo cp ca-cert.pem /usr/local/share/ca-certificates/smartcon-ca.crt
sudo update-ca-certificates
```

#### 5.1.2. mkcert 사용 (권장, 더 간편)

```bash
# mkcert 설치
# Windows: choco install mkcert
# Mac: brew install mkcert
# Linux: https://github.com/FiloSottile/mkcert 참고

# 로컬 CA 설치
mkcert -install

# 로컬 도메인 인증서 생성
mkcert localhost 127.0.0.1 ::1

# 생성된 파일
# localhost+2.pem (인증서)
# localhost+2-key.pem (개인 키)
```

### 5.2. Nginx SSL 설정

```nginx
server {
    listen 443 ssl http2;
    server_name localhost;

    ssl_certificate /path/to/server-cert.pem;
    ssl_certificate_key /path/to/server-key.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name localhost;
    return 301 https://$server_name$request_uri;
}
```

### 5.3. Spring Boot SSL 설정

**application.yml**:
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: smartcon
```

**키스토어 생성**:
```bash
keytool -genkeypair -alias smartcon -keyalg RSA \
  -keysize 2048 -storetype PKCS12 -keystore keystore.p12 \
  -validity 365
```

---

## 6. 외부 서비스 연동 준비

### 6.1. 카카오 OAuth 연동

#### 6.1.1. 카카오 개발자 등록

**1. 카카오 개발자 계정 생성**
- URL: https://developers.kakao.com/
- 카카오 계정으로 로그인

**2. 애플리케이션 등록**
- 내 애플리케이션 > 애플리케이션 추가하기
- 앱 이름: SmartCON Lite
- 사업자명: 회사명 입력

**3. 플랫폼 설정**
- Web 플랫폼 추가
  - 사이트 도메인: `https://yourdomain.com`
  - Redirect URI: `https://yourdomain.com/api/auth/callback/kakao`
- Android 플랫폼 추가 (모바일앱용)
  - 패키지명: `com.smartcon.app`
  - 키 해시 등록
- iOS 플랫폼 추가 (모바일앱용)
  - 번들 ID: `com.smartcon.app`

**4. 카카오 로그인 활성화**
- 제품 설정 > 카카오 로그인 > 활성화 설정: ON
- Redirect URI 등록:
  - `https://yourdomain.com/api/auth/callback/kakao`
  - `smartcon://oauth/kakao` (모바일앱용)

**5. 동의 항목 설정**
- 필수 동의: 닉네임, 프로필 사진
- 선택 동의: 카카오계정(이메일), 전화번호

**6. REST API 키 확인**
- 앱 설정 > 앱 키
- REST API 키 복사 (환경 변수에 저장)

#### 6.1.2. 환경 변수 설정

```bash
# .env 파일 또는 환경 변수
KAKAO_CLIENT_ID=your_rest_api_key
KAKAO_CLIENT_SECRET=your_client_secret
KAKAO_REDIRECT_URI=https://yourdomain.com/api/auth/callback/kakao
```

### 6.2. 네이버 OAuth 연동

#### 6.2.1. 네이버 개발자 등록

**1. 네이버 개발자 센터 가입**
- URL: https://developers.naver.com/
- 네이버 계정으로 로그인

**2. 애플리케이션 등록**
- Applications > 애플리케이션 등록
- 애플리케이션 이름: SmartCON Lite
- 사용 API: 네이버 로그인

**3. 서비스 URL 및 Callback URL 설정**
- 서비스 URL: `https://yourdomain.com`
- Callback URL: `https://yourdomain.com/api/auth/callback/naver`

**4. Client ID 및 Client Secret 확인**
- 애플리케이션 정보에서 확인
- 환경 변수에 저장

#### 6.2.2. 환경 변수 설정

```bash
NAVER_CLIENT_ID=your_client_id
NAVER_CLIENT_SECRET=your_client_secret
NAVER_REDIRECT_URI=https://yourdomain.com/api/auth/callback/naver
```

### 6.3. PASS 인증 연동

#### 6.3.1. PASS 인증 서비스 신청

**1. PASS 인증 서비스 제공사 연락**
- 통신사별 PASS 인증 서비스 제공사:
  - SKT: SKT PASS
  - KT: PASS (통합)
  - LG U+: U+모바일인증

**2. 서비스 신청 절차**
- 사업자 등록증 제출
- 서비스 이용 계약서 작성
- 테스트 환경 제공 요청

**3. API 키 및 인증서 발급**
- API Key 발급
- 인증서 파일(.p12) 발급
- 테스트/운영 환경 구분

#### 6.3.2. 환경 변수 설정

```bash
PASS_API_URL=https://api.pass.com/v1/verify
PASS_API_KEY=your_api_key
PASS_CERT_PATH=/path/to/cert.p12
PASS_CERT_PASSWORD=your_cert_password
```

### 6.4. 신한홀딩스 Face API 연동

#### 6.4.1. Face API 서비스 신청

**1. 신한홀딩스 또는 제휴사 연락**
- Face API 서비스 제공사 확인
- 서비스 신청서 작성

**2. 테스트 환경 제공**
- 테스트 API 엔드포인트 확인
- 테스트 API Key 발급
- API 명세서 수령

**3. 연동 테스트**
- 샘플 코드로 연동 테스트
- 응답 시간 및 정확도 확인

#### 6.4.2. 환경 변수 설정

```bash
FACE_API_URL=https://api.face.smartcon.com/v1/compare
FACE_API_KEY=your_api_key
FACE_API_TIMEOUT=30000
FACE_API_RETRY_COUNT=3
FACE_MATCH_THRESHOLD=0.85
```

---

## 7. 프로젝트 초기 설정

### 7.1. Git 저장소 설정

#### 7.1.1. Git 저장소 생성

**1. 원격 저장소 생성**
- GitHub, GitLab, Bitbucket 등 선택
- Private 저장소 권장

**2. 로컬 저장소 초기화**
```bash
cd SmartCON
git init
git remote add origin https://github.com/yourcompany/smartcon.git
git branch -M main
```

#### 7.1.2. .gitignore 설정

**이미 생성됨**: `.gitignore` 파일 확인

**추가 확인 사항**:
- 환경 변수 파일 (`.env`, `.env.local`)
- 인증서 파일 (`*.pem`, `*.p12`, `*.key`)
- 빌드 산출물 (`dist/`, `target/`, `build/`)
- IDE 설정 파일 (`.idea/`, `.vscode/`)

### 7.2. CI/CD 파이프라인

#### 7.2.1. GitHub Actions 설정

**.github/workflows/ci.yml**:
```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: |
          cd backend
          mvn clean test

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: |
          cd frontend/web
          npm ci
      - name: Run tests
        run: |
          cd frontend/web
          npm test
```

#### 7.2.2. 배포 파이프라인

**.github/workflows/deploy.yml**:
```yaml
name: Deploy

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to production
        run: |
          # 배포 스크립트 실행
          ./scripts/deploy.sh
```

### 7.3. 코드 리뷰 프로세스

#### 7.3.1. 브랜치 전략

```
main (프로덕션)
  ↑
develop (개발)
  ↑
feature/* (기능 개발)
  ↑
hotfix/* (긴급 수정)
```

#### 7.3.2. Pull Request 템플릿

**.github/pull_request_template.md**:
```markdown
## 변경 사항
- 

## 테스트
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] 수동 테스트 완료

## 체크리스트
- [ ] 코드 리뷰 요청
- [ ] 문서 업데이트 (필요시)
- [ ] Breaking Change 여부 확인
```

### 7.4. 환경 변수 관리

#### 7.4.1. 환경 변수 파일 구조

```
.env.example          # 템플릿 파일 (Git에 포함)
.env.local           # 로컬 개발용 (Git 제외)
.env.development     # 개발 환경용
.env.test            # 테스트 환경용
.env.production      # 운영 환경용 (Git 제외)
```

#### 7.4.2. 환경 변수 예시

**.env.example**:
```bash
# Database
DB_HOST=localhost
DB_PORT=1433
DB_NAME=SmartCON_Dev
DB_USER=sa
DB_PASSWORD=YourPassword123!

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=3600

# OAuth
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret

# PASS
PASS_API_URL=https://api.pass.com/v1/verify
PASS_API_KEY=your_pass_api_key

# Face API
FACE_API_URL=https://api.face.smartcon.com/v1/compare
FACE_API_KEY=your_face_api_key

# File Upload
UPLOAD_DIR=/var/www/smartcon/uploads
MAX_FILE_SIZE=1048576
```

---

## 8. 체크리스트

### 9.1. 개발 환경 준비

#### 하드웨어
- [ ] 개발자 PC 사양 확인 (CPU, RAM, Storage)
- [ ] Mac 준비 (iOS 개발용, 필요시)

#### 소프트웨어 설치
- [ ] Node.js 18.x 이상 설치 및 확인
- [ ] Java 17 이상 설치 및 확인
- [ ] Maven 3.8 이상 설치 및 확인
- [ ] Git 설치 및 설정
- [ ] MSSQL 설치 또는 Docker 설정
- [ ] Docker 설치 (선택사항)

#### IDE 및 도구
- [ ] Visual Studio Code 설치 및 확장 프로그램 설치
- [ ] IntelliJ IDEA 설치 및 플러그인 설치
- [ ] Android Studio 설치 및 SDK 설정
- [ ] Xcode 설치 (Mac, iOS 개발용)

#### 개발 환경 설정
- [ ] 환경 변수 설정 (JAVA_HOME, MAVEN_HOME 등)
- [ ] 로컬 데이터베이스 설정
- [ ] 프로젝트 초기 설정 완료

### 9.2. 테스트 환경 준비

#### 서버 구성
- [ ] 테스트 서버 사양 확인
- [ ] 서버 OS 설치 (Ubuntu 22.04 LTS 권장)
- [ ] Nginx 설치 및 설정
- [ ] 애플리케이션 서버 설정
- [ ] 데이터베이스 서버 설정

#### 테스트 도구
- [ ] Postman 또는 Insomnia 설치
- [ ] 성능 테스트 도구 설치
- [ ] E2E 테스트 도구 설치

### 9.3. 운영 환경 준비

#### 서버 구성
- [ ] 운영 서버 사양 확인
- [ ] 로드 밸런서 설정
- [ ] 애플리케이션 서버 설정 (고가용성)
- [ ] 데이터베이스 서버 설정 (고가용성)

#### 보안 설정
- [ ] 방화벽 규칙 설정
- [ ] SSL 인증서 발급 및 설치
- [ ] 데이터베이스 암호화 설정
- [ ] 접근 제어 설정

#### 모니터링
- [ ] 모니터링 도구 설치 및 설정
- [ ] 로그 수집 도구 설정
- [ ] 알림 채널 설정

#### 백업
- [ ] 데이터베이스 백업 전략 수립
- [ ] 파일 백업 전략 수립
- [ ] 백업 스크립트 작성 및 테스트

### 9.4. 사설 인증 준비

#### 개발 환경
- [ ] 사설 CA 생성 또는 mkcert 설치
- [ ] 로컬 인증서 생성
- [ ] 브라우저에 CA 인증서 설치
- [ ] Nginx SSL 설정
- [ ] Spring Boot SSL 설정

### 9.5. 외부 서비스 연동 준비

#### 카카오 OAuth
- [ ] 카카오 개발자 계정 생성
- [ ] 애플리케이션 등록
- [ ] 플랫폼 설정 (Web, Android, iOS)
- [ ] Redirect URI 등록
- [ ] REST API 키 확인

#### 네이버 OAuth
- [ ] 네이버 개발자 센터 가입
- [ ] 애플리케이션 등록
- [ ] Callback URL 설정
- [ ] Client ID/Secret 확인

#### PASS 인증
- [ ] PASS 인증 서비스 제공사 연락
- [ ] 서비스 신청 및 계약
- [ ] API Key 및 인증서 발급
- [ ] 테스트 환경 확인

#### Face API
- [ ] 신한홀딩스 또는 제휴사 연락
- [ ] 서비스 신청
- [ ] 테스트 API Key 발급
- [ ] API 명세서 수령
- [ ] 연동 테스트

### 9.6. 프로젝트 초기 설정

### 9.7. Cursor 바이브코딩 환경 준비

#### 필수 도구
- [ ] Cursor 설치 및 계정 생성
- [ ] Cursor Pro 구독 (권장)
- [ ] Git 최신 버전 설치 및 설정
- [ ] 필수 확장 프로그램 설치 (Volar, ESLint, Prettier 등)

#### Cursor 설정
- [ ] Cursor 기본 설정 완료
- [ ] AI 모델 설정 (GPT-4 권장)
- [ ] Chat 기능 테스트
- [ ] Composer 기능 테스트
- [ ] Inline Suggestions 테스트

#### 워크플로우 확인
- [ ] Cursor에서 프로젝트 열기 테스트
- [ ] Chat으로 질문 및 코드 생성 테스트
- [ ] Composer로 여러 파일 동시 편집 테스트

#### Git 저장소
- [ ] 원격 저장소 생성
- [ ] 로컬 저장소 초기화
- [ ] .gitignore 확인

#### CI/CD
- [ ] CI 파이프라인 설정
- [ ] CD 파이프라인 설정
- [ ] 자동 테스트 설정

#### 환경 변수
- [ ] .env.example 파일 생성
- [ ] 환경별 .env 파일 준비
- [ ] 환경 변수 문서화

---

## 9. 참고 자료

### 9.1. 공식 문서

- **Vue.js**: https://vuejs.org/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **MSSQL**: https://docs.microsoft.com/ko-kr/sql/
- **Capacitor**: https://capacitorjs.com/
- **Vuetify**: https://vuetifyjs.com/

### 9.2. 개발 가이드

- **카카오 개발자 가이드**: https://developers.kakao.com/docs
- **네이버 개발자 가이드**: https://developers.naver.com/docs
- **PASS 인증 가이드**: 제공사 문서 참고
- **Face API 가이드**: 제공사 문서 참고

### 9.3. 유용한 도구

- **mkcert**: https://github.com/FiloSottile/mkcert
- **Docker**: https://www.docker.com/
- **Let's Encrypt**: https://letsencrypt.org/

---

## 10. 문제 해결 가이드

### 10.1. 자주 발생하는 문제

#### Node.js 버전 충돌
```bash
# nvm 사용 권장
nvm install 18
nvm use 18
```

#### Java 버전 문제
```bash
# JAVA_HOME 확인
echo $JAVA_HOME
# 또는
java -version
```

#### MSSQL 연결 실패
```bash
# 방화벽 확인
sudo ufw status
# 포트 확인
netstat -an | grep 1433
```

#### SSL 인증서 오류
```bash
# 브라우저에 CA 인증서 설치 확인
# Chrome: Settings > Privacy and security > Security > Manage certificates
```

---

## C. 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 1.0 | 2025-12-13 | 초안 작성 | Gemini |
| 1.1 | 2025-12-13 | Cursor 바이브코딩 환경 준비 섹션 추가 | Gemini |

---

**문서 끝**

