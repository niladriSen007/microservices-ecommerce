package com.niladri.inventory_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.niladri.inventory_service.model.Inventory;
import com.niladri.inventory_service.model.InventoryReservation;
import com.niladri.inventory_service.repository.InventoryRepository;
import com.niladri.inventory_service.repository.InventoryReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventorySaveService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional
    public void saveInventories(List<Inventory> inventories) {
        inventoryRepository.saveAll(inventories);
    }

    @Transactional
    public void saveReservations(List<InventoryReservation> inventoryReservations) {
        inventoryReservationRepository.saveAll(inventoryReservations);
    }
}
