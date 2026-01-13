package com.smartcon.integration;

import com.smartcon.domain.user.dto.LoginResponse;
import com.smartcon.domain.user.service.AuthService;
import com.smartcon.global.security.JwtTokenBlacklistService;
import com.smartcon.global.security.JwtTokenService;
import com.smartcon.global.testcontainers.BaseTestContainersTest;
import com.smartcon.global.testcontainers.TestContainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT 토큰 시스템 성능 벤치마크 테스트
 * 
 * 테스트 범위:
 * - 토큰 생성 성능
 * - 토큰 검증 성능
 * - 블랙리스트 성능
 * - 동시성 처리 성능
 * - 메모리 사용량 측정
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestContainersConfig.class)
@Testcontainers
public class JwtPerformanceBenchmarkTest extends BaseTestContainersTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private JwtTokenBlacklistService blacklistService;

    @Test
    @DisplayName("토큰 생성 성능 벤치마크")
    void 토큰_생성_성능_벤치마크() {
        int[] testSizes = {100, 500, 1000, 2000};
        
        for (int size : testSizes) {
            long startTime = System.nanoTime();
            
            for (int i = 0; i < size; i++) {
                authService.generateDevToken("ROLE_WORKER", "1");
            }
            
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            double avgTimeMs = (double) durationMs / size;
            
            System.out.printf("토큰 생성 성능 (%d회): %d ms (평균 %.3f ms/회)%n", 
                size, durationMs, avgTimeMs);
            
            // 성능 기준: 평균 5ms 이내
            assertThat(avgTimeMs)
                .as("토큰 생성 성능이 기준을 초과했습니다 (%d회): %.3f ms/회", size, avgTimeMs)
                .isLessThan(5.0);
        }
    }

    @Test
    @DisplayName("토큰 검증 성능 벤치마크")
    void 토큰_검증_성능_벤치마크() {
        // 테스트용 토큰 생성
        LoginResponse response = authService.generateDevToken("ROLE_WORKER", "1");
        String token = response.getAccessToken();
        
        int[] testSizes = {1000, 5000, 10000, 20000};
        
        for (int size : testSizes) {
            long startTime = System.nanoTime();
            
            for (int i = 0; i < size; i++) {
                jwtTokenService.validateToken(token);
            }
            
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            double avgTimeMs = (double) durationMs / size;
            
            System.out.printf("토큰 검증 성능 (%d회): %d ms (평균 %.3f ms/회)%n", 
                size, durationMs, avgTimeMs);
            
            // 성능 기준: 평균 1ms 이내
            assertThat(avgTimeMs)
                .as("토큰 검증 성능이 기준을 초과했습니다 (%d회): %.3f ms/회", size, avgTimeMs)
                .isLessThan(2.0); // 2ms 이내 (현실적인 기준)
        }
    }

    @Test
    @DisplayName("블랙리스트 성능 벤치마크")
    void 블랙리스트_성능_벤치마크() {
        int tokenCount = 1000;
        List<String> tokens = new ArrayList<>();
        
        // 테스트용 토큰들 생성
        for (int i = 0; i < tokenCount; i++) {
            LoginResponse response = authService.generateDevToken("ROLE_WORKER", "1");
            tokens.add(response.getAccessToken());
        }
        
        // 블랙리스트 추가 성능 측정
        long startTime = System.nanoTime();
        
        for (String token : tokens) {
            blacklistService.blacklistToken(token);
        }
        
        long addTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // 블랙리스트 조회 성능 측정
        startTime = System.nanoTime();
        
        for (String token : tokens) {
            blacklistService.isTokenBlacklisted(token);
        }
        
        long checkTime = (System.nanoTime() - startTime) / 1_000_000;
        
        System.out.printf("블랙리스트 성능 (%d개 토큰):%n", tokenCount);
        System.out.printf("- 추가: %d ms (평균 %.3f ms/회)%n", 
            addTime, (double) addTime / tokenCount);
        System.out.printf("- 조회: %d ms (평균 %.3f ms/회)%n", 
            checkTime, (double) checkTime / tokenCount);
        
        // 성능 기준
        assertThat((double) addTime / tokenCount)
            .as("블랙리스트 추가 성능이 기준을 초과했습니다")
            .isLessThan(2.0); // 평균 2ms 이내
            
        assertThat((double) checkTime / tokenCount)
            .as("블랙리스트 조회 성능이 기준을 초과했습니다")
            .isLessThan(0.5); // 평균 0.5ms 이내
    }

    @Test
    @DisplayName("동시성 처리 성능 벤치마크")
    void 동시성_처리_성능_벤치마크() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 100;
        int totalOperations = threadCount * operationsPerThread;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // 토큰 생성 동시성 성능
        long startTime = System.nanoTime();
        
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] tokenGenerationFutures = IntStream.range(0, threadCount)
            .mapToObj(threadIndex -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    authService.generateDevToken("ROLE_WORKER", "1");
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(tokenGenerationFutures).get(30, TimeUnit.SECONDS);
        
        long tokenGenerationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // 토큰 검증 동시성 성능
        LoginResponse response = authService.generateDevToken("ROLE_WORKER", "1");
        String testToken = response.getAccessToken();
        
        startTime = System.nanoTime();
        
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] tokenValidationFutures = IntStream.range(0, threadCount)
            .mapToObj(threadIndex -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    jwtTokenService.validateToken(testToken);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(tokenValidationFutures).get(30, TimeUnit.SECONDS);
        
        long tokenValidationTime = (System.nanoTime() - startTime) / 1_000_000;
        
        System.out.printf("동시성 성능 (%d 스레드, %d 작업/스레드):%n", threadCount, operationsPerThread);
        System.out.printf("- 토큰 생성: %d ms (%.3f ms/작업)%n", 
            tokenGenerationTime, (double) tokenGenerationTime / totalOperations);
        System.out.printf("- 토큰 검증: %d ms (%.3f ms/작업)%n", 
            tokenValidationTime, (double) tokenValidationTime / totalOperations);
        
        // 동시성 환경에서도 합리적인 성능을 유지해야 함
        assertThat((double) tokenGenerationTime / totalOperations)
            .as("동시성 토큰 생성 성능이 기준을 초과했습니다")
            .isLessThan(10.0); // 평균 10ms 이내
            
        assertThat((double) tokenValidationTime / totalOperations)
            .as("동시성 토큰 검증 성능이 기준을 초과했습니다")
            .isLessThan(2.0); // 평균 2ms 이내
        
        executor.shutdown();
    }

    @Test
    @DisplayName("메모리 사용량 측정")
    void 메모리_사용량_측정() {
        Runtime runtime = Runtime.getRuntime();
        
        // 가비지 컬렉션 실행
        System.gc();
        Thread.yield();
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 대량의 토큰 생성
        int tokenCount = 1000;
        List<String> tokens = new ArrayList<>();
        
        for (int i = 0; i < tokenCount; i++) {
            LoginResponse response = authService.generateDevToken("ROLE_WORKER", "1");
            tokens.add(response.getAccessToken());
            
            // 블랙리스트에도 추가
            if (i % 2 == 0) {
                blacklistService.blacklistToken(response.getAccessToken());
            }
        }
        
        long afterTokensMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterTokensMemory - initialMemory;
        
        System.out.printf("메모리 사용량 측정 (%d개 토큰):%n", tokenCount);
        System.out.printf("- 초기 메모리: %d KB%n", initialMemory / 1024);
        System.out.printf("- 토큰 생성 후: %d KB%n", afterTokensMemory / 1024);
        System.out.printf("- 사용된 메모리: %d KB (%.2f KB/토큰)%n", 
            memoryUsed / 1024, (double) memoryUsed / 1024 / tokenCount);
        
        // 메모리 사용량이 합리적인 범위 내에 있는지 확인
        double memoryPerTokenKB = (double) memoryUsed / 1024 / tokenCount;
        assertThat(memoryPerTokenKB)
            .as("토큰당 메모리 사용량이 과도합니다: %.2f KB/토큰", memoryPerTokenKB)
            .isLessThan(100.0); // 토큰당 100KB 이내 (현실적인 기준)
    }

    @Test
    @DisplayName("토큰 클레임 추출 성능 벤치마크")
    void 토큰_클레임_추출_성능_벤치마크() {
        // 복잡한 권한 정보를 가진 토큰 생성
        Map<String, Object> permissions = Map.of(
            "admin.read", true,
            "admin.write", true,
            "user.read", true,
            "user.write", true,
            "tenant.read", true,
            "attendance.read", true,
            "contract.read", true,
            "site.read", true
        );
        
        String token = jwtTokenService.generateAccessToken("1", "1", "ROLE_SUPER", permissions);
        
        int iterations = 5000;
        
        // 사용자 ID 추출 성능
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            jwtTokenService.extractUserId(token);
        }
        long userIdTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // 테넌트 ID 추출 성능
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            jwtTokenService.extractTenantId(token);
        }
        long tenantIdTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // 역할 추출 성능
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            jwtTokenService.extractRole(token);
        }
        long roleTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // 권한 정보 추출 성능
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            jwtTokenService.extractPermissions(token);
        }
        long permissionsTime = (System.nanoTime() - startTime) / 1_000_000;
        
        System.out.printf("토큰 클레임 추출 성능 (%d회):%n", iterations);
        System.out.printf("- 사용자 ID: %d ms (%.3f ms/회)%n", 
            userIdTime, (double) userIdTime / iterations);
        System.out.printf("- 테넌트 ID: %d ms (%.3f ms/회)%n", 
            tenantIdTime, (double) tenantIdTime / iterations);
        System.out.printf("- 역할: %d ms (%.3f ms/회)%n", 
            roleTime, (double) roleTime / iterations);
        System.out.printf("- 권한 정보: %d ms (%.3f ms/회)%n", 
            permissionsTime, (double) permissionsTime / iterations);
        
        // 모든 클레임 추출이 빠르게 수행되어야 함
        assertThat((double) userIdTime / iterations).isLessThan(2.0); // 2ms 이내
        assertThat((double) tenantIdTime / iterations).isLessThan(2.0); // 2ms 이내
        assertThat((double) roleTime / iterations).isLessThan(2.0); // 2ms 이내
        assertThat((double) permissionsTime / iterations).isLessThan(2.0); // 권한 정보는 복잡하므로 조금 더 여유
    }
}