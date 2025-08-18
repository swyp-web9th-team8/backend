package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.common.TestFactory;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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
        log.info(user.toString() + post.toString());
    }

}
