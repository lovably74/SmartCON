package com.smartcon.domain.user.repository;

import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 데이터 접근 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 전체 사용자 수 조회
     */
    long count();

    /**
     * 특정 기간 내 생성된 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 테넌트별 사용자 수 조회
     */
    long countByTenantId(Long tenantId);
    
    /**
     * 테넌트별 사용자 목록 조회
     */
    List<User> findByTenantId(Long tenantId);
    
    /**
     * 슈퍼관리자 목록 조회 (임시로 모든 사용자 반환 - 실제로는 역할 기반 필터링 필요)
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findSuperAdmins();

    /**
     * 이메일로 사용자 조회
     */
    java.util.Optional<User> findByEmail(String email);

    /**
     * 이메일과 테넌트 ID로 사용자 조회
     */
    java.util.Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    /**
     * CI값으로 사용자 조회 (임베디드 객체)
     */
    @Query("SELECT u FROM User u WHERE u.ciValue.value = :ciValue")
    java.util.Optional<User> findByCiValueValue(@Param("ciValue") String ciValue);

    /**
     * CI값으로 사용자 조회 (하위 호환성)
     */
    @Deprecated
    default java.util.Optional<User> findByCiValue(String ciValue) {
        return findByCiValueValue(ciValue);
    }

    /**
     * 사업자번호로 사용자 조회
     */
    java.util.Optional<User> findByBusinessNumber(String businessNumber);

    /**
     * 역할별 사용자 조회 (다중 역할 지원)
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    /**
     * 테넌트 ID와 역할로 사용자 조회 (다중 역할 지원)
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.tenantId = :tenantId AND r = :role")
    List<User> findByTenantIdAndRole(@Param("tenantId") Long tenantId, @Param("role") Role role);
}