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
    @DisplayName("비관적락 테스트 - 1회")
    public void test_pessimisticLockWithOneTime(){
        log.info("비관적락 테스트 - 락 획득 테스트");
        log.info("현재 참여자: " + post.getCurParticipants());
        AppUser user1 = TestFactory.newUser("유저1", "유저1@gmail.com");
        participationService.participateToPost(post.getId(), user1);
        post = postService.findById(post.getId());
        log.info("최종 참여자: " + post.getCurParticipants());
    }

    @Test
    @DisplayName("100회 동시 참여 - 실패")
    public void test_nonPessimisticLockWith20Times(TestInfo testInfo) throws InterruptedException {
        // given
        log.info("=== ({}) 테스트 시작 ===",testInfo.getDisplayName());
        long startTime = System.currentTimeMillis();
        List<AppUser> users  = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            users.add(TestFactory.newUser("유저" + i, "유저" +i +"@gmail.com"));
        }

        // when
        post = TestFactory.newPostWithUser(user);
        ConcurrencyUtil.executeConflictingParticipate(
                post.getId(),
                users,
                users.size(),
                (postId, user) ->  participationService.participateToPost(postId, user)
                );
        long endTime = System.currentTimeMillis();
        // then
        post = postService.findById(post.getId());
        PostDetailResponse dto = postService.getPostDetails(post.getId(), user.getId());
        log.info("=== 테스트 결과 ===");
        log.info("테스트 시간: {}ms",endTime - startTime);
        log.info("모임의 참여자 수: {}, 실제 참여자 수: {}",post.getCurParticipants(), dto.getParticipants().size());
    }

    @Test
    @DisplayName("비관적락 테스트 100회 동시 참여")
    public void test_pessimisticLockWith20Times(TestInfo testInfo) throws InterruptedException {
        // given
        log.info("=== ({}) 테스트 시작 ===",testInfo.getDisplayName());
        long startTime = System.currentTimeMillis();
        List<AppUser> users  = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            users.add(TestFactory.newUser("유저" + i, "유저" +i +"@gmail.com"));
        }
        post = TestFactory.newPostWithUser(user);

        // when
        ConcurrencyUtil.executeConflictingParticipate(
                post.getId(),
                users,
                users.size(),
                (postId, user) ->  participationService.participateToPostWithLock(postId, user)
                );
        long endTime = System.currentTimeMillis();
        // then
        post = postService.findById(post.getId());
        PostDetailResponse dto = postService.getPostDetails(post.getId(), user.getId());
        log.info("=== 테스트 결과");
        log.info("테스트 시간: {}ms",endTime - startTime);
        log.info("모임의 참여자 수: {}, 실제 참여자 수: {}",post.getCurParticipants(), dto.getParticipants().size());
    }
}
