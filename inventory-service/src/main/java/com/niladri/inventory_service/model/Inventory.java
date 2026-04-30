package com.niladri.inventory_service.model;

import com.niladri.inventory_service.constant.InventoryStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseModel {
	// Unique identifier for the inventory record
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "inventory_id")
	private Long inventoryId;
	// The ID of the product this inventory entry refers to
	@NotNull
	@Column(name = "product_id", nullable = false)
	private Long productId;
	// The quantity of the product available for sale
	@NotNull
	@Column(name = "available_qty", nullable = false)
	@Min(0)
	private Long availableQty;
	// The quantity of the product currently reserved
	@NotNull
	@Column(name = "reserve_qty", nullable = false)
	@Min(0)
	private Integer reserveQty;
	// The current status of the inventory item
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@NotNull
	private InventoryStatus status;

	// The timestamp until which the reserved quantity is held
	@Column(name = "reserve_expiry")
	private LocalDateTime reserveExpiry;

	// Version number for optimistic locking
	@Version
	@Column(name = "version")
	private Long version;
}
