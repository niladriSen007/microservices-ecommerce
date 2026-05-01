package com.niladri.inventory_service.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String eventType;
    private String eventId;
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private double totalAmount;
    private String occurredAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private Long productId;
        private int quantity;
    }
}