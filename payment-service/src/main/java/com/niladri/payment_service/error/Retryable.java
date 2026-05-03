package com.niladri.payment_service.error;

public class Retryable extends RuntimeException {
    public Retryable(String message) {
        super(message);
    }
}
