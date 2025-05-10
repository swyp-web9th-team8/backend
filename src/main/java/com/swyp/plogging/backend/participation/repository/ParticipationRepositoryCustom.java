package com.swyp.plogging.backend.participation.repository;

import com.swyp.plogging.backend.participation.dto.ParticipatedPostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParticipationRepositoryCustom {

    Page<ParticipatedPostResponse> findParticipatedPostsByUserId(Long userId, Pageable pageable);
}
