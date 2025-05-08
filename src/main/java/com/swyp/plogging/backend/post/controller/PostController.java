package com.swyp.plogging.backend.post.controller;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.common.dto.ApiPagedResponse;
import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.controller.dto.CreatePostRequest;
import com.swyp.plogging.backend.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.sevice.ParticipationService;
import com.swyp.plogging.backend.post.sevice.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final ParticipationService participationService;

    public PostController(PostService postService, ParticipationService participationService) {
        this.postService = postService;
        this.participationService = participationService;
    }

    @PostMapping("create")
    public ApiResponse<PostDetailResponse> createPost(@RequestBody CreatePostRequest request) {
        try {
            PostDetailResponse response = postService.createPost(request.getTitle(), request.getContent(),
                    request.getMeetingTime(), request.getPlaceId(),
                    request.getPlaceName(), request.getAddress(),
                    request.getMaxParticipants(), request.getOpenChatUrl(), null);

            return ApiResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PatchMapping("/{postId}/modify")
    public ApiResponse<PostDetailResponse> modifyPost(@PathVariable(name = "postId") Long postId,
                                                      @RequestBody CreatePostRequest request) {
        try {
            PostDetailResponse response = postService.modifyPost(
                    request.getId(),
                    request.getTitle(), request.getContent(),
                    request.getMeetingTime(), request.getPlaceId(),
                    request.getPlaceName(), request.getAddress(),
                    request.getMaxParticipants(), request.getOpenChatUrl(), null
            );

            return ApiResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/delete")
    public ApiResponse<Object> deletePost(@PathVariable(name = "postId") Long postId) {
        try {
            postService.deletePost(postId);

            return ApiResponse.ok(null, "Successfully deleted the post.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> postDetail(@PathVariable(name = "postId") Long postId) {
        try {
            PostDetailResponse response = postService.getPostDetails(postId);
            return ApiResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiPagedResponse<PostInfoResponse> getListOfPosts(
            @PageableDefault(size = 10, sort = "meetingTime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "recruitmentCompleted", defaultValue = "false", required = false) Boolean recruitmentCompleted,
            @RequestParam(name = "completed", defaultValue = "false", required = false) Boolean completed) {
        try {
            Page<PostInfoResponse> response = postService.getListOfPostInfo(pageable, recruitmentCompleted, completed);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }

    }

    @PostMapping("/{postId}/participate")
    public ApiResponse<Object> participatePost(@PathVariable(name = "postId") Long postId,
                                               @AuthenticationPrincipal CustomOAuth2User user) {
        try {
            participationService.participateToPost(postId, user.getAppUser());
            return ApiResponse.ok(null, "Successfully joined the group.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }


    }

    @PostMapping("/{postId}/leave")
    public ApiResponse<Object> leavePost(@PathVariable(name = "postId") Long postId,
                                         @AuthenticationPrincipal CustomOAuth2User user) {
        try {
            participationService.leaveFromPost(postId, user.getAppUser());
            return ApiResponse.ok(null, "Successfully left the group.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }

    }

}
