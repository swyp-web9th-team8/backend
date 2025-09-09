package com.swyp.plogging.backend.common;

import com.swyp.plogging.backend.region.service.LocationService;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.region.domain.Region;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.domain.AuthProvider;
import com.swyp.plogging.backend.user.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Profile("test")
@RequiredArgsConstructor
public class TestFactory {

    private final RegionService regionBean;
    private final LocationService locationBean;
    private final PostRepository postBean;
    private final UserRepository userBean;
    private static RegionService regionService;
    private static LocationService locationService;
    private static PostRepository postRepository;
    private static UserRepository userRepository;

    @PostConstruct
    public void init(){
        System.out.println("-------포스트 컨스트럭트 실행---------");
        regionService = regionBean;
        locationService = locationBean;
        postRepository = postBean;
        userRepository = userBean;
    }

    public static AppUser newUser(String nickname, String email){
        Region region = regionService.getRegionById((long) Math.floor(Math.random() * 30 + 1));
        // 새 사용자 생성 - 처음에는 등록되지 않은 상태로 생성
        // AppUser.newInstance 메서드는 이미 registered=false로 설정함
        AppUser user = AppUser.newInstanceWithRegion(email, nickname, region, AuthProvider.GOOGLE, "/images/basic.png");
        // 성별 정보 설정
        user.setGender("Female");
        user.completeRegistration();
        return userRepository.save(user);
    }

    public static AppUser newUserAdd(String nickname, String email){
        // 새 사용자 생성 - 처음에는 등록되지 않은 상태로 생성
        // AppUser.newInstance 메서드는 이미 registered=false로 설정함
        AppUser user = AppUser.newInstance(email, nickname, "서울시 강남구 역삼동", AuthProvider.GOOGLE, "/images/basic.png");
        // 성별 정보 설정
        user.setGender("Female");
        user.completeRegistration();
        return userRepository.save(user);
    }

    public static Post newPostWithUser(AppUser user) {
        // 서울 중심 127.0016985, 37.5642135
        // PostGIS Point 객체 생성
        int randomNumber =(int) Math.floor(Math.random() * 1000);
        Double latitude = 37.56422135 + randomNumber / 100000.0;
        Double longitude = 127.0016985 + randomNumber / 100000.0;
        String address = null;
        LocalDateTime now = LocalDateTime.now();

        // 좌표는 있는데 주소가 없는 경우, 역지오코딩으로 주소 조회
        if (latitude != null && longitude != null && (address == null || address.isEmpty())) {
            Map<String, Object> addressInfo = locationService.reverseGeocode(longitude, latitude);
            address = (String) addressInfo.get("fullAddress");
        }

        // PostGIS Point 객체 생성
        Point location = locationService.createPoint(longitude, latitude);

        Post post = Post.builder()
                .writer(user)
                .title("제목"+randomNumber)
                .content("내용"+randomNumber)
                .meetingDt(now.plusMinutes(60))
                .placeId("placeId")
                .placeName("placeName")
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .location(location)
                .completed(false)
                .maxParticipants(50)
                .openChatUrl("openChatUrl")
                .build();

        // null일 경우 30분전 세팅
        post.setUpDeadLine(30);
        return postRepository.save(post);
    }
}
