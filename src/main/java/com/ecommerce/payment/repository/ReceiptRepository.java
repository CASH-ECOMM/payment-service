package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {

    /**
     * Find receipt by receipt ID
     */
    Optional<Receipt> findByReceiptId(Integer receiptId);

    /**
     * Find receipt by receipt number
     */
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    /**
     * Find receipt by payment ID
     */
    @Query("SELECT r FROM Receipt r WHERE r.payment.paymentId = :paymentId")
    Optional<Receipt> findByPaymentId(@Param("paymentId") Integer paymentId);

    /**
     * Find all receipts for a specific user (through payment relationship)
     */
    @Query("SELECT r FROM Receipt r WHERE r.payment.userId = :userId ORDER BY r.receiptDate DESC")
    List<Receipt> findReceiptsByUserId(@Param("userId") Integer userId);

    /**
     * Find receipts by item ID
     */
    List<Receipt> findByItemId(Integer itemId);

    /**
     * Check if receipt exists for a payment
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Receipt r WHERE r.payment.paymentId = :paymentId")
    boolean existsByPaymentId(@Param("paymentId") Integer paymentId);
}
