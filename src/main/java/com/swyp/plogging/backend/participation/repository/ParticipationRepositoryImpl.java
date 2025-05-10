package com.swyp.plogging.backend.participation.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.plogging.backend.participation.domain.QParticipation;
import com.swyp.plogging.backend.participation.dto.ParticipatedPostResponse;
import com.swyp.plogging.backend.participation.dto.QParticipatedPostResponse;
import com.swyp.plogging.backend.post.domain.QPost;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ParticipatedPostResponse> findParticipatedPostsByUserId(Long userId, Pageable pageable) {
        QParticipation participation = QParticipation.participation;
        QPost post = QPost.post;

        BooleanExpression isParticipated = participation.user.id.eq(userId)
            .and(participation.joined.isTrue());

        List<ParticipatedPostResponse> content = queryFactory
            .select(new QParticipatedPostResponse(post.id, post.title, post.placeName, post.meetingDt, post.completed))
            .from(participation)
            .join(participation.post, post)
            .where(isParticipated)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(post.meetingDt.desc())
            .fetch();

        long total = countParticipationByUserId(participation, isParticipated);

        return new PageImpl<>(content, pageable, total);
    }

    private long countParticipationByUserId(QParticipation participation, BooleanExpression isParticipated) {
        Long total = queryFactory.select(participation.count())
            .from(participation)
            .where(isParticipated)
            .fetchOne();
        return total != null ? total : 0L;
    }
}
