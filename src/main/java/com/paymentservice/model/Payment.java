package com.paymentservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * The {@code Payment} entity represents a completed or ongoing payment transaction
 * in the payment microservice. This model is mapped to the "payments" table in the database.
 *
 * <p>Each {@code Payment} holds metadata such as the user and item involved,
 * the amount paid, the current status, and timestamp of creation.</p>
 *
 * <p>Card details are stored partially for logging and display: only the last 4 digits
 * and the cardholder's name are persisted for reference (not full card details).</p>
 *
 * <p>It is used throughout the service layer and gRPC layer to create, retrieve,
 * and manipulate payment records.</p>
 *
 * @author
 * Erfan YousefMoumji
 * @since Oct 24, 2025
 */
@Entity
@Table(name = "payments")
public class Payment {

    /** Unique identifier for the payment (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID of the user who made the payment. */
    private Long userId;

    /** ID of the item for which the payment was made. */
    private Long itemId;

    /** Total amount paid in the transaction. */
    private Double amount;

    /** Current status of the payment (e.g., COMPLETED, FAILED). */
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    /** Timestamp when the payment was created/processed. */
    private LocalDateTime createdAt;

    /** Last 4 digits of the credit card used (for reference/logging only). */
    private String cardLast4;

    /** Full name of the cardholder (for display/logging). */
    private String cardHolderName;

    // --- Constructors ---

    /** Default no-arg constructor required by JPA. */
    public Payment() {
    }

    /**
     * Constructor used when storing full payment details including card metadata.
     *
     * @param userId ID of the user
     * @param itemId ID of the item
     * @param amount Amount paid
     * @param status Payment status
     * @param createdAt Timestamp of payment
     * @param cardLast4 Last 4 digits of card used
     * @param cardHolderName Name of cardholder
     */
    public Payment(Long userId, Long itemId, Double amount, PaymentStatus status,
                   LocalDateTime createdAt, String cardLast4, String cardHolderName) {
        this.userId = userId;
        this.itemId = itemId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.cardLast4 = cardLast4;
        this.cardHolderName = cardHolderName;
    }

    /**
     * Alternate constructor for when card metadata is not stored.
     *
     * @param userId ID of the user
     * @param itemId ID of the item
     * @param amount Amount paid
     * @param status Payment status
     * @param createdAt Timestamp of payment
     */
    public Payment(Long userId, Long itemId, Double amount, PaymentStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.cardHolderName = null;
        this.cardLast4 = null;
    }

    // --- Getters and Setters ---

    /** @return Unique payment ID */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** @return User ID associated with the payment */
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** @return Item ID associated with the payment */
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    /** @return Amount paid */
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /** @return Current status of the payment */
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    /** @return Timestamp of when the payment was created */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** @return Last 4 digits of card used */
    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    /** @return Cardholder name */
    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }


    /**
     * Provides a readable string representation of the Payment object.
     * Avoid logging full card details; this only shows the last 4 digits and name.
     */
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", userId=" + userId +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", cardLast4='" + cardLast4 + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                '}';
    }
}