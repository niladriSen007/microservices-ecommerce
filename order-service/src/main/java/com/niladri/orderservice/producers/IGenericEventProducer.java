package com.niladri.orderservice.producers;

public interface IGenericEventProducer {
    void publishEvent(String topicName, String key, Object event);
}
