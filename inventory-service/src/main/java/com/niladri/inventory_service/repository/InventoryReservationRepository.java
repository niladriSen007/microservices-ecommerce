package com.niladri.inventory_service.repository;

import com.niladri.inventory_service.model.InventoryReservation;
import com.niladri.inventory_service.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    @Query("SELECT r FROM InventoryReservation r WHERE r.status = :status AND r.expiresAt < :expiresAt")
    List<InventoryReservation> findExpiredReservations(@Param("status") ReservationStatus status,
            @Param("expiresAt") LocalDateTime expiresAt);

    List<InventoryReservation> findByOrderIdAndStatus(String orderId, ReservationStatus status);
}
