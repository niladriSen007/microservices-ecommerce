package com.niladri.inventory_service.producers;

public interface IGenericEventProducer {
    void publishEvent(String topicName, String key, Object event);
}