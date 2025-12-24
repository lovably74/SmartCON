# SmartCON Lite 백엔드 아키텍처

**문서 버전:** 1.0  
**작성일:** 2025년 12월 23일  
**작성자:** Kiro AI Assistant  
**기반 문서:** PRD v2.8, 상세기능명세서 v3.0

---

## 1. 백엔드 아키텍처 개요

### 1.1 아키텍처 원칙
- **Domain-Driven Design (DDD)**: 비즈니스 도메인 중심 설계
- **Multi-tenant SaaS**: 테넌트별 데이터 격리
- **Clean Architecture**: 계층 분리 및 의존성 역전
- **CQRS Pattern**: 명령과 조회 분리 (복잡한 도메인에 적용)
- **Event-Driven Architecture**: 도메인 이벤트 기반 비동기 처리

### 1.2 기술 스택
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 3.3.x
- **Database**: MariaDB 10.11 (Primary), Redis 7.x (Cache)
- **Security**: Spring Security 6.x + JWT + OAuth2
- **Batch**: Spring Batch 5.x
- **Message Queue**: RabbitMQ (이벤트 처리)
- **Build**: Maven 3.8+
- **Container**: Docker

---

## 2. 프로젝트 구조

### 2.1 전체 디렉토리 구조
```
backend/
├── src/main/java/com/smartcon/
│   ├── SmartconApplication.java
│   ├── domain/                     # 도메인 계층
│   │   ├── admin/                  # 슈퍼 관리자 도메인
│   │   ├── tenant/                 # 테넌트 관리 도메인
│   │   ├── user/                   # 사용자 관리 도메인
│   │   ├── site/                   # 현장 관리 도메인
│   │   ├── team/                   # 팀 관리 도메인
│   │   ├── attendance/             # 출역 관리 도메인
│   │   ├── contract/               # 계약 관리 도메인
│   │   ├── workassignment/         # 작업 배정 도메인
│   │   ├── dailyreport/            # 공사일보 도메인
│   │   └── settlement/             # 정산 도메인
│   ├── global/                     # 공통 기능
│   │   ├── common/                 # 공통 유틸리티
│   │   ├── config/                 # 설정 클래스
│   │   ├── security/               # 보안 설정
│   │   ├── exception/              # 예외 처리
│   │   ├── tenant/                 # 멀티테넌트 관리
│   │   └── event/                  # 도메인 이벤트
│   └── infra/                      # 인프라 계층
│       ├── face/                   # FaceNet API 클라이언트
│       ├── weather/                # 기상청 API 클라이언트
│       ├── payment/                # PG사 API 클라이언트
│       ├── notification/           # 알림 서비스
│       └── storage/                # 파일 저장소 (S3)
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── db/migration/               # Flyway 마이그레이션
│   └── templates/                  # 계약서 템플릿
├── src/test/java/
│   ├── unit/                       # 단위 테스트
│   ├── integration/                # 통합 테스트
│   └── e2e/                        # E2E 테스트
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── docker-compose.prod.yml
├── pom.xml
└── README.md
```

### 2.2 도메인별 상세 구조 (예시: attendance 도메인)
```
domain/attendance/
├── entity/                         # 엔티티
│   ├── AttendanceLog.java
│   ├── FaceEmbedding.java
│   └── AttendanceStatus.java
├── repository/                     # 리포지토리 인터페이스
│   ├── AttendanceLogRepository.java
│   └── FaceEmbeddingRepository.java
├── service/                        # 도메인 서비스
│   ├── AttendanceService.java
│   ├── FaceRecognitionService.java
│   └── AttendanceCalculationService.java
├── controller/                     # REST 컨트롤러
│   ├── AttendanceController.java
│   └── FaceController.java
├── dto/                           # DTO 클래스
│   ├── request/
│   │   ├── CheckInRequest.java
│   │   └── FinalizeAttendanceRequest.java
│   └── response/
│       ├── AttendanceLogResponse.java
│       └── AttendanceStatsResponse.java
├── event/                         # 도메인 이벤트
│   ├── AttendanceCheckedInEvent.java
│   └── AttendanceFinalizedEvent.java
└── exception/                     # 도메인 예외
    ├── AttendanceNotFoundException.java
    └── FaceRecognitionFailedException.java
```

---

## 3. 핵심 컴포넌트 설계

### 3.1 멀티테넌트 관리
#### 3.1.1 BaseTenantEntity
```java
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity extends BaseEntity {
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @PrePersist
    @PreUpdate
    public void setTenantId() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getCurrentTenantId();
        }
    }
    
    // getters, setters
}
```

#### 3.1.2 TenantContext
```java
@Component
public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static Long getCurrentTenantId() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}
```

#### 3.1.3 TenantInterceptor
```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long tenantId = jwtTokenProvider.getTenantIdFromToken(token);
            TenantContext.setCurrentTenantId(tenantId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
```

### 3.2 보안 및 인증
#### 3.2.1 JWT 토큰 구조
```java
@Component
public class JwtTokenProvider {
    
    public String createAccessToken(Authentication authentication, Long tenantId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);
        
        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .claim("tenantId", tenantId)
                .claim("roles", userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public Long getTenantIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("tenantId", Long.class);
    }
}
```

#### 3.2.2 OAuth2 소셜 로그인
```java
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        
        User user = findOrCreateUser(userInfo, registrationId);
        return UserPrincipal.create(user, oauth2User.getAttributes());
    }
    
    private User findOrCreateUser(OAuth2UserInfo userInfo, String provider) {
        return userRepository.findByProviderAndProviderId(provider, userInfo.getId())
                .orElseGet(() -> createNewUser(userInfo, provider));
    }
}
```

### 3.3 도메인 이벤트 처리
#### 3.3.1 도메인 이벤트 정의
```java
public class AttendanceCheckedInEvent extends DomainEvent {
    private final Long workerId;
    private final Long siteId;
    private final LocalDateTime checkInTime;
    private final boolean faceMatched;
    
    public AttendanceCheckedInEvent(Long workerId, Long siteId, LocalDateTime checkInTime, boolean faceMatched) {
        super();
        this.workerId = workerId;
        this.siteId = siteId;
        this.checkInTime = checkInTime;
        this.faceMatched = faceMatched;
    }
    
    // getters
}
```

#### 3.3.2 이벤트 핸들러
```java
@Component
@Transactional
public class AttendanceEventHandler {
    
    @EventListener
    @Async
    public void handleAttendanceCheckedIn(AttendanceCheckedInEvent event) {
        // 팀장에게 출근 알림 발송
        notificationService.sendAttendanceNotification(event.getWorkerId(), event.getSiteId());
        
        // 출역 통계 업데이트
        attendanceStatsService.updateDailyStats(event.getSiteId(), event.getCheckInTime().toLocalDate());
    }
    
    @EventListener
    @Async
    public void handleAttendanceFinalized(AttendanceFinalizedEvent event) {
        // 급여 계산 트리거
        payrollService.calculateDailyPayroll(event.getSiteId(), event.getWorkDate());
        
        // 일보 자동 생성 트리거
        dailyReportService.generateDailyReport(event.getSiteId(), event.getWorkDate());
    }
}
```

### 3.4 배치 처리 (Spring Batch)
#### 3.4.1 안면인식 동기화 배치
```java
@Configuration
@EnableBatchProcessing
public class FaceSyncBatchConfig {
    
    @Bean
    public Job faceSyncJob() {
        return jobBuilderFactory.get("faceSyncJob")
                .incrementer(new RunIdIncrementer())
                .start(faceSyncStep())
                .build();
    }
    
    @Bean
    public Step faceSyncStep() {
        return stepBuilderFactory.get("faceSyncStep")
                .<WorkAssignment, FaceSyncData>chunk(100)
                .reader(workAssignmentReader())
                .processor(faceSyncProcessor())
                .writer(faceSyncWriter())
                .build();
    }
    
    @Bean
    public ItemReader<WorkAssignment> workAssignmentReader() {
        return new JpaPagingItemReaderBuilder<WorkAssignment>()
                .name("workAssignmentReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT wa FROM WorkAssignment wa WHERE wa.workDate = :workDate AND wa.status = 'APPROVED'")
                .parameterValues(Map.of("workDate", LocalDate.now()))
                .pageSize(100)
                .build();
    }
    
    @Bean
    public ItemProcessor<WorkAssignment, FaceSyncData> faceSyncProcessor() {
        return workAssignment -> {
            List<FaceEmbedding> embeddings = faceEmbeddingRepository.findByTeamId(workAssignment.getTeam().getId());
            return new FaceSyncData(workAssignment.getSite().getId(), embeddings);
        };
    }
    
    @Bean
    public ItemWriter<FaceSyncData> faceSyncWriter() {
        return items -> {
            for (FaceSyncData item : items) {
                faceNetClient.syncEmbeddings(item.getSiteId(), item.getEmbeddings());
            }
        };
    }
}
```

### 3.5 외부 API 클라이언트
#### 3.5.1 FaceNet API 클라이언트
```java
@Component
public class FaceNetClient {
    
    @Value("${facenet.api.url}")
    private String faceNetApiUrl;
    
    private final RestTemplate restTemplate;
    
    public FaceEmbeddingResponse generateEmbedding(MultipartFile image) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", image.getResource());
        body.add("quality", true);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        return restTemplate.postForObject(
            faceNetApiUrl + "/api/v1/face/embedding",
            requestEntity,
            FaceEmbeddingResponse.class
        );
    }
    
    public FaceMatchResponse matchFace(List<Double> sourceEmbedding, List<FaceEmbedding> targetEmbeddings) {
        FaceMatchRequest request = FaceMatchRequest.builder()
                .sourceEmbedding(sourceEmbedding)
                .targetEmbeddings(targetEmbeddings.stream()
                    .map(e -> new TargetEmbedding(e.getId().toString(), e.getEmbedding()))
                    .collect(Collectors.toList()))
                .threshold(0.8)
                .build();
        
        return restTemplate.postForObject(
            faceNetApiUrl + "/api/v1/face/match",
            request,
            FaceMatchResponse.class
        );
    }
}
```

---

## 4. API 설계 및 구현

### 4.1 REST API 컨트롤러 구조
#### 4.1.1 기본 컨트롤러 구조
```java
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Validated
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('SITE', 'HQ', 'SUPER')")
    public ResponseEntity<ApiResponse<Page<AttendanceLogResponse>>> getAttendanceLogs(
            @RequestParam Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        
        Page<AttendanceLogResponse> logs = attendanceService.getAttendanceLogs(siteId, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    @PostMapping("/logs")
    @PreAuthorize("hasRole('SYSTEM')") // 안면인식기에서 호출
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> createAttendanceLog(
            @Valid @RequestBody CheckInRequest request) {
        
        AttendanceLogResponse response = attendanceService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "출근이 기록되었습니다."));
    }
    
    @PostMapping("/finalize")
    @PreAuthorize("hasRole('SITE')")
    public ResponseEntity<ApiResponse<Void>> finalizeAttendance(
            @Valid @RequestBody FinalizeAttendanceRequest request) {
        
        attendanceService.finalizeAttendance(request.getSiteId(), request.getDate(), request.getLogIds());
        return ResponseEntity.ok(ApiResponse.success(null, "출역이 마감되었습니다."));
    }
}
```

### 4.2 DTO 설계
#### 4.2.1 Request DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    
    @NotNull(message = "노무자 ID는 필수입니다.")
    private Long workerId;
    
    @NotNull(message = "현장 ID는 필수입니다.")
    private Long siteId;
    
    @NotNull(message = "체크인 시간은 필수입니다.")
    private LocalDateTime checkInTime;
    
    private Boolean faceMatched = false;
    
    @DecimalMin(value = "0.0", message = "인식 정확도는 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "인식 정확도는 1 이하여야 합니다.")
    private BigDecimal faceConfidence;
    
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal gpsLatitude;
    
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal gpsLongitude;
}
```

#### 4.2.2 Response DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLogResponse {
    
    private Long id;
    private Long workerId;
    private String workerName;
    private Long siteId;
    private String siteName;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    private BigDecimal overtimeHours;
    private Boolean faceMatched;
    private BigDecimal faceConfidence;
    private AttendanceStatus status;
    private Boolean manualAdjustment;
    private String adjustmentReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AttendanceLogResponse from(AttendanceLog attendanceLog) {
        return AttendanceLogResponse.builder()
                .id(attendanceLog.getId())
                .workerId(attendanceLog.getWorker().getId())
                .workerName(attendanceLog.getWorker().getName())
                .siteId(attendanceLog.getSite().getId())
                .siteName(attendanceLog.getSite().getName())
                .workDate(attendanceLog.getWorkDate())
                .checkInTime(attendanceLog.getCheckInTime())
                .checkOutTime(attendanceLog.getCheckOutTime())
                .totalHours(attendanceLog.getTotalHours())
                .overtimeHours(attendanceLog.getOvertimeHours())
                .faceMatched(attendanceLog.getFaceMatched())
                .faceConfidence(attendanceLog.getFaceConfidence())
                .status(attendanceLog.getStatus())
                .manualAdjustment(attendanceLog.getManualAdjustment())
                .adjustmentReason(attendanceLog.getAdjustmentReason())
                .createdAt(attendanceLog.getCreatedAt())
                .updatedAt(attendanceLog.getUpdatedAt())
                .build();
    }
}
```

### 4.3 예외 처리
#### 4.3.1 글로벌 예외 핸들러
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorDetail> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "입력 데이터가 올바르지 않습니다.", errors));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "접근 권한이 없습니다."));
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
```

---

## 5. 데이터베이스 설계

### 5.1 데이터베이스 마이그레이션 (Flyway)
#### 5.1.1 초기 스키마 생성
```sql
-- V1__Create_initial_schema.sql
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_number VARCHAR(12) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    ceo_name VARCHAR(50),
    business_type VARCHAR(50),
    road_address VARCHAR(200),
    detail_address VARCHAR(100),
    zip_code VARCHAR(10),
    status ENUM('TRIAL', 'ACTIVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'TRIAL',
    subscription_plan VARCHAR(50),
    max_sites INT DEFAULT 1,
    max_users INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    provider ENUM('LOCAL', 'KAKAO', 'NAVER') NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(100),
    password_hash VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    UNIQUE KEY unique_email_per_tenant (tenant_id, email),
    UNIQUE KEY unique_provider_per_tenant (tenant_id, provider, provider_id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role ENUM('ROLE_SUPER', 'ROLE_HQ', 'ROLE_SITE', 'ROLE_TEAM', 'ROLE_WORKER') NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 5.2 인덱스 최적화
```sql
-- V2__Create_indexes.sql
-- 테넌트별 데이터 조회 최적화
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_sites_tenant_id ON sites(tenant_id);
CREATE INDEX idx_attendance_logs_tenant_id ON attendance_logs(tenant_id);

-- 출역 조회 최적화
CREATE INDEX idx_attendance_logs_site_date ON attendance_logs(site_id, work_date);
CREATE INDEX idx_attendance_logs_worker_date ON attendance_logs(worker_id, work_date);

-- 작업 배정 조회 최적화
CREATE INDEX idx_work_assignments_site_date ON work_assignments(site_id, work_date);
CREATE INDEX idx_work_assignments_team_status ON work_assignments(team_id, status);

-- 계약서 조회 최적화
CREATE INDEX idx_contracts_worker_status ON contracts(worker_id, status);
CREATE INDEX idx_contracts_site_status ON contracts(site_id, status);
```

---

## 6. 설정 및 프로파일

### 6.1 애플리케이션 설정
#### 6.1.1 application.yml (공통 설정)
```yaml
spring:
  application:
    name: smartcon-backend
  
  profiles:
    active: local
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        enable_lazy_load_no_trans: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            redirect-uri: ${KAKAO_REDIRECT_URI}
            authorization-grant-type: authorization_code
            scope: profile_nickname, account_email
          naver:
            client-id: ${NAVER_CLIENT_ID}
            client-secret: ${NAVER_CLIENT_SECRET}
            redirect-uri: ${NAVER_REDIRECT_URI}
            authorization-grant-type: authorization_code
            scope: name, email

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 900000    # 15분
  refresh-token-expiration: 604800000 # 7일

logging:
  level:
    com.smartcon: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{tenantId}] %logger{36} - %msg%n"
```

#### 6.1.2 application-local.yml (로컬 개발)
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/smartcon_local
    username: smartcon
    password: smartcon123
    driver-class-name: org.mariadb.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    password: 
  
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop

external:
  facenet:
    api-url: http://localhost:8081
  weather:
    api-url: https://api.openweathermap.org/data/2.5
    api-key: ${WEATHER_API_KEY}
  
cors:
  allowed-origins: 
    - http://localhost:5173
    - http://localhost:3000
```

#### 6.1.3 application-prod.yml (프로덕션)
```yaml
spring:
  datasource:
    url: jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
  
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

external:
  facenet:
    api-url: ${FACENET_API_URL}
  weather:
    api-url: https://api.openweathermap.org/data/2.5
    api-key: ${WEATHER_API_KEY}

logging:
  level:
    com.smartcon: INFO
    org.springframework.security: WARN
  file:
    name: /app/logs/smartcon.log
```

---

## 7. 테스트 구조

### 7.1 테스트 계층 구조
```
src/test/java/com/smartcon/
├── unit/                           # 단위 테스트
│   ├── domain/
│   │   ├── attendance/
│   │   │   ├── service/
│   │   │   └── entity/
│   │   └── user/
│   └── global/
├── integration/                    # 통합 테스트
│   ├── controller/
│   ├── repository/
│   └── external/
├── e2e/                           # E2E 테스트
│   └── scenario/
└── testcontainers/                # 테스트컨테이너 설정
    ├── MariaDBTestContainer.java
    └── RedisTestContainer.java
```

### 7.2 테스트 설정
#### 7.2.1 테스트 기본 설정
```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2025-12-23T10:00:00Z"), ZoneId.systemDefault());
    }
    
    @Bean
    @Primary
    public FaceNetClient mockFaceNetClient() {
        return Mockito.mock(FaceNetClient.class);
    }
}
```

---

## 8. 배포 및 운영

### 8.1 Docker 설정
#### 8.1.1 Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/smartcon-backend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

#### 8.1.2 docker-compose.yml (개발용)
```yaml
version: '3.8'
services:
  mariadb:
    image: mariadb:10.11
    environment:
      MYSQL_ROOT_PASSWORD: smartcon123
      MYSQL_DATABASE: smartcon_dev
      MYSQL_USER: smartcon
      MYSQL_PASSWORD: smartcon123
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_HOST: mariadb
      REDIS_HOST: redis
    depends_on:
      - mariadb
      - redis

volumes:
  mariadb_data:
```

---

이 백엔드 아키텍처는 SmartCON Lite의 요구사항을 충족하는 확장 가능하고 유지보수가 용이한 구조를 제공합니다. 다음으로 프론트엔드 아키텍처를 작성하겠습니다.