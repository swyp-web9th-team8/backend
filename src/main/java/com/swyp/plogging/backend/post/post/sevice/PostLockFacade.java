package com.swyp.plogging.backend.post.post.sevice;

import com.swyp.plogging.backend.common.exception.NotParticipatingPostException;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostLockFacade {
    private final int RETRY_DELAY_MS = 50;
    private final int RETRY_LIMIT = 20;
    private final ParticipationService participationService;

    public void participateToPost(Long postId, AppUser user) throws InterruptedException {
        int retryCount = 0;

        while(retryCount <= RETRY_LIMIT){
            try {
                participationService.participateToPostWithLock(postId, user);
                log.info("모임 참석에 성공했습니다.유저: {}, 실패 횟수: {}", user.getNickname(), retryCount);
                return;
            }catch(NotParticipatingPostException npe){
                log.warn("모임 참석에 실패했습니다. 유저: {}, 실패 횟수: {}, 에러 메세지: {}",user.getNickname(), retryCount, npe.getMessage());
                return;
            }catch(Exception e){
                log.warn("모임 참석에 실패했습니다. 유저: {}, 실패 횟수: {}, 에러 메세지: {}",user.getNickname(), retryCount++, e.getMessage());
                Thread.sleep(RETRY_DELAY_MS);
            }
        }
    }
}
