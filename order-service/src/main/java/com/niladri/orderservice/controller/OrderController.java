package com.niladri.orderservice.controller;

import com.niladri.orderservice.dto.ApiResponse;
import com.niladri.orderservice.dto.OrderRequest;
import com.niladri.orderservice.dto.OrderResponse;
import com.niladri.orderservice.model.OrderStatus;
import com.niladri.orderservice.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

        private final IOrderService orderService;

        @PostMapping
        public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.<OrderResponse>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.CREATED.value())
                                                .data(orderService.createOrder(orderRequest))
                                                .message("Order created successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
                return ResponseEntity.ok(
                                ApiResponse.<OrderResponse>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(orderService.getOrderById(id))
                                                .message("Order retrieved successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @GetMapping("/number/{orderNumber}")
        public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
                return ResponseEntity.ok(
                                ApiResponse.<OrderResponse>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(orderService.getOrderByOrderNumber(orderNumber))
                                                .message("Order retrieved successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
                return ResponseEntity.ok(
                                ApiResponse.<List<OrderResponse>>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(orderService.getAllOrders())
                                                .message("Orders retrieved successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(@PathVariable Long userId) {
                return ResponseEntity.ok(
                                ApiResponse.<List<OrderResponse>>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(orderService.getOrdersByUserId(userId))
                                                .message("Orders retrieved successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @GetMapping("/status/{status}")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
                return ResponseEntity.ok(
                                ApiResponse.<List<OrderResponse>>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(orderService.getOrdersByStatus(status))
                                                .message("Orders retrieved successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @PatchMapping("/{id}/status")
        public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
                        @PathVariable Long id,
                        @RequestParam OrderStatus status) {
                return ResponseEntity.ok(
                                ApiResponse.<OrderResponse>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(orderService.updateOrderStatus(id, status))
                                                .message("Order status updated successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }

        @DeleteMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
                orderService.cancelOrder(id);
                return ResponseEntity.ok(
                                ApiResponse.<Void>builder()
                                                .success(true)
                                                .statusCode(HttpStatus.OK.value())
                                                .message("Order canceled successfully")
                                                .timestamp(Instant.now())
                                                .build());
        }
}
