package com.niladri.payment_service.service.impl;

import com.niladri.common.dtos.EventType;
import com.niladri.common.dtos.events.EventMetadata;
import com.niladri.common.dtos.events.InventoryReservedEvent;
import com.niladri.common.dtos.events.PaymentFailedEvent;
import com.niladri.common.dtos.events.PaymentSucceededEvent;
import com.niladri.payment_service.model.*;
import com.niladri.payment_service.repository.OutboxEventRepository;
import com.niladri.payment_service.repository.PaymentRepository;
import com.niladri.payment_service.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processPayment(InventoryReservedEvent event) {
        String idempotencyKey = event.getOrderId() + "-" + event.getMetadata().getEventId();

        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate payment request for orderId={}, skipping", event.getOrderId());
            return;
        }

        String paymentId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getTotalAmount().doubleValue())
                .currency("INR")
                .status(PaymentStatus.PROCESSING)
                .paymentMethod(PaymentMethod.CARD)
                .provider("INTERNAL")
                .idempotencyKey(idempotencyKey)
                .build();

        paymentRepository.save(payment);
        log.info("Payment record created paymentId={} orderId={}", paymentId, event.getOrderId());

        // TODO: replace with external payment gateway call
        boolean succeeded = simulatePayment(event);

        EventMetadata metadata = EventMetadata.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(succeeded ? EventType.PAYMENT_SUCCEEDED : EventType.PAYMENT_FAILED)
                .occurredAt(LocalDateTime.now().toString())
                .build();

        if (succeeded) {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            PaymentSucceededEvent succeededEvent = PaymentSucceededEvent.builder()
                    .metadata(metadata)
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .paymentId(paymentId)
                    .amount(event.getTotalAmount())
                    .currency("INR")
                    .paymentMethod(PaymentMethod.CARD.name())
                    .transactionId(UUID.randomUUID().toString())
                    .build();

            outboxEventRepository.save(OutboxEvent.builder()
                    .aggregateType("Payment")
                    .aggregateId(event.getOrderId())
                    .eventType(EventType.PAYMENT_SUCCEEDED.name())
                    .payload(objectMapper.convertValue(succeededEvent, Object.class))
                    .nextAttemptAt(LocalDateTime.now())
                    .build());

            log.info("Payment succeeded, outbox event queued for orderId={}", event.getOrderId());

        } else {
            String failureReason = "Payment processing failed";
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(failureReason);
            paymentRepository.save(payment);

            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .metadata(metadata)
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .paymentId(paymentId)
                    .amount(event.getTotalAmount())
                    .currency("INR")
                    .paymentMethod(PaymentMethod.CARD.name())
                    .failureReason(failureReason)
                    .build();

            outboxEventRepository.save(OutboxEvent.builder()
                    .aggregateType("Payment")
                    .aggregateId(event.getOrderId())
                    .eventType(EventType.PAYMENT_FAILED.name())
                    .payload(objectMapper.convertValue(failedEvent, Object.class))
                    .nextAttemptAt(LocalDateTime.now())
                    .build());

            log.info("Payment failed, outbox event queued for orderId={}", event.getOrderId());
        }
    }

    private boolean simulatePayment(InventoryReservedEvent event) {
        // Placeholder — always succeed until external gateway integrated
        return true;
    }
}
