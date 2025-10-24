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

public interface PaymentService {
    Payment processPayment(Long userId, Long itemId, Double amount);
}