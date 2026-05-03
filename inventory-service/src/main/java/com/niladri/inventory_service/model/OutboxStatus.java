package com.niladri.inventory_service.model;

public enum OutboxStatus {
    NEW, PROCESSING, SENT, FAILED, DEAD
}
