package com.swyp.plogging.backend.rank.controller;

import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.rank.controller.dto.RankingResponse;
import com.swyp.plogging.backend.rank.controller.dto.RankingViewType;
import com.swyp.plogging.backend.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class RankingController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<RankingResponse>> getRankings(@AuthenticationPrincipal OAuth2User principal,
        @RequestParam(required = false) String type) {
        SecurityUtils.getUserId(principal);
        RankingViewType queryType = RankingViewType.from(type);
        List<RankingResponse> rankingResponses;
        if (queryType == RankingViewType.WEEKLY) {
            rankingResponses = userService.getWeeklyRankings();
        } else {
            rankingResponses = userService.getAllTimeRankings();
        }

        return ApiResponse.ok(rankingResponses, null);
    }
}
