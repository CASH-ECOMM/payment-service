/**
 * Represents a payment record in the Payment Microservice of the Auction System.
 *
 * <p>This entity is mapped to the "payments" table in the PostgreSQL database.
 * Each instance corresponds to a single payment attempt (successful, failed, or pending)
 * made by a user for a specific auction item.</p>
 *
 * <p><b>Main Responsibilities:</b></p>
 * <ul>
 *   <li>Store detailed information about each payment transaction.</li>
 *   <li>Enable validation checks to see if an item has already been paid for.</li>
 *   <li>Support downstream operations like processing, auditing, and receipt generation.</li>
 * </ul>
 *
 * <p><b>Database Table:</b> payments</p>
 *
 * @author Erfan YousefMoumji
 * @since October 25, 2025
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

    /**
     * Constructs a new Payment record without an ID (used for creation).
     *
     * @param userId    ID of the user making the payment
     * @param itemId    ID of the item being paid for
     * @param amount    Transaction amount
     * @param status    Payment status
     * @param createdAt Timestamp of creation
     */
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
