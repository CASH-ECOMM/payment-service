/**
 * PaymentStatus Enum
 * Location: model/PaymentStatus.java
 *
 * Overview:
 * This enum defines the possible statuses for a payment within the Auction System's Payment Microservice.
 * It is used by the Payment entity to represent and manage the state of a payment transaction.
 *
 * Purpose:
 * - Maintain consistency in tracking payment states.
 * - Help other services or business logic identify whether a payment is pending, completed, or failed.
 * - Simplify conditional checks in validation, processing, and receipt generation.
 *
 * Enum Values:
 * - PENDING: Indicates the payment is initiated but not completed.
 * - COMPLETED: Indicates the payment was successful.
 * - FAILED: Indicates the payment attempt was unsuccessful.
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 20, 2025
 */
package com.paymentservice.model;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}
