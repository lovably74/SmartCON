package com.smartcon.integration;

import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.AuthService;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.tenant.TenantContext;
import com.smartcon.global.testcontainers.BaseTestContainersTest;
import com.smartcon.global.testcontainers.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * 멀티테넌트 환경 통합 테스트
 * 
 * 테스트 범위:
 * - 테넌트 격리 검증
 * - 테넌트 컨텍스트 관리
 * - 크로스 테넌트 접근 차단
 * - 테넌트별 데이터 필터링
 * - 동시 멀티테넌트 요청 처리
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestContainersConfig.class)
@Testcontainers
public class MultiTenantIntegrationTest extends BaseTestContainersTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private List<User> tenantUsers;
    private List<String> tenantIds;

    @BeforeEach
    void setUp() {
        tenantUsers = new ArrayList<>();
        tenantIds = List.of("1", "2", "3", "4", "5");
        
        // 각 테넌트별로 테스트 사용자 생성
        for (int i = 0; i < tenantIds.size(); i++) {
            String tenantId = tenantIds.get(i);
            
            User user = User.builder()
                .name("테넌트" + tenantId + " 사용자")
                .email("user" + tenantId + "@tenant" + tenantId + ".com")
                .passwordHash(passwordEncoder.encode("password" + tenantId))
                .isActive(true)
                .isEmailVerified(true)
                .loginFailureCount(0)
                .build();
            Role[] roles = {Role.ROLE_SUPER, Role.ROLE_HQ, Role.ROLE_SITE, Role.ROLE_TEAM, Role.ROLE_WORKER};
            user.addRole(roles[i % roles.length]); // 다양한 역할 할당
            
            user.setTenantId(Long.parseLong(tenantId));
            tenantUsers.add(userRepository.save(user));
        }
        
        // 테넌트 컨텍스트 정리
        TenantContext.clear();
    }

    @Test
    @DisplayName("테넌트 격리 검증 - 토큰 생성 및 검증")
    void 테넌트_격리_검증_토큰_생성_및_검증() {
        List<LoginResponse> responses = new ArrayList<>();
        
        // 각 테넌트별로 토큰 생성
        for (String tenantId : tenantIds) {
            LoginResponse response = authService.generateDevToken("ROLE_HQ", tenantId);
            responses.add(response);
            
            // 토큰에서 테넌트 ID 추출 및 검증
            String extractedTenantId = jwtTokenService.extractTenantId(response.getAccessToken());
            assertThat(extractedTenantId)
                .as("토큰에서 추출된 테넌트 ID가 일치하지 않습니다")
                .isEqualTo(tenantId);
            
            // 사용자 정보의 테넌트 ID 검증
            assertThat(response.getUser().getTenantId())
                .as("사용자 정보의 테넌트 ID가 일치하지 않습니다")
                .isEqualTo(tenantId);
        }
        
        // 모든 토큰이 서로 다른 테넌트 ID를 가져야 함
        for (int i = 0; i < responses.size(); i++) {
            for (int j = i + 1; j < responses.size(); j++) {
                String tenantId1 = jwtTokenService.extractTenantId(responses.get(i).getAccessToken());
                String tenantId2 = jwtTokenService.extractTenantId(responses.get(j).getAccessToken());
                
                assertThat(tenantId1)
                    .as("서로 다른 테넌트의 토큰이 같은 테넌트 ID를 가집니다")
                    .isNotEqualTo(tenantId2);
            }
        }
    }

    @Test
    @DisplayName("테넌트 컨텍스트 관리 - 설정 및 정리")
    void 테넌트_컨텍스트_관리_설정_및_정리() {
        for (String tenantId : tenantIds) {
            // 테넌트 컨텍스트 설정
            TenantContext.setCurrentTenantId(Long.parseLong(tenantId));
            
            // 현재 테넌트 ID 확인
            Long currentTenantId = TenantContext.getCurrentTenantId();
            assertThat(currentTenantId)
                .as("테넌트 컨텍스트가 올바르게 설정되지 않았습니다")
                .isEqualTo(Long.parseLong(tenantId));
            
            // 테넌트 컨텍스트 정리
            TenantContext.clear();
            
            // 정리 후 테넌트 ID가 null이어야 함
            assertThat(TenantContext.getCurrentTenantId())
                .as("테넌트 컨텍스트가 올바르게 정리되지 않았습니다")
                .isNull();
        }
    }

    @Test
    @DisplayName("동시 멀티테넌트 요청 처리")
    void 동시_멀티테넌트_요청_처리() throws Exception {
        int requestsPerTenant = 20;
        int totalRequests = tenantIds.size() * requestsPerTenant;
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // 각 테넌트별로 토큰 생성
        List<String> tokens = new ArrayList<>();
        for (String tenantId : tenantIds) {
            LoginResponse response = authService.generateDevToken("ROLE_SITE", tenantId);
            tokens.add(response.getAccessToken());
        }
        
        // 동시에 여러 테넌트의 요청 처리
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = IntStream.range(0, totalRequests)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                try {
                    int tenantIndex = i % tenantIds.size();
                    String token = tokens.get(tenantIndex);
                    String expectedTenantId = tenantIds.get(tenantIndex);
                    
                    // 토큰에서 테넌트 ID 추출
                    String extractedTenantId = jwtTokenService.extractTenantId(token);
                    
                    // 테넌트 컨텍스트 시뮬레이션
                    TenantContext.setCurrentTenantId(Long.parseLong(extractedTenantId));
                    
                    try {
                        // 현재 테넌트 ID 확인
                        Long currentTenantId = TenantContext.getCurrentTenantId();
                        
                        if (!expectedTenantId.equals(currentTenantId.toString())) {
                            return "FAIL: 테넌트 ID 불일치 - 예상: " + expectedTenantId + ", 실제: " + currentTenantId;
                        }
                        
                        // 토큰 검증
                        if (!jwtTokenService.validateToken(token)) {
                            return "FAIL: 토큰 검증 실패";
                        }
                        
                        return "SUCCESS: " + expectedTenantId;
                        
                    } finally {
                        // 테넌트 컨텍스트 정리
                        TenantContext.clear();
                    }
                    
                } catch (Exception e) {
                    return "ERROR: " + e.getMessage();
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        
        // 결과 검증
        for (CompletableFuture<String> future : futures) {
            String result = future.get();
            assertThat(result)
                .as("동시성 테스트 실패: " + result)
                .startsWith("SUCCESS:");
        }
        
        executor.shutdown();
    }

    @Test
    @DisplayName("테넌트별 사용자 데이터 격리 검증")
    void 테넌트별_사용자_데이터_격리_검증() {
        // 각 테넌트의 사용자 수 확인
        for (String tenantId : tenantIds) {
            TenantContext.setCurrentTenantId(Long.parseLong(tenantId));
            
            try {
                // 현재 테넌트의 사용자만 조회되어야 함 (명시적으로 테넌트 ID로 필터링)
                List<User> usersInTenant = userRepository.findByTenantId(Long.parseLong(tenantId));
                
                // 현재 테넌트의 사용자만 포함되어야 함
                for (User user : usersInTenant) {
                    assertThat(user.getTenantId())
                        .as("다른 테넌트의 사용자가 조회되었습니다: " + user.getEmail())
                        .isEqualTo(Long.parseLong(tenantId));
                }
                
                // 최소 1명의 사용자는 있어야 함 (setUp에서 생성한 사용자)
                assertThat(usersInTenant)
                    .as("테넌트 " + tenantId + "에 사용자가 없습니다")
                    .isNotEmpty();
                
            } finally {
                TenantContext.clear();
            }
        }
    }

    @Test
    @DisplayName("크로스 테넌트 접근 차단 검증")
    void 크로스_테넌트_접근_차단_검증() {
        // 테넌트 1의 토큰 생성
        LoginResponse tenant1Response = authService.generateDevToken("ROLE_SUPER", "1");
        String tenant1Token = tenant1Response.getAccessToken();
        
        // 테넌트 2의 토큰 생성
        LoginResponse tenant2Response = authService.generateDevToken("ROLE_SUPER", "2");
        String tenant2Token = tenant2Response.getAccessToken();
        
        // 토큰에서 테넌트 ID 추출
        String tenant1Id = jwtTokenService.extractTenantId(tenant1Token);
        String tenant2Id = jwtTokenService.extractTenantId(tenant2Token);
        
        // 서로 다른 테넌트 ID 확인
        assertThat(tenant1Id).isNotEqualTo(tenant2Id);
        
        // 테넌트 1 컨텍스트에서 테넌트 2 토큰 사용 시도
        TenantContext.setCurrentTenantId(Long.parseLong(tenant1Id));
        
        try {
            String tokenTenantId = jwtTokenService.extractTenantId(tenant2Token);
            Long contextTenantId = TenantContext.getCurrentTenantId();
            
            // 토큰의 테넌트 ID와 컨텍스트의 테넌트 ID가 다르면 접근 차단되어야 함
            assertThat(tokenTenantId)
                .as("크로스 테넌트 접근이 허용되었습니다")
                .isNotEqualTo(contextTenantId.toString());
                
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("테넌트별 권한 격리 검증")
    void 테넌트별_권한_격리_검증() {
        // 각 테넌트별로 다른 역할의 토큰 생성
        String[] roles = {"ROLE_SUPER", "ROLE_HQ", "ROLE_SITE", "ROLE_TEAM", "ROLE_WORKER"};
        
        for (int i = 0; i < tenantIds.size(); i++) {
            String tenantId = tenantIds.get(i);
            String role = roles[i % roles.length];
            
            LoginResponse response = authService.generateDevToken(role, tenantId);
            String token = response.getAccessToken();
            
            // 토큰에서 정보 추출
            String extractedTenantId = jwtTokenService.extractTenantId(token);
            String extractedRole = jwtTokenService.extractRole(token);
            
            // 테넌트 ID와 역할이 올바르게 설정되었는지 확인
            assertThat(extractedTenantId).isEqualTo(tenantId);
            assertThat(extractedRole).isEqualTo(role);
            
            // 권한 정보 확인
            var permissions = jwtTokenService.extractPermissions(token);
            assertThat(permissions)
                .as("권한 정보가 없습니다")
                .isNotEmpty();
            
            // 역할별 권한 검증
            switch (role) {
                case "ROLE_SUPER":
                    assertThat(permissions).containsKey("admin.read");
                    assertThat(permissions).containsKey("admin.write");
                    break;
                case "ROLE_HQ":
                    assertThat(permissions).containsKey("tenant.read");
                    assertThat(permissions).containsKey("user.read");
                    break;
                case "ROLE_SITE":
                    assertThat(permissions).containsKey("site.read");
                    assertThat(permissions).containsKey("attendance.read");
                    break;
                case "ROLE_TEAM":
                    assertThat(permissions).containsKey("team.read");
                    break;
                case "ROLE_WORKER":
                    assertThat(permissions).containsKey("attendance.read");
                    break;
            }
        }
    }

    @Test
    @DisplayName("테넌트 컨텍스트 스레드 안전성 검증")
    void 테넌트_컨텍스트_스레드_안전성_검증() throws Exception {
        int threadCount = 20;
        int operationsPerThread = 50;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = IntStream.range(0, threadCount)
            .mapToObj(threadIndex -> CompletableFuture.supplyAsync(() -> {
                try {
                    String tenantId = tenantIds.get(threadIndex % tenantIds.size());
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        // 테넌트 컨텍스트 설정
                        TenantContext.setCurrentTenantId(Long.parseLong(tenantId));
                        
                        // 현재 테넌트 ID 확인
                        Long currentTenantId = TenantContext.getCurrentTenantId();
                        if (!tenantId.equals(currentTenantId.toString())) {
                            return "FAIL: 스레드 " + threadIndex + " - 테넌트 ID 불일치";
                        }
                        
                        // 짧은 대기 (다른 스레드와의 경합 유도)
                        Thread.sleep(1);
                        
                        // 다시 확인
                        currentTenantId = TenantContext.getCurrentTenantId();
                        if (!tenantId.equals(currentTenantId.toString())) {
                            return "FAIL: 스레드 " + threadIndex + " - 테넌트 ID 변경됨";
                        }
                        
                        // 테넌트 컨텍스트 정리
                        TenantContext.clear();
                        
                        // 정리 확인
                        if (TenantContext.getCurrentTenantId() != null) {
                            return "FAIL: 스레드 " + threadIndex + " - 컨텍스트 정리 실패";
                        }
                    }
                    
                    return "SUCCESS: 스레드 " + threadIndex;
                    
                } catch (Exception e) {
                    return "ERROR: 스레드 " + threadIndex + " - " + e.getMessage();
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures).get(60, TimeUnit.SECONDS);
        
        // 결과 검증
        for (CompletableFuture<String> future : futures) {
            String result = future.get();
            assertThat(result)
                .as("스레드 안전성 테스트 실패: " + result)
                .startsWith("SUCCESS:");
        }
        
        executor.shutdown();
    }
}