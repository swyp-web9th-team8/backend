package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.common.exception.NotParticipatingPostException;
import com.swyp.plogging.backend.post.domain.Participation;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.ParticipationRepository;
import com.swyp.plogging.backend.user.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipationService {
    private final ParticipationRepository participationRepository;
    private final PostService postService;

    public ParticipationService(ParticipationRepository participationRepository, PostService postService){
        this.participationRepository = participationRepository;
        this.postService = postService;
    }

    @Transactional
    public void participateToPost(Long postId, AppUser user) {
        Post target = postService.findById(postId);
        // todo 오너일 경우 제외

        // 남은 자리 없음
        if(target.isMax()){
            throw new NotParticipatingPostException();
        }

        // 이미 참가중
        if(target.isParticipating(user) != null){
            throw new NotParticipatingPostException(user);
        }

        // 참여 생성 및 Post 연결
        Participation participation = Participation.newInstance(target, user);
        participation = participationRepository.save(participation);

        target.addParticipation(participation);
    }

    @Transactional
    public void leaveFromPost(Long postId, AppUser user) {
        Post target = postService.findById(postId);
        // todo except owner

        Participation participation = target.leave(user);
        // 잘못된 접근 제외
        if(participation == null){
            throw new IllegalArgumentException("참가하지 않은 모임입니다.");
        }

        // 참가목록 제거
        participationRepository.delete(participation);
    }
}
