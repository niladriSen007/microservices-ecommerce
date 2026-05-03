package com.niladri.orderservice.error;

public class Retryable extends RuntimeException {
    public Retryable(String message) {
        super(message);
    }
}
