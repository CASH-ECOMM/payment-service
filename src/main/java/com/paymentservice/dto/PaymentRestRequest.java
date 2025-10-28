package com.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for creating payments via REST")
public class PaymentRestRequest {

    @Schema(description = "User ID making the payment", example = "123")
    private Long userId;

    @Schema(description = "Item ID to purchase", example = "456")
    private Long itemId;

    @Schema(description = "Amount to pay", example = "29.99")
    private Double amount;

    @Schema(description = "Credit card number", example = "4111111111111111")
    private String cardNumber;

    @Schema(description = "Name on the credit card", example = "John Doe")
    private String cardHolderName;

    @Schema(description = "Expiration date of the card (MM/YY)", example = "12/27")
    private String expirationDate;

    @Schema(description = "CVV code on the back of the card", example = "123")
    private String cvv;

    // Getters and Setters

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

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}