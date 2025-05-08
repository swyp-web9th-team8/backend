package com.swyp.plogging.backend.domain;

import com.swyp.plogging.backend.domain.base.BaseTimeEntity;
import com.swyp.plogging.backend.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
