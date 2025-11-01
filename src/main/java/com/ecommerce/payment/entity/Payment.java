package com.ecommerce.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "payment_id", updatable = false, nullable = false, unique = true)
    private Integer paymentId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "item_cost", nullable = false)
    private Integer itemCost;

    @Column(name = "shipping_cost", nullable = false)
    private Integer shippingCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_type", nullable = false)
    private ShippingType shippingType;

    @Column(name = "estimated_shipping_days")
    private Integer estimatedShippingDays;

    @Column(name = "hst_amount", nullable = false)
    private Double hstAmount;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Embedded
    private Address address;

    @Embedded
    private CreditCardInfo creditCardInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PROCESSING;
        }
        if (paymentId == null ) {
            paymentId = java.util.concurrent.ThreadLocalRandom.current()
                    .nextInt(100_000_000, Integer.MAX_VALUE);
        }
    }

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "payment_error_message")
    private String paymentErrorMessage;

    public enum ShippingType {
        REGULAR,
        EXPEDITED
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
