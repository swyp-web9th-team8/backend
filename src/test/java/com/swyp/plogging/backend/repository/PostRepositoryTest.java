package com.swyp.plogging.backend.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(PostRepositoryImpl.class)
public class PostRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostRepositoryImpl postRepositoryImpl;

    @BeforeEach
    void setUp() {
        // todo 보류
//        Post post = Post.builder().build();
//        em.persist(post);
//
//        Participation p = new Participation(post, "user1");
//        em.persist(p);
//
//        em.flush();
//        em.clear();
    }
}
