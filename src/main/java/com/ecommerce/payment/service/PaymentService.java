//package com.paymentservice.service;
//
//import com.paymentservice.model.Payment;
//
///**
// * {@code PaymentService} defines the contract for handling payment operations.
// *
// * <p>Updated to support additional fields such as shipping cost, expedited shipping,
// * and full shipping address, ensuring total payment calculation and shipping info
// * align with the gRPC PaymentRequest schema.</p>
// *
// * <h2>Responsibilities:</h2>
// * <ul>
// *   <li>Validate payment eligibility and prevent duplicate payments.</li>
// *   <li>Simulate credit card payment processing.</li>
// *   <li>Include shipping details in total payment computation and persistence.</li>
// *   <li>Persist successfully processed payments.</li>
// * </ul>
// *
// * <p><b>Author:</b> Erfan YousefMoumji</p>
// * <p><b>Date:</b> Oct 28, 2025</p>
// */
//public interface PaymentService {
//
//    /**
//     * Processes a payment for a given user and item, including shipping address.
//     *
//     * @param userId            the ID of the user making the payment
//     * @param itemId            the ID of the item being paid for
//     * @param itemPrice         base item price
//     * @param shippingCost      shipping cost applied to this order
//     * @param expedited         whether expedited shipping was selected
//     * @param cardNumber        the credit card number (simulated)
//     * @param cardHolderName    the cardholder's name
//     * @param expiryDate        the card expiration date (MM/YY)
//     * @param cvv               the card’s security code
//     * @param shippingFirstName first name of the shipping recipient
//     * @param shippingLastName  last name of the shipping recipient
//     * @param street            street address
//     * @param number            house/unit number
//     * @param province          province or state
//     * @param country           country
//     * @param postalCode        postal/ZIP code
//     * @return a {@link Payment} entity if processing succeeds
//     * @throws IllegalStateException if payment is not valid for processing
//     */
//    Payment processPayment(Long userId,
//                           Long itemId,
//                           Double itemPrice,
//                           Double shippingCost,
//                           Boolean expedited,
//                           String cardNumber,
//                           String cardHolderName,
//                           String expiryDate,
//                           String cvv,
//                           String shippingFirstName,
//                           String shippingLastName,
//                           String street,
//                           String number,
//                           String province,
//                           String country,
//                           String postalCode);
//
//
//}