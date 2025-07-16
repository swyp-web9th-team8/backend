package com.swyp.plogging.backend.user.user.repository;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.plogging.backend.badge.domain.QBadge;
import com.swyp.plogging.backend.post.participation.domain.QParticipation;
import com.swyp.plogging.backend.post.post.domain.QPost;
import com.swyp.plogging.backend.rank.controller.dto.RankingResponse;
import com.swyp.plogging.backend.user.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.user.controller.dto.QProfileResponse;
import com.swyp.plogging.backend.user.user.domain.QAppUser;
import com.swyp.plogging.backend.user.user.domain.QUserBadge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    @PersistenceContext
    private EntityManager em;

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

        // join은 행을 n * m으로 만드는 최악의 방법이여서 서브쿼리로 사전집계 후 조인하는 방법을 사용.
        // jpql은 from절, join절에서 서브쿼리를 사용하지 못하여 네이티브 쿼리 사용

        String postSub = """
                SELECT p.writer_id AS writer_id,
                               COUNT(DISTINCT p.id) AS post_count
                        FROM post p """;
        if(hasCondition){
            postSub += "WHERE p.created_dt between :start and :end ";
        }
        postSub += "GROUP BY p.writer_id";

        String participationSub = """
                SELECT pa.user_id AS user_id,
                               COUNT(DISTINCT pa.id) AS participation_count
                        FROM participation pa """;
        if(hasCondition){
            participationSub += "WHERE pa.created_dt between :start and :end ";
        }
        participationSub += "GROUP BY pa.user_id";

        String sql = """
                    SELECT u.id,
                           u.nickname,
                           u.profile_image_url,
                           COALESCE(pc.post_count, 0) AS post_count,
                           COALESCE(pa.participation_count, 0) AS participation_count
                    FROM app_user u
                    LEFT JOIN (""" + postSub +"""
                    ) pc ON pc.writer_id = u.id
                    LEFT JOIN ("""+ participationSub +"""
                    ) pa ON pa.user_id = u.id
                    ORDER BY (COALESCE(pc.post_count, 0) + COALESCE(pa.participation_count, 0)) DESC
                    LIMIT 10
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);

        List<Object[]> result = query.getResultList();

        List<RankingResponse> responses = result.stream()
                .map(row -> new RankingResponse(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        (Long) row[3],
                        (Long) row[4]))
                .toList();
        return responses;
    }
}
