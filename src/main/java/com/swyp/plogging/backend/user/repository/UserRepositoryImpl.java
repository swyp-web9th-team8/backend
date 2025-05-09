package com.swyp.plogging.backend.user.repository;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.plogging.backend.domain.QBadge;
import com.swyp.plogging.backend.domain.QUserBadge;
import com.swyp.plogging.backend.post.domain.QParticipation;
import com.swyp.plogging.backend.post.domain.QPost;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.QProfileResponse;
import com.swyp.plogging.backend.user.domain.QAppUser;
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
                post.count().intValue(), badge.iconUrl, participatedCount))
            .from(appUser)
            .leftJoin(post).on(post.writer.eq(appUser))
            .leftJoin(userBadge).on(userBadge.id.eq(latestUserBadgeId))
            .leftJoin(userBadge.badge, badge)
            .where(appUser.id.eq(userId))
            .groupBy(appUser.id, badge.iconUrl)
            .fetchOne();
    }
}
