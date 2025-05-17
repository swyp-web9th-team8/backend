package com.swyp.plogging.backend.participation.service;

import com.swyp.plogging.backend.common.exception.NotParticipatingPostException;
import com.swyp.plogging.backend.participation.domain.Participation;
import com.swyp.plogging.backend.participation.dto.MyPostResponse;
import com.swyp.plogging.backend.participation.repository.ParticipationRepository;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.PostRepository;
import com.swyp.plogging.backend.post.sevice.PostService;
import com.swyp.plogging.backend.user.domain.AppUser;
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
    private final PostService postService;
    private final PostRepository postRepository;

    @Transactional
    public void participateToPost(Long postId, AppUser user) {
        Post target = postService.findById(postId);

        // 작성자 제외
        if (target.isWriter(user)) {
            throw new NotParticipatingPostException(user);
        }

        // 남은 자리 없음
        if (target.isMax()) {
            throw new NotParticipatingPostException();
        }

        // 이미 참가중
        if (target.isParticipating(user) != null) {
            throw new NotParticipatingPostException(user);
        }

        // 참여 생성 및 Post 연결
        Participation participation = Participation.newInstance(target, user);
        participation = participationRepository.save(participation);

        target.addParticipation(participation);
        // todo 이부분은 책임 분리 개선 필요
        postRepository.save(target);
    }

    @Transactional
    public void leaveFromPost(Long postId, AppUser user) {
        Post target = postService.findById(postId);

        if (target.isWriter(user)) {
            throw NotParticipatingPostException.isWriter();
        }

        Participation participation = target.leave(user);
        // 잘못된 접근 제외
        if (participation == null) {
            throw new IllegalArgumentException("참가하지 않은 모임입니다.");
        }

        // 참가목록 제거
        participationRepository.delete(participation);
    }

    public Page<MyPostResponse> getParticipatedPosts(Long userId, Pageable pageable) {
        return participationRepository.findParticipatedPostsByUserId(userId, pageable);
    }

    public Page<MyPostResponse> getCreatedPosts(Long userId, Pageable pageable) {
        return participationRepository.findCreatedPostsByUserId(userId, pageable);
    }
}
