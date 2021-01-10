package com.linkshorter.app.features.payment.service;

import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.model.PaymentStatus;
import com.linkshorter.app.features.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;
    private Payment payment;

    @BeforeEach
    void setUp() {
        initPaymentService();
        initPayment();
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    private void initPaymentService() {
        if (paymentService == null) {
            paymentService = new PaymentService(paymentRepository);
        }
    }

    private void initPayment() {
        payment = Payment.builder().paymentStatus(PaymentStatus.CREATED).amount(BigDecimal.ZERO)
                .paymentDescription("Some description").build();
    }

    @Test
    public void shouldChangePaymentStatus() {
        payment = paymentRepository.save(payment);
        try {
            PaymentStatus newPaymentStatus = PaymentStatus.PAID;
            Payment paymentAfterUpdate = paymentService.updatePaymentStatus(payment.getId(), newPaymentStatus);

            assertNotNull(paymentAfterUpdate);
            assertEquals(newPaymentStatus, paymentAfterUpdate.getPaymentStatus());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldNotChangePaymentStatusForNullablePaymentId() {
        assertThrows(Exception.class, () -> paymentService.updatePaymentStatus(null, PaymentStatus.PAID));
    }
    
    @Test
    public void shouldNotChangePaymentStatusForNUllablePaymentStatus() {
        assertThrows(Exception.class, () -> paymentService.updatePaymentStatus(payment.getId(), null));
    }
}