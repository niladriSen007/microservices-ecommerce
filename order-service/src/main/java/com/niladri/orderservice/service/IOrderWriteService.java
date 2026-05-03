package com.niladri.orderservice.service;

import com.niladri.orderservice.dto.OrderRequest;
import com.niladri.orderservice.dto.OrderResponse;
import com.niladri.orderservice.model.OrderStatus;

public interface IOrderWriteService {

    OrderResponse createOrder(OrderRequest orderRequest);

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    void cancelOrder(Long id);

    void handleInventoryUnavailable(String orderId);

    void handlePaymentSucceeded(String orderId);

    void handlePaymentFailed(String orderId);
}
