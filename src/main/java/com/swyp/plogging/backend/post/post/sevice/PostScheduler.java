package com.swyp.plogging.backend.post.post.sevice;

import com.swyp.plogging.backend.notification.event.NotiType;
import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostScheduler {

    private final PostService postService;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 */10 * * * *")
    public void meetingCompleteProcess() {
        log.info("------------> Start of a scheduled task - meetingCompleteProcess.");
        List<Post> targetPosts = postRepository.findAllByMeetingDtBeforeAndCompletedFalse(LocalDateTime.now());
        targetPosts.forEach(post -> {
            post.complete();
            NotificationEvent event = new NotificationEvent();
            event.setStrategy(NotiStrategy.FCM);
            event.setType(NotiType.REVIEW);
            event.setPostId(post.getId());
            event.setUser(post.getWriter());
            log.info("-----{}에게 독촉 알림 보내기", post.getWriter().getNickname());
            eventPublisher.publishEvent(event);
        });
        log.info("------------> End of a scheduled task meetingCompleteProcess.");
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void fillRegionOfPost(){
        List<Post> posts = postService.findTop100ByRegionIdIsNull();
        if(!posts.isEmpty()) {
            postService.fillRegion(posts);
            log.info(posts.size() + "개의 모임에 지역을 채워 넣었습니다.");
        }
    }
}
