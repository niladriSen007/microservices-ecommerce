package com.niladri.inventory_service.repository;

import com.niladri.inventory_service.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation,Long> {
}
