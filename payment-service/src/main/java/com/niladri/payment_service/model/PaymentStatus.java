package com.niladri.payment_service.model;

public enum PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED,
    REFUNDED, PARTIALLY_REFUNDED, CANCELLED
}
