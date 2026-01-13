package com.smartcon.global.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers 기반 테스트를 위한 베이스 클래스
 * 
 * 이 클래스를 상속받는 모든 테스트는 자동으로 MariaDB 컨테이너를 사용합니다.
 * 
 * 사용법:
 * - 통합 테스트 클래스에서 이 클래스를 상속받으면 됩니다
 * - @Testcontainers 어노테이션으로 컨테이너 자동 관리
 * - @Import로 TestContainersConfig 설정 자동 로드
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
public abstract class BaseTestContainersTest {
    
    // 하위 클래스에서 공통으로 사용할 수 있는 유틸리티 메서드들을 여기에 추가할 수 있습니다
    
    /**
     * 테스트 데이터베이스 초기화 상태 확인
     * 
     * @return 데이터베이스 연결 상태
     */
    protected boolean isDatabaseReady() {
        // 필요시 데이터베이스 상태 확인 로직 추가
        return true;
    }
}