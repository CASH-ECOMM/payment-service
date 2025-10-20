package com.paymentservice.grpc;

import com.paymentservice.service.PaymentService;
import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

// This is your real implementation of the generated gRPC abstract class
@GrpcService
public class PaymentGrpcServiceImpl extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {

    @Autowired
    private PaymentService paymentService;

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        try {
            Payment payment = paymentService.processPayment(
                    request.getUserId(),
                    request.getItemId(),
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