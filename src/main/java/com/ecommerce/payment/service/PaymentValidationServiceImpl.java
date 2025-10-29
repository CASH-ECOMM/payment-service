///**
// * PaymentValidationServiceImpl
// * Location: service/PaymentValidationServiceImpl.java
// *
// * Overview:
// * This service class implements the PaymentValidationService interface and contains the business logic
// * for validating whether a payment is allowed for a specific auction item.
// *
// * Responsibilities:
// * - Prevent duplicate or unauthorized payments.
// * - Enforce business rules around one-time payments per item.
// *
// * Logic Summary:
// * - Queries the PaymentRepository for existing payments linked to the given itemId.
// * - If a record exists with status COMPLETED, returns false (payment not allowed).
// * - If no record exists or status is not COMPLETED, returns true (payment allowed).
// *
// * Design Rationale:
// * - Promotes separation of concerns by isolating validation logic from the main PaymentService.
// * - Enhances testability and modularity of the codebase.
// * - Enables reusability across other services or future extensions (e.g., auditing, fraud detection).
// *
// * Author: Erfan YousefMoumji
// * Date: Oct 24, 2025
// */
//package com.paymentservice.service;
//
//import com.paymentservice.model.Payment;
//import com.paymentservice.model.PaymentStatus;
//import com.paymentservice.repository.PaymentRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class PaymentValidationServiceImpl implements PaymentValidationService {
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Override
//    public boolean isValidForPayment(Long itemId) {
//        return paymentRepository.findByItemId(itemId)
//                .map(payment -> payment.getStatus() != PaymentStatus.COMPLETED)
//                .orElse(true);
//    }
//}
