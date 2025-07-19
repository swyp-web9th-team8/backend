package com.swyp.plogging.backend.post.post.repository;

import com.swyp.plogging.backend.post.post.domain.PostAggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAggregationRepository extends JpaRepository<PostAggregation, Long> {
    @Query("SELET pa FROM post_aggregation pa ORDER BY pa.total_count desc LIMIT 10")
    List<PostAggregation> findTop10ByTotalCount();
}
