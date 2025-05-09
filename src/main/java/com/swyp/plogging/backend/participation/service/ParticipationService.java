package com.swyp.plogging.backend.participation.service;

import com.swyp.plogging.backend.participation.dto.ParticipatedPostResponse;
import com.swyp.plogging.backend.participation.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;

    @Transactional
    public Page<ParticipatedPostResponse> getParticipatedPosts(Long userId, Pageable pageable) {
        return participationRepository.findParticipatedPostsByUserId(userId, pageable);
    }
}
