package com.smartcon.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 테스트 전용 설정
 * 
 * 테스트 환경에서 스케줄링을 비활성화하고 H2 호환성을 제공합니다.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {
    
    /**
     * 테스트용 TaskScheduler - 스케줄링 비활성화
     */
    @Bean
    @Primary
    public TaskScheduler testTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("test-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.setAwaitTerminationSeconds(0);
        scheduler.initialize();
        return scheduler;
    }
}