package com.ecommerce.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id", updatable = false, nullable = false)
    private Integer receiptId;

    @OneToOne
    @JoinColumn(
            name = "payment_id",                 // column in receipts table
            referencedColumnName = "payment_id", // CHANGED: join to Payment.paymentId (int)
            nullable = false,
            unique = true
    )
    private Payment payment;

    @Column(name = "receipt_number", unique = true, nullable = false)
    private String receiptNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_address", nullable = false, length = 500)
    private String customerAddress;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "item_cost", nullable = false)
    private Integer itemCost;

    @Column(name = "shipping_cost", nullable = false)
    private Integer shippingCost;

    @Column(name = "hst_amount", nullable = false)
    private Double hstAmount;

    @Column(name = "total_paid", nullable = false)
    private Double totalPaid;

    @Column(name = "payment_method")
    private String paymentMethod;

    @CreationTimestamp
    @Column(name = "receipt_date", updatable = false)
    private LocalDateTime receiptDate;

    @Column(name = "shipping_estimate_days")
    private Integer shippingEstimateDays;

    /**
     * Generates a formatted receipt number based on timestamp
     */
    @PrePersist
    public void generateReceiptNumber() {
        if (this.receiptNumber == null) {
            this.receiptNumber = "RCP-" + System.currentTimeMillis();
        }
    }
}
