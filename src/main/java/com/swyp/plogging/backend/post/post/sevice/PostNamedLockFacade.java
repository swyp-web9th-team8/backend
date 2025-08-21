package com.swyp.plogging.backend.post.post.sevice;

import com.swyp.plogging.backend.common.exception.NotParticipatingPostException;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Profile("!redis")
@Service
@RequiredArgsConstructor
public class PostNamedLockFacade {
    private final PostRepository postRepository;
    private final ParticipationService participationService;
    private final int RETRY_DELAY = 50; //ms
    private final int RETRY_LIMIT = 20;


    @Transactional
    public void participateWithNamedLock(Long postId, AppUser user) throws InterruptedException {
        int retryCount = 0;
        boolean getLock = false;
        try {
            while (retryCount <= RETRY_LIMIT) {
                getLock = postRepository.getTryAdvisoryXactLock(postId); // 트랜잭션 레벨 언락
                if (!getLock) {
                    retryCount++;
                    log.info("락 획득 실패, 유저: {}, 재시도 횟수: {}",user.getNickname(), retryCount);
                    Thread.sleep(RETRY_DELAY);
                } else {
                    participationService.participateToPostWithLock(postId, user);
                    return;
                }
            }
        }catch(NotParticipatingPostException npe){
            log.info("참여 실패, 유저: {}, 재시도 횟수: {}, 에러: {}",user.getNickname(), retryCount, npe.getMessage());
        }catch(Exception e){
            log.info("참여 실패, 유저: {}, 재시도 횟수: {}, 에러: {}",user.getNickname(), retryCount, e.getMessage());
        }
    }
}
