package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Find payment by payment ID
     */
    Optional<Payment> findByPaymentId(Integer paymentId);

    /**
     * Find all payments for a specific user
     */
    List<Payment> findByUserId(Integer userId);

    /**
     * Find all payments for a specific user with pagination
     */
    Page<Payment> findByUserId(Integer userId, Pageable pageable);

    /**
     * Find payments by status
     */
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    /**
     * Find payments by user and status
     */
    List<Payment> findByUserIdAndPaymentStatus(Integer userId, Payment.PaymentStatus status);

    /**
     * Find payments within a date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find payments for a specific item
     */
    List<Payment> findByItemId(Integer itemId);

    /**
     * Get total amount paid by a user
     */
    @Query("SELECT SUM(p.totalAmount) FROM Payment p WHERE p.userId = :userId AND p.paymentStatus = 'COMPLETED'")
    Double getTotalAmountPaidByUser(@Param("userId") Integer userId);

    /**
     * Count payments by status
     */
    long countByPaymentStatus(Payment.PaymentStatus status);

    /**
     * Find recent payments ordered by creation date
     */
    List<Payment> findTop10ByOrderByCreatedAtDesc();
}

