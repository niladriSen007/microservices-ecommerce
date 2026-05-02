package com.niladri.inventory_service.error;

public class Retryable extends RuntimeException {
    public Retryable(String message) {
        super(message);
    }
}
