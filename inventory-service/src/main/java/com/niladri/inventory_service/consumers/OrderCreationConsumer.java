package com.niladri.inventory_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.OrderCreatedEvent;
import com.niladri.inventory_service.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreationConsumer {

    private final IInventoryService inventoryService;

    @KafkaListener(topics = Topics.ORDER_CREATED)
    public void consumeProductCreationEvent(@Payload OrderCreatedEvent orderCreatedEvent,
                                            @Header(value = "messageId", required = true) String messageId,
                                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) throws IOException {
        // in Non Retryable exception, the event will not be retried and will be discarded and the event will be stored into dead letter topic if configured
//        throw new NonRetryable("Simulated non-retryable exception for product creation event with ID: " + productCreatedEvent.getProductId());

        ObjectMapper mapper = new ObjectMapper();

//        OrderCreatedEvent event =
//                mapper.readValue((DataInput) orderCreatedEvent, OrderCreatedEvent.class);
        inventoryService.reserveOrder(orderCreatedEvent);

//        notificationRepository.findByMessageId(messageId).ifPresentOrElse(
//                existingNotification -> {
//                    log.info("Duplicate event received with messageId: {}, ignoring it.", messageId);
//                    return;
//                },
//                () -> {
//                    log.info("Processing new product creation event with messageId: {}", messageId);
//                }
//        );

        log.info("Received product creation event: {}", orderCreatedEvent.getMetadata());
        // Here you can add logic to process the event, e.g., save it to a database or trigger a notification

//        try {
//            ResponseEntity<String> res = restTemplate.exchange("http://localhost:8082/api/mock/error", HttpMethod.GET, null, String.class);
//            if (res.getStatusCode().is2xxSuccessful()) {
//                log.info("Successfully consumed product creation event");
//            }
//        } catch (
//                ResourceAccessException ex) { // mwans the resource is not running or there is a network issue, so we can retry
//            log.error("Failed to consume product creation event, : {}", ex.getMessage());
//            throw new Retryable("Simulated retryable exception for product creation event with ID: " + productCreatedEvent.getProductId());
//        } catch (HttpServerErrorException ex) {
//            log.error("Failed to consume product creation event, message - {}", ex.getMessage());
//            throw new NonRetryable("Simulated non-retryable exception for product creation event with ID: " + productCreatedEvent.getProductId());
//        } catch (Exception ex) {
//            log.error("Failed to consume product creation event, cause{}", ex.getMessage());
//            throw new NonRetryable("Simulated non-retryable exception for product creation event with ID: " + productCreatedEvent.getProductId());
//        }

//        try {
//            notificationRepository.save(ProcessedEvent.builder().messageId(messageId).productId(productCreatedEvent.getProductId()).build());
//        } catch (DataIntegrityViolationException ex) {
//            log.warn("Event with messageId {} has already been processed, skipping duplicate entry.", messageId);
//            throw new NonRetryable(ex.getMessage());
    }
}
