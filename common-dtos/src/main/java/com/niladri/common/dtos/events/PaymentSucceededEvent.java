package com.niladri.common.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceededEvent {
    private EventMetadata metadata;
    private String orderId;
    private String userId;
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionId;
}
