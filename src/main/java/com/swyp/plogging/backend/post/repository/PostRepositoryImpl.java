package com.swyp.plogging.backend.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.swyp.plogging.backend.participation.domain.QParticipation;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.domain.QPost;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PostRepositoryImpl implements PostRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Post> findPostByCondition(Pageable pageable, Boolean recruitmentCompleted, Boolean completed) {
        QPost post = QPost.post;
        QParticipation participation = QParticipation.participation;

        // query 기본
        JPAQuery<Post> query = new JPAQuery<>(em);
        query.select(post).from(post);

        // paging을 위한 totalCount
        JPAQuery<Post> countQuery = new JPAQuery<>(em);
        countQuery.select(post).from(post);

        // 조건
        BooleanBuilder postCondition = new BooleanBuilder();

        // completed 조건 추가
        postCondition.and(post.completed.eq(completed));

        // recruitmentCompleted 조건 추가
        if (!recruitmentCompleted) {
            postCondition.and(post.deadLine.gt(LocalDateTime.now()));
            // 기본 쿼리에 조인과 그룹바이 적용
            query.leftJoin(participation).on(participation.post.eq(post))
                .groupBy(post.id)
                .having(participation.count().lt(post.maxParticipants));

            // 카운트 쿼리에 조인과 그룹바이 적용
            countQuery.leftJoin(participation).on(participation.post.eq(post))
                .groupBy(post.id)
                .having(participation.count().lt(post.maxParticipants));
        }

        // 조건 적용
        query.where(postCondition);
        countQuery.where(postCondition);

        // sort
        PathBuilder<Post> pathBuilder = new PathBuilder<>(Post.class, "post");
        Sort.Order order = pageable.getSort().get().findFirst().orElseThrow();
        String property = order.getProperty();

        if (property.equals("meetingTime")) {
            property = "meetingDt";
            if (order.isDescending()) {
                query.orderBy(pathBuilder.getDateTime(property, LocalDateTime.class).desc());
            } else {
                query.orderBy(pathBuilder.getDateTime(property, LocalDateTime.class).asc());
            }
        }

        // pageable 적용
        query.offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        List<Post> result = query.fetch();
        long totalCount = countQuery.fetch().size();

        return new PageImpl<>(result, pageable, totalCount);
    }
}
