package com.swyp.plogging.backend.post.post.repository;


import com.swyp.plogging.backend.post.post.controller.dto.PostAggregationDto;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    Page<Post> findPostByCondition(MultiPolygon multiPolygon,Pageable pageable, Boolean recruitmentCompleted, Boolean completed);

    // 추가: 위치 기반 검색 메서드
    Page<Post> findNearbyPosts(Double latitude, Double longitude, Double radiusKm, Pageable pageable);

    Page<Post> findPostByRegion(MultiPolygon polygons, Pageable pageable, String keyword);

    List<Post> find10ByCompletedAndNotCertificated(Long writerId);

    List<PostAggregationDto> getTotalPostCountAndTotalParticipationCountByUsersOrderById(List<AppUser> users);
}
