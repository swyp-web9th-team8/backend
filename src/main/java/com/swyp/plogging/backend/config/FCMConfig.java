package com.swyp.plogging.backend.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FCMConfig {
    @Autowired
    private static Environment env;
    @PostConstruct
    public void init() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(getCredentailFileInputStream()))
                .build();

        FirebaseApp.initializeApp(options);
    }

    public static FileInputStream getCredentailFileInputStream() throws IOException {
        String[] activeProfiles =  env.getActiveProfiles();
        // service-account.json 필요
        for(String profile : activeProfiles){
            if("prod".equals(profile)){
                return (FileInputStream) new ClassPathResource("service_account.json").getInputStream();
            }
        }
        return new FileInputStream("src/main/resources/service_account.json");
    }
}
