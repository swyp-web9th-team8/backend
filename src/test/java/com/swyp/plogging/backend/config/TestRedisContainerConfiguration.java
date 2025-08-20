package com.swyp.plogging.backend.config;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@Profile("redis")
@Configuration
public class TestRedisContainerConfiguration {
    @Container
    private static final GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>("redis:7.0")
                .withExposedPorts(6379);
        redisContainer.start();

        String host = redisContainer.getHost();
        Integer port = redisContainer.getMappedPort(6379);

        System.setProperty("spring.data.redis.host", host);
        System.setProperty("spring.data.redis.port", port.toString());
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return redisContainer;
    }

    @PreDestroy
    public void stop() {
        if (redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }
}
