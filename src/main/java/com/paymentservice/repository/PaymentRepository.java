/**
 * PaymentRepository Interface
 * Location: repository/PaymentRepository.java
 *
 * Overview:
 * This is the Spring Data JPA repository interface for accessing and manipulating
 * Payment records stored in the PostgreSQL database. It extends JpaRepository to
 * provide CRUD operations out-of-the-box and includes a custom query method.
 *
 * Purpose:
 * - Abstracts away database interactions for Payment entities.
 * - Enables querying for existing payments based on itemId.
 * - Used to determine if a payment has already been made for a given auction item.
 * - Plays a key role in payment validation logic and status checks.
 *
 * Methods:
 * - findByItemId(Long itemId): Returns an Optional containing the Payment (if any)
 *   that corresponds to the given item ID.
 *
 * Benefits:
 * - No need to write boilerplate SQL — Spring Data JPA auto-generates implementations.
 * - Clean separation of data access logic from business logic.
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 20, 2025
 */
package com.paymentservice.repository;
import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByItemId(Long itemId);
    Optional<Payment> findByUserIdAndItemIdAndStatus(Long userId, Long itemId, PaymentStatus status);
}
