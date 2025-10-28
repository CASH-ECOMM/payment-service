package com.paymentservice.controller;

import com.paymentservice.dto.PaymentRestRequest;
import com.paymentservice.model.Payment;
import com.paymentservice.repository.PaymentRepository;
import com.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.paymentservice.exception.PaymentValidationException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Debug-only REST controller to manually test or inspect payment data.
 * Not used in production, mainly for Swagger UI and manual testing.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentDebugController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    // GET: All Payments
    @Operation(summary = "Get all payments in the database")
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // GET: Payment by ID
    @Operation(summary = "Get a specific payment by its ID")
    @GetMapping("/{id}")
    public Optional<Payment> getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id);
    }

    // POST: Create Payment (Manual)
    @Operation(
            summary = "Create a new payment manually (REST version)",
            description = "Submits a manual payment request to the Payment Service. " +
                    "Use this endpoint for testing payment creation directly through Swagger.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details (user, item, card, amount)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PaymentRestRequest.class),
                            examples = @ExampleObject(
                                    name = "Sample Payment",
                                    summary = "Example of a valid payment request",
                                    value = """
                {
                  "userId": 101,
                  "itemId": 555,
                  "amount": 49.99,
                  "cardNumber": "4111111111111111",
                  "cardHolderName": "John Doe",
                  "expirationDate": "12/26",
                  "cvv": "123"
                }
                """
                            )
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Payment created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Payment.class),
                    examples = @ExampleObject(
                            name = "Successful Payment",
                            value = """
            {
              "id": 1,
              "userId": 101,
              "itemId": 555,
              "amount": 49.99,
              "status": "COMPLETED",
              "cardHolderName": "John Doe",
              "cardLast4": "1111",
              "createdAt": "2025-10-28T14:30:00"
            }
            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request due to invalid payment",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "Missing Card Number", value = "{\"status\":\"FAILED\", \"message\":\"Invalid card number.\"}"),
                            @ExampleObject(name = "Missing Expiration Date", value = "{\"status\":\"FAILED\", \"message\":\"Expiration date is required.\"}"),
                            @ExampleObject(name = "Negative Amount", value = "{\"status\":\"FAILED\", \"message\":\"Payment amount must be greater than zero.\"}"),
                            @ExampleObject(name = "Duplicate Payment", value = "{\"status\":\"FAILED\", \"message\":\"Duplicate payment: this user already paid for this item.\"}")
                    }
            )
    )
    @PostMapping
    public Payment createPaymentManually(
            @org.springframework.web.bind.annotation.RequestBody PaymentRestRequest request
    ) {
        return paymentService.processPayment(
                request.getUserId(),
                request.getItemId(),
                request.getAmount(),
                request.getCardNumber(),
                request.getCardHolderName(),
                request.getExpirationDate(),
                request.getCvv()
        );
    }

    @Operation(summary = "Delete payments: all or by specific ID")
    @DeleteMapping
    public ResponseEntity<String> deletePayments(
            @RequestParam(required = false) Boolean allpayments,
            @RequestParam(required = false) Long id
    ) {
        // Delete all
        if (Boolean.TRUE.equals(allpayments)) {
            paymentRepository.deleteAll();
            return ResponseEntity.ok("All payments deleted.");
        }

        // Delete by ID
        if (id != null) {
            if (paymentRepository.existsById(id)) {
                paymentRepository.deleteById(id);
                return ResponseEntity.ok("Payment with ID " + id + " deleted.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Payment with ID " + id + " not found.");
            }
        }

        // Invalid input
        return ResponseEntity.badRequest().body("Please specify either 'allpayments=true' or 'id={value}'.");
    }

    /**
     * Handles validation-related errors thrown from PaymentServiceImpl.
     * Returns a clean JSON response with a clear error message for Swagger testing.
     */
    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<?> handleValidationException(PaymentValidationException ex) {
        return ResponseEntity
                .badRequest()
                .body(
                        java.util.Map.of(
                                "status", "FAILED",
                                "message", ex.getMessage()
                        )
                );
    }
}