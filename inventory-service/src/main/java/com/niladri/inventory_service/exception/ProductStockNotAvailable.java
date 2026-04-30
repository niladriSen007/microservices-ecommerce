package com.niladri.inventory_service.exception;

public class ProductStockNotAvailable extends RuntimeException{
    public ProductStockNotAvailable(String message) {
        super(message);
    }
}
