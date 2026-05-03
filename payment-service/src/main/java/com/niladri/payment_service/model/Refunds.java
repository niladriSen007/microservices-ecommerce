package com.niladri.payment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "refunds")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refunds extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "refund_id", nullable = false, unique = true)
    private String refundId;

    @NotBlank
    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @NotBlank
    @Column(name = "order_id", nullable = false)
    private String orderId;

    @NotBlank
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @NotBlank
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    @Size(max = 500)
    @Column(name = "reason")
    private String reason;

    @Column(name = "provider_refund_id")
    private String providerRefundId;
}
