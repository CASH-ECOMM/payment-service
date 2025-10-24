package com.paymentservice.grpc;

import com.paymentservice.service.PaymentService;
import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * PaymentGrpcServiceImpl Class
 * Location: grpc/PaymentGrpcServiceImpl.java
 *
 * Overview:
 * This class defines the gRPC service implementation for processing auction payments
 * in a distributed microservices architecture. It is exposed via gRPC using the
 * Spring Boot gRPC starter (`@GrpcService`), acting as the communication bridge
 * between external clients and the internal `PaymentService` logic.
 *
 * Responsibilities:
 * - Receives payment requests via gRPC protocol.
 * - Validates incoming payment requests to prevent duplicates.
 * - Delegates valid payment requests to the core PaymentService.
 * - Returns structured gRPC responses based on success, failure, or duplication.
 *
 * Key Flow:
 * 1. **Validation**:
 *     • Checks if a payment for the given user and item with status COMPLETED already exists.
 *     • If so, returns a gRPC response with status "DUPLICATE" and exits early.
 * 2. **Processing**:
 *     • Calls the `processPayment()` method in PaymentService.
 *     • Logs and returns success response with payment ID and status.
 * 3. **Error Handling**:
 *     • Catches unexpected exceptions and returns a gRPC response with status "FAILED".
 *
 * Why It's Important:
 * - Enables other services or front-end clients to initiate payments via gRPC.
 * - Maintains transactional safety by checking for duplicates before processing.
 * - Central point for exposing payment functionality over a network interface.
 *
 * Dependencies:
 * - `PaymentService`: handles the core business logic for payment processing.
 * - `PaymentRepository`: queried directly to validate against duplicate payments.
 *
 * Logging:
 * - Uses SLF4J for request and error logging to trace gRPC calls and failures.
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 24, 2025
 */
@GrpcService
public class PaymentGrpcServiceImpl extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGrpcServiceImpl.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Long userId = request.getUserId();
        Long itemId = request.getItemId();
        double amount = request.getAmount();

        logger.info("Received gRPC payment request: userId={}, itemId={}, amount={}", userId, itemId, amount);

        try {
            // Step 1: Check for duplicate payment
            Optional<Payment> existing = paymentRepository
                    .findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED);

            if (existing.isPresent()) {
                logger.warn("Duplicate payment attempt detected for userId={} and itemId={}", userId, itemId);

                PaymentResponse duplicateResponse = PaymentResponse.newBuilder()
                        .setStatus("DUPLICATE")
                        .setMessage("Payment already completed for this user and item.")
                        .build();

                responseObserver.onNext(duplicateResponse);
                responseObserver.onCompleted();
                return;
            }

            // Step 2: Process new payment
            Payment payment = paymentService.processPayment(userId, itemId, amount);

            logger.info("Payment processed successfully: paymentId={}, status={}", payment.getId(), payment.getStatus());

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setPaymentId(payment.getId())
                    .setStatus(payment.getStatus().name())
                    .setMessage("Payment successful")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Payment processing failed: userId={}, itemId={}, amount={}, error={}", userId, itemId, amount, e.getMessage(), e);

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setStatus("FAILED")
                    .setMessage(e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}