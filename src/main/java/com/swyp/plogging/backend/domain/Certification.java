package com.swyp.plogging.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    private LocalDateTime certifiedAt;

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
