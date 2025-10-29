//package com.paymentservice.service;
//
//import com.paymentservice.exception.PaymentValidationException;
//import com.paymentservice.model.Payment;
//import com.paymentservice.model.PaymentStatus;
//import com.paymentservice.repository.PaymentRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
///**
// * {@code PaymentServiceImpl} implements {@link PaymentService} and handles
// * the core business logic for processing and validating payments.
// *
// * <p>Updated to include support for shipping cost, expedited shipping,
// * and total amount calculation.</p>
// */
//@Service
//public class PaymentServiceImpl implements PaymentService {
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Autowired
//    private PaymentValidationService paymentValidationService;
//
//    /**
//     * Processes a payment by validating eligibility, checking credit card fields,
//     * masking sensitive data, and persisting the result.
//     *
//     * @param userId           the user’s ID
//     * @param itemId           the item being purchased
//     * @param itemPrice        base item price
//     * @param shippingCost     additional shipping fee
//     * @param expedited        true if expedited shipping is selected
//     * @param cardNumber       the full (simulated) card number
//     * @param cardHolderName   the name on the card
//     * @param expirationDate   the card expiration date
//     * @param cvv              the card’s CVV code
//     * @return the saved {@link Payment} entity
//     */
//    @Override
//    public Payment processPayment(Long userId, Long itemId, Double itemPrice,
//                                  Double shippingCost, Boolean expedited,
//                                  String cardNumber, String cardHolderName,
//                                  String expirationDate, String cvv,
//                                  String shippingFirstName, String shippingLastName,
//                                  String street, String number, String province,
//                                  String country, String postalCode) {
//
//        // Validate eligibility
//        if (!paymentValidationService.isValidForPayment(itemId)) {
//            throw new IllegalStateException("Payment already completed for this item.");
//        }
//        if (itemPrice == null || itemPrice <= 0) {
//            throw new PaymentValidationException("Payment amount must be greater than zero.");
//        }
//
//        boolean duplicate = paymentRepository.existsByUserIdAndItemId(userId, itemId);
//        if (duplicate) {
//            throw new PaymentValidationException("Duplicate payment: this user already paid for this item.");
//        }
//
//        //Validate credit card fields
//        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
//            throw new IllegalArgumentException("Invalid card number.");
//        }
//        if (cvv == null || cvv.length() < 3 || cvv.length() > 4) {
//            throw new IllegalArgumentException("Invalid CVV.");
//        }
//        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
//            throw new IllegalArgumentException("Cardholder name is required.");
//        }
//        if (expirationDate == null || expirationDate.trim().isEmpty()) {
//            throw new IllegalArgumentException("Expiration date is required.");
//        }
//
//        //Calculate total
//        double totalAmountPaid = itemPrice + (shippingCost != null ? shippingCost : 0.0);
//
//        // Mask sensitive card data
//        String maskedCard = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
//        String last4 = cardNumber.substring(cardNumber.length() - 4);
//
//        System.out.printf(
//                "Shipping to: %s %s, %s %s, %s, %s, %s%n",
//                shippingFirstName, shippingLastName, street, number, province, country, postalCode
//        );
//
//        System.out.printf(
//                "Processing payment for item %d (expedited=%s) with card ending %s, total=%.2f%n",
//                itemId, expedited, last4, totalAmountPaid
//        );
//        Payment payment = new Payment();
//        payment.setUserId(userId);
//        payment.setItemId(itemId);
//        payment.setAmount(itemPrice);
//        payment.setShippingCost(shippingCost);
//        payment.setExpeditedShipping(expedited != null ? expedited : false);
//        payment.setCardLast4(last4);
//        payment.setCardHolderName(cardHolderName);
//        payment.setStatus(PaymentStatus.COMPLETED);
//        payment.setCreatedAt(LocalDateTime.now());
//        payment.setShippingFirstName(shippingFirstName);
//        payment.setShippingLastName(shippingLastName);
//        payment.setStreet(street);
//        payment.setNumber(number);
//        payment.setProvince(province);
//        payment.setCountry(country);
//        payment.setPostalCode(postalCode);
//        payment.setTotalAmountPaid(totalAmountPaid);
//        payment.setTransactionId(UUID.randomUUID().toString());
//
//
//
//
//
//        //  Save and return
//        return paymentRepository.save(payment);
//    }
//}