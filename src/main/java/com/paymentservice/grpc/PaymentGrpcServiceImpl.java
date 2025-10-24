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