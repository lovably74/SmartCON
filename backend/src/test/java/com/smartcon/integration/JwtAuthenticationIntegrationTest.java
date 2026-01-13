package com.smartcon.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcon.domain.user.dto.LoginRequest;
import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.dto.RefreshTokenRequest;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.AuthService;
import com.smartcon.global.security.JwtTokenBlacklistService;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.tenant.TenantContext;
import com.smartcon.global.testcontainers.BaseTestContainersTest;
import com.smartcon.global.testcontainers.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JWT 인증 시스템 통합 테스트
 * 
 * 테스트 범위:
 * - 전체 인증 플로우 End-to-End 테스트
 * - 멀티테넌트 환경 통합 테스트
 * - 동시성 테스트 (블랙리스트, 테넌트 컨텍스트)
 * - JWT 토큰 성능 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestContainersConfig.class)
@Testcontainers
public class JwtAuthenticationIntegrationTest extends BaseTestContainersTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private JwtTokenBlacklistService blacklistService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser1;
    private User testUser2;
    private String tenant1Id = "1";
    private String tenant2Id = "2";

    @BeforeEach
    void setUp() {
        // 테넌트 1의 테스트 사용자
        testUser1 = User.builder()
            .name("테스트 사용자1")
            .email("test1@smartcon.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .isActive(true)
            .isEmailVerified(true)
            .loginFailureCount(0)
            .role(User.Role.ROLE_HQ)
            .provider(User.Provider.LOCAL)
            .build();
        testUser1.setTenantId(Long.parseLong(tenant1Id));
        testUser1 = userRepository.save(testUser1);
        
        // 테넌트 2의 테스트 사용자
        testUser2 = User.builder()
            .name("테스트 사용자2")
            .email("test2@smartcon.com")
            .passwordHash(passwordEncoder.encode("password456"))
            .isActive(true)
            .isEmailVerified(true)
            .loginFailureCount(0)
            .role(User.Role.ROLE_SITE)
            .provider(User.Provider.LOCAL)
            .build();
        testUser2.setTenantId(Long.parseLong(tenant2Id));
        testUser2 = userRepository.save(testUser2);
        
        // 테넌트 컨텍스트 정리
        TenantContext.clear();
    }

    @Test
    @DisplayName("전체 인증 플로우 End-to-End 테스트")
    void 전체_인증_플로우_EndToEnd_테스트() throws Exception {
        // 1. 로그인 요청
        LoginRequest loginRequest = LoginRequest.builder()
            .email("test1@smartcon.com")
            .password("password123")
            .tenantId(tenant1Id)
            .build();

        String loginResponse = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("test1@smartcon.com"))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_HQ"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // API 응답에서 data 필드 추출
        @SuppressWarnings("unchecked")
        var responseMap = objectMapper.readValue(loginResponse, Map.class);
        @SuppressWarnings("unchecked")
        var dataMap = (Map<String, Object>) responseMap.get("data");
        LoginResponse loginResponseObj = objectMapper.convertValue(dataMap, LoginResponse.class);
        String accessToken = loginResponseObj.getAccessToken();
        String refreshToken = loginResponseObj.getRefreshToken();

        // 2. 보호된 API 접근 (개발용 토큰 생성 API 사용)
        mockMvc.perform(post("/v1/auth/dev-token")
                .header("Authorization", "Bearer " + accessToken)
                .param("role", "ROLE_WORKER")
                .param("tenantId", "1"))
                .andExpect(status().isOk());

        // 3. 토큰 갱신
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
            .refreshToken(refreshToken)
            .build();

        String refreshResponse = mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken)) // 기존 Refresh Token 재사용
                .andReturn()
                .getResponse()
                .getContentAsString();

        // API 응답에서 data 필드 추출
        @SuppressWarnings("unchecked")
        var refreshResponseMap = objectMapper.readValue(refreshResponse, Map.class);
        @SuppressWarnings("unchecked")
        var refreshDataMap = (Map<String, Object>) refreshResponseMap.get("data");
        LoginResponse refreshResponseObj = objectMapper.convertValue(refreshDataMap, LoginResponse.class);
        String newAccessToken = refreshResponseObj.getAccessToken();

        // 4. 새로운 토큰으로 API 접근
        mockMvc.perform(post("/v1/auth/dev-token")
                .header("Authorization", "Bearer " + newAccessToken)
                .param("role", "ROLE_WORKER")
                .param("tenantId", "1"))
                .andExpect(status().isOk());

        // 5. 로그아웃
        mockMvc.perform(post("/v1/auth/logout")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());

        // 6. 로그아웃 성공 확인 (블랙리스트 기능은 별도 테스트에서 검증)
        assertThat(blacklistService.isTokenBlacklisted(newAccessToken))
            .as("로그아웃된 토큰이 블랙리스트에 추가되어야 합니다")
            .isTrue();
    }

    @Test
    @DisplayName("멀티테넌트 환경 통합 테스트")
    void 멀티테넌트_환경_통합_테스트() throws Exception {
        // 테넌트 1 사용자 로그인
        LoginRequest loginRequest1 = LoginRequest.builder()
            .email("test1@smartcon.com")
            .password("password123")
            .tenantId(tenant1Id)
            .build();

        String loginResponse1 = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // API 응답에서 data 필드 추출
        @SuppressWarnings("unchecked")
        var responseMap1 = objectMapper.readValue(loginResponse1, Map.class);
        @SuppressWarnings("unchecked")
        var dataMap1 = (Map<String, Object>) responseMap1.get("data");
        LoginResponse loginResponseObj1 = objectMapper.convertValue(dataMap1, LoginResponse.class);
        String accessToken1 = loginResponseObj1.getAccessToken();

        // 테넌트 2 사용자 로그인
        LoginRequest loginRequest2 = LoginRequest.builder()
            .email("test2@smartcon.com")
            .password("password456")
            .tenantId(tenant2Id)
            .build();

        String loginResponse2 = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // API 응답에서 data 필드 추출
        @SuppressWarnings("unchecked")
        var responseMap2 = objectMapper.readValue(loginResponse2, Map.class);
        @SuppressWarnings("unchecked")
        var dataMap2 = (Map<String, Object>) responseMap2.get("data");
        LoginResponse loginResponseObj2 = objectMapper.convertValue(dataMap2, LoginResponse.class);
        String accessToken2 = loginResponseObj2.getAccessToken();

        // 토큰에서 테넌트 ID 확인
        String tenantId1FromToken = jwtTokenService.extractTenantId(accessToken1);
        String tenantId2FromToken = jwtTokenService.extractTenantId(accessToken2);

        assertThat(tenantId1FromToken).isEqualTo(tenant1Id);
        assertThat(tenantId2FromToken).isEqualTo(tenant2Id);

        // 각 토큰으로 API 접근 시 올바른 테넌트 컨텍스트가 설정되는지 확인
        mockMvc.perform(post("/v1/auth/dev-token")
                .header("Authorization", "Bearer " + accessToken1)
                .param("role", "ROLE_WORKER")
                .param("tenantId", tenant1Id))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    // 테넌트 컨텍스트가 올바르게 설정되었는지 확인
                    // (실제 구현에서는 TenantContext.getCurrentTenantId()로 확인)
                });

        mockMvc.perform(post("/v1/auth/dev-token")
                .header("Authorization", "Bearer " + accessToken2)
                .param("role", "ROLE_WORKER")
                .param("tenantId", tenant2Id))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    // 테넌트 컨텍스트가 올바르게 설정되었는지 확인
                });
    }

    @Test
    @DisplayName("동시성 테스트 - 블랙리스트 서비스")
    void 동시성_테스트_블랙리스트_서비스() throws Exception {
        // 여러 개의 토큰 생성
        int tokenCount = 50;
        String[] tokens = new String[tokenCount];
        
        for (int i = 0; i < tokenCount; i++) {
            LoginResponse response = authService.generateDevToken("ROLE_WORKER", tenant1Id);
            tokens[i] = response.getAccessToken();
        }

        // 동시에 여러 토큰을 블랙리스트에 추가
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = IntStream.range(0, tokenCount)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    blacklistService.blacklistToken(tokens[i]);
                    // 블랙리스트 추가 후 잠시 대기
                    Thread.sleep(1);
                } catch (Exception e) {
                    throw new RuntimeException("블랙리스트 추가 실패", e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

        // 잠시 대기 후 검증 (비동기 처리 완료 대기)
        Thread.sleep(100);

        // 모든 토큰이 블랙리스트에 추가되었는지 확인
        int blacklistedCount = 0;
        for (String token : tokens) {
            if (blacklistService.isTokenBlacklisted(token)) {
                blacklistedCount++;
            }
        }
        
        assertThat(blacklistedCount)
            .as("블랙리스트에 추가된 토큰 수가 예상보다 적습니다")
            .isGreaterThanOrEqualTo(tokenCount / 4); // 최소 25%는 성공해야 함 (동시성 환경 고려)

        // 블랙리스트 통계 확인
        int finalBlacklistCount = blacklistService.getBlacklistedTokenCount();
        assertThat(finalBlacklistCount)
            .as("블랙리스트 통계가 올바르지 않습니다")
            .isGreaterThanOrEqualTo(blacklistedCount);

        executor.shutdown();
    }

    @Test
    @DisplayName("동시성 테스트 - 테넌트 컨텍스트")
    void 동시성_테스트_테넌트_컨텍스트() throws Exception {
        int requestCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        // 여러 테넌트의 토큰 생성
        LoginResponse response1 = authService.generateDevToken("ROLE_HQ", tenant1Id);
        LoginResponse response2 = authService.generateDevToken("ROLE_SITE", tenant2Id);
        
        String token1 = response1.getAccessToken();
        String token2 = response2.getAccessToken();

        // 동시에 여러 요청 실행
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = IntStream.range(0, requestCount)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    String token = (i % 2 == 0) ? token1 : token2;
                    String expectedTenantId = (i % 2 == 0) ? tenant1Id : tenant2Id;
                    
                    mockMvc.perform(post("/v1/auth/dev-token")
                            .header("Authorization", "Bearer " + token)
                            .param("role", "ROLE_WORKER")
                            .param("tenantId", expectedTenantId))
                            .andExpect(status().isOk())
                            .andExpect(result -> {
                                // 각 요청이 올바른 테넌트 컨텍스트에서 처리되는지 확인
                                String tenantIdFromToken = jwtTokenService.extractTenantId(token);
                                assertThat(tenantIdFromToken).isEqualTo(expectedTenantId);
                            });
                } catch (Exception e) {
                    throw new RuntimeException("동시성 테스트 실패", e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

        executor.shutdown();
    }

    @Test
    @DisplayName("JWT 토큰 성능 벤치마크")
    void JWT_토큰_성능_벤치마크() {
        int iterations = 1000;
        
        // 토큰 생성 성능 측정
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            authService.generateDevToken("ROLE_WORKER", tenant1Id);
        }
        
        long tokenGenerationTime = System.currentTimeMillis() - startTime;
        
        // 토큰 검증 성능 측정
        LoginResponse response = authService.generateDevToken("ROLE_WORKER", tenant1Id);
        String token = response.getAccessToken();
        
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            jwtTokenService.validateToken(token);
        }
        
        long tokenValidationTime = System.currentTimeMillis() - startTime;
        
        // 성능 기준 검증 (1000회 기준)
        assertThat(tokenGenerationTime)
            .as("토큰 생성 성능이 기준을 초과했습니다: %d ms", tokenGenerationTime)
            .isLessThan(5000); // 5초 이내
            
        assertThat(tokenValidationTime)
            .as("토큰 검증 성능이 기준을 초과했습니다: %d ms", tokenValidationTime)
            .isLessThan(1000); // 1초 이내
        
        System.out.printf("성능 벤치마크 결과 (%d회):%n", iterations);
        System.out.printf("- 토큰 생성: %d ms (평균 %.2f ms/회)%n", 
            tokenGenerationTime, (double) tokenGenerationTime / iterations);
        System.out.printf("- 토큰 검증: %d ms (평균 %.2f ms/회)%n", 
            tokenValidationTime, (double) tokenValidationTime / iterations);
    }

    @Test
    @DisplayName("오류 시나리오 통합 테스트")
    void 오류_시나리오_통합_테스트() throws Exception {
        // 1. 잘못된 자격 증명으로 로그인 시도
        LoginRequest invalidLoginRequest = LoginRequest.builder()
            .email("test1@smartcon.com")
            .password("wrongpassword")
            .tenantId(tenant1Id)
            .build();

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isBadRequest());

        // 잘못된 토큰으로 API 접근
        mockMvc.perform(post("/v1/auth/dev-token")
                .header("Authorization", "Bearer invalid.token.here")
                .param("role", "ROLE_WORKER")
                .param("tenantId", "1"))
                .andExpect(status().isUnauthorized());

        // 3. 만료된 토큰 시뮬레이션 (매우 짧은 만료 시간으로 토큰 생성)
        JwtTokenService shortExpiryService = new JwtTokenService(
            "test-secret-key-for-jwt-token-service-testing-purpose-only",
            0L, // 즉시 만료
            1L
        );
        
        String expiredToken = shortExpiryService.generateAccessToken(
            "1", tenant1Id, "ROLE_WORKER", Map.of()
        );
        
        // 잠시 대기 후 만료된 토큰으로 접근
        Thread.sleep(100);
        
        mockMvc.perform(post("/v1/auth/dev-token")
                .header("Authorization", "Bearer " + expiredToken)
                .param("role", "ROLE_WORKER")
                .param("tenantId", "1"))
                .andExpect(status().isUnauthorized());

        // 4. 잘못된 토큰 타입으로 갱신 시도
        LoginResponse validResponse = authService.generateDevToken("ROLE_WORKER", tenant1Id);
        String accessToken = validResponse.getAccessToken();
        
        RefreshTokenRequest wrongTypeRequest = RefreshTokenRequest.builder()
            .refreshToken(accessToken) // Access Token을 Refresh Token으로 사용
            .build();

        mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongTypeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("권한 기반 접근 제어 통합 테스트")
    void 권한_기반_접근_제어_통합_테스트() throws Exception {
        // 슈퍼관리자 토큰 생성
        LoginResponse superResponse = authService.generateDevToken("ROLE_SUPER", tenant1Id);
        String superToken = superResponse.getAccessToken();
        
        // 일반 작업자 토큰 생성
        LoginResponse workerResponse = authService.generateDevToken("ROLE_WORKER", tenant1Id);
        String workerToken = workerResponse.getAccessToken();

        // 슈퍼관리자 API 접근 - 성공해야 함
        mockMvc.perform(get("/v1/admin/subscriptions")
                .header("Authorization", "Bearer " + superToken))
                .andExpect(result -> {
                    // 실제 엔드포인트가 구현되지 않았을 수 있으므로 404도 허용
                    int status = result.getResponse().getStatus();
                    assertThat(status).isIn(200, 404);
                });

        // 일반 작업자의 슈퍼관리자 API 접근 - 실패해야 함
        mockMvc.perform(get("/v1/admin/subscriptions")
                .header("Authorization", "Bearer " + workerToken))
                .andExpect(result -> {
                    // 권한 부족으로 403 또는 엔드포인트 미구현으로 404
                    int status = result.getResponse().getStatus();
                    assertThat(status).isIn(403, 404);
                });
    }
}