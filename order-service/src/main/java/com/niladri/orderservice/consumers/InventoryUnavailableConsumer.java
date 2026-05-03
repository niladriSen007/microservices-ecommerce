package com.niladri.orderservice.consumers;

import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.InventoryUnavailableEvent;
import com.niladri.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryUnavailableConsumer {

    private final IOrderService orderService;

    @KafkaListener(topics = Topics.INVENTORY_UNAVAILABLE)
    public void consumeInventoryUnavailableEvent(
            @Payload InventoryUnavailableEvent event,
            @Header(value = "messageId", required = true) String messageId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        log.info("Received inventory-unavailable event: orderId={}, productId={}, messageId={}",
                event.getOrderId(), event.getProductId(), messageId);
        orderService.handleInventoryUnavailable(event.getOrderId());
    }
}
