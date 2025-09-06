package com.swyp.plogging.backend.post.post.listener;

import com.swyp.plogging.backend.common.util.RoadAddressUtil;
import com.swyp.plogging.backend.common.util.dto.Address;
import com.swyp.plogging.backend.notification.event.NotiType;
import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.event.PostEvent;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.region.domain.Region;
import com.swyp.plogging.backend.region.service.LocationService;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {
    private final RegionService regionService;
    private final LocationService locationService;
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final PostRepository postRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void postListen(PostEvent postEvent){
        Post post = (Post) postEvent.getSource();
        log.info("이벤트 발생: 모임ID-{}, 이벤트 타입-{}",post.getId(),postEvent.getEventType());
        switch (postEvent.getEventType()){
            case CREATE -> {
                processCreatedEvent(post);
                return;
            }
        }
    }

    @Transactional
    private void processCreatedEvent(Post post) {
        Address original = RoadAddressUtil.getAddressObject(post.getAddress());

        // 외부 API 이벤트로 비동기 처리
        // 네이버 지도에서 도로명 주소로 위치를 검색 후 위도경도 입력
        // 검색한 결과중 첫번째 선택
        List<Map<String, Object>> list = locationService.searchCoordinatesByAddress(original.getFullName());
        Map<String, Object> location = list.stream().filter(
                        map -> {
                            Address forCheck = RoadAddressUtil.getAddressObject((CharSequence) map.get("roadAddress"));
                            // 새로 받아온 데이터가 적절한지 도로명 주소로 비교
                            return RoadAddressUtil.compareRoadAddress(original, forCheck);
                        })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("찾는 주소가 없습니다."));

        // 위경도 추출
        Double latitude = (Double) location.get("latitude");
        Double longitude = (Double) location.get("longitude");
        // 포인트 생성
        Point point = locationService.createPoint(longitude, latitude);
        if (point == null) {
            throw new RuntimeException("해당 주소의 지점을 생성할 수 없습니다.");
        }
        Region region = regionService.getContainedRegion(point);

        post.updatePoint(latitude, longitude, point);
        post.updateRegion(region);
        postRepository.save(post);


        // 알림 비동기 처리
        List<AppUser> receivers = userService.findAllByNeighborhood(region.getNeighborhood());
        for(AppUser receiver : receivers) {
            publisher.publishEvent(new NotificationEvent(NotiStrategy.FCM, NotiType.CREATE,receiver,post.getId()));
        }
    }
}
