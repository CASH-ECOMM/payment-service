package com.paymentservice.service;
/**
 * PaymentValidationService Interface
 * Location: service/PaymentValidationService.java
 *
 * Overview:
 * Defines the contract for validating whether a payment can be processed
 * for a given auction item.
 *
 * Purpose:
 * - Abstracts the logic that determines payment eligibility.
 * - Allows multiple implementations if business rules evolve in the future.
 *
 * Method:
 * - isValidForPayment(Long itemId):
 *     • Returns true if payment is allowed for the specified item.
 *     • Returns false if the item has already been paid for (COMPLETED).
 *
 * Design Rationale:
 * - Promotes separation of concerns by delegating validation logic to a dedicated component.
 * - Supports dependency injection and easier testing via mocking.
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 24, 2025
 */

public interface PaymentValidationService {
    boolean isValidForPayment(Long itemId);
}
