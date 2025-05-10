package com.swyp.plogging.backend.participation.repository;

import com.swyp.plogging.backend.participation.dto.MyPostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParticipationRepositoryCustom {

    Page<MyPostResponse> findParticipatedPostsByUserId(Long userId, Pageable pageable);

    Page<MyPostResponse> findCreatedPostsByUserId(Long userId, Pageable pageable);
}
