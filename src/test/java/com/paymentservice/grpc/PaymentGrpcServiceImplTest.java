package com.paymentservice.grpc;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import com.paymentservice.service.PaymentService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
/**
 * Unit Tests for PaymentGrpcServiceImpl
 * Location: test/java/com/paymentservice/grpc/PaymentGrpcServiceImplTest.java
 *
 * Overview:
 * This class tests the gRPC implementation of the Payment Service.
 * The PaymentGrpcServiceImpl class exposes the core payment logic to other microservices via gRPC calls.
 * These tests use Mockito to mock dependencies and verify service behavior across multiple scenarios.
 *
 * Scenarios Covered:
 * 1. testProcessPayment_Success:
 *    - Verifies that a successful payment (no existing duplicate) triggers processPayment
 *    - Confirms the response includes proper status, message, and ID.
 *
 * 2. testProcessPayment_Duplicate:
 *    - Mocks a situation where a completed payment already exists for the given user & item
 *    - Ensures the service returns a DUPLICATE status without processing again.
 *
 * 3. testProcessPayment_Failure:
 *    - Simulates a RuntimeException during payment processing
 *    - Validates that the response returns a FAILED status with the error message.
 *
 * Testing Tools and Concepts:
 * - JUnit 5 (for structuring tests)
 * - Mockito (for mocking services and repositories)
 * - ArgumentCaptor (to capture gRPC responses)
 * - StreamObserver (gRPC callback interface mocked to simulate client-side interaction)
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 24, 2025
 */

@ExtendWith(MockitoExtension.class)
class PaymentGrpcServiceImplTest {

    @InjectMocks
    private PaymentGrpcServiceImpl paymentGrpcService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StreamObserver<PaymentResponse> responseObserver;

    @Captor
    private ArgumentCaptor<PaymentResponse> responseCaptor;

    @Test
    void testProcessPayment_Success() {
        // Arrange
        Long userId = 1L;
        Long itemId = 101L;
        double amount = 29.99;
        String cardNumber = "4111111111111111";
        String cardHolderName = "Test User";
        String expirationDate = "12/29";
        String cvv = "123";

        PaymentRequest request = PaymentRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setAmount(amount)
                .setCardNumber(cardNumber)
                .setCardHolderName(cardHolderName)
                .setExpirationDate(expirationDate)
                .setCvv(cvv)
                .build();

        Payment payment = new Payment(userId, itemId, amount, PaymentStatus.COMPLETED, LocalDateTime.now());
        payment.setId(999L);

        when(paymentRepository.findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED))
                .thenReturn(Optional.empty());
        when(paymentService.processPayment(userId, itemId, amount, cardNumber, cardHolderName, expirationDate, cvv))
                .thenReturn(payment);

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(999L, response.getPaymentId());
        assertEquals("Payment successful", response.getMessage());
    }

    @Test
    void testProcessPayment_Duplicate() {
        // Arrange
        Long userId = 1L;
        Long itemId = 101L;
        double amount = 29.99;

        PaymentRequest request = PaymentRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setAmount(amount)
                .setCardNumber("4111111111111111")
                .setCardHolderName("John Doe")
                .setExpirationDate("12/30")
                .setCvv("123")
                .build();

        Payment existingPayment = new Payment(userId, itemId, amount, PaymentStatus.COMPLETED, LocalDateTime.now());

        // 🛠️ Use eq() to match exact enum instance
        when(paymentRepository.findByUserIdAndItemIdAndStatus(
                eq(userId), eq(itemId), eq(PaymentStatus.COMPLETED)))
                .thenReturn(Optional.of(existingPayment));

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assertEquals("DUPLICATE", response.getStatus());
        assertEquals("Payment already completed for this user and item.", response.getMessage());
    }

    @Test
    void testProcessPayment_Failure() {
        // Arrange
        Long userId = 1L;
        Long itemId = 101L;
        double amount = 29.99;
        String cardNumber = "4111111111111111";
        String cardHolderName = "Test User";
        String expirationDate = "12/29";
        String cvv = "123";

        PaymentRequest request = PaymentRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setAmount(amount)
                .setCardNumber(cardNumber)
                .setCardHolderName(cardHolderName)
                .setExpirationDate(expirationDate)
                .setCvv(cvv)
                .build();

        when(paymentRepository.findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED))
                .thenReturn(Optional.empty());
        when(paymentService.processPayment(userId, itemId, amount, cardNumber, cardHolderName, expirationDate, cvv))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assertEquals("FAILED", response.getStatus());
        assertEquals("Database error", response.getMessage());
    }

    @Test
    void testProcessPayment_CreditCardFieldsMissing() {
        // Arrange
        Long userId = 1L;
        Long itemId = 101L;
        double amount = 29.99;

        // All fields empty (you can test individually too)
        PaymentRequest request = PaymentRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setAmount(amount)
                .setCardNumber("")
                .setCardHolderName("")
                .setExpirationDate("")
                .setCvv("")
                .build();

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assertEquals("FAILED", response.getStatus());
        assertEquals("All credit card fields are required.", response.getMessage());
    }
}