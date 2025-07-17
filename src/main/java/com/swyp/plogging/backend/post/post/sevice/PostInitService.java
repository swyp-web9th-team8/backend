package com.swyp.plogging.backend.post.post.sevice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostInitService implements ApplicationListener<ApplicationReadyEvent> {
    private final PostService postService;

    //어플리케이션 초기화 시 빈 생성 후 아직 AOP 프록시가 적용이 안되어 있어 Lazy로딩이 안된다.
    // 따라서, 어플리케이션이 완전히 로딩된 후 발생하는 이벤트(ApplicationReadyEvent)를 사용하여 초기화를 진행한다.
    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Pageable pageable = PageRequest.of(0, 30, Sort.by(Sort.Order.desc("meetingDt")));
        postService.cachedCompletedPostInfo = postService.getListOfCompletePostInfo(pageable, "", false, true).getContent();
    }
}
