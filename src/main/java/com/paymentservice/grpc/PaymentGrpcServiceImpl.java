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
 * {@code PaymentGrpcServiceImpl} provides the gRPC interface for handling payment operations.
 *
 * <p>This class acts as a bridge between remote clients and the {@link PaymentService} business logic.
 * It validates payment requests, prevents duplicate transactions, and returns structured responses
 * to gRPC clients.</p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Receive and handle gRPC payment requests.</li>
 *     <li>Validate input fields and check for duplicate completed payments.</li>
 *     <li>Delegate valid payments to {@link PaymentService} for processing.</li>
 *     <li>Respond with detailed gRPC messages for success, duplication, or failure cases.</li>
 * </ul>
 *
 * <h2>Key Flow:</h2>
 * <ol>
 *     <li>Check for missing credit card fields → respond with {@code FAILED}.</li>
 *     <li>Check for duplicate completed payments → respond with {@code DUPLICATE}.</li>
 *     <li>Otherwise, process the payment and respond with {@code COMPLETED}.</li>
 * </ol>
 *
 * <h2>Dependencies:</h2>
 * <ul>
 *     <li>{@link PaymentService} — contains core business logic for processing payments.</li>
 *     <li>{@link PaymentRepository} — used for duplicate payment validation.</li>
 * </ul>
 *
 * <h2>Logging:</h2>
 * Uses SLF4J for structured logging to track incoming requests, results, and errors.
 *
 * <p><b>Author:</b> Erfan YousefMoumji</p>
 * <p><b>Date:</b> Oct 24, 2025</p>
 */
@GrpcService
public class PaymentGrpcServiceImpl extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGrpcServiceImpl.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Handles incoming gRPC payment requests by validating and processing them.
     *
     * @param request the incoming payment request containing user, item, and card info
     * @param responseObserver gRPC stream observer used to send responses back to the client
     */
    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Long userId = request.getUserId();
        Long itemId = request.getItemId();
        double amount = request.getAmount();

        String cardNumber = request.getCardNumber();
        String cardHolderName = request.getCardHolderName();
        String expirationDate = request.getExpirationDate();
        String cvv = request.getCvv();

        logger.info("Received gRPC payment request: userId={}, itemId={}, amount={}, cardHolder={}",
                userId, itemId, amount, cardHolderName);

        try {
            // Validate mandatory card fields
            if (cardNumber.isEmpty() || cardHolderName.isEmpty() || expirationDate.isEmpty() || cvv.isEmpty()) {
                responseObserver.onNext(PaymentResponse.newBuilder()
                        .setStatus("FAILED")
                        .setMessage("All credit card fields are required.")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Prevent duplicate payments
            Optional<Payment> existing = paymentRepository
                    .findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED);

            if (existing.isPresent()) {
                logger.warn("Duplicate payment attempt detected for userId={} and itemId={}", userId, itemId);

                responseObserver.onNext(PaymentResponse.newBuilder()
                        .setStatus("DUPLICATE")
                        .setMessage("Payment already completed for this user and item.")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Process new valid payment
            Payment payment = paymentService.processPayment(
                    userId, itemId, amount, cardNumber, cardHolderName, expirationDate, cvv);

            logger.info("Payment processed successfully: paymentId={}, status={}", payment.getId(), payment.getStatus());

            responseObserver.onNext(PaymentResponse.newBuilder()
                    .setPaymentId(payment.getId())
                    .setStatus(payment.getStatus().name())
                    .setMessage("Payment successful")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Payment processing failed: userId={}, itemId={}, amount={}, error={}",
                    userId, itemId, amount, e.getMessage(), e);

            responseObserver.onNext(PaymentResponse.newBuilder()
                    .setStatus("FAILED")
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }
}