package com.ecommerce.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", updatable = false, nullable = false)
    private String paymentId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "item_cost", nullable = false)
    private Double itemCost;

    @Column(name = "shipping_cost", nullable = false)
    private Double shippingCost;

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
