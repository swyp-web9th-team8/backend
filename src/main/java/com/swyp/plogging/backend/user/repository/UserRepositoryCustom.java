package com.swyp.plogging.backend.user.repository;

import com.swyp.plogging.backend.rank.controller.dto.RankingResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface UserRepositoryCustom {

    ProfileResponse findProfileByUserId(Long userId);

    List<RankingResponse> findWeeklyRanking(LocalDateTime since);

    List<RankingResponse> findAllTimeRankings();
}
