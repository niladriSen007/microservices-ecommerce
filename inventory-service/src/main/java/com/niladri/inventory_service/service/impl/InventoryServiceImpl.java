package com.niladri.inventory_service.service.impl;

import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.OrderCreatedEvent;
import com.niladri.common.dtos.events.OrderItem;
import com.niladri.inventory_service.constant.InventoryStatus;
import com.niladri.inventory_service.exception.ProductNotAvailable;
import com.niladri.inventory_service.model.Inventory;
import com.niladri.inventory_service.model.InventoryReservation;
import com.niladri.inventory_service.model.ReservationStatus;
import com.niladri.inventory_service.producers.IGenericEventProducer;
import com.niladri.inventory_service.repository.InventoryRepository;
import com.niladri.inventory_service.service.IInventoryService;
import com.niladri.inventory_service.service.InventorySaveService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements IInventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventorySaveService inventorySaveService;
    private final IGenericEventProducer genericEventProducer;

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
        // If any single item is missing or has insufficient stock, publish the
        // unavailable event and stop — nothing gets reserved for this order.
        for (OrderItem item : items) {
            Inventory inventory = inventoryByProductId.get(item.getProductId());
            if (inventory == null) {
                throw new ProductNotAvailable(
                        "Product with productId " + item.getProductId() + " not available");
            }
            if (inventory.getAvailableQty() < item.getQuantity()) {
                log.info("Insufficient stock for productId={}: requested={}, available={}",
                        item.getProductId(), item.getQuantity(), inventory.getAvailableQty());
                genericEventProducer.publishEvent(Topics.INVENTORY_UNAVAILABLE,
                        orderRequest.getOrderId(),
                        item.getProductId());
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
        return "Inventory Reserved";
    }

}
