package com.swyp.plogging.backend.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class FCMConfig {
    @PostConstruct
    public void init() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(getCredentailFileInputStream()))
                .build();

        FirebaseApp.initializeApp(options);
    }

    @Bean
    public static FileInputStream getCredentailFileInputStream() throws FileNotFoundException {
        // service-account.json 필요
        return new FileInputStream("service-account.json");
    }
}
