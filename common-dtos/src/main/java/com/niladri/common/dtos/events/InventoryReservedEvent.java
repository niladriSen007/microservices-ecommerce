package com.niladri.common.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {
    private EventMetadata metadata;
    private String orderId;
    private String userId;
    private List<ReservedItem> items;
    private BigDecimal totalAmount;
    private String reservationStatus;
}
