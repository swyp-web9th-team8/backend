package com.swyp.plogging.backend.user.user.repository;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.plogging.backend.badge.domain.QBadge;
import com.swyp.plogging.backend.post.participation.domain.QParticipation;
import com.swyp.plogging.backend.post.post.domain.QPost;
import com.swyp.plogging.backend.rank.controller.dto.QRankingResponse;
import com.swyp.plogging.backend.rank.controller.dto.RankingResponse;
import com.swyp.plogging.backend.user.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.user.controller.dto.QProfileResponse;
import com.swyp.plogging.backend.user.user.domain.QAppUser;
import com.swyp.plogging.backend.user.user.domain.QUserBadge;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public ProfileResponse findProfileByUserId(Long userId) {
        QAppUser appUser = QAppUser.appUser;
        QPost post = QPost.post;
        QUserBadge userBadge = QUserBadge.userBadge;
        QBadge badge = QBadge.badge;
        QParticipation participation = QParticipation.participation;

        SubQueryExpression<Long> latestUserBadgeId = JPAExpressions
            .select(userBadge.id)
            .from(userBadge)
            .where(userBadge.user.id.eq(userId))
            .orderBy(userBadge.createdDt.desc())
            .limit(1);

        SubQueryExpression<Integer> participatedCount = JPAExpressions
            .select(participation.count().intValue())
            .from(participation)
            .where(participation.user.id.eq(userId));

        return queryFactory
            .select(new QProfileResponse(appUser.id, appUser.nickname, appUser.email, appUser.region, appUser.profileImageUrl,
                post.count().intValue(), badge.activeBadgeIconDir, participatedCount))
            .from(appUser)
            .leftJoin(post).on(post.writer.eq(appUser))
            .leftJoin(userBadge).on(userBadge.id.eq(latestUserBadgeId))
            .leftJoin(userBadge.badge, badge)
            .where(appUser.id.eq(userId))
            .groupBy(appUser.id, badge.activeBadgeIconDir)
            .fetchOne();
    }

    @Override
    public List<RankingResponse> findWeeklyRanking(LocalDateTime startOfCurrentWeek) {
        return getRankingResponseQuery(startOfCurrentWeek, LocalDateTime.now());
    }

    @Override
    public List<RankingResponse> findAllTimeRankings() {
        return getRankingResponseQuery(null, null);
    }

    private List<RankingResponse> getRankingResponseQuery(LocalDateTime start, LocalDateTime end) {
        QAppUser user = QAppUser.appUser;
        QPost post = QPost.post;
        QParticipation participation = QParticipation.participation;
        boolean hasCondition = start != null && end != null;

        // 쿼리DSL에서 select절에 그냥 사용해도 알아서 sql엔진에서 별칭 처리후 orderby에 사용하지만
        // 가독성을 위해 별도 선언
        NumberExpression<Long> postCount = post.id.countDistinct();
        NumberExpression<Long> participationCount = participation.id.countDistinct();

        return queryFactory.select(new QRankingResponse(user.id, user.nickname, user.profileImageUrl, postCount, participationCount))
                .from(user)
                .leftJoin(post).on(post.writer.eq(user)
                        .and(hasCondition ? post.createdDt.between(start, end) : null))
                .leftJoin(participation).on(participation.user.eq(user)
                        .and(hasCondition ? participation.createdDt.between(start, end) : null))
                .groupBy(user)
                .orderBy(postCount.add(participationCount).desc())
                .limit(10)
                .fetch();
    }
}
