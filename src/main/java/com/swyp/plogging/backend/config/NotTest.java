package com.swyp.plogging.backend.config;

import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@RequiredArgsConstructor
@Configuration
public class NotTest {
    private final RegionService regionService;
    private final PostService postService;

    @Bean
    public ApplicationRunner work(){
        return args -> {
            regionService.initRegionData();
            regionService.initRegionPolygon();
            // 운영환경에서는 너무 많은 데이터로 OOM발생하여 주석처리
//            postService.fillRegion();
        };
    }
}
