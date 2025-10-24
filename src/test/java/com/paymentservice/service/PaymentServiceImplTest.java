package com.paymentservice.service;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentValidationService paymentValidationService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Test
    void processPayment_validItem_shouldSavePayment() {
        // Arrange
        Long userId = 1L;
        Long itemId = 100L;
        Double amount = 50.0;
        String cardNumber = "4111111111111111";
        String cardHolderName = "Test User";
        String expirationDate = "12/29";
        String cvv = "123";

        String expectedLast4 = cardNumber.substring(cardNumber.length() - 4);
        Payment expectedSaved = new Payment(userId, itemId, amount, PaymentStatus.COMPLETED,
                LocalDateTime.now(), expectedLast4, cardHolderName);
        expectedSaved.setId(1001L);

        when(paymentValidationService.isValidForPayment(itemId)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedSaved);

        // Act
        Payment result = paymentService.processPayment(userId, itemId, amount, cardNumber,
                cardHolderName, expirationDate, cvv);

        // Assert
        verify(paymentValidationService).isValidForPayment(itemId);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment captured = paymentCaptor.getValue();
        assertEquals(userId, captured.getUserId());
        assertEquals(itemId, captured.getItemId());
        assertEquals(amount, captured.getAmount());
        assertEquals(PaymentStatus.COMPLETED, captured.getStatus());
        assertNotNull(captured.getCreatedAt());
        assertEquals(expectedLast4, captured.getCardLast4());
        assertEquals(cardHolderName, captured.getCardHolderName());

        assertEquals(expectedSaved, result);
    }

    @Test
    void processPayment_invalidItem_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        Long itemId = 200L;
        Double amount = 30.0;
        String cardNumber = "4111111111111111";
        String cardHolderName = "Test User";
        String expirationDate = "12/29";
        String cvv = "123";

        when(paymentValidationService.isValidForPayment(itemId)).thenReturn(false);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                paymentService.processPayment(userId, itemId, amount, cardNumber, cardHolderName, expirationDate, cvv)
        );

        assertEquals("Payment already completed for this item.", ex.getMessage());
        verify(paymentValidationService).isValidForPayment(itemId);
        verify(paymentRepository, never()).save(any());
    }
}