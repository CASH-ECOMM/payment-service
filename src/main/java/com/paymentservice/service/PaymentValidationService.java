/**
 * PaymentValidationService Class
 * Location: service/PaymentValidationService.java
 *
 * Overview:
 * This service contains the business logic to determine whether a payment can be
 * processed for a given auction item. It acts as a validator that ensures no duplicate
 * or unauthorized payments are made for items that have already been paid for.
 *
 * Purpose:
 * - To check if an item is still eligible for payment.
 * - Prevents multiple payments for the same auction item.
 * - Ensures payment integrity across the system.
 *
 * What it Does:
 * - Fetches existing payment record (if any) using PaymentRepository.
 * - If a record exists and its status is COMPLETED, further payment attempts are denied.
 * - If no record exists or the status is not COMPLETED, payment is allowed.
 *
 * Why It's a Separate Service:
 * - Follows separation of concerns:
 *     • Controller or gRPC: handles communication/interface
 *     • Service: handles business logic
 *     • Repository: handles data access
 * - Easier to test business rules in isolation.
 * - Keeps the codebase modular, readable, and reusable (e.g., for logging, auditing, or receipt generation).
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
@Service
public class PaymentValidationService {
    @Autowired
    private PaymentRepository paymentRepository;

    public boolean isValidForPayment(Long itemId) {
        return paymentRepository.findByItemId(itemId)
                .map(payment -> payment.getStatus() != PaymentStatus.COMPLETED)
                .orElse(true);
    }
}
