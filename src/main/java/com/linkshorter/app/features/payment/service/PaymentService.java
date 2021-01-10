package com.linkshorter.app.features.payment.service;

import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.model.PaymentStatus;
import com.linkshorter.app.features.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment updatePaymentStatus(UUID paymentId, PaymentStatus newPaymentStatus) throws Exception {
        Objects.requireNonNull(paymentId, "Given payment id is null");
        Objects.requireNonNull(newPaymentStatus, "Given payment status is null");
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            throw new Exception("Payment with id: " + paymentId + " doesnt exists");
        }
        Payment payment = optionalPayment.get();
        payment.setPaymentStatus(newPaymentStatus);
        return paymentRepository.save(payment);
    }
}
