package com.linkshorter.app.features.payment.controller;

import com.google.gson.Gson;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.service.UserService;
import com.linkshorter.app.features.links.controller.LinkActivityController;
import com.linkshorter.app.features.payment.model.Invoice;
import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.provider.service.PaymentProviderService;
import com.linkshorter.app.features.payment.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoice")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);
    private final InvoiceService invoiceService;
    private final PaymentProviderService paymentProviderService;
    private final UserService userService;
    private final Gson gson;

    public InvoiceController(InvoiceService invoiceService, Gson gson,
                             UserService userService, PaymentProviderService paymentProviderService) {
        this.invoiceService = invoiceService;
        this.gson = gson;
        this.userService = userService;
        this.paymentProviderService = paymentProviderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'premium-user')")
    public ResponseEntity<List<Invoice>> findAll(@AuthenticationPrincipal User user) {
        ResponseEntity<List<Invoice>> responseEntity;
        List<Invoice> invoices = invoiceService.findAll(user);
        responseEntity = ResponseEntity.ok(invoices);
        return responseEntity;
    }

    @PostMapping("/{userIdConnectedWithInvoice}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> createInvoice(@RequestBody Invoice invoice, @PathVariable(value = "userIdConnectedWithInvoice") UUID userIdConnectedWithInvoice) {
        ResponseEntity<?> responseEntity;
        try {
            User userConnectedWithInvoice = userService.findById(userIdConnectedWithInvoice).orElse(null);
            invoice.setUser(userConnectedWithInvoice);
            invoice = invoiceService.createInvoice(invoice);
            responseEntity = ResponseEntity.ok(gson.toJson("Faktura utworzona poprawnie"));
        } catch (Exception creatingInvoiceException) {
            String exceptionMessage = "Wystąpił błąd podczas tworzenia faktury. Spróbuj ponownie później";
            log.error(exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionMessage));
        }
        return responseEntity;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'premium-user')")
    public ResponseEntity<?> findInvoice(@PathVariable(value = "id") UUID invoiceId) {
        ResponseEntity<?> responseEntity;
        try {
            Invoice invoice = invoiceService.findById(invoiceId).orElseThrow(() -> new Exception("Can't find invoice with id: " + invoiceId));
            responseEntity = ResponseEntity.ok(invoice);
        } catch (Exception findInvoiceByIdException) {
            String exceptionMessage = "There was an error during finding invoice with id: " + invoiceId;
            log.error(exceptionMessage);
            String jsonFormattedExceptionMessage = gson.toJson(exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(jsonFormattedExceptionMessage);
        }
        return responseEntity;
    }

    @GetMapping("/{id}/payment-link")
    @PreAuthorize("hasAnyAuthority('admin', 'premium-user')")
    public ResponseEntity<String> getPaymentLink(@PathVariable(value = "id") UUID invoiceId) {
        ResponseEntity<String> responseEntity;
        try {
            Invoice invoice = invoiceService.findById(invoiceId).orElseThrow(() -> new Exception("Can't find invoice with id: " + invoiceId));
            Payment payment = invoice.getPayment();
            String paymentLink = paymentProviderService.createPaymentLink(payment);
            responseEntity = ResponseEntity.ok(gson.toJson(paymentLink));
        } catch (Exception fetchingPaymentLinkException) {
            String exceptionMessage = "There was an error during getting payment link: " + fetchingPaymentLinkException.getMessage();
            String jsonFormattedExceptionMessage = gson.toJson(exceptionMessage);
            log.error(exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(jsonFormattedExceptionMessage);
        }
        return responseEntity;
    }
}
