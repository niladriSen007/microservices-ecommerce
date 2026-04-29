package com.niladri.productservice.model;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummary {

    private Double averageRating;
    private Integer totalReviewCount;

    @Builder.Default
    private Map<Integer, Integer> ratingDistribution = new HashMap<Integer, Integer>();
}
