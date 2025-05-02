package com.swyp.plogging.backend.controller;

import com.swyp.plogging.backend.common.ApiResponse;
import com.swyp.plogging.backend.controller.dto.CreatePostRequest;
import com.swyp.plogging.backend.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.controller.dto.PostListResponse;
import com.swyp.plogging.backend.sevice.PostService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("create")
    public ApiResponse<PostDetailResponse> createPost(@RequestBody CreatePostRequest request) {
        ApiResponse<PostDetailResponse> APIResponse = new ApiResponse<>();
        try {
            PostDetailResponse response = postService.createPost(request.getTitle(), request.getContent(),
                request.getMeetingTime(), request.getPlaceId(),
                request.getPlaceName(), request.getAddress(),
                request.getMaxParticipants(), request.getOpenChatUrl(), null);

            return APIResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return APIResponse.error(e.getMessage());
        }
    }

    @PatchMapping("/{postId}/modify")
    public ApiResponse<PostDetailResponse> modifyPost(@PathVariable(name = "postId") Long postId,
        @RequestBody CreatePostRequest request) {
        ApiResponse<PostDetailResponse> APIresponse = new ApiResponse<>();
        try {
            PostDetailResponse response = postService.modifyPost(
                request.getId(),
                request.getTitle(), request.getContent(),
                request.getMeetingTime(), request.getPlaceId(),
                request.getPlaceName(), request.getAddress(),
                request.getMaxParticipants(), request.getOpenChatUrl(), null
            );

            return APIresponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return APIresponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/delete")
    public ApiResponse<Object> deletePost(@PathVariable(name = "postId") Long postId) {
        ApiResponse<Object> APIResponse = new ApiResponse<>();

        try {
            postService.deletePost(postId);

            return APIResponse.ok(null, "Successfully deleted the post.");
        } catch (Exception e) {
            return APIResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> postDetail(@PathVariable(name = "postId") Long postId) {
        ApiResponse<PostDetailResponse> APIResponse = new ApiResponse<>();

        try {
            PostDetailResponse response = postService.getPostDetails(postId);
            return APIResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return APIResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<PostListResponse<PostInfoResponse>> getListOfPosts(
        @PageableDefault(size = 10, sort = "meetingTime", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(name = "recruitmentCompleted", defaultValue = "false", required = false) Boolean recruitmentCompleted,
        @RequestParam(name = "completed", defaultValue = "false", required = false) Boolean completed) {
        ApiResponse<PostListResponse<PostInfoResponse>> APIResponse = new ApiResponse<>();
        try {
            PostListResponse<PostInfoResponse> response = postService.getListOfPostInfo(pageable, recruitmentCompleted, completed);

            return APIResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return APIResponse.error(e.getMessage());
        }

    }

    @PostMapping("/{postId}/participate")
    public String participatePost(@PathVariable(name = "postId") Long postId) {
        return "Successfully joined the group.";

    }

    @PostMapping("/{postId}/leave")
    public String leavePost(@PathVariable(name = "postId") Long postId) {
        return "Successfully left the group.";
    }

}
