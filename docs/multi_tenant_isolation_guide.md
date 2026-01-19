# SmartCON Lite 멀티테넌트 데이터 격리 구현 가이드 (Hibernate Filter 기반)

**작성자**: Manus AI
**작성일**: 2026년 1월 19일

## 1. 개요 및 격리 전략

SmartCON Lite는 **Shared Database, Shared Schema** 전략을 채택하여 멀티테넌시를 구현합니다. 이는 단일 데이터베이스와 스키마를 사용하되, 모든 비즈니스 테이블에 `tenant_id` 컬럼을 추가하여 데이터를 논리적으로 격리하는 방식입니다.

이 가이드는 Spring Boot 환경에서 **Hibernate Filter**를 사용하여 이 `tenant_id` 기반의 격리 로직을 모든 JPA 쿼리에 자동으로 적용하는 방법을 상세히 설명합니다. 이 방식을 통해 초급 개발자는 비즈니스 로직 코드에서 `WHERE tenant_id = ?` 조건을 수동으로 작성할 필요 없이, 데이터 접근의 안정성과 보안을 확보할 수 있습니다.

| 구분 | 내용 | 구현 기술 |
| :--- | :--- | :--- |
| **격리 전략** | Shared Database, Shared Schema | 논리적 격리 (Logical Isolation) |
| **격리 기준** | `tenant_id` (회사/테넌트 고유 식별자) | Long 타입 |
| **핵심 기술** | Hibernate Filter | 모든 JPA/Hibernate 쿼리에 `WHERE tenant_id = :tenantId` 조건 자동 추가 |
| **컨텍스트 관리** | ThreadLocal | 요청 스레드별 `tenant_id` 저장 및 관리 |

## 2. 핵심 컴포넌트 구현

### 2.1. Tenant ID 컨텍스트 관리 (`TenantContext.java`)

현재 요청을 처리하는 스레드에 `tenant_id`를 안전하게 저장하고 조회하기 위해 `ThreadLocal`을 사용합니다.

```java
// src/main/java/com/smartcon/multitenant/TenantContext.java
package com.smartcon.multitenant;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
```

### 2.2. HTTP 요청 인터셉터 (`TenantIdInterceptor.java`)

모든 HTTP 요청이 들어올 때마다 JWT 토큰에서 `tenant_id`를 추출하여 `TenantContext`에 설정하고, 응답 후에는 반드시 해제합니다.

```java
// src/main/java/com/smartcon/multitenant/TenantIdInterceptor.java
package com.smartcon.multitenant;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TenantIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. JWT 토큰 또는 HTTP 헤더에서 tenant_id 추출 (예시: "X-Tenant-ID" 헤더 사용)
        String tenantIdHeader = request.getHeader("X-Tenant-ID");
        
        if (tenantIdHeader != null) {
            try {
                Long tenantId = Long.parseLong(tenantIdHeader);
                TenantContext.setTenantId(tenantId);
            } catch (NumberFormatException e) {
                // 유효하지 않은 tenant_id 처리 (400 Bad Request 등)
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Tenant ID format.");
                return false;
            }
        } else {
            // tenant_id가 없는 경우 (예: 슈퍼관리자 API 또는 공통 API)
            TenantContext.setTenantId(null);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 2. 요청 처리 완료 후 반드시 ThreadLocal 해제
        TenantContext.clear();
    }
}
```

## 3. Hibernate Filter 설정

### 3.1. 엔티티에 필터 적용 (`@FilterDef`, `@Filter`)

데이터 격리가 필요한 모든 엔티티(예: `Site`, `Worker`, `Contract` 등)에 `@Filter`를 적용합니다.

```java
// src/main/java/com/smartcon/domain/Site.java
package com.smartcon.domain;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = "long"),
    defaultCondition = "tenant_id = :tenantId"
)
@Filter(name = "tenantFilter")
public class Site {

    @Id
    private Long id;

    private Long tenantId; // 필수 컬럼

    private String siteName;
    // ... 기타 필드
    
    // Getter, Setter 생략
}
```

### 3.2. 필터 활성화 및 값 바인딩 (`TenantFilterConfigurer.java`)

JPA 세션이 열릴 때마다 `tenantFilter`를 활성화하고, `TenantContext`에서 가져온 `tenant_id`를 필터 파라미터에 바인딩합니다.

```java
// src/main/java/com/smartcon/multitenant/TenantFilterConfigurer.java
package com.smartcon.multitenant;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class TenantFilterConfigurer {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void enableTenantFilter() {
        Long tenantId = TenantContext.getTenantId();
        
        // tenantId가 null이 아닌 경우에만 필터 활성화
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter")
                   .setParameter("tenantId", tenantId);
        }
    }
}
```

### 3.3. 필터 자동 적용 설정 (`HibernateFilterAspect.java`)

AOP(Aspect-Oriented Programming)를 사용하여 모든 `@Transactional` 메서드 실행 전에 필터 활성화 로직을 삽입합니다.

```java
// src/main/java/com/smartcon/multitenant/HibernateFilterAspect.java
package com.smartcon.multitenant;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HibernateFilterAspect {

    private final TenantFilterConfigurer tenantFilterConfigurer;

    public HibernateFilterAspect(TenantFilterConfigurer tenantFilterConfigurer) {
        this.tenantFilterConfigurer = tenantFilterConfigurer;
    }

    // 모든 서비스 레이어의 트랜잭션 메서드 실행 전에 필터 활성화
    @Before("execution(* com.smartcon.service.*.*(..)) && @annotation(org.springframework.transaction.annotation.Transactional)")
    public void enableFilter() {
        tenantFilterConfigurer.enableTenantFilter();
    }
}
```

## 4. Spring Boot 설정

### 4.1. `application.yml` 설정

Hibernate가 필터 기능을 인식하도록 설정합니다.

```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: smartcon_lite # 스키마 이름
        # Hibernate Filter 기능을 활성화합니다.
        enable_lazy_load_no_trans: true
        # 필터가 적용된 쿼리를 확인할 수 있도록 로깅 레벨을 설정합니다.
        show_sql: true
        format_sql: true
```

### 4.2. WebMvcConfigurer 등록

작성한 `TenantIdInterceptor`를 Spring MVC에 등록하여 모든 요청에 적용합니다.

```java
// src/main/java/com/smartcon/config/WebConfig.java
package com.smartcon.config;

import com.smartcon.multitenant.TenantIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantIdInterceptor())
                .addPathPatterns("/api/v1/**") // API 경로에만 적용
                .excludePathPatterns("/api/v1/public/**"); // 예외 경로 설정
    }
}
```

## 5. 보안 및 테스트 전략

### 5.1. 데이터 무결성 및 보안 강화

| 구분 | 문제점 | 해결 방안 (구현 가이드) |
| :--- | :--- | :--- |
| **데이터 누락 방지** | 엔티티 저장 시 `tenant_id` 누락 가능성 | `@PrePersist` 리스너를 사용하여 엔티티 저장 직전에 `TenantContext.getTenantId()`를 자동 주입하는 로직을 구현합니다. |
| **슈퍼관리자 접근** | `tenant_id`가 없는 전역 관리 API 처리 | `TenantIdInterceptor`에서 슈퍼관리자 API 경로를 `excludePathPatterns`로 명시적으로 제외하고, 해당 서비스 레이어에서는 필터 활성화를 건너뛰도록 `TenantContext.getTenantId() == null` 조건을 추가합니다. |
| **필터 우회 방지** | 네이티브 쿼리 사용 시 필터 미적용 | 개발자는 네이티브 쿼리(Native Query) 사용을 지양하고, 반드시 JPA/Hibernate를 통해 쿼리를 작성하도록 가이드합니다. 네이티브 쿼리 사용이 불가피할 경우, `tenant_id` 조건을 수동으로 추가하도록 코드 리뷰를 강화합니다. |
| **ThreadLocal 누수** | 요청 처리 후 `tenant_id`가 해제되지 않는 문제 | `TenantIdInterceptor.afterCompletion()`에서 `TenantContext.clear()`를 호출하여 요청 완료 후 반드시 `ThreadLocal`을 해제하도록 보장합니다. |

### 5.2. 테스트 케이스 명세

| 테스트 케이스 | 시나리오 | 예상 결과 |
| :--- | :--- | :--- |
| **정상 격리 테스트** | Tenant A의 ID로 요청 후 `siteRepository.findAll()` 호출 | Tenant A에 속한 Site 데이터만 조회되어야 합니다. |
| **교차 접근 테스트** | Tenant A의 ID로 요청 후 Tenant B의 Site ID로 `siteRepository.findById(B_ID)` 호출 | `Optional.empty()`가 반환되거나, `EntityNotFoundException`이 발생해야 합니다. (데이터 접근 차단) |
| **슈퍼관리자 테스트** | `X-Tenant-ID` 헤더 없이 요청 후 전역 데이터 조회 API 호출 | 필터가 비활성화되어 모든 테넌트의 데이터가 조회되어야 합니다. |
| **저장 시 자동 주입** | 새로운 Site 엔티티를 저장할 때 `tenantId` 필드를 명시하지 않음 | `@PrePersist` 리스너에 의해 현재 `TenantContext`의 `tenant_id`가 자동으로 주입되어 저장되어야 합니다. |

## 6. 구현 결과 및 요약

이 구현 방식을 적용하면, 개발자가 `siteRepository.findAll()`을 호출하더라도, Hibernate는 내부적으로 현재 `TenantContext`에 설정된 `tenant_id`를 사용하여 다음과 같은 SQL을 실행합니다.

```sql
SELECT * FROM site WHERE tenant_id = 12345;
```

이 가이드를 통해 초급 개발자도 멀티테넌트 환경에서 안전하고 효율적인 데이터 격리 로직을 구현할 수 있습니다.
