package com.smartcon.global.config;

import com.smartcon.global.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 멀티테넌트 설정 및 Hibernate Filter 관리
 * MariaDB 환경에서 테넌트별 데이터 격리를 위한 설정
 * JPA가 활성화된 경우에만 동작합니다.
 */
@Configuration
@ConditionalOnClass(EntityManager.class)
@RequiredArgsConstructor
@Slf4j
public class MultiTenantConfig implements WebMvcConfigurer {

    private final EntityManager entityManager;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor());
    }

    /**
     * 테넌트 컨텍스트 관리 인터셉터
     */
    public class TenantInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // 슈퍼관리자 API는 테넌트 필터를 적용하지 않음
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/admin/")) {
                log.debug("슈퍼관리자 API 요청 - 테넌트 필터 비활성화: {}", requestURI);
                return true;
            }

            // 테넌트 ID 추출 (헤더, JWT 토큰, 또는 기타 방법으로)
            String tenantIdHeader = request.getHeader("X-Tenant-ID");
            if (tenantIdHeader != null && !tenantIdHeader.trim().isEmpty()) {
                try {
                    Long tenantId = Long.parseLong(tenantIdHeader);
                    TenantContext.setCurrentTenantId(tenantId);
                    enableTenantFilter(tenantId);
                    log.debug("테넌트 필터 활성화: {}", tenantId);
                } catch (NumberFormatException e) {
                    log.warn("유효하지 않은 테넌트 ID 헤더: {}", tenantIdHeader);
                }
            }

            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) {
            // 요청 완료 후 테넌트 컨텍스트 정리
            disableTenantFilter();
            TenantContext.clear();
        }

        /**
         * 테넌트 필터 활성화
         */
        private void enableTenantFilter(Long tenantId) {
            try {
                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
                log.debug("Hibernate 테넌트 필터 활성화: {}", tenantId);
            } catch (Exception e) {
                log.error("테넌트 필터 활성화 실패: {}", tenantId, e);
            }
        }

        /**
         * 테넌트 필터 비활성화
         */
        private void disableTenantFilter() {
            try {
                Session session = entityManager.unwrap(Session.class);
                session.disableFilter("tenantFilter");
                log.debug("Hibernate 테넌트 필터 비활성화");
            } catch (Exception e) {
                log.debug("테넌트 필터 비활성화 중 오류 (정상적일 수 있음): {}", e.getMessage());
            }
        }
    }
}