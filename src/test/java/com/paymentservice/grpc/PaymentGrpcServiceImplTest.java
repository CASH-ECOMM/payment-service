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