package com.niladri.payment_service.error;

public class NonRetryable extends RuntimeException {
    public NonRetryable(String message) {
        super(message);
    }
}
