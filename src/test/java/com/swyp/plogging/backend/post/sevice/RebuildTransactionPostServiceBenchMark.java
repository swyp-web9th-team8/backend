package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.common.util.RoadAddressUtil;
import com.swyp.plogging.backend.notification.event.NotiType;
import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.service.FCMPushNotificationService;
import com.swyp.plogging.backend.notification.service.NotificationService;
import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.post.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.repository.PostAggregationRepository;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.region.domain.Region;
import com.swyp.plogging.backend.region.service.LocationService;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.domain.AuthProvider;
import com.swyp.plogging.backend.user.user.service.UserService;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openjdk.jmh.annotations.*;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class RebuildTransactionPostServiceBenchMark {

    private PostService postService;
    private PostRepository postRepository;
    private PostAggregationRepository postAggregationRepository;
    private RegionService regionService;
    private LocationService locationService;
    private NotificationService notificationService;
    private UserService userService;
    private ApplicationEventPublisher eventPublisher;
    private RoadAddressUtil roadAddressUtil;

    private AppUser user;
    private Region region;
    private Point point;
    private List<Map<String, Object>> list;
    private Post post;
    private NotificationEvent event;

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID using reflection", e);
        }
    }

    @Setup
    public void setUp(){
        // mock생성 및 의존성 주입
        postRepository = mock(PostRepository.class);
        postAggregationRepository = mock(PostAggregationRepository.class);
        regionService = mock(RegionService.class);
        locationService = mock(LocationService.class);
        notificationService = mock(FCMPushNotificationService.class);
        userService = mock(UserService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        roadAddressUtil = new RoadAddressUtil();
        roadAddressUtil.init();
        postService = new PostService(postRepository, locationService, regionService,postAggregationRepository, eventPublisher);
        try {
            Field userServiceField = postService.getClass().getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(postService, userService);
            Field notificationServiceField = postService.getClass().getDeclaredField("notificationService");
            notificationServiceField.setAccessible(true);
            notificationServiceField.set(postService, notificationService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // 사용할 데이터 생성
        // 새로운 지역
        region = new Region();
        region.setCity("서울시");
        region.setDistrict("강남구");
        region.setNeighborhood("역삼동");
        region.setCode("code");
        region.setPolygons(new MultiPolygon(new Polygon[]{}, new GeometryFactory()));

        // 새 사용자 생성 - 처음에는 등록되지 않은 상태로 생성
        user = AppUser.newInstanceWithRegion("user1", "user1", region, AuthProvider.GOOGLE, "/images/basic.png");
        user.setGender("Female");
        user.completeRegistration();
        setId(user, 1L);

        // 위치
        String pointWKT = String.format("POINT(%f %f)", 127.0016985 + 0.000004,37.56422135 + 0.00004);
        WKTReader wktReader = new WKTReader();
        try {
            point = (Point) wktReader.read(pointWKT);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        point.setSRID(4326); // WGS84 좌표계

        // API응답
        list = List.of(Map.of("roadAddress", "서울시 강남구 테헤란로 127","latitude",  37.56422135 + 0.00004, "longitude", 127.0016985 + 0.000004));

        // 모임
        post = Post.builder()
                .writer(user)
                .title("제목1")
                .content("내용1")
                .meetingDt(LocalDateTime.now().plusMinutes(60))
                .placeId("placeId")
                .placeName("placeName")
                .address("서울시 강남구 테헤란로 127")
                .latitude(37.56422135 + 0.00004)
                .longitude(127.0016985 + 0.000004)
                .location(point)
                .completed(false)
                .maxParticipants(50)
                .openChatUrl("openChatUrl")
                .build();
        post.setUpDeadLine(30);
        post.updateRegion(region);
        setId(post, 1L);

        // 이벤트
        event = new NotificationEvent(NotiStrategy.FCM, NotiType.CREATE,user,post.getId());

        doAnswer(invocation -> {
            Thread.sleep(50);
            return list;
        }).when(locationService).searchCoordinatesByAddress(any());
        when(locationService.createPoint(any(), any())).thenReturn(point);
        doAnswer(invocation -> {
            Thread.sleep(30);
            return region;
        }).when(regionService).getContainedRegion(any());
        when(postRepository.save(any())).thenReturn(post);
        doAnswer(invocation -> {
            Thread.sleep(30);
            return List.of(user);
        }).when(userService).findAllByNeighborhood(any());
        doAnswer(invocation -> {
            Thread.sleep(30);
            return null;
        }).when(notificationService).notify(any(NotificationEvent.class));
    }

    @Benchmark
    public void createPost(){
        PostDetailResponse response = postService.createPost(
                user,
                "제목1", "내용1",
                LocalDateTime.now().plusMinutes(60), "1L",
                "placeId", "강남구 테헤란로 127",
              5, "openChatUrl",
                30
        );
    }
    @Benchmark
    public void improvedCreatePost(){
        PostDetailResponse response = postService.improvedCreatePost(
                user,
                "제목1", "내용1",
                LocalDateTime.now().plusMinutes(60), "1L",
                "placeId", "강남구 테헤란로 127",
                5, "openChatUrl",
                30
        );
    }
}
