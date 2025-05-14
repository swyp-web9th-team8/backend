package com.swyp.plogging.backend.post.sevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swyp.plogging.backend.common.exception.PostNotFoundException;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.common.service.LocationService;
import com.swyp.plogging.backend.domain.Region;
import com.swyp.plogging.backend.post.controller.dto.CreatePostRequest;
import com.swyp.plogging.backend.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.PostRepository;
import com.swyp.plogging.backend.service.RegionService;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

    public PostDetailResponse getPostDetails(Long postId) {
        return findById(postId).toDetailResponse();
    }

    // 현재 모집 중인 모임 조회
    public Page<PostInfoResponse> getListOfPostInfo(Pageable pageable, String position, String keyword) throws JsonProcessingException {
        return findPostsByDistrictAndNeighborhood(pageable, position, keyword);
    }

    public Page<PostInfoResponse> getListOfCompletePostInfo(Pageable pageable, boolean recruitmentCompleted, boolean completed) {
        // 모집만 완료한 모임 또는 모임을 완료한 모임 조회
        Page<Post> posts = postRepository.findPostByCondition(pageable, recruitmentCompleted, completed);
        List<PostInfoResponse> postList = posts.stream().map(post -> {
            if(post.isCompleted()){
                return new PostInfoResponse(post, post.getCertification());
            }
            return new PostInfoResponse(post);
        }).toList();

        return new PageImpl<>(postList, pageable, posts.getTotalElements());
    }
//    public Page<PostInfoResponse> getListOfPostInfo(Pageable pageable, Boolean recruitmentCompleted, Boolean completed) {
//        // 데이터 DTO로 정제
//        Page<Post> data = postRepository.findPostByCondition(pageable, recruitmentCompleted, completed);
//        List<PostInfoResponse> content;
//        // 완료 여부에 따른 응답 분리
//        if(completed){
//            content = data.getContent().stream().map(post -> new PostInfoResponse(post, post.getCertification())).toList();
//        }
//        else{
//            content = data.getContent().stream().map(PostInfoResponse::new).toList();
//        }
//
//        return new PageImpl<>(content, data.getPageable(), data.getTotalElements());
//    }

    /**
     * 주변 모임 검색 메서드
     */
    public Page<PostInfoResponse> findNearbyPosts(Double latitude, Double longitude, Double radiusKm, Pageable pageable) {
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
    public Page<PostInfoResponse> findPostsByDistrictAndNeighborhood(Pageable pageable, String position, String keyword) throws JsonProcessingException {
        // 검색하고 싶은 지역 찾기
        String[] regionFields = position.split(" ");
        Optional<Region> opRegion = regionService.findByDistrictAndNeighborhood(regionFields[0], regionFields[1]);
        if (opRegion.isEmpty()) {
            throw new RuntimeException("해당 지역이 없습니다.");
        }

        Region region = opRegion.get();
        if (!region.hasPolygons()) {
            MultiPolygon multiPolygon = regionService.getPolygonOfNeighborhood(region.getCode());
            region.setPolygons(multiPolygon);
        }

        Page<Post> posts = postRepository.findPostByRegion(region.getPolygons(), pageable, keyword);
        List<PostInfoResponse> postList = posts.stream().map(PostInfoResponse::new).toList();

        return new PageImpl<>(postList, pageable, posts.getTotalElements());
    }
}