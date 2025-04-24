package com.swyp.plogging.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfiguration {

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper ob = new ObjectMapper();
        ob.registerModule(new JavaTimeModule());
        return ob;
    }
}
