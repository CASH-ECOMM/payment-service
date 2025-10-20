/**
 * PaymentService.java
 * Location: service/PaymentService.java
 *
 * Overview:
 * This service class handles the core logic of processing a payment request.
 * It acts as a bridge between the controller/gRPC layer and the repository/business logic.
 *
 * Responsibilities:
 * - Validates if the payment is allowed using PaymentValidationService
 * - Creates and saves a new Payment entity to the database
 * - Returns the saved payment record
 *
 * Why it's needed:
 * - Keeps payment processing logic centralized and reusable
 * - Separates concerns from controller and repository
 * - Makes unit testing and maintenance easier
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 20, 2025
 */
package com.paymentservice.service;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidationService paymentValidationService;

    /**
     * Process a payment for a given user and item.
     * @param userId The ID of the user making the payment
     * @param itemId The ID of the item being paid for
     * @param amount The amount to be paid
     * @return Saved Payment object
     * @throws IllegalStateException if payment is not allowed
     */
    public Payment processPayment(Long userId, Long itemId, Double amount) {
        // Check if the item is eligible for payment
        boolean isValid = paymentValidationService.isValidForPayment(itemId);

        if (!isValid) {
            throw new IllegalStateException("Payment already completed for this item.");
        }

        // Create new Payment object
        Payment payment = new Payment(
                userId,
                itemId,
                amount,
                PaymentStatus.COMPLETED, // You may change to PENDING if payment is asynchronous
                LocalDateTime.now()
        );

        // Save to database
        return paymentRepository.save(payment);
    }
}

