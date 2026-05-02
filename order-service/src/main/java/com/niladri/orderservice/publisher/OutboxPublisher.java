package com.niladri.orderservice.publisher;

import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.OrderCreatedEvent;
import com.niladri.orderservice.model.OutboxEvent;
import com.niladri.orderservice.model.OutboxStatus;
import com.niladri.orderservice.producers.IGenericEventProducer;
import com.niladri.orderservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final IGenericEventProducer genericEventProducer;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void publishEvent() {
        List<OutboxEvent> events = outboxEventRepository.fetchBatchForUpdate();


        for (OutboxEvent outboxEvent : events) {

//            OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
//                    .metadata(
//                            EventMetadata.builder()
//                                    .eventId(UUID.randomUUID().toString())
//                                    .eventType(EventType.ORDER_CREATED)
//                                    .occurredAt(outboxEvent.getCreatedAt().toString())
//                                    .build()
//                    ).
//                    orderId(outboxEvent.getAggregateId()).
//                    userId(outboxEvent.get()).
//                    items(orderedItemsForEvent).
//                    totalAmount(order.getTotalAmount()).
//                    build();

            try {

                OrderCreatedEvent event = objectMapper.readValue(
                        outboxEvent.getPayload().toString(),
                        OrderCreatedEvent.class
                );

                genericEventProducer.publishEvent(Topics.ORDER_CREATED, outboxEvent.getAggregateId(), event);
                outboxEvent.setStatus(OutboxStatus.SENT);
                outboxEventRepository.save(outboxEvent);
                log.info("Published event to topic {}: {}", Topics.ORDER_CREATED, outboxEvent.getPayload());
            } catch (Exception e) {

                int retry = outboxEvent.getRetryCount() + 1;
                outboxEvent.setRetryCount(retry);

                if (retry >= 3) {
                    outboxEvent.setStatus(OutboxStatus.DEAD);
                } else {
                    outboxEvent.setStatus(OutboxStatus.NEW);
                    outboxEvent.setNextAttemptAt(
                            LocalDateTime.now().plusSeconds((long) Math.pow(2, retry))
                    );
                }

                log.error("Failed to publish event to topic {}: {}", Topics.ORDER_CREATED, e.getMessage());
                // Handle retry logic or error handling as needed
            }
            outboxEvent.setUpdatedAt(LocalDateTime.now());
            outboxEventRepository.save(outboxEvent);
        }
    }

}
