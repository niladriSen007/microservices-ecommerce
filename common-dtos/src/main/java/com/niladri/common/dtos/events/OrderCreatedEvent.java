package com.niladri.common.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private EventMetadata metadata;
    private String orderId;
    private Long userId;
    private List<OrderItem> items;
    private Double totalAmount;
}
