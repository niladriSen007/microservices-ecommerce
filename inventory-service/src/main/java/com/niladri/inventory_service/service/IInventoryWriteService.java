package com.niladri.inventory_service.service;

import com.niladri.inventory_service.dto.OrderRequest;

public interface IInventoryWriteService {
    public String inventoryProductAddition();

    public String inventoryProductDeletion();

    String reserveOrder(OrderRequest  orderRequest);
}
