package com.niladri.orderservice.error;

public class NonRetryable extends RuntimeException {
    public NonRetryable(String message) {
        super(message);
    }
}
