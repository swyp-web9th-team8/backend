package com.swyp.plogging.backend.post.post.sevice;

import com.swyp.plogging.backend.common.exception.NotParticipatingPostException;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Profile("redis")
@Service
@RequiredArgsConstructor
public class PostWithRedisNamedLockFacade {
    private final RedissonClient redissonClient;
    private final ParticipationService participationService;
    private final String POST_LOCK_PREFIX = "P_0:";
    private final int RETRY_LIMIT = 20;

    public void participateWithNamedLock(Long postId, AppUser user) {
        RLock lock = redissonClient.getLock(POST_LOCK_PREFIX + postId);
        boolean acquired = false;
        int retryCount = 0;

        try {
            while (retryCount < RETRY_LIMIT) {
                acquired = lock.tryLock(10L, -1L, TimeUnit.SECONDS);
                if (!acquired) {
                    log.info("락획득 실패, 유저: {}, 재시도 횟수: {}", user.getNickname(), retryCount++);
                    Thread.sleep(50); // ms
                    continue;
                }
                participationService.participateToPostWithLock(postId, user);
                return;
            }
        } catch (NotParticipatingPostException npe) {
            log.info("참여 실패, 유저: {}, 재시도 횟수: {}, 에러: {}", user.getNickname(), retryCount, npe.getMessage());
        } catch (Exception e) {
            log.info("참여 실패, 유저: {}, 재시도 횟수: {}, 에러: {}", user.getNickname(), retryCount, e.getMessage());
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
