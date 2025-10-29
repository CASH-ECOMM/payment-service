///**
// * PaymentRepository Interface
// * Location: repository/PaymentRepository.java
// *
// * Overview:
// * This is the Spring Data JPA repository interface for accessing and manipulating
// * Payment records stored in the PostgreSQL database. It extends JpaRepository to
// * provide out-of-the-box CRUD operations and includes custom query methods for
// * payment validation and retrieval logic.
// *
// * Purpose:
// * - Abstracts database interaction for Payment entities.
// * - Provides a clean API to query and manage payment records.
// * - Supports business logic that requires checking payment status before processing.
// *
// * Methods:
// * - findByItemId(Long itemId): Returns an Optional containing the payment (if any)
// *   associated with the given item ID.
// * - findByUserIdAndItemIdAndStatus(Long userId, Long itemId, PaymentStatus status):
// *   Returns a Payment if the given user has already made a payment for the item with
// *   the specified status (e.g., COMPLETED).
// *
// * Benefits:
// * - Eliminates boilerplate SQL using Spring Data JPA's dynamic query generation.
// * - Improves maintainability and testability of data access logic.
// * - Ensures separation between persistence and service/business layers.
// *
// * Design Rationale:
// * - Supports validation flows and enables efficient lookups for duplicate payments.
// * - Facilitates future expansion (e.g., querying payments by date, status, etc.).
// *
// * Author: Erfan YousefMoumji
// * Date: Oct 24, 2025
// */
//package com.paymentservice.repository;
//import com.paymentservice.model.Payment;
//import com.paymentservice.model.PaymentStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.Optional;
//
//public interface PaymentRepository extends JpaRepository<Payment, Long> {
//    Optional<Payment> findByItemId(Long itemId);
//    Optional<Payment> findByUserIdAndItemIdAndStatus(Long userId, Long itemId, PaymentStatus status);
//    int deleteByItemId(Long itemId);
//    boolean existsByUserIdAndItemId(Long userId, Long itemId);
//}
