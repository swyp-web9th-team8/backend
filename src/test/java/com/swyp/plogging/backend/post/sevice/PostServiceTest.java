package com.swyp.plogging.backend.post.sevice;


import com.swyp.plogging.backend.region.service.LocationService;
import com.swyp.plogging.backend.common.util.RoadAddressUtil;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.region.domain.Region;
import com.swyp.plogging.backend.post.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.domain.AuthProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    PostService postService;
    RoadAddressUtil roadAddressUtil;

    @Mock
    PostRepository postRepository;
    @Mock
    RegionService regionService;
    @Mock
    LocationService locationService;
    private static Post data;
    private static AppUser user;
    private static Point point;

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);

    @BeforeEach
    public void createData() throws Exception {
        user = AppUser.newInstance("user@user.com", "user", "Seoul", AuthProvider.GOOGLE, null);
        setEntityId(user, 1L);
        WKTReader rd = new WKTReader();
        point = (Point) rd.read(String.format("POINT(%f %f)", 23.2, 23.2));
        data = Post.builder()
            .id(1L)
            .writer(user)
            .title("생성 시험")
            .content("생성 시험 내용")
            .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
            .placeId("1")
            .placeName("서울시청")
            .address("서울특별시 중구 세종대로 126")
            .maxParticipants(10)
            .openChatUrl("https://open.kakao.com/몰라")
            .latitude(23.2)
            .longitude(23.2)
            .location(point)
            .build();
        data.setUpDeadLine(null);
        roadAddressUtil = new RoadAddressUtil();
        roadAddressUtil.init();
    }

    @Test
    @DisplayName("모임 생성 테스트")
    public void createPostTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post expected = data;
        List<Map<String, Object>> mapList = List.of(
            Map.of("latitude", Double.parseDouble("23.2"), "longitude", Double.parseDouble("23.2"), "roadAddress", "서울특별시 중구 세종대로 125"));
        when(postRepository.save(any(Post.class))).thenReturn(expected);
        when(locationService.searchCoordinatesByAddress(any(String.class))).thenReturn(mapList);
        when(locationService.createPoint(Double.parseDouble("23.2"), Double.parseDouble("23.2"))).thenReturn(point);

        //when
        PostDetailResponse dto = postService.createPost(user,
            "생성 시험",
            "생성 시험 내용",
            LocalDateTime.parse("2025-04-29T10:40:32"),
            "1",
            "서울시청",
            "서울특별시 중구 세종대로 125",
            10,
            "https://open.kakao.com/몰라",
            null);
        //then
        Assertions.assertEquals(dto.getId(), 1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험");
        Assertions.assertEquals(dto.getDeadLine(), expected.getMeetingDt().minusMinutes(30));
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 수정 테스트")
    public void modifyPostTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post given = data;
        when(postRepository.findById(1L)).thenReturn(Optional.of(given));

        //when
        PostDetailResponse dto = postService.modifyPost(user,
            1L,
            "생성 시험2",
            "생성 시험 내용2",
            null,
            null,
            null,
            null,
            null,
            null,
            60);
        //then
        Assertions.assertEquals(dto.getId(), 1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험2");
        Assertions.assertEquals(dto.getMeetingTime(), given.getMeetingDt());
        Assertions.assertEquals(dto.getDeadLine(), given.getMeetingDt().minusMinutes(60));

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 삭제 테스트")
    public void deletePostTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post given = data;
        when(postRepository.findById(1L)).thenReturn(Optional.of(given));

        //when
        postService.deletePost(1L, user);

        //verify
        verify(postRepository, times(1)).delete(given);
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 상세정보 조회 테스트")
    public void getPostDetailsTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post given = data;
        when(postRepository.findById(1L)).thenReturn(Optional.of(given));

        //when
        PostDetailResponse dto = postService.getPostDetails(1L, 1L);

        //then
        Assertions.assertEquals(dto.getId(), 1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험");
        Assertions.assertEquals(dto.getMeetingTime(), given.getMeetingDt());

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 정보목록 조회 테스트")
    public void getPostListTest(TestInfo testInfo) throws Exception {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        List<Post> givenList = new ArrayList<>();

        //given
        for (int i = 0; i < 20; i++) {
            Post post = Post.builder()
                .id((long) i)
                .writer(user)
                .title("생성 시험" + i)
                .content("생성 시험 내용" + i)
                .meetingDt(LocalDateTime.now().minusMinutes(100 - i))
                .placeId("" + i)
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 125")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .build();
            post.setUpDeadLine(30);
            givenList.add(post);
        }
        Region region = new Region("서울특별시", "강남구", "역삼동", "1111");
        region.setPolygons(new MultiPolygon(new Polygon[]{}, new PrecisionModel(), 4326));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("meetingTime").descending());
        Boolean recruitmentCompleted = false;
        Boolean completed = false;
        when(postRepository.findPostByRegion(region.getPolygons(), pageable, "")).thenReturn(
            new PageImpl<>(givenList, pageable, givenList.size()));
        when(regionService.findByDistrictAndNeighborhood(any(String.class), any(String.class))).thenReturn(Optional.of(region));

        //when
        Page<PostInfoResponse> dto = postService.getListOfPostInfo(pageable, "서울특별시 강남구 역삼동", "", user);

        //then
        Assertions.assertEquals(dto.getNumber(), pageable.getPageNumber());
        Assertions.assertEquals(dto.getTotalElements(), givenList.size());

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    private void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
