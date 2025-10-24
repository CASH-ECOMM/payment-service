package com.paymentservice.grpc;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import com.paymentservice.service.PaymentService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        Long userId = 1L;
        Long itemId = 101L;
        double amount = 29.99;

        PaymentRequest request = PaymentRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setAmount(amount)
                .build();

        Payment payment = new Payment(userId, itemId, amount, PaymentStatus.COMPLETED, LocalDateTime.now());
        payment.setId(999L);

        when(paymentRepository.findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED))
                .thenReturn(Optional.empty());

        when(paymentService.processPayment(userId, itemId, amount)).thenReturn(payment);

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assert response.getStatus().equals("COMPLETED");
        assert response.getPaymentId() == 999L;
        assert response.getMessage().equals("Payment successful");
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
                .build();

        Payment existingPayment = new Payment(userId, itemId, amount, PaymentStatus.COMPLETED, LocalDateTime.now());
        when(paymentRepository.findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED))
                .thenReturn(Optional.of(existingPayment));

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assert response.getStatus().equals("DUPLICATE");
        assert response.getMessage().equals("Payment already completed for this user and item.");
    }

    @Test
    void testProcessPayment_Failure() {
        // Arrange
        Long userId = 1L;
        Long itemId = 101L;
        double amount = 29.99;

        PaymentRequest request = PaymentRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setAmount(amount)
                .build();

        when(paymentRepository.findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED))
                .thenReturn(Optional.empty());

        when(paymentService.processPayment(userId, itemId, amount))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        paymentGrpcService.processPayment(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PaymentResponse response = responseCaptor.getValue();
        assert response.getStatus().equals("FAILED");
        assert response.getMessage().equals("Database error");
    }
}