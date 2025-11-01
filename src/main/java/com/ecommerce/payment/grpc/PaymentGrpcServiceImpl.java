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

        String validationError = validateRequest(request);
        if (validationError != null) {
            responseObserver.onNext(failureResponse(validationError));
            responseObserver.onCompleted();
            return;
        }
        try {
            PaymentResponse response = paymentService.processPayment(request);
            if (response.getTransactionDate().isBlank()) {
                String now = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                response = PaymentResponse.newBuilder(response)
                        .setTransactionDate(now)
                        .build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("ProcessPayment gRPC call completed successfully");
        } catch (Exception e) {
            log.error("Error in ProcessPayment gRPC call", e);
            String msg = "An error occurred while processing your payment: " + e.getMessage();
            responseObserver.onNext(failureResponse(msg));
            responseObserver.onCompleted();
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

    private String validateRequest(PaymentRequest r) {
        if (!r.hasUserInfo()) return "Missing user information.";
        if (r.getUserInfo().getUserId().isBlank()) return "Missing userId.";
        if (r.getItemId().isBlank()) return "Missing itemId.";
        if (r.getItemCost() < 0) return "itemCost must be non-negative.";
        if (!r.hasShippingInfo()) return "Missing shipping info.";
        if (!r.hasCreditCardInfo()) return "Missing credit card info.";
        if (r.getCreditCardInfo().getCardNumber().isBlank()) return "Missing card number.";
        if (r.getCreditCardInfo().getNameOnCard().isBlank()) return "Missing name on card.";
        if (r.getCreditCardInfo().getExpiryDate().isBlank()) return "Missing expiry date.";
        if (r.getCreditCardInfo().getSecurityCode().isBlank()) return "Missing security code.";
        return null;
    }
    private PaymentResponse failureResponse(String message) {
        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return PaymentResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .setTransactionDate(now)
                .build();
    }
}
