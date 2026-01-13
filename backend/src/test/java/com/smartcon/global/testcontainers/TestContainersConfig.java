package com.smartcon.global.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers MariaDB 설정 클래스
 * 
 * 테스트 환경에서 MariaDB 컨테이너를 자동으로 시작하고 관리합니다.
 * Spring Boot의 @ServiceConnection을 사용하여 자동으로 데이터소스를 설정합니다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    /**
     * MariaDB 테스트 컨테이너 생성
     * 
     * @return MariaDB 컨테이너 인스턴스
     */
    @Bean
    @ServiceConnection
    public MariaDBContainer<?> mariaDBContainer() {
        return new MariaDBContainer<>(DockerImageName.parse("mariadb:10.11"))
                .withDatabaseName("smartcon_test")
                .withUsername("test_user")
                .withPassword("test_password")
                .withReuse(true); // 테스트 간 컨테이너 재사용으로 성능 향상
    }
}