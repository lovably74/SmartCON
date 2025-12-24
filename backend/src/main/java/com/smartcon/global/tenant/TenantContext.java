package com.smartcon.global.tenant;

import lombok.extern.slf4j.Slf4j;

/**
 * ThreadLocal을 사용하여 현재 요청의 테넌트 식별자를 관리하는 클래스
 * MariaDB 최적화를 위해 Long 타입 사용
 */
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();

    /**
     * 현재 스레드의 테넌트 ID 설정
     * @param tenantId 테넌트 ID (Long 타입)
     */
    public static void setCurrentTenantId(Long tenantId) {
        if (tenantId != null && tenantId <= 0) {
            throw new IllegalArgumentException("테넌트 ID는 양수여야 합니다: " + tenantId);
        }
        CURRENT_TENANT_ID.set(tenantId);
        log.debug("테넌트 컨텍스트 설정: {}", tenantId);
    }

    /**
     * 현재 스레드의 테넌트 ID 조회
     * @return 테넌트 ID (Long 타입, null 가능)
     */
    public static Long getCurrentTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    /**
     * 현재 스레드의 테넌트 컨텍스트 정리
     */
    public static void clear() {
        Long tenantId = CURRENT_TENANT_ID.get();
        CURRENT_TENANT_ID.remove();
        log.debug("테넌트 컨텍스트 정리: {}", tenantId);
    }

    /**
     * 테넌트 컨텍스트가 설정되어 있는지 확인
     * @return 테넌트 ID가 설정되어 있으면 true
     */
    public static boolean isSet() {
        return CURRENT_TENANT_ID.get() != null;
    }

    /**
     * 슈퍼관리자 여부 확인 (테넌트 ID가 없는 경우)
     * @return 슈퍼관리자면 true
     */
    public static boolean isSuperAdmin() {
        return CURRENT_TENANT_ID.get() == null;
    }

    /**
     * 현재 테넌트 ID를 문자열로 반환 (하위 호환성)
     * @return 테넌트 ID 문자열 (null이면 null 반환)
     * @deprecated Long 타입의 getCurrentTenantId() 사용 권장
     */
    @Deprecated
    public static String getCurrentTenant() {
        Long tenantId = getCurrentTenantId();
        return tenantId != null ? tenantId.toString() : null;
    }

    /**
     * 테넌트 ID를 문자열로 설정 (하위 호환성)
     * @param tenantId 테넌트 ID 문자열
     * @deprecated Long 타입의 setCurrentTenantId() 사용 권장
     */
    @Deprecated
    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            setCurrentTenantId(null);
        } else {
            try {
                setCurrentTenantId(Long.parseLong(tenantId));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("유효하지 않은 테넌트 ID 형식: " + tenantId, e);
            }
        }
    }
}
