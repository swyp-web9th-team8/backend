package com.swyp.plogging.backend.post.post.repository;

import com.swyp.plogging.backend.post.post.domain.PostAggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAggregationRepository extends JpaRepository<PostAggregation, Long> {
    List<PostAggregation> findTop10ByOrderByTotalCountDesc();
}
