package com.swyp.plogging.backend.participation.service;

import com.swyp.plogging.backend.common.exception.NotParticipatingPostException;
import com.swyp.plogging.backend.participation.domain.Participation;
import com.swyp.plogging.backend.participation.repository.ParticipationRepository;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.sevice.PostService;
import com.swyp.plogging.backend.post.sevice.PostServiceTest;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.AuthProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ParticipationServiceTest {

    @InjectMocks
    ParticipationService participationService;

    @Mock
    PostService postService;
    @Mock
    ParticipationRepository participationRepository;


    private static Post data;
    private static AppUser user;
    private static AppUser user2;

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);

    @BeforeEach
    public void createData() {
        user = AppUser.newInstance("user@user.com", "user1", "Soeul", AuthProvider.valueOf("GOOGLE"), null);
        user2 = AppUser.newInstance("user2@user.com", "user2", "Seoul", AuthProvider.KAKAO, null);
        data = Post.builder()
            .id(1L)
            .writer(user)
            .title("생성 시험")
            .content("생성 시험 내용")
            .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
            .placeId("1")
            .placeName("서울시청")
            .address("서울특별시 중구 세종대로 126")
            .maxParticipants(2)
            .openChatUrl("https://open.kakao.com/몰라")
            .build();
        data.setUpDeadLine(null);
    }

    @Test
    @DisplayName("모임 참여 기능 구현")
    public void participationToPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        when(postService.findById(postId)).thenReturn(data);

        //when
        participationService.participateToPost(postId, user2);

        //then
        verify(participationRepository, times(1)).save(any(Participation.class));
        Assertions.assertEquals(1, data.getParticipations().size());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 참여 기능 예외1 - 최대 참가자")
    public void isMaxParticipantsOfPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        Queue<Participation> queue = new LinkedList<>();
        queue.add(Participation.newInstance(data, user2));
        queue.add(Participation.newInstance(data, AppUser.newInstance("1", "1", "1", AuthProvider.GOOGLE, null)));

        when(postService.findById(postId)).thenReturn(data);
        when(participationRepository.save(any(Participation.class))).thenReturn(queue.poll());

        participationService.participateToPost(postId, user2);
        participationService.participateToPost(postId, AppUser.newInstance("1", "1", "1", AuthProvider.GOOGLE, null));

        //when
        Exception e = Assertions.assertThrows(NotParticipatingPostException.class, () ->
            participationService.participateToPost(postId, AppUser.newInstance("2", "2", "2", AuthProvider.GOOGLE, null)));

        //then
        verify(participationRepository, times(2)).save(any(Participation.class));
        Assertions.assertEquals("해당 모임에 참석할 수 없습니다.", e.getMessage());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 참여 기능 예외2 - 이미 참가한 참가자")
    public void alreadyParticipatedToPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        when(postService.findById(postId)).thenReturn(data);
        when(participationRepository.save(any(Participation.class))).thenReturn(Participation.newInstance(data, user2));
        participationService.participateToPost(postId, user2);

        //when
        Exception e = Assertions.assertThrows(NotParticipatingPostException.class, () ->
            participationService.participateToPost(postId, user2));

        //then
        verify(participationRepository, times(1)).save(any(Participation.class));
        Assertions.assertEquals(user2.getNickname() + " 님은 이미 참가중입니다.", e.getMessage());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 나가기 기능 구현")
    public void leaveFromPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        data.addParticipation(Participation.newInstance(data, user2));
        when(postService.findById(postId)).thenReturn(data);

        //when
        participationService.leaveFromPost(postId, user2);

        //then
        verify(participationRepository, times(1)).delete(any(Participation.class));
        Assertions.assertEquals(0, data.getParticipations().size());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 나가기 기능 예외 점검")
    public void exceptionOfLeaveFromPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        data.addParticipation(Participation.newInstance(data, user));
        when(postService.findById(postId)).thenReturn(data);

        //when
        Exception e = Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                participationService
                    .leaveFromPost(postId, user2));

        //then
        Assertions.assertEquals("참가하지 않은 모임입니다.", e.getMessage());
        Assertions.assertEquals(1, data.getParticipations().size());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }
}
