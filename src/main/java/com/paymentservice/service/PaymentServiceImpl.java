package com.paymentservice.service;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of the {@link PaymentService} interface that handles payment processing logic.
 *
 * <p>This service ensures that a payment is only processed if it is valid (e.g., no duplicate payments).
 * It uses a {@link PaymentValidationService} to verify the eligibility of an item for payment
 * and then persists the payment data through the {@link PaymentRepository}.</p>
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Validates if the item is eligible for payment.</li>
 *     <li>Creates a new {@link Payment} object with status COMPLETED and the current timestamp.</li>
 *     <li>Persists the payment to the database.</li>
 * </ul>
 * </p>
 *
 * @author Erfan
 * @since Oct 2025
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidationService paymentValidationService;

    @Override
    public Payment processPayment(Long userId, Long itemId, Double amount) {
        boolean isValid = paymentValidationService.isValidForPayment(itemId);

        if (!isValid) {
            throw new IllegalStateException("Payment already completed for this item.");
        }

        Payment payment = new Payment(
                userId,
                itemId,
                amount,
                PaymentStatus.COMPLETED,
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }
}