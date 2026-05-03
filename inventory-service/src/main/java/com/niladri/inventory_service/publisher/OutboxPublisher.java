package com.niladri.inventory_service.publisher;


import com.niladri.common.dtos.EventType;
import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.InventoryReservedEvent;
import com.niladri.common.dtos.events.InventoryUnavailableEvent;
import com.niladri.inventory_service.model.OutboxEvent;
import com.niladri.inventory_service.model.OutboxStatus;
import com.niladri.inventory_service.producers.IGenericEventProducer;
import com.niladri.inventory_service.repository.OutboxEventRepository;
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

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishEvent() {
        List<OutboxEvent> events = outboxEventRepository.fetchBatchForUpdate();

        for (OutboxEvent outboxEvent : events) {
            try {
                EventType eventType = EventType.valueOf(outboxEvent.getEventType());

                if (eventType == EventType.INVENTORY_RESERVED) {
                    InventoryReservedEvent event = objectMapper.convertValue(
                            outboxEvent.getPayload(), InventoryReservedEvent.class);
                    genericEventProducer.publishEvent(Topics.INVENTORY_RESERVED, outboxEvent.getAggregateId(), event);
                    log.info("Published INVENTORY_RESERVED event for orderId={}", outboxEvent.getAggregateId());

                } else if (eventType == EventType.INVENTORY_UNAVAILABLE) {
                    InventoryUnavailableEvent event = objectMapper.convertValue(
                            outboxEvent.getPayload(), InventoryUnavailableEvent.class);
                    genericEventProducer.publishEvent(Topics.INVENTORY_UNAVAILABLE, outboxEvent.getAggregateId(), event);
                    log.info("Published INVENTORY_UNAVAILABLE event for orderId={}", outboxEvent.getAggregateId());

                } else {
                    log.warn("Unknown eventType={} for outboxId={}, marking DEAD", outboxEvent.getEventType(), outboxEvent.getId());
                    outboxEvent.setStatus(OutboxStatus.DEAD);
                    outboxEvent.setUpdatedAt(LocalDateTime.now());
                    outboxEventRepository.save(outboxEvent);
                    continue;
                }

                outboxEvent.setStatus(OutboxStatus.SENT);

            } catch (Exception e) {
                int retry = outboxEvent.getRetryCount() + 1;
                outboxEvent.setRetryCount(retry);

                if (retry >= 3) {
                    outboxEvent.setStatus(OutboxStatus.DEAD);
                } else {
                    outboxEvent.setStatus(OutboxStatus.NEW);
                    outboxEvent.setNextAttemptAt(
                            LocalDateTime.now().plusSeconds((long) Math.pow(2, retry)));
                }

                log.error("Failed to publish outbox event type={} orderId={}: {}",
                        outboxEvent.getEventType(), outboxEvent.getAggregateId(), e.getMessage());
            }

            outboxEvent.setUpdatedAt(LocalDateTime.now());
            outboxEventRepository.save(outboxEvent);
        }
    }
}
