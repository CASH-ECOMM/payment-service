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

    @Value("${payment.shipping.expedited.surcharge:10.0}")
    private double expeditedSurcharge;

    private static final int SCALE = 2;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    /** Convert a primitive amount to 2-dp BigDecimal (HALF_UP). */
    private static BigDecimal money(double v) {
        return BigDecimal.valueOf(v).setScale(SCALE, ROUND);
    }

    /** Ensure an existing BigDecimal is 2-dp (HALF_UP). */
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

            BigDecimal itemCostBD     = money(request.getItemCost());
            BigDecimal shippingCostBD = money(calculateShippingCost(request.getShippingInfo()));
            BigDecimal subTotalBD     = itemCostBD.add(shippingCostBD);
            BigDecimal hstRateBD      = BigDecimal.valueOf(hstRate);
            BigDecimal hstAmountBD    = money(subTotalBD.multiply(hstRateBD));
            BigDecimal totalAmountBD  = money(subTotalBD.add(hstAmountBD));

            double itemCost    = itemCostBD.doubleValue();
            double shippingCost= shippingCostBD.doubleValue();
            double hstAmount   = hstAmountBD.doubleValue();
            double totalAmount = totalAmountBD.doubleValue();

            log.debug("Payment calculation - Item: ${}, Shipping: ${}, HST: ${}, Total: ${}",
                    itemCost, shippingCost, hstAmount, totalAmount);

            // Create and save payment entity
            Payment payment = createPaymentEntity(request, shippingCost, hstAmount, totalAmount);
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
    private double calculateShippingCost(ShippingInfo shippingInfo) {
        double baseCost = shippingInfo.getShippingCost();

        if (shippingInfo.getShippingType() == ShippingType.EXPEDITED) {
            return baseCost + expeditedSurcharge;
        }

        return baseCost;
    }

    /**
     * Create payment entity from request
     */
    private Payment createPaymentEntity(PaymentRequest request, double shippingCost,
                                        double hstAmount, double totalAmount) {
        int streetNumber = coerceStreetNumber(request.getUserInfo().getNumber());
        // Create address
        Address address = Address.builder()
                .firstName(request.getUserInfo().getFirstName())
                .lastName(request.getUserInfo().getLastName())
                .street(request.getUserInfo().getStreet())
                .number(streetNumber)
                .province(request.getUserInfo().getProvince())
                .country(request.getUserInfo().getCountry())
                .postalCode(request.getUserInfo().getPostalCode())
                .build();

        // Create credit card info (masked)
        CreditCardInfo cardInfo = new CreditCardInfo();
        cardInfo.setMaskedCardNumber(request.getCreditCardInfo().getCardNumber());
        cardInfo.setNameOnCard(request.getCreditCardInfo().getNameOnCard());
        cardInfo.setExpiryDate(request.getCreditCardInfo().getExpiryDate());

        // Create payment
        return Payment.builder()
                .userId(request.getUserInfo().getUserId())
                .itemId(request.getItemId())
                .itemCost(request.getItemCost())
                .shippingCost(shippingCost)
                .shippingType(request.getShippingInfo().getShippingType() == ShippingType.EXPEDITED
                        ? Payment.ShippingType.EXPEDITED : Payment.ShippingType.REGULAR)
                .estimatedShippingDays(request.getShippingInfo().getEstimatedDays())
                .hstAmount(hstAmount)
                .totalAmount(totalAmount)
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
        return Receipt.builder()
                .payment(payment)
                .customerName(payment.getAddress().getFirstName() + " " + payment.getAddress().getLastName())
                .customerAddress(payment.getAddress().getFormattedAddress())
                .itemId(payment.getItemId())
                .itemCost(payment.getItemCost())
                .shippingCost(payment.getShippingCost())
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
