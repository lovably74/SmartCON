package com.smartcon.global.tenant;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * 모든 테넌트 관련 엔티티의 공통 부모 클래스
 * tenant_id 컬럼을 자동으로 관리하며 멀티테넌트 데이터 격리를 제공
 * MariaDB 최적화된 설정 적용
 */
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public abstract class BaseTenantEntity extends BaseEntity {

    /**
     * 테넌트 ID (멀티테넌트 데이터 격리용)
     * MariaDB BIGINT 타입으로 최적화
     */
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    /**
     * 엔티티 저장 전 테넌트 ID 자동 설정
     */
    @PrePersist
    @Override
    protected void onCreate() {
        // 테넌트 ID가 설정되지 않은 경우 현재 컨텍스트에서 가져옴
        if (this.tenantId == null) {
            Long currentTenantId = TenantContext.getCurrentTenantId();
            if (currentTenantId != null) {
                this.tenantId = currentTenantId;
            }
        }
        super.onCreate();
    }

    /**
     * 테넌트 ID 검증
     * 업데이트 시 테넌트 ID 변경 방지
     */
    @PreUpdate
    protected void onUpdate() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null && !currentTenantId.equals(this.tenantId)) {
            throw new IllegalStateException(
                String.format("테넌트 ID 불일치: 현재=%d, 엔티티=%d", 
                    currentTenantId, this.tenantId)
            );
        }
        super.onUpdate();
    }

    /**
     * 동등성 비교 시 테넌트 ID도 고려
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        
        BaseTenantEntity that = (BaseTenantEntity) obj;
        return tenantId != null && tenantId.equals(that.tenantId);
    }

    /**
     * 해시코드 생성 시 테넌트 ID도 고려
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }

    /**
     * 문자열 표현에 테넌트 ID 포함
     */
    @Override
    public String toString() {
        return String.format("%s{id=%d, tenantId=%d, createdAt=%s, updatedAt=%s}", 
                getClass().getSimpleName(), getId(), tenantId, getCreatedAt(), getUpdatedAt());
    }
}
