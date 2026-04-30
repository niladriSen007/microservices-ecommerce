package com.niladri.inventory_service.controller;

import com.niladri.inventory_service.dto.OrderRequest;
import com.niladri.inventory_service.service.IInventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.niladri.inventory_service.service.impl.InventoryServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

	private final IInventoryService inventoryService;
	
	@PostMapping("/product/add")
	public ResponseEntity<String> increaseProductQuantity(@RequestBody  OrderRequest  orderRequest) {
		return ResponseEntity.ok(inventoryService.reserveOrder(orderRequest));
	}
	
	@PostMapping("/product/delete")
	public ResponseEntity<String> reduceProductQuantity(){
		return ResponseEntity.ok(inventoryService.inventoryProductDeletion());
	}
}
