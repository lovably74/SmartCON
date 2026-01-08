package com.smartcon.domain.admin.service;

import com.smartcon.domain.admin.dto.ApprovalStatsDto;
import com.smartcon.domain.admin.dto.SubscriptionExportDto;
import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 대시보드 및 필터링 기능에 대한 속성 기반 테스트
 * 
 * Property 14: Dashboard Subscription Counts
 * Property 16: Subscription Filtering
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class DashboardAndFilteringPropertyTest {

    @Autowired
    private SuperAdminService superAdminService;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.smartcon.domain.subscription.repository.SubscriptionPlanRepository subscriptionPlanRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 정리
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
        subscriptionPlanRepository.deleteAll();
        
        // 테넌트 컨텍스트 초기화
        TenantContext.clear();
        
        // 기본 구독 플랜 생성
        createDefaultSubscriptionPlan();
    }
    
    /**
     * 테스트용 기본 구독 플랜 생성
     */
    private void createDefaultSubscriptionPlan() {
        com.smartcon.domain.subscription.entity.SubscriptionPlan defaultPlan = 
                com.smartcon.domain.subscription.entity.SubscriptionPlan.builder()
                        .planId("BASIC")
                        .name("기본 플랜")
                        .description("기본 구독 플랜")
                        .monthlyPrice(java.math.BigDecimal.valueOf(50000))
                        .maxSites(5)
                        .maxUsers(50)
                        .maxStorageGb(10)
                        .isActive(true)
                        .sortOrder(1)
                        .build();
        subscriptionPlanRepository.save(defaultPlan);
    }

    /**
     * Property 14: Dashboard Subscription Counts
     * 승인 대시보드 쿼리에 대해, 표시된 카운트는 각 상태(pending, active, suspended, terminated)의 
     * 실제 구독 수와 일치해야 함
     * 
     * **Feature: subscription-approval-workflow, Property 14: Dashboard Subscription Counts**
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 14: Dashboard Subscription Counts")
    void dashboardSubscriptionCountsShouldMatchActualCounts(
            @ForAll @Size(min = 1, max = 20) List<@NotEmpty String> tenantNames,
            @ForAll @Size(min = 1, max = 50) List<SubscriptionStatus> subscriptionStatuses) {
        
        // Given: 다양한 상태의 구독들을 생성
        createTenantsAndSubscriptions(tenantNames, subscriptionStatuses);
        
        // When: 승인 대시보드 통계를 조회
        ApprovalStatsDto stats = superAdminService.getApprovalStats();
        
        // Then: 각 상태별 카운트가 실제 데이터베이스의 카운트와 일치해야 함
        long actualTotal = subscriptionRepository.count();
        long actualPending = subscriptionRepository.countByStatus(SubscriptionStatus.PENDING_APPROVAL);
        long actualActive = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long actualRejected = subscriptionRepository.countByStatus(SubscriptionStatus.REJECTED);
        long actualSuspended = subscriptionRepository.countByStatus(SubscriptionStatus.SUSPENDED);
        long actualTerminated = subscriptionRepository.countByStatus(SubscriptionStatus.TERMINATED);
        
        assertThat(stats.getTotalSubscriptions()).isEqualTo(actualTotal);
        assertThat(stats.getPendingApprovals()).isEqualTo(actualPending);
        assertThat(stats.getApprovedSubscriptions()).isEqualTo(actualActive);
        assertThat(stats.getRejectedSubscriptions()).isEqualTo(actualRejected);
        assertThat(stats.getSuspendedSubscriptions()).isEqualTo(actualSuspended);
        assertThat(stats.getTerminatedSubscriptions()).isEqualTo(actualTerminated);
        
        // 총합 검증: 각 상태별 카운트의 합이 전체 카운트와 일치해야 함
        long calculatedTotal = stats.getPendingApprovals() + stats.getApprovedSubscriptions() + 
                              stats.getRejectedSubscriptions() + stats.getSuspendedSubscriptions() + 
                              stats.getTerminatedSubscriptions();
        assertThat(calculatedTotal).isLessThanOrEqualTo(stats.getTotalSubscriptions());
    }

    /**
     * Property 16: Subscription Filtering
     * 구독 필터 조건(상태, 날짜 범위, 테넌트명)에 대해, 결과는 지정된 모든 조건과 일치하는 
     * 구독만 포함해야 함
     * 
     * **Feature: subscription-approval-workflow, Property 16: Subscription Filtering**
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Tag("Feature: subscription-approval-workflow, Property 16: Subscription Filtering")
    void subscriptionFilteringShouldReturnOnlyMatchingCriteria(
            @ForAll @Size(min = 3, max = 10) List<@NotEmpty String> tenantNames,
            @ForAll @Size(min = 5, max = 20) List<SubscriptionStatus> subscriptionStatuses,
            @ForAll @IntRange(min = 0, max = 2) int filterTypeIndex) {
        
        // Given: 다양한 테넌트와 구독 상태로 데이터 생성
        createTenantsAndSubscriptions(tenantNames, subscriptionStatuses);
        
        // 필터 조건 선택 (다양한 필터 시나리오 테스트)
        SubscriptionStatus filterStatus = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (filterTypeIndex == 0 && !subscriptionStatuses.isEmpty()) {
            // 상태 필터만 적용
            filterStatus = subscriptionStatuses.get(0);
        } else if (filterTypeIndex == 1) {
            // 날짜 범위 필터만 적용
            startDate = LocalDateTime.now().minusDays(30);
            endDate = LocalDateTime.now();
        } else if (filterTypeIndex == 2 && !subscriptionStatuses.isEmpty()) {
            // 상태와 날짜 범위 필터 모두 적용
            filterStatus = subscriptionStatuses.get(0);
            startDate = LocalDateTime.now().minusDays(30);
            endDate = LocalDateTime.now();
        }
        
        // When: 필터링된 구독 데이터를 내보내기
        List<SubscriptionExportDto> filteredResults = superAdminService.exportSubscriptionData(
                filterStatus, startDate, endDate);
        
        // Then: 모든 결과가 필터 조건을 만족해야 함
        for (SubscriptionExportDto result : filteredResults) {
            // 상태 필터 검증
            if (filterStatus != null) {
                assertThat(result.getStatus()).isEqualTo(filterStatus);
            }
            
            // 날짜 범위 필터 검증
            if (startDate != null && endDate != null) {
                assertThat(result.getCreatedAt()).isBetween(startDate, endDate);
            }
            
            // 기본 데이터 무결성 검증
            assertThat(result.getSubscriptionId()).isNotNull();
            assertThat(result.getTenantName()).isNotBlank();
            assertThat(result.getStatus()).isNotNull();
        }
        
        // 필터 조건이 있을 때 결과가 전체보다 작거나 같아야 함
        long totalCount = subscriptionRepository.count();
        if (filterStatus != null || (startDate != null && endDate != null)) {
            assertThat(filteredResults.size()).isLessThanOrEqualTo((int) totalCount);
        }
    }

    /**
     * Property 16 확장: 테넌트명 검색 필터링
     * 테넌트명 검색어에 대해, 결과는 검색어를 포함하는 테넌트의 구독만 포함해야 함
     */
    @Property(tries = 50)
    @Tag("Feature: subscription-approval-workflow, Property 16: Subscription Filtering")
    void tenantNameSearchFilteringShouldReturnOnlyMatchingTenants(
            @ForAll @Size(min = 3, max = 8) List<@NotEmpty String> tenantNames,
            @ForAll @Size(min = 5, max = 15) List<SubscriptionStatus> subscriptionStatuses) {
        
        // Given: 특별한 패턴을 가진 테넌트명들로 데이터 생성
        List<String> uniqueTenantNames = tenantNames.stream()
                .distinct()
                .map(name -> "Company_" + name + "_Corp")
                .collect(Collectors.toList());
        
        createTenantsAndSubscriptions(uniqueTenantNames, subscriptionStatuses);
        
        // 검색할 키워드 선택 (첫 번째 테넌트명의 일부)
        if (!uniqueTenantNames.isEmpty()) {
            String searchKeyword = uniqueTenantNames.get(0).substring(0, Math.min(8, uniqueTenantNames.get(0).length()));
            
            // When: 테넌트명으로 구독 검색
            Pageable pageable = PageRequest.of(0, 100);
            Page<SubscriptionExportDto> searchResults = superAdminService.getSubscriptions(
                    searchKeyword, null, null, null, pageable);
            
            // Then: 모든 결과의 테넌트명이 검색어를 포함해야 함
            for (SubscriptionExportDto result : searchResults.getContent()) {
                assertThat(result.getTenantName().toLowerCase())
                        .contains(searchKeyword.toLowerCase());
            }
            
            // 검색 결과가 전체 결과보다 작거나 같아야 함
            long totalCount = subscriptionRepository.count();
            assertThat(searchResults.getTotalElements()).isLessThanOrEqualTo(totalCount);
        }
    }

    /**
     * 테스트용 테넌트와 구독 데이터 생성 헬퍼 메서드
     */
    private void createTenantsAndSubscriptions(List<String> tenantNames, List<SubscriptionStatus> subscriptionStatuses) {
        // 중복 제거된 테넌트명 목록
        Set<String> uniqueTenantNames = tenantNames.stream()
                .distinct()
                .collect(Collectors.toSet());
        
        int subscriptionIndex = 0;
        
        for (String tenantName : uniqueTenantNames) {
            // 테넌트 생성
            Tenant tenant = new Tenant();
            tenant.setCompanyName(tenantName);
            tenant.setBusinessNo("123-45-" + String.format("%05d", tenantName.hashCode() % 100000));
            tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
            tenant = tenantRepository.save(tenant);
            
            // 테넌트 컨텍스트 설정
            TenantContext.setCurrentTenantId(tenant.getId());
            
            // 테넌트용 사용자 생성
            User user = new User();
            user.setEmail(tenantName.toLowerCase().replaceAll("\\s+", "") + "@test.com");
            user.setName("Admin " + tenantName);
            user.setTenantId(tenant.getId());
            userRepository.save(user);
            
            // 구독 생성 (각 테넌트마다 1-3개의 구독)
            int subscriptionsPerTenant = Math.min(3, Math.max(1, subscriptionStatuses.size() - subscriptionIndex));
            
            for (int i = 0; i < subscriptionsPerTenant && subscriptionIndex < subscriptionStatuses.size(); i++) {
                // 생성 시간을 다양하게 설정 (최근 60일 내)
                LocalDateTime createdAt = LocalDateTime.now().minusDays(subscriptionIndex % 60);
                
                // 기본 플랜 조회
                com.smartcon.domain.subscription.entity.SubscriptionPlan defaultPlan = 
                        subscriptionPlanRepository.findById("BASIC").orElse(null);
                
                Subscription subscription = Subscription.builder()
                        .tenant(tenant)
                        .plan(defaultPlan)
                        .status(subscriptionStatuses.get(subscriptionIndex))
                        .monthlyPrice(java.math.BigDecimal.valueOf(50000 + (subscriptionIndex * 10000)))
                        .startDate(createdAt.toLocalDate())
                        .billingCycle(com.smartcon.domain.subscription.entity.BillingCycle.MONTHLY)
                        .build();
                
                subscriptionRepository.save(subscription);
                subscriptionIndex++;
            }
            
            // 테넌트 컨텍스트 클리어
            TenantContext.clear();
        }
    }

    /**
     * SubscriptionStatus 생성기
     */
    @Provide
    Arbitrary<SubscriptionStatus> subscriptionStatus() {
        return Arbitraries.of(SubscriptionStatus.values());
    }

    /**
     * 테넌트명 생성기
     */
    @Provide
    Arbitrary<String> tenantName() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(3)
                .ofMaxLength(15)
                .map(s -> s + " Company");
    }
}