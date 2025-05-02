package com.swyp.plogging.backend.repository;


import com.swyp.plogging.backend.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findPostByCondition(Pageable pageable, Boolean recruitmentCompleted, Boolean completed);
}
