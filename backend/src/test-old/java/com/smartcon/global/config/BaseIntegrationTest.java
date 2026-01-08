package com.smartcon.global.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트를 위한 기본 설정 클래스
 * H2 인메모리 데이터베이스를 MariaDB 호환 모드로 사용합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    
    // 통합 테스트에서 공통으로 사용할 설정이나 유틸리티 메서드를 여기에 추가할 수 있습니다.
}