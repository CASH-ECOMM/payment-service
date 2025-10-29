package com.ecommerce.payment.util;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Component
public class CreditCardValidator {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern SECURITY_CODE_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    private static final Pattern EXPIRY_DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/([0-9]{2})$");

    /**
     * Validates credit card number using Luhn algorithm
     */
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        String cleanNumber = cardNumber.replaceAll("\\s+", "");

        if (!CARD_NUMBER_PATTERN.matcher(cleanNumber).matches()) {
            return false;
        }

        return luhnCheck(cleanNumber);
    }

    /**
     * Luhn algorithm implementation for card validation
     */
    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Validates security code (CVV/CVC)
     */
    public boolean isValidSecurityCode(String securityCode) {
        if (securityCode == null || securityCode.isEmpty()) {
            return false;
        }

        return SECURITY_CODE_PATTERN.matcher(securityCode).matches();
    }

    /**
     * Validates expiry date format and checks if card is not expired
     */
    public boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }

        if (!EXPIRY_DATE_PATTERN.matcher(expiryDate).matches()) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth cardExpiry = YearMonth.parse(expiryDate, formatter);
            YearMonth currentMonth = YearMonth.now();

            return !cardExpiry.isBefore(currentMonth);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates name on card
     */
    public boolean isValidNameOnCard(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Name should be at least 2 characters and contain only letters and spaces
        String trimmedName = name.trim();
        return trimmedName.length() >= 2 && trimmedName.matches("^[a-zA-Z\\s]+$");
    }

    /**
     * Comprehensive validation of all credit card information
     */
    public ValidationResult validateCreditCard(String cardNumber, String nameOnCard,
                                               String expiryDate, String securityCode) {
        ValidationResult result = new ValidationResult();

        if (!isValidCardNumber(cardNumber)) {
            result.addError("Invalid card number");
        }

        if (!isValidNameOnCard(nameOnCard)) {
            result.addError("Invalid name on card");
        }

        if (!isValidExpiryDate(expiryDate)) {
            result.addError("Invalid or expired card");
        }

        if (!isValidSecurityCode(securityCode)) {
            result.addError("Invalid security code");
        }

        return result;
    }

    /**
     * Inner class to hold validation results
     */
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();

        public void addError(String error) {
            this.valid = false;
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors.toString();
        }
    }
}
