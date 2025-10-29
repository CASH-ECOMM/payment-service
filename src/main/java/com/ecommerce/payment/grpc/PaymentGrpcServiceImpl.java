//package com.paymentservice.grpc;
//import com.paymentservice.grpc.ShippingAddress;
//import com.paymentservice.model.Payment;
//import com.paymentservice.model.PaymentStatus;
//import com.paymentservice.repository.PaymentRepository;
//import com.paymentservice.service.PaymentService;
//import io.grpc.stub.StreamObserver;
//import net.devh.boot.grpc.server.service.GrpcService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Optional;
//import java.util.UUID;
//
///**
// * {@code PaymentGrpcServiceImpl} provides the gRPC interface for handling payment operations.
// *
// * Updated to handle shipping cost, expedited shipping, total payment, and receipt generation.
// */
//@GrpcService
//public class PaymentGrpcServiceImpl extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {
//
//    private static final Logger logger = LoggerFactory.getLogger(PaymentGrpcServiceImpl.class);
//
//    @Autowired
//    private final PaymentService paymentService;
//
//    public PaymentGrpcServiceImpl(PaymentService paymentService) {
//        this.paymentService = paymentService;
//    }
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Override
//    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
//        Long userId = request.getUserId();
//        Long itemId = request.getItemId();
//        double itemPrice = request.getAmount();
//        double shippingCost = request.getShippingCost();
//        boolean expedited = request.getExpeditedShipping();
//        String cardNumber = request.getCardNumber();
//        String cardHolderName = request.getCardHolderName();
//        String expirationDate = request.getExpirationDate();
//        String cvv = request.getCvv();
//
//        ShippingAddress address = request.getShippingAddress();
//        String shippingFirstName = address.getFirstName();
//        String shippingLastName = address.getLastName();
//        String street = address.getStreet();
//        String number = address.getNumber();
//        String province = address.getProvince();
//        String country = address.getCountry();
//        String postalCode = address.getPostalCode();
//
//        logger.info("Received gRPC payment request: userId={}, itemId={}, amount={}, shipping={}, expedited={}, cardHolder={}",
//                userId, itemId, itemPrice, shippingCost, expedited, cardHolderName);
//
//        try {
//            //Validate mandatory card fields
//            if (cardNumber.isEmpty() || cardHolderName.isEmpty() || expirationDate.isEmpty() || cvv.isEmpty()) {
//                responseObserver.onNext(PaymentResponse.newBuilder()
//                        .setStatus("FAILED")
//                        .setMessage("All credit card fields are required.")
//                        .setTotalAmountPaid(0.0)
//                        .build());
//                responseObserver.onCompleted();
//                return;
//            }
//
//            // ✅ Prevent duplicate completed payments
//            Optional<Payment> existing = paymentRepository
//                    .findByUserIdAndItemIdAndStatus(userId, itemId, PaymentStatus.COMPLETED);
//
//            if (existing.isPresent()) {
//                logger.warn("Duplicate payment attempt detected for userId={} and itemId={}", userId, itemId);
//
//                responseObserver.onNext(PaymentResponse.newBuilder()
//                        .setStatus("DUPLICATE")
//                        .setMessage("Payment already completed for this user and item.")
//                        .setTotalAmountPaid(existing.get().getAmount())
//                        .setTransactionId("N/A")
//                        .setReceiptUrl("N/A")
//                        .build());
//                responseObserver.onCompleted();
//                return;
//            }
//
//            //Process new valid payment
//            Payment payment = paymentService.processPayment(
//                    userId,
//                    itemId,
//                    itemPrice,
//                    shippingCost,
//                    expedited,
//                    cardNumber,
//                    cardHolderName,
//                    expirationDate,
//                    cvv,
//                    shippingFirstName,
//                    shippingLastName,
//                    street,
//                    number,
//                    province,
//                    country,
//                    postalCode
//            );
//            //Compute total cost (item + shipping)
//            double totalAmountPaid = itemPrice + shippingCost;
//
//            //Generate transaction details
//            String transactionId = UUID.randomUUID().toString();
//            String receiptUrl = "https://payments.yourdomain.com/receipts/" + transactionId;
//
//            logger.info("Payment processed successfully: paymentId={}, total=${}, transactionId={}",
//                    payment.getId(), totalAmountPaid, transactionId);
//
//            //Send success response
//            PaymentResponse response = PaymentResponse.newBuilder()
//                    .setPaymentId(payment.getId())
//                    .setStatus(PaymentStatus(payment.getStatus()))
//                    .setCreatedAt(payment.getCreatedAt().toString())
//                    .setCardLast4(payment.getCardLast4())
//                    .setCardHolderName(payment.getCardHolderName())
//                    .setTotalAmountPaid(payment.getTotalAmountPaid())
//                    .setTransactionId(payment.getTransactionId())
//                    .build();
//
//            responseObserver.onNext(response);
//            responseObserver.onCompleted();
//
//        } catch (Exception e) {
//            logger.error("Payment processing failed: userId={}, itemId={}, amount={}, error={}",
//                    userId, itemId, itemPrice, e.getMessage(), e);
//
//            responseObserver.onNext(PaymentResponse.newBuilder()
//                    .setStatus("FAILED")
//                    .setMessage("Payment error: " + e.getMessage())
//                    .setTotalAmountPaid(0.0)
//                    .build());
//            responseObserver.onCompleted();
//        }
//    }
//}