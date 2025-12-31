package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.AutoApprovalRuleDto;
import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.entity.AutoApprovalRule;
import com.smartcon.domain.subscription.entity.BillingCycle;
import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import com.smartcon.domain.subscription.repository.AutoApprovalRuleRepository;
import com.smartcon.domain.subscription.repository.SubscriptionPlanRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.tenant.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 자동 승인 규칙 속성 기반 테스트
 * 
 * Feature: subscription-approval-workflow
 * Property 27: Auto-Approval Rule Configuration
 * Property 28: Auto-Approval Processing and Logging
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutoApprovalRulePropertyTest {
    
    @Mock
    private AutoApprovalRuleRepository autoApprovalRuleRepository;
    
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    
    @Mock
    private TenantRepository tenantRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private AutoApprovalRuleServiceImpl autoApprovalRuleService;
    
    private Tenant testTenant;
    private SubscriptionPlan testPlan;
    
    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 테넌트 생성
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setCompanyName("테스트 회사");
        testTenant.setBusinessNo("123-45-67890");
        testTenant.setEmail("test@example.com");
        testTenant.setPhoneNumber("010-1234-5678");
        testTenant.setRoadAddress("서울시 강남구");
        
        // 테넌트 컨텍스트 설정
        TenantContext.setCurrentTenantId(testTenant.getId());
        
        // 테스트용 구독 요금제 생성
        testPlan = SubscriptionPlan.builder()
                .planId("BASIC")
                .name("기본 요금제")
                .description("기본 기능을 제공하는 요금제")
                .monthlyPrice(new BigDecimal("50000"))
                .maxSites(5)
                .maxUsers(50)
                .isActive(true)
                .sortOrder(1)
                .build();
        
        // Mock 설정
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(subscriptionPlanRepository.findById("BASIC")).thenReturn(Optional.of(testPlan));
        
        // ObjectMapper mock 설정 - JSON 파싱 결과 반환
        when(objectMapper.readValue(eq("[\"BASIC\"]"), any(TypeReference.class)))
                .thenReturn(List.of("BASIC"));
        when(objectMapper.readValue(eq("[\"CARD\"]"), any(TypeReference.class)))
                .thenReturn(List.of("CARD"));
        when(objectMapper.readValue(eq("[\"CARD\", \"BANK_TRANSFER\"]"), any(TypeReference.class)))
                .thenReturn(List.of("CARD", "BANK_TRANSFER"));
        
        // JSON 직렬화 mock 설정
        when(objectMapper.writeValueAsString(List.of("BASIC"))).thenReturn("[\"BASIC\"]");
        when(objectMapper.writeValueAsString(List.of("CARD"))).thenReturn("[\"CARD\"]");
        when(objectMapper.writeValueAsString(List.of("CARD", "BANK_TRANSFER"))).thenReturn("[\"CARD\", \"BANK_TRANSFER\"]");
    }
    
    private AutoApprovalRule createTestRule() {
        return AutoApprovalRule.builder()
                .ruleName("테스트 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
    }
    
    /**
     * Property 27: Auto-Approval Rule Configuration
     * 자동 승인 규칙 설정 기능 테스트
     * 
     * **Validates: Requirements 7.1**
     */
    @Test
    void autoApprovalRuleConfigurationTest() throws Exception {
        // Given: Mock 설정
        AutoApprovalRule mockRule = AutoApprovalRule.builder()
                .ruleName("테스트 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\", \"BANK_TRANSFER\"]")
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        when(autoApprovalRuleRepository.save(any(AutoApprovalRule.class))).thenReturn(mockRule);
        when(autoApprovalRuleRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockRule)));
        
        // Given: 유효한 자동 승인 규칙 설정
        AutoApprovalRuleDto ruleDto = AutoApprovalRuleDto.builder()
                .ruleName("테스트 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD", "BANK_TRANSFER"))
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        // When: 자동 승인 규칙을 생성
        AutoApprovalRuleDto createdRule = autoApprovalRuleService.createRule(ruleDto);
        
        // Then: 규칙이 올바르게 생성되고 설정된 조건들이 저장되어야 함
        assertThat(createdRule).isNotNull();
        assertThat(createdRule.getRuleName()).isEqualTo("테스트 규칙");
        assertThat(createdRule.getIsActive()).isTrue();
        assertThat(createdRule.getPlanIds()).containsExactly(testPlan.getPlanId());
        assertThat(createdRule.getVerifiedTenantsOnly()).isFalse();
        assertThat(createdRule.getPaymentMethods()).containsExactly("CARD", "BANK_TRANSFER");
        assertThat(createdRule.getMaxAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(createdRule.getPriority()).isEqualTo(1);
        
        // And: 데이터베이스에서 조회 가능해야 함
        Page<AutoApprovalRuleDto> allRules = autoApprovalRuleService.getAllRules(PageRequest.of(0, 10));
        assertThat(allRules.getContent()).hasSize(1);
    }
    
    /**
     * Property 28: Auto-Approval Processing and Logging
     * 자동 승인 처리 및 로깅 기능 테스트
     * 
     * **Validates: Requirements 7.2, 7.3**
     */
    @Test
    void autoApprovalProcessingAndLoggingTest() throws Exception {
        // Given: Mock 설정
        AutoApprovalRule mockRule = AutoApprovalRule.builder()
                .ruleName("테스트 자동 승인 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("100000")) // 테스트 요금제 가격(50000)보다 큰 값
                .priority(1)
                .build();
        
        when(autoApprovalRuleRepository.save(any(AutoApprovalRule.class))).thenReturn(mockRule);
        when(autoApprovalRuleRepository.findByIsActiveTrueOrderByPriorityDescIdAsc())
                .thenReturn(List.of(mockRule));
        
        // Given: 자동 승인 시스템이 활성화되어 있고
        autoApprovalRuleService.toggleAutoApprovalSystem(true);
        
        // And: 활성화된 자동 승인 규칙이 존재함
        AutoApprovalRuleDto ruleDto = AutoApprovalRuleDto.builder()
                .ruleName("테스트 자동 승인 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        autoApprovalRuleService.createRule(ruleDto);
        
        // And: 구독 요청이 자동 승인 조건을 만족함
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId(testPlan.getPlanId());
        request.setBillingCycle(BillingCycle.MONTHLY);
        request.setAutoRenewal(true);
        
        // When: 자동 승인 여부를 평가
        boolean shouldAutoApprove = autoApprovalRuleService.evaluateAutoApproval(request);
        
        // Then: 요금제 가격이 최대 금액 이하이므로 자동 승인되어야 함
        assertThat(shouldAutoApprove).isTrue();
        
        // And: 적용된 규칙을 조회할 수 있어야 함
        AutoApprovalRuleDto appliedRule = autoApprovalRuleService.getAppliedRule(request);
        assertThat(appliedRule).isNotNull();
        assertThat(appliedRule.getRuleName()).isEqualTo("테스트 자동 승인 규칙");
    }
    
    /**
     * 자동 승인 시스템 비활성화 시 모든 요청이 수동 승인으로 처리되는지 테스트
     */
    @Test
    void autoApprovalDisableFunctionalityTest() throws Exception {
        // Given: 자동 승인 시스템이 비활성화되어 있음
        autoApprovalRuleService.toggleAutoApprovalSystem(false);
        
        // When: 구독 요청을 평가
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId(testPlan.getPlanId());
        request.setBillingCycle(BillingCycle.MONTHLY);
        
        boolean shouldAutoApprove = autoApprovalRuleService.evaluateAutoApproval(request);
        
        // Then: 자동 승인 시스템이 비활성화되어 있으므로 자동 승인되지 않아야 함
        assertThat(shouldAutoApprove).isFalse();
        
        // And: 적용된 규칙이 없어야 함
        AutoApprovalRuleDto appliedRule = autoApprovalRuleService.getAppliedRule(request);
        assertThat(appliedRule).isNull();
    }
    
    /**
     * 우선순위에 따른 규칙 적용 순서 테스트
     */
    @Test
    void ruleApplicationPriorityTest() throws Exception {
        // Given: Mock 설정
        AutoApprovalRule highPriorityMockRule = AutoApprovalRule.builder()
                .ruleName("높은 우선순위 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("30000")) // 테스트 요금제 가격(50000)보다 작음
                .priority(1)
                .build();
        
        AutoApprovalRule lowPriorityMockRule = AutoApprovalRule.builder()
                .ruleName("낮은 우선순위 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("100000")) // 테스트 요금제 가격(50000)보다 큼
                .priority(2)
                .build();
        
        when(autoApprovalRuleRepository.save(any(AutoApprovalRule.class)))
                .thenReturn(highPriorityMockRule)
                .thenReturn(lowPriorityMockRule);
        when(autoApprovalRuleRepository.findByIsActiveTrueOrderByPriorityDescIdAsc())
                .thenReturn(List.of(highPriorityMockRule, lowPriorityMockRule));
        
        // Given: 자동 승인 시스템이 활성화되어 있고
        autoApprovalRuleService.toggleAutoApprovalSystem(true);
        
        // And: 서로 다른 우선순위를 가진 두 규칙이 존재함
        AutoApprovalRuleDto highPriorityRule = AutoApprovalRuleDto.builder()
                .ruleName("높은 우선순위 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("30000"))
                .priority(1)
                .build();
        
        AutoApprovalRuleDto lowPriorityRule = AutoApprovalRuleDto.builder()
                .ruleName("낮은 우선순위 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("100000"))
                .priority(2)
                .build();
        
        autoApprovalRuleService.createRule(highPriorityRule);
        autoApprovalRuleService.createRule(lowPriorityRule);
        
        // When: 구독 요청을 평가
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId(testPlan.getPlanId());
        request.setBillingCycle(BillingCycle.MONTHLY);
        
        AutoApprovalRuleDto appliedRule = autoApprovalRuleService.getAppliedRule(request);
        
        // Then: 테스트 요금제 가격(50000)이 높은 우선순위 규칙의 한도(30000)보다 크므로
        // 낮은 우선순위 규칙이 적용되어야 함
        assertThat(appliedRule).isNotNull();
        assertThat(appliedRule.getRuleName()).isEqualTo("낮은 우선순위 규칙");
    }
    
    /**
     * Property 29: Auto-Approval Rule Application Timing
     * 자동 승인 규칙 적용 시점 테스트
     * 
     * **Validates: Requirements 7.4**
     */
    @Test
    void autoApprovalRuleApplicationTimingTest() throws Exception {
        // Given: Mock 설정
        AutoApprovalRule oldMockRule = AutoApprovalRule.builder()
                .ruleName("기존 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("50000"))
                .priority(1)
                .build();
        
        AutoApprovalRule updatedMockRule = AutoApprovalRule.builder()
                .ruleName("기존 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("30000")) // 변경된 최대 금액
                .priority(1)
                .build();
        
        when(autoApprovalRuleRepository.save(any(AutoApprovalRule.class)))
                .thenReturn(oldMockRule)
                .thenReturn(updatedMockRule);
        when(autoApprovalRuleRepository.findById(1L))
                .thenReturn(Optional.of(oldMockRule))
                .thenReturn(Optional.of(updatedMockRule));
        when(autoApprovalRuleRepository.findByIsActiveTrueOrderByPriorityDescIdAsc())
                .thenReturn(List.of(oldMockRule))
                .thenReturn(List.of(updatedMockRule));
        
        // Given: 자동 승인 시스템이 활성화되어 있고
        autoApprovalRuleService.toggleAutoApprovalSystem(true);
        
        // And: 기존 자동 승인 규칙이 존재함
        AutoApprovalRuleDto originalRule = AutoApprovalRuleDto.builder()
                .ruleName("기존 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("50000"))
                .priority(1)
                .build();
        
        autoApprovalRuleService.createRule(originalRule);
        
        // And: 구독 요청이 기존 규칙으로는 승인됨
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId(testPlan.getPlanId());
        request.setBillingCycle(BillingCycle.MONTHLY);
        
        boolean shouldAutoApproveBeforeUpdate = autoApprovalRuleService.evaluateAutoApproval(request);
        assertThat(shouldAutoApproveBeforeUpdate).isTrue();
        
        // When: 자동 승인 규칙을 수정 (최대 금액을 낮춤)
        AutoApprovalRuleDto updatedRule = AutoApprovalRuleDto.builder()
                .ruleName("기존 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("30000")) // 테스트 요금제 가격(50000)보다 낮음
                .priority(1)
                .build();
        
        autoApprovalRuleService.updateRule(1L, updatedRule);
        
        // Then: 새로운 구독 요청에는 수정된 규칙이 적용되어야 함
        boolean shouldAutoApproveAfterUpdate = autoApprovalRuleService.evaluateAutoApproval(request);
        assertThat(shouldAutoApproveAfterUpdate).isFalse(); // 최대 금액이 낮아져서 승인되지 않음
        
        // And: 적용된 규칙이 없어야 함 (조건을 만족하지 않으므로)
        AutoApprovalRuleDto appliedRule = autoApprovalRuleService.getAppliedRule(request);
        assertThat(appliedRule).isNull();
    }
    
    /**
     * Property 30: Auto-Approval Disable Functionality
     * 자동 승인 비활성화 기능 테스트
     * 
     * **Validates: Requirements 7.5**
     */
    @Test
    void autoApprovalDisableFunctionalityPropertyTest() throws Exception {
        // Given: Mock 설정
        AutoApprovalRule mockRule = AutoApprovalRule.builder()
                .ruleName("테스트 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        when(autoApprovalRuleRepository.save(any(AutoApprovalRule.class))).thenReturn(mockRule);
        when(autoApprovalRuleRepository.findByIsActiveTrueOrderByPriorityDescIdAsc())
                .thenReturn(List.of(mockRule));
        
        // Given: 자동 승인 시스템이 활성화되어 있고
        autoApprovalRuleService.toggleAutoApprovalSystem(true);
        
        // And: 활성화된 자동 승인 규칙이 존재함
        AutoApprovalRuleDto ruleDto = AutoApprovalRuleDto.builder()
                .ruleName("테스트 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        autoApprovalRuleService.createRule(ruleDto);
        
        // And: 구독 요청이 자동 승인 조건을 만족함
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId(testPlan.getPlanId());
        request.setBillingCycle(BillingCycle.MONTHLY);
        
        // When: 자동 승인 시스템이 활성화된 상태에서 평가
        boolean shouldAutoApproveWhenEnabled = autoApprovalRuleService.evaluateAutoApproval(request);
        
        // Then: 자동 승인되어야 함
        assertThat(shouldAutoApproveWhenEnabled).isTrue();
        
        // When: 자동 승인 시스템을 비활성화
        autoApprovalRuleService.toggleAutoApprovalSystem(false);
        
        // And: 동일한 구독 요청을 다시 평가
        boolean shouldAutoApproveWhenDisabled = autoApprovalRuleService.evaluateAutoApproval(request);
        
        // Then: 자동 승인 시스템이 비활성화되어 있으므로 자동 승인되지 않아야 함
        assertThat(shouldAutoApproveWhenDisabled).isFalse();
        
        // And: 적용된 규칙이 없어야 함
        AutoApprovalRuleDto appliedRule = autoApprovalRuleService.getAppliedRule(request);
        assertThat(appliedRule).isNull();
        
        // When: 자동 승인 시스템을 다시 활성화
        autoApprovalRuleService.toggleAutoApprovalSystem(true);
        
        // And: 동일한 구독 요청을 다시 평가
        boolean shouldAutoApproveWhenReEnabled = autoApprovalRuleService.evaluateAutoApproval(request);
        
        // Then: 다시 자동 승인되어야 함
        assertThat(shouldAutoApproveWhenReEnabled).isTrue();
        
        // And: 시스템 상태 조회가 올바르게 작동해야 함
        boolean isSystemEnabled = autoApprovalRuleService.isAutoApprovalEnabled();
        assertThat(isSystemEnabled).isTrue();
    }

    /**
     * 규칙 상태 변경 테스트
     */
    @Test
    void ruleStatusToggleTest() throws Exception {
        // Given: Mock 설정
        AutoApprovalRule mockRule = AutoApprovalRule.builder()
                .ruleName("상태 변경 테스트 규칙")
                .isActive(true)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        AutoApprovalRule deactivatedMockRule = AutoApprovalRule.builder()
                .ruleName("상태 변경 테스트 규칙")
                .isActive(false)
                .planIds("[\"BASIC\"]")
                .verifiedTenantsOnly(false)
                .paymentMethods("[\"CARD\"]")
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        when(autoApprovalRuleRepository.save(any(AutoApprovalRule.class))).thenReturn(mockRule);
        when(autoApprovalRuleRepository.findById(1L))
                .thenReturn(Optional.of(mockRule))
                .thenReturn(Optional.of(deactivatedMockRule));
        
        // Given: 활성화된 자동 승인 규칙
        AutoApprovalRuleDto ruleDto = AutoApprovalRuleDto.builder()
                .ruleName("상태 변경 테스트 규칙")
                .isActive(true)
                .planIds(List.of(testPlan.getPlanId()))
                .verifiedTenantsOnly(false)
                .paymentMethods(List.of("CARD"))
                .maxAmount(new BigDecimal("100000"))
                .priority(1)
                .build();
        
        autoApprovalRuleService.createRule(ruleDto);
        
        // When: 규칙을 비활성화
        AutoApprovalRuleDto deactivatedRule = autoApprovalRuleService.toggleRuleStatus(1L, false);
        
        // Then: 규칙이 비활성화되어야 함
        assertThat(deactivatedRule.getIsActive()).isFalse();
        assertThat(deactivatedRule.getRuleName()).isEqualTo("상태 변경 테스트 규칙");
    }
}