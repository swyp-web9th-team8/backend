package com.swyp.plogging.backend.common;


import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConcurrencyUtil {

    @FunctionalInterface
    public interface Participatable{
        void participate(Long postId, AppUser user);
    }

    public static void executeConflictingParticipate(
            Long postId,
            List<AppUser> users,
            int threadNum,
            Participatable participationJob) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        CountDownLatch count = new CountDownLatch(users.size());

        for(int i = 0; i < users.size(); i++){
            final int idx = i;
            executorService.submit(() -> {
                try{
                    participationJob.participate(postId, users.get(idx));
                }catch (Exception e){
                    log.error("참가 실패 : {}", e.getMessage());
                }finally{
                    count.countDown();
                }
            });
        }
        count.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
    }
}
