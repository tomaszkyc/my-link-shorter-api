package com.linkshorter.app.features.payment.controller;

import com.google.gson.Gson;
import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.model.PaymentStatus;
import com.linkshorter.app.features.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final Gson gson;
    private final PaymentService paymentService;

    public PaymentController(Gson gson, PaymentService paymentService) {
        this.gson = gson;
        this.paymentService = paymentService;
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin', 'premium-user')")
    public ResponseEntity<Boolean> updatePaymentStatus(@RequestBody Payment payment) {
        ResponseEntity<Boolean> responseEntity;
        try {
            UUID paymentId = payment.getId();
            PaymentStatus paymentStatus = payment.getPaymentStatus();
            paymentService.updatePaymentStatus(paymentId, paymentStatus);
            responseEntity = ResponseEntity.ok(Boolean.TRUE);
        } catch (Exception exception) {
            String exceptionMessage = "There was an error during changing payment" + exception.getMessage();
            log.error(exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(Boolean.FALSE);
        }
        return responseEntity;
    }

}
