package com.niladri.orderservice.service;

import com.niladri.orderservice.dto.OrderResponse;
import com.niladri.orderservice.model.OrderStatus;

import java.util.List;

public interface IOrderReadService {

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);
}
