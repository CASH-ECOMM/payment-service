package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Address;
import com.ecommerce.payment.entity.CreditCardInfo;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.Receipt;
import com.ecommerce.payment.grpc.*;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.ReceiptRepository;
import com.ecommerce.payment.util.CreditCardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;



@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final CreditCardValidator creditCardValidator;

    @Value("${payment.hst.rate:0.13}")
    private double hstRate;

    @Value("${payment.shipping.expedited.surcharge:10}")
    private int expeditedSurcharge;

    private static final int SCALE = 2;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private static BigDecimal money(int wholeDollars) {
        return BigDecimal.valueOf(wholeDollars).setScale(SCALE, ROUND);
    }
    private static BigDecimal money(double v) {
        return BigDecimal.valueOf(v).setScale(SCALE, ROUND);
    }
    private static BigDecimal money(BigDecimal v) {
        return v.setScale(SCALE, ROUND);
    }
    //    Helper for throw error for number
    private int coerceStreetNumber(int raw) {
        if (raw <= 0) {
            throw new IllegalArgumentException("Street number must be a positive integer.");
        }

        if (raw > 999999) {
            throw new IllegalArgumentException("Street number is too large.");
        }
        return raw;
    }
    /**
     * Process payment request - Main business logic for Use Case 5
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for user: {} and item: {}",
                request.getUserInfo().getUserId(), request.getItemId());

        try {
            // Validate credit card information
            CreditCardValidator.ValidationResult validationResult = validateCreditCard(
                    request.getCreditCardInfo()
            );

            if (!validationResult.isValid()) {
                log.error("Credit card validation failed: {}", validationResult.getErrors());
                return buildErrorResponse("Payment validation failed: " + validationResult.getErrors());
            }


            // Calculate total cost
//            double itemCost = request.getItemCost();
//            double shippingCost = calculateShippingCost(request.getShippingInfo());
//            double hstAmount = (itemCost + shippingCost) * hstRate;
//            double totalAmount = itemCost + shippingCost + hstAmount;

            // ints from proto (whole dollars)
            int itemCostInt     = request.getItemCost();                 // already int32
            int shippingCostInt = calculateShippingCost(request.getShippingInfo()); // returns int

            // precise tax math with BigDecimal
            BigDecimal itemBD     = money(itemCostInt);
            BigDecimal shippingBD = money(shippingCostInt);
            BigDecimal subTotalBD = itemBD.add(shippingBD);
            BigDecimal hstBD      = subTotalBD.multiply(BigDecimal.valueOf(hstRate)).setScale(2, ROUND);
            BigDecimal totalBD    = subTotalBD.add(hstBD).setScale(2, ROUND);

            double hstAmountOut   = hstBD.doubleValue();
            double totalAmountOut = totalBD.doubleValue();
            log.debug("Payment calculation - Item: ${}, Shipping: ${}, HST: ${}, Total: ${}",
                    itemBD, shippingBD, hstBD, totalBD);

            // Create and save payment entity
            Payment payment = createPaymentEntity(
                    request, itemCostInt, shippingCostInt, hstAmountOut, totalAmountOut);
            Payment savedPayment = paymentRepository.save(payment);

            log.info("Payment saved successfully with ID: {}", savedPayment.getPaymentId());

            // Simulate payment processing (mock validation)
            boolean paymentSuccess = simulatePaymentProcessing(savedPayment);

            if (!paymentSuccess) {
                savedPayment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                savedPayment.setPaymentErrorMessage("Payment processing failed");
                paymentRepository.save(savedPayment);
                return buildErrorResponse("Payment processing failed. Please try again.");
            }

            // Update payment status to completed
            savedPayment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            savedPayment.setTransactionReference("TXN-" + System.currentTimeMillis());
            paymentRepository.save(savedPayment);

            // Generate receipt
            Receipt receipt = createReceipt(savedPayment);
            Receipt savedReceipt = receiptRepository.save(receipt);

            log.info("Receipt generated successfully with ID: {}", savedReceipt.getReceiptId());

            // Build and return success response
            return buildSuccessResponse(savedPayment, savedReceipt);

        } catch (Exception e) {
            log.error("Error processing payment", e);
            return buildErrorResponse("An error occurred while processing your payment: " + e.getMessage());
        }
    }

    /**
     * Get payment by ID
     */
    public PaymentResponse getPaymentById(String paymentId) {
        log.info("Retrieving payment with ID: {}", paymentId);

        return paymentRepository.findByPaymentId(paymentId)
                .map(payment -> {
                    Receipt receipt = receiptRepository.findByPaymentId(paymentId).orElse(null);
                    return buildSuccessResponse(payment, receipt);
                })
                .orElseGet(() -> buildErrorResponse("Payment not found with ID: " + paymentId));
    }

    /**
     * Get payment history for a user
     */
    public List<PaymentResponse> getPaymentHistory(String userId, int page, int size) {
        log.info("Retrieving payment history for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> paymentsPage = paymentRepository.findByUserId(userId, pageable);

        return paymentsPage.getContent().stream()
                .map(payment -> {
                    Receipt receipt = receiptRepository.findByPaymentId(payment.getPaymentId()).orElse(null);
                    return buildSuccessResponse(payment, receipt);
                })
                .collect(Collectors.toList());
    }

    /**
     * Validate credit card information
     */
    private CreditCardValidator.ValidationResult validateCreditCard(
            com.ecommerce.payment.grpc.CreditCardInfo creditCardInfoProto) {

        return creditCardValidator.validateCreditCard(
                creditCardInfoProto.getCardNumber(),
                creditCardInfoProto.getNameOnCard(),
                creditCardInfoProto.getExpiryDate(),
                creditCardInfoProto.getSecurityCode()
        );
    }

    /**
     * Calculate shipping cost with surcharge for expedited shipping
     */
    private int calculateShippingCost(ShippingInfo shippingInfo) {
        int baseCost = shippingInfo.getShippingCost(); // proto int32 dollars
        if (shippingInfo.getShippingType() == ShippingType.EXPEDITED) {
            return baseCost + expeditedSurcharge;      // int dollars
        }
        return baseCost;
    }

    /**
     * Create payment entity from request
     */
    private Payment createPaymentEntity(
            PaymentRequest request,
            int itemCostInt,
            int shippingCostInt,
            double hstAmount,
            double totalAmount
    ) {
        int streetNumber = coerceStreetNumber(request.getUserInfo().getNumber());

        Address address = Address.builder()
                .firstName(request.getUserInfo().getFirstName())
                .lastName(request.getUserInfo().getLastName())
                .street(request.getUserInfo().getStreet())
                .number(streetNumber)
                .province(request.getUserInfo().getProvince())
                .country(request.getUserInfo().getCountry())
                .postalCode(request.getUserInfo().getPostalCode())
                .build();

        CreditCardInfo cardInfo = new CreditCardInfo();
        cardInfo.setMaskedCardNumber(request.getCreditCardInfo().getCardNumber());
        cardInfo.setNameOnCard(request.getCreditCardInfo().getNameOnCard());
        cardInfo.setExpiryDate(request.getCreditCardInfo().getExpiryDate());

        return Payment.builder()
                .userId(request.getUserInfo().getUserId())
                .itemId(request.getItemId())
                .itemCost(itemCostInt)          // Integer in entity
                .shippingCost(shippingCostInt)  // Integer in entity
                .shippingType(request.getShippingInfo().getShippingType() == ShippingType.EXPEDITED
                        ? Payment.ShippingType.EXPEDITED : Payment.ShippingType.REGULAR)
                .estimatedShippingDays(request.getShippingInfo().getEstimatedDays())
                .hstAmount(hstAmount)           // double (2dp)
                .totalAmount(totalAmount)       // double (2dp)
                .paymentStatus(Payment.PaymentStatus.PROCESSING)
                .address(address)
                .creditCardInfo(cardInfo)
                .build();
    }

    /**
     * Simulate payment processing (mock)
     */
    private boolean simulatePaymentProcessing(Payment payment) {
        log.info("Simulating payment processing for payment ID: {}", payment.getPaymentId());

        // In a real system, this would integrate with a payment gateway
        // For now, we'll just simulate success
        try {
            Thread.sleep(500); // Simulate processing delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true; // Mock success
    }

    /**
     * Create receipt from payment
     */
    private Receipt createReceipt(Payment payment) {
        double itemDisplay = money(payment.getItemCost()).doubleValue();   // 2-dp
        double shipDisplay = money(payment.getShippingCost()).doubleValue();

        return Receipt.builder()
                .payment(payment)
                .customerName(payment.getAddress().getFirstName() + " " + payment.getAddress().getLastName())
                .customerAddress(payment.getAddress().getFormattedAddress())
                .itemId(payment.getItemId())
                .itemCost(payment.getItemCost())          // keep as double (2dp)
                .shippingCost(payment.getShippingCost())      // keep as double (2dp)
                .hstAmount(payment.getHstAmount())
                .totalPaid(payment.getTotalAmount())
                .paymentMethod(payment.getCreditCardInfo().getCardType())
                .shippingEstimateDays(payment.getEstimatedShippingDays())
                .build();
    }

    /**
     * Build success response with receipt info
     */
    private PaymentResponse buildSuccessResponse(Payment payment, Receipt receipt) {
        PaymentResponse.Builder responseBuilder = PaymentResponse.newBuilder()
                .setSuccess(true)
                .setPaymentId(payment.getPaymentId())
                .setMessage("Payment processed successfully")
                .setTransactionDate(payment.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));

        if (receipt != null) {
            double itemDisplay = money(payment.getItemCost()).doubleValue();
            double shipDisplay = money(payment.getShippingCost()).doubleValue();

            ReceiptInfo receiptInfo = ReceiptInfo.newBuilder()
                    .setReceiptId(receipt.getReceiptId())
                    .setFirstName(payment.getAddress().getFirstName())
                    .setLastName(payment.getAddress().getLastName())
                    .setFullAddress(payment.getAddress().getFullAddress())
                    .setItemCost(payment.getItemCost())
                    .setShippingCost(payment.getShippingCost())
                    .setHstAmount(payment.getHstAmount())
                    .setTotalPaid(payment.getTotalAmount())
                    .setItemId(payment.getItemId())
                    .build();

            String shippingMessage = String.format("The item will be shipped in %d days",
                    payment.getEstimatedShippingDays());

            responseBuilder.setReceiptInfo(receiptInfo)
                    .setShippingMessage(shippingMessage);
        }

        return responseBuilder.build();
    }

    /**
     * Build error response
     */
    private PaymentResponse buildErrorResponse(String errorMessage) {
        return PaymentResponse.newBuilder()
                .setSuccess(false)
                .setMessage(errorMessage)
                .build();
    }
}
