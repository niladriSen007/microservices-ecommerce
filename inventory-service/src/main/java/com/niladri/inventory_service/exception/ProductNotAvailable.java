package com.niladri.inventory_service.exception;

public class ProductNotAvailable extends RuntimeException{
    public ProductNotAvailable(String message) {
        super(message);
    }
}
