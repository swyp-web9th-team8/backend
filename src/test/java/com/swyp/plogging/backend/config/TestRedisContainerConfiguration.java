package com.swyp.plogging.backend.config;

import jakarta.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@Profile("redisTest")
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
        System.setProperty("spring.redis.host", host);
        System.setProperty("spring.redis.port", port.toString());
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return redisContainer;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config cfg = new Config();
        String host = redisContainer.getHost();
        Integer port = redisContainer.getMappedPort(6379);
        System.out.println(">>>>>>host: "+host);
        System.out.println(">>>>>>port: "+port);
        SingleServerConfig scfg = cfg.useSingleServer().setAddress(host+":"+port); // 분명 redis://host:port 였는데 URI가 "//redis://host:port"로 이상하게 찍힌다.
        System.out.println(">>>>>>address: "+scfg.getAddress());
        return Redisson.create(cfg);
    }

    @PreDestroy
    public void stop() {
        if (redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }
}
