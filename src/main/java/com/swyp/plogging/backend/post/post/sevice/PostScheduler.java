package com.swyp.plogging.backend.post.post.sevice;

import com.swyp.plogging.backend.notification.event.NotiType;
import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.post.post.controller.dto.PostAggregationDto;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.domain.PostAggregation;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.repository.PostAggregationRepository;
import com.swyp.plogging.backend.user.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostScheduler {

    private final PostService postService;
    private final PostRepository postRepository;
    private final PostAggregationRepository aggregationRepository;
    private final UserService userService;
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
        List<Post> posts = postRepository.findTop100ByRegionIdIsNull();
        if(!posts.isEmpty()) {
            postService.fillRegion(posts);
            log.info(posts.size() + "개의 모임에 지역을 채워 넣었습니다.");
        }
    }

    /**
     * 1시간 마다 캐싱을 위한 완료모임 30개 조회
     */
    @Scheduled(cron = "0 0 0/1 * * *")
    public void renewCachedCompletedPosts(){
        Pageable pageable = PageRequest.of(0, 30, Sort.by(Sort.Order.desc("meetingDt")));
        postService.cachedCompletedPostInfo = postService.getListOfCompletePostInfo(pageable, "", false, true).getContent();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void countTotalPostAndTotalParticipationByUser(){
        Long latestId = 0L;
        while(true){
            List<AppUser> users = userService.findTop100ByIdIsGreaterThanEqualOrderById(latestId);
            if(users.isEmpty()){
                break;
            }
            List<PostAggregationDto> dtos = postRepository.getTotalPostCountAndTotalParticipationCountByUsersOrderById(users);
            Queue<AppUser> userQueue = new LinkedList<>(users);
            Queue<PostAggregationDto> dtoQueue = new LinkedList<>(dtos);
            PostAggregationDto dto = null;
            AppUser user = null;
            while(!userQueue.isEmpty()){
                dto = dtoQueue.poll();
                user = userQueue.poll();
                if(dto != null  && user != null && dto.getId().equals(user.getId())){
                    Optional<PostAggregation> opAggregation = aggregationRepository.findById(user.getId());
                    PostAggregation aggregation;
                    if(opAggregation.isPresent()){
                        System.out.println("업데이트");
                        aggregation = opAggregation.get();
                        aggregation.updateCounts(dto.getTotalPostCount(), dto.getTotalParticipationCount());
                    }else{
                        System.out.println("새로 생성");
                        aggregation = new PostAggregation(user.getId(), user, dto.getTotalPostCount(), dto.getTotalParticipationCount());
                    }
                    aggregationRepository.save(aggregation);
                }
            }
            if(user != null){
                latestId = user.getId() + 1;
            }
        }

    }
}
