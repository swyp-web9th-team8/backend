package com.swyp.plogging.backend.post.post.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostAggregationDto {
    private Long id;
    private Long totalPostCount;
    private Long totalParticipationCount;
}
