package com.niladri.inventory_service.consumers;

import com.niladri.common.dtos.Topics;
import com.niladri.common.dtos.events.PaymentSucceededEvent;
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
public class PaymentSucceededConsumer {

    private final IInventoryService inventoryService;

    @KafkaListener(topics = Topics.PAYMENT_SUCCEEDED)
    public void consumePaymentSucceededEvent(
            @Payload PaymentSucceededEvent event,
            @Header(value = "messageId", required = true) String messageId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        log.info("Received PAYMENT_SUCCEEDED for orderId={} messageId={}", event.getOrderId(), messageId);
        inventoryService.confirmReservation(event.getOrderId());
    }
}
