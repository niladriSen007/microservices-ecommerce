package com.niladri.orderservice.model;

public enum OutboxStatus {
    NEW, PROCESSING, SENT, FAILED, DEAD
}
