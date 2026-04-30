package com.niladri.inventory_service.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.niladri.inventory_service.repository.InventoryRepository;
import com.niladri.inventory_service.service.IinventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IinventoryService  {
	private final InventoryRepository inventoryRepository;
	
	public String inventoryProductAddition(){
		return null;
	}
	public String inventoryProductDeletion(){
		return null;
	}
}
