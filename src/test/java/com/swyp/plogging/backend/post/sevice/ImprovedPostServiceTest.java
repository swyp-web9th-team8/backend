package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.common.ConcurrencyUtil;
import com.swyp.plogging.backend.common.TestFactory;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.post.post.sevice.PostWithRedisNamedLockFacade;
import com.swyp.plogging.backend.region.domain.Region;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test", "redisTest"})
@Sql(scripts = "/sql/insert_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class ImprovedPostServiceTest {

    @Autowired
    private ParticipationService participationService;
    @Autowired
    private PostService postService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private PostWithRedisNamedLockFacade postNamedLockFacade;

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

    @Test
    @DisplayName("네임드락 테스트 - 100회")
    public void test_namedLockWith100Times(TestInfo testInfo) throws InterruptedException {
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
                (postId, user) -> {
                    postNamedLockFacade.participateWithNamedLock(postId, user);
                }
        );
        long endTime = System.currentTimeMillis();

        // then
        PostDetailResponse dto = postService.getPostDetails(post.getId(), user.getId());
        log.info("=== 테스트 결과 ===");
        log.info("테스트 시간: {}ms", endTime - startTime);
        log.info("현재 참여자: {}, 실제 참여자: {}",dto.getCurParticipants(), dto.getParticipants().size());
    }

    @Test
    @DisplayName("기존 모임 생성 기능 테스트")
    public void test_createPost(TestInfo testInfo){
        log.info("=== ({}) 테스트 시작 ===", testInfo.getDisplayName());
        for(int i = 0; i < 5;i++) {
            TestFactory.newUserAdd("사용자1", "user@gmail.com");
        }
        long startTime = System.currentTimeMillis();

        PostDetailResponse dto = postService.createPost(user,
                "제목1", "내용1",
                LocalDateTime.now().plusMinutes(60), "1L",
                "placeId", "강남구 테헤란로 127",
                5, "openChatUrl",
                30);

        long endTime = System.currentTimeMillis();

        log.info("=== 테스트 결과 ===");
        log.info("테스트 시간: {}ms", endTime - startTime);
        log.info("모임 생성 트랜잭션 내에서 외부 API 호출");
    }

    @Test
    @DisplayName("개선된 모임 생성 기능 테스트")
    public void test_improvedCreatePost(TestInfo testInfo){
        log.info("=== ({}) 테스트 시작 ===", testInfo.getDisplayName());

        long startTime = System.currentTimeMillis();

        PostDetailResponse dto = postService.improvedCreatePost(user,
                "제목14343", "내용1",
                LocalDateTime.now().plusMinutes(60), "1L",
                "placeId", "강남구 테헤란로 127",
                5, "openChatUrl",
                30);

        long endTime = System.currentTimeMillis();

        log.info("=== 테스트 결과 ===");
        log.info("테스트 시간: {}ms", endTime - startTime);
        log.info("비동기 방식으로 외부 API 호출");
        try{
            Thread.sleep(5000);
            check(dto.getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public void check(Long id){
        Post testTarget = postService.findById(id);
        log.info("리전 생성 확인 : {} {}", testTarget.getTitle(), testTarget.getRegionId());

        Region region = regionService.getRegionById(testTarget.getRegionId().getId());
        Assertions.assertEquals(region.getId(), testTarget.getRegionId().getId());
    }
}
