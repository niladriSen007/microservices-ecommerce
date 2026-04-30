package com.niladri.inventory_service.model;

import com.niladri.inventory_service.constant.InventoryStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class Inventory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long indventoryId;
	private Long productId;
	private Long availableQty;
	private Long reserveQty;
	private InventoryStatus status;
	@Version
	private String version;
}
