package com.swyp.plogging.backend.post.sevice;

import static org.assertj.core.api.Assertions.assertThat;

import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.post.post.sevice.PostScheduler;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.domain.AuthProvider;
import com.swyp.plogging.backend.user.user.repository.AppUserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled
class PostSchedulerTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostScheduler postScheduler;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void testMeetingCompleteProcess() {
        AppUser writer = appUserRepository.save(
            AppUser.newInstance(
                "user@user.com",
                "user1",
                "Soeul",
                AuthProvider.valueOf("GOOGLE"),
                null)
        );

        Post post = Post.builder()
            .id(1L)
            .writer(writer)
            .title("생성 시험")
            .content("생성 시험 내용")
            .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
            .placeId("1")
            .placeName("서울시청")
            .address("서울특별시 중구 세종대로 126")
            .maxParticipants(2)
            .openChatUrl("https://open.kakao.com/몰라")
            .build();
        postRepository.save(post);

        postScheduler.meetingCompleteProcess();

        Post updated = postRepository.findById(post.getId()).orElseThrow();
        assertThat(updated.isCompleted()).isTrue();
    }
}