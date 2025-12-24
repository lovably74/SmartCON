# Design Document

## Overview

이 설계는 SmartCON Lite 프로젝트에서 프로토타입과 별도의 프로덕션 프론트엔드를 분리하고, 백엔드의 H2 인메모리 데이터베이스를 MariaDB로 전환하는 작업을 다룹니다. 이를 통해 프로덕션 환경에서의 데이터 영속성, 성능, 확장성을 확보하고 프론트엔드와 백엔드의 독립적인 개발 및 배포를 가능하게 합니다.

## Architecture

### 현재 아키텍처
```
SmartCON/
├── prototype/          # React 프로토타입 (포트 3000)
├── backend/           # Spring Boot + H2 (포트 8080)
└── docs/             # 문서
```

### 목표 아키텍처
```
SmartCON/
├── frontend/          # 프로덕션 React 앱 (포트 5173)
├── prototype/         # 프로토타입 (유지, 포트 3000)
├── backend/          # Spring Boot + MariaDB (포트 8080)
├── docs/            # 문서
└── docker/          # Docker 설정
    ├── docker-compose.yml
    └── mariadb/
        ├── init/
        └── data/
```

### 데이터베이스 아키텍처 변경
```
Before: Spring Boot → H2 (In-Memory)
After:  Spring Boot → MariaDB (Persistent)
                   ↓
              Connection Pool (HikariCP)
                   ↓
              Multi-tenant Schema
```

## Components and Interfaces

### 1. 프론트엔드 컴포넌트

#### 1.1 새로운 Frontend 디렉토리 구조
```
frontend/
├── public/
│   ├── manifest.json
│   └── icons/
├── src/
│   ├── components/
│   │   ├── ui/              # Shadcn/UI 컴포넌트
│   │   ├── layout/          # 레이아웃 컴포넌트
│   │   ├── forms/           # 폼 컴포넌트
│   │   └── common/          # 공통 컴포넌트
│   ├── pages/               # 역할별 페이지
│   │   ├── auth/
│   │   ├── super/
│   │   ├── hq/
│   │   ├── site/
│   │   ├── team/
│   │   └── worker/
│   ├── hooks/               # 커스텀 훅
│   ├── stores/              # Zustand 스토어
│   ├── services/            # API 서비스
│   ├── types/               # TypeScript 타입
│   ├── utils/               # 유틸리티
│   ├── styles/              # 스타일
│   └── assets/              # 정적 자산
├── package.json
├── vite.config.ts
├── tailwind.config.js
├── tsconfig.json
└── .env.example
```

#### 1.2 환경별 설정
```typescript
// frontend/src/config/environment.ts
interface Environment {
  API_BASE_URL: string;
  NODE_ENV: 'development' | 'staging' | 'production';
  ENABLE_DEVTOOLS: boolean;
}

const environments: Record<string, Environment> = {
  development: {
    API_BASE_URL: 'http://localhost:8080/api',
    NODE_ENV: 'development',
    ENABLE_DEVTOOLS: true,
  },
  staging: {
    API_BASE_URL: 'https://api-staging.smartcon.kr/api',
    NODE_ENV: 'staging',
    ENABLE_DEVTOOLS: false,
  },
  production: {
    API_BASE_URL: 'https://api.smartcon.kr/api',
    NODE_ENV: 'production',
    ENABLE_DEVTOOLS: false,
  },
};
```

### 2. 백엔드 컴포넌트

#### 2.1 MariaDB 설정 구조
```yaml
# backend/src/main/resources/application.yml
spring:
  profiles:
    active: local
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

#### 2.2 환경별 데이터베이스 설정
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/smartcon_local
    username: smartcon_user
    password: smartcon_pass
    driver-class-name: org.mariadb.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

# application-dev.yml
spring:
  datasource:
    url: jdbc:mariadb://mariadb-dev:3306/smartcon_dev
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

# application-prod.yml
spring:
  datasource:
    url: jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 3. Docker 컴포넌트

#### 3.1 로컬 개발용 Docker Compose
```yaml
# docker/docker-compose.yml
version: '3.8'
services:
  mariadb:
    image: mariadb:10.11
    container_name: smartcon-mariadb
    environment:
      MYSQL_ROOT_PASSWORD: smartcon_root
      MYSQL_DATABASE: smartcon_local
      MYSQL_USER: smartcon_user
      MYSQL_PASSWORD: smartcon_pass
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./mariadb/init:/docker-entrypoint-initdb.d
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    container_name: smartcon-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  mariadb_data:
  redis_data:
```

## Data Models

### 1. Flyway 마이그레이션 구조

#### 1.1 마이그레이션 파일 구조
```
backend/src/main/resources/db/migration/
├── V1__Create_initial_schema.sql
├── V2__Create_indexes.sql
├── V3__Insert_initial_data.sql
└── V4__Add_constraints.sql
```

#### 1.2 주요 테이블 스키마 (MariaDB 최적화)
```sql
-- V1__Create_initial_schema.sql
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_number VARCHAR(12) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    status ENUM('TRIAL', 'ACTIVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'TRIAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenants_business_number (business_number),
    INDEX idx_tenants_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE KEY unique_email_per_tenant (tenant_id, email),
    INDEX idx_users_tenant_id (tenant_id),
    INDEX idx_users_provider (provider, provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. JPA 엔티티 MariaDB 최적화

#### 2.1 Base Entity 수정
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // getters, setters
}
```

#### 2.2 Multi-tenant Entity 수정
```java
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity extends BaseEntity {
    
    @Column(name = "tenant_id", nullable = false)
    @Index(name = "idx_tenant_id")
    private Long tenantId;
    
    @PrePersist
    @PreUpdate
    public void setTenantId() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getCurrentTenantId();
        }
    }
}
```

## Error Handling

### 1. 데이터베이스 연결 오류 처리

#### 1.1 Connection Pool 모니터링
```java
@Component
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "MariaDB")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            return Health.down()
                .withDetail("database", "MariaDB")
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}
```

#### 1.2 Flyway 마이그레이션 오류 처리
```java
@Component
@Slf4j
public class FlywayMigrationValidator {
    
    @Autowired
    private Flyway flyway;
    
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        try {
            MigrationInfo[] migrations = flyway.info().all();
            for (MigrationInfo migration : migrations) {
                if (migration.getState() == MigrationState.FAILED) {
                    log.error("Migration failed: {}", migration.getDescription());
                    throw new RuntimeException("Database migration failed");
                }
            }
            log.info("All database migrations completed successfully");
        } catch (Exception e) {
            log.error("Migration validation failed", e);
            throw new RuntimeException("Database migration validation failed", e);
        }
    }
}
```

### 2. 프론트엔드 API 오류 처리

#### 2.1 API 클라이언트 오류 처리
```typescript
// frontend/src/services/api/client.ts
class ApiClient {
  private handleError(error: AxiosError): never {
    if (error.response?.status === 500) {
      // 데이터베이스 연결 오류 등 서버 오류
      throw new ApiError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', 500);
    }
    
    if (error.response?.status === 503) {
      // 데이터베이스 마이그레이션 중 등
      throw new ApiError('시스템 점검 중입니다. 잠시 후 다시 시도해주세요.', 503);
    }
    
    throw new ApiError(error.message, error.response?.status || 0);
  }
}
```

## Testing Strategy

### 1. 백엔드 테스트 전략

#### 1.1 Testcontainers를 활용한 MariaDB 테스트
```java
@Testcontainers
@SpringBootTest
class MariaDBIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.11")
            .withDatabaseName("smartcon_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withCharset("utf8mb4")
            .withCollation("utf8mb4_unicode_ci");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
        registry.add("spring.datasource.driver-class-name", mariaDB::getDriverClassName);
    }
    
    @Test
    void testDatabaseConnection() {
        assertTrue(mariaDB.isRunning());
        assertTrue(mariaDB.isCreated());
    }
}
```

#### 1.2 Repository 테스트
```java
@DataJpaTest
@Testcontainers
class TenantRepositoryTest {
    
    @Container
    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.11");
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Test
    void testFindByBusinessNumber() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setBusinessNumber("123-45-67890");
        tenant.setCompanyName("테스트 회사");
        entityManager.persistAndFlush(tenant);
        
        // When
        Optional<Tenant> found = tenantRepository.findByBusinessNumber("123-45-67890");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("테스트 회사", found.get().getCompanyName());
    }
}
```

### 2. 프론트엔드 테스트 전략

#### 2.1 컴포넌트 테스트
```typescript
// frontend/src/components/__tests__/LoginForm.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { LoginForm } from '../forms/LoginForm';

describe('LoginForm', () => {
  let queryClient: QueryClient;
  
  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
  });
  
  it('should submit form with valid data', async () => {
    const mockOnSubmit = jest.fn();
    
    render(
      <QueryClientProvider client={queryClient}>
        <LoginForm onSubmit={mockOnSubmit} />
      </QueryClientProvider>
    );
    
    fireEvent.change(screen.getByLabelText('사업자번호'), {
      target: { value: '123-45-67890' }
    });
    fireEvent.change(screen.getByLabelText('비밀번호'), {
      target: { value: 'password123!' }
    });
    
    fireEvent.click(screen.getByRole('button', { name: '로그인' }));
    
    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        businessNumber: '123-45-67890',
        password: 'password123!'
      });
    });
  });
});
```

#### 2.2 API 서비스 테스트
```typescript
// frontend/src/services/__tests__/authService.test.ts
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import { authService } from '../authService';

const server = setupServer(
  rest.post('/api/auth/login', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: {
          user: { id: 1, name: '테스트 사용자' },
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token'
        }
      })
    );
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('AuthService', () => {
  it('should login successfully', async () => {
    const result = await authService.login({
      businessNumber: '123-45-67890',
      password: 'password123!'
    });
    
    expect(result.success).toBe(true);
    expect(result.data.user.name).toBe('테스트 사용자');
    expect(result.data.accessToken).toBe('mock-access-token');
  });
});
```

### 3. E2E 테스트 전략

#### 3.1 Playwright 설정
```typescript
// frontend/tests/e2e/login.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });
  
  test('should login with valid credentials', async ({ page }) => {
    await page.click('text=로그인');
    await page.fill('[data-testid=business-number]', '123-45-67890');
    await page.fill('[data-testid=password]', 'password123!');
    await page.click('[data-testid=login-button]');
    
    await expect(page).toHaveURL('/hq/dashboard');
    await expect(page.locator('text=대시보드')).toBeVisible();
  });
});
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Frontend Technology Stack Consistency
*For any* frontend project configuration, the package.json should contain React 18+, TypeScript 5+, Vite 5+, Zustand, and TanStack Query with compatible versions
**Validates: Requirements 1.2, 1.5**

### Property 2: Frontend UI Component Completeness
*For any* required UI component from Shadcn/UI library, it should be importable and functional in the frontend application
**Validates: Requirements 1.3**

### Property 3: Role-based Routing Preservation
*For any* user role (super, hq, site, team, worker), all expected routes should be accessible and properly configured in the frontend routing system
**Validates: Requirements 1.4**

### Property 4: Frontend Production Build Optimization
*For any* production build output, the generated files should be minified, tree-shaken, and optimized for deployment
**Validates: Requirements 1.6**

### Property 5: Entity-MariaDB Compatibility
*For any* JPA entity in the system, it should be persistable and retrievable correctly when using MariaDB as the database
**Validates: Requirements 2.2**

### Property 6: Flyway Migration Execution
*For any* Flyway migration script, it should execute successfully and create the expected database schema changes
**Validates: Requirements 2.3**

### Property 7: Multi-tenant Data Isolation
*For any* tenant-specific data operation, the data should be properly isolated and not accessible to other tenants
**Validates: Requirements 2.6**

### Property 8: Build Configuration Independence
*For any* build process (frontend or backend), it should execute successfully without dependencies on the other component's build process
**Validates: Requirements 4.4**

### Property 9: API Backward Compatibility
*For any* existing API endpoint, it should continue to function correctly with the same request/response format after the migration
**Validates: Requirements 4.5**

### Property 10: Database Schema Migration Completeness
*For any* table or index that existed in H2, an equivalent structure should be created in MariaDB after migration
**Validates: Requirements 5.2**

### Property 11: JPA Entity MariaDB Validation
*For any* JPA entity operation (create, read, update, delete), it should work correctly with MariaDB maintaining data integrity
**Validates: Requirements 5.4**

### Property 12: Database Referential Integrity
*For any* foreign key relationship in the database, the referential integrity constraints should be properly enforced in MariaDB
**Validates: Requirements 5.5**

### Property 13: Environment-specific Database Connectivity
*For any* environment configuration (local, dev, prod), the backend should connect to the appropriate MariaDB instance based on the active profile
**Validates: Requirements 6.2**

### Property 14: Frontend Environment Configuration
*For any* environment setting (development, staging, production), the frontend should use the correct API endpoints and configuration values
**Validates: Requirements 6.3**

### Property 15: Test Database Isolation
*For any* test execution, each test should use an isolated MariaDB instance without affecting other concurrent tests
**Validates: Requirements 7.2**

### Property 16: Test Suite Preservation
*For any* existing unit or integration test, it should continue to pass successfully with the new MariaDB configuration
**Validates: Requirements 7.3**

## 프로젝트 마이그레이션 단계별 계획

### Phase 1: 백엔드 MariaDB 전환
1. MariaDB 의존성 추가 및 설정
2. Flyway 마이그레이션 스크립트 작성
3. 환경별 설정 파일 구성
4. Docker Compose 로컬 환경 구성
5. 테스트 환경 Testcontainers 설정

### Phase 2: 프론트엔드 분리
1. 새로운 frontend 디렉토리 생성
2. 프로토타입에서 소스 코드 복사 및 정리
3. 환경별 설정 구성
4. 빌드 및 배포 설정 최적화
5. API 클라이언트 환경별 설정

### Phase 3: 통합 테스트 및 검증
1. 백엔드 MariaDB 연동 테스트
2. 프론트엔드 API 연동 테스트
3. E2E 테스트 실행
4. 성능 테스트 및 최적화
5. 문서 업데이트