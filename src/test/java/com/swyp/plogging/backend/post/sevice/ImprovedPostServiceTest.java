package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.common.ConcurrencyUtil;
import com.swyp.plogging.backend.common.TestFactory;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/insert_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class ImprovedPostServiceTest {

    @Autowired
    private ParticipationService participationService;
    @Autowired
    private PostService postService;
    @Autowired
    private RegionService regionService;

    private static AppUser user;
    private static Post post;



    @BeforeEach
    public void setUp(){
        user = TestFactory.newUser("사용자1", "user@gmail.com");
        post = TestFactory.newPostWithUser(user);
        System.out.println(user);
        System.out.println(post);
    }

    @Test
    @DisplayName("기존 테스트 - 100회")
    public void test_pessimisticLockWithOneTime(TestInfo testInfo) throws InterruptedException {
        log.info("=== ({}) 테스트 시작 ===", testInfo.getDisplayName());
        // given
        List<AppUser> users = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            users.add(TestFactory.newUser("유저"+i,"유저"+i+"@gmail.com"));
        }
        long startTime = System.currentTimeMillis();

        // when
        ConcurrencyUtil.executeConflictingParticipate(
                post.getId(),
                users,
                32,
                (postId, user) -> participationService.participateToPost(postId, user)
        );
        long endTime = System.currentTimeMillis();

        // then
        PostDetailResponse dto = postService.getPostDetails(post.getId(), user.getId());
        log.info("=== 테스트 결과 ===");
        log.info("테스트 시간: {}ms", endTime - startTime);
        log.info("현재 참여자 == 실제 참여자: {}",dto.getParticipants().size());
    }
}
