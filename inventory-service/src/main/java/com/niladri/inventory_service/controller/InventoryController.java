package com.niladri.inventory_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.niladri.inventory_service.service.impl.InventoryServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
	private final InventoryServiceImpl inventoryServiceImpl;
	
	@PostMapping("/product/add")
	public ResponseEntity<String> increaseProductQuantity(){
		return ResponseEntity.ok(inventoryServiceImpl.inventoryProductAddition());
	}
	
	@PostMapping("/product/delete")
	public ResponseEntity<String> reduceProductQuantity(){
		return ResponseEntity.ok(inventoryServiceImpl.inventoryProductDeletion());
	}
}
