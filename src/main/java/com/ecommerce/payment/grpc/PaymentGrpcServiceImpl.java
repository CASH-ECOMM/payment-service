package com.ecommerce.payment.grpc;

import com.ecommerce.payment.service.PaymentService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class PaymentGrpcServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentService paymentService;

    /**
     * Process payment - Main endpoint for Use Case 5
     */
    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("Received gRPC ProcessPayment request for user: {}", request.getUserInfo().getUserId());

        try {
            PaymentResponse response = paymentService.processPayment(request);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("ProcessPayment gRPC call completed successfully");
        } catch (Exception e) {
            log.error("Error in ProcessPayment gRPC call", e);
            responseObserver.onError(e);
        }
    }

    /**
     * Get payment by ID
     */
    @Override
    public void getPaymentById(GetPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        log.info("Received gRPC GetPaymentById request for payment ID: {}", request.getPaymentId());

        try {
            PaymentResponse response = paymentService.getPaymentById(request.getPaymentId());

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GetPaymentById gRPC call completed successfully");
        } catch (Exception e) {
            log.error("Error in GetPaymentById gRPC call", e);
            responseObserver.onError(e);
        }
    }

    /**
     * Get payment history for a user
     */
    @Override
    public void getPaymentHistory(PaymentHistoryRequest request, StreamObserver<PaymentHistoryResponse> responseObserver) {
        log.info("Received gRPC GetPaymentHistory request for user: {}", request.getUserId());

        try {
            List<PaymentResponse> payments = paymentService.getPaymentHistory(
                    request.getUserId(),
                    request.getPage(),
                    request.getSize()
            );

            PaymentHistoryResponse response = PaymentHistoryResponse.newBuilder()
                    .addAllPayments(payments)
                    .setTotalCount(payments.size())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("GetPaymentHistory gRPC call completed successfully");
        } catch (Exception e) {
            log.error("Error in GetPaymentHistory gRPC call", e);
            responseObserver.onError(e);
        }
    }
}
