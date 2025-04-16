package com.swyp.plogging.backend.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Profile("test")
@Configuration
public class TestConfiguration {
    /**
     * jdbc:h2:mem:testdb → 메모리 DB. 테스트용으로 자주 사용.<br>
     * DB_CLOSE_DELAY=-1 → 커넥션이 닫혀도 DB를 유지 (테스트 중 여러 번 접근 가능).<br>
     * DB_CLOSE_ON_EXIT=FALSE → JVM 종료 시까지 DB 유지.
     * @return DataSource
     */
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .username("sa")
                .password("")
                .build();
    }
}
