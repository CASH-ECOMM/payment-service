package com.paymentservice.service;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
/**
 * Unit Tests for PaymentServiceImpl
 * Location: test/java/com/paymentservice/service/PaymentServiceImplTest.java
 *
 * Overview:
 * This class tests the core business logic in the PaymentServiceImpl class, which handles payment validation
 * and saving payment records to the database. It verifies both success and failure paths using Mockito for isolation.
 *
 * Scenarios Covered:
 * 1. processPayment_validItem_shouldSavePayment:
 *    - Simulates a successful payment flow when the item is valid for payment.
 *    - Verifies that the correct Payment object is constructed and persisted.
 *
 * 2. processPayment_invalidItem_shouldThrowException:
 *    - Tests the scenario where the item has already been paid for.
 *    - Confirms that no payment is saved and an exception is thrown with the expected message.
 *
 * Testing Tools and Concepts:
 * - JUnit 5 for test structure and assertions.
 * - Mockito for mocking dependencies like PaymentRepository and PaymentValidationService.
 * - ArgumentCaptor to inspect the actual Payment object passed to the repository.
 * - Behavior verification (e.g., ensuring save() is not called in invalid scenarios).
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 24, 2025
 */

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentValidationService paymentValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processPayment_validItem_shouldSavePayment() {
        // Arrange
        Long userId = 1L;
        Long itemId = 100L;
        Double amount = 50.0;

        when(paymentValidationService.isValidForPayment(itemId)).thenReturn(true);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        Payment savedPayment = new Payment(userId, itemId, amount, PaymentStatus.COMPLETED, LocalDateTime.now());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        Payment result = paymentService.processPayment(userId, itemId, amount);

        // Assert
        verify(paymentValidationService).isValidForPayment(itemId);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment captured = paymentCaptor.getValue();
        assertEquals(userId, captured.getUserId());
        assertEquals(itemId, captured.getItemId());
        assertEquals(amount, captured.getAmount());
        assertEquals(PaymentStatus.COMPLETED, captured.getStatus());
        assertNotNull(captured.getCreatedAt());

        assertEquals(savedPayment, result);
    }

    @Test
    void processPayment_invalidItem_shouldThrowException() {
        // Arrange
        Long itemId = 200L;
        when(paymentValidationService.isValidForPayment(itemId)).thenReturn(false);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                paymentService.processPayment(1L, itemId, 30.0));

        assertEquals("Payment already completed for this item.", ex.getMessage());
        verify(paymentValidationService).isValidForPayment(itemId);
        verify(paymentRepository, never()).save(any());
    }
}