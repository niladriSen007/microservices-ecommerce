package com.niladri.common.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {
    private EventMetadata metadata;
    private Long productId;
    private String name;
    private Integer quantity;
    private Double price;
}
