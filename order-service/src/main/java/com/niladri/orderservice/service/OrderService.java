package com.niladri.orderservice.service;

import com.niladri.orderservice.dto.OrderRequest;
import com.niladri.orderservice.dto.OrderResponse;
import com.niladri.orderservice.model.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    void cancelOrder(Long id);
}

