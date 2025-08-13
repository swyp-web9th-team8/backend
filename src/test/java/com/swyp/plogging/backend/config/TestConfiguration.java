package com.swyp.plogging.backend.config;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Profile("test")
@Configuration
public class TestConfiguration {

    /*
    테스트를 위한 컨테이너 생성 설정
    애플리케이션 실행 전에 postgre컨테이너를 생성한다.
    테스트 애플리케이션이 생성된 컨테이너에 연결할 수 있도록 datasource설정을 한다.
     */
    @Container
    private static final PostgreSQLContainer<?> postgreContainer;

    static {
        postgreContainer = new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:16-3.4-alpine").asCompatibleSubstituteFor("postgres")
        )
                .withDatabaseName("ploggo")
                .withUsername("test")
                .withPassword("test");
        postgreContainer.start();

        String originalJdbcUrl = postgreContainer.getJdbcUrl() + "?rewriteBatchedStatements=true";


        System.setProperty("spring.datasource.url", originalJdbcUrl);
        System.setProperty("spring.datasource.username", postgreContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreContainer.getPassword());

    }

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return postgreContainer;
    }

    @PreDestroy
    public void stop() {
        if (postgreContainer != null && postgreContainer.isRunning()) {
            postgreContainer.stop();
        }
    }

    /**
     * jdbc:h2:mem:testdb → 메모리 DB. 테스트용으로 자주 사용.<br>
     * DB_CLOSE_DELAY=-1 → 커넥션이 닫혀도 DB를 유지 (테스트 중 여러 번 접근 가능).<br>
     * DB_CLOSE_ON_EXIT=FALSE → JVM 종료 시까지 DB 유지.
     * @return DataSource
     */
//    @Bean
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//                .driverClassName("org.h2.Driver")
//                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
//                .username("sa")
//                .password("")
//                .build();
//    }
//
//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource){
//        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//        emf.setDataSource(dataSource);
//        emf.setPackagesToScan("com.swyp.plogging.backend"); // 엔티티 패키지 경로로 수정
//        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//
//        Properties props = new Properties();
//        props.setProperty("hibernate.hbm2ddl.auto", "create");
//        props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
//        emf.setJpaProperties(props);
//        return emf;
//    }
//
//    @PostConstruct
//    public void setSystemProperty(){
//        System.setProperty("vworld.api.key", "2BA2A9B6-0003-3CEF-9AEF-0702B4A9E22C");
//        System.setProperty("naver.map.client-id", "Dummy-test-id");
//        System.setProperty("naver.map.client-secret", "Dummy-test-secret");
//        System.setProperty("naver.search.client-id", "Dummy-test-id");
//        System.setProperty("naver.search.client-secret", "Dummy-test-secret");
//        System.setProperty("spring.security.oauth2.client.registration.google.client-id", "test");
//        System.setProperty("spring.security.oauth2.client.registration.google.client-secret", "test");
//        System.setProperty("spring.security.oauth2.client.registration.kakao.client-id", "test");
//        System.setProperty("spring.security.oauth2.client.registration.kakao.client-secret", "test");
//    }

}
