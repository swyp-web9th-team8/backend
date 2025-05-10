package com.swyp.plogging.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.badge-icon-dir}")
    private String badgeIconDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/images/**")
            .addResourceLocations("file:" + uploadDir + "/");

        registry.addResourceHandler("/badges/icons/**")
            .addResourceLocations("file:" + badgeIconDir + "/");
    }
}
