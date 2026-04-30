package com.niladri.inventory_service.service;

import com.niladri.inventory_service.constant.InventoryStatus;
import com.niladri.inventory_service.model.Inventory;
import com.niladri.inventory_service.model.InventoryReservation;
import com.niladri.inventory_service.model.ReservationStatus;
import com.niladri.inventory_service.repository.InventoryRepository;
import com.niladri.inventory_service.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationExpiryScheduler {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<InventoryReservation> expiredReservations = inventoryReservationRepository
                .findExpiredReservations(ReservationStatus.RESERVED, now);

        if (expiredReservations.isEmpty()) {
            log.info("No expired inventory reservations to release at {}", now);
            return;
        }

        List<Long> productIds = expiredReservations.stream()
                .map(InventoryReservation::getProductId)
                .distinct()
                .toList();

        Map<Long, Inventory> inventoryByProductId = inventoryRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));

        expiredReservations.forEach(reservation -> {
            Inventory inventory = inventoryByProductId.get(reservation.getProductId());
            if (inventory == null) {
                log.warn("Expired reservation {} refers to unknown product id {}", reservation.getReservationId(),
                        reservation.getProductId());
                return;
            }

            long releasedQuantity = reservation.getQuantity();
            inventory.setAvailableQty(inventory.getAvailableQty() + releasedQuantity);
            int updatedReserveQty = Math.max(inventory.getReserveQty() - reservation.getQuantity(), 0);
            inventory.setReserveQty(updatedReserveQty);
            inventory.setStatus(
                    inventory.getAvailableQty() == 0 ? InventoryStatus.OUT_OF_STOCK : InventoryStatus.ACTIVE);
            reservation.setStatus(ReservationStatus.EXPIRED);
        });

        inventoryRepository.saveAll(inventoryByProductId.values());
        inventoryReservationRepository.saveAll(expiredReservations);

        log.info("Released {} expired inventory reservations and restored inventory quantities",
                expiredReservations.size());
    }
}
