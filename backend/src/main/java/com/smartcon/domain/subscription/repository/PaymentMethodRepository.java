package com.smartcon.domain.subscription.repository;

import com.smartcon.domain.subscription.entity.PaymentMethod;
import com.smartcon.domain.subscription.entity.PaymentType;
import com.smartcon.domain.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 결제 수단 Repository
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    
    /**
     * 테넌트의 활성 결제 수단 목록 조회
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.tenant = :tenant AND pm.isActive = true ORDER BY pm.isDefault DESC, pm.createdAt DESC")
    List<PaymentMethod> findActiveByTenant(@Param("tenant") Tenant tenant);
    
    /**
     * 테넌트의 기본 결제 수단 조회
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.tenant = :tenant AND pm.isDefault = true AND pm.isActive = true")
    Optional<PaymentMethod> findDefaultByTenant(@Param("tenant") Tenant tenant);
    
    /**
     * 테넌트의 특정 타입 결제 수단 목록 조회
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.tenant = :tenant AND pm.type = :type AND pm.isActive = true")
    List<PaymentMethod> findByTenantAndType(@Param("tenant") Tenant tenant, @Param("type") PaymentType type);
    
    /**
     * 빌링키로 결제 수단 조회
     */
    Optional<PaymentMethod> findByBillingKeyAndIsActiveTrue(String billingKey);
    
    /**
     * 테넌트의 활성 결제 수단 개수 조회
     */
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.tenant = :tenant AND pm.isActive = true")
    long countActiveByTenant(@Param("tenant") Tenant tenant);
}