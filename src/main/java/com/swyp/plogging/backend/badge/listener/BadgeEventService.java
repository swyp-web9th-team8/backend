package com.swyp.plogging.backend.badge.listener;

import com.swyp.plogging.backend.badge.domain.Badge;
import com.swyp.plogging.backend.badge.event.CompletePostEvent;
import com.swyp.plogging.backend.badge.repository.BadgeRepository;
import com.swyp.plogging.backend.post.sevice.PostService;
import com.swyp.plogging.backend.user.domain.UserBadge;
import com.swyp.plogging.backend.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeEventService {
    private final PostService postService;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    @EventListener
    @Transactional
    public void awardBadgeToUser(CompletePostEvent event){
        Long count = postService.getCountCompletedPostByWriter(event.getAppUser());
        List<UserBadge> userBadges = userBadgeRepository.findByUser(event.getAppUser());

        List<Badge> badges = badgeRepository.findByRequiredActivitiesForBadgeLessThanEqual(count.intValue());
        for(Badge badge : badges){
            // 첫 베지는 그냥 넣기
            if(userBadges.isEmpty()){
                log.info("유저 뱃지 생성");
                userBadgeRepository.save(UserBadge.newInstance(event.getAppUser(),badge));
            }

            // 같은 베지가 있는지 확인하고 없으면 넣기
            for(UserBadge userBadge : userBadges){
                if(badge.getId().equals(userBadge.getBadge().getId())){
                    continue;
                }
                // 얻을 수 있는 벳지중 없는게 있다면 저장
                log.info("유저 뱃지 생성");
                userBadgeRepository.save(UserBadge.newInstance(event.getAppUser(),badge));
            }
        }
    }
}
