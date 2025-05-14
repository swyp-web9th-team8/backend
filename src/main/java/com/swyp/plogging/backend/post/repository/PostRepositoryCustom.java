package com.swyp.plogging.backend.post.repository;


import com.swyp.plogging.backend.post.domain.Post;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findPostByCondition(Pageable pageable, Boolean recruitmentCompleted, Boolean completed);

    // 추가: 위치 기반 검색 메서드
    Page<Post> findNearbyPosts(Double latitude, Double longitude, Double radiusKm, Pageable pageable);

    Page<Post> findPostByRegion(MultiPolygon polygons, Pageable pageable, String keyword);
}
