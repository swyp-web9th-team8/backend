package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostScheduler {

    private final PostService postService;
    private final PostRepository postRepository;

    @Scheduled(cron = "0 */10 * * * *")
    public void meetingCompleteProcess() {
        log.info("------------> Start of a scheduled task - meetingCompleteProcess.");
        List<Post> targetPosts = postRepository.findAllByMeetingDtBeforeAndCompletedFalse(LocalDateTime.now());
        targetPosts.forEach(Post::complete);
        log.info("------------> End of a scheduled task meetingCompleteProcess.");
    }
}
