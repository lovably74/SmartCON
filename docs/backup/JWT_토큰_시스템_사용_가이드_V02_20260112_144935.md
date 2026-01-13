# JWT 토큰 시스템 사용 가이드

## 개요

SmartCON Lite의 JWT 토큰 기반 인증 시스템 사용법을 안내합니다. 이 가이드는 개발자와 시스템 관리자를 위한 실용적인 사용법을 제공합니다.

## 목차

1. [JWT 토큰 시스템 소개](#jwt-토큰-시스템-소개)
2. [인증 플로우](#인증-플로우)
3. [API 사용법](#api-사용법)
4. [토큰 관리](#토큰-관리)
5. [권한 및 역할](#권한-및-역할)
6. [멀티테넌트 사용법](#멀티테넌트-사용법)
7. [개발 도구](#개발-도구)
8. [문제 해결](#문제-해결)

## JWT 토큰 시스템 소개

### 주요 특징

- **Spring Security 6.x 기반**: 최신 보안 프레임워크 사용
- **멀티테넌트 지원**: 테넌트별 데이터 격리
- **역할 기반 접근 제어**: 5단계 사용자 역할 지원
- **토큰 블랙리스트**: 로그아웃된 토큰 자동 차단
- **개발 도구 제공**: 테스트용 토큰 생성 API

### 토큰 타입

1. **Access Token**: API 접근용 (만료: 60분)
2. **Refresh Token**: 토큰 갱신용 (만료: 7일)

## 인증 플로우

### 1. 로그인 플로우

```mermaid
sequenceDiagram
    participant C as Client
    participant A as AuthController
    participant S as AuthService
    participant J as JwtTokenService
    
    C->>A: POST /v1/auth/login
    A->>S: login(email, password)
    S->>S: 사용자 검증
    S->>J: generateTokens()
    J-->>S: Access + Refresh Token
    S-->>A: LoginResponse
    A-->>C: 200 OK + Tokens
```

### 2. API 접근 플로우

```mermaid
sequenceDiagram
    participant C as Client
    participant F as JwtAuthFilter
    participant J as JwtTokenService
    participant B as BlacklistService
    participant T as TenantContext
    
    C->>F: API Request + Authorization Header
    F->>B: 블랙리스트 확인
    F->>J: 토큰 검증
    F->>T: 테넌트 컨텍스트 설정
    F->>F: SecurityContext 설정
    F-->>C: API Response
```

### 3. 토큰 갱신 플로우

```mermaid
sequenceDiagram
    participant C as Client
    participant A as AuthController
    participant S as AuthService
    participant J as JwtTokenService
    
    C->>A: POST /v1/auth/refresh
    A->>S: refreshToken(refreshToken)
    S->>J: validateToken(refreshToken)
    S->>J: generateAccessToken()
    J-->>S: New Access Token
    S-->>A: RefreshResponse
    A-->>C: 200 OK + New Token
```

## API 사용법

### 1. 로그인 API

**요청**:
```http
POST /v1/auth/login
Content-Type: application/json

{
  "email": "admin@company.com",
  "password": "password123"
}
```

**응답**:
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "admin@company.com",
      "name": "관리자",
      "role": "ROLE_SUPER",
      "tenantId": "company-001"
    },
    "permissions": {
      "admin.read": true,
      "admin.write": true,
      "subscription.approve": true
    }
  }
}
```

### 2. 토큰 갱신 API

**요청**:
```http
POST /v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답**:
```json
{
  "success": true,
  "message": "토큰 갱신 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 3. 로그아웃 API

**요청**:
```http
POST /v1/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답**:
```json
{
  "success": true,
  "message": "로그아웃 성공"
}
```

### 4. 토큰 검증 API

**요청**:
```http
POST /v1/auth/validate
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답**:
```json
{
  "success": true,
  "message": "토큰 검증 성공",
  "data": {
    "valid": true,
    "userId": 1,
    "tenantId": "company-001",
    "role": "ROLE_SUPER",
    "expiresAt": "2026-01-12T15:30:00Z"
  }
}
```

## 토큰 관리

### Authorization 헤더 사용법

모든 보호된 API 요청에는 Authorization 헤더가 필요합니다:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 토큰 저장 권장사항

**웹 애플리케이션**:
- Access Token: 메모리 또는 sessionStorage
- Refresh Token: httpOnly 쿠키 (권장)

**모바일 애플리케이션**:
- Access Token: 메모리
- Refresh Token: 보안 저장소 (Keychain/Keystore)

### 토큰 갱신 전략

```javascript
// 자동 토큰 갱신 예제
class TokenManager {
  constructor() {
    this.accessToken = null;
    this.refreshToken = null;
  }

  async makeRequest(url, options = {}) {
    // 토큰 만료 확인
    if (this.isTokenExpired(this.accessToken)) {
      await this.refreshAccessToken();
    }

    // API 요청
    return fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${this.accessToken}`
      }
    });
  }

  async refreshAccessToken() {
    const response = await fetch('/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: this.refreshToken })
    });

    if (response.ok) {
      const data = await response.json();
      this.accessToken = data.data.accessToken;
    } else {
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
    }
  }
}
```

## 권한 및 역할

### 사용자 역할 계층

1. **ROLE_SUPER (슈퍼관리자)**
   - 모든 시스템 기능 접근
   - 테넌트 관리, 구독 승인
   - 시스템 모니터링

2. **ROLE_HQ (본사 관리자)**
   - 테넌트 내 모든 기능
   - 사용자 관리, 계약 관리
   - 출근 기록 조회

3. **ROLE_SITE (현장 관리자)**
   - 현장 관리 기능
   - 출근 관리, 작업자 관리
   - 계약 조회

4. **ROLE_TEAM (팀장)**
   - 팀 관리 기능
   - 출근 기록 조회
   - 작업자 조회

5. **ROLE_WORKER (작업자)**
   - 개인 정보 관리
   - 본인 출근 기록 조회
   - 본인 계약 조회

### 권한 확인 방법

**Java (Spring Security)**:
```java
@PreAuthorize("hasRole('SUPER')")
@GetMapping("/admin/users")
public ResponseEntity<?> getUsers() {
    // 슈퍼관리자만 접근 가능
}

@PreAuthorize("hasAnyRole('SUPER', 'HQ')")
@GetMapping("/contracts")
public ResponseEntity<?> getContracts() {
    // 슈퍼관리자 또는 본사 관리자만 접근 가능
}
```

**JavaScript (프론트엔드)**:
```javascript
// 토큰에서 권한 정보 추출
function hasPermission(permission) {
  const token = getAccessToken();
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.permissions && payload.permissions[permission];
}

// 사용 예제
if (hasPermission('admin.write')) {
  // 관리자 쓰기 권한이 있는 경우
  showAdminPanel();
}
```

## 멀티테넌트 사용법

### 테넌트 컨텍스트

모든 API 요청은 자동으로 테넌트 컨텍스트가 설정됩니다:

```java
// 현재 테넌트 ID 조회
String tenantId = TenantContext.getCurrentTenantId();

// 테넌트별 데이터 조회 (자동 필터링)
List<User> users = userRepository.findAll(); // 현재 테넌트의 사용자만 조회
```

### 테넌트 격리 확인

```java
@Entity
@Table(name = "users")
public class User extends BaseTenantEntity {
    // tenant_id 컬럼이 자동으로 추가됨
    // 모든 쿼리에 tenant_id 조건이 자동 추가됨
}
```

## 개발 도구

### 개발용 토큰 생성 API

개발 및 테스트 환경에서 사용할 수 있는 토큰 생성 API입니다:

**요청**:
```http
POST /v1/auth/dev-token
Content-Type: application/json

{
  "role": "ROLE_SUPER",
  "tenantId": "test-tenant"
}
```

**응답**:
```json
{
  "success": true,
  "message": "개발용 토큰 생성 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 999,
      "email": "dev@test.com",
      "name": "개발자",
      "role": "ROLE_SUPER",
      "tenantId": "test-tenant"
    }
  }
}
```

### 기본값

- **role**: 생략 시 `ROLE_SUPER`
- **tenantId**: 생략 시 `dev-tenant`

### 사용 예제

```bash
# 슈퍼관리자 토큰 생성
curl -X POST http://localhost:8080/v1/auth/dev-token \
  -H "Content-Type: application/json" \
  -d '{}'

# 현장 관리자 토큰 생성
curl -X POST http://localhost:8080/v1/auth/dev-token \
  -H "Content-Type: application/json" \
  -d '{"role": "ROLE_SITE", "tenantId": "site-001"}'
```

## 문제 해결

### 일반적인 오류 및 해결책

#### 1. 401 Unauthorized

**원인**:
- 토큰이 없거나 잘못된 형식
- 토큰이 만료됨
- 토큰이 블랙리스트에 등록됨

**해결책**:
```javascript
// 토큰 만료 확인
function isTokenExpired(token) {
  if (!token) return true;
  
  const payload = JSON.parse(atob(token.split('.')[1]));
  const now = Date.now() / 1000;
  return payload.exp < now;
}

// 자동 토큰 갱신
if (isTokenExpired(accessToken)) {
  await refreshAccessToken();
}
```

#### 2. 403 Forbidden

**원인**:
- 권한 부족
- 잘못된 역할

**해결책**:
```javascript
// 권한 확인
function checkPermission(requiredRole) {
  const token = getAccessToken();
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.role === requiredRole;
}
```

#### 3. 토큰 검증 실패

**원인**:
- 잘못된 서명
- 토큰 형식 오류
- 클레임 누락

**해결책**:
```java
// 토큰 검증 로그 확인
@Slf4j
public class JwtTokenService {
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}
```

### 디버깅 도구

#### 1. JWT 토큰 디코딩

```javascript
// 토큰 페이로드 확인
function decodeToken(token) {
  const parts = token.split('.');
  const header = JSON.parse(atob(parts[0]));
  const payload = JSON.parse(atob(parts[1]));
  
  console.log('Header:', header);
  console.log('Payload:', payload);
  console.log('Expires:', new Date(payload.exp * 1000));
}
```

#### 2. 토큰 상태 확인

```bash
# 토큰 검증 API 사용
curl -X POST http://localhost:8080/v1/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "YOUR_TOKEN_HERE"}'
```

### 성능 최적화

#### 1. 토큰 캐싱

```java
@Service
@Slf4j
public class TokenCacheService {
    private final Cache<String, Claims> tokenCache = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    
    public Claims getCachedClaims(String token) {
        return tokenCache.get(token, this::parseToken);
    }
}
```

#### 2. 블랙리스트 최적화

```java
// 운영 환경에서는 Redis 사용 권장
@Service
public class RedisBlacklistService implements JwtTokenBlacklistService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void addToBlacklist(String token, Duration expiration) {
        redisTemplate.opsForValue().set(
            "blacklist:" + token, 
            "true", 
            expiration
        );
    }
}
```

## 보안 권장사항

### 1. 토큰 보안

- **HTTPS 사용**: 모든 토큰 전송은 HTTPS로
- **토큰 저장**: 안전한 저장소 사용
- **토큰 만료**: 적절한 만료 시간 설정
- **토큰 갱신**: 자동 갱신 로직 구현

### 2. 운영 환경 설정

```yaml
# application-prod.yml
jwt:
  secret: ${JWT_SECRET} # 환경변수로 관리
  use-rsa: true # RSA 알고리즘 사용
  access-token-expiration-minutes: 30 # 짧은 만료 시간
  refresh-token-expiration-days: 1 # 짧은 갱신 주기
```

### 3. 모니터링

```java
// 토큰 사용 통계 수집
@EventListener
public void handleTokenGenerated(TokenGeneratedEvent event) {
    meterRegistry.counter("jwt.token.generated", 
        "tenant", event.getTenantId(),
        "role", event.getRole()
    ).increment();
}
```

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026년 1월 12일  
**작성자**: SmartCON Lite 개발팀