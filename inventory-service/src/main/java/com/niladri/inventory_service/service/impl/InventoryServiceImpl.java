package com.niladri.inventory_service.service.impl;


import com.niladri.common.dtos.EventType;
import com.niladri.common.dtos.events.*;
import com.niladri.inventory_service.constant.InventoryStatus;
import com.niladri.inventory_service.exception.ProductNotAvailable;
import com.niladri.inventory_service.model.Inventory;
import com.niladri.inventory_service.model.InventoryReservation;
import com.niladri.inventory_service.model.OutboxEvent;
import com.niladri.inventory_service.model.ReservationStatus;
import com.niladri.inventory_service.repository.InventoryRepository;
import com.niladri.inventory_service.repository.InventoryReservationRepository;
import com.niladri.inventory_service.repository.OutboxEventRepository;
import com.niladri.inventory_service.service.IInventoryService;
import com.niladri.inventory_service.service.InventorySaveService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements IInventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventorySaveService inventorySaveService;
    private final OutboxEventRepository outboxEventRepository;

    public String inventoryProductAddition() {
        return null;
    }

    public String inventoryProductDeletion() {
        return null;
    }

    @Override
    @Transactional
    public String reserveOrder(OrderCreatedEvent orderRequest) {
        log.info("Tying to reserve order for user {}", orderRequest.getUserId());

        List<OrderItem> items = orderRequest.getItems();
        List<String> productIdList = items.stream().map(OrderItem::getProductId).toList();

        // Fetch inventory for the products in the order
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIdList);
        // Map productId to Inventory for easy access -> productId : Inventory
        Map<String, Inventory> inventoryByProductId = inventories.stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));

        // Pass 1: validate ALL items before reserving anything.
        // If any single item is missing or has insufficient stock, save unavailable
        // event to outbox — nothing gets reserved for this order.
        for (OrderItem item : items) {
            Inventory inventory = inventoryByProductId.get(item.getProductId());
            if (inventory == null) {
                throw new ProductNotAvailable(
                        "Product with productId " + item.getProductId() + " not available");
            }
            if (inventory.getAvailableQty() < item.getQuantity()) {
                log.info("Insufficient stock for productId={}: requested={}, available={}",
                        item.getProductId(), item.getQuantity(), inventory.getAvailableQty());

                InventoryUnavailableEvent unavailableEvent = InventoryUnavailableEvent.builder()
                        .metadata(EventMetadata.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(EventType.INVENTORY_UNAVAILABLE)
                                .occurredAt(LocalDateTime.now().toString())
                                .build())
                        .orderId(orderRequest.getOrderId())
                        .productId(item.getProductId())
                        .build();

                saveOutboxEvent(orderRequest.getOrderId(), EventType.INVENTORY_UNAVAILABLE, unavailableEvent);
                return "Inventory Unavailable";
            }
        }

        // Pass 2: all items have sufficient stock — reserve everything.
        List<Inventory> inventoriesToSave = items.stream().map(item -> {
            Inventory inventory = inventoryByProductId.get(item.getProductId());
            // TODO : Check the available_qty - reserve_qty will it improve query performance ??
            long currentAvailableQuantity = inventory.getAvailableQty() - item.getQuantity();
            inventory.setAvailableQty(currentAvailableQuantity);
            inventory.setReserveQty(inventory.getReserveQty() + item.getQuantity());
            inventory.setStatus(
                    currentAvailableQuantity == 0 ? InventoryStatus.OUT_OF_STOCK : InventoryStatus.ACTIVE);
            return inventory;
        }).toList();

        List<InventoryReservation> inventoryReservationList = items.stream().map(item -> {
            InventoryReservation inventoryReservation = new InventoryReservation();
            inventoryReservation.setProductId(item.getProductId());
            inventoryReservation.setOrderId(orderRequest.getOrderId());
            inventoryReservation.setQuantity(item.getQuantity());
            inventoryReservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            inventoryReservation.setStatus(ReservationStatus.RESERVED);
            return inventoryReservation;
        }).toList();

        inventorySaveService.saveInventories(inventoriesToSave);
        inventorySaveService.saveReservations(inventoryReservationList);

        List<ReservedItem> reservedItems = items.stream()
                .map(item -> ReservedItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        InventoryReservedEvent reservedEvent = InventoryReservedEvent.builder()
                .metadata(EventMetadata.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(EventType.INVENTORY_RESERVED)
                        .occurredAt(LocalDateTime.now().toString())
                        .build())
                .orderId(orderRequest.getOrderId())
                .userId(orderRequest.getUserId().toString())
                .items(reservedItems)
                .totalAmount(orderRequest.getTotalAmount())
                .reservationStatus(ReservationStatus.RESERVED.name())
                .build();

        saveOutboxEvent(orderRequest.getOrderId(), EventType.INVENTORY_RESERVED, reservedEvent);
        return "Inventory Reserved";
    }

    @Override
    @Transactional
    public void confirmReservation(String orderId) {
        List<InventoryReservation> reservations = inventoryReservationRepository
                .findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        if (reservations.isEmpty()) {
            log.warn("No RESERVED reservations found for orderId={}", orderId);
            return;
        }

        List<String> productIds = reservations.stream().map(InventoryReservation::getProductId).toList();
        Map<String, Inventory> inventoryMap = inventoryRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(Inventory::getProductId, i -> i));

        for (InventoryReservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            Inventory inventory = inventoryMap.get(reservation.getProductId());
            if (inventory != null) {
                inventory.setReserveQty(Math.max(0, inventory.getReserveQty() - reservation.getQuantity()));
            }
        }

        inventoryReservationRepository.saveAll(reservations);
        inventoryRepository.saveAll(new java.util.ArrayList<>(inventoryMap.values()));
        log.info("Confirmed {} reservations for orderId={}", reservations.size(), orderId);
    }

    @Override
    @Transactional
    public void releaseReservation(String orderId) {
        List<InventoryReservation> reservations = inventoryReservationRepository
                .findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        if (reservations.isEmpty()) {
            log.warn("No RESERVED reservations found for orderId={}", orderId);
            return;
        }

        List<String> productIds = reservations.stream().map(InventoryReservation::getProductId).toList();
        Map<String, Inventory> inventoryMap = inventoryRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(Inventory::getProductId, i -> i));

        for (InventoryReservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RELEASED);
            Inventory inventory = inventoryMap.get(reservation.getProductId());
            if (inventory != null) {
                inventory.setAvailableQty(inventory.getAvailableQty() + reservation.getQuantity());
                inventory.setReserveQty(Math.max(0, inventory.getReserveQty() - reservation.getQuantity()));
                if (inventory.getAvailableQty() > 0) {
                    inventory.setStatus(InventoryStatus.ACTIVE);
                }
            }
        }

        inventoryReservationRepository.saveAll(reservations);
        inventoryRepository.saveAll(new java.util.ArrayList<>(inventoryMap.values()));
        log.info("Released {} reservations for orderId={}", reservations.size(), orderId);
    }

    private void saveOutboxEvent(String orderId, EventType eventType, Object payload) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("INVENTORY_RESERVATION")
                    .aggregateId(orderId)
                    .eventType(eventType.name())
                    .payload(payload)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event type={} for orderId={}", eventType, orderId);
        } catch (Exception e) {
            log.error("Failed to save outbox event type={} for orderId={}: {}", eventType, orderId, e.getMessage());
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }
}
