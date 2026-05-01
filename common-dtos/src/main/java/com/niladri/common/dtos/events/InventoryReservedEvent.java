package com.niladri.common.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {
    private EventMetadata metadata;
    private String orderId;
    private Long productId;
    private int reservedQuantity;
    private String reservationStatus;
}
