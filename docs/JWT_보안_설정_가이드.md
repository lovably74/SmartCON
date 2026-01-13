# JWT 토큰 시스템 보안 설정 가이드

## 개요

SmartCON Lite JWT 토큰 시스템의 보안 설정 및 운영 가이드입니다. 이 문서는 시스템 관리자와 DevOps 엔지니어를 위한 보안 모범 사례를 제공합니다.

## 목차

1. [환경별 보안 설정](#환경별-보안-설정)
2. [JWT 토큰 보안](#jwt-토큰-보안)
3. [Spring Security 설정](#spring-security-설정)
4. [데이터베이스 보안](#데이터베이스-보안)
5. [네트워크 보안](#네트워크-보안)
6. [모니터링 및 로깅](#모니터링-및-로깅)
7. [보안 점검 체크리스트](#보안-점검-체크리스트)

## 환경별 보안 설정

### 개발 환경 (Development)

```yaml
# application-dev.yml
jwt:
  secret: "dev-secret-key-for-development-only-min-256-bits"
  use-rsa: false
  access-token-expiration-minutes: 60
  refresh-token-expiration-days: 7

spring:
  security:
    debug: true  # 개발 시에만 활성화
  h2:
    console:
      enabled: true  # 개발 환경에서만 허용

logging:
  level:
    com.smartcon.global.security: DEBUG
    org.springframework.security: DEBUG
```

**개발 환경 보안 주의사항**:
- H2 콘솔은 개발 환경에서만 활성화
- 디버그 로깅은 민감한 정보 노출 주의
- 개발용 비밀키는 운영 환경과 완전히 분리

### 스테이징 환경 (Staging)

```yaml
# application-staging.yml
jwt:
  secret: ${JWT_SECRET}  # 환경변수로 관리
  use-rsa: true
  access-token-expiration-minutes: 30
  refresh-token-expiration-days: 3

spring:
  security:
    debug: false
  h2:
    console:
      enabled: false

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 운영 환경 (Production)

```yaml
# application-prod.yml
jwt:
  secret: ${JWT_SECRET}  # 강력한 비밀키 (환경변수)
  use-rsa: true
  rsa-private-key: ${JWT_RSA_PRIVATE_KEY}
  rsa-public-key: ${JWT_RSA_PUBLIC_KEY}
  access-token-expiration-minutes: 15  # 짧은 만료 시간
  refresh-token-expiration-days: 1     # 짧은 갱신 주기

spring:
  security:
    debug: false
  h2:
    console:
      enabled: false

server:
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: smartcon-lite

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: never
```

## JWT 토큰 보안

### 1. 비밀키 관리

#### HMAC 비밀키 (개발용)

```bash
# 안전한 비밀키 생성 (최소 256비트)
openssl rand -base64 32

# 환경변수 설정
export JWT_SECRET="your-generated-secret-key-here"
```

#### RSA 키 쌍 (운영용)

```bash
# RSA 개인키 생성 (2048비트)
openssl genrsa -out jwt-private.pem 2048

# RSA 공개키 추출
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem

# 환경변수 설정
export JWT_RSA_PRIVATE_KEY="$(cat jwt-private.pem)"
export JWT_RSA_PUBLIC_KEY="$(cat jwt-public.pem)"
```

**키 관리 모범 사례**:
- 개인키는 절대 소스코드에 포함하지 않음
- 환경변수 또는 보안 볼트 사용
- 정기적인 키 로테이션 (6개월마다)
- 키 백업 및 복구 계획 수립

### 2. 토큰 설정 최적화

#### 만료 시간 설정

```java
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    // 운영 환경 권장 설정
    private int accessTokenExpirationMinutes = 15;  // 15분
    private int refreshTokenExpirationDays = 1;     // 1일
    
    // 개발 환경 설정
    // private int accessTokenExpirationMinutes = 60;  // 60분
    // private int refreshTokenExpirationDays = 7;     // 7일
}
```

#### 토큰 클레임 최소화

```java
public String generateAccessToken(Long userId, String tenantId, String role, Map<String, Boolean> permissions) {
    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("tenant_id", tenantId)
        .claim("role", role)
        .claim("permissions", permissions)  // 필요한 권한만 포함
        .claim("token_type", "access")
        .setIssuer("smartcon-lite")
        .setAudience("smartcon-api")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .setId(UUID.randomUUID().toString())  // JTI 클레임으로 고유성 보장
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

### 3. 토큰 블랙리스트 보안

#### 메모리 기반 (개발/소규모)

```java
@Service
@Slf4j
public class InMemoryBlacklistService implements JwtTokenBlacklistService {
    private final ConcurrentHashMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Override
    public void addToBlacklist(String token, Duration expiration) {
        String tokenId = extractTokenId(token);
        Instant expiryTime = Instant.now().plus(expiration);
        blacklistedTokens.put(tokenId, expiryTime);
        
        log.info("토큰 블랙리스트 추가: {} (만료: {})", tokenId, expiryTime);
    }
    
    // 정기적인 만료 토큰 정리
    @Scheduled(fixedRate = 3600000) // 1시간마다
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int removedCount = 0;
        
        Iterator<Map.Entry<String, Instant>> iterator = blacklistedTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Instant> entry = iterator.next();
            if (entry.getValue().isBefore(now)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.info("만료된 블랙리스트 토큰 {} 개 정리 완료", removedCount);
        }
    }
}
```

#### Redis 기반 (운영 환경 권장)

```java
@Service
@ConditionalOnProperty(name = "jwt.blacklist.type", havingValue = "redis")
public class RedisBlacklistService implements JwtTokenBlacklistService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    
    @Override
    public void addToBlacklist(String token, Duration expiration) {
        String tokenId = extractTokenId(token);
        String key = BLACKLIST_PREFIX + tokenId;
        
        redisTemplate.opsForValue().set(key, "blacklisted", expiration);
        log.info("Redis 블랙리스트에 토큰 추가: {} (TTL: {}초)", tokenId, expiration.getSeconds());
    }
    
    @Override
    public boolean isBlacklisted(String token) {
        String tokenId = extractTokenId(token);
        String key = BLACKLIST_PREFIX + tokenId;
        
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
```

## Spring Security 설정

### 1. 보안 필터 체인

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        return http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable())
            
            // 세션 정책 (STATELESS)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 보안 헤더 설정
            .headers(headers -> headers
                .frameOptions().deny()  // X-Frame-Options: DENY
                .contentTypeOptions().and()  // X-Content-Type-Options: nosniff
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)  // 1년
                    .includeSubdomains(true)
                    .preload(true)
                )
            )
            
            // 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 경로
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/v1/subscriptions/plans").permitAll()
                .requestMatchers("/v1/subscriptions/create").permitAll()
                .requestMatchers("/v1/subscriptions/current").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // 개발 환경 전용
                .requestMatchers("/h2-console/**").permitAll()
                
                // 슈퍼관리자 전용
                .requestMatchers("/v1/admin/**").hasRole("SUPER")
                
                // 관리자 이상
                .requestMatchers("/v1/management/**").hasAnyRole("SUPER", "HQ")
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 예외 처리
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                .accessDeniedHandler(jwtAccessDeniedHandler())
            )
            
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 운영 환경에서는 특정 도메인만 허용
        if (isProductionEnvironment()) {
            configuration.setAllowedOrigins(Arrays.asList(
                "https://app.smartcon.co.kr",
                "https://admin.smartcon.co.kr"
            ));
        } else {
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 2. 인증 예외 처리

```java
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        log.warn("인증 실패: {} - {}", request.getRequestURI(), authException.getMessage());
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> errorResponse = ApiResponse.error("인증이 필요합니다.");
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}

@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        log.warn("접근 거부: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> errorResponse = ApiResponse.error("접근 권한이 없습니다.");
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
```

## 데이터베이스 보안

### 1. 멀티테넌트 데이터 격리

```java
@Entity
@Table(name = "users")
@Where(clause = "tenant_id = :#{T(com.smartcon.global.tenant.TenantContext).getCurrentTenantId()}")
public class User extends BaseTenantEntity {
    // 자동으로 tenant_id 필터링 적용
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTenantEntity {
    
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;
    
    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getCurrentTenantId();
        }
    }
}
```

### 2. 데이터베이스 연결 보안

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/smartcon_lite?useSSL=true&requireSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
  
  jpa:
    hibernate:
      ddl-auto: validate  # 운영에서는 validate만 사용
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false
```

### 3. 민감한 데이터 암호화

```java
@Service
public class PasswordService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

@Entity
public class User extends BaseTenantEntity {
    
    @Column(name = "password", nullable = false)
    private String password;  // BCrypt로 암호화된 비밀번호
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    // 민감한 정보는 로그에 출력되지 않도록
    @Override
    public String toString() {
        return "User{id=" + getId() + ", email='" + email + "'}";
    }
}
```

## 네트워크 보안

### 1. HTTPS 설정

```yaml
# SSL/TLS 설정
server:
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: smartcon-lite
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers: 
      - TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
      - TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
      - TLS_DHE_RSA_WITH_AES_256_GCM_SHA384
      - TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
  port: 8443
  
# HTTP를 HTTPS로 리다이렉트
management:
  server:
    ssl:
      enabled: true
```

### 2. 방화벽 및 네트워크 정책

```bash
# 방화벽 설정 예제 (Ubuntu/CentOS)
# HTTPS만 허용
sudo ufw allow 443/tcp
sudo ufw allow 8443/tcp

# SSH 접근 제한
sudo ufw allow from 192.168.1.0/24 to any port 22

# 불필요한 포트 차단
sudo ufw deny 8080/tcp  # HTTP 차단
sudo ufw deny 3306/tcp  # MySQL 직접 접근 차단

# 방화벽 활성화
sudo ufw enable
```

### 3. API Rate Limiting

```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private final int maxRequests = 100;  // 분당 최대 요청 수
    private final long timeWindow = 60000; // 1분
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = getClientIpAddress(httpRequest);
        
        if (isRateLimited(clientIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp) {
        long now = System.currentTimeMillis();
        List<Long> requests = requestCounts.computeIfAbsent(clientIp, k -> new ArrayList<>());
        
        // 시간 윈도우 밖의 요청 제거
        requests.removeIf(timestamp -> now - timestamp > timeWindow);
        
        if (requests.size() >= maxRequests) {
            return true;
        }
        
        requests.add(now);
        return false;
    }
}
```

## 모니터링 및 로깅

### 1. 보안 이벤트 로깅

```java
@Component
@Slf4j
public class SecurityEventLogger {
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String clientIp = getClientIpAddress();
        
        log.info("로그인 성공: 사용자={}, IP={}, 시간={}", 
                username, clientIp, Instant.now());
    }
    
    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String clientIp = getClientIpAddress();
        String reason = event.getException().getMessage();
        
        log.warn("로그인 실패: 사용자={}, IP={}, 이유={}, 시간={}", 
                username, clientIp, reason, Instant.now());
    }
    
    @EventListener
    public void handleAccessDenied(AuthorizationDeniedEvent event) {
        String username = event.getAuthentication().getName();
        String resource = event.getAuthorizationDecision().toString();
        
        log.warn("접근 거부: 사용자={}, 리소스={}, 시간={}", 
                username, resource, Instant.now());
    }
}
```

### 2. 메트릭 수집

```java
@Component
public class JwtMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter tokenGeneratedCounter;
    private final Counter tokenValidationCounter;
    private final Timer tokenValidationTimer;
    
    public JwtMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.tokenGeneratedCounter = Counter.builder("jwt.token.generated")
                .description("JWT 토큰 생성 횟수")
                .register(meterRegistry);
        this.tokenValidationCounter = Counter.builder("jwt.token.validation")
                .description("JWT 토큰 검증 횟수")
                .tag("result", "success")
                .register(meterRegistry);
        this.tokenValidationTimer = Timer.builder("jwt.token.validation.time")
                .description("JWT 토큰 검증 시간")
                .register(meterRegistry);
    }
    
    public void recordTokenGenerated(String tenantId, String role) {
        tokenGeneratedCounter.increment(
                Tags.of("tenant", tenantId, "role", role)
        );
    }
    
    public void recordTokenValidation(boolean success, Duration duration) {
        tokenValidationCounter.increment(
                Tags.of("result", success ? "success" : "failure")
        );
        tokenValidationTimer.record(duration);
    }
}
```

### 3. 로그 설정

```yaml
# logback-spring.xml 설정
logging:
  level:
    com.smartcon.global.security: INFO
    org.springframework.security: WARN
    org.springframework.web.filter.CommonsRequestLoggingFilter: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{tenantId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{tenantId}] %logger{36} - %msg%n"
  file:
    name: logs/smartcon-lite.log
    max-size: 100MB
    max-history: 30
```

## 보안 점검 체크리스트

### 배포 전 보안 점검

- [ ] **환경변수 설정**
  - [ ] JWT 비밀키가 환경변수로 설정됨
  - [ ] 데이터베이스 자격증명이 환경변수로 설정됨
  - [ ] SSL 인증서 경로가 올바르게 설정됨

- [ ] **JWT 토큰 설정**
  - [ ] 운영 환경에서 RSA 알고리즘 사용
  - [ ] 적절한 토큰 만료 시간 설정 (Access: 15분, Refresh: 1일)
  - [ ] 토큰 블랙리스트 서비스 활성화

- [ ] **Spring Security 설정**
  - [ ] CSRF 보호 적절히 설정
  - [ ] CORS 정책이 운영 환경에 맞게 설정
  - [ ] 보안 헤더 설정 (HSTS, X-Frame-Options 등)
  - [ ] 세션 정책이 STATELESS로 설정

- [ ] **데이터베이스 보안**
  - [ ] 멀티테넌트 데이터 격리 확인
  - [ ] 비밀번호 BCrypt 암호화 확인
  - [ ] 데이터베이스 연결 SSL 사용

- [ ] **네트워크 보안**
  - [ ] HTTPS 강제 사용
  - [ ] 불필요한 포트 차단
  - [ ] Rate Limiting 설정

- [ ] **모니터링**
  - [ ] 보안 이벤트 로깅 활성화
  - [ ] 메트릭 수집 설정
  - [ ] 알림 시스템 구성

### 정기 보안 점검 (월 1회)

- [ ] **토큰 보안**
  - [ ] 블랙리스트 토큰 정리 상태 확인
  - [ ] 토큰 만료 시간 적절성 검토
  - [ ] 비정상적인 토큰 사용 패턴 모니터링

- [ ] **접근 로그 분석**
  - [ ] 실패한 로그인 시도 분석
  - [ ] 비정상적인 API 접근 패턴 확인
  - [ ] 권한 상승 시도 모니터링

- [ ] **시스템 업데이트**
  - [ ] Spring Security 버전 업데이트
  - [ ] JWT 라이브러리 보안 패치 적용
  - [ ] 운영체제 보안 업데이트

### 보안 사고 대응 절차

1. **사고 감지**
   - 비정상적인 로그인 시도 급증
   - 권한 없는 API 접근 시도
   - 토큰 무차별 대입 공격

2. **즉시 대응**
   - 의심스러운 IP 차단
   - 영향받은 사용자 계정 임시 잠금
   - 관련 토큰 블랙리스트 추가

3. **사후 조치**
   - 보안 로그 상세 분석
   - 취약점 패치 적용
   - 보안 정책 업데이트
   - 사용자 비밀번호 재설정 권고

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026년 1월 12일  
**작성자**: SmartCON Lite 보안팀