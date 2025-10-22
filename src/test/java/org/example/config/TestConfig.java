package org.example.config;

import org.example.service.LockService;
import org.example.service.MockRedisLockService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {
    
    @Bean
    @Primary
    public LockService mockLockService() {
        return new MockRedisLockService();
    }
}
