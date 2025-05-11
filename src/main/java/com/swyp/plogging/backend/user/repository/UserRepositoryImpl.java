package com.swyp.plogging.backend.user.repository;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.plogging.backend.badge.domain.QBadge;
import com.swyp.plogging.backend.participation.domain.QParticipation;
import com.swyp.plogging.backend.post.domain.QPost;
import com.swyp.plogging.backend.rank.controller.dto.QRankingResponse;
import com.swyp.plogging.backend.rank.controller.dto.RankingResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.QProfileResponse;
import com.swyp.plogging.backend.user.domain.QAppUser;
import com.swyp.plogging.backend.user.domain.QUserBadge;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

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

        JPQLQuery<Long> postCount = JPAExpressions
            .select(post.count())
            .from(post)
            .where(post.writer.eq(user)
                .and(hasCondition ? post.createdDt.between(start, end) : null));

        JPQLQuery<Long> participationCount = JPAExpressions
            .select(participation.count())
            .from(participation)
            .where(participation.user.eq(user)
                .and(hasCondition ? participation.createdDt.between(start, end) : null));

        return queryFactory
            .select(new QRankingResponse(user.id, user.nickname, user.profileImageUrl, postCount, participationCount))
            .from(user)
            .orderBy(Expressions.numberTemplate(Integer.class,
                "({0} + {1})", postCount, participationCount).desc())
            .limit(10)
            .fetch();
    }
}
