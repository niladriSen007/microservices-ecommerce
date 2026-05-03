package com.niladri.inventory_service.consumers;

import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.PaymentFailedEvent;
import com.niladri.inventory_service.service.IInventoryService;
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
public class PaymentFailedConsumer {

    private final IInventoryService inventoryService;

    @KafkaListener(topics = Topics.PAYMENT_FAILED)
    public void consumePaymentFailedEvent(
            @Payload PaymentFailedEvent event,
            @Header(value = "messageId", required = true) String messageId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        log.info("Received PAYMENT_FAILED for orderId={} reason={} messageId={}",
                event.getOrderId(), event.getFailureReason(), messageId);
        inventoryService.releaseReservation(event.getOrderId());
    }
}
