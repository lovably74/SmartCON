package com.smartcon.domain.admin.controller;

import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.entity.SubscriptionPlan;
import com.smartcon.domain.subscription.entity.BillingCycle;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * E2E 테스트용 데이터 관리 컨트롤러
 * 
 * 로컬 환경에서만 활성화되며, E2E 테스트를 위한 테스트 데이터 생성 및 정리 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Profile("local") // 로컬 환경에서만 활성화
public class TestDataController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("E2E 테스트 API 서버가 정상 작동 중입니다."));
    }

    /**
     * 테스트 데이터 초기화
     */
    @PostMapping("/reset-data")
    public ResponseEntity<ApiResponse<String>> resetData() {
        try {
            log.info("테스트 데이터 초기화 시작");
            
            // 모든 테스트 데이터 삭제 (순서 중요 - 외래키 제약조건 고려)
            subscriptionRepository.deleteAll();
            userRepository.deleteAll();
            tenantRepository.deleteAll();
            
            log.info("테스트 데이터 초기화 완료");
            return ResponseEntity.ok(ApiResponse.success("테스트 데이터가 초기화되었습니다."));
        } catch (Exception e) {
            log.error("테스트 데이터 초기화 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("테스트 데이터 초기화에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 슈퍼관리자 생성
     */
    @PostMapping("/create-super-admin")
    public ResponseEntity<ApiResponse<String>> createSuperAdmin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            
            log.info("슈퍼관리자 생성: {}", email);
            
            User superAdmin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role("SUPER_ADMIN")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
            
            userRepository.save(superAdmin);
            
            log.info("슈퍼관리자 생성 완료: {}", email);
            return ResponseEntity.ok(ApiResponse.success("슈퍼관리자가 생성되었습니다."));
        } catch (Exception e) {
            log.error("슈퍼관리자 생성 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("슈퍼관리자 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 테넌트 관리자 생성
     */
    @PostMapping("/create-tenant-admin")
    public ResponseEntity<ApiResponse<String>> createTenantAdmin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            String companyName = request.get("companyName");
            
            log.info("테넌트 관리자 생성: {} ({})", email, companyName);
            
            // 테넌트 생성
            Tenant tenant = Tenant.builder()
                .name(companyName)
                .businessNumber("123-45-67890") // 테스트용 사업자번호
                .contactEmail(email)
                .contactPhone("010-1234-5678")
                .address("서울시 강남구 테스트로 123")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
            
            Tenant savedTenant = tenantRepository.save(tenant);
            
            // 테넌트 관리자 생성
            User tenantAdmin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role("HQ_ADMIN")
                .tenantId(savedTenant.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
            
            userRepository.save(tenantAdmin);
            
            log.info("테넌트 관리자 생성 완료: {} (테넌트 ID: {})", email, savedTenant.getId());
            return ResponseEntity.ok(ApiResponse.success("테넌트 관리자가 생성되었습니다."));
        } catch (Exception e) {
            log.error("테넌트 관리자 생성 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("테넌트 관리자 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 테스트용 구독 데이터 생성
     */
    @PostMapping("/create-test-subscriptions")
    public ResponseEntity<ApiResponse<String>> createTestSubscriptions(@RequestBody Map<String, Integer> request) {
        try {
            int count = request.getOrDefault("count", 5);
            
            log.info("테스트 구독 데이터 생성 시작: {}개", count);
            
            for (int i = 1; i <= count; i++) {
                // 테넌트 생성
                Tenant tenant = Tenant.builder()
                    .name("테스트 회사 " + i)
                    .businessNumber("123-45-6789" + i)
                    .contactEmail("test" + i + "@company.com")
                    .contactPhone("010-1234-567" + i)
                    .address("서울시 강남구 테스트로 " + (100 + i))
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
                
                Tenant savedTenant = tenantRepository.save(tenant);
                
                // 구독 생성 (다양한 상태로)
                SubscriptionStatus status;
                switch (i % 4) {
                    case 0: status = SubscriptionStatus.PENDING_APPROVAL; break;
                    case 1: status = SubscriptionStatus.ACTIVE; break;
                    case 2: status = SubscriptionStatus.REJECTED; break;
                    default: status = SubscriptionStatus.SUSPENDED; break;
                }
                
                Subscription subscription = Subscription.builder()
                    .tenantId(savedTenant.getId())
                    .plan(i % 2 == 0 ? SubscriptionPlan.BASIC : SubscriptionPlan.PREMIUM)
                    .billingCycle(i % 2 == 0 ? BillingCycle.MONTHLY : BillingCycle.YEARLY)
                    .status(status)
                    .startDate(LocalDateTime.now().minusDays(i))
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
                
                subscriptionRepository.save(subscription);
            }
            
            log.info("테스트 구독 데이터 생성 완료: {}개", count);
            return ResponseEntity.ok(ApiResponse.success(count + "개의 테스트 구독이 생성되었습니다."));
        } catch (Exception e) {
            log.error("테스트 구독 데이터 생성 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("테스트 구독 데이터 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 테스트 데이터 정리
     */
    @PostMapping("/cleanup-data")
    public ResponseEntity<ApiResponse<String>> cleanupData() {
        try {
            log.info("테스트 데이터 정리 시작");
            
            // 테스트 데이터만 삭제 (이메일 패턴으로 구분)
            userRepository.deleteAll(userRepository.findAll().stream()
                .filter(user -> user.getEmail().contains("test") || user.getEmail().contains("@smartcon.com"))
                .toList());
            
            tenantRepository.deleteAll(tenantRepository.findAll().stream()
                .filter(tenant -> tenant.getName().contains("테스트"))
                .toList());
            
            log.info("테스트 데이터 정리 완료");
            return ResponseEntity.ok(ApiResponse.success("테스트 데이터가 정리되었습니다."));
        } catch (Exception e) {
            log.error("테스트 데이터 정리 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("테스트 데이터 정리에 실패했습니다: " + e.getMessage()));
        }
    }
}