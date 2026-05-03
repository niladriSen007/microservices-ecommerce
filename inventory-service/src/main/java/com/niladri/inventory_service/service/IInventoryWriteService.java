package com.niladri.inventory_service.service;

import com.niladri.common.dtos.events.OrderCreatedEvent;
import com.niladri.inventory_service.dto.OrderRequest;

public interface IInventoryWriteService {
    public String inventoryProductAddition();

    public String inventoryProductDeletion();

    String reserveOrder(OrderCreatedEvent orderRequest);

    void confirmReservation(String orderId);

    void releaseReservation(String orderId);
}
