package com.paymentservice.service;

import com.paymentservice.model.Payment;
import com.paymentservice.model.PaymentStatus;
import com.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidationService paymentValidationService;

    @Override
    public Payment processPayment(Long userId, Long itemId, Double amount) {
        boolean isValid = paymentValidationService.isValidForPayment(itemId);

        if (!isValid) {
            throw new IllegalStateException("Payment already completed for this item.");
        }

        Payment payment = new Payment(
                userId,
                itemId,
                amount,
                PaymentStatus.COMPLETED,
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }
}