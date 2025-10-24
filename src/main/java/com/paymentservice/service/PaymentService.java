package com.paymentservice.service;

import com.paymentservice.model.Payment;


/**
 * {@code PaymentService} defines the contract for handling payment operations.
 *
 * <p>This interface is implemented by {@link PaymentServiceImpl} to process
 * payments, perform validation, and persist the resulting {@link Payment}
 * entities into the database.</p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Validate payment eligibility and prevent duplicate payments.</li>
 *   <li>Simulate credit card payment processing.</li>
 *   <li>Persist successfully processed payments.</li>
 * </ul>
 *
 * <h2>UC5 Updates:</h2>
 * <ul>
 *   <li>Added parameters for simulated credit card processing.</li>
 *   <li>Returns a {@link Payment} entity on successful completion.</li>
 * </ul>
 *
 * <p><b>Author:</b> Erfan YousefMoumji</p>
 * <p><b>Date:</b> Oct 24, 2025</p>
 */
public interface PaymentService {

    /**
     * Processes a payment for a given user and item.
     *
     * @param userId          the ID of the user making the payment
     * @param itemId          the ID of the item being paid for
     * @param amount          the total payment amount
     * @param cardNumber      the credit card number (simulated)
     * @param cardHolderName  the cardholder's name
     * @param expiryDate      the card expiration date (MM/YY)
     * @param cvv             the card's security code
     * @return a {@link Payment} entity if processing succeeds
     * @throws IllegalStateException if payment is not valid for processing
     */
    Payment processPayment(Long userId,
                           Long itemId,
                           Double amount,
                           String cardNumber,
                           String cardHolderName,
                           String expiryDate,
                           String cvv);
}