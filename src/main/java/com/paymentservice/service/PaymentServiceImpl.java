package com.paymentservice.service;

import com.paymentservice.exception.PaymentValidationException;
import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * {@code PaymentServiceImpl} implements {@link PaymentService} and handles
 * the core business logic for processing and validating payments.
 *
 * <p>This implementation ensures that each payment is valid, simulates card
 * processing checks, securely masks sensitive data, and persists payment records
 * into the database.</p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Validate payment eligibility via {@link PaymentValidationService}.</li>
 *   <li>Simulate basic credit card validation (format and non-empty fields).</li>
 *   <li>Mask sensitive card data before saving and logging.</li>
 *   <li>Create and persist a {@link Payment} entity to the database.</li>
 * </ul>
 *
 * <h2>Security:</h2>
 * <p>Only the last 4 digits of the credit card number and the cardholder’s name
 * are stored for reference to maintain PCI compliance and protect user data.</p>
 *
 * <p><b>Author:</b> Erfan YousefMoumji</p>
 * <p><b>Date:</b> Oct 24, 2025</p>
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidationService paymentValidationService;

    /**
     * Processes a payment by validating eligibility, checking credit card fields,
     * masking sensitive data, and persisting the result.
     *
     * @param userId          the user’s ID
     * @param itemId          the item being purchased
     * @param amount          the total amount
     * @param cardNumber      the full (simulated) card number
     * @param cardHolderName  the name on the card
     * @param expirationDate  the card expiration date
     * @param cvv             the card’s CVV code
     * @return the saved {@link Payment} entity
     * @throws IllegalStateException    if payment is already completed
     * @throws IllegalArgumentException if card fields are invalid
     */

    @Override
    public Payment processPayment(Long userId, Long itemId, Double amount,
                                  String cardNumber, String cardHolderName, String expirationDate, String cvv) {

        // Validate payment eligibility =====
        if (!paymentValidationService.isValidForPayment(itemId)) {
            throw new IllegalStateException("Payment already completed for this item.");
        }
        if (amount == null || amount <= 0) {
            throw new PaymentValidationException("Payment amount must be greater than zero.");
        }
        // duplicate payment check
        boolean duplicate = paymentRepository.existsByUserIdAndItemId(userId, itemId);
        if (duplicate) {
            throw new PaymentValidationException("Duplicate payment: this user already paid for this item.");
        }

        // ===== Step 2: Validate credit card fields =====
        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
            throw new IllegalArgumentException("Invalid card number.");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4) {
            throw new IllegalArgumentException("Invalid CVV.");
        }
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cardholder name is required.");
        }
        if (expirationDate == null || expirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiration date is required.");
        }

        // Mask and log safely
        String maskedCard = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        String last4 = cardNumber.substring(cardNumber.length() - 4);

        System.out.println("Processing payment for item " + itemId +
                " with card ending in " + last4 + " belonging to " + cardHolderName);

        // Create Payment entity
        Payment payment = new Payment(
                userId,
                itemId,
                amount,
                PaymentStatus.COMPLETED,
                LocalDateTime.now(),
                last4,                // securely store only last 4 digits
                cardHolderName        // store cardholder name for reference
        );

        // Save and return
        return paymentRepository.save(payment);
    }
}