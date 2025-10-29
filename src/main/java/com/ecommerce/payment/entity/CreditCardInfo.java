package com.ecommerce.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCardInfo {

    @Column(name = "card_number_last_four", length = 4)
    private String cardNumberLastFour;

    @Column(name = "name_on_card", nullable = false)
    private String nameOnCard;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "expiry_date", length = 5)
    private String expiryDate;

    // Security code is never stored for PCI compliance
    // Only store masked/hashed information

    /**
     * Masks the credit card number and stores only last 4 digits
     */
    public void setMaskedCardNumber(String fullCardNumber) {
        if (fullCardNumber != null && fullCardNumber.length() >= 4) {
            this.cardNumberLastFour = fullCardNumber.substring(fullCardNumber.length() - 4);
            this.cardType = determineCardType(fullCardNumber);
        }
    }

    /**
     * Returns masked card number for display
     */
    public String getMaskedCardNumber() {
        if (cardNumberLastFour != null) {
            return "**** **** **** " + cardNumberLastFour;
        }
        return "****";
    }

    /**
     * Determines card type based on card number prefix
     */
    private String determineCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }

        String cleanNumber = cardNumber.replaceAll("\\s+", "");

        if (cleanNumber.startsWith("4")) {
            return "VISA";
        } else if (cleanNumber.startsWith("5")) {
            return "MASTERCARD";
        } else if (cleanNumber.startsWith("3")) {
            return "AMEX";
        } else if (cleanNumber.startsWith("6")) {
            return "DISCOVER";
        }

        return "OTHER";
    }
}
