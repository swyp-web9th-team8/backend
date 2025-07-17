package com.swyp.plogging.backend.user.user.repository;

import com.swyp.plogging.backend.post.post.domain.PostAggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostAggregationRepository extends JpaRepository<PostAggregation, Long> {
}
