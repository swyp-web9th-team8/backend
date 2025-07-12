package com.swyp.plogging.backend.post.post.sevice;

import com.swyp.plogging.backend.common.exception.PostNotFoundException;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.common.service.LocationService;
import com.swyp.plogging.backend.common.util.RoadAddressUtil;
import com.swyp.plogging.backend.common.util.dto.Address;
import com.swyp.plogging.backend.region.domain.Region;
import com.swyp.plogging.backend.post.post.controller.dto.CreatePostRequest;
import com.swyp.plogging.backend.post.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.repository.PostRepository;
import com.swyp.plogging.backend.region.service.RegionService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final LocationService locationService;
    private final RegionService regionService;

    /**
     * 기존 모임 생성 메서드
     */
    @Transactional
    public PostDetailResponse createPost(AppUser user, String title, String content,
                                         LocalDateTime meetingTime, String placeId,
                                         String placeName, String address,
                                         @NonNull Integer maxParticipants, String openChatUrl,
                                         @Nullable Integer deadLine) {

        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("최대인원 설정이 잘못되었습니다.");
        }

        if (!RoadAddressUtil.isRoadAddress(address)) {
            throw new IllegalArgumentException("'OO구 OO대로(또는 '로' 또는 '길') 0' 형식으로 API 요청바랍니다.");
        }
        Address a1 = RoadAddressUtil.getAddressObject(address);

        // 네이버 지도에서 도로명 주소로 위치를 검색 후 위도경도 입력
        // 검색한 결과중 첫번째 선택
        List<Map<String, Object>> list = locationService.searchCoordinatesByAddress(a1.getFullName());
        Map<String, Object> location = list.stream().filter(
                        map -> {
                            Address a2 = RoadAddressUtil.getAddressObject((CharSequence) map.get("roadAddress"));
                            // 새로 받아온 데이터가 적절한지 도로명 주소로 비교
                            return RoadAddressUtil.compareRoadAddress(a1, a2);
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

        Post post = Post.builder()
                .writer(user)
                .title(title)
                .content(content)
                .meetingDt(meetingTime)
                .placeId(placeId)
                .placeName(placeName)
                .address(address)
                .completed(false)
                .maxParticipants(maxParticipants)
                .openChatUrl(openChatUrl)
                .latitude(latitude)
                .longitude(longitude)
                .location(point)
                .build();

        // null일 경우 30분전 세팅
        post.setUpDeadLine(deadLine);
        post = postRepository.save(post);

        return post.toDetailResponse();
    }

    /**
     * 위치 정보를 포함한 모임 생성 메서드
     */
    @Transactional
    public PostDetailResponse createPost(AppUser user, String title, String content,
                                         LocalDateTime meetingTime, String placeId,
                                         String placeName, String address,
                                         Double latitude, Double longitude,
                                         @NonNull Integer maxParticipants, String openChatUrl,
                                         @Nullable Integer deadLine) {

        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("최대인원 설정이 잘못되었습니다.");
        }

        // 좌표는 있는데 주소가 없는 경우, 역지오코딩으로 주소 조회
        if (latitude != null && longitude != null && (address == null || address.isEmpty())) {
            Map<String, Object> addressInfo = locationService.reverseGeocode(longitude, latitude);
            address = (String) addressInfo.get("fullAddress");
        }

        // PostGIS Point 객체 생성
        Point location = locationService.createPoint(longitude, latitude);

        Post post = Post.builder()
                .writer(user)
                .title(title)
                .content(content)
                .meetingDt(meetingTime)
                .placeId(placeId)
                .placeName(placeName)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .location(location)
                .completed(false)
                .maxParticipants(maxParticipants)
                .openChatUrl(openChatUrl)
                .build();

        // null일 경우 30분전 세팅
        post.setUpDeadLine(deadLine);
        post = postRepository.save(post);

        return post.toDetailResponse();
    }

    /**
     * CreatePostRequest DTO를 사용하는 모임 생성 메서드
     */
    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, AppUser user) {
        return createPost(
                user,
                request.getTitle(),
                request.getContent(),
                request.getMeetingTime(),
                request.getPlaceId(),
                request.getPlaceName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getMaxParticipants(),
                request.getOpenChatUrl(),
                30 // 기본값으로 30분 전 설정
        );
    }

    /**
     * 기존 모임 수정 메서드
     */
    @Transactional
    public PostDetailResponse modifyPost(AppUser user, Long id, String title,
                                         String content, LocalDateTime meetingTime,
                                         String placeId, String placeName, String address,
                                         Integer maxParticipants, String openChatUrl, Integer deadLine) {

        Post post = findById(id);

        // 작성자 확인
        if (!post.isWriter(user)) {
            throw new UnauthorizedUserException("작성자가 아닙니다.");
        }

        post.modify(
                title, content, meetingTime,
                placeId, placeName, address,
                null, null, // 위치 정보는 변경하지 않음
                maxParticipants, openChatUrl, deadLine
        );

        return post.toDetailResponse();
    }

    /**
     * 위치 정보를 포함한 모임 수정 메서드
     */
    @Transactional
    public PostDetailResponse modifyPost(AppUser user, Long id, String title,
                                         String content, LocalDateTime meetingTime,
                                         String placeId, String placeName, String address,
                                         Double latitude, Double longitude,
                                         Integer maxParticipants, String openChatUrl, Integer deadLine) {

        Post post = findById(id);

        // 작성자 확인
        if (!post.isWriter(user)) {
            throw new UnauthorizedUserException("작성자가 아닙니다.");
        }

        // 좌표는 있는데 주소가 없는 경우, 역지오코딩으로 주소 조회
        if (latitude != null && longitude != null && (address == null || address.isEmpty())) {
            Map<String, Object> addressInfo = locationService.reverseGeocode(longitude, latitude);
            address = (String) addressInfo.get("fullAddress");
        }

        post.modify(
                title, content, meetingTime,
                placeId, placeName, address,
                latitude, longitude,
                maxParticipants, openChatUrl, deadLine
        );

        return post.toDetailResponse();
    }

    /**
     * CreatePostRequest DTO를 사용하는 모임 수정 메서드
     */
    @Transactional
    public PostDetailResponse modifyPost(AppUser user, Long id, CreatePostRequest request) {
        return modifyPost(
                user,
                id,
                request.getTitle(),
                request.getContent(),
                request.getMeetingTime(),
                request.getPlaceId(),
                request.getPlaceName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getMaxParticipants(),
                request.getOpenChatUrl(),
                30 // 기본값으로 30분 전 설정
        );
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Transactional
    public void deletePost(Long postId, AppUser user) {
        Post post = findById(postId);

        // 작성자인지 확인
        if (!post.isWriter(user)) {
            throw new UnauthorizedUserException("작성자가 아닙니다.");
        }

        postRepository.delete(post);
    }

    public PostDetailResponse getPostDetails(Long postId, Long userId) {
        Post post = findById(postId);
        PostDetailResponse postDetailResponse = post.toDetailResponse();

        boolean isWriter = post.isWriterId(userId);
        boolean isParticipant = post.getParticipations().stream()
                .anyMatch(p -> p.getUser().getId().equals(userId));

        postDetailResponse.setIIn(isWriter || isParticipant);

        return postDetailResponse;
    }

    // 현재 모집 중인 모임 조회
    @Transactional
    public Page<PostInfoResponse> getListOfPostInfo(Pageable pageable, String position, String keyword, AppUser user) {
        return findPostsByDistrictAndNeighborhood(pageable, position, keyword, user);
    }

    @Transactional
    public Page<PostInfoResponse> getListOfCompletePostInfo(
            Pageable pageable, String position, boolean recruitmentCompleted, boolean completed) {
        // 지역조건을 위한 폴리곤 세팅
        MultiPolygon polygon;
        if (position.isBlank()) {
            polygon = null;
        } else {
            polygon = getMultiPolygon(position);
        }

        // 모집만 완료한 모임 또는 모임을 완료한 모임 조회
        Page<Post> posts = postRepository.findPostByCondition(polygon, pageable, recruitmentCompleted, completed);
        List<PostInfoResponse> postList = posts.stream().map(post -> {
            if (post.isCompleted()) {
                return new PostInfoResponse(post, post.getCertification());
            }
            return new PostInfoResponse(post);
        }).toList();

        return new PageImpl<>(postList, pageable, posts.getTotalElements());
    }

    /**
     * 주변 모임 검색 메서드
     */
    public Page<PostInfoResponse> findNearbyPosts(Double latitude, Double longitude, Double radiusKm, Pageable
            pageable) {
        Page<Post> posts = postRepository.findNearbyPosts(latitude, longitude, radiusKm, pageable);
        List<PostInfoResponse> content = posts.getContent().stream()
                .map(PostInfoResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, posts.getTotalElements());
    }

    /**
     * 지역 모임 검색 메서드
     *
     * @param position 검색지역
     * @param keyword  검색어
     */
    @Transactional
    public Page<PostInfoResponse> findPostsByDistrictAndNeighborhood(
            Pageable pageable, String position, String keyword, AppUser user) {

        Page<Post> posts = postRepository.findPostByRegion(getMultiPolygon(position), pageable, keyword);
        List<PostInfoResponse> postList = posts.stream()
                .filter(post -> !post.isMax())
                .filter(post -> post.getDeadLine().isAfter(LocalDateTime.now()))
                .map(post -> new PostInfoResponse(post, user)).toList();

        return new PageImpl<>(postList, pageable, posts.getTotalElements());
    }

    public List<Long> getCompletedPostIdsWithNotCertificatedMax10(Long writerId) {
        List<Post> posts = postRepository.find10ByCompletedAndNotCertificated(writerId);
        return posts.stream().mapToLong(Post::getId).boxed().toList();
    }

    @Transactional
    public MultiPolygon getMultiPolygon(String position) {
        // 검색하고 싶은 지역 찾기
        Address address = RoadAddressUtil.getAddressObject(position);
        Optional<Region> opRegion = regionService.findByDistrictAndNeighborhood(address.getDistrict(), address.getNeighborhood());
        if (opRegion.isEmpty()) {
            throw new RuntimeException("해당 지역이 없습니다.");
        }

        Region region = opRegion.get();
        MultiPolygon multiPolygon;
        if (!region.hasPolygons()) {
            multiPolygon = regionService.getAndSavePolygonOfRegion(region);
        } else {
            multiPolygon = region.getPolygons();
        }
        return multiPolygon;
    }

    public Long getCountCompletedPostByWriter(AppUser user) {
        return postRepository.countByWriterAndCompleted(user, true);
    }

    // region data가 없는 post만 데이터 처리
    @Transactional
    public void fillRegion(){
        postRepository.findAllByRegionIdIsNull().stream().forEach(post -> {
            Region r = regionService.getContainedRegion(post.getLocation());
            post.updateRegion(r);
        });
    }
}