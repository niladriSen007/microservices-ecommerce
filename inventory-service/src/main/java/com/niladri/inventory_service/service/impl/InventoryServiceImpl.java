package com.niladri.inventory_service.service.impl;

import com.niladri.inventory_service.constant.InventoryStatus;
import com.niladri.inventory_service.dto.OrderRequest;
import com.niladri.inventory_service.exception.ProductNotAvailable;
import com.niladri.inventory_service.exception.ProductStockNotAvailable;
import com.niladri.inventory_service.model.Inventory;
import com.niladri.inventory_service.model.InventoryReservation;
import com.niladri.inventory_service.model.ReservationStatus;
import com.niladri.inventory_service.repository.InventoryRepository;
import com.niladri.inventory_service.repository.InventoryReservationRepository;
import com.niladri.inventory_service.service.IInventoryService;
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
    private final InventoryReservationRepository inventoryReservationRepository;

    public String inventoryProductAddition() {
        return null;
    }

    public String inventoryProductDeletion() {
        return null;
    }

    @Override
    @Transactional
    public String reserveOrder(OrderRequest orderRequest) {
        log.info("Tying to reserve order for user {}", orderRequest.getUserId());

        List<OrderRequest.OrderItem> items = orderRequest.getItems();
        List<Long> productIdList = items.stream().map(OrderRequest.OrderItem::getProductId).toList();

        // Fetch inventory for the products in the order
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIdList);
        // Map productId to Inventory for easy access -> productId : Inventory
        Map<Long, Inventory> inventoryByProductId = inventories.stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));

        // Check availability and prepare inventory updates
        List<Inventory> inventoriesToSave = items.stream().map(item -> {
            Inventory inventory = inventoryByProductId.get(item.getProductId());
            if (inventory == null) {
                throw new ProductNotAvailable(
                        "Product with productId " + item.getProductId() + " not available");
            }
            // Check if the available quantity is sufficient to reserve the requested
            // quantity
            // TODO : Check the available_qty - reserve_qty will it improve query performance ??
            if (inventory.getAvailableQty() >= item.getQuantity()) {
                Integer reserved = item.getQuantity();
                long currentAvailableQuantity = inventory.getAvailableQty() - reserved;
                inventory.setAvailableQty(currentAvailableQuantity);
                inventory.setReserveQty(inventory.getReserveQty() + reserved);
                inventory.setReserveExpiry(LocalDateTime.now().plusMinutes(10));
                inventory.setStatus(
                        currentAvailableQuantity == 0 ? InventoryStatus.OUT_OF_STOCK : InventoryStatus.ACTIVE);
                return inventory;
            }

            throw new ProductStockNotAvailable(
                    "Stock for product " + item.getProductId() + " is not available");
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

        inventoryRepository.saveAll(inventoriesToSave);
        inventoryReservationRepository.saveAll(inventoryReservationList);
        return "Inventory Reserved";
    }

    @Override
    @Transactional
    public String releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Inventory> expiredInventories = inventoryRepository.findByReserveExpiryBeforeAndReserveQtyGreaterThan(now, 0);

        if (expiredInventories.isEmpty()) {
            return "No expired reservations found";
        }

        expiredInventories.forEach(inventory -> {
            inventory.setAvailableQty(inventory.getAvailableQty() + inventory.getReserveQty());
            inventory.setReserveQty(0);
            inventory.setReserveExpiry(null);
            inventory.setStatus(inventory.getAvailableQty() == 0 ? InventoryStatus.OUT_OF_STOCK : InventoryStatus.ACTIVE);
        });

        inventoryRepository.saveAll(expiredInventories);
        return expiredInventories.size() + " expired reservation(s) released";
    }

}
