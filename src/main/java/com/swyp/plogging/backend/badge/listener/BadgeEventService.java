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

        // 만들 수 있는 활성 뱃지 다 가져오기
        List<Badge> badges = badgeRepository.findByRequiredActivitiesForBadgeLessThanEqualOrderByRequiredActivitiesForBadgeAsc(count.intValue())
                .stream()
                .filter(badge -> badge.getInactiveBadgeIconDir().isBlank())
                .toList();

        // 비어 있다면 그냥 다 넣기
        if(userBadges.isEmpty()){
            for(Badge badge : badges){
                UserBadge ub = userBadgeRepository.save(UserBadge.newInstance(event.getAppUser(),badge));
                badge.getUserBadges().add(ub);
            }
        }else if(userBadges.size() < badges.size()) {
            List<Badge> awardedBadges = userBadges.stream().map(UserBadge::getBadge).toList();
            badges.stream().filter(badge -> !awardedBadges.contains(badge))
                    .forEach(badge -> {
                        UserBadge ub = userBadgeRepository.save(UserBadge.newInstance(event.getAppUser(), badge));
                        badge.getUserBadges().add(ub);
                    });
        }
    }
}
