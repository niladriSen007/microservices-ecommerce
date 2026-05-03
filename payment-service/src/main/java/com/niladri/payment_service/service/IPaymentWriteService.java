package com.niladri.payment_service.service;

import com.niladri.common.dtos.events.InventoryReservedEvent;

public interface IPaymentWriteService {
    void processPayment(InventoryReservedEvent inventoryReservedEvent);
}
