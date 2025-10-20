/**
 * Payment Entity Class
 * Location: model/Payment.java
 *
 * Overview:
 * This class represents the Payment entity in the Auction System’s Payment Microservice.
 * It is mapped to the "payments" table in the PostgreSQL database.
 * Each record represents a single payment attempt made by a user for an auction item.
 *
 * Purpose:
 * - Store all payment attempts, whether successful, failed, or pending.
 * - Allow lookup to verify if an auction item has already been paid for.
 * - Serve as the foundation for future payment operations such as validation, processing, and receipt generation.
 *
 * Database Mapping:
 * - Uses JPA annotations for entity persistence.
 * - Mapped to the "payments" table.
 *
 * Fields and Roles:
 * - id: Primary key (unique identifier for each payment).
 * - userId: Identifies the user who initiated the payment.
 * - itemId: Identifies the auction item associated with the payment.
 * - amount: Represents the transaction amount.
 * - status: Enum (PENDING, COMPLETED, FAILED) indicating the payment state.
 * - createdAt: Timestamp of when the payment record was created (for logging and audits).
 *
 * Design Rationale:
 * - Keeps all payment information centralized for auditing and business logic.
 * - Simplifies validation by linking payments directly to users and items.
 * - Enables clean integration with gRPC calls for routing and validation.
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 20, 2025
 */
package com.paymentservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long itemId;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    // Default constructor
    public Payment() {
    }

    // Parameterized constructor (excluding id)
    public Payment(Long userId, Long itemId, Double amount, PaymentStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", userId=" + userId +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
