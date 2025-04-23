package com.swyp.plogging.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfiguration {

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper ob = new ObjectMapper();

        //타임 모듈 추가 필요
        //ob.registerModule(JSR310Module);
        return ob;
    }
}
