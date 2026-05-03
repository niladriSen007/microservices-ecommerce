package com.niladri.orderservice.service.impl;

import com.niladri.common.dtos.EventType;
import com.niladri.common.dtos.events.EventMetadata;
import com.niladri.common.dtos.events.OrderCreatedEvent;
import com.niladri.orderservice.dto.OrderItemRequest;
import com.niladri.orderservice.dto.OrderItemResponse;
import com.niladri.orderservice.dto.OrderRequest;
import com.niladri.orderservice.dto.OrderResponse;
import com.niladri.orderservice.exception.OrderNotFoundException;
import com.niladri.orderservice.exception.OrderStatusException;
import com.niladri.orderservice.model.*;
import com.niladri.orderservice.repository.OrderRepository;
import com.niladri.orderservice.repository.OutboxEventRepository;
import com.niladri.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for user id: {}", orderRequest.getUserId());

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(orderRequest.getUserId())
                .shippingAddress(orderRequest.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                .map(itemRequest -> buildOrderItem(itemRequest, order))
                .toList();
        order.setOrderItems(orderItems);
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);


        List<com.niladri.common.dtos.events.OrderItem> orderedItemsForEvent =
                orderRequest.getOrderItems().stream().
                        map(item -> buildOrderItemForEvent(item, order)).toList();

        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .metadata(
                        EventMetadata.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(EventType.ORDER_CREATED)
                                .occurredAt(savedOrder.getCreatedAt().toString())
                                .build()
                ).
                orderId(savedOrder.getOrderNumber()).
                userId(savedOrder.getUserId()).
                items(orderedItemsForEvent).
                totalAmount(order.getTotalAmount()).
                build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("ORDER")
                .aggregateId(savedOrder.getOrderNumber())
                .eventType(EventType.ORDER_CREATED.name())
                .payload(toJson(orderCreatedEvent))
                .status(OutboxStatus.NEW)
                .nextAttemptAt(savedOrder.getCreatedAt().plusSeconds(30))
                .build();

//        genericEventProducer.publishEvent(Topics.ORDER_CREATED, savedOrder.getUserId().toString(), orderCreatedEvent);
        outboxEventRepository.save(outboxEvent);
        log.info("Order created with id: {} and number: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        return mapToOrderResponse(savedOrder);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order with number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user id: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating status of order id: {} to {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderStatusException("Cannot update status of a cancelled order");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderStatusException("Cannot update status of a delivered order");
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order id: {} status updated to {}", id, status);
        return mapToOrderResponse(updatedOrder);
    }

    @Override
    public void handleInventoryUnavailable(String orderId) {
        log.info("Handling inventory unavailable event for orderId: {}", orderId);
        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderId));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled due to inventory unavailability", orderId);
    }

    @Override
    public void handlePaymentSucceeded(String orderId) {
        log.info("Handling payment succeeded for orderId={}", orderId);
        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderId));
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order {} confirmed after payment success", orderId);
    }

    @Override
    public void handlePaymentFailed(String orderId) {
        log.info("Handling payment failed for orderId={}", orderId);
        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderId));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled due to payment failure", orderId);
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderStatusException("Cannot cancel an order that has been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order id: {} has been cancelled", id);
    }

    private OrderItem buildOrderItem(OrderItemRequest itemRequest, Order order) {
        BigDecimal totalPrice = itemRequest.getUnitPrice()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
        return OrderItem.builder()
                .productId(itemRequest.getProductId())
                .productName(itemRequest.getProductName())
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getUnitPrice())
                .totalPrice(totalPrice)
                .order(order)
                .build();
    }


    private com.niladri.common.dtos.events.OrderItem buildOrderItemForEvent(OrderItemRequest itemRequest, Order order) {
        return com.niladri.common.dtos.events.OrderItem.builder()
                .productId(itemRequest.getProductId())
                .quantity(itemRequest.getQuantity())
                .build();
    }

    private String generateOrderNumber() {
        return "order_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .orderItems(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
