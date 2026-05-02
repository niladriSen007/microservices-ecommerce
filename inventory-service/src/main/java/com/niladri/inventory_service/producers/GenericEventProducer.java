package com.niladri.inventory_service.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenericEventProducer implements IGenericEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishEvent(String topicName, String key, Object event) {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topicName, key, event);
        producerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        kafkaTemplate.send(producerRecord)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send event to topic: {}", topicName, exception);
                    } else {
                        log.info("Event sent successfully to topic: {} with key : {} where event is : {}", topicName, key, event.toString());
                    }
                });
    }
}