package com.paymentservice.grpc;

import com.paymentservice.service.PaymentService;
import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository; // added
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class PaymentGrpcServiceImpl extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository; // inject repository

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        try {
            Long userId = request.getUserId();
            Long itemId = request.getItemId();

            // Step 1: Check if a completed payment already exists
            Optional<Payment> existing = paymentRepository
                    .findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED);

            if (existing.isPresent()) {
                PaymentResponse duplicateResponse = PaymentResponse.newBuilder()
                        .setStatus("DUPLICATE")
                        .setMessage("Payment already completed for this user and item.")
                        .build();

                responseObserver.onNext(duplicateResponse);
                responseObserver.onCompleted();
                return;
            }

            // Step 2: Proceed with payment
            Payment payment = paymentService.processPayment(
                    userId,
                    itemId,
                    request.getAmount()
            );

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setPaymentId(payment.getId())
                    .setStatus(payment.getStatus().name())
                    .setMessage("Payment successful")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            PaymentResponse response = PaymentResponse.newBuilder()
                    .setStatus("FAILED")
                    .setMessage(e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}