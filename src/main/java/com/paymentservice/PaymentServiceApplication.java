package com.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * PaymentServiceApplication
 * Location: com.paymentservice.PaymentServiceApplication.java
 *
 * Overview:
 * This is the main entry point for the Payment Microservice in the Auction System.
 * It bootstraps the Spring Boot application and initializes all configurations,
 * beans, and component scanning for the service.
 *
 * Purpose:
 * - Starts the Payment microservice as a standalone Spring Boot application.
 * - Enables component scanning and autoconfiguration via @SpringBootApplication.
 *
 * Context:
 * - Acts as the gateway to service logic including gRPC server, database access,
 *   business logic services, and REST/gRPC endpoints.
 *
 * Usage:
 * - Run this class to launch the Payment Microservice independently or as part of
 *   a larger system using microservice orchestration (e.g., Docker, Kubernetes).
 *
 * Author: Erfan YousefMoumji
 * Date: Oct 24, 2025
 */

@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
