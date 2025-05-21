package com.swyp.plogging.backend.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.swyp.plogging.backend.domain.QRegion;
import com.swyp.plogging.backend.participation.domain.QParticipation;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.domain.QPost;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public class PostRepositoryImpl implements PostRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    // 기존 메서드 유지
    @Override
    public Page<Post> findPostByCondition(MultiPolygon regionPolygons, Pageable pageable, Boolean recruitmentCompleted, Boolean completed) {
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

        // 지역 조건 검색
        postCondition.and(Expressions.booleanTemplate(
                "ST_Contains({0},{1})",
                regionPolygons,
                post.location
        ));

        // completed 조건 추가
        postCondition.and(post.completed.eq(completed));

        // recruitmentCompleted 조건 추가
        if (recruitmentCompleted) {
            postCondition.and(post.deadLine.gt(LocalDateTime.now()));
            // 기본 쿼리에 조인과 그룹바이 적용
            query.leftJoin(participation).on(participation.post.eq(post))
                    .groupBy(post.id)
                    .having(participation.id.count().eq(post.maxParticipants.castToNum(Long.class)));

            // 카운트 쿼리에 조인과 그룹바이 적용
            countQuery.leftJoin(participation).on(participation.post.eq(post))
                    .groupBy(post.id)
                    .having(participation.id.count().eq(post.maxParticipants.castToNum(Long.class)));
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

    // 수정: PostGIS를 활용한 위치 기반 검색 메서드 - 네이티브 SQL 쿼리 사용
    @Override
    public Page<Post> findNearbyPosts(Double latitude, Double longitude, Double radiusKm, Pageable pageable) {
        // 네이티브 SQL 쿼리 사용
        String sql = "SELECT * FROM post p " +
                "WHERE p.completed = false " +
                "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
                "AND ST_DWithin(" +
                "  ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography, " +
                "  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
                "  :radius" +
                ") = true " +
                "ORDER BY ST_Distance(" +
                "  ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography, " +
                "  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography" +
                ")";

        // 네이티브 쿼리 실행
        Query query = em.createNativeQuery(sql, Post.class);
        query.setParameter("latitude", latitude);
        query.setParameter("longitude", longitude);
        query.setParameter("radius", radiusKm * 1000); // km -> m 변환

        // 페이징 적용
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Post> results = query.getResultList();

        // 전체 개수 조회를 위한 네이티브 쿼리
        String countSql = "SELECT COUNT(*) FROM post p " +
                "WHERE p.completed = false " +
                "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
                "AND ST_DWithin(" +
                "  ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography, " +
                "  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
                "  :radius" +
                ") = true";

        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("latitude", latitude);
        countQuery.setParameter("longitude", longitude);
        countQuery.setParameter("radius", radiusKm * 1000); // km -> m 변환

        Number count = (Number) countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, count.longValue());
    }

    public Page<Post> findPostByRegion(MultiPolygon regionPolygons, Pageable pageable, String keyword) {
        QPost post = QPost.post;
        QRegion region = QRegion.region;

        String pattern = "%" + keyword + "%";
        BooleanExpression conditions = Expressions.booleanTemplate(
                "ST_Contains({0},{1})",
                regionPolygons,
                post.location
        ).and(post.completed.eq(false));

        if (keyword != null && !keyword.isBlank()) {
            conditions = conditions.and(
                    post.content.like(pattern)
                            .or(post.title.like(pattern))
            );
        }

        JPAQuery<Post> postJPAQuery = new JPAQuery<>(em)
                .select(post).from(post)
                .where(conditions)
                .orderBy(post.createdDt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());

        JPAQuery<Long> countQuery = new JPAQuery<>(em)
                .select(post.id).from(post)
                .where(conditions);

        List<Post> posts = postJPAQuery
                .fetch();
        int totalCount = countQuery.fetch().size();

        return new PageImpl<>(posts, pageable, totalCount);
    }
}