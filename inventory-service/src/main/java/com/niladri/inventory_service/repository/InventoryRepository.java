package com.niladri.inventory_service.repository;

import com.niladri.inventory_service.constant.InventoryStatus;
import com.niladri.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i.productId, i.availableQty FROM Inventory i WHERE i.productId IN :productIds")
    List<Object[]> getAvailableQuantityByProducts(@Param("productIds") List<Long> productIds);

    List<Inventory> findByProductIdIn(List<Long> productIds);

    List<Inventory> findByReserveExpiryBeforeAndReserveQtyGreaterThan(java.time.LocalDateTime now, int reserveQty);

    @Transactional
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQty = :availableQty, i.reserveQty = :reserveQty, i.status=:status WHERE i.productId = :productId")
    void updateQuantityAndStatus(@Param("productId") Long productId, @Param("availableQty") Long availableQuantity,
            @Param("reserveQty") Integer reserveQuantity, @Param("status") InventoryStatus inventoryStatus);

    Optional<Inventory> findByProductId(Long productId);

}
